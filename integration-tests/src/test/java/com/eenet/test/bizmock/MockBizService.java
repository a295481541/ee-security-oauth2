package com.eenet.test.bizmock;

import com.eenet.base.SimpleResultSet;
import com.eenet.base.query.QueryCondition;

public interface MockBizService {
	
	public SimpleResultSet<Person> query(QueryCondition condition);
	
	public String withoutIBaseResponse(String anything);
}
