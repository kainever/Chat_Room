package com.client.handler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JOptionPane;

import net.sf.json.JSONObject;

import com.client.ui.ChatBox;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.builder.BuilderDirector;
import com.util.builder.PrintWriterBuilder;

public class ResReceiveFileHandler extends AbstractResponseHandler {

	public ResReceiveFileHandler(Socket socket, Socket fileSocket) {
		super(socket);
		this.fileSocket = fileSocket;
	}

	private Socket fileSocket;
	private String filePath;
	private String originalFileName;
	private long fileLength;

	private String bool;

	
	@Override
	public void handleResponse() {
		System.out.println("�յ��ļ�");

		JSONObject json = (JSONObject) JsonUtil.parseJson(super.responseMsg,
				"res");
		String friendName = json.getString("publisher");
		String filePart = json.getString("content");

		// �����ļ������ļ���С
		JSONObject json1 = (JSONObject) JsonUtil.parseJson(filePart,
				"filePart");
		filePath = json1.getString("serverFileName"); // �������ļ���
		String length = json1.getString("length");
		fileLength = Long.parseLong(length);
		originalFileName = json1.getString("fileName");

//		ChatBox c = ChatBox.getInstance(socket, ResAddFriendHandler.getRealName(),
//				friendName);
//		c.setTitle("File Chat Box");

		/* �������������Ϣ���Ƿ�����ļ� */
		int n = JOptionPane.showConfirmDialog(null, "�û� " + friendName + " �����ļ� "
				+ originalFileName + "���Ƿ�����ļ���", "ȷ�Ͻ����ļ�",
				JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			bool = "true";
		} else if (n == JOptionPane.NO_OPTION) {
			bool = "false";
		}

		System.out.println("�Ƿ�����ļ���" + bool);

		BuilderDirector builderDirector = new BuilderDirector(new PrintWriterBuilder(socket));
		try {
			PrintWriter writer = (PrintWriter) builderDirector.construct();

			// ���ļ������ļ���С�����content
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("serverFileName", filePath); // �������ļ���
			map.put("bool", bool);
			JSONObject filePart1 = JSONObject.fromObject(map);
			String filePartJson = JsonUtil.buildJson("filePart", filePart1);

			// ���ַ����������Ҫ�ͻ��˽�������ʽ
			Message trans = new Message();
//			trans.setPublisher(ResAddFriendHandler.getRealName());
			trans.setMsgNum("9");
			trans.setReceiver(friendName);
			trans.setWords(filePartJson);
			String result = trans.getResult();

			// ��ӿͻ��˵İ�ͷ
			String output = JsonUtil.buildJson("msg", result);
			writer.println(output);

		} catch (IOException e) {
			e.printStackTrace();
		}

		ReceiveFileThread thread = new ReceiveFileThread();
		thread.setDaemon(true);
		thread.start();
	}

	public class ReceiveFileThread extends Thread {
		public void run() {
			if (bool.equals("true")) {
				try {
					byte[] buffer = new byte[1024];

					DataInputStream din = new DataInputStream(
							new BufferedInputStream(fileSocket.getInputStream()));

					// ����Ҫ������ļ�
					File f = new File("downloadFiles//" + originalFileName);
					FileOutputStream fos = new FileOutputStream(f);

					System.out.println("׼�������ļ�");

					// int i = 1;
					int length = 0;
					int sum = 0;
					while ((length = din.read(buffer)) != -1) {
						sum += length;
						fos.write(buffer, 0, length);
						// System.out.println(i++ + " times��" + length);
						fos.flush();
						if (sum == fileLength)
							break;
					}

					System.out.println("�����ļ����");
					fos.close();

					JOptionPane.showMessageDialog(null, "�ļ�������ϣ�");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
