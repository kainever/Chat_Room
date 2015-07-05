package com.server.startup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.server.user.UserMap;
import com.transmit.protocol.Message;
import com.util.JsonUtil;

/**
 * 监听客户端的连接,加入请求队列，然后通过线程池 启动reader线程， 从请求队列中查找读取, 并想userMap中注册！
 * 将消息加入消息队列！handler线程就会对进行处理并将消息返回。
 * 
 * @author slave_1
 */
class ListenerThread extends Thread {

	public static Logger log = Logger.getLogger(ListenerThread.class);
	// private Socket socket;
	// private Socket fileSocket;
	private Server server;
	ExecutorService exec = Executors.newFixedThreadPool(10);
	private ServerSocketChannel acceptChannel = null;
	Reader[] readers = new Reader[10];
	Selector selector;
	InetSocketAddress address;

	/**
	 * 监听线程绑定到相应的地址，初始话读线程
	 * 
	 * @param readThreads
	 *            读线程数目
	 * @param bindAddress
	 * @param port
	 * @throws IOException
	 */
	public ListenerThread(String bindAddress, int port, Server server)
			throws IOException {
		this.server = server;
		address = new InetSocketAddress(bindAddress, port);
		// Create a new server socket and set to non blocking mode
		acceptChannel = ServerSocketChannel.open();
		acceptChannel.configureBlocking(false);
		acceptChannel.socket().bind(address);
		log.info("服务器绑定到" + address.toString());
		// create a selector;
		selector = Selector.open();
		
//		启动读线程
		log.info("启动读线程");
		 for (int i = 0; i < 10; i++) {
			 Reader reader = new Reader("reader " + i);
			 readers[i] = reader;
			 exec.execute(reader);
		 }

		acceptChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.setName("IPC Server listener on " + port);
		this.setDaemon(true);
	}

	@Override
	public void run() {
		log.info("监听线程启动");
		while (true) {
			SelectionKey key = null;
			try {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
	log.info("检测到连接事件 , 事件数 = "+ keys.size());
				Iterator<SelectionKey> iter = keys.iterator();
				while (iter.hasNext()) {
					key = iter.next();
					iter.remove();
					if (key.isValid()) {
						if (key.isAcceptable())
							try {
								doAccept(key);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
					}
					key = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 接收事件处理, 接收连连接, 并将Channel通过随机算法
	 * 分配给reader! 
	 * @param key
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void doAccept(SelectionKey key) throws IOException, InterruptedException {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		SocketChannel channel;
		while ((channel = server.accept()) != null) {
			channel.configureBlocking(false);
			log.info("listener接收连接   " + channel);
			Reader reader = getReader();
			
			reader.addChannel(channel);
			
		}
	}

	private Reader getReader() {
		Random rand = new Random();
		int cur = rand.nextInt(readers.length);
	    return readers[cur];
	}


	
	
	/**
	 * 读取msg 并将消息解析成Message对象
	 * 让入消息队列
	 * @author slave_1
	 */
	class Reader implements Runnable {
		
		final private BlockingQueue<SocketChannel> pendingConnections;
		private final Selector readSelector;
		boolean isRunning;
		String name;
		
		Reader(String name) throws IOException {
			this.pendingConnections = new LinkedBlockingQueue<SocketChannel>(10);
			this.readSelector = Selector.open();
			this.name = name;
		}
		

		/**
		 * 向reader中添加连接 一点要注意wakeup()方法的使用
		 * 这个地方为什么要使用wakeup()?
		 * 因为要知道当reader线程启动的时候，它会阻塞在select()方法上面，
		 * 而此时即使你向reader的读队列中添加了一个对象，它也不能够看到，因为阻塞在哪里
		 * 所以，就需要wakeup来叫醒他
		 * @param channel
		 * @throws InterruptedException 
		 */
		public void addChannel(SocketChannel channel) throws InterruptedException {
			pendingConnections.put(channel);
			this.readSelector.wakeup();
			log.info(this.name + "  get channnel  pending-Size = " + pendingConnections.size() );
		}

		
		@Override
		public void run() {
			log.info(name + "  启动");
			this.isRunning= true;
			try {
				try {
					doRunLoop();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} finally {
				try {
					readSelector.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		
		/**
		 * 同步方法
		 * @throws InterruptedException
		 */
		private synchronized void doRunLoop() throws InterruptedException {
			while (this.isRunning) {
				SelectionKey key = null;
				try {
					// consume as many connections as currently queued to avoid
					// unbridled acceptance of connections that starves the
					// select
					int size = pendingConnections.size();
					for (int i = size; i > 0; i--) {
						SocketChannel channel = pendingConnections.take();
						channel.register(readSelector,
								SelectionKey.OP_READ, channel);
						log.info(channel + "  注册了读事件");
					}
					readSelector.select();
					Set<SelectionKey> keys = readSelector.selectedKeys();
			log.info("检测到读事件 , 事件数 = "+ keys.size());

					Iterator<SelectionKey> iter = keys.iterator();
					while (iter.hasNext()) {
						key = iter.next();
						iter.remove();
						if (key.isValid()) {
							if (key.isReadable()) {
								doRead(key);
							}
						}
						key = null;
					}
				} catch (IOException e) {
//					this.isRunning =false;
					e.printStackTrace();
				}
			}
		}

	}
	
	
	/**
	 * 读事件 解析请求 写入userMap
	 * @param key
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void doRead(SelectionKey key){
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			socketChannel.configureBlocking(false);
			socketChannel.read(buffer);
		} catch (IOException e) {
//			e.printStackTrace();
			try {
				socketChannel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	    
	    byte[] bytes = buffer.array();
	    String msg = new String(bytes);
log.info("get message = " + msg);
		if(msg == null || msg.trim().isEmpty()) return ;
		JSONObject json = (JSONObject) JsonUtil.parseJson(msg, "msg");
	    Message msgObj = Message.build(json);
log.info("msgObj " + msgObj);
	    // 插入userMap
	    String mapKey = msgObj.getReceiverIP() + ":" + msgObj.getReceiverPort();
	    if(!UserMap.contains(mapKey)) {
	    	UserMap.put(mapKey , socketChannel);
	    }
	    server.msgManager.addMsg(msgObj);
	    
//	    key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
	}


}
