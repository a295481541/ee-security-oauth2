package com.eenet.authen.bizimpl;

import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserCredentialBizService;
import com.eenet.authen.AdminUserLoginAccountBizService;
import com.eenet.authen.AdminUserSignOnBizService;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.cacheSyn.AuthenCacheKey;
import com.eenet.authen.util.IdentityUtil;
import com.eenet.authen.util.SignOnUtil;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.common.cache.RedisClient;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.AdminUserInfoBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.MD5Util;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 服务人员登录实现逻辑，身份认证服务见：
 * @see com.eenet.authen.IdentityAuthenticationBizService
 * @author Orion
 * 2016年6月10日
 */
public class AdminUserSignOnBizImpl implements AdminUserSignOnBizService {
	private RedisClient RedisClient;//Redis客户端
	private RSADecrypt StorageRSADecrypt;//数据存储解密私钥
	private RSADecrypt TransferRSADecrypt;//数据传输解密私钥
	private BusinessAppBizService businessAppBizService;//业务系统服务
	private AdminUserCredentialBizService adminUserCredentialBizService;//服务人员登录秘钥服务
	private AdminUserLoginAccountBizService adminUserLoginAccountBizService;//服务人员登录账号服务
	private AdminUserInfoBizService adminUserInfoBizService;//服务人员管理服务
	private SignOnUtil signOnUtil;//登录工具
	private IdentityUtil identityUtil;//身份认证工具
	@Override
	public SignOnGrant getSignOnGrant(String appId, String redirectURI, String loginAccount, String password) {
		SignOnGrant grant = new SignOnGrant();
		grant.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(loginAccount) || EEBeanUtils.isNULL(password)) {
			grant.addMessage("参数不完整("+this.getClass().getName()+")");
			return grant;
		}
		
		/* 计算传入的服务人员登录密码明文 */
		String passwordPlaintext = null;
		try {
			passwordPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), password, 2);
			if (EEBeanUtils.isNULL(passwordPlaintext)) {
				grant.addMessage("无法解密提供的服务人员登录密码("+this.getClass().getName()+")");
				return grant;
			}
		} catch (EncryptException e) {
			grant.addMessage(e.toString());
			return grant;
		}
		
		/* 检查业务应用app是否存在，跳转地址是否合法(仅web应用) */
		SimpleResponse existApp = getSignOnUtil().existAPP(appId, redirectURI, getBusinessAppBizService());
		if (!existApp.isSuccessful()) {
			grant.addMessage(existApp.getStrMessage());
			return grant;
		}
		
		/* 获得服务人员个人信息及登录秘钥信息 */
		AdminUserInfo adminUser = getAdminUserLoginAccountBizService().retrieveAdminUserInfo(loginAccount);
		if (!adminUser.isSuccessful()) {
			grant.addMessage(adminUser.getStrMessage());
			return grant;
		}
		AdminUserCredential credential = getAdminUserCredentialBizService().retrieveAdminUserSecretKey(adminUser.getAtid(), getStorageRSADecrypt());
		if (!credential.isSuccessful()) {
			grant.addMessage(credential.getStrMessage());
			return grant;
		}
		
		/*
		 * 判断密码是否能匹配，不对则返回错误信息
		 * 根据加密方式进行不同的密码匹配
		 */
		if (credential.getEncryptionType().equals("RSA")) {
			if (!passwordPlaintext.equals(credential.getPassword())) {
				grant.addMessage("服务人员登录账号或密码错误("+this.getClass().getName()+")");
				return grant;
			}
		} else if (credential.getEncryptionType().equals("MD5")) {
			try {
				if (!MD5Util.encrypt(passwordPlaintext).equals(credential.getPassword())) {
					grant.addMessage("服务人员登录账号或密码错误("+this.getClass().getName()+")");
					return grant;
				}
			} catch (EncryptException e) {
				grant.addMessage(e.toString());
				return grant;
			}
		}  else {
			grant.addMessage("加密方式未知["+credential.getEncryptionType()+"]("+this.getClass().getName()+")");
			return grant;
		}
		
		/* 生成并缓存code */
		StringResponse makeCodeResult = 
				getSignOnUtil().makeGrantCode(AuthenCacheKey.ADMINUSER_GRANTCODE_PREFIX, appId, adminUser.getAtid());
		grant.setSuccessful(makeCodeResult.isSuccessful());
		if (makeCodeResult.isSuccessful())
			grant.setGrantCode(makeCodeResult.getResult());
		else
			grant.addMessage(makeCodeResult.getStrMessage());
		
		return grant;
	}

	@Override
	public AccessToken getAccessToken(String appId, String secretKey, String grantCode) {
		AccessToken token = new AccessToken();
		token.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(secretKey) || EEBeanUtils.isNULL(grantCode)) {
			token.addMessage("参数不完整("+this.getClass().getName()+")");
			return token;
		}
		
		/* 计算传入的app密码明文 */
		String secretKeyPlaintext = null;
		try {
			secretKeyPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), secretKey, 2);
			if (EEBeanUtils.isNULL(secretKeyPlaintext)) {
				token.addMessage("无法解密提供的业务系统秘钥("+this.getClass().getName()+")");
				return token;
			}
		} catch (EncryptException e) {
			token.addMessage(e.toString());
			return token;
		}
		
		/* 验证业务应用系统 */
		SimpleResponse validateResult = getIdentityUtil().validateAPP(appId, secretKeyPlaintext, getStorageRSADecrypt(), getBusinessAppBizService());
		if (!validateResult.isSuccessful()) {
			token.addMessage(validateResult.getStrMessage());
			return token;
		}
		
		/* 验证授权码 */
		StringResponse getUserIdResult = 
				getIdentityUtil().getUserIdByCodeOrToken(AuthenCacheKey.ADMINUSER_GRANTCODE_PREFIX, grantCode, appId);
		if (!getUserIdResult.isSuccessful()) {
			token.addMessage(getUserIdResult.getStrMessage());
			return token;
		}
		
		/* 删除授权码（授权码只能用一次） */
		SimpleResponse rmCodeResult = 
				getSignOnUtil().removeCodeOrToken(AuthenCacheKey.ADMINUSER_GRANTCODE_PREFIX, grantCode, appId);
		if (!rmCodeResult.isSuccessful()) {
			token.addMessage(rmCodeResult.getStrMessage());
			return token;
		}
		
		/* 生成并记录访问令牌 */
		StringResponse mkAccessTokenResult = 
				getSignOnUtil().makeAccessToken(AuthenCacheKey.ADMINUSER_ACCESSTOKEN_PREFIX, appId, getUserIdResult.getResult(), getBusinessAppBizService());
		if (!mkAccessTokenResult.isSuccessful()) {
			token.addMessage(mkAccessTokenResult.getStrMessage());
			return token;
		}
		
		/* 生成并记录刷新令牌 */
		StringResponse mkFreshTokenResult = 
				getSignOnUtil().makeRefreshToken(AuthenCacheKey.ADMINUSER_REFRESHTOKEN_PREFIX, appId, getUserIdResult.getResult());
		if (!mkFreshTokenResult.isSuccessful()) {
			token.addMessage(mkFreshTokenResult.getStrMessage());
			return token;
		}
		
		/* 获得服务人员基本信息 */
		AdminUserInfo getAdminUserResult = getAdminUserInfoBizService().get(getUserIdResult.getResult());
		if (!getAdminUserResult.isSuccessful()) {
			token.addMessage(getAdminUserResult.getStrMessage());
			return token;
		}
		
		/* 所有参数已缓存，拼返回对象 */
		token.setUserInfo(getAdminUserResult);
		token.setAccessToken(mkAccessTokenResult.getResult());
		token.setRefreshToken(mkFreshTokenResult.getResult());
		token.setSuccessful(true);
		return token;
	}

	@Override
	public AccessToken getAccessToken(String fromAppId, String toAppId, String secretKey, String adminUserId,
			String refreshToken) {
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		result.addMessage("该服务暂未开放("+this.getClass().getName()+")");
		return result;
	}

	@Override
	public AccessToken refreshAccessToken(String appId, String secretKey, String refreshToken, String adminUserId) {
		AccessToken token = new AccessToken();
		token.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(secretKey) || EEBeanUtils.isNULL(refreshToken) || EEBeanUtils.isNULL(adminUserId)) {
			token.addMessage("参数不完整("+this.getClass().getName()+")");
			return token;
		}
		
		/* 计算传入的app密码明文 */
		String secretKeyPlaintext = null;
		try {
			secretKeyPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), secretKey, 2);
			if (EEBeanUtils.isNULL(secretKeyPlaintext)) {
				token.addMessage("无法解密提供的业务系统秘钥("+this.getClass().getName()+")");
				return token;
			}
		} catch (EncryptException e) {
			token.addMessage(e.toString());
			return token;
		}
		
		/* 验证业务应用系统 */
		SimpleResponse validateResult = getIdentityUtil().validateAPP(appId, secretKeyPlaintext, getStorageRSADecrypt(), getBusinessAppBizService());
		if (!validateResult.isSuccessful()) {
			token.addMessage(validateResult.getStrMessage());
			return token;
		}
		
		/* 根据刷新令牌获得服务人员标识 */
		StringResponse getUserIdResult = 
				getIdentityUtil().getUserIdByCodeOrToken(AuthenCacheKey.ADMINUSER_REFRESHTOKEN_PREFIX, refreshToken, appId);
		if (!getUserIdResult.isSuccessful()) {
			token.addMessage(getUserIdResult.getStrMessage());
			return token;
		}
		
		/* 验证刷新令牌是否属于传入的人员标识 */
		if (!adminUserId.equals(getUserIdResult.getResult())) {
			token.addMessage("服务人员刷新令牌错误("+this.getClass().getName()+")");
			return token;
		}
		
		/* 删除访问令牌（防止一个用户可以通过两个令牌登录） */
		/* ★★★★★★★★★★★★★★★★★★★此处有缺陷★★★★★★★★★★★★★★★★★★★★★★★★★ */
//		SimpleResponse rmAccessTokenResult = 
//				getSignOnUtil().removeCodeOrToken(AuthenCacheKey.ADMINUSER_ACCESSTOKEN_PREFIX, accessToken, appId);
//		if (!rmAccessTokenResult.isSuccessful()) {
//			token.addMessage(rmAccessTokenResult.getStrMessage());
//			return token;
//		}
		/* ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★ */
		
		/* 删除刷新令牌（一个刷新令牌只能置换一次访问令牌） */
		SimpleResponse rmFreshTokenResult = 
				getSignOnUtil().removeCodeOrToken(AuthenCacheKey.ADMINUSER_REFRESHTOKEN_PREFIX, refreshToken, appId);
		if (!rmFreshTokenResult.isSuccessful()) {
			token.addMessage(rmFreshTokenResult.getStrMessage());
			return token;
		}
		
		/* 生成并记录访问令牌（超过有效期后令牌会从Redis中自动消失） */
		StringResponse mkAccessTokenResult = 
				getSignOnUtil().makeAccessToken(AuthenCacheKey.ADMINUSER_ACCESSTOKEN_PREFIX, appId, getUserIdResult.getResult(), getBusinessAppBizService());
		if (!mkAccessTokenResult.isSuccessful()) {
			token.addMessage(mkAccessTokenResult.getStrMessage());
			return token;
		}
		
		/* 生成并记录新的刷新令牌 */
		StringResponse mkFreshTokenResult = 
				getSignOnUtil().makeRefreshToken(AuthenCacheKey.ADMINUSER_REFRESHTOKEN_PREFIX, appId, getUserIdResult.getResult());
		if (!mkFreshTokenResult.isSuccessful()) {
			token.addMessage(mkFreshTokenResult.getStrMessage());
			return token;
		}
		
		/* 所有参数已缓存，拼返回对象 */
		token.setAccessToken(mkAccessTokenResult.getResult());
		token.setRefreshToken(mkFreshTokenResult.getResult());
		token.setSuccessful(true);
		return token;
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	
	/**
	 * @return the Redis客户端
	 */
	public RedisClient getRedisClient() {
		return RedisClient;
	}

	/**
	 * @param redisClient the Redis客户端 to set
	 */
	public void setRedisClient(RedisClient redisClient) {
		RedisClient = redisClient;
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
	 * @return the 服务人员登录秘钥服务
	 */
	public AdminUserCredentialBizService getAdminUserCredentialBizService() {
		return adminUserCredentialBizService;
	}

	/**
	 * @param adminUserCredentialBizService the 服务人员登录秘钥服务 to set
	 */
	public void setAdminUserCredentialBizService(AdminUserCredentialBizService adminUserCredentialBizService) {
		this.adminUserCredentialBizService = adminUserCredentialBizService;
	}

	/**
	 * @return the 服务人员登录账号服务
	 */
	public AdminUserLoginAccountBizService getAdminUserLoginAccountBizService() {
		return adminUserLoginAccountBizService;
	}

	/**
	 * @param adminUserLoginAccountBizService the 服务人员登录账号服务 to set
	 */
	public void setAdminUserLoginAccountBizService(AdminUserLoginAccountBizService adminUserLoginAccountBizService) {
		this.adminUserLoginAccountBizService = adminUserLoginAccountBizService;
	}
	
	/**
	 * @return the 服务人员管理服务
	 */
	public AdminUserInfoBizService getAdminUserInfoBizService() {
		return adminUserInfoBizService;
	}

	/**
	 * @param adminUserInfoBizService the 服务人员管理服务 to set
	 */
	public void setAdminUserInfoBizService(AdminUserInfoBizService adminUserInfoBizService) {
		this.adminUserInfoBizService = adminUserInfoBizService;
	}

	/**
	 * @return the 登录工具类
	 */
	public SignOnUtil getSignOnUtil() {
		return signOnUtil;
	}

	/**
	 * @param signOnUtil the 登录工具类 to set
	 */
	public void setSignOnUtil(SignOnUtil signOnUtil) {
		this.signOnUtil = signOnUtil;
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
