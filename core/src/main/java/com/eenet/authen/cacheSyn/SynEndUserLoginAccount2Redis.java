package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eenet.SecurityCacheKey;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 最终用户登录账号在Redis中的操作thread safe
 * 数据格式，redisKey = ENDUSER_LOGIN_ACCOUNT, mapKey = [seriesId]:[loginAccount], value = 最终用户登录账号对象(@see com.eenet.authen.EndUserLoginAccount)
 * @author Orion
 * 2016年6月7日
 */
public final class SynEndUserLoginAccount2Redis {
	private static final Logger log = LoggerFactory.getLogger(SynEndUserLoginAccount2Redis.class);
	/**
	 * 将最终用户登录账号同步到Redis
	 * @param client
	 * @param accounts
	 * 2016年6月7日
	 * @author Orion
	 */
	public static void syn(final RedisClient client, final EndUserLoginAccount... accounts) {
		if (accounts == null || accounts.length == 0 || client == null)
			return;
		try {
			ToRedis syn = new SynEndUserLoginAccount2Redis().new ToRedis(client, accounts);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			log.error("end user login account syn to redis error! exception info: "+e.getMessage());
		}
	}
	
	/**
	 * 根据登录账号获得最终用户登录账号对象
	 * @param client
	 * @param seriesId 体系标识
	 * @param loginAccount 登录账号
	 * @return
	 * 2016年6月7日
	 * @author Orion
	 */
	public static EndUserLoginAccount get(final RedisClient client, final String seriesId, final String loginAccount) {
		if (EEBeanUtils.isNULL(seriesId) || EEBeanUtils.isNULL(loginAccount) || client == null)
			return null;
		
		EndUserLoginAccount account = null;
		try {
			account = EndUserLoginAccount.class.cast(client.getMapValue(SecurityCacheKey.ENDUSER_LOGIN_ACCOUNT, seriesId+":"+loginAccount));
		} catch (Exception e) {
			log.error("can not get end user login account from redis, cacheKey: " + SecurityCacheKey.ENDUSER_LOGIN_ACCOUNT
							+ ",  mapKey: " + seriesId + ":" + loginAccount + ", exception info: " + e.getMessage());
		}
		return account;
	}
	
	/**
	 * 将最终用户登录账号从Redis移除
	 * 只能批量删除统一体系内的账号
	 * @param client
	 * @param seriesId 体系标识
	 * @param accounts 登录账号
	 * 2016年6月7日
	 * @author Orion
	 */
	public static void remove(final RedisClient client, final String seriesId, final String[] accounts) {
		if (accounts==null || accounts.length==0 || EEBeanUtils.isNULL(seriesId))
			return;
		
		String[] mapKeys = new String[accounts.length];
		for (int i=0;i<accounts.length;i++)
			mapKeys[i] = seriesId+":"+accounts[i];
		
		log.info("end user login account remove from redis, cacheKey: " + SecurityCacheKey.ENDUSER_LOGIN_ACCOUNT + ",  map data: " + EEBeanUtils.object2Json(mapKeys));
		RemoveMapItemFromRedisThread.execute(client, mapKeys, SecurityCacheKey.ENDUSER_LOGIN_ACCOUNT);
	}
	
	/**
	 * 将最终用户登录账号同步到Redis
	 * @author Orion
	 * 2016年6月7日
	 */
	private class ToRedis implements Runnable {
		private final RedisClient redisClient;
		private final EndUserLoginAccount[] accounts;
		
		public ToRedis(RedisClient redisClient, EndUserLoginAccount[] accounts) throws Exception {
			this.accounts = new EndUserLoginAccount[accounts.length];
			for (int i=0;i<this.accounts.length;i++) {
				EndUserLoginAccount dest = new EndUserLoginAccount();
				EEBeanUtils.coverProperties(dest, accounts[i]);
				this.accounts[i] = dest;
			}
			this.redisClient = redisClient;
		}
		@Override
		public void run() {
			try {
				Map<String, EndUserLoginAccount> map = new HashMap<String, EndUserLoginAccount>();
				
				for (EndUserLoginAccount account : this.accounts)
					map.put(account.getBusinessSeries().getAtid() + ":" + account.getLoginAccount(), account);
				
				log.info("end user login account save to redis, cacheKey :" + SecurityCacheKey.ENDUSER_LOGIN_ACCOUNT + ",  value :" +EEBeanUtils.object2Json(map));
				this.redisClient.addMapItem(SecurityCacheKey.ENDUSER_LOGIN_ACCOUNT, map, -1);
			} catch (RedisOPException e) {
				log.error("end user login account save to redis error! exception info: "+e.getMessage());
			} catch (Exception e) {
				log.error("end user login account save to redis error! exception info: "+e.getMessage());
			}
		}
	}
}
