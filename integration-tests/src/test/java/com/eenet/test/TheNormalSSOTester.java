package com.eenet.test;

import com.eenet.auth.ThreadSharedAuthParameter;
import com.eenet.authen.AccessToken;
import com.eenet.authen.EENetEndUserCredential;
import com.eenet.authen.EENetEndUserCredentialBizService;
import com.eenet.authen.EENetEndUserLoginAccount;
import com.eenet.authen.EENetEndUserMainAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.EndUserLoginAccountType;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.SingleSignOnBizService;
import com.eenet.authen.ThirdPartySSOAppBizService;
import com.eenet.base.BaseEntity;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.test.bizmock.MockBizWithUserService;
import com.eenet.test.bizmock.MockSSOSystem;
import com.eenet.test.env.DubboBizConsumerENV;
import com.eenet.test.env.DubboBizProviderENV;
import com.eenet.util.cryptography.RSADecrypt;

import static org.junit.Assert.*;

import org.junit.Test;

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
	private final String thirdPartyAppID = "74821455C9A9484FB57FAB8642440F70";
	private final String thirdPartyAppSecretKey = "web#APp99";
	private final String thirdPartyAppRedirectURIPrefix = "http://www.eenet.com/";
	
//	@Test
	public void temp() {
		EndUserLoginAccountBizService loginAccountService = (EndUserLoginAccountBizService)super.getContext().getBean("EndUserLoginAccountBizService");
//		SimpleResponse result = loginAccountService.removeEndUserLoginAccount(this.email);//先删掉肯存在的登录账号
		
		StringResponse result = loginAccountService.retrieveEndUserMainAccount(this.email);
		
		System.out.println(result.isSuccessful() +" : "+ result.getStrMessage());
	}
	
	/**
	 * 测试前提条件：
	 * 主账号表（表名：V_USER_AUTH）存在账号（字段名：ACCOUNT）：OrionTest
	 * 单点登录接入系统表（表名：AUTHEN_SSO_SYSTEM）存在ID（字段名：ID）：74821455C9A9484FB57FAB8642440F70、密码（字段名：SECRT_KEY）：web#APp99、跳转地址前缀：（REDIRECT_URI_PREFIX）：http://www.eenet.com/
	 * 
	 * 2016年5月18日
	 * @author Orion
	 * @throws InterruptedException 
	 */
	@Test
	public void test() throws InterruptedException{
		//启动服务提供者
		DubboBizProviderENV.initServiceProvider();
		
		EndUserLoginAccountBizService loginAccountService = (EndUserLoginAccountBizService)super.getContext().getBean("EndUserLoginAccountBizService");
		EENetEndUserCredentialBizService credentialService = (EENetEndUserCredentialBizService)super.getContext().getBean("EENetEndUserCredentialBizService");
		MockSSOSystem ssoSystem = new MockSSOSystem();
		SingleSignOnBizService ssoService = (SingleSignOnBizService)super.getContext().getBean("SingleSignOnBizService");;
		MockBizWithUserService bizService = (MockBizWithUserService)super.getContext().getBean("MockBizWithUserService");
		
		/* 注册用户登录账号 */ //<---- 该动作由业务系统完成
		EENetEndUserMainAccount mainAccount = new EENetEndUserMainAccount();
		mainAccount.setAccount(this.mainAccount);
		EENetEndUserLoginAccount loginAccount = new EENetEndUserLoginAccount();
		loginAccount.setMainAccount(mainAccount);loginAccount.setLoginAccount(email);loginAccount.setAccountType(this.loginType);
		loginAccountService.removeEndUserLoginAccount(this.email);//先删掉肯存在的登录账号
		EENetEndUserLoginAccount userRegistResult = loginAccountService.registeEndUserLoginAccount(loginAccount);
		if (!userRegistResult.isSuccessful())
			fail(userRegistResult.getStrMessage());
		
		/* 初始化用户登录密码 */ //<---- 该动作由业务系统完成
		EENetEndUserCredential credential = new EENetEndUserCredential();
		credential.setMainAccount(mainAccount);credential.setSecretKey(loginPassword);
		SimpleResponse setPasswordResult = credentialService.initUserLoginPassword(credential);
		if (!setPasswordResult.isSuccessful()) {
			RSADecrypt decrypt = (RSADecrypt)super.getContext().getBean("RSADecrypt");
			StringResponse getExistPasswordResult = credentialService.retrieveUserSecretKey(this.mainAccount, decrypt);
			if (!getExistPasswordResult.isSuccessful())
				fail("getExistPasswordResult: \n"+getExistPasswordResult.getStrMessage()+"\n\n\n\nsetPasswordResult: "+setPasswordResult.getStrMessage());
			System.out.println("----------------------现密码 ： "+getExistPasswordResult.getResult()+"---------------------");
			credential.setSecretKey(getExistPasswordResult.getResult());
			setPasswordResult = credentialService.changeUserLoginPassword(credential, loginPassword);
		}
		if (!setPasswordResult.isSuccessful()) {
			fail(setPasswordResult.getStrMessage());
		}
		
		/* 获得认证授权码 */
		SignOnGrant signOnGrant = ssoSystem.getSignOnGrant(thirdPartyAppID,thirdPartyAppRedirectURIPrefix,email,loginPassword);
		if(!signOnGrant.isSuccessful())
			fail(signOnGrant.getStrMessage());
		
		/* 获得访问授权码 */
		AccessToken accessToken = ssoService.getAccessToken(thirdPartyAppID, thirdPartyAppSecretKey,
				signOnGrant.getGrantCode());
		if(!accessToken.isSuccessful())
			fail(accessToken.getStrMessage());
		
		/* 当前用户信息及访问令牌写入线程变量 */
		ThreadSharedAuthParameter.CurEndUserMainAccount.set(mainAccount);
		ThreadSharedAuthParameter.CurEndUserToken.set(accessToken);
		
		/* 访问业务应用 */
		String callBizResult = bizService.sayHello(new BaseEntity());
		assertNotNull("业务服务返回空对象",callBizResult);
		
		/* 刷新访问授权码 */
		accessToken = ssoService.refreshAccessToken(thirdPartyAppID, thirdPartyAppSecretKey, accessToken.getRefreshToken());
		if(!accessToken.isSuccessful())
			fail(accessToken.getStrMessage());
		
		/* 再次访问业务应用（使用新的访问令牌） */
		callBizResult = bizService.sayHello(new BaseEntity());
		assertNotNull("业务服务返回空对象",callBizResult);
		
		/* 废弃最终用户登录账号 */
		SimpleResponse rmLoginAccountResult = loginAccountService.removeEndUserLoginAccount(this.email);
		if (!rmLoginAccountResult.isSuccessful())
			fail(rmLoginAccountResult.getStrMessage());
		
		for (int i=0;i<5;i++) { //删除用户后accessToken仍会保留5分钟
			Thread.sleep(1000 * 61);
			System.out.println("等待"+(i+1)+"分钟");
		}
		
		/* 再次访问业务应用，被拒绝 */
		callBizResult = bizService.sayHello(new BaseEntity());
		assertNull("废弃登录账号和单点登录系统后仍可以调用业务服务",callBizResult);
		
		//停止服务提供者
		DubboBizProviderENV.stopServiceProvider();
	}
}
