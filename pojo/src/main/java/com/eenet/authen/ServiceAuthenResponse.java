package com.eenet.authen;

import com.eenet.base.SimpleResponse;
/**
 * 服务认证结果
 * @author Orion
 *
 */
public class ServiceAuthenResponse extends SimpleResponse {
	private static final long serialVersionUID = -7227380164377259947L;
	private boolean identityConfirm = false;
	private ServiceConsumer serviceConsumer;
	/**
	 * @return 身份确认
	 */
	public boolean isIdentityConfirm() {
		return identityConfirm;
	}
	/**
	 * @param identityConfirm 身份确认
	 */
	public void setIdentityConfirm(boolean identityConfirm) {
		this.identityConfirm = identityConfirm;
	}
	/**
	 * @return the serviceConsumer
	 */
	public ServiceConsumer getServiceConsumer() {
		return serviceConsumer;
	}
	/**
	 * @param serviceConsumer the serviceConsumer to set
	 */
	public void setServiceConsumer(ServiceConsumer serviceConsumer) {
		this.serviceConsumer = serviceConsumer;
	}
}
