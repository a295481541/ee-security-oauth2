package com.eenet.test;

import java.util.Random;

import org.junit.Test;

import com.eenet.base.BooleanResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.test.env.SpringEnvironment;

public class EndUserInfoTester extends SpringEnvironment {
	
	@Test
	public void crud(){
		EndUserInfo endUser = new EndUserInfo();
		EndUserInfoBizService service = (EndUserInfoBizService)super.getContext().getBean("EndUserInfoBizService");
		long mobile = new Random(1300000000).nextInt(1000);
		endUser.setName("Orion");
		endUser = service.save(endUser);
		
		if (!endUser.isSuccessful()) {
			System.out.println(endUser.getStrMessage());
			return;
		}
		
		endUser.setMobile(mobile);
		endUser = service.save(endUser);
		if (!endUser.isSuccessful()) {
			System.out.println(endUser.getStrMessage());
			return;
		}
		
		try {
			EndUserInfo getEndUser = service.get(endUser.getAtid());
			if (!getEndUser.isSuccessful()) {
				System.out.println(getEndUser.getStrMessage());
				return;
			}
			
			BooleanResponse check = service.existMobileEmailId(String.valueOf(mobile), null, null);
			if (!check.isSuccessful()) {
				System.out.println(check.getStrMessage());
				return;
			}
			System.out.println("exist mobile : " + check.isResult());
			
			EndUserInfo getByMEI = service.getByMobileEmailId(String.valueOf(mobile), null, null);
			if (!getByMEI.isSuccessful()) {
				System.out.println(getByMEI.getStrMessage());
				return;
			}
			System.out.println("getByMEI name : " + getByMEI.getName());
		} finally {
			SimpleResponse delete = service.delete(endUser.getAtid());
			if (!delete.isSuccessful())
				System.out.println(delete.getStrMessage());
		}
	}
}
