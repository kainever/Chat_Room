package com.server.handler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONObject;

import com.server.startup.Server;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;
import com.util.builder.PrintWriterBuilder;


public class SendFileHandler extends AbstractRequestHandler {


	public SendFileHandler() throws IOException {
		super();
	}

	private Socket fileSocket;
	private Socket friSocket;
	//private Socket fileFriSocket;
	private String filePath; // ԭ�ļ���
	private long fileLength;
	private String fileName; // ���ڷ������ϵ��ļ���

	@Override
	public void handleRequest(Message msg) {
		
		String name = msg.getPublisher();// ��������Ϣ�ķ�����
		String friendName = msg.getReceiver();
		Object filePart =  msg.getWords();
		String selfIp = msg.getReceiverIP();
		String selfPort = msg.getReceiverPort();
		String selfFilePort = msg.getFilePort();

		// �����ļ������ļ���С
		JSONObject json1 = (JSONObject) JsonUtil.parseJson(filePart.toString(),
				"filePart");
		filePath = json1.getString("fileName");
		String length = json1.getString("length");
		fileLength = Long.parseLong(length);

		// �ȵ��û��ϴ��û���fileSocket
log.info("fileSocketMap == " + Server.fileSocketMap + "  " + selfIp+":"+selfFilePort);
		fileSocket = Server.fileSocketMap.get(selfIp+":"+selfFilePort);
log.info("�õ� " + name + " fileSocket " + fileSocket);
		// friend��������
//		String linkFriName = users.getOnlineUsers().get(friendName);
//		friSocket = users.getSocketsMap().get(linkFriName);
		// friend���ļ�������
		//fileFriSocket = users.getFileSocketsMap().get(linkFriName);

		log.info(name + " �� " + friendName + " �����ļ�");

		Message res = new Message();
		res.setMsgNum(MsgKey.SEND_FILE);
		res.setWords(filePath);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		log.info(this.getClass().getName() + " ���ؽ�� = "  + res + " ��ӵ����ض���");
		handler.addResponse(res);
//���������߳� �ȴ��ͻ���д����Ϣ ����һֱ����...	
		ReceiveFileThread thread = new ReceiveFileThread(name, friendName);
		thread.setDaemon(true);
		thread.start();

	}

	public class ReceiveFileThread extends Thread {
		private String name;
		private String friName;

		public ReceiveFileThread(String name, String friName) {
			this.name = name;
			this.friName = friName;
		}

		public void run() {
			try {
				byte[] buffer = new byte[1024];
				DataInputStream dis = new DataInputStream(
						new BufferedInputStream(fileSocket.getInputStream()));

				// ���ļ���Ψһ��
				fileName = getDateName()+"." +getFileType(filePath);
				
				File upload = new File("uploadFiles");
				upload.mkdir();

				// ����Ҫ������ļ�
				File f = new File("uploadFiles//" + fileName);
log.info("�ļ���Ψһ�� �� "+fileName  + " ����·�� " + f.getAbsolutePath());
				// RandomAccessFile fw = new RandomAccessFile(f, "rw");
				if(!f.exists()) f.createNewFile();
				FileOutputStream fos = new FileOutputStream(f);

				log.info("׼�������ļ�");

				// int i = 1;
				int length = 0;
				int sum = 0;
				while ((length = dis.read(buffer)) != -1) {
					sum += length;
					fos.write(buffer, 0, length);
					// System.out.println(i++ + " times��" + length);
					fos.flush();
					if (sum == fileLength)
						break;
				}
				// System.out.println(i++ + " times��" + length);

	log.info("����˽����ļ����");
				fos.close();
				// fw.close();
				// din.close();//��ر�socket

			} catch (Exception e) {
				System.out.println("������");
				e.printStackTrace();
			}
log.info("����֪ͨ��������...." + friName);
//			Thread.sleep();
//			sendFileMsg(name, friName);
		}

		private String getDateName() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
			Date d = new Date();
			return sdf.format(d);
		}
	}

	private void sendFileMsg(String publisher, String friendName) {
		
		
		try {
			/* ֪ͨ�����߽����ļ� */
			Director director = new Director(new PrintWriterBuilder(friSocket));
			PrintWriter writer = (PrintWriter) director.construct();

			// ���ļ������ļ���С�����content
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("serverFileName", fileName); //�������ļ���
			String realFileName = getFileName(filePath); //�õ������ļ�·�����ļ���
			map.put("fileName", realFileName);
			map.put("length", fileLength);
			JSONObject filePart1 = JSONObject.fromObject(map);
			String filePartJson = JsonTrans.buildJson("filePart", filePart1);

			// ���ַ����������Ҫ�ͻ��˽�������ʽ
			ResTrans trans = new ResTrans();
			trans.setPublisher(publisher);
			trans.setMsgNum(MsgKey.RCV_FILE);
			trans.setContent(filePartJson);
			String result = trans.getResult();

			// ��ӷ������İ�ͷ
			String output = JsonTrans.buildJson("res", result);
			writer.println(output);

			System.out.println("��Ӧ���շ� " + friendName + "����ϢΪ��" + output);

			/* �ڽ����ߵ����촰������ʾ�������ļ����ˡ� */
			trans = new ResTrans();
			trans.setPublisher(publisher);
			trans.setMsgNum("2");
			trans.setContent(publisher + "�������ļ���" + realFileName);

			result = trans.getResult();

			// ��ӷ������İ�ͷ
			output = JsonTrans.buildJson("res", result);

			writer.println(output);
			writer.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ��ȡ�ļ���׺��
	private String getFileType(String originalFileName) {
		String fileType = originalFileName.substring(originalFileName
				.lastIndexOf(".") + 1);
		return fileType;
	}

	private String getFileName(String fileNameWithFolder) {
		String fileName = fileNameWithFolder.substring(fileNameWithFolder
				.lastIndexOf("/") + 1);
		return fileName;
	}

}
