package com.eenet.authen;

/**
 * 身份认证服务
 * @author Orion
 *
 */
public interface IdentityAuthenticationService {
	
	public EndUserAuthenResult endUserAuthen(EndUserAuthenRequest request);
	
	public void requestEndUserAccessToken(EndUserAuthenResult result);
	
	public AppAuthenResult appAuthen(AppAuthenRequest request);
}
