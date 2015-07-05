package com.client.handler;

import java.net.Socket;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.client.ui.LoginPage;
import com.util.JsonUtil;

/**
 * 客户端对登录响应的处理器
 * @author slave_1
 */
public class ResLoginHandler extends AbstractResponseHandler {

	public static Logger log = Logger.getLogger(ResLoginHandler.class);
	
	public ResLoginHandler(Socket socket) {
		super(socket);
	}

	private LoginPage loginPage;

	private String loginFlagTemp;

	public void updateLoginFlag() {
		loginPage.setLoginFlag(loginFlagTemp);
	}

	@Override
	public void handleResponse() {
		
		log.info("正在进行客户端登陆响应操作.." + super.responseMsg);
		JSONObject json = (JSONObject) JsonUtil.parseJson(super.responseMsg, "res");
		String loginUserName = json.getString("receiver");
		loginPage = LoginPage.getInstance(super.socket);
		loginPage.setUserName(loginUserName);

		loginFlagTemp = super.responseMsg;

		// 双向依赖更新loginPage的登录标志
		loginPage.login();
	}

}
