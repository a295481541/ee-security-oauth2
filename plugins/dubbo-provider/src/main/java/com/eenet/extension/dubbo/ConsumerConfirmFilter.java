package com.eenet.extension.dubbo;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.eenet.authen.CommonKey;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.ServiceAuthenRequest;
import com.eenet.authen.ServiceAuthenResponse;
import com.eenet.base.IBaseEntity;
/**
 * 服务消费者身份确认过滤器
 * 2016年3月25日
 * @author Orion
 */
@Activate(group = Constants.PROVIDER)
public class ConsumerConfirmFilter implements Filter {
	@Autowired
	private IdentityAuthenticationBizService AuthenService;

	/**
	 * 请求（invocation）的Attachment中应该包含：consumer_code，consumer_secret
	 * 返回（result）的Attachment中包含：identity_confirm
	 */
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = null;
		/* 执行业务服务前 */
		ServiceAuthenRequest request = new ServiceAuthenRequest();
		request.setConsumerCode(invocation.getAttachment(CommonKey.SERVICE_CONSUMER_CODE));
		request.setConsumerSecretKey(invocation.getAttachment(CommonKey.SERVICE_CONSUMER_SECRET));
		ServiceAuthenResponse response = this.AuthenService.consumerAuthen(request);
		
		if ((!response.isIdentityConfirm()) || (!response.isSuccessful())) {//没验证通过
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			return result;
		}
		
		for (Object arg : invocation.getArguments()) {
			if (arg instanceof IBaseEntity) {
				((IBaseEntity) arg).setMdss(request.getConsumerCode());
			}
		}
		
		result = invoker.invoke(invocation);
		result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(true));
		/* 执行业务服务后 */
		// do nothing
		return result;
	}

}
