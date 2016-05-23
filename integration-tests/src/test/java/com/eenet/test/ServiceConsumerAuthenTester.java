package com.eenet.test;

import java.util.UUID;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.alibaba.dubbo.rpc.RpcException;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.ServiceAuthenRequest;
import com.eenet.authen.ServiceAuthenResponse;
import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.ServiceConsumerBizService;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.DubboAuthenConsumerENV;
import static org.junit.Assert.*;

public class ServiceConsumerAuthenTester {
	
	/**
	 * 测试流程：
	 * |
	 * | 注册服务消费者 - 预期成功
	 * | 服务消费者认证 - 预期成功
	 * | 服务消费者认证 - 预期失败 ：错误密码
	 * | 删除服务消费者 - 预期成功
	 * | 服务消费者认证 - 预期失败：消费者不存在（在上一步已被删除）
	 * |
	 * V
	 * 
	 * 2016年5月16日
	 * @author Orion
	 */
	@Test
	public void test(){
		ApplicationContext context = DubboAuthenConsumerENV.getInstance().getContext();
		ServiceConsumerBizService regisService = (ServiceConsumerBizService)context.getBean("ServiceConsumerBizService");
		IdentityAuthenticationBizService authenService = (IdentityAuthenticationBizService)context.getBean("IdentityAuthenticationBizService");
		
		System.out.println("----------------- service is ok ?"+authenService.authenServiceProviderPing()+"--------------------------");
		
		/* 注册消费者客户端 */
		ServiceConsumer consumer = new ServiceConsumer();
		consumer.setConsumerName("服务消费者-测试-"+UUID.randomUUID().toString().substring(0, 20));
		consumer.setSecretKey("123456");
		ServiceConsumer savedConsumer = null;
		try {
			savedConsumer = regisService.registeServiceConsumer(consumer);
		} catch (RpcException ex) {
			System.err.println(ex.getMessage());
			if (ex.getMessage().indexOf("Please check if the providers have been started and registered") != -1) {
				System.err.println("没有服务提供者，测试退出");
				return;
			}
		}
		assertTrue(savedConsumer.isSuccessful());
		System.out.println("saved consumer code : "+savedConsumer.getCode());
		
		/* 认证成功 */
		ServiceAuthenRequest request = new ServiceAuthenRequest();
		request.setConsumerCode(savedConsumer.getCode());
		request.setConsumerSecretKey("123456");
		ServiceAuthenResponse responseFirst = null;
		try {
			responseFirst = authenService.consumerAuthen(request);
		} catch (RpcException ex) {
			System.err.println(ex.getMessage());
			if (ex.getMessage().indexOf("Please check if the providers have been started and registered") != -1) {
				System.err.println("没有服务提供者，测试退出");
				return;
			}
		}
		assertTrue(responseFirst.isIdentityConfirm());
		System.out.println("authen first : "+responseFirst.isIdentityConfirm());
		
		/* 认证失败 */
		request.setConsumerSecretKey("654321");
		ServiceAuthenResponse responseSecond = null;
		try {
			responseSecond = authenService.consumerAuthen(request);
		} catch (RpcException ex) {
			System.err.println(ex.getMessage());
			if (ex.getMessage().indexOf("Please check if the providers have been started and registered") != -1) {
				System.err.println("没有服务提供者，测试退出");
				return;
			}
		}
		assertFalse(responseSecond.isIdentityConfirm());
		System.out.println("authen second : "+responseSecond.isIdentityConfirm());
		
		/* 删除数据 */
		SimpleResponse delResult = regisService.removeServiceConsumer(savedConsumer.getCode());
		if (!delResult.isSuccessful()) {
			System.out.println(delResult.getStrMessage());
		}
		assertTrue(delResult.isSuccessful());
		
		/* 认证失败（数据已被删除） */
		request.setConsumerSecretKey("123456");
		ServiceAuthenResponse responseThird = null;
		try {
			responseThird = authenService.consumerAuthen(request);
		} catch (RpcException ex) {
			System.err.println(ex.getMessage());
			if (ex.getMessage().indexOf("Please check if the providers have been started and registered") != -1) {
				System.err.println("没有服务提供者，测试退出");
				return;
			}
		}
		assertFalse(responseThird.isIdentityConfirm());
		System.out.println("authen second : "+responseThird.isIdentityConfirm());
	}
}
