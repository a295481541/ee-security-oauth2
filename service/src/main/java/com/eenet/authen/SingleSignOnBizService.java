package com.eenet.authen;

/**
 * 单点登录服务 2016年3月29日
 * 
 * @author Orion
 */
public interface SingleSignOnBizService {

	public String webAPPGetGrantCode(ServiceConsumer SSOSystem, String thirdPartSystemCode, String redirectURIPrefix,
			EENetEndUserCredential userCredential);

	public void nativeAPPGetGrantCode(ServiceConsumer SSOSystem,String thirdPartSystemCode);
}
