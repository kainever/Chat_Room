package com.server.handler;

import java.io.IOException;

import net.sf.json.JSONObject;
//import Server.UserCollection.UserDB;


import com.server.user.User;
import com.server.user.UserMap;
import com.transmit.protocol.Message;


public class LogoutHandler extends AbstractRequestHandler {

	public LogoutHandler() throws IOException {
		super();
	}

	@Override
	public void handleRequest(Message msg) {
		String name = msg.getPublisher();
		log.info(name + " 用户下线了...");
		User user = new User();
		user.setIp(null);
		user.setPort(null);
		user.setName(name);
		userService.updateAddress(user);
		userService.updateOnline(user);
		UserMap.remove(msg.getReceiverIP() + ":" + msg.getReceiverPort());
	}
}
