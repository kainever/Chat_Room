package com.util;

import net.sf.json.JSONObject;

/**
 * json对象工具类 put -- build
 * get --- parse
 * @author slave_1
 */
public class JsonUtil {

	public static String buildJson(String key, Object obj) {
		// JSON格式数据解析对象
		JSONObject jo = new JSONObject();
		jo.put(key, obj);
		return jo.toString();
	}

	/**
	 * 将接受到的字符串解析成json对象 , 然后得到key相关的对象
	 * @param jsonString
	 * @param key 
	 * @return
	 */
	public static Object parseJson(String jsonString, String key) {
		JSONObject jsonObj = JSONObject.fromObject(jsonString);

		return jsonObj.get(key);
	}

}
