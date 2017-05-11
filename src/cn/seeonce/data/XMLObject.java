package cn.seeonce.data;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @introduce: 用于生成单行XML对象
 * @author dawndevil
 *
 */
public class XMLObject {
	private String attribute = null;
	
	private Map<String, String> keyValue;
	
	public XMLObject(){
		keyValue = new HashMap<String, String>();
	}
	
	//设置属性 <XML name="hello"/> XML是属性
	public void setAttribute(String attribute){
		this.attribute = attribute;
	}
	
	public String getAttribute(){
		return attribute;
	}
	
	//添加子属性 比如name
	public void add(String key, String value){
		keyValue.put(key, value);
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
			buf.append(URLEncoder.encode(keyValue.get(key)));
			buf.append("\"");
		}
		
		buf.append("/>");
		
		return buf.toString();
	}
	
	@Override
	public String toString(){
		return create();
	}
	
}
