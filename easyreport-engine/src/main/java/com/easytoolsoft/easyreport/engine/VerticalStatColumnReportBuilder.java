package com.easytoolsoft.easyreport.engine;

import java.util.List;
import java.util.Map;

import com.easytoolsoft.easyreport.engine.data.AbstractReportDataSet;
import com.easytoolsoft.easyreport.engine.data.ColumnTree;
import com.easytoolsoft.easyreport.engine.data.ColumnTreeNode;
import com.easytoolsoft.easyreport.engine.data.ReportDataCell;
import com.easytoolsoft.easyreport.engine.data.ReportDataRow;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;

/**
 * 纵向展示统计列的报表生成类
 *
 * @author tomdeng
 */
public class VerticalStatColumnReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	/**
	 * 纵向展示统计列的报表生成类
	 *
	 * @param reportDataSet
	 *            报表数据集
	 * @param reportParameter
	 *            报表参数
	 */
	public VerticalStatColumnReportBuilder(final AbstractReportDataSet reportDataSet, final ReportParameter reportParameter) {
		super(reportDataSet, reportParameter);
	}

	@Override
	public void drawTableBodyRows() {
		final ColumnTree leftFixedColumnTree = this.reportDataSet.getBodyLeftFixedColumnTree();
		final List<ColumnTreeNode> rowNodes = leftFixedColumnTree.getLastLevelNodes();
		final List<ColumnTreeNode> columnNodes = this.reportDataSet.getBodyRightColumnNodes();
		final Map<String, ReportDataRow> dataRowMap = this.reportDataSet.getRowMap();
		final Map<String, ColumnTreeNode> treeNodePathMap = this.getTreeNodePathMap(leftFixedColumnTree);
		final String defaultColumName = this.reportDataSet.getEnabledStatColumns().get(0).getName();
		final boolean isHideStatColumn = this.reportDataSet.isHideStatColumn();

		int rowIndex = 0;
		String[] lastNodePaths = null;
		//
		LinkFunc linkFunc = null;
		boolean showDataLinks = this.reportParameter.shouldShowDataLinks();
		String reportCode = this.reportParameter.getUcode();
		//
		this.tableRows.append("<tbody>");
		for (final ColumnTreeNode rowNode : rowNodes) {
			final String columnName = isHideStatColumn ? defaultColumName : rowNode.getName();
			this.tableRows.append("<tr").append(rowIndex % 2 == 0 ? " class=\"easyreport-row\"" : "").append(">");
			lastNodePaths = this.drawLeftFixedColumn(treeNodePathMap, lastNodePaths, rowNode, this.reportParameter.isRowSpan());
			for (final ColumnTreeNode columnNode : columnNodes) {
				final String rowKey = this.reportDataSet.getRowKey(rowNode, columnNode);
				ReportDataRow dataRow = dataRowMap.get(rowKey);
				if (dataRow == null) {
					dataRow = new ReportDataRow();
				}
				final ReportDataCell cell = dataRow.getCell(columnName);
				String value = (cell == null) ? "" : cell.toString();
				linkFunc = columnNode.getColumn().getLinkFunc();
				if (showDataLinks && linkFunc != null && value.length() > 0) {
					value = LinkFunc.toLinkHtml(value, linkFunc, reportCode, dataRow.getDataMap());
				}
				String style = cell == null ? "" : cell.getStyle();
				this.tableRows.append(String.format("<td style=\"%s\">", style)).append(value).append("</td>");
			}
			this.tableRows.append("</tr>");
			rowIndex++;
		}
		this.tableRows.append("</tbody>");
	}

	@Override
	public void drawTableFooterRows() {
	}
}
