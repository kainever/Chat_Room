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
	
//	private ServerSocket chatserver; // 通信用
	private ServerSocket fileServer; // 传输文件用
	UserMap userMap;
	MsgManager msgManager;
	Map<String, AbstractRequestHandler> requestMap;
	boolean fileServerRun;
	public static Map<String , Socket> fileSocketMap = new ConcurrentHashMap<String , Socket> ();
	 
//	private UserList users;
	
	class AcceptThread extends Thread {
		@Override
		public void run() {
			log.info("fileServer接收线程启动....");
			while(fileServerRun) {
				try {
					Socket s = fileServer.accept();
					String key = s.getRemoteSocketAddress().toString().trim().substring(1);
					log.info("fileServer 接收到socket " + s + " " + key);
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
		
		log.info("文件传输服务器启动，开始监听.....");
	}

	public void runServer() throws IOException {

		// 使用日志代理，记录MsgManager类的行为 （类似于Spring AOP 的使用）
		msgManager = LogInvoHandler
				.getProxyInstance(MsgManagerImpl.class);
		log.info("msgManager-消息管理代理类生成 ...");
		
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
		log.info("启动监听线程....");
		ListenerThread listener = new ListenerThread("127.0.0.1" , 8888 , this);
		listener.start();

		// 消息处理线程
		HandlerThread manager = new HandlerThread(this);
		manager.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("服务器启动就绪..");
		

	}

//	public void closeServer() {
//		try {
//			chatserver.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	
}
