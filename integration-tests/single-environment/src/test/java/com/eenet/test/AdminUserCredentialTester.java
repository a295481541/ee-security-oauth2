package com.eenet.test;

import org.junit.Test;

import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserCredentialBizService;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.AdminUserInfo;
import com.eenet.baseinfo.user.AdminUserInfoBizService;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class AdminUserCredentialTester extends SpringEnvironment {
	@Test
	public void crud() throws Exception{
		System.out.println("==========================="+this.getClass().getName()+".crud()===========================");
		AdminUserCredentialBizService credentialService = (AdminUserCredentialBizService)super.getContext().getBean("AdminUserCredentialBizImpl");
		AdminUserInfoBizService adminService = (AdminUserInfoBizService)super.getContext().getBean("AdminUserInfoBizService");
		RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
		RSADecrypt StorageRSADecrypt = (RSADecrypt)super.getContext().getBean("StorageRSADecrypt");
		
		AdminUserInfo user = new AdminUserInfo();
		user.setName("Orion");
		user = adminService.save(user);
		
		AdminUserCredential credential = new AdminUserCredential();
		credential.setAdminUser(user);
		credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, "myPassword"));
		SimpleResponse initResult = credentialService.initAdminUserLoginPassword(credential);
		if (!initResult.isSuccessful()) {
			System.out.println(initResult.getStrMessage());
			return;
		}
		
		AdminUserCredential curCredential = new AdminUserCredential();
		curCredential.setAdminUser(user);
		curCredential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, "myPassword"));
		String newSecretKey = RSAUtil.encrypt(encrypt, "myPassword2");
		SimpleResponse changeResult = credentialService.changeAdminUserLoginPassword(curCredential, newSecretKey);
		if (!changeResult.isSuccessful()) {
			System.out.println(changeResult.getStrMessage());
			return;
		}
		
		AdminUserCredential retrievePassword = credentialService.retrieveAdminUserSecretKey(user.getAtid(), StorageRSADecrypt);
		if (!retrievePassword.isSuccessful()) {
			System.out.println(retrievePassword.getStrMessage());
			return;
		}
		System.out.println("解密结果："+retrievePassword.getPassword());
		
		adminService.delete(user.getAtid());
	}
}
