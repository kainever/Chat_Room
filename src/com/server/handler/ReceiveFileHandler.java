package com.server.handler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.transmit.protocol.Message;

import net.sf.json.JSONObject;

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
		
	}

	/*@Override
	public void handleRequest(Message msg) {
		String name = msg.getPublisher();// ��������Ϣ�ķ�����
		String friendName = msg.getReceiver();
		String filePart = msg.getWords();

		String linkName = users.getOnlineUsers().get(name);
		Socket socket = users.getSocket(linkName);
		fileSocket = users.getFileSocketsMap().get(linkName);

		// �����ļ������ļ���С
		JSONObject json1 = (JSONObject) JsonTrans.parseJson(filePart,
				"filePart");
		filePath = json1.getString("serverFileName"); // �������ļ���
		// String length = json1.getString("length");
		// fileLength = Long.parseLong(length);
		bool = json1.getString("bool"); // �Ƿ�����ļ�

		System.out.println(name + " �Ƿ�����ļ���" + bool);

		 ������߶Ի����л�����Ϣ 
		Director director = new Director(new PrintWriterBuilder(socket));

		// ʹ��ָ��������writer
		PrintWriter writer = null;
		try {
			writer = (PrintWriter) director.construct();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ���ַ����������Ҫ�ͻ��˽�������ʽ
		ResTrans trans = new ResTrans();
		trans.setPublisher(friendName);
		trans.setMsgNum("2");
		trans.setContent(name + " �Ƿ�����ļ���" + bool);
		String result = trans.getResult();

		// ��ӷ������İ�ͷ
		String output = JsonTrans.buildJson("res", result);
		writer.println(output);

		SendFileThread thread = new SendFileThread();
		thread.setDaemon(true);
		thread.start();
	}

	public class SendFileThread extends Thread {
		public void run() {
			if (bool.equals("true")) {
				try {
					byte[] buffer = new byte[1024]; // ÿ�ζ�ȡ1024�ֽڣ�1Kb��
					File f = new File("uploadFiles//" + filePath);

					// ���������
					DataOutputStream dout = new DataOutputStream(
							new BufferedOutputStream(
									fileSocket.getOutputStream()));
					// �ļ�������
					FileInputStream fin = new FileInputStream(f);

					System.out.println("��ʼ�����ļ�");

					// int i = 1;
					int length = 0;
					while ((length = fin.read(buffer)) != -1) {
						dout.write(buffer, 0, length);
						// System.out.println(i++ + " �Σ�" + length);
						dout.flush();
					}
					// System.out.println(i++ + " �Σ�" + length);

					System.out.println("�ļ��������");

					// �ر���
					fin.close();
					// dout.close();//��ر�socket

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

*/
}
