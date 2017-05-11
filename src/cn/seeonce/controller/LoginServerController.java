package cn.seeonce.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;
import cn.seeonce.model.QQModel;

public class LoginServerController{
	private QQModel model;
	
	private DataOutputStream output;
	
	public LoginServerController(DataOutputStream output) throws IOException{
		this.model  = new QQModel();
		this.output = output;
	}
	
	public synchronized void setDataOutputStream(DataOutputStream output){
		this.output = output;
	}
	
	private void sendMessage(DataOutputStream output, String message){
		try {
			output.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void commandLogin(String message, Map<String, String> msgXML){
		boolean success    = model.login(msgXML.get("username"), msgXML.get("password"));
		String aimuser = msgXML.get("aimuser");	
		sendMessage(output, QQMessage.rsLogin(aimuser, success, 
    	        success+"", success ? model.getUser(aimuser) : null));
	}
	
	public synchronized void commandSign(String message, Map<String, String> msgXML){
		String hostuser = msgXML.get("aimuser");
		String username = msgXML.get("username");
		String password = msgXML.get("password");
		int success = model.sign(username, password);
		
		String resultMessage = null;
		switch(success){
		case QQModel.SUCCESS : resultMessage = "注册成功"; break;
		case QQModel.EXIST_USER : resultMessage = "用户已经存在"; break;
		case QQModel.UNVALID_USERNAME : resultMessage = "用户名格式不对"; break;
		case QQModel.UNVALID_PASSWORD : resultMessage = "密码格式不对"; break;
		}
		
		sendMessage(output, 
		QQMessage.rsSign(hostuser, success == QQModel.SUCCESS, resultMessage));
	}
	
}
