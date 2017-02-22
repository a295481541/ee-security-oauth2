package com.eenet.security;

import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.base.BooleanResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;

public class PreRegistEndUserBizImpl implements PreRegistEndUserBizService {
	private EndUserInfoBizService endUserInfoBizService;
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	
	@Override
	public BooleanResponse existMobileEmailId(String mobile, String email, String idCard) {
		boolean loginAccountExist = false;
		
		/* 检查是否存在登录账号 */
		if ( !loginAccountExist )
			loginAccountExist = getEndUserLoginAccountBizService().retrieveEndUserInfo(mobile).isSuccessful();
		if ( !loginAccountExist )
			loginAccountExist = getEndUserLoginAccountBizService().retrieveEndUserInfo(email).isSuccessful();
		if ( !loginAccountExist )
			loginAccountExist = getEndUserLoginAccountBizService().retrieveEndUserInfo(idCard).isSuccessful();
		
		BooleanResponse result = new BooleanResponse();
		result.setResult(loginAccountExist);
		return result;
	}

	@Override
	public EndUserInfo getByMobileEmailId(String mobile, String email, String idCard) {
		EndUserInfo endUser = new EndUserInfo();
		endUser.setSuccessful(false);
		
		/* 根据登录账号获得人员基本信息 */
		if ( !endUser.isSuccessful() )
			endUser = getEndUserLoginAccountBizService().retrieveEndUserInfo(mobile);
		if ( !endUser.isSuccessful() )
			endUser = getEndUserLoginAccountBizService().retrieveEndUserInfo(email);
		if ( !endUser.isSuccessful() )
			endUser = getEndUserLoginAccountBizService().retrieveEndUserInfo(idCard);
		
		return endUser;
		
	}

	/**
	 * @return the endUserInfoBizService
	 */
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	/**
	 * @param endUserInfoBizService the endUserInfoBizService to set
	 */
	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
	}

	/**
	 * @return the endUserLoginAccountBizService
	 */
	public EndUserLoginAccountBizService getEndUserLoginAccountBizService() {
		return endUserLoginAccountBizService;
	}

	/**
	 * @param endUserLoginAccountBizService the endUserLoginAccountBizService to set
	 */
	public void setEndUserLoginAccountBizService(EndUserLoginAccountBizService endUserLoginAccountBizService) {
		this.endUserLoginAccountBizService = endUserLoginAccountBizService;
	}
}
