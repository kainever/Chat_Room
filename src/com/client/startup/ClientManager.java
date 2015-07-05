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
 * �ͻ��˹�����,��ά����һ��map,����洢�Ÿ����¼����Ӧ�Ĵ������
 * һ����Ϣ����manager
 * ͬʱ , ����������һ���̲߳��ϵĴ���Ϣ������ȥȡֵ������������ͷ��Ϣ
 * �е�msgNum ��response�����map���ض���handler����	
 * 
 * ����һ�����߳�, ����Ӧ������Ϣ����
 * @author slave_1
 */
public class ClientManager {
	
	public static Logger log = Logger.getLogger(ClientManager.class);
	Client client;
	// �ͻ�����Ϣ����

	ClientManager(Client c) {
		this.client = c;
	}
	
	/**
	 * new ����¼���� ͬʱ���������߳�
	 */
	public void runManager() {
		Socket socket = client.socket;
		HashMap<String, AbstractResponseHandler> responseMap = client.responseMap;
		// ������ҳ���ڣ�����Ϊ���ɼ�
		HomePage homePage = null;
		try {
			homePage = HomePage.getInstance(client.socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		homePage.setVisible(false);
		homePage.setResponseHandler((ResDisplayFriendsHandler) client.responseMap
				.get("1"));
		log.info("����homePage");
		// homePage.paintWaitAction();

		// ������¼ҳ
		LoginPage loginPage = LoginPage.getInstance(socket);
		loginPage.paintWaitAction();
		loginPage.setHomePage(homePage);
		loginPage.jtfName.requestFocus(); // ���뽹��
		loginPage.setResponseHandler((ResLoginHandler) responseMap.get("6"));
		loginPage.setRegistHandler((ResRegistHandler) responseMap.get("5")); // ��ע��ҳ��ע��handler
		log.info("��¼���洴�����..");
//	���߳�	
		ReaderThread reader = new ReaderThread();
		reader.setDaemon(true);
		reader.start();
//		�����߳�
		ResParseThread thread = new ResParseThread();
		thread.setDaemon(true); // ����Ϊ�ػ�����
		thread.start();
	}

	
	/**
	 * ��Ӧ�����߳� 
	 * 1. ����Ӧ������ȡ��msg
	 * 2. �����õ�msgNum --- handler
	 * 3. msg ���� handler.responseMsg
	 * 4. handler.handler();
	 * @author slave_1
	 */
	public class ResParseThread extends Thread {
		public void run() {
			log.info("�����߳�����..");
			ResponseCollectionManager resCollection = client.resCollec;
			HashMap<String, AbstractResponseHandler> responseMap = client.responseMap;
			while (true) {
				String msg = resCollection.getMsg(); // ����ʽ��ȡ
				System.out.println("ReParseThread ��ȡ������Server�Ļ�Ӧ��ϢΪ��" + msg + "��");

				try {
					// ������������ͷ
					JSONObject json = (JSONObject) JsonUtil.parseJson(msg,
							"res");
					String key = json.getString("msgNum");// ����������Ϣ���

					// ����ģʽ
					AbstractResponseHandler handler = (AbstractResponseHandler) responseMap
							.get(key);
					handler.setResponseMsg(msg);
					log.info("�������� " + handler.getClass().getName());
					handler.handleResponse();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * �ͻ��˶��߳� ����Ϣ����msgQueue
	 * @author slave_1
	 */
	public class ReaderThread extends Thread {

		public void run() {
			log.info("���߳�����...");
			ResponseCollectionManager resCollection = client.resCollec;
			// ָ����
			BuilderDirector builderDirector = new BuilderDirector(new BufferedReaderBuilder(client.socket));
			try {
				// ʹ��ָ��������reader
//				BufferedReader reader = (BufferedReader) builderDirector.construct();
				BufferedInputStream reader = new BufferedInputStream(client.socket.getInputStream());

				String receiveMsg = "";
				while (true) {
					StringBuffer sb = new StringBuffer();
					//�������Է���������Ϣ? ��һ���е����ʣ��ܱ�֤ÿ�ΰ����е���Ϣ��������
		log.info("�ȴ���Ӧ..."  + client.socket);
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
		log.info("�ͻ��˶�ȡ������ �ͻ��˶�ȡ��ʽ���Ľ�(��Ӧ�����ݱ���С��1024���ֽ�)..��" + rcv + " ��ӵ���Ϣ����");			
					//������Ϣ����
					resCollection.addMsg(rcv);				
				}
				// reader.close();// �ر����ᵼ��socket�Ĺرգ���
			} catch (Exception e) {
				System.out.print("�������׳��쳣��");
				e.printStackTrace();
			} finally {
				System.out.println("������ִ����ϣ�");
			}
		}

	}
}
