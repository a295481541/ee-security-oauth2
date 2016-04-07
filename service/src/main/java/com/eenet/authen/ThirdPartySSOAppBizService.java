package com.eenet.authen;

import com.eenet.base.SimpleResponse;

/**
 * 第三方单点登录系统服务
 * 2016年4月7日
 * @author Orion
 */
public interface ThirdPartySSOAppBizService {
	/**
	 * 单点登录系统注册
	 * @param app 设置除appId以外的其他必要信息
	 * @return 附带appId，不带身份认证秘钥
	 * 2016年3月30日
	 * @author Orion
	 */
	public ThirdPartySSOAPP registeThirdPartySSOApp(ThirdPartySSOAPP app);
	/**
	 * 废弃单点登录系统
	 * @param code
	 * @return
	 * 2016年3月30日
	 * @author Orion
	 */
	public SimpleResponse removeThirdPartySSOApp(String... appIds);
	/**
	 * 取得单点登录系统
	 * @param appId
	 * @return
	 * 2016年4月7日
	 * @author Orion
	 */
	public ThirdPartySSOAPP retrieveThirdPartySSOApp(String appId);
}
