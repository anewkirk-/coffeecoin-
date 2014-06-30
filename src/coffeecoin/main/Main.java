package coffeecoin.main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import coffeecoin.ui.MainWindow;

/**
 * This class launches the GUI, it is the main
 * entry point to the application.
 * @author 
 */
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch(Exception e) {}
				
				try{
					MainWindow gui = MainWindow.getInstance();
					gui.setVisible(true);
				} catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Application failed to start.\nSorry about that." + e.getMessage());
				}
			}
		});
	}
}
