package coffeecoin.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import coffeecoin.main.BlockchainDbManager;
import coffeecoin.main.Miner;
import coffeecoin.main.UpdateState;
import coffeecoin.main.VerificationTools;
import coffeecoin.ui.MainWindow;

/**
 * This class is instantiated and started each time a new connection is
 * received.
 */
public class NetworkRequestHandler extends Thread {

	private BlockchainDbManager dbman;
	private MainWindow gui;
	private Miner miner;
	private Socket socket;
	private String host;

	public NetworkRequestHandler(Socket socket) throws Exception {
		this.dbman = BlockchainDbManager.getInstance();
		this.gui = MainWindow.getInstance();
		this.miner = Miner.getInstance();
		this.socket = socket;
		this.host = socket.getInetAddress().toString();
		System.out.println("RequestHandler Started:" + host);
		NetworkAction action;
		try {
			action = getAction();
			processRequest(action);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves a NetworkAction from the socket passed in when the
	 * NetworkRequestHandler was instantiated.
	 */
	private NetworkAction getAction() throws IOException,
			ClassNotFoundException {
		InputStream is = socket.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is);
		NetworkAction receivedAction = (NetworkAction) ois.readObject();
		return receivedAction;
	}

	/**
	 * This method defines behavior for each type of NetworkAction.
	 */
	public void processRequest(NetworkAction a) throws Exception {
		if (a instanceof TxAction) {
			TxAction currentTransaction = (TxAction) a;
			boolean validTx = VerificationTools.verifyTransaction(currentTransaction);
			System.out.println("Valid TX:" + validTx);
			float balance = dbman.checkBalance(currentTransaction.getInput());
			if ((balance >= currentTransaction.getAmt() || ((TxAction) a)
					.getInput().equals("coinbase")) && validTx) {
				dbman.addTx(currentTransaction);
				miner.checkDb();
				gui.updatePanels();
			}
		}
		if (a instanceof BlockMinedAction) {
			BlockMinedAction blockMined = (BlockMinedAction) a;
			boolean valid = VerificationTools.verifyBlock(blockMined);
			if (valid) {
				dbman.addBlock(blockMined);
				miner.checkDb();
				gui.updatePanels();
			}

		}
		if (a instanceof UpdateAction) {
			UpdateAction update = (UpdateAction) a;
			UpdateState upToDate = dbman.hasNewer(update);
			System.out.println("Update received:" + upToDate);
			switch (upToDate) {
			case OLDER: {
				if (!socket.getInetAddress().toString().equals("/127.0.0.1")) {
					System.out.println("[+] Responding to InetAddress: "
							+ socket.getInetAddress());
					Socket newSocket = new Socket(socket.getInetAddress(),
							NetworkConfiguration.PORT);
					NetworkSender netSender = new NetworkSender(newSocket,
							new UpdateAction());
					netSender.start();
				}
				break;
			}
			case EQUAL: {
				// Do nothing if they're the same
				break;
			}
			case NEWER: {
				System.out.println("\"NEWER\" code block executing");
				dbman.updateDb(update.getDb());
				miner.checkDb();
				gui.updatePanels();
				gui.displayMessage("DB Updated");
				break;
			}
			}
		}
		if (a instanceof NetworkTestAction) {
			gui.displayMessage("NetworkTestAction Received");
		}
	}
}
