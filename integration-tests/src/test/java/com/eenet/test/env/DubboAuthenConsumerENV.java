package com.eenet.test.env;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboAuthenConsumerENV {
	private static DubboAuthenConsumerENV INSTANCE;
	private static ApplicationContext context;

	public static DubboAuthenConsumerENV getInstance() {
		if (INSTANCE == null)
			INSTANCE = new DubboAuthenConsumerENV();
		return INSTANCE;
	}

	public ApplicationContext getContext() {
		if (DubboAuthenConsumerENV.context == null)
			this.initEnvironment();
		return context;
	}

	private void initEnvironment(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-authen-consumer.xml" });
		context.start();
		DubboAuthenConsumerENV.context = context;
	}

	private DubboAuthenConsumerENV() {
	}
}
