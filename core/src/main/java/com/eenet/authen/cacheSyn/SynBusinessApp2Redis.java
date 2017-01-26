package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eenet.SecurityCacheKey;
import com.eenet.authen.BusinessApp;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 业务系统在Redis中的操作thread safe
 * 数据格式，redisKey:BIZ_APP, mapKey:appId，value:业务系统对象(@see com.eenet.authen.BusinessApp)
 * @author Orion
 * 2016年6月6日
 */
public final class SynBusinessApp2Redis {
	private static final Logger log = LoggerFactory.getLogger(SynBusinessApp2Redis.class);
	
	/**
	 * 将业务应用系统同步到Redis
	 * @param client redis客户端
	 * @param bizApps 要同步的业务应用系统对象
	 * 2016年6月6日
	 * @author Orion
	 */
	public static void syn(final RedisClient client, final BusinessApp... bizApps) {
		if (bizApps == null || bizApps.length == 0 || client == null)
			return;
		try {
			ToRedis syn = new SynBusinessApp2Redis().new ToRedis(client, bizApps);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			log.error("business app syn to redis error! exception info: "+e.getMessage());
		}
	}
	
	/**
	 * 根据app id获得业务应用系统
	 * @param client
	 * @param appId
	 * @return redis中没有指定的对象或发生错误均返回null
	 * 2016年6月6日
	 * @author Orion
	 */
	public static BusinessApp get(final RedisClient client, final String appId) {
		if (EEBeanUtils.isNULL(appId) || client == null)
			return null;
		
		BusinessApp bizApp = null;
		try {
			bizApp = BusinessApp.class.cast(client.getMapValue(SecurityCacheKey.BIZ_APP, appId));
		} catch (Exception e) {
			log.error("can not get business app from redis,  cacheKey: " + SecurityCacheKey.BIZ_APP + ",mapKey: "
					+ appId + ", exception info: " + e.getMessage());
		}
		return bizApp;
	}
	
	/**
	 * 将业务系统从Redis移除
	 * @param client
	 * @param appIds
	 * 2016年6月6日
	 * @author Orion
	 */
	public static void remove(final RedisClient client, final String[] appIds) {
		log.info("business app remove from redis, cacheKey: " + SecurityCacheKey.BIZ_APP + ",  map data: " + EEBeanUtils.object2Json(appIds));
		RemoveMapItemFromRedisThread.execute(client, appIds, SecurityCacheKey.BIZ_APP);
	}
	
	/**
	 * 将业务应用系统同步到Redis
	 * @author Orion
	 * 2016年6月6日
	 */
	private class ToRedis implements Runnable {
		private final RedisClient redisClient;
		private final BusinessApp[] bizApp;
		
		public ToRedis(RedisClient redisClient, BusinessApp[] bizApp) throws Exception {
			this.bizApp = new BusinessApp[bizApp.length];
			for (int i=0;i<this.bizApp.length;i++) {
				BusinessApp dest = new BusinessApp();
				EEBeanUtils.coverProperties(dest, bizApp[i]);
				this.bizApp[i] = dest;
			}
			this.redisClient = redisClient;
		}
		
		@Override
		public void run() {
			try {
				Map<String, BusinessApp> map = new HashMap<String, BusinessApp>();
				
				for (BusinessApp ssoapp : this.bizApp)
					map.put(ssoapp.getAppId(), ssoapp);
				
				log.info("business app save to redis, cacheKey :" + SecurityCacheKey.BIZ_APP + ",  value :" +EEBeanUtils.object2Json(map));
				this.redisClient.addMapItem(SecurityCacheKey.BIZ_APP, map, -1);
			} catch (RedisOPException e) {
				log.error("business app save to redis error! exception info: "+e.getMessage());
			} catch (Exception e) {
				log.error("business app save to redis error! exception info: "+e.getMessage());
			}
		}
		
	}
	
	private SynBusinessApp2Redis() {}
}
