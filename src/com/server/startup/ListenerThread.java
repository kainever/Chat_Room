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
 * �����ͻ��˵�����,����������У�Ȼ��ͨ���̳߳� ����reader�̣߳� ����������в��Ҷ�ȡ, ����userMap��ע�ᣡ
 * ����Ϣ������Ϣ���У�handler�߳̾ͻ�Խ��д�������Ϣ���ء�
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
	 * �����̰߳󶨵���Ӧ�ĵ�ַ����ʼ�����߳�
	 * 
	 * @param readThreads
	 *            ���߳���Ŀ
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
		log.info("�������󶨵�" + address.toString());
		// create a selector;
		selector = Selector.open();
		
//		�������߳�
		log.info("�������߳�");
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
		log.info("�����߳�����");
		while (true) {
			SelectionKey key = null;
			try {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
	log.info("��⵽�����¼� , �¼��� = "+ keys.size());
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
	 * �����¼�����, ����������, ����Channelͨ������㷨
	 * �����reader! 
	 * @param key
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void doAccept(SelectionKey key) throws IOException, InterruptedException {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		SocketChannel channel;
		while ((channel = server.accept()) != null) {
			channel.configureBlocking(false);
			log.info("listener��������   " + channel);
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
	 * ��ȡmsg ������Ϣ������Message����
	 * ������Ϣ����
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
		 * ��reader��������� һ��Ҫע��wakeup()������ʹ��
		 * ����ط�ΪʲôҪʹ��wakeup()?
		 * ��ΪҪ֪����reader�߳�������ʱ������������select()�������棬
		 * ����ʱ��ʹ����reader�Ķ������������һ��������Ҳ���ܹ���������Ϊ����������
		 * ���ԣ�����Ҫwakeup��������
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
			log.info(name + "  ����");
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
		 * ͬ������
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
						log.info(channel + "  ע���˶��¼�");
					}
					readSelector.select();
					Set<SelectionKey> keys = readSelector.selectedKeys();
			log.info("��⵽���¼� , �¼��� = "+ keys.size());

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
	 * ���¼� �������� д��userMap
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
	    // ����userMap
	    String mapKey = msgObj.getReceiverIP() + ":" + msgObj.getReceiverPort();
	    if(!UserMap.contains(mapKey)) {
	    	UserMap.put(mapKey , socketChannel);
	    }
	    server.msgManager.addMsg(msgObj);
	    
//	    key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
	}


}
