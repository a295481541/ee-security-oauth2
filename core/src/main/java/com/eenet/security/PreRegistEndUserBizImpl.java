package com.eenet.security;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessSeries;
import com.eenet.authen.BusinessSeriesBizService;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.base.BooleanResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.OPOwner;
import com.eenet.util.EEBeanUtils;

public class PreRegistEndUserBizImpl implements PreRegistEndUserBizService {
	private EndUserInfoBizService endUserInfoBizService;
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	private BusinessSeriesBizService businessSeriesBizService;
	private BusinessAppBizService businessAppBizService;
	
	
	@Override
	public BooleanResponse existAccount(String appId,String seriesId, String... accounts) {
		
		
		System.out.println("传入的seriesId :"+seriesId +"  传入的appId：" +appId);
		
		BooleanResponse result =new BooleanResponse();
		
		BusinessApp app = businessAppBizService.retrieveApp(appId);
		
		System.out.println("appp get " +EEBeanUtils.object2Json(app));
		if ( (app.getBusinessSeries()==null || EEBeanUtils.isNULL(app.getBusinessSeries().getAtid())) && !EEBeanUtils.isNULL(seriesId) ) 
			app.setBusinessSeries(businessSeriesBizService.retrieveBusinessSeries(seriesId, null));
		
		
		if (!app.isSuccessful() || !app.getBusinessSeries().isSuccessful()|| EEBeanUtils.isNULL(app.getBusinessSeries().getAtid()) ) {
			result.addMessage("该体系系统不存在("+this.getClass().getName()+")");
			return result;
		}
		System.out.println("seriesId:" +seriesId +"   " +app.getBusinessSeries().getAtid());
		if (!EEBeanUtils.isNULL(seriesId)&&!seriesId.equals(app.getBusinessSeries().getAtid())) {
			result.addMessage("指定的业务体系与系统所隶属的业务体系不一致("+this.getClass().getName()+")");
			return result;
		}
		
		seriesId = app.getBusinessSeries().getAtid();
		
		EndUserInfo endUserInfo = null;
		for(String account : accounts){
			endUserInfo = endUserLoginAccountBizService.retrieveEndUserInfo(seriesId, account);
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
		
		
		System.out.println("传入的seriesId :"+seriesId +"  传入的appId：" +appId);
		BusinessApp app = businessAppBizService.retrieveApp(appId);
		
		System.out.println("appp get " +EEBeanUtils.object2Json(app));
		if ( (app.getBusinessSeries()==null || EEBeanUtils.isNULL(app.getBusinessSeries().getAtid())) && !EEBeanUtils.isNULL(seriesId) ) 
			app.setBusinessSeries(businessSeriesBizService.retrieveBusinessSeries(seriesId, null));
		
		
		if (!app.isSuccessful() || !app.getBusinessSeries().isSuccessful()|| EEBeanUtils.isNULL(app.getBusinessSeries().getAtid()) ) {
			result.addMessage("该体系系统不存在("+this.getClass().getName()+")");
			return result;
		}
		System.out.println("seriesId:" +seriesId +"   " +app.getBusinessSeries().getAtid());
		if (!EEBeanUtils.isNULL(seriesId)&&!seriesId.equals(app.getBusinessSeries().getAtid())) {
			result.addMessage("指定的业务体系与系统所隶属的业务体系不一致("+this.getClass().getName()+")");
			return result;
		}
		
		
		
		
		return endUserLoginAccountBizService.retrieveEndUserInfo(app.getBusinessSeries().getAtid(), account);
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

	public BusinessAppBizService getBusinessAppBizService() {
		return businessAppBizService;
	}

	public void setBusinessAppBizService(BusinessAppBizService businessAppBizService) {
		this.businessAppBizService = businessAppBizService;
	}
	
	

	
}
