package com.eenet.test;

import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;

public class InitEndUserLoginPasswordTester {
	ApiURL baseURL = new ApiURL("test");
	/* 定义调用地址和调用参数 */
	private final String initEndUserLoginPasswordURL = baseURL.getSecurityApiURL()+"/initEndUserLoginPassword";
	
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	
	private final String appId = "4F6C06B5C9BB41A08335D51F5E5CD39C";
	private final String appDomain = "http://mtss.gzedu.com";
	private final String appSecretKey = "MtsSP&38^";
	
	public static void main(String[] args) throws Exception {
		InitEndUserLoginPasswordTester me = new InitEndUserLoginPasswordTester();
		me.executeByAdmin("51F08E0D6F454A01B396FD09A9FC335D","abc123");
	}
	
	public void executeByAdmin(String endUserId, String endUserPassword) throws Exception {
		UserSignOn signOn = new UserSignOn(baseURL);
		Map<String, String> adminSignOn = signOn.adminSignOn(client, appId, appSecretKey, appDomain, "xlims.admin", "oucnet888");
		System.out.println( adminSignOn.get("accessToken") );
		System.out.println( adminSignOn.get("refreshToken") );
		System.out.println( adminSignOn.get("atid") );
		
		/* 基本参数 */
		method = new PostMethod(initEndUserLoginPasswordURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()) );
		method.addParameter("userId", adminSignOn.get("atid"));
		method.addParameter("userAccessToken", adminSignOn.get("accessToken"));
		method.addParameter("userType", "adminUser");
		
		/* 业务参数 */
		method.addParameter("endUser.atid", endUserId);
		method.addParameter("password", MockHttpRequest.encrypt(endUserPassword+"##"+System.currentTimeMillis()) );
		
		client.executeMethod(method);
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}

	public InitEndUserLoginPasswordTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}

}
