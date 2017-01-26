package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eenet.SecurityCacheKey;
import com.eenet.authen.BusinessSeries;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 业务体系在Redis中的操作thread safe
 * 数据格式，redisKey:BIZ_SERIES, mapKey:seriesId，value:业务体系对象(@see com.eenet.authen.BusinessSeries)
 * @author koop
 * 2017年1月10日
 */
public final class SynBusinessSeries2Redis {
	private static final Logger log = LoggerFactory.getLogger(SynBusinessSeries2Redis.class);
	/**
	 * 将业务体系同步到Redis
	 * @param client redis客户端
	 * @param bizApps 要同步的业务体系对象
	 * 2017年1月10日
	 * @author koop
	 */
	public static void syn(final RedisClient client, final BusinessSeries... businessSeries) {
		if (businessSeries == null || businessSeries.length == 0 || client == null)
			return;
		try {
			ToRedis syn = new SynBusinessSeries2Redis().new ToRedis(client, businessSeries);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			log.error("business series syn to redis error! exception info: "+e.getMessage());
		}
	}
	
	/**
	 * 根据体系id获得业务体系对象
	 * @param client
	 * @param seriesId
	 * @return redis中没有指定的对象或发生错误均返回null
	 * 2017年1月10日
	 * @author koop
	 */
	public static BusinessSeries get(final RedisClient client, final String seriesId) {
		if (EEBeanUtils.isNULL(seriesId) || client == null)
			return null;
		
		BusinessSeries bizApp = null;
		try {
			bizApp = BusinessSeries.class.cast(client.getMapValue(SecurityCacheKey.BIZ_SERIES, seriesId));
		} catch (Exception e) {
			log.error("can not get business series from redis,  cacheKey: " + SecurityCacheKey.BIZ_SERIES + ",mapKey: "
					+ seriesId + ", exception info: " + e.getMessage());
		}
		return bizApp;
	}
	
	/**
	 * 将业务体系从Redis移除
	 * @param client
	 * @param appIds
	 * 2017年1月10日
	 * @author koop
	 */
	public static void remove(final RedisClient client, final String[] seriesIds) {
		log.info("business series remove from redis, cacheKey: " + SecurityCacheKey.BIZ_SERIES + ",  map data: " + EEBeanUtils.object2Json(seriesIds));
		RemoveMapItemFromRedisThread.execute(client, seriesIds, SecurityCacheKey.BIZ_SERIES);
	}
	
	/**
	 * 将业务体系同步到Redis
	 * @author koop
	 * 2017年1月10日
	 */
	private class ToRedis implements Runnable {
		private final RedisClient redisClient;
		private final BusinessSeries[] bizSeries;
		
		public ToRedis(RedisClient redisClient, BusinessSeries[] bizSeries) throws Exception {
			this.bizSeries = new BusinessSeries[bizSeries.length];
			for (int i=0;i<this.bizSeries.length;i++) {
				BusinessSeries dest = new BusinessSeries();
				EEBeanUtils.coverProperties(dest, bizSeries[i]);
				this.bizSeries[i] = dest;
			}
			this.redisClient = redisClient;
		}
		
		@Override
		public void run() {
			try {
				Map<String, BusinessSeries> map = new HashMap<String, BusinessSeries>();
				
				for (BusinessSeries series : this.bizSeries)
					map.put(series.getAtid(), series);
				
				log.info("business series save to redis, cacheKey :" + SecurityCacheKey.BIZ_SERIES + ",  value :" +EEBeanUtils.object2Json(map));
				this.redisClient.addMapItem(SecurityCacheKey.BIZ_SERIES, map, -1);
			} catch (RedisOPException e) {
				log.error("business app save to redis error! exception info: "+e.getMessage());
			} catch (Exception e) {
				log.error("business app save to redis error! exception info: "+e.getMessage());
			}
		}
		
	}
	
	private SynBusinessSeries2Redis() {}
}
