package com.eenet.test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class PreRegistEndUserTester {
	ApiURL baseURL = new ApiURL("test");
	/* 定义调用地址和调用参数 */
	private final String existMobileEmailIdURL = baseURL.getSecurityApiURL()+"/endUserExistMEID";
	private final String getByMobileEmailIdURL = baseURL.getSecurityApiURL()+"/getEndUserByMEID";
	private final String getAdminSignOnGrantURL = baseURL.getSecurityApiURL()+"/getAdminSignOnGrant";
	private final String getAdminAccessTokenURL = baseURL.getSecurityApiURL()+"/getAdminAccessToken";
	
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	private final String appId = "9CFF0CA0D43D4B2DAC1EFC6A86FCB191";
	private final String appDomain = "http://hz.saas.workeredu.com";
	private final String appSecretKey = "pASS41#";
	
	private final String adminAccount = "superman";
	private final String adminPassword = "sEPp$341";
	private final String adminId = "620368E56754424585A0FD296FBADC55";
	
	public static void main(String[] args) throws Exception{
		long mobile = 13922202252l;
		PreRegistEndUserTester me = new PreRegistEndUserTester();
		if (me.existMobileEmailId(mobile))
			me.getByMobileEmailId(mobile);
	}
	
	public boolean existMobileEmailId(long mobile) throws Exception{
		method = new PostMethod(existMobileEmailIdURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("mobile", String.valueOf(mobile));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		
		return jsonObject.get("successful").toString().equals("true");
	}
	
	public void getByMobileEmailId(long mobile) throws Exception{
		/* 获得登录授权码 */
		method = new PostMethod(getAdminSignOnGrantURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("loginAccount", adminAccount);
		method.addParameter("password", MockHttpRequest.encrypt(adminPassword+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("getAdminSignOnGrant returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String grantCode = jsonObject.get("grantCode").toString();
		
		/* 获得访问令牌 */
		method = new PostMethod(getAdminAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("grantCode", grantCode);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("getAdminAccessToken returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String accessToken = jsonObject.get("accessToken").toString();
		String refreshToken = jsonObject.get("refreshToken").toString();
		
		
		method = new PostMethod(getByMobileEmailIdURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("mobile", String.valueOf(mobile));
		method.addParameter("userType", "adminUser");
		method.addParameter("userId", adminId);
		method.addParameter("userAccessToken", accessToken);
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
	}
	
	public PreRegistEndUserTester(){
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}
}
