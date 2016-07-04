package com.eenet.test;

import org.junit.Test;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessAppType;
import com.eenet.base.SimpleResponse;
import com.eenet.test.env.SpringEnvironment;

public class BusinessAppTester extends SpringEnvironment {
	@Test
	public void crud(){
		System.out.println("==========================="+this.getClass().getName()+".crud()===========================");
		BusinessApp app = new BusinessApp();
		BusinessAppBizService appService = (BusinessAppBizService)super.getContext().getBean("BusinessAppBizImpl");
		app.setAppName("测试系统");
		app.setAppType(BusinessAppType.WEBAPP);
		app.setRedirectURIPrefix("http://www.eenet.com");
		app.setSecretKey("999Aa$");
		app = appService.registeApp(app);
		
		if (!app.isSuccessful()) {
			System.out.println(app.getStrMessage());
			return;
		}
		System.out.println("appid: "+app.getAppId());
		
		BusinessApp retrieveApp = appService.retrieveApp(app.getAppId());
		if (!retrieveApp.isSuccessful()) {
			System.out.println(retrieveApp.getStrMessage());
			return;
		}
		System.out.println("appid: "+retrieveApp.getAppId()+", appName: "+retrieveApp.getAppName());
		
		SimpleResponse removeApp = appService.removeApp(retrieveApp.getAppId());
		if (!removeApp.isSuccessful()) {
			System.out.println(removeApp.getStrMessage());
			return;
		}
	}
}
