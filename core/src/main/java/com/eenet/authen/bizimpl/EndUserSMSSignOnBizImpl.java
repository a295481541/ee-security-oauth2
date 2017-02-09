package com.eenet.authen.bizimpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eenet.authen.AccessToken;
import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessAppBizService;
import com.eenet.authen.BusinessSeriesBizService;
import com.eenet.authen.EndUserSMSSignOnBizService;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.SecurityCacheKey;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.authen.response.AppAuthenResponse;
import com.eenet.authen.util.SignOnUtil;
import com.eenet.base.BooleanResponse;
import com.eenet.base.SimpleResponse;
import com.eenet.base.StringResponse;
import com.eenet.baseinfo.user.EndUserInfo;
import com.eenet.baseinfo.user.EndUserInfoBizService;
import com.eenet.common.cache.RedisClient;
import com.eenet.common.exception.RedisOPException;
import com.eenet.security.PreRegistEndUserBizService;
import com.eenet.sms.SendSMSBizService;
import com.eenet.sms.SendSMSBizType;
import com.eenet.sms.ShortMessageBody;
import com.eenet.util.EEBeanUtils;

public class EndUserSMSSignOnBizImpl implements EndUserSMSSignOnBizService {
	


	@Override
	public SimpleResponse sendSMSCode4Login(String appId, long mobile) {
		 return sendSMSCode4Login(appId, null, mobile);
	}
	
	
	@Override
	public SimpleResponse sendSMSCode4Login(String appId, String seriesId, long mobile) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || mobile<13000000000l) {
			result.addMessage("接入应用标识或手机号不正确("+this.getClass().getName()+")");
			return result;
		}
		
		/* 检查该手机是否一分钟内发送过短信 */
		try {
			if ( getRedisClient().getMapValue(SecurityCacheKey.RECENT_SEND_SMS, String.valueOf(mobile)) != null ) {
				result.addMessage("发送短信频率太高("+this.getClass().getName()+")");
				return result;
			}
		} catch (RedisOPException e) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(e.toString());
			return result;
		}
		
		
		System.out.println("传入的seriesId :"+seriesId +"  传入的appId：" +appId);
		BusinessApp app = businessAppBizService.retrieveApp(appId);
		
		System.out.println("appp get " +EEBeanUtils.object2Json(app));
		if ( (app.getBusinessSeries()==null || EEBeanUtils.isNULL(app.getBusinessSeries().getAtid())) && !EEBeanUtils.isNULL(seriesId) ) 
			app.setBusinessSeries(businessSeriesBizService.retrieveBusinessSeries(seriesId, null));
		
		
		if (!app.isSuccessful() || !app.getBusinessSeries().isSuccessful() || EEBeanUtils.isNULL(app.getBusinessSeries().getAtid()) ) {
			result.addMessage("该体系系统不存在("+this.getClass().getName()+")");
			return result;
		}
		System.out.println("seriesId:" +seriesId +"   " +app.getBusinessSeries().getAtid());
		if (!EEBeanUtils.isNULL(seriesId)&&!seriesId.equals(app.getBusinessSeries().getAtid())) {
			result.addMessage("指定的业务体系与系统所隶属的业务体系不一致("+this.getClass().getName()+")");
			return result;
		}
		
		
		
		seriesId = app.getBusinessSeries().getAtid();
		
		
		
		/* 检查该手机所属用户信息 */
		BooleanResponse existUser = getPreRegistEndUserBizService().existAccount(appId, seriesId, String.valueOf(mobile));
		System.out.println(EEBeanUtils.object2Json(existUser));
		
		if ( existUser.isResult() ) {
			result.addMessage("未找到该手机所属用户");
			return result;
		}
		
		/* 生成短信验证码并缓存 */
		String smsCode = EEBeanUtils.randomSixNum();
		try {
			getRedisClient().addMapItem(SecurityCacheKey.RECENT_SEND_SMS, String.valueOf(mobile), System.currentTimeMillis(), 62);
			//已记录短信验证码
			System.out.println(SecurityCacheKey.ENDUSER_FASTLOGIN_SMS_CODE_PREFIX + ":" + appId + ":"+seriesId+":" + mobile    +":" +smsCode);
			boolean cached = getRedisClient().setObject(SecurityCacheKey.ENDUSER_FASTLOGIN_SMS_CODE_PREFIX + ":" + appId + ":"+seriesId+":" + mobile , smsCode, 600);
			if ( !cached )
				throw new RedisOPException("记录短信验证码失败("+this.getClass().getName()+")");
		} catch (RedisOPException e) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(e.toString());
			return result;
		}
		
		/* 发送短信 */
		ShortMessageBody body = new ShortMessageBody();
		body.setAppId(appId);//要发送短信的应用
		body.setSendSMSBizType(SendSMSBizType.LoginUsingCode);//短信的业务类型
		Map<String,List<String>> smsParam = new HashMap<String, List<String>>();
		smsParam.put("smsApiParams", Arrays.asList(smsCode));
		body.setSmsParam(smsParam);//短信模板参数（与短信接口平台耦合）
		body.setMobile(mobile);//接收手机
		SimpleResponse sendSMSRS = getSendSMSBizService().sendSMSUsingTemplate(body);
		if (!sendSMSRS.isSuccessful()) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(sendSMSRS.getStrMessage());
			return result;
		}
		
		result.setSuccessful(true);
		return result;
	}

	@Override
	public AccessToken getAccessToken(AppAuthenRequest appRequest, long mobile, String smsCode) {
		System.out.println("getAccessToken :====="+EEBeanUtils.object2Json(appRequest));
		AccessToken result = new AccessToken();
		result.setSuccessful(false);
		
		/* 参数检查 */
		if ( EEBeanUtils.isNULL(smsCode) || EEBeanUtils.isNULL(smsCode) ) {
			result.addMessage("手机号码和短信验证码均不可为空("+this.getClass().getName()+")");
			return result;
		}
		
		/* 业务应用认证 */
		AppAuthenResponse appAuthenRS = getAuthenService().appAuthen(appRequest);
		System.out.println(EEBeanUtils.object2Json(appAuthenRS));
		if (!appAuthenRS.isAppIdentityConfirm()) {
			result.addMessage("业务应用认证失败("+this.getClass().getName()+")");
			return result;
		}
		
		
		
		
		System.out.println("传入的seriesId :"+appRequest.getBizSeriesId() +"  传入的appId：" +appRequest.getAppId());
		BusinessApp app = businessAppBizService.retrieveApp(appRequest.getAppId());
		
		System.out.println("appp get " +EEBeanUtils.object2Json(app));
		if ( (app.getBusinessSeries()==null || EEBeanUtils.isNULL(app.getBusinessSeries().getAtid())) && !EEBeanUtils.isNULL(appRequest.getBizSeriesId()) ) 
			app.setBusinessSeries(businessSeriesBizService.retrieveBusinessSeries(appRequest.getBizSeriesId(), null));
		
		
		if (!app.isSuccessful() || !app.getBusinessSeries().isSuccessful() || EEBeanUtils.isNULL(app.getBusinessSeries().getAtid()) ) {
			result.addMessage("该体系系统不存在("+this.getClass().getName()+")");
			return result;
		}
		System.out.println("seriesId:" +appRequest.getBizSeriesId() +"   " +app.getBusinessSeries().getAtid());
		if (!EEBeanUtils.isNULL(appRequest.getBizSeriesId())&&!appRequest.getBizSeriesId().equals(app.getBusinessSeries().getAtid())) {
			result.addMessage("指定的业务体系与系统所隶属的业务体系不一致("+this.getClass().getName()+")");
			return result;
		}
		
		
		/* 校验并删除短信验证码 */
		SimpleResponse smsCodeCorrect = validateSMSCode4Login(app.getAppId(), app.getBusinessSeries().getAtid() ,mobile, smsCode, true);
		if ( !smsCodeCorrect.isSuccessful() ) {
			result.addMessage(smsCodeCorrect.getStrMessage());
			return result;
		}
		
		/* 获取手机所属用户个人信息 */
		EndUserInfo user = getPreRegistEndUserBizService().retrieveEndUserInfo(app.getAppId(), app.getBusinessSeries().getAtid(), String.valueOf(mobile));
		
		System.out.println("	/* 获取手机所属用户个人信息 */" +EEBeanUtils.object2Json(user));
		
		if ( !user.isSuccessful() ) {
			result.addMessage(user.getStrMessage());
			return result;
		}
		
		/* 删除访问令牌（防止一个用户可以通过两个令牌登录） */
		getSignOnUtil().removeUserTokenInApp(SecurityCacheKey.ENDUSER_CACHED_TOKEN,
				SecurityCacheKey.ENDUSER_ACCESSTOKEN_PREFIX, SecurityCacheKey.ENDUSER_REFRESHTOKEN_PREFIX,
				app.getAppId(), user.getAtid(), app.getBusinessSeries().getAtid());
		
		/* 生成并记录访问令牌 */
		StringResponse mkAccessTokenResult = 
				getSignOnUtil().makeAccessToken(SecurityCacheKey.ENDUSER_ACCESSTOKEN_PREFIX, app.getAppId(), user.getAtid(), app.getBusinessSeries().getAtid());
		
		if (!mkAccessTokenResult.isSuccessful()) {
			result.addMessage(mkAccessTokenResult.getStrMessage());
			return result;
		}
		
		/* 生成并记录刷新令牌 */
		StringResponse mkFreshTokenResult = getSignOnUtil().makeRefreshToken(
				SecurityCacheKey.ENDUSER_REFRESHTOKEN_PREFIX, app.getAppId(), user.getAtid(),
				app.getBusinessSeries().getAtid());
		if (!mkFreshTokenResult.isSuccessful()) {
			result.addMessage(mkFreshTokenResult.getStrMessage());
			return result;
		}
		
		/* 标记最终用户已缓存令牌 */
		getSignOnUtil().markUserTokenInApp(SecurityCacheKey.ENDUSER_CACHED_TOKEN, app.getAppId(), user.getAtid(),
				app.getBusinessSeries().getAtid(), mkAccessTokenResult.getResult(), mkFreshTokenResult.getResult());
		
		/* 所有参数已缓存，拼返回对象 */
		result.setUserInfo(user);
		result.setAccessToken(mkAccessTokenResult.getResult());
		result.setRefreshToken(mkFreshTokenResult.getResult());
		result.setSuccessful(true);
		return result;
	}
	
	@Override
	public SimpleResponse validateSMSCode4Login(String appId, String seriesId, long mobile, String smsCode, boolean rmSmsCode) {
		SimpleResponse result = new SimpleResponse();
		result.setSuccessful(false);
		
		/* 参数检查 */
		if (EEBeanUtils.isNULL(appId) || mobile<13000000000l || EEBeanUtils.isNULL(smsCode)) {
			result.addMessage("接入应用标识为空、手机号码错误或手机验证码为空("+this.getClass().getName()+")");
			return result;
		}
		
		/* 校验短信验证码 */
		String sentSmsCode = null;
		try {
			System.out.println("smsCode:" +smsCode);
			System.out.println(SecurityCacheKey.ENDUSER_FASTLOGIN_SMS_CODE_PREFIX + ":" + appId + ":"+seriesId+":"  + mobile +":" +smsCode);
			sentSmsCode = getRedisClient().getObject(SecurityCacheKey.ENDUSER_FASTLOGIN_SMS_CODE_PREFIX + ":" + appId + ":"+seriesId+":" + mobile, String.class);
			System.out.println("sentSmsCode:" +sentSmsCode);
			if (EEBeanUtils.isNULL(sentSmsCode) || !sentSmsCode.equals(smsCode)) {
				result.addMessage("短信验证码错误或已经失效("+this.getClass().getName()+")");
				return result;
			}
		} catch (RedisOPException e) {
			result.addMessage("from : "+this.getClass().getName());
			result.addMessage(e.toString());
			return result;
		}
		
		/* 删除短信验证码（如需要） */
		if (rmSmsCode) {
			try {
				getRedisClient().remove(SecurityCacheKey.ENDUSER_FASTLOGIN_SMS_CODE_PREFIX + ":" + appId + ":" + mobile);
			} catch (RedisOPException e) {
				e.printStackTrace();//do nothing
			}
		}
		
		result.setSuccessful(true);
		return result;
	}
	
	private EndUserInfoBizService endUserInfoBizService;//用户基本信息服务
	private RedisClient RedisClient;//Redis客户端
	private SendSMSBizService sendSMSBizService;//发送短信服务
	private PreRegistEndUserBizService preRegistEndUserBizService;//注册新用户服务预先检查服务
	private IdentityAuthenticationBizService authenService;//身份认证服务
	private SignOnUtil signOnUtil;//登录工具
	private BusinessAppBizService businessAppBizService;//业务系统服务
	private BusinessSeriesBizService businessSeriesBizService;//业务体系服务
	/**
	 * @return the 用户基本信息服务
	 */
	public EndUserInfoBizService getEndUserInfoBizService() {
		return endUserInfoBizService;
	}

	/**
	 * @param endUserInfoBizService the 用户基本信息服务 to set
	 */
	public void setEndUserInfoBizService(EndUserInfoBizService endUserInfoBizService) {
		this.endUserInfoBizService = endUserInfoBizService;
	}

	/**
	 * @return the Redis客户端
	 */
	public RedisClient getRedisClient() {
		return RedisClient;
	}

	/**
	 * @param redisClient the Redis客户端 to set
	 */
	public void setRedisClient(RedisClient redisClient) {
		RedisClient = redisClient;
	}

	/**
	 * @return the 发送短信服务 
	 */
	public SendSMSBizService getSendSMSBizService() {
		return sendSMSBizService;
	}

	/**
	 * @param sendSMSBizService the 发送短信服务  to set
	 */
	public void setSendSMSBizService(SendSMSBizService sendSMSBizService) {
		this.sendSMSBizService = sendSMSBizService;
	}

	/**
	 * @return the 注册新用户服务预先检查服务
	 */
	public PreRegistEndUserBizService getPreRegistEndUserBizService() {
		return preRegistEndUserBizService;
	}

	/**
	 * @param preRegistEndUserBizService the 注册新用户服务预先检查服务 to set
	 */
	public void setPreRegistEndUserBizService(PreRegistEndUserBizService preRegistEndUserBizService) {
		this.preRegistEndUserBizService = preRegistEndUserBizService;
	}
	
	/**
	 * @return the 身份认证服务
	 */
	public IdentityAuthenticationBizService getAuthenService() {
		return authenService;
	}

	/**
	 * @param authenService the 身份认证服务 to set
	 */
	public void setAuthenService(IdentityAuthenticationBizService authenService) {
		this.authenService = authenService;
	}
	
	/**
	 * @return the 登录工具类
	 */
	public SignOnUtil getSignOnUtil() {
		return signOnUtil;
	}

	/**
	 * @param signOnUtil the 登录工具类 to set
	 */
	public void setSignOnUtil(SignOnUtil signOnUtil) {
		this.signOnUtil = signOnUtil;
	}
	
	/**
	 * @return the 业务系统服务
	 */
	public BusinessAppBizService getBusinessAppBizService() {
		return businessAppBizService;
	}

	/**
	 * @param businessAppBizService the 业务系统服务 to set
	 */
	public void setBusinessAppBizService(BusinessAppBizService businessAppBizService) {
		this.businessAppBizService = businessAppBizService;
	}
	
	
	/**
	 * @return the 业务体系服务
	 */
	public BusinessSeriesBizService getBusinessSeriesBizService() {
		return businessSeriesBizService;
	}

	/**
	 * @param businessSeriesBizService the 业务体系服务to set
	 */
	public void setBusinessSeriesBizService(BusinessSeriesBizService businessSeriesBizService) {
		this.businessSeriesBizService = businessSeriesBizService;
	}

	

}
