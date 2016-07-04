package com.eenet.test;

import org.junit.Test;

import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.LoginAccountType;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.user.EndUserInfo;
import com.eenet.user.EndUserInfoBizService;

public class EndUserLoginAccountTester extends SpringEnvironment {
	@Test
	public void crud(){
		System.out.println("==========================="+this.getClass().getName()+".crud()===========================");
		EndUserLoginAccountBizService accountService = (EndUserLoginAccountBizService)super.getContext().getBean("EndUserLoginAccountBizImpl");
		EndUserInfoBizService userService = (EndUserInfoBizService)super.getContext().getBean("EndUserInfoBizImpl");
		
		EndUserInfo user = new EndUserInfo();
		user.setName("Orion");
		user.setMobileChecked(true);
		user = userService.save(user);
		
		EndUserLoginAccount account = new EndUserLoginAccount();
		account.setUserInfo(user);
		account.setAccountType(LoginAccountType.MOBILE);
		account.setLoginAccount("13900139000");
		account = accountService.registeEndUserLoginAccount(account);
		
		if (!account.isSuccessful()) {
			System.out.println(account.getStrMessage());
			return;
		}
		System.out.println("atid: "+account.getAtid());
		
		EndUserInfo retrieveEndUserInfo = accountService.retrieveEndUserInfo(account.getLoginAccount());
		if (!retrieveEndUserInfo.isSuccessful()) {
			System.out.println(retrieveEndUserInfo.getStrMessage());
			return;
		}
		System.out.println("userId: "+retrieveEndUserInfo.getAtid()+", user name: "+retrieveEndUserInfo.getName());
		
		SimpleResponse removeEndUserLoginAccount = accountService.removeEndUserLoginAccount(account.getLoginAccount());
		if (!removeEndUserLoginAccount.isSuccessful()) {
			System.out.println(removeEndUserLoginAccount.getStrMessage());
			return;
		}
		userService.delete(user.getAtid());
	}
}
