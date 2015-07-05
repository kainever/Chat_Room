package com.server.handler;

import java.io.IOException;

import com.server.user.User;
import com.transmit.protocol.Message;
import com.util.MsgKey;

/**
 * 服务端登录逻辑处理器
 * @author slave_1
 */
public class LoginHandler extends AbstractRequestHandler {

	public LoginHandler() throws IOException {
		super();
	}


	/* (non-Javadoc)
	 *  登录逻辑：
	 *  1. 验证用户名密码是否正确 
	 *  2. 如果不正确 移除userMap;还不能在这里移除 得通过它写值给客户端
	 *  3. 写入消息到响应队列 
	 * @see com.server.handler.AbstractRequestHandler#handleRequest(com.transmit.protocol.Message)
	 */
	@Override
	public void handleRequest(Message msg) {
		log.info("loginHandler 处理消息.." + msg );
		String words = (String) msg.getWords();
		String filePort = msg.getFilePort();
		String[] ss = words.split("_");
		String content = "";
		log.info("name = " + ss[0] + " password = " + ss[1]);
		if(userService.check(ss[0] , ss[1])) {
			content = "true"; // 说明可以登录
//			更新ip地址和在线状态
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
		
//		包装返回信息
		Message res = new Message();
		res.setMsgNum(MsgKey.LOGIN);
		res.setReceiverIP(msg.getReceiverIP());
		res.setReceiverPort(msg.getReceiverPort());
		res.setFilePort(filePort);
		res.setReceiver(ss[0]);
		res.setWords(content);
		
		log.info(this.getClass().getName() + " 返回结果 = "  + res + " 添加到返回队列");
		handler.addResponse(res);
	}

	

}
