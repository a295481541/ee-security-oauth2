package com.eenet.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserCredentialBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.user.EndUserInfo;
import com.eenet.user.EndUserInfoBizService;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class EndUserCredentialTester extends SpringEnvironment {
	private final EndUserCredentialBizService credentialService = (EndUserCredentialBizService)super.getContext().getBean("EndUserCredentialBizImpl");
	private final EndUserInfoBizService userService = (EndUserInfoBizService)super.getContext().getBean("EndUserInfoBizImpl");
	private final RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
	private final RSADecrypt StorageRSADecrypt = (RSADecrypt)super.getContext().getBean("StorageRSADecrypt");
	private final EndUserSignOnBizService signService = (EndUserSignOnBizService)super.getContext().getBean("EndUserSignOnBizImpl");
	
//	@Test
	public void crud() throws Exception{
		System.out.println("==========================="+this.getClass().getName()+".crud()===========================");
		
		EndUserInfo user = new EndUserInfo();
		user.setName("Orion");
		user = userService.save(user);
		
		EndUserCredential credential = new EndUserCredential();
		credential.setEndUser(user);
		credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, "myPassword"));
		SimpleResponse initResult = credentialService.initEndUserLoginPassword(credential);
		if (!initResult.isSuccessful()) {
			System.out.println(initResult.getStrMessage());
			return;
		}
		
		EndUserCredential curCredential = new EndUserCredential();
		curCredential.setEndUser(user);
		curCredential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, "myPassword"));
		String newSecretKey = RSAUtil.encrypt(encrypt, "myPassword2");
		SimpleResponse changeResult = credentialService.changeEndUserLoginPassword(curCredential, newSecretKey);
		if (!changeResult.isSuccessful()) {
			System.out.println(changeResult.getStrMessage());
			return;
		}
		
		EndUserCredential retrievePassword = credentialService.retrieveEndUserSecretKey(user.getAtid(), StorageRSADecrypt);
		if (!retrievePassword.isSuccessful()) {
			System.out.println(retrievePassword.getStrMessage());
			return;
		}
		System.out.println("解密结果："+retrievePassword.getPassword());
		
		userService.delete(user.getAtid());
	}
	
	/**
	 * 重置没有设置统一密码的用户
	 * ★该用例不可自动执行（测不出效果）！！！！！！！
	 * ★先执行：delete AUTHEN_ENDUSER_SECRET_KEY where user_id='de904a03ac1082a3608d116e80655ae8'
	 * 2016年6月21日
	 * @author Orion
	 * @throws Exception 
	 */
	@Test
	public void resetPasswordWithoutCredential() throws Exception {
		System.out.println("==========================="+this.getClass().getName()+".resetPasswordWithoutCredential()===========================");
		EndUserCredentialBizService credentialService = (EndUserCredentialBizService)super.getContext().getBean("EndUserCredentialBizImpl");
		String userId = "de904a03ac1082a3608d116e80655ae8";
		
		SimpleResponse resetResult = credentialService.resetEndUserLoginPassword(userId);
		if (!resetResult.isSuccessful()) {
			System.out.println(resetResult.getStrMessage());
		}
		String appId = "432B31FB2F7C4BB19ED06374FB0C1850";
		String appDomain = "http://www.zhigongjiaoyu.com";
		String loginAccount = "gjm2015";
		String password = new SimpleDateFormat("YYYYMMdd").format(new Date());
		
		/* 获得登录授权码 */
		SignOnGrant getSignOnGrant = 
				signService.getSignOnGrant(appId, appDomain, loginAccount, RSAUtil.encryptWithTimeMillis(encrypt, password));
		if (!getSignOnGrant.isSuccessful()){
			System.out.println("getSignOnGrant : \n"+getSignOnGrant.getStrMessage());
			return;
		}
		System.out.println("getSignOnGrant: "+getSignOnGrant.getGrantCode());
		
	}
}
