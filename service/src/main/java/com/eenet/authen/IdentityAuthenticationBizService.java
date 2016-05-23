package com.eenet.authen;

/**
 * 身份认证服务，含：服务消费者、最终用户
 * @author Orion
 *
 */
public interface IdentityAuthenticationBizService {
	
	/**
	 * 服务消费者认证
	 * @param consumer 
	 * @return
	 */
	public ServiceAuthenResponse consumerAuthen(ServiceAuthenRequest request);
	
	/**
	 * 最终用户认证
	 * 同时认证用户身份和集成单点登录的业务系统身份
	 * @param consumer
	 * @return
	 */
	public EENetEndUserAuthenResponse endUserAuthen(EENetEndUserAuthenRequest request);
	
	/**
	 * 检查服务提供者状态
	 * @return
	 * 2016年3月29日
	 * @author Orion
	 */
	public boolean authenServiceProviderPing();
}
