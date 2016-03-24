package com.eenet.authen.cacheSyn;

import java.util.HashMap;
import java.util.Map;

import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.bizimpl.CacheKey;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;
/**
 * 将服务消费者同步到Redis
 * @author Orion
 *
 */
public class SynServiceConsumerToRedis implements Runnable {
	private RedisClient redisClient;
	private ServiceConsumer[] consumers;
	
	/**
	 * @param client redis客户端
	 * @param consumers 要同步的数据
	 * @throws Exception
	 */
	private SynServiceConsumerToRedis(RedisClient client, ServiceConsumer[] consumers) throws Exception{
		this.consumers = new ServiceConsumer[consumers.length];
		for (int i=0;i<this.consumers.length;i++) {
			ServiceConsumer dest = new ServiceConsumer();
			EEBeanUtils.coverProperties(dest, consumers[i]);
			this.consumers[i] = dest;
		}
		this.redisClient = client;
	}
	
	public void run() {
		try {
			Map<String,ServiceConsumer> map = new HashMap<String, ServiceConsumer>();
			for (ServiceConsumer consumer : this.consumers) {
				map.put(consumer.getCode(), consumer);
			}
			this.redisClient.addMapItem(CacheKey.SERVICE_CONSUMER, map, -1);
		} catch (RedisOPException e) {
			e.printStackTrace();//缓存写入失败，do nothing
		} catch (Exception e) {
			e.printStackTrace();//其他错误，do nothing
		}
	}
	
	/**
	 * 服务消费者同步到Redis
	 * @param client redis客户端
	 * @param consumers 要同步的数据
	 */
	public static void syn(RedisClient client, ServiceConsumer... consumers) {
		if (consumers==null || consumers.length==0 || client==null)
			return;
		try {
			SynServiceConsumerToRedis syn = new SynServiceConsumerToRedis(client,consumers);
			Thread thread = new Thread(syn);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();//同步到Redis失败
		}
	}

}
