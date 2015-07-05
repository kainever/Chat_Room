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

	// 提供给homePage使用的方法，双向依赖，called
	public void updateFriendListStr() {
		homePage.setFriendListStr(friendListJsonTemp);
	}

	@Override
	public void handleResponse() {
		System.out.println("刷新好友列表");
		
		try {
			homePage = HomePage.getInstance(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		friendListJsonTemp = super.responseMsg;

		// 通知homePage更新好友列表
		homePage.updateFriendList();

	}

}
