package com.client.startup;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.client.handler.ResDisplayFriendsHandler;
import com.client.handler.ResLoginHandler;
import com.client.handler.ResPrivateChatHandler;
import com.client.handler.ResReceiveFileHandler;
import com.client.handler.ResRegistHandler;
import com.client.handler.ResSendFileHandler;
import com.client.handler.AbstractResponseHandler;
import com.client.handler.ResAddFriendHandler;
import com.client.response.ResponseCollectionManager;
import com.client.response.ResponseCollectionManagerImpl;
import com.util.MsgKey;

public class Client {
	
	public static Logger log = Logger.getLogger(Client.class);
	
	ResponseCollectionManager resCollec;
	Socket socket ;
	Socket fileSocket ;
	HashMap<String, AbstractResponseHandler> responseMap;

	public Client() {
	}

	// 主要负责两条线程的依赖注入
	public void runClient() {
		try {
			this.socket = new Socket("localhost", 8888);
			log.info("连接到通信服务器，进行通信..." + socket.getLocalAddress().getHostAddress() + " " + socket.getLocalPort());
			 this.fileSocket = new Socket("localhost", 8889);
			log.info("连接到文件传输服务器，准备传输文件。。。");
			
			// 回复的消息集合
			this.resCollec = new ResponseCollectionManagerImpl();

			// 策略map 
			responseMap = new HashMap<String, AbstractResponseHandler>();
// handler 策略模式
			AbstractResponseHandler resRegistHandler = new ResRegistHandler(socket);
			AbstractResponseHandler resLoginHandler = new ResLoginHandler(socket);
			AbstractResponseHandler resDisplayFriendsHandler = new ResDisplayFriendsHandler(socket);
			AbstractResponseHandler resPrivateChatHandler = new ResPrivateChatHandler(socket);
			AbstractResponseHandler resAddFriendHandler = new ResAddFriendHandler(socket);
			AbstractResponseHandler resSendFileHandler = new ResSendFileHandler(socket, fileSocket);
			AbstractResponseHandler resReceiveFileHandler = new ResReceiveFileHandler(socket, fileSocket);
			
			responseMap.put(MsgKey.REGISTER, resRegistHandler);
			responseMap.put(MsgKey.LOGIN, resLoginHandler);
			responseMap.put(MsgKey.DISPLAY_FRIEND, resDisplayFriendsHandler);
			responseMap.put(MsgKey.PRIVATE_CHAT, resPrivateChatHandler);
			responseMap.put(MsgKey.ADD_FRIEND, resAddFriendHandler);
			responseMap.put("8", resSendFileHandler);
			responseMap.put("9", resReceiveFileHandler);
			

			// 启动clientManager
			log.info("启动ClientManager.." );
			ClientManager manager = new ClientManager(this);
			manager.runManager();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {

		Client client = new Client();
		client.runClient();

	}
}
