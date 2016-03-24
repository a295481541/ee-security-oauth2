package com.eenet.authen.bizimpl;

import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.ServiceAuthenRequest;
import com.eenet.authen.ServiceAuthenResponse;
import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.cacheSyn.SynServiceConsumerToRedis;
import com.eenet.base.dao.BaseDAOService;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.DBOPException;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;

public class IdentityAuthenticationBizImpl implements IdentityAuthenticationBizService {
	private RedisClient redisClient;
	private BaseDAOService daoService;

	public ServiceAuthenResponse consumerAuthen(ServiceAuthenRequest request) {
		ServiceAuthenResponse response = new ServiceAuthenResponse();
		if (EEBeanUtils.isNULL(request.getConsumerCode()) || EEBeanUtils.isNULL(request.getConsumerSecretKey())) {
			response.setSuccessful(false);
			response.addMessage(this.getClass().getName() + ": 服务消费者的编码和秘钥均不允许为空");
		}

		ServiceConsumer consumer = null;
		/* 从缓存取服务消费者信息 */
		try {
			consumer = ServiceConsumer.class
					.cast(getRedisClient().getMapValue(CacheKey.SERVICE_CONSUMER, request.getConsumerCode()));
		} catch (RedisOPException | ClassCastException e) {
			e.printStackTrace();
		}
		/* 缓存中没有消费者信息，从数据库取 */
		if (consumer == null) {
			try {
				consumer = daoService.get(request.getConsumerCode(), ServiceConsumer.class);
				SynServiceConsumerToRedis.syn(getRedisClient(), consumer);//将该消费者信息同步到缓存中
			} catch (DBOPException e) {
				/* 缓存和数据库均没有指定的消费者信息 */
				response.setSuccessful(false);
				response.addMessage(e.toString());
				response.addMessage("未找到指定的服务消费者（服务消费者唯一编码：" + request.getConsumerCode() + "）");
				return response;
			}
		}

		/* 校对密码 */
		if (request.getConsumerSecretKey().equals(consumer.getSecretKey())) {
			response.setIdentityConfirm(true);
			consumer.setSecretKey(null);// 清空密码
			response.setServiceConsumer(consumer);
		} else
			response.setIdentityConfirm(false);

		return response;
	}

	public ServiceAuthenResponse consumerNUserAuthen(ServiceAuthenRequest request) {
		return null;
	}

	/**
	 * @return the redisClient
	 */
	public RedisClient getRedisClient() {
		return redisClient;
	}

	/**
	 * @param redisClient
	 *            the redisClient to set
	 */
	public void setRedisClient(RedisClient redisClient) {
		this.redisClient = redisClient;
	}

	/**
	 * @return the daoService
	 */
	public BaseDAOService getDaoService() {
		return daoService;
	}

	/**
	 * @param daoService
	 *            the daoService to set
	 */
	public void setDaoService(BaseDAOService daoService) {
		this.daoService = daoService;
	}
}