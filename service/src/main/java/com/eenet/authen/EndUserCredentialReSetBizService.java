package com.eenet.authen;

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
	 * @return
	 * 2016年7月18日
	 * @author Orion
	 */
	public StringResponse sendSMSCode4ResetPassword(String appId, long mobile);
	
	/**
	 * 校验重置密码短信验证码，生成重置码（一次性）
	 * @param endUserId
	 * @param smsCode
	 * @return
	 * 2016年7月18日
	 * @author Orion
	 */
	public StringResponse validateSMSCode4ResetPassword(String endUserId, String smsCode);
	
	/**
	 * 使用短信验证码重置密码，并模拟登陆获得访问令牌
	 * @param 应用身份对象
	 * @param curCredential
	 * @param resetCode
	 * @return
	 * 2016年7月18日
	 * @author Orion
	 */
	public AccessToken resetPasswordBySMSCode(AppAuthenRequest appID, EndUserCredential curCredential, String resetCode);
	
	/**
	 * 使用短信验证码重置密码
	 * @param curCredential
	 * @param resetCode
	 * @return
	 * 2016年7月18日
	 * @author Orion
	 */
	public SimpleResponse resetPasswordBySMSCode(EndUserCredential curCredential, String resetCode);
}
