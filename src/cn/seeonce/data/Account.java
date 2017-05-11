package cn.seeonce.data;

import java.io.Serializable;

public class Account implements Serializable{

	private static final long serialVersionUID = 1L;

	private String username = null;
	
	private String password = null;
	
	private int datetime = 0;
	
	private String sex = "无";
	
	private String nickname = null;
	
	public Account(){
		
	}
	
	public Account(String username, String password, int datetime){
		setUsername(username);
		setPassword(password);
		setDatetime(datetime);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public int getDatetime() {
		return datetime;
	}

	public void setDatetime(int datetime) {
		this.datetime = datetime;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Account){
			Account objAccount = (Account)obj;
			
			return objAccount.username == username;
		}
		return false;
		
	}
	
	@Override
	public String toString(){
		return username + "(" + nickname + ")";
	}
}
