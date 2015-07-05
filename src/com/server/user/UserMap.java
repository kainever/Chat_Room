package com.server.user;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * ӳ���, ÿһ���û� ͨ������ip:port �� ��Ӧ��socketChannel(ip:port)��Ӧ
 * ��������ͨ�������ת����Ϣ
 * @author slave_1
 */
public class UserMap {
	public static Logger log = Logger.getLogger(UserMap.class);
	
	private static HashMap<String, SocketChannel> userMap = new HashMap<String, SocketChannel>();

//	public HashMap<String, SocketChannel> getUsersMap() {
//		return usersMap;
//	}
//
//	public void setUsersMap(HashMap<String, SocketChannel> usersMap) {
//		this.usersMap = usersMap;
//	}

	/**
	 * �Ƿ����key
	 * @param mapKey
	 * @return
	 */
	public static boolean contains(String mapKey) {
		return userMap.containsKey(mapKey);
	}

	/**
	 * ����key-value
	 * @param mapKey
	 * @param socketChannel
	 */
	public static void put(String mapKey, SocketChannel socketChannel) {
		userMap.put(mapKey, socketChannel);
		log.info("usermap ����һ����¼ == " + mapKey + " -> " + socketChannel) ;
	}

	public static void remove(String kv) {
		userMap.remove(kv);
		log.info("usermap �Ƴ�һ����¼  key == " + kv);
	}

	public static SocketChannel get(String string) {
		return userMap.get(string);
	}

}
