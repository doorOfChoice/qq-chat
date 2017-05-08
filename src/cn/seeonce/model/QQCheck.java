package cn.seeonce.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 此类提供各种数据的验证函数和登录验证等等
 * @author dawndevil
 * @version 1.0
 */

public class QQCheck {
	private static Connection conn = QQSql.getConnection();
	
	public static boolean validUsername(String username){
		return username.matches("[\\w]{6,16}");
	}
	
	public static boolean validPassword(String password){
		return password.matches("[\\w]{6,20}");
	}
	
	/**
	 * 验证登录
	 * @param username
	 * @param password
	 * @return 是否成功登录
	 */
	@SuppressWarnings("finally")
	public static boolean validAccountMessage(String username, String password){
		String sql = "SELECT * FROM qq_user WHERE username=? AND password=?";
		
		boolean isExist = false;
		
		PreparedStatement pstate;
		
		try {
			pstate = conn.prepareStatement(sql);
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
	 * 判断账户是否已经存在于数据库中
	 * @param username
	 * @return true:存在, false:不存在
	 */
	public static boolean accountExist(String username){
		String sql = "SELECT * FROM qq_user WHERE username=?";
		
		boolean isExist = false;
		
		try {
			PreparedStatement pstate = conn.prepareStatement(sql);
			
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
