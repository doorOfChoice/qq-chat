package cn.seeonce.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.controller.LoginServerController;
import cn.seeonce.library.QQTool;

public class EntranceLoginServer implements Runnable{
	private Thread task;
	
	private ServerSocket server;
	
	private LoginServerController controller;
	
	private final static int PORT = 9998;
	private EntranceLoginServer(){
		try {
			this.server     = new ServerSocket(PORT);
			this.controller = new LoginServerController(null);
			this.task       = new Thread(this);
			this.task.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		Socket socket;
		
		String methodName = null;
		
		DataInputStream  input;
		DataOutputStream output;
		
		System.out.println("登录服务器已经启动在 port=>" + PORT);
		
		try {
			while((socket = server.accept()) != null){
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				
				String message = input.readUTF();
				
				Map<String, String> msgXML = QQTool.analyseXML(message);
				
				String attr = msgXML.get("attribute");
				
				methodName = attr + QQTool.first2up(msgXML.get("name"));
				
				try {
					Method method = controller.getClass()
							      .getDeclaredMethod(methodName, String.class, Map.class);
					controller.setDataOutputStream(output);
					method.invoke(controller, message, msgXML);
				}catch(Exception ex){ex.printStackTrace();}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void newServer(){
		new EntranceLoginServer();
	}
}
