package com.util.builder;

import java.io.IOException;


/**
 * ��������
 * @author slave_1
 */
public abstract class AbstractBuilder {

	public AbstractBuilder(){}
	protected abstract Object getResult() throws IOException;
}
