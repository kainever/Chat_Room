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
import javax.swing.JTextField;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.client.handler.AbstractResponseHandler;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;
import com.util.builder.BuilderDirector;
import com.util.builder.PrintWriterBuilder;

/**
 * ��Ӻ��ѽ���
 * 
 * @author slave_1
 */
public class AddPage extends JFrame {

	public static void main(String[] args) {
		// new AddPage()
	}

	// ����ģʽ
	private static AddPage s_addPage;

	public static Logger log = Logger.getLogger(AddPage.class);
	private AbstractResponseHandler handler;
	public JTextField jtfName = new JTextField(12); // ֻ���ڻ�����ɺ��������뽹��
	private Socket socket;
	private PrintWriter writer;
	private String addName;
	private String userName;

	public static AddPage getInstance(Socket socket , String userName) {
		if (s_addPage == null) {
			s_addPage = new AddPage(socket , userName);
		} else {
			log.info("�Ѿ����ڸ���ӽ���...");
		}
		return s_addPage;
	}

	private AddPage(Socket socket , String userName) {
		this.socket = socket;
		this.userName = userName;
		BuilderDirector builderDirector = new BuilderDirector(
				new PrintWriterBuilder(this.socket));
		try {
			writer = (PrintWriter) builderDirector.construct();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		init();
	}

	public void init() {
		setLocation(500, 220);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(280, 150);
		setTitle("���");
		setLayout(new BorderLayout());

		JPanel jpInput = new JPanel(new GridLayout(1, 2));

		JLabel jlbName = new JLabel("��ӵ��û���:");
		jtfName.setText("");

		jpInput.add(jlbName);
		jpInput.add(jtfName);

		add(jpInput, BorderLayout.NORTH);

		// ��
		JPanel jpButton = new JPanel(new GridLayout(1, 1));

		JButton jbtLogin = new JButton("add");
		jpButton.add(jbtLogin);

		add(jpButton, BorderLayout.SOUTH);

		setVisible(true);

		jbtLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �������롱�ж�
				if (jtfName.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "������Ϣ����Ϊ��");
				} else {
					// ��ȡ���������
					addName = jtfName.getText();

					// �����ݴ��
					Message message = new Message();
					// message.setPublisher(ResAddFriendHandler.getTempName());
					message.setMsgNum(MsgKey.ADD_FRIEND);
					message.setWords("false");
					message.setReceiver(addName);
					message.setPublisher(userName);
					message.setReceiverIP(socket.getLocalAddress()
							.getHostAddress());
					message.setReceiverPort("" + socket.getLocalPort());
					String sendMsg = message.getResult();
					// ���Client��ͷ
					String jsonOut = JsonUtil.buildJson("msg", sendMsg);

					writer.println(jsonOut);
					writer.flush();

				}// end of else
			}
		});// end of jbtOK
		
		addWindowListener(new WindowAdapter() // �رմ���
		{
			//���ڹر��¼�
			public void windowClosing(WindowEvent e) {
				s_addPage = null;
			}
		});
	}

	public void isAdd(String res) {
		// ����loginFlag����
		JSONObject json = (JSONObject) JsonUtil.parseJson(res, "res");
		String flag = json.getString("words");

		if (flag.equals("true")) {
			// ��¼�ɹ�
			int select = JOptionPane.showConfirmDialog(this, "���ҵ�,�Ƿ����");
			if(select == 1 || select == 3) {
				log.info(userName + " ȡ�����");
			} else {
				Message message = new Message();
				// message.setPublisher(ResAddFriendHandler.getTempName());
				message.setMsgNum(MsgKey.ADD_FRIEND);
				message.setPublisher(userName);
				message.setReceiver(json.getString("receiver"));
				message.setWords("true");
				message.setReceiverIP(socket.getLocalAddress()
						.getHostAddress());
				message.setReceiverPort("" + socket.getLocalPort());
				String sendMsg = message.getResult();
				// ���Client��ͷ
				String jsonOut = JsonUtil.buildJson("msg", sendMsg);

				writer.println(jsonOut);
				writer.flush();
			}
			dispose(); // �رմ���
			s_addPage = null;
		} else if(flag.equals("false")) {
			// �û�δע��
			JOptionPane.showMessageDialog(null, "�����ڸ��û�");
		} else if (flag.equals("add")) {
//			this.setVisible(false);
			JOptionPane.showMessageDialog(this, "��ӳɹ�");
			dispose(); // �رմ���
			s_addPage = null;
//			ˢ��
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
	}

}
