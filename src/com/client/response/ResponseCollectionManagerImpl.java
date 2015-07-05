package com.client.response;

/**
 * @author slave_1
 * ����Ϣ���н��й���
 * @see ResponseCollectionManager
 */
public class ResponseCollectionManagerImpl implements ResponseCollectionManager{
	
	private ResponseMsgCollection msgQueue;
	
	/**
	 *  �õ�msgQueue ��Ϣ����
	 */
	public ResponseCollectionManagerImpl(){
		msgQueue = ResponseMsgQueue.getInstance();
	}
	
	public  void addMsg(String msg) {
		msgQueue.addMsg(msg); 
	}

	public  String getMsg() {
		return msgQueue.getMsg();
	}
}
