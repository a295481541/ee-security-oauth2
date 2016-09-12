package com.eenet.amq.jassauth;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.jaas.PropertiesLoginModule;
import org.apache.activemq.jaas.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;

/**
 * amq认证
 */
public class CustomLoginModule extends PropertiesLoginModule {

	private static final Logger LOG = LoggerFactory.getLogger(CustomLoginModule.class);

	private Subject subject;
	private CallbackHandler callbackHandler;

	private String user;
	private Set<Principal> principals = new HashSet<Principal>();
	private boolean loginSucceeded;

	@SuppressWarnings("rawtypes")
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
		super.initialize(subject, callbackHandler, sharedState, options);
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		loginSucceeded = false;
	}
	@Override
	public boolean login() throws LoginException {
		
		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("Username: ");
		callbacks[1] = new PasswordCallback("Password: ", false);
		try {
			callbackHandler.handle(callbacks);
		} catch (IOException ioe) {
			throw new LoginException(ioe.getMessage());
		} catch (UnsupportedCallbackException uce) {
			throw new LoginException(uce.getMessage() + " not available to obtain information from user");
		}
		user = ((NameCallback) callbacks[0]).getName();
		char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
		if (tmpPassword == null) {
			throw new FailedLoginException("Password Has not  completed !");
		}

		String password = new String(tmpPassword);
		AppAuthenRequest request = new AppAuthenRequest();
		request.setAppId(user);
		request.setAppSecretKey(password);
		
//		LOG.info("user : " +user );
//		LOG.info("password : " +password );
//		LOG.info("request : " +request );
		
		SimpleResponse response = null;
		try {
			
			IdentityAuthenticationBizService service = DubboUtil.getService();
//			LOG.info("service : " +service );
			response = service.appAuthen(request);
			LOG.info("response result: " + response.isSuccessful());
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new FailedLoginException("Connect to center auth system error!   " + response.getStrMessage());
		}
		
//		LOG.info("response : " + response);
//		LOG.info("response : " + response.isSuccessful());

		loginSucceeded = response.isSuccessful();
		if (loginSucceeded)
			return loginSucceeded;
		else
			return super.login();

	}
	@Override
	public boolean commit() throws LoginException {
		boolean result = loginSucceeded;
		
		if (result) {
			System.out.println("principals" + principals);
			principals.add(new UserPrincipal(user));
			principals.add(new GroupPrincipal("bizApps"));
			subject.getPrincipals().addAll(principals);
		} else
			return super.commit();
		clear();
		return result;
	}
	@Override
	public boolean abort() throws LoginException {
		super.abort();
		clear();
		return true;
	}
	@Override
	public boolean logout() throws LoginException {
		super.logout();
		subject.getPrincipals().removeAll(principals);
		principals.clear();
		clear();
		return true;
	}
	private void clear() {
		user = null;
		loginSucceeded = false;
	}

}