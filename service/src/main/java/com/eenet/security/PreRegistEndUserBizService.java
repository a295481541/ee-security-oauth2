package com.eenet.security;

import com.eenet.base.BooleanResponse;

/**
 * 注册新用户服务预先检查服务
 * 2016年7月28日
 * @author Orion
 */
public interface PreRegistEndUserBizService {
	
	/**
	 * 账号是否可注册（appid 优先）
	 * @param appid     业务系统标识
	 * @param seriesId  业务体系标识 
	 * @param account 待注册账号
	 * @return result属性为true表示可注册
	 * 2017年1月10日
	 * @author Orion
	 */
	public BooleanResponse existAccount(String appId, String seriesId, String[] accounts);
}
