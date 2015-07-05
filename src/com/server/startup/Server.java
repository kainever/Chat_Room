package com.server.startup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.server.handler.AbstractRequestHandler;
import com.server.handler.AddFriendHandler;
import com.server.handler.DisplayFriendsHandler;
import com.server.handler.LoginHandler;
import com.server.handler.LogoutHandler;
import com.server.handler.PrivateChatHandler;
import com.server.handler.ReceiveFileHandler;
import com.server.handler.RegistHandler;
import com.server.handler.SendFileHandler;
import com.server.log.LogInvoHandler;
//import Server.UserCollection.UserDB;
import com.server.request.MsgManager;
import com.server.request.MsgManagerImpl;
import com.server.user.UserMap;
import com.util.MsgKey;

public class Server {
	public static Logger log = Logger.getLogger(Server.class);
	
//	private ServerSocket chatserver; // ͨ����
	private ServerSocket fileServer; // �����ļ���
	UserMap userMap;
	MsgManager msgManager;
	Map<String, AbstractRequestHandler> requestMap;
	boolean fileServerRun;
	public static Map<String , Socket> fileSocketMap = new ConcurrentHashMap<String , Socket> ();
	 
//	private UserList users;
	
	class AcceptThread extends Thread {
		@Override
		public void run() {
			log.info("fileServer�����߳�����....");
			while(fileServerRun) {
				try {
					Socket s = fileServer.accept();
					String key = s.getRemoteSocketAddress().toString().trim().substring(1);
					log.info("fileServer ���յ�socket " + s + " " + key);
					fileSocketMap.put(key, s);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Server() throws IOException {

		fileServer = new ServerSocket(8889);
		this.fileServerRun = true;
		new AcceptThread().start();
//		fileServer.accept();
		
		log.info("�ļ������������������ʼ����.....");
	}

	public void runServer() throws IOException {

		// ʹ����־������¼MsgManager�����Ϊ ��������Spring AOP ��ʹ�ã�
		msgManager = LogInvoHandler
				.getProxyInstance(MsgManagerImpl.class);
		log.info("msgManager-��Ϣ������������� ...");
		
//		MsgManager msgController = new MsgManager();

		requestMap = new HashMap<String, AbstractRequestHandler>();
//
		AbstractRequestHandler listFriendsHandler = new DisplayFriendsHandler();
		AbstractRequestHandler privateChatHandler = new PrivateChatHandler();
//		AbstractRequestHandler groupChatHandler = new GroupChatHandler(users);
		AbstractRequestHandler logoutHandler = new LogoutHandler();
		AbstractRequestHandler registHandler = new RegistHandler();
		AbstractRequestHandler loginHandler = new LoginHandler();
		AbstractRequestHandler sendFileHandler = new SendFileHandler();
		AbstractRequestHandler receiveFileHandler = new ReceiveFileHandler();
		AbstractRequestHandler addFriendHandler = new AddFriendHandler();

		requestMap.put(MsgKey.DISPLAY_FRIEND, listFriendsHandler);
		requestMap.put(MsgKey.PRIVATE_CHAT, privateChatHandler);
//		requestMap.put("3", groupChatHandler);
		requestMap.put(MsgKey.LOGOUT, logoutHandler);
		requestMap.put(MsgKey.REGISTER, registHandler);
		requestMap.put(MsgKey.LOGIN, loginHandler);
		requestMap.put(MsgKey.SEND_FILE, sendFileHandler);
		requestMap.put(MsgKey.RCV_FILE, receiveFileHandler);
		requestMap.put(MsgKey.ADD_FRIEND, addFriendHandler);
		log.info("���������߳�....");
		ListenerThread listener = new ListenerThread("127.0.0.1" , 8888 , this);
		listener.start();

		// ��Ϣ�����߳�
		HandlerThread manager = new HandlerThread(this);
		manager.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("��������������..");
		

	}

//	public void closeServer() {
//		try {
//			chatserver.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	
}
