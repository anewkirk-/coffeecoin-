package coffeecoin.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import coffeecoin.network.BlockMinedAction;
import coffeecoin.network.TxAction;
import coffeecoin.network.UpdateAction;

/**
 * This class contains methods for CRUD operations on the blockchain db
 * 
 * @author
 */
public class BlockchainDbManager extends DbManager {

	private static BlockchainDbManager instance = null;

	private BlockchainDbManager() {
	}

	/**
	 * This is a singleton class
	 */
	public synchronized static BlockchainDbManager getInstance() {
		if (instance == null) {
			instance = new BlockchainDbManager();
		}
		return instance;
	}

	/**
	 * Returns total balance on a given address. This is checked by first
	 * subtracting the amounts of all transactions with the address as their
	 * input, THEN adding the amounts of all transactions with the address as
	 * their output.
	 */
	public synchronized long checkBalance(String address)
			throws SqlJetException {
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable transactions = db.getTable("transactions");
		ISqlJetCursor cursor = transactions.lookup("input_index", address);
		long balance = 0;
		if (!cursor.eof()) {
			do {
				if (cursor.getString("input").equals(address)) {
					balance -= cursor.getInteger("amt");
				}
			} while (cursor.next());
		}
		cursor.close();
		cursor = transactions.lookup("output_index", address);
		if (!cursor.eof()) {
			do {
				if (cursor.getString("output").equals(address)) {
					balance += cursor.getInteger("amt");
				}
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return balance;
	}

	/**
	 * Adds a transaction to the transactions table. This method does NOT verify
	 * the tx before adding it. Tx must be verified before passing it into this
	 * method.
	 */
	public synchronized void addTx(TxAction currentTransaction)
			throws SqlJetException {
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, true);
		db.beginTransaction(SqlJetTransactionMode.EXCLUSIVE);
		ISqlJetTable txTable = db.getTable("transactions");
		String input = currentTransaction.getInput();
		long amt = currentTransaction.getAmt();
		String output = currentTransaction.getOutput();
		String publickey = currentTransaction.getPublicKey();
		String signature = currentTransaction.getSignature();
		long timestamp = currentTransaction.getTimestamp();
		int blockno = currentTransaction.getBlockno();
		try {
			txTable.insert(input, amt, output, publickey, signature, timestamp,
					blockno);
			System.out.println(input + amt + output + timestamp + blockno);
		} finally {
			db.commit();
			db.close();
		}

	}

	/**
	 * Adds a mined block into the blockchain table. This method does NOT verify
	 * the block before adding it. Block must be verified before passing it into
	 * this method.
	 */
	public synchronized void addBlock(BlockMinedAction blockMined)
			throws SqlJetException {
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, true);
		long timestamp = blockMined.getTimestamp();
		String publickey = blockMined.getPublickey();
		String hash = blockMined.getHash();
		long nonce = blockMined.getNonce();
		int blockno = blockMined.getBlockno();
		String transactions = blockMined.getTransactions();
		db.beginTransaction(SqlJetTransactionMode.EXCLUSIVE);
		ISqlJetTable blockchain = db.getTable("blockchain");
		try {
			blockchain.insert(timestamp, publickey, hash, nonce, transactions,
					blockno);
		} finally {
			db.commit();
		}
		db.close();
	}

	/**
	 * Compares the length of the current blockchain with a new one passed in as
	 * a byte array using hasNewer(). If the new one is longer and validates, it
	 * deletes the old db and writes the new one to disk.
	 */
	public synchronized void updateDb(byte[] newDb) throws Exception {
		File tempFile = new File(Configuration.TEMP_DB_NAME);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		FileOutputStream tempDb = new FileOutputStream(tempFile);
		tempDb.write(newDb);
		tempDb.flush();
		tempDb.close();
		tempDb = null;
		System.gc();
		if (validateDb(tempFile)) {
			File dbFile = new File(Configuration.DB_NAME);
			if (dbFile.exists()) {
				dbFile.delete();
			}
			FileOutputStream fos = new FileOutputStream(dbFile);
			fos.write(newDb);
			fos.flush();
			fos.close();
			fos = null;
			System.gc();
		}
		if (tempFile.exists()) {
			tempFile.delete();
		}
		System.gc();
	}

	/**
	 * Compares the length of the current blockchain to a bytearray attached to
	 * an UpdateAction.
	 */
	public synchronized UpdateState hasNewer(UpdateAction update)
			throws IOException, SqlJetException {
		int currentBlockLength = getCurrentBlockNo(Configuration.DB_NAME);
		if (currentBlockLength > update.getBlockLength()) {
			return UpdateState.OLDER;
		} else if (currentBlockLength == update.getBlockLength()) {
			return UpdateState.EQUAL;
		} else
			return UpdateState.NEWER;
	}

	/**
	 * Validates each transaction and each block in a database. For
	 * transactions, the signature is checked, and for blocks the data is hashed
	 * and compared to the hash attached to it.
	 */
	public synchronized boolean validateDb(File dbFile) throws Exception {
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable txTable = db.getTable("transactions");
		ISqlJetCursor cursor = txTable.lookup("transaction_index");
		String input, output, publickey, signature, hash, transactions;
		//Should target have a different default value?
		BigInteger target = null;
		long timestamp, nonce, amt;
		int blockno;

		if (!cursor.eof()) {
			do {
				input = cursor.getString("input");
				amt = cursor.getInteger("amt");
				output = cursor.getString("output");
				publickey = cursor.getString("publickey");
				signature = cursor.getString("signature");
				timestamp = cursor.getInteger("timestamp");
				target = new BigInteger(cursor.getString("target"), 16);
				blockno = (int) cursor.getInteger("blockno");
				TxAction currentTx = new TxAction(input, amt, output,
						publickey, timestamp, blockno);
				currentTx.setPublicKey(publickey);
				currentTx.setSignature(signature);
				boolean valid = VerificationTools.verifyTransaction(currentTx);
				if (!valid) {
					return false;
				}
			} while (cursor.next());
		}
		cursor.close();
		ISqlJetTable blockTable = db.getTable("blockchain");
		cursor = blockTable.lookup("blockchain_index");

		if (!cursor.eof()) {
			do {
				timestamp = cursor.getInteger("timestamp");
				publickey = cursor.getString("publickey");
				hash = cursor.getString("hash");
				nonce = cursor.getInteger("nonce");
				blockno = (int) cursor.getInteger("blockno");
				transactions = getTransactionsFromBlock(blockno - 1);
				BlockMinedAction currentBlock = new BlockMinedAction(timestamp,
						publickey, hash, nonce, transactions, target, blockno);
				boolean valid = VerificationTools.verifyBlock(currentBlock);
				if (!valid) {
					return false;
				}
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return true;
	}

	/**
	 * Returns the hash that was submitted with a given block.
	 */
	public synchronized String getHashFromBlock(int blk) throws SqlJetException {
		if (blk < 1) {
			return "";
		}
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable blockchain = db.getTable("blockchain");
		ISqlJetCursor cursor = blockchain.lookup("blockchain_index");
		String hash = "";
		if (!cursor.eof()) {
			do {
				if (cursor.getInteger("blockno") == blk) {
					hash = cursor.getString("hash");
				}
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return hash;
	}

	/**
	 * Returns 1 greater than the blockno of the last solved block.
	 */
	public synchronized int getCurrentBlockNo(String filename)
			throws SqlJetException {
		File dbFile = new File(filename);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable blockchain = db.getTable("blockchain");
		ISqlJetCursor cursor = blockchain.lookup("blockchain_index");
		int mostRecentBlock = 0;
		if (!cursor.eof()) {
			do {
				int currentBlockNo = (int) cursor.getInteger("blockno");
				if (currentBlockNo > mostRecentBlock)
					mostRecentBlock = currentBlockNo;
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return mostRecentBlock + 1;
	}

	/**
	 * Returns a string built from every transaction listed on a given block
	 * number.
	 */
	public synchronized String getTransactionsFromBlock(int blockn)
			throws SqlJetException {
		/*
		 * For consistency, the transaction strings built by this method should
		 * match Tools.buildTransactionString(); change to > inp, amt, output!!
		 */
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable blockchain = db.getTable("transactions");
		ISqlJetCursor cursor = blockchain.order("transaction_index");
		String transactions = "";
		int blockno = blockn;
		if (!cursor.eof()) {
			do {
				if (cursor.getInteger("blockno") == blockno) {
					String input = cursor.getString("input");
					if (input.length() > 65) {
						transactions += input.substring(input.length() - 65);
					} else {
						transactions += input;
					}
					transactions += cursor.getInteger("amt");
					transactions += cursor.getString("output");
				}
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return transactions;

	}
	
	public synchronized long getTimestampFromBlock(int blockno) throws SqlJetException {
		if (blockno < 2) {
			//What should the default value be?!
			return -1;
		}
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable blockchain = db.getTable("blockchain");
		ISqlJetCursor cursor = blockchain.lookup("blockchain_index");
		long timestamp = -1;
		if (!cursor.eof()) {
			do {
				if (cursor.getInteger("blockno") == blockno) {
					timestamp = cursor.getInteger("timestamp");
				}
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return timestamp;
	}
	
	public synchronized BigInteger getTargetFromBlock(int blockno) throws SqlJetException {
		if (blockno < 2) {
			return Configuration.DIFFICULTY_1_TARGET;
		}
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable blockchain = db.getTable("blockchain");
		ISqlJetCursor cursor = blockchain.lookup("blockchain_index");
		String targetString = "";
		if (!cursor.eof()) {
			do {
				if (cursor.getInteger("blockno") == blockno) {
					targetString = cursor.getString("target");
				}
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return new BigInteger(targetString, 16);
	}

	/**
	 * Returns a BlockMinedAction built from the data in the db for a given
	 * block number.
	 */
	public synchronized BlockMinedAction getBlock(int blockno)
			throws SqlJetException {
		File dbFile = new File(Configuration.DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable blockTable = db.getTable("blockchain");
		ISqlJetCursor cursor = blockTable.lookup("blockno_index");
		BlockMinedAction currentBlock = null;
		if (!cursor.eof()) {
			do {
				long b = cursor.getInteger("blockno");
				if (b == blockno) {
					long timestamp = cursor.getInteger("timestamp");
					String publickey = cursor.getString("publickey");
					String hash = cursor.getString("hash");
					long nonce = cursor.getInteger("nonce");
					String transactions = cursor.getString("transactions");
					BigInteger target = new BigInteger(cursor.getString("target"), 16);
					System.out.println("[++] TIMESTAMP:" + timestamp);
					currentBlock = new BlockMinedAction(timestamp, publickey,
							hash, nonce, transactions, target, blockno);
					break;
				}
			} while (cursor.next());
		}
		cursor.close();
		db.close();
		return currentBlock;
	}
}
