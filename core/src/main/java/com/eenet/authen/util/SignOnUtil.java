package com.eenet.authen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger log = LoggerFactory.getLogger("error");
	
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
	 * 授权码存储格式：[prefix]:[appid]:[grant code]
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
			log.error("[makeGrantCode("+Thread.currentThread().getId()+")] make redis key : " + prefix+":"+appId+":"+code +", value : "+userId);
			boolean cached = getRedisClient().setObject(prefix+":"+appId+":"+code, userId, 60 * 15);
			log.error("[makeGrantCode("+Thread.currentThread().getId()+")] get redis result : " + getRedisClient().getObject(prefix+":"+appId+":"+code,String.class));
			
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
	 * 访问令牌存储格式：[prefix]:[appid]:[access token]
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
			boolean cached = getRedisClient().setObject(prefix + ":" + appId + ":" + accessToken , userId, expire);
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
	 * 刷新令牌存储格式：[prefix]:[appid]:[refresh token]
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
			boolean cached = getRedisClient().setObject(prefix + ":" + appId + ":" + refreshToken, userId, expire);
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
	 * 标记（或更新）人员（最终用户/服务人员）已缓存令牌
	 * @param prefix 缓存标识前缀
	 * @param appId 应用标识
	 * @param userId 用户标识
	 * @param accessToken
	 * @param refreshToken
	 * @return
	 * 2016年7月6日
	 * @author Orion
	 */
	public SimpleResponse markUserTokenInApp(String prefix, String appId, String userId, String accessToken, String refreshToken) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		
		/* 参数检查 */
		if (EEBeanUtils.isNULL(prefix) || EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(userId)){
			result.addMessage("授权码前缀、应用标识、用户标识均不可为空("+this.getClass().getName()+")");
			return result;
		}
		if (EEBeanUtils.isNULL(accessToken) && EEBeanUtils.isNULL(refreshToken)) {
			result.addMessage("访问令牌和刷新令牌不可同时为空("+this.getClass().getName()+")");
			return result;
		}
		
		String tokens = new String();
		try {
			tokens = getRedisClient().getObject(prefix + ":" + appId + ":" + userId, String.class);
		} catch (RedisOPException e) {}
		
		try {
			if (EEBeanUtils.isNULL(tokens)) {
				if (!EEBeanUtils.isNULL(accessToken))
					tokens = accessToken;
				tokens = tokens+":";
				if (!EEBeanUtils.isNULL(refreshToken))
					tokens = tokens+refreshToken;
			} else if (!EEBeanUtils.isNULL(tokens) && tokens.indexOf(":")!=-1) {
				if (!EEBeanUtils.isNULL(accessToken))
					tokens = accessToken + tokens.substring(tokens.indexOf(":"));
				if (!EEBeanUtils.isNULL(refreshToken))
					tokens = tokens.substring(0,tokens.indexOf(":")+1) + refreshToken;
			} else {
				result.addMessage("不是预期的数据格式，预期的格式是:[access token]:[refresh token]("+this.getClass().getName()+")");
				return result;
			}
			
			//缓存令牌标识有效期
			int expire = 60 * 60 * 24 * 30;
			//记录缓存令牌标识
			boolean cached = getRedisClient().setObject(prefix + ":" + appId + ":" + userId, tokens, expire);
			
			result.setSuccessful(cached);
			if (!cached)
				result.addMessage("记录缓存令牌失败("+this.getClass().getName()+")");
			return result;
		} catch (RedisOPException e) {
			result.addMessage(e.toString());
			return result;
		} catch (Exception e) {
			result.addMessage(e.getMessage());
			return result;
		}
	}
	
	/**
	 * 删除人员（最终用户/服务人员）标识码（登录授权码/访问授权码/刷新授权码）
	 * @param prefix 缓存标识前缀
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
			Boolean rmResult = getRedisClient().remove(prefix + ":" + appId + ":" + codeOrToken);
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
	
	/**
	 * 删除人员（最终用户/服务人员）刷新令牌、访问令牌和已缓存令牌标识
	 * @param cachedPrefix 已缓存令牌前缀
	 * @param accessTokenPrefix 访问令牌前缀
	 * @param refreshTokenPrefix 刷新令牌前缀
	 * @param appId 应用标识
	 * @param userId 用户标识
	 * @return 只要不发生异常或格式错误均返回true，无论是否删除成功
	 * 2016年7月7日
	 * @author Orion
	 */
	public SimpleResponse removeUserTokenInApp(String cachedPrefix, String accessTokenPrefix, String refreshTokenPrefix, String appId, String userId) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(userId)){
			result.addMessage("应用标识、用户标识均不可为空("+this.getClass().getName()+")");
			return result;
		}
		if (EEBeanUtils.isNULL(cachedPrefix) || EEBeanUtils.isNULL(accessTokenPrefix) || EEBeanUtils.isNULL(refreshTokenPrefix)){
			result.addMessage("已缓存令牌前缀、访问令牌前缀、刷新令牌前缀均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		try {
			String tokenStr = getRedisClient().getObject(cachedPrefix + ":" + appId + ":" + userId, String.class);
			if (EEBeanUtils.isNULL(tokenStr)) {
				result.setSuccessful(true);
				return result;
			}
			
			String[] tokens = tokenStr.split(":");
			if (tokens==null || tokens.length!=2) {
				result.addMessage("不是预期的数据格式，预期的格式是:[access token]:[refresh token]("+this.getClass().getName()+")");
				return result;
			}
			getRedisClient().remove(accessTokenPrefix + ":" + appId + ":" + tokens[0]);
			getRedisClient().remove(refreshTokenPrefix + ":" + appId + ":" + tokens[1]);
			getRedisClient().remove(cachedPrefix + ":" + appId + ":" + userId);
			
			result.setSuccessful(true);//无论删除是否成功都返回true
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
