package eenet.test.security.consumer;

import org.junit.Assert;
import org.junit.Test;

import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.LoginAccountType;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

public class EndUserLoginAccountTester extends SpringEnvironment  {
	
	
//	@Test
	public void initEndUserLoginPassword() throws Exception {
		
		EndUserInfo endUserInfo_1 = new EndUserInfo();
		endUserInfo_1.setAtid("24E8111F0BDB485D9766D5C5F3E11E54");
		
		EndUserLoginAccount endUserLoginAccount = new EndUserLoginAccount();
		endUserLoginAccount.setLoginAccount("13922990915");
		
		endUserLoginAccount.setAccountType(LoginAccountType.USERNAME);
		
		endUserLoginAccount.setUserInfo(endUserInfo_1);
		
		
		EndUserLoginAccount response_1 = endUserLoginAccountBizService.registeEndUserLoginAccount(endUserLoginAccount);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
	
	
	
	
	
	@Test
	public void removeEndUserLoginAccount() throws Exception {
		
		SimpleResponse response_1 = endUserLoginAccountBizService.removeEndUserLoginAccount("13922990915");
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
	
	
//	@Test
	public void retrieveEndUserInfo() throws Exception {
		
		EndUserInfo response_1 = endUserLoginAccountBizService.retrieveEndUserInfo("544A1FB8166D4E979E32DD87E346544D","13922990915");
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
	
//	@Test
	public void retrieveEndUserLoginAccountInfo() throws Exception {
		
		EndUserLoginAccount response_1 = endUserLoginAccountBizService.retrieveEndUserLoginAccountInfo("13922990914");
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
		
	}
	
//	@Test
	public void retrieveEndUserLoginAccountInfo2() throws Exception {
		
		EndUserLoginAccount response_1 = endUserLoginAccountBizService.retrieveEndUserLoginAccountInfo("544A1FB8166D4E979E32DD87E346544D","13922990914");
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
	}
	
	
	
//	@Test
	public void retrieveEndUserAccountPassword() throws Exception {
		
		EndUserLoginAccount response_1 = endUserLoginAccountBizService.retrieveEndUserAccountPassword("13922990914",StorageRSADecrypt);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
	}
	
	
	
//	@Test
	public void retrieveEndUserAccountPassword2() throws Exception {
		
		EndUserLoginAccount response_1 = endUserLoginAccountBizService.retrieveEndUserAccountPassword("544A1FB8166D4E979E32DD87E346544D","13922990914",StorageRSADecrypt);
		System.out.println(EEBeanUtils.object2Json(response_1));
		
		if ( response_1!=null)
			Assert.assertTrue(response_1.isSuccessful());
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
	private EndUserLoginAccountBizService endUserLoginAccountBizService = (EndUserLoginAccountBizService)super.getContext().getBean("EndUserLoginAccountBizService");

}
