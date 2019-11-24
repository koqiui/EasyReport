package com.easytoolsoft.easyreport.engine.data;

import java.text.Format;

/**
 * @author tomdeng
 */
public class ReportMetaDataCell {
	private final ReportMetaDataColumn column;
	private final String name;
	private final Object value;

	public ReportMetaDataCell(ReportMetaDataColumn column, String name, Object value) {
		this.column = column;
		this.name = name;
		this.value = value;
		//
		column.clctCellValue(this.value);
	}

	public ReportMetaDataColumn getColumn() {
		return this.column;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
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
		ReportMetaDataColumn metaData = this.column;
		String theType = metaData.getTheType();
		Format format = metaData.getFormatX();
		if ("float".equals(theType) || "integer".equals(theType) || "date".equals(theType)) {
			if (format != null) {
				try {
					return format.format(this.value);
				} catch (Exception ex) {
					//
				}
			}
		} else if ("bool".equals(theType)) {
			Boolean blValue = (Boolean) value;
			return Boolean.TRUE.equals(blValue) ? "√" : "";
		}
		//
		return this.value.toString();
	}
}
