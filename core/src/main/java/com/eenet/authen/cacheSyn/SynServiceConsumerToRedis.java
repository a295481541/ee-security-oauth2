package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.bizimpl.CacheKey;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;

/**
 * 服务消费者在Redis中的操作 thread safe
 * 
 * @author Orion
 *
 */
public final class SynServiceConsumerToRedis {
	/**
	 * 服务消费者同步到Redis
	 * 
	 * @param client redis客户端
	 * @param consumers 要同步的数据
	 */
	public static void syn(RedisClient client, ServiceConsumer... consumers) {
		if (consumers == null || consumers.length == 0 || client == null)
			return;
		try {
			SynToRedis syn = new SynServiceConsumerToRedis().new SynToRedis(client, consumers);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	public static ServiceConsumer consumer(RedisClient client, String code) throws RedisOPException, ClassCastException {
		if (client == null || EEBeanUtils.isNULL(code))
			return null;
		return new SynServiceConsumerToRedis().new ServiceConsumerFromRedis(client).consumer(code);
	}
	
	/**
	 * 根据编码获得服务消费者对象
	 * 2016年4月3日
	 * @author Orion
	 */
	private class ServiceConsumerFromRedis {
		private final RedisClient redisClient;
		
		public ServiceConsumer consumer(String code) throws RedisOPException,ClassCastException {
			ServiceConsumer consumer = null;
			Object consumerObj = this.redisClient.getMapValue(CacheKey.SERVICE_CONSUMER, code);
			if (consumerObj != null)
				consumer = ServiceConsumer.class.cast(consumerObj);
			return consumer;
		}
		
		public ServiceConsumerFromRedis(RedisClient redisClient) {
			super();
			this.redisClient = redisClient;
		}
	}
	
	/**
	 * 将消费者数据同步到Redis
	 * 
	 * 2016年3月28日
	 * @author Orion
	 */
	private class SynToRedis implements Runnable {
		private RedisClient redisClient;
		private ServiceConsumer[] consumers;

		private SynToRedis(RedisClient client, ServiceConsumer[] consumers) throws Exception {
			this.consumers = new ServiceConsumer[consumers.length];
			for (int i = 0; i < this.consumers.length; i++) {
				ServiceConsumer dest = new ServiceConsumer();
				EEBeanUtils.coverProperties(dest, consumers[i]);
				this.consumers[i] = dest;
			}
			this.redisClient = client;
		}

		@Override
		public void run() {
			try {
				Map<String, ServiceConsumer> map = new HashMap<String, ServiceConsumer>();
				for (ServiceConsumer consumer : this.consumers) {
					map.put(consumer.getCode(), consumer);
				}
				this.redisClient.addMapItem(CacheKey.SERVICE_CONSUMER, map, -1);
			} catch (RedisOPException e) {
				e.printStackTrace();// 缓存写入失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}
	}

	private SynServiceConsumerToRedis() {}
}
