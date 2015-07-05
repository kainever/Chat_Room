package com.server.handler;

import java.io.IOException;

import com.server.user.User;
import com.transmit.protocol.Message;
import com.util.MsgKey;

public class RegistHandler extends AbstractRequestHandler{


	public RegistHandler() throws IOException {
		super();
	}

	@Override
	public void handleRequest(Message msg) {
		String words = (String) msg.getWords();
		String[] ss = words.split("_");
		Message res = new Message();
		String content = "";
		if(userService.check(ss[0])) {
			User u = new User();
			u.setName(ss[0]);
			u.setPassword(ss[1]);
			u.setOnline(false);
			userService.createUser(u);
			res.setReceiver(u.getName()+"_"+u.getPassword());
			content = "true";
		} else {
			content = "false";
		}
		
		res.setMsgNum(MsgKey.REGISTER);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		res.setWords(content);
		
		log.info(this.getClass().getName() + "返回结果 = "  + res + " 添加到返回队列");
		handler.addResponse(res);
	}

}
