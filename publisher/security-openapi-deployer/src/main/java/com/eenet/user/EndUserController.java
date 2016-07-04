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
import com.eenet.base.BooleanResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.base.query.QueryCondition;
import com.eenet.util.EEBeanUtils;

@Controller
public class EndUserController {
	@Autowired
	private EndUserInfoBizService endUserInfoBizService;
	@Autowired
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	public String queryEndUser(APIRequestIdentity identity) {
		return null;
	}
	
	@RequestMapping(value = "/getEndUser", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)// , method = RequestMethod.GET
	@ResponseBody
	public String getEndUser(APIRequestIdentity identity, String getEndUserId) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (identity.getUserType().equals("sysUser") || identity.getUserType().equals("anonymous")) {
			response.addMessage(identity.getUserType()+"类型的用户不可读取最终用户个人信息");
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
		
		/* 身份验证通过 */
		EndUserInfo endUser = this.endUserInfoBizService.get(getEndUserId);
		return EEBeanUtils.object2Json(endUser);
	}
	
	@RequestMapping(value = "/saveEndUser", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
	@ResponseBody
	public String saveEndUser(APIRequestIdentity identity, EndUserInfo endUser) {
		SimpleResponse response = new SimpleResponse();
		response.setSuccessful(false);
		
		/* 用户类型检查 */
		if (identity==null || EEBeanUtils.isNULL(identity.getUserType())) {
			response.addMessage("用户类型未知");
			return EEBeanUtils.object2Json(response);
		} else if (identity.getUserType().equals("sysUser")) {
			response.addMessage(identity.getUserType()+"类型的用户不可创建或修改最终用户个人信息");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 用户类型可进行操作判断 */
		boolean opCheck = false;
		if (identity.getUserType().equals("adminUser") && !EEBeanUtils.isNULL(identity.getUserId())) {//管理员可新增和修改数据
			opCheck = true;
		} else if (identity.getUserType().equals("endUser") && !EEBeanUtils.isNULL(identity.getUserId())
				&& identity.getUserId().equals(endUser.getAtid())) {// endUser可修改自己的信息
			opCheck = true;
		} else if (identity.getUserType().equals("anonymous") && EEBeanUtils.isNULL(identity.getUserId())) {//anonymous可新增数据
			opCheck = true;
		}
		if (!opCheck) {
			response.addMessage("不允许的操作");
			return EEBeanUtils.object2Json(response);
		}
		
		/* 根据用户类型验证身份 */
		if (identity.getUserType().equals("anonymous")) {
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
		endUser.setCrss(identity.getAppId());
		endUser.setMdss(identity.getAppId());
		if (identity.getUserType().equals("endUser") || identity.getUserType().equals("adminUser")) {
			endUser.setCrps(identity.getUserId());
			endUser.setMdps(identity.getUserId());
		} else {
			endUser.setCrps(identity.getUserType());
			endUser.setMdps(identity.getUserType());
		}
		
		/* 执行业务 */
		EndUserInfo result = this.endUserInfoBizService.save(endUser);
		return EEBeanUtils.object2Json(result);
	}
	
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
		BooleanResponse result = this.endUserInfoBizService.existMobileEmailId(mobile, email, idCard);
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
		EndUserInfo result = this.endUserInfoBizService.getByMobileEmailId(mobile, email, idCard);
		return EEBeanUtils.object2Json(result);
	}
}