package cn.seeonce.qq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import cn.seeonce.model.QQMessage;
import cn.seeonce.model.QQSql;
import cn.seeonce.model.QQTool;

public class QQLoginServer implements Runnable{
	private QQSql model;
	
	private ServerSocket server;
	
	public QQLoginServer(QQSql model) throws IOException{
		this.model = model;
		
		this.server = new ServerSocket(9998);
		
		new Thread(this).start();
	}
	
	public void sendMessage(DataOutputStream output, String message){
		try {
			output.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		Socket socket;
		DataInputStream input;
		DataOutputStream output;
		System.out.println("9998 start");
		try {
			while((socket = server.accept()) != null){
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				
				String message = input.readUTF();
				//System.out.println(message);
				Map<String, String> msgXML = QQTool.analyseXML(message);
				
				String aimuser = msgXML.get("aimuser");
				String attr    = msgXML.get("attribute");
				
				//命令区域
				if(attr.equals(QQMessage.COMMAND)){
					String commandName = msgXML.get("name");
					/*【验证登录】
					 *验证客户端传来的用户和密码
					 *并且返回一个包含账户信息的xml 
					 */
					if(commandName.equals(QQMessage.C_LOGIN)){
						boolean success    = model.login(msgXML.get("username"), msgXML.get("password"));
						
						sendMessage(output, QQMessage.rsLogin(aimuser, success, 
			    		        success+"", success ? model.getUser(aimuser) : null));
					}		
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
