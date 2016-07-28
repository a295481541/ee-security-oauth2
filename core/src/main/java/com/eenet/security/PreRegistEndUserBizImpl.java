package com.eenet.security;

import com.eenet.base.BooleanResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;

public class PreRegistEndUserBizImpl implements PreRegistEndUserBizService {
	private EndUserInfoBizService endUserInfoBizService;
	
	@Override
	public BooleanResponse existMobileEmailId(String mobile, String email, String idCard) {
		return getEndUserInfoBizService().existMobileEmailId(mobile, email, idCard);
	}

	@Override
	public EndUserInfo getByMobileEmailId(String mobile, String email, String idCard) {
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
}
