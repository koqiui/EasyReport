package com.easytoolsoft.easyreport.engine.query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;

/**
 * @author tomdeng
 */ /*
	 * @see https://prestodb.io/docs/current/installation/jdbc.html
	 * 
	 * @author tomdeng
	 */
public class PrestoQueryer extends AbstractQueryer {
	public PrestoQueryer(ReportDataSource dataSource, ReportParameter parameter) {
		super(dataSource, parameter);

	}

	@Override
	protected Connection getJdbcConnection() {
		try {
			Class.forName(this.dataSource.getDriverClass());
			return DriverManager.getConnection(this.dataSource.getJdbcUrl(), this.dataSource.getUser(), this.dataSource.getPassword());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected String filterSqlText(String sqlText) {
		sqlText = super.filterSqlText(sqlText);
		final Pattern pattern = Pattern.compile("limit.*?$", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(sqlText);
		if (matcher.find()) {
			sqlText = matcher.replaceFirst("");
		}
		return sqlText;
	}

	@Override
	protected String asPagedSqlText(String sqlText, boolean forMetaOnly) {
		if (forMetaOnly) {
			return sqlText + "\r" + "limit 1";
		}

		String retSql = sqlText;
		if (this.endsWithOrderBy(retSql)) {
			retSql = "SELECT TMP.* FROM (\r" + retSql + "\r) TMP";
		}

		String orderByStr = this.getOrderByStr();
		if (orderByStr != null) {
			retSql += "\r" + orderByStr;
		}

		int pageNo = this.parameter.getPageNo();
		int pageSize = this.parameter.getPageSize();
		if (this.parameter.isPageUsed()) {
			// long offset = (pageNo - 1) * pageSize;
			// retSql += "\r" + "limit " + offset + ", " + pageSize;
		} else {
			// retSql += "\r" + "limit " + pageSize;
		}

		retSql += "\r" + "limit " + pageSize;

		return retSql;
	}
}