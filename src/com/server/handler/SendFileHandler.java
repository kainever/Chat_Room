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
import com.server.user.User;
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
				log.info("���������" + this.getName() + "�����ļ�");
				byte[] buffer = new byte[1024];
				DataInputStream dis = new DataInputStream(
						new BufferedInputStream(fileSocket.getInputStream()));

				// ���ļ���Ψһ��
				fileName = getDateName()+"." +getFileType(filePath);
				
				File upload = new File("uploadFiles");
				if(!upload.exists())
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
			sendFileMsg(name, friName);
		}

		private String getDateName() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
			Date d = new Date();
			return sdf.format(d);
		}
	}

	/**
	 * ֪ͨ�����߽����ļ�
	 * @param publisher
	 * @param friendName
	 */
	private void sendFileMsg(String publisher, String friendName) {
		
		
		try {
			User friend = userService.getUserByName(friendName);
	log.info(Server.fileSocketMap + "  " + friend.getIp()+":"+friend.getFilePort());
			this.friSocket = Server.fileSocketMap.get(friend.getIp()+":"+friend.getFilePort());
	log.info("ȡ��"+friend.getName()+"��fileSocket " + friSocket);

			// ���ļ������ļ���С�����content
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("serverFileName", fileName); //�������ļ���
			String realFileName = getFileName(filePath); //�õ������ļ�·�����ļ���
			map.put("fileName", realFileName);
			map.put("length", fileLength);
			JSONObject filePart1 = JSONObject.fromObject(map);
			String filePartJson = JsonUtil.buildJson("filePart", filePart1);

			// ���ַ����������Ҫ�ͻ��˽�������ʽ
			Message res = new Message();
			res.setReceiverIP(friend.getIp());
			res.setReceiverPort(friend.getPort());
			res.setMsgNum(MsgKey.RCV_FILE);
			res.setPublisher(publisher);
			res.setWords(filePartJson);
			
	log.info(this.getClass().getName() + " ��Ӧ���շ� = "  + res + " ��ӵ����ض���");
			handler.addResponse(res);
			
//			Message res2 = new Message();
//			res2.setPublisher(publisher);
//			res2.setReceiverIP(friend.getIp());
//			res2.setReceiverPort(friend.getPort());
//			res2.setMsgNum(MsgKey.PRIVATE_CHAT);
//			res2.setWords(publisher + "�������ļ���" + realFileName);
//			
//			log.info(this.getClass().getName() + " ���ؽ�� = "  + res2 + " ��ӵ����ض���");
//			handler.addResponse(res2);

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
