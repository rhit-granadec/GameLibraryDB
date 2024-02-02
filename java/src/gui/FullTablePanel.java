package gui;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class FullTablePanel extends JPanel {
	
	private ConnectionManager connectionManager;
	private UserManager userManager;
	
	private JScrollPane scrollPane;
	private JTable table;
	
	Object[] columnNames = {"Name", "Release Date", "Description", "Genres"};
	
	public FullTablePanel(ConnectionManager connectionManager, UserManager userManager) {
		this.connectionManager = connectionManager;
		this.userManager = userManager;		
		this.table = new JTable(BuildTable(), columnNames);
		this.scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		this.add(scrollPane);
	}

	public void UpdateView() {
		this.remove(scrollPane);
		this.table = new JTable(BuildTable(), columnNames);
		this.scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		this.add(scrollPane);
	}
	
	public Object[][] BuildTable() {
		
		if(userManager.getUser() == null) {
			Object[][] current = {{"Log In to use", "Log In to use", "Log In to use", "Log In to use"}};
			return current;
		}
		
		Connection connection = connectionManager.getConnection();
		
		ArrayList<Object[]> results = new ArrayList<Object[]>();
						
		CallableStatement stmt;
		try {
			stmt = connection.prepareCall("{call getFrontPage(?)}");
			stmt.setString(1, userManager.getUser());
			
			ResultSet rs = stmt.executeQuery();
			
			int gameNameIndex = rs.findColumn("Name");
			int descriptionIndex = rs.findColumn("Description");
			int releaseDateIndex = rs.findColumn("ReleaseDate");
			int genresIndex = rs.findColumn("Genres");
			
			while (rs.next()) {
				String CurrentGame = rs.getString(gameNameIndex);
				String CurrentDescription = rs.getString(descriptionIndex);
				Date CurrentDate = rs.getDate(releaseDateIndex);
				String CurrentGenres = rs.getString(genresIndex);
				
				Object[] rowValues = {CurrentGame, CurrentDate, CurrentDescription, CurrentGenres};
				
				results.add(rowValues);
			}
			
			Object[][] dsf = new Object[results.size()][4];
			
//			Object[][] dsf2 = results.toArray(dsf);
//			
//			for(int i = 0; i < dsf2.length; i++) {
//				System.out.print("[");
//				for(int j=0; j < dsf2[i].length; j++) {
//					System.out.print(dsf[i][j].toString() + ", ");
//				}
//				System.out.println("]");
//			}
			
			return results.toArray(dsf);
			
		} catch (SQLException e) {
			e.printStackTrace();
			Object[][] current = {{"Error Encountered", "Error Encountered", "Error Encountered", "Error Encountered"}};
			return current;
		}
	}
}