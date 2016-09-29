package com.eenet.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class UserSignOn {
	private String getAdminSignOnGrantURL;
	private String getAdminAccessTokenURL;
	
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	/**
	 * @return key:accessToken, refreshToken, atid
	 * @throws Exception
	 * 2016年9月24日
	 * @author Orion
	 */
	public Map<String, String> adminSignOn(HttpClient client, String appId, String appSecretKey, String appDomain, String loginAccount, String adminPassword) throws Exception {
		Map<String,String> result = new HashMap<String,String>();
		/* 获得授权码 */
		method = new PostMethod(getAdminSignOnGrantURL);
		method.addParameter("appId", appId);
		method.addParameter("redirectURI", appDomain);
		method.addParameter("loginAccount", loginAccount);
		method.addParameter("password", MockHttpRequest.encrypt(adminPassword+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println(getAdminSignOnGrantURL+": " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		String grantCode = jsonObject.get("grantCode").toString();
		
		/* 获得访问令牌 */
		method = new PostMethod(getAdminAccessTokenURL);
		method.addParameter("appId", appId);
		method.addParameter("grantCode", grantCode);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()));
		client.executeMethod(method);
		
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println(getAdminAccessTokenURL+": " + returnMessage);
		jsonObject = new JSONObject(returnMessage);
		result.put("accessToken", jsonObject.get("accessToken").toString());
		result.put("refreshToken", jsonObject.get("refreshToken").toString());
		result.put("atid", jsonObject.getJSONObject("userInfo").get("atid").toString());
		
		return result;
	}

	public UserSignOn(ApiURL baseURL) {
		this.getAdminSignOnGrantURL = baseURL.getSecurityApiURL()+"/getAdminSignOnGrant";
		this.getAdminAccessTokenURL = baseURL.getSecurityApiURL()+"/getAdminAccessToken";
	}

}
