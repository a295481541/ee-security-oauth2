package com.eenet.authen;

import com.eenet.base.SimpleResponse;

/**
 * 账号注册服务
 * @author Orion
 *
 */
public interface RegistrationBizService {
	/**
	 * 服务消费者注册，登录编码不可设置（由系统产生）
	 * @param consumer 设置消费者中文名和秘钥
	 * @return 附带登录编码，不带身份认证秘钥
	 */
	public ServiceConsumer serviceConsumerRegiste(ServiceConsumer consumer);
	/**
	 * 废弃服务消费者
	 * @param code 登录编码
	 * @return
	 */
	public SimpleResponse serviceConsumerDrop(String... codes);
	
	
	/**
	 * 单点登录系统注册
	 * @param app 设置除appId以外的其他必要信息
	 * @return 附带appId，不带身份认证秘钥
	 * 2016年3月30日
	 * @author Orion
	 */
	public ThirdPartySSOAPP thirdPartySSOAppRegiste(ThirdPartySSOAPP app);
	/**
	 * 废弃单点登录系统
	 * @param code
	 * @return
	 * 2016年3月30日
	 * @author Orion
	 */
	public SimpleResponse thirdPartySSOAppPDrop(String... apIds);
	
	
	/**
	 * 用户登录账号注册
	 * @param user
	 * @return
	 */
	public EENetEndUserLoginAccount endUserLoginAccountRegiste(EENetEndUserLoginAccount user);
	/**
	 * 最终用户登录账号废弃
	 * @param code
	 * @return
	 * 2016年3月30日
	 * @author Orion
	 */
	public SimpleResponse endUserLoginAccountDrop(String... loginAccounts);
	
	/**
	 * 初始化用户登录密码
	 * @param mainAccount
	 * @param credential 
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse initUserLoginPassword(EENetEndUserCredential credential);
	
	/**
	 * 修改用户登录密码
	 * @param mainAccount
	 * @param curCredential
	 * @param newSecretKey
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse changeUserLoginPassword(EENetEndUserCredential curCredential, String newSecretKey);
	
	/**
	 * 重置用户登录密码（适合忘记密码）
	 * @param mainAccount
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse resetUserLoginPassword(EENetEndUserMainAccount mainAccount);
}
