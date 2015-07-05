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

import com.client.handler.ResLoginHandler;
import com.client.handler.ResRegistHandler;
import com.client.handler.AbstractResponseHandler;
import com.client.handler.ResAddFriendHandler;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.builder.BuilderDirector;
import com.util.builder.PrintWriterBuilder;

//import net.sf.json.JSONObject;

public class LoginPage extends JFrame {
	
	// 单件模式
	private static LoginPage s_loginPage;
	
	public static Logger log = Logger.getLogger(LoginPage.class);
	
	
	private static final long serialVersionUID = 1L;

	// 代替注册页注入处理策略类
	private AbstractResponseHandler agentHandler;

	// 注入登录处理策略类
	private AbstractResponseHandler handler;

	// 登录成功，将homePage设置为可见
	private HomePage homePage;

	public JTextField jtfName = new JTextField(12); // 只能在绘制完成后设置输入焦点

	public JPasswordField jtfPasswd = new JPasswordField(15);

	/**
	 * 登录成功的标志
	 */
	private String loginFlag;

	private Socket socket;

	private String userName;
	private String password;

	private PrintWriter writer;

	private LoginPage(Socket socket) {
		this.socket = socket;
		this.loginFlag = null;

		// 向服务器发送登录请求
		BuilderDirector builderDirector = new BuilderDirector(new PrintWriterBuilder(this.socket));
		try {
			writer = (PrintWriter) builderDirector.construct();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		// paintWaitAction();
	}
	
	
	public static LoginPage getInstance(Socket socket) {
		if (null == s_loginPage) {
			s_loginPage = new LoginPage(socket);
		} else {
			log.info("登录窗口已经存在，返回已存在的实例");
		}
		return s_loginPage;
	}
	
	
	/**
	 * 根据服务端反馈信息处理 
	 */
	public void login() {
		((ResLoginHandler) handler).updateLoginFlag();

		// 解析loginFlag部分
		JSONObject json = (JSONObject) JsonUtil.parseJson(loginFlag, "res");
		String flag = json.getString("words");
		
//		String userName = json.getString("receiver");

		if (flag.equals("true")) {
			// 登录成功
			homePage.setVisible(true);
			homePage.setUser(userName);
			log.info("启动homepage并向server请求好友列表");
			homePage.paintWaitAction();

//			SetNameHandler.setRealName(userName);
			
			dispose(); //关闭窗口
		} else {
			// 用户未注册
			JOptionPane.showMessageDialog(null, "未注册用户");
		}
	}

	public void paintWaitAction() {
		setLocation(500, 220);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(280, 150);
		setTitle("登录");
		setLayout(new BorderLayout());

		// 上
		JPanel jpInput = new JPanel(new GridLayout(2, 2));

		JLabel jlbName = new JLabel("用户名:");
		JLabel jlbPasswd = new JLabel("密码:");
		jtfName.setText("");

		jpInput.add(jlbName);
		jpInput.add(jtfName);
		jpInput.add(jlbPasswd);
		jpInput.add(jtfPasswd);

		add(jpInput, BorderLayout.NORTH);

		// 下
		JPanel jpButton = new JPanel(new GridLayout(1, 2));

		JButton jbtLogin = new JButton("登录");
		JButton jbtRegist = new JButton("注册");
		jpButton.add(jbtLogin);
		jpButton.add(jbtRegist);

		add(jpButton, BorderLayout.SOUTH);

		setVisible(true);
		
		// 登录
		jbtLogin.addActionListener(new ActionListener() {
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
					message.setMsgNum("6");
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

		// 注册
		jbtRegist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 跳转到注册页面
				RegistPage registPage = RegistPage.getInstance(socket);
//				registPage.setResponseHandler((ResRegistHandler) agentHandler);
				registPage.paintWaitAction();
			}
		});// end of jbtOK

		addWindowListener(new WindowAdapter() // 关闭窗口
		{
			public void windowClosing(WindowEvent e) {
				System.out.println("关闭登录页");
			}
		});
	}

	public void setHomePage(HomePage home) {
		this.homePage = home;
	}

	public void setLoginFlag(String flag) {
		this.loginFlag = flag;
	}

	public void setRegistHandler(ResRegistHandler handler) {
		this.agentHandler = handler;
	}

	public void setResponseHandler(ResLoginHandler handler) {
		this.handler = handler;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public void setUserName(String loginUserName) {
		this.userName = loginUserName;
	}

}
