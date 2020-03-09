package com.apas.Assertions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;
import com.apas.Reports.ExtentManager;
import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;

public class SoftAssertion {

	String currentMethodName = null;
	String currentDescription = null;
	Boolean testCaseExecutionFailedFlag = false;
	public static Boolean isSoftAssertionUsedFlag = null;

	/**
	 * Class constructor to set the isSoftAssertionUsedFlag variable as false
	 * Initially this variable is null, when class object is created it sets to false
	 */
	public SoftAssertion() {
		isSoftAssertionUsedFlag = false;
	}

	/**
	 * Updates the test case map with assertion result
	 * @param testCaseKey: SMAB-T418
	 * @param testCaseStatus: Pass / Fail
	 */
	private static void updateTestCaseStatusInMap(String testCaseKey, String testCaseStatus) {
		String currentStatus = JiraAdaptavistStatusUpdate.testStatus.get(testCaseKey);
		if((currentStatus == null) || !(currentStatus.equalsIgnoreCase("Fail"))) {
				JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, testCaseStatus);
		}
	}

	/**
	 * Extracts the test case key from the argument and sets a system property with it
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	private static void extractTestCaseKey(String message) {
		String testCaseKey = message.substring(0, message.indexOf(":")).trim();
		System.setProperty("testCaseKey", testCaseKey);
	}

	/**
	 * Captures the screen shot on assertion failure and attaches it toExtent report
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public void getScreenShot(String message) {
		String methodName = System.getProperty("currentMethodName");
		RemoteWebDriver ldriver = BrowserDriver.getBrowserInstance();
		TakesScreenshot ts = (TakesScreenshot) ldriver;
		File source = ts.getScreenshotAs(OutputType.FILE);

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
		String upDate = sdf.format(date);
		String dest = System.getProperty("user.dir")+ "//test-output//ErrorScreenshots//" + methodName + upDate +".png";

		File destination = new File(dest);
		try {
			FileUtils.copyFile(source, destination);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Snapshot for the failed validation : " + message
					+ ExtentTestManager.getUpTestVariable().addScreenCapture(dest));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Asserts all the asserts used in test method sequentially & flushes the ExtentManager instance
	 * Sets isSoftAssertionUsedFlag to true to control onTestPassed / onTestFailure in SuiteListener
	 * Throws AssertionError if testCaseExecutionFailedFlag variable is true
	 */
	public void assertAll() {
		isSoftAssertionUsedFlag = true;
		ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
		ExtentManager.getExtentInstance().flush();
		if (testCaseExecutionFailedFlag) {
			throw new AssertionError();
		}
	}

	/**
	 * Asserts that given condition is true, if not AssertionError is thrown when assertAll is called
	 * Updates the test case status in test case map as pass or fail
	 * Takes screen shot on failure and sets testCaseExecutionFailedFlag variable as true
	 * 
	 * @param actual: true
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public void assertTrue(final boolean condition, String message) {
		String formattedMessage = message + " :: Expected: true" + " || " + "Actual: " + condition;
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (condition) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
		} else {
			testCaseExecutionFailedFlag = true;
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
		}
	}

	/**
	 * Asserts that given condition is false, if not AssertionError is thrown when assertAll is called
	 * Updates the test case status in test case map as pass or fail
	 * Takes screen shot on failure and sets testCaseExecutionFailedFlag variable as true
	 * 
	 * @param actual: false
	 * @param message: "SMAB-T418: <Some validation message>"
	 */	
	public void assertFalse(final boolean condition, String message) {
		String formattedMessage = message + " :: Expected: false" + " || " + "Actual: " + condition;
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (!condition) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
		} else {
			testCaseExecutionFailedFlag = true;
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
		}
	}

}	