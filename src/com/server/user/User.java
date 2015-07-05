package com.server.user;

/**
 * 用户实体类
 * @author slave_1
 */
public class User {
	int id;
	private String name;
	private String password;
	private String ip;
	private String port;
	private boolean isOnline;
	private String filePort;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	@Override
	public String toString() {
		return name;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public String getFilePort() {
		return filePort;
	}
	public void setFilePort(String filePort) {
		this.filePort = filePort;
	}
}
