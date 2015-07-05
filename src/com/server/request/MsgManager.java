package com.server.request;

import org.apache.log4j.Logger;

import com.transmit.protocol.Message;

//为了使用日志代理类，必须将实现类转化为抽象类
public interface MsgManager {
	public static Logger log = Logger.getLogger(MsgManager.class);
	
	public void addMsg(Message msg);

	public Message getMsg();
}
