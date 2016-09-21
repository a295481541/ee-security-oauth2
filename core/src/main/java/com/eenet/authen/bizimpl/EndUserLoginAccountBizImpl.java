package com.eenet.authen.bizimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.impl.Log4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.cacheSyn.SynEndUserLoginAccount2Redis;
import com.eenet.authen.util.ABBizCode;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.common.OPOwner;
import com.eenet.common.cache.RedisClient;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

/**
 * 最终用户登录账号管理实现逻辑
 * @author Orion
 * 2016年6月7日
 */
public class EndUserLoginAccountBizImpl extends SimpleBizImpl implements EndUserLoginAccountBizService {
	private static final Logger log = LoggerFactory.getLogger("error");
	private RedisClient RedisClient;//Redis客户端
	@Override
	public EndUserLoginAccount registeEndUserLoginAccount(EndUserLoginAccount account) {
		log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] start..........." +OPOwner.getUsertype());
		EndUserLoginAccount result = new EndUserLoginAccount();
		result.setSuccessful(true);
		/* 参数检查 */
		if (account == null) {
			log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] account null ");
			result.setSuccessful(false);
			result.addMessage("要注册的用户登录账号未知("+this.getClass().getName()+")");
		} else if (account.getUserInfo()==null || EEBeanUtils.isNULL(account.getUserInfo().getAtid()) || EEBeanUtils.isNULL(account.getLoginAccount()) || account.getAccountType()==null ) {
			log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] atid not nul or account null");
			result.setSuccessful(false);
			result.addMessage("要注册的用户登录账号参数不全，END USER标识、登录账号、账号类型均不可为空("+this.getClass().getName()+")");
		} else if (!EEBeanUtils.isNULL(account.getAccountLoginPassword())) {
			log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] account password not null(no allow)");
			result.setSuccessful(false);
			result.addMessage("新账号不再支持使用私有密码，请设置统一用户密码，已有账号的私有秘钥仍可登陆("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] check over");
		
		/* 检查要注册的账号是否存在 */
		EndUserLoginAccount existAccount = this.retrieveEndUserLoginAccountInfo(account.getLoginAccount());
		log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] exist account check: "+EEBeanUtils.object2Json(account));
		log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] exist account check: "+EEBeanUtils.object2Json(existAccount));
		if ( existAccount.isSuccessful() && account.getUserInfo().getAtid().equals(existAccount.getUserInfo().getAtid()) ) {//账号存在并且指向同一个用户
			existAccount.setRSBizCode(ABBizCode.AB0001);
			return existAccount;
		} else if ( existAccount.isSuccessful() && !account.getUserInfo().getAtid().equals(existAccount.getUserInfo().getAtid()) ) {//账号已被其他用户使用
			result.setSuccessful(false);
			result.setRSBizCode(ABBizCode.AB0002);
			return result;
		}
		
		/* 保存到数据库 */
		result = super.save(account);
		log.error("[registeEndUserLoginAccount("+Thread.currentThread().getId()+")] saved result : " + EEBeanUtils.object2Json(result));
		if (result.isSuccessful())
			SynEndUserLoginAccount2Redis.syn(getRedisClient(), result);
		
		return result;
	}

	@Override
	public SimpleResponse removeEndUserLoginAccount(String... loginAccounts) {
		SimpleResponse result = null;
		/* 参数检查 */
		if (loginAccounts==null || loginAccounts.length==0) {
			result = new SimpleResponse();
			result.setSuccessful(false);
			result.addMessage("要废弃的最终用户登录账号未知("+this.getClass().getName()+")");
			return result;
		}
		
		/* （1/2）从缓存中取得要删除登录账号的对象 */
		List<EndUserLoginAccount> accountInCacheObj = new ArrayList<EndUserLoginAccount>();//已经在缓存中的登录账号对象(list中放对象)
		List<String> accountNoInCache = new ArrayList<String>();//未在缓存中的登录账号对象(list中放登录账号字符串)
		for (String account : loginAccounts) {
			EndUserLoginAccount accountObj = SynEndUserLoginAccount2Redis.get(getRedisClient(), account);
			if (accountObj == null)
				accountNoInCache.add(account);
			else
				accountInCacheObj.add(accountObj);
		}
		
		/* (2/2)缓存中没有的对象，从数据库取得 */
		List<EndUserLoginAccount> accountNoInCacheObj = new ArrayList<EndUserLoginAccount>();//未在缓存中的登录账号对象(list中放对象)
		if (accountNoInCache.size() > 0) {
			QueryCondition query = new QueryCondition();
			SimpleResultSet<EndUserLoginAccount> queryResult = null;
			for (String account : accountNoInCache) {
				query.cleanAllCondition();
				query.addCondition(new ConditionItem("loginAccount",RangeType.EQUAL,account,null));
				queryResult = super.query(query, EndUserLoginAccount.class);
				accountNoInCacheObj.addAll(queryResult.getResultSet());
			}
		}
		
		/* 计算要删除对象的id */
		String[] accountInCacheIds = new String[accountInCacheObj.size()];//在缓存中账号ID
		String[] accountNoInCacheIds = new String[accountNoInCacheObj.size()];//不在缓存中账号ID
		String[] loginAccountIDs = new String[accountInCacheObj.size()+accountNoInCacheObj.size()];//所有账号ID
		
		for (int i=0;i<accountInCacheIds.length;i++)
			accountInCacheIds[i] = accountInCacheObj.get(i).getAtid();
		
		for (int i=0;i<accountNoInCacheIds.length;i++)
			accountNoInCacheIds[i] = accountNoInCacheObj.get(i).getAtid();
		//将在缓存中账号ID和不在缓存中账号ID拼在一个数组中
		System.arraycopy(accountInCacheIds, 0, loginAccountIDs, 0, accountInCacheIds.length);
		System.arraycopy(accountNoInCacheIds, 0, loginAccountIDs, accountInCacheIds.length, accountNoInCacheIds.length);
		
		/* 从缓存和数据库中删除存在的对象 */
		SynEndUserLoginAccount2Redis.remove(getRedisClient(), loginAccounts);
		result = super.delete(EndUserLoginAccount.class, loginAccountIDs);
		
		return result;
	}
	
	@Override
	public EndUserLoginAccount retrieveEndUserLoginAccountInfo(String loginAccount) {
		EndUserLoginAccount err = new EndUserLoginAccount();
		err.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(loginAccount)) {
			err.addMessage("登录账号未知");
			return err;
		}
		
		/* 从缓存取数据 */
		EndUserLoginAccount account = SynEndUserLoginAccount2Redis.get(getRedisClient(), loginAccount);
		if (account!=null) {
			return account;
		}
		
		/* 从数据库取数据 */
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("loginAccount",RangeType.EQUAL,loginAccount,null));
		SimpleResultSet<EndUserLoginAccount> queryResult = super.query(query, EndUserLoginAccount.class);
		if (!queryResult.isSuccessful()){
			err.addMessage(queryResult.getStrMessage());
			return err;
		} else if (queryResult.getResultSet().size()!=1) {
			err.addMessage("无法取得登录账号，有"+queryResult.getResultSet().size()+"个账号与之匹配");
			return err;
		}
		/* 数据库有，缓存没有，同步到缓存 */
		SynEndUserLoginAccount2Redis.syn(getRedisClient(), queryResult.getResultSet().get(0));
		
		return queryResult.getResultSet().get(0);
	}

	@Override
	public EndUserInfo retrieveEndUserInfo(String loginAccount) {
		EndUserLoginAccount account = this.retrieveEndUserLoginAccountInfo(loginAccount);
		if (account.isSuccessful())
			return account.getUserInfo();
		else {
			EndUserInfo err = new EndUserInfo();
			err.setSuccessful(false);
			err.addMessage(account.getStrMessage());
			return err;
		}
	}

	@Override
	public EndUserLoginAccount retrieveEndUserAccountPassword(String loginAccount, RSADecrypt StorageRSAEncrypt) {
		/* 取秘钥密文（未取到或不是RSA密文都直接返回结果） */
		EndUserLoginAccount result = this.retrieveEndUserLoginAccountInfo(loginAccount);
		if (!result.isSuccessful() || !"RSA".equals(result.getEncryptionType()))
			return result;
		
		/* 参数检查 */
		if (StorageRSAEncrypt==null) {
			result.setSuccessful(false);
			result.addMessage("最终用户解密私钥未知");
			return result;
		}
		
		/* 密文解密 */
		try {
			String plaintext = RSAUtil.decrypt(StorageRSAEncrypt, result.getAccountLoginPassword());
			if (EEBeanUtils.isNULL(plaintext))
				throw new EncryptException("解密密码失败（空字符）");
			result.setAccountLoginPassword(plaintext);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		return result;
	}
	
	@Override
	public SimpleResultSet<EndUserLoginAccount> query(QueryCondition condition) {
		return super.query(condition);
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	@Override
	public Class<?> getPojoCLS() {
		return EndUserLoginAccount.class;
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
