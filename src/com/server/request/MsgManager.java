package com.server.request;

import org.apache.log4j.Logger;

import com.transmit.protocol.Message;

//Ϊ��ʹ����־�����࣬���뽫ʵ����ת��Ϊ������
public interface MsgManager {
	public static Logger log = Logger.getLogger(MsgManager.class);
	
	public void addMsg(Message msg);

	public Message getMsg();
}
