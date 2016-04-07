package com.eenet.authen;

import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.util.cryptography.RSADecrypt;

/**
 * 最终用户登录秘钥服务
 * 2016年4月7日
 * @author Orion
 */
public interface EENetEndUserCredentialBizService {
	/**
	 * 初始化用户登录密码
	 * @param mainAccount
	 * @param credential 
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse initUserLoginPassword(EENetEndUserCredential credential);
	
	/**
	 * 修改用户登录密码
	 * @param mainAccount
	 * @param curCredential
	 * @param newSecretKey
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse changeUserLoginPassword(EENetEndUserCredential curCredential, String newSecretKey);
	
	/**
	 * 重置用户登录密码（适合忘记密码）
	 * @param mainAccount
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse resetUserLoginPassword(EENetEndUserMainAccount mainAccount);
	
	/**
	 * 获得用户登录密码（密文）
	 * @param mainAccount 解密参数
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	public StringResponse retrieveUserSecretKey(String mainAccount);
	
	/**
	 * 获得用户登录密码（明文）
	 * @param mainAccount 主账号
	 * @param redisRSADecrypt 解密参数
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	public StringResponse retrieveUserSecretKey(String mainAccount, RSADecrypt redisRSADecrypt);
}
