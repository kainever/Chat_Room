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
 * 添加好友界面
 * 
 * @author slave_1
 */
public class AddPage extends JFrame {

	public static void main(String[] args) {
		// new AddPage()
	}

	// 单件模式
	private static AddPage s_addPage;

	public static Logger log = Logger.getLogger(AddPage.class);
	private AbstractResponseHandler handler;
	public JTextField jtfName = new JTextField(12); // 只能在绘制完成后设置输入焦点
	private Socket socket;
	private PrintWriter writer;
	private String addName;
	private String userName;

	public static AddPage getInstance(Socket socket , String userName) {
		if (s_addPage == null) {
			s_addPage = new AddPage(socket , userName);
		} else {
			log.info("已经存在该添加界面...");
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
		setTitle("添加");
		setLayout(new BorderLayout());

		JPanel jpInput = new JPanel(new GridLayout(1, 2));

		JLabel jlbName = new JLabel("添加的用户名:");
		jtfName.setText("");

		jpInput.add(jlbName);
		jpInput.add(jtfName);

		add(jpInput, BorderLayout.NORTH);

		// 下
		JPanel jpButton = new JPanel(new GridLayout(1, 1));

		JButton jbtLogin = new JButton("add");
		jpButton.add(jbtLogin);

		add(jpButton, BorderLayout.SOUTH);

		setVisible(true);

		jbtLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// “空输入”判断
				if (jtfName.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "输入信息不能为空");
				} else {
					// 获取输入框内容
					addName = jtfName.getText();

					// 将内容打包
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
					// 添加Client包头
					String jsonOut = JsonUtil.buildJson("msg", sendMsg);

					writer.println(jsonOut);
					writer.flush();

				}// end of else
			}
		});// end of jbtOK
		
		addWindowListener(new WindowAdapter() // 关闭窗口
		{
			//窗口关闭事件
			public void windowClosing(WindowEvent e) {
				s_addPage = null;
			}
		});
	}

	public void isAdd(String res) {
		// 解析loginFlag部分
		JSONObject json = (JSONObject) JsonUtil.parseJson(res, "res");
		String flag = json.getString("words");

		if (flag.equals("true")) {
			// 登录成功
			int select = JOptionPane.showConfirmDialog(this, "已找到,是否添加");
			if(select == 1 || select == 3) {
				log.info(userName + " 取消添加");
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
				// 添加Client包头
				String jsonOut = JsonUtil.buildJson("msg", sendMsg);

				writer.println(jsonOut);
				writer.flush();
			}
			dispose(); // 关闭窗口
			s_addPage = null;
		} else if(flag.equals("false")) {
			// 用户未注册
			JOptionPane.showMessageDialog(null, "不存在该用户");
		} else if (flag.equals("add")) {
//			this.setVisible(false);
			JOptionPane.showMessageDialog(this, "添加成功");
			dispose(); // 关闭窗口
			s_addPage = null;
//			刷新
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
	}

}
