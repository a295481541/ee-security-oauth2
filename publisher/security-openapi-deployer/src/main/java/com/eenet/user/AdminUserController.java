package com.eenet.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.authen.APIRequestIdentity;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.response.UserAccessTokenAuthenResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.util.EEBeanUtils;

@Controller
public class AdminUserController {
	@Autowired
	private AdminUserInfoBizService adminUserInfoBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	@RequestMapping(value = "/getAdminUser", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST) //, method = RequestMethod.GET
	@ResponseBody
	public String getAdminUser(APIRequestIdentity identity, String getAdminUserId) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (identity.getUserType().equals("endUser") || identity.getUserType().equals("anonymous")) {
			response.addMessage(identity.getUserType()+"类型的用户不可读取服务人员个人信息");
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
		
		/* 身份验证通过 */
		AdminUserInfo admin = this.adminUserInfoBizService.get(getAdminUserId);
		return EEBeanUtils.object2Json(admin);
	}
	
	@RequestMapping(value = "/saveAdminUser", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String saveAdminUser(APIRequestIdentity identity, AdminUserInfo admin) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (!identity.getUserType().equals("adminUser")) {
			response.addMessage(identity.getUserType()+"类型的用户不可创建或修改服务人员个人信息");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 用户身份验证（令牌） */
		UserAccessTokenAuthenResponse tokenAuthen = identityAuthenticationBizService.adminUserAuthen(identity);
		if (!tokenAuthen.isSuccessful()) {
			response.addMessage(tokenAuthen.getStrMessage());
			return EEBeanUtils.object2Json(response);
		}
		
		/* 注入当前操作者信息 */
		admin.setCrss(identity.getAppId());
		admin.setMdss(identity.getAppId());
		if (identity.getUserType().equals("endUser") || identity.getUserType().equals("adminUser")) {
			admin.setCrps(identity.getUserId());
			admin.setMdps(identity.getUserId());
		} else {
			admin.setCrps(identity.getUserType());
			admin.setMdps(identity.getUserType());
		}
		
		/* 执行业务 */
		AdminUserInfo result = this.adminUserInfoBizService.save(admin);
		return EEBeanUtils.object2Json(result);
	}
}