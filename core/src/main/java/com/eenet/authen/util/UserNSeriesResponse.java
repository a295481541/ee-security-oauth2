package com.eenet.authen.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.eenet.base.IBaseResponse;
import com.eenet.common.code.BizCode;

/**
 * 从缓存获得用户和业务体系ID
 * 2017年2月8日
 * @author Orion
 */
public class UserNSeriesResponse implements IBaseResponse, Serializable {
	private static final long serialVersionUID = 5427396320886815613L;
	
	/* 用户id */
	private String userId;
	/* 业务体系id */
	private String seriesId;
	
	/**
	 * @return the 用户id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the 用户id to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the 业务体系id
	 */
	public String getSeriesId() {
		return seriesId;
	}

	/**
	 * @param seriesId the 业务体系id to set
	 */
	public void setSeriesId(String seriesId) {
		this.seriesId = seriesId;
	}
	/*
	 * 实现BaseResponse接口
	 * @see com.eenet.base.BaseResponse
	 */
	private Boolean successful = new Boolean(true);
	private BizCode RSBizCode;
	private List<String> messages = new ArrayList<String>();
	@Override
	public void setSuccessful(Boolean successful) {
		this.successful = successful;
	}

	@Override
	public Boolean isSuccessful() {
		return this.successful;
	}

	@Override
	public void addMessage(String message) {
		this.messages.add(message);
	}

	@Override
	public List<String> getMessages() {
		return messages;
	}

	@Override
	public String getStrMessage() {
		StringBuffer msg = new StringBuffer();
		for (String s : this.messages)
			msg.append(s).append("\n");
		return msg.toString();
	}

	@Override
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	@Override
	public BizCode getRSBizCode() {
		return RSBizCode;
	}
	@Override
	public void setRSBizCode(BizCode rSBizCode) {
		RSBizCode = rSBizCode;
	}

	protected UserNSeriesResponse() {
		super();
	}
}
