package cn.seeonce.entrance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.seeonce.controller.ClientServerController;
import cn.seeonce.controller.LoginServerController;
import cn.seeonce.model.QQModel;

public class EntranceChatServer {
	
		//聊天服务器
		private ServerSocket server;
		//登录服务器
		private LoginServerController loginServer;
		//所有在线的用户
		private Map<String, ClientServerController> clients;
		//sql model
		private QQModel model ;
		
		public final static int PORT = 9999;
		
		private EntranceChatServer() throws IOException{
			this.model = new QQModel();
			
			clients = new  ConcurrentHashMap<String, ClientServerController>();
			
			server  = new ServerSocket(PORT);
			
			ExecutorService pool = Executors.newCachedThreadPool();
			
			Socket client = null;
			
			System.out.println("聊天服务器已经启动在=>" + PORT);
			
			while((client = server.accept()) != null){
				ClientServerController value = new ClientServerController(client, clients);
				
				clients.put(value.getIdentity(), value);
				
				pool.execute(value);
			}
			
		}
		
	
	public static void newServer(){
		try {
			 new EntranceChatServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
