package com.eenet.authen;

import com.eenet.base.SimpleResponse;

/**
 * 服务消费者服务
 * 2016年4月7日
 * @author Orion
 */
public interface ServiceConsumerBizService {
	/**
	 * 服务消费者注册，登录编码不可设置（由系统产生）
	 * @param consumer 设置消费者中文名和秘钥
	 * @return 附带登录编码，不带身份认证秘钥
	 */
	public ServiceConsumer registeServiceConsumer(ServiceConsumer consumer);
	/**
	 * 废弃服务消费者
	 * @param code 登录编码
	 * @return
	 */
	public SimpleResponse removeServiceConsumer(String... codes);
	/**
	 * 取得服务消费者对象
	 * @param code
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	public ServiceConsumer retrieveServiceConsumer(String code);
}
