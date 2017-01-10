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
	private BusinessAppBizService  businessAppBizService;

	@Override
	public Class<?> getPojoCLS() {
		return BusinessApp.class;
	}

	@Override
	public BusinessSeries retrieveBusinessSeries(String seriesId, String appId) {
		
		BusinessSeries result = new BusinessSeries();
		result.setSuccessful(false);
		
		if (EEBeanUtils.isNULL(seriesId) && EEBeanUtils.isNULL(appId))  {
			result.setSuccessful(false);
			result.addMessage("seriesId 或者 appid 必须指定("+this.getClass().getName()+")");
			return result;
		}
		
		if (EEBeanUtils.isNULL(seriesId)) {//是否有传业务体系标识
			result = SynBusinessSeries2Redis2.get(getRedisClient(), seriesId);//缓存中取数据
			if (result != null )
				return result;
			
			BusinessSeries dbResult   = super.get(seriesId);//数据库中取数据
			if (dbResult != null ){
				SynBusinessSeries2Redis2.syn(getRedisClient(), dbResult);//同步到redis
				return dbResult;
			}else {
				result = new BusinessSeries();
				result.setSuccessful(false);
				result.addMessage("改业务系统:"+seriesId+",不存在("+this.getClass().getName()+")");
				return result;
			}
		}
		
		if (EEBeanUtils.isNULL(appId)) {//是否有传业务系统标识
			BusinessApp businessApp = SynBusinessApp2Redis.get(getRedisClient(), appId);//缓存中取数据
			
			if (businessApp != null && businessApp.getBusinessSeries()!= null  ) 
				return businessApp.getBusinessSeries();
			
			businessApp = businessAppBizService.retrieveApp(appId); //数据库中取数据
			
			
			if (businessApp !=null ) { ////同步到redis
				SynBusinessApp2Redis.syn(getRedisClient(), businessApp);
				if (businessApp.getBusinessSeries() == null) {
					result.setSuccessful(false);
					result.addMessage("改业务体系:"+seriesId+",不存在("+this.getClass().getName()+")");
					return result;
				}
				return businessApp.getBusinessSeries();
			}else {
				result.setSuccessful(false);
				result.addMessage("改业务体系:"+seriesId+",不存在("+this.getClass().getName()+")");
				return result;
			}
		}
		return result;
	}

	public RedisClient getRedisClient() {
		return RedisClient;
	}

	public void setRedisClient(RedisClient redisClient) {
		RedisClient = redisClient;
	}
	


}
