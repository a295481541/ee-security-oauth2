package com.eenet.authen;

import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.StringResponse;

public interface EndUserSMSSignOnBizService {
	
	public StringResponse sendSMSCode4Login(String appId, long mobile);
	
	public AccessToken getAccessToken(AppAuthenRequest appRequest, long mobile, String smsCode);
}
