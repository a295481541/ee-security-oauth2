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

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

/**
 * 单元测试：最终用户通过有体系系统登录
 * 2017年1月14日
 * @author Orion
 */
public class EndUserLoginThroughAppWithSeriesTester extends SpringEnvironment {
	/* ==============================================================================
	 * 预期：用户登录成功
	 ============================================================================= */
	@Test
	public void loginByAccount() {
		SignOnGrant grantObj = userSignOnService.getSignOnGrant(appId, redirectURI, loginAccount, password);
		if ( grantObj==null || grantObj.isSuccessful()==false )
			Assert.fail(grantObj.getStrMessage());
		
		AccessToken tokenObj = userSignOnService.getAccessToken(appId, appSecret, grantObj.getGrantCode());
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(tokenObj));
	}
	
//	@Test
	public void sentSMS4Login() {
		SimpleResponse result = userSMSSignOnService.sendSMSCode4Login(appId, Long.parseLong(loginAccount));
		if ( result==null || result.isSuccessful()==false )
			Assert.fail(result.getStrMessage());
		System.out.println(EEBeanUtils.object2Json(result));
	}
	
//	@Test
	public void loginBySMS() {
		String smsCode = "";
		
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(appSecret);
		AccessToken tokenObj = userSMSSignOnService.getAccessToken(appRequest, Long.parseLong(loginAccount), smsCode);
		
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(tokenObj));
	}
	
	
	/* ==============================================================================
	 * 预期：用户登录失败
	 ============================================================================= */
//	@Test
	public void loginByAccountFail() {
		//失败原因一：指定的业务体系ID与应用所隶属的业务体系不一致
		SignOnGrant grantObj_1 = userSignOnService.getSignOnGrant(appId, seriesId+"AAA", redirectURI, loginAccount, password);
		Assert.assertFalse(grantObj_1.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(grantObj_1));
		
		//失败原因二：密码错误
		SignOnGrant grantObj_2 = userSignOnService.getSignOnGrant(appId, redirectURI, loginAccount, password+"AAA");
		Assert.assertFalse(grantObj_2.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(grantObj_2));
	}
	
//	@Test
	public void sentSMS4LoginFail() {
		//失败原因一：指定的业务体系ID与应用所隶属的业务体系不一致
		SimpleResponse result_1 = userSMSSignOnService.sendSMSCode4Login(appId, seriesId+"AAA", Long.parseLong(loginAccount));
		Assert.assertFalse(result_1.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(result_1));
		
		//失败原因二：手机号码不存在
		SimpleResponse result_2 = userSMSSignOnService.sendSMSCode4Login(appId, Long.parseLong("13812345678"));
		Assert.assertFalse(result_2.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(result_2));
		
		//失败原因三：验证短信验证码时，体系ID错误
		SimpleResponse result_3 = userSMSSignOnService.sendSMSCode4Login(appId, Long.parseLong(loginAccount));
		if ( result_3==null || result_3.isSuccessful()==false )
			Assert.fail(result_3.getStrMessage());
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(appSecret);appRequest.setBizSeriesId(seriesId2);
		AccessToken tokenObj = userSMSSignOnService.getAccessToken(appRequest, Long.parseLong(loginAccount), "111111");
		Assert.assertFalse(tokenObj.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(tokenObj));
	}
	
	
	private EndUserSignOnBizService userSignOnService = (EndUserSignOnBizService)super.getContext().getBean("EndUserSignOnBizService");
	private EndUserSMSSignOnBizService userSMSSignOnService = (EndUserSMSSignOnBizService)super.getContext().getBean("EndUserSMSSignOnBizService");
	private final String appId = IdentityInfo.seriesedAppId;
	private final String seriesId = IdentityInfo.seriesId;
	private final String redirectURI = IdentityInfo.seriesedRedirectURI;
	private final String appSecret = IdentityInfo.seriesedAppSecret;
	private final String loginAccount = IdentityInfo.userLoginAccount;
	private final String password = IdentityInfo.userPassword;
	
	private final String seriesId2 = "B19E4873E1DD4750A3A584A8C0584AAA";//国家开放大学
}
