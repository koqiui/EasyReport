package com.easytoolsoft.easyreport.engine.data;

import java.text.SimpleDateFormat;

import com.easytoolsoft.easyreport.engine.util.NumberFormatUtils;

/**
 * @author tomdeng
 */
public class ReportDataCell {
	private final ReportDataColumn column;
	private final String name;
	private Object value;

	public ReportDataCell(final ReportDataColumn column, final String name, final Object value) {
		this.column = column;
		this.name = name;
		this.value = value;
	}

	public ReportDataColumn getColumn() {
		return this.column;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(final Object value) {
		this.value = value;
	}

	public String getStyle() {
		return this.column.getStyle(null);
	}

	@Override
	public String toString() {
		ReportMetaDataColumn metaData = this.column.getMetaData();
		int decimals = metaData.getDecimals();
		if (metaData.isPercent()) {
			decimals = decimals <= 0 ? 2 : decimals;
			return NumberFormatUtils.percentFormat(this.value, decimals);
		}
		String sqlType = metaData.getDataType();
		String theType = metaData.getJavaType();
		if ("float".equals(theType)) {
			decimals = decimals <= 0 ? 4 : decimals;
			return NumberFormatUtils.decimalFormat(this.value, decimals);
		} else if ("date".equals(theType)) {
			if (this.value instanceof java.util.Date) {
				if ("DATE".equals(sqlType)) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					return df.format(this.value);
				}
				if ("TIMESTAMP".equals(sqlType)) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					return df.format(this.value);
				}
				if ("TIME".equals(sqlType)) {
					SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
					return df.format(this.value);
				}
			}
		}

		return NumberFormatUtils.format(this.value);

	}
}
