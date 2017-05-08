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

import cn.seeonce.model.QQTool;

public class QQServer {
	public static final String ALL = "all";
	private ServerSocket server;
	
	private Map<String, ClientListener> clients;
	
	public QQServer() throws IOException{
		clients = new HashMap<String, ClientListener>();
		server  = new ServerSocket(9999);
		ExecutorService pool = Executors.newCachedThreadPool();
		
		Socket client = null;
		
		while((client = server.accept()) != null){
			ClientListener value = new ClientListener(client);
			String key = value.getIdentity();
			clients.put(key, value);
			
			System.out.println(clients);
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
					//发给所有人
					if(aimuser.equals(ALL)){
						for(String key : clients.keySet()){
							ClientListener tClient = clients.get(key);
							tClient.sendMessage(message);
						}
					//发给指定用户
					}else{
						ClientListener tClient = clients.get(aimuser);
						if(tClient != null)
						{tClient.sendMessage(message);}
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
		new QQServer();
	}

}
