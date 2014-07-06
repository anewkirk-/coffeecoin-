package coffeecoin.network;

/**
 * This class is used to hold data from a mined block.
 * 
 * @author
 */
public class BlockMinedAction extends NetworkAction {

	private static final long serialVersionUID = 8778067921249368708L;
	private long timestamp;
	private String publickey;
	private String hash;
	private long nonce;
	private int blockno;
	private String transactions;

	public BlockMinedAction(long timestamp, String publickey, String hash,
			long nonce, String transactions, int blockno) {
		this.setTimestamp(timestamp);
		this.setPublickey(publickey);
		this.setHash(hash);
		this.setNonce(nonce);
		this.setBlockno(blockno);
		this.setTransactions(transactions);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getPublickey() {
		return publickey;
	}

	public void setPublickey(String publickey) {
		this.publickey = publickey;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public int getBlockno() {
		return blockno;
	}

	public void setBlockno(int blockno) {
		this.blockno = blockno;
	}

	public String getTransactions() {
		return transactions;
	}

	public void setTransactions(String transactions) {
		this.transactions = transactions;
	}

}