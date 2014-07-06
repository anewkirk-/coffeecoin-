package coffeecoin.main;

import java.math.BigInteger;

/**
 * This class holds configuration data for the entire application
 */
public class Configuration {

	// Filenames for local databases,
	public static final String DB_NAME = "blockchain.db";
	public static final String TEMP_DB_NAME = "temp.db";
	public static final String WALLET_DB_NAME = "wallet.db";
	public static final String PEER_DB_NAME = "peers.db";

	// The payout for mining a block, This needs to be verified
	// on each coinbase transaction!
	public static final long BLOCK_REWARD = 100;

	// The amount of confirms a transaction needs before the coins can
	// be spent again
	public static final int CONFIRMS = 1;

	// The target block time, in milliseconds
	public static final long BLOCK_TIME = 10000;

	// Target at difficulty 1.
	public static final BigInteger DIFFICULTY_1_TARGET = new BigInteger(
			"00000000FFFF"
					+ "0000000000000000000000000000000000000000000000000000",
			16);

	// Will be deprecated in version 0.1.0
	public static int difficulty = 4;
}
