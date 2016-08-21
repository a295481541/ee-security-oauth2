package com.eenet.extension.dubbo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.eenet.authen.identifier.RPCAuthenParamKey;
import com.eenet.util.EEBeanUtils;

public class IdentityConfirmFilter implements Filter,ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String IdentityBeanId;
	
	private static boolean defaultEndUser = true; //最终用户是否可访问
	private static boolean defaultAdminUser = true; //管理员是否可访问
	private static boolean defaultAnonymous = false; //匿名用户是否可访问
	private static boolean defaultApp = true; //接入系统是否必须认证
	private static Properties AuthenRuleProperties = null; //认证规则配置文件

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = null;
		
		/* 判断当前用户类型能否访问该服务  */
		String userType = invocation.getAttachment(RPCAuthenParamKey.USER_TYPE,"anonymous");
		boolean userTypeAccessAble = this.isUserTypeAccessAble(userType,invoker.getInterface().getName(),invocation.getMethodName(),invocation.getParameterTypes());
		if (!userTypeAccessAble) {
			result = new RpcResult();
			result.getAttachments().put(RPCAuthenParamKey.AUTHEN_CONFIRM, String.valueOf(false));
			result.getAttachments().put(RPCAuthenParamKey.AUTHEN_FAIL_REASON, userType+"用户不可访问该服务");
			return result;
		}
		boolean appAuthenLimit = this.isAppAuthenLimit(invoker.getInterface().getName(),invocation.getMethodName(),invocation.getParameterTypes());
		
		
		
		return invoker.invoke(invocation);
	}
	
	public void endUserAuthen() {
		
	}
	
	public void adminUserAuthen() {
		
	}
	
	public void appAuthen() {
		
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
		if ("adminUser".equals(userType))
			userTypeAccessAble = defaultAdminUser;
		if ("anonymous".equals(userType))
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
			userTypeAccessAble = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+paramExp.toString(),"false"));
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+methodNameExp+"(*)") ) //按服务名+方法名计算访问权限
			userTypeAccessAble = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+"(*)","false"));
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+".*") ) //按服务名计算访问权限
			userTypeAccessAble = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+".*","false"));
		
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
			appAuthenLimit = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+paramExp.toString(),"true"));
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+methodNameExp+"(*)") ) //按服务名+方法名计算访问权限
			appAuthenLimit = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+methodNameExp+"(*)","true"));
		else if ( AuthenRuleProperties.containsKey(serviceNameExp+".*") ) //按服务名计算访问权限
			appAuthenLimit = Boolean.valueOf(AuthenRuleProperties.getProperty(serviceNameExp+".*","true"));
		
		return appAuthenLimit;
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (IdentityConfirmFilter.applicationContext == null)
			IdentityConfirmFilter.applicationContext = applicationContext;
	}
	
	public void setIdentityBeanId(String identityBeanId) {
		if (IdentityConfirmFilter.IdentityBeanId == null)
			IdentityConfirmFilter.IdentityBeanId = identityBeanId;
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
			
			if (EEBeanUtils.isNULL(properties.getProperty("default.endUser")))
				defaultEndUser = Boolean.valueOf(properties.getProperty("default.endUser","true").trim());
			if (EEBeanUtils.isNULL(properties.getProperty("default.adminUser")))
				defaultAdminUser = Boolean.valueOf(properties.getProperty("default.adminUser","true").trim());
			if (EEBeanUtils.isNULL(properties.getProperty("default.anonymous")))
				defaultAnonymous = Boolean.valueOf(properties.getProperty("default.anonymous","false").trim());
			if (EEBeanUtils.isNULL(properties.getProperty("default.default.app")))
				defaultApp = Boolean.valueOf(properties.getProperty("default.default.app","true").trim());
			
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
	
	public static void main(String[] args) {
		Object[] objs = new Object[8];
		int a = 3;
		ppp(args);
		ppp(objs);
		ppp(a);
	}
	
	public static void ppp(Object o) {
		System.out.println(o.getClass().isArray()+" , "+o.getClass().getName() + " , " +o.getClass().getSimpleName());
	}
}
