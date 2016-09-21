package com.eenet.model;

import java.util.List;

import com.eenet.authen.EndUserLoginAccount;

public class EndUserLoginAccountListModel {
	private List<EndUserLoginAccount> m;
	
	
	public EndUserLoginAccountListModel(List<EndUserLoginAccount> m) {
		super();
		this.m = m;
	}

	public EndUserLoginAccountListModel() {
		super();
	}

	/**
	 * @return the m
	 */
	public List<EndUserLoginAccount> getM() {
		return m;
	}

	/**
	 * @param m the m to set
	 */
	public void setM(List<EndUserLoginAccount> m) {
		this.m = m;
	}
	
}
