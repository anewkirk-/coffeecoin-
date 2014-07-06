package coffeecoin.network;

import java.io.IOException;
import java.io.Serializable;
import org.tmatesoft.sqljet.core.SqlJetException;

import coffeecoin.main.*;

/**
 * This class holds a byte[] of a blockchain database.
 * 
 * @author
 */
public class UpdateAction extends NetworkAction implements Serializable {

	private static final long serialVersionUID = 2124801234005489977L;
	private byte[] dbFile;
	private int blockLength;

	public UpdateAction() throws IOException, SqlJetException {
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		this.dbFile = dbman.getCurrentDb(Configuration.DB_NAME);
		this.setBlockLength(dbman.getCurrentBlockNo(Configuration.DB_NAME));
	}

	public String getHash() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getDb() {
		return this.dbFile;
	}

	public int getBlockLength() {
		return blockLength;
	}

	public void setBlockLength(int blockLength) {
		this.blockLength = blockLength;
	}

}
