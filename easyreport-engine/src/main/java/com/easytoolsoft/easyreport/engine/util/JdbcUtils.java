package com.easytoolsoft.easyreport.engine.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.dbpool.DataSourcePoolFactory;

/**
 * Jdbc工具类.
 *
 * @author tomdeng
 */
public class JdbcUtils {
	private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);
	private static final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>(100);

	private static final Map<Integer, String> STD_SQL_TYPE_NAME_MAP;
	static {
		STD_SQL_TYPE_NAME_MAP = new LinkedHashMap<>();
		//
		Field[] typeFields = Types.class.getDeclaredFields();
		for (Field typeField : typeFields) {
			int modifiers = typeField.getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
				if (typeField.getType() == int.class) {
					try {
						int value = typeField.getInt(null);
						String name = typeField.getName();
						STD_SQL_TYPE_NAME_MAP.put(value, name);
						// System.out.println("sqlType: " + value + " => " + name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static DataSource getDataSource(final ReportDataSource rptDs) {
		// 用数据源用户名,密码,jdbcUrl做为key
		final String key = String.format("%s|%s|%s", rptDs.getUser(), rptDs.getPassword(), rptDs.getJdbcUrl()).toLowerCase();
		DataSource dataSource = dataSourceMap.get(key);
		if (dataSource == null) {
			dataSource = DataSourcePoolFactory.create(rptDs.getDbPoolClass()).wrap(rptDs);
			dataSourceMap.put(key, dataSource);
		}
		return dataSource;
	}

	public static void releaseJdbcResource(final Connection conn, final Statement stmt, final ResultSet rs) {
		try {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (final SQLException ex) {
			logger.error("数据库资源释放异常", ex);
			throw new RuntimeException("数据库资源释放异常", ex);
		}
	}

	/** 转换为标准sql类型名称 */
	public static String toStdSqlTypeName(int sqlType) {
		return STD_SQL_TYPE_NAME_MAP.get(sqlType);
	}

	public static String toSimpleDataType(String sqlTypeName) {
		if (sqlTypeName != null) {
			if ("DECIMAL".equals(sqlTypeName) || "DOUBLE".equals(sqlTypeName) || "FLOAT".equals(sqlTypeName) || "REAL".equals(sqlTypeName)) {
				return "float";
			}
			if ("BOOLEAN".equals(sqlTypeName)) {
				return "bool";
			}
			if (sqlTypeName.indexOf("INT") != -1) {
				return "integer";
			}
			if ("DATE".equals(sqlTypeName) || "TIMESTAMP".equals(sqlTypeName) || "TIME".equals(sqlTypeName)) {
				return "date";
			}
		}
		return "string";
	}
}
