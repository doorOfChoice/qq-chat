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
	
	
	/**
	 * 首字母转大写	
	 * @param str
	 * @return
	 */
	public static String first2up(String str){
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
	
	/**
	 * 获取绝对路径下的文件名
	 * @param filename
	 * @return
	 */
	public static String basename(String filename){
		int start = filename.lastIndexOf("/");
		return start == -1 ? filename : filename.substring(start + 1);
	}
	
	/**
	 * 获取源字节数组下指定范围内的数据
	 * @param src
	 * @param offset
	 * @param len
	 * @return
	 */
	 public static byte[] getBytes(byte[] src, int offset, int len){
		byte[] nBuf = new byte[len];
		for(int i = 0; i < len; i++){
		   nBuf[i] = src[i + offset];
		}
		                
		return nBuf;
     }

}
