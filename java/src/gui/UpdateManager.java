package gui;


public class UpdateManager {
	
	private FullTablePanel ftp;
	private GameEditPanel gep;
	
	public UpdateManager(FullTablePanel FTP, GameEditPanel GEP) {
		this.ftp = FTP;
		this.gep = GEP;
	}
	
	public void UserUpdate() {
		this.ftp.UpdateView();
		this.gep.redoPersonalGames();
		this.gep.redoStoredGameList();
	}

}