package coffeecoin.test;

import java.math.BigInteger;

import coffeecoin.main.Tools;

public class Difficulty_Test_1 {

	public static void main(String[] args) {
		BigInteger randomt = new BigInteger("00000000000404CB"
				+ "000000000000000000000000000000000000000000000000", 16);

		System.out.println("Difficulty: "
				+ Tools.findDifficultyFromTarget(randomt));
	}

}
