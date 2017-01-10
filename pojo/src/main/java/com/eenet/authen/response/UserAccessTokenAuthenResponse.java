package com.eenet.authen.response;

import com.eenet.base.SimpleResponse;

/**
 * 用户身份校验结果
 * 2016年5月11日
 * @author Orion
 */
public class UserAccessTokenAuthenResponse extends SimpleResponse {
	private static final long serialVersionUID = -2806873186264054791L;
	private boolean appIdentityConfirm = false;//业务系统身份认证结果
	private boolean userIdentityConfirm = false;//用户身份认证结果
	private String bizSeriesId;//业务体系ID
	/**
	 * @return the 业务系统身份认证结果
	 */
	public boolean isAppIdentityConfirm() {
		return appIdentityConfirm;
	}
	/**
	 * @param appIdentityConfirm the 业务系统身份认证结果 to set
	 */
	public void setAppIdentityConfirm(boolean appIdentityConfirm) {
		this.appIdentityConfirm = appIdentityConfirm;
	}
	/**
	 * @return the 用户身份认证结果
	 */
	public boolean isUserIdentityConfirm() {
		return userIdentityConfirm;
	}
	/**
	 * @param userIdentityConfirm the 用户身份认证结果 to set
	 */
	public void setUserIdentityConfirm(boolean userIdentityConfirm) {
		this.userIdentityConfirm = userIdentityConfirm;
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
