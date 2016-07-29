package com.eenet.test;

import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class EndUserCredentialReSetTester {
	ApiURL baseURL = new ApiURL("test");
	/* 定义调用地址和调用参数 */
	private final String sendSMSCode4ResetPasswordURL = baseURL.getSecurityApiURL()+"/security/sendSMSCode4ResetPassword";
	private final String resetPasswordBySMSCodeWithLoginURL = baseURL.getSecurityApiURL()+"/security/resetPasswordBySMSCodeWithLogin";
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	private final String appId = "9CFF0CA0D43D4B2DAC1EFC6A86FCB191";
	private final String appDomain = "http://hz.saas.workeredu.com";
	private final String appSecretKey = "pASS41#";
	private final long mobile = 13922202252l;
	
	public static void main(String[] args) throws Exception {
		EndUserCredentialReSetTester tester = new EndUserCredentialReSetTester();
//		tester.resetPasswordBySMS();
		
		String userId = "9BC7BA2AEF584220BBC2845BF61A04B9";//<==要重置密码用户的标识，从resetPasswordBySMS()方法获得
		String smsCode = "343827";//<==收到短信后填于此处
		String newPassword = new Random().nextInt(1000000) +"^AAb";
		System.out.println("设置新密码： "+newPassword);
		tester.resetPasswordBySMSCodeWithLogin(userId, smsCode, newPassword);
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

	public EndUserCredentialReSetTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}
}
