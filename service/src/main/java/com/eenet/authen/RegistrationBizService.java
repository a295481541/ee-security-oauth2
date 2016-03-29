package com.eenet.authen;

import com.eenet.base.SimpleResponse;

/**
 * 账号注册服务
 * @author Orion
 *
 */
public interface RegistrationBizService {
	/**
	 * 服务消费者注册，登录编码不可设置（由系统产生）
	 * @param consumer 设置消费者中文名和秘钥
	 * @return 附带登录编码，不带连接秘钥
	 */
	public ServiceConsumer serviceConsumerRegiste(ServiceConsumer consumer);
	
	/**
	 * 单点登录系统注册
	 * @param client
	 * @return
	 */
//	public ThirdPartSSOWebSystem thirdPartSSOWebSystemRegiste(ThirdPartSSOWebSystem client);
	
	/**
	 * 最终用户注册
	 * @param user
	 * @return
	 */
//	public EENetEndUser eeNetEndUserRegiste(EENetEndUser user);
	
	/**
	 * 废弃服务消费者
	 * @param code 登录编码
	 * @return
	 */
	public SimpleResponse serviceConsumerDrop(String... code);
}
