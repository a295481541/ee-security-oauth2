package com.eenet.user.dao;

import com.eenet.base.dao.BaseDAOService;
import com.eenet.common.exception.DBOPException;
import com.eenet.user.AdminUserInfo;

/**
 * 服务人员信息数据库查询服务
 * @author Orion
 *
 */
public interface AdminUserInfoDAOService extends BaseDAOService {
	
	/**
	 * 判断手机、邮箱和身份证号是否已存在
	 * @param m 服务人员信息对象
	 * @return true/false
	 * @throws DBOPException
	 */
	public boolean existMobileEmailId(AdminUserInfo m) throws DBOPException;
}
