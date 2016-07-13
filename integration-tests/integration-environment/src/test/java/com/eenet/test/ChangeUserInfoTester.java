package com.eenet.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class ChangeUserInfoTester {
	private String baseURL = "http://172.16.146.152:8080/security-api";
	/* 定义调用地址和调用参数 */
	private String getEndUserSignOnGrantURL = baseURL+"/getEndUserSignOnGrant";
	private String getEndUserAccessTokenURL = baseURL+"/getEndUserAccessToken";
	private String refreshEndUserAccessTokenURL = baseURL+"/refreshEndUserAccessToken";
	private String saveEndUserURL = baseURL+"/saveEndUser";
	private String appId = "FAC6D2625780432A9C197EF3E2337F08";
	private String appSecretKey = "pASS12#";
	private String appDomain = "http://hz.zhigongjiaoyu.com";
	
	public static void main(String[] args) throws Exception {
		ChangeUserInfoTester me = new ChangeUserInfoTester();
		Map<String,String> loginRS = me.endUserLogin("13071220990","123456");
		me.refreshEndUserToken(loginRS.get("refreshToken"), loginRS.get("userId"));
//		me.changeUserInfo(loginRS.get("accessToken"), loginRS.get("refreshToken"), loginRS.get("userId"));
	}
	
	public Map<String,String> endUserLogin(String loginAccount, String password) throws Exception {
		/* 获得登录授权码 */
		method = new PostMethod(getEndUserSignOnGrantURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("loginAccount", loginAccount);
		method.addParameter("password", MockHttpRequest.encrypt(password+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("获得登录授权码 : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String grantCode = jsonObject.get("grantCode").toString();
		
		/* 获得访问令牌 */
		method = new PostMethod(getEndUserAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("grantCode", grantCode);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("获得访问令牌 : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String accessToken = jsonObject.get("accessToken").toString();
		String refreshToken = jsonObject.get("refreshToken").toString();
		
		String userInfoJson = jsonObject.get("userInfo").toString();
		JSONObject userInfoJsonObj = new JSONObject(userInfoJson);
		String userId = userInfoJsonObj.getString("atid");
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("accessToken", accessToken);
		result.put("refreshToken", refreshToken);
		result.put("userId", userId);
		return result;
	}
	
	public Map<String,String> refreshEndUserToken(String refreshToken, String userId) throws Exception {
		method = new PostMethod(refreshEndUserAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("refreshToken", refreshToken);
		method.addParameter("endUserId", userId);
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("刷新令牌 : " + returnMessage);
		return null;
	}
	
	public void changeUserInfo(String accessToken, String refreshToken, String userId) throws Exception {
		method = new PostMethod(saveEndUserURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", accessToken);
		method.addParameter("userType", "endUser");
		method.addParameter("atid", userId);
		method.addParameter("province", "440000000");
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("修改个人信息 : " + returnMessage);
	}
	
	/* 公用参数 */
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	public ChangeUserInfoTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}
	
}
