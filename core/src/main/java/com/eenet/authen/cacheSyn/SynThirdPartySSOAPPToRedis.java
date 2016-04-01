package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.ThirdPartySSOAPP;
import com.eenet.authen.bizimpl.CacheKey;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;

/**
 * 第三方单点登录系统在Redis中的操作 thread safe
 * 2016年4月1日
 * @author Orion
 */
public final class SynThirdPartySSOAPPToRedis {
	
	/**
	 * 将单点登录系统同步到Redis
	 * @param client
	 * @param apps
	 * 2016年4月1日
	 * @author Orion
	 */
	public static void syn(RedisClient client, ThirdPartySSOAPP... ssoapps) {
		if (ssoapps == null || ssoapps.length == 0 || client == null)
			return;
		try {
			SynToRedis syn = new SynThirdPartySSOAPPToRedis().new SynToRedis(client, ssoapps);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	
	/**
	 * 将单点登录系统同步到Redis
	 * 2016年4月1日
	 * @author Orion
	 */
	private class SynToRedis implements Runnable {
		private RedisClient redisClient;
		private ThirdPartySSOAPP[] ssoapp;
		
		@Override
		public void run() {
			try {
				Map<String, ThirdPartySSOAPP> map = new HashMap<String, ThirdPartySSOAPP>();
				for (ThirdPartySSOAPP ssoapp : this.ssoapp) {
					map.put(ssoapp.getAppId(), ssoapp);
					this.redisClient.addMapItem(CacheKey.SSO_APP, map, -1);
				}
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}

		public SynToRedis(RedisClient redisClient, ThirdPartySSOAPP[] ssoapp) throws Exception {
			this.ssoapp = new ThirdPartySSOAPP[ssoapp.length];
			for (int i=0;i<this.ssoapp.length;i++) {
				ThirdPartySSOAPP dest = new ThirdPartySSOAPP();
				EEBeanUtils.coverProperties(dest, ssoapp[i]);
				this.ssoapp[i] = dest;
			}
			this.redisClient = redisClient;
		}
	}
	
	private SynThirdPartySSOAPPToRedis() {}
}
