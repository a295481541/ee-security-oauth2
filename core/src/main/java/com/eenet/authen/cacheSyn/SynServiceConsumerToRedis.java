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
			SynToRedis syn = new SynServiceConsumerToRedis().getSynToRedis(client, consumers);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 将务消费者数据从Redis中删除
	 * @param client redis客户端
	 * @param consumerCodes 服务消费者code
	 * 2016年3月28日
	 * @author Orion
	 */
	public static void del(RedisClient client, String... consumerCodes) {
		if (consumerCodes == null || consumerCodes.length == 0 || client == null)
			return;
		try {
			DelFromRedis syn = new SynServiceConsumerToRedis().getDelFromRedis(client, consumerCodes);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	
	/**
	 * 将所有消费者数据从Redis中清除
	 * Redis不再缓存消费者数据 
	 * @param client redis客户端
	 * 2016年3月28日
	 * @author Orion
	 */
	public static void drop(RedisClient client) {
		if (client == null)
			return;
		try {
			DropFromRedis syn = new SynServiceConsumerToRedis().getDropFromRedis(client);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();// 同步到Redis失败
		}
	}
	/**
	 * 将务消费者数据同步到Redis
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

	/**
	 * 将务消费者数据从Redis中删除
	 * 2016年3月28日
	 * @author Orion
	 */
	private class DelFromRedis implements Runnable {
		private RedisClient redisClient;
		private String[] consumerCodes;

		private DelFromRedis(RedisClient client, String[] consumerCodes) {
			this.consumerCodes = consumerCodes;
			this.redisClient = client;
		}

		@Override
		public void run() {
			try {
				this.redisClient.removeMapItem(CacheKey.SERVICE_CONSUMER, consumerCodes);
			} catch (RedisOPException e) {
				e.printStackTrace();// 删除数据失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}

	}

	/**
	 * 将所有消费者数据从Redis中清除 
	 * Redis不再缓存消费者数据 
	 * 
	 * 2016年3月28日
	 * @author Orion
	 */
	private class DropFromRedis implements Runnable {
		private RedisClient redisClient;
		
		private DropFromRedis(RedisClient client) {
			this.redisClient = client;
		}
		@Override
		public void run() {
			try {
				this.redisClient.remove(CacheKey.SERVICE_CONSUMER);
			} catch (RedisOPException e) {
				e.printStackTrace();// 清除数据失败，do nothing
			} catch (Exception e) {
				e.printStackTrace();// 其他错误，do nothing
			}
		}

	}

	private SynToRedis getSynToRedis(RedisClient client, ServiceConsumer[] consumers) throws Exception {
		return new SynToRedis(client, consumers);
	}
	
	private DelFromRedis getDelFromRedis(RedisClient client, String[] consumerCodes) {
		return new DelFromRedis(client, consumerCodes);
	}
	
	private DropFromRedis getDropFromRedis(RedisClient client) {
		return new DropFromRedis(client);
	}
	private SynServiceConsumerToRedis() {}
}
