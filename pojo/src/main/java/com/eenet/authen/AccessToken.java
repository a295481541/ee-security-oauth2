package com.eenet.authen;

import com.eenet.base.SimpleResponse;
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
	private EENetEndUserMainAccount mainAccount;//用户主账号
	
	/**
	 * 访问授权码
	 * 有效期：web应用5分钟、native应用7天，每次访问自动续期
	 * 2016年4月15日
	 * @author Orion
	 */
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	/**
	 * 重新获得访问授权码的授权码
	 * 有效期：30天
	 * @return
	 * 2016年4月15日
	 * @author Orion
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	/**
	 * 当前用户主账号
	 * @return
	 * 2016年4月21日
	 * @author Orion
	 */
	public EENetEndUserMainAccount getMainAccount() {
		return mainAccount;
	}
	public void setMainAccount(EENetEndUserMainAccount mainAccount) {
		this.mainAccount = mainAccount;
	}
}
