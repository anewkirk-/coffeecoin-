package coffeecoin.test;

import java.math.BigInteger;
import coffeecoin.main.Configuration;
import coffeecoin.main.Tools;

public class Difficulty_Test_2 {
	
	public static void main(String[] args) {
		BigInteger newTarget = Tools.retarget(1, 5673, Configuration.DIFFICULTY_1_TARGET);
		System.out.println("New Target: " + newTarget);
		System.out.println("hex value: 0x" + newTarget.toString(16));
		System.out.println("calc'd diff:" + Tools.findDifficultyFromTarget(newTarget));
	}

}
