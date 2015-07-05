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
 * ���촰��
 * 1. getInstance() new ChatBox����
 * 2. ����һ��update�̲߳�����ˢ���Ƿ����µ���Ϣ������
 * 3. һ����Ϣ�Ĵ��ݹ��̣�
 *   3.1 ����Ϣ��װ��Message����
 *   3.2 getResult() --- json��ʽ
 *   3.3 jsonUtil.build()
 *   3.4 send
 * @author slave_1
 */
public class ChatBox extends JFrame {

	public static final Logger log = Logger.getLogger(ChatBox.class);
	/**
	 * �����Ѿ����ڵ����촰��
	 */
	private static Map<String, JFrame> s_chatBoxesMap = new HashMap<String, JFrame>();

	private static final long serialVersionUID = 1L;

	private User friend; // ����

	private JTextArea jtaMsg = new JTextArea(4, 30);

	private JTextField jtfMsg = new JTextField();// ��Ϊ����Ϊ�������ڲ���

	// ��������ά��һ����Ϣ����
	private Queue<String> msgQ;

	private Socket socket;

	private String userName; // ����

	private PrintWriter writer;


	private ChatBox(Socket socket, String userName, User friend)
			throws IOException {
		this.socket = socket;
		this.userName = userName;
		this.friend = friend;

		msgQ = new LinkedBlockingQueue<String>();

		// ָ����
		BuilderDirector builderDirector = new BuilderDirector(
				new PrintWriterBuilder(this.socket));
		// ʹ��ָ��������һ��writer
		try {
			writer = (PrintWriter) builderDirector.construct();
		} catch (IOException e) {
			e.printStackTrace();
		}

		paintWaitAction();

		// �رյ�ǰ����ʱ���ػ��߳�Ҳ��ر�
		UpdateTextAreaThread thread = new UpdateTextAreaThread();
		thread.setDaemon(true);// ����Ϊ�ػ��߳�
		thread.start();
	}

	public static ChatBox getInstance(String friendName) {
		// ����Map����
		ChatBox box = (ChatBox) s_chatBoxesMap.get(friendName);
		return box;
	}

	public static ChatBox getInstance(Socket socket, String userName,
			User friend) {
		log.info("��ǰ���ڵĻỰ��..." + s_chatBoxesMap);
		String friendName = friend.getName();
		// ����Map����
		ChatBox box = (ChatBox) s_chatBoxesMap.get(friendName);

		if (null == box) {
			try {
				box = new ChatBox(socket, userName, friend);

				// ���½�ʵ������map
				s_chatBoxesMap.put(friendName, box);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return box;
	}

	/**
	 * ��һ���Ҿ��þ��ǿͻ�������ҳ��һ����Ҫ����
	 * �ͻ������������һ���߳����������ֻҪ����Ϣ������
	 * �ͼ�ʱ���£�����ҳ��ֻ���ٴ�ˢ�²��ܿ������µ���Ϣ
	 * @author slave_1
	 */
	public class UpdateTextAreaThread extends Thread {
		public void run() {
			try {
				while (true) {
					// �ӵ�����Ϣ��ˢ����Ϣ����
					String content = ((LinkedBlockingQueue<String>) msgQ)
							.take();

					String a = friend.getName() + " ˵��" + content;

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
		// setVisible(true); //���д�����Ҫ��Ȼ�ؼ�һ��ʼ��ʾ��������

		setTitle(this.friend.getName());
		setLayout(new BorderLayout());

		JLabel jlbUserName = new JLabel("��ǰ�û�:" + userName);
		JLabel jlbFriendName = new JLabel("Friend Name:" + friend.getName());

		JPanel jpnTop = new JPanel();
		jpnTop.add(jlbFriendName);
		jpnTop.add(jlbUserName);

		jtaMsg.setEnabled(false);
		jtaMsg.setLineWrap(true);// �����Զ����й���
		jtaMsg.setWrapStyleWord(true);// ������в����ֹ���
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
				// ��ȡ���������
				String sendMsg = jtfMsg.getText();
				try {
					// // �����ݴ��
					Message message = new Message();
					message.setPublisher(userName);
					message.setReceiver(friend.getName());
					message.setMsgNum("2");
					message.setReceiverIP(friend.getIp());
					message.setReceiverPort(friend.getPort());
					message.setWords(sendMsg);
					message.setSelfIp(socket.getLocalAddress().getHostAddress());
					message.setSelfPort("" + socket.getLocalPort());
					//json��ʽ
					String sendMsg1 = message.getResult();
					String jsonOut = JsonUtil.buildJson("msg", sendMsg1);
log.info("д����Ϣ " + jsonOut);
					writer.println(jsonOut);
					writer.flush();

					// ���Լ����͵���Ϣ�ӵ�textArea
					String a = userName + " ˵��" + sendMsg;

					String temp = jtaMsg.getText();
					String output = "";
					if (temp.equals(""))
						output = a;
					else
						output = temp + "\n" + a;
					jtaMsg.setText(output);
					jtaMsg.setCaretPosition(jtaMsg.getText().length());

					jtfMsg.grabFocus();// ��ý���
					jtfMsg.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		// �����ļ�
		jbtSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SendFilePage sendFilePage = SendFilePage.getInstance(socket,
						userName, friend.getName());
				sendFilePage.setTitle("�ϴ��ļ�");
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					// �رմ��ھͰѵ�ǰchatBox��map���Ƴ�
					s_chatBoxesMap.remove(friend.getName());
					log.info(userName + " �˳�˽��");

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
