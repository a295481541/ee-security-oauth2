package com.eenet.authen.extension.dubbo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.authen.identifier.RPCAuthenParamKey;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.common.OPOwner;
import com.eenet.common.exception.AuthenException;
import com.eenet.util.EEBeanUtils;

/**
 * 服务提供者认证调用者身份过滤器
 * 2016年8月22日
 * @author Orion
 */
public class IdentityConfirmFilter implements Filter,ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String AuthenServiceBeanId;
	
	private static boolean defaultEndUser = true; //最终用户是否可访问
	private static boolean defaultAdminUser = true; //管理员是否可访问
	private static boolean defaultAnonymous = false; //匿名用户是否可访问
	private static boolean defaultApp = true; //接入系统是否必须认证
	private static Properties AuthenRuleProperties = null; //认证规则配置文件

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		/* 将当前身份设置为默认值 */
		OPOwner.reset();
		CallerIdentityInfo.reset();
		
		Result result = null;
		if (applicationContext==null || !applicationContext.containsBean(AuthenServiceBeanId)) {
			RpcResult rpcRS = new RpcResult();
			rpcRS.setException(new AuthenException("业务服务Provider配置文件有误"));
			return rpcRS;
		}
		
		/* 判断当前用户类型能否访问该服务  */
		String userType = invocation.getAttachment(RPCAuthenParamKey.USER_TYPE,"anonymous");
		boolean userTypeAccessAble = this.isUserTypeAccessAble(userType,invoker.getInterface().getName(),invocation.getMethodName(),invocation.getParameterTypes());
		if (!userTypeAccessAble) {
			RpcResult rpcRS = new RpcResult();
			rpcRS.setException(new AuthenException(userType+"用户不可访问"+invoker.getInterface().getName()+"."+invocation.getMethodName()+"服务"));
			return rpcRS;
		}
		
		/* 根据参数组装认证请求对象  */
		boolean appAuthenLimit = this.isAppAuthenLimit(invoker.getInterface().getName(),invocation.getMethodName(),invocation.getParameterTypes());
		UserAccessTokenAuthenRequest userAuthenReq = new UserAccessTokenAuthenRequest();
		userAuthenReq.setAppId(invocation.getAttachment(RPCAuthenParamKey.BIZ_APP_ID,""));
		userAuthenReq.setAppSecretKey(invocation.getAttachment(RPCAuthenParamKey.BIZ_APP_SECRETKEY,""));
		userAuthenReq.setUserId(invocation.getAttachment(RPCAuthenParamKey.USER_ID,""));
		userAuthenReq.setUserAccessToken(invocation.getAttachment(RPCAuthenParamKey.USER_ACCESS_TOKEN,""));
		
		/* 根据不同用户类型进行不同形式认证  */
		boolean authenConfirm = false;
		String authenFailReason = "";
		if ("endUser".equals(userType)) {
			UserAccessTokenAuthenResponse authenResponse = this.endUserAuthen(appAuthenLimit, userAuthenReq);
			authenConfirm = authenResponse.isSuccessful();
			if ( !authenConfirm )
				authenFailReason = authenResponse.getStrMessage();
		} else if ("adminUser".equals(userType)) {
			UserAccessTokenAuthenResponse authenResponse = this.adminUserAuthen(appAuthenLimit, userAuthenReq);
			authenConfirm = authenResponse.isSuccessful();
			if ( !authenConfirm )
				authenFailReason = authenResponse.getStrMessage();
		} else if ( "anonymous".equals(userType) ) {
			if (!appAuthenLimit)
				authenConfirm = true;
			else {
				AppAuthenRequest appAuthenReq = new AppAuthenRequest();
				appAuthenReq.setAppId(userAuthenReq.getAppId());
				appAuthenReq.setAppSecretKey(userAuthenReq.getAppSecretKey());
				SimpleResponse authenResponse = this.appAuthen(appAuthenReq);
				authenConfirm = authenResponse.isSuccessful();
				if ( !authenConfirm )
					authenFailReason = authenResponse.getStrMessage();
			}
		}
		
		/* 认证失败：返回失败信息 */
		if ( !authenConfirm ) {
			RpcResult rpcRS = new RpcResult();
			rpcRS.setException(new AuthenException(authenFailReason));
			return rpcRS;
		}
		
		/* 认证成功：记录当前用户、当前调用服务的消费者 */
		OPOwner.setUsertype(userType);
		if ("endUser".equals(userType) || "adminUser".equals(userType) ) {
			OPOwner.setCurrentUser(userAuthenReq.getUserId());
			CallerIdentityInfo.setAccesstoken(userAuthenReq.getUserAccessToken());
		} else 
			OPOwner.setCurrentUser(userType);
		if (appAuthenLimit) {
			OPOwner.setCurrentSys(userAuthenReq.getAppId());
			CallerIdentityInfo.setAppsecretkey(userAuthenReq.getAppSecretKey());
			CallerIdentityInfo.setRedirecturi(invocation.getAttachment(RPCAuthenParamKey.BIZ_APP_DOMAIN,""));
		}
		
		/* 执行并标记认证通过 */
		result = invoker.invoke(invocation);
		return result;
	}
	
	private UserAccessTokenAuthenResponse endUserAuthen(boolean appAuthenLimit, UserAccessTokenAuthenRequest autenRequest) {
		IdentityAuthenticationBizService authenService = (IdentityAuthenticationBizService) applicationContext.getBean(AuthenServiceBeanId);
		if (appAuthenLimit)
			return authenService.endUserAuthen(autenRequest);
		else
			return authenService.endUserAuthenOnly(autenRequest);
	}
	
	private UserAccessTokenAuthenResponse adminUserAuthen(boolean appAuthenLimit, UserAccessTokenAuthenRequest autenRequest) {
		IdentityAuthenticationBizService authenService = (IdentityAuthenticationBizService) applicationContext.getBean(AuthenServiceBeanId);
		if (appAuthenLimit)
			return authenService.adminUserAuthen(autenRequest);
		else
			return authenService.adminUserAuthenOnly(autenRequest);
	}
	
	private SimpleResponse appAuthen(AppAuthenRequest appAuthenRequest) {
		IdentityAuthenticationBizService authenService = (IdentityAuthenticationBizService) applicationContext.getBean(AuthenServiceBeanId);
		return authenService.appAuthen(appAuthenRequest);
	}
	
	/**
	 * 检查当前用户类型是否可访问该服务
	 * @param userType 用户类型，有效取值：endUser、adminUser、anonymous
	 * @param serviceName 服务名称
	 * @param methodName 被调用方法名称
	 * @param parameterTypes 被调用方法的参数类型
	 * @return 是/否
	 * 2016年8月21日
	 * @author Orion
	 */
	private boolean isUserTypeAccessAble(String userType, String serviceName, String methodName, Class<?>[] parameterTypes) {
		boolean userTypeAccessAble = false;
		/* 按默认值计算访问权限 */
		if ("endUser".equals(userType))
			userTypeAccessAble = defaultEndUser;
		else if ("adminUser".equals(userType))
			userTypeAccessAble = defaultAdminUser;
		else if ("anonymous".equals(userType))
			userTypeAccessAble = defaultAnonymous;
		
		/* 未定义认证控制配置文件（即认证控制按默认值走） */
		if (AuthenRuleProperties == null)
			return userTypeAccessAble;
		
		/* 计算认证控制定义可能的表达式 */
		String serviceNameExp = "["+userType+"]"+serviceName;
		String methodNameExp = "."+methodName;
		StringBuffer paramExp = new StringBuffer().append("(");
		for (Class<?> paramT : parameterTypes)
			paramExp.append(paramT.isArray() ? paramT.getSimpleName() : paramT.getName()).append(",");
		if (paramExp.lastIndexOf(",")!=-1)
			paramExp.deleteCharAt(paramExp.lastIndexOf(","));
		paramExp.append(")");
		
		
		
		
		/* 按优先级匹配表达式并赋值 */
		if ( AuthenRuleProperties.containsKey(serviceNameExp+methodNameExp+paramExp.toString()) ) //按服务名+方法名+参数列表计算访问权限
			userTypeAccessAble = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+paramExp.toString(),"false").trim());
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+methodNameExp+"(*)") ) //按服务名+方法名计算访问权限
			userTypeAccessAble = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+"(*)","false").trim());
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+".*") ) //按服务名计算访问权限
			userTypeAccessAble = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+".*","false").trim());
		
		
		return userTypeAccessAble;
	}
	
	/**
	 * 是否必须认证服务消费者身份
	 * @param serviceName 服务名称
	 * @param methodName 被调用方法名称
	 * @param parameterTypes 被调用方法的参数类型
	 * @return 是/否
	 * 2016年8月21日
	 * @author Orion
	 */
	private boolean isAppAuthenLimit(String serviceName, String methodName, Class<?>[] parameterTypes) {
		boolean appAuthenLimit = defaultApp;
		/* 未定义认证控制配置文件（即认证控制按默认值走） */
		if (AuthenRuleProperties == null)
			return appAuthenLimit;
		
		/* 计算认证控制定义可能的表达式 */
		String serviceNameExp = "[app]"+serviceName;
		String methodNameExp = "."+methodName;
		StringBuffer paramExp = new StringBuffer().append("(");
		for (Class<?> paramT : parameterTypes)
			paramExp.append(paramT.isArray() ? paramT.getSimpleName() : paramT.getName()).append(",");
		if (paramExp.lastIndexOf(",")!=-1)
			paramExp.deleteCharAt(paramExp.lastIndexOf(","));
		paramExp.append(")");
		
		/* 按优先级匹配表达式并赋值 */
		if ( AuthenRuleProperties.containsKey(serviceNameExp+methodNameExp+paramExp.toString()) ) //按服务名+方法名+参数列表计算访问权限
			appAuthenLimit = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+paramExp.toString(),"true").trim());
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+methodNameExp+"(*)") ) //按服务名+方法名计算访问权限
			appAuthenLimit = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+"(*)","true").trim());
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+".*") ) //按服务名计算访问权限
			appAuthenLimit = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+".*","true").trim());
		
		return appAuthenLimit;
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		if (IdentityConfirmFilter.applicationContext == null)
			IdentityConfirmFilter.applicationContext = applicationContext;
	}
	
	public void setAuthenServiceBeanId(String authenServiceBeanId) {
		if (IdentityConfirmFilter.AuthenServiceBeanId == null)
			IdentityConfirmFilter.AuthenServiceBeanId = authenServiceBeanId;
	}
	
	public void setPropertyFile(String propertyFile) {
		if (IdentityConfirmFilter.AuthenRuleProperties!=null || EEBeanUtils.isNULL(propertyFile)) {
			return;
		}
		
		InputStream propertiesIn = null;
		try {
			propertiesIn = this.getClass().getResourceAsStream(propertyFile);
			Properties properties = new Properties();
			properties.load(propertiesIn);
			
			if ( properties.containsKey("default.endUser") )
				defaultEndUser = Boolean.valueOf(properties.getProperty("default.endUser","true").trim());
			if ( properties.containsKey("default.adminUser") )
				defaultAdminUser = Boolean.valueOf(properties.getProperty("default.adminUser","true").trim());
			if ( properties.containsKey("default.anonymous") )
				defaultAnonymous = Boolean.valueOf(properties.getProperty("default.anonymous","false").trim());
			if ( properties.containsKey("default.app") )
				defaultApp = Boolean.valueOf(properties.getProperty("default.app","true").trim());
			
			AuthenRuleProperties = properties;
		} catch (Exception e) {
			return;
		} finally {
			try {
				if (propertiesIn!=null)
					propertiesIn.close();
			} catch (IOException e) {}
		}
	}
}
