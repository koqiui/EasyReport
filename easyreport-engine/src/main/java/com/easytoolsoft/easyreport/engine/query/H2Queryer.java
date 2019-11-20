package com.easytoolsoft.easyreport.engine.query;

import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;

public class H2Queryer extends AbstractQueryer {
	public H2Queryer(final ReportDataSource dataSource, final ReportParameter parameter) {
		super(dataSource, parameter);
	}

	@Override
	protected String preprocessSqlText(String sqlText) {
		sqlText = sqlText.replace('"', '\'');
		return sqlText;
	}
}
