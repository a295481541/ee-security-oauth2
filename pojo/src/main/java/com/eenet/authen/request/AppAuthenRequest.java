package com.eenet.authen.request;

import java.io.Serializable;

/**
 * 业务应用身份凭证
 * @author Orion
 *
 */
public class AppAuthenRequest implements Serializable {
	private static final long serialVersionUID = -8522386568621829527L;
	private String appId;//应用标识
	private String appSecretKey;//应用接入秘钥（密文）
	/**
	 * @return the 应用标识
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId the 应用标识 to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	/**
	 * @return the 应用接入秘钥（密文）
	 */
	public String getAppSecretKey() {
		return appSecretKey;
	}
	/**
	 * @param appSecretKey the 应用接入秘钥（密文） to set
	 */
	public void setAppSecretKey(String appSecretKey) {
		this.appSecretKey = appSecretKey;
	}
	
}
