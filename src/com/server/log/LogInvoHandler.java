package com.server.log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;

//log invocation handler ��־���ô���
public class LogInvoHandler implements InvocationHandler {
	// ��ȡ��־
	private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	private Object target; // ����Ŀ��
	private Object proxy; // �������

	// ʹ��map���洢��ͬ��InvocationHandler���󣬱������ɹ���
	private static HashMap<Class<?>, LogInvoHandler> invoHandlers = new HashMap<Class<?>, LogInvoHandler>();

	private LogInvoHandler() {

	}

	//ͨ��Class�����ɶ�̬�������Proxy
	@SuppressWarnings("unchecked")
	public synchronized static <T> T getProxyInstance(Class<T> clazz) {
		// ����map�в����ظ�����
		LogInvoHandler invoHandler = invoHandlers.get(clazz);

		if (null == invoHandler) {
			invoHandler = new LogInvoHandler();

			try {
				T tar = clazz.newInstance();//

				invoHandler.setTarget(tar);

				invoHandler.setProxy(Proxy.newProxyInstance(tar.getClass()
						.getClassLoader(), tar.getClass().getInterfaces(),
						invoHandler));

			} catch (Exception e) {
				e.printStackTrace();
			}
			// �½��ķ���map
			invoHandlers.put(clazz, invoHandler);
		}

		return (T) invoHandler.getProxy();
	}

	// ʵ�ֵĽӿ�
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = method.invoke(target, args); // ִ��ҵ����

		// ��ӡ��־
		logger.info("____invoke method: " + method.getName() + "; args: "
				+ (null == args ? "null" : Arrays.asList(args).toString())
				+ "; return: " + result);

		return result;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public Object getProxy() {
		return proxy;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}
}