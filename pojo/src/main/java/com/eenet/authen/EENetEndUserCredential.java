package com.eenet.authen;

import com.eenet.base.BaseEntity;

public class EENetEndUserCredential extends BaseEntity {
	private static final long serialVersionUID = 1650933617094538884L;
	private EENetEndUserMainAccount mainAccount;
	private String secretKey;
	
	public EENetEndUserMainAccount getMainAccount() {
		return mainAccount;
	}
	public void setMainAccount(EENetEndUserMainAccount mainAccount) {
		this.mainAccount = mainAccount;
	}
	
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
