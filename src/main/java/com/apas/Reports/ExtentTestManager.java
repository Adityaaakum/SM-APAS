package com.apas.Reports;

import java.util.HashMap;
import java.util.Map;

import com.relevantcodes.extentreports.ExtentTest;

public class ExtentTestManager {

	static private ExtentTest test;

	static Map<Integer, ExtentTest> extentTestMap = new HashMap<Integer, ExtentTest>();
	// static ExtentReports extent = new ExtentManager().getExtentInstance();

	public static synchronized ExtentTest getTest() {
		return (ExtentTest) extentTestMap.get((int) (long) (Thread.currentThread().getId()));
	}

	public static synchronized void endTest() {
		ExtentManager.getExtentInstance()
				.endTest((ExtentTest) extentTestMap.get((int) (long) (Thread.currentThread().getId())));
	}

	public static synchronized ExtentTest startTest(String testName) {
		return startTest(testName, "");
	}

	public static synchronized ExtentTest startTest(String testName, String desc) {
		ExtentTest test = ExtentManager.getExtentInstance().startTest(testName, desc);
		setUpTestVariable(test);
		extentTestMap.put((int) (long) (Thread.currentThread().getId()), test);
		return test;
	}
	
	public static void setUpTestVariable(ExtentTest _test) {
		test = _test;
	}

	public static ExtentTest getUpTestVariable() {
		return test;
	}
}
