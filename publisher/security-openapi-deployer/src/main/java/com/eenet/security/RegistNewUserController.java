package com.eenet.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.authen.APIRequestIdentity;
import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.base.BooleanResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.AdminUserInfo;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.common.OPOwner;
import com.eenet.common.code.SystemCode;
import com.eenet.model.EndUserLoginAccountListModel;
import com.eenet.security.RegistNewUserBizService;
import com.eenet.util.EEBeanUtils;

@Controller
public class RegistNewUserController {
	private static final Logger log = LoggerFactory.getLogger("error");
	@Autowired
	private RegistNewUserBizService registNewUserBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	@Autowired
	private PreRegistEndUserBizService preRegistEndUserBizService;
	
	@RequestMapping(value = "/endUserExistMEID", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String existMobileEmailId(APIRequestIdentity identity,String mobile, String email, String idCard) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 接入系统认证 */
		AppAuthenRequest request = new AppAuthenRequest();
		request.setAppId(identity.getAppId());
		request.setAppSecretKey(identity.getAppSecretKey());
		SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(request);
		if (!appAuthen.isSuccessful()) {
			response.addMessage(appAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 执行业务 */
		BooleanResponse result = this.preRegistEndUserBizService.existMobileEmailId(mobile, email, idCard);
		return EEBeanUtils.object2Json(result);
	}
	
	@RequestMapping(value = "/getEndUserByMEID", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String getByMobileEmailId(APIRequestIdentity identity,String mobile, String email, String idCard) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (!identity.getUserType().equals("endUser") && !identity.getUserType().equals("adminUser")) {
			response.addMessage(identity.getUserType()+"类型的用户不可通过手机、邮箱或身份证获得最终用户个人信息");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 接入系统认证 */
		AppAuthenRequest request = new AppAuthenRequest();
		request.setAppId(identity.getAppId());
		request.setAppSecretKey(identity.getAppSecretKey());
		SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(request);
		if (!appAuthen.isSuccessful()) {
			response.addMessage(appAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 根据用户类型验证身份 */
		UserAccessTokenAuthenResponse tokenAuthen = null;
		if (identity.getUserType().equals("endUser")) {
			tokenAuthen = identityAuthenticationBizService.endUserAuthen(identity);
		} else if (identity.getUserType().equals("adminUser")) {
			tokenAuthen = identityAuthenticationBizService.adminUserAuthen(identity);
		} else {
			response.addMessage("未知的用户类型："+identity.getUserType());
			return EEBeanUtils.object2Json(response);
		}
		if (tokenAuthen==null || !tokenAuthen.isSuccessful()) {
			if (tokenAuthen==null)
				response.addMessage("验证失败，无错误信息");
			else
				response.addMessage(tokenAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 执行业务 */
		EndUserInfo result = preRegistEndUserBizService.getByMobileEmailId(mobile, email, idCard);
		return EEBeanUtils.object2Json(result);
	}
	
	@RequestMapping(value = "/registEndUserWithLogin", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String registEndUserWithLogin(String redirectURI, @ModelAttribute("user")EndUserInfo user, @ModelAttribute("account")EndUserLoginAccount account, @ModelAttribute("credential")EndUserCredential credential) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		if (OPOwner.UNKNOW_APP_FLAG.equals(OPOwner.getCurrentSys())
				|| EEBeanUtils.isNULL(CallerIdentityInfo.getAppsecretkey())
				|| EEBeanUtils.isNULL(CallerIdentityInfo.getRedirecturi())) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		log.error("CurrentSys : "+OPOwner.getCurrentSys());
		
		AccessToken token = registNewUserBizService.registEndUserWithLogin(user, account, credential);
		return EEBeanUtils.object2Json(token);
	}
	
	@RequestMapping(value = "/regist/endUserWithMulAccountAndLogin", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String registEndUserWithMulAccountAndLogin(@ModelAttribute("user")EndUserInfo user, @ModelAttribute("account")EndUserLoginAccountListModel account, @ModelAttribute("credential")EndUserCredential credential) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		if (OPOwner.UNKNOW_APP_FLAG.equals(OPOwner.getCurrentSys())
				|| EEBeanUtils.isNULL(CallerIdentityInfo.getAppsecretkey())
				|| EEBeanUtils.isNULL(CallerIdentityInfo.getRedirecturi())) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		log.error("CurrentSys : "+OPOwner.getCurrentSys());
		
		AccessToken token = registNewUserBizService.registEndUserWithMulAccountAndLogin(user, account.getM(), credential);
		return EEBeanUtils.object2Json(token);
	}
	
	@RequestMapping(value = "/registAdminUserWithoutLogin", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String registAdminUserWithoutLogin(APIRequestIdentity identity, @ModelAttribute("user")AdminUserInfo adminUser, @ModelAttribute("account")AdminUserLoginAccount account, @ModelAttribute("credential")AdminUserCredential credential){
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		if (identity==null || EEBeanUtils.isNULL(identity.getAppId()) || EEBeanUtils.isNULL(identity.getAppSecretKey()) ) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		AppAuthenRequest appAttribute = new AppAuthenRequest();
		appAttribute.setAppId(identity.getAppId());
		appAttribute.setAppSecretKey(identity.getAppSecretKey());
		SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(appAttribute);
		if (!appAuthen.isSuccessful()) {
			response.addMessage(appAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		adminUser.setCrss(identity.getAppId());
		account.setCrss(identity.getAppId());
		credential.setCrss(identity.getAppId());
		
		response = registNewUserBizService.registAdminUserWithoutLogin(adminUser, account, credential);
		return EEBeanUtils.object2Json(response);
	}
	
	@InitBinder("account")
    public void bindAccount(WebDataBinder binder) {
            binder.setFieldDefaultPrefix("account.");
    }
    @InitBinder("user")
    public void bindUser(WebDataBinder binder) {
            binder.setFieldDefaultPrefix("user.");
    }
    @InitBinder("credential")
    public void bindCredential(WebDataBinder binder) {
            binder.setFieldDefaultPrefix("credential.");
    }
}
