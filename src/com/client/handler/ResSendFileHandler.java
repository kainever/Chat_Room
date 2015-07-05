package com.client.handler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import net.sf.json.JSONObject;

import com.util.JsonUtil;

public class ResSendFileHandler extends AbstractResponseHandler {

	public ResSendFileHandler(Socket socket, Socket fileSocket) {
		super(socket);
		this.fileSocket = fileSocket;
	}

	private Socket fileSocket;
	private String filePath;

	@Override
	public void handleResponse() {
		System.out.println("�����ļ�");
		
		// ����loginFlag����
		JSONObject json = (JSONObject) JsonUtil.parseJson(super.responseMsg,
				"res");
		filePath = json.getString("content");

		//System.out.println(fileSocket.getPort());

		SendFileThread thread = new SendFileThread();
		thread.setDaemon(true);
		thread.start();
	}

	public class SendFileThread extends Thread {
		public void run() {
			try {
				byte[] buffer = new byte[1024]; // ÿ�ζ�ȡ1024�ֽڣ�1Kb��

				File f = new File(filePath);

				// ���������
				DataOutputStream dout = new DataOutputStream(
						new BufferedOutputStream(fileSocket.getOutputStream()));
				// �ļ�������
				FileInputStream fin = new FileInputStream(f);

				System.out.println("��ʼ�����ļ�");

				//int i = 1;
				int length = 0;
				while ((length = fin.read(buffer)) != -1) {
					dout.write(buffer, 0, length);
					//System.out.println(i++ + " �Σ�" + length);
					dout.flush();
				}
				//System.out.println(i++ + " �Σ�" + length);
				
				System.out.println("�ļ��������");

				// �ر���
				fin.close();
				//dout.close();//��ر�socket

				JOptionPane.showMessageDialog(null, "�ϴ��ɹ���");

			} catch (Exception ex) {
				System.out.println("������");
				ex.printStackTrace();
			}
		}
	}

}
