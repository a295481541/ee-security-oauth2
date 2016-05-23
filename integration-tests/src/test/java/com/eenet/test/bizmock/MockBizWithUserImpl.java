package com.eenet.test.bizmock;

import com.eenet.base.BaseEntity;
import com.eenet.util.EEBeanUtils;

public class MockBizWithUserImpl implements MockBizWithUserService {

	@Override
	public String sayHello(BaseEntity pojo) {
		if (EEBeanUtils.isNULL(pojo.getCrps()))
			return null;
		return "hello "+pojo.getCrps();
	}

}
