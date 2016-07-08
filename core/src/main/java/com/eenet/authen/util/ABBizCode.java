package com.eenet.authen.util;

import com.eenet.common.code.BizCode;

public enum ABBizCode implements BizCode {
	AB0001("AB0001","指定的用户已存在该登录账号"),
	AB0002("AB0002","该登录账号已被其他用户使用"),
	
	;
	
	private final String code;
	private final String info;
	private ABBizCode(String code,String info) {
		this.code = code;
		this.info = info;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getInfo() {
		return this.info;
	}
	
	@Override
	public String toString() {
		return this.code + " : " + this.info;
	}
}
