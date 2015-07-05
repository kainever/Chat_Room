package com.client.handler;

import java.net.Socket;

import net.sf.json.JSONObject;

import com.client.ui.ChatBox;
import com.server.user.User;
import com.util.JsonUtil;

/**
 * 这是一个核心处理器 它读取服务端传过来的对话信息
 * 并进行解析 加如msgQueue中，然后ChatBox中有一个update线程
 * 不断在检查是否msgQueue中有消息传过来
 * @author slave_1
 */
public class ResPrivateChatHandler extends AbstractResponseHandler{

	public ResPrivateChatHandler(Socket socket) {
		super(socket);
	}

	@Override
	public void handleResponse() {
		log.info(super.socket + "收到私聊消息" + super.responseMsg);
		
		//解析msgQ中的消息，分发到不同的chatBox中
		JSONObject json = (JSONObject) JsonUtil.parseJson(super.responseMsg,
				"res");
		String friendName = (String) json.get("publisher");
		String content = json.getString("words");
		String username = (String) json.get("receiver");
		User friend = new User();
		friend.setIp((String)json.get("selfIp"));
		friend.setPort((String)json.get("selfPort"));
		friend.setName(friendName);
log.info("friend " + friend.getIp() + " " + friend.getPort() + " 当前用户   " + username);		

		// 在map中找到chatBox
		ChatBox chatbox = ChatBox.getInstance(socket, username, friend);
		
		//将消息传入chatBox
		chatbox.getMsgQueue().offer(content);		
	}

}
