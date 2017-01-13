package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessSeries;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 业务体系在Redis中的操作thread safe
 * @author koop
 * 2017年1月10日
 */
public final class SynBusinessSeries2Redis2 {
	
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
			ToRedis syn = new SynBusinessSeries2Redis2().new ToRedis(client, businessSeries);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 根据app id获得业务体系
	 * @param client
	 * @param appId
	 * @return redis中没有指定的对象或发生错误均返回null
	 * 2017年1月10日
	 * @author koop
	 */
	public static BusinessSeries get(final RedisClient client, final String seriesId) {
		if (EEBeanUtils.isNULL(seriesId) || client == null)
			return null;
		
		BusinessSeries bizApp = null;
		try {
			bizApp = BusinessSeries.class.cast(client.getMapValue(AuthenCacheKey.BIZ_SERIES, seriesId));
		} catch (Exception e) {
			e.printStackTrace();//此处应该有log
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
		RemoveMapItemFromRedisThread.execute(client, seriesIds, AuthenCacheKey.BIZ_SERIES);
	}
	
	/**
	 * 将业务体系同步到Redis
	 * @author koop
	 * 2017年1月10日
	 */
	private class ToRedis implements Runnable {
		private final RedisClient redisClient;
		private final BusinessSeries[] bizApp;
		
		public ToRedis(RedisClient redisClient, BusinessSeries[] bizApp) throws Exception {
			this.bizApp = new BusinessSeries[bizApp.length];
			for (int i=0;i<this.bizApp.length;i++) {
				BusinessSeries dest = new BusinessSeries();
				EEBeanUtils.coverProperties(dest, bizApp[i]);
				this.bizApp[i] = dest;
			}
			this.redisClient = redisClient;
		}
		
		@Override
		public void run() {
			try {
				Map<String, BusinessSeries> map = new HashMap<String, BusinessSeries>();
				for (BusinessSeries ssoapp : this.bizApp) {
					map.put(ssoapp.getAtid(), ssoapp);
					this.redisClient.addMapItem(AuthenCacheKey.BIZ_SERIES, map, -1);
				}
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}
		
	}
	
	private SynBusinessSeries2Redis2() {}
}
