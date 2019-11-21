package com.easytoolsoft.easyreport.engine.data;

import java.text.Format;

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
		if (this.value == null) {
			return "";
		}
		//
		ReportMetaDataColumn metaData = this.column.getMetaData();
		String theType = metaData.getJavaType();
		Format format = metaData.getFormatX();
		if ("float".equals(theType) || "integer".equals(theType) || "date".equals(theType)) {
			if (format != null) {
				try {
					return format.format(this.value);
				} catch (Exception ex) {
					//
				}
			}
		}
		//
		return this.value.toString();
	}
}
