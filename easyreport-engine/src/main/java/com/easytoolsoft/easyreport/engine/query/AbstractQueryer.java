package com.easytoolsoft.easyreport.engine.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easytoolsoft.easyreport.engine.SortItem;
import com.easytoolsoft.easyreport.engine.data.ColumnType;
import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataCell;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataColumn;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataRow;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;
import com.easytoolsoft.easyreport.engine.data.ReportQueryParamItem;
import com.easytoolsoft.easyreport.engine.data.ReportResult;
import com.easytoolsoft.easyreport.engine.exception.SQLQueryException;
import com.easytoolsoft.easyreport.engine.util.JdbcUtils;

/**
 * @author tomdeng
 */
public abstract class AbstractQueryer implements Queryer {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected final ReportDataSource dataSource;
	protected final ReportParameter parameter;
	protected final List<ReportMetaDataColumn> metaDataColumns;

	protected AbstractQueryer(final ReportDataSource dataSource, final ReportParameter parameter) {
		this.dataSource = dataSource;
		this.parameter = parameter;
		this.metaDataColumns = this.parameter == null ? new ArrayList<>(0) : new ArrayList<>(this.parameter.getMetaColumns());
	}

	public List<ReportMetaDataColumn> parseMetaDataColumns(final String sqlText) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ReportMetaDataColumn> columns = null;
		try {
			this.logger.debug("parseMetaDataColumns SQL:");
			this.logger.debug(sqlText);
			conn = this.getJdbcConnection();
			stmt = conn.createStatement();
			//
			String sqlStr = this.filterSqlText(sqlText);
			sqlStr = this.asSingleRowSqlText(sqlStr);
			this.logger.debug(sqlStr);
			//
			rs = stmt.executeQuery(sqlStr);
			final ResultSetMetaData rsMataData = rs.getMetaData();
			final int count = rsMataData.getColumnCount();
			columns = new ArrayList<>(count);
			String colName = null;
			int colWidthInChars = 0;
			for (int i = 1; i <= count; i++) {
				colName = rsMataData.getColumnLabel(i);
				if (Queryer.ROWNUM_ALIAS.equals(colName)) {
					// 分页行号
					continue;
				}
				final ReportMetaDataColumn column = new ReportMetaDataColumn();
				column.setName(colName);
				String className = rsMataData.getColumnClassName(i);
				column.setClassName(className);
				int sqlType = rsMataData.getColumnType(i);
				if (Boolean.class.getName().equals(className)) {
					sqlType = Types.BOOLEAN;
				}
				// 使用标准sql类型名称（而不是 getColumnTypeName）
				column.setSqlType(JdbcUtils.toStdSqlTypeName(sqlType));
				colWidthInChars = rsMataData.getColumnDisplaySize(i);
				column.setWidthInChars(colWidthInChars);
				column.setWidth(ReportMetaDataColumn.getAvgPixWidthByChars(colWidthInChars));
				columns.add(column);
			}
		} catch (final SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.releaseJdbcResource(conn, stmt, rs);
		}
		return columns;
	}

	public List<ReportQueryParamItem> parseQueryParamItems(final String sqlText) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		final HashSet<String> set = new HashSet<>();
		final List<ReportQueryParamItem> rows = new ArrayList<>();
		try {
			this.logger.debug("parseQueryParamItems SQL:");
			this.logger.debug(sqlText);
			conn = this.getJdbcConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlText);
			while (rs.next()) {
				String name = rs.getString("name");
				String text = rs.getString("text");
				name = (name == null) ? "" : name.trim();
				text = (text == null) ? "" : text.trim();
				if (!set.contains(name)) {
					set.add(name);
				}
				rows.add(new ReportQueryParamItem(name, text));
			}
		} catch (final SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.releaseJdbcResource(conn, stmt, rs);
		}
		set.clear();
		return rows;
	}

	public List<ReportMetaDataColumn> getMetaDataColumns() {
		return this.metaDataColumns;
	}

	public ReportResult getMetaDataResult() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			ReportResult result = new ReportResult();

			conn = this.getJdbcConnection();
			stmt = conn.createStatement();
			//
			this.logger.debug("getMetaDataResult SQL:");
			String sqlText = this.parameter.getSqlText();
			this.logger.debug(sqlText);
			sqlText = this.filterSqlText(sqlText);
			// count
			if (this.parameter.isPageUsed()) {
				String countSql = this.asCountSqlText(sqlText);
				this.logger.debug(countSql);
				rs = stmt.executeQuery(countSql);
				rs.next();
				result.total = rs.getLong(1);
				rs.close();
			} else {
				result.total = -1; // 未知
			}
			// rows
			String sqlStr = this.asPagedSqlText(sqlText);
			this.logger.debug(sqlStr);
			//
			rs = stmt.executeQuery(sqlStr);
			result.rows = this.getMetaDataRows(rs, this.getSqlColumns(this.parameter.getMetaColumns()));
			//
			return result;
		} catch (final Exception ex) {
			this.logger.error(String.format("SqlText:%s，Msg:%s", this.parameter.getSqlText(), ex));
			throw new SQLQueryException(ex);
		} finally {
			JdbcUtils.releaseJdbcResource(conn, stmt, rs);
		}
	}

	protected List<ReportMetaDataRow> getMetaDataRows(final ResultSet rs, final List<ReportMetaDataColumn> sqlColumns) throws SQLException {
		final List<ReportMetaDataRow> rows = new ArrayList<>();
		while (rs.next()) {
			final ReportMetaDataRow row = new ReportMetaDataRow();
			for (final ReportMetaDataColumn column : sqlColumns) {
				Object value = rs.getObject(column.getName());
				if (column.getSqlType().contains("BINARY")) {
					value = new String((byte[]) value);
				}
				row.add(new ReportMetaDataCell(column, column.getName(), value));
			}
			rows.add(row);
		}
		// 实现列各种格式化
		for (final ReportMetaDataColumn column : sqlColumns) {
			column.initFormatInfo();
		}
		//
		return rows;
	}

	public List<Map<String, Object>> getResultSetRows() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		//
		this.logger.debug("getResultSetRows SQL:");
		String sqlText = this.parameter.getSqlText();
		this.logger.debug(sqlText);
		try {
			conn = this.getJdbcConnection();
			stmt = conn.createStatement();
			//
			String sqlStr = this.filterSqlText(sqlText);
			sqlStr = this.asPagedSqlText(sqlStr);
			this.logger.debug(sqlStr);
			rs = stmt.executeQuery(sqlStr);
			// 获取列名称
			final ResultSetMetaData rsMataData = rs.getMetaData();
			final int colCount = rsMataData.getColumnCount();
			String[] colNames = new String[colCount];
			// 日期时间格式
			SimpleDateFormat[] colFormats = new SimpleDateFormat[colCount];
			String colName = null;
			String colStdSqlTypeName = null;
			Set<Integer> binaryCols = new HashSet<>();
			for (int i = 1; i <= colCount; i++) {
				colName = rsMataData.getColumnLabel(i);
				if (Queryer.ROWNUM_ALIAS.equals(colName)) {
					colNames[i - 1] = null;// 分页行号
					continue;
				} else {
					colNames[i - 1] = colName;
				}
				int sqlType = rsMataData.getColumnType(i);
				String className = rsMataData.getColumnClassName(i);
				if (Boolean.class.getName().equals(className)) {
					sqlType = Types.BOOLEAN;
				} else {
					colStdSqlTypeName = JdbcUtils.toStdSqlTypeName(sqlType);
					if (colStdSqlTypeName.contains("BINARY")) {
						binaryCols.add(i);
					}
				}
				// 日期格式化
				if (Types.DATE == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("yyyy-MM-dd");
				} else if (Types.TIME == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("HH:mm:ss");
				} else if (Types.TIMESTAMP == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				} else {
					colFormats[i - 1] = null;
				}
			}
			// 获取结果集
			List<Map<String, Object>> retRows = new ArrayList<>();
			SimpleDateFormat format = null;
			while (rs.next()) {
				final Map<String, Object> retRow = new HashMap<>();
				for (int i = 1; i <= colCount; i++) {
					colName = colNames[i - 1];
					if (colName == null) {
						continue;
					}
					Object value = rs.getObject(colName);
					if (value != null) {
						if (binaryCols.contains(i)) {
							value = new String((byte[]) value);
						} else {
							format = colFormats[i - 1];
							// 日期格式化
							if (format != null) {
								value = format.format(value);
							}
						}
					}
					retRow.put(colName, value);
				}
				retRows.add(retRow);
			}
			return retRows;
		} catch (final Exception ex) {
			this.logger.error(String.format("SqlText:%s，Msg:%s", sqlText, ex));
			throw new SQLQueryException(ex);
		} finally {
			JdbcUtils.releaseJdbcResource(conn, stmt, rs);
		}
	}

	public Map<String, Object> getResultSetMap() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		//
		this.logger.debug("getResultSetMap SQL:");
		String sqlText = this.parameter.getSqlText();
		this.logger.debug(sqlText);
		try {
			Map<String, Object> retMap = new HashMap<>();
			//
			conn = this.getJdbcConnection();
			stmt = conn.createStatement();
			//
			sqlText = this.filterSqlText(sqlText);
			// total
			String countSql = this.asCountSqlText(sqlText);
			rs = stmt.executeQuery(countSql);
			rs.next();//
			long totalCount = rs.getLong(1);
			rs.close();//
			retMap.put("total", totalCount);//
			// rows
			String sqlStr = this.asPagedSqlText(sqlText);
			this.logger.debug(sqlStr);
			rs = stmt.executeQuery(sqlStr);
			// 获取列名称
			final ResultSetMetaData rsMataData = rs.getMetaData();
			final int colCount = rsMataData.getColumnCount();
			String[] colNames = new String[colCount];
			// 日期时间格式
			SimpleDateFormat[] colFormats = new SimpleDateFormat[colCount];
			String colName = null;
			String colStdSqlTypeName = null;
			Set<Integer> binaryCols = new HashSet<>();
			for (int i = 1; i <= colCount; i++) {
				colName = rsMataData.getColumnLabel(i);
				if (Queryer.ROWNUM_ALIAS.equals(colName)) {
					colNames[i - 1] = null;// 分页行号
					continue;
				} else {
					colNames[i - 1] = colName;
				}
				int sqlType = rsMataData.getColumnType(i);
				String className = rsMataData.getColumnClassName(i);
				if (Boolean.class.getName().equals(className)) {
					sqlType = Types.BOOLEAN;
				} else {
					colStdSqlTypeName = JdbcUtils.toStdSqlTypeName(sqlType);
					if (colStdSqlTypeName.contains("BINARY")) {
						binaryCols.add(i);
					}
				}
				// 日期格式化
				if (Types.DATE == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("yyyy-MM-dd");
				} else if (Types.TIME == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("HH:mm:ss");
				} else if (Types.TIMESTAMP == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				} else {
					colFormats[i - 1] = null;
				}
			}
			// 获取结果集
			List<Map<String, Object>> resultRows = new ArrayList<>();
			retMap.put("rows", resultRows);//
			SimpleDateFormat format = null;
			while (rs.next()) {
				final Map<String, Object> retRow = new HashMap<>();
				for (int i = 1; i <= colCount; i++) {
					colName = colNames[i - 1];
					if (colName == null) {
						continue;
					}
					Object value = rs.getObject(colName);
					if (value != null) {
						if (binaryCols.contains(i)) {
							value = new String((byte[]) value);
						} else {
							format = colFormats[i - 1];
							// 日期格式化
							if (format != null) {
								value = format.format(value);
							}
						}
					}
					retRow.put(colName, value);
				}
				resultRows.add(retRow);
			}
			return retMap;
		} catch (final Exception ex) {
			this.logger.error(String.format("SqlText:%s，Msg:%s", sqlText, ex));
			throw new SQLQueryException(ex);
		} finally {
			JdbcUtils.releaseJdbcResource(conn, stmt, rs);
		}
	}

	public List<Map<String, Object>> getResultMapRows(String sqlText) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			this.logger.debug("getResultMapRows SQL:");
			this.logger.debug(sqlText);
			//
			conn = this.getJdbcConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlText);
			// 获取列名称
			final ResultSetMetaData rsMataData = rs.getMetaData();
			final int colCount = rsMataData.getColumnCount();
			String[] colNames = new String[colCount];
			// 日期时间格式
			SimpleDateFormat[] colFormats = new SimpleDateFormat[colCount];
			String colName = null;
			String colStdSqlTypeName = null;
			Set<Integer> binaryCols = new HashSet<>();
			for (int i = 1; i <= colCount; i++) {
				colName = rsMataData.getColumnLabel(i);
				if (Queryer.ROWNUM_ALIAS.equals(colName)) {
					colNames[i - 1] = null;// 分页行号
					continue;
				} else {
					colNames[i - 1] = colName;
				}
				int sqlType = rsMataData.getColumnType(i);
				String className = rsMataData.getColumnClassName(i);
				if (Boolean.class.getName().equals(className)) {
					sqlType = Types.BOOLEAN;
				} else {
					colStdSqlTypeName = JdbcUtils.toStdSqlTypeName(sqlType);
					if (colStdSqlTypeName.contains("BINARY")) {
						binaryCols.add(i);
					}
				}
				// 日期格式化
				if (Types.DATE == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("yyyy-MM-dd");
				} else if (Types.TIME == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("HH:mm:ss");
				} else if (Types.TIMESTAMP == sqlType) {
					colFormats[i - 1] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				} else {
					colFormats[i - 1] = null;
				}
			}
			// 获取结果集
			List<Map<String, Object>> retRows = new ArrayList<>();
			SimpleDateFormat format = null;
			while (rs.next()) {
				final Map<String, Object> retRow = new HashMap<>();
				for (int i = 1; i <= colCount; i++) {
					colName = colNames[i - 1];
					if (colName == null) {
						continue;
					}
					Object value = rs.getObject(colName);
					if (value != null) {
						if (binaryCols.contains(i)) {
							value = new String((byte[]) value);
						} else {
							format = colFormats[i - 1];
							// 日期格式化
							if (format != null) {
								value = format.format(value);
							}
						}
					}
					retRow.put(colName, value);
				}
				retRows.add(retRow);
			}
			return retRows;
		} catch (final Exception ex) {
			this.logger.error(String.format("SqlText:%s，Msg:%s", sqlText, ex));
			throw new SQLQueryException(ex);
		} finally {
			JdbcUtils.releaseJdbcResource(conn, stmt, rs);
		}
	}

	protected List<ReportMetaDataColumn> getSqlColumns(final List<ReportMetaDataColumn> metaDataColumns) {
		return metaDataColumns.stream().filter(x -> x.getType() != ColumnType.COMPUTED).collect(Collectors.toList());
	}

	/** 返回order by 语句 */
	protected String getOrderByStr() {
		return this.getOrderByStr(null);
	}

	/** 返回order by 语句（列名加表别名） */
	protected String getOrderByStr(String tblAlias) {
		List<SortItem> sortItems = this.parameter.getSortItems();
		if (sortItems != null && sortItems.size() > 0) {
			List<String> orderStrs = new ArrayList<>();
			if (tblAlias == null) {
				for (SortItem sortItem : sortItems) {
					orderStrs.add(sortItem.toString());
				}
			} else {
				for (SortItem sortItem : sortItems) {
					orderStrs.add(sortItem.toString(tblAlias));
				}
			}
			return "ORDER BY " + StringUtils.join(orderStrs, ", ");
		}
		//
		return null;
	}

	protected boolean endsWithOrderBy(final String sqlText) {
		final Pattern pattern = Pattern.compile("order\\s+by\\s+.*?$", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(sqlText);
		if (matcher.find()) {
			String matched = matcher.group();
			if (matched.indexOf(")") == -1) {
				return true;
			}
		}
		return false;
	}

	/** 过滤sql文本（比如转义、去掉结尾的; 及 分页信息等） */
	protected String filterSqlText(final String sqlText) {
		return StringUtils.stripEnd(sqlText.trim(), ";");
	}

	/** 转换为仅获取1行结果的sql */
	protected String asSingleRowSqlText(final String sqlText) {
		return this.asPagedSqlText(sqlText, true);
	}

	/** 转换为 计算总数量的sql */
	protected String asCountSqlText(final String sqlText) {
		return "SELECT count(1) count FROM (" + sqlText + ") TMP_TBL";
	}

	/** 为sql 增加分页 */
	protected abstract String asPagedSqlText(final String sqlText, boolean forMetaOnly);

	/** 为sql 增加分页 */
	protected String asPagedSqlText(final String sqlText) {
		return this.asPagedSqlText(sqlText, false);
	}

	/**
	 * 获取当前报表查询器的JDBC Connection对象
	 *
	 * @return Connection
	 */
	protected Connection getJdbcConnection() {
		try {
			Class.forName(this.dataSource.getDriverClass());
			return JdbcUtils.getDataSource(this.dataSource).getConnection();
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
