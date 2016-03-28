package com.eenet.extension.dubbo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.eenet.authen.CommonKey;
import com.eenet.authen.ServiceConsumer;
import com.eenet.common.exception.AuthenException;

/**
 * 服务消费者身份注入
 * 2016年3月25日
 * @author Orion
 */

public class ConsumerInjectFilter implements Filter,ApplicationContextAware {
	private static ApplicationContext applicationContext;
	
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		if (ConsumerInjectFilter.applicationContext==null || !ConsumerInjectFilter.applicationContext.containsBean("ConsumerIdentity"))
			throw new AuthenException("未获得服务消费者身份信息：请检查配置文件");
		
		Result result = null;
		ServiceConsumer ConsumerIdentity = (ServiceConsumer) ConsumerInjectFilter.applicationContext.getBean("ConsumerIdentity");
		/* 调用业务服务前 */
		invocation.getAttachments().put(CommonKey.SERVICE_CONSUMER_CODE, ConsumerIdentity.getCode());
		invocation.getAttachments().put(CommonKey.SERVICE_CONSUMER_SECRET, ConsumerIdentity.getSecretKey());
		
		result = invoker.invoke(invocation);
		/* 调用业务服务后 */
		boolean identityConfirm = Boolean.valueOf(result.getAttachment(CommonKey.IDENTITY_CONFIRM));
		if (!identityConfirm) {
			System.err.print(result.getAttachment(CommonKey.IDENTITY_CONFIRM_FAIL_REASON));
			throw new AuthenException("服务消费者端身份认证未通过");
		}
		
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		ConsumerInjectFilter.applicationContext = context;
	}
}
