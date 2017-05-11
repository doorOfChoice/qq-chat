package cn.seeonce.library;

import java.util.ArrayList;

import cn.seeonce.data.Account;
import cn.seeonce.data.XMLObject;

import com.alibaba.fastjson.JSON;
public class QQMessage {
	public final static String COMMAND = "command";
	public final static String MESSAGE = "message";
	public final static String RESULT  = "result";
	
	public final static String CHAT          = "chat";
	public final static String SIGN          = "sign" ;
	public final static String LOGIN         = "login";
	public final static String DELIVER       = "deliver";
	public final static String ADD_FRIEND    = "friendAdd";
	public final static String GET_FRIENDS   = "friendGet";
	public final static String DELETE_FRIEND = "friendDelete";
	
	
	public static String msgChat(String hostuser, String aimuser, String message){
		XMLObject xml = new XMLObject();
		xml.setAttribute(MESSAGE);
		xml.add("name", CHAT);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		xml.add("message", message);
		return xml.toString();
	}
	
	public static String msgFile(String aimuser,String basename, String message, boolean isEnd){
		XMLObject xml = new XMLObject();
		xml.setAttribute(MESSAGE);
		xml.add("name", DELIVER);
		xml.add("isEnd", isEnd + "");
		xml.add("basename", basename);
		xml.add("aimuser", aimuser);
		xml.add("message", message);
		return xml.toString();
	}
	
	public static String cmDeliver(String hostuser, String aimuser, String filename){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", DELIVER);
		xml.add("aimuser", aimuser);
		xml.add("hostuser", hostuser);
		xml.add("filename", filename);
		return xml.toString();
	}
	
	public static String cmLogin(String aimuser, String username, String password){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", LOGIN);
		xml.add("username", username);
		xml.add("password", password);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String cmSign(String aimuser, String username, String password){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", SIGN);
		xml.add("username", username);
		xml.add("password", password);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String cmFriendGet(String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", GET_FRIENDS);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String cmFriendAdd(String hostuser, String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", ADD_FRIEND);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String cmFriendDelete(String hostuser, String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", DELETE_FRIEND);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	
	
	public static String rsFriendGet(String aimuser, ArrayList<Account> accounts){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", GET_FRIENDS);
		xml.add("accounts", JSON.toJSONString(accounts));
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}

	public static String rsLogin(String aimuser, boolean canLogin
			, String message, Account account){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", LOGIN);
		xml.add("success", canLogin + "");
		xml.add("account", JSON.toJSONString(account));
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String rsSign(String aimuser, boolean success
			, String message){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", SIGN);
		xml.add("message", message);
		xml.add("success", success + "");
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String rsDeliver(String hostuser, String aimuser, boolean success, String filename){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", DELIVER);
		xml.add("success", success + "");
		xml.add("filename", filename);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String rsFriendAdd(String hostuser, String aimuser
			, boolean success, ArrayList<Account> friends){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", ADD_FRIEND);
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
		xml.add("name", DELETE_FRIEND);
		xml.add("aimuser", aimuser);
		xml.add("hostuser", hostuser);
		xml.add("accounts", JSON.toJSONString(friends));
		return xml.toString();
	}

}
