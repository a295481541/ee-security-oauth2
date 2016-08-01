package com.eenet.security.cache;

/**
 * 安全模块在缓存中的标识
 * 2016年7月20日
 * @author Orion
 */
public final class SecurityCacheKey {
	
	/**
	 * 近期有发送短信的手机号（用于限制短信发送频率）
	 * redisKey:RECENT_SEND_SMS, mapKey:[手机号码]，value:[时间戳（与1970年1月1号0时0分0秒所差的毫秒数）]
	 */
	public final static String RECENT_SEND_SMS = "RECENT_SEND_SMS";
	
	/**
	 * 最终用户重置密码短信验证码
	 * redisKey:ENDUSER_RESETPASSWORD_SMS_CODE, mapKey:[end user id]，value:[sms code]
	 */
	public final static String ENDUSER_RESETPASSWORD_SMS_CODE = "ENDUSER_RESETPASSWORD_SMS_CODE";
}
