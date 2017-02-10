package eenet.test.security.consumer;

import org.junit.Assert;
import org.junit.Test;

import com.eenet.authen.AccessToken;
import com.eenet.authen.BusinessSeries;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserCredentialBizService;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.SignOnGrant;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

public class EndUserReSetCredentialTester extends SpringEnvironment  {
	
	
//	@Test
	public void initEndUserLoginPassword() throws Exception {
		
		
		EndUserInfo endUserInfo_1 = new EndUserInfo();
		endUserInfo_1.setAtid("24E8111F0BDB485D9766D5C5F3E11E54");
		
		EndUserCredential  credential_1 = new EndUserCredential();
		credential_1.setEndUser(endUserInfo_1);
		credential_1.setPassword(RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		
		SimpleResponse response_1 = endUserCredentialBizService.initEndUserLoginPassword(credential_1);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1==null || response_1.isSuccessful()==false )
			Assert.assertFalse(response_1.isSuccessful());
		
	}
	
	
	
//	@Test
	public void changeEndUserLoginPassword() throws Exception {
		
		
		EndUserInfo endUserInfo_1 = new EndUserInfo();
		endUserInfo_1.setAtid("24E8111F0BDB485D9766D5C5F3E11E54");
		
		EndUserCredential  credential_1 = new EndUserCredential();
		credential_1.setEndUser(endUserInfo_1);
		credential_1.setPassword(RSAUtil.encrypt(transferRSAEncrypt, password+"##"+System.currentTimeMillis()));
		
		SimpleResponse response_1 = endUserCredentialBizService.changeEndUserLoginPassword(credential_1,RSAUtil.encrypt(transferRSAEncrypt, "888888"));
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1==null || response_1.isSuccessful()==false )
			Assert.assertFalse(response_1.isSuccessful());
		
	}
		
		
		
//	@Test
	public void changeEndUserLoginPassword_2() throws Exception {
		
		
		EndUserInfo endUserInfo_1 = new EndUserInfo();
		endUserInfo_1.setAtid("24E8111F0BDB485D9766D5C5F3E11E54");
		
		EndUserCredential  credential_1 = new EndUserCredential();
		credential_1.setEndUser(endUserInfo_1);
		credential_1.setPassword(RSAUtil.encrypt(transferRSAEncrypt, ""+"##"+System.currentTimeMillis()));
		
		
		EndUserLoginAccount endUserLoginAccount = new EndUserLoginAccount();
		endUserLoginAccount.setLoginAccount("13922990914");
		
		SimpleResponse response_1 = endUserCredentialBizService.changeEndUserLoginPassword(credential_1,endUserLoginAccount,RSAUtil.encrypt(transferRSAEncrypt, "888888"));
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1==null || response_1.isSuccessful()==false )
			Assert.assertFalse(response_1.isSuccessful());
		
	}
	
	
//	@Test
	public void resetEndUserLoginPassword() throws Exception {
		
		SimpleResponse response_1 = endUserCredentialBizService.resetEndUserLoginPassword("24E8111F0BDB485D9766D5C5F3E11E54");
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1==null || response_1.isSuccessful()==false )
			Assert.assertFalse(response_1.isSuccessful());
		
	}
	
	
//	@Test
	public void retrieveEndUserCredentialInfo() throws Exception {
		
		EndUserCredential response_1 = endUserCredentialBizService.retrieveEndUserCredentialInfo("544A1FB8166D4E979E32DD87E346544D","24E8111F0BDB485D9766D5C5F3E11E54");
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null  )
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
	
//	@Test
	public void retrieveEndUserSecretKey() throws Exception {
		
		EndUserCredential response_1 = endUserCredentialBizService.retrieveEndUserSecretKey("544A1FB8166D4E979E32DD87E346544D","24E8111F0BDB485D9766D5C5F3E11E54");
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null  )
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
	
	
	
	@Test
	public void retrieveEndUserSecretKey2() throws Exception {
		
		
		
		EndUserCredential response_1 = endUserCredentialBizService.retrieveEndUserSecretKey("544A1FB8166D4E979E32DD87E346544D","24E8111F0BDB485D9766D5C5F3E11E54",StorageRSADecrypt);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null  )
			Assert.assertTrue(response_1.isSuccessful());
		
		System.out.println(response_1.getPassword());
		
	}
	
	
	private final String appId = IdentityInfo.seriesedAppId;
	private final String seriesId = IdentityInfo.seriesId;
	private final String redirectURI = IdentityInfo.seriesedRedirectURI;
	private final String appSecret = IdentityInfo.seriesedAppSecret;
	private final String loginAccount = IdentityInfo.userLoginAccount;
	private final String password = IdentityInfo.userPassword;
	private final String userId = IdentityInfo.userId;
	private RSAEncrypt transferRSAEncrypt = (RSAEncrypt) super.getContext().getBean("transferRSAEncrypt");
	private RSADecrypt StorageRSADecrypt = (RSADecrypt) super.getContext().getBean("StorageRSADecrypt");
	private EndUserCredentialBizService endUserCredentialBizService = (EndUserCredentialBizService)super.getContext().getBean("EndUserCredentialBizService");

}
