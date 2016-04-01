package com.eenet.authen;

import com.eenet.base.BaseEntity;

public class EENetEndUserLoginAccount extends BaseEntity {
	private static final long serialVersionUID = -6762737260719096975L;
	private EENetEndUserMainAccount mainAccount;
	private String loginAccount;
	/**
	 * 主账号
	 * @return
	 * 2016年4月1日
	 * @author Orion
	 */
	public EENetEndUserMainAccount getMainAccount() {
		return mainAccount;
	}
	public void setMainAccount(EENetEndUserMainAccount mainAccount) {
		this.mainAccount = mainAccount;
	}
	/**
	 * 登录账号
	 * @return
	 * 2016年4月1日
	 * @author Orion
	 */
	public String getLoginAccount() {
		return loginAccount;
	}
	public void setLoginAccount(String loginAccount) {
		this.loginAccount = loginAccount;
	}
	
}
