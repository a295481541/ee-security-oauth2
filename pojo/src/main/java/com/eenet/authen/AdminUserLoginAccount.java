package com.eenet.authen;

import com.eenet.base.BaseEntity;
import com.eenet.baseinfo.user.AdminUserInfo;
import com.eenet.common.BackupDeletedData;
import com.eenet.common.BackupUpdatedData;

/**
 * 服务人员登录账号信息
 * @author Orion
 *
 */
public class AdminUserLoginAccount extends BaseEntity implements BackupDeletedData,BackupUpdatedData {
	private static final long serialVersionUID = -6762737260719096975L;
	private AdminUserInfo userInfo;//服务人员基本信息
	private String loginAccount;//登录账号
	private LoginAccountType accountType;//登录账号类型
	/**
	 * @return the 服务人员基本信息
	 */
	public AdminUserInfo getUserInfo() {
		return userInfo;
	}
	/**
	 * @param userInfo the 服务人员基本信息 to set
	 */
	public void setUserInfo(AdminUserInfo userInfo) {
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
}
