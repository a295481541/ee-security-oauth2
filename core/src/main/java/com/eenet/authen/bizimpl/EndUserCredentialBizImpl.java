package com.eenet.authen.bizimpl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eenet.authen.BusinessSeries;
import com.eenet.authen.BusinessSeriesBizService;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserCredentialBizService;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.cacheSyn.SynEndUserCredential2Redis;
import com.eenet.authen.cacheSyn.SynEndUserLoginAccount2Redis;
import com.eenet.base.SimpleResponse;
import com.eenet.base.SimpleResultSet;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.cache.RedisClient;
import com.eenet.security.bizComponent.ReSetLoginPasswordCom;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.MD5Util;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;
/**
 * 最终用户登录秘钥服务实现逻辑
 * @author Orion
 * 2016年6月9日
 */
public class EndUserCredentialBizImpl extends SimpleBizImpl implements EndUserCredentialBizService {
	private static final Logger log = LoggerFactory.getLogger(EndUserCredentialBizImpl.class);
	private RedisClient RedisClient;//Redis客户端
	private RSAEncrypt StorageRSAEncrypt;//数据存储加密公钥
	private RSADecrypt StorageRSADecrypt;//数据存储解密私钥
	private RSADecrypt TransferRSADecrypt;//数据传输解密私钥
	private EndUserInfoBizService endUserInfoBizService;//最终用户信息服务
	private ReSetLoginPasswordCom reSetLoginPasswordCom;//重置密码业务组件
	private EndUserLoginAccountBizService endUserLoginAccountBizService;//最终用户账户服务
	private BusinessSeriesBizService businessSeriesBizService;//业务体系服务
	
	@Override
	public SimpleResponse initEndUserLoginPassword(EndUserCredential credential) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (credential == null) {
			result.setSuccessful(false);
			result.addMessage("要初始化的最终用户登录秘钥未知("+this.getClass().getName()+")");
			return result;
		} else if (credential.getBusinessSeries() == null ||EEBeanUtils.isNULL(credential.getBusinessSeries().getAtid())){
			result.setSuccessful(false);
			result.addMessage("要初始化的最终用户登录秘钥参数不全，业务体系必须指定("+this.getClass().getName()+")");
		}else if (EEBeanUtils.isNULL(credential.getPassword()) || credential.getEndUser()==null || EEBeanUtils.isNULL(credential.getEndUser().getAtid())) {
			result.setSuccessful(false);
			result.addMessage("要初始化的最终用户登录秘钥参数不全，END USER标识、登录秘钥均不可为空("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		
		/* 判断最终用户是否已设置过密码，有则返回错误信息 */
		EndUserCredential existCredential = this.retrieveEndUserCredentialInfo(credential.getBusinessSeries().getAtid() ,credential.getEndUser().getAtid());
		if (existCredential.isSuccessful()) {
			result.setSuccessful(false);
			result.addMessage("该最终用户已经设置过统一登录密码");
			return result;
		}
		
		/* 判断指定的最终用户是否存在 */
		EndUserInfo existEndUser = endUserInfoBizService.get(credential.getEndUser().getAtid());//TODO
		if ( !existEndUser.isSuccessful() ) {
			result.setSuccessful(false);
			result.addMessage("未找到指定要设置登录密码对应的最终用户("+existEndUser.getStrMessage()+")");
			return result;
		}
		
		/* 判断指定的业务体系是否存在 */
		BusinessSeries  businessSeries= businessSeriesBizService.retrieveBusinessSeries(credential.getBusinessSeries().getAtid(), null);
		
		if (businessSeries.isSuccessful() ) {
			result.setSuccessful(false);
			result.addMessage("未找到指定要设置登录密码对应的业务体系("+existEndUser.getStrMessage()+")");
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
		EndUserCredential savedResult = super.save(credential);
		result.setSuccessful(savedResult.isSuccessful());
		if (savedResult.isSuccessful())
			SynEndUserCredential2Redis.syn(getRedisClient(), savedResult);
		else
			result.addMessage(savedResult.getStrMessage());
		
		return result;
	}

	@Override
	public SimpleResponse changeEndUserLoginPassword(EndUserCredential curCredential, String newSecretKey) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (curCredential==null || EEBeanUtils.isNULL(newSecretKey)) {
			result.setSuccessful(false);
			result.addMessage("要修改的最终用户登录密码未知("+this.getClass().getName()+")");
			return result;
		}  else if (curCredential.getBusinessSeries() == null ||EEBeanUtils.isNULL(curCredential.getBusinessSeries().getAtid())){
			result.setSuccessful(false);
			result.addMessage("要初始化的最终用户登录秘钥参数不全，业务体系必须指定("+this.getClass().getName()+")");
		}else if (EEBeanUtils.isNULL(curCredential.getPassword()) || curCredential.getEndUser()==null || EEBeanUtils.isNULL(curCredential.getEndUser().getAtid())) {
			result.setSuccessful(false);
			result.addMessage("要修改的最终用户登录秘钥参数不全，END USER标识、当前登录密码均不可为空("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		
		/* 判断最终用户是否已设置过密码，没有则返回错误信息 */
		EndUserCredential existCredential = this.retrieveEndUserCredentialInfo(curCredential.getBusinessSeries().getAtid() , curCredential.getEndUser().getAtid());
		if (!existCredential.isSuccessful()) {
			result.setSuccessful(false);
			result.addMessage(existCredential.getStrMessage());
			return result;
		}
		
		/* 判断指定的最终用户是否存在 */
		EndUserInfo existEndUser = curCredential.getEndUser();
		if (EEBeanUtils.isNULL(existEndUser.getName()) && existEndUser.getMobile()==null) {//尝试从密码对象中判断人员姓名或手机是否已存在
			existEndUser = this.getEndUserInfoBizService().get(curCredential.getEndUser().getAtid());
			if (!existEndUser.isSuccessful() || EEBeanUtils.isNULL(existEndUser.getAtid())) {
				result.setSuccessful(false);
				result.addMessage("未找到指定要设置登录密码对应的最终用户");
				return result;
			}
		}
		
		
		/* 判断指定的业务体系是否存在 */
		BusinessSeries  businessSeries= businessSeriesBizService.retrieveBusinessSeries(curCredential.getBusinessSeries().getAtid(), null);
		
		if (businessSeries.isSuccessful() ) {
			result.setSuccessful(false);
			result.addMessage("未找到指定要设置登录密码对应的业务体系("+existEndUser.getStrMessage()+")");
			return result;
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
		EndUserCredential savedResult = super.save(existCredential);
		result.setSuccessful(savedResult.isSuccessful());
		if (savedResult.isSuccessful())
			SynEndUserCredential2Redis.syn(getRedisClient(), savedResult);
		else
			result.addMessage(savedResult.getStrMessage());
		
		return result;
	}
	
	
	
	@Override
	public SimpleResponse changeEndUserLoginPassword(EndUserCredential curCredential,EndUserLoginAccount account, String newSecretKey) {
		SimpleResponse result = new SimpleResponse();
		/* 参数检查 */
		if (curCredential==null || EEBeanUtils.isNULL(newSecretKey) || account ==null) {
			result.setSuccessful(false);
			result.addMessage("要修改的最终用户登录密码未知("+this.getClass().getName()+")");
			return result;
		} else if (curCredential.getBusinessSeries() == null ||EEBeanUtils.isNULL(curCredential.getBusinessSeries().getAtid())){
			result.setSuccessful(false);
			result.addMessage("要初始化的最终用户登录秘钥参数不全，业务体系必须指定("+this.getClass().getName()+")");
		} else if (EEBeanUtils.isNULL(curCredential.getPassword()) || curCredential.getEndUser()==null || EEBeanUtils.isNULL(curCredential.getEndUser().getAtid())) {
			result.setSuccessful(false);
			result.addMessage("要修改的最终用户登录秘钥参数不全，END USER标识、当前登录密码均不可为空("+this.getClass().getName()+")");
		}
		if (!result.isSuccessful())
			return result;
		
		/* 判断指定的最终用户是否存在 */
		EndUserInfo existEndUser = curCredential.getEndUser();
		if (EEBeanUtils.isNULL(existEndUser.getName()) && existEndUser.getMobile()==null) {//尝试从密码对象中判断人员姓名或手机是否已存在
			existEndUser = this.getEndUserInfoBizService().get(curCredential.getEndUser().getAtid());
			if (!existEndUser.isSuccessful() || EEBeanUtils.isNULL(existEndUser.getAtid())) {
				result.setSuccessful(false);
				result.addMessage("未找到指定要设置登录密码对应的最终用户");
				return result;
			}
		}
		
		
		/* 判断指定的业务体系是否存在 */
		BusinessSeries  businessSeries= businessSeriesBizService.retrieveBusinessSeries(curCredential.getBusinessSeries().getAtid(), null);
		
		if (businessSeries.isSuccessful() ) {
			result.setSuccessful(false);
			result.addMessage("未找到指定要设置登录密码对应的业务体系("+existEndUser.getStrMessage()+")");
			return result;
		}
		
		
		
		/* 获得传入原密码明文 */
		String passwordPlainText = null;//传入原密码明文
		try {
			log.info("get passwordPlainText start :" +curCredential.getPassword());
			passwordPlainText = RSAUtil.decryptWithTimeMillis(getTransferRSADecrypt(), curCredential.getPassword(), 2);
			log.info("passwordPlainText : " +passwordPlainText);
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		log.info("判断该用户是否有该账号 start");
		/* 判断该用户在该体系中是否有该账号 */
		EndUserLoginAccount existLoginAccount = endUserLoginAccountBizService.retrieveEndUserLoginAccountInfo(curCredential.getBusinessSeries().getAtid() ,account.getLoginAccount());
		
		if (existLoginAccount == null ) {
			result.setSuccessful(false);
			result.addMessage("未在体系中找到该账户："+account.getLoginAccount());
			return result;
		}
		
		log.info("判断该用户是否有该账号 end" +existLoginAccount);
		
		log.info("判断该用户是否有最终用户登录密码 start");
		/* 判断该用户在该体系中是否有最终用户登录密码 */
		EndUserCredential existCredential = getExistCredential(curCredential.getBusinessSeries().getAtid(),existEndUser.getAtid());
		log.info("判断该用户是否有最终用户登录密码 end"+existCredential);
		
		boolean isAccountMatch = false;
		boolean isCredentialMatch = false;
		
		try {
			if (existCredential !=null ) {//有最终用户登录密码
				isCredentialMatch = isPasswordMatch(existCredential.getEncryptionType(), existCredential.getPassword(), passwordPlainText);
			}
			
			if(existLoginAccount != null){//有该账户
				if (existEndUser.getAtid().equals(existLoginAccount.getUserInfo().getAtid())) {
					if (!EEBeanUtils.isNULL(existLoginAccount.getAccountLoginPassword()) ) //没有指定密码
						isAccountMatch = isPasswordMatch(existLoginAccount.getEncryptionType(),existLoginAccount.getAccountLoginPassword(), passwordPlainText);
				}else{
					result.setSuccessful(false);
					result.addMessage("账户："+existLoginAccount.getAccountLoginPassword()+"指定错误！("+this.getClass().getName()+")");
					return result;
				}
			}
			
			if (!isAccountMatch && !isCredentialMatch ) {
				result.setSuccessful(false);
				result.addMessage("原密码不正确！("+this.getClass().getName()+")");
				return result;
			}
			
		} catch (EncryptException e) {
			log.info("isAccountMatch" +isAccountMatch);
			log.info("isCredentialMatch" +isCredentialMatch);
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		
		log.info("isAccountMatch" +isAccountMatch);
		log.info("isCredentialMatch" +isCredentialMatch);
		
		/* 新密码加密 */
		try {
			String newPasswordPlainText = RSAUtil.decrypt(getTransferRSADecrypt(), newSecretKey);//用传输私钥解出新密码明文
			String newPasswordCipherText = RSAUtil.encrypt(getStorageRSAEncrypt(), newPasswordPlainText);//用存储公钥加密新密码
			log.info("newPasswordCipherText" +newPasswordCipherText);
			if (EEBeanUtils.isNULL(newPasswordCipherText))
				throw new EncryptException("修改密码前加密失败（空字符）");

			if (isAccountMatch ||(!isAccountMatch && isCredentialMatch )  || (isCredentialMatch &&EEBeanUtils.isNULL(existLoginAccount.getAccountLoginPassword()) )) {// 命中 或者  没命中但是S表中存在  或者是有账号没密码
				existLoginAccount.setAccountLoginPassword(newPasswordCipherText);
				log.info("existLoginAccount store to db ...");
				EndUserLoginAccount savedResult = endUserLoginAccountBizService.save(existLoginAccount);
				log.info("existLoginAccount stored to db");
				result.setSuccessful(savedResult.isSuccessful());
				
				if (savedResult.isSuccessful())
					SynEndUserLoginAccount2Redis.syn(getRedisClient(), savedResult);
				else
					result.addMessage(savedResult.getStrMessage());
				
				log.info("existLoginAccount updated or created");
			}
			
			if (existCredential == null) { //不存在登陆密码时创建登陆密码
				existCredential = new EndUserCredential();
				existCredential.setBusinessSeries(businessSeries);
				existCredential.setEndUser(existEndUser);
				existCredential.setPassword(newPasswordCipherText);
				/* 保存到数据库，再根据保存结果写缓存或返回错误信息 */
				log.info("existCredential store to db ...");
				EndUserCredential savedResult = super.save(existCredential);
				log.info("existCredential stored to db");
				result.setSuccessful(savedResult.isSuccessful());
				if (savedResult.isSuccessful())
					SynEndUserCredential2Redis.syn(getRedisClient(), savedResult);
				else
					result.addMessage(savedResult.getStrMessage());
				log.info("existCredential updated or created");
			}
			
		} catch (EncryptException e) {
			result.setSuccessful(false);
			result.addMessage(e.toString());
			return result;
		}
		
		return result;
	}
	
	
	
	
	private EndUserCredential getExistCredential (String seriesId,String userId){
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("endUser.atid",RangeType.EQUAL,userId,null));
		query.addCondition(new ConditionItem("businessSeries.atid",RangeType.EQUAL,seriesId,null));
		SimpleResultSet<EndUserCredential> existCredential = super.query(query, EndUserCredential.class);
		if (existCredential.isSuccessful() &&existCredential.getResultSet().size()>0) {
			return existCredential.getResultSet().get(0);
		}
		return null;
	}
	
	
	
	/* 
	 * 判断原有密码是否能匹配，不对则返回错误信息
	 * 根据加密方式进行不同的密码匹配
	 */
	private boolean isPasswordMatch(String encryptionType,String existPasswordPlainText,String passwordPlainText) throws EncryptException{
		
		if (encryptionType.equals("RSA")) {
			String exit = RSAUtil.decrypt(getStorageRSADecrypt(),existPasswordPlainText);
			return !(EEBeanUtils.isNULL(exit) || !passwordPlainText.equals(exit));
		} else if (encryptionType.equals("MD5")) {
			String passwordPlainTextMD5 = MD5Util.encrypt(passwordPlainText);//对传入原密码进行md5加密
			return !(EEBeanUtils.isNULL(passwordPlainTextMD5) || !passwordPlainTextMD5.equals(existPasswordPlainText));
		} else {
			throw new EncryptException("加密方式未知["+encryptionType+"]("+this.getClass().getName()+")");
		}
	}
	

	@Override
	public SimpleResponse resetEndUserLoginPassword(String seriesId, String endUserId) {
		
		String newPasswordPlainText = new SimpleDateFormat("YYYYMMdd").format(new Date());//重置的密码
		return getReSetLoginPasswordCom().resetEndUserLoginPassword(seriesId,endUserId, newPasswordPlainText,
				getStorageRSAEncrypt());
	}
	
	@Override
	public EndUserCredential retrieveEndUserCredentialInfo (String seriesId, String endUserId) {
		EndUserCredential result = new EndUserCredential();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(endUserId)) {
			result.setSuccessful(false);
			result.addMessage("最终用户标识未知");
			return result;
		}
		/* 从数据库取秘钥对象 */
		QueryCondition query = new QueryCondition();
		query.addCondition(new ConditionItem("endUser.atid",RangeType.EQUAL,endUserId,null));
		query.addCondition(new ConditionItem("businessSeries.atid",RangeType.EQUAL,seriesId,null));
		SimpleResultSet<EndUserCredential> existCredential = super.query(query, EndUserCredential.class);
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
			result.addMessage("匹配到该最终用户在该体系中设置了个"+existCredential.getResultSet().size()+"统一登录密码");
		}
		return result;
	}

	@Override
	public EndUserCredential retrieveEndUserSecretKey(String seriesId, String endUserId) {
		EndUserCredential result = new EndUserCredential();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(endUserId)) {
			result.setSuccessful(false);
			result.addMessage("未指定最终用户标识");
		}
		
		/* 从缓存取数据 */
		String ciphertext = SynEndUserCredential2Redis.get(getRedisClient(), endUserId+":"+seriesId);
		
		
		
		/* 从数据库取数据 */
		if (EEBeanUtils.isNULL(ciphertext)) {
			result = this.retrieveEndUserCredentialInfo(seriesId ,endUserId);
			if (result.isSuccessful())
				SynEndUserCredential2Redis.syn(getRedisClient(), result);
			return result;
		}
		
		/* 缓存中有数据，分析加密算法和密文 */
		if (ciphertext.indexOf("RSA##")!=0 && ciphertext.indexOf("MD5##")!=0) {
			result.addMessage("最终用户密码类型（加密方式）未知");
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
	public EndUserCredential retrieveEndUserSecretKey(String seriesId, String endUserId, RSADecrypt decrypt) {
		
		/* 取秘钥密文（未取到或不是RSA密文都直接返回结果） */
		
//		EndUserCredential result = this.retrieveEndUserCredentialInfo(endUserId);
		EndUserCredential result = this.retrieveEndUserSecretKey(seriesId ,endUserId);
		
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
			
			String  plaintext = RSAUtil.decrypt(decrypt, result.getPassword());
			
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
		return EndUserCredential.class;
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
	 * @return the 重置密码业务组件
	 */
	public ReSetLoginPasswordCom getReSetLoginPasswordCom() {
		return reSetLoginPasswordCom;
	}

	/**
	 * @param reSetLoginPasswordCom the 重置密码业务组件 to set
	 */
	public void setReSetLoginPasswordCom(ReSetLoginPasswordCom reSetLoginPasswordCom) {
		this.reSetLoginPasswordCom = reSetLoginPasswordCom;
	}

	public EndUserLoginAccountBizService getEndUserLoginAccountBizService() {
		return endUserLoginAccountBizService;
	}

	public void setEndUserLoginAccountBizService(EndUserLoginAccountBizService endUserLoginAccountBizService) {
		this.endUserLoginAccountBizService = endUserLoginAccountBizService;
	}

	public BusinessSeriesBizService getBusinessSeriesBizService() {
		return businessSeriesBizService;
	}

	public void setBusinessSeriesBizService(BusinessSeriesBizService businessSeriesBizService) {
		this.businessSeriesBizService = businessSeriesBizService;
	}
}
