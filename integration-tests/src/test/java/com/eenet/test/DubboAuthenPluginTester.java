package com.eenet.test;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.eenet.authen.RegistrationBizService;
import com.eenet.authen.ServiceConsumer;
import com.eenet.base.SimpleResultSet;
import com.eenet.test.bizmock.MockBizService;
import com.eenet.test.bizmock.Person;
import com.eenet.test.env.DubboAuthenConsumerENV;
import com.eenet.test.env.DubboBizConsumerENV;
import com.eenet.test.env.DubboBizProviderENV;

@Component
public class DubboAuthenPluginTester extends DubboBizProviderENV {
	
	@Test
	public void test() {
		ApplicationContext context = DubboBizConsumerENV.getInstance().getContext();
		MockBizService mockService = (MockBizService)context.getBean("MockBizService");
		
		SimpleResultSet<Person> result = mockService.query(null);
		for (Person p : result.getResultSet()) {
			System.out.println("======="+p.getFirstName()+"=======");
		}
	}
	
//	@Test
	public void createConsumer() {
		/* 消费者身份注册 */
		ApplicationContext context = DubboAuthenConsumerENV.getInstance().getContext();
		RegistrationBizService regisService = (RegistrationBizService)context.getBean("RegistrationBizService");
		ServiceConsumer consumer = new ServiceConsumer();
		consumer.setConsumerName("单元测试用户");
		consumer.setSecretKey("abb76b8ca8f54d4388513024dcb8a340".toUpperCase());
		System.out.println("密码："+consumer.getSecretKey());
		ServiceConsumer savedConsumer = regisService.serviceConsumerRegiste(consumer);
		System.out.println("用户名："+savedConsumer.getCode());
		/* 消费者身份删除 */
	}
}
