package eenet.test.security.consumer;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.authen.response.AppAuthenResponse;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

public class IdentityAuthenticationTester extends SpringEnvironment  {
	
//	@Test
	public void appAuthen() throws Exception {
		
		AppAuthenRequest request = new AppAuthenRequest();  //有体系系统
		request.setAppId("5215065683F6418AA8202ED24C0D25C0");
		request.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, "pASS3#"+"##"+System.currentTimeMillis()));
		request.setRedirectURI("http://www.gdzgjy.com");
		
		
		AppAuthenResponse response_1 = identityAuthenticationBizService.appAuthen(request);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
		
		
		//无体系系统
		request.setAppId("0CC2921AB3264F29BA2C4DA68F152715");
		request.setAppSecretKey( RSAUtil.encrypt(transferRSAEncrypt, "pAsW3^"+"##"+System.currentTimeMillis()));
		request.setRedirectURI("http://eechat.gzedu.com");
		request.setBizSeriesId("544A1FB8166D4E979E32DD87E346544F");
		
		
		response_1 = identityAuthenticationBizService.appAuthen(request);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
//	@Test
	public void appAuthenWithoutTimeMillis() throws Exception {
		
		AppAuthenRequest request = new AppAuthenRequest();  //有体系系统
		request.setAppId("5215065683F6418AA8202ED24C0D25C0");
		request.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, "pASS3#"));
		request.setRedirectURI("http://www.gdzgjy.com");
		
		
		AppAuthenResponse response_1 = identityAuthenticationBizService.appAuthenWithoutTimeMillis(request);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
		
		
		//无体系系统
		request.setAppId("0CC2921AB3264F29BA2C4DA68F152715");
		request.setAppSecretKey( RSAUtil.encrypt(transferRSAEncrypt, "pAsW3^"));
		request.setRedirectURI("http://eechat.gzedu.com");
		request.setBizSeriesId("544A1FB8166D4E979E32DD87E346544F");
		
		
		response_1 = identityAuthenticationBizService.appAuthenWithoutTimeMillis(request);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
//	@Test
	public void endUserAuthenOnly() throws Exception {
		
		
		SignOnGrant grantObj = userSignOnService.getSignOnGrant(appId, redirectURI, loginAccount, encrypt(password+"##"+System.currentTimeMillis()));
		if ( grantObj==null || grantObj.isSuccessful()==false )
			Assert.fail(grantObj.getStrMessage());
		
		AccessToken tokenObj = userSignOnService.getAccessToken(appId, RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()), grantObj.getGrantCode());
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(tokenObj));
		
		
		UserAccessTokenAuthenRequest request = new UserAccessTokenAuthenRequest();
		request.setUserAccessToken(tokenObj.getAccessToken());
		
		
		
		UserAccessTokenAuthenResponse response = identityAuthenticationBizService.endUserAuthenOnly(request);
		
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(response));
		
	}
	
	@Test
	public void endUserAuthen() throws Exception {
		
		
		SignOnGrant grantObj = userSignOnService.getSignOnGrant(appId, redirectURI, loginAccount, encrypt(password+"##"+System.currentTimeMillis()));
		if ( grantObj==null || grantObj.isSuccessful()==false )
			Assert.fail(grantObj.getStrMessage());
		
		AccessToken tokenObj = userSignOnService.getAccessToken(appId, RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()), grantObj.getGrantCode());
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(tokenObj));
		
		
		UserAccessTokenAuthenRequest request = new UserAccessTokenAuthenRequest();
		request.setUserAccessToken(tokenObj.getAccessToken());
		
		request.setAppId(appId);
		request.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()));
		request.setUserId(userId);
		
		
		UserAccessTokenAuthenResponse response = identityAuthenticationBizService.endUserAuthen(request);
		
		if ( tokenObj==null || tokenObj.isSuccessful()==false )
			Assert.fail(tokenObj.getStrMessage());
		
		System.out.println(EEBeanUtils.object2Json(response));
		
	}
	
	
	
	
	
	
	private final String appId = IdentityInfo.seriesedAppId;
	private final String seriesId = IdentityInfo.seriesId;
	private final String redirectURI = IdentityInfo.seriesedRedirectURI;
	private final String appSecret = IdentityInfo.seriesedAppSecret;
	private final String loginAccount = IdentityInfo.userLoginAccount;
	private final String password = IdentityInfo.userPassword;
	private final String userId = IdentityInfo.userId;
	private  RSAEncrypt transferRSAEncrypt = (RSAEncrypt) super.getContext().getBean("transferRSAEncrypt");
	private  RSADecrypt StorageRSADecrypt = (RSADecrypt) super.getContext().getBean("StorageRSADecrypt");
	private EndUserSignOnBizService userSignOnService = (EndUserSignOnBizService)super.getContext().getBean("EndUserSignOnBizService");
	private IdentityAuthenticationBizService identityAuthenticationBizService = (IdentityAuthenticationBizService)super.getContext().getBean("IdentityAuthenticationBizService");
	
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

}
