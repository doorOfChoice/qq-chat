package cn.seeonce.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.seeonce.data.XMLObject;
import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;
import cn.seeonce.model.QQModel;

public class ClientServerController implements Runnable{
		//所有在线的用户
		private Map<String, ClientServerController> clients;
		//sql model
		private QQModel          model;
		
		
		private String           identity; 
		
		private ObjectInputStream  input ;
		
		private ObjectOutputStream output;
		
		private boolean          isInterrupt;
		
		public ClientServerController(Socket client, Map<String, ClientServerController> clients){
			this.model = new QQModel();
			this.clients = clients;
			try {
				output = new ObjectOutputStream(client.getOutputStream());
				input = new ObjectInputStream(client.getInputStream());
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
		
		/*向客户端发送信息*/
		public void sendMessage(XMLObject msgXML){
			try {
				output.writeObject(msgXML);
			} catch (IOException e)
			{e.printStackTrace();}
		}
		
		/**
		 * 直接交付信息给指定用户
		 * 有些数据服务器不做处理,仅仅只是做转发,直接使用此方法
		 * @param message
		 * @param msgXML
		 */
		public synchronized void shiftDirect(XMLObject msgXML){
			String aimuser = msgXML.getString("aimuser");
			if(userOnline(aimuser))
				clients.get(msgXML.getString("aimuser")).sendMessage(msgXML);
		}
		
		public synchronized void messageChat(XMLObject msgXML){
			String hostuser = msgXML.getString("hostuser");
			String aimuser = msgXML.getString("aimuser");
			
			if(userOnline(aimuser)){
				clients.get(msgXML.getString("aimuser")).sendMessage(msgXML);
			}
			else{
				model.writeMessage(hostuser, aimuser, msgXML.getString("message"));
			}
		}
		
		public synchronized void commandLeave(XMLObject msgXML){
			String aimuser = msgXML.getString("aimuser");
			sendMessage(QQMessage.rsLeave(aimuser, model.getMessage(aimuser)));
		}
		
		public synchronized void commandFriendGet(XMLObject msgXML){
			String aimuser = msgXML.getString("aimuser");
			if(userOnline(aimuser))
				getUser(aimuser).sendMessage(QQMessage
				.rsFriendGet(aimuser, model.getFriends(aimuser)));
		}
		
		public synchronized void commandFriendDelete(XMLObject msgXML){
			String hostuser = msgXML.getString("hostuser");
			String aimuser  = msgXML.getString("aimuser") ;
			if(model.removeFriend(hostuser, aimuser)){
				if(userOnline(hostuser))
				{getUser(hostuser).sendMessage(QQMessage
					   .rsFriendDelete(aimuser, hostuser, model.getFriends(hostuser)));}
				
				if(userOnline(aimuser))
				{getUser(aimuser).sendMessage(QQMessage
					   .rsFriendDelete(hostuser, aimuser, model.getFriends(aimuser)));}
			}
		}
		public synchronized void resultFriendAdd(XMLObject msgXML){
			boolean success = false;
			String hostuser = msgXML.getString("hostuser");
			String aimuser  = msgXML.getString("aimuser") ;
			if(Boolean.valueOf(msgXML.getString("success"))){
				success = model.addFriend(hostuser, aimuser);
			}
			if(userOnline(hostuser))
				getUser(hostuser)
				.sendMessage(QQMessage.rsFriendAdd(aimuser, hostuser, success, model.getFriends(hostuser)));
			if(userOnline(aimuser))
				getUser(aimuser)
				.sendMessage(QQMessage.rsFriendAdd(hostuser, aimuser, success, model.getFriends(aimuser)));
		
		}
		
		
		@Override
		public void run() {
			while(!isInterrupt){
				try {
					
					XMLObject msgXML = null;
					try {
						msgXML = (XMLObject)input.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					String attr = msgXML.getAttribute();
					
					String methodName = null;
					
					methodName = attr + QQTool.first2up(msgXML.getString("name"));
					System.out.println(methodName);
					try {
						Method method = getClass().getDeclaredMethod(methodName, XMLObject.class);
						method.invoke(this, msgXML);
					}catch(Exception ex){
						shiftDirect(msgXML);
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
		private boolean userOnline(String username){
			return clients.get(username) != null;
		}
		private ClientServerController getUser(String username){
			return clients.get(username);
		}
		


}
