package com.apas.Assertions;

import static org.testng.internal.EclipseInterface.ASSERT_LEFT;
import static org.testng.internal.EclipseInterface.ASSERT_MIDDLE;
import static org.testng.internal.EclipseInterface.ASSERT_RIGHT;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.collections.Maps;

import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;

public class HardAssertions extends Assert {

	private static boolean softAssertionsFlag = false;
	private static Map<String, String> failedAssertionsMap = null;
	List<Boolean> testCaseStatusList = new ArrayList<Boolean>();

	public HardAssertions() {

	}

	protected HardAssertions(boolean softAssertionsFlag) {
		HardAssertions.softAssertionsFlag = softAssertionsFlag;
		HardAssertions.failedAssertionsMap = Maps.newLinkedHashMap();
	}

	private static void updateTestCaseStatusInMap(String testCaseName, String testCaseStatus) {
		int startIndex = testCaseName.indexOf("$") + 1;
		String testCaseKey = testCaseName.substring(startIndex, testCaseName.length()).replace("_", "-");
		JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, testCaseStatus);
	}

	private static String extractTestCaseKey(String message) {
		String testCaseKey = message.substring(0, message.indexOf(":")).trim();
		return testCaseKey;
	}

	public void assertTrueCondition(boolean condition, String message) {
		String testCaseKey = extractTestCaseKey(message);
		if (!condition) {
			failForTrue(condition, Boolean.TRUE, message, testCaseKey);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	private void failForTrue(Object actual, Object expected, String message, String testCaseKey) {
		fail(format(actual, expected, message, testCaseKey), testCaseKey);
	}

	private String format(Object actual, Object expected, String message, String testCaseKey) {
		String formatted = "";
		if (null != message) {
			formatted = message + " ";
		}
		return formatted + ASSERT_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
	}

	private void fail(String message, String testCaseKey) {
		updateTestCaseStatusInMap(testCaseKey, "Fail");
		if (softAssertionsFlag) {
			failedAssertionsMap.put(testCaseKey, message);
		} else {
			throw new AssertionError(message);
		}
	}

	public Map<String, String> getAssertionMap() {
		return HardAssertions.failedAssertionsMap;
	}
}
