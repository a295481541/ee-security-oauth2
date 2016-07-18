package com.eenet.sms.eesms;

import com.eenet.base.BaseEntity;
import com.eenet.sms.SendSMSBizType;

/**
 * eenet短信平台模板信息
 * 2016年7月17日
 * @author Orion
 */
public class EeSmsTemplate extends BaseEntity {
	private static final long serialVersionUID = -6314237280862862191L;
	private String appId;//业务应用标识
	private SendSMSBizType bizType;//发送短信的业务标识
	private String templateId;//模板标识
	
	/**
	 * @return 业务应用标识
	 * 2016年7月17日
	 * @author Orion
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId 业务应用标识
	 * 2016年7月17日
	 * @author Orion
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	/**
	 * @return 发送短信的业务标识
	 * 2016年7月17日
	 * @author Orion
	 */
	public SendSMSBizType getBizType() {
		return bizType;
	}
	/**
	 * @param bizType 发送短信的业务标识
	 * 2016年7月17日
	 * @author Orion
	 */
	public void setBizType(SendSMSBizType bizType) {
		this.bizType = bizType;
	}
	
	/**
	 * @return 模板标识
	 * 2016年7月17日
	 * @author Orion
	 */
	public String getTemplateId() {
		return templateId;
	}
	/**
	 * @param templateId 模板标识
	 * 2016年7月17日
	 * @author Orion
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	
}
