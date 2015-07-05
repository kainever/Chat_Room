package com.client.handler;

import java.net.Socket;

import net.sf.json.JSONObject;

import com.client.ui.ChatBox;
import com.server.user.User;
import com.util.JsonUtil;

/**
 * ����һ�����Ĵ����� ����ȡ����˴������ĶԻ���Ϣ
 * �����н��� ����msgQueue�У�Ȼ��ChatBox����һ��update�߳�
 * �����ڼ���Ƿ�msgQueue������Ϣ������
 * @author slave_1
 */
public class ResPrivateChatHandler extends AbstractResponseHandler{

	public ResPrivateChatHandler(Socket socket) {
		super(socket);
	}

	@Override
	public void handleResponse() {
		log.info(super.socket + "�յ�˽����Ϣ" + super.responseMsg);
		
		//����msgQ�е���Ϣ���ַ�����ͬ��chatBox��
		JSONObject json = (JSONObject) JsonUtil.parseJson(super.responseMsg,
				"res");
		String friendName = (String) json.get("publisher");
		String content = json.getString("words");
		String username = (String) json.get("receiver");
		User friend = new User();
		friend.setIp((String)json.get("selfIp"));
		friend.setPort((String)json.get("selfPort"));
		friend.setName(friendName);
log.info("friend " + friend.getIp() + " " + friend.getPort() + " ��ǰ�û�   " + username);		

		// ��map���ҵ�chatBox
		ChatBox chatbox = ChatBox.getInstance(socket, username, friend);
		
		//����Ϣ����chatBox
		chatbox.getMsgQueue().offer(content);		
	}

}
