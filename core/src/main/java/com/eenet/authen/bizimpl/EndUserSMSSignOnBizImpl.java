package com.eenet.authen.bizimpl;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserSMSSignOnBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.BooleanResponse;
import com.eenet.base.StringResponse;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.security.PreRegistEndUserBizService;
import com.eenet.security.cache.SecurityCacheKey;
import com.eenet.sms.SendSMSBizService;
import com.eenet.util.EEBeanUtils;

public class EndUserSMSSignOnBizImpl implements EndUserSMSSignOnBizService {

	@Override
	public StringResponse sendSMSCode4Login(String appId, long mobile) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || mobile<13000000000l) {
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
		
		/* 检查该手机所属用户信息 */
		BooleanResponse existUser = getPreRegistEndUserBizService().existMobileEmailId(String.valueOf(mobile), null, null);
		if ( !existUser.isResult() )
			;
		
		return null;
	}

	@Override
	public AccessToken getAccessToken(AppAuthenRequest appRequest, long mobile, String smsCode) {
		return null;
	}
	
	private EndUserInfoBizService endUserInfoBizService;//用户基本信息服务
	private RedisClient RedisClient;//Redis客户端
	private SendSMSBizService sendSMSBizService;//发送短信服务
	private PreRegistEndUserBizService preRegistEndUserBizService;//注册新用户服务预先检查服务
	/**
	 * @return the 用户基本信息服务
	 */
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	/**
	 * @param endUserInfoBizService the 用户基本信息服务 to set
	 */
	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
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
	 * @param sendSMSBizService the 发送短信服务  to set
	 */
	public void setSendSMSBizService(SendSMSBizService sendSMSBizService) {
		this.sendSMSBizService = sendSMSBizService;
	}

	/**
	 * @return the 注册新用户服务预先检查服务
	 */
	public PreRegistEndUserBizService getPreRegistEndUserBizService() {
		return preRegistEndUserBizService;
	}

	/**
	 * @param preRegistEndUserBizService the 注册新用户服务预先检查服务 to set
	 */
	public void setPreRegistEndUserBizService(PreRegistEndUserBizService preRegistEndUserBizService) {
		this.preRegistEndUserBizService = preRegistEndUserBizService;
	}
}
