package coffeecoin.main;

import java.math.BigInteger;

/**
 * This class holds configuration data
 * for the entire application
 */
public class Configuration {
	
	public static final String DB_NAME = "blockchain.db";
	public static final String TEMP_DB_NAME = "temp.db";
	public static final String WALLET_DB_NAME = "wallet.db";
	public static final String PEER_DB_NAME = "peers.db";
	public static final long BLOCK_REWARD = 100;
	public static final int CONFIRMS = 1;
	public static final long BLOCK_TIME = 10000; //milliseconds
	public static final BigInteger DIFFICULTY_1_TARGET = new BigInteger("00000000FFFF" +
			"0000000000000000000000000000000000000000000000000000", 16);
	public static int difficulty = 4;
}
