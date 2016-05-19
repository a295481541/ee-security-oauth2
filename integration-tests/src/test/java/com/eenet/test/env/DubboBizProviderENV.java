package com.eenet.test.env;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboBizProviderENV {
	static ClassPathXmlApplicationContext context;
	
	@BeforeClass
	public static void initServiceProvider() {
		context = new ClassPathXmlApplicationContext("dubbo-biz-provider.xml");
		context.start();
	}
	
	@AfterClass
	public static void stopServiceProvider() {
		if (context != null) {
			context.stop();
			context.close();
			context = null;
		}
	}
}
