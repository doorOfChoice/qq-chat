package cn.seeonce.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.alibaba.fastjson.JSON;

import cn.seeonce.model.QQMessage;
import cn.seeonce.model.QQSql;
import cn.seeonce.model.QQTool;
import cn.seeonce.qq.data.Account;

public class QQSignFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton        sign;
	private JTextField     username;
	private JPasswordField password;

	public QQSignFrame(){
		initAssembly();
		setTitle("See Once Sign");
		setSize(480, 320);
		setLayout(null);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//默认退出  
	}
	
	public void initAssembly(){

		sign     = new JButton("Sign");
		username = new JTextField();
		password = new JPasswordField();
		
		sign.addActionListener(new ButtonEvent());
		
		username.setBounds(100, 100, 280, 30);
		password.setBounds(100, 160, 280, 30);
		sign.setBounds(200, 240, 80, 30);
		

		add(sign);
		add(username);
		add(password);
		
	}
	
	
	class ButtonEvent implements ActionListener{
		private void sign(String username, String password){
			Socket server;
			try {
				server = new Socket("localhost", 9998);
				
				DataOutputStream output = new DataOutputStream(server.getOutputStream());
				DataInputStream  input  = new DataInputStream(server.getInputStream());
				//发送登录请求
				output.writeUTF(QQMessage.cmSign(username, username, password));
				//接受服务器响应
				String str = input.readUTF();
				System.out.println(str);
				Map<String, String> msgXML = QQTool.analyseXML(str);
				//显示服务器返回的信息
				JOptionPane.showMessageDialog(null, msgXML.get("message"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			
			if(obj == sign){
				sign(username.getText(), password.getText());
				System.out.println("gg");
			}
		}
		
	}

}
