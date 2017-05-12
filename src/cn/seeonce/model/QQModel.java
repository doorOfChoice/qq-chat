package cn.seeonce.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import cn.seeonce.data.Account;
import cn.seeonce.library.QQTool;
import cn.seeonce.library.QQVerify;


/**
 * 该类提供有关数据库操作的静态函数
 * @author dawndevil
 */

public class QQModel {
	private  Connection connect;
	
	public final static int UNVALID_USERNAME = -1;
	public final static int UNVALID_PASSWORD = -2;
	public final static int EXIST_USER = -3;
	public final static int SUCCESS = 0;
	
	public QQModel(){
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
	
	public Connection getConnection(){
		return connect;
	}
	/**
	 * 注册函数
	 * @param username
	 * @param password
	 * @return
	 */
	public synchronized int sign(String username, String password){
				//创建临时用户
		Account account = new 
				Account(username, password, (int)(System.currentTimeMillis() / 1000));
		
		if(!QQVerify.validUsername(account.getUsername()))
		{return UNVALID_USERNAME;}
		
		if(!QQVerify.validPassword(account.getPassword()))
		{return UNVALID_PASSWORD;}
		
		if(accountExist(account.getUsername()))
		{return EXIST_USER;}
		
		//受影响的行数
		int effects = 0;
		
		String sql = "INSERT INTO qq_user(username,password,signtime) VALUES(?,?,?)";
		
		try{
			PreparedStatement pstate = connect.prepareStatement(sql);
			pstate.setString(1, account.getUsername());
			pstate.setString(2, QQTool.sha1(account.getPassword()));
			pstate.setLong(3, account.getDatetime());
			
			effects = pstate.executeUpdate();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return SUCCESS;
	}
	/**
	 * 验证登录的函数
	 * @param username
	 * @param password
	 * @return
	 */
	public synchronized  boolean login(String username, String password){
		System.out.println(username);
		if(!QQVerify.validUsername(username))
		{return false;}
		
		if(!QQVerify.validPassword(password))
		{return false;}
		
		if(!validAccountMessage(username, password))
		{return false;}
		
		return true;
	}
	

	/**
	 * 获取指定用户名的Account对象
	 * @param username
	 * @return 用户不存在返回null
	 */
	public synchronized  Account getUser(String username){
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
	/**
	 * 互相添加对方为好友
	 * @param hostname
	 * @param friendname
	 * @return
	 */
	public synchronized boolean addFriend(String hostname, String friendname){
		if(accountExist(friendname)){
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
				return false;
			} 
			
			return effects > 0;
			
		}
		
		return false;
	}
	/**
	 * 获取自己的好友列表
	 * @param hostname
	 * @return ArrayList<Account>
	 */
	public synchronized  ArrayList<Account> getFriends(String hostname){
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
	
	/**
	 * 从双发联系人列表中删除好友
	 * @param hostuser
	 * @param frienduser
	 * @return
	 */
	public synchronized boolean removeFriend(String hostuser, String frienduser){
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
			return false;
		}
		
		return effects > 0;
		
	}
	
	/**
	 * 验证登录
	 * @param username
	 * @param password
	 * @return 是否成功登录
	 */
	public synchronized boolean validAccountMessage(String username, String password){
		String sql = "SELECT * FROM qq_user WHERE username=? AND password=?";
		
		boolean isExist = false;
		
		PreparedStatement pstate;
		
		try {
			pstate = connect.prepareStatement(sql);
			pstate.setString(1, username);
			pstate.setString(2, QQTool.sha1(password));
			ResultSet set = pstate.executeQuery();
			
			isExist = set.next();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
		return isExist;
		
	}
	/**
	 * 将离线消息写入数据库中
	 * @param from
	 * @param to
	 * @param content
	 * @return
	 */
	public synchronized boolean writeMessage(String from, String to, String content){
		String sql = "INSERT INTO qq_record(`from`, `to`, `content`, `datetime`) VALUES(" +
				"?, ?, ?, ?)";
		int effects = 0;
		try {
			PreparedStatement pstate = connect.prepareStatement(sql);
			pstate.setString(1, from);
			pstate.setString(2, to);
			pstate.setString(3, content);
			pstate.setInt(4, (int) (System.currentTimeMillis() / 1000));
			
			effects = pstate.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return effects != 0;
	}
	
	/**
	 * 获取好友发送给自己的信息, 并且分组存储, 返回数据 
	 * @param hostname
	 * @return
	 */
	public synchronized Map<String, Stack<String>> getMessage(String username){
		String sql = "SELECT * FROM qq_record WHERE `to`=? ORDER BY datetime ASC";
		Map<String, Stack<String>> messages = 
					new HashMap<String, Stack<String>>();
		try {
			PreparedStatement pstate = connect.prepareStatement(sql);
			pstate.setString(1, username);
			
			ResultSet set = pstate.executeQuery();
			while(set.next()){
				String friend = set.getString("from");
				if(messages.get(friend) == null){
					messages.put(friend, new Stack<String>());
				}
				messages.get(friend).push(set.getString("content"));
			}
			//删除已读数据
			sql = "DELETE FROM qq_record WHERE `to`=?";
			pstate = connect.prepareStatement(sql);
			pstate.setString(1, username);
			pstate.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	/**
	 * 判断账户是否已经存在于数据库中
	 * @param username
	 * @return true:存在, false:不存在
	 */
	public synchronized boolean accountExist(String username){
		String sql = "SELECT * FROM qq_user WHERE username=?";
		
		boolean isExist = false;
		
		try {
			PreparedStatement pstate = connect.prepareStatement(sql);
			
			pstate.setString(1, username);
			
			ResultSet set = pstate.executeQuery();
			//判断是否存在第一条记录
			isExist = !set.next() ? false : true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return isExist;
	}
	
}
