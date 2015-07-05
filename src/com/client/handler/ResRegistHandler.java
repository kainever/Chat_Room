package com.client.handler;

import java.net.Socket;

import com.client.ui.RegistPage;

public class ResRegistHandler extends AbstractResponseHandler{

	public ResRegistHandler(Socket socket) {
		super(socket);
	}

	private RegistPage registPage;
	
	@Override
	public void handleResponse() {
		log.info("注册结果返回." + super.responseMsg);
		registPage = RegistPage.getInstance(super.socket);
		//双向依赖更新registPage的注册标志
		registPage.regist(super.responseMsg);
	}

}
