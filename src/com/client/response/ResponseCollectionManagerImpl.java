package com.client.response;

/**
 * @author slave_1
 * 对消息队列进行管理
 * @see ResponseCollectionManager
 */
public class ResponseCollectionManagerImpl implements ResponseCollectionManager{
	
	private ResponseMsgCollection msgQueue;
	
	/**
	 *  得到msgQueue 消息队列
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
