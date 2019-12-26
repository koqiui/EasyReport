package com.easytoolsoft.easyreport.engine.query;

import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;

/**
 * Oracle数据库查询器类。 在使用该查询器时,请先参考:http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html 获取与相应版本的Oracle jdbc driver,然后把相关jdbc driver的jar包加入该系统的类路径下(如WEB-INF/lib)
 *
 * @author tomdeng
 */
public class OracleQueryer extends AbstractQueryer {
	public OracleQueryer(final ReportDataSource dataSource, final ReportParameter parameter) {
		super(dataSource, parameter);
	}

	@Override
	protected String asPagedSqlText(String sqlText, boolean forMetaOnly) {
		if (forMetaOnly) {
			return "SELECT TMP_TBL.* FROM (\r" + sqlText + "\r) TMP_TBL WHERE ROWNUM = 1";
		}

		String retSql = sqlText;
		if (this.endsWithOrderBy(retSql)) {
			retSql = "SELECT TMP.* FROM (\r" + retSql + "\r) TMP";
		}

		long startRow = 0;
		long endRow = 0;
		//
		int pageNo = this.parameter.getPageNo();
		int pageSize = this.parameter.getPageSize();
		if (this.parameter.isPageUsed()) {
			startRow = (pageNo - 1) * pageSize + 1;
			endRow = pageNo * pageSize;
		} else {
			startRow = 1;
			endRow = pageSize;
		}
		retSql = "SELECT TMP_TBX.* FROM (SELECT TMP_TBL.*, ROWNUM " + Queryer.ROWNUM_ALIAS + " FROM (\r" + retSql + "\r) TMP_TBL WHERE ROWNUM <= " + endRow + ") TMP_TBX WHERE " + Queryer.ROWNUM_ALIAS + " >= " + startRow;

		String orderByStr = this.getOrderByStr();
		if (orderByStr != null) {
			retSql += "\r" + orderByStr;
		}

		return retSql;
	}

}
