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
log.info("�ȵ������б�.." + friends);
		JSONArray json1 = JSONArray.fromObject(friends);
		String userFriends = JsonUtil.buildJson("friends", json1);
log.info("��ʽ����json����.." + userFriends);
		Message res = new Message();
		res.setMsgNum(MsgKey.DISPLAY_FRIEND);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		res.setWords(userFriends);
		
		String result = res.getResult();
		
		String output = JsonUtil.buildJson("res", result);
	log.info("д��ͻ���  " + output);	
		log.info("loginHandler ���ؽ�� = "  + res + " ��ӵ����ض���");
		handler.addResponse(res);
	}

}
