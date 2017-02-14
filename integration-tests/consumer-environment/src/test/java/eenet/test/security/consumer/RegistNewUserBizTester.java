package eenet.test.security.consumer;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.LoginAccountType;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.security.RegistNewUserBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

public class RegistNewUserBizTester extends SpringEnvironment  {
//	@Test
	public void registEndUserWithLogin() throws Exception {
		
		EndUserInfo endUser = new EndUserInfo();
		endUser.setName("张三");
		
		
		EndUserLoginAccount account  =  new EndUserLoginAccount();
		account.setAccountType(LoginAccountType.USERNAME);
		account.setLoginAccount("13533594931");
		
		
		
		EndUserCredential  credential = new EndUserCredential();
		credential.setPassword(RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		
		
		AccessToken  token  =  registNewUserBizService.registEndUserWithLogin(endUser, account, credential);
		
		System.out.println(EEBeanUtils.object2Json(token));
		if (token!= null)
			Assert.assertTrue(token.isSuccessful());
		
	}
	
	@Test
	public void registEndUserWithMulAccountAndLogin() throws Exception {
		EndUserInfo endUser = new EndUserInfo();
		endUser.setName("张三");
		
		
		EndUserLoginAccount account1  =  new EndUserLoginAccount();
		account1.setLoginAccount("13533594932");
		account1.setAccountType(LoginAccountType.USERNAME);
		
		EndUserLoginAccount account2  =  new EndUserLoginAccount();
		account2.setLoginAccount("13533594933");
		account2.setAccountType(LoginAccountType.USERNAME);
		
		List<EndUserLoginAccount> accounts =new ArrayList<>();
		accounts.add(account1);
		accounts.add(account2);
		
		
		EndUserCredential  credential = new EndUserCredential();
		credential.setPassword(RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		AccessToken  token  =  registNewUserBizService.registEndUserWithMulAccountAndLogin(endUser, accounts, credential);
		
		System.out.println(EEBeanUtils.object2Json(token));
		if (token!= null)
			Assert.assertTrue(token.isSuccessful());
		
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
	private RegistNewUserBizService registNewUserBizService = (RegistNewUserBizService)super.getContext().getBean("RegistNewUserBizService");
	
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
