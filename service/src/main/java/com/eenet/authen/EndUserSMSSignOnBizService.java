package com.eenet.authen;

import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.StringResponse;

/**
 * 通过短信验证码快速登录服务
 * 2016年8月1日
 * @author Orion
 */
public interface EndUserSMSSignOnBizService {
	
	/**
	 * 发送短信获得快速登录验证码
	 * @param appId
	 * @param mobile
	 * @return
	 * 2016年8月1日
	 * @author Orion
	 */
	public StringResponse sendSMSCode4Login(String appId, long mobile);
	
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
}
