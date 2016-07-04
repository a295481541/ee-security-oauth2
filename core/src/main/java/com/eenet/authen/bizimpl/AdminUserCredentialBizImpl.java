package com.eenet.authen.bizimpl;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserCredentialBizService;
import com.eenet.authen.cacheSyn.SynAdminUserCredential2Redis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.common.cache.RedisClient;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.AdminUserInfoBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.MD5Util;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;
/**
 * 服务人员登录秘钥实现逻辑
 * @author Orion
 * 2016年6月8日
 */
public class AdminUserCredentialBizImpl extends SimpleBizImpl implements AdminUserCredentialBizService {
	private RedisClient RedisClient;//Redis客户端
	private RSAEncrypt StorageRSAEncrypt;//数据存储加密公钥
	private RSADecrypt StorageRSADecrypt;//数据存储解密私钥
	private RSADecrypt TransferRSADecrypt;//数据传输解密私钥
	private AdminUserInfoBizService adminUserInfoBizService;//服务人员信息服务

	@Override
	public SimpleResponse initAdminUserLoginPassword(AdminUserCredential credential) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (credential == null) {
			result.setSuccessful(false);
			result.addMessage("要初始化的服务人员登录秘钥未知("+this.getClass().getName()+")");
			return result;
		} else if (EEBeanUtils.isNULL(credential.getPassword()) || credential.getAdminUser()==null || EEBeanUtils.isNULL(credential.getAdminUser().getAtid())) {
			result.setSuccessful(false);
			result.addMessage("要初始化的服务人员登录秘钥参数不全，ADMIN USER标识、登录秘钥均不可为空("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		
		/* 判断服务人员是否已设置过密码，有则返回错误信息 */
		AdminUserCredential existCredential = this.retrieveAdminUserCredentialInfo(credential.getAdminUser().getAtid());
		if (existCredential.isSuccessful()) {
			result.setSuccessful(false);
			result.addMessage("该服务人员已经设置过统一登录密码");
			return result;
		}
		
		/* 判断指定的服务人员是否存在 */
		AdminUserInfo existAdminUser = this.getAdminUserInfoBizService().get(credential.getAdminUser().getAtid());
		if (!existAdminUser.isSuccessful() || EEBeanUtils.isNULL(existAdminUser.getAtid())) {
			result.setSuccessful(false);
			result.addMessage("未找到指定要设置登录密码对应的服务人员");
			return result;
		}
		
		/* 秘钥加密 */
		try {
			String passwordPlainText = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), credential.getPassword(), 2);
			String passwordCipherText = RSAUtil.encrypt(getStorageRSAEncrypt(), passwordPlainText);
			if (EEBeanUtils.isNULL(passwordCipherText))
				throw new EncryptException("初始化密码前加密失败（空字符）");
			credential.setPassword(passwordCipherText);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		/* 保存到数据库，再根据保存结果写缓存或返回错误信息 */
		AdminUserCredential savedResult = super.save(credential);
		result.setSuccessful(savedResult.isSuccessful());
		if (savedResult.isSuccessful())
			SynAdminUserCredential2Redis.syn(getRedisClient(), savedResult);
		else
			result.addMessage(savedResult.getStrMessage());
		
		return result;
	}
	
	/**
	 * 主流程：
	 * -> 参数检查
	 * -> 判断服务人员是否已设置过密码，没有则返回错误信息
	 * -> 判断指定的服务人员是否存在
	 * -> 判断原有密码是否能匹配，不对则返回错误信息
	 * -> 新密码加密
	 * -> 保存到数据库，再根据保存结果写缓存或返回错误信息
	 * 2016年6月9日
	 * @author Orion
	 */
	@Override
	public SimpleResponse changeAdminUserLoginPassword(AdminUserCredential curCredential, String newSecretKey) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (curCredential==null || EEBeanUtils.isNULL(newSecretKey)) {
			result.setSuccessful(false);
			result.addMessage("要修改的服务人员登录密码未知("+this.getClass().getName()+")");
			return result;
		} else if (EEBeanUtils.isNULL(curCredential.getPassword()) || curCredential.getAdminUser()==null || EEBeanUtils.isNULL(curCredential.getAdminUser().getAtid())) {
			result.setSuccessful(false);
			result.addMessage("要修改的服务人员登录秘钥参数不全，ADMIN USER标识、当前登录密码均不可为空("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		
		/* 判断服务人员是否已设置过密码，没有则返回错误信息 */
		AdminUserCredential existCredential = this.retrieveAdminUserCredentialInfo(curCredential.getAdminUser().getAtid());
		if (!existCredential.isSuccessful()) {
			result.setSuccessful(false);
			result.addMessage(existCredential.getStrMessage());
			return result;
		}
		
		/* 判断指定的服务人员是否存在 */
		AdminUserInfo existAdminUser = curCredential.getAdminUser();
		if (EEBeanUtils.isNULL(existAdminUser.getName()) && existAdminUser.getMobile()==null) {//尝试从密码对象中判断人员姓名或手机是否已存在
			existAdminUser = this.getAdminUserInfoBizService().get(curCredential.getAdminUser().getAtid());
			if (!existAdminUser.isSuccessful() || EEBeanUtils.isNULL(existAdminUser.getAtid())) {
				result.setSuccessful(false);
				result.addMessage("未找到指定要设置登录密码对应的服务人员");
				return result;
			}
		}
		
		/* 获得传入原密码明文 */
		String passwordPlainText = null;//传入原密码明文
		try {
			passwordPlainText = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), curCredential.getPassword(), 2);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		/* 
		 * 判断原有密码是否能匹配，不对则返回错误信息
		 * 根据加密方式进行不同的密码匹配
		 */
		try {
			if (existCredential.getEncryptionType().equals("RSA")) {
				String existPasswordPlainText = RSAUtil.decrypt(getStorageRSADecrypt(), existCredential.getPassword());
				if (EEBeanUtils.isNULL(existPasswordPlainText) || !passwordPlainText.equals(existPasswordPlainText))
					throw new EncryptException("原密码不正确[RSA]！("+this.getClass().getName()+")");
			} else if (existCredential.getEncryptionType().equals("MD5")) {
				String passwordPlainTextMD5 = MD5Util.encrypt(passwordPlainText);//对传入原密码进行md5加密
				if (EEBeanUtils.isNULL(passwordPlainTextMD5) || !passwordPlainTextMD5.equals(existCredential.getPassword()))
					throw new EncryptException("原密码不正确[MD5]！("+this.getClass().getName()+")");
			} else {
				throw new EncryptException("加密方式未知["+existCredential.getEncryptionType()+"]("+this.getClass().getName()+")");
			}
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		/* 新密码加密 */
		try {
			String newPasswordPlainText = RSAUtil.decrypt(getTransferRSADecrypt(), newSecretKey);//用传输私钥解出新密码明文
			String newPasswordCipherText = RSAUtil.encrypt(getStorageRSAEncrypt(), newPasswordPlainText);//用存储公钥加密新密码
			if (EEBeanUtils.isNULL(newPasswordCipherText))
				throw new EncryptException("修改密码前加密失败（空字符）");
			existCredential.setPassword(newPasswordCipherText);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		/* 保存到数据库，再根据保存结果写缓存或返回错误信息 */
		AdminUserCredential savedResult = super.save(existCredential);
		result.setSuccessful(savedResult.isSuccessful());
		if (savedResult.isSuccessful())
			SynAdminUserCredential2Redis.syn(getRedisClient(), savedResult);
		else
			result.addMessage(savedResult.getStrMessage());
		
		return result;
	}

	@Override
	public SimpleResponse resetAdminUserLoginPassword(String adminUserId) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(adminUserId)) {
			result.setSuccessful(false);
			result.addMessage("未指定要重置密码的服务人员标识("+this.getClass().getName()+")");
			return result;
		}
		
		/* 判断服务人员是否已设置过密码，没有则返回错误信息 */
		AdminUserCredential existCredential = retrieveAdminUserCredentialInfo(adminUserId);
		if (!existCredential.isSuccessful()) {
			result.setSuccessful(false);
			result.addMessage(existCredential.getStrMessage());
			return result;
		}
		
		/* 判断指定的服务人员是否存在 */
		AdminUserInfo existAdminUser = getAdminUserInfoBizService().get(adminUserId);
		if (!existAdminUser.isSuccessful() || EEBeanUtils.isNULL(existAdminUser.getAtid())) {
			result.setSuccessful(false);
			result.addMessage("未找到指定要重置登录密码对应的服务人员");
			return result;
		}
		
		/* 新密码加密 */
		try {
			String newPasswordPlainText = new SimpleDateFormat("YYYYMMdd").format(new Date());//用传输私钥解出新密码明文
			String newPasswordCipherText = RSAUtil.encrypt(getStorageRSAEncrypt(), newPasswordPlainText);//用存储公钥加密新密码
			if (EEBeanUtils.isNULL(newPasswordCipherText))
				throw new EncryptException("重置密码前加密失败（空字符）");
			existCredential.setPassword(newPasswordCipherText);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}

		/* 保存到数据库，再根据保存结果写缓存或返回错误信息 */
		AdminUserCredential savedResult = super.save(existCredential);
		result.setSuccessful(savedResult.isSuccessful());
		if (savedResult.isSuccessful())
			SynAdminUserCredential2Redis.syn(getRedisClient(), savedResult);
		else
			result.addMessage(savedResult.getStrMessage());
		
		return result;
	}
	
	@Override
	public AdminUserCredential retrieveAdminUserCredentialInfo (String adminUserId) {
		AdminUserCredential result = new AdminUserCredential();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(adminUserId)) {
			result.setSuccessful(false);
			result.addMessage("服务人员标识未知");
			return result;
		}
		
		/* 从数据库取秘钥对象 */
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("adminUser.atid",RangeType.EQUAL,adminUserId,null));
		SimpleResultSet<AdminUserCredential> existCredential = super.query(query, AdminUserCredential.class);
		if (!existCredential.isSuccessful()) {
			result.setSuccessful(false);
			result.addMessage(existCredential.getStrMessage());
			return result;
		}
		
		/* 根据取得的数据构建返回结果 */
		if (existCredential.getCount()==1 && existCredential.getResultSet().size()==1) {
			result = existCredential.getResultSet().get(0);
		} else {
			result.setSuccessful(false);
			result.addMessage("匹配到该服务人员设置了个"+existCredential.getResultSet().size()+"统一登录密码");
		}
		return result;
	}

	@Override
	public AdminUserCredential retrieveAdminUserSecretKey(String adminUserId) {
		AdminUserCredential result = new AdminUserCredential();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(adminUserId)) {
			result.addMessage("未指定服务人员标识");
			return result;
		}
		
		/* 从缓存取数据 */
		String ciphertext = SynAdminUserCredential2Redis.get(getRedisClient(), adminUserId);
		
		/* 从数据库取数据 */
		if (EEBeanUtils.isNULL(ciphertext)) {
			result = this.retrieveAdminUserCredentialInfo(adminUserId);
			if (result.isSuccessful())
				SynAdminUserCredential2Redis.syn(getRedisClient(), result);
			return result;
		}
		
		/* 缓存中有数据，分析加密算法和密文 */
		if (ciphertext.indexOf("RSA##")!=0 && ciphertext.indexOf("MD5##")!=0) {
			result.addMessage("服务人员密码类型（加密方式）未知");
			return result;
		} else if (ciphertext.indexOf("RSA##")==0){//RSA算法加密数据
			result.setEncryptionType("RSA");
		} else if (ciphertext.indexOf("MD5##")==0){//MD5算法加密数据
			result.setEncryptionType("MD5");
		}
		
		result.setPassword(ciphertext.substring(ciphertext.lastIndexOf("##")+2));
		result.setSuccessful(true);
		return result;
	}

	@Override
	public AdminUserCredential retrieveAdminUserSecretKey(String adminUserId, RSADecrypt decrypt) {
		/* 取秘钥密文（未取到或不是RSA密文都直接返回结果） */
		AdminUserCredential result = this.retrieveAdminUserSecretKey(adminUserId);
		if (!result.isSuccessful() || !"RSA".equals(result.getEncryptionType()))
			return result;
		
		/* 参数检查 */
		if (decrypt==null) {
			result.setSuccessful(false);
			result.addMessage("服务人员解密私钥未知");
			return result;
		}
		
		/* 密文解密 */
		try {
			String plaintext = RSAUtil.decrypt(decrypt, result.getPassword());
			if (EEBeanUtils.isNULL(plaintext))
				throw new EncryptException("解密密码失败（空字符）");
			result.setPassword(plaintext);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
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
		// TODO Auto-generated method stub
		return AdminUserCredential.class;
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

	/**
	 * @return the 数据存储加密公钥
	 */
	public RSAEncrypt getStorageRSAEncrypt() {
		return StorageRSAEncrypt;
	}

	/**
	 * @param storageRSAEncrypt the 数据存储加密公钥 to set
	 */
	public void setStorageRSAEncrypt(RSAEncrypt storageRSAEncrypt) {
		StorageRSAEncrypt = storageRSAEncrypt;
	}

	/**
	 * @return the 服务人员信息服务
	 */
	public AdminUserInfoBizService getAdminUserInfoBizService() {
		return adminUserInfoBizService;
	}

	/**
	 * @param adminUserInfoBizService the 服务人员信息服务 to set
	 */
	public void setAdminUserInfoBizService(AdminUserInfoBizService adminUserInfoBizService) {
		this.adminUserInfoBizService = adminUserInfoBizService;
	}

	/**
	 * @return the 数据传输解密私钥
	 */
	public RSADecrypt getTransferRSADecrypt() {
		return TransferRSADecrypt;
	}

	/**
	 * @param transferRSADecrypt the 数据传输解密私钥 to set
	 */
	public void setTransferRSADecrypt(RSADecrypt transferRSADecrypt) {
		TransferRSADecrypt = transferRSADecrypt;
	}

	/**
	 * @return the 数据存储解密私钥
	 */
	public RSADecrypt getStorageRSADecrypt() {
		return StorageRSADecrypt;
	}

	/**
	 * @param storageRSADecrypt the 数据存储解密私钥 to set
	 */
	public void setStorageRSADecrypt(RSADecrypt storageRSADecrypt) {
		StorageRSADecrypt = storageRSADecrypt;
	}
}
