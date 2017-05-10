package cn.seeonce.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSON;

import cn.seeonce.model.QQMessage;
import cn.seeonce.model.QQSql;
import cn.seeonce.model.QQTool;

public class QQServer {
	
	private ServerSocket server;
	
	private QQLoginServer loginServer;
	
	private Map<String, ClientListener> clients;
	
	private QQSql model ;
	
	public QQServer(QQSql model) throws IOException{
		this.model = model;
		
		clients = new HashMap<String, ClientListener>();
		server  = new ServerSocket(9999);
		loginServer = new QQLoginServer(model);
		ExecutorService pool = Executors.newCachedThreadPool();
		
		Socket client = null;
		
		while((client = server.accept()) != null){
			ClientListener value = new ClientListener(client);
			
			clients.put(value.getIdentity(), value);
			
			pool.execute(value);
		}
		
	}
	
	
	class ClientListener implements Runnable{
		
		private boolean          isInterrupt = false;
		
		private String           identity; 
		
		private DataInputStream  input ;
		
		private DataOutputStream output;
		
		public ClientListener(Socket client){
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
	
		
		/*向客户端发送信息*/
		public void sendMessage(String message){
			try {
				output.writeUTF(message);
			} catch (IOException e)
			{e.printStackTrace();}
		}
		
		@Override
		public void run() {
			while(!isInterrupt){
				try {
					String message = input.readUTF();
					
					Map<String, String> msgXML = QQTool.analyseXML(message);
					
					String attr = msgXML.get("attribute");
					
					String methodName = null;
					
					if(attr.equals(QQMessage.COMMAND) || attr.equals(QQMessage.RESULT))
					{methodName = attr + QQTool.first2up(msgXML.get("name"));}
					else
					{methodName = "messageGet";}
					
					try {
						Method method = getClass().getDeclaredMethod(methodName, String.class);
						method.invoke(this, message);
					}catch(Exception ex){ex.printStackTrace();}
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
		
		
		private synchronized void commandFriendAdd(String message){
			Map<String, String> msgXML = QQTool.analyseXML(message);
			String aimuser = msgXML.get("aimuser");
			if(clients.get(aimuser) != null)
				clients.get(msgXML.get("aimuser")).sendMessage(message);
		}
		
		private synchronized void commandFriendGet(String message){
			Map<String, String> msgXML = QQTool.analyseXML(message);
			String aimuser = msgXML.get("aimuser");
			clients.get(aimuser).sendMessage(QQMessage
					   .rsFriendGet(aimuser, model.getFriends(aimuser)));
		}
		
		private synchronized void commandFriendDelete(String message){
			Map<String, String> msgXML = QQTool.analyseXML(message);
			String hostuser = msgXML.get("hostuser");
			String aimuser  = msgXML.get("aimuser") ;
			if(model.removeFriend(hostuser, aimuser)){
				ClientListener client1 = clients.get(hostuser);
				if(client1 != null)
				{client1.sendMessage(QQMessage
					   .rsFriendDelete(aimuser, hostuser, model.getFriends(hostuser)));}
				
				ClientListener client2 = clients.get(aimuser);
				if(client2 != null)
				{client2.sendMessage(QQMessage
					   .rsFriendDelete(hostuser, aimuser, model.getFriends(aimuser)));}
			}
		}
		
		private synchronized void resultFriendAdd(String message){
			Map<String, String> msgXML = QQTool.analyseXML(message);
			boolean success = false;
			String hostuser = msgXML.get("hostuser");
			String aimuser  = msgXML.get("aimuser") ;
			if(Boolean.valueOf(msgXML.get("success"))){
				success = model.addFriend(hostuser, aimuser);
			}
			if(clients.get(hostuser) != null)
				clients.get(hostuser)
				.sendMessage(QQMessage.rsFriendAdd(aimuser, hostuser, success, model.getFriends(hostuser)));
			if(clients.get(aimuser) != null)
				clients.get(aimuser)
				.sendMessage(QQMessage.rsFriendAdd(hostuser, aimuser, success, model.getFriends(aimuser)));
		
		}
		
		private synchronized void messageGet(String message){
			Map<String, String> msgXML = QQTool.analyseXML(message);
			String aimuser = msgXML.get("aimuser");
			
			if(clients.get(aimuser) != null)
			{clients.get(aimuser).sendMessage(message);}
		}
		
	}
	
	
	public static void main(String[] args) throws IOException {
		new QQServer(new QQSql());
	}

}
