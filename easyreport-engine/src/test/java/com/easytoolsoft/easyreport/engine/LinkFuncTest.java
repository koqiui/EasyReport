package com.easytoolsoft.easyreport.engine;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class LinkFuncTest {

	@Test
	public void testFromLinkFuncExpr1() {
		String linkFuncExpr = " showReportDetail ( ) ";
		LinkFunc linkFunc = LinkFunc.fromLinkFuncExpr(linkFuncExpr);
		System.out.println(JSON.toJSONString(linkFunc));
	}

	@Test
	public void testFromLinkFuncExpr2() {
		String linkFuncExpr = " showReportDetail ( '   colName1 '    ,  ) ";
		LinkFunc linkFunc = LinkFunc.fromLinkFuncExpr(linkFuncExpr);
		System.out.println(JSON.toJSONString(linkFunc));
	}

	@Test
	public void testFromLinkFuncExpr3() {
		String linkFuncExpr = " showReportDetailX ( [ '   colName1 '    , \" colName2\" ] ) ";
		LinkFunc linkFunc = LinkFunc.fromLinkFuncExpr(linkFuncExpr);
		System.out.println(JSON.toJSONString(linkFunc));
	}

	@Test
	public void testToLinkHtml1() {
		String linkFuncExpr = " showReportDetail ( ) ";
		LinkFunc linkFunc = LinkFunc.fromLinkFuncExpr(linkFuncExpr);
		String colName = "name";
		String valueText = "商维宝";
		String reportCode = "xyz-report";
		Map<String, Object> dataMap = new HashMap<>();

		dataMap.put("name", "koqi'u&x&i\"s>s<ssssdfdfd");
		dataMap.put("gender", "男");
		dataMap.put("age", 99);
		dataMap.put("birthDate", "1978-06-22");
		dataMap.put("marriage", true);

		String linkHtml = LinkFunc.toLinkHtml(valueText, linkFunc, reportCode, colName, dataMap);

		System.out.println(linkHtml);
	}

	@Test
	public void testToLinkHtml2() {
		String linkFuncExpr = " showReportDetail ( [ gender, birthDate] ) ";
		LinkFunc linkFunc = LinkFunc.fromLinkFuncExpr(linkFuncExpr);

		String colName = "name";
		String valueText = "商维宝";
		String reportCode = "xyz-report";
		Map<String, Object> dataMap = new HashMap<>();

		dataMap.put("name", "koqi'u&x&i\"s>s<ssssdfdfd");
		dataMap.put("gender", "男");
		dataMap.put("age", 99);
		dataMap.put("birthDate", "1978-06-22");
		dataMap.put("marriage", true);

		String linkHtml = LinkFunc.toLinkHtml(valueText, linkFunc, reportCode, colName, dataMap);

		System.out.println(linkHtml);
	}

}
