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
			log.info("��������Ϣ���д����ɹ����ȴ�������Ϣ������");
		} else {
			log.info("��Ϣ�����Ѿ����ڣ������Ѵ��ڵ�ʵ��");
		}
		return s_msgQueue;
	}

	@Override
	public  void addMsg(Message msg) {
		queue.offer(msg); // ���һ��Ԫ�ز�����true����������������򷵻�false
	}

	@Override
	public  Message getMsg() {
		Message result = null;
		try {
			result = ((LinkedBlockingQueue<Message>) queue).take();// pull�Ƴ������ʶ���ͷ����Ԫ�أ��������Ϊ�գ��򷵻�null
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * put ���һ��Ԫ�� �����������������; take �Ƴ������ض���ͷ����Ԫ�� �������Ϊ�գ�������
	 */

}
