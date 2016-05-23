package com.eenet.authen.bizimpl;

import java.lang.reflect.InvocationTargetException;

import com.eenet.authen.EENetEndUserCredential;
import com.eenet.authen.EENetEndUserCredentialBizService;
import com.eenet.authen.EENetEndUserMainAccount;
import com.eenet.authen.cacheSyn.SynEENetEndUserCredentialToRedis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.StringResponse;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class EENetEndUserCredentialBizImpl extends SimpleBizImpl implements EENetEndUserCredentialBizService {
	private RedisClient redisClient;
	private RSAEncrypt redisRSAEncrypt;
	private RSADecrypt redisRSADecrypt;

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
		
		/* 从数据库取最终用户密码信息（涉及到表ID问题所以不从缓存中取） */
		query.cleanAllCondition();
		query.addCondition(new ConditionItem("mainAccount.account",RangeType.EQUAL,curCredential.getMainAccount().getAccount(),null));
		SimpleResultSet<EENetEndUserCredential> existCredentialRS = super.query(query, EENetEndUserCredential.class);
		
		if (!existCredentialRS.isSuccessful()) {//从数据库中取密码时报错
			result.setSuccessful(false);
			result.addMessage("获得\""+curCredential.getMainAccount().getAccount()+"\"原始密码时错误：\n"+existCredentialRS.getStrMessage());
			return result;
		}
		if (existCredentialRS.getCount()!=1) {//未设置最终用户密码，返回错误信息
			result.setSuccessful(false);
			result.addMessage("账号："+curCredential.getMainAccount().getAccount()+"现有["+existCredentialRS.getCount()+"]个主密码，不可修改密码");
			return result;
		}
		EENetEndUserCredential existCredential = existCredentialRS.getResultSet().get(0);
		if (!EEBeanUtils.isNULL(curCredential.getAtid()) && !(existCredential.getAtid().equals(curCredential.getAtid()))) {
			result.setSuccessful(false);
			result.addMessage(curCredential.getAtid()+"不是用户"+existCredential.getMainAccount().getAccount()+"秘钥的流水号");
			return result;
		}
		
		/* 判断原有密码是否能匹配，不对则返回错误信息 */
		//解密从缓存或数据库中取得的密码
		String plaintext = null;
		try {
			plaintext = RSAUtil.decrypt(getRedisRSADecrypt(), existCredential.getSecretKey());
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
			if (EEBeanUtils.isNULL(ciphertext))
				throw new EncryptException("无法加密新密码");
			EEBeanUtils.coverProperties(existCredential, curCredential);//将用户传入的数据覆盖数据库中的数据
		} catch (EncryptException | IllegalAccessException | InvocationTargetException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
		}
		if (EEBeanUtils.isNULL(ciphertext))
			return result;
		
		//保存数据
		existCredential.setSecretKey(ciphertext);
		EENetEndUserCredential savedResult = super.save(existCredential);
		if (!savedResult.isSuccessful()) {
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
	
	@Override
	public StringResponse retrieveUserSecretKey(String mainAccount) {
		StringResponse result = new StringResponse();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(mainAccount)) {
			result.setSuccessful(false);
			result.addMessage("未指定用户主账号");
		}
		
		/* 从缓存取数据 */
		try {
			String ciphertext = SynEENetEndUserCredentialToRedis.secretKey(getRedisClient(), mainAccount, null);
			if (!EEBeanUtils.isNULL(ciphertext))
				result.setResult(ciphertext);
		} catch (RedisOPException | ClassCastException | EncryptException e) {
			e.printStackTrace();
		}
		
		/* 从数据库取数据 */
		if (result.getResult() == null) {
			QueryCondition query = new QueryCondition();
			query.addCondition(new ConditionItem("mainAccount.account",RangeType.EQUAL,mainAccount,null));
			SimpleResultSet<EENetEndUserCredential> queryResult = super.query(query, EENetEndUserCredential.class);
			if (!queryResult.isSuccessful())
				result.addMessage(queryResult.getStrMessage());
			if (queryResult.getResultSet().size() == 1) {
				result.setResult(queryResult.getResultSet().get(0).getSecretKey());
				SynEENetEndUserCredentialToRedis.syn(getRedisClient(), queryResult.getResultSet().get(0));
			}
		}
		
		/* 从数据库也取不到数据 */
		if (EEBeanUtils.isNULL(result.getResult())) {
			result.setSuccessful(false);
			result.addMessage("未找到主账号为："+mainAccount+"的秘钥（密文）");
		}
		return result;
	}

	@Override
	public StringResponse retrieveUserSecretKey(String mainAccount, RSADecrypt redisRSADecrypt) {
		StringResponse result = new StringResponse();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(mainAccount)) {
			result.setSuccessful(false);
			result.addMessage("未指定用户主账号");
			return result;
		}
		if (redisRSADecrypt == null) {
			result.setSuccessful(false);
			result.addMessage("未指定解密参数");
			return result;
		}
		
		/* 取秘钥密文 */
		StringResponse ciphertextResponse = this.retrieveUserSecretKey(mainAccount);
		if (!ciphertextResponse.isSuccessful())
			return ciphertextResponse;
		
		/* 密文解密 */
		String plaintext = null;
		try {
			plaintext = RSAUtil.decrypt(redisRSADecrypt, ciphertextResponse.getResult());
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		if (EEBeanUtils.isNULL(plaintext)) {
			result.setSuccessful(false);
			result.addMessage("无法解密主账号："+mainAccount+"的登录秘钥");
		} else
			result.setResult(plaintext);
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
