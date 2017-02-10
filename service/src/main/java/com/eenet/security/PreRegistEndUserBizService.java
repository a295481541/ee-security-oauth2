package com.eenet.security;

import com.eenet.base.BooleanResponse;
import com.eenet.baseinfo.user.EndUserInfo;

/**
 * 注册新用户服务预先检查服务
 * 2016年7月28日
 * @author Orion
 */
public interface PreRegistEndUserBizService {
	
	/**
	 * 账号是否可注册（appid 优先）
	 * @param appId     业务系统标识
	 * @param seriesId  业务体系标识 
	 * @param account 待注册账号
	 * @return result属性为true表示可注册
	 * 2017年1月10日
	 * @author Orion
	 */
	public BooleanResponse existAccount(String appId,String seriesId, String... accounts);
	
	/**
	 * 根据账户获取用户
	 * @param appid     业务系统标识
	 * @param seriesId  业务体系标识 
	 * @param account 待注册账号
	 * @return result属性为true表示可注册
	 * 2017年1月10日
	 * @author Orion
	 */
	public EndUserInfo retrieveEndUserInfo(String appId, String seriesId, String account);
}
