package com.eenet.authen;

import com.eenet.base.BaseEntity;

/**
 * 登录授权信息
 * 2016年3月30日
 * @author Orion
 */
public class SignOnGrant extends BaseEntity {
	private static final long serialVersionUID = -455346407809610530L;
	private String grantCode;//登录授权码
	/**
	 * @return 登录授权码
	 * 2016年3月30日
	 * @author Orion
	 */
	public String getGrantCode() {
		return grantCode;
	}
	public void setGrantCode(String grantCode) {
		this.grantCode = grantCode;
	}
	
}
