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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.client.handler.ResRegistHandler;
import com.client.handler.AbstractResponseHandler;
import com.client.handler.ResAddFriendHandler;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;
import com.util.builder.BuilderDirector;
import com.util.builder.PrintWriterBuilder;

public class RegistPage extends JFrame {
	private static final long serialVersionUID = 1L;

	private Socket socket;
	private String userName;

	private PrintWriter writer;

	// 注册策略类
	private AbstractResponseHandler handler;

	public void setResponseHandler(ResRegistHandler handler) {
		this.handler = handler;
	}

	// 注册成功标志，双向依赖于handler
	private String registFlag;

	public void setRegistFlag(String flag) {
		this.registFlag = flag;
	}

	public JTextField jtfName = new JTextField(12); // 共有为了设置输入焦点
	public JPasswordField jtfPasswd = new JPasswordField(15);

	protected String password;
	
	public static Logger log = Logger.getLogger(RegistPage.class);

	// 单件模式
	private static RegistPage s_registPage;

	public static RegistPage getInstance(Socket socket) {
		if (s_registPage == null) {
			s_registPage = new RegistPage(socket);
		} else {
			System.out.println("注册页面已经存在，返回已存在的实例");
		}
		return s_registPage;
	}

	private RegistPage(Socket socket) {
		this.socket = socket;

		// 向服务器发送登录请求
		BuilderDirector builderDirector = new BuilderDirector(
				new PrintWriterBuilder(this.socket));
		try {
			writer = (PrintWriter) builderDirector.construct();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	public void paintWaitAction() {
		setLocation(500, 220);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(300, 180);
		setTitle("注册");
		setLayout(new BorderLayout());

		// 上
		JPanel jpInput = new JPanel(new GridLayout(2, 2));

		JLabel jlbName = new JLabel("用户名:");
		jtfName.setText("");
		JLabel jlbPasswd = new JLabel("密码:");
		jpInput.add(jlbName);
		jpInput.add(jtfName);
		jpInput.add(jlbPasswd);
		jpInput.add(jtfPasswd);

		add(jpInput, BorderLayout.NORTH);

		// 下
		JPanel jpButton = new JPanel(new GridLayout(1, 2));

		JButton jbtCancel = new JButton("取消");
		JButton jbtRegist = new JButton("注册");
		jpButton.add(jbtCancel);
		jpButton.add(jbtRegist);

		add(jpButton, BorderLayout.SOUTH);

		setVisible(true);

		// 注册
		jbtRegist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// “空输入”判断
				if (jtfName.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "输入信息不能为空");
				} else {
					// 获取输入框内容
					userName = jtfName.getText();
					password = new String(jtfPasswd.getPassword());

					// 将内容打包
					Message message = new Message();
//					message.setPublisher(ResAddFriendHandler.getTempName());
					message.setMsgNum(MsgKey.REGISTER);
					message.setWords(userName + "_" + password);
					message.setReceiverIP(socket.getLocalAddress().getHostAddress());
					message.setReceiverPort("" + socket.getLocalPort());
					String sendMsg = message.getResult();
					// 添加Client包头
					String jsonOut = JsonUtil.buildJson("msg", sendMsg);

					writer.println(jsonOut);
					writer.flush();
				}// end of else
			}
		});// end of jbtOK

		// 取消
		jbtCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 关闭当前页
				log.info("取消注册");
				s_registPage.dispose();
			}
		});

		addWindowListener(new WindowAdapter() // 关闭窗口
		{
			public void windowClosing(WindowEvent e) {
				log.info("关闭注册页");
			}
		});
	}

	public void regist(String res) {

		// 解析registFlag部分
		JSONObject json = (JSONObject) JsonUtil.parseJson(res, "res");
		String flag = json.getString("words");

		if (flag.equals("true")) {
			JOptionPane.showMessageDialog(null, "注册成功！");

			s_registPage.dispose();// 关闭当前页面
			
			String userInfo = json.getString("receiver");
	log.info("得到登录信息 =======" + userInfo);
			// 自动登录
			Message message = new Message();
//			message.setPublisher(ResAddFriendHandler.getTempName());
			message.setMsgNum(MsgKey.LOGIN);
			message.setWords(userInfo);
			message.setReceiverIP(socket.getLocalAddress().getHostAddress());
			message.setReceiverPort("" + socket.getLocalPort());
			String sendMsg = message.getResult();
			// 添加Client包头
			String jsonOut = JsonUtil.buildJson("msg", sendMsg);

			writer.println(jsonOut);
			writer.flush();
			
		} else {
			// 注册失败
			JOptionPane.showMessageDialog(null, "该用户已经注册");
		}
	}
}