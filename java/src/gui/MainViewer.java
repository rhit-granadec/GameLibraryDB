package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class MainViewer {
	
	private ConnectionManager connectionManager;
	private UserManager userManager;
	private JTabbedPane LoginTabs;
	private JTabbedPane MainTabs;
	private JFrame frame;
	
	
	public MainViewer(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		this.userManager = new UserManager();
	}
	
	
	public void viewerMain() {
		final String frameTitle = "Game Library - Register";
		final int frameWidth = 1080;
		final int frameHeight = 720;
		final int frameXLoc = 100;
		final int frameYLoc = 0;
		
		//Set up frame
		this.frame = new JFrame();
		frame.setTitle(frameTitle);
		frame.setSize(frameWidth, frameHeight);
		frame.setLocation(frameXLoc, frameYLoc);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		        connectionManager.closeConnection();
		    }
		});
		
		//Create tabs
		this.LoginTabs = new JTabbedPane();
		this.MainTabs = new JTabbedPane();
		
		FullTablePanel tableScreen = new FullTablePanel(connectionManager, userManager);
		
		//Add screens as new tabs
		RegisterPanel registerScreen = new RegisterPanel(connectionManager, userManager);
		GameEditPanel gameScreen = new GameEditPanel(connectionManager, userManager);
		NotePanel notePanel = new NotePanel(connectionManager, userManager);
		ReviewPanel reviewPanel = new ReviewPanel(connectionManager, userManager);
		GameBrowserPanel gameBrowserPanel = new GameBrowserPanel(connectionManager, userManager);
		
		
		UpdateManager updateManager = new UpdateManager(tableScreen, gameScreen, gameBrowserPanel, this, notePanel, reviewPanel);
		
		gameScreen.setUpdateManager(updateManager);
		gameBrowserPanel.setUpdateManager(updateManager);
		
		LoginPanel LoginScreen = new LoginPanel(connectionManager, userManager, updateManager);
		
		LoginTabs.addTab("Register", null, registerScreen, "Register an account");
		LoginTabs.addTab("Login Here", null, LoginScreen, "Login to your account here");
		MainTabs.addTab("Edit Games", null, gameScreen, "Edit your library");
		MainTabs.addTab("See Games", null, tableScreen, "Browse your library");
		MainTabs.addTab("Notes", null, notePanel, "Add Notes");
		MainTabs.addTab("Reviews", null, reviewPanel, "Add Reviews");
		MainTabs.addTab("Browse Games", null, gameBrowserPanel, "Browse Games");
		
		frame.add(LoginTabs);
		
		frame.setVisible(true);
	}
	
	public void UserLoginUpdate() {
		frame.setVisible(false);
		frame.setTitle("Game Library");
		frame.remove(LoginTabs);
		frame.add(MainTabs);
		frame.setVisible(true);
	}
}
