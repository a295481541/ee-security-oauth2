package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

/**
 * 服务人员登录账号在Redis中的操作thread safe
 * @author Orion
 * 2016年6月7日
 */
public class SynAdminUserLoginAccount2Redis {
	
	/**
	 * 将服务人员登录账号同步到Redis
	 * @param client
	 * @param accounts
	 * 2016年6月7日
	 * @author Orion
	 */
	public static void syn(final RedisClient client, final AdminUserLoginAccount... accounts) {
		if (accounts == null || accounts.length == 0 || client == null)
			return;
		try {
			ToRedis syn = new SynAdminUserLoginAccount2Redis().new ToRedis(client, accounts);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 根据登录账号获得服务人员登录账号对象
	 * @param client
	 * @param loginAccount
	 * @return redis中没有指定的对象或发生错误均返回null
	 * 2016年6月7日
	 * @author Orion
	 */
	public static AdminUserLoginAccount get(final RedisClient client, final String loginAccount) {
		if (EEBeanUtils.isNULL(loginAccount) || client == null)
			return null;
		
		AdminUserLoginAccount account = null;
		try {
			System.out.println("SynAdminUserLoginAccount2Redis get:  cacheKey" + AuthenCacheKey.ADMINUSER_LOGIN_ACCOUNT +"mapKey :"+loginAccount +"value :" +EEBeanUtils.object2Json(client.getMapValue(AuthenCacheKey.ADMINUSER_LOGIN_ACCOUNT, loginAccount)));

			account = AdminUserLoginAccount.class.cast(client.getMapValue(AuthenCacheKey.ADMINUSER_LOGIN_ACCOUNT, loginAccount));
		} catch (Exception e) {
			e.printStackTrace();//此处应该有log
		}
		return account;
	}
	
	/**
	 * 将服务人员登录账号从Redis移除
	 * @param client
	 * @param accounts
	 * 2016年6月7日
	 * @author Orion
	 */
	public static void remove(final RedisClient client, final String[] accounts) {
		RemoveMapItemFromRedisThread.execute(client, accounts, AuthenCacheKey.ADMINUSER_LOGIN_ACCOUNT);
	}
	
	/**
	 * 将服务人员登录账户同步到Redis
	 * @author Orion
	 * 2016年6月7日
	 */
	private class ToRedis implements Runnable {
		private final RedisClient redisClient;
		private final AdminUserLoginAccount[] accounts;
		
		public ToRedis(RedisClient redisClient, AdminUserLoginAccount[] accounts) throws Exception {
			this.accounts = new AdminUserLoginAccount[accounts.length];
			for (int i=0;i<this.accounts.length;i++) {
				AdminUserLoginAccount dest = new AdminUserLoginAccount();
				EEBeanUtils.coverProperties(dest, accounts[i]);
				this.accounts[i] = dest;
			}
			this.redisClient = redisClient;
		}
		@Override
		public void run() {
			try {
				Map<String, AdminUserLoginAccount> map = new HashMap<String, AdminUserLoginAccount>();
				for (AdminUserLoginAccount account : this.accounts) {
					map.put(account.getLoginAccount(), account);
					this.redisClient.addMapItem(AuthenCacheKey.ADMINUSER_LOGIN_ACCOUNT, map, -1);
					System.out.println("SynAdminUserLoginAccount2Redis set:  cacheKey :" + AuthenCacheKey.ADMINUSER_LOGIN_ACCOUNT +"  value :" +EEBeanUtils.object2Json(map));

				}
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}
		
	}
}
