package com.client.response;

/**
 * @author slave_1
 * 响应集合/队列的管理者
 */
public interface ResponseCollectionManager {
	
	public void addMsg(String msg);

	public String getMsg();
}
