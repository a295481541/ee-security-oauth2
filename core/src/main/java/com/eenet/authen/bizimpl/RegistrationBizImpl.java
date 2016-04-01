package com.eenet.authen.bizimpl;

import com.eenet.authen.EENetEndUserCredential;
import com.eenet.authen.EENetEndUserLoginAccount;
import com.eenet.authen.EENetEndUserMainAccount;
import com.eenet.authen.RegistrationBizService;
import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.ThirdPartySSOAPP;
import com.eenet.authen.cacheSyn.SynEENetEndUserCredentialToRedis;
import com.eenet.authen.cacheSyn.SynEENetEndUserLoginAccountToRedis;
import com.eenet.authen.cacheSyn.SynServiceConsumerToRedis;
import com.eenet.authen.cacheSyn.SynThirdPartySSOAPPToRedis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.common.util.RemoveMapItemFromRedisThread;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class RegistrationBizImpl extends SimpleBizImpl implements RegistrationBizService {
	private RedisClient redisClient;
	private RSAEncrypt redisRSAEncrypt;
	private RSADecrypt redisRSADecrypt;
	
	/****************************************************************************
	**                                                                         **
	**                          服务消费者注册与注销                                                                                **
	**                                                                         **
	****************************************************************************/
	@Override
	public ServiceConsumer registeServiceConsumer(ServiceConsumer consumer) {
		ServiceConsumer result = null;
		/* 参数检查 */
		if (consumer == null) {
			result = new ServiceConsumer();
			result.setSuccessful(false);
			result.addMessage("要注册的服务消费者未知("+this.getClass().getName()+")");
			return result;
		}
		
		/* 秘钥加密 */
		try {
			String ciphertext = RSAUtil.encrypt(getRedisRSAEncrypt(), consumer.getSecretKey());
			if (EEBeanUtils.isNULL(ciphertext))
				throw new EncryptException("密文为空");
			consumer.setSecretKey(ciphertext);
		} catch (EncryptException e) {
			result = new ServiceConsumer();
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		
		/* 保存到数据库 */
		result = super.save(consumer);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynServiceConsumerToRedis.syn(getRedisClient(), consumer);
		
		result.setSecretKey(null);
		return result;
	}
	
	@Override
	public SimpleResponse removeServiceConsumer(String... code) {
		SimpleResponse result = null;
		/* 参数检查 */
		if (code == null || code.length==0) {
			result = new SimpleResponse();
			result.setSuccessful(false);
			result.addMessage("未指定要删除的服务消费者("+this.getClass().getName()+")");
			return result;
		}
		
		result = super.delete(ServiceConsumer.class,code);
		/* 删除成功，同时从缓存中删除 */
		if (result.isSuccessful())
			RemoveMapItemFromRedisThread.execute(getRedisClient(), code, CacheKey.SERVICE_CONSUMER);
		
		return result;
	}
	
	/****************************************************************************
	**                                                                         **
	**                        第三方SSO系统注册与注销                                                                              **
	**                                                                         **
	****************************************************************************/
	@Override
	public ThirdPartySSOAPP registeThirdPartySSOApp(ThirdPartySSOAPP app) {
		ThirdPartySSOAPP result = null;
		/* 参数检查 */
		if (app == null) {
			result = new ThirdPartySSOAPP();
			result.setSuccessful(false);
			result.addMessage("要注册的单点登录系统未知("+this.getClass().getName()+")");
			return result;
		}
		
		/* 秘钥加密 */
		try {
			String ciphertext = RSAUtil.encrypt(getRedisRSAEncrypt(), app.getSecretKey());
			if (EEBeanUtils.isNULL(ciphertext))
				throw new EncryptException("密文为空");
			app.setSecretKey(ciphertext);
		} catch (EncryptException e) {
			result = new ThirdPartySSOAPP();
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		
		/* 保存到数据库 */
		result = super.save(app);
		
		/* 保存成功，写缓存 */
		if (result.isSuccessful())
			SynThirdPartySSOAPPToRedis.syn(getRedisClient(), app);
		
		result.setSecretKey(null);
		return result;
	}
	
	@Override
	public SimpleResponse removeThirdPartySSOApp(String... appIds) {
		SimpleResponse result = null;
		/* 参数检查 */
		if (appIds == null || appIds.length==0) {
			result = new SimpleResponse();
			result.setSuccessful(false);
			result.addMessage("要废弃得单点登录系统未知("+this.getClass().getName()+")");
			return result;
		}
		
		result = super.delete(ThirdPartySSOAPP.class,appIds);
		/* 删除成功，同时从缓存中删除 */
		if (result.isSuccessful())
			RemoveMapItemFromRedisThread.execute(getRedisClient(), appIds, CacheKey.SSO_APP);
		
		return result;
	}
	
	/****************************************************************************
	**                                                                         **
	**                      end user登录账号注册与注销                                                                          **
	**                                                                         **
	****************************************************************************/
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
	
	/****************************************************************************
	**                                                                         **
	**                           end user登录密码管理                                                                         **
	**                                                                         **
	****************************************************************************/
	@Override
	public SimpleResponse initUserLoginPassword(EENetEndUserCredential credential) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (credential == null) {
			result.setSuccessful(false);
			result.addMessage("要初始化用户登录秘钥未知("+this.getClass().getName()+")");
			return result;
		}
		
		/* 判断主账号是否存在 */
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("account",RangeType.EQUAL,credential.getMainAccount().getAccount(),null));
		SimpleResultSet<EENetEndUserMainAccount> existMainAccount = super.query(query, EENetEndUserMainAccount.class);
		if (existMainAccount.getCount() != 1) {
			result.setSuccessful(false);
			if (existMainAccount.getCount() < 1)
				result.addMessage("不存在主账号："+credential.getMainAccount().getAccount()+"，不可初始化密码");
			if (existMainAccount.getCount() > 1)
				result.addMessage("存在" + existMainAccount.getCount() + "个相同主账号："
						+ credential.getMainAccount().getAccount() + "，不可初始化密码");
			return result;
		}
		
		/* 判断主账号是否已设置过密码，有则返回错误信息 */
		query.cleanAllCondition();
		query.addCondition(new ConditionItem("mainAccount.account",RangeType.EQUAL,credential.getMainAccount().getAccount(),null));
		SimpleResultSet<EENetEndUserCredential> existCredential = super.query(query, EENetEndUserCredential.class);
		if (existCredential.getCount() != 0) {
			result.setSuccessful(false);
			result.addMessage("账号为："+credential.getMainAccount().getAccount()+"的用户已设置过密码");
			return result;
		}
		
		/* 秘钥加密 */
		try {
			String ciphertext = RSAUtil.encrypt(getRedisRSAEncrypt(), credential.getSecretKey());
			if (EEBeanUtils.isNULL(ciphertext))
				throw new EncryptException("密文为空");
			credential.setSecretKey(ciphertext);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
			
		/* 保存到数据库 */
		EENetEndUserCredential savedResult = super.save(credential);
		if (savedResult.isSuccessful()){
			result.setSuccessful(true);
			//保存成功，写缓存
			SynEENetEndUserCredentialToRedis.syn(getRedisClient(), savedResult);
		} else {
			result.setSuccessful(false);
			result.addMessage(savedResult.getStrMessage());
		}
		return result;
	}
	
	/**
	 * 主流程：
	 * V 
	 * | 判断主账号是否存在
	 * | 从缓存取最终用户密码信息
	 * V 缓存中没有最终用户密码信息
	 * | 判断是否已设置最终用户密码
	 * | 判断原有密码是否能匹配
	 * V 修改数据库值
	 * | 修改缓存值
	 * V 
	 */
	@Override
	public SimpleResponse changeUserLoginPassword(EENetEndUserCredential curCredential, String newSecretKey) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (curCredential == null || EEBeanUtils.isNULL(newSecretKey)) {
			result.setSuccessful(false);
			result.addMessage("要修改的最终用户秘钥参数不完整("+this.getClass().getName()+")");
			return result;
		}
		
		/* 判断主账号是否存在 */
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("account",RangeType.EQUAL,curCredential.getMainAccount().getAccount(),null));
		SimpleResultSet<EENetEndUserMainAccount> existMainAccount = super.query(query, EENetEndUserMainAccount.class);
		if (existMainAccount.getCount() != 1) {
			result.setSuccessful(false);
			if (existMainAccount.getCount() < 1)
				result.addMessage("不存在主账号："+curCredential.getMainAccount().getAccount()+"，不可修改密码");
			if (existMainAccount.getCount() > 1)
				result.addMessage("存在" + existMainAccount.getCount() + "个相同主账号："
						+ curCredential.getMainAccount().getAccount() + "，不可修改密码");
			return result;
		}
		
		/* 从缓存取最终用户密码信息 */
		EENetEndUserCredential credential = null;//从缓存或数据库中取回的对象
		try {
			EENetEndUserCredential.class.cast(getRedisClient().getMapValue(CacheKey.ENDUSER_CREDENTIAL,
					curCredential.getMainAccount().getAccount()));
		} catch (RedisOPException | ClassCastException e) {
			e.printStackTrace();
		}
		
		/* 缓存中没有最终用户密码信息，从数据库取 */
		if (credential == null) {
			query.cleanAllCondition();
			query.addCondition(new ConditionItem("mainAccount.account",RangeType.EQUAL,curCredential.getMainAccount().getAccount(),null));
			SimpleResultSet<EENetEndUserCredential> existCredential = super.query(query, EENetEndUserCredential.class);
			if (existCredential.getResultSet().size() == 1)
				credential = existCredential.getResultSet().get(0);
		}
		
		/* 判断是否已设置最终用户密码，没有则返回错误信息*/
		if (credential == null) {
			result.setSuccessful(false);
			result.addMessage("账号："+curCredential.getMainAccount().getAccount()+"未初始化密码，不可修改密码");
			return result;
		}
		
		/* 判断原有密码是否能匹配，不对则返回错误信息 */
		//解密从缓存或数据库中取得的密码
		String plaintext = null;
		try {
			plaintext = RSAUtil.decrypt(getRedisRSADecrypt(), credential.getSecretKey());
			if (EEBeanUtils.isNULL(plaintext));
				throw new EncryptException("无法解密已存在的密码");
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		if (EEBeanUtils.isNULL(plaintext))
			return result;
		//匹配系统中已存在的密码与用户提供密码的一致性
		if (!plaintext.equals(curCredential.getSecretKey())) {
			result.setSuccessful(false);
			result.addMessage("当前密码不正确！");
		}
		
		/* 修改数据库值，保存失败则返回错误信息 */
		//加密新秘钥
		String ciphertext = null;
		try {
			ciphertext = RSAUtil.encrypt(getRedisRSAEncrypt(), newSecretKey);
			if (EEBeanUtils.isNULL(ciphertext));
				throw new EncryptException("无法加密新密码");
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		if (EEBeanUtils.isNULL(ciphertext))
			return result;
		//保存数据
		curCredential.setSecretKey(ciphertext);
		EENetEndUserCredential savedResult = super.save(curCredential);
		if (savedResult.isSuccessful() == false) {
			result.setSuccessful(false);
			result.addMessage(savedResult.getStrMessage());
		}
		
		/* 修改缓存值 */
		SynEENetEndUserCredentialToRedis.syn(getRedisClient(), savedResult);
		result.setSuccessful(true);
		return result;
	}

	@Override
	public SimpleResponse resetUserLoginPassword(EENetEndUserMainAccount mainAccount) {
		return null;
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

	public RSAEncrypt getRedisRSAEncrypt() {
		return redisRSAEncrypt;
	}

	public void setRedisRSAEncrypt(RSAEncrypt redisRSAEncrypt) {
		this.redisRSAEncrypt = redisRSAEncrypt;
	}

	public RSADecrypt getRedisRSADecrypt() {
		return redisRSADecrypt;
	}

	public void setRedisRSADecrypt(RSADecrypt redisRSADecrypt) {
		this.redisRSADecrypt = redisRSADecrypt;
	}
}
