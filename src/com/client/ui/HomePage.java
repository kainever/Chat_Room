package com.client.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.client.handler.AbstractResponseHandler;
import com.client.handler.ResDisplayFriendsHandler;
import com.server.user.User;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.builder.BuilderDirector;
import com.util.builder.PrintWriterBuilder;

public class HomePage extends JFrame {
	
	public static Logger log = Logger.getLogger(HomePage.class);
	private static final long serialVersionUID = 1L;

	private JTree tree;
	private DefaultTreeModel model;
	private DefaultMutableTreeNode rootNode;

	private Socket socket;
	private PrintWriter writer; // �������ڲ�����Ҫʹ��

	// ��ǰ�û�
	private String userName;

	public void setUser(String user) {
		this.userName = user;
	}

	// ������ˢ�º����б������
	private AbstractResponseHandler handler;

	public void setResponseHandler(ResDisplayFriendsHandler handler) {
		this.handler = handler;
	}

	// �����б�ԭJson��
	private String friendListStr;

	public void setFriendListStr(String s) {
		this.friendListStr = s;
	}

	// ����ģʽ
	private static HomePage s_homePage;

	public static HomePage getInstance(Socket socket) throws IOException {
		if (s_homePage == null) {
			s_homePage = new HomePage(socket);
		} else {
			System.out.println("��ҳ�Ѿ����ڣ������Ѵ��ڵ�ʵ��");
		}
		return s_homePage;
	}
	
	private HomePage() {
		
	}

	private HomePage(Socket socket) {
		this.socket = socket;

		this.userName = null;
		this.handler = null;
		this.friendListStr = null;

		BuilderDirector builderDirector = new BuilderDirector(new PrintWriterBuilder(socket));
		try {
			writer = (PrintWriter) builderDirector.construct();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// paintWaitAction(); //�ⲿָ��
	}

	// caller
	public void updateFriendList() {
		rootNode.removeAllChildren();

		// ֪ͨhandler�����Լ��ĺ����б�
		((ResDisplayFriendsHandler) handler).updateFriendListStr();

		// ������������ͷ
		JSONObject json1 = (JSONObject) JsonUtil.parseJson(friendListStr,
				"res");
		String content = json1.getString("words");// ȡ��content����

		// ����content����
		JSONArray friends = (JSONArray) JsonUtil.parseJson(content,
				"friends");

		DefaultMutableTreeNode leafTreeNode;

		Iterator<?> it = friends.iterator();
		while (it.hasNext()) {
//			String name = (String) it.next();// ��ʵ��
			JSONObject f = (JSONObject) it.next();
			User friend = new User();
			friend.setIp((String)f.get("ip"));
			friend.setName((String)f.get("name"));
			friend.setOnline(f.getBoolean("online"));
			friend.setPort(f.getString("port"));
			
			leafTreeNode = new DefaultMutableTreeNode(friend);
			rootNode.add(leafTreeNode);
		}
		model.reload();
	}

	public void paintWaitAction() {
		setLocation(500, 170);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 540);
		// setVisible(true);
		setLayout(new BorderLayout());

		try {
			JLabel jlbUserName = new JLabel("�û�����" + userName);

			// ����map����
			rootNode = new DefaultMutableTreeNode("����");

			// ���������������б�
			Message message = new Message();
			message.setPublisher(userName);
			message.setMsgNum("1");
			message.setReceiverIP(socket.getLocalAddress().getHostAddress());
			message.setReceiverPort("" + socket.getLocalPort());
			String sendMsg = message.getResult();
			// ��ӿͻ��˰�ͷ
			String a = JsonUtil.buildJson("msg", sendMsg);

			writer.println(a);
			writer.flush();
			tree = new JTree(rootNode);
			model = (DefaultTreeModel) tree.getModel();

			JScrollPane jsp = new JScrollPane(tree);

			JButton jbtAddFriend = new JButton("��Ӻ���");
			JButton jbtUpdate = new JButton("ˢ��");
//			JButton jbtGroupChat = new JButton("Ⱥ��");

			JPanel jpSouth = new JPanel();
			jpSouth.add(jbtAddFriend);
			jpSouth.add(jbtUpdate);
//			jpSouth.add(jbtGroupChat);

			add(jlbUserName, BorderLayout.NORTH);
			add(jsp, BorderLayout.CENTER);
			add(jpSouth, BorderLayout.SOUTH);

			setVisible(true);

			// ��̳��ť�¼������������
			jbtAddFriend.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					AddPage add = AddPage.getInstance(socket ,userName);
					add.setTitle("add");
				}
				
			});

//			// ˢ�°�ť�¼�
			jbtUpdate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// ���������������б�
					Message message = new Message();
					message.setPublisher(userName);
					message.setMsgNum("1");
					message.setReceiverIP(socket.getLocalAddress().getHostAddress());
					message.setReceiverPort("" + socket.getLocalPort());
					String sendMsg = message.getResult();
					// ��ӿͻ��˰�ͷ
					String a = JsonUtil.buildJson("msg", sendMsg);

					writer.println(a);
					writer.flush();
				}
			});
//
//			// Ⱥ�İ�ť�¼�
//			jbtGroupChat.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					// ����ģʽ�µ�groupChatBox
//					GroupChatBox groupBox = GroupChatBox.getInstance(socket,
//							userName);
//					groupBox.setTitle("Ⱥ�Ĵ���");
//				}
//			});

			// ��Ҷ�ڵ�ĵ���¼�
			tree.addTreeSelectionListener(new TreeSelectionListener() {

				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
							.getLastSelectedPathComponent();
					if (node == null)
						return;

					Object object = node.getUserObject();
					if (node.isLeaf()) {
						User friend = (User) object;
						if(!friend.isOnline()) {
							JOptionPane.showMessageDialog(null, "���û�δ�����ݲ�֧��");
						}
						// �Լ����Լ���������
						else if (!friend.getName().equals(userName)) {
							ChatBox chatbox = ChatBox.getInstance(socket,
									userName, friend);
							chatbox.setTitle(friend.getName());
						}
					}
				}
			});

			addWindowListener(new WindowAdapter() // �رմ���
			{
				//���ڹر��¼�
				public void windowClosing(WindowEvent e) {
					// ��װ��Ϣ
					Message message = new Message();
					message.setPublisher(userName);
					message.setMsgNum("4");
					message.setReceiverIP(socket.getLocalAddress().getHostAddress());
					message.setReceiverPort("" + socket.getLocalPort());

					String sendMsg = message.getResult();

					// ��ӿͻ��˰�ͷ
					String a = JsonUtil.buildJson("msg", sendMsg);
log.info("�ر�homePage д����Ϣ " + a );
					writer.println(a);
					writer.flush();

					// �ر��߳�ResConsumeThread->�Ѿ������߳�����Ϊ�ػ�����
					// thread.stop();
					// thread1.stop();

					System.out.println("�ر���ҳ");

					System.exit(0); // ǿ�йر������߳�
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		HomePage h = new HomePage();
	}
}