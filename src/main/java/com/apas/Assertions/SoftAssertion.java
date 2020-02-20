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

	public SoftAssertion() {
		isSoftAssertionUsedFlag = false;
	}

	private static void updateTestCaseStatusInMap(String testCaseKey, String testCaseStatus) {
		JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, testCaseStatus);
	}

	private static void extractTestCaseKey(String message) {
		String testCaseKey = message.substring(0, message.indexOf(":")).trim();
		System.setProperty("testCaseKey", testCaseKey);
	}

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

	public void assertAll() {
		isSoftAssertionUsedFlag = true;
		ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
		ExtentManager.getExtentInstance().flush();
		if (testCaseExecutionFailedFlag) {
			throw new AssertionError();
		}
	}

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

	public void assertEquals(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		String formattedMessage = message + " :: Expected: " + expected + " || " + "Actual: " + actual;
		if ((expected == null) && (actual == null)) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
			return;
		}
		if (expected == null || actual == null) {
			testCaseExecutionFailedFlag = true;
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
		}
		if (actual.equals(expected)) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
			return;
		} else {
			testCaseExecutionFailedFlag = true;
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
		}
	}

	public void assertNotEquals(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		String formattedMessage = message + " :: Expected: " + expected + " || " + "Actual: " + actual;

		if ((expected == null) && (actual == null)) {
			testCaseExecutionFailedFlag = true;
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
			return;
		}
		if (expected == null || actual == null) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
			return;
		}
		if (actual.equals(expected)) {
			testCaseExecutionFailedFlag = true;
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
			return;
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
		}
	}

	public boolean compareMaps(Map<String, String> actualMap, Map<String, String> expectedMap) {
		boolean comparisonStatus = false;
		if ((actualMap != null && expectedMap != null) && (actualMap.size() == expectedMap.size())
				&& (actualMap.keySet().equals(expectedMap.keySet()))) {
			Set<String> entrySet = actualMap.keySet();
			Iterator<String> itr = entrySet.iterator();
			while (itr.hasNext()) {
				String valueActaul = actualMap.get(itr.next());
				String valueExpected = expectedMap.get(itr.next());
				if (valueActaul.equals(valueExpected)) {
					comparisonStatus = true;
				} else {
					comparisonStatus = false;
					break;
				}
			}
		} else {
			comparisonStatus = false;
		}
		return comparisonStatus;
	}

	public void assertEquals(Map<String, String> actualMap, Map<String, String> expectedMap, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		String formattedMessage = message + " :: Expected: " + expectedMap + " || " + "Actual: " + actualMap;

		boolean comparisonStatus = compareMaps(actualMap, expectedMap);

		if (comparisonStatus) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
		} else {
			testCaseExecutionFailedFlag = true;
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
		}
	}

	public void assertNotEquals(Map<String, String> actualMap, Map<String, String> expectedMap, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		String formattedMessage = message + " :: Expected: " + expectedMap + " || " + "Actual: " + actualMap;

		boolean comparisonStatus = compareMaps(actualMap, expectedMap);

		if (comparisonStatus) {
			testCaseExecutionFailedFlag = true;
			updateTestCaseStatusInMap(testCaseKey, "Fail");
			getScreenShot(message);
			ExtentTestManager.getTest().log(LogStatus.FAIL, formattedMessage);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			ExtentTestManager.getTest().log(LogStatus.PASS, formattedMessage);
		}
	}
}
