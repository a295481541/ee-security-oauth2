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
	private String redirectURI;//跳转地址（非web系统可空）
	private String bizSeriesId;//业务体系ID
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
	
	/**
	 * @return 跳转地址（非web系统可空）
	 * 2016年7月8日
	 * @author Orion
	 */
	public String getRedirectURI() {
		return redirectURI;
	}
	/**
	 * @param redirectURI 跳转地址（非web系统可空）
	 * 2016年7月8日
	 * @author Orion
	 */
	public void setRedirectURI(String redirectURI) {
		this.redirectURI = redirectURI;
	}
	/**
	 * @return the 业务体系ID
	 */
	public String getBizSeriesId() {
		return bizSeriesId;
	}
	/**
	 * @param bizSeriesId the 业务体系ID to set
	 */
	public void setBizSeriesId(String bizSeriesId) {
		this.bizSeriesId = bizSeriesId;
	}
	
}
