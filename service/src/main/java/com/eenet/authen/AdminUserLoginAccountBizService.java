package com.eenet.authen;

import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.AdminUserInfo;

/**
 * 服务人员登录账号服务
 * 2016年4月7日
 * @author Orion
 */
public interface AdminUserLoginAccountBizService {
	/**
	 * 服务人员登录账号注册
	 * @param user
	 * @return
	 */
	public AdminUserLoginAccount registeAdminUserLoginAccount(AdminUserLoginAccount account);
	/**
	 * 服务人员登录账号废弃
	 * @param code
	 * @return
	 * 2016年3月30日
	 * @author Orion
	 */
	public SimpleResponse removeAdminUserLoginAccount(String... loginAccounts);
	/**
	 * 根据登录账号获得服务人员基本信息
	 * @param loginAccount 登录账号
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	public AdminUserInfo retrieveAdminUserInfo(String loginAccount);
	/**
	 * 根据登录账号获得服务人员登陆账号其他信息
	 * @param loginAccount 登录账号
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	public AdminUserLoginAccount retrieveAdminUserLoginAccountInfo(String loginAccount);
}
