package com.easytoolsoft.easyreport.common.util;

import org.apache.commons.lang3.StringUtils;

public class NumUtils {
	/**
	 * 解析错误返回 null
	 * 
	 * @author koqiui
	 * 
	 * @param numStr
	 * @return
	 */
	public static Long parseLong(String numStr) {
		if (StringUtils.isBlank(numStr)) {
			return null;
		}
		//
		try {
			return Long.parseLong(numStr);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	/**
	 * 解析错误返回 null
	 * 
	 * @author koqiui
	 * 
	 * @param numStr
	 * @return
	 */
	public static Double parseDouble(String numStr) {
		if (StringUtils.isBlank(numStr)) {
			return null;
		}
		//
		try {
			return Double.parseDouble(numStr);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
}
