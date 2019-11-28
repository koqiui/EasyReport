package com.easytoolsoft.easyreport.engine.util;

import java.util.Date;

import org.junit.Test;

public class DateUtilsTest {

	@Test
	public void test_getToday() {
		Date theDate = DateUtils.getToday();
		System.out.println(DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

	@Test
	public void test_getMonthFirstDay() {
		Date theDate = DateUtils.getMonthFirstDay();
		System.out.println(DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

	@Test
	public void test_getMonthLastDay() {
		Date theDate = DateUtils.getMonthLastDay();
		System.out.println(DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

	@Test
	public void test_getYearFirstDay() {
		Date theDate = DateUtils.getYearFirstDay();
		System.out.println(DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

	@Test
	public void test_getYearLastDay() {
		Date theDate = DateUtils.getYearLastDay();
		System.out.println(DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

	@Test
	public void test_eval_today() {
		String dateExpr = null;
		Date theDate = null;
		//
		dateExpr = "  ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " +7 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " today ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " today - 1 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " today + 7 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

	@Test
	public void test_eval_month() {
		String dateExpr = null;
		Date theDate = null;
		//
		dateExpr = " month . firstday  ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " month . firstday - 1 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " month . firstday + 7 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		//
		dateExpr = " month . lastday  ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " month . lastday - 1 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " month . lastday + 7 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

	@Test
	public void test_eval_year() {
		String dateExpr = null;
		Date theDate = null;
		//
		dateExpr = " year . firstday  ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " year . firstday - 1 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " year . firstday + 7 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		//
		dateExpr = " year . lastday  ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " year . lastday - 1 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));

		dateExpr = " year . lastday + 7 ";
		theDate = DateUtils.evalDateExpr(dateExpr);
		System.out.println(theDate == null ? null : DateUtils.getDate(theDate, "yyyy-MM-dd"));
	}

}
