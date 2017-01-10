package com.eenet.authen;

import com.eenet.base.BaseEntity;
import com.eenet.common.BackupDeletedData;
import com.eenet.common.BackupUpdatedData;
/**
 * 业务系统信息
 * @author Orion
 */
public class BusinessApp extends BaseEntity implements BackupDeletedData,BackupUpdatedData {
	private static final long serialVersionUID = 8516730683890875834L;
	private String redirectURIPrefix;//跳转地址前缀
	private String secretKey;//应用接入秘钥
	private String appName;//应用中文名称
	private BusinessAppType appType;//应用类型
	private BusinessSeries businessSeries;//业务体系
	/**
	 * @return the 应用标识
	 */
	public String getAppId() {
		return super.getAtid();
	}
	/**
	 * 
	 * @param appId the 应用标识 to set
	 */
	public void setAppId(String appId) {
		this.setAtid(appId);
	}
	/**
	 * @return the 跳转地址前缀
	 */
	public String getRedirectURIPrefix() {
		return redirectURIPrefix;
	}
	/**
	 * @param redirectURIPrefix the 跳转地址前缀 to set
	 */
	public void setRedirectURIPrefix(String redirectURIPrefix) {
		this.redirectURIPrefix = redirectURIPrefix;
	}
	/**
	 * @return the 应用接入秘钥
	 */
	public String getSecretKey() {
		return secretKey;
	}
	/**
	 * @param secretKey the 应用接入秘钥 to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	/**
	 * @return the 应用中文名称
	 */
	public String getAppName() {
		return appName;
	}
	/**
	 * @param appName the 应用中文名称 to set
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
	 * @return the 应用类型
	 */
	public BusinessAppType getAppType() {
		return appType;
	}
	/**
	 * @param appType the 应用类型 to set
	 */
	public void setAppType(BusinessAppType appType) {
		this.appType = appType;
	}
	
	/**
	 * @return the 业务体系
	 */
	public BusinessSeries getBusinessSeries() {
		return businessSeries;
	}
	/**
	 * @param appType the 业务体系 to set
	 */
	public void setBusinessSeries(BusinessSeries businessSeries) {
		this.businessSeries = businessSeries;
	}
	
}
