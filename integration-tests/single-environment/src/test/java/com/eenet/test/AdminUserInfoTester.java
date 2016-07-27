package com.eenet.test;

import org.junit.Test;

import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.AdminUserInfo;
import com.eenet.baseinfo.user.AdminUserInfoBizService;
import com.eenet.test.env.SpringEnvironment;

public class AdminUserInfoTester extends SpringEnvironment {
	
	@Test
	public void crud(){
		AdminUserInfo admin = new AdminUserInfo();
		AdminUserInfoBizService service = (AdminUserInfoBizService)super.getContext().getBean("AdminUserInfoBizService");
		admin.setName("Orion");
		
		try {
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
		} finally {
			SimpleResponse delete = service.delete(admin.getAtid());
			if (!delete.isSuccessful())
				System.out.println(delete.getStrMessage());
		}
		
	}
}
