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
import com.eenet.authen.EENetEndUserAuthenRequest;
import com.eenet.authen.EENetEndUserAuthenResponse;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.base.IBaseEntity;

/**
 * 最终用户身份确认过滤器
 * 2016年5月19日
 * @author Orion
 */
public class EndUserConfirmFilter implements Filter, ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String AuthenServiceID;

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = null;
		if (applicationContext==null || !applicationContext.containsBean(AuthenServiceID)) {
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_FROM, String.valueOf(IdentityConfirmFailFrom.syserr));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_REASON, "业务服务Provider配置文件有误");
			return result;
		}
		
		IdentityAuthenticationBizService AuthenService = (IdentityAuthenticationBizService) applicationContext.getBean(AuthenServiceID);
		
		/* 执行业务服务前 */
		EENetEndUserAuthenRequest request = new EENetEndUserAuthenRequest();
		request.setAppId(invocation.getAttachment(CommonKey.THIRDPARTY_APP_ID));
		request.setSecretKey(invocation.getAttachment(CommonKey.THIRDPARTY_APP_SECRET));
		request.setEndUserAccount(invocation.getAttachment(CommonKey.ENDUSER_MAIN_ACCOUNT));
		request.setEndUserTocken(invocation.getAttachment(CommonKey.ENDUSER_ACCESS_TOCKEN));
		EENetEndUserAuthenResponse response = AuthenService.endUserAuthen(request);
		
		/* 身份认证失败 */
		if (!response.isSuccessful()) {
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_FROM, String.valueOf(IdentityConfirmFailFrom.syserr));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_REASON, "认证异常："+response.getStrMessage());
			return result;
		}
		if (!response.isSsoSysIdentityConfirm()) {
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_FROM, String.valueOf(IdentityConfirmFailFrom.thirdPartySys));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_REASON, "应用认证异常：系统ID(appId)和秘钥(secretKey)不匹配");
			return result;
		}
		if (!response.isEndUseridentityConfirm()) {
			result = new RpcResult();
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM, String.valueOf(false));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_FROM, String.valueOf(IdentityConfirmFailFrom.endUser));
			result.getAttachments().put(CommonKey.IDENTITY_CONFIRM_FAIL_REASON, "用户身份认证异常：用户主账号(endUserAccount)和用户令牌(endUserTocken)不匹配");
			return result;
		}
		
		
		/* 注入当前最终用户身份 */
		for (Object arg : invocation.getArguments()) {
			if (arg instanceof IBaseEntity) {
				((IBaseEntity) arg).setCrps(request.getEndUserAccount());
				((IBaseEntity) arg).setMdps(request.getEndUserAccount());
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
		if (EndUserConfirmFilter.applicationContext == null)
			EndUserConfirmFilter.applicationContext = applicationContext;
	}
	
	public void setAuthenServiceID(String authenServiceID) {
		if (EndUserConfirmFilter.AuthenServiceID == null)
			EndUserConfirmFilter.AuthenServiceID = authenServiceID;
	}
}
