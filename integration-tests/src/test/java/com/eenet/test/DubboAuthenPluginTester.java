package com.eenet.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.eenet.authen.RegistrationBizService;
import com.eenet.authen.ServiceConsumer;
import com.eenet.base.SimpleResultSet;
import com.eenet.common.exception.AuthenException;
import com.eenet.test.bizmock.MockBizService;
import com.eenet.test.bizmock.Person;
import com.eenet.test.env.DubboAuthenConsumerENV;
import com.eenet.test.env.DubboBizConsumerENV;
import com.eenet.test.env.DubboBizProviderENV;

public class DubboAuthenPluginTester extends DubboBizProviderENV {
	/**
	 * 必须已经注册了服务消费者：
	 * code:b90680abcf854943b0a12ca23830ea00
	 * secretKey:ABB76B8CA8F54D4388513024DCB8A340
	 * 2016年3月29日
	 * @author Orion
	 */
	@Test
	public void test() {
		ApplicationContext context = DubboBizConsumerENV.getInstance().getContext();
		MockBizService mockService = (MockBizService)context.getBean("MockBizService");
		
		/* 认证通过 */
		SimpleResultSet<Person> result = mockService.query(null);
		assertTrue(result.isSuccessful());
		assertEquals(3,result.getResultSet().size());//该断言与业务实现类关联
		
		/* 修改成错误密码 */
		ServiceConsumer consumer = (ServiceConsumer)context.getBean("ConsumerIdentity");
		consumer.setSecretKey("1111111");
		
		/* 认证不通过，错误信息在返回对象中 */
		SimpleResultSet<Person> result2 = mockService.query(null);
		assertFalse(result2.isSuccessful());
		assertEquals(0,result2.getResultSet().size());//该断言与业务实现类关联
		System.err.println(result2.getStrMessage());
		
		/* 认证不通过，抛异常 */
		String result3 = null;
		try {
			result3 = mockService.withoutIBaseResponse("mouse");
		} catch (AuthenException ex){
			ex.printStackTrace();
		}
		assertNull(result3);
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
