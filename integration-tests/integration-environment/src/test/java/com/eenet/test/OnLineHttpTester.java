package com.eenet.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class OnLineHttpTester {
	private String baseURL = "http://security-api.open.gzedu.com";
	/* 定义调用地址和调用参数 */
	private String getAdminSignOnGrantURL = baseURL+"/getAdminSignOnGrant";
	private String getAdminAccessTokenURL = baseURL+"/getAdminAccessToken";
	private String saveAdminUserURL = baseURL+"/saveAdminUser";
	private String registeAdminUserLoginAccountURL = baseURL+"/registeAdminUserLoginAccount";
	private String initAdminUserLoginPasswordURL = baseURL+"/initAdminUserLoginPassword";
	private String getEndUserURL = baseURL+"/getEndUser";
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	private String appId = "AC9CCD9AD6194E1CAD8C05FE718DD6C6";
	private String appSecretKey = "pASS25#";
	private String appDomain = "http://xlims.gzedu.com";
	private String loginAccount = "superman";
	private String password = "sEPp$341";
	
	public static void main(String[] args) throws Exception {
		OnLineHttpTester me = new OnLineHttpTester();
//		me.createAdminUser();
//		me.adminUserLogin("xlims.admin","oucnet888");
		me.getEndUserInfoByAdmin("xlims.admin","oucnet888", "9fee01daac1082a32cd90727c975a688");
	}
	
	public void createAdminUser() throws Exception {
		/* 获得登录授权码 */
		method = new PostMethod(getAdminSignOnGrantURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("loginAccount", loginAccount);
		method.addParameter("password", MockHttpRequest.encrypt(password+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String grantCode = jsonObject.get("grantCode").toString();
		System.out.println("grantCode : " + grantCode);
		
		/* 获得访问令牌 */
		method = new PostMethod(getAdminAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("grantCode", grantCode);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String accessToken = jsonObject.get("accessToken").toString();
		String refreshToken = jsonObject.get("refreshToken").toString();
		
		String userInfoJson = jsonObject.get("userInfo").toString();
		JSONObject userInfoJsonObj = new JSONObject(userInfoJson);
		String userId = userInfoJsonObj.getString("atid");

		System.out.println("accessToken : " + accessToken + ", refreshToken : " + refreshToken + " user atid: "+userId);
		
		/* 创建管理员 */
		method = new PostMethod(saveAdminUserURL);
		method.addParameter("userType", "adminUser");
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", accessToken);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()) );
		method.addParameter("name", "国开学历教学教务管理平台超级管理员");
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String savedAdminId = jsonObject.get("atid").toString();
		System.out.println("savedAdminId : " + savedAdminId);
		
		/* 注册服务人员登录账号 */
		method = new PostMethod(registeAdminUserLoginAccountURL);
		method.addParameter("userType", "adminUser");
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", accessToken);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()) );
		method.addParameter("userInfo.atid",savedAdminId);
		method.addParameter("loginAccount","xlims.admin");
		method.addParameter("accountType","USERNAME");
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		
		/* 初始化服务人员密码 */
		method = new PostMethod(initAdminUserLoginPasswordURL);
		method.addParameter("userType", "adminUser");
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", accessToken);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()) );
		method.addParameter("adminUser.atid",savedAdminId);
		method.addParameter("password",MockHttpRequest.encrypt("oucnet888##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}
	
	/**
	 * 管理员登录
	 * @param loginAccount 管理员登录账号
	 * @param password 管理员密码
	 * @return map: userId,accessToken,refreshToken
	 * @throws Exception
	 * 2016年7月1日
	 * @author Orion
	 */
	public Map<String,String> adminUserLogin(String loginAccount, String password) throws Exception {
		/* 获得登录授权码 */
		method = new PostMethod(getAdminSignOnGrantURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("loginAccount", loginAccount);
		method.addParameter("password", MockHttpRequest.encrypt(password+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String grantCode = jsonObject.get("grantCode").toString();
		System.out.println("grantCode : " + grantCode);
		
		/* 获得访问令牌 */
		method = new PostMethod(getAdminAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("grantCode", grantCode);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String accessToken = jsonObject.get("accessToken").toString();
		String refreshToken = jsonObject.get("refreshToken").toString();
		
		String userInfoJson = jsonObject.get("userInfo").toString();
		JSONObject userInfoJsonObj = new JSONObject(userInfoJson);
		String userId = userInfoJsonObj.getString("atid");

		System.out.println("accessToken : " + accessToken + ", refreshToken : " + refreshToken + " user atid: "+userId);
		
		Map<String,String> result = new HashMap<String, String>();
		if ( userInfoJsonObj.getBoolean("successful") ) {
			result.put("userId", userId);
			result.put("accessToken", accessToken);
			result.put("refreshToken", refreshToken);
		} else {
			throw new Exception("管理员登录失败");
		}
		return result;
	}
	
	public void getEndUserInfoByAdmin(String loginAccount, String password, String getEndUserId) throws Exception {
		/* 获得当前登录者身份 */
		Map<String,String> currentAdminUser = this.adminUserLogin(loginAccount, password);
		
		method = new PostMethod(getEndUserURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("userId", currentAdminUser.get("userId"));
		method.addParameter("userAccessToken", currentAdminUser.get("accessToken"));
		method.addParameter("getEndUserId", getEndUserId);
		method.addParameter("userType", "adminUser");
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}
	
	public OnLineHttpTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
//		String returnMessage = "{\"accessToken\":\"B8B2CF4523EE40989B06144A0C1555A3\",\"refreshToken\":\"ABCB487AEB814C5E87737BEF3CB0F4B6\",\"userInfo\":{\"name\":\"超级管理员\",\"realnameChecked\":false,\"mobileChecked\":false,\"emailChecked\":false,\"atid\":\"F88F85AA767C4AA2BC166AC6EDC0E062\",\"crdt\":\"ISODate(2016-06-17 17:20:07)\",\"crps\":\"用户未知\",\"crss\":\"来源系统未知\",\"mddt\":\"ISODate(2016-06-17 17:20:07)\",\"mdps\":\"NEVER_CHANGE\",\"mdss\":\"NEVER_CHANGE\",\"successful\":true,\"messages\":[]},\"successful\":true,\"messages\":[]}";
//		jsonObject = new JSONObject(returnMessage);
//		System.out.println(new JSONObject(jsonObject.get("userInfo").toString()).get("atid"));
	}
}
