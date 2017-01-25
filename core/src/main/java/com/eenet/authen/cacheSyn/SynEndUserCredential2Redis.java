package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eenet.SecurityCacheKey;
import com.eenet.authen.EndUserCredential;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 最终用户登录密码在Redis中的操作thread safe
 * 数据格式，redisKey = ENDUSER_CREDENTIAL, mapKey = [seriesId]:[endUserId], value = [EncryptionType]:[最终用户登录账号对象(@see com.eenet.authen.EndUserLoginAccount)]
 * @author Orion
 * 2016年6月9日
 */
public final class SynEndUserCredential2Redis {
	private static final Logger log = LoggerFactory.getLogger(SynEndUserCredential2Redis.class);
	
	/**
	 * 
	 * @param client
	 * @param credentials
	 * 2017年1月25日
	 * @author Orion
	 */
	public static void syn(final RedisClient client, final EndUserCredential... credentials) {
		if (credentials == null || credentials.length == 0 || client == null)
			return;
		try {
			ToRedis syn = new SynEndUserCredential2Redis().new ToRedis(client, credentials);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			log.error("end user credential syn to redis error! exception info: "+e.getMessage());
		}
	}
	
	/**
	 * 根据最终用户标识获得最终用户登录密码
	 * @param client
	 * @param seriesId 体系标识
	 * @param userId 最终用户标识
	 * @return 已加密的密码
	 * 2016年6月9日
	 * @author Orion
	 */
	public static String get(final RedisClient client, final String seriesId, final String userId) {
		if (EEBeanUtils.isNULL(seriesId) || EEBeanUtils.isNULL(userId) || client == null)
			return null;
		
		String password = null;
		try {
			password = String.class.cast(client.getMapValue(SecurityCacheKey.ENDUSER_CREDENTIAL, seriesId+":"+userId));
		} catch (RedisOPException e) {
			log.error("can not get end user Credential from redis, cacheKey: " + SecurityCacheKey.ENDUSER_CREDENTIAL
					+ ",  mapKey: " + seriesId + ":" + userId + ", exception info: " + e.getMessage());
		}
		
		return password;
	}
	
	/**
	 * 将最终用户登录秘钥从Redis移除
	 * @param client
	 * @param seriesId 体系标识
	 * @param userIds 最终用户标识
	 * 2016年6月9日
	 * @author Orion
	 */
	public static void remove(final RedisClient client, final String seriesId, final String[] userIds) {
		if (userIds==null || userIds.length==0 || EEBeanUtils.isNULL(seriesId))
			return;
		
		String[] mapKeys = new String[userIds.length];
		for (int i=0;i<userIds.length;i++)
			mapKeys[i] = seriesId+":"+userIds[i];
		
		log.info("end user credential remove from redis, cacheKey: " + SecurityCacheKey.ENDUSER_CREDENTIAL + ",  map data: " + EEBeanUtils.object2Json(mapKeys));
		RemoveMapItemFromRedisThread.execute(client, userIds, SecurityCacheKey.ENDUSER_CREDENTIAL);
	}
	
	/**
	 * 将最终用户登录密码同步到Redis
	 * @author Orion
	 * 2016年6月9日
	 */
	private class ToRedis implements Runnable {
		private final RedisClient redisClient;
		private final EndUserCredential[] credentials;
		
		public ToRedis(RedisClient redisClient, EndUserCredential[] credentials) throws Exception {
			this.credentials = new EndUserCredential[credentials.length];
			for (int i=0;i<this.credentials.length;i++) {
				EndUserCredential dest = new EndUserCredential();
				EEBeanUtils.coverProperties(dest, credentials[i]);
				this.credentials[i] = dest;
			}
			this.redisClient = redisClient;
		}
		
		@Override
		public void run() {
			try {
				Map<String, String> map = new HashMap<String, String>();
				
				for (EndUserCredential credential : this.credentials)
					map.put(credential.getBusinessSeries().getAtid()+":"+credential.getEndUser().getAtid(), credential.getEncryptionType() +"##"+credential.getPassword());
				
				log.info("end user credential save to redis, cacheKey :" + SecurityCacheKey.ENDUSER_CREDENTIAL + ",  value :" +EEBeanUtils.object2Json(map));
				this.redisClient.addMapItem(SecurityCacheKey.ENDUSER_CREDENTIAL, map, -1);
			} catch (RedisOPException e) {
				log.error("end user credential save to redis error! exception info: "+e.getMessage());
			} catch (Exception e) {
				log.error("end user credential save to redis error! exception info: "+e.getMessage());
			}
		}
	}
}
