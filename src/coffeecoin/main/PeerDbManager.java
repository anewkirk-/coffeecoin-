package coffeecoin.main;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * This class contains methods for CRUD operations
 * on the peer database
 * @author 
 */
public class PeerDbManager {
	
	private static PeerDbManager instance;
	
	private PeerDbManager(){}
	
	public static PeerDbManager getInstance() {
		if(instance == null) {
			instance = new PeerDbManager();
		}
		return instance;
	}
	
	/**
	 * Returns an arraylist of each peer in the db
	 */
	public synchronized ArrayList<InetAddress> getPeers() throws SqlJetException, UnknownHostException {
		ArrayList<InetAddress> peers = new ArrayList<InetAddress>();
		File dbFile = new File(Configuration.PEER_DB_NAME);
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetTable peerTable = db.getTable("peers");
		ISqlJetCursor cursor = peerTable.lookup("peer_index");
		if(!cursor.eof()){
			do {
				peers.add(InetAddress.getByName(cursor.getString("ip")));
			} while(cursor.next());
		}
		cursor.close();
		db.close();
		return peers;
	}

}
