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
	private String endUserAccount;//用户主账号
	private String endUserTocken;//用户令牌
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
	/**
	 * @return 用户主账号
	 */
	public String getEndUserAccount() {
		return endUserAccount;
	}
	/**
	 * @param 用户主账号
	 */
	public void setEndUserAccount(String endUserAccount) {
		this.endUserAccount = endUserAccount;
	}
	/**
	 * @return 用户令牌
	 */
	public String getEndUserTocken() {
		return endUserTocken;
	}
	/**
	 * @param 用户令牌
	 */
	public void setEndUserTocken(String endUserTocken) {
		this.endUserTocken = endUserTocken;
	}
}
