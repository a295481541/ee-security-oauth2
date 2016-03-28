package com.eenet.test.env;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboBizConsumerENV {
	private static DubboBizConsumerENV INSTANCE;
	private static ApplicationContext context;
	
	public static DubboBizConsumerENV getInstance() {
		if (INSTANCE == null)
			INSTANCE = new DubboBizConsumerENV();
		return INSTANCE;
	}
	
	public ApplicationContext getContext() {
		if (DubboBizConsumerENV.context == null)
			this.initEnvironment();
		return context;
	}
	
	private void initEnvironment(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-biz-consumer.xml" });
		context.start();
		DubboBizConsumerENV.context = context;
	}
	
	private DubboBizConsumerENV() {
	}
}
