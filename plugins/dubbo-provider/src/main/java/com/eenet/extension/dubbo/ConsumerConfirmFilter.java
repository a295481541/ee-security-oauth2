package com.eenet.extension.dubbo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.eenet.auth.CommonKey;
import com.eenet.auth.IdentityConfirmFailFrom;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.ServiceAuthenRequest;
import com.eenet.authen.ServiceAuthenResponse;
import com.eenet.base.IBaseEntity;
/**
 * 服务消费者身份确认过滤器
 * 2016年3月25日
 * @author Orion
 */
public class ConsumerConfirmFilter implements Filter,ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String AuthenServiceID;

	/**
	 * 请求（invocation）的Attachment中应该包含：consumer_code，consumer_secret
	 * 返回（result）的Attachment中包含：identity_confirm
	 */
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = null;
		if (ConsumerConfirmFilter.applicationContext==null || !ConsumerConfirmFilter.applicationContext.containsBean(AuthenServiceID)) {
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_FROM, String.valueOf(IdentityConfirmFailFrom.syserr));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_REASON, "业务服务Provider配置文件有误");
			return result;
		}
		
		IdentityAuthenticationBizService AuthenService = (IdentityAuthenticationBizService) ConsumerConfirmFilter.applicationContext.getBean(AuthenServiceID);
		
		/* 执行业务服务前 */
		ServiceAuthenRequest request = new ServiceAuthenRequest();
		request.setConsumerCode(invocation.getAttachment(CommonKey.SERVICE_CONSUMER_CODE));
		request.setConsumerSecretKey(invocation.getAttachment(CommonKey.SERVICE_CONSUMER_SECRET));
		ServiceAuthenResponse response = AuthenService.consumerAuthen(request);
		
		/* 身份认证失败 */
		if (!response.isSuccessful()) {
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_FROM, String.valueOf(IdentityConfirmFailFrom.syserr));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_REASON, "服务消费者认证异常："+response.getStrMessage());
			return result;
		}
		if (!response.isIdentityConfirm()) {
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_FROM, String.valueOf(IdentityConfirmFailFrom.consumer));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_REASON, "服务消费者认证异常：用户编码(code)和秘钥(secretKey)不匹配");
			return result;
		}
		
		/* 注入当前系统调用者 */
		for (Object arg : invocation.getArguments()) {
			if (arg instanceof IBaseEntity) {
				((IBaseEntity) arg).setCrss(request.getConsumerCode());
				((IBaseEntity) arg).setMdss(request.getConsumerCode());
			}
		}
		result = invoker.invoke(invocation);
		result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(true));
		/* 执行业务服务后 */
		// do nothing
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (ConsumerConfirmFilter.applicationContext == null)
			ConsumerConfirmFilter.applicationContext = applicationContext;
	}

	public void setAuthenServiceID(String authenServiceID) {
		if (ConsumerConfirmFilter.AuthenServiceID == null)
			ConsumerConfirmFilter.AuthenServiceID = authenServiceID;
	}
}
