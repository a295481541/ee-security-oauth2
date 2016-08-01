package com.eenet.authen.cacheSyn;

/**
 * 认证模块在缓存中的标识
 * @author Orion
 * 2016年6月6日
 */
public final class AuthenCacheKey {
	
	/**
	 * 业务应用系统
	 * redisKey:BIZ_APP, mapKey:appId，value:序列化的BusinessApp对象
	 */
	public final static String BIZ_APP = "BIZ_APP";
	
	
	/**
	 * 服务人员登录账号
	 * redisKey:ADMINUSER_LOGIN_ACCOUNT, mapKey:登录账号，value:服务人员对象(@see com.eenet.authen.AdminUserLoginAccount)
	 */
	public final static String ADMINUSER_LOGIN_ACCOUNT = "ADMINUSER_LOGIN_ACCOUNT";
	/**
	 * 服务人员登录密码
	 * redisKey:ADMINUSER_CREDENTIAL, mapKey:对应服务人员ID，value:[加密方式]##服务人员登录密码(String)
	 */
	public final static String ADMINUSER_CREDENTIAL = "ADMINUSER_CREDENTIAL";
	/**
	 * 最终用户登录账号
	 * redisKey:ENDUSER_LOGIN_ACCOUNT, mapKey:登录账号，value:最终用户对象(@see com.eenet.authen.EndUserLoginAccount)
	 */
	public final static String ENDUSER_LOGIN_ACCOUNT = "ENDUSER_LOGIN_ACCOUNT";
	/**
	 * 最终用户登录密码
	 * redisKey:ENDUSER_CREDENTIAL, mapKey:对应最终用户ID，value:最终用户登录密码(String)
	 */
	public final static String ENDUSER_CREDENTIAL = "ENDUSER_CREDENTIAL";
	
	
	/**
	 * 服务人员登录授权码前缀
	 * key: ADMINUSER_GRANTCODE:[appid]:[grant code], value: 服务人员标识(String)
	 */
	public final static String ADMINUSER_GRANTCODE_PREFIX = "ADMINUSER_GRANTCODE";
	/**
	 * 服务人员访问令牌前缀
	 * key: ADMINUSER_ACCESSTOKEN:[appid]:[access token], value: 服务人员标识(String)
	 */
	public final static String ADMINUSER_ACCESSTOKEN_PREFIX = "ADMINUSER_ACCESSTOKEN";
	/**
	 * 服务人员刷新令牌前缀
	 * key: ADMINUSER_REFRESHTOKEN:[appid]:[refresh token], value: 服务人员标识(String)
	 */
	public final static String ADMINUSER_REFRESHTOKEN_PREFIX = "ADMINUSER_REFRESHTOKEN";
	/**
	 * 服务人员已缓存令牌
	 * key: ADMINUSER_CACHED_TOKEN:[appid]:[adminUserId], value: [access token]:[refresh token]
	 */
	public final static String ADMINUSER_CACHED_TOKEN = "ADMINUSER_CACHED_TOKEN";
	
	
	/**
	 * 最终用户登录授权码前缀
	 * key: ENDUSER_GRANTCODE:[appid]:[grant code], value: 最终用户标识(String)
	 */
	public final static String ENDUSER_GRANTCODE_PREFIX = "ENDUSER_GRANTCODE";
	/**
	 * 最终用户访问令牌前缀
	 * key: ENDUSER_ACCESSTOKEN:[appid]:[access token], value: 服务人员标识(String)
	 */
	public final static String ENDUSER_ACCESSTOKEN_PREFIX = "ENDUSER_ACCESSTOKEN";
	/**
	 * 最终用户刷新令牌前缀
	 * key: ENDUSER_REFRESHTOKEN:[appid]:[refresh token], value: 服务人员标识(String)
	 */
	public final static String ENDUSER_REFRESHTOKEN_PREFIX = "ENDUSER_REFRESHTOKEN";
	/**
	 * 最终用户已缓存令牌
	 * key: ENDUSER_CACHED_TOKEN:[appid]:[endUserId], value: [access token]:[refresh token]
	 */
	public final static String ENDUSER_CACHED_TOKEN = "ENDUSER_CACHED_TOKEN";
	
	/**
	 * 最终用户重置密码短信验证码
	 * redisKey:ENDUSER_FASTLOGIN_SMS_CODE, mapKey:[end user id]，value:[sms code]
	 */
	public final static String ENDUSER_FASTLOGIN_SMS_CODE = "ENDUSER_FASTLOGIN_SMS_CODE";
	
	private AuthenCacheKey() {}
}
