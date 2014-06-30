package coffeecoin.network;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class sends a NetworkAction over a Socket.
 * @author 
 */
public class NetworkSender extends Thread {
	
	private Socket socket;
	private NetworkAction action;

	public NetworkSender(Socket socket, NetworkAction a) {
		this.socket = socket;
		this.action = a;
	}
	
	/**
	 * This method is responsible for actually sending the 
	 * action over the socket.
	 */
	@Override
	public void run() {
		try {
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream ois = new ObjectOutputStream(os);
			ois.writeObject(action);
			ois.flush();
			ois.close();
			os.flush();
			os.close();
		} catch(Exception e){}
	}

}
