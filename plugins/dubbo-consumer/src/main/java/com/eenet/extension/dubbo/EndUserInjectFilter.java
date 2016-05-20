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
import com.eenet.authen.AccessToken;
import com.eenet.authen.SingleSignOnBizService;
import com.eenet.authen.ThirdPartySSOAPP;
import com.eenet.base.IBaseResponse;
import com.eenet.common.exception.AuthenException;
import static com.eenet.auth.ThreadSharedAuthParameter.*;

import java.lang.reflect.Constructor;

/**
 * 
 * 调用服务提供者前，注入第三方业务系统（单点登录集成）、最终用户身份信息
 * 2016年5月20日
 * @author Orion
 */
public class EndUserInjectFilter implements Filter, ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String thirdPartyIdentityBeanId;

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException,AuthenException {
		if (applicationContext==null || !applicationContext.containsBean(thirdPartyIdentityBeanId))
			throw new AuthenException("未获得业务系统（单点登录集成）身份信息：请检查配置文件");
		
		Result result = null;
		/* 调用业务服务前 */
		ThirdPartySSOAPP thirdPartySSOAPP = (ThirdPartySSOAPP)applicationContext.getBean(thirdPartyIdentityBeanId);
		invocation.getAttachments().put(CommonKey.THIRDPARTY_APP_ID, thirdPartySSOAPP.getAppId());
		invocation.getAttachments().put(CommonKey.THIRDPARTY_APP_SECRET, thirdPartySSOAPP.getSecretKey());
		invocation.getAttachments().put(CommonKey.ENDUSER_MAIN_ACCOUNT, CurEndUserMainAccount.get().getAccount());
		invocation.getAttachments().put(CommonKey.ENDUSER_ACCESS_TOCKEN, CurEndUserToken.get().getAccessToken());
		
		result = invoker.invoke(invocation);
		
		/* 身份确认，直接返回 */
		boolean identityConfirm = Boolean.valueOf(result.getAttachment(CommonKey.IDENTITY_CONFIRM));
		if (identityConfirm)
			return result;
		
		/* 身份认证不通过，并且是由于用户访问令牌所致：刷新令牌并重新访问 */
		String confirmFailFrom = String.valueOf(result.getAttachment(CommonKey.IDENTITY_CONFIRM_FAIL_FROM));
		if (confirmFailFrom.equals(IdentityConfirmFailFrom.endUser)) {
			result = this.refresTokenNReCall(invoker, invocation);
		}
		identityConfirm = Boolean.valueOf(result.getAttachment(CommonKey.IDENTITY_CONFIRM));
		if (identityConfirm)
			return result;
		
		/* 身份认证不通过，如果是IBaseResponse的子类，则写入错误信息 */
		try {
			boolean isRpcResult = result instanceof RpcResult;//返回的是RpcResult对象
			boolean hasNoParamConstruct = false;//服务接口返回类型有空构造函数
			//计算返回对象类型
			Class<?> serviceReturnType = invoker.getInterface()
					.getMethod(invocation.getMethodName(), invocation.getParameterTypes()).getReturnType();
			
			//判断是否有空构造函数
			for (Constructor<?> c : serviceReturnType.getConstructors()) {
				if (c.getParameterTypes()==null || c.getParameterTypes().length==0) {
					hasNoParamConstruct = true;
					break;
				}
			}
			
			//创建对象
			Object returnValue = null;
			if (isRpcResult && hasNoParamConstruct)
				returnValue = serviceReturnType.newInstance();
			
			//是IBaseResponse实现类，设置提示信息并返回
			if ((returnValue!=null) && (returnValue instanceof IBaseResponse)) {
				((IBaseResponse) returnValue).setSuccessful(false);
				((IBaseResponse) returnValue).addMessage(result.getAttachment(CommonKey.IDENTITY_CONFIRM_FAIL_REASON));
				((RpcResult)result).setValue(returnValue);
				return result;
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		/* 身份认证不通过，如果不是IBaseResponse的子类，则抛出异常 */
		String exceptionMessage = "未知错误";
		if (confirmFailFrom.equals(IdentityConfirmFailFrom.syserr.toString())){
			exceptionMessage = "系统错误";
		} else if (confirmFailFrom.equals(IdentityConfirmFailFrom.thirdPartySys.toString())){
			exceptionMessage = "第三方业务系统（单点登录集成）身份校验错误";
		} else if (confirmFailFrom.equals(IdentityConfirmFailFrom.endUser.toString())){
			exceptionMessage = "用户令牌校验错误";
		} else if (confirmFailFrom.equals(IdentityConfirmFailFrom.consumer.toString())){
			exceptionMessage = "服务消费者身份校验错误";
		}
		throw new AuthenException(exceptionMessage+"："+result.getAttachment(CommonKey.IDENTITY_CONFIRM_FAIL_REASON));
	}
	
	/**
	 * 刷新令牌并重新调用
	 * 2016年5月20日
	 * @author Orion
	 */
	public Result refresTokenNReCall(Invoker<?> invoker, Invocation invocation) {
		Result result = null;
		
		ThirdPartySSOAPP thirdPartySSOAPP = (ThirdPartySSOAPP)applicationContext.getBean(thirdPartyIdentityBeanId);
		SingleSignOnBizService ssoService = (SingleSignOnBizService)applicationContext.getBean("");
		
		/* 刷新令牌 */
		AccessToken newAccessToken = ssoService.refreshAccessToken(thirdPartySSOAPP.getAppId(), thirdPartySSOAPP.getSecretKey(), CurEndUserToken.get().getRefreshToken());
		CurEndUserToken.set(newAccessToken);
		
		invocation.getAttachments().put(CommonKey.THIRDPARTY_APP_ID, thirdPartySSOAPP.getAppId());
		invocation.getAttachments().put(CommonKey.THIRDPARTY_APP_SECRET, thirdPartySSOAPP.getSecretKey());
		invocation.getAttachments().put(CommonKey.ENDUSER_MAIN_ACCOUNT, CurEndUserMainAccount.get().getAccount());
		invocation.getAttachments().put(CommonKey.ENDUSER_ACCESS_TOCKEN, CurEndUserToken.get().getAccessToken());
		
		result = invoker.invoke(invocation);
		
		return result;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (EndUserInjectFilter.applicationContext == null)
			EndUserInjectFilter.applicationContext = applicationContext;
	}

	/**
	 * @param thirdPartyIdentityBeanId the thirdPartyIdentityBeanId to set
	 */
	public static void setThirdPartyIdentityBeanId(String thirdPartyIdentityBeanId) {
		if (EndUserInjectFilter.thirdPartyIdentityBeanId == null)
			EndUserInjectFilter.thirdPartyIdentityBeanId = thirdPartyIdentityBeanId;
	}

}
