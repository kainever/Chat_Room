package com.server.handler;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.server.startup.HandlerThread;
import com.server.user.UserService;
import com.transmit.protocol.Message;

//import Server.UserCollection.UserDB;

/**
 * 服务端请求处理基类
 * 模板方法的使用
 * @author slave_1
 */
public abstract class AbstractRequestHandler implements Runnable {
	
	public static Logger log = Logger.getLogger(AbstractRequestHandler.class);
	
	final  BlockingQueue<Message> pendingMessage;
	private boolean isRunning;
	HandlerThread handler;
	UserService userService;
	
//	
	public AbstractRequestHandler() throws IOException{
		this.pendingMessage = new LinkedBlockingQueue<Message>(10);
		userService = UserService.getInstance();
	}
	
	public HandlerThread getHandler() {
		return handler;
	}

	public void setHandler(HandlerThread handler) {
		this.handler = handler;
	}

	public void addMessage(Message message) {
		pendingMessage.add(message);
		log.info("添加消息 "+ message +  this.getClass().getName() + "的队列");
	}
	
	
	/**
	 * 处理请求的一个钩子函数
	 * 模板方法的使用
	 */
	public abstract void handleRequest(Message msg);
	
	
	@Override
	public void run() {
		log.info(this.getClass().getName() + " 处理线程启动....");
		isRunning = true;
		try {
			doRunLoop();
		} catch (InterruptedException e) {
			isRunning = false;
			log.error(this.getClass().getName() + " error , 退出..");
			e.printStackTrace();
		}
	}
	
	public void doRunLoop() throws InterruptedException {
		while (isRunning) {
			Message msg = pendingMessage.take();
			log.info(this.getClass().getName() + " 线程取得消息...." + msg);
			handleRequest(msg);
		}
	}
	
	
}
