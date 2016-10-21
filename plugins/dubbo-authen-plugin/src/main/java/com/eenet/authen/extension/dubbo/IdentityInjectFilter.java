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
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 身份注入过滤器
 * 2016年8月23日
 * @author Orion
 */
public class IdentityInjectFilter implements Filter, ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private static String AppIdentityBeanId;
	private static RSAEncrypt encrypt;
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = new RpcResult();
		if (applicationContext==null)
			throw new RuntimeException("Spring环境未知("+this.getClass().getName()+")");
		
		boolean identityConfirm = false;
		try {
			/* 注入获取当前系统或当前应用身份信息 */
			AppAuthenRequest appIdentifier = this.obtainCurrentApp();
			invocation.getAttachments().put(RPCAuthenParamKey.BIZ_APP_ID, appIdentifier.getAppId());
			invocation.getAttachments().put(RPCAuthenParamKey.BIZ_APP_SECRETKEY, appIdentifier.getAppSecretKey());
			invocation.getAttachments().put(RPCAuthenParamKey.BIZ_APP_DOMAIN, appIdentifier.getRedirectURI());
			
			/* 注入当前用户信息 */
			UserAccessTokenAuthenRequest userIdentifier = this.obtainCurrentUser();
			invocation.getAttachments().put(RPCAuthenParamKey.USER_ID, userIdentifier.getUserId());
			invocation.getAttachments().put(RPCAuthenParamKey.USER_ACCESS_TOKEN, userIdentifier.getUserAccessToken());
			invocation.getAttachments().put(RPCAuthenParamKey.USER_TYPE, OPOwner.getUsertype());
			
			/* 调用服务，身份确认直接返回 */
			result = invoker.invoke(invocation);
			if ( result.getException()==null || !result.getException().getClass().getName().equals(AuthenException.class.getName()) )
				identityConfirm = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (identityConfirm)
			return result;
		
		/* 身份认证失败 */
		return this.authenFailHandler(result, invocation);
	}
	
	private AppAuthenRequest obtainCurrentApp() throws EncryptException {
		AppAuthenRequest result = new AppAuthenRequest();
		if ( OPOwner.getCurrentSys().equals(OPOwner.UNKNOW_APP_FLAG) ) {
			if ( !EEBeanUtils.isNULL(AppIdentityBeanId) && applicationContext.containsBean(AppIdentityBeanId) ) {//定义了当前系统标识
				if (encrypt==null)
					throw new EncryptException("未找到加密公钥("+this.getClass().getName()+")");
				result = (AppAuthenRequest)applicationContext.getBean(AppIdentityBeanId);
				result.setAppSecretKey(RSAUtil.encrypt(encrypt, result.getAppSecretKey()+"##"+System.currentTimeMillis() ));
				System.out.println("AppIdentity: " + EEBeanUtils.object2Json(result));
			}
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
	
	/**
	 * 处理身份验证没通过事件
	 * @param result exception属性是com.eenet.common.AuthenException对象
	 * @param invocation
	 * @return
	 * 2016年8月29日
	 * @author Orion
	 */
	private Result authenFailHandler(Result result, Invocation invocation) {
		if (result==null || invocation==null)
			throw new RuntimeException("调用方法或返回对象为空("+this.getClass().getName()+")");
		if ( !result.getException().getClass().getName().equals(AuthenException.class.getName()) )
			return result;
		
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
				((IBaseResponse) returnValue).addMessage(result.getException().getMessage());
				((RpcResult)result).setValue(returnValue);
				((RpcResult)result).setException(null);//认证错误信息已写入返回对象，所以删除该异常
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();//do nothing
		}
		
		/* 如果返回对象不是IBaseResponse的子类或返回对象没有空构造函数，则抛出异常 */
		throw new RuntimeException(result.getException().getMessage());
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

	/**
	 * @param encrypt the encrypt to set
	 */
	public void setEncrypt(RSAEncrypt encrypt) {
		IdentityInjectFilter.encrypt = encrypt;
	}
}