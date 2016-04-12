package com.eenet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.eenet.authen.ServiceConsumer;
import com.eenet.authen.SignOnGrant;
import com.eenet.authen.SingleSignOnBizService;
import com.eenet.base.SimpleResponse;
import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSADecrypt;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

@Controller
public class LoginController {
	@Autowired
	private SingleSignOnBizService signOnService;
	@Autowired
	private ServiceConsumer SSOSystemIdentity;
	@Autowired
	private RSAEncrypt transferRSAEncrypt;
	@Autowired
	private RSADecrypt transferRSADecrypt;
	
	/**
	 * 加载登录界面
	 * @param appId 第三方应用标识
	 * @param redirectURI 认证成功跳转地址
	 * 2016年4月11日
	 * @author Orion
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView login(String appId, String redirectURI){
		ModelAndView mNv = new ModelAndView();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(redirectURI)) {
			SimpleResponse response = new SimpleResponse();
			response.addMessage("无效的业务应用标识（appId）或回调地址（redirectURI）");
			mNv.setViewName("common/error");
			return mNv;
		}
		
		/* 生成页面安全码 */
		String pageCodeCiphertext = null;
		try {
			pageCodeCiphertext = RSAUtil.encrypt(transferRSAEncrypt, appId+"~"+redirectURI);
		} catch (EncryptException e) {
			e.printStackTrace();
		}
		if (EEBeanUtils.isNULL(appId)){
			SimpleResponse response = new SimpleResponse();
			response.addMessage("生成页面安全码失败");
			mNv.setViewName("common/error");
			return mNv;
		}
		
		mNv.addObject("pageCode", pageCodeCiphertext);
		mNv.addObject("appId", appId);
		mNv.addObject("redirectURI", EEBeanUtils.encodeBase64(redirectURI));
		mNv.setViewName("login");
		return mNv;
	}
	
	/**
	 * 用户登录
	 * @param appId 第三方应用标识
	 * @param redirectURI 认证成功跳转地址
	 * @param pageCode 页面安全码
	 * @param loginAccount 用户账号
	 * @param userPassword 用户密码
	 * @return
	 * 2016年4月11日
	 * @author Orion
	 */
	@RequestMapping(value = "/signOn", method = RequestMethod.POST)
	public ModelAndView signOn(String appId, String redirectURI, String pageCode, String loginAccount, String userPassword) {
		ModelAndView mNv = new ModelAndView();
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || EEBeanUtils.isNULL(redirectURI)) {
			SimpleResponse response = new SimpleResponse();
			response.addMessage("无效的业务应用标识（appId）或回调地址（redirectURI）");
			mNv.setViewName("common/error");
			return mNv;
		}
		if (EEBeanUtils.isNULL(loginAccount) || EEBeanUtils.isNULL(userPassword)) {
			SimpleResponse response = new SimpleResponse();
			response.addMessage("用户名、密码不可为空");
			mNv.setViewName("common/error");
			return mNv;
		}
		if (EEBeanUtils.isNULL(pageCode)) {
			SimpleResponse response = new SimpleResponse();
			response.addMessage("该登录界面未被授权");
			mNv.setViewName("common/error");
			return mNv;
		}
		
		/* 校验页面安全码 */
		String pageCodePlaintext = null;
		try {
			pageCodePlaintext = RSAUtil.decrypt(transferRSADecrypt, pageCode);
		} catch (EncryptException e) {
			e.printStackTrace();
		}
		if (EEBeanUtils.isNULL(pageCodePlaintext)) {
			SimpleResponse response = new SimpleResponse();
			response.addMessage("无法校验该页面是否已被授权");
			mNv.setViewName("common/error");
			return mNv;
		}
		String redirectURIDecode = EEBeanUtils.decodeBase64(redirectURI);
		if (!pageCode.equals(appId+"~"+redirectURIDecode)) {
			SimpleResponse response = new SimpleResponse();
			response.addMessage("登录界面存在被篡改的风险");
			mNv.setViewName("common/error");
			return mNv;
		}
		
		/* 用户认证 */
		SignOnGrant grant = this.signOnService.getSignOnGrant(SSOSystemIdentity, appId, redirectURIDecode, loginAccount, userPassword);
		if (grant.isSuccessful()) {
			StringBuffer redirect = new StringBuffer("redirect:").append(redirectURIDecode);
			redirect.append("&GrantCode=").append(grant.getGrantCode());
			mNv.setViewName(redirect.toString());
		} else {
			SimpleResponse response = new SimpleResponse();
			response.addMessage("获取登录授权码失败");
			mNv.setViewName("common/error");
		}
		return mNv;
	}
}
