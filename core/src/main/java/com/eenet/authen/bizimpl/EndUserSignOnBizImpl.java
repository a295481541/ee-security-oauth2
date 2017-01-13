package com.eenet.authen.bizimpl;

import com.eenet.authen.AccessToken;
import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessSeriesBizService;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserCredentialBizService;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.cacheSyn.AuthenCacheKey;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.authen.util.ABBizCode;
import com.eenet.authen.util.IdentityUtil;
import com.eenet.authen.util.SignOnUtil;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.OPOwner;
import com.eenet.common.cache.RedisClient;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.MD5Util;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 最终用户登录实现逻辑，身份认证服务见：
 * @see com.eenet.authen.IdentityAuthenticationBizService
 * @author Orion
 * 2016年6月11日
 */
public class EndUserSignOnBizImpl implements EndUserSignOnBizService {
	private RedisClient RedisClient;//Redis客户端
	private RSADecrypt StorageRSADecrypt;//数据存储解密私钥
	private RSADecrypt TransferRSADecrypt;//数据传输解密私钥
	private BusinessAppBizService businessAppBizService;//业务系统服务
	private BusinessSeriesBizService businessSeriesBizService;//业务体系服务
	private EndUserCredentialBizService endUserCredentialBizService;//最终用户登录秘钥服务
	private EndUserLoginAccountBizService endUserLoginAccountBizService;//最终用户登录账号服务
	private EndUserInfoBizService endUserInfoBizService;//最终用户管理服务
	private SignOnUtil signOnUtil;//登录工具
	private IdentityUtil identityUtil;//身份认证工具
	
	@Override
	public SignOnGrant getSignOnGrant(String appId, String redirectURI, String loginAccount, String password) {
		return getSignOnGrant(appId, null, redirectURI, loginAccount, password);
	}
	
	@Override
	public AccessToken getAccessToken(String appId, String secretKey, String grantCode) {
		AccessToken token = new AccessToken();
		token.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(secretKey) || EEBeanUtils.isNULL(grantCode)) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage("参数不完整("+this.getClass().getName()+")");
			return token;
		}
		
		/* 计算传入的app密码明文 */
		String secretKeyPlaintext = null;
		try {
			secretKeyPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), secretKey, 5);
			if (EEBeanUtils.isNULL(secretKeyPlaintext)) {
				token.setRSBizCode(ABBizCode.AB0006);
				token.addMessage("无法解密提供的业务系统秘钥("+this.getClass().getName()+")");
				return token;
			}
		} catch (EncryptException e) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage(e.toString());
			return token;
		}
		
		/* 验证业务应用系统 */
		SimpleResponse validateResult = getIdentityUtil().validateAPP(appId, secretKeyPlaintext, getStorageRSADecrypt(), getBusinessAppBizService());
		if (!validateResult.isSuccessful()) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage(validateResult.getStrMessage());
			return token;
		}
		
		/* 验证授权码 */
		StringResponse getUserIdResult = 
				getIdentityUtil().getUserIdByCodeOrToken(AuthenCacheKey.ENDUSER_GRANTCODE_PREFIX, grantCode, appId);
		if (!getUserIdResult.isSuccessful()) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage(getUserIdResult.getStrMessage());
			return token;
		}
		
		
		/* 删除授权码（授权码只能用一次） */
		SimpleResponse rmCodeResult = 
				getSignOnUtil().removeCodeOrToken(AuthenCacheKey.ENDUSER_GRANTCODE_PREFIX, grantCode, appId);
		if (!rmCodeResult.isSuccessful()) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage(rmCodeResult.getStrMessage());
			return token;
		}
		
		/* 删除访问令牌（防止一个用户可以通过两个令牌登录） */
		getSignOnUtil().removeUserTokenInApp(AuthenCacheKey.ENDUSER_CACHED_TOKEN,
				AuthenCacheKey.ENDUSER_ACCESSTOKEN_PREFIX, AuthenCacheKey.ENDUSER_REFRESHTOKEN_PREFIX, appId,
				getUserIdResult.getResult());
		
		/* 生成并记录访问令牌 */
		StringResponse mkAccessTokenResult = 
				getSignOnUtil().makeAccessToken(AuthenCacheKey.ENDUSER_ACCESSTOKEN_PREFIX, appId, getUserIdResult.getResult(), getBusinessAppBizService());
		if (!mkAccessTokenResult.isSuccessful()) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage(mkAccessTokenResult.getStrMessage());
			return token;
		}
		
		
		
		/* 生成并记录刷新令牌 */
		StringResponse mkFreshTokenResult = 
				getSignOnUtil().makeRefreshToken(AuthenCacheKey.ENDUSER_REFRESHTOKEN_PREFIX, appId, getUserIdResult.getResult());//用户标识：业务体系id
		if (!mkFreshTokenResult.isSuccessful()) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage(mkFreshTokenResult.getStrMessage());
			return token;
		}
		
		/* 在当前线程注入个人访问令牌和请求应用身份信息
		 * 注入访问令牌是因为调用基础服务取个人信息时需要验证令牌 */
		OPOwner.setCurrentSys(appId);
		CallerIdentityInfo.setAppsecretkey(secretKey);
		OPOwner.setCurrentUser(getUserIdResult.getResult());
		OPOwner.setUsertype("endUser");
		CallerIdentityInfo.setAccesstoken(mkAccessTokenResult.getResult());
		
		/* 获得最终用户基本信息 */
		EndUserInfo getEndUserResult = getEndUserInfoBizService().get(getUserIdResult.getResult());
		if (!getEndUserResult.isSuccessful()) {
			token.setRSBizCode(ABBizCode.AB0006);
			token.addMessage(getEndUserResult.getStrMessage());
			return token;
		}
		
		/* 标记最终用户已缓存令牌 */
		getSignOnUtil().markUserTokenInApp(AuthenCacheKey.ENDUSER_CACHED_TOKEN, appId, getUserIdResult.getResult(),
				mkAccessTokenResult.getResult(), mkFreshTokenResult.getResult());
		
		/* 所有参数已缓存，拼返回对象 */
		token.setUserInfo(getEndUserResult);
		token.setAccessToken(mkAccessTokenResult.getResult());
		token.setRefreshToken(mkFreshTokenResult.getResult());
		token.setSuccessful(true);
		return token;
	}

	@Override
	public AccessToken getAccessToken(String fromAppId, String toAppId, String secretKey, String endUserId,
			String refreshToken) {
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		result.addMessage("该服务暂未开放("+this.getClass().getName()+")");
		return result;
	}

	@Override
	public AccessToken refreshAccessToken(String appId, String secretKey, String refreshToken, String endUserId) {
		AccessToken token = new AccessToken();
		token.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(secretKey) || EEBeanUtils.isNULL(refreshToken) || EEBeanUtils.isNULL(endUserId)) {
			token.addMessage("参数不完整("+this.getClass().getName()+")");
			return token;
		}
		
		/* 计算传入的app密码明文 */
		String secretKeyPlaintext = null;
		try {
			secretKeyPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), secretKey, 5);
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
		
		/* 根据刷新令牌获得最终用户标识 */
		StringResponse getUserIdResult = 
				getIdentityUtil().getUserIdByCodeOrToken(AuthenCacheKey.ENDUSER_REFRESHTOKEN_PREFIX, refreshToken, appId);
		if (!getUserIdResult.isSuccessful()) {
			token.addMessage(getUserIdResult.getStrMessage());
			return token;
		}
		
		/* 验证刷新令牌是否属于传入的人员标识:业务体系id */
		if (!endUserId.equals(getUserIdResult.getResult())) {
			token.addMessage("最终用户刷新令牌错误("+this.getClass().getName()+")");
			return token;
		}
		
		/* 删除当前用户在当前应用的所有令牌（包括：刷新令牌、访问令牌和已缓存令牌标识），防止一个用户可以通过两个令牌登录 */
		getSignOnUtil().removeUserTokenInApp(AuthenCacheKey.ENDUSER_CACHED_TOKEN,
				AuthenCacheKey.ENDUSER_ACCESSTOKEN_PREFIX, AuthenCacheKey.ENDUSER_REFRESHTOKEN_PREFIX, appId,
				getUserIdResult.getResult());
				
		/* 生成并记录访问令牌（超过有效期后令牌会从Redis中自动消失） */
		StringResponse mkAccessTokenResult = 
				getSignOnUtil().makeAccessToken(AuthenCacheKey.ENDUSER_ACCESSTOKEN_PREFIX, appId, getUserIdResult.getResult(), getBusinessAppBizService());
		if (!mkAccessTokenResult.isSuccessful()) {
			token.addMessage(mkAccessTokenResult.getStrMessage());
			return token;
		}
		
		/* 生成并记录新的刷新令牌 */
		StringResponse mkFreshTokenResult = 
				getSignOnUtil().makeRefreshToken(AuthenCacheKey.ENDUSER_REFRESHTOKEN_PREFIX, appId, getUserIdResult.getResult());
		if (!mkFreshTokenResult.isSuccessful()) {
			token.addMessage(mkFreshTokenResult.getStrMessage());
			return token;
		}
		
		/* 更新最终用户已缓存令牌 */
		getSignOnUtil().markUserTokenInApp(AuthenCacheKey.ENDUSER_CACHED_TOKEN, appId, getUserIdResult.getResult(),
				mkAccessTokenResult.getResult(), mkFreshTokenResult.getResult());
		
		/* 所有参数已缓存，拼返回对象 */
		token.setAccessToken(mkAccessTokenResult.getResult());
		token.setRefreshToken(mkFreshTokenResult.getResult());
		token.setSuccessful(true);
		return token;
	}
	
	@Override
	public void signOut(String appId, String userId) {
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(userId)) {
			return;
		}
		
		/* 删除所有令牌 */
		getSignOnUtil().removeUserTokenInApp(AuthenCacheKey.ENDUSER_CACHED_TOKEN,
				AuthenCacheKey.ENDUSER_ACCESSTOKEN_PREFIX, AuthenCacheKey.ENDUSER_REFRESHTOKEN_PREFIX, appId,
				userId);
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
	 * @return the 最终用户登录秘钥服务
	 */
	public EndUserCredentialBizService getEndUserCredentialBizService() {
		return endUserCredentialBizService;
	}

	/**
	 * @param endUserCredentialBizService the 最终用户登录秘钥服务 to set
	 */
	public void setEndUserCredentialBizService(EndUserCredentialBizService endUserCredentialBizService) {
		this.endUserCredentialBizService = endUserCredentialBizService;
	}

	/**
	 * @return the 最终用户登录账号服务
	 */
	public EndUserLoginAccountBizService getEndUserLoginAccountBizService() {
		return endUserLoginAccountBizService;
	}

	/**
	 * @param endUserLoginAccountBizService the 最终用户登录账号服务 to set
	 */
	public void setEndUserLoginAccountBizService(EndUserLoginAccountBizService endUserLoginAccountBizService) {
		this.endUserLoginAccountBizService = endUserLoginAccountBizService;
	}

	/**
	 * @return the 最终用户管理服务
	 */
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	/**
	 * @param endUserInfoBizService the 最终用户管理服务 to set
	 */
	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
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

	@Override
	public SignOnGrant getSignOnGrant(String appId, String seriesId, String redirectURI, String loginAccount, String password) {
		SignOnGrant grant = new SignOnGrant();
		grant.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(loginAccount) || EEBeanUtils.isNULL(password)) {
			grant.setRSBizCode(ABBizCode.AB0006);
			grant.addMessage("参数不完整("+this.getClass().getName()+")");
			return grant;
		}
		
		/* 计算传入的最终用户登录密码明文 */
		String passwordPlaintext = null;
		try {
			passwordPlaintext = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), password, 5);
			if (EEBeanUtils.isNULL(passwordPlaintext)) {
				grant.setRSBizCode(ABBizCode.AB0006);
				grant.addMessage("无法解密提供的最终用户登录密码("+this.getClass().getName()+")");
				return grant;
			}
		} catch (EncryptException e) {
			grant.setRSBizCode(ABBizCode.AB0006);
			grant.addMessage(e.toString());
			return grant;
		}
		
		/* 检查业务应用app是否存在，跳转地址是否合法(仅web应用) */
		SimpleResponse existApp = getSignOnUtil().existAPP(appId, redirectURI, getBusinessAppBizService());
		if (!existApp.isSuccessful()) {
			grant.setRSBizCode(ABBizCode.AB0006);
			grant.addMessage(existApp.getStrMessage());
			return grant;
		}
		
		BusinessApp app = null;
		if (EEBeanUtils.isNULL(seriesId)) {
			app =  businessAppBizService.retrieveApp(appId);
		}else{
			app=new BusinessApp();
			app.setSuccessful(true);
			app.setBusinessSeries(businessSeriesBizService.retrieveBusinessSeries(seriesId, appId));
		}
		
		
		if (!app.isSuccessful() ||app.getBusinessSeries()== null  ) {
			grant.addMessage("无体系系统必须指定体系id("+this.getClass().getName()+")");
			return grant;
		}
		
		
		/* 获得最终用户当前登录账号信息、统一登录秘钥信息 */
		EndUserLoginAccount loginAccountInfo = 
				getEndUserLoginAccountBizService().retrieveEndUserLoginAccountInfo(app.getBusinessSeries().getAtid() , loginAccount); //TODO
		
			
		if (!loginAccountInfo.isSuccessful()) {
			grant.setRSBizCode(ABBizCode.AB0007);
			grant.addMessage(loginAccountInfo.getStrMessage());
			return grant;
		}
		EndUserInfo endUser = loginAccountInfo.getUserInfo();
		EndUserCredential credential = getEndUserCredentialBizService().retrieveEndUserSecretKey(app.getBusinessSeries().getAtid(),endUser.getAtid(), getStorageRSADecrypt());
		
		/*
		 * 用户可能使用账号私有密码登录，所以取统一密码失败也应该继续
		 * 最终用户身份认证（提供的密码能匹配统一密码或私有密码任意一个即可）
		 * 判断密码是否能匹配，不对则返回错误信息
		 * 根据加密方式进行不同的密码匹配
		 */
		boolean passwordEqual = false;//passwordPlaintext.equals(credential.getResult());
		String encryptionType = credential.getEncryptionType();
		//统一密码标识为RSA并且解密后的明文与传入的明文一致
		if (!passwordEqual && encryptionType.equals("RSA") && passwordPlaintext.equals(credential.getPassword()) )
			passwordEqual = true;
		//统一密码标识为MD5并且密文与传入的密文（明文经MD5加密）一致
		try {
			if (!passwordEqual && encryptionType.equals("MD5") && MD5Util.encrypt(passwordPlaintext).equals(credential.getPassword()) )
				passwordEqual = true;
		} catch (EncryptException e) {
			grant.setRSBizCode(ABBizCode.AB0006);
			grant.addMessage(e.toString());
			return grant;
		}
		if (!passwordEqual) {//获得账号私有密码加密类型
			encryptionType = getEndUserLoginAccountBizService().retrieveEndUserLoginAccountInfo(app.getBusinessSeries().getAtid() ,loginAccount).getEncryptionType();//TODO
			EndUserLoginAccount accountPassword = getEndUserLoginAccountBizService().retrieveEndUserAccountPassword(app.getBusinessSeries().getAtid() ,loginAccount, getStorageRSADecrypt());//TODO
			if ( !passwordEqual && accountPassword.isSuccessful() && encryptionType.equals("RSA") && passwordPlaintext.equals(accountPassword.getAccountLoginPassword()) )
				passwordEqual = true;
			//私有密码标识为MD5并且密文与传入的密文（明文经MD5加密）一致
			try {
				if ( !passwordEqual && accountPassword.isSuccessful() && encryptionType.equals("MD5") && MD5Util.encrypt(passwordPlaintext).equals(accountPassword.getAccountLoginPassword()) )
					passwordEqual = true;
			} catch (EncryptException e) {
				grant.setRSBizCode(ABBizCode.AB0006);
				grant.addMessage(e.toString());
				return grant;
			}
			
		}
		if (!passwordEqual) {
			grant.setRSBizCode(ABBizCode.AB0007);
			grant.addMessage("最终用户登录账号或密码错误("+this.getClass().getName()+")");
			return grant;
		}
		
		/* 生成并缓存code */
		StringResponse makeCodeResult = 
				getSignOnUtil().makeGrantCode(AuthenCacheKey.ENDUSER_GRANTCODE_PREFIX, appId, endUser.getAtid()+":"+app.getBusinessSeries().getAtid());
		grant.setSuccessful(makeCodeResult.isSuccessful());
		if (makeCodeResult.isSuccessful())
			grant.setGrantCode(makeCodeResult.getResult());
		else {
			grant.setRSBizCode(ABBizCode.AB0006);
			grant.addMessage(makeCodeResult.getStrMessage());
		}
		
		return grant;
	}
}
