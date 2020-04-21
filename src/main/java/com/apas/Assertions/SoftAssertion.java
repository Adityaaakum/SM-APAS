package com.apas.Assertions;

import com.apas.Utils.Util;
import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;
import com.apas.Reports.ExtentManager;
import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;

public class SoftAssertion {

	Boolean testCaseExecutionFailedFlag = false;
	public static Boolean isSoftAssertionUsedFlag = null;
	Util objUtil = new Util();
	JiraAdaptavistStatusUpdate jiraAdaptavistStatusUpdate = new JiraAdaptavistStatusUpdate();

	/**
	 * Class constructor to set the isSoftAssertionUsedFlag variable as false
	 * Initially this variable is null, when class object is created it sets to false
	 */
	public SoftAssertion() {
		isSoftAssertionUsedFlag = false;
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
	 * @param condition: true/false
	 * @param message: "<Jira ID>: <Some validation message>"
	 */
	public void assertTrue(final boolean condition, String message) {
		if (!message.contains(":: Expected:"))
			message = message + " :: Expected: true" + " || " + "Actual: " + condition;

		String testCaseKey = JiraAdaptavistStatusUpdate.extractTestCaseKey(message);
		if (condition) {
			ExtentTestManager.getTest().log(LogStatus.PASS, message);
			JiraAdaptavistStatusUpdate.updateTestCaseStatusInMap(testCaseKey, "Pass");
		} else {
			ExtentTestManager.getTest().log(LogStatus.FAIL, message);
			JiraAdaptavistStatusUpdate.updateTestCaseStatusInMap(testCaseKey, "Fail");
			objUtil.getScreenShot(message);
			testCaseExecutionFailedFlag = true;
		}
	}

	/**
	 * Asserts that given actual and expected value and adds the message passed in message parameter
	 * Updates the test case status in test case map as pass or fail
	 * Takes screen shot on failure and sets testCaseExecutionFailedFlag variable as true
	 *
	 * @param actual: actual value received from the APAS
	 * @param expected: expected value passed in the test case
	 * @param message: "<Jira ID>: <Some validation message>"
	 */
	public void assertEquals(Object actual, Object expected, String message) {
		String formattedMessage = message + " :: Expected: " + expected + " || " + "Actual: " + actual;
		assertTrue(actual.equals(expected), formattedMessage);
	}

}	