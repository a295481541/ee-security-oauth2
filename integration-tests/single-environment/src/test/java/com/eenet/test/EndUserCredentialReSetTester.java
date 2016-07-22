package com.eenet.test;

import org.junit.Test;

import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.security.EndUserCredentialReSetBizService;
import com.eenet.test.env.SpringEnvironment;

/**
 * 因为涉及到短信验证码
 * 该测试只能分阶段手工完成
 * 2016年7月22日
 * @author Orion
 */
public class EndUserCredentialReSetTester extends SpringEnvironment {
	private final EndUserCredentialReSetBizService resetService = (EndUserCredentialReSetBizService)super.getContext().getBean("EndUserCredentialReSetBizImpl");
	
//	@Test
	public void resetPasswordBySMS() {
		String appId = "9CFF0CA0D43D4B2DAC1EFC6A86FCB191";
		long mobile = 13922202252l;
		StringResponse sendSMSRS = resetService.sendSMSCode4ResetPassword(appId, mobile);
		if ( !sendSMSRS.isSuccessful() ) {
			System.out.println(sendSMSRS.getStrMessage());
			return;
		}
		String userId = sendSMSRS.getResult();
		System.out.println("重置密码用户：" + userId);
	}
	
	@Test
	public void resetPasswordAndLogin() {
		String userId = "9BC7BA2AEF584220BBC2845BF61A04B9";//<==要重置密码用户的标识，从resetPasswordBySMS()方法获得
		String smsCode = "138201";//<==收到短信后填于此处
		SimpleResponse smsCodeCorrect = resetService.validateSMSCode4ResetPassword(userId, smsCode, false);
		if ( !smsCodeCorrect.isSuccessful()) {
			System.out.println(smsCodeCorrect.getStrMessage());
			return;
		}
		System.out.println("短信验证码："+smsCode+" 正确");
	}
	
//	@Test
	public void threeTimeSendFail() {
		resetPasswordBySMS();
		resetPasswordBySMS();
		resetPasswordBySMS();
	}
}
