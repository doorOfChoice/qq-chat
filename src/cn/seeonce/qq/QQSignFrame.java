package cn.seeonce.qq;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cn.seeonce.model.QQSql;
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
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			
			if(obj == sign){
				if(QQSql.sign(username.getText(), password.getText()) > 0){
					JOptionPane.showMessageDialog(null, "注册成功");
				}
			}
		}
		
	}

}
