package com.eenet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.base.SimpleResponse;
import com.eenet.util.EEBeanUtils;

/**
 * 身份认证
 * 2016年5月29日
 * @author Orion
 */
@Controller
public class IdentityAuthenController {
	
	@RequestMapping(value = "/endUserAuthen", method = RequestMethod.GET)
	@ResponseBody
	public String endUserAuthen(String appId, String appSecretKey,String userId, String accessToken) {
		EENetEndUserAuthenResponse response = new EENetEndUserAuthenResponse();
		response.setAppIdentityConfirm(true);
		response.setEndUseridentityConfirm(true);
		return EEBeanUtils.object2Json(response);
	}
}

class EENetEndUserAuthenResponse extends SimpleResponse {
	private static final long serialVersionUID = -381206820410817525L;
	private boolean appIdentityConfirm = false;//应用系统身份认证结果
	public boolean isAppIdentityConfirm() {
		return appIdentityConfirm;
	}
	public void setAppIdentityConfirm(boolean appIdentityConfirm) {
		this.appIdentityConfirm = appIdentityConfirm;
	}
	public boolean isEndUseridentityConfirm() {
		return endUseridentityConfirm;
	}
	public void setEndUseridentityConfirm(boolean endUseridentityConfirm) {
		this.endUseridentityConfirm = endUseridentityConfirm;
	}
	private boolean endUseridentityConfirm = false;//用户身份认证结果
}
