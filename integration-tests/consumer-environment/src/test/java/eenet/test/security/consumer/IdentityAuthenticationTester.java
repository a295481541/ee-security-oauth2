package eenet.test.security.consumer;

import org.junit.Assert;
import org.junit.Test;

import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.response.AppAuthenResponse;
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
	
	@Test
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
	
	
	
	
	
	
	private final String appId = IdentityInfo.seriesedAppId;
	private final String seriesId = IdentityInfo.seriesId;
	private final String redirectURI = IdentityInfo.seriesedRedirectURI;
	private final String appSecret = IdentityInfo.seriesedAppSecret;
	private final String loginAccount = IdentityInfo.userLoginAccount;
	private final String password = IdentityInfo.userPassword;
	private final String userId = IdentityInfo.userId;
	private  RSAEncrypt transferRSAEncrypt = (RSAEncrypt) super.getContext().getBean("transferRSAEncrypt");
	private  RSADecrypt StorageRSADecrypt = (RSADecrypt) super.getContext().getBean("StorageRSADecrypt");
	private IdentityAuthenticationBizService identityAuthenticationBizService = (IdentityAuthenticationBizService)super.getContext().getBean("IdentityAuthenticationBizService");


}
