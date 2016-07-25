package com.eenet.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.authen.APIRequestIdentity;
import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.common.code.SystemCode;
import com.eenet.util.EEBeanUtils;

@Controller
public class EndUserCredentialReSetController {
	@Autowired
	private EndUserCredentialReSetBizService endUserCredentialReSetBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	@RequestMapping(value = "/security/sendSMSCode4ResetPassword", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String sendSMSCode4ResetPassword(APIRequestIdentity identity, long mobile) {
		StringResponse response = new StringResponse();
		response.setSuccessful(false);
		
		/* 参数检查 */
		if (identity==null) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		/* 业务系统认证 */
		AppAuthenRequest appAttribute = new AppAuthenRequest();
		appAttribute.setAppId(identity.getAppId());
		appAttribute.setAppSecretKey(identity.getAppSecretKey());
		
		SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(appAttribute);
		if (!appAuthen.isSuccessful()) {
			response.addMessage(appAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		response = endUserCredentialReSetBizService.sendSMSCode4ResetPassword(identity.getAppId(), mobile);
		return EEBeanUtils.object2Json(response);
	}
	
	@RequestMapping(value = "/security/validateSMSCode4ResetPassword", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String validateSMSCode4ResetPassword(APIRequestIdentity identity, String endUserId, String smsCode) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 参数检查 */
		if (identity==null || EEBeanUtils.isNULL(endUserId) || EEBeanUtils.isNULL(smsCode) ) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		/* 业务系统认证 */
		AppAuthenRequest appAttribute = new AppAuthenRequest();
		appAttribute.setAppId(identity.getAppId());
		appAttribute.setAppSecretKey(identity.getAppSecretKey());
		
		SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(appAttribute);
		if (!appAuthen.isSuccessful()) {
			response.addMessage(appAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		response = endUserCredentialReSetBizService.validateSMSCode4ResetPassword(endUserId, smsCode, false);
		return EEBeanUtils.object2Json(response);
	}
	
	@RequestMapping(value = "/security/resetPasswordBySMSCodeWithLogin", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String resetPasswordBySMSCodeWithLogin(APIRequestIdentity identity, String redirectURI, EndUserCredential credential, String smsCode) {
		AccessToken response = new AccessToken();
		response.setSuccessful(false);
		
		/* 参数检查 */
		if ( identity==null || credential==null || EEBeanUtils.isNULL(smsCode) || EEBeanUtils.isNULL(redirectURI) ) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		/* 业务系统身份对象 */
		AppAuthenRequest appAttribute = new AppAuthenRequest();
		appAttribute.setAppId(identity.getAppId());
		appAttribute.setAppSecretKey(identity.getAppSecretKey());
		appAttribute.setRedirectURI(redirectURI);
		
		response = endUserCredentialReSetBizService.resetPasswordBySMSCodeWithLogin(appAttribute, credential, smsCode);
		return EEBeanUtils.object2Json(response);
	}
	
	@RequestMapping(value = "/security/resetPasswordBySMSCodeWithoutLogin", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String resetPasswordBySMSCodeWithoutLogin(APIRequestIdentity identity, EndUserCredential credential, String smsCode) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 参数检查 */
		if ( identity==null || credential==null || EEBeanUtils.isNULL(smsCode)) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		/* 业务系统身份对象 */
		AppAuthenRequest appAttribute = new AppAuthenRequest();
		appAttribute.setAppId(identity.getAppId());
		appAttribute.setAppSecretKey(identity.getAppSecretKey());
		
		SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(appAttribute);
		if (!appAuthen.isSuccessful()) {
			response.addMessage(appAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		response = endUserCredentialReSetBizService.resetPasswordBySMSCodeWithoutLogin(credential, smsCode);
		return EEBeanUtils.object2Json(response);
	}
}
