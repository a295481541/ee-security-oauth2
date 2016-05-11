package com.eenet.authen;

import com.eenet.base.SimpleResponse;

/**
 * 用户身份校验结果
 * 2016年5月11日
 * @author Orion
 */
public class EENetEndUserAuthenResponse extends SimpleResponse {
	private static final long serialVersionUID = -2806873186264054791L;
	private boolean ssoSysIdentityConfirm = false;//单点登录接入系统身份认证结果
	private boolean endUseridentityConfirm = false;//用户身份认证结果
	/**
	 * @return the 单点登录接入系统身份认证结果
	 */
	public boolean isSsoSysIdentityConfirm() {
		return ssoSysIdentityConfirm;
	}
	/**
	 * @param ssoSysIdentityConfirm the 单点登录接入系统身份认证结果 to set
	 */
	public void setSsoSysIdentityConfirm(boolean ssoSysIdentityConfirm) {
		this.ssoSysIdentityConfirm = ssoSysIdentityConfirm;
	}
	/**
	 * @return the 用户身份认证结果
	 */
	public boolean isEndUseridentityConfirm() {
		return endUseridentityConfirm;
	}
	/**
	 * @param endUseridentityConfirm the 用户身份认证结果 to set
	 */
	public void setEndUseridentityConfirm(boolean endUseridentityConfirm) {
		this.endUseridentityConfirm = endUseridentityConfirm;
	}
}
