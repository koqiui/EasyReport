package com.easytoolsoft.easyreport.meta.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.easytoolsoft.easyreport.common.util.NumUtils;
import com.easytoolsoft.easyreport.common.util.StrUtils;
import com.easytoolsoft.easyreport.engine.ReportGenerator;
import com.easytoolsoft.easyreport.engine.data.ColumnType;
import com.easytoolsoft.easyreport.engine.data.ReportDataSet;
import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataColumn;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataSet;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;
import com.easytoolsoft.easyreport.engine.data.ReportQueryParamItem;
import com.easytoolsoft.easyreport.engine.data.ReportSqlTemplate;
import com.easytoolsoft.easyreport.engine.data.ReportTable;
import com.easytoolsoft.easyreport.engine.query.Queryer;
import com.easytoolsoft.easyreport.engine.util.VelocityUtils;
import com.easytoolsoft.easyreport.meta.domain.Report;
import com.easytoolsoft.easyreport.meta.domain.options.QueryParameterOptions;
import com.easytoolsoft.easyreport.meta.domain.options.ReportOptions;
import com.easytoolsoft.easyreport.meta.form.control.HtmlCheckBox;
import com.easytoolsoft.easyreport.meta.form.control.HtmlCheckBoxList;
import com.easytoolsoft.easyreport.meta.form.control.HtmlComboBox;
import com.easytoolsoft.easyreport.meta.form.control.HtmlDateBox;
import com.easytoolsoft.easyreport.meta.form.control.HtmlFormElement;
import com.easytoolsoft.easyreport.meta.form.control.HtmlSelectOption;
import com.easytoolsoft.easyreport.meta.form.control.HtmlTextBox;
import com.easytoolsoft.easyreport.meta.service.ReportService;
import com.easytoolsoft.easyreport.meta.service.TableReportService;

/**
 * 报表生成服务类
 *
 * @author Tom Deng
 * @date 2017-03-25
 */
@Service("TableReportService")
public class TableReportServiceImpl implements TableReportService {
	@Resource
	private ReportService reportService;

	@Override
	public ReportParameter getReportParameter(final Report report, final Map<?, ?> parameters) {
		final Map<String, Object> formParams = this.getFormParameters(parameters);
		return this.createReportParameter(report, formParams);
	}

	@Override
	public ReportTable getReportTable(final int id, final Map<String, Object> formParams) {
		final Report report = this.reportService.getById(id);
		return this.getReportTable(report, formParams);
	}

	@Override
	public ReportTable getReportTable(final String uid, final Map<String, Object> formParams) {
		final Report report = this.reportService.getByUid(uid);
		return this.getReportTable(report, formParams);
	}

	@Override
	public ReportTable getReportTable(final Report report, final Map<String, Object> formParams) {
		final ReportDataSource reportDs = this.reportService.getReportDataSource(report.getDsId());
		return ReportGenerator.generate(reportDs, this.createReportParameter(report, formParams));
	}

	@Override
	public ReportTable getReportTable(final Queryer queryer, final ReportParameter reportParameter) {
		return ReportGenerator.generate(queryer, reportParameter);
	}

	@Override
	public ReportTable getReportTable(final ReportMetaDataSet metaDataSet, final ReportParameter reportParameter) {
		return ReportGenerator.generate(metaDataSet, reportParameter);
	}

	@Override
	public ReportDataSet getReportDataSet(final Report report, final Map<String, Object> parameters) {
		final ReportDataSource reportDs = this.reportService.getReportDataSource(report.getDsId());
		return ReportGenerator.getDataSet(reportDs, this.createReportParameter(report, parameters));
	}

	private ReportParameter createReportParameter(final Report report, final Map<String, Object> formParams) {
		final String sqlText = new ReportSqlTemplate(report.getSqlText(), formParams).execute();
		final Set<String> enabledStatColumn = this.getEnabledStatColumns(formParams);
		final ReportOptions options = this.reportService.parseOptions(report.getOptions());
		final List<ReportMetaDataColumn> metaColumns = this.reportService.parseMetaColumns(report.getMetaColumns());
		return new ReportParameter(report.getId().toString(), report.getName(), options.getLayout(), options.getStatColumnLayout(), metaColumns, enabledStatColumn, Boolean.valueOf(formParams.get("isRowSpan").toString()), sqlText);
	}

	private Set<String> getEnabledStatColumns(final Map<String, Object> formParams) {
		final Set<String> checkedSet = new HashSet<>();
		final String checkedColumnNames = formParams.get("statColumns").toString();
		if (StringUtils.isBlank(checkedColumnNames)) {
			return checkedSet;
		}
		final String[] columnNames = StringUtils.split(checkedColumnNames, ',');
		for (final String columnName : columnNames) {
			if (!checkedSet.contains(columnName)) {
				checkedSet.add(columnName);
			}
		}
		return checkedSet;
	}

	@Override
	public Map<String, Object> getBuildInParameters(final Map<?, ?> httpReqParamMap) {
		final Map<String, Object> formParams = new HashMap<>();
		this.setBuildInParams(formParams, httpReqParamMap);
		return formParams;
	}

	@Override
	public Map<String, Object> getFormParameters(final Map<?, ?> httpReqParamMap) {
		final Map<String, Object> formParams = new HashMap<>();
		this.setBuildInParams(formParams, httpReqParamMap);
		this.setQueryParams(formParams, httpReqParamMap);
		return formParams;
	}

	private void setBuildInParams(final Map<String, Object> formParams, final Map<?, ?> httpReqParamMap) {
		// 判断是否设置报表统计列
		if (httpReqParamMap.containsKey("statColumns")) {
			final String[] values = (String[]) httpReqParamMap.get("statColumns");
			formParams.put("statColumns", StringUtils.join(values, ','));
		} else {
			formParams.put("statColumns", "");
		}
		// 判断是否设置报表表格行跨行显示
		if (httpReqParamMap.containsKey("isRowSpan")) {
			final String[] values = (String[]) httpReqParamMap.get("isRowSpan");
			formParams.put("isRowSpan", values[0]);
		} else {
			formParams.put("isRowSpan", "true");
		}
	}

	private void setQueryParams(final Map<String, Object> formParams, final Map<?, ?> httpReqParamMap) {
		String[] values = (String[]) httpReqParamMap.get("uid");
		if (ArrayUtils.isEmpty(values) || "".equals(values[0].trim())) {
			return;
		}

		final String uid = values[0].trim();
		final Report report = this.reportService.getByUid(uid);
		final List<QueryParameterOptions> queryParams = this.reportService.parseQueryParams(report.getQueryParams());
		for (final QueryParameterOptions queryParam : queryParams) {
			String value = "";
			String dataType = queryParam.getDataType();
			values = (String[]) httpReqParamMap.get(queryParam.getName());
			if (values == null) {
				// 自定补全bool型false值（前端html不会回传）
				if ("bool".equals(dataType) && queryParam.isRequired()) {
					value = Boolean.FALSE.toString();
				}
			} else if (values.length > 0) {
				value = this.getQueryParamValue(dataType, values);
			}
			formParams.put(queryParam.getName(), value);
		}
	}

	// 支持in（列表，和字符串转义）
	private String getQueryParamValue(final String dataType, final String[] values) {
		if (values.length == 1) {
			if ("integer".equals(dataType) || "float".equals(dataType) || "bool".equals(dataType)) {
				return values[0];
			}
			return StrUtils.toSqlStrValue(values[0]);
		}
		// 多个参数值
		if ("integer".equals(dataType) || "float".equals(dataType) || "bool".equals(dataType)) {
			return StringUtils.join(values, ", ");
		}
		// 处理\, \n , \r, '转义问题
		List<String> valuesTmp = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			valuesTmp.add(StrUtils.toSqlStrValue(values[i]));
		}
		return StringUtils.join(valuesTmp, "', '");
	}

	@Override
	public Map<String, HtmlFormElement> getFormElementMap(final String uid, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getByUid(uid);
		return this.getFormElementMap(report, buildinParams, minDisplayedStatColumn);
	}

	@Override
	public Map<String, HtmlFormElement> getFormElementMap(final int id, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getById(id);
		return this.getFormElementMap(report, buildinParams, minDisplayedStatColumn);
	}

	@Override
	public Map<String, HtmlFormElement> getFormElementMap(final Report report, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final List<HtmlFormElement> formElements = this.getFormElements(report, buildinParams, minDisplayedStatColumn);
		final Map<String, HtmlFormElement> formElementMap = new HashMap<>(formElements.size());
		for (final HtmlFormElement element : formElements) {
			formElementMap.put(element.getName(), element);
		}
		return formElementMap;
	}

	@Override
	public List<HtmlFormElement> getFormElements(final String uid, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getByUid(uid);
		return this.getFormElements(report, buildinParams, minDisplayedStatColumn);
	}

	@Override
	public List<HtmlFormElement> getFormElements(final int id, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getById(id);
		return this.getFormElements(report, buildinParams, minDisplayedStatColumn);
	}

	//
	@Override
	public List<HtmlFormElement> getFormElements(final Report report, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final List<HtmlFormElement> formElements = new ArrayList<>(15);
		formElements.addAll(this.getDateFormElements(report, buildinParams));
		formElements.addAll(this.getQueryParamFormElements(report, buildinParams));
		formElements.add(this.getStatColumnFormElements(this.reportService.parseMetaColumns(report.getMetaColumns()), minDisplayedStatColumn));
		return formElements;
	}

	//
	@Override
	public List<HtmlFormElement> getDateAndQueryParamFormElements(final Report report, final Map<String, Object> buildinParams) {
		final List<HtmlFormElement> formElements = new ArrayList<>(15);
		formElements.addAll(this.getDateFormElements(report, buildinParams));
		formElements.addAll(this.getQueryParamFormElements(report, buildinParams));
		return formElements;
	}

	@Override
	public List<HtmlDateBox> getDateFormElements(final String uid, final Map<String, Object> buildinParams) {
		final Report report = this.reportService.getByUid(uid);
		return this.getDateFormElements(report, buildinParams);
	}

	@Override
	public List<HtmlDateBox> getDateFormElements(final int id, final Map<String, Object> buildinParams) {
		final Report report = this.reportService.getById(id);
		return this.getDateFormElements(report, buildinParams);
	}

	@Override
	public List<HtmlDateBox> getDateFormElements(final Report report, final Map<String, Object> buildinParams) {
		return new ArrayList<>(0);
	}

	@Override
	public List<HtmlFormElement> getQueryParamFormElements(final String uid, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getByUid(uid);
		return this.getFormElements(report, buildinParams, minDisplayedStatColumn);
	}

	@Override
	public List<HtmlFormElement> getQueryParamFormElements(final int id, final Map<String, Object> buildinParams, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getById(id);
		return this.getFormElements(report, buildinParams, minDisplayedStatColumn);
	}

	/**
	 * 使用【隐藏+非必须】参数 和 内建参数值 作为下拉列表参数控件的sql所需的变量值源
	 * 
	 * @author koqiui
	 * @date 2019年11月11日 下午5:34:49
	 * 
	 * @param report
	 * @param buildinParams
	 * @return
	 */
	private Map<String, Object> getMergedParamValueMap(final Report report, final Map<String, Object> buildinParams) {
		Map<String, Object> mergedParamMap = new HashMap<>(buildinParams);
		List<QueryParameterOptions> metaParams = JSON.parseArray(report.getQueryParams(), QueryParameterOptions.class);
		for (QueryParameterOptions metaParam : metaParams) {
			// 【隐藏+非必须】
			if (BooleanUtils.isTrue(metaParam.isHidden() && BooleanUtils.isFalse(metaParam.isRequired()))) {
				String strValue = metaParam.hasDefaultValue() ? metaParam.getDefaultValue() : null;
				if (strValue == null) {
					continue;
				}
				String theType = metaParam.getDataType();
				if (theType == null) {
					theType = "string";
				}
				boolean isMulSelect = "selectMul".equals(metaParam.getFormElement());
				if (isMulSelect) {// 多选可能是列表
					String[] values = strValue.split(",", -1);
					boolean isInvalidVal = false;
					String value = null;
					Object objValue = null;
					for (int i = 0; i < values.length; i++) {
						value = values[i].trim();// trim,增强容错性
						objValue = value;
						if (!"string".equals(theType)) {// integer, float, date, bool
							if (StringUtils.isBlank(value)) {
								objValue = null;
							} else if ("integer".equals(theType)) {
								objValue = NumUtils.parseLong(value);
							} else if ("float".equals(theType)) {
								objValue = NumUtils.parseDouble(value);
							} else if ("bool".equals(theType)) {
								objValue = Boolean.valueOf(value);
							}
						}
						if (objValue == null) {
							isInvalidVal = true;
							break;
						}
					}
					mergedParamMap.put(metaParam.getName(), isInvalidVal ? null : strValue);
				} else {
					Object objValue = strValue;
					if (!"string".equals(theType)) {// integer, float, date, bool
						if (StringUtils.isBlank(strValue)) {
							objValue = null;
						} else if ("integer".equals(theType)) {
							objValue = NumUtils.parseLong(strValue);
						} else if ("float".equals(theType)) {
							objValue = NumUtils.parseDouble(strValue);
						} else if ("bool".equals(theType)) {
							objValue = Boolean.valueOf(strValue);
						}
					}
					mergedParamMap.put(metaParam.getName(), objValue);
				}
			}
		}
		return mergedParamMap;
	}

	@Override
	public List<HtmlFormElement> getQueryParamFormElements(final Report report, final Map<String, Object> buildinParams) {
		final List<QueryParameterOptions> queryParams = this.reportService.parseQueryParams(report.getQueryParams());
		final List<HtmlFormElement> formElements = new ArrayList<>(3);
		for (final QueryParameterOptions queryParam : queryParams) {
			HtmlFormElement htmlFormElement = null;
			queryParam.setDefaultText(VelocityUtils.parse(queryParam.getDefaultText(), buildinParams));
			queryParam.setDefaultValue(VelocityUtils.parse(queryParam.getDefaultValue(), buildinParams));
			queryParam.setContent(VelocityUtils.parse(queryParam.getContent(), buildinParams));
			final String formElement = queryParam.getFormElement();
			if ("select".equals(formElement) || "selectMul".equals(formElement)) {
				Map<String, Object> mergedParamMap = this.getMergedParamValueMap(report, buildinParams);
				//
				htmlFormElement = this.getComboBoxFormElements(queryParam, report.getDsId(), mergedParamMap);
			} else if ("checkbox".equals(formElement)) {
				htmlFormElement = new HtmlCheckBox(queryParam.getName(), queryParam.getText(), queryParam.getRealDefaultValue());
			} else if ("text".equals(formElement)) {
				htmlFormElement = new HtmlTextBox(queryParam.getName(), queryParam.getText(), queryParam.getRealDefaultValue());
			} else if ("date".equals(formElement)) {
				htmlFormElement = new HtmlDateBox(queryParam.getName(), queryParam.getText(), queryParam.getRealDefaultValue());
			}
			if (htmlFormElement != null) {
				this.setElementCommonProperities(queryParam, htmlFormElement);
				formElements.add(htmlFormElement);
			}
		}
		return formElements;
	}

	// 加强对多选列表的支持
	private HtmlComboBox getComboBoxFormElements(final QueryParameterOptions queryParam, final int dsId, final Map<String, Object> mergedParamMap) {
		final List<ReportQueryParamItem> options = this.getOptions(queryParam, dsId, mergedParamMap);
		final List<HtmlSelectOption> htmlSelectOptions = new ArrayList<>(options.size());
		String defaultValue = queryParam.hasDefaultValue() ? queryParam.getDefaultValue() : null;
		List<String> defaultValues = null;
		boolean isMulSelect = "selectMul".equals(queryParam.getFormElement());
		boolean isRequired = queryParam.isRequired();
		boolean isHidden = queryParam.isHidden();
		boolean hasSelected = false;
		if (isMulSelect) {
			if (defaultValue == null) {
				defaultValues = new ArrayList<>(0);
			} else {
				String[] tmpValues = defaultValue.split(",", -1);
				defaultValues = new ArrayList<>(tmpValues.length);
				for (int i = 0; i < tmpValues.length; i++) {
					defaultValues.add(tmpValues[i].trim());// trim 增强容错性
				}
			}
			//
			for (int i = 0; i < options.size(); i++) {
				final ReportQueryParamItem option = options.get(i);
				String optionName = option.getName();
				boolean selected = !defaultValues.isEmpty() && defaultValues.contains(optionName);
				hasSelected = hasSelected || selected;
				htmlSelectOptions.add(new HtmlSelectOption(option.getText(), optionName, selected));
			}
		} else {
			for (int i = 0; i < options.size(); i++) {
				final ReportQueryParamItem option = options.get(i);
				String optionName = option.getName();
				boolean selected = false;
				if (defaultValue != null && !hasSelected) {// 单选
					selected = defaultValue.equals(optionName);
					hasSelected = hasSelected || selected;
				}
				htmlSelectOptions.add(new HtmlSelectOption(option.getText(), optionName, selected));
			}
		}
		if (isRequired && !hasSelected && htmlSelectOptions.size() > 0) {
			// 必须的参数默认选中第一项
			htmlSelectOptions.get(0).setSelected(true);
		}
		final HtmlComboBox htmlComboBox = new HtmlComboBox(queryParam.getName(), queryParam.getText(), htmlSelectOptions);
		htmlComboBox.setMultipled(isMulSelect);
		htmlComboBox.setAutoComplete(queryParam.isAutoComplete());
		if (isHidden && !isRequired) {// 提示隐藏参数
			if (isMulSelect) {
				htmlComboBox.setHintText(defaultValues == null ? "" : defaultValues.toString());
			} else {
				htmlComboBox.setHintText(defaultValue == null ? "" : defaultValue);
			}
		}
		return htmlComboBox;
	}

	private void setElementCommonProperities(final QueryParameterOptions queryParam, final HtmlFormElement htmlFormElement) {
		htmlFormElement.setDataType(queryParam.getDataType());
		htmlFormElement.setHeight(queryParam.getHeight());
		htmlFormElement.setWidth(queryParam.getWidth());
		htmlFormElement.setRequired(queryParam.isRequired());
		htmlFormElement.setHidden(queryParam.isHidden());
		htmlFormElement.setDefaultText(queryParam.getRealDefaultText());
		htmlFormElement.setDefaultValue(queryParam.getRealDefaultValue());
		htmlFormElement.setComment(queryParam.getComment());
	}

	// modified by koqiui 2019-11-11 （支持使用【隐藏+非必须】变量 作为下拉列表的sql变量值源）
	private List<ReportQueryParamItem> getOptions(final QueryParameterOptions queryParam, final int dsId, final Map<String, Object> mergedParamMap) {
		if ("sql".equals(queryParam.getDataSource())) {
			String sqlText = queryParam.getContent();
			// 缺少的sql变量名称
			List<String> varNames = StrUtils.extractVarNames(sqlText);
			boolean hasVars = varNames != null && !varNames.isEmpty();
			if (hasVars) {
				List<String> lckValueVarNames = new ArrayList<>();
				for (String varName : varNames) {
					Object objValue = mergedParamMap.get(varName);
					if (objValue == null) {
						lckValueVarNames.add(varName);
					}
				}
				if (lckValueVarNames.size() > 0) {
					throw new RuntimeException("查询/统计条件【" + queryParam.getText() + "】的sql缺少如下变量值有效值：\r\n" + lckValueVarNames.toString());
				}
				// 解析/替换sql变量
				sqlText = VelocityUtils.parse(sqlText, mergedParamMap);
			}
			return this.reportService.executeQueryParamSqlText(dsId, sqlText);
		}
		// name,text|name,text|...
		final List<ReportQueryParamItem> options = new ArrayList<>();
		if ("text".equals(queryParam.getDataSource()) && StringUtils.isNoneBlank(queryParam.getContent())) {
			final HashSet<String> set = new HashSet<>();
			final String[] optionSplits = StringUtils.split(queryParam.getContent(), '|');
			for (final String option : optionSplits) {
				final String[] nameValuePairs = StringUtils.split(option, ',');
				final String name = nameValuePairs[0];
				final String text = nameValuePairs.length > 1 ? nameValuePairs[1] : name;
				if (!set.contains(name)) {
					set.add(name);
					options.add(new ReportQueryParamItem(name, text));
				}
			}
		}
		return options;
	}

	@Override
	public HtmlCheckBoxList getStatColumnFormElements(final String uid, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getByUid(uid);
		return this.getStatColumnFormElements(this.reportService.parseMetaColumns(report.getMetaColumns()), minDisplayedStatColumn);
	}

	@Override
	public HtmlCheckBoxList getStatColumnFormElements(final int id, final int minDisplayedStatColumn) {
		final Report report = this.reportService.getById(id);
		return this.getStatColumnFormElements(this.reportService.parseMetaColumns(report.getMetaColumns()), minDisplayedStatColumn);
	}

	@Override
	public HtmlCheckBoxList getStatColumnFormElements(final List<ReportMetaDataColumn> columns, final int minDisplayedStatColumn) {
		final List<ReportMetaDataColumn> statColumns = columns.stream().filter(column -> column.getType() == ColumnType.STATISTICAL || column.getType() == ColumnType.COMPUTED).collect(Collectors.toList());
		if (statColumns.size() <= minDisplayedStatColumn) {
			return null;
		}

		final List<HtmlCheckBox> checkBoxes = new ArrayList<>(statColumns.size());
		for (final ReportMetaDataColumn column : statColumns) {
			final HtmlCheckBox checkbox = new HtmlCheckBox(column.getName(), column.getText(), column.getName());
			checkbox.setChecked(!column.isOptional());
			checkBoxes.add(checkbox);
		}
		return new HtmlCheckBoxList("statColumns", "统计列", checkBoxes);
	}

	@Override
	public List<HtmlFormElement> getNonStatColumnFormElements(final List<ReportMetaDataColumn> columns) {
		final List<HtmlFormElement> formElements = new ArrayList<>(10);
		columns.stream().filter(column -> column.getType() == ColumnType.LAYOUT || column.getType() == ColumnType.DIMENSION).forEach(column -> {
			final HtmlComboBox htmlComboBox = new HtmlComboBox("dim_" + column.getName(), column.getText(), new ArrayList<>(0));
			htmlComboBox.setAutoComplete(true);
			formElements.add(htmlComboBox);
		});
		return formElements;
	}
}