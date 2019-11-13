package com.easytoolsoft.easyreport.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {
	// ${varName|format}
	public final static String VAR_PLACEHOLDER_PREFIX = "${";
	public final static String VAR_PLACEHOLDER_SUFFIX = "}";
	public final static Pattern VAR_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{\\s*[a-zA-Z0-9_]+\\s*\\}");

	// extractVarNameFromVarNameHolder("${卖家名称}") => 卖家名称
	private static String extractVarNameFromVarNameHolder(String varNameHolder) {
		return extractVarNameFromVarNameHolder(varNameHolder, VAR_PLACEHOLDER_PREFIX, VAR_PLACEHOLDER_SUFFIX);
	}

	// extractVarNameFromVarNameHolder("<%卖家名称%>", "<%", "%>") => 卖家名称
	public static String extractVarNameFromVarNameHolder(String varNameHolder, String varNamePrefix, String varNameSuffix) {
		int index1 = varNameHolder.indexOf(varNamePrefix);
		if (index1 == -1) {
			return null;
		}
		int index11 = index1 + varNamePrefix.length();

		int index22 = varNameHolder.indexOf(varNameSuffix, index11);
		if (index22 == -1) {
			return null;
		}
		// int index2 = index22 + varNameSuffix.length();
		return varNameHolder.substring(index11, index22).trim();
	}

	public static List<String> extractVarNames(String template) {
		if (template == null) {
			return null;
		}
		//
		List<String> result = new ArrayList<>();
		//
		Matcher matcher = VAR_PLACEHOLDER_PATTERN.matcher(template);
		while (matcher.find()) {
			String varNameHolder = matcher.group();
			String varName = extractVarNameFromVarNameHolder(varNameHolder);
			// System.out.println(varNameHolder + " => [" + varName + "]");
			if (!result.contains(varName)) {
				result.add(varName);
			}
		}
		return result;
	}

	/**
	 * 判断给定的字符串是否已经被单引号 ' 引住了
	 * 
	 * @author koqiui
	 * @date 2019年11月13日 下午7:17:03
	 * 
	 * @return
	 */
	public static boolean isSingleQuoted(String checkStr) {
		if (checkStr == null) {
			return false;
		}
		return checkStr.startsWith("'") && checkStr.endsWith("'");
	}

	/**
	 * 处理字符串中 \, \n , \r, '转义
	 * 
	 * @author koqiui
	 * @date 2019年11月13日 下午7:45:47
	 * 
	 * @param srcStr
	 * @return
	 */
	public static String toSqlStrValue(String srcStr) {
		if (srcStr == null) {
			return null;
		}
		String retStr = srcStr.replaceAll("\\", "\\\\").replaceAll("\n", "\\n").replaceAll("\r", "\\r");
		return retStr.replaceAll("'", "\\'");
	}

}
