package com.eenet.authen;

import com.eenet.base.BaseEntity;

/**
 * 服务消费者信息
 * 
 * @author Orion
 *
 */
public class ServiceConsumer extends BaseEntity {
	private static final long serialVersionUID = 5516691185188863869L;
	private String consumerName;// 消费者中文名
	private String secretKey;//秘钥
	
	/**
	 * @return 消费者中文名
	 */
	public String getConsumerName() {
		return consumerName;
	}
	/**
	 * @param consumerName 消费者中文名
	 */
	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}
	/**
	 * @return 秘钥
	 */
	public String getSecretKey() {
		return secretKey;
	}
	/**
	 * @param secretKey 秘钥
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	/**
	 * @return 登录编码
	 */
	public String getCode() {
		return this.getAtid();
	}
	/**
	 * @param code 登录编码
	 */
	public void setCode(String code) {
		this.setAtid(code);
	}
}
