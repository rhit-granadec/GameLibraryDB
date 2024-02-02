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
		JFrame frame = new JFrame();
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
		JTabbedPane tabs = new JTabbedPane();
		
		FullTablePanel tableScreen = new FullTablePanel(connectionManager, userManager);
		
		//Add screens as new tabs
		RegisterPanel registerScreen = new RegisterPanel(connectionManager, userManager);
		//Add Login screens as new tabs
		GameEditPanel gameScreen = new GameEditPanel(connectionManager, userManager);
		SearchBarPanel searchPanel = new SearchBarPanel(connectionManager, userManager);
		
		NotePanel NoteScreen = new NotePanel(connectionManager, userManager);
		tabs.addTab("Add Note Here", null, NoteScreen, "Add game notes here");

		ReviewPanel ReviewScreen = new ReviewPanel(connectionManager, userManager);
		tabs.addTab("Add Review Here", null, ReviewScreen, "Add game reviews here");
		
		UpdateManager updateManager = new UpdateManager(tableScreen, gameScreen);
		
		LoginPanel LoginScreen = new LoginPanel(connectionManager, userManager, updateManager);
		tabs.addTab("Register", null, registerScreen, "Register an account");
		tabs.addTab("Login Here", null, LoginScreen, "Login to your account here");
		tabs.addTab("Search", null, searchPanel, "Search for games");
		tabs.addTab("Edit Games", null, gameScreen, "Edit your library");
		tabs.addTab("See Games", null, tableScreen, "Browse your library");

		
		frame.add(tabs);
		
		frame.setVisible(true);
	}
}
