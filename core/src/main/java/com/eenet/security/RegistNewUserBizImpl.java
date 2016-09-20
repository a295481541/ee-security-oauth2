package com.eenet.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.eenet.authen.bizimpl.EndUserLoginAccountBizImpl;
import com.eenet.authen.identifier.CallerIdentityInfo;
import com.eenet.authen.util.ABBizCode;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.AdminUserInfo;
import com.eenet.baseinfo.user.AdminUserInfoBizService;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.OPOwner;
import com.eenet.common.code.SystemCode;
import com.eenet.security.RegistNewUserBizService;
import com.eenet.util.EEBeanUtils;

public class RegistNewUserBizImpl implements RegistNewUserBizService {
	private static final Logger log = LoggerFactory.getLogger("error");

	@Override
	public AccessToken registEndUserWithLogin(EndUserInfo endUser, EndUserLoginAccount account,
			EndUserCredential credential) {
		log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] start..........." +OPOwner.getUsertype());
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		
		/* 参数检查 */
		if (endUser==null || account==null || credential==null) {
			log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] 最终用户信息、登陆账号信息或密码信息未知");
			result.addMessage("最终用户信息、登陆账号信息或密码信息未知("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0002);
			return result;
		}
		if (!EEBeanUtils.isNULL(endUser.getAtid())) {
			log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] 定义了已存在的最终用户");
			result.addMessage("定义了已存在的最终用户("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0003);
			return result;
		}
		/* 检查该账号是否已被使用 */
		EndUserLoginAccount existAccount = getEndUserLoginAccountBizService().retrieveEndUserLoginAccountInfo(account.getLoginAccount());
		if (existAccount.isSuccessful()) {
			result.addMessage("该账号已被使用("+this.getClass().getName()+")");
			result.setRSBizCode(ABBizCode.AB0002);
			return result;
		}
		log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] check over, current app :"+OPOwner.getCurrentSys()+", current user :" + OPOwner.getCurrentUser() + ", current userType :" + OPOwner.getUsertype());
		
		/* 新增用户 */
		EndUserInfo savedEndUser = getEndUserInfoBizService().save(endUser);
		log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] saved user result : "+ EEBeanUtils.object2Json(savedEndUser));
		if (!savedEndUser.isSuccessful()) {
			result.setRSBizCode(savedEndUser.getRSBizCode());
			result.addMessage(savedEndUser.getStrMessage());
			return result;
		}
		
		/* 注册登陆账号 */
		account.setUserInfo(savedEndUser);
		EndUserLoginAccount savedAccount = getEndUserLoginAccountBizService().registeEndUserLoginAccount(account);
		log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] registe account result : " + EEBeanUtils.object2Json(savedAccount));
		if (!savedAccount.isSuccessful()) {
			result.setRSBizCode(savedAccount.getRSBizCode());
			result.addMessage(savedAccount.getStrMessage());
			return result;
		}
		
		/* 初始化登陆密码 */
		String userCipherPassword = credential.getPassword();
		credential.setEndUser(savedEndUser);
		SimpleResponse savedCredential = getEndUserCredentialBizService().initEndUserLoginPassword(credential);
		log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] saved password result : "+ EEBeanUtils.object2Json(savedCredential));
		if (!savedCredential.isSuccessful()) {
			result.setRSBizCode(savedCredential.getRSBizCode());
			result.addMessage(savedCredential.getStrMessage());
			return result;
		}
		
		/* 获得认证授权码 */
		SignOnGrant grant = getEndUserSignOnBizService().getSignOnGrant(OPOwner.getCurrentSys(), CallerIdentityInfo.getRedirecturi(), account.getLoginAccount(),
				userCipherPassword);
		log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] get grant code result : "+EEBeanUtils.object2Json(grant));
		if (!grant.isSuccessful()) {
			result.setRSBizCode(grant.getRSBizCode());
			result.addMessage(grant.getStrMessage());
			return result;
		}
		
		/* 获得访问令牌 */
		result = getEndUserSignOnBizService().getAccessToken(OPOwner.getCurrentSys(), CallerIdentityInfo.getAppsecretkey(), grant.getGrantCode());
		log.error("[registEndUserWithLogin("+Thread.currentThread().getId()+")] get token result : "+EEBeanUtils.object2Json(result));
		return result;
	}
	
	@Override
	public AccessToken registEndUserWithMulAccountAndLogin(EndUserInfo endUser, List<EndUserLoginAccount> accounts,
			EndUserCredential credential) {
		log.info("start........... userType: " + OPOwner.getUsertype() + " appId: " + OPOwner.getCurrentSys());
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		
		/* 参数检查 */
		if (endUser==null || accounts==null || credential==null || accounts.size()==0) {
			result.addMessage("最终用户信息、登陆账号信息或密码信息未知("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0002);
			log.info(EEBeanUtils.object2Json(result));
			return result;
		}
		if (!EEBeanUtils.isNULL(endUser.getAtid())) {
			result.addMessage("定义了已存在的最终用户("+this.getClass().getName()+")");
			result.setRSBizCode(SystemCode.AA0003);
			log.info(EEBeanUtils.object2Json(result));
			return result;
		}
		
		/* 检查该账号是否已被使用 */
		for (EndUserLoginAccount account : accounts) {
			EndUserLoginAccount existAccount = getEndUserLoginAccountBizService().retrieveEndUserLoginAccountInfo(account.getLoginAccount());
			if (existAccount.isSuccessful()) {
				result.addMessage("该账号已被使用("+this.getClass().getName()+")");
				result.setRSBizCode(ABBizCode.AB0002);
				log.info(EEBeanUtils.object2Json(result));
				return result;
				
			}
		}
		
		/* 注册最终用户、第一个账号和密码，并检查注册结果 */
		result = this.registEndUserWithLogin(endUser, accounts.get(0), credential);
		log.info("注册最终用户、第一个账号和密码结果: " + EEBeanUtils.object2Json(result));
		if (!result.isSuccessful())
			return result;
		if ( !(result.getUserInfo() instanceof EndUserInfo) ) {
			result.addMessage("登录账号，除了"+accounts.get(0).getLoginAccount()+"，均未注册成功");
			result.setRSBizCode(ABBizCode.AB0008);
			log.info(EEBeanUtils.object2Json(result));
			return result;
		}
		
		/* 为已注册用户增加登录账号 */
		for (int i=1; i<accounts.size(); i++) {//第0个账号已创建，从第1个开始
			accounts.get(i).setUserInfo( (EndUserInfo)result.getUserInfo() );
			EndUserLoginAccount savedAccount = getEndUserLoginAccountBizService().registeEndUserLoginAccount(accounts.get(i));
			if (!savedAccount.isSuccessful()) {//有错误也不返回继续尝试注册下一个
				result.setRSBizCode(ABBizCode.AB0008);
				result.addMessage(accounts.get(i).getLoginAccount()+"注册失败: "+savedAccount.getStrMessage()+";");
			}
		}
		
		log.info("end! result: " + EEBeanUtils.object2Json(result));
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
