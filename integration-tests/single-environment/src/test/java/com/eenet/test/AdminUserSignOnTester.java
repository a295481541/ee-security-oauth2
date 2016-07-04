package com.eenet.test;

import java.util.Random;

import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserCredentialBizService;
import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.AdminUserLoginAccountBizService;
import com.eenet.authen.AdminUserSignOnBizService;
import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessAppType;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.LoginAccountType;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.AdminUserInfoBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class AdminUserSignOnTester extends SpringEnvironment {
	private BusinessAppBizService appService = (BusinessAppBizService)super.getContext().getBean("BusinessAppBizImpl");
	private AdminUserInfoBizService userService = (AdminUserInfoBizService)super.getContext().getBean("AdminUserInfoBizImpl");
	private AdminUserLoginAccountBizService accountService = (AdminUserLoginAccountBizService)super.getContext().getBean("AdminUserLoginAccountBizImpl");
	private AdminUserCredentialBizService credentialService = (AdminUserCredentialBizService)super.getContext().getBean("AdminUserCredentialBizImpl");
	private AdminUserSignOnBizService signService = (AdminUserSignOnBizService)super.getContext().getBean("AdminUserSignOnBizImpl");
	private IdentityAuthenticationBizService identityService = (IdentityAuthenticationBizService)super.getContext().getBean("IdentityAuthenticationBizImpl");
	private RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
	
	private BusinessApp app;
	private AdminUserInfo user;
	private AdminUserLoginAccount account;
	private String userPassword = "myPassword";
	private String appPassword = "999Aa$";
	
	@Test
	public void justLogin() throws Exception {
		System.out.println("==========================="+this.getClass().getName()+".justLogin()===========================");
		/* 获得登录授权码 */
		SignOnGrant getSignOnGrant = 
				signService.getSignOnGrant("432B31FB2F7C4BB19ED06374FB0C1850", "http://www.zhigongjiaoyu.com", "md5Account", RSAUtil.encryptWithTimeMillis(encrypt, "md5Password"));
		if (!getSignOnGrant.isSuccessful()){
			System.out.println("getSignOnGrant : \n"+getSignOnGrant.getStrMessage());
			return;
		}
		System.out.println("getSignOnGrant: "+getSignOnGrant.getGrantCode());
	}
	
//	@Test
	public void normalFlow() throws Exception{
		System.out.println("==========================="+this.getClass().getName()+".normalFlow()===========================");
		try {
			if (!this.preNormalFlow())
				return;
			
			/* ●●●●●●●●●●●●●●●●●●●●●●●●●●登录●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●● */
			
			/* 获得登录授权码 */
			SignOnGrant getSignOnGrant = 
					signService.getSignOnGrant(app.getAppId(), app.getRedirectURIPrefix(), account.getLoginAccount(), RSAUtil.encryptWithTimeMillis(encrypt, userPassword));
			if (!getSignOnGrant.isSuccessful()){
				System.out.println("getSignOnGrant : \n"+getSignOnGrant.getStrMessage());
				return;
			}
			System.out.println("getSignOnGrant: "+getSignOnGrant.getGrantCode());
			
			/* 获得访问令牌 */
			AccessToken getAccessToken = 
					signService.getAccessToken(app.getAppId(), RSAUtil.encryptWithTimeMillis(encrypt, appPassword), getSignOnGrant.getGrantCode());
			if (!getAccessToken.isSuccessful()){
				System.out.println("getAccessToken : \n"+getAccessToken.getStrMessage());
				return;
			}
			System.out.println("getAccessToken AccessToken: "+getAccessToken.getAccessToken());
			System.out.println("getAccessToken RefreshToken: "+getAccessToken.getRefreshToken());
			System.out.println("getAccessToken UserInfo.Name: "+getAccessToken.getUserInfo().getName());
			
			/* 刷新访问令牌 */
			AccessToken refreshAccessToken = 
					signService.refreshAccessToken(app.getAppId(), RSAUtil.encryptWithTimeMillis(encrypt, appPassword), getAccessToken.getRefreshToken(), getAccessToken.getUserInfo().getAtid());
			if (!refreshAccessToken.isSuccessful()){
				System.out.println("refreshAccessToken : \n"+refreshAccessToken.getStrMessage());
				return;
			}
			System.out.println("refreshAccessToken AccessToken: "+refreshAccessToken.getAccessToken());
			System.out.println("refreshAccessToken RefreshToken: "+refreshAccessToken.getRefreshToken());
			
			/* ●●●●●●●●●●●●●●●●●●●●●●●●●●认证●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●● */
			
			/* 应用系统认证 */
			AppAuthenRequest appAuthenRequest = new AppAuthenRequest();
			appAuthenRequest.setAppId(app.getAppId());
			appAuthenRequest.setAppSecretKey(RSAUtil.encryptWithTimeMillis(encrypt, appPassword));
			identityService.appAuthen(appAuthenRequest);
			
			/* 服务人员令牌认证 */
			UserAccessTokenAuthenRequest tokenAuthenRequest = new UserAccessTokenAuthenRequest();
			tokenAuthenRequest.setAppId(app.getAppId());
			tokenAuthenRequest.setAppSecretKey(RSAUtil.encryptWithTimeMillis(encrypt, appPassword));
			tokenAuthenRequest.setUserId(getAccessToken.getUserInfo().getAtid());
			tokenAuthenRequest.setUserAccessToken(refreshAccessToken.getAccessToken());
			UserAccessTokenAuthenResponse adminUserAuthenResult = 
					identityService.adminUserAuthen(tokenAuthenRequest);
			System.out.println("adminUserAuthenResult successful: " + adminUserAuthenResult.isSuccessful());
			System.out.println("adminUserAuthenResult isAppIdentityConfirm: " + adminUserAuthenResult.isAppIdentityConfirm());
			System.out.println("adminUserAuthenResult isUserIdentityConfirm: " + adminUserAuthenResult.isUserIdentityConfirm());
		} finally {
			this.afterNormalFlow();
		}
	}
	
	public boolean preNormalFlow() throws Exception{
		/* 注册业务系统 */
		BusinessApp app = new BusinessApp();
		app.setAppName("临时系统"+EEBeanUtils.getUUID());
		app.setAppType(BusinessAppType.WEBAPP);
		app.setRedirectURIPrefix("http://www.eenet.com");
		app.setSecretKey(this.appPassword);
		app = appService.registeApp(app);
		if (!app.isSuccessful()) {
			System.out.println(app.getStrMessage());
			return false;
		}
		System.out.println("appid: "+app.getAppId());
		this.app = app;
		
		/* 注册服务人员个人信息 */
		AdminUserInfo user = new AdminUserInfo();
		user.setName("Orion");
		user = userService.save(user);
		if (!user.isSuccessful()) {
			System.out.println(user.getStrMessage());
			return false;
		}
		System.out.println("user atid: "+user.getAtid());
		this.user = user;
		
		/* 注册服务人员登录账号 */
		AdminUserLoginAccount account = new AdminUserLoginAccount();
		account.setUserInfo(user);
		account.setAccountType(LoginAccountType.MOBILE);
		account.setLoginAccount(String.valueOf(13900000000l + (new Random().nextInt(100))));
		account = accountService.registeAdminUserLoginAccount(account);
		if (!account.isSuccessful()) {
			System.out.println(account.getStrMessage());
			return false;
		}
		System.out.println("account atid: "+account.getAtid());
		this.account = account;
		
		/* 初始化服务人员登录密码 */
		AdminUserCredential credential = new AdminUserCredential();
		credential.setAdminUser(user);
		credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, this.userPassword));
		SimpleResponse initResult = credentialService.initAdminUserLoginPassword(credential);
		if (!initResult.isSuccessful()) {
			System.out.println(initResult.getStrMessage());
			return false;
		}
		
		return true;
	}
	
	public void afterNormalFlow() {
		/* 注销服务人员个人信息 */
		if (user!=null)
			userService.delete(user.getAtid());
		/* 注销服务人员登录账号 */
		if (account!=null)
			accountService.removeAdminUserLoginAccount(account.getLoginAccount());
		/* 注销业务系统 */
		if (app!=null)
			appService.removeApp(app.getAppId());
	}
}
