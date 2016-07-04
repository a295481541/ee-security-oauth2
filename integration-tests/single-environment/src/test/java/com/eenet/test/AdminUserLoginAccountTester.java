package com.eenet.test;

import org.junit.Test;

import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.AdminUserLoginAccountBizService;
import com.eenet.authen.LoginAccountType;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.AdminUserInfoBizService;

public class AdminUserLoginAccountTester extends SpringEnvironment {
	@Test
	public void crud(){
		System.out.println("==========================="+this.getClass().getName()+".crud()===========================");
		AdminUserLoginAccountBizService accountService = (AdminUserLoginAccountBizService)super.getContext().getBean("AdminUserLoginAccountBizImpl");
		AdminUserInfoBizService adminService = (AdminUserInfoBizService)super.getContext().getBean("AdminUserInfoBizImpl");
		
		AdminUserInfo user = new AdminUserInfo();
		user.setName("Orion");
		user = adminService.save(user);
		
		AdminUserLoginAccount account = new AdminUserLoginAccount();
		account.setUserInfo(user);
		account.setAccountType(LoginAccountType.MOBILE);
		account.setLoginAccount("13800138000");
		account = accountService.registeAdminUserLoginAccount(account);
		
		if (!account.isSuccessful()) {
			System.out.println(account.getStrMessage());
			return;
		}
		System.out.println("atid: "+account.getAtid());
		
		AdminUserInfo retrieveAdminUserInfo = accountService.retrieveAdminUserInfo(account.getLoginAccount());
		if (!retrieveAdminUserInfo.isSuccessful()) {
			System.out.println(retrieveAdminUserInfo.getStrMessage());
			return;
		}
		System.out.println("userId: "+retrieveAdminUserInfo.getAtid()+", user name: "+retrieveAdminUserInfo.getName());
		
		SimpleResponse removeAdminUserLoginAccount = accountService.removeAdminUserLoginAccount(account.getLoginAccount());
		if (!removeAdminUserLoginAccount.isSuccessful()) {
			System.out.println(removeAdminUserLoginAccount.getStrMessage());
			return;
		}
		adminService.delete(user.getAtid());
	}
}
