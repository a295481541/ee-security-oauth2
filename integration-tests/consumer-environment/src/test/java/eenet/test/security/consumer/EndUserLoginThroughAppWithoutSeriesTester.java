package eenet.test.security.consumer;

import org.junit.Assert;
import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserSMSSignOnBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

/**
 * 单元测试：最终用户通过无体系系统登录
 * 2017年1月14日
 * @author Orion
 */
public class EndUserLoginThroughAppWithoutSeriesTester extends SpringEnvironment {
	/* ==============================================================================
	 * 预期：用户登录成功
	 ============================================================================= */
//	@Test
	public void loginByAccount() throws EncryptException {
		System.out.println("=======loginByAccount");
		SignOnGrant grantObj = userSignOnService.getSignOnGrant(appId, seriesId, redirectURI, loginAccount,RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		if ( grantObj==null || grantObj.isSuccessful()==false )
			Assert.fail(grantObj.getStrMessage());
		
		System.out.println("======="+EEBeanUtils.object2Json(grantObj));
		
		System.out.println("========================================"+RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()));
		AccessToken tokenObj = userSignOnService.getAccessToken(appId, RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()), grantObj.getGrantCode());
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(tokenObj));
	}
	
//	@Test
	public void sentSMS4Login() {
		SimpleResponse result = userSMSSignOnService.sendSMSCode4Login(appId, seriesId, Long.parseLong(loginAccount));
		if ( result==null || result.isSuccessful()==false )
			Assert.fail(result.getStrMessage());
		System.out.println(EEBeanUtils.object2Json(result));
	}
	
//	@Test
	public void loginBySMS() throws EncryptException {
		String smsCode = "919640";
		
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()));appRequest.setBizSeriesId(seriesId);
		AccessToken tokenObj = userSMSSignOnService.getAccessToken(appRequest, Long.parseLong(loginAccount), smsCode);
		
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(tokenObj));
	}
	
	
	/* ==============================================================================
	 * 预期：用户登录失败
	 ============================================================================= */
//	@Test
	public void loginByAccountFail() throws EncryptException {
		//失败原因一：公共系统没有指定业务体系ID
		SignOnGrant grantObj_1 = userSignOnService.getSignOnGrant(appId, redirectURI, loginAccount, RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		Assert.assertFalse(grantObj_1.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(grantObj_1));
		
		//失败原因二：密码错误
		SignOnGrant grantObj_2 = userSignOnService.getSignOnGrant(appId, seriesId, redirectURI, loginAccount, RSAUtil.encrypt(transferRSAEncrypt, password+"AAA"+"##"+System.currentTimeMillis()));
		Assert.assertFalse(grantObj_2.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(grantObj_2));
	}
	
	
//	@Test
	public void sentSMS4LoginFail() throws EncryptException {
//		//失败原因一：公共系统没有指定业务体系ID
//		SimpleResponse result_1 = userSMSSignOnService.sendSMSCode4Login(appId, Long.parseLong(loginAccount));
//		Assert.assertFalse(result_1.isSuccessful());
//		System.out.println(EEBeanUtils.object2Json(result_1));
//		
//		//失败原因二：手机号码不存在
//		SimpleResponse result_2 = userSMSSignOnService.sendSMSCode4Login(appId, seriesId, Long.parseLong("13812345678"));
//		Assert.assertFalse(result_2.isSuccessful());
//		System.out.println(EEBeanUtils.object2Json(result_2));
		
		//失败原因三：验证短信时的体系ID与发短信时的体系ID不一致
		SimpleResponse result_3 = userSMSSignOnService.sendSMSCode4Login(appId, seriesId, Long.parseLong(loginAccount));
		if ( result_3==null || result_3.isSuccessful()==false )
			Assert.fail(result_3.getStrMessage());
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()));appRequest.setBizSeriesId(seriesId2);
		AccessToken tokenObj = userSMSSignOnService.getAccessToken(appRequest, Long.parseLong(loginAccount), "792081");
		Assert.assertFalse(tokenObj.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(tokenObj));
	}
	
	private EndUserSignOnBizService userSignOnService = (EndUserSignOnBizService)super.getContext().getBean("EndUserSignOnBizService");
	private EndUserSMSSignOnBizService userSMSSignOnService = (EndUserSMSSignOnBizService)super.getContext().getBean("EndUserSMSSignOnBizService");
	private RSAEncrypt transferRSAEncrypt = (RSAEncrypt) super.getContext().getBean("transferRSAEncrypt");
	private final String appId = IdentityInfo.commonAppId;
	private final String redirectURI = IdentityInfo.commonRedirectURI;
	private final String appSecret = IdentityInfo.commonAppSecret;
	private final String seriesId = IdentityInfo.seriesId;
	private final String loginAccount = IdentityInfo.userLoginAccount;
	private final String password = IdentityInfo.userPassword;
	
	private final String seriesId2 = "B19E4873E1DD4750A3A584A8C0584AAA";//国家开放大学
}
