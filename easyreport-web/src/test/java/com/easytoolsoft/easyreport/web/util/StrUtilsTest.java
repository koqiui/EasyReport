package com.easytoolsoft.easyreport.web.util;

import java.util.List;

import org.junit.Test;

import com.easytoolsoft.easyreport.common.util.StrUtils;

public class StrUtilsTest {

	@Test
	public void testExtractVarNames() {
		String sql = "select * from order where retailerId = ${ retailer } and year = ${year}";

		List<String> varNames = StrUtils.extractVarNames(sql);

		System.out.println(varNames);
	}

}
