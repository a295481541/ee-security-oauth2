package com.eenet.authen;

/**
 * 身份认证服务
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
	 * 服务消费者与最终用户认证
	 * @param consumer
	 * @return
	 */
	public ServiceAuthenResponse consumerNUserAuthen(ServiceAuthenRequest request);
}
