package com.eenet.authen.util;

import com.eenet.common.code.BizCode;

public class ABBizCode {
	/** 指定的用户已存在该登录账号 */
	public static final BizCode AB0001 = new BizCode("AB0001","指定的用户已存在该登录账号");
	/** 该登录账号已被其他用户使用 */
	public static final BizCode AB0002 = new BizCode("AB0002","该登录账号已被其他用户使用");
	/** 短信发送失败-调用短信平台时出错 */
	public static final BizCode AB0003 = new BizCode("AB0003","短信发送失败-调用短信平台时出错");
	/** 短信发送失败-短信平台反馈发送有误 */
	public static final BizCode AB0004 = new BizCode("AB0004","短信发送失败-短信平台反馈发送有误");
	/** 短信发送失败-运营商反馈发送有误 */
	public static final BizCode AB0005 = new BizCode("AB0005","短信发送失败-运营商反馈发送有误");
	
}
