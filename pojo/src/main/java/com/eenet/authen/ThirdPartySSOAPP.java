package com.eenet.authen;

import com.eenet.base.BaseEntity;
/**
 * 单点登录系统
 * @author Orion
 */
public class ThirdPartySSOAPP extends BaseEntity {
	private static final long serialVersionUID = 8516730683890875834L;
	private String redirectURIPrefix;//跳转地址前缀
	private String secretKey;//应用秘钥
	private String appName;//应用中文名称
	private ThirdPartyAPPType appType;//应用类型
	/**
	 * 应用id
	 * 2016年3月30日
	 * @author Orion
	 */
	public String getAppId() {
		return super.getAtid();
	}
	public void setAppId(String appId) {
		this.setAtid(appId);
	}
	
	/**
	 * 跳转地址前缀
	 * 含协议，如：http://
	 * 2016年3月30日
	 * @author Orion
	 */
	public String getRedirectURIPrefix() {
		return redirectURIPrefix;
	}
	public void setRedirectURIPrefix(String redirectURIPrefix) {
		this.redirectURIPrefix = redirectURIPrefix;
	}
	
	/**
	 * 应用秘钥
	 * 2016年3月30日
	 * @author Orion
	 */
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	/**
	 * 应用中文名称
	 * 2016年3月30日
	 * @author Orion
	 */
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	/**
	 * 应用类型
	 * 2016年3月30日
	 * @author Orion
	 */
	public ThirdPartyAPPType getAppType() {
		return appType;
	}
	public void setAppType(ThirdPartyAPPType appType) {
		this.appType = appType;
	}
}
