package eenet.test.security.consumer.env;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringEnvironment {
	private static SpringEnvironment instance;
	private static ClassPathXmlApplicationContext context;
	
	public static SpringEnvironment getInstance() {
		if (instance == null)
			instance = new SpringEnvironment();
		return instance;
	}
	
	/**
	 * 
	 * @param envTag test-测试环境（默认），dev-开发环境
	 * @return
	 * 2017年1月16日
	 * @author Orion
	 */
	public ApplicationContext getContext() {
		if (SpringEnvironment.context == null)
			initEnvironment();
		return context;
	}
	
	@BeforeClass
	public static void initEnvironment() {
		if (SpringEnvironment.context != null)
			return;
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
			"applicationContext.xml"
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
