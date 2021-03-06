package com.eenet.test.mkTestData;

import java.util.Random;

import org.junit.Test;

import com.eenet.authen.AdminUserCredential;
import com.eenet.authen.AdminUserCredentialBizService;
import com.eenet.authen.AdminUserLoginAccount;
import com.eenet.authen.AdminUserLoginAccountBizService;
import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessAppType;
import com.eenet.authen.EndUserCredential;
import com.eenet.authen.EndUserCredentialBizService;
import com.eenet.authen.EndUserLoginAccount;
import com.eenet.authen.EndUserLoginAccountBizService;
import com.eenet.authen.LoginAccountType;
import com.eenet.base.SimpleResponse;
import com.eenet.baseinfo.user.AdminUserInfo;
import com.eenet.baseinfo.user.AdminUserInfoBizService;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.test.env.SpringEnvironment;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class NewAppNUser extends SpringEnvironment{
	private AdminUserInfoBizService adminService = (AdminUserInfoBizService)super.getContext().getBean("AdminUserInfoBizService");
	private EndUserInfoBizService endUserService = (EndUserInfoBizService)super.getContext().getBean("EndUserInfoBizService");
	private BusinessAppBizService appService = (BusinessAppBizService)super.getContext().getBean("BusinessAppBizImpl");
	private AdminUserLoginAccountBizService adminAccountService = (AdminUserLoginAccountBizService)super.getContext().getBean("AdminUserLoginAccountBizService");
	private AdminUserCredentialBizService adminCredentialService = (AdminUserCredentialBizService)super.getContext().getBean("AdminUserCredentialBizService");
	private EndUserLoginAccountBizService endUserAccountService = (EndUserLoginAccountBizService)super.getContext().getBean("EndUserLoginAccountBizImpl");
	private EndUserCredentialBizService endUserCredentialService = (EndUserCredentialBizService)super.getContext().getBean("EndUserCredentialBizImpl");
	private RSAEncrypt encrypt = (RSAEncrypt)super.getContext().getBean("TransferRSAEncrypt");
	
//	@Test
	public void createAdmin() throws Exception {
		super.adminLogin();
		
		AdminUserInfo admin = new AdminUserInfo();
		admin.setName("测试系统管理员");
		admin.setDataDescription("测试系统管理员，勿同步到生产环境");
		admin = adminService.save(admin);
		if (!admin.isSuccessful())
			System.err.println(admin.getStrMessage());
		else
			System.out.println(admin.getAtid() + "," + admin.getName());
	}
	
//	@Test
	public void createEndUserNAccountNCredential() throws Exception {
		for (int i=0;i<5;i++) {
			EndUserInfo endUser = new EndUserInfo();
			endUser.setName("职工#"+i);
			endUser.setMobile(13900000000l + new Random().nextInt(100));
			endUser.setMobileChecked(true);
			endUser.setDataDescription("勿删！！！重要测试数据");
			endUser = endUserService.save(endUser);
			
			EndUserLoginAccount account = new EndUserLoginAccount();
			account.setUserInfo(endUser);
			account.setAccountType(LoginAccountType.MOBILE);
			account.setLoginAccount(String.valueOf(endUser.getMobile()));
			account.setDataDescription("勿删！！！重要测试数据");
			account = endUserAccountService.registeEndUserLoginAccount(account);
			
			String password = "abc"+new Random().nextInt(100);
			EndUserCredential credential = new EndUserCredential();
			credential.setEndUser(endUser);
			credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, password));
			credential.setDataDescription("勿删！！！重要测试数据");
			SimpleResponse initResult = endUserCredentialService.initEndUserLoginPassword(credential);
			
			if (endUser.isSuccessful() && account.isSuccessful() && initResult.isSuccessful()) {
				StringBuffer info = new StringBuffer("ID: " + endUser.getAtid());
				info.append(",姓名：" + endUser.getName());
				info.append(",手机：" + endUser.getMobile());
				info.append(",登录账号："+ account.getLoginAccount());
				info.append(",登录密码："+password);
				System.out.println(info.toString());
			} else
				System.out.println("出错了");
		}
	}
	
	@Test
	public void createAPP() {
		BusinessApp app = new BusinessApp();
		String appSecretKey = "gdWorker^"+(new Random().nextInt(100))+"*";
		app.setAppName("广东职工教育云");
		app.setAppType(BusinessAppType.WEBAPP);
		app.setRedirectURIPrefix("http://gd.saas.workeredu.com/");
		app.setSecretKey(appSecretKey);
//		app.setDataDescription("勿删！！！重要测试数据");
		app = appService.registeApp(app);
		System.out.println("APPID: " + app.getAtid() + ",系统中文名：" + app.getAppName() + ",接入密码: "+appSecretKey+",合法地址： "+app.getRedirectURIPrefix());
	}
	
//	@Test
	public void batchCreateAPP() {
		String[] appName = {"杭州职工教育网","湖州职工教育网","湖北职工教育网"};
		String[] appDomain = {"http://hz.saas.workeredu.com","http://huzhou.saas.workeredu.com","http://hb.saas.workeredu.com"};
		for (int i=0;i<appName.length;i++) {
			BusinessApp app = new BusinessApp();
			String appSecretKey = "zgWe#"+EEBeanUtils.randomSixNum()+")";
			app.setAppName(appName[i]);
			app.setAppType(BusinessAppType.WEBAPP);
			app.setRedirectURIPrefix(appDomain[i]);
			app.setSecretKey(appSecretKey);
			app = appService.registeApp(app);
			System.out.println("APPID: " + app.getAtid() + ",系统中文名：" + app.getAppName() + ",接入密码: "+appSecretKey+",合法地址： "+app.getRedirectURIPrefix());
		}
	}
	
//	@Test
	public void batchAdminLoginAccountNCredential() throws Exception{
		String[] adminUserName = {"招生管理平台管理员","广东工会补贴申请管理员","广东工会补贴管理员"};
		String[] loginAccount = {"espSYSAdmin","gdghbtsqAdmin","gdghbtglAdmin"};
		String[] password = {"eSp!1357","gdGhbt#578","tglAdm$209"};
		for (int i=0;i<adminUserName.length;i++) {
			AdminUserInfo admin = new AdminUserInfo();
			admin.setName(adminUserName[i]);
			admin = adminService.save(admin);
			if (!admin.isSuccessful()){
				System.out.println(admin.getStrMessage());
				return;
			}
			
			String adminUserId = admin.getAtid();
			AdminUserLoginAccount account = new AdminUserLoginAccount();
			account.setUserInfo(admin);
			account.setAccountType(LoginAccountType.USERNAME);
			account.setLoginAccount(loginAccount[i]);
			account = adminAccountService.registeAdminUserLoginAccount(account);
			if (!account.isSuccessful()){
				System.out.println(account.getStrMessage());
				return;
			}
			
			AdminUserCredential credential = new AdminUserCredential();
			credential.setAdminUser(admin);
			credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, password[i]));
			SimpleResponse initResult = adminCredentialService.initAdminUserLoginPassword(credential);
			if (!initResult.isSuccessful()){
				System.out.println(initResult.getStrMessage());
				return;
			}
			
			if (account.isSuccessful() && initResult.isSuccessful())
				System.out.println(adminUserName[i]+"登录账号： "+loginAccount[i]+",登录密码："+password[i]);
			else
				System.out.print("出错了");
		}
	}
	
//	@Test
	public void createAdminLoginAccountNCredential() throws Exception {
		super.adminLogin();
		
		String loginAccount = "test.admin";
		String password = "sEpa$738";
		String adminUserId = "906161E5364F40658AA208E2493A84DE";
		AdminUserLoginAccount account = new AdminUserLoginAccount();
		AdminUserInfo admin = new AdminUserInfo();admin.setAtid(adminUserId);
		account.setUserInfo(admin);
		account.setAccountType(LoginAccountType.USERNAME);
		account.setLoginAccount(loginAccount);
		account = adminAccountService.registeAdminUserLoginAccount(account);
		account.setDataDescription("测试用户，勿同步到生产环境");
		
		AdminUserCredential credential = new AdminUserCredential();
		credential.setAdminUser(admin);
		credential.setPassword(RSAUtil.encryptWithTimeMillis(encrypt, password));
		credential.setDataDescription("测试用户，勿同步到生产环境，密码："+password);
		SimpleResponse initResult = adminCredentialService.initAdminUserLoginPassword(credential);
		
		if (account.isSuccessful() && initResult.isSuccessful())
			System.out.println("管理员登录账号： "+loginAccount+",管理员登录密码："+password);
		else
			System.out.print(account.getStrMessage());
	}
}
