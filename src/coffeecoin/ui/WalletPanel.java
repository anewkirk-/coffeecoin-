package coffeecoin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.tmatesoft.sqljet.core.SqlJetException;

import coffeecoin.main.BlockchainDbManager;
import coffeecoin.main.Configuration;
import coffeecoin.main.Tools;
import coffeecoin.main.WalletDbManager;
import coffeecoin.network.ClientAgent;
import coffeecoin.network.TxAction;

/**
 * newTx() needs to be rewritten, it appears to be broken.
 * write up some pseudo-code and try again.
 */

/**
 * This panel is displayed on the "Wallet" tab 
 * on the gui. It holds the gui components themselves,
 * as well as the logic for sending transactions.
 * @author 
 */
public class WalletPanel extends JPanel {
	
	private static final long serialVersionUID = 3649700800374438134L;
	private JList<Object> list;
	private JLabel balance;
	private JButton sendButton;
	private JButton addButton;

	public WalletPanel() throws SqlJetException {
		list = new JList<Object>(); //data has type Object[]
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		balance = new JLabel("BALANCE");
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(590, 175));
		sendButton = new JButton("New Transaction");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					newTx();
				} catch (SqlJetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchProviderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SignatureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		addButton = new JButton("Add Address");
		this.add(balance, BorderLayout.PAGE_START);
		this.add(listScroller, BorderLayout.CENTER);
		this.add(sendButton, BorderLayout.SOUTH);
		this.add(addButton, BorderLayout.SOUTH);
		this.repaint();
		updateBalance();
		updateAddressList();
	}
	
	/**
	 * 
	 */
	public void updateAddressList() throws SqlJetException {
		WalletDbManager wdbman = WalletDbManager.getInstance();
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		ArrayList<String> data = wdbman.getAddresses();
		ArrayList<String> shortenedData = new ArrayList<String>();
		for(Iterator<String> it = data.iterator(); it.hasNext();) {
			String ad = it.next();
			shortenedData.add("&"+ dbman.checkBalance(ad) + "  -  " + ad);//.substring(54));
		}
		String[] dataArray = (String[]) shortenedData.toArray(new String[data.size()]);
		list.setListData(dataArray);
		list.setVisibleRowCount(data.size());
		this.repaint();
	}
	
	/**
	 * 
	 */
	public void updateBalance() throws SqlJetException {
		WalletDbManager wdbman = WalletDbManager.getInstance();
		double bal = wdbman.getBalance();
		balance.setText("&" + bal);
		this.repaint();
	}
	
	/**
	 * 
	 */
	private void newTx() throws Exception {
		long amount = Long.valueOf(JOptionPane.showInputDialog("Input transaction amount:"));
		String output = JOptionPane.showInputDialog("Enter Recipient Address:");
		WalletDbManager wdb = WalletDbManager.getInstance();
		long bal = wdb.getBalance();
		long change = 0;
		boolean lastTxMade = false;
		System.out.println("[+] Total Balance:" + bal);
		if(bal >= amount) {
			System.out.println("[-] Attempting transaction...");
			BlockchainDbManager dbman = BlockchainDbManager.getInstance();
			String[] addressForChange = Tools.generateKeypair();
			ClientAgent ca = ClientAgent.getInstance();
			ArrayList<String> addresses = wdb.getAddresses();
			ArrayList<String> addsUsed = new ArrayList<String>();
			long total = 0;
			for(Iterator<String> it = addresses.iterator(); it.hasNext();) {
				String current = it.next();
				long currentBal = dbman.checkBalance(current);
				if(total + currentBal <= amount && currentBal > 0) {
					addsUsed.add(current);
					total += currentBal;
				} else if(!lastTxMade) {
					addsUsed.add(current);
					change = (total + currentBal) - amount;
//					if(change < 0) {
//						change = 0;
//					}
					lastTxMade = true;
				}
			}
			for(Iterator<String> it = addsUsed.iterator(); it.hasNext();) {
				String current = it.next();
				long currentBal = dbman.checkBalance(current);
				boolean changeTxMade = false;
				System.out.println("[-] Change owed:" + change);
				if(currentBal > change && currentBal > 0 ) {
					if(change > 0 && !changeTxMade) {
						//SEND TX TO CHANGE ADDRESS
						String privKey = wdb.getPrivateKey(current);
						String pubKey = wdb.getPublicKeyFromAddress(current);
						String address = Tools.addressFromKey(addressForChange[0]);
						TxAction changeAction = buildTx(current, change,  address, pubKey, privKey);
						System.out.println("[-] Change transaction verified:  " + Tools.verifyTxSignature(changeAction));
						
						wdb.addNewAddress(address, addressForChange[0], addressForChange[1]);
						dbman.addTx(changeAction);
						ca.sendAction(changeAction);
						//SEND REST TO RECIPIENT
						TxAction rAction = buildTx(current, (currentBal-change), output, pubKey, privKey);
						System.out.println("[-] Transaction verified: " + Tools.verifyTxSignature(rAction));
						dbman.addTx(rAction);
						ca.sendAction(rAction);
						it.remove();
						changeTxMade = true;
					}
				}
			}
			for(Iterator<String> it = addsUsed.iterator(); it.hasNext();) {
				String current = it.next();
				long currentBal = dbman.checkBalance(current);
				if(currentBal > 0) {
					String privKey = wdb.getPrivateKey(current);
					String pubKey = wdb.getPublicKeyFromAddress(current);
					TxAction t = buildTx(current, currentBal, output, pubKey, privKey);
					if(Tools.verifyTransaction(t)) {
						System.out.println("[-] Transaction verified.");
						dbman.addTx(t);
						ca.sendAction(t);
					} else {
						System.out.println("[-] Transaction could not be verified.\nSomething broke.");
					}
				}
			}
		}
		
	}
	
	/**
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	private TxAction buildTx(String input, long amount, String output, String publickey, String privatekey) throws SqlJetException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException {
//		System.out.println("{Building tx:}");
//		System.out.println("  {input} : " + input);
//		System.out.println("  {amount} : " + amount);
//		System.out.println("  {output} : " + output);
		BlockchainDbManager dbman = BlockchainDbManager.getInstance();
		Date now = new Date();
		long timestamp = now.getTime();
		int blockno = dbman.getCurrentBlockNo(Configuration.DB_NAME) - 1;
		TxAction result = new TxAction(input, amount, output, publickey, timestamp, blockno);
		result.setPublicKey(publickey);
		result.setSignature(Tools.signTransaction(result, privatekey));
		return result;
	}
}
