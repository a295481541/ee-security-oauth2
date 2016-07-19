package com.eenet.security;

import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.EndUserInfo;

/**
 * 注册新用户服务
 * 2016年7月8日
 * @author Orion
 */
public interface RegistNewUserBizService {
	/**
	 * 注册最终用户（含账号和密码）并登陆，该方法对接入系统进行验证
	 * @param endUser 最终用户对象
	 * @param account 登陆账号对象，该对象的userInfo属性应置空
	 * @param credential 登录密码对象，该对象的userInfo属性应置空，登录密码属性必须是：带时间戳加密的形式
	 * @param appID 应用身份对象
	 * @return 访问令牌
	 * 2016年7月8日
	 * @author Orion
	 */
	public AccessToken registEndUserWithLogin(EndUserInfo endUser, EndUserLoginAccount account, EndUserCredential credential, AppAuthenRequest appID);
	
	/**
	 * 注册服务人员（含账号和密码）
	 * @param adminUser 服务人员对象
	 * @param account 登陆账号对象，该对象的userInfo属性应置空
	 * @param credential 登录密码对象，该对象的userInfo属性应置空，登录密码属性必须是：带时间戳加密的形式
	 * @return
	 * 2016年7月8日
	 * @author Orion
	 */
	public SimpleResponse registAdminUserWithoutLogin(AdminUserInfo adminUser, AdminUserLoginAccount account, AdminUserCredential credential);
}
