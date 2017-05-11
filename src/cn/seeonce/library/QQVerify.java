package cn.seeonce.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 此类提供各种数据的验证函数和登录验证等等
 * @author dawndevil
 * @version 1.0
 */

public class QQVerify {
	
	public static boolean validUsername(String username){
		return username.matches("[\\w]{6,16}");
	}
	
	public static boolean validPassword(String password){
		return password.matches("[\\w]{6,20}");
	}
	
}
