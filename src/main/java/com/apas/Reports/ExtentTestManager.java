package com.apas.Reports;

import java.util.HashMap;
import java.util.Map;

import com.relevantcodes.extentreports.ExtentTest;

public class ExtentTestManager {

	static private ExtentTest test;

	static Map<Integer, ExtentTest> extentTestMap = new HashMap<Integer, ExtentTest>();

	/**
	 * Description: This will return the instance of the test being set for extent report
	**/
	public static synchronized ExtentTest getTest() {
		return (ExtentTest) extentTestMap.get((int) (long) (Thread.currentThread().getId()));
	}

	/**
	 * Description: This will end the test on the current thread
	**/
	public static synchronized void endTest() {
		ExtentManager.getExtentInstance()
				.endTest((ExtentTest) extentTestMap.get((int) (long) (Thread.currentThread().getId())));
	}

	/**
	 * Description: Overloaded method of startTest(String testName, String desc)
	**/
	public static synchronized ExtentTest startTest(String testName) {
		return startTest(testName, "");
	}

	/**
	 * Description: This will start the test 
	 * @param testName: Name of the test case to be started
	 * @param desc: Description of the test case
	**/
	public static synchronized ExtentTest startTest(String testName, String desc) {
		ExtentTest test = ExtentManager.getExtentInstance().startTest(testName, desc);
		setUpTestVariable(test);
		extentTestMap.put((int) (long) (Thread.currentThread().getId()), test);
		return test;
	}
	
	/**
	 * Description: This function is to set up the test variable
	**/
	public static void setUpTestVariable(ExtentTest _test) {
		test = _test;
	}

	/**
	 * Description: This function is to get the instance of the test variable
	**/
	public static ExtentTest getUpTestVariable() {
		return test;
	}
}
