package com.eenet.test.bizmock;

import com.eenet.base.SimpleResultSet;
import com.eenet.base.query.QueryCondition;

public class MockBizImpl implements MockBizService {

	@Override
	public SimpleResultSet<Person> query(QueryCondition condition) {
		SimpleResultSet<Person> result = new SimpleResultSet<Person>();
		Person p1 = new Person();
		Person p2 = new Person();
		Person p3 = new Person();
		
		p1.setFirstName("p1");
		p2.setFirstName("p2");
		p3.setFirstName("p3");
		
		result.setSuccessful(true);
		result.addResult(p1);
		result.addResult(p2);
		result.addResult(p3);
		
		return result;
	}

	@Override
	public String withoutIBaseResponse(String anything) {
		return "hello"+anything;
	}
	
}
