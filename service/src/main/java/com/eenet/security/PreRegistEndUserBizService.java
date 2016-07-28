package com.eenet.security;

import com.eenet.base.BooleanResponse;
import com.eenet.baseinfo.user.EndUserInfo;

/**
 * 注册新用户服务预先检查服务
 * 2016年7月28日
 * @author Orion
 */
public interface PreRegistEndUserBizService {
	/**
	 * 新增最终用户前检查，检查是否存在相同的手机、电子邮箱或身份证号
	 * @param mobile 手机
	 * @param email 邮箱
	 * @param idCard 身份证
	 * @return true表示存在
	 * 2016年6月16日
	 * @author Orion
	 */
	public BooleanResponse existMobileEmailId(String mobile, String email, String idCard);
	
	/**
	 * 根据手机、电子邮箱或身份证号获得最终用户个人信息
	 * @param mobile 手机
	 * @param email 邮箱
	 * @param idCard 身份证
	 * @return 如果找个一条符合记录的数据则返回，其他情况均返回错误信息
	 * 2016年6月22日
	 * @author Orion
	 */
	public EndUserInfo getByMobileEmailId(String mobile, String email, String idCard);
}
