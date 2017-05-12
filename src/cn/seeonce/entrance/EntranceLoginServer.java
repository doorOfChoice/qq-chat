package cn.seeonce.entrance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.controller.LoginServerController;
import cn.seeonce.data.XMLObject;
import cn.seeonce.library.QQTool;

public class EntranceLoginServer implements Runnable{
	private Thread task;
	
	private ServerSocket server;
	
	private LoginServerController controller;
	
	public final static int PORT = 9998;
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
		
		ObjectInputStream  input;
		ObjectOutputStream output;
		
		System.out.println("登录服务器已经启动在 port=>" + PORT);
		
		try {
			while((socket = server.accept()) != null){
				input = new ObjectInputStream(socket.getInputStream());
				output = new ObjectOutputStream(socket.getOutputStream());
				
				
				XMLObject msgXML = null;
				try {
					msgXML = (XMLObject)input.readObject();
				} catch (ClassNotFoundException e) 
				{e.printStackTrace();};
				
				System.out.println(msgXML);
				
				String attr = msgXML.getAttribute();
				
				methodName = attr + QQTool.first2up(msgXML.getString("name"));
				
				try {
					Method method = controller.getClass()
							      .getDeclaredMethod(methodName, XMLObject.class);
					controller.setDataOutputStream(output);
					method.invoke(controller, msgXML);
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
