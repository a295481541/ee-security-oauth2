package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.EENetEndUserLoginAccount;
import com.eenet.authen.bizimpl.CacheKey;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;

public final class SynEENetEndUserLoginAccountToRedis {
	
	/**
	 * 将用户登录账号同步到Redis
	 * @param client
	 * @param accounts
	 * 2016年4月1日
	 * @author Orion
	 */
	public static void syn(RedisClient client, EENetEndUserLoginAccount... accounts) {
		if (accounts == null || accounts.length == 0 || client == null)
			return;
		try {
			SynToRedis syn = new SynEENetEndUserLoginAccountToRedis().new SynToRedis(client, accounts);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 获得最终用户主账号
	 * @param client
	 * @param loginAccount
	 * @return
	 * @throws RedisOPException
	 * 2016年4月3日
	 * @author Orion
	 * @throws ClassCastException 
	 */
	public static String mainAccount(RedisClient client, String loginAccount) throws RedisOPException, ClassCastException {
		if (client == null || EEBeanUtils.isNULL(loginAccount))
			return null;
		return new SynEENetEndUserLoginAccountToRedis().new MainAccountFromRedis(client).mainAccount(loginAccount);
	}
	
	/**
	 * 获得最终用户主账号
	 * 2016年4月3日
	 * @author Orion
	 */
	private class MainAccountFromRedis {
		private final RedisClient redisClient;
		
		public String mainAccount(String loginAccount) throws RedisOPException,ClassCastException {
			String mainAccount = null;
			Object mainAccountObj = this.redisClient.getMapValue(CacheKey.ENDUSER_LOGIN_ACCOUNT, loginAccount);
			if (mainAccountObj != null)
				mainAccount = String.class.cast(mainAccountObj);
			return mainAccount;
		}

		public MainAccountFromRedis(RedisClient redisClient) {
			super();
			this.redisClient = redisClient;
		}
	}
	
	/**
	 * 将用户登录账号同步到Redis
	 * 2016年4月1日
	 * @author Orion
	 */
	private class SynToRedis implements Runnable {
		private RedisClient redisClient;
		private EENetEndUserLoginAccount[] accounts;
		
		@Override
		public void run() {
			try {
				Map<String, String> map = new HashMap<String, String>();
				for (EENetEndUserLoginAccount account : this.accounts) {
					map.put(account.getLoginAccount(), account.getMainAccount().getAccount());
					this.redisClient.addMapItem(CacheKey.ENDUSER_LOGIN_ACCOUNT, map, -1);
				}
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}

		public SynToRedis(RedisClient redisClient, EENetEndUserLoginAccount[] accounts) throws Exception {
			this.accounts = new EENetEndUserLoginAccount[accounts.length];
			for (int i=0;i<this.accounts.length;i++) {
				EENetEndUserLoginAccount dest = new EENetEndUserLoginAccount();
				EEBeanUtils.coverProperties(dest, accounts[i]);
				this.accounts[i] = dest;
			}
			this.redisClient = redisClient;
		}
	}
	
	
	private SynEENetEndUserLoginAccountToRedis(){}
}
