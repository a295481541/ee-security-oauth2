package com.eenet.test.env;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboAuthenConsumerENV {
	private static DubboAuthenConsumerENV INSTANCE;
	private static ClassPathXmlApplicationContext context;

	public static DubboAuthenConsumerENV getInstance() {
		if (INSTANCE == null)
			INSTANCE = new DubboAuthenConsumerENV();
		return INSTANCE;
	}

	public ApplicationContext getContext() {
		if (DubboAuthenConsumerENV.context == null)
			DubboAuthenConsumerENV.initSSOSystem();
		return context;
	}
	
	@BeforeClass
	public static void initSSOSystem(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-authen-consumer.xml" });
		context.start();
		DubboAuthenConsumerENV.context = context;
	}
	
	@AfterClass
	public static void stopSSOSystem() {
		if (context != null) {
			context.stop();
			context.close();
			context = null;
		}
	}
}
