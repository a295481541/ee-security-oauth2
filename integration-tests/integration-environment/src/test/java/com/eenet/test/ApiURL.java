package com.eenet.test;

public class ApiURL {
	private static final String devEnvSecurityApiURL = "http://172.16.165.223:8080/security-api";
	private static final String devEnvbaseinfoApiURL = "http://172.16.165.223:8080/baseinfo-api";
	
	private static final String testEnvSecurityApiURL = "http://172.16.146.152:8080/security-api";
	private static final String testEnvbaseinfoApiURL = "http://172.16.146.152:8080/baseinfo-api";
	
	private static final String rtEnvSecurityApiURL = "http://security-api.open.gzedu.com";
	private static final String rtEnvbaseinfoApiURL = "http://baseinfo-api.open.gzedu.com";
	
	private final String envFlag;
	
	/**
	 * dev,test,runtime
	 * @param envFlag
	 */
	public ApiURL(String envFlag) {
		this.envFlag = envFlag;
	}
	/**
	 * @return the securityApiURL
	 */
	public String getSecurityApiURL() {
		if (envFlag.equals("dev"))
			return devEnvSecurityApiURL;
		else if (envFlag.equals("test"))
			return testEnvSecurityApiURL;
		else if (envFlag.equals("runtime"))
			return rtEnvSecurityApiURL;
		throw new RuntimeException("要测试的环境无法标识");
	}
	/**
	 * @return the baseinfoApiURL
	 */
	public String getBaseinfoApiURL() {
		if (envFlag.equals("dev"))
			return devEnvbaseinfoApiURL;
		else if (envFlag.equals("test"))
			return testEnvbaseinfoApiURL;
		else if (envFlag.equals("runtime"))
			return rtEnvbaseinfoApiURL;
		throw new RuntimeException("要测试的环境无法标识");
	}
}
