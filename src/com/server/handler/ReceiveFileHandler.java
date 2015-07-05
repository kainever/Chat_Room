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
	private String filePath; // 服务器上的文件名
	// private long fileLength; //发送方不需要知道文件大小
	private String bool;
	private Socket fileSocket;

	@Override
	public void handleRequest(Message msg) {
		String name = msg.getPublisher();// 解析出消息的发送者
		String friendName = msg.getReceiver();
		Object filePart = msg.getWords();
		
		String ip = msg.getReceiverIP();
		String filePort = msg.getFilePort();
		fileSocket = Server.fileSocketMap.get(ip + ":" + filePort);
log.info("get 接收方的fileSocket   " + fileSocket);

//		String linkName = users.getOnlineUsers().get(name);
//		Socket socket = users.getSocket(linkName);
//		fileSocket = users.getFileSocketsMap().get(linkName);

		// 解析文件名和文件大小
		JSONObject json1 = (JSONObject) JsonUtil.parseJson(filePart.toString(),
				"filePart");
		filePath = json1.getString("serverFileName"); // 服务器文件名
		// String length = json1.getString("length");
		// fileLength = Long.parseLong(length);
		bool = json1.getString("bool"); // 是否接收文件

log.info(name + " 是否接收文件：" + bool);
log.info("服务端反馈消息给接收方..");
		
		Message res = new Message();
		res.setMsgNum(MsgKey.PRIVATE_CHAT);
		res.setPublisher(friendName);
		res.setWords(name + " 是否接收文件：" + bool);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		
		handler.addResponse(res);
		log.info(this.getClass().getName() + "反馈消息给客户端 " + res);
		
		SendFileThread thread = new SendFileThread();
		thread.setDaemon(true);
		thread.start();
	}

	public class SendFileThread extends Thread {
		public void run() {
			if (bool.equals("true")) {
				log.info("服务端正在通过java io 传递将文件写入网络流");
				try {
					byte[] buffer = new byte[1024]; // 每次读取1024字节（1Kb）
					File f = new File("uploadFiles//" + filePath);

					// 数据输出流
					DataOutputStream dout = new DataOutputStream(
							new BufferedOutputStream(
									fileSocket.getOutputStream()));
					// 文件读入流
					FileInputStream fin = new FileInputStream(f);

					log.info("开始传输文件");

					// int i = 1;
					int length = 0;
					while ((length = fin.read(buffer)) != -1) {
						dout.write(buffer, 0, length);
						// System.out.println(i++ + " 次：" + length);
						dout.flush();
					}
					// System.out.println(i++ + " 次：" + length);

					log.info("文件传输完毕");

					// 关闭流
					fin.close();
					// dout.close();//会关闭socket

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


}
