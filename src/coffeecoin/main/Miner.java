package coffeecoin.main;

import coffeecoin.network.*;
import coffeecoin.ui.MainWindow;
import java.util.Date;

import org.tmatesoft.sqljet.core.SqlJetException;

public class Miner extends Thread {

	private static Miner instance;
	private long timestamp;
	private String transactions = "";
	private String lastHash = "";
	private int blockno;

	/**
	 * This is a singleton class.
	 */
	private Miner() {
	}

	public static Miner getInstance() throws SqlJetException {
		if (instance == null) {
			instance = new Miner();
		}
		instance.checkDb();
		return instance;
	}

	/**
	 * Mines one block, then sets instance to null. A better way might be to
	 * just initialize all fields to default, call checkDb(), and return a
	 * boolean
	 */
	@Override
	public void run() {
		String hash = "";
		String difficultyMatch = "";
		long nonce = -1;
		for (int i = 0; i < Configuration.difficulty; i++) {
			difficultyMatch += "3";
		}
		while (hash.length() == 0
				|| !hash.substring(0, Configuration.difficulty).equals(
						difficultyMatch)) {
			try {
				nonce++;
				hash = genHash(lastHash, this.transactions, this.timestamp,
						nonce);
				System.out.println("[-] " + hash);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String[] newKeyPair = null;
		try {
			newKeyPair = Tools.generateKeypair();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n\n[+]  Block Mined\n\n");
		String publicKey = newKeyPair[0];
		BlockMinedAction blockMined = new BlockMinedAction(timestamp,
				publicKey, hash, nonce, this.transactions, blockno);
		boolean verified = false;
		try {
			verified = VerificationTools.verifyBlock(blockMined);
			if (verified) {
				String privateKey = newKeyPair[1];
				String address = Tools.addressFromKey(publicKey);
				WalletDbManager wDbMan = WalletDbManager.getInstance();
				wDbMan.addNewAddress(address, publicKey, privateKey);
				BlockchainDbManager dbman = BlockchainDbManager.getInstance();
				dbman.addBlock(blockMined);
				TxAction coinbaseTransaction = new TxAction("coinbase",
						Configuration.BLOCK_REWARD, address, publicKey,
						timestamp, blockno);
				coinbaseTransaction.setPublicKey(publicKey);
				dbman.addTx(coinbaseTransaction);
				MainWindow gui = MainWindow.getInstance();
				gui.displayMessage("Block Successfully Mined");
				ClientAgent ca = ClientAgent.getInstance();
				// Do these two lines need to be here? If other nodes accept the
				// UpdateAction,
				// their blockchains will be up to date with this one
				// ca.sendAction(coinbaseTransaction);
				ca.sendAction(new UpdateAction());
				// ca.sendAction(blockMined);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance = null;
	}

	/**
	 * Updates fields with current data so we can call run()
	 */
	public void checkDb() throws SqlJetException {
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		this.timestamp = new Date().getTime();
		int currentBlock = dbman.getCurrentBlockNo(Configuration.DB_NAME);
		this.blockno = currentBlock;
		this.transactions = dbman.getTransactions(currentBlock - 1);
		this.lastHash = dbman.getHashFromBlock(currentBlock - 1);
	}

	/**
	 * Returns a sha256 hash from a previous hash, a transaction string, a
	 * timestamp, and a nonce
	 */
	public String genHash(String hash, String transactions, long timestamp,
			long nonce) throws Exception {
		String text = hash + transactions + timestamp + nonce;
		String sha256hash = Tools.hashText(text);
		return sha256hash;
	}
}
