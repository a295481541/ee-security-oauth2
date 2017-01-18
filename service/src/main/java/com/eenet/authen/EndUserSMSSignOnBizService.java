package com.eenet.authen;

import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;

/**
 * 通过短信验证码快速登录服务
 * 2016年8月1日
 * @author Orion
 */
public interface EndUserSMSSignOnBizService {
	
	/**
	 * 发送短信获得快速登录验证码
	 * 仅限已知业务体系的系统使用
	 * @param appId 业务系统标识
	 * @param mobile AUTHEN_ENDUSER_LOGIN_ACCOUNT表中的账户
	 * @return
	 * 2016年8月1日
	 * @author Orion
	 */
	public SimpleResponse sendSMSCode4Login(String appId, long mobile);
	
	
	/**
	 * 发送短信获得快速登录验证码
	 * 业务系统可以是已知业务体系的系统，也可以是未知业务体系的系统。
	 * 未知业务体系的系统，手机号码（已作为账号的手机号码）依据是业务体系id；
	 * 已知业务体系的系统，校验业务体系id与业务系统所属业务体系id是否一致，除非业务体系id(bizSeriesId)为空
	 * @param appId    业务系统标识
	 * @param bizSeriesId 业务体系id。当appId是已知业务体系的系统，可空
	 * @param mobile   手机号码（已作为账号的手机号码）
	 * @return
	 * 2017年1月10日
	 * @author koop
	 */
	public SimpleResponse sendSMSCode4Login(String appId, String bizSeriesId, long mobile);
	
	/**
	 * 获得访问授权码（通过短信验证码）
	 * 对于未知业务体系的系统，必须指定业务体系id
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
	public SimpleResponse validateSMSCode4Login(String appId,  String seriesId,long mobile, String smsCode, boolean rmSmsCode);
}
