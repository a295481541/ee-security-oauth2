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
	 * key:appId，value:序列化的ThirdPartySSOAPP对象
	 */
	public final static String SSO_APP = "3PARTY_SSO_APP";
	/**
	 * 授权码前缀
	 * key: AUTHEN_CODE:[grant code]:[appid], value: user main account
	 */
	public final static String AUTHEN_CODE_PREFIX = "AUTHEN_CODE";
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
	/**
	 * enduser access token
	 * key: ENDUSER_ACCESS_TOKEN:[acess token]:[appid], value: main account
	 */
	public final static String ACCESS_TOKEN_PREFIX = "ENDUSER_ACCESS_TOKEN";
	/**
	 * token for get new access token
	 * key: ENDUSER_REFRESS_TOKEN:[refress token]:[appid], value: main account
	 */
	public final static String REFRESS_TOKEN_PREFIX = "ENDUSER_REFRESS_TOKEN";
}
