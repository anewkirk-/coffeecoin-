package coffeecoin.network;

import java.util.ArrayList;
import java.util.Iterator;
import java.net.*;
import java.io.*;

/**
 * This class handles all network connections to and from a node.
 * 
 * @author
 */
public class ClientAgent {

	private static ClientAgent instance;
	private NetworkListener netListener;
	private ArrayList<InetAddress> peers;

	private ClientAgent() {
		this.netListener = NetworkListener.getInstance();
		// TODO: set up code to load peers from db
		this.peers = new ArrayList<InetAddress>();
	}

	/**
	 * This is a singleton class
	 */
	public static ClientAgent getInstance() {
		if (instance == null) {
			instance = new ClientAgent();
		}
		return instance;
	}

	/**
	 * Starts a new network listener thread
	 */
	public void startNetListener() {
		this.netListener.start();
	}

	/**
	 * Sends a NetworkAction to each peer in the peer list. Does not send the
	 * action to localhost if it is an UpdateAction
	 */
	public void sendAction(NetworkAction a) throws IOException {
		for (Iterator<InetAddress> peerIterator = peers.iterator(); peerIterator
				.hasNext();) {
			InetAddress currentPeer = peerIterator.next();
			if (!(currentPeer.toString().equals("127.0.0.1") && a instanceof UpdateAction)) {
				Socket socket = new Socket(currentPeer,
						NetworkConfiguration.PORT);
				NetworkSender sender = new NetworkSender(socket, a);
				sender.start();
			}
		}
	}

	public ArrayList<InetAddress> getPeers() {
		return peers;
	}

	public void setPeers(ArrayList<InetAddress> peers) {
		this.peers = peers;
	}

	/**
	 * Checks if an address is already present in the peer list. If not, the
	 * address is added to the list.
	 */
	public void addPeer(InetAddress newPeer) throws UnknownHostException {
		boolean clientAlreadyPresent = false;
		for (Iterator<InetAddress> it = peers.iterator(); it.hasNext();) {
			InetAddress current = it.next();
			if (current.equals(newPeer)) {
				clientAlreadyPresent = true;
			}
		}
		if (!clientAlreadyPresent) {
			InetAddress peer = newPeer;
			peers.add(peer);
		}
	}

}
