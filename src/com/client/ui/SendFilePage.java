package com.client.ui;

import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.client.startup.Client;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;
import com.util.builder.BuilderDirector;
import com.util.builder.PrintWriterBuilder;

public class SendFilePage extends JFrame {
	
	public static final Logger log = Logger.getLogger(SendFilePage.class);
	
	private static final long serialVersionUID = 1L;

	private Socket socket;
	private String userName; // 真名
	private String friendName; // 真名

	private JTextField jtfFile;

	// 单件
	private static SendFilePage s_sendFilePage = null;

	public static SendFilePage getInstance(Socket socket, String userName,
			String friendName) {
		if (s_sendFilePage == null) {
			s_sendFilePage = new SendFilePage(socket, userName, friendName);
		} else {
			System.out.println("传输文件页已经存在，返回已存在实例");

			s_sendFilePage.socket = socket;
			s_sendFilePage.userName = userName;
			s_sendFilePage.friendName = friendName;
		}
		return s_sendFilePage;
	}

	private SendFilePage(Socket socket, String userName, String friendName) {
		this.socket = socket;
		this.userName = userName;
		this.friendName = friendName;

		paintWaitAction();
	}

	public void paintWaitAction() {
		setLocation(500, 250);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(300, 150);
		setTitle("上传文件");
		setLayout(new GridLayout(3, 1));

		JButton jbtUpload = new JButton("上传");
		JButton jbtBrowse = new JButton("浏览");

		jtfFile = new JTextField();
		jtfFile.setEditable(false);

		add(jtfFile);
		add(jbtBrowse);
		add(jbtUpload);

		jbtBrowse.addActionListener(new BtnBrowse_ActionAdapter(this));

		jbtUpload.addActionListener(new BtnUpload_ActionAdapter(this));

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("关闭上传文件窗口");

				s_sendFilePage = null;
			}
		});

		setVisible(true);
	}

	public void closeWindows() {
		dispose();
		s_sendFilePage = null;
	}

	// 浏览按钮事件
	@SuppressWarnings("deprecation")
	public void btnBrowse_actionPerformed(ActionEvent e) {

		FileDialog fd = new FileDialog(this, "上传文件", FileDialog.LOAD);
		fd.show();
		String jfPath = fd.getDirectory() + fd.getFile();

		if ("null".equals(jfPath) == false) { // 浏览文件不选情况
			jtfFile.setText(jfPath);

			// JOptionPane.showMessageDialog(null, fd.getFile(), "标题条文字串",
			// JOptionPane.ERROR_MESSAGE);
		}
	}

	// 上传按钮事件
	public void btnUpload_actionPerformed(ActionEvent e) {
		String filePath = jtfFile.getText();

		if ("".equals(filePath)) { // 检查是否选择文件
			JOptionPane.showMessageDialog(this, "请选择文件", "提示", 2);
			return;
		}
		
		String a = filePath.replaceAll("\\\\", "/");
		
		//String b = a.substring(a.lastIndexOf("/") + 1);
		//System.out.println(a);
		File f = new File(a);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("fileName", a);
		Long l = f.length();
		map.put("length", l.toString());
		JSONObject filePart = JSONObject.fromObject(map);
log.info("文件上床第一次握手文件信息 ： " + filePart);		

		String filePartJson = JsonUtil.buildJson("filePart", filePart);
		
		// 将文件名和msgNum发送到server
		Message message = new Message();
		message.setPublisher(userName);
		message.setReceiver(friendName);
		message.setMsgNum(MsgKey.SEND_FILE);
		message.setWords(filePartJson);
		message.setReceiverIP(socket.getLocalAddress().getHostAddress());
		message.setReceiverPort("" + socket.getLocalPort());
		message.setFilePort(Client.localFilePort);

		String sendMsg1 = message.getResult();
		String jsonOut = JsonUtil.buildJson("msg", sendMsg1);
log.info("文件上床第一次握手完整信息 ：" + jsonOut);
		// 指导者
		BuilderDirector builderDirector = new BuilderDirector(new PrintWriterBuilder(this.socket));
		// 使用指导者生成一个writer
		PrintWriter writer = null;
		try {
			writer = (PrintWriter) builderDirector.construct();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		writer.println(jsonOut);
		writer.flush();

		dispose();
	}

	// 浏览 双向依赖
	public class BtnBrowse_ActionAdapter implements ActionListener {
		private SendFilePage adaptee;

		BtnBrowse_ActionAdapter(SendFilePage adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.btnBrowse_actionPerformed(e);
		}
	}

	// 上传 双向依赖
	public class BtnUpload_ActionAdapter implements ActionListener {
		private SendFilePage adaptee;

		BtnUpload_ActionAdapter(SendFilePage adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.btnUpload_actionPerformed(e);
		}
	}

}
