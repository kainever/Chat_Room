package com.client.handler;

import java.io.IOException;
import java.net.Socket;

import com.client.ui.HomePage;

public class ResDisplayFriendsHandler extends AbstractResponseHandler {

	public ResDisplayFriendsHandler(Socket socket) {
		super(socket);
	}

	private HomePage homePage;

	private String friendListJsonTemp;

	// �ṩ��homePageʹ�õķ�����˫��������called
	public void updateFriendListStr() {
		homePage.setFriendListStr(friendListJsonTemp);
	}

	@Override
	public void handleResponse() {
		System.out.println("ˢ�º����б�");
		
		try {
			homePage = HomePage.getInstance(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		friendListJsonTemp = super.responseMsg;

		// ֪ͨhomePage���º����б�
		homePage.updateFriendList();

	}

}
