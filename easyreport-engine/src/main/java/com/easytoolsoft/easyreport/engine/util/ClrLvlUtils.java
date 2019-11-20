package com.easytoolsoft.easyreport.engine.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * 简单色阶辅助类
 * 
 * @author koqiui
 * @date 2019年11月20日 下午6:13:19
 *
 */
public class ClrLvlUtils {
	private static final String hexRegex = "^#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$";

	/**
	 * 将十六进制hex（#RRGGBB | #RGB）颜色表示方式转换为rgb元组
	 * 
	 * @author koqiui
	 * @date 2019年11月20日 下午3:42:49
	 * 
	 * @param hexClrStr
	 * @return
	 */
	public static Integer[] hexColorToRgbTuple(String hexClrStr) {
		if (StringUtils.isBlank(hexClrStr)) {
			return null;
		}
		hexClrStr = hexClrStr.trim();
		if (hexClrStr.matches(hexRegex)) {
			if (!hexClrStr.startsWith("#")) {
				// 统一格式
				hexClrStr = "#" + hexClrStr;
			}
			//
			if (hexClrStr.length() == 4) { // 统一为7位
				String tmpClrStr = "#";
				Character chrClr = null;
				for (int i = 1; i < 4; i = i + 1) {
					chrClr = hexClrStr.charAt(i);
					tmpClrStr += chrClr + chrClr; // 重复rgb值
				}
				hexClrStr = tmpClrStr;
			}
			// 处理六位的颜色值
			Integer[] rgbClrVals = new Integer[3];
			String hexClr = null;
			for (int i = 1, c = 0; i < 7; i += 2) {
				hexClr = hexClrStr.substring(i, i + 2);
				rgbClrVals[c++] = Integer.parseInt(hexClr, 16);
			}
			return rgbClrVals;
		} else {
			System.err.println("无效的 #RRGGBB 颜色字符串");
			return null;
		}
	}

	public static Color hexColorToColor(String hexClrStr) {
		Integer[] rgbTuple = hexColorToRgbTuple(hexClrStr);
		if (rgbTuple == null) {
			return null;
		}
		return new Color(rgbTuple[0], rgbTuple[1], rgbTuple[2]);
	}

	public static String colorToHex(Color color) {
		return rgbToHex(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static String rgbToHex(int r, int g, int b) {
		String hexClr = null;
		StringBuilder hexClrStr = new StringBuilder("#");
		// r
		hexClr = Integer.toHexString(r);
		if (hexClr.length() == 1) {
			hexClrStr.append("0");
		}
		hexClrStr.append(hexClr);
		// g
		hexClr = Integer.toHexString(g);
		if (hexClr.length() == 1) {
			hexClrStr.append("0");
		}
		hexClrStr.append(hexClr);
		// b
		hexClr = Integer.toHexString(b);
		if (hexClr.length() == 1) {
			hexClrStr.append("0");
		}
		hexClrStr.append(hexClr);
		//
		return hexClrStr.toString();
	}

	public static Map<Number, String> getGradientColorMap(Color startColor, Color endColor, long[] ukNums, int valve) {
		if (valve < 2) {
			valve = 2;
		}
		int count = ukNums == null ? 0 : ukNums.length;
		if (count < valve) {
			return null;
		}
		Arrays.sort(ukNums);
		System.out.println(StringUtils.join(ukNums, ", "));
		//
		int startR = startColor.getRed();
		int startG = startColor.getGreen();
		int startB = startColor.getBlue();

		int endR = endColor.getRed();
		int endG = endColor.getGreen();
		int endB = endColor.getBlue();
		//
		Map<Number, String> colorMap = new HashMap<>(count);
		//
		int diffR = endR - startR;
		int diffG = endG - startG;
		int diffB = endB - startB;
		//
		long baseNum = ukNums[0]; // 第一个数
		long diffNum = ukNums[count - 1] - baseNum;
		//
		String hexClrStr = null;
		for (int i = 0; i < count; i++) {
			long num = ukNums[i];
			double frg = (num - baseNum) * 1.0 / diffNum;
			Double newR = startR + diffR * frg;
			Double newG = startG + diffG * frg;
			Double newB = startB + diffB * frg;
			hexClrStr = rgbToHex(newR.intValue(), newG.intValue(), newB.intValue());
			colorMap.put(num, hexClrStr);
		}
		return colorMap;
	}

	public static Map<Number, String> getGradientColorMap(Color startColor, Color endColor, double[] ukNums, int valve) {
		if (valve < 2) {
			valve = 2;
		}
		int count = ukNums == null ? 0 : ukNums.length;
		if (count < valve) {
			return null;
		}
		Arrays.sort(ukNums);
		System.out.println(StringUtils.join(ukNums, ", "));
		//
		int startR = startColor.getRed();
		int startG = startColor.getGreen();
		int startB = startColor.getBlue();

		int endR = endColor.getRed();
		int endG = endColor.getGreen();
		int endB = endColor.getBlue();
		//
		Map<Number, String> colorMap = new HashMap<>(count);
		//
		int diffR = endR - startR;
		int diffG = endG - startG;
		int diffB = endB - startB;
		//
		double baseNum = ukNums[0]; // 第一个数
		double diffNum = ukNums[count - 1] - baseNum;
		//
		String hexClrStr = null;
		for (int i = 0; i < count; i++) {
			double num = ukNums[i];
			double frg = (num - baseNum) * 1.0 / diffNum;
			Double newR = startR + diffR * frg;
			Double newG = startG + diffG * frg;
			Double newB = startB + diffB * frg;
			hexClrStr = rgbToHex(newR.intValue(), newG.intValue(), newB.intValue());
			colorMap.put(num, hexClrStr);
		}
		return colorMap;
	}
}
