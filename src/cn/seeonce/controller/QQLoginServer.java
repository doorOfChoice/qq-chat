package cn.seeonce.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.model.QQMessage;
import cn.seeonce.model.QQSql;
import cn.seeonce.model.QQTool;

public class QQLoginServer implements Runnable{
	private QQSql model;
	private DataInputStream input;
	private DataOutputStream output;
	private ServerSocket server;
	
	public QQLoginServer(QQSql model) throws IOException{
		this.model = model;
		
		this.server = new ServerSocket(9998);
		
		new Thread(this).start();
	}
	
	private void sendMessage(DataOutputStream output, String message){
		try {
			output.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void commandLogin(String message){
		Map<String, String> msgXML = QQTool.analyseXML(message);
		boolean success    = model.login(msgXML.get("username"), msgXML.get("password"));
		String aimuser = msgXML.get("aimuser");	
		sendMessage(output, QQMessage.rsLogin(aimuser, success, 
    	        success+"", success ? model.getUser(aimuser) : null));
	}
	
	private synchronized void commandSign(String message){
		Map<String, String> msgXML = QQTool.analyseXML(message);
		String hostuser = msgXML.get("aimuser");
		String username = msgXML.get("username");
		String password = msgXML.get("password");
		int success = model.sign(username, password);
		
		String resultMessage = null;
		switch(success){
		case QQSql.SUCCESS : resultMessage = "注册成功"; break;
		case QQSql.EXIST_USER : resultMessage = "用户已经存在"; break;
		case QQSql.UNVALID_USERNAME : resultMessage = "用户名格式不对"; break;
		case QQSql.UNVALID_PASSWORD : resultMessage = "密码格式不对"; break;
		}
		
		sendMessage(output, 
		QQMessage.rsSign(hostuser, success == QQSql.SUCCESS, resultMessage));
	}
	@Override
	public void run() {
		Socket socket;
		String methodName = null;
		
		System.out.println("登录服务器已经启动在 port=>9998");
		
		try {
			while((socket = server.accept()) != null){
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				
				String message = input.readUTF();
				
				Map<String, String> msgXML = QQTool.analyseXML(message);
				
				String attr = msgXML.get("attribute");
				
				if(attr.equals(QQMessage.COMMAND) || attr.equals(QQMessage.RESULT))
				{methodName = attr + QQTool.first2up(msgXML.get("name"));}
				else
				{methodName = "messageGet";}
				
				try {
					Method method = getClass().getDeclaredMethod(methodName, String.class);
					method.invoke(this, message);
				}catch(Exception ex){ex.printStackTrace();}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
