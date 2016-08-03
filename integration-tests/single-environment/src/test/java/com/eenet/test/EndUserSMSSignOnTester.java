package com.eenet.test;

import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserSMSSignOnBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class EndUserSMSSignOnTester extends SpringEnvironment {
	private final EndUserSMSSignOnBizService signOnService = (EndUserSMSSignOnBizService)super.getContext().getBean("EndUserSMSSignOnBizImpl");
	private final RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
	private final String appId = "9CFF0CA0D43D4B2DAC1EFC6A86FCB191";
	private final String redirectURI = "http://hz.saas.workeredu.com";
	private final String appSecretKey = "pASS41#";
	private final long mobile = 13071220990l;
	private final String smsCode = "525929";//<----要按收到的
	
	@Test
	public void sendSMSCode4Login() {
		SimpleResponse sendRS = signOnService.sendSMSCode4Login(appId, mobile);
		if (!sendRS.isSuccessful())
			System.out.println(sendRS.getStrMessage());
		else
			System.out.println("发送成功");
	}
	
//	@Test
	public void getAccessToken() throws Exception{
		SimpleResponse validateSMS = signOnService.validateSMSCode4Login(appId, mobile, smsCode, false);
		if ( !validateSMS.isSuccessful() ) {
			System.out.println(validateSMS.getStrMessage());
			return;
		}
		
		/* app身份对象 */
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(RSAUtil.encryptWithTimeMillis(encrypt, appSecretKey));appRequest.setRedirectURI(redirectURI);
		
		/* 登录，获得令牌 */
		AccessToken accessToken = signOnService.getAccessToken(appRequest, mobile, smsCode);
		if ( !accessToken.isSuccessful() ) {
			System.out.println(accessToken.getStrMessage());
			return;
		}
		System.out.println("访问令牌："+accessToken.getAccessToken()+",刷新令牌："+accessToken.getRefreshToken());
		
	}
}
