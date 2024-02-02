package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginPanel extends JPanel {
	private static final Random RANDOM = new SecureRandom();
	private static final Base64.Encoder enc = Base64.getEncoder();
	private static final Base64.Decoder dec = Base64.getDecoder();
	JPanel textPanel = new JPanel();
	JPanel buttonPanel = new JPanel();
	private ConnectionManager connectionManager;
	private UserManager userManager;
	private UpdateManager updateManager;
	JTextField usernameField = new JTextField(20);
	JPasswordField passwordField = new JPasswordField(20);
	JLabel infoLabel = new JLabel("Enter a Username and Password");
	JButton loginButton = new JButton("Login");

	public LoginPanel(ConnectionManager connectionManager, UserManager userManager, UpdateManager updateManager) {
		setLayout(new GridLayout(20, 10));
		this.connectionManager = connectionManager;
		this.userManager = userManager;
		this.updateManager = updateManager;
		JLabel userNameLabel = new JLabel("Username:");
		JLabel passwordLabel = new JLabel("Password:");

		loginButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String givenUsername = usernameField.getText();
				if (login(givenUsername, passwordField.getPassword())) {
					userManager.setUser(givenUsername);
					System.out.println("HitREDO");
					updateManager.UserUpdate();
					infoLabel.setText("Login Successful!");
				}
			}
		});

		add(userNameLabel);
		add(usernameField);
		add(passwordLabel);
		add(passwordField);
		add(new JLabel());
		add(loginButton);
		add(infoLabel);
	}

	public boolean login(String username, char[] password) {
		Connection connection = connectionManager.getConnection();
		String query = "SELECT Salt, HashPass FROM [User] WHERE Username = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, username); 
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				byte[] salt = rs.getBytes("Salt");
				String correctHash = rs.getString("HashPass"); 
				String inputHash = hashPassword(salt, password);
				if(correctHash.equals(inputHash)) {
					this.userManager.setUser(username);
					 JOptionPane.showMessageDialog(null, "Login Successful");
					return true; 
				}
				
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Login Failed");
		}
	return false;
	}

//	 private boolean login(String username, char[] password) {
//    	 try {
//		        Connection connection = this.connectionManager.getConnection();
//		        String query = "SELECT PasswordSalt, PasswordHash FROM [User] WHERE Username = ?";
//		        PreparedStatement preparedStatement = connection.prepareStatement(query);
//		        preparedStatement.setString(1, username);
//
//		        ResultSet resultSet = preparedStatement.executeQuery();
//
//		        if (resultSet.next()) {
//		            String storedSalt = resultSet.getString("PasswordSalt");
//		            String storedHash = resultSet.getString("PasswordHash");
//		            System.out.println(storedSalt.length());
//
//		            byte[] salt = Base64.getDecoder().decode(storedSalt);
//		            String enteredHash = hashPassword(salt, password);
//
//		            if (storedHash.equals(enteredHash)) {
//		                JOptionPane.showMessageDialog(null, "Login Successful");
//		                return true;
//		            } else {
//		                JOptionPane.showMessageDialog(null, "Login Failed");
//		                return false;
//		            }
//		        } else {
//		            JOptionPane.showMessageDialog(null, "Login Failed");
//		            return false;
//		        }
//
//		    } catch (SQLException e) {
//		        JOptionPane.showMessageDialog(null, "Login Failed");
//		        return false;
//		    }
//    }
//	 
	 public String hashPassword(byte[] salt, char[] password) {

			KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
			SecretKeyFactory f;
			byte[] hash = null;
			try {
				f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
				hash = f.generateSecret(spec).getEncoded();
			} catch (NoSuchAlgorithmException e) {
				JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
				e.printStackTrace();
			}
			return getStringFromBytes(hash);
		}

	public byte[] getNewSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}

	public String getStringFromBytes(byte[] data) {
		return enc.encodeToString(data);
	}

}
