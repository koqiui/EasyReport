package com.easytoolsoft.easyreport.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;

/**
 * 自定义链接函数信息
 * 
 * @author koqiui
 * @date 2019年12月6日 下午4:22:36
 *
 */
public class LinkFunc {
	public String funcName;
	public String[] colNames;
	public boolean useAllCols = false;
	//
	public String tplStr;

	// showReportDetail ([colName1 , colName2,...])
	public static LinkFunc fromLinkFuncExpr(String linkFuncExpr) {
		if (StringUtils.isBlank(linkFuncExpr)) {
			return null;
		}
		// System.out.println(linkFuncExpr);
		linkFuncExpr = linkFuncExpr.trim();
		// System.out.println(linkFuncExpr);
		linkFuncExpr = linkFuncExpr.replaceAll("\\s", "");
		// System.out.println(linkFuncExpr);
		linkFuncExpr = linkFuncExpr.replaceAll("\"", "");
		// System.out.println(linkFuncExpr);
		linkFuncExpr = linkFuncExpr.replaceAll("\'", "");
		// System.out.println(linkFuncExpr);
		linkFuncExpr = linkFuncExpr.replace("[", "");
		// System.out.println(linkFuncExpr);
		linkFuncExpr = linkFuncExpr.replace("]", "");
		// System.out.println(linkFuncExpr);
		int tmpIndex = linkFuncExpr.indexOf("(");
		if (tmpIndex == -1) {
			System.err.println("linkFuncExpr 缺少 (");
			return null;
		}
		String funcName = linkFuncExpr.substring(0, tmpIndex).trim();
		if (funcName.length() == 0) {
			System.err.println("linkFuncExpr 缺少 funcName");
			return null;
		}
		int tmpIndex2 = linkFuncExpr.indexOf(")");
		if (tmpIndex2 == -1) {
			System.err.println("linkFuncExpr 缺少 )");
			return null;
		}
		if (tmpIndex2 < tmpIndex) {
			System.err.println("linkFuncExpr ( 和 ) 位置错误");
			return null;
		}
		//
		LinkFunc retFunc = new LinkFunc();
		retFunc.funcName = funcName;

		String colNamesExpr = linkFuncExpr.substring(tmpIndex + 1, tmpIndex2).trim();
		if (colNamesExpr.length() == 0) {
			retFunc.useAllCols = true;
		} else {
			String[] colNamesTmp = colNamesExpr.split(",", -1);
			List<String> colNames = new ArrayList<>();
			// System.out.println(JSON.toJSONString(colNamesTmp));
			for (String colName : colNamesTmp) {
				if (colName.length() > 0) {
					colNames.add(colName);
				}
			}
			if (colNames.size() == 0) {
				retFunc.useAllCols = true;
			} else {
				retFunc.useAllCols = false;
				retFunc.colNames = colNames.toArray(new String[0]);
			}
		}
		retFunc.tplStr = retFunc.funcName + "(%s, \"%s\")";
		// System.out.println(retFunc.tplStr);
		return retFunc;
	}

	//
	private static final String LINK_HTML_TPL = "<a href=\"#\" onclick=\"%s\">%s</a>";

	private static String escapeXmlAttrValue(String rawStr) {
		String retStr = null;
		if (rawStr != null) {
			retStr = rawStr.replaceAll("&", "&#38;");//
			retStr = retStr.replaceAll("\"", "&#34;");
			retStr = retStr.replaceAll("\'", "&#39;");
			retStr = retStr.replaceAll("<", "&#60;");
			retStr = retStr.replaceAll(">", "&#62;");
		}
		return retStr;
	}

	public static String toLinkHtml(String valueText, LinkFunc linkFunc, String reportCode, String dataName, Object dataValue) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(dataName, dataValue);
		return toLinkHtml(valueText, linkFunc, reportCode, dataMap);
	}

	public static String toLinkHtml(String valueText, LinkFunc linkFunc, String reportCode, Map<String, Object> dataMap) {
		if (linkFunc == null) {
			return valueText;
		}
		String dataMapJson = null;
		if (linkFunc.useAllCols) {
			dataMapJson = JSON.toJSONString(dataMap);
		} else {
			Map<String, Object> tempMap = new HashMap<>();
			for (String colName : linkFunc.colNames) {
				if (dataMap.containsKey(colName)) {
					tempMap.put(colName, dataMap.get(colName));
				}
			}
			if (tempMap.isEmpty()) {
				// 没找到key，使用全部数据
				tempMap = dataMap;
			}
			dataMapJson = JSON.toJSONString(tempMap);
		}
		String funcStr = String.format(linkFunc.tplStr, dataMapJson, reportCode);
		// System.out.println(funcStr);

		funcStr = escapeXmlAttrValue(funcStr);
		// System.out.println(funcStr);

		return String.format(LINK_HTML_TPL, funcStr, valueText);
	}
}
