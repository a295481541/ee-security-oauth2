package com.eenet.extension.dubbo;

import java.lang.reflect.Constructor;

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
import com.eenet.authen.ServiceConsumer;
import com.eenet.base.IBaseResponse;
import com.eenet.common.exception.AuthenException;
import com.eenet.util.EEBeanUtils;

/**
 * 服务消费者身份注入
 * 2016年3月25日
 * @author Orion
 */

public class ConsumerInjectFilter implements Filter,ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String IdentityBeanId;
	
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException,AuthenException {
		if (ConsumerInjectFilter.applicationContext==null || !ConsumerInjectFilter.applicationContext.containsBean(IdentityBeanId))
			throw new AuthenException("未获得服务消费者身份信息：请检查配置文件");
		
		Result result = null;
		ServiceConsumer ConsumerIdentity = (ServiceConsumer) ConsumerInjectFilter.applicationContext.getBean(IdentityBeanId);
		/* 调用业务服务前 */
		invocation.getAttachments().put(CommonKey.SERVICE_CONSUMER_CODE, ConsumerIdentity.getCode());
		invocation.getAttachments().put(CommonKey.SERVICE_CONSUMER_SECRET, ConsumerIdentity.getSecretKey());
		
		result = invoker.invoke(invocation);
		/* 没有身份确认信息（即服务提供者无身份校验要求），直接返回  */
		if (EEBeanUtils.isNULL(result.getAttachment(CommonKey.IDENTITY_CONFIRM)))
			return result;
		
		/* 身份确认，直接返回 */
		boolean identityConfirm = Boolean.valueOf(result.getAttachment(CommonKey.IDENTITY_CONFIRM));
		if (identityConfirm)
			return result;
		//以下是身份认证不通过
		
		/* 如果是IBaseResponse的子类，则写入错误信息 */
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
		
		/* 如果不是IBaseResponse的子类，则抛出异常 */
		String exceptionMessage = "未知错误";
		String confirmFailFrom = String.valueOf(result.getAttachment(CommonKey.IDENTITY_CONFIRM_FAIL_FROM));
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

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		if (ConsumerInjectFilter.applicationContext == null)
			ConsumerInjectFilter.applicationContext = context;
	}

	public void setIdentityBeanId(String identityBeanId) {
		if (ConsumerInjectFilter.IdentityBeanId == null)
			ConsumerInjectFilter.IdentityBeanId = identityBeanId;
	}
}
