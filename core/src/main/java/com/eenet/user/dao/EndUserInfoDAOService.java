package com.eenet.user.dao;

import java.util.List;

import com.eenet.base.dao.BaseDAOService;
import com.eenet.common.exception.DBOPException;
import com.eenet.user.EndUserInfo;

/**
 * 最终用户信息数据库查询服务
 * @author Orion
 *
 */
public interface EndUserInfoDAOService extends BaseDAOService {
	
	/**
	 * 判断手机、邮箱和身份证号是否已存在
	 * @param m 最终用户信息对象
	 * @return 存在-true,不存在-false
	 * @throws DBOPException
	 */
	public boolean existMobileEmailId(EndUserInfo m) throws DBOPException;
	
	/**
	 * 根据手机、电子邮箱或身份证号获得最终用户个人信息
	 * 三者任意匹配一项即可（OR查询）
	 * @param m
	 * @return
	 * @throws DBOPException
	 * 2016年6月22日
	 * @author Orion
	 */
	public List<EndUserInfo> getByMobileEmailId(EndUserInfo m) throws DBOPException;
}
