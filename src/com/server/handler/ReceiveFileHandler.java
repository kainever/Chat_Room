package com.server.handler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import net.sf.json.JSONObject;

import com.server.startup.Server;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;
import com.util.builder.PrintWriterBuilder;

public class ReceiveFileHandler extends AbstractRequestHandler {


	public ReceiveFileHandler() throws IOException {
		super();
	}
	private String filePath; // �������ϵ��ļ���
	// private long fileLength; //���ͷ�����Ҫ֪���ļ���С
	private String bool;
	private Socket fileSocket;

	@Override
	public void handleRequest(Message msg) {
		String name = msg.getPublisher();// ��������Ϣ�ķ�����
		String friendName = msg.getReceiver();
		Object filePart = msg.getWords();
		
		String ip = msg.getReceiverIP();
		String filePort = msg.getFilePort();
		fileSocket = Server.fileSocketMap.get(ip + ":" + filePort);
log.info("get ���շ���fileSocket   " + fileSocket);

//		String linkName = users.getOnlineUsers().get(name);
//		Socket socket = users.getSocket(linkName);
//		fileSocket = users.getFileSocketsMap().get(linkName);

		// �����ļ������ļ���С
		JSONObject json1 = (JSONObject) JsonUtil.parseJson(filePart.toString(),
				"filePart");
		filePath = json1.getString("serverFileName"); // �������ļ���
		// String length = json1.getString("length");
		// fileLength = Long.parseLong(length);
		bool = json1.getString("bool"); // �Ƿ�����ļ�

log.info(name + " �Ƿ�����ļ���" + bool);
log.info("����˷�����Ϣ�����շ�..");
		
		Message res = new Message();
		res.setMsgNum(MsgKey.PRIVATE_CHAT);
		res.setPublisher(friendName);
		res.setWords(name + " �Ƿ�����ļ���" + bool);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		
		handler.addResponse(res);
		log.info(this.getClass().getName() + "������Ϣ���ͻ��� " + res);
		
		SendFileThread thread = new SendFileThread();
		thread.setDaemon(true);
		thread.start();
	}

	public class SendFileThread extends Thread {
		public void run() {
			if (bool.equals("true")) {
				log.info("���������ͨ��java io ���ݽ��ļ�д��������");
				try {
					byte[] buffer = new byte[1024]; // ÿ�ζ�ȡ1024�ֽڣ�1Kb��
					File f = new File("uploadFiles//" + filePath);

					// ���������
					DataOutputStream dout = new DataOutputStream(
							new BufferedOutputStream(
									fileSocket.getOutputStream()));
					// �ļ�������
					FileInputStream fin = new FileInputStream(f);

					log.info("��ʼ�����ļ�");

					// int i = 1;
					int length = 0;
					while ((length = fin.read(buffer)) != -1) {
						dout.write(buffer, 0, length);
						// System.out.println(i++ + " �Σ�" + length);
						dout.flush();
					}
					// System.out.println(i++ + " �Σ�" + length);

					log.info("�ļ��������");

					// �ر���
					fin.close();
					// dout.close();//��ر�socket

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


}
