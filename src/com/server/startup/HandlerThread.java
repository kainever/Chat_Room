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
 * 处理线程， 从消息队列中取值, 解析id 分配给相应的Handler handler是核心业务逻辑进行的地方
 * 
 * @author slave_1
 */
public class HandlerThread extends Thread {
	
	public static Logger log = Logger.getLogger(HandlerThread.class);

	Server server;
	/** 处理器线程池 */
	ExecutorService exec = Executors.newFixedThreadPool(10);
	
	boolean isRunning;

	/**
	 * 响应队列
	 */
	final BlockingQueue<Message> responseMessage;
	
	ResponderThread responseThread;

	public HandlerThread(Server server) throws IOException {
		this.server = server;
		this.responseMessage = new LinkedBlockingQueue<Message>(50);
		// 启动处理线程
		Set<Entry<String, AbstractRequestHandler>> set = server.requestMap
				.entrySet();
		for (Entry<String, AbstractRequestHandler> e : set) {
			// 设置父容器
			e.getValue().setHandler(this);
			exec.execute(e.getValue());
		}
		
		log.info("启动响应线程..");
		responseThread = new ResponderThread();
		responseThread.start();
	}

	public void run() {
		log.info("handler线程启动");
		isRunning = true;
		while (isRunning) {
			Message msg = server.msgManager.getMsg(); // 阻塞式的取
			log.info("【取出来自Client的请求消息为" + msg + "】");

			// JSONObject json = (JSONObject) JsonTrans.parseJson(msg, "msg");
			// String key = json.getString("msgNum");// 解析出的消息编号
			String key = msg.getMsgNum();

			// 策略模式
			AbstractRequestHandler handler = (AbstractRequestHandler) server.requestMap.get(key);
			handler.addMessage(msg);
			// handler.handleRequest();
		}
	}

	/**
	 * 响应线程
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
			log.info("响应线程启动");
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
		log.info("监听到写事件 , 事件数 = " + keys.size());
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
//			移除userMap
			if(res.getMsgNum().equals(MsgKey.LOGIN) 
					&& res.getWords().equals("false")) {
				String kv = res.getReceiverIP() + ":" + res.getReceiverPort();
				log.info("从usermap移除");
				UserMap.remove(kv);
				String kv2 = res.getReceiverIP()+":"+res.getFilePort();
				log.info("从FileSocketMap中移除");
				Server.fileSocketMap.remove(kv2);
			}
			String s = res.getResult();
			String output = JsonUtil.buildJson("res", s);
	log.info("开始写入" + output);
			int length = channel.write(ByteBuffer.wrap(output.getBytes()));
	log.info("写入完成 写入数据 " + length + "  取消写事件");
			key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
		}

	}

	/**
	 * 添加响应到响应队列
	 * 
	 * @param res
	 * @throws ClosedChannelException 
	 */
	public void addResponse(Message res) {
//		SocketChannel channel = UserMap.get(res.getReceiverIP() + ":" + res.getReceiverPort());
		responseMessage.offer(res);
//		log.info(res + " 添加到响应队列");
//		if(this.responseMessage.size() == 1) {
//			processResponse();
//		}
		
//log.info("取得channel " + channel);
		responseThread.writeSelector.wakeup();
//		try {
//			channel.register(responseThread.writeSelector, SelectionKey.OP_WRITE,res);
//		} catch (ClosedChannelException e) {
//			e.printStackTrace();
//		}
//log.info("注册了写事件");	
//		log.info(res + " 添加到返回队列  并注册了写事件");
	}

}
