package com.eenet.user.bizimpl;

import com.eenet.user.dao.AdminUserInfoDAOService;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.QueryCondition;
import com.eenet.common.exception.DBOPException;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.AdminUserInfoBizService;
import com.eenet.util.EEBeanUtils;

/**
 * 服务人员管理业务逻辑
 * @author Orion
 *
 */
public class AdminUserInfoBizImpl extends SimpleBizImpl implements AdminUserInfoBizService {
	private AdminUserInfoDAOService adminDAOService;
	@Override
	public AdminUserInfo save(AdminUserInfo m) {
		if (m == null) {
			m = new AdminUserInfo();
			m.setSuccessful(false);
			m.addMessage("无法处理空对象");
			return m;
		}
		try {
			// atid为空（即新增用户），同时手机、邮箱、身份证至少一个不为空，判断是否存在重复的手机、邮箱、身份证
			if (EEBeanUtils.isNULL(m.getAtid()) && (m.getMobile() != null || !EEBeanUtils.isNULL(m.getEmail())
					|| !EEBeanUtils.isNULL(m.getIdCard())) ) {
				if (this.getAdminDAOService().existMobileEmailId(m)) {
					m.setSuccessful(false);
					m.addMessage("存在重复的手机、邮箱或身份证");
					return m;
				}
			}
		} catch (DBOPException e) {
			m.setSuccessful(false);
			m.addMessage("系统错误：无法判断是否存在重复的手机、邮箱或身份证");
			m.addMessage(e.getMessage());
			return m;
		}
		return super.save(m);
	}

	@Override
	public AdminUserInfo get(String pk) {
		return super.get(pk);
	}

	@Override
	public SimpleResultSet<AdminUserInfo> query(QueryCondition condition) {
		return super.query(condition);
	}

	@Override
	public Class<?> getPojoCLS() {
		// TODO Auto-generated method stub
		return AdminUserInfo.class;
	}

	/**
	 * @return the adminDAOService
	 */
	public AdminUserInfoDAOService getAdminDAOService() {
		return adminDAOService;
	}
	/**
	 * @param adminDAOService the adminDAOService to set
	 */
	public void setAdminDAOService(AdminUserInfoDAOService adminDAOService) {
		this.adminDAOService = adminDAOService;
	}
}
