package cn.seeonce.model;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class QQFileWrite {
	private String filename;
	
	private DataOutputStream output;
	
	public QQFileWrite(String filename){
		this.filename = filename;
		System.out.println(filename);
		try {
			this.output   = new DataOutputStream(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized boolean write(Map<String, String> msgXML){
		try{
			if(Boolean.valueOf(msgXML.get("isEnd"))){
				output.close();
				return true;
			}
			//获取文件中真实的byte[]
			byte[] realBuf = Base64.getDecoder()
					               .decode(msgXML.get("message").getBytes());
			output.write(realBuf);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return false;
	}
}
