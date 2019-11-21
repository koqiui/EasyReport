package com.easytoolsoft.easyreport.engine;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class XTest {
	static String preprocessSqlText(String sqlText) {
		sqlText = StringUtils.stripEnd(sqlText.trim(), ";");
		Pattern pattern = Pattern.compile("limit.*?$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sqlText);
		if (matcher.find()) {
			sqlText = matcher.replaceFirst("");
		}
		return sqlText + " limit 1";
	}

	@Test
	public void test_processText() {
		String sqlText = "select * from user where id = 258 limit 12, 23 ;";
		//
		String sqlResult = preprocessSqlText(sqlText);
		
		System.out.println(sqlResult);
	}
	
	
	@Test
	public void test_double() {
		BigDecimal dec = new BigDecimal(3.4555);
		//
		String decStr = dec.toString();
		
		System.out.println(decStr);
		
		double dbl = Double.valueOf(decStr);
		
		System.out.println(dbl);
	}
	
	@Test
	public void test_numformat() {
		DecimalFormat df = new DecimalFormat("###,##0.00%");
		//
		double val = 0.53;
		System.out.println(df.format(val));
	}

}
