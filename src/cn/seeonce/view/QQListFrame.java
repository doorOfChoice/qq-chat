package cn.seeonce.view;

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
import java.util.Arrays;
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

import com.alibaba.fastjson.JSON;

import cn.seeonce.controller.ClientLocalController;
import cn.seeonce.data.Account;
import cn.seeonce.data.XMLObject;
import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;
import cn.seeonce.model.QQModel;

public class QQListFrame extends JFrame {

	private static final int WIDTH = 240;
	
	private static final int HEIGHT = 600;
	
	//聊天服务器控制器的引用
	private ClientLocalController controller;
	// swing frame映射
	private Map<String, QQChatFrame> friendsFrame;
	// swing 好友列表
	private JList list;
	// swing 添加好友按钮
	private JButton addFriend;
	// swing 弹出菜单
	private JPopupMenu menu;
	// swing 删除好友菜单
	private JMenuItem deleteFriend;
	// swing 添加好友窗口
	private JFrame addFriendFrame = null;

	public QQListFrame(ClientLocalController controller) {
		this.controller = controller;
		friendsFrame = new HashMap<String, QQChatFrame>();
		// 注册组件
		initAssembly();

	}

	/**
	 * 初始化界面组件
	 */
	public void initAssembly() {
		setLayout(new BorderLayout());

		add(new JScrollPane(list = new JList()), BorderLayout.CENTER);
		add(addFriend = new JButton("new friend"), BorderLayout.NORTH);
		addFriend.addActionListener(new ListEvent());

		menu = new JPopupMenu();
		menu.add(deleteFriend = new JMenuItem("delete friend"));

		deleteFriend.addActionListener(new ListEvent());
		/*
		 * 1. 右键显示弹出窗 2. 打开一个私聊窗口, 并且将存储在数据结构中的数据传给私聊窗口
		 */
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					menu.show(QQListFrame.this, e.getX(), e.getY());
					// 显示聊天栏目
				} else if (e.getButton() == MouseEvent.BUTTON1) {
					int index = list.getSelectedIndex();
					if (index != -1) {
						String aimuser = controller.getFriendName(index);
						// 窗口不存在则新建私聊窗口
						if (friendsFrame.get(aimuser) == null) {
							QQChatFrame chatFrame = new QQChatFrame(controller.getAccount().getUsername(),
									aimuser, controller.getOutput(), friendsFrame);
							//将存储在临时内存中的消息放入聊天窗口
							Stack<String> msg = controller.popMessage(aimuser);
							while(!msg.isEmpty()){
								chatFrame.showMessage(msg.pop());
							}
							//更新好友列表
							QQListFrame.this.updateFriends(controller.getListMessage());
							friendsFrame.put(aimuser, chatFrame);
						}

					}
				}
			}
		});

		setTitle(controller.getAccount().getUsername());
		setSize(WIDTH, HEIGHT);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 默认退出
	}

	/**
	 * 本项目中的所有ActionListener事件集合
	 * 
	 * @author dawndevil
	 */
	class ListEvent implements ActionListener {
		/* 从双方删除好友 */
		private void friendDelete() {
			int index;
			if ((index = list.getSelectedIndex()) == -1)
				return;
			// 获取好友的用户名
			String frienduser = controller.getFriendName(index);
			/*
			 * 从数据库删除双发数据 删除成功则直接更新本用户好友数据 向服务器发送xml通知另外一个用户删除自己
			 */
			sendMessage(QQMessage.cmFriendDelete(controller.getAccount().getUsername(),
					frienduser));
		}
		
		private synchronized void sendMessage(XMLObject msgXML){
			try {
				controller.getOutput().writeObject(msgXML);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			// 添加好友
			if (obj == addFriend) {
				if (addFriendFrame == null) {
					addFriendFrame = new QQAddFriendFrame(
							controller.getAccount().getUsername(), controller.getOutput());
				}

				addFriendFrame.setVisible(true);
			} else if (obj == deleteFriend) {
				friendDelete();
			}
		}
	}

	public synchronized void updateFriends(String[] listCompany){
		list.setListData(listCompany);
	}
	
	public synchronized QQChatFrame getChatFrame(String aimuser){
		return friendsFrame.get(aimuser);
	}
	
	public synchronized void startDeliver(String aimuser, String filename){
		if(friendsFrame.get(aimuser) != null){
			friendsFrame.get(aimuser).startDeliver(filename);
		}
	}
}
