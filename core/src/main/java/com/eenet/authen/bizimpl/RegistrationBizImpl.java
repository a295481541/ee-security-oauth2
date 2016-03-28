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
	
	@Override
	public ServiceConsumer serviceConsumerRegiste(ServiceConsumer consumer) {
		System.out.println("==============================================================");
		System.out.println("==============================================================");
		System.out.println("==============================================================");
		System.out.println("==============================================================");
		System.out.println("==============================================================");
		System.out.println("==============================================================");
		
		ServiceConsumer result = super.save(consumer);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynServiceConsumerToRedis.syn(getRedisClient(), consumer);
		
		result.setSecretKey(null);
		return result;
	}
	
	@Override
	public SingleSignOnSystem singleSignOnSystemRegiste(SingleSignOnSystem client) {
		return null;
	}
	
	@Override
	public SimpleResponse serviceConsumerDrop(String... code) {
		SimpleResponse result = super.delete(ServiceConsumer.class,code);
		
		/* 删除成功，同时从缓存中删除 */
		if (result.isSuccessful())
			SynServiceConsumerToRedis.del(getRedisClient(), code);
		
		return result;
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

	@Override
	public boolean authenProviderPing() {
		return true;
	}
}
