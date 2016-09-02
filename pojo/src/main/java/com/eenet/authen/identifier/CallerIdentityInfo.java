package com.eenet.authen.identifier;

/**
 * 调用者者身份认证信息
 * 2016年8月15日
 * @author Orion
 */
public class CallerIdentityInfo {
	private static final ThreadLocal<String> accessToken = new ThreadLocal<String>();//用户访问令牌
	private static final ThreadLocal<String> appSecretKey = new ThreadLocal<String>();//app秘钥（加密）
	private static final ThreadLocal<String> redirectURI = new ThreadLocal<String>();//app跳转地址（仅web应用有效）
	/**
	 * @return the 用户访问令牌
	 */
	public static String getAccesstoken() {
		return accessToken.get();
	}
	/**
	 * @param accesstoken the 用户访问令牌 to set
	 */
	public static void setAccesstoken(String accesstoken) {
		accessToken.set(accesstoken);
	}
	/**
	 * @return the app秘钥（加密）
	 */
	public static String getAppsecretkey() {
		return appSecretKey.get();
	}
	/**
	 * @param appsecretkey the app秘钥（加密） to set
	 */
	public static void setAppsecretkey(String appsecretkey) {
		appSecretKey.set(appsecretkey);
	}
	/**
	 * @return the app跳转地址（仅web应用有效）
	 */
	public static String getRedirecturi() {
		return redirectURI.get();
	}
	/**
	 * @param redirecturi the app跳转地址（仅web应用有效） to set
	 */
	public static void setRedirecturi(String redirecturi) {
		redirectURI.set(redirecturi);
	}
}
