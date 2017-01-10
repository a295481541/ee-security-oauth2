package com.eenet.authen.bizimpl;

import com.eenet.authen.BusinessApp;
import com.eenet.authen.BusinessSeriesBizService;
import com.eenet.base.biz.SimpleBizImpl;
/**
 * 业务体系服务实现逻辑
 * @author koop
 *
 */
public class BusinessSeriesBizImpl  extends SimpleBizImpl implements BusinessSeriesBizService {

	@Override
	public Class<?> getPojoCLS() {
		return BusinessApp.class;
	}

}
