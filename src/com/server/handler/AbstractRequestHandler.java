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
 * ��������������
 * ģ�巽����ʹ��
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
		log.info("�����Ϣ "+ message +  this.getClass().getName() + "�Ķ���");
	}
	
	
	/**
	 * ���������һ�����Ӻ���
	 * ģ�巽����ʹ��
	 */
	public abstract void handleRequest(Message msg);
	
	
	@Override
	public void run() {
		log.info(this.getClass().getName() + " �����߳�����....");
		isRunning = true;
		try {
			doRunLoop();
		} catch (InterruptedException e) {
			isRunning = false;
			log.error(this.getClass().getName() + " error , �˳�..");
			e.printStackTrace();
		}
	}
	
	public void doRunLoop() throws InterruptedException {
		while (isRunning) {
			Message msg = pendingMessage.take();
			log.info(this.getClass().getName() + " �߳�ȡ����Ϣ...." + msg);
			handleRequest(msg);
		}
	}
	
	
}
