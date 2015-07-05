package com.server.request;

import org.apache.log4j.Logger;

import com.transmit.protocol.Message;

public abstract class MsgCollection {
	
	public static Logger log = Logger.getLogger(MsgCollection.class);

	public abstract void addMsg(Message msg);
	
	public abstract Message getMsg();
}
