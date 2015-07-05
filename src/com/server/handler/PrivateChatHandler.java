package com.server.handler;

import java.io.IOException;

import com.transmit.protocol.Message;
import com.util.MsgKey;


public class PrivateChatHandler extends AbstractRequestHandler {

	public PrivateChatHandler() throws IOException {
		super();
	}


	// ת���߼�....�о�ʲôҲû��...
	@Override
	public void handleRequest(Message msg) {
		String name = msg.getPublisher();
		String fName = msg.getReceiver();
		String words = msg.getWords();
		String content = words;
log.info("get ������Ϣ = " + content);
		Message res = new Message();
		res.setMsgNum(MsgKey.PRIVATE_CHAT);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		res.setSelfIp(msg.getSelfIp());
		res.setSelfPort(msg.getSelfPort());
		res.setWords(content);
		res.setPublisher(name);
		res.setReceiver(fName);
		
		log.info(this.getClass().getName() + "���ؽ�� = "  + res + " ��ӵ����ض���");
		handler.addResponse(res);
	}
}
