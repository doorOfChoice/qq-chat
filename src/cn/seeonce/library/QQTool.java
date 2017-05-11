package cn.seeonce.library;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 该类提供聊天程序运行过程中可能会用到的某些辅助函数
 * 比如SHA-1加密等等
 * @author dawndevil
 */

public class QQTool {
	public final static String KEY_SHA = "SHA";
	public final static String KEY_MD  = "MD";

	/**
	 * SHA1加密算法
	 * @param message 任意要加密的字符串
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String sha1(String message){
		StringBuffer sha1Code = new StringBuffer();
		try {
			//获取SHA-1的实例
			MessageDigest mg = MessageDigest.getInstance(KEY_SHA);
			mg.update(message.getBytes());
			//获取经过计算的摘要
			byte[] dgt = mg.digest();
			
			//把每个字节转化为16进制
			for(int i = 0; i < dgt.length; ++i){
				String hex = Integer.toHexString(dgt[i] & 0xFF);
				
				if(hex.length() < 2)
					sha1Code.append("0");
				
				sha1Code.append(hex);
			}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return sha1Code.toString();
	}
	
	//获取匹配器
	public static Matcher getMatcher(String pattern, String str){
			Pattern pt = Pattern.compile(pattern);
			Matcher match = pt.matcher(str);
			
			return match;
		}
		
		//一般XML分析器
	public static Map<String, String> analyseXML(String str){
		System.out.println(str);
		
		if(!str.matches("<[\\w]+?(\\s+?[\\w]+?=\"([\\w\\W]*?)\")+/>"))
				return null;
			
		Matcher match = getMatcher("([\\w]+?)=\"([\\w\\W]*?)\"", str);
			
		Map<String, String> keyValue = new HashMap<String, String>();
			
		String attribute = str.substring(1, str.indexOf(" "));
			
		keyValue.put("attribute", attribute);
			
		while(match.find()){
			keyValue.put(match.group(1), URLDecoder.decode(match.group(2)));
		}
			
		return keyValue;
	}
	
	public static String first2up(String str){
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
	
	public static String basename(String filename){
		int start = filename.lastIndexOf("/");
		return start == -1 ? filename : filename.substring(start + 1);
	}
	
	public static byte[] getBytes(byte[] src, int offset, int len){
		byte[] nBuf = new byte[len];
		for(int i = 0; i < len; i++){
			nBuf[i] = src[i + offset];
		}
		
		return nBuf;
	}
	
	public static void main(String[] args){
		String aim = "ew  qewq\n";
		System.out.println(analyseXML("<name aimuser=\"" +aim+ "\"/>").get("aimuser"));
	}
}
