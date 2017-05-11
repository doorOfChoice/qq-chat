package cn.seeonce.data;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @introduce: 用于生成单行XML对象
 * @author dawndevil
 *
 */
public class XMLObject implements Serializable{
	private String attribute = null;
	
	private Map<String, Object> keyValue;
	
	public XMLObject(){
		keyValue = new HashMap<String, Object>();
	}
	
	public XMLObject(Map<String, Object> keyValue){
		keyValue = keyValue;
	}
	
	//设置属性 <XML name="hello"/> XML是属性
	public void setAttribute(String attribute){
		this.attribute = attribute;
	}
	
	public String getAttribute(){
		return attribute;
	}
	
	//添加子属性 比如name
	public void add(String key, Object value){
		keyValue.put(key, value);
	}
	
	public Object get(String key){
		return keyValue.get(key);
	}
	
	public String getString(String key){
		return (String)get(key);
	}
	
	//创建XML字符串
	public String create(){
		
		if(attribute == null)
			return null;
		
		StringBuffer buf = new StringBuffer();
		buf.append("<");
		buf.append(attribute);
		for(String key : keyValue.keySet()){
			buf.append(" ");
			buf.append(key);
			buf.append("=");
			buf.append("\"");
			buf.append(keyValue.get(key));
			buf.append("\"");
		}
		
		buf.append("/>");
		
		return buf.toString();
	}
	
	//获取匹配器
	private static Matcher getMatcher(String pattern, String str){
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
	
	@Override
	public String toString(){
		return create();
	}
	
}
