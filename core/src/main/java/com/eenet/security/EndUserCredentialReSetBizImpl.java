package com.eenet.security;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.BooleanResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.security.bizComponent.ReSetLoginPasswordCom;
import com.eenet.user.EndUserInfoBizService;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class EndUserCredentialReSetBizImpl implements EndUserCredentialReSetBizService {

	@Override
	public StringResponse sendSMSCode4ResetPassword(String appId, long mobile) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || mobile<10000000000l) {
			result.addMessage("接入应用标识或手机号不正确("+this.getClass().getName()+")");
		}
		
		/* 检查该手机是否一分钟内发送过短信 */
		
		/* get enduser id */
		
		/* 生成短信验证码并缓存 */
		
		/* 发送短信 */
		
		return null;
	}

	@Override
	public BooleanResponse validateSMSCode4ResetPassword(String endUserId, String smsCode, boolean rmSmsCode) {
		
		/* 参数检查 */
		
		/* 校验短信验证码 */
		
		/* 删除短信验证码（如需要） */
		
		return null;
	}

	@Override
	public AccessToken resetPasswordBySMSCodeWithLogin(AppAuthenRequest appRequest, EndUserCredential curCredential,
			String smsCode) {
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		/* 参数检查 */
		
		/* 使用短信验证码重置密码 */
		SimpleResponse resetRS = resetPasswordBySMSCodeWithoutLogin(curCredential, smsCode);
		if (!resetRS.isSuccessful()) {
			result.addMessage(resetRS.getStrMessage());
			return result;
		}
		
		/* 获得当前用户登录账号（取一个账号） */
		
		/* 获得认证授权码 */
		
		/* 获得访问令牌 */
		return null;
	}

	@Override
	public SimpleResponse resetPasswordBySMSCodeWithoutLogin(EndUserCredential credential, String smsCode) {
		/* 参数检查 */
		
		/* 校验并删除短信验证码 */
		
		/* 重置用户登录密码 */
		String newPasswordPlainText = null;
		try {
			newPasswordPlainText = RSAUtil.decrypt(getTransferRSADecrypt(), credential.getPassword());//用传输私钥解出新密码明文
		} catch (EncryptException e) {
			e.printStackTrace();
		}
		getResetLoginPasswordCom().resetEndUserLoginPassword(credential.getEndUser().getAtid(),
				newPasswordPlainText, getStorageRSAEncrypt());
		return null;
	}
	
	private EndUserInfoBizService endUserInfoBizService;
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	private ReSetLoginPasswordCom resetLoginPasswordCom;//重置密码业务组件
	private RSAEncrypt StorageRSAEncrypt;//数据存储加密公钥
	private RSADecrypt TransferRSADecrypt;//数据传输解密私钥
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
	}

	public EndUserLoginAccountBizService getEndUserLoginAccountBizService() {
		return endUserLoginAccountBizService;
	}

	public void setEndUserLoginAccountBizService(EndUserLoginAccountBizService endUserLoginAccountBizService) {
		this.endUserLoginAccountBizService = endUserLoginAccountBizService;
	}

	/**
	 * @return the 重置密码业务组件
	 */
	public ReSetLoginPasswordCom getResetLoginPasswordCom() {
		return resetLoginPasswordCom;
	}

	/**
	 * @param resetLoginPasswordCom the 重置密码业务组件 to set
	 */
	public void setResetLoginPasswordCom(ReSetLoginPasswordCom resetLoginPasswordCom) {
		this.resetLoginPasswordCom = resetLoginPasswordCom;
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
}
