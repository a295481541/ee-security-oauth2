package com.eenet.authen.bizimpl;

import com.eenet.authen.ThirdPartySSOAPP;
import com.eenet.authen.ThirdPartySSOAppBizService;
import com.eenet.authen.cacheSyn.SynThirdPartySSOAPPToRedis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class ThirdPartySSOAppBizImpl extends SimpleBizImpl implements ThirdPartySSOAppBizService {
	private RedisClient redisClient;
	private RSAEncrypt redisRSAEncrypt;
	
	@Override
	public ThirdPartySSOAPP registeThirdPartySSOApp(ThirdPartySSOAPP app) {
		ThirdPartySSOAPP result = null;
		/* 参数检查 */
		if (app == null) {
			result = new ThirdPartySSOAPP();
			result.setSuccessful(false);
			result.addMessage("要注册的单点登录系统未知("+this.getClass().getName()+")");
			return result;
		}
		
		/* 秘钥加密 */
		try {
			String ciphertext = RSAUtil.encrypt(getRedisRSAEncrypt(), app.getSecretKey());
			if (EEBeanUtils.isNULL(ciphertext))
				throw new EncryptException("密文为空");
			app.setSecretKey(ciphertext);
		} catch (EncryptException e) {
			result = new ThirdPartySSOAPP();
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		
		/* 保存到数据库 */
		result = super.save(app);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynThirdPartySSOAPPToRedis.syn(getRedisClient(), app);
		
		result.setSecretKey(null);
		return result;
	}
	
	@Override
	public SimpleResponse removeThirdPartySSOApp(String... appIds) {
		SimpleResponse result = null;
		/* 参数检查 */
		if (appIds == null || appIds.length==0) {
			result = new SimpleResponse();
			result.setSuccessful(false);
			result.addMessage("要废弃得单点登录系统未知("+this.getClass().getName()+")");
			return result;
		}
		
		result = super.delete(ThirdPartySSOAPP.class,appIds);
		/* 删除成功，同时从缓存中删除 */
		if (result.isSuccessful())
			RemoveMapItemFromRedisThread.execute(getRedisClient(), appIds, CacheKey.SSO_APP);
		
		return result;
	}
	
	@Override
	public ThirdPartySSOAPP retrieveThirdPartySSOApp(String appId) {
		ThirdPartySSOAPP result = null;
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId)) {
			result = new ThirdPartySSOAPP();
			result.setSuccessful(false);
			result.addMessage("未指定单点登录系统的appId");
			return result;
		}
		
		/* 从缓存取数据 */
		try {
			result = SynThirdPartySSOAPPToRedis.ssoApp(getRedisClient(), appId);
		} catch (RedisOPException | ClassCastException e) {
			e.printStackTrace();
		}
		
		/* 从数据库取数据 */
		if (result == null) {
			result = super.get(appId, ThirdPartySSOAPP.class);
			/* 同步缓存 */
			if (result!=null && result.isSuccessful())
				SynThirdPartySSOAPPToRedis.syn(getRedisClient(), result);
		}
		
		/* 从数据库也取不到数据 */
		if (result == null) {
			result = new ThirdPartySSOAPP();
			result.setSuccessful(false);
			result.addMessage("未找到appId为："+appId+"的单点登录系统");
		}
		return result;
	}

	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	@Override
	public Class<?> getPojoCLS() {
		return null;
	}

	/**
	 * @return the redisClient
	 */
	public RedisClient getRedisClient() {
		return redisClient;
	}

	/**
	 * @param redisClient the redisClient to set
	 */
	public void setRedisClient(RedisClient redisClient) {
		this.redisClient = redisClient;
	}
	
	public RSAEncrypt getRedisRSAEncrypt() {
		return redisRSAEncrypt;
	}

	public void setRedisRSAEncrypt(RSAEncrypt redisRSAEncrypt) {
		this.redisRSAEncrypt = redisRSAEncrypt;
	}

}
