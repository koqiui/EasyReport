package com.easytoolsoft.easyreport.engine.util;

import static org.junit.Assert.*;

import java.sql.Types;

import org.junit.Test;

public class JdbcUtilsTest {

	@Test
	public void testToStdSqlTypeName() {
		int sqlType = Types.BOOLEAN;
		String sqlTypeName = JdbcUtils.toStdSqlTypeName(sqlType);
		System.out.println(sqlTypeName);
	}

	@Test
	public void testToSimpleDataType() {
		fail("Not yet implemented");
	}

}
