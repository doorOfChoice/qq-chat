package cn.seeonce.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import com.alibaba.fastjson.JSON;

import cn.seeonce.data.Account;
import cn.seeonce.data.XMLObject;
import cn.seeonce.library.QQFileWrite;
import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;
import cn.seeonce.view.QQChatFrame;
import cn.seeonce.view.QQListFrame;

/**
 * QQ Client的控制器, 主要起到请求服务器数据, 并且将数据传给窗口层处理的作用
 * 不过也提供了部分功能给View使用, 但是不能直接篡改controller里面的内容
 * @author dawndevil
 *
 */
public class ClientLocalController{

		// 客户端socket
		private Socket client;
		// 客户端用户信息
		private Account account;
		// 客户端好友列表
		private ArrayList<Account> dataFriends;
		// 客户端信息列表
		private Map<String, Stack<String>> dataMessage;
		// 所有等待接受的文件流
		private Map<String, QQFileWrite> fileStreams;
		// 向服务器的接收流
		private ObjectInputStream input = null;
		// 向服务器的输出流
		private ObjectOutputStream output = null;
		
		private QQListFrame friendList;
		
		public ClientLocalController(Account account, Socket client,
				 ObjectInputStream input, ObjectOutputStream output){
			this.account = account;
			this.client  = client;
			this.input   = input;
			this.output  = output;
			dataMessage = new ConcurrentHashMap<String, Stack<String>>();
			fileStreams = new ConcurrentHashMap<String, QQFileWrite>();
			try {
				// 向服务器注册key-value，提示已经上线
				output.writeUTF(account.getUsername());
			} catch (IOException e) {
				e.printStackTrace();
			}
			friendList = new QQListFrame(this);
			
			// 获取好友列表
			sendMessage(QQMessage.cmFriendGet(account.getUsername()));
			// 获取离线消息
			sendMessage(QQMessage.cmLeave(account.getUsername()));
		}
		/**
		 * 向服务器发送信息
		 * @param message
		 */
		private void sendMessage(XMLObject msgXML){
			try {
				output.writeObject(msgXML);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 将服务器返还的json用户列表，解析成ArrayList，并且告知View层更新数据
		 * @param accounts
		 */
		private synchronized void rsFriendGet(ArrayList<Account> accounts) {
			dataFriends = accounts;
			friendList.updateFriends(getListMessage());
		}

		
		/**
		 * 客户端接收到对方添加好友的请求
		 * 选择YES发送一个接受的rsFriendAdd命令
		 * NO拒绝
		 * @param msgXML
		 * @throws IOException
		 */
		public synchronized void commandFriendAdd(XMLObject msgXML)
				throws IOException {
			String hostuser = msgXML.getString("hostuser");
			String aimuser = msgXML.getString("aimuser");
			String request = hostuser + "想添加你为好友";
			int select = JOptionPane.showConfirmDialog(null, request, "好友添加",
					JOptionPane.YES_NO_OPTION);
			switch (select) {
			// 同意添加, 则添加双方到数据库, 并且通知另外的朋友更新好友列表
			case JOptionPane.YES_OPTION:
				output.writeObject(QQMessage.rsFriendAdd(hostuser, aimuser, true,
						null));
				break;
			// 拒绝添加
			case JOptionPane.NO_OPTION:
				output.writeObject(QQMessage.rsFriendAdd(hostuser, aimuser, false,
						null));
			}
		}
		
		public synchronized void commandDeliver(XMLObject msgXML)
				throws IOException {
			String hostuser = msgXML.getString("hostuser");
			String aimuser  = msgXML.getString("aimuser");
			String basename = QQTool.basename(msgXML.getString("filename")); 
			String request = hostuser + " 想传输文件 " + basename + "给你";
			
			int select = JOptionPane.showConfirmDialog(null, request, "文件传输",
					JOptionPane.YES_NO_OPTION);
			
			if(select == JOptionPane.YES_OPTION){
				QQFileWrite writer = new QQFileWrite(basename);
				fileStreams.put(basename, writer);
				sendMessage(QQMessage.rsDeliver(aimuser, hostuser, 
						true, msgXML.getString("filename")));
				return ;
			}
			sendMessage(QQMessage.rsDeliver(aimuser, hostuser, 
					false, null));
		}
		
		/**
		 * 服务器告知朋友是否添加成功, 成功的话更新好友列表
		 * @param msgXML
		 */
		public synchronized void resultDeliver(XMLObject msgXML) {
			String hostuser = msgXML.getString("hostuser");
			
			if(Boolean.valueOf(msgXML.getString("success"))){
				friendList.startDeliver(hostuser, msgXML.getString("filename"));
				return;
			}
			
			JOptionPane.showMessageDialog(null, hostuser + " 拒绝了你的传输请求");
		}
		
		public synchronized void resultLeave(XMLObject msgXML) {
			dataMessage = (Map<String, Stack<String>>)msgXML.get("messages");
			rsFriendGet(dataFriends);
		}
		
		public synchronized void resultFriendAdd(XMLObject msgXML){
			String hostuser = msgXML.getString("hostuser");
			if(Boolean.valueOf(msgXML.getString("success"))){
				JOptionPane.showMessageDialog(null, hostuser + " 已经和你成为好友");
				System.out.println(msgXML.get("accounts"));
				rsFriendGet((ArrayList<Account>)msgXML.get("accounts"));
			}else{
				JOptionPane.showMessageDialog(null, "添加好友失败");
			}
		}
		/**
		 * 删除好友列表结果
		 * @param msgXML
		 */
		public synchronized void resultFriendDelete(XMLObject msgXML) {
			rsFriendGet((ArrayList<Account>)msgXML.get("accounts"));
			JOptionPane.showMessageDialog(null, msgXML.get("hostuser")
					+ " 从好友列表删除了你");
		}
		/**
		 * 服务器返还的最新的好友列表
		 * @param msgXML
		 */
		public synchronized void resultFriendGet(XMLObject msgXML) {
			rsFriendGet((ArrayList<Account>)msgXML.get("accounts"));
		}

		public synchronized void messageDeliver(XMLObject msgXML) {
			String basename = msgXML.getString("basename");
			QQFileWrite writer = fileStreams.get(basename);
			if(writer != null){
				if(writer.write(msgXML)){
					fileStreams.remove(basename);
					JOptionPane.showMessageDialog(null, "文件传输完毕");
				}
			}
		}
		/**
		 * 此方法是聊天的核心 将从服务器接收到的数据进行判断，如果双发聊天窗口都是打开的则直接传输数据到私聊窗口上;
		 * 否则,存储数据到临时栈中，等待新开私聊窗口的时候更新数据
		 * 
		 * @param hostuser
		 * @param aimuser
		 * @param message
		 */
		public synchronized void messageChat(XMLObject msgXML) {
			String hostuser = msgXML.getString("hostuser");
			String message = msgXML.getString("message");
			// 对方发送数据
			Stack<String> msgs = dataMessage.get(hostuser);
			
			// 对方的私聊窗口
			QQChatFrame chatFrame = friendList.getChatFrame(hostuser);
			//窗口未打开则存在临时内存中
			if (chatFrame == null) {
				if (dataMessage.get(hostuser) == null) {
					dataMessage.put(hostuser, new Stack<String>());
				}
				dataMessage.get(hostuser).push(message);
			} else {
				chatFrame.showMessage(message);
			}
			friendList.updateFriends(getListMessage());
		}

		
		/**
		 * ===================================================
		 * 部分提供给视图层的方法
		 =====================================================*/
		//获取客户端Socket
		public Socket getClient() {
			return client;
		}
		//获取当前用户
		public Account getAccount() {
			return account;
		}
		//获取当前输出流
		public synchronized ObjectOutputStream getOutput(){
			return output;
		}
		//获取指定下标下的好友
		public synchronized String getFriendName(int index){
			if(dataFriends.get(index) != null)
				return dataFriends.get(index).getUsername();
			
			return null;
		}
		//弹出存在临时内存中的数据
		public synchronized Stack<String> popMessage(String aimuser){
			Stack<String> newMessage = new Stack<String>();
			Stack<String> oldMessage = dataMessage.get(aimuser);
			while(oldMessage != null && !oldMessage.isEmpty()){
				newMessage.push(oldMessage.pop());
			}
			
			return newMessage;
		}
		
		public synchronized String[] getListMessage(){
			String[] listCompany = new String[dataFriends.size()];
			for(int i = 0; i < listCompany.length; i++){
				String username = dataFriends.get(i).getUsername();
				int count    = dataMessage.get(username) != null ? 
							   dataMessage.get(username).size()  : 0;
				listCompany[i] = username + (count != 0 ? " 未读:" + count : "");
			}
			
			return listCompany;
		}

}
