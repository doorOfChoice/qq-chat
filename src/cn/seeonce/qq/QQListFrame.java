package cn.seeonce.qq;

import java.awt.BorderLayout;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import cn.seeonce.model.QQListener;
import cn.seeonce.model.QQMessage;
import cn.seeonce.model.QQSql;
import cn.seeonce.model.QQTool;
import cn.seeonce.qq.data.Account;

public class QQListFrame extends JFrame{
	
	private static final int WIDTH = 240;
	private static final int HEIGHT = 600;
	//客户端socket
	private Socket client;
	//客户端用户信息
	private Account account;
	//客户端好友列表
	private ArrayList<Account> dataFriends;
	//客户端信息列表
	private Map<String, Stack<String>> dataMessage;
	//swing frame映射
	private Map<String, QQChatFrame> friendsFrame;
	//向服务器的接收流
	private DataInputStream input   = null;
	//向服务器的输出流
	private DataOutputStream output = null;
	//监听任务
	private Thread task;
	//swing 好友列表
	private JList list;
	//swing 添加好友按钮
	private JButton addFriend;
	//swing 弹出菜单
	private JPopupMenu menu;
	//swing 删除好友菜单
	private JMenuItem deleteFriend;
	//swing 添加好友窗口
	private JFrame addFriendFrame = null;
	
	public QQListFrame(Account account, Socket client){
		this.account = account;
		this.client = client;
		try {
			dataMessage = new HashMap<String, Stack<String>>();
			friendsFrame = new HashMap<String, QQChatFrame>();
			
			input  = new DataInputStream(client.getInputStream());
			output = new DataOutputStream(client.getOutputStream());
			//开始监听任务
			task   = new Thread(new ServerListener());
			task.start();
			//向服务器注册key-value
			output.writeUTF(account.getUsername());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//注册组件
		initAssembly();
		
	}
	
	/**
	 * 初始化界面组件
	 */
	public void initAssembly(){
		setLayout(new BorderLayout());
		//获取好友列表
		dataFriends = QQSql.getFriends(account.getUsername());
		
		add(new JScrollPane(list = new JList(dataFriends.toArray())), BorderLayout.CENTER);
		add(addFriend = new JButton("new friend"), BorderLayout.NORTH);
		
		addFriend.addActionListener(new ListEvent());
		
		menu = new JPopupMenu();
		menu.add(deleteFriend = new JMenuItem("delete friend"));
		
		deleteFriend.addActionListener(new ListEvent());
		/*
		 * 1. 右键显示弹出窗
		 * 2. 打开一个私聊窗口, 并且将存储在数据结构中的数据传给私聊窗口
		 */
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {  
	            if (e.getButton() == MouseEvent.BUTTON3) {  
	            	menu.show(QQListFrame.this, e.getX(), e.getY());
	            //显示聊天栏目
	            }else if(e.getButton() == MouseEvent.BUTTON1){
	            	int index = list.getSelectedIndex();
	            	if(index != -1){
		            	String aimuser = dataFriends.get(index).getUsername();
		            	//窗口不存在则新建窗口
		            	if(friendsFrame.get(aimuser) == null){
		            		friendsFrame.put(aimuser, new QQChatFrame(account.getUsername(), aimuser,
		            			         output, friendsFrame));
		            		//将存储的数据发送到聊天栏目
		            		Stack<String> storedMessage = dataMessage.get(aimuser);
		            		while(storedMessage != null && !storedMessage.isEmpty()){
		            			friendsFrame.get(aimuser).showMessage(storedMessage.pop());
		            		}
		            	}
		            	
	            	}
	            }
	        }     
		});
		
		
		setTitle("Friends List");
		setSize(WIDTH, HEIGHT);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//默认退出 
	}
	
	/**
	 * 本项目中的所有ActionListener事件集合
	 * @author dawndevil
	 */
	class ListEvent implements ActionListener{
		/*从双方删除好友*/
		private void friendDelete(){
			int index;
			if((index = list.getSelectedIndex()) == -1)
				return;
			//获取好友的用户名
			String frienduser = dataFriends.get(index).getUsername();
			/*
			 * 从数据库删除双发数据
			 * 删除成功则直接更新本用户好友数据
			 * 向服务器发送xml通知另外一个用户删除自己
			 */
			if(QQSql.removeFriend(account.getUsername(), frienduser)){
				dataFriends.remove(index);
				list.setListData(dataFriends.toArray());
				//服务通知
				try {
					output.writeUTF(QQMessage.rsFriendDelete(account.getUsername(), frienduser));
				} catch (IOException e) 
				{e.printStackTrace();}
			
			}
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			//添加好友
			if(obj == addFriend){
				if(addFriendFrame == null)
				{addFriendFrame = new QQAddFriendFrame(account.getUsername(), client);}
				
				addFriendFrame.setVisible(true);
			}else if(obj == deleteFriend){
				friendDelete();
			}
		}
	}
	
	/**
	 * 服务器监听类
	 * 该类起到了响应所有客户端-服务器数据的作用
	 * 并且对服务器传来的数据进行展示
	 * @author dawndevil
	 */
	class ServerListener implements QQListener, Runnable{
		
		private boolean interrupt = false;
		
		public ServerListener(){}
		/*重新拉取好友列表*/
		private void regetFriends(){
			dataFriends = QQSql.getFriends(account.getUsername());
			list.setListData(dataFriends.toArray());
			list.repaint();
		}
		
		/*响应添加好友的命令*/
		private void commandFriendAdd(String hostuser, String aimuser) throws IOException{
			if(aimuser.equals(account.getUsername())){
				String request  = hostuser + "想添加你为好友";
				int select = JOptionPane.showConfirmDialog(null, request, "好友添加",
			              JOptionPane.YES_NO_OPTION);
				switch(select){
				//同意添加, 则添加双方到数据库, 并且通知另外的朋友更新好友列表
				case JOptionPane.YES_OPTION:
					QQSql.addFriend(hostuser, aimuser);
					output.writeUTF(QQMessage.rsFriendAdd(hostuser, true));
					regetFriends();
					break;
				//拒绝添加
				case JOptionPane.NO_OPTION:
					output.writeUTF(QQMessage.rsFriendAdd(hostuser, false));
				}
			}
		}
		/*响应删除好友的结果*/
		private void resultFriendDelete(String hostuser, String aimuser){
			if(aimuser.equals(account.getUsername())){
				regetFriends();
				JOptionPane.showMessageDialog(null, hostuser + " 从好友列表删除了你");
			}
		}
		/*响应添加好友的结果*/
		private void resultFriendAdd(String aimuser, boolean success){
			if(aimuser.equals(account.getUsername())){
				if(success){
					regetFriends();
					JOptionPane.showMessageDialog(null, "添加好友成功");
				}
				else
				{JOptionPane.showMessageDialog(null, "添加请求被对方拒绝");}
			}
		}
		
		/**
		 * 此方法是聊天的核心
		 * 将从服务器接收到的数据进行判断，如果双发聊天窗口都是打开的则直接传输数据到私聊窗口上;
		 * 否则,存储数据到临时栈中，等待新开私聊窗口的时候更新数据
		 * @param hostuser
		 * @param aimuser
		 * @param message
		 */
		private void messageGet(String hostuser, String aimuser, String message){
			if(aimuser.equals(account.getUsername())){
				//对方发送数据
				Stack<String> msgs = dataMessage.get(hostuser);
				//对方的私聊窗口
				QQChatFrame chatFrame = friendsFrame.get(hostuser);
				
				if(chatFrame == null){
					if(msgs == null){
						dataMessage.put(hostuser, new Stack<String>());
					}
					dataMessage.get(hostuser).push(message);
				}else{
					chatFrame.showMessage(message);
				}
			}
		}
		
		private void analyseMessage(Map<String, String> msgXML) throws IOException{
			String attr = msgXML.get("attribute");
			
			if(attr.equals(QQMessage.COMMAND)){
				//接受到好友添加请求
				String name = msgXML.get("name");
				//添加好友请求
				if(name.equals("friendadd")){
					String aimuser = msgXML.get("aimuser");
					String hostuser = msgXML.get("hostuser");
					commandFriendAdd(hostuser, aimuser);
				}
			}else if(attr.equals(QQMessage.RESULT)){
				String name = msgXML.get("name");
				//添加好友的结果
				if(name.equals("friendadd")){
					String aimuser = msgXML.get("aimuser");
					boolean success = Boolean.valueOf(msgXML.get("success"));
					resultFriendAdd(aimuser, success);
				//删除好友的结果
				}else if(name.equals("frienddelete")){
					String aimuser = msgXML.get("aimuser");
					String hostuser = msgXML.get("hostuser");
					resultFriendDelete(hostuser, aimuser);
				}
			}else if(attr.equals(QQMessage.MESSAGE)){
				String aimuser = msgXML.get("aimuser");
				String hostuser = msgXML.get("hostuser");
				String message = msgXML.get("message");
				messageGet(hostuser, aimuser, message);
			}
		}
		
		@Override
		public void listen() {
			while(!interrupt){
				try {
					String message = input.readUTF();
					//System.out.println("client: " + message);
					
					Map<String, String> msgXML = QQTool.analyseXML(message);
					
					analyseMessage(msgXML);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {
			listen();
		}
		
	}

}
