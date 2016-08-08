package com.eenet.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class EndUserSMSSignOnTester {
	ApiURL baseURL = new ApiURL("test");
	private final String sendSMSCode4LoginURL = baseURL.getSecurityApiURL()+"/authen/EndUserSMSSignOn/sendSMSCode4Login";//发送短信获得快速登录验证码
	private final String getAccessTokenURL = baseURL.getSecurityApiURL()+"/authen/EndUserSMSSignOn/getAccessToken";//获得访问授权码（通过短信验证码）
	private final String getEndUserURL = baseURL.getBaseinfoApiURL()+"/getEndUser";//获得用户基本信息
	/* 定义调用地址和调用参数 */
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	private final String appId = "9CFF0CA0D43D4B2DAC1EFC6A86FCB191";
	private final String appDomain = "http://hz.saas.workeredu.com";
	private final String appSecretKey = "pASS41#";
	private final long mobile = 13922202252l;
	
	public static void main(String[] args) throws Exception {
		EndUserSMSSignOnTester me = new EndUserSMSSignOnTester();
		me.sendSMSCode4Login();
		String smsCode = "638027";//<--此处收到短信后填写
//		Map<String, String> getTokenRS =  me.getAccessToken(smsCode);
//		me.getMySelfInfo(getTokenRS.get("userId"), getTokenRS.get("accessToken"));
	}
	
	public void sendSMSCode4Login() throws Exception {
		method = new PostMethod(sendSMSCode4LoginURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("mobile", String.valueOf(mobile));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}
	
	public Map<String,String> getAccessToken(String smsCode) throws Exception {
		/* 获得访问令牌 */
		method = new PostMethod(getAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("mobile", String.valueOf(mobile));
		method.addParameter("smsCode", smsCode);
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		
		/* 取访问令牌、刷新令牌和用户id */
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
	
	public void getMySelfInfo(String userId,String accessToken) throws Exception {
		method = new PostMethod(getEndUserURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		method.addParameter("userId", userId);
		method.addParameter("userAccessToken", accessToken);
		method.addParameter("userType", "endUser");
		method.addParameter("endUserId", userId);
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}
	
	public EndUserSMSSignOnTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}

}
