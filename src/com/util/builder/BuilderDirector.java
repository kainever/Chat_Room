package com.util.builder;

import java.io.IOException;

/**
 * 指导者，用于指导生产所需的builder类对象
 * 为什么要这么做了？
 *  ---如果以后要改写 写入写出流 , 是不是不就不要到LoginPage 这些页面中去了！
 *  --- 将可能变化的地方抽取出来...
 * @author slave_1
 */
public class BuilderDirector {

	private AbstractBuilder abstractBuilder;
	
	public BuilderDirector(){
		abstractBuilder = null;
	}
	
	public BuilderDirector(AbstractBuilder abstractBuilder){
		this.abstractBuilder = abstractBuilder;
	}
	
	/**
	 * 构建相应的builder对象
	 * @return
	 * @throws IOException
	 */
	public Object construct() throws IOException{
		return abstractBuilder.getResult();
	}
}
