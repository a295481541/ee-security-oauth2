package eenet.test.security.consumer;

import org.junit.Assert;
import org.junit.Test;

import com.eenet.base.BooleanResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.security.PreRegistEndUserBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;

import eenet.test.security.consumer.env.IdentityInfo;
import eenet.test.security.consumer.env.SpringEnvironment;

public class PreRegistEndUserBizTester extends SpringEnvironment  {
	
	@Test
	public void existAccount() throws Exception { 
		BooleanResponse result = preRegistEndUserBizService.existAccount(appId, seriesId, loginAccount,"13690991009");
		System.out.println(EEBeanUtils.object2Json(result));
		if (result!= null )
			Assert.assertTrue(result.isSuccessful());
		
		result = preRegistEndUserBizService.existAccount(appId, seriesId,"13690991009");
		System.out.println(EEBeanUtils.object2Json(result));
		if (result!= null )
			Assert.assertTrue(result.isSuccessful());
		
		
		result = preRegistEndUserBizService.existAccount(appId, seriesId,"13690991000");
		System.out.println(EEBeanUtils.object2Json(result));
		if (result!= null )
			Assert.assertFalse(result.isSuccessful());
		
		
		
		
		
	}
	
//	@Test
	public void retrieveEndUserInfo() throws Exception {
		EndUserInfo endUserInfo =  preRegistEndUserBizService.retrieveEndUserInfo(appId, seriesId, loginAccount);
		
		System.out.println(EEBeanUtils.object2Json(endUserInfo));
		
		if (endUserInfo!= null )
			Assert.assertTrue(endUserInfo.isSuccessful());
		
		
		endUserInfo =  preRegistEndUserBizService.retrieveEndUserInfo(appId, seriesId, loginAccount+"AA");
		
		System.out.println(EEBeanUtils.object2Json(endUserInfo));
		
		if (endUserInfo!= null )
			Assert.assertFalse(endUserInfo.isSuccessful());
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
	private PreRegistEndUserBizService preRegistEndUserBizService = (PreRegistEndUserBizService)super.getContext().getBean("PreRegistEndUserBizService");
	

}
