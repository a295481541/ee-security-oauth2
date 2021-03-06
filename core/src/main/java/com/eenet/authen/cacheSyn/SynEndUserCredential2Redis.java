package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.EndUserCredential;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 最终用户登录密码在Redis中的操作thread safe
 * @author Orion
 * 2016年6月9日
 */
public final class SynEndUserCredential2Redis {
	
	public static void syn(final RedisClient client, final EndUserCredential... credentials) {
		if (credentials == null || credentials.length == 0 || client == null)
			return;
		try {
			ToRedis syn = new SynEndUserCredential2Redis().new ToRedis(client, credentials);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 根据最终用户标识获得最终用户登录密码
	 * @param client
	 * @param userId 最终用户标识
	 * @return 已加密的密码
	 * 2016年6月9日
	 * @author Orion
	 */
	public static String get(final RedisClient client, final String userId) {
		if (EEBeanUtils.isNULL(userId) || client == null)
			return null;
		
		String password = null;
		try {
			password = String.class.cast(client.getMapValue(AuthenCacheKey.ENDUSER_CREDENTIAL, userId));
		} catch (RedisOPException e) {
			e.printStackTrace();//此处应该有log
		}
		
		return password;
	}
	
	/**
	 * 将最终用户登录秘钥从Redis移除
	 * @param client
	 * @param userIds 最终用户标识
	 * 2016年6月9日
	 * @author Orion
	 */
	public static void remove(final RedisClient client, final String[] userIds) {
		RemoveMapItemFromRedisThread.execute(client, userIds, AuthenCacheKey.ENDUSER_CREDENTIAL);
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
				for (EndUserCredential credential : this.credentials) {
					map.put(credential.getEndUser().getAtid(), credential.getEncryptionType() +"##"+credential.getPassword());
					this.redisClient.addMapItem(AuthenCacheKey.ENDUSER_CREDENTIAL, map, -1);
				}
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}
	}
}
