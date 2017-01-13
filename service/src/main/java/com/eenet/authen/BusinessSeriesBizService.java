package com.eenet.authen;


/**
 * 业务体系服务
 * 2017年1月10日
 * @author koop
 */
public interface BusinessSeriesBizService {
	
	/**
	 * 获取业务体系（appid 优先）
	 * @param seriesId 业务体系标识   appid 业务系统标识
	 * @return
	 * 2016年3月30日
	 * @author Orion
	 */
	public BusinessSeries retrieveBusinessSeries(String seriesId,String appId);
	
	
}
