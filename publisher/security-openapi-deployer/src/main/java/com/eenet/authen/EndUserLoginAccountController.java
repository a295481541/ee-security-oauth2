package com.eenet.authen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.util.EEBeanUtils;

@Controller
public class EndUserLoginAccountController {
	@Autowired
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	@Autowired
	private EndUserCredentialBizService endUserCredentialBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	@RequestMapping(value = "/registeEndUserLoginAccount", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String registeEndUserLoginAccount(APIRequestIdentity identity, EndUserLoginAccount loginAccount) {
		//all
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 参数检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 根据用户类型验证身份 */
		if (identity.getUserType().equals("anonymous") || identity.getUserType().equals("sysUser")) {
			AppAuthenRequest request = new AppAuthenRequest();
			request.setAppId(identity.getAppId());
			request.setAppSecretKey(identity.getAppSecretKey());
			SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(request);
			if (!appAuthen.isSuccessful()) {
				response.addMessage(appAuthen.getStrMessage());
				return EEBeanUtils.object2Json(response);
			}
		} else if (identity.getUserType().equals("endUser") || identity.getUserType().equals("adminUser")) {
			UserAccessTokenAuthenResponse tokenAuthen = null;
			if (identity.getUserType().equals("endUser")) {
				tokenAuthen = identityAuthenticationBizService.endUserAuthen(identity);
			} else if (identity.getUserType().equals("adminUser")) {
				tokenAuthen = identityAuthenticationBizService.adminUserAuthen(identity);
			}
			if (tokenAuthen==null || !tokenAuthen.isSuccessful()) {
				if (tokenAuthen==null)
					response.addMessage("验证失败，无错误信息");
				else
					response.addMessage(tokenAuthen.getStrMessage());
				return EEBeanUtils.object2Json(response);
			}
		} else {
			response.addMessage("未知的用户类型："+identity.getUserType());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 注入当前操作者信息 */
		loginAccount.setCrss(identity.getAppId());
		loginAccount.setMdss(identity.getAppId());
		if (identity.getUserType().equals("endUser") || identity.getUserType().equals("adminUser")) {
			loginAccount.setCrps(identity.getUserId());
			loginAccount.setMdps(identity.getUserId());
		} else {
			loginAccount.setCrps(identity.getUserType());
			loginAccount.setMdps(identity.getUserType());
		}
		
		/* 执行业务 */
		EndUserLoginAccount result = endUserLoginAccountBizService.registeEndUserLoginAccount(loginAccount);
		return EEBeanUtils.object2Json(result);
	}
	
	@RequestMapping(value = "/initEndUserLoginPassword", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String initEndUserLoginPassword(APIRequestIdentity identity, EndUserCredential credential) {
		SimpleResponse result = endUserCredentialBizService.initEndUserLoginPassword(credential);
		return EEBeanUtils.object2Json(result);
	}
	
	@RequestMapping(value = "/changeEndUserLoginPassword", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String changeEndUserLoginPassword(APIRequestIdentity identity, EndUserCredential curCredential, String newSecretKey) {
		//endUser(self)
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (!identity.getUserType().equals("endUser")) {
			response.addMessage(identity.getUserType()+"类型的用户不可修改最终用户主登录密码");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 可进行操作判断 */
		if (EEBeanUtils.isNULL(identity.getUserId()) || !identity.getUserId().equals(curCredential.getEndUser().getAtid())) {
			response.addMessage("只允许修改自己的密码");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 用户身份验证（令牌） */
		UserAccessTokenAuthenResponse tokenAuthen = identityAuthenticationBizService.endUserAuthen(identity);
		if (!tokenAuthen.isSuccessful()) {
			response.addMessage(tokenAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 注入当前操作者信息 */
		curCredential.setCrss(identity.getAppId());
		curCredential.setMdss(identity.getAppId());
		if (identity.getUserType().equals("endUser") || identity.getUserType().equals("adminUser")) {
			curCredential.setCrps(identity.getUserId());
			curCredential.setMdps(identity.getUserId());
		} else {
			curCredential.setCrps(identity.getUserType());
			curCredential.setMdps(identity.getUserType());
		}
		
		/* 执行业务 */
		SimpleResponse result = endUserCredentialBizService.changeEndUserLoginPassword(curCredential, newSecretKey);
		return EEBeanUtils.object2Json(result);
	}
	
	
	
	
	@RequestMapping(value = "/changeEndUserLoginPasswordNew", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String changeEndUserLoginPasswordNew(APIRequestIdentity identity, EndUserCredential curCredential, EndUserLoginAccount account, String newSecretKey) {
		//endUser(self)
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (!identity.getUserType().equals("endUser")) {
			response.addMessage(identity.getUserType()+"类型的用户不可修改最终用户主登录密码");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 可进行操作判断 */
		if (EEBeanUtils.isNULL(identity.getUserId()) || !identity.getUserId().equals(curCredential.getEndUser().getAtid())) {
			response.addMessage("只允许修改自己的密码");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 用户身份验证（令牌） */
		UserAccessTokenAuthenResponse tokenAuthen = identityAuthenticationBizService.endUserAuthen(identity);
		if (!tokenAuthen.isSuccessful()) {
			response.addMessage(tokenAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 注入当前操作者信息 */
		curCredential.setCrss(identity.getAppId());
		curCredential.setMdss(identity.getAppId());
		if (identity.getUserType().equals("endUser") || identity.getUserType().equals("adminUser")) {
			curCredential.setCrps(identity.getUserId());
			curCredential.setMdps(identity.getUserId());
		} else {
			curCredential.setCrps(identity.getUserType());
			curCredential.setMdps(identity.getUserType());
		}
		
		/* 执行业务 */
		SimpleResponse result = endUserCredentialBizService.changeEndUserLoginPassword(curCredential, account, newSecretKey);
		return EEBeanUtils.object2Json(result);
	}
	
	
	
	
	@RequestMapping(value = "/resetEndUserLoginPassword", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String resetEndUserLoginPassword(APIRequestIdentity identity, String endUserId) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		/* 参数检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		}
		
		UserAccessTokenAuthenResponse tokenAuthen = null;
		if (identity.getUserType().equals("endUser")) {
			tokenAuthen = identityAuthenticationBizService.endUserAuthen(identity);
		} else if (identity.getUserType().equals("adminUser")) {
			tokenAuthen = identityAuthenticationBizService.adminUserAuthen(identity);
		} else {
			response.addMessage("不允许的用户类型："+identity.getUserType());
			return EEBeanUtils.object2Json(response);
		}
		if (!tokenAuthen.isSuccessful()) {
			response.addMessage(tokenAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 执行业务 */
		SimpleResponse result = endUserCredentialBizService.resetEndUserLoginPassword(endUserId);
		return EEBeanUtils.object2Json(result);
	}
}
