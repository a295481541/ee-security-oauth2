package com.eenet;

/**
 * 安全系统各业务在缓存中的标识
 * @author Orion
 * 2016年6月6日
 */
public final class SecurityCacheKey {
	
	/**
	 * 业务应用系统
	 * redisKey:BIZ_APP, mapKey:appId，value:业务系统对象(@see com.eenet.authen.BusinessApp)
	 */
	public final static String BIZ_APP = "BIZ_APP";
	
	/**
	 * 业务体系
	 * redisKey:BIZ_SERIES, mapKey:seriesId，value:业务体系对象(@see com.eenet.authen.BusinessSeries)
	 */
	public final static String BIZ_SERIES = "BIZ_SERIES";
	
	/**
	 * 近期有发送短信的手机号（用于限制短信发送频率）
	 * redisKey = RECENT_SEND_SMS, mapKey = [seriesId]:[mobile], value = [时间戳（与1970年1月1号0时0分0秒所差的毫秒数）]
	 */
	public final static String RECENT_SEND_SMS = "RECENT_SEND_SMS";
	
	
	/****************************************************************************
	**                                                                         **
	**                        End User 缓存数据结构（Redis）                                                         **
	**                                                                         **
	****************************************************************************/
	
	/**
	 * 最终用户登录账号
	 * redisKey = ENDUSER_LOGIN_ACCOUNT, mapKey = [seriesId]:[loginAccount], value = [EncryptionType]:[最终用户登录账号对象(@see com.eenet.authen.EndUserLoginAccount)]
	 */
	public final static String ENDUSER_LOGIN_ACCOUNT = "ENDUSER_LOGIN_ACCOUNT";
	/**
	 * 最终用户登录密码
	 * redisKey = ENDUSER_CREDENTIAL, mapKey = [seriesId]:[endUserId], value = [EncryptionType]##[密码密文]
	 */
	public final static String ENDUSER_CREDENTIAL = "ENDUSER_CREDENTIAL";
	/**
	 * 最终用户登录授权码前缀
	 * key = ENDUSER_GRANTCODE:[appId]:[grantCode], value = [endUserId]:[seriesId]
	 */
	public final static String ENDUSER_GRANTCODE_PREFIX = "ENDUSER_GRANTCODE";
	/**
	 * 最终用户访问令牌前缀
	 * key = ENDUSER_ACCESSTOKEN:[appId]:[accessToken], value = [endUserId]:[seriesId]
	 */
	public final static String ENDUSER_ACCESSTOKEN_PREFIX = "ENDUSER_ACCESSTOKEN";
	/**
	 * 最终用户刷新令牌前缀
	 * key = ENDUSER_REFRESHTOKEN:[appId]:[refreshToken], value = [endUserId]:[seriesId]
	 */
	public final static String ENDUSER_REFRESHTOKEN_PREFIX = "ENDUSER_REFRESHTOKEN";
	/**
	 * 最终用户已缓存令牌
	 * key = ENDUSER_CACHED_TOKEN:[seriesId]:[appId]:[endUserId], value = [accessToken]:[refreshToken]
	 */
	public final static String ENDUSER_CACHED_TOKEN = "ENDUSER_CACHED_TOKEN";
	
	/**
	 * 最终用户短信验证码登录
	 * key: ENDUSER_FASTLOGIN_SMS_CODE:[seriesId]:[appId]:[mobile]， value:[SMS code]
	 */
	public final static String ENDUSER_FASTLOGIN_SMS_CODE_PREFIX = "ENDUSER_FASTLOGIN_SMS_CODE";
	
	/**
	 * 最终用户重置密码短信验证码
	 * redisKey:ENDUSER_RESETPASSWORD_SMS_CODE, mapKey:[seriesId]:[endUserId]，value:[sms code]
	 */
	public final static String ENDUSER_RESETPASSWORD_SMS_CODE = "ENDUSER_RESETPASSWORD_SMS_CODE";
	
	/****************************************************************************
	**                                                                         **
	**                        Admin User 缓存数据结构（Redis）                                                    **
	**                                                                         **
	****************************************************************************/
	
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
	 * 服务人员登录授权码前缀
	 * key: ADMINUSER_GRANTCODE:[appid]:[grantCode], value: 服务人员标识(String)
	 */
	public final static String ADMINUSER_GRANTCODE_PREFIX = "ADMINUSER_GRANTCODE";
	/**
	 * 服务人员访问令牌前缀
	 * key: ADMINUSER_ACCESSTOKEN:[appid]:[accessToken], value: 服务人员标识(String)
	 */
	public final static String ADMINUSER_ACCESSTOKEN_PREFIX = "ADMINUSER_ACCESSTOKEN";
	/**
	 * 服务人员刷新令牌前缀
	 * key: ADMINUSER_REFRESHTOKEN:[appid]:[refresh token], value: 服务人员标识(String)
	 */
	public final static String ADMINUSER_REFRESHTOKEN_PREFIX = "ADMINUSER_REFRESHTOKEN";
	/**
	 * 服务人员已缓存令牌
	 * key: ADMINUSER_CACHED_TOKEN:[appid]:[adminUserId], value: [accessToken]:[refresh token]
	 */
	public final static String ADMINUSER_CACHED_TOKEN = "ADMINUSER_CACHED_TOKEN";
	
	private SecurityCacheKey() {}
}
