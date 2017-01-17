package com.eenet.authen.request;

import java.io.Serializable;

/**
 * 最终用户/服务人员身份认证请求
 * 2016年5月11日
 * @author Orion
 */
public class UserAccessTokenAuthenRequest implements Serializable {
	private static final long serialVersionUID = 4294641015303128230L;
	private String userId;//最终用户id/服务人员id
	private String userAccessToken;//最终用户访问令牌/服务人员访问令牌
	private String appId;//应用标识
	private String appSecretKey;//应用接入秘钥（密文）
	private String seriesId;//业务体系id
	/**
	 * @return the 最终用户id/服务人员id
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the 最终用户id/服务人员id to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the 最终用户访问令牌/服务人员访问令牌
	 */
	public String getUserAccessToken() {
		return userAccessToken;
	}
	/**
	 * @param userAccessToken the 最终用户访问令牌/服务人员访问令牌 to set
	 */
	public void setUserAccessToken(String userAccessToken) {
		this.userAccessToken = userAccessToken;
	}
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
	public String getSeriesId() {
		return seriesId;
	}
	public void setSeriesId(String seriesId) {
		this.seriesId = seriesId;
	}
	
}
