package cn.seeonce.entrance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.controller.ClientServerController;
import cn.seeonce.controller.ClientLocalController;
import cn.seeonce.data.Account;
import cn.seeonce.data.XMLObject;
import cn.seeonce.library.QQTool;

public class EntranceClient implements Runnable{
	private Socket client;
	// 向服务器的接收流
	private ObjectInputStream input = null;
	// 向服务器的输出流
	private ObjectOutputStream output = null;
	
	private ClientLocalController controller;
	
	private Thread task;
	private EntranceClient(){}
	private EntranceClient(Socket client, Account account){
		this.client = client;
		System.out.println("初始化完成1");
		try {
			input      = new ObjectInputStream(client.getInputStream());
			output     = new ObjectOutputStream(client.getOutputStream());
			controller = new ClientLocalController(account, client, input, output);
			task       = new Thread(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		task.start();
		
	}
	
	private void analyseMessage(XMLObject msgXML)
			throws IOException {
		
		if(msgXML == null)
			return;
		
		String attr = msgXML.getAttribute();
		
		String methodName = attr + QQTool.first2up(msgXML.getString("name"));
		
		try {
			Method method = controller.getClass()
						              .getDeclaredMethod(methodName, XMLObject.class);
			method.invoke(controller, msgXML);
		}catch(Exception ex){ex.printStackTrace();}

	}
	
	@Override
	public void run() {
		while (true) {
			try {
				XMLObject msgXML = null;
				try {
					msgXML = (XMLObject)input.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				analyseMessage(msgXML);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void newClient(Socket client, Account account){
		new EntranceClient(client, account);
	}
	
}
