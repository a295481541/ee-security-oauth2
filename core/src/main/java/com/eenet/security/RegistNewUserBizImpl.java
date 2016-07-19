package com.eenet.security;

import com.eenet.authen.AccessToken;
import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserCredentialBizService;
import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.AdminUserLoginAccountBizService;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserCredentialBizService;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.EndUserSignOnBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;
import com.eenet.common.code.SystemCode;
import com.eenet.security.RegistNewUserBizService;
import com.eenet.user.AdminUserInfo;
import com.eenet.user.AdminUserInfoBizService;
import com.eenet.user.EndUserInfo;
import com.eenet.user.EndUserInfoBizService;
import com.eenet.util.EEBeanUtils;

public class RegistNewUserBizImpl implements RegistNewUserBizService {

	@Override
	public AccessToken registEndUserWithLogin(EndUserInfo endUser, EndUserLoginAccount account,
			EndUserCredential credential, AppAuthenRequest appID) {
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		/* 参数检查 */
		if (endUser==null || account==null || credential==null || appID==null) {
			result.addMessage("最终用户信息、登陆账号信息或密码信息未知("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0002);
			return result;
		}
		if (!EEBeanUtils.isNULL(endUser.getAtid())) {
			result.addMessage("定义了已存在的最终用户("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0003);
			return result;
		}
		
		/* 接入系统身份认证 */
		SimpleResponse appAuthRS = getIdentityAuthenticationBizService().appAuthen(appID);
		if (!appAuthRS.isSuccessful()) {
			result.addMessage(appAuthRS.getStrMessage());
			return result;
		}
		
		/* 新增用户 */
		EndUserInfo savedEndUser = getEndUserInfoBizService().save(endUser);
		if (!savedEndUser.isSuccessful()) {
			result.addMessage(savedEndUser.getStrMessage());
			return result;
		}
		
		/* 注册登陆账号 */
		account.setUserInfo(savedEndUser);
		EndUserLoginAccount savedAccount = getEndUserLoginAccountBizService().registeEndUserLoginAccount(account);
		if (!savedAccount.isSuccessful()) {
			result.addMessage(savedAccount.getStrMessage());
			return result;
		}
		
		/* 初始化登陆密码 */
		String userCipherPassword = credential.getPassword();
		credential.setEndUser(savedEndUser);
		SimpleResponse savedCredential = getEndUserCredentialBizService().initEndUserLoginPassword(credential);
		if (!savedCredential.isSuccessful()) {
			result.addMessage(savedCredential.getStrMessage());
			return result;
		}
		
		/* 获得认证授权码 */
		SignOnGrant grant = getEndUserSignOnBizService().getSignOnGrant(appID.getAppId(), appID.getRedirectURI(), account.getLoginAccount(),
				userCipherPassword);
		if (!grant.isSuccessful()) {
			result.addMessage(grant.getStrMessage());
			return result;
		}
		
		/* 获得访问令牌 */
		result = getEndUserSignOnBizService().getAccessToken(appID.getAppId(), appID.getAppSecretKey(), grant.getGrantCode());
		return result;
	}

	@Override
	public SimpleResponse registAdminUserWithoutLogin(AdminUserInfo adminUser, AdminUserLoginAccount account,
			AdminUserCredential credential) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (adminUser==null || account==null || credential==null) {
			result.addMessage("服务人员信息、登陆账号信息或密码信息未知("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0002);
			return result;
		}
		if (!EEBeanUtils.isNULL(adminUser.getAtid())) {
			result.addMessage("定义了已存在的服务人员("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0003);
			return result;
		}
		
		/* 新增用户 */
		AdminUserInfo savedAdmin = getAdminUserInfoBizService().save(adminUser);
		if (!savedAdmin.isSuccessful()) {
			result.addMessage(savedAdmin.getStrMessage());
			return result;
		}
		
		/* 注册登陆账号 */
		account.setUserInfo(savedAdmin);
		AdminUserLoginAccount savedAccount = getAdminUserLoginAccountBizService().registeAdminUserLoginAccount(account);
		if (!savedAccount.isSuccessful()) {
			result.addMessage(savedAccount.getStrMessage());
			return result;
		}
		
		/* 初始化登陆密码 */
		credential.setAdminUser(savedAdmin);
		SimpleResponse savedCredential = getAdminUserCredentialBizService().initAdminUserLoginPassword(credential);
		if (!savedCredential.isSuccessful()) {
			result.addMessage(savedCredential.getStrMessage());
			return result;
		}
		
		result.setSuccessful(true);
		return result;
	}
	
	private IdentityAuthenticationBizService identityAuthenticationBizService;
	
	private AdminUserInfoBizService adminUserInfoBizService;
	private AdminUserLoginAccountBizService adminUserLoginAccountBizService;
	private AdminUserCredentialBizService adminUserCredentialBizService;
	
	private EndUserInfoBizService endUserInfoBizService;
	private EndUserLoginAccountBizService endUserLoginAccountBizService;
	private EndUserCredentialBizService endUserCredentialBizService;
	private EndUserSignOnBizService endUserSignOnBizService;
	public AdminUserInfoBizService getAdminUserInfoBizService() {
		return adminUserInfoBizService;
	}

	public void setAdminUserInfoBizService(AdminUserInfoBizService adminUserInfoBizService) {
		this.adminUserInfoBizService = adminUserInfoBizService;
	}

	public AdminUserLoginAccountBizService getAdminUserLoginAccountBizService() {
		return adminUserLoginAccountBizService;
	}

	public void setAdminUserLoginAccountBizService(AdminUserLoginAccountBizService adminUserLoginAccountBizService) {
		this.adminUserLoginAccountBizService = adminUserLoginAccountBizService;
	}

	public AdminUserCredentialBizService getAdminUserCredentialBizService() {
		return adminUserCredentialBizService;
	}

	public void setAdminUserCredentialBizService(AdminUserCredentialBizService adminUserCredentialBizService) {
		this.adminUserCredentialBizService = adminUserCredentialBizService;
	}

	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
	}

	public EndUserLoginAccountBizService getEndUserLoginAccountBizService() {
		return endUserLoginAccountBizService;
	}

	public void setEndUserLoginAccountBizService(EndUserLoginAccountBizService endUserLoginAccountBizService) {
		this.endUserLoginAccountBizService = endUserLoginAccountBizService;
	}

	public EndUserCredentialBizService getEndUserCredentialBizService() {
		return endUserCredentialBizService;
	}

	public void setEndUserCredentialBizService(EndUserCredentialBizService endUserCredentialBizService) {
		this.endUserCredentialBizService = endUserCredentialBizService;
	}

	public EndUserSignOnBizService getEndUserSignOnBizService() {
		return endUserSignOnBizService;
	}

	public void setEndUserSignOnBizService(EndUserSignOnBizService endUserSignOnBizService) {
		this.endUserSignOnBizService = endUserSignOnBizService;
	}

	public IdentityAuthenticationBizService getIdentityAuthenticationBizService() {
		return identityAuthenticationBizService;
	}

	public void setIdentityAuthenticationBizService(IdentityAuthenticationBizService identityAuthenticationBizService) {
		this.identityAuthenticationBizService = identityAuthenticationBizService;
	}
}
