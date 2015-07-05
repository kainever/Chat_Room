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
	
	// ����ģʽ
	private static LoginPage s_loginPage;
	
	public static Logger log = Logger.getLogger(LoginPage.class);
	
	
	private static final long serialVersionUID = 1L;

	// ����ע��ҳע�봦�������
	private AbstractResponseHandler agentHandler;

	// ע���¼���������
	private AbstractResponseHandler handler;

	// ��¼�ɹ�����homePage����Ϊ�ɼ�
	private HomePage homePage;

	public JTextField jtfName = new JTextField(12); // ֻ���ڻ�����ɺ��������뽹��

	public JPasswordField jtfPasswd = new JPasswordField(15);

	/**
	 * ��¼�ɹ��ı�־
	 */
	private String loginFlag;

	private Socket socket;

	private String userName;
	private String password;

	private PrintWriter writer;

	private LoginPage(Socket socket) {
		this.socket = socket;
		this.loginFlag = null;

		// ����������͵�¼����
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
			log.info("��¼�����Ѿ����ڣ������Ѵ��ڵ�ʵ��");
		}
		return s_loginPage;
	}
	
	
	/**
	 * ���ݷ���˷�����Ϣ���� 
	 */
	public void login() {
		((ResLoginHandler) handler).updateLoginFlag();

		// ����loginFlag����
		JSONObject json = (JSONObject) JsonUtil.parseJson(loginFlag, "res");
		String flag = json.getString("words");
		
//		String userName = json.getString("receiver");

		if (flag.equals("true")) {
			// ��¼�ɹ�
			homePage.setVisible(true);
			homePage.setUser(userName);
			log.info("����homepage����server��������б�");
			homePage.paintWaitAction();

//			SetNameHandler.setRealName(userName);
			
			dispose(); //�رմ���
		} else {
			// �û�δע��
			JOptionPane.showMessageDialog(null, "δע���û�");
		}
	}

	public void paintWaitAction() {
		setLocation(500, 220);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(280, 150);
		setTitle("��¼");
		setLayout(new BorderLayout());

		// ��
		JPanel jpInput = new JPanel(new GridLayout(2, 2));

		JLabel jlbName = new JLabel("�û���:");
		JLabel jlbPasswd = new JLabel("����:");
		jtfName.setText("");

		jpInput.add(jlbName);
		jpInput.add(jtfName);
		jpInput.add(jlbPasswd);
		jpInput.add(jtfPasswd);

		add(jpInput, BorderLayout.NORTH);

		// ��
		JPanel jpButton = new JPanel(new GridLayout(1, 2));

		JButton jbtLogin = new JButton("��¼");
		JButton jbtRegist = new JButton("ע��");
		jpButton.add(jbtLogin);
		jpButton.add(jbtRegist);

		add(jpButton, BorderLayout.SOUTH);

		setVisible(true);
		
		// ��¼
		jbtLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �������롱�ж�
				if (jtfName.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "������Ϣ����Ϊ��");
				} else {
					// ��ȡ���������
					userName = jtfName.getText();
					password = new String(jtfPasswd.getPassword());

					// �����ݴ��
					Message message = new Message();
//					message.setPublisher(ResAddFriendHandler.getTempName());
					message.setMsgNum("6");
					message.setWords(userName + "_" + password);
					message.setReceiverIP(socket.getLocalAddress().getHostAddress());
					message.setReceiverPort("" + socket.getLocalPort());
					String sendMsg = message.getResult();
					// ���Client��ͷ
					String jsonOut = JsonUtil.buildJson("msg", sendMsg);

					writer.println(jsonOut);
					writer.flush();
					
				}// end of else			
			}
		});// end of jbtOK

		// ע��
		jbtRegist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ��ת��ע��ҳ��
				RegistPage registPage = RegistPage.getInstance(socket);
//				registPage.setResponseHandler((ResRegistHandler) agentHandler);
				registPage.paintWaitAction();
			}
		});// end of jbtOK

		addWindowListener(new WindowAdapter() // �رմ���
		{
			public void windowClosing(WindowEvent e) {
				System.out.println("�رյ�¼ҳ");
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
