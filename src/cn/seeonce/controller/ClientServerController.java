package cn.seeonce.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;
import cn.seeonce.model.QQModel;

public class ClientServerController implements Runnable{
		//所有在线的用户
		private Map<String, ClientServerController> clients;
		//sql model
		private QQModel          model;
		
		
		private String           identity; 
		
		private DataInputStream  input ;
		
		private DataOutputStream output;
		
		private boolean          isInterrupt;
		
		public ClientServerController(Socket client, Map<String, ClientServerController> clients){
			this.model = new QQModel();
			this.clients = clients;
			try {
				input = new DataInputStream(client.getInputStream());
				output = new DataOutputStream(client.getOutputStream());
				//在map中创建可管理的映射
				identity = input.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		/*获取当前线程数据哪一个用户*/
		public String getIdentity(){
			return identity;
		}
		
		public ClientServerController getUser(String username){
			return clients.get(username);
		}
		
		public boolean userOnline(String username){
			return clients.get(username) != null;
		}
		
		/*向客户端发送信息*/
		public void sendMessage(String message){
			try {
				output.writeUTF(message);
			} catch (IOException e)
			{e.printStackTrace();}
		}
		
		/**
		 * 直接交付信息给指定用户
		 * 有些数据服务器不做处理,仅仅只是做转发,直接使用此方法
		 * @param message
		 * @param msgXML
		 */
		public synchronized void shiftDirect(String message, Map<String, String> msgXML){
			String aimuser = msgXML.get("aimuser");
			if(userOnline(aimuser))
				clients.get(msgXML.get("aimuser")).sendMessage(message);
		}
		
		public synchronized void commandFriendGet(String message, Map<String, String> msgXML){
			String aimuser = msgXML.get("aimuser");
			clients.get(aimuser).sendMessage(QQMessage
					   .rsFriendGet(aimuser, model.getFriends(aimuser)));
		}
		
		public synchronized void commandFriendDelete(String message, Map<String, String> msgXML){
			String hostuser = msgXML.get("hostuser");
			String aimuser  = msgXML.get("aimuser") ;
			if(model.removeFriend(hostuser, aimuser)){
				if(userOnline(hostuser))
				{getUser(hostuser).sendMessage(QQMessage
					   .rsFriendDelete(aimuser, hostuser, model.getFriends(hostuser)));}
				
				if(userOnline(aimuser))
				{getUser(aimuser).sendMessage(QQMessage
					   .rsFriendDelete(hostuser, aimuser, model.getFriends(aimuser)));}
			}
		}
		public synchronized void resultFriendAdd(String message, Map<String, String> msgXML){
			boolean success = false;
			String hostuser = msgXML.get("hostuser");
			String aimuser  = msgXML.get("aimuser") ;
			if(Boolean.valueOf(msgXML.get("success"))){
				success = model.addFriend(hostuser, aimuser);
			}
			if(userOnline(hostuser))
				getUser("hostuser")
				.sendMessage(QQMessage.rsFriendAdd(aimuser, hostuser, success, model.getFriends(hostuser)));
			if(userOnline(aimuser))
				getUser("aimuser")
				.sendMessage(QQMessage.rsFriendAdd(hostuser, aimuser, success, model.getFriends(aimuser)));
		
		}
		
		public synchronized void messageChat(String message, Map<String, String> msgXML){
			String aimuser = msgXML.get("aimuser");
			
			if(userOnline(aimuser))
			{getUser(aimuser).sendMessage(message);}
		}
		
		@Override
		public void run() {
			while(!isInterrupt){
				try {
					String message = input.readUTF();
					
					Map<String, String> msgXML = QQTool.analyseXML(message);
					
					String attr = msgXML.get("attribute");
					
					String methodName = null;
					
					methodName = attr + QQTool.first2up(msgXML.get("name"));
					
					try {
						Method method = getClass().getDeclaredMethod(methodName, String.class, Map.class);
						method.invoke(this, message, msgXML);
					}catch(Exception ex){
						shiftDirect(message, msgXML);
					}
					/*
					 * 客户端异常退出
					 * 通常就是用户关闭了程序，检测用户离线
					 */
				} catch (IOException e) {
					isInterrupt = true;
					clients.remove(identity);
					System.out.println(identity + ":客户端已经退出");
				}
			}
		}
		


}
