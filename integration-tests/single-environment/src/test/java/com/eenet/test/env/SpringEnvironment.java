package com.eenet.test.env;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserSignOnBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.common.OPOwner;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class SpringEnvironment {
	private static SpringEnvironment INSTANCE;
	private static ClassPathXmlApplicationContext context;
	
	public static SpringEnvironment getInstance() {
		if (INSTANCE == null)
			INSTANCE = new SpringEnvironment();
		return INSTANCE;
	}
	
	public ApplicationContext getContext() {
		if (SpringEnvironment.context == null)
			initEnvironment();
		return context;
	}
	
	public void adminLogin() throws Exception {
		if ("adminUser".equals(OPOwner.getUsertype())) 
			return;
		
		OPOwner.reset();
		CallerIdentityInfo.reset();
		
		AdminUserSignOnBizService signService = (AdminUserSignOnBizService) getContext().getBean("AdminUserSignOnBizService");
		AppAuthenRequest appAuthenRequest = (AppAuthenRequest) getContext().getBean("AppIdentity");
		RSAEncrypt encrypt = (RSAEncrypt) getContext().getBean("transferRSAEncrypt");
		SignOnGrant grant = signService.getSignOnGrant(appAuthenRequest.getAppId(), appAuthenRequest.getRedirectURI(), "superman", RSAUtil.encrypt(encrypt, "sEPp$341##"+System.currentTimeMillis()));
		if ( !grant.isSuccessful() )
			System.out.println(grant.getStrMessage());
		AccessToken accessToken = signService.getAccessToken(appAuthenRequest.getAppId(), RSAUtil.encrypt(encrypt, appAuthenRequest.getAppSecretKey()+"##"+System.currentTimeMillis()), grant.getGrantCode());
		if ( !accessToken.isSuccessful() )
			System.out.println(accessToken.getStrMessage());
		
		OPOwner.setCurrentSys(appAuthenRequest.getAppId());
		CallerIdentityInfo.setAppsecretkey( RSAUtil.encrypt(encrypt, appAuthenRequest.getAppSecretKey()+"##"+System.currentTimeMillis()) );
		CallerIdentityInfo.setRedirecturi(appAuthenRequest.getRedirectURI());
		
		OPOwner.setUsertype("adminUser");
		OPOwner.setCurrentUser(accessToken.getUserInfo().getAtid());
		CallerIdentityInfo.setAccesstoken(accessToken.getAccessToken());
	}
	
	@BeforeClass
	public static void initEnvironment() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
			"dubbo-debugEnv.xml",
			"global-config.xml",
			"rdbms-dataSource.xml",
			"redis.xml",
			"transaction.xml",
			"reference-user-service.xml",
			"authen-service.xml",
			"security-service.xml"
		});
		context.start();
		SpringEnvironment.context = context;
	}
	
	@AfterClass
	public static void stopServiceConsumer() {
		if (context != null) {
			context.stop();
			context.close();
			context = null;
		}
	}
}
