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
			loginAccountExist = getEndUserLoginAccountBizService().retrieveEndUserInfo(null ,mobile).isSuccessful();//TODO
		if ( !loginAccountExist )
			loginAccountExist = getEndUserLoginAccountBizService().retrieveEndUserInfo(null ,email).isSuccessful();//TODO
		if ( !loginAccountExist )
			loginAccountExist = getEndUserLoginAccountBizService().retrieveEndUserInfo(null ,idCard).isSuccessful();//TODO
		
		if (loginAccountExist) {
			BooleanResponse result = new BooleanResponse();
			result.setResult(true);
			return result;
		}
			
		
		/* 账号不存在，检查人员信息中的手机、邮箱、身份证是否存在 */
		return getEndUserInfoBizService().existMobileEmailId(mobile, email, idCard);
	}

	@Override
	public EndUserInfo getByMobileEmailId(String mobile, String email, String idCard) {
		EndUserInfo endUser = new EndUserInfo();
		endUser.setSuccessful(false);
		
		/* 根据登录账号获得人员基本信息 */
		if ( !endUser.isSuccessful() )
			endUser = getEndUserLoginAccountBizService().retrieveEndUserInfo(null ,mobile);//TODO
		if ( !endUser.isSuccessful() )
			endUser = getEndUserLoginAccountBizService().retrieveEndUserInfo(null ,email);//TODO
		if ( !endUser.isSuccessful() )
			endUser = getEndUserLoginAccountBizService().retrieveEndUserInfo(null ,idCard);//TODO
		
		if (endUser.isSuccessful())
			return endUser;
		
		/* 账号不存在，恩局人员信息中的手机、邮箱、身份证获取个人信息 */
		return getEndUserInfoBizService().getByMobileEmailId(mobile, email, idCard);
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
