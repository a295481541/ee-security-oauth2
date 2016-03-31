package com.eenet.authen.bizimpl;

/**
 * 业务数据在缓存中的标识
 * @author Orion
 *
 */
public class CacheKey {
	/**
	 * 服务消费者
	 * key:code，value:序列化的ServiceConsumer对象
	 */
	public final static String SERVICE_CONSUMER = "SERVICE_CONSUMER";
	/**
	 * 单点登录应用
	 * key:code，value:序列化的ThirdPartySSOAPP对象
	 */
	public final static String SSO_APP = "third_party_sso_app";
	/**
	 * 授权码前缀
	 */
	public final static String AUTHEN_CODE_PREFIX = "AUTHEN_CODE_";
	/**
	 * 最终用户登录密码
	 * key:主账号，value:秘钥
	 */
	public final static String ENDUSER_CREDENTIAL = "ENDUSER_CREDENTIAL";
	/**
	 * 最终用户登录账号
	 * key:登录账号，value:主账号
	 */
	public final static String ENDUSER_LOGIN_ACCOUNT = "ENDUSER_LOGIN_ACCOUNT";
}
