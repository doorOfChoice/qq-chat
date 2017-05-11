package cn.seeonce.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.event.WindowAdapter;

import cn.seeonce.data.XMLObject;
import cn.seeonce.library.QQMessage;
import cn.seeonce.library.QQTool;

public class QQChatFrame extends JFrame{
	
	private ObjectOutputStream output;
	private String           hostuser;
	private String           aimuser ;
	//朋友-》窗口映射, 放在这里是因为要在退出时删除自己
	private Map<String, QQChatFrame> friendsFrame;
	
	private JTextArea        showpanel;
	private JTextArea		 sendpanel;
	private JButton          send;
	private JButton			 delivery;
	private JFileChooser     chooser;
	private Executor         pool;
	public QQChatFrame(String hostuser, String aimuser, 
			ObjectOutputStream output, Map<String, QQChatFrame> friendsFrame){
		this.output       = output;
		this.aimuser      = aimuser;
		this.hostuser     = hostuser;
		this.friendsFrame = friendsFrame;
		this.chooser      = new JFileChooser();
		this.pool         = Executors.newCachedThreadPool();
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
		buttonPanel.add(delivery  = new JButton("deliver file"));
		
		send.addActionListener(new ButtonEvent());
		delivery.addActionListener(new ButtonEvent());
		
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
		
		setTitle(hostuser +  " is chatting with " + aimuser);
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
	
	
	public void startDeliver(String filename){
		pool.execute(new FileDeliver(filename));
	}
	
	private synchronized void sendMessage(XMLObject obj){
		try {
			output.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	class ButtonEvent implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			try {
				if(obj == send){
					String message = hostuser + " said:)\n" + sendpanel.getText() + "\n";
					output.writeObject(QQMessage.msgChat(hostuser, aimuser, message));
					showpanel.setText(showpanel.getText() + message);
				}else if(obj == delivery){
					chooser.showOpenDialog(null);
					File file = chooser.getSelectedFile();
					if(file != null){
						sendMessage(QQMessage.cmDeliver(hostuser, aimuser, file.getAbsolutePath()));
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * 文件传输任务
	 * @author dawndevil
	 */
	class FileDeliver implements Runnable{
		private final String filename;
		private DataInputStream fin;
		
		public FileDeliver(String filename){
			this.filename = filename;
			System.out.println(filename);
			
		}
		
		@Override
		public void run() {
			try {
				int count = -1;
				byte[] tempBuf; //base64转码后的byte[]
				byte[] buf = new byte[1024];
				String basename = QQTool.basename(filename);
				fin = new DataInputStream(new FileInputStream(filename));
				while((count = fin.read(buf)) != -1){
					//将读取到的byte[]进行base64编码
					tempBuf = Base64.getEncoder().encode(QQTool.getBytes(buf, 0, count));
					String bufString = new String(tempBuf);
					System.out.println("数据包" + bufString);
					sendMessage(QQMessage.msgFile(aimuser, basename, bufString, false));
				}
				//发送结束标志
				sendMessage(QQMessage.msgFile(aimuser, basename, "end", true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
