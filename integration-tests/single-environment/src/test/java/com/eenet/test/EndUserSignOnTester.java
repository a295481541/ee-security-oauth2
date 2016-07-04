package com.eenet.test;

import java.util.Random;

import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserCredentialBizService;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessAppType;
import com.eenet.authen.LoginAccountType;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.user.EndUserInfo;
import com.eenet.user.EndUserInfoBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class EndUserSignOnTester extends SpringEnvironment {
	private BusinessAppBizService appService = (BusinessAppBizService)super.getContext().getBean("BusinessAppBizImpl");
	private EndUserInfoBizService userService = (EndUserInfoBizService)super.getContext().getBean("EndUserInfoBizImpl");
	private EndUserLoginAccountBizService accountService = (EndUserLoginAccountBizService)super.getContext().getBean("EndUserLoginAccountBizImpl");
	private EndUserCredentialBizService credentialService = (EndUserCredentialBizService)super.getContext().getBean("EndUserCredentialBizImpl");
	private EndUserSignOnBizService signService = (EndUserSignOnBizService)super.getContext().getBean("EndUserSignOnBizImpl");
	private IdentityAuthenticationBizService identityService = (IdentityAuthenticationBizService)super.getContext().getBean("IdentityAuthenticationBizImpl");
	private RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
	
	private BusinessApp app;
	private EndUserInfo user;
	private EndUserLoginAccount account;
	private String userPassword = "myPassword";
	private String appPassword = "999Aa$";
	
	/**
	 * 对只设置了私有账号并且是MD5加密的密码进行认证
	 * 2016年6月16日
	 * @author Orion
	 */
	@Test
	public void loginByAccountPasswordAndMD5() throws Exception {
		System.out.println("==========================="+this.getClass().getName()+".loginByAccountPasswordAndMD5()===========================");
		String appId = "432B31FB2F7C4BB19ED06374FB0C1850";
		String appDomain = "http://www.zhigongjiaoyu.com";
		String loginAccount = "gjm2015";
		String password = "gjm2015Password";
		/* 获得登录授权码 */
		SignOnGrant getSignOnGrant = 
				signService.getSignOnGrant(appId, appDomain, loginAccount, RSAUtil.encryptWithTimeMillis(encrypt, password));
		if (!getSignOnGrant.isSuccessful()){
			System.out.println("getSignOnGrant : \n"+getSignOnGrant.getStrMessage());
			return;
		}
		System.out.println("getSignOnGrant: "+getSignOnGrant.getGrantCode());
	}
	
//	@Test
	public void normalFlow() throws Exception {
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
			//其他用例已测
			
			/* 最终用户令牌认证 */
			UserAccessTokenAuthenRequest tokenAuthenRequest = new UserAccessTokenAuthenRequest();
			tokenAuthenRequest.setAppId(app.getAppId());
			tokenAuthenRequest.setAppSecretKey(RSAUtil.encryptWithTimeMillis(encrypt, appPassword));
			tokenAuthenRequest.setUserId(getAccessToken.getUserInfo().getAtid());
			tokenAuthenRequest.setUserAccessToken(refreshAccessToken.getAccessToken());
			UserAccessTokenAuthenResponse endUserAuthenResult = 
					identityService.endUserAuthen(tokenAuthenRequest);
			System.out.println("endUserAuthenResult successful: " + endUserAuthenResult.isSuccessful());
			System.out.println("endUserAuthenResult isAppIdentityConfirm: " + endUserAuthenResult.isAppIdentityConfirm());
			System.out.println("endUserAuthenResult isUserIdentityConfirm: " + endUserAuthenResult.isUserIdentityConfirm());
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
		EndUserInfo user = new EndUserInfo();
		user.setName("Orion");
		user = userService.save(user);
		if (!user.isSuccessful()) {
			System.out.println(user.getStrMessage());
			return false;
		}
		System.out.println("user atid: "+user.getAtid());
		this.user = user;
		
		/* 注册服务人员登录账号 */
		EndUserLoginAccount account = new EndUserLoginAccount();
		account.setUserInfo(user);
		account.setAccountType(LoginAccountType.MOBILE);
		account.setLoginAccount(String.valueOf(13900000000l + (new Random().nextInt(100))));
		account = accountService.registeEndUserLoginAccount(account);
		if (!account.isSuccessful()) {
			System.out.println(account.getStrMessage());
			return false;
		}
		System.out.println("account atid: "+account.getAtid());
		this.account = account;
		
		/* 初始化服务人员登录密码 */
		EndUserCredential credential = new EndUserCredential();
		credential.setEndUser(user);
		credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, this.userPassword));
		SimpleResponse initResult = credentialService.initEndUserLoginPassword(credential);
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
			accountService.removeEndUserLoginAccount(account.getLoginAccount());
		/* 注销业务系统 */
		if (app!=null)
			appService.removeApp(app.getAppId());
	}
}
