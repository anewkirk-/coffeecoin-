package coffeecoin.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * This class contains methods for manipulating
 * the wallet database
 * @author 
 */
public class WalletDbManager {
	
	private static WalletDbManager instance;
	
	private WalletDbManager() {}
	
	/**
	 * This is a singleton class
	 */
	public static WalletDbManager getInstance() {
		if(instance == null) {
			instance = new WalletDbManager();
		}
		return instance;
	}

	/**
	 * Checks if a given address is already in
	 * the wallet db, if not then the address
	 * is added.
	 */
	public void addNewAddress(String address, String publickey, String privatekey) throws SqlJetException {
		File wFile = new File(Configuration.WALLET_DB_NAME);
		SqlJetDb wDb = SqlJetDb.open(wFile, true);
		wDb.beginTransaction(SqlJetTransactionMode.WRITE);
		ISqlJetTable addressTable = wDb.getTable("wallet");
		//
		ISqlJetCursor cursor = addressTable.lookup("wallet_index");
		boolean alreadyPresent = false;
		if(!cursor.eof()) {
			do {
				if(address.equals(cursor.getString("address"))) {
					alreadyPresent = true;
				}
			} while(cursor.next());
		}
		//
		if(!alreadyPresent) {
			try {
				addressTable.insert(address, publickey, privatekey);
			} finally {
				wDb.commit();
				wDb.close();
				System.out.println("New address added to wallet[+]");
			}
		}
	}
	
	/**
	 * returns an arraylist of every publickey
	 * in the wallet db
	 */
	public ArrayList<String> getAddresses() throws SqlJetException {
		ArrayList<String> addressList = new ArrayList<String>();
		File wFile = new File(Configuration.WALLET_DB_NAME);
		SqlJetDb wDb = SqlJetDb.open(wFile, false);
		wDb.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable addressTable = wDb.getTable("wallet");
		ISqlJetCursor cursor = addressTable.lookup("wallet_index");
		if(!cursor.eof()) {
			do {
				addressList.add(cursor.getString("address"));
			} while(cursor.next());
		}
		cursor.close();
		wDb.close();
		return addressList;
	}
	
	/**
	 * returns the private key associated with a given
	 * address, if the address is in the db. Used to sign
	 * transactions.
	 */
	public String getPrivateKey(String address) throws SqlJetException {
		String privkey = "";
		File wFile = new File(Configuration.WALLET_DB_NAME);
		SqlJetDb wDb = SqlJetDb.open(wFile, false);
		wDb.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable addressTable = wDb.getTable("wallet");
		ISqlJetCursor cursor = addressTable.lookup("wallet_index");
		if(!cursor.eof()) {
			do {
				if(cursor.getString("address").equals(address)) {
					privkey = cursor.getString("privatekey");
				}
			} while(cursor.next());
		}
		cursor.close();
		wDb.close();
		return privkey;
	}

	/**
	 * Returns total balance of all addresses
	 * in the database
	 */
	public long getBalance() throws SqlJetException {
		ArrayList<String> adds = getAddresses();
		long bal = 0;
		String current = "";
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		for(Iterator<String> it = adds.iterator(); it.hasNext();) {
			current = it.next();
			bal += dbman.checkBalance(current);
		}
		return bal;
	}

	public String getPublicKeyFromAddress(String address) throws SqlJetException {
		String publicKey = "";
		File wFile = new File(Configuration.WALLET_DB_NAME);
		SqlJetDb wDb = SqlJetDb.open(wFile, false);
		wDb.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable addressTable = wDb.getTable("wallet");
		ISqlJetCursor cursor = addressTable.lookup("wallet_index");
		if(!cursor.eof()) {
			do {
				if(cursor.getString("address").equals(address)) {
					publicKey = cursor.getString("publickey");
				}
			} while(cursor.next());
		}
		cursor.close();
		wDb.close();
		return publicKey;
	}

}
