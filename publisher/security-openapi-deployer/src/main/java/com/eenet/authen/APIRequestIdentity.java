package com.eenet.authen;

import com.eenet.authen.request.UserAccessTokenAuthenRequest;

public class APIRequestIdentity extends UserAccessTokenAuthenRequest {
	private static final long serialVersionUID = -3394269099161009562L;
	private String userType;//用户类型(endUser,adminUser,anonymous，sysUser)
	/**
	 * @return the 用户类型(endUser,adminUser,anonymous，sysUser)
	 */
	public String getUserType() {
		return userType;
	}
	/**
	 * @param userType the 用户类型(endUser,adminUser,anonymous，sysUser) to set
	 */
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
}
