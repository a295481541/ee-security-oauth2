package com.eenet.authen.identifier;

/**
 * 远程调用时传递身份及其他信息的参数标识
 * 2016年8月20日
 * @author Orion
 */
public class RPCAuthenParamKey {
	/**
	 * 业务系统编码
	 */
	public static final String BIZ_APP_ID = "AUTHEN_BIZ_APP_ID";
	
	/**
	 * 业务系统秘钥（带时间戳加密）
	 */
	public static final String BIZ_APP_SECRETKEY = "AUTHEN_BIZ_APP_SECRETKEY";
	
	/**
	 * 业务系统合法地址
	 */
	public static final String BIZ_APP_DOMAIN = "AUTHEN_BIZ_APP_DOMAIN";
	
	/**
	 * 用户类型
	 */
	public static final String USER_TYPE = "AUTHEN_USER_TYPE";
	
	/**
	 * 最终用户/服务人员标识
	 */
	public static final String USER_ID = "AUTHEN_USER_ID";
	
	/**
	 * 访问令牌
	 */
	public static final String USER_ACCESS_TOKEN = "AUTHEN_USER_ACCESS_TOKEN";
	
	/**
	 * 身份确认结果标识【含：最终用户/服务人员、业务系统】
	 */
//	public final static String AUTHEN_CONFIRM = "AUTHEN_CONFIRM";
	
	/**
	 * 身份验证失败原因
	 */
//	public final static String AUTHEN_FAIL_REASON = "AUTHEN_FAIL_REASON";
	
	/**
	 * 身份验证失败来源哪里（@see com.eenet.auth.IdentityConfirmFailFrom）
	 */
}
