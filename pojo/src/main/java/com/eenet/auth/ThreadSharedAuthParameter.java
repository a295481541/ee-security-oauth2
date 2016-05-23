package com.eenet.auth;

import com.eenet.authen.AccessToken;
import com.eenet.authen.EENetEndUserMainAccount;

/**
 * 线程共享的认证、授权变量
 * 2016年5月20日
 * @author Orion
 */
public final class ThreadSharedAuthParameter {
	/**
	 * 当前最终用户
	 */
	public static final ThreadLocal<EENetEndUserMainAccount> CurEndUserMainAccount = new ThreadLocal<EENetEndUserMainAccount>();
	/**
	 * 当前最终用户的令牌（临时、该变量即将删掉并转移到redis）
	 */
	public static final ThreadLocal<AccessToken> CurEndUserToken = new ThreadLocal<AccessToken>();
	
	private ThreadSharedAuthParameter() {
	}
}
