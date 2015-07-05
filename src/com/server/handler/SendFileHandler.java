package com.server.handler;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import com.transmit.protocol.Message;


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

//	@Override
//	public void handleRequest() {
//		JSONObject json = (JSONObject) JsonTrans.parseJson(super.requestMsg,
//				"msg");
//		String name = json.getString("publisher");// ��������Ϣ�ķ�����
//		String friendName = json.getString("receiver");
//		String filePart = json.getString("words");
//
//		// �����ļ������ļ���С
//		JSONObject json1 = (JSONObject) JsonTrans.parseJson(filePart,
//				"filePart");
//		filePath = json1.getString("fileName");
//		String length = json1.getString("length");
//		fileLength = Long.parseLong(length);
//
//		// user��������
//		String linkName = users.getOnlineUsers().get(name);
//		fileSocket = users.getFileSocketsMap().get(linkName);
//
//		// friend��������
//		String linkFriName = users.getOnlineUsers().get(friendName);
//		friSocket = users.getSocketsMap().get(linkFriName);
//		// friend���ļ�������
//		//fileFriSocket = users.getFileSocketsMap().get(linkFriName);
//
//		System.out.println(name + " �� " + friendName + " �����ļ�");
//
//		try {
//			/* �ظ������ߣ�֪ͨ�����߿�ʼ�����ļ� */
//			Director director = new Director(new PrintWriterBuilder(
//					users.getSocket(linkName)));
//
//			// ʹ��ָ��������writer
//			PrintWriter writer = (PrintWriter) director.construct();
//
//			// ���ַ����������Ҫ�ͻ��˽�������ʽ
//			ResTrans trans = new ResTrans();
//			trans.setMsgNum("8");
//			trans.setContent(filePath);
//			String result = trans.getResult();
//
//			// ��ӷ������İ�ͷ
//			String output = JsonTrans.buildJson("res", result);
//			writer.println(output);
//
//			System.out.println("��Ӧ���ͷ� " + name + " ����ϢΪ��" + output);
//
//			ReceiveFileThread thread = new ReceiveFileThread(name, friendName);
//			thread.setDaemon(true);
//			thread.start();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public class ReceiveFileThread extends Thread {
//		private String name;
//		private String friName;
//
//		public ReceiveFileThread(String name, String friName) {
//			this.name = name;
//			this.friName = friName;
//		}
//
//		public void run() {
//			try {
//				byte[] buffer = new byte[1024];
//				DataInputStream din = new DataInputStream(
//						new BufferedInputStream(fileSocket.getInputStream()));
//
//				// ���ļ���Ψһ��
//				fileName = ClientNameGenerator.gen() + "."
//						+ getFileType(filePath);
//
//				// ����Ҫ������ļ�
//				File f = new File("uploadFiles//" + fileName);
//				// RandomAccessFile fw = new RandomAccessFile(f, "rw");
//				FileOutputStream fos = new FileOutputStream(f);
//
//				System.out.println("׼�������ļ�");
//
//				// int i = 1;
//				int length = 0;
//				int sum = 0;
//				while ((length = din.read(buffer)) != -1) {
//					sum += length;
//					fos.write(buffer, 0, length);
//					// System.out.println(i++ + " times��" + length);
//					fos.flush();
//					if (sum == fileLength)
//						break;
//				}
//				// System.out.println(i++ + " times��" + length);
//
//				System.out.println("�����ļ����");
//				fos.close();
//				// fw.close();
//				// din.close();//��ر�socket
//
//			} catch (Exception e) {
//				System.out.println("������");
//				e.printStackTrace();
//			}
//
//			sendFileMsg(name, friName);
//		}
//	}
//
//	private void sendFileMsg(String publisher, String friendName) {
//		try {
//			/* ֪ͨ�����߽����ļ� */
//			Director director = new Director(new PrintWriterBuilder(friSocket));
//			PrintWriter writer = (PrintWriter) director.construct();
//
//			// ���ļ������ļ���С�����content
//			HashMap<String, Object> map = new HashMap<String, Object>();
//			map.put("serverFileName", fileName); //�������ļ���
//			String realFileName = getFileName(filePath); //�õ������ļ�·�����ļ���
//			map.put("fileName", realFileName);
//			map.put("length", fileLength);
//			JSONObject filePart1 = JSONObject.fromObject(map);
//			String filePartJson = JsonTrans.buildJson("filePart", filePart1);
//
//			// ���ַ����������Ҫ�ͻ��˽�������ʽ
//			ResTrans trans = new ResTrans();
//			trans.setPublisher(publisher);
//			trans.setMsgNum("9");
//			trans.setContent(filePartJson);
//			String result = trans.getResult();
//
//			// ��ӷ������İ�ͷ
//			String output = JsonTrans.buildJson("res", result);
//			writer.println(output);
//
//			System.out.println("��Ӧ���շ� " + friendName + "����ϢΪ��" + output);
//
//			/* �ڽ����ߵ����촰������ʾ�������ļ����ˡ� */
//			trans = new ResTrans();
//			trans.setPublisher(publisher);
//			trans.setMsgNum("2");
//			trans.setContent(publisher + "�������ļ���" + realFileName);
//
//			result = trans.getResult();
//
//			// ��ӷ������İ�ͷ
//			output = JsonTrans.buildJson("res", result);
//
//			writer.println(output);
//			writer.flush();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	// ��ȡ�ļ���׺��
//	private String getFileType(String originalFileName) {
//		String fileType = originalFileName.substring(originalFileName
//				.lastIndexOf(".") + 1);
//		return fileType;
//	}
//
//	private String getFileName(String fileNameWithFolder) {
//		String fileName = fileNameWithFolder.substring(fileNameWithFolder
//				.lastIndexOf("/") + 1);
//		return fileName;
//	}

	@Override
	public void handleRequest(Message msg) {
		// TODO Auto-generated method stub
		
	}
}
