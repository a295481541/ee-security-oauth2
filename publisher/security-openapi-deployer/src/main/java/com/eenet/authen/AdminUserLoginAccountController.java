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
public class AdminUserLoginAccountController {
	@Autowired
	private AdminUserLoginAccountBizService adminUserLoginAccountBizService;
	@Autowired
	private AdminUserCredentialBizService adminUserCredentialBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	@RequestMapping(value = "/registeAdminUserLoginAccount", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String registeAdminUserLoginAccount(APIRequestIdentity identity, AdminUserLoginAccount loginAccount) {
		//adminUser,sysUser
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (identity.getUserType().equals("endUser") || identity.getUserType().equals("anonymous")) {
			response.addMessage(identity.getUserType()+"类型的用户不可注册服务人员登录账号");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 根据用户类型验证身份 */
		if (identity.getUserType().equals("sysUser")) {
			AppAuthenRequest request = new AppAuthenRequest();
			request.setAppId(identity.getAppId());
			request.setAppSecretKey(identity.getAppSecretKey());
			SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(request);
			if (!appAuthen.isSuccessful()) {
				response.addMessage(appAuthen.getStrMessage());
				return EEBeanUtils.object2Json(response);
			}
		} else if (identity.getUserType().equals("adminUser")) {
			UserAccessTokenAuthenResponse tokenAuthen = identityAuthenticationBizService.adminUserAuthen(identity);
			if (!tokenAuthen.isSuccessful()) {
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
		AdminUserLoginAccount result = adminUserLoginAccountBizService.registeAdminUserLoginAccount(loginAccount);
		return EEBeanUtils.object2Json(result);
	}
	
	@RequestMapping(value = "/initAdminUserLoginPassword", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String initAdminUserLoginPassword(APIRequestIdentity identity, AdminUserCredential credential) {
		//adminUser,sysUser
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (identity.getUserType().equals("endUser") || identity.getUserType().equals("anonymous")) {
			response.addMessage(identity.getUserType()+"类型的用户不可初始化服务人员登录密码");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 根据用户类型验证身份 */
		if (identity.getUserType().equals("sysUser")) {
			AppAuthenRequest request = new AppAuthenRequest();
			request.setAppId(identity.getAppId());
			request.setAppSecretKey(identity.getAppSecretKey());
			SimpleResponse appAuthen = identityAuthenticationBizService.appAuthen(request);
			if (!appAuthen.isSuccessful()) {
				response.addMessage(appAuthen.getStrMessage());
				return EEBeanUtils.object2Json(response);
			}
		} else if (identity.getUserType().equals("adminUser")) {
			UserAccessTokenAuthenResponse tokenAuthen = identityAuthenticationBizService.adminUserAuthen(identity);
			if (!tokenAuthen.isSuccessful()) {
				response.addMessage(tokenAuthen.getStrMessage());
				return EEBeanUtils.object2Json(response);
			}
		} else {
			response.addMessage("未知的用户类型："+identity.getUserType());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 注入当前操作者信息 */
		credential.setCrss(identity.getAppId());
		credential.setMdss(identity.getAppId());
		if (identity.getUserType().equals("endUser") || identity.getUserType().equals("adminUser")) {
			credential.setCrps(identity.getUserId());
			credential.setMdps(identity.getUserId());
		} else {
			credential.setCrps(identity.getUserType());
			credential.setMdps(identity.getUserType());
		}
		
		/* 执行业务 */
		SimpleResponse result = adminUserCredentialBizService.initAdminUserLoginPassword(credential);
		return EEBeanUtils.object2Json(result);
	}
	
	@RequestMapping(value = "/changeAdminUserLoginPassword", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String changeAdminUserLoginPassword(APIRequestIdentity identity, AdminUserCredential curCredential, String newSecretKey) {
		//adminUser(self)
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (!identity.getUserType().equals("adminUser")) {
			response.addMessage(identity.getUserType()+"类型的用户不可修改服务人员登录密码");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 可进行操作判断 */
		if (EEBeanUtils.isNULL(identity.getUserId()) || !identity.getUserId().equals(curCredential.getAdminUser().getAtid())) {
			response.addMessage("只允许修改自己的密码");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 用户身份验证（令牌） */
		UserAccessTokenAuthenResponse tokenAuthen = identityAuthenticationBizService.adminUserAuthen(identity);
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
		SimpleResponse result = adminUserCredentialBizService.changeAdminUserLoginPassword(curCredential, newSecretKey);
		return EEBeanUtils.object2Json(result);
	}
}
