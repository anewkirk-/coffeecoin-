package coffeecoin.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.tmatesoft.sqljet.core.SqlJetException;

import coffeecoin.main.DbBuilder;
import coffeecoin.main.Genesis;
import coffeecoin.main.Miner;
import coffeecoin.main.Tools;
import coffeecoin.network.ClientAgent;
import coffeecoin.network.NetworkTestAction;
import coffeecoin.network.TxAction;
import coffeecoin.network.UpdateAction;

/**
 * This is the main class for the GUI
 * It should always be launched from Main
 * @author 
 */
public class MainWindow extends JFrame {
	
	private JTabbedPane tabbedPane;
	private MinerPanel minerPanel;
	private NetworkPanel networkPanel;
	private WalletPanel walletPanel;
	private ClientAgent clientAgent;
	private static final long serialVersionUID = -7724928765762657015L;
	private static MainWindow instance = null;

	private MainWindow() throws SqlJetException {
		super("CoffeeCoin");
		this.initUI();
		this.clientAgent = ClientAgent.getInstance();
	}
	
	public static MainWindow getInstance() throws SqlJetException {
		if(instance == null) {
			instance = new MainWindow();
		}
		return instance;
	}
	
	private void initUI() throws SqlJetException {
		//set up swing components here
		this.tabbedPane = new JTabbedPane();
		this.minerPanel = new MinerPanel();
		this.networkPanel = new NetworkPanel();
		this.walletPanel = new WalletPanel();
		tabbedPane.add(minerPanel, "Mining");
		tabbedPane.add(networkPanel, "Network");
		tabbedPane.add(walletPanel, "Wallet");
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int selected = tabbedPane.getSelectedIndex();
				switch(selected) {
					case 0:
						minerPanel.repaint();
						break;
					case 1:
						networkPanel.repaint();
						break;
					case 2:
					try {
						walletPanel.updateAddressList();
						walletPanel.updateBalance();
					} catch (SqlJetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						walletPanel.repaint();
						break;
				}
			}
		});
		this.getContentPane().add(tabbedPane);
		this.setJMenuBar(initMenuBar());
		this.setSize(new Dimension(600,310));
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private JMenuBar initMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu debugMenu = new JMenu("Debug");
		JMenuItem sendNetTestActionItem = new JMenuItem("Send Network Test Action");
		sendNetTestActionItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					clientAgent.sendAction(new NetworkTestAction());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		JMenuItem sendTestTxItem = new JMenuItem("Send Test Transaction");
		JMenuItem addPeerMenuItem = new JMenuItem("Add Peer...");
		addPeerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newPeer = JOptionPane.showInputDialog("Enter IP for new peer:");
				try {
					clientAgent.addPeer(InetAddress.getByName(newPeer));
					clientAgent.sendAction(new UpdateAction());
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (SqlJetException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
			}
		});
		JMenuItem startNetListenerItem = new JMenuItem("Start Network Listener");
		startNetListenerItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clientAgent.startNetListener();
			}
		});
		JMenuItem dbBuilderMenuItem = new JMenuItem("Run DB Builder");
		dbBuilderMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					DbBuilder.main(new String[] {});
				} catch (SqlJetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		JMenuItem genesisBlockMenuItem = new JMenuItem("Build Genesis Block");
		genesisBlockMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Genesis.main(new String[] {});
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		JMenuItem launchMinerMenuItem = new JMenuItem("Launch Miner");
		launchMinerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Miner miner = Miner.getInstance();
					miner.start();
				} catch (SqlJetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		debugMenu.add(launchMinerMenuItem);
		debugMenu.add(genesisBlockMenuItem);
		debugMenu.add(dbBuilderMenuItem);
		debugMenu.add(addPeerMenuItem);
		debugMenu.add(sendNetTestActionItem);
		debugMenu.add(sendTestTxItem);
		debugMenu.add(startNetListenerItem);
		fileMenu.add(exitMenuItem);
		mb.add(fileMenu);
		mb.add(debugMenu);
		return mb;
	}

	public void displayMessage(String s) {
		JOptionPane.showMessageDialog(null, s);
	}

	public void updatePanels() {
		// TODO Auto-generated method stub
		
	}

}
