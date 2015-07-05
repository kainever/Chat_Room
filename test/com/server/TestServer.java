package com.server;

import java.io.IOException;

import com.server.startup.Server;

public class TestServer {

	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.runServer();
		
	}

}
