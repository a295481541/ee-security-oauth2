package com.eenet.authen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.common.code.SystemCode;
import com.eenet.util.EEBeanUtils;

@Controller
public class EndUserSMSSignOnController {
	@Autowired
	private EndUserSMSSignOnBizService endUserSMSSignOnBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	@RequestMapping(value = "/authen/EndUserSMSSignOn/sendSMSCode4Login", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String sendSMSCode4Login(APIRequestIdentity identity, long mobile) {
		SimpleResponse response = new SimpleResponse();
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
		
		/* 发送短信获得快速登录验证码 */
		response = endUserSMSSignOnBizService.sendSMSCode4Login(identity.getAppId(), mobile);
		return EEBeanUtils.object2Json(response);
	}
	
	@RequestMapping(value = "/authen/EndUserSMSSignOn/validateSMSCode4Login", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String validateSMSCode4Login(APIRequestIdentity identity, long mobile, String smsCode) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 参数检查 */
		if (identity==null || EEBeanUtils.isNULL(smsCode) ) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		/* 校验快速登录验证码 */
		response = endUserSMSSignOnBizService.validateSMSCode4Login(identity.getAppId(), mobile, smsCode, false);
		return EEBeanUtils.object2Json(response);
	}
	
	@RequestMapping(value = "/authen/EndUserSMSSignOn/getAccessToken", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String getAccessToken(APIRequestIdentity identity, String redirectURI, long mobile, String smsCode) {
		AccessToken response = new AccessToken();
		response.setSuccessful(false);
		
		/* 参数检查 */
		if ( identity==null || EEBeanUtils.isNULL(smsCode) || EEBeanUtils.isNULL(redirectURI) ) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		/* 业务系统身份对象 */
		AppAuthenRequest appAttribute = new AppAuthenRequest();
		appAttribute.setAppId(identity.getAppId());
		appAttribute.setAppSecretKey(identity.getAppSecretKey());
		appAttribute.setRedirectURI(redirectURI);
		
		/* 获得访问授权码（通过短信验证码） */
		response = endUserSMSSignOnBizService.getAccessToken(appAttribute, mobile, smsCode);
		return EEBeanUtils.object2Json(response);
	}
}
