package com.eenet.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.eenet.base.SimpleResponse;
import com.eenet.sms.SendSMSBizService;
import com.eenet.sms.SendSMSBizType;
import com.eenet.sms.ShortMessageBody;
import com.eenet.test.env.SpringEnvironment;

public class EeSendSMSTester extends SpringEnvironment{
	
	@Test
	public void sendSMSUsingTemplate(){
		System.out.println("==========================="+this.getClass().getName()+".sendSMSUsingTemplate()===========================");
		SendSMSBizService service = (SendSMSBizService)super.getContext().getBean("EeSendSMSBizImpl");
		
		ShortMessageBody body = new ShortMessageBody();
		body.setAppId("9CFF0CA0D43D4B2DAC1EFC6A86FCB191");
		body.setSendSMSBizType(SendSMSBizType.LoginUsingCode);
		Map<String,List<String>> smsParam = new HashMap<String, List<String>>();
		smsParam.put("smsApiParams", Arrays.asList("135791"));
		body.setSmsParam(smsParam);
		body.setMobile(13922202252l);
		
		
		SimpleResponse result = service.sendSMSUsingTemplate(body);
		System.out.println("sendSMSUsingTemplate: " +result.isSuccessful());
		if (!result.isSuccessful()) {
			System.out.println(result.getStrMessage());
		}
	}
}
