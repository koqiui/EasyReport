package com.easytoolsoft.easyreport.engine.query;

import java.util.List;
import java.util.Map;

import com.easytoolsoft.easyreport.engine.data.ReportMetaDataColumn;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataRow;
import com.easytoolsoft.easyreport.engine.data.ReportQueryParamItem;

/**
 * 报表查询器接口
 *
 * @author tomdeng
 */
public interface Queryer {
	/**
	 * 从sql语句中解析出报表元数据列集合
	 *
	 * @param sqlText
	 *            sql语句
	 * @return List[ReportMetaDataColumn]
	 */
	List<ReportMetaDataColumn> parseMetaDataColumns(String sqlText);

	/**
	 * 从sql语句中解析出报表查询参数(如下拉列表参数）的列表项集合
	 *
	 * @param sqlText
	 *            sql语句
	 * @return List[ReportQueryParamItem]
	 */
	List<ReportQueryParamItem> parseQueryParamItems(String sqlText);

	/**
	 * 获取报表原始数据行集合
	 *
	 * @return List[ReportMetaDataRow]
	 */
	List<ReportMetaDataRow> getMetaDataRows();

	/**
	 * 获取报表原始数据列集合
	 *
	 * @return List[ReportMetaDataColumn]
	 */
	List<ReportMetaDataColumn> getMetaDataColumns();

	/**
	 * 返回查询统计主体的原生结果行集
	 * 
	 * @author koqiui
	 * @date 2019年11月10日 下午6:48:51
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getResultSetRows();

	/**
	 * 返回指定的sql语句的原生结果行集
	 * 
	 * @author koqiui
	 * @date 2019年11月10日 下午10:18:48
	 * 
	 * @param sqlText
	 *            指定的sql语句
	 * @return
	 */
	public List<Map<String, Object>> getResultSetRows(String sqlText);
}
