package com.easytoolsoft.easyreport.engine.data;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.easytoolsoft.easyreport.engine.LinkFunc;
import com.easytoolsoft.easyreport.engine.util.ClrLvlUtils;
import com.easytoolsoft.easyreport.engine.util.JdbcUtils;
import com.easytoolsoft.easyreport.engine.util.NumberFormatUtils;

/**
 * 报表元数据列类
 *
 * @author tomdeng
 */
public class ReportMetaDataColumn {
	private int ordinal;
	private String name;
	private String text;
	private String className;
	private String sqlType;// sql type name
	private String theType; // simple data type
	// 对齐方式
	private String align;
	// 色阶
	private Boolean clrLvlEnabled = false;// 是否启用色阶
	private Integer clrLvlValve;// 最少异值数量
	private String clrLvlStart;// 色阶起始色
	private String clrLvlEnd;// 色阶终止色
	//
	private String expression;
	private String format;// 日期、数值格式化字符串
	private Format formatX;
	private String comment;
	private int width;
	private ColumnType type = ColumnType.DIMENSION;
	private ColumnSortType sortType = ColumnSortType.DEFAULT;
	private boolean isExtensions;
	private boolean isFootings;
	private boolean isPercent;
	// 自定义链接函数
	// showReportDetail([colName])
	// showReportDetail([colName1, colName2,...])
	// => showReportDetail({"colName1" : value1, "colName2" : value2, ...}, "${reportCode}")
	// <a href='#' onclick='showReportDetail({"colName1" : value1, "colName2" : value2, ...}, "${reportCode}")'></a>
	private String linkFuncExpr;
	private boolean isOptional;
	private boolean isDisplayInMail;
	private boolean isHidden;

	// 不同地数值集合
	private Set<Number> diffValues;
	private Map<Number, String> clrLvlMap;
	// 链接信息
	private LinkFunc linkFunc;

	public ReportMetaDataColumn() {
		//
	}

	public ReportMetaDataColumn(final String name, final String text, final ColumnType type) {
		this.name = name;
		this.text = text;
		this.type = type;
	}

	private String asNumStr(Object cellValue) {
		String valueStr = cellValue == null ? null : cellValue.toString();
		if (valueStr == null) {
			return null;
		}
		if (NumberFormatUtils.isNumber(valueStr)) {
			return valueStr;
		}
		return null;
	}

	/** 收集单元格的值（异值） */
	public void clctCellValue(Object cellValue) {
		if (diffValues == null) {
			diffValues = new TreeSet<>();
		}
		if (clrLvlEnabled && clrLvlValve != null && clrLvlValve.intValue() > 0 && StringUtils.isNotBlank(this.clrLvlStart) && StringUtils.isNotBlank(this.clrLvlEnd)) {
			if ((ColumnType.STATISTICAL.equals(this.type) || ColumnType.COMPUTED.equals(this.type))) {
				if (cellValue != null) {
					String theType = this.theType;
					String valStr = this.asNumStr(cellValue);
					if (valStr != null) {// 有可能是tinyint之类的bool值
						if ("integer".equals(theType)) {
							diffValues.add(Long.valueOf(valStr));
						} else if ("float".equals(theType)) {
							diffValues.add(Double.valueOf(valStr));
						}
					}
				}
			}
		}
	}

	public void initFormatInfo() {
		String theType = this.theType;
		//
		String theFormat = null;
		if (StringUtils.isBlank(this.format)) {
			if ("date".equals(theType)) {
				if ("DATE".equals(sqlType)) {
					theFormat = "yyyy-MM-dd";
				} else if ("TIME".equals(sqlType)) {
					theFormat = "HH:mm:ss";
				} else {// "TIMESTAMP".equals(sqlType)
					theFormat = "yyyy-MM-dd HH:mm:ss";
				}
				formatX = new SimpleDateFormat(theFormat);
			} else if ("float".equals(theType)) {
				theFormat = "#0.00";
				if (this.isPercent) {
					theFormat += "%";
				}
				// 不给integer默认
				formatX = new DecimalFormat(theFormat);
			}
		} else {
			theFormat = this.format.trim();
			try {
				if ("date".equals(theType)) {
					formatX = new SimpleDateFormat(theFormat);
				} else if ("float".equals(theType)) {
					if (this.isPercent && theFormat.indexOf("%") == -1) {
						theFormat += "%";
					}
					formatX = new DecimalFormat(theFormat);
				} else if ("integer".equals(theType)) {
					formatX = new DecimalFormat(theFormat);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// 色阶信息
		if (clrLvlEnabled && clrLvlValve != null && clrLvlValve.intValue() > 0 && StringUtils.isNotBlank(this.clrLvlStart) && StringUtils.isNotBlank(this.clrLvlEnd)) {
			if (diffValues != null && diffValues.size() >= clrLvlValve.intValue()) {
				// System.out.println(diffValues);
				// 计算色阶colorMap
				Color startColor = ClrLvlUtils.hexColorToColor(this.clrLvlStart);
				if (startColor == null) {
					return;
				}
				Color endColor = ClrLvlUtils.hexColorToColor(this.clrLvlEnd);
				if (endColor == null) {
					return;
				}
				if ("integer".equals(theType)) {
					int ukCount = this.diffValues.size();
					long[] ukNums = new long[ukCount];
					int ukIndex = 0;
					for (Number diffValue : this.diffValues) {
						ukNums[ukIndex++] = (long) diffValue;
					}
					this.clrLvlMap = ClrLvlUtils.getGradientColorMap(startColor, endColor, ukNums, this.clrLvlValve);
				} else if ("float".equals(theType)) {
					int ukCount = this.diffValues.size();
					double[] ukNums = new double[ukCount];
					int ukIndex = 0;
					for (Number diffValue : this.diffValues) {
						ukNums[ukIndex++] = (double) diffValue;
					}
					this.clrLvlMap = ClrLvlUtils.getGradientColorMap(startColor, endColor, ukNums, this.clrLvlValve);
				}
			}
		}

	}

	/**
	 * 获取报表元数据列的顺序(从1开始)
	 *
	 * @return 报表元数据列的顺序
	 */
	public int getOrdinal() {
		return this.ordinal;
	}

	/**
	 * 设置报表元数据列的顺序
	 *
	 * @return 报表元数据列的顺序(从1开始)
	 */
	public void setOrdinal(final int ordinal) {
		this.ordinal = ordinal;
	}

	/**
	 * 获取报表元数据列名
	 *
	 * @return 元数据列名
	 */
	public String getName() {
		return this.name == null ? "" : this.name.trim();
	}

	/**
	 * 设置报表元数据列名
	 *
	 * @param name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * 获取报表元数据列对应的标题文本
	 *
	 * @return 报表元数据列对应的标题文本
	 */
	public String getText() {
		if (this.text == null || this.text.trim().length() == 0) {
			return this.name;
		}
		return this.text;
	}

	/**
	 * 设置报表元数据列对应的标题文本
	 *
	 * @param text
	 */
	public void setText(final String text) {
		this.text = text;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * 获取报表元数据列数据类型名称
	 *
	 * @return sqlType java.sql.Types中的类型名称
	 */
	public String getSqlType() {
		return this.sqlType;
	}

	/**
	 * 设置报表元数据列数据类型名称 => setSqlType
	 * 
	 * @param sqlType
	 *            java.sql.Types中的类型名称
	 */
	@Deprecated
	public void setDataType(final String sqlType) {
		if (StringUtils.isNotBlank(sqlType)) {
			this.sqlType = sqlType;
			//
			this.theType = JdbcUtils.toSimpleDataType(sqlType);
		}
	}

	public void setSqlType(final String sqlType) {
		this.sqlType = sqlType;
		//
		this.theType = JdbcUtils.toSimpleDataType(sqlType);
	}

	/** string, integer, float, date */
	public String getTheType() {
		return theType;
	}

	public String getAlign() {
		if (StringUtils.isBlank(align)) {
			String theType = this.theType;
			if ("integer".equals(theType) || "float".equals(theType)) {
				return "right";
			}
			if ("date".equals(theType) || "bool".equals(theType)) {
				return "center";
			}
			return "left";
		}
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public Boolean getClrLvlEnabled() {
		return clrLvlEnabled == null ? false : clrLvlEnabled;
	}

	public void setClrLvlEnabled(Boolean clrLvlEnabled) {
		this.clrLvlEnabled = clrLvlEnabled == null ? false : clrLvlEnabled;
	}

	public Integer getClrLvlValve() {
		return clrLvlValve;
	}

	public void setClrLvlValve(Integer clrLvlValve) {
		this.clrLvlValve = clrLvlValve;
	}

	public String getClrLvlStart() {
		return clrLvlStart;
	}

	public void setClrLvlStart(String clrLvlStart) {
		this.clrLvlStart = clrLvlStart;
	}

	public String getClrLvlEnd() {
		return clrLvlEnd;
	}

	public void setClrLvlEnd(String clrLvlEnd) {
		this.clrLvlEnd = clrLvlEnd;
	}

	/**
	 * 获取报表元数据计算列的计算表达式
	 *
	 * @return 计算表达式
	 */
	public String getExpression() {
		return this.expression == null ? "" : this.expression;
	}

	/**
	 * 设置报表元数据计算列的计算表达式
	 *
	 * @param expression
	 *            计算表达式
	 */
	public void setExpression(final String expression) {
		this.expression = expression;
	}

	/**
	 * 获取报表元数据列的格式 日期、数值格式化字符串
	 *
	 * @return 格式化字符串
	 */
	public String getFormat() {
		return this.format;
	}

	/**
	 * 设置报表元数据列的格式
	 *
	 * @param format
	 *            格式化字符串
	 */
	public void setFormat(final String format) {
		this.format = format;
	}

	public Format getFormatX() {
		return this.formatX;
	}

	/**
	 * 获取报表元数据列宽(单位:像素)
	 *
	 * @return 列宽(单位:像素)
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * 设置报表元数据列宽(单位:像素)
	 *
	 * @param width
	 */
	public void setWidth(final int width) {
		this.width = width;
	}

	/**
	 * 获取报表元数据列类型
	 *
	 * @return 列类型(1：布局列, 2:维度列，3:统计列, 4:计算列)
	 */
	public ColumnType getType() {
		return this.type;
	}

	public ColumnType guessType() {
		String theType = this.theType;
		if ("integer".equals(theType) || "float".equals(theType)) {
			return ColumnType.STATISTICAL;
		}
		//
		return this.type;
	}

	/**
	 * 设置报表元数据列类型
	 *
	 * @param type
	 *            (1：布局列,2:维度列，3:统计列,4:计算列)
	 */
	public void setType(final int type) {
		this.type = ColumnType.valueOf(type);
	}

	/**
	 * 设置列排序类型(0:默认,1：数字优先升序,2:数字优先降序,3：字符优先升序,4:字符优先降序)
	 */
	public ColumnSortType getSortType() {
		return this.sortType;
	}

	/**
	 * 设置列排序类型
	 *
	 * @param sortType
	 *            (0:默认,1：数字优先升序,2:数字优先降序,3：字符优先升序,4:字符优先降序)
	 */
	public void setSortType(final int sortType) {
		this.sortType = ColumnSortType.valueOf(sortType);
	}

	/**
	 * 获取元数据列是否支持小计
	 *
	 * @return true|false
	 */
	public boolean isExtensions() {
		return this.isExtensions;
	}

	/**
	 * 设置元数据列是否支持小计
	 *
	 * @param isExtensions
	 */
	public void setExtensions(final boolean isExtensions) {
		this.isExtensions = isExtensions;
	}

	/**
	 * 获取元数据列是否支持条件查询
	 *
	 * @return true|false
	 */
	public boolean isFootings() {
		return this.isFootings;
	}

	/**
	 * 设置元数据列是否支持合计
	 *
	 * @param isFootings
	 */
	public void setFootings(final boolean isFootings) {
		this.isFootings = isFootings;
	}

	/**
	 * 获取元数据列是否支持百分比显示
	 *
	 * @return true|false
	 */
	public boolean isPercent() {
		return this.isPercent;
	}

	/**
	 * 设置元数据列是否支持百分比显示
	 *
	 * @param isPercent
	 */
	public void setPercent(final boolean isPercent) {
		this.isPercent = isPercent;
	}

	public String getLinkFuncExpr() {
		return linkFuncExpr;
	}

	public void setLinkFuncExpr(String linkFuncExpr) {
		this.linkFuncExpr = linkFuncExpr;
		//
		this.linkFunc = LinkFunc.fromLinkFuncExpr(linkFuncExpr);
	}

	public LinkFunc getLinkFunc() {
		return linkFunc;
	}

	/**
	 * 获取配置列是否支持可选择显示
	 *
	 * @return true|false
	 */
	public boolean isOptional() {
		return this.isOptional;
	}

	/**
	 * 设置配置列是否支持可选择显示
	 *
	 * @param isOptional
	 */
	public void setOptional(final boolean isOptional) {
		this.isOptional = isOptional;
	}

	/**
	 * 获取配置列中的统计列(含计算)是否支持在邮件中显示
	 *
	 * @return true|false
	 */
	public boolean isDisplayInMail() {
		return this.isDisplayInMail;
	}

	/**
	 * 设置配置列中的统计列(含计算)是否支持在邮件中显示
	 *
	 * @param isDisplayInMail
	 */
	public void setDisplayInMail(final boolean isDisplayInMail) {
		this.isDisplayInMail = isDisplayInMail;
	}

	/**
	 * 获取元数据列是否隐藏
	 *
	 * @return true|false
	 */
	public boolean isHidden() {
		return this.isHidden;
	}

	/**
	 * 设置元数据列是否隐藏
	 *
	 * @param isHidden
	 */
	public void setHidden(final boolean isHidden) {
		this.isHidden = isHidden;
	}

	/**
	 * 获取元数据列备注
	 *
	 * @return
	 */
	public String getComment() {
		return this.comment == null ? "" : this.comment;
	}

	/**
	 * 设置元数据列备注
	 *
	 * @param comment
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}

	public String getStyle(Object cellValue) {
		List<String> styleItems = new ArrayList<>();
		// 对齐
		String align = this.getAlign();
		styleItems.add("text-align: " + align);
		// 色阶
		if (cellValue != null && this.clrLvlMap != null) {
			String theType = this.theType;
			String valStr = this.asNumStr(cellValue);
			if (valStr != null) {// 有可能是tinyint之类的bool值
				Number valueKey = null;
				if ("integer".equals(theType)) {
					valueKey = Long.valueOf(valStr);
				} else if ("float".equals(theType)) {
					valueKey = Double.valueOf(valStr);
				}
				if (valueKey != null) {
					String color = this.clrLvlMap.get(valueKey);
					if (color != null) {
						styleItems.add("background-color: " + color);
					}
				}
			}
		}
		//
		return StringUtils.join(styleItems, "; ");
	}

}