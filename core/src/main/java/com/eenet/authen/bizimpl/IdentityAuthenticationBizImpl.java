package com.eenet.authen.bizimpl;

import com.eenet.SecurityCacheKey;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.authen.response.AppAuthenResponse;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.authen.util.IdentityUtil;
import com.eenet.authen.util.UserNSeriesResponse;
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
	public AppAuthenResponse appAuthen(AppAuthenRequest request) {
		AppAuthenResponse result = new AppAuthenResponse();
		result.setAppIdentityConfirm(false);
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
		StringResponse validateResult = getIdentityUtil().validateAPP(request.getAppId(), secretKeyPlaintext, getStorageRSADecrypt(), getBusinessAppBizService());
		if (!validateResult.isSuccessful()) {
			result.addMessage(validateResult.getStrMessage());
			return result;
		}
		
		/* 判断提供的业务体系ID和应用所属的业务体系ID是否有冲突 */
		if (!EEBeanUtils.isNULL(request.getBizSeriesId()) && !EEBeanUtils.isNULL(validateResult.getResult())
				&& request.getBizSeriesId().equals(validateResult.getResult())) {
			result.addMessage("指定的业务体系与应用所属的业务体系有冲突");
			return result;
		}
		
		/* 设置返回信息 */
		result.setAppIdentityConfirm(true);
		result.setAtid(request.getAppId());
		if ( !EEBeanUtils.isNULL(validateResult.getResult()) )
			result.setBizSeriesId(validateResult.getResult());
		else if ( !EEBeanUtils.isNULL(request.getBizSeriesId()) )
			result.setBizSeriesId(request.getBizSeriesId());
		System.out.println("appAuthen:" +EEBeanUtils.object2Json(result) );
		return result;
	}
	
	@Override
	public AppAuthenResponse appAuthenWithoutTimeMillis(AppAuthenRequest request) {
		AppAuthenResponse result = new AppAuthenResponse();
		result.setAppIdentityConfirm(false);
		/* 参数检查 */
		if (request==null || EEBeanUtils.isNULL(request.getAppId()) || EEBeanUtils.isNULL(request.getAppSecretKey())) {
			result.addMessage("参数不完整("+this.getClass().getName()+"("+this.getClass().getName()+")");
			return result;
		}
		
		
		/* 计算传入的app密码明文 */
		String secretKeyPlaintext = null;
		try {
			secretKeyPlaintext = RSAUtil.decrypt(getTransferRSADecrypt(), request.getAppSecretKey());
//			secretKeyPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), request.getAppSecretKey(), 2);
			if (EEBeanUtils.isNULL(secretKeyPlaintext)) {
				result.addMessage("无法解密提供的业务系统秘钥("+this.getClass().getName()+"("+this.getClass().getName()+")");
				return result;
			}
		} catch (EncryptException e) {
			result.addMessage(e.toString());
			return result;
		}
		/* 验证业务应用系统 */
		StringResponse validateResult = getIdentityUtil().validateAPP(request.getAppId(), secretKeyPlaintext, getStorageRSADecrypt(), getBusinessAppBizService());
		if (!validateResult.isSuccessful()) {
			result.addMessage(validateResult.getStrMessage());
			return result;
		}
		
		/* 判断提供的业务体系ID和应用所属的业务体系ID是否有冲突 */
		if (!EEBeanUtils.isNULL(request.getBizSeriesId()) && !EEBeanUtils.isNULL(validateResult.getResult())
				&& request.getBizSeriesId().equals(validateResult.getResult())) {
			result.addMessage("指定的业务体系与应用所属的业务体系有冲突");
			return result;
		}
		
		/* 设置返回信息 */
		result.setAppIdentityConfirm(true);
		result.setAtid(request.getAppId());
		if ( !EEBeanUtils.isNULL(validateResult.getResult()) )
			result.setBizSeriesId(validateResult.getResult());
		else if ( !EEBeanUtils.isNULL(request.getBizSeriesId()) )
			result.setBizSeriesId(request.getBizSeriesId());
		
		return result;
	}

	@Override
	public UserAccessTokenAuthenResponse endUserAuthen(UserAccessTokenAuthenRequest request) {
		UserAccessTokenAuthenResponse result = new UserAccessTokenAuthenResponse();
		result.setSuccessful(false);
		
		/* 认证业务系统 */
		AppAuthenRequest appAuthenRequest = new AppAuthenRequest();
		appAuthenRequest.setAppId(request.getAppId());
		appAuthenRequest.setAppSecretKey(request.getAppSecretKey());
		AppAuthenResponse appAuthenResponse = this.appAuthen(appAuthenRequest);
		if ( !appAuthenResponse.isSuccessful() ) {
			result.addMessage(appAuthenResponse.getStrMessage());
			return result;
		}
		
		return this.endUserAuthenOnly(request);
	}
	@Override
	public UserAccessTokenAuthenResponse endUserAuthenOnly(UserAccessTokenAuthenRequest request) {
		UserAccessTokenAuthenResponse result = new UserAccessTokenAuthenResponse();
		result.setSuccessful(false);
		
		StringResponse userAuthenResult = this.userTokenAuthen(request, SecurityCacheKey.ENDUSER_ACCESSTOKEN_PREFIX);
		result.setUserIdentityConfirm(userAuthenResult.isSuccessful());
		if ( !userAuthenResult.isSuccessful() ) {
			result.addMessage(userAuthenResult.getStrMessage());
			return result;
		}
		
		result.setSuccessful(true);
		result.setBizSeriesId(userAuthenResult.getResult());
		return result;
	}

	@Override
	public UserAccessTokenAuthenResponse adminUserAuthen(UserAccessTokenAuthenRequest request) {
		UserAccessTokenAuthenResponse result = new UserAccessTokenAuthenResponse();
		result.setSuccessful(false);
		
		/* 认证业务系统 */
		AppAuthenRequest appAuthenRequest = new AppAuthenRequest();
		appAuthenRequest.setAppId(request.getAppId());
		appAuthenRequest.setAppSecretKey(request.getAppSecretKey());
		AppAuthenResponse appAuthenResponse = this.appAuthen(appAuthenRequest);
		if ( !appAuthenResponse.isSuccessful() ) {
			result.addMessage(appAuthenResponse.getStrMessage());
			return result;
		}
		
		return this.adminUserAuthenOnly(request);
	}
	@Override
	public UserAccessTokenAuthenResponse adminUserAuthenOnly(UserAccessTokenAuthenRequest request) {
		UserAccessTokenAuthenResponse result = new UserAccessTokenAuthenResponse();
		result.setSuccessful(false);
		
		StringResponse userAuthenResult = this.userTokenAuthen(request, SecurityCacheKey.ADMINUSER_ACCESSTOKEN_PREFIX);
		result.setUserIdentityConfirm(userAuthenResult.isSuccessful());
		if ( !userAuthenResult.isSuccessful() ) {
			result.addMessage(userAuthenResult.getStrMessage());
			return result;
		}
		
		result.setSuccessful(true);
		result.setBizSeriesId(userAuthenResult.getResult());
		return result;
	}

	@Override
	public boolean authenServiceProviderPing() {
		return true;
	}
	
	/**
	 * 认证用户令牌
	 * @param request
	 * @param accesstokenPrefix
	 * @return successful属性标识令牌是否认证成功，result记录业务体系标识
	 * 2016年8月22日
	 * @author Orion
	 */
	private StringResponse userTokenAuthen(UserAccessTokenAuthenRequest request, String accesstokenPrefix) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (request == null || EEBeanUtils.isNULL(request.getUserId())
				|| EEBeanUtils.isNULL(request.getUserAccessToken())) {
			result.addMessage("参数不完整(" + this.getClass().getName() + ")");
			return result;
		}
		
		/* 获得传入访问令牌的所有者标识 */
		UserNSeriesResponse getTokenInfoResult = 
				getIdentityUtil().getUserNSeriesByCodeOrToken(accesstokenPrefix, request.getUserAccessToken(), request.getAppId());
		if ( !getTokenInfoResult.isSuccessful() ) { 
			result.addMessage(getTokenInfoResult.getStrMessage());
			return result;
		}
		
		/* 验证访问令牌（令牌所有者是否与传入的用户标识匹配） */
		if (!request.getUserId().equals(getTokenInfoResult.getUserId())) {
			result.addMessage("访问令牌验证失败("+this.getClass().getName()+"("+this.getClass().getName()+")");
			return result;
		}
		
		result.setSuccessful(true);
		result.setResult(getTokenInfoResult.getSeriesId());
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
