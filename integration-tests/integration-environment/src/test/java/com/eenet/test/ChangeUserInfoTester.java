package com.eenet.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class ChangeUserInfoTester {
	ApiURL baseURL = new ApiURL("test");
	/* 定义调用地址和调用参数 */
	private final String getEndUserSignOnGrantURL = baseURL.getSecurityApiURL()+"/getEndUserSignOnGrant";
	private final String getEndUserAccessTokenURL = baseURL.getSecurityApiURL()+"/getEndUserAccessToken";
	private final String refreshEndUserAccessTokenURL = baseURL.getSecurityApiURL()+"/refreshEndUserAccessToken";
	private final String saveEndUserURL = baseURL.getBaseinfoApiURL()+"/saveEndUser";
	private final String appId = "AC9CCD9AD6194E1CAD8C05FE718DD6C6";
	private final String appSecretKey = "pASS25#";
	private final String appDomain = "http://xlims.gzedu.com";
	
	public static void main(String[] args) throws Exception {
		ChangeUserInfoTester me = new ChangeUserInfoTester();
		//测试机登录用户：mx_test14746122262611 密码：888888
		Map<String,String> loginRS = me.endUserLogin("mx_test14746122262611","888888");
//		loginRS = me.refreshEndUserToken(loginRS.get("refreshToken"), loginRS.get("userId"));
//		me.changeUserInfo(loginRS.get("accessToken"), loginRS.get("userId"));
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
		
		jsonObject = new JSONObject(returnMessage);
		String newAccessToken = jsonObject.get("accessToken").toString();
		String newRefreshToken = jsonObject.get("refreshToken").toString();
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("accessToken", newAccessToken);
		result.put("refreshToken", newRefreshToken);
		result.put("userId", userId);
		return result;
	}
	
	public void changeUserInfo(String accessToken, String userId) throws Exception {
		System.out.println("准备修改用户信息，accessToken: "+accessToken+",userId : "+userId);
		method = new PostMethod(saveEndUserURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", accessToken);
		method.addParameter("userType", "endUser");
		method.addParameter("atid", userId);
		method.addParameter("province", "440000888");//<--此处为修改内容
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
