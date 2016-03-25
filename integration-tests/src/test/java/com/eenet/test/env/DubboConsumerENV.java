package com.eenet.test.env;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboConsumerENV {
	private static DubboConsumerENV INSTANCE;
	private ApplicationContext context;

	public static DubboConsumerENV getInstance() {
		if (INSTANCE == null)
			INSTANCE = new DubboConsumerENV();
		return INSTANCE;
	}

	public ApplicationContext getContext() {
		if (this.context == null)
			this.initEnvironment();
		return context;
	}

	private void initEnvironment() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-consumer.xml" });
		context.start();
		this.context = context;
	}

	private DubboConsumerENV() {
	}
}
