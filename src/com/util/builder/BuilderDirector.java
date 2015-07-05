package com.util.builder;

import java.io.IOException;

/**
 * ָ���ߣ�����ָ�����������builder�����
 * ΪʲôҪ��ô���ˣ�
 *  ---����Ժ�Ҫ��д д��д���� , �ǲ��ǲ��Ͳ�Ҫ��LoginPage ��Щҳ����ȥ�ˣ�
 *  --- �����ܱ仯�ĵط���ȡ����...
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
	 * ������Ӧ��builder����
	 * @return
	 * @throws IOException
	 */
	public Object construct() throws IOException{
		return abstractBuilder.getResult();
	}
}
