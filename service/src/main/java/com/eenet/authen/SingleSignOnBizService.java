package com.eenet.authen;

/**
 * 单点登录服务
 * 2016年3月29日
 * 
 * @author Orion
 */
public interface SingleSignOnBizService {

	/**
	 * 获得认证授权码，授权码仅可使用一次
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
	
	/**
	 * 获得访问授权码
	 * 业务规则：web应用5分钟内、native应用7天内无操作则accessToken失效，通过refreshToken重新获取
	 * 适用场景：只要30天内有访问系统，则可以持续保持自动登录
	 * @param appId 第三方应用标识
	 * @param secretKey 第三方应用秘钥（明文）
	 * @param grantCode 访问授权码
	 * @return
	 * 2016年4月15日
	 * @author Orion
	 */
	public AccessToken getAccessToken(String appId, String secretKey, String grantCode);
	
	/**
	 * 刷新访问授权码
	 * 业务规则：单accessToken失效后通过refreshToken重新获取，有效期30天
	 * 当refreshToken作重新获取refreshToken时，一次性失效并重新颁发
	 * 当refreshToken用于系统跳转时，可多次使用
	 * @param appId 第三方应用标识
	 * @param secretKey 第三方应用秘钥（明文）
	 * @param refreshToken
	 * @return
	 * 2016年4月21日
	 * @author Orion
	 */
	public AccessToken refreshAccessToken(String appId, String secretKey, String refreshToken);

}
