package com.eenet.security;

import com.eenet.authen.BusinessSeries;
import com.eenet.authen.BusinessSeriesBizService;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.base.BooleanResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.util.EEBeanUtils;

public class PreRegistEndUserBizImpl implements PreRegistEndUserBizService {
	private EndUserInfoBizService endUserInfoBizService;
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	private BusinessSeriesBizService businessSeriesBizService;
	
	
	@Override
	public BooleanResponse existAccount(String appId, String seriesId, String... accounts) {
		BooleanResponse result =new BooleanResponse();
		BusinessSeries businessSeries =  businessSeriesBizService.retrieveBusinessSeries(seriesId, appId);
		if (businessSeries == null ||  EEBeanUtils.isNULL(businessSeries.getAtid())) {
			result.setSuccessful(false);
			result.setResult(false);
			result.addMessage("业务体系不存在或者未指定("+this.getClass().getName()+")");
			return result;
		}
		
		EndUserInfo endUserInfo = null;
		for(String account : accounts){
			endUserInfo = endUserLoginAccountBizService.retrieveEndUserInfo(businessSeries.getAtid(), account);
			if (endUserInfo == null ||!endUserInfo.isSuccessful()) {
				result.setSuccessful(true);
				result.setResult(true);
				result.addMessage("账户"+account+"已存在("+this.getClass().getName()+")");
				return result;
			}
		}
		result.setSuccessful(false);
		result.setResult(false);
		return result;
	}

	@Override
	public EndUserInfo retrieveEndUserInfo(String appId, String seriesId, String account) {
		
		EndUserInfo  result  = new EndUserInfo();
		
		BusinessSeries businessSeries =  businessSeriesBizService.retrieveBusinessSeries(seriesId, appId);
		
		if (businessSeries == null ||  EEBeanUtils.isNULL(businessSeries.getAtid())) {
			result.setSuccessful(false);
			result.addMessage("业务体系不存在或者未指定("+this.getClass().getName()+")");
			return result;
		}
		
		return endUserLoginAccountBizService.retrieveEndUserInfo(businessSeries.getAtid(), account);
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
	
	/**
	 * @return the businessSeriesBizService
	 */
	public BusinessSeriesBizService getBusinessSeriesBizService() {
		return businessSeriesBizService;
	}

	/**
	 * @param businessSeriesBizService the businessSeriesBizService to set
	 */
	public void setBusinessSeriesBizService(BusinessSeriesBizService businessSeriesBizService) {
		this.businessSeriesBizService = businessSeriesBizService;
	}
	
	

	
}
