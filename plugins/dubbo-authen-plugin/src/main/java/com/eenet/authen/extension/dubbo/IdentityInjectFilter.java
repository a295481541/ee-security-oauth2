package com.eenet.authen.extension.dubbo;

import java.lang.reflect.Constructor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.authen.identifier.RPCAuthenParamKey;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.base.IBaseResponse;
import com.eenet.common.OPOwner;
import com.eenet.common.exception.AuthenException;

/**
 * 身份注入过滤器
 * 2016年8月23日
 * @author Orion
 */
public class IdentityInjectFilter implements Filter, ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String AppIdentityBeanId;
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = null;
		if (applicationContext==null)
			throw new AuthenException("Spring环境未知("+this.getClass().getName()+")");
		
		/* 注入获取当前系统或当前应用身份信息 */
		AppAuthenRequest appIdentifier = this.obtainCurrentApp();
		invocation.getAttachments().put(RPCAuthenParamKey.BIZ_APP_ID, appIdentifier.getAppId());
		invocation.getAttachments().put(RPCAuthenParamKey.BIZ_APP_SECRETKEY, appIdentifier.getAppSecretKey());
		invocation.getAttachments().put(RPCAuthenParamKey.BIZ_APP_DOMAIN, appIdentifier.getRedirectURI());
		
		/* 注入当前用户信息 */
		UserAccessTokenAuthenRequest userIdentifier = this.obtainCurrentUser();
		invocation.getAttachments().put(RPCAuthenParamKey.USER_ID, userIdentifier.getUserId());
		invocation.getAttachments().put(RPCAuthenParamKey.USER_ACCESS_TOKEN, userIdentifier.getUserAccessToken());
		invocation.getAttachments().put(RPCAuthenParamKey.USER_TYPE, CallerIdentityInfo.getUsertype());
		
		/* 调用服务，身份确认直接返回 */
		result = invoker.invoke(invocation);
		boolean identityConfirm = Boolean.valueOf(result.getAttachment(RPCAuthenParamKey.AUTHEN_CONFIRM,"true"));
		if (identityConfirm)
			return result;
		
		/* 身份认证失败 */
		return this.authenFailHandler(result, invocation);
	}
	
	private AppAuthenRequest obtainCurrentApp() {
		AppAuthenRequest result = new AppAuthenRequest();
		if ( OPOwner.getCurrentSys().equals(OPOwner.UNKNOW_APP_FLAG) ) {
			if ( applicationContext.containsBean(AppIdentityBeanId) )
				result = (AppAuthenRequest)applicationContext.getBean(AppIdentityBeanId);
		} else {
			result.setAppId( OPOwner.getCurrentSys() );
			result.setAppSecretKey( CallerIdentityInfo.getAppsecretkey() );
			result.setRedirectURI( CallerIdentityInfo.getRedirecturi() );
		}
		return result;
	}
	
	private UserAccessTokenAuthenRequest obtainCurrentUser() {
		UserAccessTokenAuthenRequest result = new UserAccessTokenAuthenRequest();
		result.setUserId( OPOwner.getCurrentUser() );
		result.setUserAccessToken( CallerIdentityInfo.getAccesstoken() );
		return result;
	}
	
	private Result authenFailHandler(Result result, Invocation invocation) {
		/* 如果返回对象是IBaseResponse的子类则写入错误信息 */
		try {
			boolean isRpcResult = result instanceof RpcResult;//返回的是RpcResult对象
			boolean hasNoParamConstruct = false;//服务接口返回类型有空构造函数
			//计算返回对象类型
			Class<?> serviceReturnType = invocation.getInvoker().getInterface()
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
				((IBaseResponse) returnValue).addMessage(result.getAttachment(RPCAuthenParamKey.AUTHEN_FAIL_REASON));
				((RpcResult)result).setValue(returnValue);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();//do nothing
		}
		
		/* 如果返回对象不是IBaseResponse的子类或返回对象没有空构造函数，则抛出异常 */
		throw new AuthenException(result.getAttachment(RPCAuthenParamKey.AUTHEN_FAIL_REASON));
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		if (IdentityInjectFilter.applicationContext == null)
			IdentityInjectFilter.applicationContext = applicationContext;
	}
	
	public void setAppIdentityBeanId(String appIdentityBeanId) {
		if (IdentityInjectFilter.AppIdentityBeanId == null)
			IdentityInjectFilter.AppIdentityBeanId = appIdentityBeanId;
	}
}