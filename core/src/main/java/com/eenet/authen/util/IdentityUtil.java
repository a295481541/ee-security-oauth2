package com.eenet.authen.util;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 身份认证工具类
 * @author Orion
 * 2016年6月10日
 */
public class IdentityUtil {
	private RedisClient RedisClient;//Redis客户端
	
	/**
	 * 校验业务系统身份
	 * @param appId 业务系统标识
	 * @param secretKeyPlaintext 业务系统秘钥（明文）
	 * @param StorageRSADecrypt /数据存储解密私钥
	 * @return
	 * 2016年6月10日
	 * @author Orion
	 */
	public SimpleResponse validateAPP(String appId, String secretKeyPlaintext, RSADecrypt StorageRSADecrypt, BusinessAppBizService businessAppBizService) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(secretKeyPlaintext) || StorageRSADecrypt==null){
			result.setSuccessful(false);
			result.addMessage("业务系统标识、业务系统秘钥、解码私钥均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		BusinessApp app = businessAppBizService.retrieveApp(appId);
		if (!app.isSuccessful()){
			result.addMessage("不存在指定的业务系统("+this.getClass().getName()+")");
			return result;
		}
		
		/* 计算正确的的业务系统秘钥明文 */
		String existSecretKeyPlaintext = null;
		try {
			existSecretKeyPlaintext = RSAUtil.decrypt(StorageRSADecrypt, app.getSecretKey());
			if (EEBeanUtils.isNULL(existSecretKeyPlaintext)) {
				result.addMessage("无法解密提供的业务系统秘钥连接密码("+this.getClass().getName()+")");
				return result;
			}
		} catch (EncryptException e) {
			result.addMessage(e.toString());
			return result;
		}
		
		if (!secretKeyPlaintext.equals(existSecretKeyPlaintext)) {
			result.addMessage("业务系统标识或秘钥错误("+this.getClass().getName()+")");
			return result;
		}
		
		result.setSuccessful(true);
		return result;
	}
	
	/**
	 * 根据标识码（登录授权码/访问授权码/刷新授权码）获得人员（最终用户/服务人员）标识
	 * @param prefix 授权码前缀
	 * @param codeOrToken 登录授权码/访问授权码/刷新授权码
	 * @param appId 应用标识
	 * @return
	 * 2016年6月10日
	 * @author Orion
	 */
	public StringResponse getUserIdByCodeOrToken(String prefix, String codeOrToken, String appId) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		if (EEBeanUtils.isNULL(prefix) || EEBeanUtils.isNULL(codeOrToken) || EEBeanUtils.isNULL(appId)){
			result.addMessage("授权码前缀、登录授权码/访问授权码/刷新授权码、应用标识均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		try {
			String userId = getRedisClient().getObject(prefix + ":" + appId + ":" + codeOrToken, String.class);
			if (EEBeanUtils.isNULL(userId))
				result.addMessage("无效登录授权码/访问授权码/刷新授权码("+this.getClass().getName()+")");
			else {
				result.setResult(userId);
				result.setSuccessful(true);
			}
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
