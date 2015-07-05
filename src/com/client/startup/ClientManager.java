package com.client.startup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.client.handler.AbstractResponseHandler;
import com.client.handler.ResDisplayFriendsHandler;
import com.client.handler.ResLoginHandler;
import com.client.handler.ResRegistHandler;
import com.client.response.ResponseCollectionManager;
import com.client.ui.HomePage;
import com.client.ui.LoginPage;
import com.util.JsonUtil;
import com.util.builder.BufferedReaderBuilder;
import com.util.builder.BuilderDirector;

/**
 * 客户端管理者,它维持了一个map,里面存储着各个事件相对应的处理机制
 * 一个消息队列manager
 * 同时 , 它还会启动一个线程不断的从消息队列中去取值，解析，根据头信息
 * 中的msgNum 将response分配给map中特定的handler处理	
 * 
 * 还有一个读线程, 将相应读入消息队列
 * @author slave_1
 */
public class ClientManager {
	
	public static Logger log = Logger.getLogger(ClientManager.class);
	Client client;
	// 客户端消息队列

	ClientManager(Client c) {
		this.client = c;
	}
	
	/**
	 * new 出登录界面 同时启动解析线程
	 */
	public void runManager() {
		Socket socket = client.socket;
		HashMap<String, AbstractResponseHandler> responseMap = client.responseMap;
		// 创建主页窗口，设置为不可见
		HomePage homePage = null;
		try {
			homePage = HomePage.getInstance(client.socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		homePage.setVisible(false);
		homePage.setResponseHandler((ResDisplayFriendsHandler) client.responseMap
				.get("1"));
		log.info("创建homePage");
		// homePage.paintWaitAction();

		// 创建登录页
		LoginPage loginPage = LoginPage.getInstance(socket);
		loginPage.paintWaitAction();
		loginPage.setHomePage(homePage);
		loginPage.jtfName.requestFocus(); // 输入焦点
		loginPage.setResponseHandler((ResLoginHandler) responseMap.get("6"));
		loginPage.setRegistHandler((ResRegistHandler) responseMap.get("5")); // 帮注册页面注入handler
		log.info("登录界面创建完成..");
//	读线程	
		ReaderThread reader = new ReaderThread();
		reader.setDaemon(true);
		reader.start();
//		解析线程
		ResParseThread thread = new ResParseThread();
		thread.setDaemon(true); // 设置为守护进程
		thread.start();
	}

	
	/**
	 * 响应解析线程 
	 * 1. 从响应队列中取得msg
	 * 2. 解析得到msgNum --- handler
	 * 3. msg 放入 handler.responseMsg
	 * 4. handler.handler();
	 * @author slave_1
	 */
	public class ResParseThread extends Thread {
		public void run() {
			log.info("解析线程启动..");
			ResponseCollectionManager resCollection = client.resCollec;
			HashMap<String, AbstractResponseHandler> responseMap = client.responseMap;
			while (true) {
				String msg = resCollection.getMsg(); // 阻塞式的取
				System.out.println("ReParseThread 【取出来自Server的回应消息为：" + msg + "】");

				try {
					// 解析服务器包头
					JSONObject json = (JSONObject) JsonUtil.parseJson(msg,
							"res");
					String key = json.getString("msgNum");// 解析出的消息编号

					// 策略模式
					AbstractResponseHandler handler = (AbstractResponseHandler) responseMap
							.get(key);
					handler.setResponseMsg(msg);
					log.info("任务分配给 " + handler.getClass().getName());
					handler.handleResponse();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * 客户端读线程 将消息读入msgQueue
	 * @author slave_1
	 */
	public class ReaderThread extends Thread {

		public void run() {
			log.info("读线程启动...");
			ResponseCollectionManager resCollection = client.resCollec;
			// 指导者
			BuilderDirector builderDirector = new BuilderDirector(new BufferedReaderBuilder(client.socket));
			try {
				// 使用指导者生成reader
//				BufferedReader reader = (BufferedReader) builderDirector.construct();
				BufferedInputStream reader = new BufferedInputStream(client.socket.getInputStream());

				String receiveMsg = "";
				while (true) {
					StringBuffer sb = new StringBuffer();
					//接收来自服务器的消息? 这一步有点疑问？能保证每次把所有的信息读过来吗？
		log.info("等待响应..."  + client.socket);
					byte[] buffer = new byte[1024];
					int a = 0;
//					receiveMsg = reader.readLine();
//					while (a != -1) {
//						log.info(a);
//						a = reader.read(buffer);
//						sb.append(buffer);
//					}
					reader.read(buffer);
					String rcv = new String(buffer);
//					sb.append(buffer);
//					receiveMsg = sb.toString();
		log.info("客户端读取到数据 客户端读取方式待改进(响应的数据必须小于1024个字节)..：" + rcv + " 添加到消息队列");			
					//存入消息队列
					resCollection.addMsg(rcv);				
				}
				// reader.close();// 关闭流会导致socket的关闭！！
			} catch (Exception e) {
				System.out.print("读进程抛出异常！");
				e.printStackTrace();
			} finally {
				System.out.println("读进程执行完毕！");
			}
		}

	}
}
