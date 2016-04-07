package com.eenet.authen.bizimpl;

import com.eenet.authen.EENetEndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.cacheSyn.SynEENetEndUserLoginAccountToRedis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.StringResponse;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;

public class EndUserLoginAccountBizImpl extends SimpleBizImpl implements EndUserLoginAccountBizService {
	private RedisClient redisClient;
	
	@Override
	public EENetEndUserLoginAccount registeEndUserLoginAccount(EENetEndUserLoginAccount user) {
		EENetEndUserLoginAccount result = null;
		/* 参数检查 */
		if (user == null) {
			user = new EENetEndUserLoginAccount();
			user.setSuccessful(false);
			user.addMessage("要注册的用户登录账号未知("+this.getClass().getName()+")");
			return user;
		}
		
		/* 保存到数据库 */
		result = super.save(user);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynEENetEndUserLoginAccountToRedis.syn(getRedisClient(), user);
		
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
		
		result = super.delete(EENetEndUserLoginAccount.class, loginAccounts);
		/* 删除成功，同时从缓存中删除 */
		if (result.isSuccessful())
			RemoveMapItemFromRedisThread.execute(getRedisClient(), loginAccounts, CacheKey.ENDUSER_LOGIN_ACCOUNT);

		return result;
	}
	
	@Override
	public StringResponse retrieveEndUserMainAccount(String loginAccount) {
		StringResponse result = new StringResponse();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(loginAccount)) {
			result.setSuccessful(false);
			result.addMessage("未指定用户登录账号");
			return result;
		}
		
		/* 从缓存取数据 */
		try {
			String mainAccount = SynEENetEndUserLoginAccountToRedis.mainAccount(getRedisClient(), loginAccount);
			if (!EEBeanUtils.isNULL(mainAccount))
				result.setResult(mainAccount);
		} catch (RedisOPException | ClassCastException e) {
			e.printStackTrace();
		}
		
		/* 从数据库取数据 */
		if (result.getResult() == null) {
			QueryCondition query = new QueryCondition();
			query.addCondition(new ConditionItem("loginAccount",RangeType.EQUAL,loginAccount,null));
			SimpleResultSet<EENetEndUserLoginAccount> queryResult = super.query(query, EENetEndUserLoginAccount.class);
			if (queryResult.getResultSet().size() == 1) {
				result.setResult(queryResult.getResultSet().get(0).getMainAccount().getAccount());
				SynEENetEndUserLoginAccountToRedis.syn(getRedisClient(), queryResult.getResultSet().get(0));
			}
		}
		
		/* 从数据库也取不到数据 */
		if (EEBeanUtils.isNULL(result.getResult())) {
			result.setSuccessful(false);
			result.addMessage("未找到登录账号为："+loginAccount+"的主账号");
		}
		return result;
	}

	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	@Override
	public Class<?> getPojoCLS() {
		return null;
	}

	/**
	 * @return the redisClient
	 */
	public RedisClient getRedisClient() {
		return redisClient;
	}

	/**
	 * @param redisClient the redisClient to set
	 */
	public void setRedisClient(RedisClient redisClient) {
		this.redisClient = redisClient;
	}

}
