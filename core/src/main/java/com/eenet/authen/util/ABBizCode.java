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
	/** 登录失败：系统错误 */
	public static final BizCode AB0006 = new BizCode("AB0006","登录失败-系统错误");
	/** 登录失败：登录账号或密码错误 */
	public static final BizCode AB0007 = new BizCode("AB0007","登录失败-登录账号或密码错误");
	/** 注册未全部成功-部分登录账号未能完成绑定但有部分已绑定 */
	public static final BizCode AB0008 = new BizCode("AB0008","注册未全部成功-部分登录账号未能完成绑定但有部分已绑定");
}
