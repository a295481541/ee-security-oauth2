package com.eenet.authen.bizimpl;

import java.lang.reflect.InvocationTargetException;

import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.cacheSyn.AuthenCacheKey;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.authen.util.IdentityUtil;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 身份认证逻辑实现，含：服务消费者、最终用户、业务应用系统
 * @author Orion
 * 2016年6月11日
 */
public class IdentityAuthenticationBizImpl implements IdentityAuthenticationBizService {
	private RSADecrypt TransferRSADecrypt;//数据传输解密私钥
	private RSADecrypt StorageRSADecrypt;//数据存储解密私钥
	private BusinessAppBizService businessAppBizService;//业务系统服务
	private IdentityUtil identityUtil;//身份认证工具
	
	@Override
	public SimpleResponse appAuthen(AppAuthenRequest request) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (request==null || EEBeanUtils.isNULL(request.getAppId()) || EEBeanUtils.isNULL(request.getAppSecretKey())) {
			result.addMessage("参数不完整("+this.getClass().getName()+"("+this.getClass().getName()+")");
			return result;
		}
		
		/* 计算传入的app密码明文 */
		String secretKeyPlaintext = null;
		try {
			secretKeyPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), request.getAppSecretKey(), 2);
			if (EEBeanUtils.isNULL(secretKeyPlaintext)) {
				result.addMessage("无法解密提供的业务系统秘钥("+this.getClass().getName()+"("+this.getClass().getName()+")");
				return result;
			}
		} catch (EncryptException e) {
			result.addMessage(e.toString());
			return result;
		}
		
		/* 验证业务应用系统 */
		SimpleResponse validateResult = getIdentityUtil().validateAPP(request.getAppId(), secretKeyPlaintext, getStorageRSADecrypt(), getBusinessAppBizService());
		if (!validateResult.isSuccessful()) {
			result.addMessage(validateResult.getStrMessage());
			return result;
		}
		
		result.setSuccessful(true);
		return result;
	}

	@Override
	public UserAccessTokenAuthenResponse endUserAuthen(UserAccessTokenAuthenRequest request) {
		return this.userAuthen(request, AuthenCacheKey.ENDUSER_ACCESSTOKEN_PREFIX);
	}

	@Override
	public UserAccessTokenAuthenResponse adminUserAuthen(UserAccessTokenAuthenRequest request) {
		return this.userAuthen(request, AuthenCacheKey.ADMINUSER_ACCESSTOKEN_PREFIX);
	}

	@Override
	public boolean authenServiceProviderPing() {
		return true;
	}
	
	private UserAccessTokenAuthenResponse userAuthen(UserAccessTokenAuthenRequest request, String accesstokenPrefix) {
		UserAccessTokenAuthenResponse result = new UserAccessTokenAuthenResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (request == null || EEBeanUtils.isNULL(request.getAppId()) || EEBeanUtils.isNULL(request.getAppSecretKey())
				|| EEBeanUtils.isNULL(request.getUserId()) || EEBeanUtils.isNULL(request.getUserAccessToken())) {
			result.addMessage("参数不完整("+this.getClass().getName()+")");
			return result;
		}
		
		/* 验证业务应用系统 */
		SimpleResponse appAuthenResult = null;
		try {
			AppAuthenRequest appAuthenRequest = new AppAuthenRequest();
			EEBeanUtils.coverProperties(appAuthenRequest, request);
			appAuthenResult = this.appAuthen(appAuthenRequest);
		} catch (IllegalAccessException | InvocationTargetException e) {
			result.addMessage(e.getMessage()+"("+this.getClass().getName()+")");
		}
		if (request==null || !appAuthenResult.isSuccessful()) {
			result.addMessage("业务应用系统验证失败("+this.getClass().getName()+")");
			return result;
		}
		result.setAppIdentityConfirm(true);
		
		/* 获得传入访问令牌的所有者标识 */
		StringResponse getUserIdResult = 
				getIdentityUtil().getUserIdByCodeOrToken(accesstokenPrefix, request.getUserAccessToken(), request.getAppId());
		if (!getUserIdResult.isSuccessful()) {
			result.addMessage(getUserIdResult.getStrMessage());
			return result;
		}
		
		/* 验证访问令牌（令牌所有者是否与传入的用户标识匹配） */
		if (!request.getUserId().equals(getUserIdResult.getResult())) {
			result.addMessage("访问令牌验证失败("+this.getClass().getName()+"("+this.getClass().getName()+")");
			return result;
		}
		result.setUserIdentityConfirm(true);
		
		result.setSuccessful(true);
		return result;
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	
	/**
	 * @return the 数据传输解密私钥
	 */
	public RSADecrypt getTransferRSADecrypt() {
		return TransferRSADecrypt;
	}

	/**
	 * @param transferRSADecrypt the 数据传输解密私钥 to set
	 */
	public void setTransferRSADecrypt(RSADecrypt transferRSADecrypt) {
		TransferRSADecrypt = transferRSADecrypt;
	}
	
	/**
	 * @return the 数据存储解密私钥
	 */
	public RSADecrypt getStorageRSADecrypt() {
		return StorageRSADecrypt;
	}

	/**
	 * @param storageRSADecrypt the 数据存储解密私钥 to set
	 */
	public void setStorageRSADecrypt(RSADecrypt storageRSADecrypt) {
		StorageRSADecrypt = storageRSADecrypt;
	}
	
	/**
	 * @return the 业务系统服务
	 */
	public BusinessAppBizService getBusinessAppBizService() {
		return businessAppBizService;
	}

	/**
	 * @param businessAppBizService the 业务系统服务 to set
	 */
	public void setBusinessAppBizService(BusinessAppBizService businessAppBizService) {
		this.businessAppBizService = businessAppBizService;
	}
	
	/**
	 * @return the 身份认证工具
	 */
	public IdentityUtil getIdentityUtil() {
		return identityUtil;
	}

	/**
	 * @param identityUtil the 身份认证工具 to set
	 */
	public void setIdentityUtil(IdentityUtil identityUtil) {
		this.identityUtil = identityUtil;
	}
}
