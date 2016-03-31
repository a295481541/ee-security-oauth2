package com.eenet.authen.bizimpl;

import com.eenet.authen.EENetEndUserCredential;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.SingleSignOnBizService;
import com.eenet.authen.ThirdPartySSOAPP;

public class SingleSignOnBizImpl implements SingleSignOnBizService {
	private IdentityAuthenticationBizService authenService;

	@Override
	public SignOnGrant getSignOnGrant(ServiceConsumer SSOSystem, ThirdPartySSOAPP app,
			EENetEndUserCredential userCredential) {
		SignOnGrant grant = new SignOnGrant();
		/* 参数检查 */
		
		/* 单点登录系统身份认证 */
		
		/* 单点登录系统权限校验 */
		
		/* 第三方系统标识(appId)认证 */
		
		/* 第三方系统跳转地址校验(仅web应用) */
		
		/* 最终用户身份认证 */
		
		/* 生成code，放入缓存 */
		
		return grant;
	}

	public IdentityAuthenticationBizService getAuthenService() {
		return authenService;
	}

	public void setAuthenService(IdentityAuthenticationBizService authenService) {
		this.authenService = authenService;
	}
}
