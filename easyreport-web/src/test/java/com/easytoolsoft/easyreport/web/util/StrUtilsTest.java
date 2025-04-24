package com.easytoolsoft.easyreport.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.easytoolsoft.easyreport.common.util.StrUtils;
import com.easytoolsoft.easyreport.engine.util.VelocityUtils;

/**
 * 注意：sql模版采用的时 velocity 语法，其中的参数最终都是字符串，<br/>
 * 条件化输出写法(!=null && != '')： <br/>
 * <code>
 	#if(${xxx} && ${xxx} != '')  
	  ...
	#end
	</code>
 */
public class StrUtilsTest {

	@Test
	public void testExtractVarNames() {
		String sql = "select * from order where retailerId = ${ retailer } and year = ${year}";

		List<String> varNames = StrUtils.extractVarNames(sql);

		System.out.println(varNames);
	}

	@Test
	public void test_selectMul() {
		String sqlTpl = "XXX \r\n" + "#if(${groupIds} && ${groupIds} != '')\r\n" + "AND f.devcGroupId IN(${groupIds})\r\n" + "#end\r\n" + "AND YYY";
		Map<String, Object> sqlParams = new HashMap<>();
		List<Integer> groupIds = new ArrayList<>();
		groupIds.add(1);
		groupIds.add(3);
		groupIds.add(5);

		String groupIdStr = StringUtils.join(groupIds, ",");

		sqlParams.put("groupIds", groupIdStr);
		String sqlText = VelocityUtils.parse(sqlTpl, sqlParams);

		System.out.println(sqlText);

		System.out.println("======================================================");

		sqlParams.put("groupIds", null);
		sqlText = VelocityUtils.parse(sqlTpl, sqlParams);

		System.out.println(sqlText);

	}

}
