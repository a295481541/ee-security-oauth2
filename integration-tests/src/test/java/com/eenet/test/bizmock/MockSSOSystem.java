package com.eenet.test.bizmock;

import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.SingleSignOnBizService;
import com.eenet.test.env.DubboAuthenConsumerENV;

/**
 * 模拟单点登录系统
 * 
 * 2016年5月18日
 * @author Orion
 */
public class MockSSOSystem extends DubboAuthenConsumerENV {
	
	public SignOnGrant getSignOnGrant(String appId, String redirectURI, String endUserLoginAccount,String endUserPassword){
		ServiceConsumer SSOSystem = (ServiceConsumer)super.getContext().getBean("ConsumerIdentity");
		SingleSignOnBizService singleSignOnService = (SingleSignOnBizService)super.getContext().getBean("SingleSignOnBizService");
		
		SignOnGrant grant = singleSignOnService.getSignOnGrant(SSOSystem, appId, redirectURI, endUserLoginAccount, endUserPassword);
		return grant;
	}
}
