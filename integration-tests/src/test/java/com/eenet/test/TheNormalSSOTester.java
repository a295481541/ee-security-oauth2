package com.eenet.test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EENetEndUserCredential;
import com.eenet.authen.EENetEndUserCredentialBizService;
import com.eenet.authen.EENetEndUserLoginAccount;
import com.eenet.authen.EENetEndUserMainAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.EndUserLoginAccountType;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.SingleSignOnBizService;
import com.eenet.authen.ThirdPartyAPPType;
import com.eenet.authen.ThirdPartySSOAPP;
import com.eenet.authen.ThirdPartySSOAppBizService;
import com.eenet.base.BaseEntity;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.test.bizmock.MockBizWithUserService;
import com.eenet.test.bizmock.MockSSOSystem;
import com.eenet.test.env.DubboBizConsumerENV;
import com.eenet.util.cryptography.RSADecrypt;

import static org.junit.Assert.*;

/**
 * 测试正常单点登录及认证流程。
 * 测试流程：
+---------------+                                                                                                                     
|  Test Client  |          +-----------------------+    +--------------------------+    +--------------------+    +--------------+    +----------------+    +--------------------------+    
| Role:Consumer |          |  EndUserLoginAccount  |    |  EENetEndUserCredential  |    |  ThirdPartySSOApp  |    |  SSO System  |    |  SingleSignOn  |    |  IdentityAuthentication  |    +------------------+
|   3rdSSOApp   |          |  BizService           |    |  BizService              |    |  BizService        |    | (Just Mock)  |    |  BizService    |    |  BizService              |    |  MockBizService  |
+---------------+          +-----------------------+    +--------------------------+    +--------------------+    +--------------+    +----------------+    +--------------------------+    +------------------+
        |                              |                               |                           |                     |                     |                           |                           |
        |  registeEndUserLoginAccount  |                               |                           |                     |                     |                           |                           |
        |----------------------------->|                               |                           |                     |                     |                           |                           |
        |                              |   initUserLoginPassword       |                           |                     |                     |                           |                           |
        |------------------------------------------------------------->|                           |                     |                     |                           |                           |
        |                              |                               |  registeThirdPartySSOApp  |                     |                     |                           |                           |
        |----------------------------------------------------------------------------------------->|                     |                     |                           |                           |
        |                              |                               |                           |   getSignOnGrant   +-+                    |                           |                           |
        |-------------------------------------------------------------------------------------------------------------->| |   getSignOnGrant   |                           |                           |
        |  return:SignOnGrant          |                               |                           |                    | |------------------->|                           |                           |
        |<--------------------------------------------------------------------------------------------------------------| |                    |                           |                           |
        |                              |                               |                           |                    +-+                    |                           |                           |
        |                              |                               |                           |                     |    getAccessToken   |                           |                           |
        |------------------------------------------------------------------------------------------------------------------------------------->|                           |                           |
        |                              |                               |                           |                     |                     |                           |               doBiz:xxx  +-+
        |-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------->| |
        |                              |                               |                           |                     |                     |                           |                          | |
        |                              |                               |                           |                     |                     |                          +-+  endUserAuthen          | |
        |                              |                               |                           |                     |                     |                          | |<------------------------| |
        |                              |                               |                           |                     |                     |                          | |------------------------>| |--+
        |                              |                               |                           |                     |                     |                          +-+          True           | |  |
        |                              |                               |                           |                     |                     |                           |                          | |<-+
        |                              |                               |                           |                     |                     |                           |                          | |
        |                              |                               |                           |                     |                     |                           |                          +-+
        |                              |                               |                           |                     |  refreshAccessToken |                           |                           |
        |------------------------------------------------------------------------------------------------------------------------------------->|                           |                           |
        |                              |                               |                           |                     |                     |                           |               doBiz:xxx  +-+
        |-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------->| |
        |                              |                               |                           |                     |                     |                           |                          | |
        |                              |                               |                           |                     |                     |                          +-+  endUserAuthen          | |
        |                              |                               |                           |                     |                     |                          | |<------------------------| |
        |                              |                               |                           |                     |                     |                          | |------------------------>| |--+
        |                              |                               |                           |                     |                     |                          +-+           True          | |  |
        |                              |                               |                           |                     |                     |                           |                          | |<-+
        |                              |                               |                           |                     |                     |                           |                          | |
        |                              |                               |                           |                     |                     |                           |                          +-+
        |                              |                               |  removeThirdPartySSOApp   |                     |                     |                           |                           |
        |----------------------------------------------------------------------------------------->|                     |                     |                           |                           |
        |                              |                               |                           |                     |                     |                           |                           |
        |                              |   removeEndUserLoginAccount   |                           |                     |                     |                           |                           |
        |------------------------------------------------------------->|                           |                     |                     |                           |                           |
        |                              |                               |                           |                     |                     |                           |               doBiz:xxx  +-+
        |-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------->| |
        |                              |                               |                           |                     |                     |                           |                          | |
        |                              |                               |                           |                     |                     |                          +-+  endUserAuthen          | |
        |                              |                               |                           |                     |                     |                          | |<------------------------| |
        |                              |                               |                           |                     |                     |                          | |------------------------>| |--+
        |                              |                               |                           |                     |                     |                          +-+         False           | |  |
        |                              |                               |                           |                     |                     |                           |                          | |<-+
        |                              |                               |                           |                     |                     |                           |                          | |
        |                              |                               |                           |                     |                     |                           |                          +-+
        |                              |                               |                           |                     |                     |                           |                           |
 * 2016年5月17日
 * @author Orion
 */
public class TheNormalSSOTester extends DubboBizConsumerENV {
	private final String mainAccount = "OrionTest";
	private final String email = "linhao@eenet.com";
	private final String loginPassword = "eenetABC123";
	private final EndUserLoginAccountType loginType = EndUserLoginAccountType.EMAIL;
	private final String thirdPartyAppSecretKey = "web#APp99";
	/**
	 * 测试前提条件：
	 * 主账号表（表名：V_USER_AUTH）存在账号（字段名：ACCOUNT）：OrionTest
	 * 单点登录接入系统表（表名：AUTHEN_SSO_SYSTEM）存在ID（字段名：ID）：74821455C9A9484FB57FAB8642440F70、密码（字段名：SECRT_KEY）：web#APp99
	 * 
	 * 2016年5月18日
	 * @author Orion
	 */
	public void test(){
		EndUserLoginAccountBizService loginAccountService = (EndUserLoginAccountBizService)super.getContext().getBean("EndUserLoginAccountBizService");
		EENetEndUserCredentialBizService credentialService = (EENetEndUserCredentialBizService)super.getContext().getBean("EENetEndUserCredentialBizService");
		ThirdPartySSOAppBizService thirdPartyService = (ThirdPartySSOAppBizService)super.getContext().getBean("ThirdPartySSOAppBizService");;
		MockSSOSystem ssoSystem = new MockSSOSystem();
		SingleSignOnBizService ssoService = (SingleSignOnBizService)super.getContext().getBean("SingleSignOnBizService");;
		MockBizWithUserService bizService = (MockBizWithUserService)super.getContext().getBean("MockBizWithUserService");
		
		/* 注册用户登录账号 */
		EENetEndUserMainAccount mainAccount = new EENetEndUserMainAccount();
		mainAccount.setAccount(this.mainAccount);
		EENetEndUserLoginAccount loginAccount = new EENetEndUserLoginAccount();
		loginAccount.setMainAccount(mainAccount);loginAccount.setLoginAccount(email);loginAccount.setAccountType(this.loginType);
		loginAccountService.removeEndUserLoginAccount(this.email);//先删掉肯存在的登录账号
		EENetEndUserLoginAccount userRegistResult = loginAccountService.registeEndUserLoginAccount(loginAccount);
		if (!userRegistResult.isSuccessful())
			fail(userRegistResult.getStrMessage());
		
		/* 初始化用户登录密码 */
		EENetEndUserCredential credential = new EENetEndUserCredential();
		credential.setMainAccount(mainAccount);credential.setSecretKey(loginPassword);
		SimpleResponse setPasswordResult = credentialService.initUserLoginPassword(credential);
		if (!setPasswordResult.isSuccessful()) {
			RSADecrypt decrypt = (RSADecrypt)super.getContext().getBean("RSADecrypt");
			StringResponse getExistPasswordResult = credentialService.retrieveUserSecretKey(this.mainAccount, decrypt);
			if (!getExistPasswordResult.isSuccessful())
				fail(setPasswordResult.getStrMessage()+"\n"+getExistPasswordResult.getStrMessage());
			credential.setSecretKey(getExistPasswordResult.getResult());
			setPasswordResult = credentialService.changeUserLoginPassword(credential, loginPassword);
		}
		if (!setPasswordResult.isSuccessful()) {
			fail(setPasswordResult.getStrMessage());
		}
		
		/* 单点登录系统注册 */
		ThirdPartySSOAPP thirdPartyApp = new ThirdPartySSOAPP();
		thirdPartyApp.setRedirectURIPrefix("https://www.google.com");thirdPartyApp.setAppName("单点登录业务系统");
		thirdPartyApp.setAppType(ThirdPartyAPPType.WEBAPP);thirdPartyApp.setSecretKey(thirdPartyAppSecretKey);
		thirdPartyApp = thirdPartyService.registeThirdPartySSOApp(thirdPartyApp);
		if (!thirdPartyApp.isSuccessful())
			fail(thirdPartyApp.getStrMessage());
		
		/* 获得认证授权码 */
		SignOnGrant signOnGrant = ssoSystem.getSignOnGrant(thirdPartyApp.getAppId(),
				thirdPartyApp.getRedirectURIPrefix(), this.email, this.loginPassword);
		if(!signOnGrant.isSuccessful())
			fail(signOnGrant.getStrMessage());
		
		/* 获得访问授权码 */
		AccessToken accessToken = ssoService.getAccessToken(thirdPartyApp.getAppId(), thirdPartyAppSecretKey,
				signOnGrant.getGrantCode());
		if(!accessToken.isSuccessful())
			fail(accessToken.getStrMessage());
		
		/* 访问业务应用 */
		String callBizResult = bizService.sayHello(new BaseEntity());
		assertNotNull("业务服务返回空对象",callBizResult);
		
		/* 刷新访问授权码 */
		accessToken = ssoService.refreshAccessToken(thirdPartyApp.getAppId(), thirdPartyAppSecretKey, accessToken.getRefreshToken());
		if(!accessToken.isSuccessful())
			fail(accessToken.getStrMessage());
		
		/* 再次访问业务应用（使用新的访问令牌） */
		callBizResult = bizService.sayHello(new BaseEntity());
		assertNotNull("业务服务返回空对象",callBizResult);
		
		/* 废弃最终用户登录账号 */
		SimpleResponse rmLoginAccountResult = loginAccountService.removeEndUserLoginAccount(this.email);
		if (!rmLoginAccountResult.isSuccessful())
			fail(rmLoginAccountResult.getStrMessage());
		
		/* 废弃单点登录系统 */
		SimpleResponse rm3rdAppResult = thirdPartyService.removeThirdPartySSOApp(thirdPartyApp.getAppId());
		if (!rm3rdAppResult.isSuccessful())
			fail(rm3rdAppResult.getStrMessage());
		
		/* 再次访问业务应用，被拒绝 */
		callBizResult = bizService.sayHello(new BaseEntity());
		assertNull("废弃登录账号和单点登录系统后仍可以调用业务服务",callBizResult);
	}
}
