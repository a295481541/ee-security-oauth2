package com.eenet.authen.bizimpl;

import com.eenet.authen.EENetEndUserCredentialBizService;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.ServiceConsumerBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.SingleSignOnBizService;
import com.eenet.authen.ThirdPartyAPPType;
import com.eenet.authen.ThirdPartySSOAPP;
import com.eenet.authen.ThirdPartySSOAppBizService;
import com.eenet.base.StringResponse;
import com.eenet.base.dao.BaseDAOService;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAUtil;

public class SingleSignOnBizImpl implements SingleSignOnBizService {
	private IdentityAuthenticationBizService authenService;
	private EndUserLoginAccountBizService loginAccountService;
	private EENetEndUserCredentialBizService userCredentialService;
	private ServiceConsumerBizService consumerService;
	private ThirdPartySSOAppBizService SSOAppService;
	private String SSOSystemConsumerCode;
	private RSADecrypt redisRSADecrypt;
	private BaseDAOService DAOService;
	private RedisClient redisClient;

	@Override
	public SignOnGrant getSignOnGrant(ServiceConsumer SSOSystem, String appId, String redirectURI, String endUserLoginAccount,
			String endUserPassword) {
		SignOnGrant grant = new SignOnGrant();
		grant.setSuccessful(false);
		/* 参数检查 */
		
		/* 单点登录系统身份认证、权限校验 */
		if (!this.validateSSOSystem(SSOSystem)) {
			grant.addMessage("不是合法的单点登录系统");
			return grant;
		}
		
		/* 检查第三方SSO系统是否存在，跳转地址是否合法(仅web应用) */
		if (!this.existSSOApp(appId, redirectURI)){
			grant.addMessage("不是合法的业务系统");
			return grant;
		}
		
		/* 最终用户身份认证 */
		if (this.validateEndUserCredential(endUserLoginAccount, endUserPassword)){
			grant.addMessage("登录用户的账号、密码不匹配");
			return grant;
		}
		
		/* 生成code */
		String code = this.generateNCacheGrantCode(appId);
		if (EEBeanUtils.isNULL(code)) {
			grant.addMessage("无法生成登录授权码");
			return grant;
		}
		
		grant.setGrantCode(code);
		grant.setSuccessful(true);
		return grant;
	}
	
	/**
	 * 单点登录系统身份认证、权限校验
	 * @param SSOSystem
	 * 2016年4月7日
	 * @author Orion
	 */
	private boolean validateSSOSystem(ServiceConsumer SSOSystem) {
		/* 参数检查 */
		if (EEBeanUtils.isNULL(SSOSystem.getSecretKey()) || EEBeanUtils.isNULL(SSOSystem.getCode()))
			return false;
		
		/* 不是合法的单点登录系统 */
		if (!SSOSystem.getCode().equals(getSSOSystemConsumerCode()))
			return false;
		
		ServiceConsumer existConsumer = getConsumerService().retrieveServiceConsumer(SSOSystem.getCode());
		if(!existConsumer.isSuccessful())
			return false;
		try {
			String plaintext = RSAUtil.decrypt(getRedisRSADecrypt(), existConsumer.getSecretKey());
			if (!EEBeanUtils.isNULL(plaintext) && plaintext.equals(SSOSystem.getSecretKey()))
				return true;
			else
				return false;
		} catch (EncryptException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 检查第三方SSO系统是否存在
	 * 跳转地址是否合法(仅web应用)
	 * @param appId
	 * @param redirectURI 跳转地址（非web系统可空）
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	private boolean existSSOApp(String appId, String redirectURI) {
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId))
			return false;
		
		ThirdPartySSOAPP app = getSSOAppService().retrieveThirdPartySSOApp(appId);
		if (!app.isSuccessful())//不存在此第三方系统
			return false;
		
		if (app.getAppType().equals(ThirdPartyAPPType.WEBAPP)) {
			if (EEBeanUtils.isNULL(redirectURI))
				return false;
			return redirectURI.indexOf(app.getRedirectURIPrefix())==0;
		}
		return true;
	}
	
	/**
	 * 校验最终用户账号、密码
	 * 2016年4月3日
	 * @author Orion
	 */
	private boolean validateEndUserCredential(String loginAccount, String password) {
		/* **************************************** 取主账号 **************************************** */
		String mainAccount = null;
		StringResponse mainAccountResponse = getLoginAccountService().retrieveEndUserMainAccount(loginAccount);
		if (mainAccountResponse.isSuccessful() && !EEBeanUtils.isNULL(mainAccountResponse.getResult()))
			mainAccount = mainAccountResponse.getResult();
		else
			return false;
		
		/* **************************************** 取秘钥 **************************************** */
		String secretKey = null;
		StringResponse secretKeyResponse = getUserCredentialService().retrieveUserSecretKey(mainAccount, getRedisRSADecrypt());
		if (secretKeyResponse.isSuccessful() && !EEBeanUtils.isNULL(secretKeyResponse.getResult()))
			secretKey = secretKeyResponse.getResult();
		else
			return false;
		
		/* 秘钥匹配 */
		if (secretKey.equals(password))
			return true;
		else
			return false;
	}
	/**
	 * 生成并缓存授权码
	 * @param appId 第三方业务系统id
	 * @return 授权码或null（如校验或生成失败）
	 * @throws RedisOPException
	 * 2016年4月2日
	 * @author Orion
	 */
	private String generateNCacheGrantCode(String appId) {
		try {
			String code = EEBeanUtils.getUUID();
			getRedisClient().setObject(CacheKey.AUTHEN_CODE_PREFIX+code, appId, 60*2);
			return code;
		} catch (RedisOPException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/****************************************************************************
	**                                                                         **
	**                           Getter & Setter                               **
	**                                                                         **
	****************************************************************************/

	public IdentityAuthenticationBizService getAuthenService() {
		return authenService;
	}

	public void setAuthenService(IdentityAuthenticationBizService authenService) {
		this.authenService = authenService;
	}

	public RedisClient getRedisClient() {
		return redisClient;
	}

	public void setRedisClient(RedisClient redisClient) {
		this.redisClient = redisClient;
	}
	
	/**
	 * 利用IOC机制，通过此方法注入数据库操作服务对象。
	 * @param daoService 数据库操作服务对象
	 */
	public void setDAOService(BaseDAOService daoService){
		this.DAOService = daoService;
	}
	/**
	 * 获得数据库操作服务对象。
	 * @return 数据库操作服务对象
	 */
	public BaseDAOService getDAOService(){
		return this.DAOService;
	}
	
	public RSADecrypt getRedisRSADecrypt() {
		return redisRSADecrypt;
	}

	public void setRedisRSADecrypt(RSADecrypt redisRSADecrypt) {
		this.redisRSADecrypt = redisRSADecrypt;
	}

	public EndUserLoginAccountBizService getLoginAccountService() {
		return loginAccountService;
	}

	public void setLoginAccountService(EndUserLoginAccountBizService loginAccountService) {
		this.loginAccountService = loginAccountService;
	}

	public EENetEndUserCredentialBizService getUserCredentialService() {
		return userCredentialService;
	}

	public void setUserCredentialService(EENetEndUserCredentialBizService userCredentialService) {
		this.userCredentialService = userCredentialService;
	}

	public String getSSOSystemConsumerCode() {
		return SSOSystemConsumerCode;
	}

	public void setSSOSystemConsumerCode(String sSOSystemConsumerCode) {
		SSOSystemConsumerCode = sSOSystemConsumerCode;
	}

	public ServiceConsumerBizService getConsumerService() {
		return consumerService;
	}

	public void setConsumerService(ServiceConsumerBizService consumerService) {
		this.consumerService = consumerService;
	}

	public ThirdPartySSOAppBizService getSSOAppService() {
		return SSOAppService;
	}

	public void setSSOAppService(ThirdPartySSOAppBizService sSOAppService) {
		SSOAppService = sSOAppService;
	}
}
