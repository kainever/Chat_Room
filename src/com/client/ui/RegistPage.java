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

	// ע�������
	private AbstractResponseHandler handler;

	public void setResponseHandler(ResRegistHandler handler) {
		this.handler = handler;
	}

	// ע��ɹ���־��˫��������handler
	private String registFlag;

	public void setRegistFlag(String flag) {
		this.registFlag = flag;
	}

	public JTextField jtfName = new JTextField(12); // ����Ϊ���������뽹��
	public JPasswordField jtfPasswd = new JPasswordField(15);

	protected String password;
	
	public static Logger log = Logger.getLogger(RegistPage.class);

	// ����ģʽ
	private static RegistPage s_registPage;

	public static RegistPage getInstance(Socket socket) {
		if (s_registPage == null) {
			s_registPage = new RegistPage(socket);
		} else {
			System.out.println("ע��ҳ���Ѿ����ڣ������Ѵ��ڵ�ʵ��");
		}
		return s_registPage;
	}

	private RegistPage(Socket socket) {
		this.socket = socket;

		// ����������͵�¼����
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
		setTitle("ע��");
		setLayout(new BorderLayout());

		// ��
		JPanel jpInput = new JPanel(new GridLayout(2, 2));

		JLabel jlbName = new JLabel("�û���:");
		jtfName.setText("");
		JLabel jlbPasswd = new JLabel("����:");
		jpInput.add(jlbName);
		jpInput.add(jtfName);
		jpInput.add(jlbPasswd);
		jpInput.add(jtfPasswd);

		add(jpInput, BorderLayout.NORTH);

		// ��
		JPanel jpButton = new JPanel(new GridLayout(1, 2));

		JButton jbtCancel = new JButton("ȡ��");
		JButton jbtRegist = new JButton("ע��");
		jpButton.add(jbtCancel);
		jpButton.add(jbtRegist);

		add(jpButton, BorderLayout.SOUTH);

		setVisible(true);

		// ע��
		jbtRegist.addActionListener(new ActionListener() {
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
					message.setMsgNum(MsgKey.REGISTER);
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

		// ȡ��
		jbtCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �رյ�ǰҳ
				log.info("ȡ��ע��");
				s_registPage.dispose();
			}
		});

		addWindowListener(new WindowAdapter() // �رմ���
		{
			public void windowClosing(WindowEvent e) {
				log.info("�ر�ע��ҳ");
			}
		});
	}

	public void regist(String res) {

		// ����registFlag����
		JSONObject json = (JSONObject) JsonUtil.parseJson(res, "res");
		String flag = json.getString("words");

		if (flag.equals("true")) {
			JOptionPane.showMessageDialog(null, "ע��ɹ���");

			s_registPage.dispose();// �رյ�ǰҳ��
			
			String userInfo = json.getString("receiver");
	log.info("�õ���¼��Ϣ =======" + userInfo);
			// �Զ���¼
			Message message = new Message();
//			message.setPublisher(ResAddFriendHandler.getTempName());
			message.setMsgNum(MsgKey.LOGIN);
			message.setWords(userInfo);
			message.setReceiverIP(socket.getLocalAddress().getHostAddress());
			message.setReceiverPort("" + socket.getLocalPort());
			String sendMsg = message.getResult();
			// ���Client��ͷ
			String jsonOut = JsonUtil.buildJson("msg", sendMsg);

			writer.println(jsonOut);
			writer.flush();
			
		} else {
			// ע��ʧ��
			JOptionPane.showMessageDialog(null, "���û��Ѿ�ע��");
		}
	}
}