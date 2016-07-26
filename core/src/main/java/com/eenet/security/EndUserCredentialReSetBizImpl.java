package com.eenet.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.StringResponse;
import com.eenet.base.biz.GenericSimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.security.bizComponent.ReSetLoginPasswordCom;
import com.eenet.security.cache.SecurityCacheKey;
import com.eenet.sms.SendSMSBizService;
import com.eenet.sms.SendSMSBizType;
import com.eenet.sms.ShortMessageBody;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class EndUserCredentialReSetBizImpl implements EndUserCredentialReSetBizService {

	@Override
	public StringResponse sendSMSCode4ResetPassword(String appId, long mobile) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || mobile<10000000000l) {
			result.addMessage("接入应用标识或手机号不正确("+this.getClass().getName()+")");
			return result;
		}
		
		/* 检查该手机是否一分钟内发送过短信 */
		try {
			if ( getRedisClient().getMapValue(SecurityCacheKey.RECENT_SEND_SMS, String.valueOf(mobile)) != null ) {
				result.addMessage("发送短信频率太高("+this.getClass().getName()+")");
				return result;
			}
		} catch (RedisOPException e) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(e.toString());
			return result;
		}
		
		/* 获得该手机所属用户信息 */
		EndUserInfo user = getEndUserInfoBizService().getByMobileEmailId(String.valueOf(mobile), null, null);
		if (!user.isSuccessful())
			user = getEndUserLoginAccountBizService().retrieveEndUserInfo(String.valueOf(mobile));
		if (!user.isSuccessful()) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(user.getStrMessage());
			return result;
		}
		
		/* 生成短信验证码并缓存 */
		String smsCode = EEBeanUtils.randomSixNum();
		try {
			getRedisClient().addMapItem(SecurityCacheKey.RECENT_SEND_SMS, String.valueOf(mobile), System.currentTimeMillis(), 62);
			getRedisClient().addMapItem(SecurityCacheKey.ENDUSER_RESETPASSWORD_SMS_CODE, user.getAtid(), smsCode, 600);
		} catch (RedisOPException e) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(e.toString());
			return result;
		}
		
		/* 发送短信 */
		ShortMessageBody body = new ShortMessageBody();
		body.setAppId(appId);//要发送短信的应用
		body.setSendSMSBizType(SendSMSBizType.GetCodeForResetPassword);//短信的业务类型
		Map<String,List<String>> smsParam = new HashMap<String, List<String>>();
		smsParam.put("smsApiParams", Arrays.asList(smsCode));
		body.setSmsParam(smsParam);//短信模板参数（与短信接口平台耦合）
		body.setMobile(mobile);//接收手机
		SimpleResponse sendSMSRS = getSendSMSBizService().sendSMSUsingTemplate(body);
		if (!sendSMSRS.isSuccessful()) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(sendSMSRS.getStrMessage());
			return result;
		}
		
		/* 所有过程正常 */
		result.setSuccessful(true);
		result.setResult(user.getAtid());
		
		return result;
	}

	@Override
	public SimpleResponse validateSMSCode4ResetPassword(String endUserId, String smsCode, boolean rmSmsCode) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		
		/* 参数检查 */
		if (EEBeanUtils.isNULL(endUserId) || EEBeanUtils.isNULL(smsCode)) {
			result.addMessage("用户标识或手机验证码均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		/* 校验短信验证码 */
		String retrieveSmsCode = null;
		try {
			retrieveSmsCode = String.class.cast(getRedisClient().getMapValue(SecurityCacheKey.ENDUSER_RESETPASSWORD_SMS_CODE, endUserId));
			if (EEBeanUtils.isNULL(retrieveSmsCode) || !retrieveSmsCode.equals(smsCode)) {
				result.addMessage("短信验证码错误或已经失效("+this.getClass().getName()+")");
				return result;
			}
		} catch (RedisOPException e) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(e.toString());
			return result;
		}
		
		/* 删除短信验证码（如需要） */
		if (rmSmsCode) {
			try {
				getRedisClient().removeMapItem(SecurityCacheKey.ENDUSER_RESETPASSWORD_SMS_CODE, endUserId);
			} catch (RedisOPException e) {
				e.printStackTrace();//do nothing
			}
		}
		
		result.setSuccessful(true);
		return result;
	}

	@Override
	public AccessToken resetPasswordBySMSCodeWithLogin(AppAuthenRequest appRequest, EndUserCredential credential,
			String smsCode) {
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		
		/* 业务应用认证 */
		SimpleResponse appAuthenRS = getAuthenService().appAuthen(appRequest);
		if (!appAuthenRS.isSuccessful()) {
			result.addMessage(appAuthenRS.getStrMessage());
			return result;
		}
		
		/* 使用短信验证码重置密码 */
		SimpleResponse resetRS = resetPasswordBySMSCodeWithoutLogin(credential, smsCode);
		if ( !resetRS.isSuccessful() ) {
			result.addMessage(resetRS.getStrMessage());
			return result;
		}
		
		/* 获得当前用户登录账号（取一个账号） */
		QueryCondition condition = new QueryCondition();
		condition.setMaxQuantity(1);
		condition.addCondition(new ConditionItem("userInfo.atid",RangeType.EQUAL,credential.getEndUser().getAtid(),null));
		
		SimpleResultSet<EndUserLoginAccount> queryAccountRS = getGenericBiz().query(condition,EndUserLoginAccount.class);
		if ( !queryAccountRS.isSuccessful() ) {
			result.addMessage(queryAccountRS.getStrMessage());
			return result;
		}
		if ( queryAccountRS.getResultSet().size()!=1 ) {
			result.addMessage("密码重置成功，但未设置登录账号无法直接登录("+this.getClass().getName()+")");
			return result;
		}
		String loginAccount = queryAccountRS.getResultSet().get(0).getLoginAccount();
		
		/* 获得认证授权码 */
		SignOnGrant grant = getEndUserSignOnBizService().getSignOnGrant(appRequest.getAppId(),
				appRequest.getRedirectURI(), loginAccount, credential.getPassword());
		if (!grant.isSuccessful()) {
			result.addMessage(grant.getStrMessage());
			return result;
		}
		
		/* 获得访问令牌 */
		result = getEndUserSignOnBizService().getAccessToken(appRequest.getAppId(), appRequest.getAppSecretKey(), grant.getGrantCode());
		return result;
	}

	@Override
	public SimpleResponse resetPasswordBySMSCodeWithoutLogin(EndUserCredential credential, String smsCode) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if ( credential==null || EEBeanUtils.isNULL(credential.getEndUser().getAtid()) || EEBeanUtils.isNULL(smsCode) ) {
			result.addMessage("要重置密码的用户标识、新密码和短信验证码均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		/* 校验并删除短信验证码 */
		SimpleResponse smsCodeCorrect = validateSMSCode4ResetPassword(credential.getEndUser().getAtid(), smsCode, true);
		if ( !smsCodeCorrect.isSuccessful()) {
			result.addMessage(smsCodeCorrect.getStrMessage());
			return result;
		}
		
		/* 重置用户登录密码 */
		String newPasswordPlainText = null;
		try {
			newPasswordPlainText = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), credential.getPassword(), 10);//用传输私钥解出新密码明文
		} catch (EncryptException e) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(e.toString());
			return result;
		}
		
		result = getResetLoginPasswordCom().resetEndUserLoginPassword(credential.getEndUser().getAtid(),
				newPasswordPlainText, getStorageRSAEncrypt());
		return result;
	}
	
	private EndUserInfoBizService endUserInfoBizService;
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	private ReSetLoginPasswordCom resetLoginPasswordCom;//重置密码业务组件
	private RSAEncrypt StorageRSAEncrypt;//数据存储加密公钥
	private RSADecrypt TransferRSADecrypt;//数据传输解密私钥
	private RedisClient RedisClient;//Redis客户端
	private SendSMSBizService sendSMSBizService;//发送短信服务 
	private IdentityAuthenticationBizService authenService;//身份认证服务
	private GenericSimpleBizImpl genericBiz;//通用业务操作实现类
	private EndUserSignOnBizService endUserSignOnBizService;//最终用户登录服务
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
	}

	public EndUserLoginAccountBizService getEndUserLoginAccountBizService() {
		return endUserLoginAccountBizService;
	}

	public void setEndUserLoginAccountBizService(EndUserLoginAccountBizService endUserLoginAccountBizService) {
		this.endUserLoginAccountBizService = endUserLoginAccountBizService;
	}

	/**
	 * @return the 重置密码业务组件
	 */
	public ReSetLoginPasswordCom getResetLoginPasswordCom() {
		return resetLoginPasswordCom;
	}

	/**
	 * @param resetLoginPasswordCom the 重置密码业务组件 to set
	 */
	public void setResetLoginPasswordCom(ReSetLoginPasswordCom resetLoginPasswordCom) {
		this.resetLoginPasswordCom = resetLoginPasswordCom;
	}

	/**
	 * @return the 数据存储加密公钥
	 */
	public RSAEncrypt getStorageRSAEncrypt() {
		return StorageRSAEncrypt;
	}

	/**
	 * @param storageRSAEncrypt the 数据存储加密公钥 to set
	 */
	public void setStorageRSAEncrypt(RSAEncrypt storageRSAEncrypt) {
		StorageRSAEncrypt = storageRSAEncrypt;
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
	 * @return the 发送短信服务
	 */
	public SendSMSBizService getSendSMSBizService() {
		return sendSMSBizService;
	}

	/**
	 * @param sendSMSBizService the 发送短信服务 to set
	 */
	public void setSendSMSBizService(SendSMSBizService sendSMSBizService) {
		this.sendSMSBizService = sendSMSBizService;
	}

	/**
	 * @return the 身份认证服务
	 */
	public IdentityAuthenticationBizService getAuthenService() {
		return authenService;
	}

	/**
	 * @param authenService the 身份认证服务 to set
	 */
	public void setAuthenService(IdentityAuthenticationBizService authenService) {
		this.authenService = authenService;
	}

	/**
	 * @return the 通用业务操作实现类
	 */
	public GenericSimpleBizImpl getGenericBiz() {
		return genericBiz;
	}

	/**
	 * @param genericBiz the 通用业务操作实现类 to set
	 */
	public void setGenericBiz(GenericSimpleBizImpl genericBiz) {
		this.genericBiz = genericBiz;
	}

	/**
	 * @return the 最终用户登录服务
	 */
	public EndUserSignOnBizService getEndUserSignOnBizService() {
		return endUserSignOnBizService;
	}

	/**
	 * @param endUserSignOnBizService the 最终用户登录服务 to set
	 */
	public void setEndUserSignOnBizService(EndUserSignOnBizService endUserSignOnBizService) {
		this.endUserSignOnBizService = endUserSignOnBizService;
	}
}
