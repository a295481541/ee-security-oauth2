package eenet.test.security.consumer;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
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
import oracle.net.aso.p;

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
	public void loginByAccount() throws Exception {
		SignOnGrant grantObj = userSignOnService.getSignOnGrant(appId, redirectURI, loginAccount, encrypt(password+"##"+System.currentTimeMillis()));
		if ( grantObj==null || grantObj.isSuccessful()==false )
			Assert.fail(grantObj.getStrMessage());
		
		AccessToken tokenObj = userSignOnService.getAccessToken(appId, RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()), grantObj.getGrantCode());
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
	public void loginBySMS() throws EncryptException {
		String smsCode = "684522";
		
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()));
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
		//失败原因一：指定的业务体系ID与应用所隶属的业务体系不一致
		SignOnGrant grantObj_1 = userSignOnService.getSignOnGrant(appId, seriesId+"AAA", redirectURI, loginAccount, RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		Assert.assertFalse(grantObj_1.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(grantObj_1));
		
		//失败原因二：密码错误
		SignOnGrant grantObj_2 = userSignOnService.getSignOnGrant(appId, redirectURI, loginAccount,RSAUtil.encrypt(transferRSAEncrypt, password+"AAA"+"##"+System.currentTimeMillis()));
		Assert.assertFalse(grantObj_2.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(grantObj_2));
	}
	
//	@Test
	public void sentSMS4LoginFail() throws EncryptException {
//		//失败原因一：指定的业务体系ID与应用所隶属的业务体系不一致
//		SimpleResponse result_1 = userSMSSignOnService.sendSMSCode4Login(appId, seriesId+"AAA", Long.parseLong(loginAccount));
//		Assert.assertFalse(result_1.isSuccessful());
//		System.out.println(EEBeanUtils.object2Json(result_1));
//		
//		//失败原因二：手机号码不存在
//		SimpleResponse result_2 = userSMSSignOnService.sendSMSCode4Login(appId, Long.parseLong("13812345678"));
//		Assert.assertFalse(result_2.isSuccessful());
//		System.out.println(EEBeanUtils.object2Json(result_2));
		
		//失败原因三：验证短信验证码时，体系ID错误
//		SimpleResponse result_3 = userSMSSignOnService.sendSMSCode4Login(appId, Long.parseLong(loginAccount));
//		if ( result_3==null || result_3.isSuccessful()==false )
//			Assert.fail(result_3.getStrMessage());
		AppAuthenRequest appRequest = new AppAuthenRequest();
		appRequest.setAppId(appId);appRequest.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()));appRequest.setBizSeriesId(seriesId2);
		AccessToken tokenObj = userSMSSignOnService.getAccessToken(appRequest, Long.parseLong(loginAccount), "967909");
		Assert.assertFalse(tokenObj.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(tokenObj));
	}
	
	public static String encrypt(String plaintext) throws Exception {
		String sslPublicKey = 
				"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3ofG3TuzCBaolNYFuTVkOv8yN" + "\r" +
				"B+u3KvSwqqMYsqAKK/q518kyVnl5Mq2h4kqE6YKaV1hJgsd0n4McjCg06xXQP1nh" + "\r" +
				"w3kjX/cL0W6jKTTERDnNDK6ifIdczsFOsaFMSxuA9T3Laji3WmTz4sDpkBN7Ymql" + "\r" +
				"yzqa7HG12GH4zODWtwIDAQAB" + "\r";
		
		byte[] buffer= Base64.decodeBase64(sslPublicKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec= new X509EncodedKeySpec(buffer);
		RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
		
		Cipher cipher= Cipher.getInstance("RSA/ECB/PKCS1Padding");//RSA是加密方法，ECB是加密模式，PKCS1Padding是填充方式
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] output= cipher.doFinal(plaintext.getBytes("UTF-8"));
		
		return Base64.encodeBase64String(output);
	}
	
	
	private EndUserSignOnBizService userSignOnService = (EndUserSignOnBizService)super.getContext().getBean("EndUserSignOnBizService");
	private EndUserSMSSignOnBizService userSMSSignOnService = (EndUserSMSSignOnBizService)super.getContext().getBean("EndUserSMSSignOnBizService");
	private final String appId = IdentityInfo.seriesedAppId;
	private final String seriesId = IdentityInfo.seriesId;
	private final String redirectURI = IdentityInfo.seriesedRedirectURI;
	private final String appSecret = IdentityInfo.seriesedAppSecret;
	private final String loginAccount = IdentityInfo.userLoginAccount;
	private final String password = IdentityInfo.userPassword;
	private RSAEncrypt transferRSAEncrypt = (RSAEncrypt) super.getContext().getBean("transferRSAEncrypt");
	
	private final String seriesId2 = "B19E4873E1DD4750A3A584A8C0584AAA";//国家开放大学
	
	
}
