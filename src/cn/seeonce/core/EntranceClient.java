package cn.seeonce.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.controller.ClientServerController;
import cn.seeonce.controller.ClientLocalController;
import cn.seeonce.data.Account;
import cn.seeonce.library.QQTool;

public class EntranceClient implements Runnable{
	private Socket client;
	// 向服务器的接收流
	private DataInputStream input = null;
	// 向服务器的输出流
	private DataOutputStream output = null;
	
	private ClientLocalController controller;
	
	private Thread task;
	private EntranceClient(){}
	private EntranceClient(Socket client, Account account){
		this.client = client;
		try {
			input      = new DataInputStream(client.getInputStream());
			output     = new DataOutputStream(client.getOutputStream());
			controller = new ClientLocalController(account, client, input, output);
			task       = new Thread(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		task.start();
	}
	
	private void analyseMessage(Map<String, String> msgXML)
			throws IOException {
		
		String attr = msgXML.get("attribute");
		
		String methodName = attr + QQTool.first2up(msgXML.get("name"));
		
		try {
			Method method = controller.getClass()
						              .getDeclaredMethod(methodName, Map.class);
			method.invoke(controller, msgXML);
		}catch(Exception ex){ex.printStackTrace();}

	}
	
	@Override
	public void run() {
		while (true) {
			try {
				String message = input.readUTF();
				analyseMessage(QQTool.analyseXML(message));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void newClient(Socket client, Account account){
		new EntranceClient(client, account);
	}
	
}
