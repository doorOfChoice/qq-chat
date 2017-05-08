package cn.seeonce.qq;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cn.seeonce.model.QQSql;


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
				//是否登录成功
				if(QQSql.login(username.getText(), password.getText())){
					Socket client = null;
					try {
						//创建一个客户端通信socket
						client = new Socket("localhost", 9999);
						
						//创建好友界面
						new QQListFrame(QQSql.getUser(username.getText()), client);
						//隐藏登录界面
						QQLoginFrame.this.setVisible(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		
	}
}
