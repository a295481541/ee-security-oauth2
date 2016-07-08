package com.eenet.authen.bizimpl;

import java.util.ArrayList;
import java.util.List;

import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.AdminUserLoginAccountBizService;
import com.eenet.authen.cacheSyn.SynAdminUserLoginAccount2Redis;
import com.eenet.authen.util.ABBizCode;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.common.cache.RedisClient;
import com.eenet.user.AdminUserInfo;
import com.eenet.util.EEBeanUtils;

/**
 * 服务人员登录账号管理实现逻辑
 * @author Orion
 * 2016年6月7日
 */
public class AdminUserLoginAccountBizImpl extends SimpleBizImpl implements AdminUserLoginAccountBizService {
	private RedisClient RedisClient;//Redis客户端
	
	@Override
	public AdminUserLoginAccount registeAdminUserLoginAccount(AdminUserLoginAccount account) {
		AdminUserLoginAccount result = new AdminUserLoginAccount();
		/* 参数检查 */
		if (account == null) {
			result.setSuccessful(false);
			result.addMessage("要注册的用户登录账号未知("+this.getClass().getName()+")");
		} else if (account.getUserInfo()==null || EEBeanUtils.isNULL(account.getUserInfo().getAtid()) || EEBeanUtils.isNULL(account.getLoginAccount()) || account.getAccountType()==null ) {
			result.setSuccessful(false);
			result.addMessage("要注册的用户登录账号参数不全，ADMIN USER标识、登录账号、账号类型均不可为空("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		
		/* 检查要注册的账号是否存在 */
		AdminUserLoginAccount existAccount = this.retrieveAdminUserLoginAccountInfo(account.getLoginAccount());
		if ( existAccount.isSuccessful() && account.getUserInfo().getAtid().equals(existAccount.getUserInfo().getAtid()) ) {//账号存在并且指向同一个用户
			existAccount.setRSBizCode(ABBizCode.AB0001);
			return existAccount;
		} else if ( existAccount.isSuccessful() && !account.getUserInfo().getAtid().equals(existAccount.getUserInfo().getAtid()) ) {//账号已被其他用户使用
			result.setSuccessful(false);
			existAccount.setRSBizCode(ABBizCode.AB0002);
			return existAccount;
		}
		
		/* 保存到数据库 */
		result = super.save(account);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynAdminUserLoginAccount2Redis.syn(getRedisClient(), result);
		
		return result;
	}

	@Override
	public SimpleResponse removeAdminUserLoginAccount(String... loginAccounts) {
		SimpleResponse result = null;
		/* 参数检查 */
		if (loginAccounts==null || loginAccounts.length==0) {
			result = new SimpleResponse();
			result.setSuccessful(false);
			result.addMessage("要废弃的服务人员登录账号未知("+this.getClass().getName()+")");
			return result;
		}
		
		/* （1/2）从缓存中取得要删除登录账号的对象 */
		List<AdminUserLoginAccount> accountInCacheObj = new ArrayList<AdminUserLoginAccount>();//已经在缓存中的登录账号对象(list中放对象)
		List<String> accountNoInCache = new ArrayList<String>();//未在缓存中的登录账号对象(list中放登录账号字符串)
		for (String account : loginAccounts) {
			AdminUserLoginAccount accountObj = SynAdminUserLoginAccount2Redis.get(getRedisClient(), account);
			if (accountObj == null)
				accountNoInCache.add(account);
			else
				accountInCacheObj.add(accountObj);
		}
		
		/* (2/2)缓存中没有的对象，从数据库取得 */
		List<AdminUserLoginAccount> accountNoInCacheObj = new ArrayList<AdminUserLoginAccount>();//未在缓存中的登录账号对象(list中放对象)
		if (accountNoInCache.size() > 0) {
			QueryCondition query = new QueryCondition();
			SimpleResultSet<AdminUserLoginAccount> queryResult = null;
			for (String account : accountNoInCache) {
				query.cleanAllCondition();
				query.addCondition(new ConditionItem("loginAccount",RangeType.EQUAL,account,null));
				queryResult = super.query(query, AdminUserLoginAccount.class);
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
		SynAdminUserLoginAccount2Redis.remove(getRedisClient(), loginAccounts);
		result = super.delete(AdminUserLoginAccount.class, loginAccountIDs);
		
		return result;
	}
	
	@Override
	public AdminUserLoginAccount retrieveAdminUserLoginAccountInfo(String loginAccount) {
		AdminUserLoginAccount err = new AdminUserLoginAccount();
		err.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(loginAccount)) {
			err.addMessage("登录账号未知");
			return err;
		}
		
		/* 从缓存取数据 */
		AdminUserLoginAccount account = SynAdminUserLoginAccount2Redis.get(getRedisClient(), loginAccount);
		if (account!=null) {
			return account;
		}
		
		/* 从数据库取数据 */
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("loginAccount",RangeType.EQUAL,loginAccount,null));
		SimpleResultSet<AdminUserLoginAccount> queryResult = super.query(query, AdminUserLoginAccount.class);
		if (!queryResult.isSuccessful()){
			err.addMessage(queryResult.getStrMessage());
			return err;
		} else if (queryResult.getResultSet().size()!=1) {
			err.addMessage("无法取得登录账号，有"+queryResult.getResultSet().size()+"个账号与之匹配");
			return err;
		}
		/* 数据库有，缓存没有，同步到缓存 */
		SynAdminUserLoginAccount2Redis.syn(getRedisClient(), queryResult.getResultSet().get(0));
		
		return queryResult.getResultSet().get(0);
	}

	@Override
	public AdminUserInfo retrieveAdminUserInfo(String loginAccount) {
		AdminUserLoginAccount account = this.retrieveAdminUserLoginAccountInfo(loginAccount);
		if (account.isSuccessful())
			return account.getUserInfo();
		else {
			AdminUserInfo err = new AdminUserInfo();
			err.setSuccessful(false);
			err.addMessage(account.getStrMessage());
			return err;
		}
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/
	@Override
	public Class<?> getPojoCLS() {
		return AdminUserLoginAccount.class;
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
