package coffeecoin.test;

import org.tmatesoft.sqljet.core.SqlJetException;

import coffeecoin.main.BlockchainDbManager;

public class BalanceTest {

	public static void main(String[] args) throws SqlJetException {
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		System.out.println(dbman.checkBalance("i"));
	}

}
