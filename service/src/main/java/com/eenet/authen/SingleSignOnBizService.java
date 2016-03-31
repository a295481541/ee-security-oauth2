package com.eenet.authen;

/**
 * 单点登录服务 2016年3月29日
 * 
 * @author Orion
 */
public interface SingleSignOnBizService {
	
	/**
	 * 获得认证授权码
	 * @param SSOSystem 单点登录系统身份
	 * @param app 第三方应用
	 * @param userCredential 最终用户身份
	 * @return 登录授权信息
	 * 2016年3月30日
	 * @author Orion
	 */
	public SignOnGrant getSignOnGrant(ServiceConsumer SSOSystem, ThirdPartySSOAPP app, EENetEndUserCredential userCredential);
	
}
