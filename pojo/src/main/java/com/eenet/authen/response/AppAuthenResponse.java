package com.eenet.authen.response;

import java.io.Serializable;

/**
 * 应用系统校验结果
 * 2017年1月10日
 * @author Orion
 */
public class AppAuthenResponse implements Serializable {
	private static final long serialVersionUID = -1805313524940128660L;
	private boolean appIdentityConfirm = false;//业务系统身份认证结果
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
