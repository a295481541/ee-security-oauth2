package com.eenet.authen;

import com.eenet.base.BaseEntity;
import com.eenet.common.BackupDeletedData;
import com.eenet.common.BackupUpdatedData;
import com.eenet.user.AdminUserInfo;

/**
 * 服务人员登录密码
 * @author Orion
 *
 */
public class AdminUserCredential extends BaseEntity implements BackupDeletedData,BackupUpdatedData {
	private static final long serialVersionUID = 1650933617094538884L;
	private AdminUserInfo adminUser;//服务人员信息
	private String password;//服务人员登录密码
	private String encryptionType = "RSA";//加密方式，RSA或MD5，默认RSA
	
	/**
	 * @return the 服务人员信息
	 */
	public AdminUserInfo getAdminUser() {
		return adminUser;
	}
	/**
	 * @param adminUser the 服务人员信息 to set
	 */
	public void setAdminUser(AdminUserInfo adminUser) {
		this.adminUser = adminUser;
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
}