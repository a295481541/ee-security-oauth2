package com.eenet.authen;

import com.eenet.base.BaseEntity;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.common.BackupDeletedData;
import com.eenet.common.BackupUpdatedData;

/**
 * 最终用户登录密码
 * @author Orion
 *
 */
public class EndUserCredential extends BaseEntity implements BackupDeletedData,BackupUpdatedData {
	private static final long serialVersionUID = 1650933617094538884L;
	private EndUserInfo endUser;//最终用户信息
	private String password;//用户登录密码（区别于账号登录密码）
	private String encryptionType = "RSA";//加密方式，RSA或MD5，默认RSA
	private BusinessSeries businessSeries;//业务体系
	/**
	 * @return the 最终用户信息
	 */
	public EndUserInfo getEndUser() {
		return endUser;
	}
	/**
	 * @param endUser the 最终用户信息 to set
	 */
	public void setEndUser(EndUserInfo endUser) {
		this.endUser = endUser;
	}
	/**
	 * @return the 用户登录密码
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the 用户登录密码 to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the 加密方式，RSA或MD5，默认RSA
	 */
	public String getEncryptionType() {
		return encryptionType;
	}
	/**
	 * @param encryptionType the 加密方式，RSA或MD5，默认RSA to set
	 */
	public void setEncryptionType(String encryptionType) {
		this.encryptionType = encryptionType;
	}
	
	/**
	 * @return the 业务体系
	 */
	public BusinessSeries getBusinessSeries() {
		return businessSeries;
	}
	
	/**
	 * @param encryptionType the 业务体系 to set
	 */
	public void setBusinessSeries(BusinessSeries businessSeries) {
		this.businessSeries = businessSeries;
	}
	
	
}