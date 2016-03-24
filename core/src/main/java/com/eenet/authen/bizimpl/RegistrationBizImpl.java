package com.eenet.authen.bizimpl;

import com.eenet.authen.RegistrationBizService;
import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.SingleSignOnSystem;
import com.eenet.authen.cacheSyn.SynServiceConsumerToRedis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.common.cache.RedisClient;

public class RegistrationBizImpl extends SimpleBizImpl implements RegistrationBizService {
	private RedisClient redisClient;
	
	public ServiceConsumer serviceConsumerRegiste(ServiceConsumer consumer) {
		ServiceConsumer result = super.save(consumer);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynServiceConsumerToRedis.syn(getRedisClient(), consumer);
		
		result.setSecretKey(null);
		return result;
	}

	public SingleSignOnSystem singleSignOnSystemRegiste(SingleSignOnSystem client) {
		return null;
	}
	
	
	public SimpleResponse serviceConsumerDrop(String... code) {
		return null;
	}

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
}
