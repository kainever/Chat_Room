package com.client.handler;

import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * 客户端handler的抽象基类
 * 策略模式的使用
 * @author slave_1
 */
public abstract class AbstractResponseHandler {
	
	public static final Logger log = Logger.getLogger(AbstractResponseHandler.class);

	protected String responseMsg;
	protected Socket socket;
	
	public AbstractResponseHandler(Socket socket){
		this.socket = socket;
	}
	
	public void setResponseMsg(String msg) {
		this.responseMsg = msg;
	}

	/**
	 * 不同的事件对应着不同的处理方法
	 */
	public abstract void handleResponse();
}
