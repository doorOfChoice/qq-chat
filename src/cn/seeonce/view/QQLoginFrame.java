package cn.seeonce.view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

import cn.seeonce.controller.QQClient;
import cn.seeonce.model.QQMessage;
import cn.seeonce.model.QQSql;
import cn.seeonce.model.QQTool;
import cn.seeonce.qq.data.Account;


public class QQLoginFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton        login;
	private JButton		   openSign;
	private JTextField     username;
	private JPasswordField password;
	private JFrame         signFrame = null;
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
	
	public static void main(String[] args) {
		new QQLoginFrame();
	}
	
	class ButtonEvent implements ActionListener{
		private void verifyAccount(String username, String password){
			try {
				Socket server = new Socket("localhost", 9998);
				DataOutputStream output = new DataOutputStream(server.getOutputStream());
				DataInputStream  input  = new DataInputStream(server.getInputStream());
				//发送登录请求
				output.writeUTF(QQMessage.cmLogin(username, username, password));
				//接受服务器响应
				String str = input.readUTF();
				Map<String, String> msgXML = QQTool.analyseXML(str);
				if(Boolean.valueOf(msgXML.get("success"))){
					Account user  = JSON.parseObject(msgXML.get("account"),Account.class);
					
					Socket client = new Socket("localhost", 9999);
					
					new QQClient(user, client);
					
					setVisible(false);
				}
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
