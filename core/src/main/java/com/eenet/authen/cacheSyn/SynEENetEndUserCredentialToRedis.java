package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.EENetEndUserCredential;
import com.eenet.authen.bizimpl.CacheKey;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

public class SynEENetEndUserCredentialToRedis {
	/**
	 * 将最终用户秘钥同步到Redis
	 * @param client
	 * @param credentials
	 * 2016年4月1日
	 * @author Orion
	 */
	public static void syn(RedisClient client, EENetEndUserCredential... credentials) {
		if (credentials == null || credentials.length == 0 || client == null)
			return;
		try {
			SynToRedis syn = new SynEENetEndUserCredentialToRedis().new SynToRedis(client, credentials);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 根据主账号获得秘钥
	 * @param client
	 * @param mainAccount
	 * @param redisRSADecrypt
	 * @return redisRSADecrypt为空时返回密文，redisRSADecrypt不为空是返回明文
	 * @throws RedisOPException
	 * @throws EncryptException
	 * 2016年4月3日
	 * @author Orion
	 * @throws ClassCastException 
	 */
	public static String secretKey(RedisClient client, String mainAccount, RSADecrypt redisRSADecrypt) throws RedisOPException, ClassCastException, EncryptException {
		if (client == null || mainAccount == null)
			return null;
		SecretKeyFromRedis get = null;
		if (redisRSADecrypt == null) {
			get = new SynEENetEndUserCredentialToRedis().new SecretKeyFromRedis(client);
			return get.ciphertext(mainAccount);
		} else {
			get = new SynEENetEndUserCredentialToRedis().new SecretKeyFromRedis(client,redisRSADecrypt);
			return get.plaintext(mainAccount);
		}
	}
	
	/**
	 * 根据主账号获得秘钥
	 * 2016年4月3日
	 * @author Orion
	 */
	private class SecretKeyFromRedis {
		private final RedisClient redisClient;
		private final RSADecrypt redisRSADecrypt;
		
		public String plaintext(String mainAccount) throws RedisOPException, EncryptException,ClassCastException{
			String plaintext = null;
			String ciphertext = this.ciphertext(mainAccount);
			if (ciphertext != null)
				plaintext = RSAUtil.decrypt(redisRSADecrypt, ciphertext);
			return plaintext;
		}
		
		public String ciphertext(String mainAccount) throws RedisOPException,ClassCastException {
			String secertKey = null;
			Object secertKeyObj = this.redisClient.getMapValue(CacheKey.ENDUSER_CREDENTIAL, mainAccount);
			if (secertKeyObj != null)
				secertKey = String.class.cast(secertKeyObj);
			return secertKey;
		}

		public SecretKeyFromRedis(RedisClient redisClient, RSADecrypt redisRSADecrypt) {
			super();
			this.redisClient = redisClient;
			this.redisRSADecrypt = redisRSADecrypt;
		}

		public SecretKeyFromRedis(RedisClient redisClient) {
			super();
			this.redisClient = redisClient;
			this.redisRSADecrypt = null;
		}
	}
	
	/**
	 * 将最终用户秘钥同步到Redis
	 * 2016年4月1日
	 * @author Orion
	 */
	private class SynToRedis implements Runnable {
		private RedisClient redisClient;
		private EENetEndUserCredential[] credentials;
		
		@Override
		public void run() {
			try {
				Map<String, String> map = new HashMap<String, String>();
				for (EENetEndUserCredential credential : this.credentials) {
					map.put(credential.getMainAccount().getAccount(), credential.getSecretKey());
					this.redisClient.addMapItem(CacheKey.ENDUSER_CREDENTIAL, map, -1);
				}
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}

		public SynToRedis(RedisClient redisClient, EENetEndUserCredential[] credentials) throws Exception {
			this.credentials = new EENetEndUserCredential[credentials.length];
			for (int i=0;i<this.credentials.length;i++) {
				EENetEndUserCredential dest = new EENetEndUserCredential();
				EEBeanUtils.coverProperties(dest, credentials[i]);
				this.credentials[i] = dest;
			}
			this.redisClient = redisClient;
		}
	}
	
	private SynEENetEndUserCredentialToRedis(){}
}
