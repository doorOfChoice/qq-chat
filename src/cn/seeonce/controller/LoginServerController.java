package cn.seeonce.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.data.XMLObject;
import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;
import cn.seeonce.model.QQModel;

public class LoginServerController{
	private QQModel model;
	
	private ObjectOutputStream output;
	
	public LoginServerController(ObjectOutputStream output) throws IOException{
		this.model  = new QQModel();
		this.output = output;
	}
	
	public synchronized void setDataOutputStream(ObjectOutputStream output){
		this.output = output;
	}
	
	private void sendMessage(XMLObject message){
		try {
			output.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void commandLogin(XMLObject msgXML){
		boolean success    = model.login(msgXML.getString("username"), msgXML.getString("password"));
		String aimuser = msgXML.getString("aimuser");	
		sendMessage(QQMessage.rsLogin(aimuser, success, 
    	        success + "", success ? model.getUser(aimuser) : null));
	}
	
	public synchronized void commandSign(XMLObject msgXML){
		String hostuser = msgXML.getString("aimuser");
		String username = msgXML.getString("username");
		String password = msgXML.getString("password");
		int success = model.sign(username, password);
		
		String resultMessage = null;
		switch(success){
		case QQModel.SUCCESS : resultMessage = "注册成功"; break;
		case QQModel.EXIST_USER : resultMessage = "用户已经存在"; break;
		case QQModel.UNVALID_USERNAME : resultMessage = "用户名格式不对"; break;
		case QQModel.UNVALID_PASSWORD : resultMessage = "密码格式不对"; break;
		}
		
		sendMessage(
		QQMessage.rsSign(hostuser, success == QQModel.SUCCESS, resultMessage));
	}
	
}
