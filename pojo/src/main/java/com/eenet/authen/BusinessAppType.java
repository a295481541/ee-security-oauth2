package com.eenet.authen;

/**
 * 业务系统类型
 * 2016年3月30日
 * @author Orion
 */
public enum BusinessAppType {
	WEBAPP,//web应用
	IOSAPP,//ios应用
	ANDAPP,//安卓应用
	NATIVEAPP;//桌面应用
	
	/**
	 * 将字符转换为业务系统类型枚举对象
	 * @param value 可以接受的字符：
	 * ----------------------------------
	 * | WEBAPP,IOSAPP,ANDAPP,NATIVEAPP |
	 * ----------------------------------
	 * @return 返回相应的枚举对象，当输入参数不在上表范围时返回null
	 * 2016年3月30日
	 * @author Orion
	 */
	public static BusinessAppType obtainEnum(String value) {
		BusinessAppType[] allType = BusinessAppType.values();
		for (BusinessAppType type : allType) {
			if (type.name().equals(value))
				return type;
		}
		return null;
	}
}
