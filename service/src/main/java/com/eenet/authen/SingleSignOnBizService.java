package com.eenet.authen;

/**
 * 单点登录服务 2016年3月29日
 * 
 * @author Orion
 */
public interface SingleSignOnBizService {

	/**
	 * 获得认证授权码
	 * @param SSOSystem 单点登录系统身份
	 * @param appId 第三方应用
	 * @param redirectURI 跳转地址（非web系统可空）
	 * @param endUserLoginAccount 最终用户登录账号
	 * @param endUserPassword 最终用户登录密码
	 * @return 授权码
	 * 2016年4月7日
	 * @author Orion
	 */
	public SignOnGrant getSignOnGrant(ServiceConsumer SSOSystem, String appId, String redirectURI, String endUserLoginAccount,
			String endUserPassword);

}
