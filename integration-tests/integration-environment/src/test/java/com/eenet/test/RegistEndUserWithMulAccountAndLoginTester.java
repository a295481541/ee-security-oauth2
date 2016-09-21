package com.eenet.test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class RegistEndUserWithMulAccountAndLoginTester {
	ApiURL baseURL = new ApiURL("local");
	/* 定义调用地址和调用参数 */
	private final String registEndUserWithMulAccountAndLoginURL = baseURL.getSecurityApiURL()+"/regist/endUserWithMulAccountAndLogin";
	
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	private final String appId = "4F6C06B5C9BB41A08335D51F5E5CD39C";
	private final String appDomain = "http://mtss.gzedu.com";
	private final String appSecretKey = "MtsSP&38^";
	
	public static void main(String[] args) throws Exception {
		RegistEndUserWithMulAccountAndLoginTester me = new RegistEndUserWithMulAccountAndLoginTester();
		me.execute();
	}
	
	public void execute() throws Exception {
		StringBuffer loginAccounts = new StringBuffer();
		String password = "abc123";
		
		/* 基本参数 */
		method = new PostMethod(registEndUserWithMulAccountAndLoginURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()) );
		method.addParameter("userType", "anonymous");
		
		/* 业务参数 - 人员信息 */
		method.addParameter("user.name", "Orion");
		method.addParameter("user.sex", "O");
		method.addParameter("user.dataDescription", "测试数据");
		
		/* 业务参数 - 账号信息 */
		for (int i=0;i<3;i++){
			String loginAccount = "Orion-"+System.currentTimeMillis();
			method.addParameter("account.m["+i+"].loginAccount", loginAccount);
			method.addParameter("account.m["+i+"].accountType", "USERNAME");
			method.addParameter("account.m["+i+"].dataDescription", "测试数据");
			loginAccounts.append("登录账号").append(i).append(": ").append(loginAccount).append(";\n");
			Thread.sleep(3l);
		}
		System.out.println(loginAccounts.toString());
		
		/* 业务参数 - 密码信息 */
		method.addParameter("credential.password", MockHttpRequest.encrypt(password+"##"+System.currentTimeMillis()));
		method.addParameter("credential.dataDescription", "测试数据");
		
		client.executeMethod(method);
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}

	public RegistEndUserWithMulAccountAndLoginTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}

}
