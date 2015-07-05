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
	private PrintWriter writer; // 有匿名内部类需要使用

	// 当前用户
	private String userName;

	public void setUser(String user) {
		this.userName = user;
	}

	// 依赖于刷新好友列表策略类
	private AbstractResponseHandler handler;

	public void setResponseHandler(ResDisplayFriendsHandler handler) {
		this.handler = handler;
	}

	// 好友列表，原Json串
	private String friendListStr;

	public void setFriendListStr(String s) {
		this.friendListStr = s;
	}

	// 单件模式
	private static HomePage s_homePage;

	public static HomePage getInstance(Socket socket) throws IOException {
		if (s_homePage == null) {
			s_homePage = new HomePage(socket);
		} else {
			System.out.println("主页已经存在，返回已存在的实例");
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

		// paintWaitAction(); //外部指定
	}

	// caller
	public void updateFriendList() {
		rootNode.removeAllChildren();

		// 通知handler更新自己的好友列表
		((ResDisplayFriendsHandler) handler).updateFriendListStr();

		// 解析服务器包头
		JSONObject json1 = (JSONObject) JsonUtil.parseJson(friendListStr,
				"res");
		String content = json1.getString("words");// 取出content部分

		// 解析content部分
		JSONArray friends = (JSONArray) JsonUtil.parseJson(content,
				"friends");

		DefaultMutableTreeNode leafTreeNode;

		Iterator<?> it = friends.iterator();
		while (it.hasNext()) {
//			String name = (String) it.next();// 真实名
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
			JLabel jlbUserName = new JLabel("用户名：" + userName);

			// 利用map建树
			rootNode = new DefaultMutableTreeNode("好友");

			// 向服务器请求好友列表
			Message message = new Message();
			message.setPublisher(userName);
			message.setMsgNum("1");
			message.setReceiverIP(socket.getLocalAddress().getHostAddress());
			message.setReceiverPort("" + socket.getLocalPort());
			String sendMsg = message.getResult();
			// 添加客户端包头
			String a = JsonUtil.buildJson("msg", sendMsg);

			writer.println(a);
			writer.flush();
			tree = new JTree(rootNode);
			model = (DefaultTreeModel) tree.getModel();

			JScrollPane jsp = new JScrollPane(tree);

			JButton jbtAddFriend = new JButton("添加好友");
			JButton jbtUpdate = new JButton("刷新");
//			JButton jbtGroupChat = new JButton("群聊");

			JPanel jpSouth = new JPanel();
			jpSouth.add(jbtAddFriend);
			jpSouth.add(jbtUpdate);
//			jpSouth.add(jbtGroupChat);

			add(jlbUserName, BorderLayout.NORTH);
			add(jsp, BorderLayout.CENTER);
			add(jpSouth, BorderLayout.SOUTH);

			setVisible(true);

			// 论坛按钮事件，跳出浏览器
			jbtAddFriend.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					AddPage add = AddPage.getInstance(socket ,userName);
					add.setTitle("add");
				}
				
			});

//			// 刷新按钮事件
			jbtUpdate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// 向服务器请求好友列表
					Message message = new Message();
					message.setPublisher(userName);
					message.setMsgNum("1");
					message.setReceiverIP(socket.getLocalAddress().getHostAddress());
					message.setReceiverPort("" + socket.getLocalPort());
					String sendMsg = message.getResult();
					// 添加客户端包头
					String a = JsonUtil.buildJson("msg", sendMsg);

					writer.println(a);
					writer.flush();
				}
			});
//
//			// 群聊按钮事件
//			jbtGroupChat.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					// 单件模式下的groupChatBox
//					GroupChatBox groupBox = GroupChatBox.getInstance(socket,
//							userName);
//					groupBox.setTitle("群聊窗口");
//				}
//			});

			// 树叶节点的点击事件
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
							JOptionPane.showMessageDialog(null, "该用户未在线暂不支持");
						}
						// 自己和自己不能聊天
						else if (!friend.getName().equals(userName)) {
							ChatBox chatbox = ChatBox.getInstance(socket,
									userName, friend);
							chatbox.setTitle(friend.getName());
						}
					}
				}
			});

			addWindowListener(new WindowAdapter() // 关闭窗口
			{
				//窗口关闭事件
				public void windowClosing(WindowEvent e) {
					// 组装消息
					Message message = new Message();
					message.setPublisher(userName);
					message.setMsgNum("4");
					message.setReceiverIP(socket.getLocalAddress().getHostAddress());
					message.setReceiverPort("" + socket.getLocalPort());

					String sendMsg = message.getResult();

					// 添加客户端包头
					String a = JsonUtil.buildJson("msg", sendMsg);
log.info("关闭homePage 写入信息 " + a );
					writer.println(a);
					writer.flush();

					// 关闭线程ResConsumeThread->已经将子线程设置为守护进程
					// thread.stop();
					// thread1.stop();

					System.out.println("关闭主页");

					System.exit(0); // 强行关闭所有线程
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