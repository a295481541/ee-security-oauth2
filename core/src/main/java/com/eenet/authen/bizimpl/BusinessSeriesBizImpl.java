package com.eenet.authen.bizimpl;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessSeries;
import com.eenet.authen.BusinessSeriesBizService;
import com.eenet.authen.cacheSyn.SynBusinessApp2Redis;
import com.eenet.authen.cacheSyn.SynBusinessSeries2Redis2;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.common.cache.RedisClient;
import com.eenet.util.EEBeanUtils;
/**
 * 业务体系服务实现逻辑
 * @author koop
 *
 */
public class BusinessSeriesBizImpl  extends SimpleBizImpl implements BusinessSeriesBizService {
	private RedisClient RedisClient;//Redis客户端
	private BusinessAppBizService  businessAppBizService;//业务系统服务


	@Override
	public BusinessSeries retrieveBusinessSeries(String seriesId, String appId) {
		System.out.println("retrieveBusinessSeries start :" );
		
		
		if (EEBeanUtils.isNULL(seriesId) && EEBeanUtils.isNULL(appId))  {
			BusinessSeries result = new BusinessSeries();
			result.setSuccessful(false);
			result.addMessage("seriesId 或者 appid 必须指定("+this.getClass().getName()+")");
			return result;
		}
		
		if (!EEBeanUtils.isNULL(appId)) {//是否有传业务系统标识
			BusinessApp  businessApp = businessAppBizService.retrieveApp(appId); //数据库中取数据
			System.out.println(businessApp);
			System.out.println("businessApp retieApp :" +EEBeanUtils.object2Json(businessApp) );
			if (businessApp !=null &&businessApp.getBusinessSeries() != null ) {
				return businessApp.getBusinessSeries();
			}
		}
		
		
		if (!EEBeanUtils.isNULL(seriesId)) {//是否有传业务体系标识
			BusinessSeries   result = SynBusinessSeries2Redis2.get(getRedisClient(), seriesId);//缓存中取数据
			System.out.println(result);
			System.out.println("businessSeries redis :" +EEBeanUtils.object2Json(result) );
			if (result != null )
				return result;
			
			result   = super.get(seriesId);//数据库中取数据
			if (result != null ){
				System.out.println(result);
				System.out.println("businessSeries db :" +EEBeanUtils.object2Json(result) );
				SynBusinessSeries2Redis2.syn(getRedisClient(), result);
				return result;
			}
		}
		return null;
	
	}


	@Override
	public Class<?> getPojoCLS() {
		return BusinessSeries.class;
	}
	
	
	public RedisClient getRedisClient() {
		return RedisClient;
	}

	public void setRedisClient(RedisClient redisClient) {
		RedisClient = redisClient;
	}

	public BusinessAppBizService getBusinessAppBizService() {
		return businessAppBizService;
	}

	public void setBusinessAppBizService(BusinessAppBizService businessAppBizService) {
		this.businessAppBizService = businessAppBizService;
	}
	
	


}
