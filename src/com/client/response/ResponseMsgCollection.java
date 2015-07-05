package com.client.response;


/**
 * @author slave_1
 * 消息队列抽象类！AbstractResponseMsgQueue
 * 这样的的话 当你想采用其他方法实现消息队列的时候 扩展性不就更好
 * 这里采用阻塞队列...
 */
public abstract class ResponseMsgCollection {
	
	public abstract void addMsg(String msg);
	
	public abstract String getMsg();
}
