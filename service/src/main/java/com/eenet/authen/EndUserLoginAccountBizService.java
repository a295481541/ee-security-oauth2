package com.eenet.authen;

import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;

/**
 * 最终用户登录账号服务
 * 2016年4月7日
 * @author Orion
 */
public interface EndUserLoginAccountBizService {
	/**
	 * 用户登录账号注册
	 * @param user
	 * @return
	 */
	public EENetEndUserLoginAccount registeEndUserLoginAccount(EENetEndUserLoginAccount user);
	/**
	 * 最终用户登录账号废弃
	 * @param code
	 * @return
	 * 2016年3月30日
	 * @author Orion
	 */
	public SimpleResponse removeEndUserLoginAccount(String... loginAccounts);
	/**
	 * 根据登录账号获得主账号
	 * @param loginAccount 登录账号
	 * @return 主账号
	 * 2016年4月7日
	 * @author Orion
	 */
	public StringResponse retrieveEndUserMainAccount(String loginAccount);
}
