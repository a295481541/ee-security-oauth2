package com.eenet.test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.JSONObject;

public class RegisteEndUserLoginAccountTester {
	ApiURL baseURL = new ApiURL("runtime");
	/* 定义调用地址和调用参数 */
	private final String registeEndUserLoginAccountURL = baseURL.getSecurityApiURL()+"/registeEndUserLoginAccount";
	
	private HttpClient client;
	private PostMethod method;
	private String returnMessage;
	private JSONObject jsonObject;
	
	private final String appId = "5215065683F6418AA8202ED24C0D25C0";
	private final String appDomain = "http://www.gdzgjy.com";
	private final String appSecretKey = "pASS3#";
	
	public static void main(String[] args) throws Exception {
		RegisteEndUserLoginAccountTester me = new RegisteEndUserLoginAccountTester();
		me.execute();
	}
	
	public void execute() throws Exception {
		method = new PostMethod(registeEndUserLoginAccountURL);
		method.addParameter("appId", appId);
		method.addParameter("appSecretKey", MockHttpRequest.encrypt(appSecretKey+"##"+System.currentTimeMillis()) );
		method.addParameter("userType", "anonymous");
		
		/* 业务参数 */
		method.addParameter("userInfo.atid", "4ED83EC8D2BD4BA39A1D403885012F1F");
		method.addParameter("loginAccount", "13543421610");
		method.addParameter("accountType", "MOBILE");
		
		client.executeMethod(method);
		returnMessage = EncodingUtil.getString(method.getResponseBody(), "UTF-8");
		System.out.println("returnMessage : " + returnMessage);
	}

	public RegisteEndUserLoginAccountTester() {
		client = new HttpClient();
		client.getParams().setContentCharset("UTF-8");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	}

}
