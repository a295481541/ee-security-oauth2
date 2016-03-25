package com.eenet.extension.dubbo;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
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
@Activate(group = Constants.CONSUMER)
public class ConsumerInjectFilter implements Filter {
	@Autowired
	private ServiceConsumer ConsumerIdentity;
	
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = null;
		/* 调用业务服务前 */
		invocation.getAttachments().put(CommonKey.SERVICE_CONSUMER_CODE, ConsumerIdentity.getCode());
		invocation.getAttachments().put(CommonKey.SERVICE_CONSUMER_SECRET, ConsumerIdentity.getSecretKey());
		
		result = invoker.invoke(invocation);
		/* 调用业务服务后 */
		boolean identityConfirm = Boolean.valueOf(result.getAttachment(CommonKey.IDENTITY_CONFIRM));
		if (!identityConfirm) {
			throw new AuthenException("服务消费者端身份认证未通过");
		}
		
		return result;
	}
}
