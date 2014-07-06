package coffeecoin.network;

import org.tmatesoft.sqljet.core.SqlJetException;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.net.*;

/**
 * This class listens for incoming network connections, and launches a new
 * NetworkRequestHandler for each one. Once launched, it listens indefinitely
 * until application exit
 * 
 * @author
 */
public class NetworkListener extends Thread {

	private ServerSocket servSocket;
	private static NetworkListener instance;

	private NetworkListener() {
	}

	public static NetworkListener getInstance() {
		if (instance == null) {
			instance = new NetworkListener();
		}
		return instance;
	}

	public void run() {
		try {
			servSocket = new ServerSocket(NetworkConfiguration.PORT);
			while (true) {
				Socket newClient = servSocket.accept();
				ClientAgent.getInstance().addPeer(newClient.getInetAddress());
				NetworkRequestHandler handler = new NetworkRequestHandler(
						newClient);
				handler.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SqlJetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
