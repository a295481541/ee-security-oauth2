package com.eenet.test.env;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboBizConsumerENV {
	private static DubboBizConsumerENV INSTANCE;
	private static ClassPathXmlApplicationContext context;
	private boolean providerStartByMe = false;
	
	public static DubboBizConsumerENV getInstance() {
		if (INSTANCE == null)
			INSTANCE = new DubboBizConsumerENV();
		return INSTANCE;
	}
	
	public ApplicationContext getContext() {
		if (DubboBizConsumerENV.context == null)
			initServiceConsumer();
		return context;
	}
	
	@BeforeClass
	public static void initServiceConsumer(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-biz-consumer.xml" });
		context.start();
		DubboBizConsumerENV.context = context;
	}
	
	@AfterClass
	public static void stopServiceConsumer() {
		if (context != null) {
			context.stop();
			context.close();
			context = null;
		}
	}
}
