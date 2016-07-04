package com.eenet.authen.bizimpl;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.cacheSyn.SynBusinessApp2Redis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.common.cache.RedisClient;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 业务系统服务实现逻辑
 * @author Orion
 *
 */
public class BusinessAppBizImpl extends SimpleBizImpl implements BusinessAppBizService {
	private RedisClient RedisClient;//Redis客户端
	private RSAEncrypt StorageRSAEncrypt;//数据存储解密私钥
	
	@Override
	public BusinessApp registeApp(BusinessApp app) {
		BusinessApp result = new BusinessApp();
		result.setSuccessful(true);
		/* 参数检查 */
		if (app==null) {
			result.setSuccessful(false);
			result.addMessage("要注册的单点登录系统未知("+this.getClass().getName()+")");
		} else if (EEBeanUtils.isNULL(app.getAppName()) || app.getAppType()==null || EEBeanUtils.isNULL(app.getSecretKey()) ) {
			result.setSuccessful(false);
			result.addMessage("要注册的单点登录系统参数不全，应用中文名称、应用类型、应用接入秘钥均不可为空("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		
		/* 秘钥加密 */
		try {
			String ciphertext = RSAUtil.encrypt(getStorageRSAEncrypt(), app.getSecretKey());
			if (EEBeanUtils.isNULL(ciphertext)) {
				result.setSuccessful(false);
				result.addMessage("加密业务系统接入秘钥失败("+this.getClass().getName()+")，错误原因未知");
			}
			app.setSecretKey(ciphertext);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage("加密业务系统接入秘钥失败("+this.getClass().getName()+")，错误原因：" + e.toString());
		}
		if (!result.isSuccessful())
			return result;
		
		/* 保存到数据库 */
		result = super.save(app);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynBusinessApp2Redis.syn(getRedisClient(), app);
		
		result.setSecretKey(null);
		return result;
	}

	@Override
	public SimpleResponse removeApp(String... appIds) {
		SimpleResponse result = null;
		/* 参数检查 */
		if (appIds == null || appIds.length==0) {
			result = new SimpleResponse();
			result.setSuccessful(false);
			result.addMessage("要废弃得单点登录系统未知("+this.getClass().getName()+")");
			return result;
		}
		
		result = super.delete(BusinessApp.class,appIds);
		/* 删除成功，同时从缓存中删除 */
		if (result.isSuccessful())
			SynBusinessApp2Redis.remove(RedisClient, appIds);
		
		return result;
	}

	@Override
	public BusinessApp retrieveApp(String appId) {
		BusinessApp result = null;
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId)) {
			result = new BusinessApp();
			result.setSuccessful(false);
			result.addMessage("未指定单点登录系统的appId");
			return result;
		}
		
		/* 从缓存取数据 */
		result = SynBusinessApp2Redis.get(getRedisClient(), appId);
		
		/* 从数据库取数据 */
		if (result==null || !result.isSuccessful()) {
			result = super.get(appId, BusinessApp.class);
			/* 同步缓存 */
			if (result!=null && result.isSuccessful())
				SynBusinessApp2Redis.syn(getRedisClient(), result);
		}
		
		/* 从数据库也取不到数据 */
		if (result==null) {
			result = new BusinessApp();
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
		return BusinessApp.class;
	}

	/**
	 * @return the Redis客户端
	 */
	public RedisClient getRedisClient() {
		return RedisClient;
	}

	/**
	 * @param redisClient the Redis客户端 to set
	 */
	public void setRedisClient(RedisClient redisClient) {
		RedisClient = redisClient;
	}

	/**
	 * @return the 数据存储解密私钥
	 */
	public RSAEncrypt getStorageRSAEncrypt() {
		return StorageRSAEncrypt;
	}

	/**
	 * @param storageRSAEncrypt the 数据存储解密私钥 to set
	 */
	public void setStorageRSAEncrypt(RSAEncrypt storageRSAEncrypt) {
		StorageRSAEncrypt = storageRSAEncrypt;
	}
	
}
