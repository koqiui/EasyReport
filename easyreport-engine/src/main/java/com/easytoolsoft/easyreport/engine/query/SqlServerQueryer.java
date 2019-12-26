package com.easytoolsoft.easyreport.engine.query;

import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;

/**
 * MS SQLServer 数据库查询器类。 在使用该查询器时,请先参考:https://msdn.microsoft.com/library/mt484311.aspx <br/>
 * 获取sqlserver jdbc driver,然后把相关jdbc driver的jar包加入该系统的类路径下(如WEB-INF/lib) <br/>
 * 注意：仅支持 2005及以上版本 sql server
 * 
 * @author tomdeng
 */
public class SqlServerQueryer extends AbstractQueryer {
	public SqlServerQueryer(final ReportDataSource dataSource, final ReportParameter parameter) {
		super(dataSource, parameter);
	}

	@Override
	protected String asPagedSqlText(String sqlText, boolean forMetaOnly) {
		if (forMetaOnly) {
			return "SELECT top 1 * FROM (\r" + sqlText + "\r) TMP_TBL";
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

		String overStr = "";
		String orderByStr = this.getOrderByStr("TMP_TBL");
		if (orderByStr == null) {
			orderByStr = "ORDER BY CURRENT_TIMESTAMP";// 没有over会报错
		}
		overStr = "over( " + orderByStr + " )";

		retSql = "SELECT TMP_TBX.* FROM (SELECT TMP_TBL.*, row_number() " + overStr + " as " + Queryer.ROWNUM_ALIAS + " FROM (\r" + retSql + "\r) TMP_TBL) TMP_TBX WHERE " + Queryer.ROWNUM_ALIAS + " >= " + startRow + " AND "
				+ Queryer.ROWNUM_ALIAS + " <= " + endRow;

		return retSql;
	}

}
