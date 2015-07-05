package com.server.request;

import com.transmit.protocol.Message;

public class MsgManagerImpl implements MsgManager{

	private MsgCollection msgQueue;
	
	public MsgManagerImpl(){
		msgQueue = MsgQueue.getInstance();
	}
	

	public  Message getMsg() {
		return msgQueue.getMsg();
	}


	@Override
	public void addMsg(Message msg) {
		msgQueue.addMsg(msg);
		log.info("服务端消息队列加入msg = " + msg.getResult());
	}
}
