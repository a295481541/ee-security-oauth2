package com.eenet.authen;

import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;

/**
 * 通过短信验证码快速登录服务
 * 2016年8月1日
 * @author Orion
 */
public interface EndUserSMSSignOnBizService {//TODO
	
	/**
	 * 发送短信获得快速登录验证码
	 * @param appId 业务系统标识
	 * @param mobile AUTHEN_ENDUSER_LOGIN_ACCOUNT表中的账户
	 * @return
	 * 2016年8月1日
	 * @author Orion
	 */
	public SimpleResponse sendSMSCode4Login(String appId, long mobile);
	
	
	/**
	 * 发送短信获得快速登录验证码
	 * @param appId    业务系统标识
	 * @param seriesId 业务体系标识
	 * @param mobile   AUTHEN_ENDUSER_LOGIN_ACCOUNT表中的账户
	 * @return
	 * 2017年1月10日
	 * @author koop
	 */
	public SimpleResponse sendSMSCode4Login(String appId, String seriesId ,long mobile);
	
	/**
	 * 获得访问授权码（通过短信验证码）
	 * @param appRequest
	 * @param mobile
	 * @param smsCode
	 * @return
	 * 2016年8月1日
	 * @author Orion
	 */
	public AccessToken getAccessToken(AppAuthenRequest appRequest, long mobile, String smsCode);
	
	/**
	 * 校验快速登录验证码
	 * @param appId
	 * @param mobile
	 * @param smsCode
	 * @param rmSmsCode
	 * @return
	 * 2016年8月2日
	 * @author Orion
	 */
	public SimpleResponse validateSMSCode4Login(String appId, long mobile, String smsCode, boolean rmSmsCode);
}
