package cn.seeonce.view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.alibaba.fastjson.JSON;

import cn.seeonce.controller.ClientLocalController;
import cn.seeonce.data.Account;
import cn.seeonce.data.XMLObject;
import cn.seeonce.entrance.EntranceChatServer;
import cn.seeonce.entrance.EntranceClient;
import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;
import cn.seeonce.model.QQModel;


public class QQLoginFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton        login;
	private JButton		   openSign;
	private JTextField     username;
	private JPasswordField password;
	private JFrame         signFrame;
	
	public QQLoginFrame(){
		initAssembly();
		setTitle("See Once Login");
		setSize(480, 320);
		setLayout(null);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//默认退出
		
	}
	
	public void initAssembly(){
		login    = new JButton("Login");
		openSign = new JButton("Sign");
		username = new JTextField();
		password = new JPasswordField();
		
		login.addActionListener(new ButtonEvent());
		openSign.addActionListener(new ButtonEvent());
		
		login.setBounds(260, 240, 80, 30);
		username.setBounds(100, 100, 280, 30);
		password.setBounds(100, 160, 280, 30);
		openSign.setBounds(140, 240, 80, 30);
		
		add(openSign);
		add(login);
		add(username);
		add(password);
		
	}
		
	class ButtonEvent implements ActionListener{
		private void verifyAccount(String username, String password){
			try {
				Socket server = new Socket("localhost", 9998);
				ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream  input  = new ObjectInputStream(server.getInputStream());
				//发送登录请求
				output.writeObject(QQMessage.cmLogin(username, username, password));
				//接受服务器响应
				
				XMLObject msgXML = null;
				try {
					msgXML = (XMLObject)input.readObject();
					System.out.println(msgXML);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if(Boolean.valueOf(msgXML.getString("success"))){
					Account user  = (Account)msgXML.get("account");
					Socket client = new Socket("localhost", EntranceChatServer.PORT);
					EntranceClient.newClient(client, user);
					setVisible(false);
					return;
				}
				
				JOptionPane.showMessageDialog(null, "登录失败");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			Object obj = arg0.getSource();
			
			if(obj == openSign){
				//第一次没加载窗口则加载一次
				if(signFrame == null)
				{signFrame = new QQSignFrame();}
				//否则只显示
				signFrame.setVisible(true);
			}else if(obj == login){
				verifyAccount(username.getText(), password.getText());
			}
		}
		
	}
}
