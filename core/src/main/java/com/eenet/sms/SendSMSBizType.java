package com.eenet.sms;

/**
 * 发送短信的业务标识
 * 2016年7月14日
 * @author Orion
 */
public enum SendSMSBizType {
	GetCodeForResetPassword,//通过手机验证码重置密码
	LoginUsingCode;//手机验证码快速登录
}
