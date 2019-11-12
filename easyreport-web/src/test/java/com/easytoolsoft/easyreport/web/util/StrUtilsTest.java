package com.easytoolsoft.easyreport.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.easytoolsoft.easyreport.common.util.StrUtils;
import com.easytoolsoft.easyreport.engine.util.VelocityUtils;

public class StrUtilsTest {

	@Test
	public void testExtractVarNames() {
		String sql = "select * from order where retailerId = ${ retailer } and year = ${year}";

		List<String> varNames = StrUtils.extractVarNames(sql);

		System.out.println(varNames);
	}

	@Test
	public void test_selectMul() {
		String sqlTpl = "select id , name from goods_cat where id in (${cat_id_list})";
		Map<String, Object> sqlParams = new HashMap<>();
		List<Integer> catIdList = new ArrayList<>();
		catIdList.add(1);
		catIdList.add(3);
		catIdList.add(5);
		sqlParams.put("cat_id_list", catIdList);
		String sqlText = VelocityUtils.parse(sqlTpl, sqlParams);

		System.out.println(sqlText);
		
		
		System.out.println(catIdList);
	}

}
