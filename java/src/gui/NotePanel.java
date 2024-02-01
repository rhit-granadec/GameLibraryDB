package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class NotePanel extends JPanel {
	private ConnectionManager connectionManager;
	private UserManager userManager;
	private JPanel gamePanel = new JPanel();
	private JComboBox<String> gameList = new JComboBox<>();
	private JButton chooseButton = new JButton("Choose");
	private JLabel infoLabel = new JLabel("Pick a game to add notes");
	private JLabel addLabel = new JLabel("Add the note using this textbox on the left.");
	private JLabel noteLabel = new JLabel("Note of this game is on the textbox on the right");

	private JPanel actionPanel = new JPanel();
	private JTextField noteInput = new JTextField(20);
	private JTextField noteOutput = new JTextField(20);
	private JButton refreshGameListButton = new JButton("RefreshGameList");
	private JButton addNoteButton = new JButton("Add");
	private JButton deleteNoteButton = new JButton("Delete");
	private JButton updateNoteButton = new JButton("Update");
	private String selectedGame= "";
	private int currentGameNoteID;
	public NotePanel(ConnectionManager connectionManager, UserManager userManager) {
		this.connectionManager = connectionManager;
		this.userManager = userManager;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		gameList.setEditable(true);
		gamePanel.add(gameList);
		gamePanel.add(chooseButton);
		gamePanel.add(infoLabel);
		gamePanel.add(refreshGameListButton);
		add(gamePanel);

		actionPanel.add(addLabel);
		actionPanel.add(noteInput);
		actionPanel.add(noteLabel);
		actionPanel.add(noteOutput);
		actionPanel.add(addNoteButton);
		actionPanel.add(deleteNoteButton);
		actionPanel.add(updateNoteButton);
		add(actionPanel);

		refreshGameListButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				redoPersonalGames();
			}
		}

		);
		
		chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedGame = (String) gameList.getSelectedItem(); // Store the selected game
                JOptionPane.showMessageDialog(null, "Selected game: " + selectedGame);
                currentGameNoteID = chooseGameGetNoteID();
                if(currentGameNoteID==-1) {
                	JOptionPane.showMessageDialog(null, "This game does not have a note yet");
                }
                else {
                System.out.println("chosen noteID: "+ currentGameNoteID);
                noteOutput.setText(readNoteFromDatabase(currentGameNoteID));}
            }
        });
    

		addNoteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNoteToDatabase();
			}
		});
		
		deleteNoteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteNoteFromDatabase(currentGameNoteID);
			}
		}
				
				);
		
		updateNoteButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String newText = noteInput.getText();
		        try {
					System.out.println(connectionManager.getConnection().isClosed());
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        updateNoteInDatabase(currentGameNoteID, newText);
		    }
		});


		redoPersonalGames();
	}

	public void redoPersonalGames() {
		this.gameList.removeAllItems();
		String[] currentList = getAllPersonalGames();

		System.out.println("CurrentGameWork, list incoming");
		System.out.println(currentList.toString());

		for (int i = 0; i < currentList.length; i++) {
			System.out.println(currentList[i]);
			this.gameList.addItem(currentList[i]);
		}
	}

	private String[] getAllPersonalGames() {

		if (userManager.getUser() == null) {
			String[] current = { "Log In to use" };
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

			String[] dsf = new String[results.size()];
			return results.toArray(dsf);

		} catch (SQLException e) {
			e.printStackTrace();
			String[] current = { "Error Encountered" };
			return current;
		}
	}
	
	private int chooseGameGetNoteID() {
	    int noteID = -1; // Default value if noteID is not found or an error occurs

	    if (selectedGame.isEmpty() || selectedGame.equals("Log In to use")) {
	        JOptionPane.showMessageDialog(this, "Please select a game first.", "Error", JOptionPane.ERROR_MESSAGE);
	        return noteID;
	    }

	    

	    try {
	    	Connection connection= connectionManager.getConnection();
		    CallableStatement stmt;
		    ResultSet rs;
	        stmt = connection.prepareCall("{call GetNoteID(?, ?)}");
	        stmt.setString(1, selectedGame);
	        stmt.setString(2, userManager.getUser());

	        rs = stmt.executeQuery();

	        if (rs.next()) {
	            noteID = rs.getInt("Id"); 

	        } 
//	        System.out.println(connection.isClosed());
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(this, "Error retrieving NoteID: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	    }

	    return noteID;
	}
	
	private String readNoteFromDatabase(int noteId) {
	    String noteText = null; 

	    try  {
	    	Connection conn = connectionManager.getConnection();
	        CallableStatement stmt = conn.prepareCall("{call readNote(?)}");
	        stmt.setInt(1, noteId);
	        ResultSet rs = stmt.executeQuery();
	        
	        if (rs.next()) {
	            noteText = rs.getString("text"); 
	        } else {
	            JOptionPane.showMessageDialog(this, "Note not found.", "Error", JOptionPane.ERROR_MESSAGE);
	        }
//	        System.out.println(connectionManager.getConnection().isClosed());

	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(this, "Error reading note: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	    }

	    return noteText; 
	}



	private void addNoteToDatabase() {
		String tempText="";
		String currentUser = userManager.getUser();
		if (selectedGame.isEmpty()|| selectedGame=="" ||selectedGame=="Log In to use") {
	        JOptionPane.showMessageDialog(this, "Please select a game first.", "Error", JOptionPane.ERROR_MESSAGE);
	        return;
	    }
    
	    String noteText = noteInput.getText();

	    try  {
	    	Connection conn = connectionManager.getConnection();
	         CallableStatement stmt = conn.prepareCall("{call addNote(?, ?, ?)}");
	        stmt.setString(1, selectedGame);
	        stmt.setString(2, noteText);
	        stmt.setString(3, currentUser);

	        stmt.executeUpdate();
            
	        tempText=noteInput.getText();
	        noteInput.setText("");
	        noteOutput.setText(tempText);
	        
	        JOptionPane.showMessageDialog(this, "Note added successfully.");

	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(this, "Error adding note: " + ex.getMessage(), "Error",
	                JOptionPane.ERROR_MESSAGE);
	    }
	}
	
	private void deleteNoteFromDatabase(int currentNoteId) {
	    if (currentNoteId == -1) {
	        JOptionPane.showMessageDialog(this, "No note selected to delete.", "Error", JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    try  {
	    	Connection conn = connectionManager.getConnection();
	         CallableStatement stmt = conn.prepareCall("{call deleteNote(?)}");
	        stmt.setInt(1, currentNoteId);
	        stmt.executeUpdate();
	        noteOutput.setText("");
	        JOptionPane.showMessageDialog(this, "Note deleted successfully.");

	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(this, "Error deleting note: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	
	
	private void updateNoteInDatabase(int currentNoteId, String newText) {
	    if (currentNoteId == -1) {
	        JOptionPane.showMessageDialog(this, "No note selected to update.", "Error", JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    try {
	    	Connection conn = connectionManager.getConnection();
	        CallableStatement stmt = conn.prepareCall("{call updateNote(?, ?)}");
	    	
	        stmt.setInt(1, currentNoteId);
	        stmt.setString(2, newText);

	        stmt.executeUpdate();
	        JOptionPane.showMessageDialog(this, "Note updated successfully.");

	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(this, "Error updating note: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	
	
	


	
	
}