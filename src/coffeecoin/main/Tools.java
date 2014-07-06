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
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import coffeecoin.network.TxAction;

/**
 * This class provides various utilities for manipulating strings and performing
 * cryptographic operations such as hashing and signing
 * 
 * @author anewkirk
 */
public class Tools {

	/**
	 * Creates a SHA-256 hash from a string input returns hex string
	 */
	public static String hashText(String t) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(t.getBytes("UTF-8"), 0, t.length());
		byte byteData[] = md.digest();
		return String.format("%032x", new BigInteger(1, byteData));
	}

	/**
	 * Takes a hex value of type String and returns raw bytes. 
	 * StackOverflow solution, rewrite original solution
	 */
	public static byte[] hexStringToByteArray(String s) {
		String str = s;
		if (s == null || s.isEmpty()) {
			return new byte[] {};
		}
		// This section has a small chance to cause mystery problems.
		// Keep an eye on it!
		// while(str.length() < 64) {
		// str = "0" + str;
		// }
		// end section
		int len = str.length();
		byte[] data = new byte[len / 2];
		try {
			for (int i = 0; i < len; i += 2) {
				data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character
						.digit(str.charAt(i + 1), 16));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * Turns raw bytes into a hexadecimal string.
	 * StackOverflow solution, rewrite original implementation
	 */
	public static String bytesToHex(byte[] bytes) {
		char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);

	}

	/**
	 * Generates an ECDSA keypair as an array of hex strings.
	 * 
	 * @return {publickey, privatekey}
	 */
	public static String[] generateKeypair() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
		ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
		keyGen.initialize(ecSpec, new SecureRandom());
		KeyPair keyPair = keyGen.generateKeyPair();
		ECPublicKey pubkey = (ECPublicKey) keyPair.getPublic();
		ECPrivateKey privkey = (ECPrivateKey) keyPair.getPrivate();
		byte[] pubBytes = pubkey.getEncoded();
		byte[] privBytes = privkey.getEncoded();
		return new String[] { bytesToHex(pubBytes), bytesToHex(privBytes) };
	}

	/**
	 * Returns an ECDSA hexadecimal signature from tx data and a private key
	 */
	public static String signTransaction(TxAction action, String privatekey)
			throws InvalidKeySpecException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException,
			UnsupportedEncodingException {
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
	 * Returns a String created by concatenating the input address, amount, and
	 * output address of a transaction
	 */
	public static String buildTransactionString(TxAction action) {
		String txString = "";
		String input = action.getInput();
		txString += input;
		txString += action.getAmt();
		txString += action.getOutput();
		return txString;
	}

	/**
	 * Builds a shortened address from a public key.
	 * base58encode(sha256(publickey))
	 */
	public static String addressFromKey(String pubkey) throws Exception {
		String keyHash = hashText(pubkey);
		byte[] hashbytes = hexStringToByteArray(keyHash);
		BigInteger hashInt = new BigInteger(1, hashbytes);
		return base58Encode(hashInt);
	}

	/**
	 * Encodes a BigInteger into BitCoin's base58 alphabet. Used for creating
	 * addresses
	 */
	public static String base58Encode(BigInteger i) {
		BigInteger start = i;
		String code_string = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
		String output = "";
		while (start.compareTo(BigInteger.valueOf(0)) > 0) {
			BigInteger[] division = start.divideAndRemainder(BigInteger
					.valueOf(58));
			long remainder = division[1].longValue();
			start = division[0];
			output += code_string.charAt((int) remainder);
		}
		return output;
	}

	/**
	 * Calculates the difficulty of a given target
	 */
	public static double findDifficultyFromTarget(BigInteger target) {
		double diff1t = Configuration.DIFFICULTY_1_TARGET.doubleValue();
		double t = target.doubleValue();
		return t / diff1t;
	}

	/**
	 * Calculates a new target based on the previous difficulty, the previous
	 * target, and the time it took to solve the previous block
	 */
	public static BigInteger retarget(double difficulty, long time,
			BigInteger lastTarget) {
		double newDifficulty = difficulty
				/ ((double) time / Configuration.BLOCK_TIME);
		System.out.println("new diff: " + newDifficulty);
		double newTarget = newDifficulty * lastTarget.doubleValue();
		BigDecimal bd = new BigDecimal(newTarget);
		return bd.toBigInteger();
	}
}
