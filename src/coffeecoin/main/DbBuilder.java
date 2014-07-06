package coffeecoin.main;

import java.io.File;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * This class deletes all of the required database files if present, and
 * constructs new blank ones with the proper tables and indicies.
 * 
 * @author
 */
public class DbBuilder {

	private static final String DB_FILE = Configuration.DB_NAME;

	public static void main(String[] args) throws SqlJetException {
		File dbFile = new File(DB_FILE);
		if (dbFile.exists()) {
			dbFile.delete();
		}
		//Does System.gc() need to be called here and after the other two deletes?
		SqlJetDb db = SqlJetDb.open(dbFile, true);
		db.getOptions().setAutovacuum(true);
		db.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
			db.getOptions().setUserVersion(1);
			db.createTable("CREATE TABLE blockchain (timestamp INTEGER, publickey VARCHAR(128), hash VARCHAR(128), nonce INTEGER, transactions VARCHAR(1024), blockno INTEGER PRIMARY KEY)");
			db.createTable("CREATE TABLE transactions (input VARCHAR(128), amt INTEGER, output VARCHAR(128), publickey VARCHAR(128), signature VARCHAR(128), timestamp INTEGER, blockno INTEGER)");
			db.createIndex("CREATE INDEX blockno_index ON blockchain(blockno)");
			db.createIndex("CREATE INDEX blockchain_index ON blockchain(timestamp, publickey, hash, nonce, transactions, blockno)");
			db.createIndex("CREATE INDEX transaction_index ON transactions(input, amt, output, publickey, signature, timestamp, blockno)");
			db.createIndex("CREATE INDEX input_index ON transactions(input, amt)");
			db.createIndex("CREATE INDEX output_index ON transactions(output, amt)");
		} finally {
			db.commit();
			db.close();
			System.out.println("Db Built");
		}

		File walletDbFile = new File(Configuration.WALLET_DB_NAME);
		if (walletDbFile.exists()) {
			walletDbFile.delete();
		}
		SqlJetDb wdb = SqlJetDb.open(walletDbFile, true);
		wdb.getOptions().setAutovacuum(true);
		wdb.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
			wdb.createTable("CREATE TABLE wallet (address VARCHAR(128), publickey VARCHAR(512), privatekey VARCHAR(512))");
			wdb.createIndex("CREATE INDEX wallet_index on wallet(address, publickey, privatekey)");
		} finally {
			wdb.commit();
			wdb.close();
		}

		File peerDbFile = new File(Configuration.PEER_DB_NAME);
		if (peerDbFile.exists()) {
			peerDbFile.delete();
		}
		SqlJetDb pdb = SqlJetDb.open(peerDbFile, true);
		pdb.getOptions().setAutovacuum(true);
		pdb.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
			pdb.createTable("CREATE TABLE peers (ip VARCHAR(15))");
			pdb.createIndex("CREATE INDEX peer_index on peers(ip)");

		} finally {
			pdb.commit();
			pdb.close();
		}
		System.gc();
	}

}
