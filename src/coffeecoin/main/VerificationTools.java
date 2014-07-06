package coffeecoin.main;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import coffeecoin.network.BlockMinedAction;
import coffeecoin.network.TxAction;

public class VerificationTools {

	/**
	 * This method verifies that an address's balance is sufficient to cover a
	 * transaction, and also runs it through verifyTxSignature()
	 */
	public static boolean verifyTransaction(TxAction currentTransaction)
			throws Exception {
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		if (currentTransaction.getInput().equals("coinbase")) {
			/*
			 * Check that the coinbase tx amount matches
			 * Configuration.BLOCK_REWARD Do we need to check anything else to
			 * verify a coinbase transaction?
			 */
			System.out.println("[+] Verified coinbase transaction.");
			return true;
		} else {
			if (!verifyTxSignature(currentTransaction)) {
				return false;
			}
			long balance = dbman.checkBalance(currentTransaction.getInput());
			boolean verified = balance >= currentTransaction.getAmt();
			System.out.println("[+] Verified transaction.");
			return verified;
		}
	}

	/**
	 * This method takes the previous hash, tx string, timestamp, and nonce of a
	 * block, hashes it, and compares it to the submitted hash
	 */
	public static boolean verifyBlock(BlockMinedAction blockMined)
			throws Exception {
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
		if (currentBlock < 1) { // == 0) {
			generatedHash = miner.genHash("", "", timestamp, nonce);
		} else {
			generatedHash = miner.genHash(hash, transactions, timestamp, nonce);
		}
		String submittedHash = blockMined.getHash();
		System.out.println("  [-] Submitted hash: " + submittedHash);
		System.out.println("  [-] Generated hash: " + generatedHash);
		return generatedHash.equals(submittedHash);
	}
	
	/**
	 * Verifies the ECDSA signature attached to a TxAction.
	 */
	public static boolean verifyTxSignature(TxAction a)
			throws SignatureException, InvalidKeyException,
			InvalidKeySpecException, NoSuchAlgorithmException {
		String txString = Tools.buildTransactionString(a);
		String submittedSig = a.getSignature();
		String submittedPubKey = a.getPublicKey();
		byte[] pubkey = Tools.hexStringToByteArray(submittedPubKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubkey);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		PublicKey keyObj = keyFactory.generatePublic(keySpec);
		Signature sig = Signature.getInstance("SHA1withECDSA");
		sig.initVerify(keyObj);
		try {
			sig.update(txString.getBytes("UTF-8"));
		} catch (Exception e) {
			System.err.println("Updating Signature object unsuccessful");
		}
		boolean verified = sig.verify(Tools.hexStringToByteArray(submittedSig));
		System.out.println("[~] Transaction signature verified: " + verified);
		sig = null;
		System.gc();
		return verified;
	}
}
