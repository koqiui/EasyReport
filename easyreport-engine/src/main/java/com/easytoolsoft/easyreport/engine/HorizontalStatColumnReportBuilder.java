package com.easytoolsoft.easyreport.engine;

import java.util.List;
import java.util.Map;

import com.easytoolsoft.easyreport.engine.data.AbstractReportDataSet;
import com.easytoolsoft.easyreport.engine.data.ColumnTree;
import com.easytoolsoft.easyreport.engine.data.ColumnTreeNode;
import com.easytoolsoft.easyreport.engine.data.ReportDataCell;
import com.easytoolsoft.easyreport.engine.data.ReportDataColumn;
import com.easytoolsoft.easyreport.engine.data.ReportDataRow;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;

/**
 * 横向展示统计列的报表生成类
 *
 * @author tomdeng
 */
public class HorizontalStatColumnReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	/**
	 * 横向展示统计列的报表生成类
	 *
	 * @param reportDataSet
	 *            报表数据集
	 * @param reportParameter
	 *            报表参数
	 */
	public HorizontalStatColumnReportBuilder(final AbstractReportDataSet reportDataSet, final ReportParameter reportParameter) {
		super(reportDataSet, reportParameter);
	}

	@Override
	public void drawTableBodyRows() {
		final ColumnTree leftFixedColumnTree = this.reportDataSet.getBodyLeftFixedColumnTree();
		final List<ColumnTreeNode> rowNodes = leftFixedColumnTree.getLastLevelNodes();
		final List<ColumnTreeNode> columnNodes = this.reportDataSet.getBodyRightColumnNodes();
		final Map<String, ReportDataRow> dataRowMap = this.reportDataSet.getRowMap();
		final List<ReportDataColumn> statColumns = this.reportDataSet.getEnabledStatColumns();
		final Map<String, ColumnTreeNode> treeNodePathMap = this.getTreeNodePathMap(leftFixedColumnTree);

		int rowIndex = 0;
		String[] lastNodePaths = null;
		//
		LinkFunc linkFunc = null;
		boolean showDataLinks = this.reportParameter.shouldShowDataLinks();
		String reportCode = this.reportParameter.getUcode();
		//
		this.tableRows.append("<tbody>");
		for (final ColumnTreeNode rowNode : rowNodes) {
			this.tableRows.append("<tr").append(rowIndex % 2 == 0 ? " class=\"easyreport-row\"" : "").append(">");
			lastNodePaths = this.drawLeftFixedColumn(treeNodePathMap, lastNodePaths, rowNode, this.reportParameter.isRowSpan());
			for (final ColumnTreeNode columnNode : columnNodes) {
				final String rowKey = this.reportDataSet.getRowKey(rowNode, columnNode);
				ReportDataRow dataRow = dataRowMap.get(rowKey);
				if (dataRow == null) {
					dataRow = new ReportDataRow();
				}
				for (final ReportDataColumn statColumn : statColumns) {
					final ReportDataCell cell = dataRow.getCell(statColumn.getName());
					String value = (cell == null) ? "" : cell.toString();
					linkFunc = statColumn.getLinkFunc();
					if (showDataLinks && linkFunc != null && value.length() > 0) {
						value = LinkFunc.toLinkHtml(value, linkFunc, reportCode, dataRow.getDataMap());
					}
					String style = cell == null ? "" : statColumn.getStyle(cell.getValue());
					this.tableRows.append(String.format("<td style=\"%s\">", style)).append(value).append("</td>");
				}
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
