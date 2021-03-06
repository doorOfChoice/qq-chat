package cn.seeonce.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cn.seeonce.library.QQMessage;
import cn.seeonce.model.QQModel;


public class QQAddFriendFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private ObjectOutputStream output;
	private String           username;
	
	private JTextField       friendName;
	private JButton          add;
	
	public QQAddFriendFrame(String username, ObjectOutputStream output){
		this.output   = output;
		this.username = username;
		initAssembly();
	}
	
	public void initAssembly(){
		JPanel panel = new JPanel(new BorderLayout());		
		
		panel.add(add = new JButton("add"), BorderLayout.EAST);
		panel.add(friendName = new JTextField(), BorderLayout.CENTER);
		
		add.addActionListener(new ButtonEvent());
		add(panel);
		
		setTitle("friend add");
		setSize(240, 80);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//默认退出
	}
	
	class ButtonEvent implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			
			try{
				if(obj == add)
				{output.writeObject(QQMessage.cmFriendAdd(username, friendName.getText()));}
			}catch(Exception ex){ex.printStackTrace();}
		}
		
	}
	
}
