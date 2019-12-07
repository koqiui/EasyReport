package com.easytoolsoft.easyreport.web.util;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.easytoolsoft.easyreport.common.util.StrUtils;
import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataColumn;
import com.easytoolsoft.easyreport.engine.data.ReportMetaDataSet;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;
import com.easytoolsoft.easyreport.engine.data.ReportTable;
import com.easytoolsoft.easyreport.engine.query.Queryer;
import com.easytoolsoft.easyreport.engine.query.QueryerFactory;
import com.easytoolsoft.easyreport.engine.util.DateUtils;
import com.easytoolsoft.easyreport.meta.domain.Report;
import com.easytoolsoft.easyreport.meta.domain.options.QueryParameterOptions;
import com.easytoolsoft.easyreport.meta.form.QueryParamFormView;
import com.easytoolsoft.easyreport.meta.form.control.HtmlFormElement;
import com.easytoolsoft.easyreport.meta.service.ReportService;
import com.easytoolsoft.easyreport.meta.service.TableReportService;

/**
 * @author Tom Deng
 * @date 2017-03-25
 */
@Component
public class ReportUtils {
	private static ReportService reportService;
	private static TableReportService tableReportService;

	@Autowired
	public ReportUtils(final ReportService reportService, final TableReportService tableReportService) {
		ReportUtils.reportService = reportService;
		ReportUtils.tableReportService = tableReportService;
	}

	public static List<String> parseSqlVarNames(String sqlText) {
		return StrUtils.extractVarNames(sqlText);
	}

	// 是否本地funcLink测试模式
	private static final boolean LOCAL_FUNC_LINK_DEV_MODE = false;

	/**
	 * 验证sql语句中的变量是否都提供了值
	 * 
	 * @author koqiui
	 * @date 2019年11月11日 下午3:46:33
	 * 
	 * @param sqlText
	 * @param qryParams
	 * @param qryValueMap
	 * @return 缺值的变量名称
	 */
	public static List<String> validateSqlParamValues(String sqlText, List<QueryParameterOptions> qryParams, Map<String, Object> qryValueMap) {
		List<String> lckParamNames = new ArrayList<>();
		//
		List<String> sqlVarNames = parseSqlVarNames(sqlText);
		boolean hasSqlVars = sqlVarNames != null && !sqlVarNames.isEmpty();
		if (hasSqlVars) {
			// name => object
			Map<String, QueryParameterOptions> qryParamMap = new HashMap<>();
			if (qryParams != null) {
				for (QueryParameterOptions qryParam : qryParams) {
					qryParamMap.put(qryParam.getName(), qryParam);
				}
			}
			//
			QueryParameterOptions qryParm = null;
			String dataType = null; // (string|float|integer|date|bool)
			String varText = null;
			String strVal = null;
			boolean isMulSelect = false;
			for (String sqlVarName : sqlVarNames) {
				qryParm = qryParamMap.get(sqlVarName);
				dataType = qryParm == null ? "string" : qryParm.getDataType();
				varText = qryParm == null || StringUtils.isBlank(qryParm.getText()) ? sqlVarName : qryParm.getText();
				Object objValue = qryValueMap.get(sqlVarName);
				if (objValue == null) {
					lckParamNames.add(varText);
					continue;
				}
				strVal = String.valueOf(objValue);
				if (!"string".equals(dataType) && StringUtils.isBlank(strVal)) {
					lckParamNames.add(varText);
					continue;
				}
				//
				isMulSelect = "selectMul".equals(qryParm.getFormElement());
				if (isMulSelect) {
					String[] values = strVal.split(",", -1);
					String value = null;
					for (int i = 0; i < values.length; i++) {
						value = values[i].trim();// trim 增强容错性
						if ("integer".equals(dataType)) {
							try {
								Long.valueOf(value);
							} catch (NumberFormatException nfe) {
								lckParamNames.add(varText);
								break;
							}
						} else if ("float".equals(dataType)) {
							try {
								Double.valueOf(value);
							} catch (NumberFormatException nfe) {
								lckParamNames.add(varText);
								break;
							}
						}
					}
				} else {
					if ("integer".equals(dataType)) {
						try {
							Long.valueOf(strVal);
						} catch (NumberFormatException nfe) {
							lckParamNames.add(varText);
						}
					} else if ("float".equals(dataType)) {
						try {
							Double.valueOf(strVal);
						} catch (NumberFormatException nfe) {
							lckParamNames.add(varText);
						}
					}
				}
			}
		}
		//
		return lckParamNames;
	}

	public static Report getReportMetaDataByUid(final String uid) {
		return reportService.getByUid(uid);
	}

	public static Report getReportMetaDataByUcode(final String ucode) {
		return reportService.getByUcode(ucode);
	}

	public static JSONObject getDefaultChartData() {
		return new JSONObject(6) {
			private static final long serialVersionUID = 1L;
			{
				put("dimColumnMap", null);
				put("dimColumns", null);
				put("statColumns", null);
				put("dataRows", null);
				put("msg", "");
			}
		};
	}

	public static ReportDataSource getReportDataSource(final Report report) {
		return reportService.getReportDataSource(report.getDsId());
	}

	public static ReportParameter getReportParameter(final Report report, final Map<?, ?> parameters) {
		return tableReportService.getReportParameter(report, parameters);
	}

	/**
	 * 返回报表（原生）结果数据集
	 * 
	 * @author koqiui
	 * @date 2019年11月10日 下午10:36:12
	 * 
	 * @param uid
	 * @param parameters
	 * @return
	 */
	public static List<Map<String, Object>> getReportResultSetRows(final String uid, final Map<?, ?> parameters) {
		Report report = reportService.getByUid(uid);
		ReportDataSource dataSource = reportService.getReportDataSource(report.getDsId());
		ReportParameter parameter = tableReportService.getReportParameter(report, parameters);
		Queryer queryer = QueryerFactory.create(dataSource, parameter);
		return queryer.getResultSetRows();
	}

	/**
	 * 返回指定报表基于sql的参数选项列表
	 * 
	 * @author koqiui
	 * @date 2019年11月10日 下午10:38:10
	 * 
	 * @param uid
	 * @param sqlText
	 * @return
	 */
	public static List<Map<String, Object>> getReportSqlBasedParamOptionList(final String uid, String sqlText) {
		if (sqlText == null) {
			return new ArrayList<>(0);
		}
		String tmpSql = sqlText.toLowerCase().replace('\n', ' ').replace('\r', ' ');
		// sql语句必须同时包含select from where
		if (tmpSql.indexOf("select ") == -1 || tmpSql.indexOf("from ") == -1 || tmpSql.indexOf("where ") == -1) {
			return new ArrayList<>(0);
		}
		Report report = reportService.getByUid(uid);
		ReportDataSource dataSource = reportService.getReportDataSource(report.getDsId());
		Queryer queryer = QueryerFactory.create(dataSource, null);
		return queryer.getResultSetRows(sqlText);
	}

	public static void renderByFormMap(final String uid, final ModelAndView modelAndView, final HttpServletRequest request) {
		final Report report = reportService.getByUid(uid);
		final Map<String, Object> buildInParams = tableReportService.getBuildInParameters(request.getParameterMap());
		final Map<String, HtmlFormElement> formMap = tableReportService.getFormElementMap(report, buildInParams, 1);
		modelAndView.addObject("formMap", formMap);
		modelAndView.addObject("uid", uid);
		modelAndView.addObject("id", report.getId());
		modelAndView.addObject("name", report.getName());
	}

	public static void renderByTemplate(final String uid, final ModelAndView modelAndView, final QueryParamFormView formView, final HttpServletRequest request) {
		final Report report = reportService.getByUid(uid);
		final List<ReportMetaDataColumn> metaDataColumns = reportService.parseMetaColumns(report.getMetaColumns());
		final Map<String, Object> buildInParams = tableReportService.getBuildInParameters(request.getParameterMap());
		final List<HtmlFormElement> dateAndQueryElements = tableReportService.getDateAndQueryParamFormElements(report, buildInParams);
		final HtmlFormElement statColumnFormElements = tableReportService.getStatColumnFormElements(metaDataColumns, 0);
		final List<HtmlFormElement> nonStatColumnFormElements = tableReportService.getNonStatColumnFormElements(metaDataColumns);
		modelAndView.addObject("uid", uid);
		modelAndView.addObject("id", report.getId());
		modelAndView.addObject("name", report.getName());
		modelAndView.addObject("comment", report.getComment().trim());
		modelAndView.addObject("formHtmlText", formView.getFormHtmlText(dateAndQueryElements));
		modelAndView.addObject("statColumHtmlText", formView.getFormHtmlText(statColumnFormElements));
		modelAndView.addObject("nonStatColumHtmlText", formView.getFormHtmlText(nonStatColumnFormElements));
	}

	public static void generate(final String uid, final JSONObject data, final HttpServletRequest request) {
		generate(uid, data, request.getParameterMap());
	}

	public static void generate(final String uid, final JSONObject data, final Map<?, ?> parameters) {
		generate(uid, data, new HashMap<>(0), parameters);
	}

	public static void generate(final String uid, final JSONObject data, final Map<String, Object> attachParams, final Map<?, ?> parameters) {
		if (StringUtils.isBlank(uid)) {
			data.put("htmlTable", "uid参数为空导致数据不能加载！");
			return;
		}
		final ReportTable reportTable = generate(uid, attachParams, parameters);
		data.put("htmlTable", reportTable.getHtmlText());
		data.put("metaDataRowCount", reportTable.getMetaDataRowCount());
		data.put("metaDataColumnCount", reportTable.getMetaDataColumnCount());
	}

	public static void generate(final Queryer queryer, final ReportParameter reportParameter, final JSONObject data) {
		final ReportTable reportTable = tableReportService.getReportTable(queryer, reportParameter);
		data.put("htmlTable", reportTable.getHtmlText());
		data.put("metaDataRowCount", reportTable.getMetaDataRowCount());
	}

	public static void generate(final ReportMetaDataSet metaDataSet, final ReportParameter reportParameter, final JSONObject data) {
		final ReportTable reportTable = tableReportService.getReportTable(metaDataSet, reportParameter);
		data.put("htmlTable", reportTable.getHtmlText());
		data.put("metaDataRowCount", reportTable.getMetaDataRowCount());
	}

	public static ReportTable generate(final String uid, final Map<?, ?> parameters) {
		return generate(uid, new HashMap<>(0), parameters);
	}

	public static ReportTable generate(final String uid, final Map<String, Object> attachParams, final Map<?, ?> parameters) {
		final Report report = reportService.getByUid(uid);
		final Map<String, Object> formParams = tableReportService.getFormParameters(parameters);
		if (MapUtils.isNotEmpty(attachParams)) {
			for (final Entry<String, Object> es : attachParams.entrySet()) {
				formParams.put(es.getKey(), es.getValue());
			}
		}
		//
		if (LOCAL_FUNC_LINK_DEV_MODE) {
			// 不能影响正常集成调用（所以要加判断）
			if (!formParams.containsKey("is_restMode")) {
				formParams.put("is_restMode", true);
				//
				formParams.put("show_dataLinks", true);
			}
		}
		//
		return tableReportService.getReportTable(report, formParams);
	}

	public static void exportToExcel(final String uid, final String name, String htmlFilter, String htmlTable, final HttpServletRequest request, final HttpServletResponse response) {
		try (OutputStream out = response.getOutputStream()) {
			String fileName = name + "_" + DateUtils.getNow("yyyyMMddHHmmss") + ".xls";
			fileName = URLEncoder.encode(fileName, "UTF-8");
			//
			if (htmlFilter == null) {
				htmlFilter = "";
			}
			//
			if (StringUtils.isBlank(htmlTable) || htmlTable.indexOf("<table") == -1) {// 从后端生成
				final Report report = reportService.getByUid(uid);
				final Map<String, Object> formParameters = tableReportService.getFormParameters(request.getParameterMap());
				final ReportTable reportTable = tableReportService.getReportTable(report, formParameters);
				htmlTable = reportTable.getHtmlText();
				//
				htmlTable = htmlTable.replaceAll("<table\\s+", "<table cellpadding=\"3\" cellspacing=\"0\"  border=\"1\" rull=\"all\" style=\"border-collapse:collapse\" ");
			}
			response.reset();
			response.setHeader("Content-Disposition", String.format("attachment; filename=%s", fileName));
			response.setContentType("application/vnd.ms-excel; charset=utf-8");
			// response.addCookie(new Cookie("fileDownload", "true"));
			response.setHeader("Set-Cookie", "fileDownload=true; path=/");
			// out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // 生成带bom的utf8文件
			String htmlText = htmlFilter + htmlTable;
			out.write(htmlText.getBytes());
			out.flush();
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
