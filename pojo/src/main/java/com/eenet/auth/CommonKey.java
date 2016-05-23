package com.eenet.auth;

public final class CommonKey {
	/**
	 * 服务消费者编码（唯一标识）
	 */
	public final static String SERVICE_CONSUMER_CODE = "consumer_code";
	/**
	 * 服务消费者秘钥
	 */
	public final static String SERVICE_CONSUMER_SECRET = "consumer_secret";
	/**
	 * 最终用户登录账号
	 */
	public final static String ENDUSER_MAIN_ACCOUNT = "enduser_main_account";
	/**
	 * 最终用户访问令牌
	 */
	public final static String ENDUSER_ACCESS_TOCKEN = "enduser_access_tocken";
	/**
	 * 单点登录接入系统ID
	 */
	public final static String THIRDPARTY_APP_ID = "thirdparty_app_id";
	/**
	 * 单点登录接入系统秘钥
	 */
	public final static String THIRDPARTY_APP_SECRET = "thirdparty_app_secret";
	/**
	 * 身份确认结果标识【含：服务消费者、最终用户身份、第三方系统（接入SSO认证的业务系统）】
	 */
	public final static String IDENTITY_CONFIRM = "identity_confirm";
	/**
	 * 身份验证失败原因
	 */
	public final static String IDENTITY_CONFIRM_FAIL_REASON = "identity_confirm_fail_reason";
	/**
	 * 身份验证失败来源哪里（@see com.eenet.auth.IdentityConfirmFailFrom）
	 */
	public final static String IDENTITY_CONFIRM_FAIL_FROM = "identity_confirm_fail_from";
}
