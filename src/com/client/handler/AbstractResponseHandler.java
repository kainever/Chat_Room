package com.client.handler;

import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * �ͻ���handler�ĳ������
 * ����ģʽ��ʹ��
 * @author slave_1
 */
public abstract class AbstractResponseHandler {
	
	public static final Logger log = Logger.getLogger(AbstractResponseHandler.class);

	protected String responseMsg;
	protected Socket socket;
	
	public AbstractResponseHandler(Socket socket){
		this.socket = socket;
	}
	
	public void setResponseMsg(String msg) {
		this.responseMsg = msg;
	}

	/**
	 * ��ͬ���¼���Ӧ�Ų�ͬ�Ĵ�����
	 */
	public abstract void handleResponse();
}
