package com.easytoolsoft.easyreport.engine.util;

import java.awt.Color;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class ClrLvlUtilsTest {

	@Test
	public void test_hexColorToRgbTuple() {
		String hexClrStr = "FF00ff";
		Integer[] rgbTuple = ClrLvlUtils.hexColorToRgbTuple(hexClrStr);
		System.out.println(hexClrStr + " => " + StringUtils.join(rgbTuple, ", "));

		hexClrStr = "#FF00ff";
		rgbTuple = ClrLvlUtils.hexColorToRgbTuple(hexClrStr);
		System.out.println(hexClrStr + " => " + StringUtils.join(rgbTuple, ", "));
	}

	@Test
	public void test_hexColorToColor() {
		String hexClrStr = "FF00ff";
		Color color = ClrLvlUtils.hexColorToColor(hexClrStr);
		System.out.println(hexClrStr + " => " + color);

		hexClrStr = "#FF00ff";
		color = ClrLvlUtils.hexColorToColor(hexClrStr);
		System.out.println(hexClrStr + " => " + color);
	}

	@Test
	public void test_rgbToHex() {
		String hexClrStr = ClrLvlUtils.rgbToHex(255, 0, 255);
		System.out.println(" => " + hexClrStr);
	}

	@Test
	public void test_colorToHex() {
		Color color = new Color(255, 0, 255);
		String hexClrStr = ClrLvlUtils.colorToHex(color);
		System.out.println(color + " => " + hexClrStr);
	}

	@Test
	public void test_getGradientColorMap_long() {
		Color startColor = new Color(255, 0, 255);
		Color endColor = new Color(0, 0, 255);
		int valve = 3;
		long[] ukNums = new long[] { 1L, 6L, 3L, 10L, 20L };
		Map<Number, String> colorMap = ClrLvlUtils.getGradientColorMap(startColor, endColor, ukNums, valve);
		System.out.println(colorMap);
	}

}
