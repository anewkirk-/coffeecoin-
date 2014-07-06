package coffeecoin.test;

import coffeecoin.main.Tools;
import coffeecoin.main.VerificationTools;
import coffeecoin.network.TxAction;

public class KeypairTest {

	public static void main(String[] args) throws Exception {
		String[] keypair = Tools.generateKeypair();
		String pubkey = keypair[0];
		String privkey = keypair[1];
		System.out.println("pubkey: " + pubkey);
		System.out.println("privkey: " + privkey);

		TxAction tx = new TxAction(
				"RYoLxZA3zWQHDPJY951dBWwTActfAZbnEq1rncTG5EcB",
				100,
				"WvQ6VMURJMJk7XxACYzZ7HE8SfRbXQ5wPMnWtz6wgE3H",
				"3059301306072A8648CE3D020106082A8648CE3D03010703420004FBCDD6170BCF87F6E501D42CE25A4D76A3E4C54BE5CCE09AE1A4C5B86EC0C62DA7DE90D56DFFD3D48BF6992EEBBA2E78CADAE58FA09F0C75A1C66D4579713636",
				1403964829546L, 4);
		String sig = Tools
				.signTransaction(
						tx,
						"3041020100301306072A8648CE3D020106082A8648CE3D0301070427302502010104206378F17367A1C3CAE94613691E5638EAD81D9DE7BEBF46970B7FD4423B86EAAB");
		tx.setSignature(sig);
		tx.setPublicKey("3059301306072A8648CE3D020106082A8648CE3D03010703420004FBCDD6170BCF87F6E501D42CE25A4D76A3E4C54BE5CCE09AE1A4C5B86EC0C62DA7DE90D56DFFD3D48BF6992EEBBA2E78CADAE58FA09F0C75A1C66D4579713636");

		System.out.println(VerificationTools.verifyTransaction(tx));
	}
}
