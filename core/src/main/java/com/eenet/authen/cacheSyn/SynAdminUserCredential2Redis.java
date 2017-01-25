package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.SecurityCacheKey;
import com.eenet.authen.AdminUserCredential;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 服务人员登录密码在Redis中的操作thread safe
 * @author Orion
 * 2016年6月8日
 */
public final class SynAdminUserCredential2Redis {
	
	/**
	 * 将服务人员登录密码同步到Redis
	 * @param client
	 * @param credentials
	 * 2016年6月8日
	 * @author Orion
	 */
	public static void syn(final RedisClient client, final AdminUserCredential... credentials) {
		if (credentials == null || credentials.length == 0 || client == null)
			return;
		try {
			ToRedis syn = new SynAdminUserCredential2Redis().new ToRedis(client, credentials);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 根据服务人员标识获得服务人员登录密码
	 * @param client
	 * @param userId 服务人员标识
	 * @return 已加密的密码
	 * 2016年6月8日
	 * @author Orion
	 */
	public static String get(final RedisClient client, final String userId) {
		if (EEBeanUtils.isNULL(userId) || client == null)
			return null;
		
		String password = null;
		try {
			System.out.println("SynAdminUserCredential2Redis get:  cacheKey" + SecurityCacheKey.ADMINUSER_CREDENTIAL +"mapKey :"+userId +"value :" +EEBeanUtils.object2Json(client.getMapValue(SecurityCacheKey.ADMINUSER_CREDENTIAL, userId)));
			password = String.class.cast(client.getMapValue(SecurityCacheKey.ADMINUSER_CREDENTIAL, userId));
		} catch (RedisOPException e) {
			e.printStackTrace();//此处应该有log
		}
		
		return password;
	}
	
	/**
	 * 将服务人员登录秘钥从Redis移除
	 * @param client
	 * @param userIds 服务人员标识
	 * 2016年6月8日
	 * @author Orion
	 */
	public static void remove(final RedisClient client, final String[] userIds) {
		RemoveMapItemFromRedisThread.execute(client, userIds, SecurityCacheKey.ADMINUSER_CREDENTIAL);
	}
	
	/**
	 * 将服务人员登录密码同步到Redis
	 * @author Orion
	 * 2016年6月8日
	 */
	private class ToRedis implements Runnable {
		private final RedisClient redisClient;
		private final AdminUserCredential[] credentials;
		
		public ToRedis(RedisClient redisClient, AdminUserCredential[] credentials) throws Exception {
			this.credentials = new AdminUserCredential[credentials.length];
			for (int i=0;i<this.credentials.length;i++) {
				AdminUserCredential dest = new AdminUserCredential();
				EEBeanUtils.coverProperties(dest, credentials[i]);
				this.credentials[i] = dest;
			}
			this.redisClient = redisClient;
		}
		
		@Override
		public void run() {
			try {
				Map<String, String> map = new HashMap<String, String>();
				for (AdminUserCredential credential : this.credentials) {
					map.put(credential.getAdminUser().getAtid(), credential.getEncryptionType()+"##"+credential.getPassword());
					this.redisClient.addMapItem(SecurityCacheKey.ADMINUSER_CREDENTIAL, map, -1);
					System.out.println("SynAdminUserCredential2Redis set:  cacheKey :" + SecurityCacheKey.ADMINUSER_CREDENTIAL +"  value :" +EEBeanUtils.object2Json(map));

				}
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}
		
	}
}
