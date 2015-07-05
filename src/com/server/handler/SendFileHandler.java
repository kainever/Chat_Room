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
	private String filePath; // 原文件名
	private long fileLength;
	private String fileName; // 存在服务器上的文件名

	@Override
	public void handleRequest(Message msg) {
		
		String name = msg.getPublisher();// 解析出消息的发送者
		String friendName = msg.getReceiver();
		Object filePart =  msg.getWords();
		String selfIp = msg.getReceiverIP();
		String selfPort = msg.getReceiverPort();
		String selfFilePort = msg.getFilePort();

		// 解析文件名和文件大小
		JSONObject json1 = (JSONObject) JsonUtil.parseJson(filePart.toString(),
				"filePart");
		filePath = json1.getString("fileName");
		String length = json1.getString("length");
		fileLength = Long.parseLong(length);

		// 等到用户上传用户的fileSocket
log.info("fileSocketMap == " + Server.fileSocketMap + "  " + selfIp+":"+selfFilePort);
		fileSocket = Server.fileSocketMap.get(selfIp+":"+selfFilePort);
log.info("得到 " + name + " fileSocket " + fileSocket);
		// friend的连接名
//		String linkFriName = users.getOnlineUsers().get(friendName);
//		friSocket = users.getSocketsMap().get(linkFriName);
		// friend的文件传输名
		//fileFriSocket = users.getFileSocketsMap().get(linkFriName);

		log.info(name + " 向 " + friendName + " 发送文件");

		Message res = new Message();
		res.setMsgNum(MsgKey.SEND_FILE);
		res.setWords(filePath);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		log.info(this.getClass().getName() + " 返回结果 = "  + res + " 添加到返回队列");
		handler.addResponse(res);
//启动接收线程 等待客户端写入信息 否则一直阻塞...	
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

				// 将文件名唯一化
				fileName = getDateName()+"." +getFileType(filePath);
				
				File upload = new File("uploadFiles");
				upload.mkdir();

				// 创建要保存的文件
				File f = new File("uploadFiles//" + fileName);
log.info("文件名唯一化 ： "+fileName  + " 保存路径 " + f.getAbsolutePath());
				// RandomAccessFile fw = new RandomAccessFile(f, "rw");
				if(!f.exists()) f.createNewFile();
				FileOutputStream fos = new FileOutputStream(f);

				log.info("准备接收文件");

				// int i = 1;
				int length = 0;
				int sum = 0;
				while ((length = dis.read(buffer)) != -1) {
					sum += length;
					fos.write(buffer, 0, length);
					// System.out.println(i++ + " times：" + length);
					fos.flush();
					if (sum == fileLength)
						break;
				}
				// System.out.println(i++ + " times：" + length);

	log.info("服务端接收文件完毕");
				fos.close();
				// fw.close();
				// din.close();//会关闭socket

			} catch (Exception e) {
				System.out.println("啦啦啦");
				e.printStackTrace();
			}
log.info("发送通知给接收者...." + friName);
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
			/* 通知接收者接收文件 */
			Director director = new Director(new PrintWriterBuilder(friSocket));
			PrintWriter writer = (PrintWriter) director.construct();

			// 将文件名和文件大小打包成content
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("serverFileName", fileName); //服务器文件名
			String realFileName = getFileName(filePath); //得到不含文件路径的文件名
			map.put("fileName", realFileName);
			map.put("length", fileLength);
			JSONObject filePart1 = JSONObject.fromObject(map);
			String filePartJson = JsonTrans.buildJson("filePart", filePart1);

			// 将字符串打包成需要客户端解析的形式
			ResTrans trans = new ResTrans();
			trans.setPublisher(publisher);
			trans.setMsgNum(MsgKey.RCV_FILE);
			trans.setContent(filePartJson);
			String result = trans.getResult();

			// 添加服务器的包头
			String output = JsonTrans.buildJson("res", result);
			writer.println(output);

			System.out.println("回应接收方 " + friendName + "的消息为：" + output);

			/* 在接收者的聊天窗口中显示出“有文件来了” */
			trans = new ResTrans();
			trans.setPublisher(publisher);
			trans.setMsgNum("2");
			trans.setContent(publisher + "发送了文件：" + realFileName);

			result = trans.getResult();

			// 添加服务器的包头
			output = JsonTrans.buildJson("res", result);

			writer.println(output);
			writer.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取文件后缀名
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
