package com.eenet.authen;

import com.eenet.base.SimpleResponse;
import com.eenet.user.UserInfo;
/**
 * 访问授权码
 * 业务规则：
 * web应用5分钟内、native应用7天内无操作则accessToken失效并且每次操作可自动续期，通过refreshToken重新获取
 * 适用场景：
 * 只要30天内有访问系统，则可以持续保持自动登录
 * 2016年4月15日
 * @author Orion
 */
public class AccessToken extends SimpleResponse {
	private static final long serialVersionUID = 102180810214540219L;
	private String accessToken;//访问授权码
	private String refreshToken;//刷新授权码
	private UserInfo userInfo;//用户信息
	/**
	 * @return the 访问授权码
	 */
	public String getAccessToken() {
		return accessToken;
	}
	/**
	 * @param accessToken the 访问授权码 to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	/**
	 * @return the 刷新授权码
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
	/**
	 * @param refreshToken the 刷新授权码 to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	/**
	 * @return the 用户信息
	 */
	public UserInfo getUserInfo() {
		return userInfo;
	}
	/**
	 * @param userInfo the 用户信息 to set
	 */
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	
}
