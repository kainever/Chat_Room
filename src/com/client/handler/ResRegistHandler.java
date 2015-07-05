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
		log.info("ע��������." + super.responseMsg);
		registPage = RegistPage.getInstance(super.socket);
		//˫����������registPage��ע���־
		registPage.regist(super.responseMsg);
	}

}
