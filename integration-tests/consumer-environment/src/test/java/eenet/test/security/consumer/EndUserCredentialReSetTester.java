package eenet.test.security.consumer;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.request.UserAccessTokenAuthenRequest;
import com.eenet.authen.response.AppAuthenResponse;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.security.EndUserCredentialReSetBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

public class EndUserCredentialReSetTester extends SpringEnvironment  {
	
//	@Test
	public void sendSMSCode4ResetPassword() throws Exception {//发送重置密码短信验证码   通过 validateSMSCode4ResetPassword 校验
		StringResponse response = endUserCredentialReSetBizService.sendSMSCode4ResetPassword(appId, Long.valueOf(loginAccount));
		if ( response!=null)
			Assert.assertTrue(response.isSuccessful());
		System.out.println(EEBeanUtils.object2Json(response));
	}
	
	@Test
	public void sendSMSCode4ResetPassword2() throws Exception {
		StringResponse response = endUserCredentialReSetBizService.sendSMSCode4ResetPassword(appId, seriesId,Long.valueOf(loginAccount));
		System.out.println(EEBeanUtils.object2Json(response));
		if ( response!=null)
			Assert.assertTrue(response.isSuccessful());
		
		
	}
	
	
	
//	@Test
	public void validateSMSCode4ResetPassword() throws Exception {//校验重置密码短信验证码
		
		SimpleResponse response = endUserCredentialReSetBizService.validateSMSCode4ResetPassword(userId, "112269", true);
		if ( response!=null)
			Assert.assertTrue(response.isSuccessful());
		
	}
	
	
//	@Test
	public void resetPasswordBySMSCodeWithLogin() throws Exception {
		
		
		String smsCode = "";
		String mobile = loginAccount;
		
		AppAuthenRequest request = new AppAuthenRequest();
		request.setAppId(appId);
		request.setAppSecretKey(RSAUtil.encrypt(transferRSAEncrypt, appSecret+"##"+System.currentTimeMillis()));
		request.setBizSeriesId(seriesId);
		request.setRedirectURI(redirectURI);
		
		
		EndUserCredential credential = new EndUserCredential();
		credential.setPassword(RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		
		AccessToken token =  endUserCredentialReSetBizService.resetPasswordBySMSCodeWithLogin(request, credential, smsCode, mobile);
		System.out.println(EEBeanUtils.object2Json(token));
		if ( token!=null)
			Assert.assertTrue(token.isSuccessful());
	}

	
	
//	@Test
	public void resetPasswordBySMSCodeWithoutLogin() throws Exception {
		String smsCode = "";
		String mobile = loginAccount;
		
		
		EndUserCredential credential = new EndUserCredential();
		credential.setPassword(RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		
		SimpleResponse response =  endUserCredentialReSetBizService.resetPasswordBySMSCodeWithoutLogin(credential, smsCode, mobile);
		System.out.println(EEBeanUtils.object2Json(response));
		if ( response!=null)
			Assert.assertTrue(response.isSuccessful());
		
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
	private EndUserCredentialReSetBizService endUserCredentialReSetBizService = (EndUserCredentialReSetBizService)super.getContext().getBean("EndUserCredentialReSetBizService");
	
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
