package com.eenet.test;

import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class RegistNewUserTester {
	ApiURL baseURL = new ApiURL("runtime");
	/* 定义调用地址和调用参数 */
	private final String registEndUserWithLoginURL = baseURL.getSecurityApiURL()+"/registEndUserWithLogin";
	
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	private final String appId = "5215065683F6418AA8202ED24C0D25C0";
	private final String appDomain = "http://www.gdzgjy.com";
	private final String appSecretKey = "pASS3#";
	
	public static void main(String[] args) throws Exception {
		RegistNewUserTester me = new RegistNewUserTester();
		me.registEndUserWithLogin();
	}
	
	public void registEndUserWithLogin() throws Exception {
		String mobile = "1300000"+String.valueOf(new Random().nextInt(1000));
		String password = "abc123";
		System.out.println("手机："+mobile+",登录密码："+password);
		
		method = new PostMethod(registEndUserWithLoginURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()) );
		method.addParameter("userType", "anonymous");
		
		/* 业务参数 */
		method.addParameter("user.name", "Orion");
		method.addParameter("user.sex", "O");
		method.addParameter("user.mobile", mobile);
		method.addParameter("user.mobileChecked", "true");
		method.addParameter("account.loginAccount", mobile);
		method.addParameter("account.accountType", "MOBILE");
		method.addParameter("credential.password", MockHttpRequest.encrypt(password+"##"+System.currentTimeMillis()));
		/* 备注为测试数据 */
		method.addParameter("user.dataDescription", "测试数据");
		method.addParameter("account.dataDescription", "测试数据");
		method.addParameter("credential.dataDescription", "测试数据");
		
		client.executeMethod(method);
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}

	public RegistNewUserTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}
}
