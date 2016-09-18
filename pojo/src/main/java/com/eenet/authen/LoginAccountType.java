package com.eenet.authen;

/**
 * 最终用户及服务人员登录账号类型
 * 2016年4月14日
 * @author Orion
 */
public enum LoginAccountType {
	ID,//身份证
	MOBILE,//手机
	EMAIL,//邮箱
	USERNAME;//用户名
	
	/**
	 * 将字符转换为登录账号类型枚举对象
	 * @param value 可以接受的字符：
	 * ----------------------------
	 * | ID,MOBILE,EMAIL,USERNAME |
	 * ----------------------------
	 * @return 返回相应的枚举对象，当输入参数不在上表范围时返回null
	 * 2016年3月30日
	 * @author Orion
	 */
	public static LoginAccountType obtainEnum(String value) {
		LoginAccountType[] allType = LoginAccountType.values();
		for (LoginAccountType type : allType) {
			if (type.name().equals(value))
				return type;
		}
		return null;
	}
}
