package com.server.handler;

import java.io.IOException;

import com.server.user.User;
import com.transmit.protocol.Message;
import com.util.MsgKey;

/**
 * ����˵�¼�߼�������
 * @author slave_1
 */
public class LoginHandler extends AbstractRequestHandler {

	public LoginHandler() throws IOException {
		super();
	}


	/* (non-Javadoc)
	 *  ��¼�߼���
	 *  1. ��֤�û��������Ƿ���ȷ 
	 *  2. �������ȷ �Ƴ�userMap;�������������Ƴ� ��ͨ����дֵ���ͻ���
	 *  3. д����Ϣ����Ӧ���� 
	 * @see com.server.handler.AbstractRequestHandler#handleRequest(com.transmit.protocol.Message)
	 */
	@Override
	public void handleRequest(Message msg) {
		log.info("loginHandler ������Ϣ.." + msg );
		String words = (String) msg.getWords();
		String filePort = msg.getFilePort();
		String[] ss = words.split("_");
		String content = "";
		log.info("name = " + ss[0] + " password = " + ss[1]);
		if(userService.check(ss[0] , ss[1])) {
			content = "true"; // ˵�����Ե�¼
//			����ip��ַ������״̬
			User u = new User();
			u.setIp(msg.getReceiverIP());
			u.setPort(msg.getReceiverPort());
			u.setName(ss[0]);
			u.setOnline(true);
			u.setFilePort(filePort);
			userService.updateAddress(u);
			userService.updateOnline(u);
		} else {
//			String kv = msg.getReceiverIP() + ":" + msg.getReceiverPort();
//			UserMap.remove(kv);
			content = "false";
		}
		
//		��װ������Ϣ
		Message res = new Message();
		res.setMsgNum(MsgKey.LOGIN);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		res.setFilePort(filePort);
		res.setReceiver(ss[0]);
		res.setWords(content);
		
		log.info(this.getClass().getName() + " ���ؽ�� = "  + res + " ��ӵ����ض���");
		handler.addResponse(res);
	}

	

}
