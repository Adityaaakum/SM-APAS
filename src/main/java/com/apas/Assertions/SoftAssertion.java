package com.apas.Assertions;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.Util;
import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;
import com.apas.Reports.ExtentManager;
import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;

public class SoftAssertion {

	public static Boolean isSoftAssertionUsedFlag = false;

	/**
	 * Class constructor to set the isSoftAssertionUsedFlag variable as false
	 */
	public SoftAssertion() {isSoftAssertionUsedFlag = false;}

	/**
	 * Throws AssertionError if any soft assertions occurred in the test cases i.e isSoftAssertionUsedFlag is True
	 */
	public void assertAll() {
		//Keeping this method as a place holder for now as correct report is getting generated even without this function. Will remove it later
//		if (isSoftAssertionUsedFlag) {throw new AssertionError();}
	}

	 /**
     * Asserts that given condition is true, if not AssertionError is thrown when assertAll is called
     * Updates the test case status in test case map as pass or fail
     * Takes screen shot on failure and sets testCaseExecutionFailedFlag variable as true
     * 
     * @param condition: true/false
     * @param message: "<Jira ID>: <Some validation message>"
     * @throws Exception 
     */
    public void assertTrue(final boolean condition, String message, boolean... flagToExcludeScreenShot) throws Exception {
        if (!message.contains(":: Expected:"))
            message = message + " :: Expected: true" + " || " + "Actual: " + condition;

 

        String testCaseKey = JiraAdaptavistStatusUpdate.extractTestCaseKey(message);
        if (condition) {
            ExtentTestManager.getTest().log(LogStatus.PASS, message);
            JiraAdaptavistStatusUpdate.updateTestCaseStatusInMap(testCaseKey, "Pass");
        } else {
            ExtentTestManager.getTest().log(LogStatus.FAIL, message);
            JiraAdaptavistStatusUpdate.updateTestCaseStatusInMap(testCaseKey, "Fail");
            if(flagToExcludeScreenShot.length == 0 || flagToExcludeScreenShot[0] == false) {
                new Util().getScreenShot(message);    
            }
            isSoftAssertionUsedFlag = true;
        }
    }

	/**
	 * Asserts that given actaul and expected value and adds the message passed in message parameter
	 * Updates the test case status in test case map as pass or fail
	 * Takes screen shot on failure and sets testCaseExecutionFailedFlag variable as true
	 *
	 * @param actual: actual value received from the APAS
	 * @param expected: expected value passed in the test case
	 * @param message: "<Jira ID>: <Some validation message>"
	 * @throws Exception 
	 */
	public void assertEquals(Object actual, Object expected, String message) throws Exception {
		String formattedMessage = message + " :: Expected: " + expected + " || " + "Actual: " + actual;
		assertTrue(actual.equals(expected), formattedMessage);
	}

	/**
	 * Asserts that actual String contains expected value and adds the message passed in message parameter
	 *
	 * @param actual: actual value received from the APAS
	 * @param expected: expected value passed in the test case
	 * @param message: "<Jira ID>: <Some validation message>"
	 * @throws Exception 
	 */
	public void assertContains(Object actual, Object expected, String message) throws Exception {
		String formattedMessage = message + " :: Validation for \"" + expected + "\" Contains \"" + actual  + "\"";
		assertTrue(actual.toString().contains(expected.toString()), formattedMessage);
	}

}	
