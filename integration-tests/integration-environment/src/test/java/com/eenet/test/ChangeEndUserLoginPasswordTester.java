package com.eenet.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.log4j.net.SyslogAppender;
import org.json.JSONObject;

public class ChangeEndUserLoginPasswordTester {
	private static ApiURL baseURL = new ApiURL("test");
	
	/* 定义调用地址和调用参数 */
	private final String getEndUserSignOnGrantURL = baseURL.getSecurityApiURL() +"/getEndUserSignOnGrant";
	private final String getEndUserAccessTokenURL = baseURL.getSecurityApiURL() +"/getEndUserAccessToken";
	private final String changeEndUserLoginPassword = baseURL.getSecurityApiURL() +"/changeEndUserLoginPassword";
	private final String changeEndUserLoginPasswordNew =  baseURL.getSecurityApiURL() +"/changeEndUserLoginPasswordNew";
	private final String appId = "AC9CCD9AD6194E1CAD8C05FE718DD6C6";
	private final String appSecretKey = "pASS25#";
	private final String appDomain = "http://xlims.gzedu.com";
	
	public static void main(String[] args) throws Exception {
		
		
		
		ChangeEndUserLoginPasswordTester me = new ChangeEndUserLoginPasswordTester();
		
		String loginAccount =  "43062419900818361X";
		String password = "zhongan888";
		String newPassword = "888888";
		
		Map<String,String> loginRS = me.endUserLogin(loginAccount,password);
		
		
		String accessToken =  loginRS.get("accessToken").toString();
		String userId =  loginRS.get("userId").toString();
		String endUser_atid =  loginRS.get("userId").toString();
		String newSecretKey =  MockHttpRequest.encrypt(newPassword);
		
		
		
//		me.changeEndUserLoginPassword(userId, accessToken, endUser_atid, password, newSecretKey);
//		me.changeEndUserLoginPasswordNew(userId, accessToken, endUser_atid, password, loginAccount, newSecretKey);
		
	}
	
	public Map<String,String> endUserLogin(String loginAccount, String password) throws Exception {
		
		
		System.out.println(getEndUserSignOnGrantURL);
		
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
		
		String userInfoJson = jsonObject.get("userInfo").toString();
		JSONObject userInfoJsonObj = new JSONObject(userInfoJson);
		String userId = userInfoJsonObj.getString("atid");
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("accessToken", accessToken);
		result.put("userId", userId);
		return result;
	}
	
	
	
	
	public  void changeEndUserLoginPassword(String userId, String userAccessToken,String endUser_atid, String password,String newSecretKey) throws Exception {
		/* 获得登录授权码 */
		method = new PostMethod(changeEndUserLoginPassword);
		
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("userType", "endUser");
		
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", userAccessToken);
		method.addParameter("endUser.atid", endUser_atid);
		method.addParameter("password", password);
		method.addParameter("newSecretKey", newSecretKey);
		client.executeMethod(method);
		
		NameValuePair[] val = method.getParameters();
		for (int i = 0; i < val.length; i++) {
			System.out.println(val[i].getName() +"   : " +val[i].getValue());
		}
		
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("changeEndUserLoginPassword : 设置结果：" + returnMessage);
	}
	
	
	
	public  void changeEndUserLoginPasswordNew(String userId, String userAccessToken,String endUser_atid, String password,String loginAccount,String newSecretKey) throws Exception {
		/* 获得登录授权码 */
		method = new PostMethod(changeEndUserLoginPasswordNew);
		
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("userType", "endUser");
		
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", userAccessToken);
		method.addParameter("endUser.atid", endUser_atid);
		method.addParameter("password", MockHttpRequest.encrypt(password+"##"+System.currentTimeMillis()));
		method.addParameter("loginAccount", loginAccount);
		method.addParameter("newSecretKey", newSecretKey);
		
		NameValuePair[] val = method.getParameters();
		for (int i = 0; i < val.length; i++) {
			System.out.println(val[i].getName() +"   : " +val[i].getValue());
		}
		
		
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("changeEndUserLoginPasswordNew : 设置结果：" + returnMessage);
	}
	
	
	
	
	
	
	
	
	/* 公用参数 */
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	public ChangeEndUserLoginPasswordTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}
	
}
