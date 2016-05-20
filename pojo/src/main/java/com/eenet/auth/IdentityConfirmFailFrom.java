package com.eenet.auth;

/**
 * 身份认证错误来源
 * 2016年5月20日
 * @author Orion
 */
public enum IdentityConfirmFailFrom {
	syserr,//系统错误
	consumer,//服务消费者身份校验错误
	thirdPartySys,//第三方业务系统（单点登录集成）身份校验错误
	endUser;//用户令牌校验错误
	
	/**
	 * 将字符转换为枚举对象
	 * @param value 可以接受的字符：
	 * -----------------------------------------
	 * | syserr,consumer,thirdPartySys,endUser |
	 * -----------------------------------------
	 * @return 返回相应的枚举对象，当输入参数不在上表范围时返回null
	 * 2016年3月30日
	 * @author Orion
	 */
	public static IdentityConfirmFailFrom obtainEnum(String value) {
		IdentityConfirmFailFrom[] allType = IdentityConfirmFailFrom.values();
		for (IdentityConfirmFailFrom type : allType) {
			if (type.name().equals(value))
				return type;
		}
		return null;
	}
}
