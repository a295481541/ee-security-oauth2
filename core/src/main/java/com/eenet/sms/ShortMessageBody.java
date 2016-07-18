package com.eenet.sms;

import java.util.HashMap;
import java.util.Map;

/**
 * 发送短信息内容
 * 2016年7月14日
 * @author Orion
 */
public class ShortMessageBody {
	private String appId;//接入应用ID
	private SendSMSBizType sendSMSBizType;//发送短信业务类型
	private Map<String,?> smsParam;//短信参数
	private boolean waittingForResult;//等待短信发送结果
	private Long mobile;//接收手机
	/**
	 * @return 接入应用ID
	 * 2016年7月14日
	 * @author Orion
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId 接入应用ID
	 * 2016年7月14日
	 * @author Orion
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	/**
	 * @return 发送短信业务类型
	 * 2016年7月14日
	 * @author Orion
	 */
	public SendSMSBizType getSendSMSBizType() {
		return sendSMSBizType;
	}
	/**
	 * 发送短信业务类型
	 * @param sendSMSBizType
	 * 2016年7月14日
	 * @author Orion
	 */
	public void setSendSMSBizType(SendSMSBizType sendSMSBizType) {
		this.sendSMSBizType = sendSMSBizType;
	}
	
	/**
	 * 短信参数
	 * @return
	 * 2016年7月14日
	 * @author Orion
	 */
	public Map<String, ?> getSmsParam() {
		if (smsParam == null)
			smsParam = new HashMap<String,Object>();
		return smsParam;
	}
	
	/**
	 * 短信参数
	 * @param smsParam
	 * 2016年7月14日
	 * @author Orion
	 */
	public void setSmsParam(Map<String, ?> smsParam) {
		this.smsParam = smsParam;
	}
	
	/**
	 * @return 等待短信发送结果
	 * 2016年7月14日
	 * @author Orion
	 */
	public boolean isWaittingForResult() {
		return waittingForResult;
	}
	
	/**
	 * 是否等待发送结果反馈，如等待则处理时间较长
	 * @param waittingForResult 等待短信发送结果
	 * 2016年7月14日
	 * @author Orion
	 */
	public void setWaittingForResult(boolean waittingForResult) {
		this.waittingForResult = waittingForResult;
	}
	/**
	 * @return 接收手机
	 * 2016年7月14日
	 * @author Orion
	 */
	public Long getMobile() {
		return mobile;
	}
	/**
	 * @param mobile 接收手机
	 * 2016年7月14日
	 * @author Orion
	 */
	public void setMobile(Long mobile) {
		this.mobile = mobile;
	}
}
