package com.eenet.test;

import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.StringResponse;
import com.eenet.base.biz.GenericSimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.security.EndUserCredentialReSetBizService;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 因为涉及到短信验证码
 * 该测试只能分阶段手工完成
 * 2016年7月22日
 * @author Orion
 */
public class EndUserCredentialReSetTester extends SpringEnvironment {
	private final EndUserCredentialReSetBizService resetService = (EndUserCredentialReSetBizService)super.getContext().getBean("EndUserCredentialReSetBizImpl");
	private final RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
	private final EndUserSignOnBizService signService = (EndUserSignOnBizService)super.getContext().getBean("EndUserSignOnBizImpl");
	private final GenericSimpleBizImpl genericBiz = (GenericSimpleBizImpl)super.getContext().getBean("GenericSimpleBizImpl");
	private final String appId = "9CFF0CA0D43D4B2DAC1EFC6A86FCB191";
	private final String redirectURI = "http://hz.saas.workeredu.com";
	private final String appSecretKey = "pASS41#";
	private long mobile = 13752059885l;
	
//	@Test
	public void resetPasswordBySMS() {
		StringResponse sendSMSRS = resetService.sendSMSCode4ResetPassword(appId, mobile);
		if ( !sendSMSRS.isSuccessful() ) {
			System.out.println(sendSMSRS.getStrMessage());
			return;
		}
		String userId = sendSMSRS.getResult();
		System.out.println("重置密码用户：" + userId);
	}
	
//	@Test
	public void resetPasswordAndLogin() throws Exception {
		String userId = "9BC7BA2AEF584220BBC2845BF61A04B9";//<==要重置密码用户的标识，从resetPasswordBySMS()方法获得
		String smsCode = "706041";//<==收到短信后填于此处
		SimpleResponse smsCodeCorrect = resetService.validateSMSCode4ResetPassword(userId, smsCode, false);
		if ( !smsCodeCorrect.isSuccessful()) {
			System.out.println(smsCodeCorrect.getStrMessage());
			return;
		}
		System.out.println("短信验证码："+smsCode+" 正确");
		
		/* 重置密码并登陆 */
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(RSAUtil.encryptWithTimeMillis(encrypt, appSecretKey));appRequest.setRedirectURI(redirectURI);
		
		EndUserCredential credential = new EndUserCredential();
		String newPassword = EEBeanUtils.randomSixNum();
		EndUserInfo endUser = new EndUserInfo();endUser.setAtid(userId);
		credential.setEndUser(endUser);credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, newPassword));
		System.out.println("新密码： "+newPassword);
		
		AccessToken accessToken = resetService.resetPasswordBySMSCodeWithLogin(appRequest, credential, smsCode, String.valueOf(mobile));
		if ( !accessToken.isSuccessful() ) {
			System.out.println(accessToken.getStrMessage());
			return;
		}
		System.out.println("accessToken: " + accessToken.getAccessToken());
		System.out.println("RefreshToken: " + accessToken.getRefreshToken());
		
		/* 登出 */
		signService.signOut(appId, userId);
		
		/* 用新密码登录 */
		String loginAccount = this.getLoginAccount(userId);
		SignOnGrant getSignOnGrant = 
				signService.getSignOnGrant(appId, redirectURI, loginAccount, RSAUtil.encryptWithTimeMillis(encrypt, newPassword));
		if (!getSignOnGrant.isSuccessful()){
			System.out.println("getSignOnGrant : \n"+getSignOnGrant.getStrMessage());
			return;
		}
		System.out.println("getSignOnGrant: "+getSignOnGrant.getGrantCode());
		
		accessToken = 
				signService.getAccessToken(appId, RSAUtil.encryptWithTimeMillis(encrypt, appSecretKey), getSignOnGrant.getGrantCode());
		if (!accessToken.isSuccessful()){
			System.out.println("getAccessToken : \n"+accessToken.getStrMessage());
			return;
		}
		System.out.println("accessToken AccessToken: "+accessToken.getAccessToken());
		System.out.println("accessToken RefreshToken: "+accessToken.getRefreshToken());
		System.out.println("accessToken UserInfo.Name: "+accessToken.getUserInfo().getName());
		
		/* 登出 */
		signService.signOut(appId, userId);
	}
	
	private String getLoginAccount(String userId) {
		QueryCondition condition = new QueryCondition();
		condition.setMaxQuantity(1);
		condition.addCondition(new ConditionItem("userInfo.atid",RangeType.EQUAL,userId,null));
		
		SimpleResultSet<EndUserLoginAccount> queryAccountRS = genericBiz.query(condition,EndUserLoginAccount.class);
		if (queryAccountRS.getResultSet().size()==1) {
			return queryAccountRS.getResultSet().get(0).getLoginAccount();
		}
		return null;
	}
	
//	@Test
	public void threeTimeSendFail() {
		resetPasswordBySMS();
		resetPasswordBySMS();
		resetPasswordBySMS();
	}
}
