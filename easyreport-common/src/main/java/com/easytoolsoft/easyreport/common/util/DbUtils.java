package com.easytoolsoft.easyreport.common.util;

/**
 * 
 * 
 * @author koqiui
 * @date 2019年11月8日 下午8:42:18
 *
 */
public class DbUtils {
	private static final String[] MERGEABLE_DB_DRIVER_CLASS_NAMES = new String[] { "org.h2.Driver" };

	// 是否可合并插入的数据库驱动类
	public static boolean isMergeableDbDriverClassName(String driverClassName) {
		for (String tmdDriverClassName : MERGEABLE_DB_DRIVER_CLASS_NAMES) {
			if (tmdDriverClassName.equalsIgnoreCase(driverClassName)) {
				return true;
			}
		}
		return false;
	}
}
