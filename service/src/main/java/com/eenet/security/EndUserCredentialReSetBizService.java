package com.eenet.security;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;

/**
 * 最终用户密码重置服务
 * 2016年7月18日
 * @author Orion
 */
public interface EndUserCredentialReSetBizService {
	
	/**
	 * 发送重置密码短信验证码
	 * @param appId
	 * @param mobile
	 * @return 手机所属用户标识（enduser id）
	 * 2016年7月18日
	 * @author Orion
	 */
	public StringResponse sendSMSCode4ResetPassword(String appId, long mobile);
	
	/**
	 * 校验重置密码短信验证码
	 * @param endUserId
	 * @param smsCode
	 * @param rmSmsCode 是否同时删除验证
	 * @return
	 * 2016年7月18日
	 * @author Orion
	 */
	public SimpleResponse validateSMSCode4ResetPassword(String endUserId, String smsCode, boolean rmSmsCode);
	
	/**
	 * 使用短信验证码重置密码，并模拟登陆获得访问令牌
	 * 如果被重置密码的用户没有手机登录账号则自动创建一个
	 * @param 应用身份对象
	 * @param curCredential password属性进行带时间戳加密
	 * @param resetCode
	 * @param mobile
	 * @return
	 * 2016年7月18日
	 * @author Orion
	 */
	public AccessToken resetPasswordBySMSCodeWithLogin(AppAuthenRequest appRequest, EndUserCredential credential, String smsCode, String mobile);
	
	/**
	 * 使用短信验证码重置密码
	 * 如果被重置密码的用户没有手机登录账号则自动创建一个
	 * @param credential password属性进行带时间戳加密
	 * @param resetCode
	 * @param mobile
	 * @return
	 * 2016年7月18日
	 * @author Orion
	 */
	public SimpleResponse resetPasswordBySMSCodeWithoutLogin(EndUserCredential credential, String smsCode, String mobile);
}
