package com.eenet.authen;

import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.util.cryptography.RSADecrypt;

/**
 * 最终用户登录秘钥服务
 * 2016年4月7日
 * @author Orion
 */
public interface EndUserCredentialBizService {
	/**
	 * 初始化用户登录密码
	 * @param credential 登录密码属性必须是：带时间戳加密的形式
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse initEndUserLoginPassword( EndUserCredential credential);
	
	/**
	 * 修改用户主登录密码
	 * @param curCredential 登录密码属性必须是：带时间戳加密的形式
	 * @param newSecretKey 新密码：明文直接加密的形式（不带时间戳）
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse changeEndUserLoginPassword(EndUserCredential curCredential, String newSecretKey);
	
	/**
	 * 修改用户主登录密码和账号私有密码
	 * 当curCredential匹配到用户当前的公共密码 或者 账号的私有密码时，同时修改公共密码和账号私有密码（没有则新增）
	 * @param curCredential 登录密码属性必须是：带时间戳加密的形式，endUser属性的atid为目标用户标识
	 * @param account 登陆账户，为空则表示只修改用户主登录密码
	 * @param newSecretKey 新密码：明文直接加密的形式（不带时间戳）
	 * @return false:主登录密码和账号私有密码均修改失败；true:主登录密码和账号私有密码任一修改成功
	 * 2016年12月9日
	 * @author Orion
	 */
	public SimpleResponse changeEndUserLoginPassword(EndUserCredential curCredential,EndUserLoginAccount account, String newSecretKey);
	
	/**
	 * 重置用户登录密码（适合忘记密码），新密码为当前日期
	 * @param endUserId 用户标识
	 * @return
	 * 2016年3月31日
	 * @author Orion
	 */
	public SimpleResponse resetEndUserLoginPassword( String endUserId);
	
	/**
	 * 获得最终用户秘钥信息
	 * @param endUserId 最终用户标识
	 * @return
	 * 2016年6月9日
	 * @author Orion
	 */
	public EndUserCredential retrieveEndUserCredentialInfo (String seriesId , String endUserId);
	/**
	 * 获得用户登录密码（密文）
	 * @param endUserId 用户标识
	 * @return 返回对象只包含加密方式和密码密文
	 * 2016年4月7日
	 * @author Orion
	 */
	public EndUserCredential retrieveEndUserSecretKey(String seriesId , String endUserId);
	
	/**
	 * 获得用户登录密码（明文）
	 * @param endUserId 用户标识
	 * @param redisRSADecrypt 解密参数（如果确定不是RSA加密则可空，否则不为空）
	 * @return 如果是RSA加密形式则返回明文，否则返回密文。返回对象只包含加密方式和密码明文（或密文）
	 * 2016年4月7日
	 * @author Orion
	 */
	public EndUserCredential retrieveEndUserSecretKey(String seriesId ,String endUserId, RSADecrypt decrypt);
}
