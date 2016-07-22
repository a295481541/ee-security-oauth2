package com.eenet.test.env;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringEnvironment {
	private static SpringEnvironment INSTANCE;
	private static ClassPathXmlApplicationContext context;
	
	public static SpringEnvironment getInstance() {
		if (INSTANCE == null)
			INSTANCE = new SpringEnvironment();
		return INSTANCE;
	}
	
	public ApplicationContext getContext() {
		if (SpringEnvironment.context == null)
			initEnvironment();
		return context;
	}
	
	@BeforeClass
	public static void initEnvironment() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
			"global-config.xml",
			"rdbms-dataSource.xml",
			"redis.xml",
			"transaction.xml",
			"user-service.xml",
			"authen-service.xml",
			"security-service.xml"
		});
		context.start();
		SpringEnvironment.context = context;
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
