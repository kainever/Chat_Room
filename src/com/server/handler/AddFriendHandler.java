package com.server.handler;

import java.io.IOException;

import net.sf.json.JSONObject;

import com.server.user.User;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;

public class AddFriendHandler extends AbstractRequestHandler {

	public AddFriendHandler() throws IOException {
		super();
	}

	
	@Override
	public void handleRequest(Message msg) {
		String flag = msg.getWords();
		Message res = new Message();
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		res.setPublisher(msg.getPublisher());
		if(flag.equals("false")) {
			User u = userService.getUserByName(msg.getReceiver());
			String content = "";
			if(u == null) {
				content = "false";
			} else {
				content = "true";
				res.setReceiver(msg.getReceiver());
			}
			res.setMsgNum(MsgKey.ADD_FRIEND);
			res.setWords(content);
		} else if (flag.equals("true")){
			String userName = msg.getPublisher();
			String fName = msg.getReceiver();
			userService.addFriend(userName,fName);
			res.setMsgNum(MsgKey.ADD_FRIEND);
			res.setWords("add");
		}
		
		log.info(this.getClass().getName() + "返回结果 = "  + res + " 添加到返回队列");
		handler.addResponse(res);
	}
	
}
