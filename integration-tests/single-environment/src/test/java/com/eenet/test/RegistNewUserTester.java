package com.eenet.test;

import java.util.Random;

import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.LoginAccountType;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.security.RegistNewUserBizService;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.EndUserInfo;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class RegistNewUserTester extends SpringEnvironment {
	private final RegistNewUserBizService regService = (RegistNewUserBizService)super.getContext().getBean("RegistNewUserBizImpl");
	private final RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
	
	@Test
	public void registEndUserWithLogin() throws Exception {
		EndUserInfo endUser = new EndUserInfo();
		endUser.setName("Orion");
		EndUserLoginAccount account = new EndUserLoginAccount();
		long mobile = new Random().nextInt(1000);
		account.setLoginAccount("1300000"+String.valueOf(mobile));
		account.setAccountType(LoginAccountType.MOBILE);
		EndUserCredential credential = new EndUserCredential();
		String password = "myPassword";
		credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, password));
		AppAuthenRequest appAtt = new AppAuthenRequest();
		appAtt.setAppId("432B31FB2F7C4BB19ED06374FB0C1850");
		appAtt.setAppSecretKey(RSAUtil.encryptWithTimeMillis(encrypt, "pASS12#"));
		appAtt.setRedirectURI("http://www.zhigongjiaoyu.com");
		
		AccessToken accessToken = regService.registEndUserWithLogin(endUser, account, credential, appAtt);
		System.out.println(EEBeanUtils.object2Json(accessToken));
	}
	
//	@Test
	public void registAdminUserWithoutLogin() throws Exception {
		AdminUserInfo adminUser = new AdminUserInfo();
		adminUser.setName("Orion");
		AdminUserLoginAccount account = new AdminUserLoginAccount();
		long mobile = new Random().nextInt(1000);
		account.setLoginAccount("1300000"+String.valueOf(mobile));
		account.setAccountType(LoginAccountType.MOBILE);
		AdminUserCredential credential = new AdminUserCredential();
		String password = "myPassword";
		credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, password));
		AppAuthenRequest appAtt = new AppAuthenRequest();
		appAtt.setAppId("432B31FB2F7C4BB19ED06374FB0C1850");
		appAtt.setAppSecretKey(RSAUtil.encryptWithTimeMillis(encrypt, "pASS12#"));
		appAtt.setRedirectURI("http://www.zhigongjiaoyu.com");
		
		SimpleResponse response = regService.registAdminUserWithoutLogin(adminUser, account, credential);
		System.out.println(EEBeanUtils.object2Json(response));
	}
}
