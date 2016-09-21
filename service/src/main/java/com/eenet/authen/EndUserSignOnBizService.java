package com.eenet.authen;

/**
 * 最终用户登录服务，身份认证服务见：
 * @see com.eenet.authen.IdentityAuthenticationBizService
 * 2016年3月29日
 * 
 * @author Orion
 */
public interface EndUserSignOnBizService {
	
	/**
	 * 获得认证授权码，授权码仅可使用一次
	 * @param appId 应用标识
	 * @param redirectURI 跳转地址（非web系统可空）
	 * @param loginAccount 登录账号
	 * @param password 最终用户登录密码，带时间戳加密的形式。可以是最终用户统一密码，也可以是账单账号私有密码
	 * @return 授权码
	 */
	public SignOnGrant getSignOnGrant(String appId, String redirectURI, String loginAccount, String password);
	
	/**
	 * 获得访问授权码
	 * 业务规则：web应用5分钟内、native应用7天内无操作则accessToken失效，通过refreshToken重新获取
	 * 适用场景：只要30天内有访问系统，则可以持续保持自动登录
	 * @param appId 业务应用标识
	 * @param secretKey 业务应用秘钥，带时间戳加密的形式
	 * @param grantCode 访问授权码
	 * @return
	 * 2016年4月15日
	 * @author Orion
	 */
	public AccessToken getAccessToken(String appId, String secretKey, String grantCode);
	
	/**
	 * 获得访问授权码
	 * 业务规则：用A系统的刷新令牌获得B系统的访问令牌
	 * 适用场景：系统间跳转
	 * @param fromAppId 来源业务应用标识
	 * @param toAppId 目标业务应用标识
	 * @param secretKey 目标业务应用秘钥，带时间戳加密的形式
	 * @param endUserId 来源业务应用登录用户的id
	 * @param refreshToken 来源业务应用登录用户的refreshToken
	 * @return
	 */
	public AccessToken getAccessToken(String fromAppId, String toAppId, String secretKey, String endUserId , String refreshToken);
	
	/**
	 * 刷新访问授权码
	 * 业务规则：当accessToken失效后通过refreshToken重新获取，有效期30天
	 * 当refreshToken作重新获取accessToken时，一次性失效并重新颁发
	 * 当refreshToken用于系统跳转时，可多次使用
	 * @param appId 业务应用标识
	 * @param secretKey 业务应用秘钥，带时间戳加密的形式
	 * @param refreshToken
	 * @param endUserId
	 * @return
	 * 2016年4月21日
	 * @author Orion
	 */
	public AccessToken refreshAccessToken(String appId, String secretKey, String refreshToken, String endUserId);
	
	/**
	 * 最终用户登出
	 * @param appId 业务应用标识
	 * @param userId 登出用户标识
	 * 2016年7月7日
	 * @author Orion
	 */
	public void signOut(String appId, String userId);
}
