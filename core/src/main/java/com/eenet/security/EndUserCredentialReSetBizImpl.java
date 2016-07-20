package com.eenet.security;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.BooleanResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.user.EndUserInfoBizService;
import com.eenet.util.EEBeanUtils;

public class EndUserCredentialReSetBizImpl implements EndUserCredentialReSetBizService {

	@Override
	public StringResponse sendSMSCode4ResetPassword(String appId, long mobile) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || mobile<10000000000l) {
			result.addMessage("接入应用标识或手机号不正确("+this.getClass().getName()+")");
		}
		
		/* 检查该手机是否一分钟内发送过短信 */
		
		/* get enduser id */
		
		/* 生成短信验证码并缓存 */
		
		/* 发送短信 */
		
		return null;
	}

	@Override
	public BooleanResponse validateSMSCode4ResetPassword(String endUserId, String smsCode, boolean rmSmsCode) {
		
		/* 参数检查 */
		
		/* 校验短信验证码 */
		
		/* 删除短信验证码（如需要） */
		
		return null;
	}

	@Override
	public AccessToken resetPasswordBySMSCodeWithLogin(AppAuthenRequest appRequest, EndUserCredential curCredential,
			String smsCode) {
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		/* 参数检查 */
		
		/* 使用短信验证码重置密码 */
		SimpleResponse resetRS = resetPasswordBySMSCodeWithoutLogin(curCredential, smsCode);
		if (!resetRS.isSuccessful()) {
			result.addMessage(resetRS.getStrMessage());
			return result;
		}
		
		/* 获得当前用户登录账号（取一个账号） */
		
		/* 获得认证授权码 */
		
		/* 获得访问令牌 */
		return null;
	}

	@Override
	public SimpleResponse resetPasswordBySMSCodeWithoutLogin(EndUserCredential curCredential, String smsCode) {
		/* 参数检查 */
		
		/* 校验并删除短信验证码 */
		
		/* 重置用户登录密码 */
		return null;
	}
	
	/**
	 * 重置用户登录密码
	 * ★无任何旧密码校验，不可对外暴露服务
	 * @param endUserId 最终用户ID
	 * @param newPasswordPlainText 新密码
	 * @return
	 * 2016年7月19日
	 * @author Orion
	 */
	public SimpleResponse resetEndUserLoginPassword(String endUserId,String newPasswordPlainText) {
		return null;
	}
	
	private EndUserInfoBizService endUserInfoBizService;
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
	}

	public EndUserLoginAccountBizService getEndUserLoginAccountBizService() {
		return endUserLoginAccountBizService;
	}

	public void setEndUserLoginAccountBizService(EndUserLoginAccountBizService endUserLoginAccountBizService) {
		this.endUserLoginAccountBizService = endUserLoginAccountBizService;
	}
}
