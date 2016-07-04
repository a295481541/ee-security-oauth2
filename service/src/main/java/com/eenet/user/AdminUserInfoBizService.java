package com.eenet.user;

import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.query.QueryCondition;

/**
 * 服务人员管理服务
 * @author Orion
 *
 */
public interface AdminUserInfoBizService {
	/**
	 * 保存服务人员信息
	 * @param m
	 * @return
	 */
	public AdminUserInfo save(AdminUserInfo m);
	
	/**
	 * 获得服务人员信息
	 * @param pk
	 * @return
	 */
	public AdminUserInfo get(String pk);
	
	/**
	 * 删除服务人员信息
	 * @param pk
	 * @return
	 */
	public SimpleResponse delete(String... pk);
	
	/**
	 * 查询服务人员信息
	 * @param condition
	 * @return
	 */
	
	public SimpleResultSet<AdminUserInfo> query (QueryCondition condition);
}
