package com.eenet.test;

import org.junit.Test;

import com.eenet.test.env.SpringEnvironment;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.AdminUserInfoBizService;

public class AdminUserInfoTester extends SpringEnvironment {
	
	@Test
	public void crud(){
		AdminUserInfo admin = new AdminUserInfo();
		AdminUserInfoBizService service = (AdminUserInfoBizService)super.getContext().getBean("AdminUserInfoBizImpl");
		admin.setName("Orion");
		admin = service.save(admin);
		
		if (!admin.isSuccessful()) {
			System.out.println(admin.getStrMessage());
			return;
		}
		
		admin.setMobile(13800138000l);
		admin = service.save(admin);
		if (!admin.isSuccessful()) {
			System.out.println(admin.getStrMessage());
		}
	}
}
