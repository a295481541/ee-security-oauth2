package com.eenet.authen.util;

import com.eenet.common.code.BizCode;

public enum ABBizCode implements BizCode {
	/** 指定的用户已存在该登录账号 */
	AB0001("AB0001","指定的用户已存在该登录账号"),
	/** 该登录账号已被其他用户使用 */
	AB0002("AB0002","该登录账号已被其他用户使用"),
	/** 短信发送失败-调用短信平台时出错 */
	AB0003("AB0003","短信发送失败-调用短信平台时出错"),
	/** 短信发送失败-短信平台反馈发送有误 */
	AB0004("AB0004","短信发送失败-短信平台反馈发送有误"),
	/** 短信发送失败-运营商反馈发送有误 */
	AB0005("AB0005","短信发送失败-运营商反馈发送有误"),
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
