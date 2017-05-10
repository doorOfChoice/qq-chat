package cn.seeonce.qq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
		/*中断该线程*/
		public void setInterrupt(boolean isInterrupt){
			this.isInterrupt = isInterrupt;
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
					//System.out.println(message);
					Map<String, String> msgXML = QQTool.analyseXML(message);
					
					String aimuser = msgXML.get("aimuser");
					String attr    = msgXML.get("attribute");
					
					/*===================================
					 * 命令区域
					 ====================================*/
					if(attr.equals(QQMessage.COMMAND)){
						String commandName = msgXML.get("name");
						/*【添加好友】
						 *将信息不做处理直接交付目标客户端
						 */		
						if(commandName.equals(QQMessage.C_ADD_FRIEND)){
							clients.get(aimuser).sendMessage(message);
						/*【获取制定用户好友列表】
						 * 返回一个带有ArrayList<Account>的xml数据
						 */	
						}else if(commandName.equals(QQMessage.C_GET_FRIENDS)){
							clients.get(aimuser).sendMessage(QQMessage
								   .rsFriendGet(aimuser, model.getFriends(aimuser)));
						/*【删除指定好友】
						 * 返回带有ArrayList<Account>的xml数据
						 */	
						}else if(commandName.equals(QQMessage.C_DELETE_FRIEND)){
							String hostuser = msgXML.get("hostuser");
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
						
						
					/*===================================
					 * 聊天消息区域	
					 ====================================*/
					}else if(attr.equals(QQMessage.MESSAGE)){
						/*【聊天消息】
						 *将信息不做处理直接交付目标客户端
						 */		
						clients.get(aimuser).sendMessage(message);
						
						
					/*====================================
					 * 结果区域
					 =====================================*/
					}else if(attr.equals(QQMessage.RESULT)){
						String resultName = msgXML.get("name");
						/*【添加好友的结果】
						 * 如果另一方同意，则在数据库中添加双发信息
						 * 并且将最新的好友列表更新给双发
						 */		
						if(resultName.equals(QQMessage.S_ADD_FRIEND)){
							boolean success = false;
							String hostuser = msgXML.get("hostuser");
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
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					isInterrupt = true;
					clients.remove(identity);
				}
			}
		}
		
	}
	
	
	public static void main(String[] args) throws IOException {
		new QQServer(new QQSql());
	}

}
