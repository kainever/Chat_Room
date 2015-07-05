package com.server.request;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.transmit.protocol.Message;

public class MsgQueue extends MsgCollection {

	private Queue<Message> queue = new LinkedBlockingQueue<Message>();

	private static MsgQueue s_msgQueue;

	private MsgQueue() {

	}

	public static MsgQueue getInstance() {
		if (s_msgQueue == null) {
			s_msgQueue = new MsgQueue();
			log.info("服务器消息队列创建成功，等待接收消息。。。");
		} else {
			log.info("消息队列已经存在，返回已存在的实例");
		}
		return s_msgQueue;
	}

	@Override
	public  void addMsg(Message msg) {
		queue.offer(msg); // 添加一个元素并返回true；如果队列已满，则返回false
	}

	@Override
	public  Message getMsg() {
		Message result = null;
		try {
			result = ((LinkedBlockingQueue<Message>) queue).take();// pull移除并返问队列头部的元素；如果队列为空，则返回null
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * put 添加一个元素 如果队列满，则阻塞; take 移除并返回队列头部的元素 如果队列为空，则阻塞
	 */

}
