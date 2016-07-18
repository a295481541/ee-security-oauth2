package com.eenet;

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
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.common.code.SystemCode;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.EndUserInfo;
import com.eenet.util.EEBeanUtils;

@Controller
public class RegistNewUserController {
	@Autowired
	private RegistNewUserBizService registNewUserBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	@RequestMapping(value = "/registEndUserWithLogin", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String registEndUserWithLogin(APIRequestIdentity identity,String redirectURI, @ModelAttribute("user")EndUserInfo user, @ModelAttribute("account")EndUserLoginAccount account, @ModelAttribute("credential")EndUserCredential credential) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		if (identity==null || EEBeanUtils.isNULL(identity.getAppId()) || EEBeanUtils.isNULL(identity.getAppSecretKey()) ) {
			response.setRSBizCode(SystemCode.AA0002);
			return EEBeanUtils.object2Json(response);
		}
		
		AppAuthenRequest appAttribute = new AppAuthenRequest();
		appAttribute.setAppId(identity.getAppId());
		appAttribute.setAppSecretKey(identity.getAppSecretKey());
		appAttribute.setRedirectURI(redirectURI);
		
		user.setCrss(identity.getAppId());
		account.setCrss(identity.getAppId());
		credential.setCrss(identity.getAppId());
		
		AccessToken token = registNewUserBizService.registEndUserWithLogin(user, account, credential, appAttribute);
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
