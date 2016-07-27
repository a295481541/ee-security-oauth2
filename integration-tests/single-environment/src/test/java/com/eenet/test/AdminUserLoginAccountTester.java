package com.eenet.test;

import org.junit.Test;

import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.AdminUserLoginAccountBizService;
import com.eenet.authen.LoginAccountType;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.AdminUserInfo;
import com.eenet.baseinfo.user.AdminUserInfoBizService;
import com.eenet.test.env.SpringEnvironment;

public class AdminUserLoginAccountTester extends SpringEnvironment {
	@Test
	public void crud(){
		System.out.println("==========================="+this.getClass().getName()+".crud()===========================");
		AdminUserLoginAccountBizService accountService = (AdminUserLoginAccountBizService)super.getContext().getBean("AdminUserLoginAccountBizImpl");
		AdminUserInfoBizService adminService = (AdminUserInfoBizService)super.getContext().getBean("AdminUserInfoBizService");
		
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
		
		/* 再注册一个相同 */
		AdminUserLoginAccount account2 = new AdminUserLoginAccount();
		account2.setUserInfo(user);
		account2.setAccountType(LoginAccountType.MOBILE);
		account2.setLoginAccount("13800138000");
		account2 = accountService.registeAdminUserLoginAccount(account);
		if (!account2.isSuccessful()) {
			System.out.println(account2.getStrMessage());
			return;
		}
		System.out.println("atid: "+account2.getAtid() + "bizCode: ["+account2.getRSBizCode().toString()+"]");
		
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
