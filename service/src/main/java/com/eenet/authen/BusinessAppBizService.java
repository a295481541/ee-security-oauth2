package com.eenet.authen;

import com.eenet.base.SimpleResponse;

/**
 * 业务系统服务
 * 2016年4月7日
 * @author Orion
 */
public interface BusinessAppBizService {//TODO 
	/**
	 * 注册业务系统
	 * @param app 设置除appId以外的其他必要信息，对象中的秘钥以明文方式表示
	 * @return 附带appId，不带身份认证秘钥
	 * 2016年3月30日
	 * @author Orion
	 */
	public BusinessApp registeApp(BusinessApp app);
	/**
	 * 废弃业务系统
	 * @param code
	 * @return
	 * 2016年3月30日
	 * @author Orion
	 */
	public SimpleResponse removeApp(String... appIds);
	/**
	 * 取得业务系统
	 * @param appId
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	public BusinessApp retrieveApp(String appId);
}
