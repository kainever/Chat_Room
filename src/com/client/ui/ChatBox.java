package com.client.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.server.user.User;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.builder.BuilderDirector;
import com.util.builder.PrintWriterBuilder;

/**
 * 聊天窗口
 * 1. getInstance() new ChatBox对象
 * 2. 启动一个update线程不断在刷新是否有新的消息传过来
 * 3. 一条消息的传递过程：
 *   3.1 将消息封装成Message对象
 *   3.2 getResult() --- json格式
 *   3.3 jsonUtil.build()
 *   3.4 send
 * @author slave_1
 */
public class ChatBox extends JFrame {

	public static final Logger log = Logger.getLogger(ChatBox.class);
	/**
	 * 管理已经存在的聊天窗口
	 */
	private static Map<String, JFrame> s_chatBoxesMap = new HashMap<String, JFrame>();

	private static final long serialVersionUID = 1L;

	private User friend; // 真名

	private JTextArea jtaMsg = new JTextArea(4, 30);

	private JTextField jtfMsg = new JTextField();// 设为属性为了匿名内部类

	// 单个窗口维持一个消息队列
	private Queue<String> msgQ;

	private Socket socket;

	private String userName; // 真名

	private PrintWriter writer;


	private ChatBox(Socket socket, String userName, User friend)
			throws IOException {
		this.socket = socket;
		this.userName = userName;
		this.friend = friend;

		msgQ = new LinkedBlockingQueue<String>();

		// 指导者
		BuilderDirector builderDirector = new BuilderDirector(
				new PrintWriterBuilder(this.socket));
		// 使用指导者生成一个writer
		try {
			writer = (PrintWriter) builderDirector.construct();
		} catch (IOException e) {
			e.printStackTrace();
		}

		paintWaitAction();

		// 关闭当前窗口时，守护线程也会关闭
		UpdateTextAreaThread thread = new UpdateTextAreaThread();
		thread.setDaemon(true);// 设置为守护线程
		thread.start();
	}

	public static ChatBox getInstance(String friendName) {
		// 先在Map中找
		ChatBox box = (ChatBox) s_chatBoxesMap.get(friendName);
		return box;
	}

	public static ChatBox getInstance(Socket socket, String userName,
			User friend) {
		log.info("当前存在的会话框..." + s_chatBoxesMap);
		String friendName = friend.getName();
		// 先在Map中找
		ChatBox box = (ChatBox) s_chatBoxesMap.get(friendName);

		if (null == box) {
			try {
				box = new ChatBox(socket, userName, friend);

				// 将新建实例放入map
				s_chatBoxesMap.put(friendName, box);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return box;
	}

	/**
	 * 这一点我觉得就是客户端与网页的一个重要区别
	 * 客户端你可以启动一个线程阻塞在哪里，只要有消息闯过来
	 * 就及时更新，而网页你只有再次刷新才能看到最新的消息
	 * @author slave_1
	 */
	public class UpdateTextAreaThread extends Thread {
		public void run() {
			try {
				while (true) {
					// 接到新消息就刷新消息窗口
					String content = ((LinkedBlockingQueue<String>) msgQ)
							.take();

					String a = friend.getName() + " 说：" + content;

					String temp = jtaMsg.getText();

					String output = "";
					if (temp.equals(""))
						output = a;
					else
						output = temp + "\n" + a;

					jtaMsg.setText(output);
					jtaMsg.setCaretPosition(jtaMsg.getText().length());
				}
			} catch (Exception e) {
				System.out.print(e);
			}
		}
	}

	public Queue<String> getMsgQueue() {
		return msgQ;
	}

	private void paintWaitAction() {
		setLocation(500, 220);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(600, 400);
		// setVisible(true); //这个写在最后，要不然控件一开始显示不出来！

		setTitle(this.friend.getName());
		setLayout(new BorderLayout());

		JLabel jlbUserName = new JLabel("当前用户:" + userName);
		JLabel jlbFriendName = new JLabel("Friend Name:" + friend.getName());

		JPanel jpnTop = new JPanel();
		jpnTop.add(jlbFriendName);
		jpnTop.add(jlbUserName);

		jtaMsg.setEnabled(false);
		jtaMsg.setLineWrap(true);// 激活自动换行功能
		jtaMsg.setWrapStyleWord(true);// 激活断行不断字功能
		// jtaMsg.selectAll();

		JScrollPane jsp = new JScrollPane(jtaMsg);

		JPanel jpList = new JPanel(new GridLayout(2, 1));
		jpList.add(jsp);
		jpList.add(jtfMsg);

		JPanel jpnSouth = new JPanel();

		JButton jbtSendFile = new JButton("SendFile");
		JButton jbtSend = new JButton("Send");

		jpnSouth.add(jbtSendFile);
		jpnSouth.add(jbtSend);

		add(jpnTop, BorderLayout.NORTH);
		add(jpList, BorderLayout.CENTER);
		add(jpnSouth, BorderLayout.SOUTH);

		setVisible(true);

		jbtSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 获取输入框内容
				String sendMsg = jtfMsg.getText();
				try {
					// // 将内容打包
					Message message = new Message();
					message.setPublisher(userName);
					message.setReceiver(friend.getName());
					message.setMsgNum("2");
					message.setReceiverIP(friend.getIp());
					message.setReceiverPort(friend.getPort());
					message.setWords(sendMsg);
					message.setSelfIp(socket.getLocalAddress().getHostAddress());
					message.setSelfPort("" + socket.getLocalPort());
					//json格式
					String sendMsg1 = message.getResult();
					String jsonOut = JsonUtil.buildJson("msg", sendMsg1);
log.info("写入消息 " + jsonOut);
					writer.println(jsonOut);
					writer.flush();

					// 将自己发送的消息加到textArea
					String a = userName + " 说：" + sendMsg;

					String temp = jtaMsg.getText();
					String output = "";
					if (temp.equals(""))
						output = a;
					else
						output = temp + "\n" + a;
					jtaMsg.setText(output);
					jtaMsg.setCaretPosition(jtaMsg.getText().length());

					jtfMsg.grabFocus();// 获得焦点
					jtfMsg.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		// 发送文件
		jbtSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SendFilePage sendFilePage = SendFilePage.getInstance(socket,
						userName, friend.getName());
				sendFilePage.setTitle("上传文件");
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					// 关闭窗口就把当前chatBox从map中移除
					s_chatBoxesMap.remove(friend.getName());
					log.info(userName + " 退出私聊");

					SendFilePage a = SendFilePage.getInstance(socket, userName,
							friend.getName());
					if (null != a)
						a.closeWindows();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}

}
