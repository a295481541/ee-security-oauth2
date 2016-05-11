package com.eenet.authen;

import java.io.Serializable;

/**
 * 最终用户身份认证请求
 * 2016年5月11日
 * @author Orion
 */
public class EENetEndUserAuthenRequest implements Serializable {
	private static final long serialVersionUID = 4294641015303128230L;
	private String endUserAccount;//用户主账号
	private String endUserTocken;//用户令牌
	private String appId;//单点登录接入系统ID
	private String secretKey;//单点登录接入系统秘钥（明文）
	/**
	 * @return the 用户主账号
	 */
	public String getEndUserAccount() {
		return endUserAccount;
	}
	/**
	 * @param endUserAccount the 用户主账号 to set
	 */
	public void setEndUserAccount(String endUserAccount) {
		this.endUserAccount = endUserAccount;
	}
	/**
	 * @return the 用户令牌
	 */
	public String getEndUserTocken() {
		return endUserTocken;
	}
	/**
	 * @param endUserTocken the 用户令牌 to set
	 */
	public void setEndUserTocken(String endUserTocken) {
		this.endUserTocken = endUserTocken;
	}
	/**
	 * @return the 单点登录接入系统ID
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId the 单点登录接入系统ID to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	/**
	 * @return the 单点登录接入系统秘钥（明文）
	 */
	public String getSecretKey() {
		return secretKey;
	}
	/**
	 * @param secretKey the 单点登录接入系统秘钥（明文） to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
