package com.server.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import net.sf.json.JSONArray;

import com.server.user.User;
import com.transmit.protocol.Message;
import com.util.JsonUtil;
import com.util.MsgKey;

public class DisplayFriendsHandler extends AbstractRequestHandler {

	public DisplayFriendsHandler() throws IOException {
		super();
	}


	@Override
	public void handleRequest(Message msg) {
		
		List<User> friends = userService.getFriends(msg.getPublisher());
log.info("等到朋友列表.." + friends);
		JSONArray json1 = JSONArray.fromObject(friends);
		String userFriends = JsonUtil.buildJson("friends", json1);
log.info("格式化成json类型.." + userFriends);
		Message res = new Message();
		res.setMsgNum(MsgKey.DISPLAY_FRIEND);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		res.setWords(userFriends);
		
		String result = res.getResult();
		
		String output = JsonUtil.buildJson("res", result);
	log.info("写入客户端  " + output);	
		log.info("loginHandler 返回结果 = "  + res + " 添加到返回队列");
		handler.addResponse(res);
	}

}
