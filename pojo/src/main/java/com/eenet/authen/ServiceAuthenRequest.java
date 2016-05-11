package com.eenet.authen;

import java.io.Serializable;

/**
 * 服务及用户身份凭证
 * @author Orion
 *
 */
public class ServiceAuthenRequest implements Serializable {
	private static final long serialVersionUID = -8522386568621829527L;
	private String consumerCode;//消费者编码
	private String consumerSecretKey;//消费者秘钥
	/**
	 * @return 消费者编码
	 */
	public String getConsumerCode() {
		return consumerCode;
	}
	/**
	 * @param 消费者编码
	 */
	public void setConsumerCode(String consumerCode) {
		this.consumerCode = consumerCode;
	}
	/**
	 * @return 消费者秘钥
	 */
	public String getConsumerSecretKey() {
		return consumerSecretKey;
	}
	/**
	 * @param 消费者秘钥
	 */
	public void setConsumerSecretKey(String consumerSecretKey) {
		this.consumerSecretKey = consumerSecretKey;
	}
}
