package com.server.user;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * 映射表, 每一个用户 通过它的ip:port 与 对应的socketChannel(ip:port)对应
 * 服务器，通过这个表，转发消息
 * @author slave_1
 */
public class UserMap {
	public static Logger log = Logger.getLogger(UserMap.class);
	
	private static Map<String, SocketChannel> userMap = new ConcurrentHashMap<String, SocketChannel>();

//	public HashMap<String, SocketChannel> getUsersMap() {
//		return usersMap;
//	}
//
//	public void setUsersMap(HashMap<String, SocketChannel> usersMap) {
//		this.usersMap = usersMap;
//	}

	/**
	 * 是否包含key
	 * @param mapKey
	 * @return
	 */
	public static boolean contains(String mapKey) {
		return userMap.containsKey(mapKey);
	}

	/**
	 * 插入key-value
	 * @param mapKey
	 * @param socketChannel
	 */
	public static void put(String mapKey, SocketChannel socketChannel) {
		userMap.put(mapKey, socketChannel);
		log.info("usermap 插入一条记录 == " + mapKey + " -> " + socketChannel) ;
	}

	public static void remove(String kv) {
		userMap.remove(kv);
		log.info("usermap 移除一条记录  key == " + kv);
	}

	public static SocketChannel get(String string) {
		return userMap.get(string);
	}

}
