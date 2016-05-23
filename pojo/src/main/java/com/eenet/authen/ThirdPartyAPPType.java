package com.eenet.authen;

/**
 * 第三方应用类型
 * 2016年3月30日
 * @author Orion
 */
public enum ThirdPartyAPPType {
	WEBAPP,
	NATIVEAPP;
	
	/**
	 * 将字符转换为第三方应用类型枚举对象
	 * @param value 可以接受的字符：
	 * --------------------
	 * | WEBAPP,NATIVEAPP |
	 * --------------------
	 * @return 返回相应的枚举对象，当输入参数不在上表范围时返回null
	 * 2016年3月30日
	 * @author Orion
	 */
	public static ThirdPartyAPPType obtainEnum(String value) {
		ThirdPartyAPPType[] allType = ThirdPartyAPPType.values();
		for (ThirdPartyAPPType type : allType) {
			if (type.name().equals(value))
				return type;
		}
		return null;
	}
}
