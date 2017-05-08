package cn.seeonce.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import cn.seeonce.qq.data.Account;


/**
 * 该类提供有关数据库操作的静态函数
 * @author dawndevil
 */

public class QQSql {
	private static Connection connect;
	
	static{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			String username = "root";
			String password = "1997";
			String url = "jdbc:mysql://localhost:3306/qq";
			
			
			connect = (Connection)DriverManager
					. getConnection(url, username, password);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Connection getConnection(){
		return connect;
	}
	
	public static int sign(String username, String password){
				//创建临时用户
		Account account = new 
				Account(username, password, (int)(System.currentTimeMillis() / 1000));
		
		if(!QQCheck.validUsername(account.getUsername()))
		{JOptionPane.showMessageDialog(null, "username is not valid");return -1;}
		
		if(!QQCheck.validPassword(account.getPassword()))
		{JOptionPane.showMessageDialog(null, "password is not valid");return -2;}
		
		if(QQCheck.accountExist(account.getUsername()))
		{JOptionPane.showMessageDialog(null, "account is exist");return -3;}
		
		Connection conn = QQSql.getConnection();
		//受影响的行数
		int effects = 0;
		
		String sql = "INSERT INTO qq_user(username,password,signtime) VALUES(?,?,?)";
		
		try{
			PreparedStatement pstate = conn.prepareStatement(sql);
			pstate.setString(1, account.getUsername());
			pstate.setString(2, QQTool.sha1(account.getPassword()));
			pstate.setLong(3, account.getDatetime());
			
			effects = pstate.executeUpdate();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return effects;
	}
	
	public static boolean login(String username, String password){
		
		if(!QQCheck.validUsername(username))
		{JOptionPane.showMessageDialog(null, "username is not valid");return false;}
		
		if(!QQCheck.validPassword(password))
		{JOptionPane.showMessageDialog(null, "password is not valid");return false;}
		
		if(!QQCheck.validAccountMessage(username, password))
		{JOptionPane.showMessageDialog(null, "your message is not right");return false;}
		
		return true;
	}
	
	/**
	 * 获取指定用户名的Account对象
	 * @param username
	 * @return 用户不存在返回null
	 */
	public static Account getUser(String username){
		String sql = "SELECT * FROM qq_user WHERE username=?";
		Account account = null;
		try {
			PreparedStatement pstate = connect.prepareStatement(sql);
			pstate.setString(1, username);
			ResultSet set = pstate.executeQuery();
			
			while(set.next()){
				account = new Account();
				account.setUsername(set.getString("username"));
				account.setPassword(set.getString("password"));
				account.setNickname(set.getString("nickname"));
				account.setDatetime(set.getInt("signtime"));
				account.setSex(set.getString("sex"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return account;
	}
	
	public static boolean addFriend(String hostname, String friendname){
		if(QQCheck.accountExist(friendname)){
			String sql = "INSERT INTO qq_friends(hostuser,frienduser) VALUES(?,?),(?,?)";
			int effects = 0;
			try {
				PreparedStatement pstate = connect.prepareStatement(sql);
				
				pstate.setString(1, hostname);
				pstate.setString(2, friendname);
				pstate.setString(3, friendname);
				pstate.setString(4, hostname);
				
				effects = pstate.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} 
			
			return effects > 0;
			
		}
		
		return false;
	}
	
	public static ArrayList<Account> getFriends(String hostname){
		String sql = "SELECT * FROM qq_user WHERE username IN " +
				"(SELECT frienduser FROM qq_friends WHERE hostuser=?)";
		ArrayList<Account> accounts = new ArrayList<Account>();
		try {
			PreparedStatement pstate = connect.prepareStatement(sql);
			pstate.setString(1, hostname);
			ResultSet set = pstate.executeQuery();
			
			while(set.next()){
				Account account = new Account();
				account.setUsername(set.getString("username"));
				account.setNickname(set.getString("nickname"));
				
				accounts.add(account);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return accounts;
		
	}
	
	public static boolean removeFriend(String hostuser, String frienduser){
		String sql = "DELETE FROM qq_friends WHERE (hostuser=? AND "
				   + "frienduser=?) OR (hostuser=?" + " AND " + "frienduser=?)";
		int effects = 0;
		try {
			
			PreparedStatement pstate = connect.prepareStatement(sql);
			pstate.setString(1, hostuser);
			pstate.setString(2, frienduser);
			pstate.setString(3, frienduser);
			pstate.setString(4, hostuser);
			effects = pstate.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return effects > 0;
		
	}
}
