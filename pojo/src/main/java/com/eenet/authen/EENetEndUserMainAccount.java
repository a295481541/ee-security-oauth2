package com.eenet.authen;

/**
 * EENet终端用户主账号
 * 2016年3月30日
 * @author Orion
 */
public class EENetEndUserMainAccount implements java.io.Serializable {
	private static final long serialVersionUID = -331453201352478696L;
	private String account;
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	
}
