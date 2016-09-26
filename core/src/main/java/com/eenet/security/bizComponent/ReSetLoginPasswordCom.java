package com.eenet.security.bizComponent;

import com.eenet.authen.EndUserCredential;
import com.eenet.authen.cacheSyn.SynEndUserCredential2Redis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.biz.GenericSimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.cache.RedisClient;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 重置密码业务组件
 * 2016年7月20日
 * @author Orion
 */
public class ReSetLoginPasswordCom {
	
	/**
	 * 重置用户登录密码
	 * 该方法已进行必要的参数检查
	 * @param endUserId 最终用户标识
	 * @param newPasswordPlainText 新密码明文
	 * @param storageRSAEncrypt 密码数据存储加密公钥
	 * @return
	 * 2016年7月20日
	 * @author Orion
	 */
	public SimpleResponse resetEndUserLoginPassword(String endUserId,String newPasswordPlainText,RSAEncrypt storageRSAEncrypt) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(endUserId) || EEBeanUtils.isNULL(newPasswordPlainText)) {
			result.setSuccessful(false);
			result.addMessage("未指定要重置密码的最终用户标识或要设置的新密码("+this.getClass().getName()+")");
			return result;
		}
		if (storageRSAEncrypt == null) {
			result.setSuccessful(false);
			result.addMessage("缺少加密密钥("+this.getClass().getName()+")");
			return result;
		}
		
		/* 判断指定的最终用户是否存在 */
		if ( !getEndUserInfoBizService().exist(endUserId).isSuccessful() ) {
			result.setSuccessful(false);
			result.addMessage("未找到指定要重置登录密码对应的最终用户");
			return result;
		}
		EndUserInfo existEndUser = new EndUserInfo();
		existEndUser.setAtid(endUserId);
		
		/* 从数据库取秘钥对象 */
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("endUser.atid",RangeType.EQUAL,endUserId,null));
		SimpleResultSet<EndUserCredential> queryResult = getGenericBiz().query(query, EndUserCredential.class);
		if (!queryResult.isSuccessful()) {
			result.setSuccessful(false);
			result.addMessage(queryResult.getStrMessage());
			return result;
		}
		
		/* 根据最终用户是否设置过统一密码，作不同处理 */
		EndUserCredential newCredential = null;
		if (queryResult.getCount()==0 && queryResult.getResultSet().size()==0) {
			newCredential = new EndUserCredential();
			newCredential.setEndUser(existEndUser);
		} else if (queryResult.getCount()==1 && queryResult.getResultSet().size()==1) {
			newCredential = queryResult.getResultSet().get(0);
		} else {
			result.setSuccessful(false);
			result.addMessage("匹配到该最终用户设置了个"+queryResult.getResultSet().size()+"统一登录密码");
			return result;
		}
		
		/* 新密码加密 */
		try {
			String newPasswordCipherText = RSAUtil.encrypt(storageRSAEncrypt, newPasswordPlainText);//用存储公钥加密新密码
			if (EEBeanUtils.isNULL(newPasswordCipherText))
				throw new EncryptException("重置密码前加密失败（空字符）");
			newCredential.setPassword(newPasswordCipherText);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		/* 保存到数据库，再根据保存结果写缓存或返回错误信息 */
		EndUserCredential savedResult = getGenericBiz().save(newCredential);
		result.setSuccessful(savedResult.isSuccessful());
		if (savedResult.isSuccessful())
			SynEndUserCredential2Redis.syn(getRedisClient(), savedResult);
		else
			result.addMessage(savedResult.getStrMessage());
		
		return result;
		
	}
	
	private GenericSimpleBizImpl genericBiz;//通用业务操作实现类
	private EndUserInfoBizService endUserInfoBizService;//最终用户信息服务
	private RedisClient RedisClient;//Redis客户端
	/**
	 * @return the 最终用户信息服务
	 */
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	/**
	 * @param endUserInfoBizService the 最终用户信息服务 to set
	 */
	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
	}

	/**
	 * @return the 通用业务操作实现类
	 */
	public GenericSimpleBizImpl getGenericBiz() {
		return genericBiz;
	}

	/**
	 * @param genericBiz the 通用业务操作实现类 to set
	 */
	public void setGenericBiz(GenericSimpleBizImpl genericBiz) {
		this.genericBiz = genericBiz;
	}
	
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
