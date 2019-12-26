package com.easytoolsoft.easyreport.engine.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;

/**
 * SQLite3数据库查询器类。 在使用该查询器时,请先参考:https://bitbucket.org/xerial/sqlite-jdbc <br/>
 * 获取jdbc driver,然后把相关jdbc driver的jar包加入该系统的类路径下(如WEB-INF/lib) <br/>
 * limit ? offset ?
 * 
 * @author tomdeng
 */
public class SQLiteQueryer extends AbstractQueryer {
	public SQLiteQueryer(final ReportDataSource dataSource, final ReportParameter parameter) {
		super(dataSource, parameter);
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
			long offset = (pageNo - 1) * pageSize;
			retSql += "\r" + "limit " + pageSize + " offset " + offset;
		} else {
			retSql += "\r" + "limit " + pageSize;
		}

		return retSql;
	}
}
