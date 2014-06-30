package coffeecoin.main;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import coffeecoin.network.BlockMinedAction;
import coffeecoin.network.TxAction;

/**
 * This class needs to be split in two: Tools and Verifier
 * because it's becoming bloated.
 */

/**
 * This class provides various utilities for manipulating
 * strings and performing cryptographic operations such as
 * hashing and signature verification/creation.
 * All methods are static.
 * @author 
 */
public class Tools {
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	/**
	 * Creates a SHA-256 hash from a string input
	 * returns hex string
	 */
	public static String hashText(String t) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(t.getBytes("UTF-8"), 0, t.length());
		byte byteData[] = md.digest();
		return String.format("%032x", new BigInteger(1, byteData));
	}

	/**
	 * 
	 */
	public static boolean verifyTransaction(TxAction currentTransaction) throws Exception {
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		if( currentTransaction.getInput().equals("coinbase")) {
			/*
			 * Do we need to check anything else to verify
			 * a coinbase transaction?
			 */
			System.out.println("[+] Verified coinbase transaction.");
			return true;
		} else {
			if(!verifyTxSignature(currentTransaction)) {
				return false;
			}
			long balance = dbman.checkBalance(currentTransaction.getInput());
			boolean verified = balance >= currentTransaction.getAmt();
			System.out.println("[+] Verified transaction.");
			return verified;
		}		
	}

	/**
	 * 
	 */
	public static boolean verifyBlock(BlockMinedAction blockMined) throws Exception {
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		int currentBlock = blockMined.getBlockno();
		System.out.println("[+]Current block number:" + currentBlock + "\n");
		String hash = dbman.getHashFromBlock(currentBlock - 1);
		String transactions = blockMined.getTransactions().replace(" ", "");
		long timestamp = blockMined.getTimestamp();
		long nonce = blockMined.getNonce();
		System.out.println("[!] Verifying block data:");
		System.out.println("  [-] Previous hash: " + hash);
		System.out.println("  [-] Transaction string: " + transactions);
		System.out.println("  [-] Timestamp: " + timestamp);
		System.out.println("  [-] Nonce: " + nonce);
		Miner miner = Miner.getInstance();
		String generatedHash = "";
		if(currentBlock < 1) { // == 0) {
			generatedHash = miner.genHash("", "", timestamp, nonce);
		}else{
			generatedHash = miner.genHash(hash, transactions, timestamp, nonce);
		}
		String submittedHash = blockMined.getHash();
		System.out.println("  [-] Submitted hash: " + submittedHash);
		System.out.println("  [-] Generated hash: " + generatedHash);
		return generatedHash.equals(submittedHash);
	}
	
	/**
	 * 
	 */
	public static byte[] hexStringToByteArray(String s) {
		String str = s;
		if(s == null || s.isEmpty()) {
			return new byte[]{};
		}
		//This section has a small chance to cause mystery problems. 
		//Keep an eye on it!
//		while(str.length() < 64) {
//			str = "0" + str;
//		}
		//end section
	    int len = str.length();
	    byte[] data = new byte[len / 2];
	    try{
		    for (int i = 0; i < len; i += 2) {
		        data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4)
		                             + Character.digit(str.charAt(i+1), 16));
		    }
	    } catch(Exception e) {
	    	System.err.println(e.getMessage());
	    }
	    return data;
	}
	
	/**
	 * 
	 */
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
 
	}

	/**
	 * @throws InvalidAlgorithmParameterException 
	 * 
	 */
	public static String[] generateKeypair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
	    ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
	    keyGen.initialize(ecSpec, new SecureRandom());
	    KeyPair keyPair = keyGen.generateKeyPair();
	    ECPublicKey pubkey = (ECPublicKey) keyPair.getPublic();
	    ECPrivateKey privkey = (ECPrivateKey) keyPair.getPrivate();
	    byte[] pubBytes = pubkey.getEncoded();
	    byte[] privBytes = privkey.getEncoded();
	    return new String[] {bytesToHex(pubBytes), bytesToHex(privBytes)};
	}
	
	/**
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	public static String signTransaction(TxAction action, String privatekey) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
		/* format of transaction string:
		 * input timestamp blockno */
		//Should this be changed to include amt/output?
		
		String txString = buildTransactionString(action);
		byte[] privkey = hexStringToByteArray(privatekey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privkey);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		Signature sig = Signature.getInstance("SHA1withECDSA");
		PrivateKey keyObj = keyFactory.generatePrivate(keySpec);
		sig.initSign(keyObj);
		sig.update(txString.getBytes("UTF-8"));
		System.out.println("[+] Signed transaction.");
		return bytesToHex(sig.sign());
	}
	
	/**
	 * 
	 */
	public static boolean verifyTxSignature(TxAction a) throws SignatureException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {
		String txString = buildTransactionString(a);
		String submittedSig = a.getSignature();
		String submittedPubKey = a.getPublicKey();
		byte[] pubkey = hexStringToByteArray(submittedPubKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubkey);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		PublicKey keyObj = keyFactory.generatePublic(keySpec);
		Signature sig = Signature.getInstance("SHA1withECDSA");
		sig.initVerify(keyObj);
		try {
			sig.update(txString.getBytes("UTF-8"));
		} catch(Exception e){System.err.println("Updating Signature object unsuccessful");}
		boolean verified = sig.verify(hexStringToByteArray(submittedSig));
		System.out.println("[~] Transaction signature verified: " + verified);
		sig = null;
		System.gc();
		return verified;
	}
	
	/**
	 * 
	 */
	private static String buildTransactionString(TxAction action) {
		String txString = "";
		String input = action.getInput();
		txString += input;
		txString += action.getAmt();
		txString += action.getOutput();
		return txString;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public static String addressFromKey(String pubkey) throws Exception {		
		String keyHash = hashText(pubkey);
		byte[] hashbytes = hexStringToByteArray(keyHash);
		BigInteger hashInt = new BigInteger(1, hashbytes);
		return base58Encode(hashInt);
	}
	
	public static String base58Encode(BigInteger i) {
		BigInteger start = i;
		String code_string = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";		
		String output = "";
		while(start.compareTo(BigInteger.valueOf(0)) > 0) {
			BigInteger[] division = start.divideAndRemainder(BigInteger.valueOf(58));
			long remainder = division[1].longValue();
			start = division[0];
			output += code_string.charAt((int)remainder);
		}
		return output;
	}
	
	public static double findDifficultyFromTarget(BigInteger target) {
		double diff1t = Configuration.DIFFICULTY_1_TARGET.doubleValue();
		double t = target.doubleValue();
		return t/diff1t;
	}
	
	public static BigInteger retarget(double difficulty, long time, BigInteger lastTarget) {
		double newDifficulty = difficulty / ((double)time/Configuration.BLOCK_TIME);
		System.out.println("new diff: " + newDifficulty);
		double newTarget = newDifficulty * lastTarget.doubleValue();
		BigDecimal bd = new BigDecimal(newTarget);
		return bd.toBigInteger();
	}
	
//	public static double getDifficulty() {
//		
//	}
}
