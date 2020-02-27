package com.apas.Assertions;

import static org.testng.internal.EclipseInterface.ASSERT_LEFT;
import static org.testng.internal.EclipseInterface.ASSERT_LEFT2;
import static org.testng.internal.EclipseInterface.ASSERT_MIDDLE;
import static org.testng.internal.EclipseInterface.ASSERT_RIGHT;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.collections.Lists;

import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;

public class HardAssertion {

	protected HardAssertion() {

	}
	
	/**
	 * Asserts that the given object is null, if not null AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param object: null
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertNull(Object object, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (object != null) {
			failNotSame(object, null, message);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	/**
	 * Asserts that two objects refer to the same object, if not AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: obj1 (TestClass Obj1 = new TestClass())
	 * @param expected: obj2 (obj2 = obj1)
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertSame(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (expected == actual) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}
		failNotSame(actual, expected, message);
	}

	/**
	 * Asserts that given condition is true, if not AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: true
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertTrue(boolean condition, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (!condition) {
			failNotEquals(condition, Boolean.TRUE, message, testCaseKey);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	/**
	 * Asserts that given objects are equal (basis on their values)
	 * If not AssertionError is thrown & updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: "Testing" or 2341 or false or 4512.22 or 'C'
	 * @param expected: "Testing" or 2341 or false or 4512.22 or 'C'
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertEquals(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (expected != null && expected.getClass().isArray()) {
			assertArrayEquals(actual, expected, message, testCaseKey);
			return;
		}
		assertEqualsImpl(actual, expected, message, testCaseKey);
	}

	/**
	 * Asserts that given array objects are equal (basis on their size, values & order of values)
	 * If not AssertionError is thrown & updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: {1,2,3,4,5} or {"Hello", "Java"} or {true, false, false, true}
	 * @param expected: {1,2,3,4,5} or {"Hello", "Java"} or {true, false, false, true}
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertEquals(Object[] actual, Object[] expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if ((actual == null && expected != null) || (actual != null && expected == null)) {
			fail(message + "Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual),
					testCaseKey);
		}
		assertEquals(Arrays.asList(actual), Arrays.asList(expected), message);
	}

	/**
	 * Asserts that given array objects are equal (basis on their size, values BUT not order of values)
	 * If not AssertionError is thrown & updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: {1,2,3,5,4} or {"Hello", "Java"} or {true, false, true, false}
	 * @param expected: {1,2,3,4,5} or {"Java", "Hello"} or {false, true, false, true}
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertEqualsNoOrder(Object[] actual, Object[] expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if ((actual == null && expected != null) || (actual != null && expected == null)) {
			failAssertNoEqual(testCaseKey, "Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual), message);
		}

		if (actual.length != expected.length) {
			failAssertNoEqual(testCaseKey, "Arrays do not have the same size:" + actual.length + " != " + expected.length, message);
		}

		List<Object> actualCollection = Lists.newArrayList();
		for (Object a : actual) {
			actualCollection.add(a);
		}
		for (Object o : expected) {
			actualCollection.remove(o);
		}
		if (actualCollection.size() != 0) {
			failAssertNoEqual(testCaseKey, "Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual), message);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	/**
	 * Asserts that given Collection types are equal, if not AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: List Object / Set Object
	 * @param expected: List Object / Set Object
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertEquals(Collection<?> actual, Collection<?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if (actual == null || expected == null) {
			fail(message + "Collections not equal: expected: " + expected + " and actual: " + actual, testCaseKey);
		}

		assertEquals(actual.size(), expected.size(), message + " :: Validating size of expected & actual objects.");

		Iterator<?> actIt = actual.iterator();
		Iterator<?> expIt = expected.iterator();
		int i = -1;
		while (actIt.hasNext() && expIt.hasNext()) {
			i++;
			Object e = expIt.next();
			Object a = actIt.next();
			String explanation = "Lists differ at element [" + i + "]: " + e + " != " + a;
			String errorMessage = message + " :: " + explanation;

			assertEqualsImpl(a, e, errorMessage, testCaseKey);
		}
	}

	/**
	 * Asserts that given Map types are equal, if not AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: Map object
	 * @param expected: Map object
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertEquals(Map<?, ?> actual, Map<?, ?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if (actual == null || expected == null) {
			fail(message + ". Maps not equal: expected: " + expected + " and actual: " + actual, testCaseKey);
		}

		if (actual.size() != expected.size()) {
			fail(message + ". Maps do not have the same size:" + actual.size() + " != " + expected.size(), testCaseKey);
		}

		Set<?> entrySet = actual.entrySet();
		for (Object anEntrySet : entrySet) {
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>) anEntrySet;
			Object key = entry.getKey();
			Object value = entry.getValue();
			Object expectedValue = expected.get(key);
			String assertMessage = message != null ? message : "Maps do not match for key:" + key + " actual:" + value + " expected:" + expectedValue;
			assertEqualsImpl(value, expectedValue, assertMessage, testCaseKey);
		}
	}
	
	/**
	 * Asserts that given condition is false, if not AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: false
	 * @param message: "SMAB-T418: <Some validation message>"
	 */	
	public static void assertFalse(boolean condition, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (condition) {
			failNotEquals(condition, Boolean.TRUE, message, testCaseKey);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	/**
	 * Asserts that the given object is not null, if null AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param object: Like-> String object ("Hello") or any other runtime generated object
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertNotNull(Object object, String message) {
		assertTrue(object != null, message);
	}

	/**
	 * Asserts that two objects don't refer to the same object, if they refer AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: obj1 (TestClass Obj1 = new TestClass())
	 * @param expected: obj2 (TestClass Obj2 = new TestClass())
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertNotSame(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (expected == actual) {
			failSame(actual, expected, message);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	/**
	 * Asserts that given objects are not equal (basis on their values), if equal AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: "Testing" or 2341 or false or 4512.22 or 'C'
	 * @param expected: "Testing Java" or 1239 or true or 12.22 or 'F'
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertNotEquals(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		boolean fail;
		try {
			assertEquals(actual, expected, message);
			fail = true;
		} catch (AssertionError e) {
			fail = false;
		}

		if (fail) {
			fail(message, testCaseKey);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	/**
	 * Asserts that given Map types are not equal, if equal AssertionError is thrown
	 * Updates the test case status in test case map as pass or fail
	 * 
	 * @param actual: Map object
	 * @param expected: Map object
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static void assertNotEquals(Map<?, ?> actual, Map<?, ?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		boolean fail;
		try {
			assertEquals(actual, expected, message);
			fail = true;
		} catch (AssertionError e) {
			fail = false;
		}

		if (fail) {
			fail(message, testCaseKey);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	
	/**
	 * Below methods are for internal calling within the class, hence kept private.
	 */

	private static void updateTestCaseStatusInMap(String testCaseKey, String testCaseStatus) {
		JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, testCaseStatus);
	}

	private static void extractTestCaseKey(String message) {
		String testCaseKey = message.substring(0, message.indexOf(":")).trim();
		System.setProperty("testCaseKey", testCaseKey);
	}

	private static String format(Object actual, Object expected, String message) {
		String formatted = "";
		if (null != message) {
			formatted = message + " ";
		}
		return formatted + ASSERT_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
	}

	private static void fail(String message, String testCaseKey) {
		updateTestCaseStatusInMap(testCaseKey, "Fail");
		throw new AssertionError(message);
	}

	private static void failSame(Object actual, Object expected, String message) {
		String testCaseKey = System.getProperty("testCaseKey");
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + ASSERT_LEFT2 + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT, testCaseKey);
	}

	private static void failNotSame(Object actual, Object expected, String message) {
		String testCaseKey = System.getProperty("testCaseKey");
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + ASSERT_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT, testCaseKey);
	}

	private static void failNotEquals(Object actual, Object expected, String message, String testCaseKey) {
		fail(format(actual, expected, message), testCaseKey);
	}

	private static void assertArrayEquals(Object actual, Object expected, String message, String testCaseKey) {
		if (expected == actual) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}
		if (null == expected) {
			fail(message + " Expected a null array, but not null found. ", testCaseKey);
		}
		if (null == actual) {
			fail(message + " Expected not null array, but null found. ", testCaseKey);
		}
		// This if section is called only when expected input is an array.
		if (actual.getClass().isArray()) {
			int expectedLength = Array.getLength(expected);
			if (expectedLength == Array.getLength(actual)) {
				for (int i = 0; i < expectedLength; i++) {
					Object _actual = Array.get(actual, i);
					Object _expected = Array.get(expected, i);
					try {
						assertEquals(_actual, _expected, message);
					} catch (AssertionError ae) {
						failNotEquals(actual, expected, message + " (values not matched at index " + i + ")",
								testCaseKey);
					}
				}
				// All the values of given array and expected array have
				// matched.
				updateTestCaseStatusInMap(testCaseKey, "Pass");
				return;
			} else {
				failNotEquals(Array.getLength(actual), expectedLength, message + " (Array lengths not same)",
						testCaseKey);
			}
		}
		failNotEquals(actual, expected, message, testCaseKey);
	}

	private static void assertEqualsImpl(Object actual, Object expected, String message, String testCaseKey) {
		if ((expected == null) && (actual == null)) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}
		if (expected == null || actual == null) {
			failNotEquals(actual, expected, message, testCaseKey);
		}
		if (expected.equals(actual) && actual.equals(expected)) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}
		failNotEquals(actual, expected, message, testCaseKey);
	}

	private static void failAssertNoEqual(String testCaseKey, String defaultMessage, String message) {
		if (message != null) {
			fail(message, testCaseKey);
		} else {
			fail(defaultMessage, testCaseKey);
		}
	}
}
