package com.client.handler;

import java.net.Socket;

import net.sf.json.JSONObject;

import com.client.ui.AddPage;
import com.util.JsonUtil;

public class ResAddFriendHandler extends AbstractResponseHandler {

	public ResAddFriendHandler(Socket socket) {
		super(socket);
	}
	
	private AddPage addPage;
	
	@Override
	public void handleResponse() {
		JSONObject json = (JSONObject) JsonUtil.parseJson(responseMsg, "res");
		String userName = json.getString("publisher");
		log.info("username = ======" + userName);
		log.info("接收的响应消息  " +  super.responseMsg);
		addPage = AddPage.getInstance(socket , userName);
		
		addPage.isAdd(super.responseMsg);
	}

	
}
