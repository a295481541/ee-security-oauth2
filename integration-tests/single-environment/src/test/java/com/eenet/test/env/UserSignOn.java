package com.eenet.test.env;

import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserSignOnBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.common.OPOwner;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class UserSignOn {
	private RSAEncrypt encrypt = (RSAEncrypt) SpringEnvironment.getInstance().getContext().getBean("RSAEncrypt");
	private AppAuthenRequest currentApp = (AppAuthenRequest) SpringEnvironment.getInstance().getContext().getBean("currentApp");

	public void adminUserSignOn(String loginAccount, String password) throws Exception {
		AdminUserSignOnBizService adminUserSignOnBizService = (AdminUserSignOnBizService) SpringEnvironment
				.getInstance().getContext().getBean("AdminUserSignOnBizImpl");
		OPOwner.reset();
		CallerIdentityInfo.reset();
		
		if (EEBeanUtils.isNULL(loginAccount) || EEBeanUtils.isNULL(password)) {
			loginAccount = "superman";
			password = "sEPp$341";
		}

		String cipherPassword = RSAUtil.encrypt(encrypt, password+"##"+System.currentTimeMillis());
		SignOnGrant grant = adminUserSignOnBizService.getSignOnGrant(currentApp.getAppId(), currentApp.getRedirectURI(), loginAccount, cipherPassword);
		if (!grant.isSuccessful())
			throw new Exception(grant.getStrMessage());
		
		String cipherAppSK = RSAUtil.encrypt(encrypt, currentApp.getAppSecretKey()+"##"+System.currentTimeMillis());
		AccessToken accessToken = adminUserSignOnBizService.getAccessToken(currentApp.getAppId(), cipherAppSK, grant.getGrantCode());
		if (!accessToken.isSuccessful())
			throw new Exception(accessToken.getStrMessage());
		
		OPOwner.setCurrentUser(accessToken.getUserInfo().getAtid());
		OPOwner.setUsertype("adminUser");
		CallerIdentityInfo.setAccesstoken(accessToken.getAccessToken());
	}

	public void endUserSignOn() throws Exception {
		EndUserSignOnBizService endUserSignOnBizService = (EndUserSignOnBizService) SpringEnvironment.getInstance()
				.getContext().getBean("EndUserSignOnBizImpl");
//		SignOnGrant grant = endUserSignOnBizService.getSignOnGrant(appId, redirectURI, loginAccount, password);
	}

	public UserSignOn() {
		// TODO Auto-generated constructor stub
	}

}
