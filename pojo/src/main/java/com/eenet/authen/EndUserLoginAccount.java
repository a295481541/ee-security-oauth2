package com.eenet.authen;

import com.eenet.base.BaseEntity;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.common.BackupDeletedData;
import com.eenet.common.BackupUpdatedData;

/**
 * 最终用户登录账号信息
 * @author Orion
 *
 */
public class EndUserLoginAccount extends BaseEntity implements BackupDeletedData,BackupUpdatedData {
	private static final long serialVersionUID = -6762737260719096975L;
	private EndUserInfo userInfo;//用户基本信息
	private String loginAccount;//登录账号
	private LoginAccountType accountType;//登录账号类型
	private String accountLoginPassword;//账号登录密码
	private String encryptionType = "RSA";//加密方式，RSA或MD5，默认RSA
	private BusinessSeries businessSeries;
	/**
	 * @return the 用户基本信息
	 */
	public EndUserInfo getUserInfo() {
		return userInfo;
	}
	/**
	 * @param userInfo the 用户基本信息 to set
	 */
	public void setUserInfo(EndUserInfo userInfo) {
		this.userInfo = userInfo;
	}
	/**
	 * @return the 登录账号
	 */
	public String getLoginAccount() {
		return loginAccount;
	}
	/**
	 * @param loginAccount the 登录账号 to set
	 */
	public void setLoginAccount(String loginAccount) {
		this.loginAccount = loginAccount;
	}
	/**
	 * @return the 登录账号类型
	 */
	public LoginAccountType getAccountType() {
		return accountType;
	}
	/**
	 * @param accountType the 登录账号类型 to set
	 */
	public void setAccountType(LoginAccountType accountType) {
		this.accountType = accountType;
	}
	/**
	 * @return the 账号登录密码
	 */
	public String getAccountLoginPassword() {
		return accountLoginPassword;
	}
	/**
	 * @param accountLoginPassword the 账号登录密码 to set
	 */
	public void setAccountLoginPassword(String accountLoginPassword) {
		this.accountLoginPassword = accountLoginPassword;
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
