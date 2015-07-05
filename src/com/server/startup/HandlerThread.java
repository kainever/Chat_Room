package com.server.startup;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.server.handler.AbstractRequestHandler;
import com.server.user.UserMap;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;

/**
 * �����̣߳� ����Ϣ������ȡֵ, ����id �������Ӧ��Handler handler�Ǻ���ҵ���߼����еĵط�
 * 
 * @author slave_1
 */
public class HandlerThread extends Thread {
	
	public static Logger log = Logger.getLogger(HandlerThread.class);

	Server server;
	/** �������̳߳� */
	ExecutorService exec = Executors.newFixedThreadPool(10);
	
	boolean isRunning;

	/**
	 * ��Ӧ����
	 */
	final BlockingQueue<Message> responseMessage;
	
	ResponderThread responseThread;

	public HandlerThread(Server server) throws IOException {
		this.server = server;
		this.responseMessage = new LinkedBlockingQueue<Message>(50);
		// ���������߳�
		Set<Entry<String, AbstractRequestHandler>> set = server.requestMap
				.entrySet();
		for (Entry<String, AbstractRequestHandler> e : set) {
			// ���ø�����
			e.getValue().setHandler(this);
			exec.execute(e.getValue());
		}
		
		log.info("������Ӧ�߳�..");
		responseThread = new ResponderThread();
		responseThread.start();
	}

	public void run() {
		log.info("handler�߳�����");
		isRunning = true;
		while (isRunning) {
			Message msg = server.msgManager.getMsg(); // ����ʽ��ȡ
			log.info("��ȡ������Client��������ϢΪ" + msg + "��");

			// JSONObject json = (JSONObject) JsonTrans.parseJson(msg, "msg");
			// String key = json.getString("msgNum");// ����������Ϣ���
			String key = msg.getMsgNum();

			// ����ģʽ
			AbstractRequestHandler handler = (AbstractRequestHandler) server.requestMap.get(key);
			handler.addMessage(msg);
			// handler.handleRequest();
		}
	}

	/**
	 * ��Ӧ�߳�
	 * 
	 * @author slave_1
	 */
	class ResponderThread extends Thread {

		Selector writeSelector;
		boolean isRunning;

		public ResponderThread() throws IOException {
			writeSelector = Selector.open();
		}

		@Override
		public void run() {
			log.info("��Ӧ�߳�����");
			isRunning = true;
			try {
				doRunLoop();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void doRunLoop() throws IOException, InterruptedException {
			while (isRunning) {
				for(int i =0 ; i < responseMessage.size() ; i++) {
					Message res = responseMessage.take();
					SocketChannel channel = UserMap.get(res.getReceiverIP() + ":" + res.getReceiverPort());
					channel.register(responseThread.writeSelector, SelectionKey.OP_WRITE,res);
				}
				writeSelector.select();
				Set<SelectionKey> keys = writeSelector.selectedKeys();
		log.info("������д�¼� , �¼��� = " + keys.size());
				Iterator<SelectionKey> iter = keys.iterator();
				while (iter.hasNext()) {
					SelectionKey key= iter.next();
					iter.remove();
					if (key.isValid() && key.isWritable()) {
						doWrite(key);
					}
				}
			}
		}

		private void doWrite(SelectionKey key) throws IOException {
			SocketChannel channel = (SocketChannel) key.channel();
			Message res = (Message) key.attachment();
//			�Ƴ�userMap
			if(res.getMsgNum().equals(MsgKey.LOGIN) 
					&& res.getWords().equals("false")) {
				String kv = res.getReceiverIP() + ":" + res.getReceiverPort();
				log.info("��usermap�Ƴ�");
				UserMap.remove(kv);
				String kv2 = res.getReceiverIP()+":"+res.getFilePort();
				log.info("��FileSocketMap���Ƴ�");
				Server.fileSocketMap.remove(kv2);
			}
			String s = res.getResult();
			String output = JsonUtil.buildJson("res", s);
	log.info("��ʼд��" + output);
			int length = channel.write(ByteBuffer.wrap(output.getBytes()));
	log.info("д����� д������ " + length + "  ȡ��д�¼�");
			key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
		}

	}

	/**
	 * �����Ӧ����Ӧ����
	 * 
	 * @param res
	 * @throws ClosedChannelException 
	 */
	public void addResponse(Message res) {
//		SocketChannel channel = UserMap.get(res.getReceiverIP() + ":" + res.getReceiverPort());
		responseMessage.offer(res);
//		log.info(res + " ��ӵ���Ӧ����");
//		if(this.responseMessage.size() == 1) {
//			processResponse();
//		}
		
//log.info("ȡ��channel " + channel);
		responseThread.writeSelector.wakeup();
//		try {
//			channel.register(responseThread.writeSelector, SelectionKey.OP_WRITE,res);
//		} catch (ClosedChannelException e) {
//			e.printStackTrace();
//		}
//log.info("ע����д�¼�");	
//		log.info(res + " ��ӵ����ض���  ��ע����д�¼�");
	}

}
