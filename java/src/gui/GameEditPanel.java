package gui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class GameEditPanel extends JPanel {
	JPanel textPanel = new JPanel();
	JPanel buttonPanel = new JPanel();
	JPanel removeTextPanel = new JPanel();
	JPanel removeButtonPanel = new JPanel();

	JComboBox<String> GameList; 
	JButton submitButton = new JButton("Submit add");
	
	JComboBox<String> removeGameList; 
	JButton removeSubmitButton = new JButton("Submit remove");
	
	JLabel infoLabel = new JLabel("Pick a game to add");
	
	JLabel removeInfoLabel = new JLabel("Pick a game to remove");
	
	private ConnectionManager connectionManager;
	private UserManager userManager;
	
	public GameEditPanel(ConnectionManager connectionManager, UserManager userManager) {
		this.connectionManager = connectionManager;
		this.userManager = userManager;
		
		GameList = new JComboBox<String>(getAllStoredGames());
		GameList.setEditable(true);
			
		textPanel.add(GameList, BorderLayout.NORTH);
		buttonPanel.add(submitButton);
		submitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (addGameToUser(GameList.getSelectedItem().toString())) {
					JOptionPane.showMessageDialog(null, "Successfully Added Game to account!");
				}
			}
			
		}); 
		
		this.removeGameList = new JComboBox<String>(getAllPersonalGames());
		this.removeGameList.setEditable(true);
		
		removeTextPanel.add(removeGameList, BorderLayout.NORTH);
		removeButtonPanel.add(removeSubmitButton);
		removeSubmitButton.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				if (RemoveGameFromUser(removeGameList.getSelectedItem().toString())) {
					JOptionPane.showMessageDialog(null, "Successfully Removed Game from account!");
				}
			}
		
		}); 
		
		this.add(infoLabel);
		this.add(textPanel);
		this.add(buttonPanel);
		
		this.add(removeInfoLabel);
		this.add(removeTextPanel);
		this.add(removeButtonPanel);
	}
	
	private String[] getAllStoredGames() {
		
		if(userManager.getUser() == null) {
			String[] current = {"Log In to use"};
			return current;
		}
		
		Connection connection = connectionManager.getConnection();
		
		ArrayList<String> results = new ArrayList<String>();
						
		CallableStatement stmt;
		try {
			stmt = connection.prepareCall("{call getAllGames}");
			ResultSet rs = stmt.executeQuery();

			int gameNameIndex = rs.findColumn("Name");
			
			while (rs.next()) {
				String CurrentGame = rs.getString(gameNameIndex);
				results.add(CurrentGame);
			}
			
			String []dsf = new String[results.size()];
			
			return results.toArray(dsf);
			
		} catch (SQLException e) {
			System.out.println(e);
			String[] current = {"Error Encountered"};
			return current;
		}
	}
	
	private boolean addGameToUser(String inputGame) {
		Connection connection = connectionManager.getConnection();
		int returnCode = 3;
		
		try {
			CallableStatement stmt = connection.prepareCall("{? = call addGameToUser(?,?)}");
			stmt.registerOutParameter(1, Types.INTEGER);
			stmt.setString(2, inputGame);
			stmt.setString(3, userManager.getUser());
			stmt.executeUpdate();
			
			returnCode = stmt.getInt(1);
			System.out.println(returnCode);
			
			if (returnCode == 1 || returnCode == 2) {
				JOptionPane.showMessageDialog(null, "Game is null or nonexistant, please select a game");
				return false;
			} else if (returnCode == 3 || returnCode == 4) {
				JOptionPane.showMessageDialog(null, "Username is null or nonexistant, please sign in.");
				return false;
			} else if (returnCode != 0) {
				JOptionPane.showMessageDialog(null, "An error occured while adding this game. Please try again");
				return false;
			}
			
			redoPersonalGames();
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(returnCode);
			if (returnCode == 3 || returnCode == 4) {
				JOptionPane.showMessageDialog(null, "Username is null or nonexistant, please sign in.");
				return false;
			} else if (returnCode == 1 || returnCode == 2) {
				JOptionPane.showMessageDialog(null, "Game is null or nonexistant, please select a game");
				return false;
			} else if (returnCode != 0) {
				JOptionPane.showMessageDialog(null, "An error occured while Adding this game.");
				return false;
			}
		}
		
		return false;
	}
	
	public void redoPersonalGames() {
		this.removeGameList.removeAllItems();
		String[] currentList = getAllPersonalGames();
		
		for(int i = 0; i < currentList.length; i++) {
			this.removeGameList.addItem(currentList[i]);
		}
	}
	
	public void redoStoredGameList() {
		this.GameList.removeAllItems();
		String[] currentList = getAllStoredGames();
		
		for(int i = 0; i < currentList.length; i++) {
			this.GameList.addItem(currentList[i]);
		}
	}
	
	private String[] getAllPersonalGames() {
		
		if(userManager.getUser() == null) {
			String[] current = {"Log In to use"};
			return current;
		}
		
		Connection connection = connectionManager.getConnection();
		
		ArrayList<String> results = new ArrayList<String>();
						
		CallableStatement stmt;
		try {
			stmt = connection.prepareCall("{call getPersonalGames(?)}");
			stmt.setString(1, userManager.getUser());
			
			ResultSet rs = stmt.executeQuery();

			int gameNameIndex = rs.findColumn("Name");
			
			while (rs.next()) {
				String CurrentGame = rs.getString(gameNameIndex);
				results.add(CurrentGame);
			}
			
			String []dsf = new String[results.size()];
			
			return results.toArray(dsf);
			
		} catch (SQLException e) {
			e.printStackTrace();
			String[] current = {"Error Encountered"};
			return current;
		}
	}
	
	private boolean RemoveGameFromUser(String inputGame) {
		Connection connection = connectionManager.getConnection();
		int returnCode = 3;
		
		try {
			CallableStatement stmt = connection.prepareCall("{? = call RemoveGameFromUser(?,?)}");
			stmt.registerOutParameter(1, Types.INTEGER);
			stmt.setString(2, inputGame);
			stmt.setString(3, userManager.getUser());
			stmt.executeUpdate();
			
			returnCode = stmt.getInt(1);
			System.out.println(returnCode);
			
			if (returnCode == 1 || returnCode == 2) {
				JOptionPane.showMessageDialog(null, "Game is null or nonexistant, please select a game");
				return false;
			} else if (returnCode == 3 || returnCode == 4) {
				JOptionPane.showMessageDialog(null, "Username is null or nonexistant, please sign in.");
				return false;
			} else if (returnCode == 5) {
				JOptionPane.showMessageDialog(null, "You do not have that game added to your library, please try again");
				return false;
			} else if (returnCode != 0) {
				JOptionPane.showMessageDialog(null, "An error occured while Removing this game. Please try again");
				return false;
			}
			
			this.redoPersonalGames();
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(returnCode);
			if (returnCode == 3 || returnCode == 4) {
				JOptionPane.showMessageDialog(null, "Username is null or nonexistant, please sign in.");
				return false;
			} else if (returnCode == 1 || returnCode == 2) {
				JOptionPane.showMessageDialog(null, "Game is null or nonexistant, please select a game");
				return false;
			} else if (returnCode == 5) {
				JOptionPane.showMessageDialog(null, "You do not have that game added to your library, please try again");
				return false;
			} else if (returnCode != 0) {
				JOptionPane.showMessageDialog(null, "An error occured while Removing this game.");
				return false;
			}
		}
		
		return false;
	}
}