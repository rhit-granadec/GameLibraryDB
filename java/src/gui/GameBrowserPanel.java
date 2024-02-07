package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class GameBrowserPanel extends JSplitPane implements MouseListener{
	private ConnectionManager connectionManager;
	private UserManager userManager;
	private UpdateManager updateManager;
	
	private JScrollPane scrollPane;
	private SelectedGamePanel selectedGamePanel;
	private JTable table;
	private SearchBarPanel searchBar;
	private JSplitPane searchSplitPane;
	private JSplitPane selectionSplitPane;
	
	private int previousRow = -1;
	
	String[] columnNames = {"Name", "Release Date", "Description", "Genres", "Platforms", "Studio"};
	
	public GameBrowserPanel(ConnectionManager connectionManager, UserManager userManager) {
		this.selectionSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                table, selectedGamePanel);
		this.searchSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                searchBar, selectionSplitPane);
		
		this.searchBar = new SearchBarPanel(connectionManager);
		this.connectionManager = connectionManager;
		this.userManager = userManager;	
		this.UpdateView();
		
//		this.table = new JTable(BuildTable(), columnNames);
//		this.scrollPane = new JScrollPane(table);
//		this.scrollPane.setPreferredSize(new Dimension(480, 640));
//		this.table.setFillsViewportHeight(true);

		
//		this.add(searchBar, c);
//		this.add(scrollPane, c);
//		this.add(selectedGamePanel, c);
//		this.add(searchSplitPane);
//		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//			
//			@Override
//			public void valueChanged(ListSelectionEvent e) {
//				int rowNum = e.getFirstIndex();
//				String gameName = (String) table.getValueAt(rowNum, 0);
//				String description = (String) table.getValueAt(rowNum, 1);
//				String studio = (String) table.getValueAt(rowNum, 2);
//				String platforms = (String) table.getValueAt(rowNum, 3);
//				String genres = (String) table.getValueAt(rowNum, 4);
//				selectedGamePanel.updateTextAreas(gameName, description, studio, platforms, genres);
//			}
//		});
	}

	public void UpdateView() {
//		this.remove(searchSplitPane);
		this.table = new JTable(BuildTable(), columnNames);
		this.scrollPane = new JScrollPane(table);
//		this.scrollPane.setPreferredSize(new Dimension(720, 480));
		table.setFillsViewportHeight(true);
		this.selectedGamePanel = new SelectedGamePanel(userManager, connectionManager, updateManager);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int rowNum = table.getSelectedRow();
				String gameName = (String) table.getValueAt(rowNum, 0);
				String description = (String) table.getValueAt(rowNum, 1);
				String studio = (String) table.getValueAt(rowNum, 2);
				String platforms = (String) table.getValueAt(rowNum, 3);
				String genres = (String) table.getValueAt(rowNum, 4);
				selectedGamePanel.updateSelectedGame(gameName, description, studio, platforms, genres);
			}
		});
		
		this.selectionSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollPane, selectedGamePanel);
//		this.searchSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                searchBar, new RegisterPanel(connectionManager, userManager));
//		
//		searchSplitPane.setLeftComponent(scrollPane);
//		this.add(searchSplitPane);
		this.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.setTopComponent(searchBar);
		this.setBottomComponent(selectionSplitPane);
	}
	
	public Object[][] BuildTable() {		
		Connection connection = connectionManager.getConnection();
		
		ArrayList<Object[]> results = new ArrayList<Object[]>();
		
		if (this.searchBar.getMostRecentSearch() == null) {
			this.searchBar.blankSearch();
		}
						
		for (String key : this.searchBar.getMostRecentSearch().keySet()) {
			GameSearchResultEntry entry = this.searchBar.getMostRecentSearch().get(key);
			String CurrentGame = entry.getGameName();
			
			String CurrentDescription = entry.getDescription();
			if (CurrentDescription.equals("")) {
				CurrentDescription = "---";
			}
			
			String CurrentDate = entry.getReleaseDate();
			if (CurrentDate.equals("1969-12-31")) {
				CurrentDate = "---";
			}
			
			String CurrentGenres = "";
			for (String genre : entry.getGenres()) {
				CurrentGenres = CurrentGenres + ", " + genre;
			}
			CurrentGenres = CurrentGenres.substring(2, CurrentGenres.length());
			if (CurrentGenres.equals("null")) {
				CurrentGenres = "---";
			}
			
			String CurrentPlatforms = "";
			for (String platform : entry.getPlatformNames()) {
				CurrentPlatforms = CurrentPlatforms + ", " + platform;
			}
			CurrentPlatforms = CurrentPlatforms.substring(2, CurrentPlatforms.length());
			if (CurrentPlatforms.equals("null")) {
				CurrentPlatforms = "---";
			}
			
			String CurrentStudio = entry.getStudioName();
			if (CurrentStudio == null) {
				CurrentStudio = "---";
			}
			
			Object[] rowValues = {CurrentGame, CurrentDate, CurrentDescription, CurrentGenres, CurrentPlatforms, CurrentStudio};
			
			results.add(rowValues);
		}
	
		Object[][] dsf = new Object[results.size()][6];
		
		return results.toArray(dsf);
	}

	public void setUpdateManager(UpdateManager um) {
		this.updateManager = um;
		this.searchBar.setUpdateManager(um);
	}	
	
	private String[] getRow(int rowNum) {
		String[] row = new String[6];
		for (int i = 0; i < 6; i++) {
			row[i] = (String) table.getValueAt(rowNum, i);
		}
		return row;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}