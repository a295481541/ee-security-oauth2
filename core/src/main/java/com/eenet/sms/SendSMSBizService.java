package com.eenet.sms;

import com.eenet.base.SimpleResponse;

/**
 * 发送短信
 * 2016年7月14日
 * @author Orion
 */
public interface SendSMSBizService {
	
	/**
	 * 发送模板短信
	 * @param smsBody
	 * @return
	 * 2016年7月15日
	 * @author Orion
	 */
	public SimpleResponse sendSMSUsingTemplate(ShortMessageBody smsBody);
}
