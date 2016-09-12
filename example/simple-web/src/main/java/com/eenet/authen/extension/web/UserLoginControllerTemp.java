package com.eenet.authen.extension.web;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.eenet.authen.AdminUserSignOnBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSAEncrypt;

public class UserLoginControllerTemp extends AbstractController{
	private EndUserSignOnBizService endUserSignOnService;
	private AdminUserSignOnBizService adminUserSignOnService;
	private RSAEncrypt transferEncrypt;//加密公钥
	private String loginFailPageLocation;//登录失败调整页面
	private String defaultPageLocation;//登录成功默认加载页面

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		System.out.println(request.getRequestURI());
		System.out.println(request.getQueryString());
		
		System.out.println("-----------------head start---------------------");
		Enumeration<String> headNames = request.getHeaderNames();
		while (headNames.hasMoreElements()) {
			String headName = headNames.nextElement();
			System.out.println( headName + " : " + request.getHeader(headName) );;
		}
		System.out.println("-----------------head end---------------------");
		
		request.getRequestDispatcher(request.getRequestURI()).forward(request, response);
		
		return null;
	}
	
	/**
	 * @return the endUserSignOnService
	 */
	public EndUserSignOnBizService getEndUserSignOnService() {
		return endUserSignOnService;
	}

	/**
	 * @param endUserSignOnService the endUserSignOnService to set
	 */
	public void setEndUserSignOnService(EndUserSignOnBizService endUserSignOnService) {
		this.endUserSignOnService = endUserSignOnService;
	}

	/**
	 * @return the adminUserSignOnService
	 */
	public AdminUserSignOnBizService getAdminUserSignOnService() {
		return adminUserSignOnService;
	}

	/**
	 * @param adminUserSignOnService the adminUserSignOnService to set
	 */
	public void setAdminUserSignOnService(AdminUserSignOnBizService adminUserSignOnService) {
		this.adminUserSignOnService = adminUserSignOnService;
	}

	/**
	 * @return the transferEncrypt
	 */
	public RSAEncrypt getTransferEncrypt() {
		return transferEncrypt;
	}

	/**
	 * @param transferEncrypt the transferEncrypt to set
	 */
	public void setTransferEncrypt(RSAEncrypt transferEncrypt) {
		this.transferEncrypt = transferEncrypt;
	}

	/**
	 * @return the loginFailPageLocation
	 */
	public String getLoginFailPageLocation() {
		return loginFailPageLocation;
	}

	/**
	 * @param loginFailPageLocation the loginFailPageLocation to set
	 */
	public void setLoginFailPageLocation(String loginFailPageLocation) {
		this.loginFailPageLocation = loginFailPageLocation;
	}

	/**
	 * @return the defaultPageLocation
	 */
	public String getDefaultPageLocation() {
		return defaultPageLocation;
	}

	/**
	 * @param defaultPageLocation the defaultPageLocation to set
	 */
	public void setDefaultPageLocation(String defaultPageLocation) {
		this.defaultPageLocation = defaultPageLocation;
	}
}
