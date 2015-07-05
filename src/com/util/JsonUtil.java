package com.util;

import net.sf.json.JSONObject;

/**
 * json���󹤾��� put -- build
 * get --- parse
 * @author slave_1
 */
public class JsonUtil {

	public static String buildJson(String key, Object obj) {
		// JSON��ʽ���ݽ�������
		JSONObject jo = new JSONObject();
		jo.put(key, obj);
		return jo.toString();
	}

	/**
	 * �����ܵ����ַ���������json���� , Ȼ��õ�key��صĶ���
	 * @param jsonString
	 * @param key 
	 * @return
	 */
	public static Object parseJson(String jsonString, String key) {
		JSONObject jsonObj = JSONObject.fromObject(jsonString);

		return jsonObj.get(key);
	}

}
