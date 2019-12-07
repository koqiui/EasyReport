package com.swb.web.base;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.swb.bean.mercht.entity.ReportMeta;
import com.swb.common.base.AppNodeInfo;
import com.swb.common.cache.Cache;
import com.swb.common.cache.CacheUtil;
import com.swb.common.config.PropertyConfigurer;
import com.swb.common.http.Ajax;
import com.swb.common.model.Result;
import com.swb.common.model.Result.Type;
import com.swb.common.util.BoolUtil;
import com.swb.common.util.DateUtil;
import com.swb.common.util.JsonUtil;
import com.swb.common.util.NumUtil;
import com.swb.common.util.StrUtil;
import com.swb.common.util.TypeUtil;
import com.swb.service.BizCache;

/**
 * EasyReport api 代理类
 * 
 * @author koqiui
 * @date 2019年11月9日 下午10:44:53
 *
 */
public class ReportProxy {
	private static final Log logger = LogFactory.getLog(ReportProxy.class);
	//
	public static final String DEF_SERVER_CONF_FILE_NAME = "report-server.properties";
	//
	private static final String CONFIG_PREFIX = "report.server.";
	//
	public static final String KEY_BASE_URL = CONFIG_PREFIX + "base.url";

	//
	private static String serverBaseUrl;
	private static BizCache bizCache;
	private static boolean useCache = false;

	public static void init() {
		init(DEF_SERVER_CONF_FILE_NAME);
	}

	public static void init(String confFileName) {
		bizCache = AppBase.bizCache;
		// 只有生产环境使用报表信息缓存
		useCache = AppNodeInfo.getCurrent().isRunningOnProductServer();
		//
		try {
			PropertyConfigurer configurer = PropertyConfigurer.newInstance(confFileName, "UTF-8");
			String tmpStr = configurer.get(KEY_BASE_URL);
			if (StrUtil.hasText(tmpStr)) {
				serverBaseUrl = tmpStr.trim();
				logger.info("报表服务器baseUrl " + serverBaseUrl);
			} else {
				logger.error("报表服务器baseUrl 加载失败");
			}
		} catch (Exception ex) {
			logger.error("报表服务器 配置信息加载失败");
			logger.error(ex);
		}
	}

	//
	// today
	public static Date getToday() {
		return new Date();
	}

	// month.firstday
	public static Date getMonthFirstDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	// month.lastday
	public static Date getMonthLastDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		return cal.getTime();
	}

	// year.firstday
	public static Date getYearFirstDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 0);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	}

	// year.lastday
	public static Date getYearLastDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		cal.set(Calendar.DAY_OF_YEAR, 0);
		return cal.getTime();
	}

	private static Pattern DATE_EXPR_REGEX = Pattern.compile("(today|month\\.firstday|month\\.lastday|year\\.firstday|year\\.lastday)");

	public static Date evalDateExpr(String dateExpr) {
		if (StrUtil.isNullOrBlank(dateExpr)) {
			return null;
		}
		dateExpr = dateExpr.replaceAll("\\s+", "");
		dateExpr = dateExpr.trim().toLowerCase();
		// System.out.println("-------------------");
		// System.out.println("'" + dateExpr + "'");
		Matcher matcher = DATE_EXPR_REGEX.matcher(dateExpr);
		if (matcher.find()) {
			String calcExpr = null;
			String coreExpr = matcher.group();
			int end = matcher.end();
			if (dateExpr.length() > end) {
				calcExpr = dateExpr.substring(end).trim();
			}
			// System.out.println("'" + coreExpr + "'");
			// System.out.println("'" + (calcExpr == null ? "" : calcExpr) + "'");
			Date baseDate = null;
			if ("month.firstday".equals(coreExpr)) {
				baseDate = getMonthFirstDay();
			} else if ("month.lastday".equals(coreExpr)) {
				baseDate = getMonthLastDay();
			} else if ("year.firstday".equals(coreExpr)) {
				baseDate = getYearFirstDay();
			} else if ("year.lastday".equals(coreExpr)) {
				baseDate = getYearLastDay();
			} else {// today
				baseDate = getToday();
			}
			Date retDate = baseDate;
			if (StrUtil.hasText(calcExpr)) {
				int diffDays = 0;
				try {
					diffDays = Integer.parseInt(calcExpr);
				} catch (NumberFormatException ex) {
					// 无效数字
				}
				if (diffDays != 0) {
					retDate = DateUtils.addDays(baseDate, diffDays);
				}
			}
			return retDate;
		}
		return null;
	}

	public static String getDateStrByExpr(String dateExpr) {
		Date theDate = evalDateExpr(dateExpr);
		return theDate == null ? null : DateUtil.toStdDateStr(theDate);
	}

	// --------------------------------------------------------------------------------------------
	private static Ajax newAjax() {
		Ajax ajax = new Ajax();
		ajax.baseUrl(serverBaseUrl);
		return ajax;
	}

	/** 报表元数据缓存时间（分钟） */
	private static final int REPORT_INFO_CACHE_MINUTES = 10;

	private static void saveReportInfoCache(ReportMeta reportInfo) {
		if (!useCache) {
			return;
		}
		//
		Cache<String, Object> bizComnCache = bizCache.getBizComnCache();

		String cacheKey = CacheUtil.makeKey("report-meta-info", reportInfo.getCode());

		bizComnCache.put(cacheKey, reportInfo);
		bizComnCache.expire(cacheKey, REPORT_INFO_CACHE_MINUTES, TimeUnit.MINUTES);
	}

	private static ReportMeta loadReportInfoCache(String reportCode) {
		if (!useCache) {
			return null;
		}
		//
		Cache<String, Object> bizComnCache = bizCache.getBizComnCache();

		String cacheKey = CacheUtil.makeKey("report-meta-info", reportCode);

		return (ReportMeta) bizComnCache.get(cacheKey, true);
	}

	/**
	 * 根据自定义代码获取报表配置信息
	 * 
	 * @author koqiui
	 * @date 2019年11月9日 下午11:57:58
	 * 
	 * @param reportCode
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ReportMeta fetchReportInfoByCode(String reportCode) {
		ReportMeta targetInfo = loadReportInfoCache(reportCode);
		if (targetInfo == null) {
			Ajax ajax = newAjax();
			ajax.get("/report/by/ucode");
			ajax.param("ucode", reportCode);
			ajax.asForm();
			ajax.send();
			String jsonText = ajax.resultAsText();
			logger.debug(jsonText);
			Map<String, Object> mapResult = JsonUtil.fromJson(jsonText, TypeUtil.TypeRefs.StringObjectMapType);
			if (mapResult != null) {
				Integer errCode = (Integer) mapResult.get("code");
				if (errCode.intValue() == 0) {
					Map<String, Object> mapData = (Map<String, Object>) mapResult.get("data");
					if (mapData != null) {
						targetInfo = new ReportMeta();
						targetInfo.setCode(reportCode);
						targetInfo.setUuid((String) mapData.get("uid"));
						targetInfo.setName((String) mapData.get("name"));
						String metaColumnsJson = (String) mapData.get("metaColumns");
						List<Map<String, Object>> metaColumns = JsonUtil.fromJson(metaColumnsJson, TypeUtil.TypeRefs.StringObjectMapListType);
						targetInfo.setMetaColumns(metaColumns);
						List<Map<String, Object>> metaColList = new ArrayList<>(metaColumns.size());
						targetInfo.setMetaColList(metaColList);
						Integer colType = null;
						for (Map<String, Object> metaColumn : metaColumns) {
							Map<String, Object> metaCol = new LinkedHashMap<>();
							metaColList.add(metaCol);
							metaCol.put("name", metaColumn.get("name"));
							metaCol.put("text", metaColumn.get("text"));
							metaCol.put("sqlType", metaColumn.get("sqlType"));
							metaCol.put("dataType", metaColumn.get("theType"));
							metaCol.put("format", metaColumn.get("format"));
							metaCol.put("align", metaColumn.get("align"));
							metaCol.put("percent", metaColumn.get("percent"));
							metaCol.put("width", metaColumn.get("width"));
							colType = NumUtil.parseInteger(String.valueOf(metaColumn.get("type")));
							// 是否统计/计算列
							metaCol.put("statis", colType != null && (colType.intValue() == 3 || colType.intValue() == 4));
						}
						String queryParamsJson = (String) mapData.get("queryParams");
						targetInfo.setQueryParams(JsonUtil.fromJson(queryParamsJson, TypeUtil.TypeRefs.StringObjectMapListType));
						Integer status = (Integer) mapData.get("status");
						targetInfo.setDisabled(status.intValue() == 0);
						String comment = (String) mapData.get("comment");
						targetInfo.setDesc(StrUtil.left(comment, 120));
						//
						saveReportInfoCache(targetInfo);
					}
				} else {
					String errMsg = (String) mapResult.get("detailMsg");
					if (StrUtil.isNullOrBlank(errMsg)) {
						errMsg = (String) mapResult.getOrDefault("message", "未知原因");
					}
					//
					logger.warn("获取报表信息失败：" + errMsg);
				}
			} else {
				logger.error("获取报表信息失败：" + ajax.getLastErrorText());
			}
		}
		return targetInfo;
	}

	// (2019-01-01,2019-01|2019-02-01,2019-02)
	// value,text|value,text|...
	private static List<Map<String, Object>> splitAsOptionList(String optionListStr) {
		List<Map<String, Object>> retList = new ArrayList<>(0);
		if (StrUtil.isNullOrBlank(optionListStr)) {
			logger.warn("列表字符串不能为空");
			return retList;
		}
		//
		String[] itemPairs = optionListStr.split("\\|", -1);
		String[] valueText = null;
		Map<String, Object> mapItem = null;
		for (String itemPair : itemPairs) {
			valueText = itemPair.split(",", -1);
			valueText[0] = valueText[0].trim();
			if (StrUtil.EmptyStr.equals(valueText[0])) {
				continue;
			}
			mapItem = new HashMap<>();
			retList.add(mapItem);
			//
			mapItem.put("value", valueText[0]);
			if (valueText.length < 2) {
				mapItem.put("text", valueText[0]);
			} else {
				mapItem.put("text", valueText[1].trim());
			}
		}
		//
		return retList;
	}

	private static String toSqlStrValue(String srcStr) {
		if (srcStr == null) {
			return null;
		}
		String retStr = StrUtil.replaceAll(srcStr, "\\", "\\\\");
		retStr = StrUtil.replaceAll(retStr, "\n", "\\n");
		retStr = StrUtil.replaceAll(retStr, "\r", "\\r");
		retStr = StrUtil.replaceAll(retStr, "'", "\\'");
		return retStr;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> fetchSqlBasedParamOptionList(String reportUuid, String sqlTpl, Map<String, Object> sqlParamMap) {
		List<Map<String, Object>> retList = new ArrayList<>(0);
		if (sqlTpl == null) {
			logger.error("sql语句不能为空");
			return retList;
		}
		String tmpSql = sqlTpl.toLowerCase().replace('\n', ' ').replace('\r', ' ');
		// sql语句必须包含
		if (tmpSql.indexOf("select ") == -1 || tmpSql.indexOf("from ") == -1 || tmpSql.indexOf("where ") == -1) {
			logger.error("不是有效的sql语句");
			return retList;
		}
		String sqlText = sqlTpl;
		List<String> varNames = StrUtil.extractVarNames(sqlTpl);
		if (varNames.size() > 0) {
			if (sqlParamMap == null) {
				sqlParamMap = new HashMap<>(0);
			}
			Map<String, Object> theParamMap = new HashMap<>(sqlParamMap.size());
			for (Map.Entry<String, Object> theParam : sqlParamMap.entrySet()) {
				String name = theParam.getKey();
				Object value = theParam.getValue();
				if (value != null) {
					Class<?> valueClass = value.getClass();
					// 处理列表值
					if (valueClass.isArray() || List.class.isAssignableFrom(valueClass)) {
						List<?> valueItemList = TypeUtil.asList(value);
						if (valueItemList.isEmpty()) {
							value = "";
						} else {
							Object itemValue = valueItemList.get(0);
							if (itemValue == null) {// 已第一个参数的类型为判断依据
								logger.error("参数错误：" + name + " " + value.toString());
								continue;
							}
							// integer, float, date, bool, string
							if (itemValue instanceof String) {
								List<String> valuesTmp = new ArrayList<>(valueItemList.size());
								for (int i = 0; i < valueItemList.size(); i++) {
									valuesTmp.add(toSqlStrValue(String.valueOf(valueItemList.get(i))));
								}
								value = StrUtil.join(valuesTmp, "', '");
							} else {
								value = StrUtil.join(valueItemList, ", ");
							}
						}
					}
				}
				//
				theParamMap.put(name, value);
			}
			//
			List<String> lckVarNames = new ArrayList<>();
			for (int i = 0; i < varNames.size(); i++) {
				String varName = varNames.get(i);
				if (theParamMap.get(varName) == null) {
					lckVarNames.add(varName);
				}
			}
			if (lckVarNames.size() > 0) {
				logger.error("缺少以下有效参数值：" + StrUtil.join(lckVarNames, "、"));
				logger.debug("要执行的Sql语句：" + sqlTpl);
				logger.debug("接收到的的Sql参数：" + JsonUtil.toFormattedJson(sqlParamMap));
				return retList;
			}
			//
			sqlText = StrUtil.format(sqlTpl, theParamMap);
		}
		//
		Ajax ajax = newAjax();
		ajax.post("/report/getSqlBasedParamOptionList.json");
		ajax.param("uid", reportUuid);
		ajax.asText();
		ajax.dataText(sqlText);
		ajax.send();
		String jsonText = ajax.resultAsText();
		Map<String, Object> mapResult = JsonUtil.fromJson(jsonText, TypeUtil.TypeRefs.StringObjectMapType);
		if (mapResult != null) {
			Integer errCode = (Integer) mapResult.get("code");
			if (errCode.intValue() == 0) {
				retList = (List<Map<String, Object>>) mapResult.get("data");
			} else {
				String errMsg = (String) mapResult.get("detailMsg");
				if (StrUtil.isNullOrBlank(errMsg)) {
					errMsg = (String) mapResult.getOrDefault("message", "未知原因");
				}
				logger.warn("获取参数选项结果失败：" + errMsg);
			}
		} else {
			String errMsg = ajax.getLastErrorText();
			logger.error("获取参数选项结果失败：" + errMsg);
		}
		//
		return retList;
	}

	private static Result<List<Map<String, Object>>> fetchReportInitParamsInner(ReportMeta reportInfo, Map<String, Object> sqlParamMap) {
		Result<List<Map<String, Object>>> result = Result.newOne();
		//
		if (reportInfo == null) {
			result.type = Type.error;
			result.message = "获取不到指定报表的配置信息";
		} else {
			List<Map<String, Object>> initParamList = new ArrayList<>();
			result.data = initParamList;
			//
			List<Map<String, Object>> queryParams = reportInfo.getQueryParams();
			for (Map<String, Object> queryParam : queryParams) {
				boolean hidden = BoolUtil.isTrue((Boolean) queryParam.get("hidden"));
				if (hidden) {// 隐藏参数不会显示给用户
					continue;
				}
				Map<String, Object> initParam = new LinkedHashMap<String, Object>();
				initParamList.add(initParam);
				//
				String name = (String) queryParam.get("name");
				initParam.put("name", name);
				String text = (String) queryParam.get("text");
				initParam.put("text", text);
				String dataType = (String) queryParam.get("dataType");
				initParam.put("dataType", dataType);
				String ctrlType = (String) queryParam.get("formElement");
				initParam.put("ctrlType", ctrlType);
				Boolean required = (boolean) queryParam.getOrDefault("required", false);
				initParam.put("required", required);
				String defValue = (String) queryParam.get("defaultValue");
				//
				if (!"string".equalsIgnoreCase(dataType)) {
					if (StrUtil.isNullOrBlank(defValue)) {
						defValue = null;
					} else {
						defValue = defValue.trim();
					}
					//
					if ("date".equalsIgnoreCase(dataType)) {
						String defExpr = (String) queryParam.get("defaultExpr");
						String tmpValue = getDateStrByExpr(defExpr);
						if (tmpValue != null) {
							defValue = tmpValue;
						}
					}
				}
				initParam.put("defValue", defValue);
				if ("select".equalsIgnoreCase(ctrlType) || "selectMul".equalsIgnoreCase(ctrlType)) {// 单选和多选
					// 变更要联动的参数名
					String cascName = (String) queryParam.get("cascName");
					if (StrUtil.hasText(cascName)) {
						initParam.put("cascName", cascName.trim());
					}
					//
					String dataSrc = (String) queryParam.get("dataSource");
					String content = (String) queryParam.get("content");
					content = content.trim();
					List<Map<String, Object>> optionList = null;
					if ("sql".equals(dataSrc)) {
						optionList = fetchSqlBasedParamOptionList(reportInfo.getUuid(), content, sqlParamMap);
					} else {// text
						optionList = splitAsOptionList(content);
					}
					initParam.put("options", optionList);
					// 处理多选默认值
					if ("selectMul".equalsIgnoreCase(ctrlType)) {
						if (defValue != null) {
							String[] tmpValues = defValue.split(",", -1);
							for (int i = 0; i < tmpValues.length; i++) {
								tmpValues[i] = tmpValues[i].trim();
							}
							initParam.put("defValue", tmpValues);
						}
					}
				}
			}
			//
		}
		return result;
	}

	/**
	 * 获取指定报表的初始参数信息（便于界面显示，以便支持用户交互，注意：隐藏参数已经过滤掉了）
	 * 
	 * @author koqiui
	 * @date 2019年11月11日 上午1:04:24
	 * 
	 * @param reportCode
	 *            报表自定代码
	 * @param sqlParamMap
	 *            基于sql的列表参数中sql所需的变量
	 * @return
	 */
	public static Result<List<Map<String, Object>>> fetchReportInitParams(String reportCode, Map<String, Object> sqlParamMap) {
		ReportMeta reportInfo = fetchReportInfoByCode(reportCode);
		return fetchReportInitParamsInner(reportInfo, sqlParamMap);
	}

	/**
	 * 获取指定报表的初始参数信息 + 统计列(name,text)列表 和 是否合并左边相同维度行（便于界面显示，以便支持用户交互，注意：隐藏参数已经过滤掉了）
	 * 
	 * @author koqiui
	 * @date 2019年11月25日 下午5:49:45
	 * 
	 * @param reportCode
	 * @param sqlParamMap
	 * @return data : { initParams : [], initExtra : { statColumns :[], isRowSpan }}
	 */
	public static Result<Map<String, Object>> fetchReportInitParamsX(String reportCode, Map<String, Object> sqlParamMap) {
		Result<Map<String, Object>> result = Result.newOne();
		//
		ReportMeta reportInfo = fetchReportInfoByCode(reportCode);

		Result<List<Map<String, Object>>> qryParamResult = fetchReportInitParamsInner(reportInfo, sqlParamMap);
		result.type = qryParamResult.type;
		result.message = qryParamResult.message;
		if (qryParamResult.isSuccess()) {
			Map<String, Object> resultData = new HashMap<>();
			result.data = resultData;
			//
			List<Map<String, Object>> initParamList = qryParamResult.data;
			resultData.put("initParams", initParamList);
			if (initParamList != null) {
				Map<String, Object> initExtra = new HashMap<>();
				resultData.put("initExtra", initExtra);
				//
				List<Map<String, Object>> statColumns = new ArrayList<>();
				initExtra.put("statColumns", statColumns);// 传回来时为name的列表
				// 所有统计列列表（默认都选中）
				Map<String, Object> statColumn = null;
				List<Map<String, Object>> metaColList = reportInfo.getMetaColList();
				for (Map<String, Object> metaCol : metaColList) {
					Boolean isStatis = (Boolean) metaCol.getOrDefault("statis", false);
					if (isStatis) {
						statColumn = new HashMap<>();
						statColumns.add(statColumn);
						//
						statColumn.put("name", metaCol.get("name"));
						statColumn.put("text", metaCol.get("text"));
						statColumn.put("selected", true);
					}
				}
				// 是否合并左边相同维度行
				resultData.put("isRowSpan", true);
			}
		}
		return result;
	}

	/**
	 * 获取指定报表指定名称的基于sql下拉列表选项列表
	 * 
	 * @author koqiui
	 * @date 2019年12月7日 下午5:05:44
	 * 
	 * @param reportCode
	 * @param paramName
	 * @param sqlParamMap
	 * @return
	 */
	public static Result<List<Map<String, Object>>> fetchReportSqlBasedParamOptions(String reportCode, String paramName, Map<String, Object> sqlParamMap) {
		Result<List<Map<String, Object>>> result = Result.newOne();
		//
		ReportMeta reportInfo = fetchReportInfoByCode(reportCode);
		if (reportInfo == null) {
			result.type = Type.error;
			result.message = "获取不到指定报表的配置信息";
		} else {
			String sqlTpl = null;
			//
			List<Map<String, Object>> queryParams = reportInfo.getQueryParams();
			for (Map<String, Object> queryParam : queryParams) {
				String name = (String) queryParam.get("name");
				if (name.equals(paramName)) {
					String ctrlType = (String) queryParam.get("formElement");
					if ("select".equalsIgnoreCase(ctrlType) || "selectMul".equalsIgnoreCase(ctrlType)) {// 单选和多选
						String dataSrc = (String) queryParam.get("dataSource");
						if ("sql".equals(dataSrc)) {
							sqlTpl = (String) queryParam.get("content");
							sqlTpl = sqlTpl.trim();
							break;
						}
					}

				}
			}
			//
			if (sqlTpl != null) {
				result.data = fetchSqlBasedParamOptionList(reportInfo.getUuid(), sqlTpl, sqlParamMap);
			} else {
				result.type = Type.error;
				result.message = "找不到指定的参数信息或参数不是基于sql的下拉控件";
			}
		}
		//
		return result;
	}

	/**
	 * 检查并过滤报表参数（必须的参数是否有有效值），返回缺少的参数名称列表（、分割）
	 * 
	 * @author koqiui
	 * @date 2019年11月10日 上午12:08:13
	 * 
	 * @param reportInfo
	 * @param paramMap
	 *            注意：日期要用yyyy-MM-dd格式的字符串
	 * @return 如果通过返回null，否则返回缺少的参数名称列表（、分割）
	 */
	@SuppressWarnings("unchecked")
	private static String checkAndFilterReportParams(ReportMeta reportInfo, Map<String, Object> paramMap) {
		// 处理 statColumns, isRowSpan
		Boolean isRowSpan = !BoolUtil.isFalse((Boolean) paramMap.get("isRowSpan"));
		paramMap.put("isRowSpan", isRowSpan);
		Object statColumnsVal = paramMap.get("statColumns");
		List<String> statColumns = (List<String>) TypeUtil.asList(statColumnsVal, true);
		if (statColumns != null && !statColumns.isEmpty()) {
			paramMap.put("statColumns", statColumns);
		} else {
			paramMap.remove("statColumns");
		}
		//
		List<String> lackParamNames = new ArrayList<>();
		List<Map<String, Object>> queryParams = reportInfo.getQueryParams();
		for (Map<String, Object> queryParam : queryParams) {
			String name = (String) queryParam.get("name");
			String text = (String) queryParam.get("text");
			String dataType = (String) queryParam.get("dataType");
			String ctrlType = (String) queryParam.get("formElement");
			Boolean required = (boolean) queryParam.getOrDefault("required", false);
			Object objValue = paramMap.get(name);
			if ("selectMul".equalsIgnoreCase(ctrlType)) {
				List<Object> valueItemList = (List<Object>) TypeUtil.asList(objValue, true);
				if (required) {
					if (valueItemList == null || valueItemList.isEmpty()) {
						lackParamNames.add("缺少 " + text);
						continue;
					}
				}
				if (valueItemList != null) {
					int invalidIndex = -1;
					String invalidMsg = null;
					for (int i = 0; i < valueItemList.size(); i++) {
						objValue = valueItemList.get(i);
						if (objValue == null) {
							invalidIndex = i;
							invalidMsg = "为null";
							break;
						}
						String strValue = String.valueOf(objValue);
						if (!"string".equalsIgnoreCase(dataType)) {
							strValue = strValue.trim();
							if (StrUtil.EmptyStr.equals(strValue)) {// integer,float,date,bool
								invalidIndex = i;
								invalidMsg = "无效";
								break;
							}
						}
						// 转换参数值
						if ("integer".equalsIgnoreCase(dataType)) {
							Long intVal = NumUtil.parseLong(strValue);
							if (intVal == null) {
								invalidIndex = i;
								invalidMsg = "不是整数";
								break;
							} else {
								objValue = intVal;
							}
						} else if ("float".equalsIgnoreCase(dataType)) {
							Double fltVal = NumUtil.parseDouble(strValue);
							if (fltVal == null) {
								invalidIndex = i;
								invalidMsg = "不是浮点数";
								break;
							} else {
								objValue = fltVal;
							}
						} else if ("date".equalsIgnoreCase(dataType)) {
							try {
								Date dtVal = DateUtil.fromStdDateStr(strValue);
								if (dtVal == null) {
									invalidIndex = i;
									invalidMsg = "不是日期形式";
									break;
								} else {
									objValue = strValue;// 使用日期字符串
								}
							} catch (ParseException e) {
								invalidIndex = i;
								invalidMsg = "不是yyyy-MM-dd日期形式";
								break;
							}
						} else if ("bool".equalsIgnoreCase(dataType)) {
							objValue = BoolUtil.isTrue(strValue);
						} else {// string
							objValue = strValue;
						}
						//
						valueItemList.set(i, objValue);
					}
					//
					if (invalidIndex != -1) {
						lackParamNames.add(text + "值的第 " + (invalidIndex + 1) + " " + invalidMsg);
						continue;
					}
				}
				//
				paramMap.put(name, valueItemList);//
			} else {// 单值
				String strValue = objValue == null ? null : String.valueOf(objValue);
				if (required) {// 检查必须性
					if (strValue == null) {// 没有值
						lackParamNames.add("缺少 " + text);
						continue;
					}
					if (!"string".equalsIgnoreCase(dataType)) {
						strValue = strValue.trim();
						if (StrUtil.EmptyStr.equals(strValue)) {// integer,float,date,bool
							lackParamNames.add("缺少 " + text);
							continue;
						}
					}
				} else {
					if (strValue != null && !"string".equalsIgnoreCase(dataType)) {
						strValue = strValue.trim();
						if (StrUtil.EmptyStr.equals(strValue)) {// integer,float,date,bool
							strValue = null;
						}
					}
				}
				// 转换参数值
				if (strValue == null) {
					objValue = null;
				} else {
					if ("integer".equalsIgnoreCase(dataType)) {
						Long intVal = NumUtil.parseLong(strValue);
						if (intVal == null) {
							lackParamNames.add(text + " 不是整数");
							continue;
						} else {
							objValue = intVal;
						}
					} else if ("float".equalsIgnoreCase(dataType)) {
						Double fltVal = NumUtil.parseDouble(strValue);
						if (fltVal == null) {
							lackParamNames.add(text + " 不是浮点数");
							continue;
						} else {
							objValue = fltVal;
						}
					} else if ("date".equalsIgnoreCase(dataType)) {
						try {
							Date dtVal = DateUtil.fromStdDateStr(strValue);
							if (dtVal == null) {
								lackParamNames.add(text + " 不是日期形式");
								continue;
							} else {
								objValue = strValue;// 使用日期字符串
							}
						} catch (ParseException e) {
							lackParamNames.add(text + " 不是yyyy-MM-dd日期形式");
							continue;
						}
					} else if ("bool".equalsIgnoreCase(dataType)) {
						objValue = BoolUtil.isTrue(strValue);
					} else {// string
						objValue = strValue;
					}
				}
				//
				paramMap.put(name, objValue);
			}

		}
		//
		if (lackParamNames.isEmpty()) {
			logger.info("--------【" + reportInfo.getName() + "】 统计查询参数 --------");
			logger.info(JsonUtil.toFormattedJson(paramMap));
			return null;
		} else {
			logger.warn("--------【" + reportInfo.getName() + "】 缺少查询参数 --------");
			logger.warn(JsonUtil.toFormattedJson(paramMap));
			return StrUtil.join(lackParamNames, "、");
		}
	}

	/**
	 * 获取指定报表的结果（data.htmlTable为html）
	 * 
	 * @author koqiui
	 * @date 2019年11月10日 上午12:23:21
	 * 
	 * @param reportCode
	 * @param paramMap
	 *            注意：日期要用yyyy-MM-dd格式的字符串
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Result<Map<String, Object>> fetchReportResult(String reportCode, Map<String, Object> paramMap) {
		Result<Map<String, Object>> result = Result.newOne();
		//
		ReportMeta reportInfo = fetchReportInfoByCode(reportCode);
		if (reportInfo == null) {
			result.type = Type.error;
			result.message = "获取不到指定报表的配置信息";
		} else {
			if (paramMap == null) {
				paramMap = new HashMap<String, Object>();
			}
			String errParamsMesage = checkAndFilterReportParams(reportInfo, paramMap);
			if (errParamsMesage != null) {
				result.type = Type.error;
				result.message = errParamsMesage;
			} else {
				// 补充必要参数
				paramMap.put("uid", reportInfo.getUuid());
				paramMap.put("is_restMode", true);
				//
				Ajax ajax = newAjax();
				ajax.post("/report/table/getData.json");
				ajax.asForm();
				ajax.dataMap(paramMap);
				ajax.send();
				String jsonText = ajax.resultAsText();
				Map<String, Object> mapResult = JsonUtil.fromJson(jsonText, TypeUtil.TypeRefs.StringObjectMapType);
				if (mapResult != null) {
					Integer errCode = (Integer) mapResult.get("code");
					if (errCode.intValue() == 0) {
						result.data = (Map<String, Object>) mapResult.get("data");
					} else {
						String errMsg = (String) mapResult.get("detailMsg");
						if (StrUtil.isNullOrBlank(errMsg)) {
							errMsg = (String) mapResult.getOrDefault("message", "未知原因");
						}
						//
						result.type = Type.error;
						result.message = errMsg;
						logger.warn("获取报表结果失败：" + errMsg);
					}
				} else {
					String errMsg = ajax.getLastErrorText();
					result.type = Type.error;
					result.message = errMsg;
					logger.error("获取报表结果失败：" + errMsg);
				}
			}
		}
		//
		return result;
	}

	/**
	 * 获取指定报表的数据集行（json）
	 * 
	 * @author koqiui
	 * @date 2019年11月11日 下午12:38:01
	 * 
	 * @param reportCode
	 * @param paramMap
	 *            注意：日期要用yyyy-MM-dd格式的字符串
	 * @return
	 */
	public static Result<List<Map<String, Object>>> fetchReportResultSet(String reportCode, Map<String, Object> paramMap) {
		ReportMeta reportInfo = fetchReportInfoByCode(reportCode);
		return fetchReportResultSetInner(reportInfo, paramMap);
	}

	@SuppressWarnings("unchecked")
	private static Result<List<Map<String, Object>>> fetchReportResultSetInner(ReportMeta reportInfo, Map<String, Object> paramMap) {
		Result<List<Map<String, Object>>> result = Result.newOne();
		if (reportInfo == null) {
			result.type = Type.error;
			result.message = "获取不到指定报表的配置信息";
		} else {
			if (paramMap == null) {
				paramMap = new HashMap<String, Object>();
			}
			String errParamsMesage = checkAndFilterReportParams(reportInfo, paramMap);
			if (errParamsMesage != null) {
				result.type = Type.error;
				result.message = errParamsMesage;
			} else {
				// 补充必要参数
				paramMap.put("uid", reportInfo.getUuid());
				//
				Ajax ajax = newAjax();
				ajax.get("/report/getResultSetRows.json");
				ajax.params(paramMap);
				ajax.asForm();
				ajax.send();
				String jsonText = ajax.resultAsText();
				logger.debug(jsonText);
				Map<String, Object> mapResult = JsonUtil.fromJson(jsonText, TypeUtil.TypeRefs.StringObjectMapType);
				if (mapResult != null) {
					Integer errCode = (Integer) mapResult.get("code");
					if (errCode.intValue() == 0) {
						result.data = (List<Map<String, Object>>) mapResult.get("data");
					} else {
						String errMsg = (String) mapResult.get("detailMsg");
						if (StrUtil.isNullOrBlank(errMsg)) {
							errMsg = (String) mapResult.getOrDefault("message", "未知原因");
						}
						//
						result.type = Type.error;
						result.message = errMsg;
						logger.warn("获取报表结果数据失败：" + errMsg);
					}
				} else {
					String errMsg = ajax.getLastErrorText();
					result.type = Type.error;
					result.message = errMsg;
					logger.error("获取报表结果数据失败：" + errMsg);
				}
			}
		}
		//
		return result;
	}

	/**
	 * 获取指定报表的数据（行信息 + 结果集行）（json）
	 * 
	 * @author koqiui
	 * @date 2019年11月11日 下午12:38:51
	 * 
	 * @param reportCode
	 * @param paramMap
	 * @return
	 */
	public static Result<Map<String, Object>> fetchReportResultData(String reportCode, Map<String, Object> paramMap) {
		Result<Map<String, Object>> result = Result.newOne();
		//
		ReportMeta reportInfo = fetchReportInfoByCode(reportCode);
		if (reportInfo == null) {
			result.type = Type.error;
			result.message = "获取不到指定报表结果信息";
		} else {
			Map<String, Object> resultData = new LinkedHashMap<>();
			result.data = resultData;
			//
			resultData.put("cols", reportInfo.getMetaColList());
			//
			Result<List<Map<String, Object>>> resultSetInfo = fetchReportResultSetInner(reportInfo, paramMap);
			result.type = resultSetInfo.type;
			result.message = resultSetInfo.message;
			resultData.put("rows", resultSetInfo.data);
		}
		//
		return result;
	}

}
