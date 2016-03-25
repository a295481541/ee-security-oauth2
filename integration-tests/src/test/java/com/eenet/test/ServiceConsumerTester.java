package com.eenet.test;

import java.util.UUID;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.RegistrationBizService;
import com.eenet.authen.ServiceAuthenRequest;
import com.eenet.authen.ServiceAuthenResponse;
import com.eenet.authen.ServiceConsumer;
import com.eenet.test.env.DubboConsumerENV;
import static org.junit.Assert.*;

public class ServiceConsumerTester {
	
	@Test
	public void test(){
		ApplicationContext context = DubboConsumerENV.getInstance().getContext();
		RegistrationBizService service = (RegistrationBizService)context.getBean("RegistrationBizService");
		IdentityAuthenticationBizService authenService = (IdentityAuthenticationBizService)context.getBean("IdentityAuthenticationBizService");
		
		/* 注册消费者客户端 */
		ServiceConsumer consumer = new ServiceConsumer();
		consumer.setConsumerName("服务消费者-测试-"+UUID.randomUUID().toString().substring(0, 20));
		consumer.setSecretKey("123456");
		ServiceConsumer savedConsumer = service.serviceConsumerRegiste(consumer);
		System.out.println(savedConsumer.getCode());
		
		/* 认证成功 */
		ServiceAuthenRequest request = new ServiceAuthenRequest();
		request.setConsumerCode(savedConsumer.getCode());
		request.setConsumerSecretKey("123456");
		ServiceAuthenResponse response = authenService.consumerAuthen(request);
		assertTrue(response.isIdentityConfirm());
		System.out.println("authen first : "+response.isIdentityConfirm());
		
		/* 认证失败 */
		request.setConsumerSecretKey("654321");
		response = authenService.consumerAuthen(request);
		assertFalse(response.isIdentityConfirm());
		System.out.println("authen second : "+response.isIdentityConfirm());
	}
}
