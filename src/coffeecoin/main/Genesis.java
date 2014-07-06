package coffeecoin.main;

import java.util.Date;
import java.util.Random;

import coffeecoin.network.BlockMinedAction;
import coffeecoin.network.ClientAgent;
import coffeecoin.network.UpdateAction;

/**
 * This class generates a genesis block onto the blockchain
 * 
 * @author anewkirk
 */
public class Genesis {

	public static void main(String[] args) throws Exception {
		Date d = new Date();
		long time = d.getTime();

		Random gen = new Random();
		String hash = "  ";
		int nonce = 0;

		// This needs to be updated
		while (!hash.substring(0, 2).equals("33")) {

			// As does this. start at 0 and increment, no need for
			// a random number
			nonce = gen.nextInt(99999);
			hash = Tools.hashText(String.valueOf(time) + String.valueOf(nonce));
			System.out.println("Hash generated: " + hash);
		}

		System.out.println("\n\n[+] Genesis Block Mined");
		System.out.println("Hash: " + hash);
		System.out.println("Blockno: 1");
		System.out.println("Nonce: " + nonce);
		System.out.println("Timestamp: " + time);

		BlockMinedAction genesisBlock = new BlockMinedAction(time, "", hash,
				nonce, "", 1);
		if (VerificationTools.verifyBlock(genesisBlock)) {
			System.out.println("Block Verified");
			BlockchainDbManager dbman = BlockchainDbManager.getInstance();
			dbman.addBlock(genesisBlock);
			System.out.println("Block added to blockchain.");
			ClientAgent ca = ClientAgent.getInstance();
			System.out.println(ca.getPeers().toString());
			ca.sendAction(new UpdateAction());
		}

	}
}
