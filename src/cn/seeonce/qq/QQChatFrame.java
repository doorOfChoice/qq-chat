package cn.seeonce.qq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.WindowAdapter;
import cn.seeonce.model.QQMessage;
import cn.seeonce.qq.QQAddFriendFrame.ButtonEvent;

public class QQChatFrame extends JFrame{
	
	private DataOutputStream output;
	private String           hostuser;
	private String           aimuser ;
	//朋友-》窗口映射, 放在这里是因为要在退出时删除自己
	private Map<String, QQChatFrame> friendsFrame;
	
	private JTextArea        showpanel;
	private JTextArea		 sendpanel;
	private JButton          send;
	
	
	
	public QQChatFrame(String hostuser, String aimuser, 
			DataOutputStream output, Map<String, QQChatFrame> friendsFrame){
		this.output   = output;
		this.aimuser  = aimuser;
		this.hostuser = hostuser;
		this.friendsFrame = friendsFrame;
		initAssembly();
	}
	
	public void initAssembly(){
		
		setLayout(new BorderLayout());
		
		JPanel menu = new JPanel();
		
		menu.setLayout(new GridLayout(2, 1, 2, 2));
		menu.add(new JScrollPane(sendpanel = new JTextArea()));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 4, 0, 0));
		buttonPanel.add(send = new JButton("send"));
		send.addActionListener(new ButtonEvent());
		
		menu.add(buttonPanel);
		
		add(new JScrollPane(showpanel = new JTextArea()), BorderLayout.CENTER);
		showpanel.setBackground(Color.BLACK);
		showpanel.setForeground(Color.GREEN);
		
		add(menu, BorderLayout.SOUTH);
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosed(WindowEvent e){
				friendsFrame.remove(aimuser);
			}
		});
		
		setTitle("friend add");
		setSize(400, 500);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//默认退出
	}
	
	/*
	 * 用于显示私聊的信息
	 */
	public void showMessage(String message){
		showpanel.setText(showpanel.getText() + message);
		showpanel.repaint();
	}
	
	class ButtonEvent implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			try {
				if(obj == send){
					output.writeUTF(QQMessage.msgChat(hostuser, aimuser, 
							                          sendpanel.getText()));
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
	}
}
