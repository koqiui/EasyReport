package com.easytoolsoft.easyreport.engine.util;

import org.junit.Test;

public class NumberUtilsTest {

	@Test
	public void test_isNumVal0() {
		String numStr = "0";
		System.out.println(numStr + " ? " + NumberUtils.isNumVal0(numStr));

		numStr = "-0";
		System.out.println(numStr + " ? " + NumberUtils.isNumVal0(numStr));

		numStr = ".0";
		System.out.println(numStr + " ? " + NumberUtils.isNumVal0(numStr));

		numStr = "0.0";
		System.out.println(numStr + " ? " + NumberUtils.isNumVal0(numStr));

		numStr = "0.0000000001";
		System.out.println(numStr + " ? " + NumberUtils.isNumVal0(numStr));
		
		numStr = "";
		System.out.println(numStr + " ? " + NumberUtils.isNumVal0(numStr));
		
		numStr = "1.";
		System.out.println(numStr + " ? " + NumberUtils.isNumVal0(numStr));
	}

}
