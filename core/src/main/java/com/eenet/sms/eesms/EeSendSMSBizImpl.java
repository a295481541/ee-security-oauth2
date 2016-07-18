package com.eenet.sms.eesms;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import com.eenet.authen.util.ABBizCode;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.base.biz.SimpleBizImpl;
import com.eenet.base.query.ConditionItem;
import com.eenet.base.query.QueryCondition;
import com.eenet.base.query.RangeType;
import com.eenet.common.exception.MyException;
import com.eenet.sms.SendSMSBizService;
import com.eenet.sms.ShortMessageBody;

public class EeSendSMSBizImpl extends SimpleBizImpl implements SendSMSBizService {
	private String sendSMSUsingTemplateURL;//按模板发送短信调用地址
	private String callSMSAppId;//调用短信接口的appid
	
	/**
	 * smsBody.smsParam key应包含：smsApiParams，为模板参数；value为list类型
	 */
	@Override
	public SimpleResponse sendSMSUsingTemplate(ShortMessageBody smsBody) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		
		/* 获得短信模板标识 */
		QueryCondition condition = new QueryCondition();
		condition.addCondition(new ConditionItem("appId",RangeType.EQUAL,smsBody.getAppId(),null));
		condition.addCondition(new ConditionItem("bizType",RangeType.EQUAL,smsBody.getSendSMSBizType().toString(),null));
		EeSmsTemplate template = super.get(condition, EeSmsTemplate.class);
		
		/* 解析要传入短信接口的参数 */
		StringBuffer param = new StringBuffer("");
		Object paramList = smsBody.getSmsParam().get("smsApiParams");
		if (paramList!=null && paramList instanceof List ) {
			for ( Object p : (List<?>)paramList ) {
				if (param.length()!=0)
					param.append(",");
				param.append(String.valueOf(p));
			}
		}
		
		/* 拼接发送报文 */
		StringBuffer xmlMessage = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xmlMessage.append("<tranceData>");
		xmlMessage.append("<MOBILE><![CDATA[").append(smsBody.getMobile()).append("]]></MOBILE>");
		xmlMessage.append("<SMS_TEMPLATE_ID><![CDATA[").append(template.getTemplateId()).append("]]></SMS_TEMPLATE_ID>");
		xmlMessage.append("<SMS_APP_ID><![CDATA[").append(getCallSMSAppId()).append("]]></SMS_APP_ID>");
		xmlMessage.append("<PARAMS><![CDATA[").append(param).append("]]></PARAMS>");
		xmlMessage.append("</tranceData>");
		
		/* 发送短信 */
		StringResponse sendSMSRS = sendSMS(xmlMessage.toString(), getSendSMSUsingTemplateURL(),
				smsBody.isWaittingForResult(), smsBody.getMobile());
		if (!sendSMSRS.isSuccessful()){
			result.addMessage(sendSMSRS.getStrMessage());
			result.setRSBizCode(sendSMSRS.getRSBizCode());
			return result;
		}
		
		result.setSuccessful(true);
		return result;
	}
	
	private StringResponse sendSMS(String requestContent, String apiURI, boolean isWaittingForResult, long mobile) {
		StringResponse result = new StringResponse();
		result.setSuccessful(false);
		
		/* 调用短信平台发送短信 */
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		String responseContent = null;
		try {
			client = HttpClients.createDefault();
			HttpPost method = new HttpPost(apiURI);
			method.setEntity(new StringEntity(requestContent, ContentType.create("text/plain", "UTF-8")));
			response = client.execute(method);
			
			int statusCode = response.getStatusLine().getStatusCode();
			responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
			if ( HttpStatus.SC_OK != statusCode )
				throw new MyException( "发送短信发生"+statusCode+"错误："+responseContent );
		} catch (Exception e) {
			result.setRSBizCode(ABBizCode.AB0003);
			result.addMessage(e.getMessage());
			return result;
		} finally {
			try {
				if (client != null){client.close();}
				if (response != null){response.close();}
			} catch (IOException e) {}
		}
		
		/* 解析短信平台反馈信息 */
		int sendSmsRSCode = -1;
		try {
			Document xmlDoc = DocumentHelper.parseText(responseContent);
			result.setResult(xmlDoc.asXML());
			sendSmsRSCode = Integer.parseInt(xmlDoc.selectSingleNode("/tranceData/result").getText());
			if (sendSmsRSCode != 1)
				throw new MyException( "短信发送失败，反馈代码："+sendSmsRSCode );
		} catch (Exception e) {
			result.setRSBizCode(ABBizCode.AB0004);
			result.addMessage(e.getMessage());
			return result;
		}
		
		/* 等待运营商反馈发送结果 */
		if (isWaittingForResult) {
			
		}
		
		result.setSuccessful(true);
		return result;
	}
	
	public static void main(String[] args) {
		EeSendSMSBizImpl impl = new EeSendSMSBizImpl();
		impl.setCallSMSAppId("e7800319ac1082a6237aaeec6260a117");
		ShortMessageBody body = new ShortMessageBody();
		body.setMobile(13922202252l);
		impl.sendSMSUsingTemplate(body);
	}
	
	/**
	 * @return 按模板发送短信调用地址
	 * 2016年7月14日
	 * @author Orion
	 */
	public String getSendSMSUsingTemplateURL() {
		return sendSMSUsingTemplateURL;
	}
	
	/**
	 * @param sendSMSUsingTemplateURL 按模板发送短信调用地址
	 * 2016年7月14日
	 * @author Orion
	 */
	public void setSendSMSUsingTemplateURL(String sendSMSUsingTemplateURL) {
		this.sendSMSUsingTemplateURL = sendSMSUsingTemplateURL;
	}
	
	/**
	 * @return 调用短信接口的appid
	 * 2016年7月14日
	 * @author Orion
	 */
	public String getCallSMSAppId() {
		return callSMSAppId;
	}
	/**
	 * @param callSMSAppId 调用短信接口的appid
	 * 2016年7月14日
	 * @author Orion
	 */
	public void setCallSMSAppId(String callSMSAppId) {
		this.callSMSAppId = callSMSAppId;
	}

	@Override
	public <M> Class<M> getPojoCLS() {
		// TODO Auto-generated method stub
		return null;
	}
}
