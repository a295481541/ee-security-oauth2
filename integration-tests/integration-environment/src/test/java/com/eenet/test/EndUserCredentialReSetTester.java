package com.eenet.test;

import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class EndUserCredentialReSetTester {
//	public static String baseURL = "http://172.16.165.223:8080/security-api";
	public static String baseURL = "http://172.16.146.152:8080/security-api";
	
	private String sendSMSCode4ResetPasswordURL = baseURL+"/security/sendSMSCode4ResetPassword";
	private String resetPasswordBySMSCodeWithLoginURL = baseURL+"/security/resetPasswordBySMSCodeWithLogin";
	private String resetPasswordBySMSCodeWithoutLoginURL = baseURL+"/security/resetPasswordBySMSCodeWithoutLogin";
	private String getEndUserSignOnGrantURL = baseURL+"/getEndUserSignOnGrant";
	private String getEndUserAccessTokenURL = baseURL+"/getEndUserAccessToken";
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	private final String appId = "9CFF0CA0D43D4B2DAC1EFC6A86FCB191";
	private final String appDomain = "http://hz.saas.workeredu.com";
	private final String appSecretKey = "pASS41#";
	private final long mobile = 13922202252l;
	private final String loginAccount = "gz2016001";//<==根据重置用户的不同修改
	
	public static void main(String[] args) throws Exception {
		EndUserCredentialReSetTester tester = new EndUserCredentialReSetTester();
//		tester.resetPasswordBySMS();
		
		String userId = "9BC7BA2AEF584220BBC2845BF61A04B9";//<==要重置密码用户的标识，从resetPasswordBySMS()方法获得
		String smsCode = "860039";//<==收到短信后填于此处
		String newPassword = new Random().nextInt(1000000) +"^AAb";
		System.out.println("设置新密码： "+newPassword);
//		tester.resetPasswordBySMSCodeWithLogin(userId, smsCode, newPassword);
		tester.resetPasswordBySMSCodeWithoutLogin(userId, smsCode, newPassword);
	}
	
	public void resetPasswordBySMS() throws Exception {
		/* 发送短信验证码 */
		method = new PostMethod(sendSMSCode4ResetPasswordURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("mobile", String.valueOf(mobile));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String endUserId = jsonObject.get("result").toString();
		System.out.println("endUserId : " + endUserId);
	}
	
	public void resetPasswordBySMSCodeWithLogin(String userId, String smsCode, String newPassword) throws Exception {
		method = new PostMethod(resetPasswordBySMSCodeWithLoginURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("endUser.atid", userId);
		method.addParameter("password", MockHttpRequest.encrypt(newPassword+"##"+System.currentTimeMillis()));
		method.addParameter("smsCode", smsCode);
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}
	
	public void resetPasswordBySMSCodeWithoutLogin(String userId, String smsCode, String newPassword) throws Exception {
		method = new PostMethod(resetPasswordBySMSCodeWithoutLoginURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("endUser.atid", userId);
		method.addParameter("password", MockHttpRequest.encrypt(newPassword+"##"+System.currentTimeMillis()));
		method.addParameter("smsCode", smsCode);
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		
		/* 获得登录授权码 */
		method = new PostMethod(getEndUserSignOnGrantURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("loginAccount", loginAccount);
		method.addParameter("password", MockHttpRequest.encrypt(newPassword+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String grantCode = jsonObject.get("grantCode").toString();
		System.out.println("grantCode : " + grantCode);
		
		/* 获得访问令牌 */
		method = new PostMethod(getEndUserAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("grantCode", grantCode);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}

	public EndUserCredentialReSetTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}
}
