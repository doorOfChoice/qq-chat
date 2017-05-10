package cn.seeonce.model;

import java.util.ArrayList;

import cn.seeonce.qq.data.Account;
import cn.seeonce.qq.data.XMLObject;
import com.alibaba.fastjson.JSON;
public class QQMessage {
	public final static String COMMAND = "command";
	public final static String MESSAGE = "message";
	public final static String RESULT  = "result";
	
	public final static String C_LOGIN = "login";
	public final static String C_ADD_FRIEND = "friendAdd";
	public final static String C_GET_FRIENDS = "friendGet";
	public final static String C_DELETE_FRIEND = "friendDelete";
	public final static String S_ADD_FRIEND = C_ADD_FRIEND;
	public final static String S_GET_FRIENDS = C_GET_FRIENDS;
	public final static String S_DELETE_FRIEND = C_DELETE_FRIEND;
	public static String msgChat(String hostuser, String aimuser, String message){
		XMLObject xml = new XMLObject();
		xml.setAttribute(MESSAGE);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		xml.add("message", message);
		return xml.toString();
	}
	
	public static String cmLogin(String aimuser, String username, String password){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", C_LOGIN);
		xml.add("username", username);
		xml.add("password", password);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String cmFriendGet(String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", C_GET_FRIENDS);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String cmFriendAdd(String hostuser, String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", C_ADD_FRIEND);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String cmFriendDelete(String hostuser, String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", C_DELETE_FRIEND);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	
	public static String rsFriendGet(String aimuser, ArrayList<Account> accounts){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", S_GET_FRIENDS);
		xml.add("accounts", JSON.toJSONString(accounts));
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}

	public static String rsLogin(String aimuser, boolean canLogin
			, String message, Account account){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", C_LOGIN);
		xml.add("success", canLogin + "");
		xml.add("account", JSON.toJSONString(account));
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}


	
	
	public static String rsFriendAdd(String hostuser, String aimuser
			, boolean success, ArrayList<Account> friends){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", S_ADD_FRIEND);
		xml.add("success", success + "");
		xml.add("aimuser", aimuser);
		xml.add("hostuser", hostuser);
		xml.add("accounts", JSON.toJSONString(friends));
		return xml.toString();
	}
	
	public static String rsFriendDelete(String hostuser, String aimuser
			, ArrayList<Account> friends){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", S_DELETE_FRIEND);
		xml.add("aimuser", aimuser);
		xml.add("hostuser", hostuser);
		xml.add("accounts", JSON.toJSONString(friends));
		return xml.toString();
	}

}
