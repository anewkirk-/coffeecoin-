package coffeecoin.network;

/**
 * This class holds data for a transaction
 * @author 
 */
public class TxAction extends NetworkAction {

	private static final long serialVersionUID = 2816666922578923703L;
	private String input, output, signature, publicKey;
	private long amt, timestamp;
	private int blockno;
	
	public TxAction(String input, long amt, String output) {
		this.input = input;
		this.amt = amt;
		this.setOutput(output);
	}

	public TxAction(String input2, long amt2, String output2, String publickey, long timestamp, int blockno) {
		this.input = input2;
		this.amt = amt2;
		this.setOutput(output2);
		this.setTimestamp(timestamp);
		this.blockno = blockno;
		this.publicKey = publickey;
	}

	public String getInput() {
		return this.input;
	}

	public long getAmt() {
		return this.amt;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public int getBlockno() {
		return blockno;
	}

	public void setBlockno(int blockno) {
		this.blockno = blockno;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
