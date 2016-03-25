package com.eenet.common.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * 身份认证异常
 * 2016年3月25日
 * @author Orion
 */
public class AuthenException extends RuntimeException {
	private static final long serialVersionUID = -4071425913973138554L;
	
	public AuthenException(String message, Throwable cause) {
		super(message,cause);
	}
	
	public AuthenException(Throwable cause) {
		super(cause);
	}
	
	public AuthenException(String message) {
		super(message);
	}
	
	public List<String> getTraceSet() {
		List<String> resultSet = new ArrayList<String>();
		StackTraceElement[] elements = super.getStackTrace();
		for (StackTraceElement element : elements) {
			resultSet.add(element.toString());
		}
		return resultSet;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer(super.getMessage()).append(" : ");
		for (String s : this.getTraceSet()) {
			str.append(s).append("\n");
		}
		return str.toString();
	}
}
