package com.client.response;


/**
 * @author slave_1
 * ��Ϣ���г����࣡AbstractResponseMsgQueue
 * �����ĵĻ� �����������������ʵ����Ϣ���е�ʱ�� ��չ�Բ��͸���
 * ���������������...
 */
public abstract class ResponseMsgCollection {
	
	public abstract void addMsg(String msg);
	
	public abstract String getMsg();
}
