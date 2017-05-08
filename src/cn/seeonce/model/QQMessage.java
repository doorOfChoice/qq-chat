package cn.seeonce.model;

import cn.seeonce.qq.data.XMLObject;

public class QQMessage {
	public final static String COMMAND = "command";
	public final static String MESSAGE = "message";
	public final static String RESULT  = "result";
	
	public static String msgChat(String hostuser, String aimuser, String message){
		XMLObject xml = new XMLObject();
		xml.setAttribute(MESSAGE);
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		xml.add("message", message);
		return xml.toString();
	}
	
	public static String cmFriendAdd(String hostuser, String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(COMMAND);
		xml.add("name", "friendadd");
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	
	public static String rsFriendDelete(String hostuser, String aimuser){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", "frienddelete");
		xml.add("hostuser", hostuser);
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}
	
	public static String rsFriendAdd(String aimuser, boolean success){
		XMLObject xml = new XMLObject();
		xml.setAttribute(RESULT);
		xml.add("name", "friendadd");
		xml.add("success", success + "");
		xml.add("aimuser", aimuser);
		
		return xml.toString();
	}

}
