package com.eenet.authen.extension.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.common.OPOwner;
import com.eenet.util.EEBeanUtils;

/**
 * 收集当前应用和用户身份信息
 * 支持从cookie和请求参数两个渠道获得身份信息
 * ★当cookie和请求参数有相同属性时以请求参数为准
 * ★当无用户信息时该过滤器不作任何跳转，收集到的用户身份记录在以下对象
 * @see com.eenet.common.OPOwner
 * @see com.eenet.authen.identifier.CallerIdentityInfo
 * 
 * 2016年8月24日
 * @author Orion
 */
public class GatherAppNUserIdentifierFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		/* 将当前身份设置为默认值 */
		OPOwner.reset();
		CallerIdentityInfo.reset();
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		
		this.injectCallerAppAndUser(httpRequest);
		
		chain.doFilter(request, response);
	}
	
	private void injectCallerAppAndUser(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		
		/* 尝试从cookie中获得当前用户和当前应用信息（应用标识、应用秘钥、应用跳转地址、当前用户标识、当前用户访问令牌、用户类型） */
		if (cookies!=null && cookies.length>0) {
			for (Cookie c : cookies) {
				if ( UserId_PARAM_TAG.equals(c.getName()) ) //当前用户标识
					OPOwner.setCurrentUser(c.getValue());
				
				if ( UserAccessToken_PARAM_TAG.equals(c.getName()) ) //当前用户访问令牌
					CallerIdentityInfo.setAccesstoken(c.getValue());
				
				if ( UserType_PARAM_TAG.equals(c.getName()) ) //用户类型
					OPOwner.setUsertype(c.getValue());
				
				if ( SeriesID_PARAM_TAG.equals(c.getName()) )
					OPOwner.setCurrentSeries(c.getValue());
			}
		}
		
		
		/* 请求参数中逐一获取 */
		if ( !EEBeanUtils.isNULL(request.getParameter(AppID_PARAM_TAG)) )
			OPOwner.setCurrentSys( request.getParameter(AppID_PARAM_TAG) );
		
		if ( !EEBeanUtils.isNULL(request.getParameter(AppSecretKey_PARAM_TAG)) )
			CallerIdentityInfo.setAppsecretkey( request.getParameter(AppSecretKey_PARAM_TAG) );
		
		if ( !EEBeanUtils.isNULL(request.getParameter(RedirectURI_PARAM_TAG)) )
			CallerIdentityInfo.setRedirecturi( request.getParameter(RedirectURI_PARAM_TAG) );
		
		if ( !EEBeanUtils.isNULL(request.getParameter(UserId_PARAM_TAG)) )
			OPOwner.setCurrentUser( request.getParameter(UserId_PARAM_TAG) );
		
		if ( !EEBeanUtils.isNULL(request.getParameter(UserAccessToken_PARAM_TAG)) )
			CallerIdentityInfo.setAccesstoken( request.getParameter(UserAccessToken_PARAM_TAG) );
		
		if ( !EEBeanUtils.isNULL(request.getParameter(UserType_PARAM_TAG)) )
			OPOwner.setUsertype( request.getParameter(UserType_PARAM_TAG) );
		
		if ( !EEBeanUtils.isNULL(request.getParameter(SeriesID_PARAM_TAG)) )
			OPOwner.setCurrentSeries(request.getParameter(SeriesID_PARAM_TAG) );
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void destroy() {

	}
	
	/**
	 * 应用标识
	 */
	public static final String AppID_PARAM_TAG = "appId";
	/**
	 * 应用秘钥，需加密
	 */
	public static final String AppSecretKey_PARAM_TAG = "appSecretKey";
	/**
	 * 应用跳转地址
	 */
	public static final String RedirectURI_PARAM_TAG = "redirectURI";
	/**
	 * 当前用户标识
	 */
	public static final String UserId_PARAM_TAG = "userId";
	/**
	 * 当前用户访问令牌
	 */
	public static final String UserAccessToken_PARAM_TAG = "userAccessToken";
	/**
	 * 用户类型
	 */
	public static final String UserType_PARAM_TAG = "userType";
	
	/**
	 * 业务体系
	 */
	public static final String SeriesID_PARAM_TAG = "seriesId";
}
