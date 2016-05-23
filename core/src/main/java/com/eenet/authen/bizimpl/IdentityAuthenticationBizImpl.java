package com.eenet.authen.bizimpl;

import com.eenet.authen.EENetEndUserAuthenRequest;
import com.eenet.authen.EENetEndUserAuthenResponse;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.ServiceAuthenRequest;
import com.eenet.authen.ServiceAuthenResponse;
import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.ServiceConsumerBizService;
import com.eenet.authen.ThirdPartySSOAPP;
import com.eenet.authen.ThirdPartySSOAppBizService;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

public class IdentityAuthenticationBizImpl implements IdentityAuthenticationBizService {
	private RedisClient redisClient;
	private RSADecrypt redisRSADecrypt;
	private ServiceConsumerBizService consumerService;
	private ThirdPartySSOAppBizService appService;
	
	@Override
	public ServiceAuthenResponse consumerAuthen(ServiceAuthenRequest request) {
		ServiceAuthenResponse response = new ServiceAuthenResponse();
		if (request==null || EEBeanUtils.isNULL(request.getConsumerCode()) || EEBeanUtils.isNULL(request.getConsumerSecretKey())) {
			response.addMessage(this.getClass().getName() + ": 服务消费者的编码和秘钥均不允许为空");
			return response;
		}
		
		/* 取服务消费者信息 */
		ServiceConsumer consumer = this.getConsumerService().retrieveServiceConsumer(request.getConsumerCode());
		if (!consumer.isSuccessful()) {
			response.addMessage(consumer.getStrMessage());
			return response;
		}
		
		/* 密码解密 */
		String secretKeyPlaintext = null;
		try {
			secretKeyPlaintext = RSAUtil.decrypt(getRedisRSADecrypt(), consumer.getSecretKey());
		} catch (EncryptException e) {
			e.printStackTrace();
		}

		/* 校对密码 */
		if (request.getConsumerSecretKey().equals(secretKeyPlaintext))
			response.setIdentityConfirm(true);
		else
			response.setIdentityConfirm(false);

		return response;
	}

	@Override
	public EENetEndUserAuthenResponse endUserAuthen(EENetEndUserAuthenRequest request) {
		EENetEndUserAuthenResponse response = new EENetEndUserAuthenResponse();
		/* 参数检查 */
		if (request == null || EEBeanUtils.isNULL(request.getAppId()) || EEBeanUtils.isNULL(request.getSecretKey())
				|| EEBeanUtils.isNULL(request.getEndUserAccount()) || EEBeanUtils.isNULL(request.getEndUserTocken())) {
			response.addMessage(this.getClass().getName() + ": 用户身份认证必须提供：用户主账号、用户令牌、业务系统ID、业务系统秘钥");
			response.setSuccessful(false);
			return response;
		}
		
		/* 取单点登录业务系统信息 */
		ThirdPartySSOAPP ssoApp = this.getAppService().retrieveThirdPartySSOApp(request.getAppId());
		if (!ssoApp.isSuccessful()){
			response.setSuccessful(false);
			response.addMessage(ssoApp.getStrMessage());
			return response;
		}
		
		/* 校验单点登录业务系统秘钥 */
		String ssoAppSecretKeyPlaintext = null;
		try {
			ssoAppSecretKeyPlaintext = RSAUtil.decrypt(getRedisRSADecrypt(), ssoApp.getSecretKey());
		} catch (EncryptException e) {
			response.setSuccessful(false);
			response.addMessage("业务系统解密失败："+e.toString());
			return response;
		}
		if (request.getSecretKey().equals(ssoAppSecretKeyPlaintext))
			response.setSsoSysIdentityConfirm(true);
		
		/* 校验最终用户访问令牌 */
		try {
			String mainAccount = getRedisClient().getObject(CacheKey.ACCESS_TOKEN_PREFIX + ":" + request.getEndUserTocken() + ":" + request.getAppId(),String.class);
			if (request.getEndUserAccount().equals(mainAccount))
				response.setEndUseridentityConfirm(true);
		} catch (RedisOPException e) {
			//缓存保存，视为访问令牌过期
		}
		
		return response;
	}
	
	@Override
	public boolean authenServiceProviderPing() {
		return true;
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	
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

	public RSADecrypt getRedisRSADecrypt() {
		return redisRSADecrypt;
	}

	public void setRedisRSADecrypt(RSADecrypt redisRSADecrypt) {
		this.redisRSADecrypt = redisRSADecrypt;
	}

	/**
	 * @return the consumerService
	 */
	public ServiceConsumerBizService getConsumerService() {
		return consumerService;
	}

	/**
	 * @param consumerService the consumerService to set
	 */
	public void setConsumerService(ServiceConsumerBizService consumerService) {
		this.consumerService = consumerService;
	}

	/**
	 * @return the appService
	 */
	public ThirdPartySSOAppBizService getAppService() {
		return appService;
	}

	/**
	 * @param appService the appService to set
	 */
	public void setAppService(ThirdPartySSOAppBizService appService) {
		this.appService = appService;
	}
}
