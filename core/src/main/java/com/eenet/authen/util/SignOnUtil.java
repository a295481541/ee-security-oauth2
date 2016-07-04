package com.eenet.authen.util;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessAppType;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;

/**
 * 登录工具类
 * @author Orion
 * 2016年6月10日
 */
public class SignOnUtil {
	private RedisClient RedisClient;//Redis客户端
	
	/**
	 * 检查业务系统是否存在，跳转地址是否合法(仅web应用需要)
	 * @param appId 业务系统标识
	 * @param redirectURI 跳转目标地址(仅web应用需要)
	 * @param businessAppBizService 业务系统服务
	 * @return
	 * 2016年6月10日
	 * @author Orion
	 */
	public SimpleResponse existAPP(String appId, String redirectURI, BusinessAppBizService businessAppBizService) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId)){
			result.addMessage("业务系统未知("+this.getClass().getName()+")");
			return result;
		}
		
		BusinessApp app = businessAppBizService.retrieveApp(appId);
		if (!app.isSuccessful()){
			result.addMessage("不存在指定的业务系统("+this.getClass().getName()+")");
			return result;
		}
		
		if (app.getAppType().equals(BusinessAppType.WEBAPP)) {
			if (EEBeanUtils.isNULL(redirectURI)) {
				result.addMessage("web应用未指定要调整的地址("+this.getClass().getName()+")");
				return result;
			} else if (redirectURI.indexOf(app.getRedirectURIPrefix()) != 0) {
				result.addMessage("要跳转的地址不合法，该应用只能跳转到"+app.getRedirectURIPrefix()+"域名下("+this.getClass().getName()+")");
				return result;
			}
		}
		
		result.setSuccessful(true);
		return result;
	}
	
	/**
	 * 生成并缓存登录授权码
	 * 授权码存储格式：[prefix]:[grant code]:[appid]
	 * @param prefix 授权码前缀
	 * @param appId 应用标识
	 * @param userId 用户标识（服务人员或最终用户）
	 * @return
	 * 2016年6月10日
	 * @author Orion
	 */
	public StringResponse makeGrantCode(String prefix, String appId, String userId) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		if (EEBeanUtils.isNULL(prefix) || EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(userId)){
			result.addMessage("授权码前缀、应用标识、用户标识均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		try {
			String code = EEBeanUtils.getUUID();
			boolean cached = getRedisClient().setObject(prefix+":"+code+":"+appId, userId, 60 * 15);
			result.setSuccessful(cached);
			if (cached)
				result.setResult(code);
			else
				result.addMessage("记录登录授权码失败("+this.getClass().getName()+")");
		} catch (RedisOPException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		return result;
	}
	
	/**
	 * 生成并记录访问令牌
	 * 访问令牌存储格式：[prefix]:[access token]:[appid]
	 * 令牌有效期：web应用30分钟，其他类型应用1天
	 * @param prefix
	 * @param appId
	 * @param userId
	 * @param businessAppBizService 业务系统服务
	 * @return
	 * 2016年6月10日
	 * @author Orion
	 */
	public StringResponse makeAccessToken(String prefix, String appId, String userId, BusinessAppBizService businessAppBizService) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		if (EEBeanUtils.isNULL(prefix) || EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(userId)){
			result.addMessage("授权码前缀、应用标识、用户标识均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		try {
			String accessToken = EEBeanUtils.getUUID();
			//APP类型
			BusinessAppType appType = businessAppBizService.retrieveApp(appId).getAppType();
			//访问令牌有效期
			int expire = BusinessAppType.WEBAPP.equals(appType) ? 60 * 30 : 60 * 60 * 24;
			//记录令牌
			boolean cached = getRedisClient().setObject(prefix + ":" + accessToken + ":" + appId, userId, expire);
			result.setSuccessful(cached);
			if (cached)
				result.setResult(accessToken);
			else
				result.addMessage("记录访问令牌失败("+this.getClass().getName()+")");
			return result;
		} catch (RedisOPException e) {
			result.addMessage(e.toString());
			return result;
		}
	}
	
	/**
	 * 生成并记录刷新令牌
	 * 刷新令牌存储格式：[prefix]:[refresh token]:[appid]
	 * 令牌有效期：30天
	 * @param prefix
	 * @param appId
	 * @param userId
	 * @return
	 * 2016年6月10日
	 * @author Orion
	 */
	public StringResponse makeRefreshToken(String prefix, String appId, String userId) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		if (EEBeanUtils.isNULL(prefix) || EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(userId)){
			result.addMessage("授权码前缀、应用标识、用户标识均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		try {
			String refreshToken = EEBeanUtils.getUUID();
			//访问令牌有效期
			int expire = 60 * 60 * 24 * 30;
			//记录令牌
			boolean cached = getRedisClient().setObject(prefix + ":" + refreshToken + ":" + appId, userId, expire);
			result.setSuccessful(cached);
			if (cached)
				result.setResult(refreshToken);
			else
				result.addMessage("记录刷新令牌失败("+this.getClass().getName()+")");
			return result;
		} catch (RedisOPException e) {
			result.addMessage(e.toString());
			return result;
		}
	}
	
	/**
	 * 删除人员（最终用户/服务人员）标识码（登录授权码/访问授权码/刷新授权码）
	 * @param prefix 授权码前缀
	 * @param codeOrToken 登录授权码/访问授权码/刷新授权码
	 * @param appId 应用标识
	 * @return
	 * 2016年6月10日
	 * @author Orion
	 */
	public SimpleResponse removeCodeOrToken(String prefix, String codeOrToken, String appId) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(prefix) || EEBeanUtils.isNULL(codeOrToken) || EEBeanUtils.isNULL(appId)){
			result.addMessage("授权码前缀、登录授权码/访问授权码/刷新授权码、应用标识均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		try {
			Boolean rmResult = getRedisClient().remove(prefix + ":" + codeOrToken + ":" + appId);
			if (rmResult)
				result.setSuccessful(rmResult);
			else
				result.addMessage("销毁登录授权码/访问授权码/刷新授权码失败：("+this.getClass().getName()+")");
			return result;
		} catch (RedisOPException e) {
			result.addMessage(e.toString());
			return result;
		}
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	
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
	
	
}
