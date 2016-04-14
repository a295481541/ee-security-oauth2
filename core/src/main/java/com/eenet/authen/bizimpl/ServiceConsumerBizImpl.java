package com.eenet.authen.bizimpl;

import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.ServiceConsumerBizService;
import com.eenet.authen.cacheSyn.SynServiceConsumerToRedis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class ServiceConsumerBizImpl extends SimpleBizImpl implements ServiceConsumerBizService {
	private RedisClient redisClient;
	private RSAEncrypt redisRSAEncrypt;

	@Override
	public ServiceConsumer registeServiceConsumer(ServiceConsumer consumer) {
		ServiceConsumer result = null;
		/* 参数检查 */
		if (consumer == null) {
			result = new ServiceConsumer();
			result.setSuccessful(false);
			result.addMessage("要注册的服务消费者未知("+this.getClass().getName()+")");
			return result;
		}
		
		/* 秘钥加密 */
		try {
			String ciphertext = RSAUtil.encrypt(getRedisRSAEncrypt(), consumer.getSecretKey());
			if (EEBeanUtils.isNULL(ciphertext))
				throw new EncryptException("密文为空");
			consumer.setSecretKey(ciphertext);
		} catch (EncryptException e) {
			result = new ServiceConsumer();
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		
		/* 保存到数据库 */
		result = super.save(consumer);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynServiceConsumerToRedis.syn(getRedisClient(), consumer);
		
		result.setSecretKey(null);
		return result;
	}
	
	@Override
	public SimpleResponse removeServiceConsumer(String... code) {
		SimpleResponse result = null;
		/* 参数检查 */
		if (code == null || code.length==0) {
			result = new SimpleResponse();
			result.setSuccessful(false);
			result.addMessage("未指定要删除的服务消费者("+this.getClass().getName()+")");
			return result;
		}
		
		result = super.delete(ServiceConsumer.class,code);
		/* 删除成功，同时从缓存中删除 */
		if (result.isSuccessful())
			RemoveMapItemFromRedisThread.execute(getRedisClient(), code, CacheKey.SERVICE_CONSUMER);
		
		return result;
	}
	
	@Override
	public ServiceConsumer retrieveServiceConsumer(String code) {
		ServiceConsumer result = null;
		/* 参数检查 */
		if (EEBeanUtils.isNULL(code)) {
			result = new ServiceConsumer();
			result.setSuccessful(false);
			result.addMessage("未指定服务消费者编码");
			return result;
		}
		
		/* 从缓存取数据 */
		try {
			result = SynServiceConsumerToRedis.consumer(getRedisClient(), code);
		} catch (RedisOPException | ClassCastException e) {
			e.printStackTrace();//从缓存取数据失败，do nothing
		}
		
		/* 从数据库取数据 */
		if (result == null) {
			result = super.get(code, ServiceConsumer.class);
			/* 同步缓存 */
			if (result!=null && result.isSuccessful())
				SynServiceConsumerToRedis.syn(getRedisClient(), result);
		}
		
		/* 从数据库也取不到数据 */
		if (result == null) {
			result = new ServiceConsumer();
			result.setSuccessful(false);
			result.addMessage("未找到编码为："+code+"的服务消费者");
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
