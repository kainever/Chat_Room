package com.transmit.protocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.util.JsonUtil;

import net.sf.json.JSONObject;

/**
 * 客户端与服务端之间传递消息的格式
 * @author slave_1
 */
public class Message {

	private String publisher;
	private String msgNum;
	private String receiver;
	private Object words;
	private String receiverIP;
	private String receiverPort;
	private String selfIp;
	private String selfPort;
	
	/**
	 * 客户端用于文件传输的端口
	 */
	private String filePort;

	public Message(){
	}
	
	
	
	public String getPublisher() {
		return publisher;
	}



	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}



	public String getMsgNum() {
		return msgNum;
	}



	public void setMsgNum(String msgNum) {
		this.msgNum = msgNum;
	}



	public String getReceiver() {
		return receiver;
	}



	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}



	public Object getWords() {
		return words;
	}



	public void setWords(String words) {
		this.words = words;
	}



	public String getReceiverIP() {
		return receiverIP;
	}



	public void setReceiverIP(String receiverIP) {
		this.receiverIP = receiverIP;
	}



	public String getReceiverPort() {
		return receiverPort;
	}



	public void setReceiverPort(String receiverPort) {
		this.receiverPort = receiverPort;
	}



	/**
	 * 将Message对象拼接成json形式的字符串
	 * @return
	 */
	public String getResult() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		Method[] methods = this.getClass().getMethods();
		boolean first = true;
		for(int i = 0 ; i < methods.length ; i++) {
			String name = methods[i].getName();
			if(name.startsWith("get") && !name.equals("getClass") 
					&& !name.equals("getResult")) {
				String fieldName = name.substring(3);
				String field = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
				if(first) {
					sb.append("\'" + field+"\':");
					first = false;
				} else {
					sb.append(",\'" + field+"\':");
				}
				try {
					Object value =  methods[i].invoke(this);
					sb.append("'" + value + "'");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		sb.append("}");

		return sb.toString();
	}



	/**
	 * 根据json对象构建Message对象
	 * @param json
	 * @return
	 */
	public static Message build(JSONObject json) {
		Message m = new Message();
		m.msgNum = (String) json.get("msgNum");
		m.publisher = (String) json.get("publisher");
		m.receiver = (String) json.get("receiver");
		m.receiverIP = (String) json.get("receiverIP");
		m.receiverPort = (String) json.get("receiverPort");
		m.words = json.get("words");
		m.selfIp = (String) json.get("selfIp");
		m.selfPort = (String) json.get("selfPort");
		m.filePort = (String) json.get("filePort");
		return m;
	}
	



	@Override
	public String toString() {
		return "Message [publisher=" + publisher + ", msgNum=" + msgNum
				+ ", receiver=" + receiver + ", words=" + words
				+ ", receiverIP=" + receiverIP + ", receiverPort="
				+ receiverPort + ", selfIp=" + selfIp + ", selfPort="
				+ selfPort + "]";
	}



	public static void main(String[] args) {
		Message m = new Message();
		m.setMsgNum("1");
		m.setReceiver("hk");
		m.setPublisher("cj");
		m.setReceiverIP("127");
		m.setReceiverPort("25");
		m.setWords("are you ao");
//		System.out.println(m.getResult());
//		String s = m.getResult();
//		System.out.println(JsonUtil.parseJson(s, "msgNum"));
		
	}



	public String getSelfIp() {
		return selfIp;
	}



	public void setSelfIp(String selfIp) {
		this.selfIp = selfIp;
	}



	public String getSelfPort() {
		return selfPort;
	}



	public void setSelfPort(String selfPort) {
		this.selfPort = selfPort;
	}



	public String getFilePort() {
		return filePort;
	}



	public void setFilePort(String filePort) {
		this.filePort = filePort;
	}
}
