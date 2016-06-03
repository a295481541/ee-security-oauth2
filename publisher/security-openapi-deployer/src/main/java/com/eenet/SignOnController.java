package com.eenet;

/**
 * 登录令牌交互入口
 */
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.authen.SignOnGrant;
import com.eenet.base.SimpleResponse;
import com.eenet.util.EEBeanUtils;

@Controller
public class SignOnController {
	
	@RequestMapping(value = "/getSignOnGrant", method = RequestMethod.GET)
	@ResponseBody
	public String getSignOnGrant(String appId, String redirectURI, String endUserLoginAccount,String endUserPassword) {
		SignOnGrant grant = new SignOnGrant();
		grant.setSuccessful(true);
		grant.setGrantCode("mockGrantCode");
		return EEBeanUtils.object2Json(grant);
	}
	
	@RequestMapping(value = "/getAdminSignOnGrant", method = RequestMethod.GET)
	@ResponseBody
	public String getAdminSignOnGrant(String appId, String redirectURI, String adminUserLoginAccount,String adminUserPassword) {
		return null;
	}
	
	@RequestMapping(value = "/getAccessToken", method = RequestMethod.GET)
	@ResponseBody
	public String getAccessToken(String appId, String appSecretKey, String grantCode) {
		AccessToken accessToken = new AccessToken();
		accessToken.setSuccessful(true);
		accessToken.setAccessToken("mockAccessToken");
		accessToken.setRefreshToken("mockRefreshToken");
		accessToken.setPersonId("mockPersonId");
		return EEBeanUtils.object2Json(accessToken);
	}
	
	@RequestMapping(value = "/getAdminAccessToken", method = RequestMethod.GET)
	@ResponseBody
	public String getAdminAccessToken(String appId, String appSecretKey, String grantCode) {
		return null;
	}
	
	@RequestMapping(value = "/getKeySuffix")
	@ResponseBody
	public String getKeySuffix() {
		return String.valueOf(System.currentTimeMillis());
	}
}

class AccessToken extends SimpleResponse {
	private static final long serialVersionUID = 2951794972680258448L;
	private String accessToken;//访问授权码
	private String refreshToken;//刷新授权码
	private String personId;//用户id
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}
}
