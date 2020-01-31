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

public class CustomHardAssert {

	protected CustomHardAssert() {

	}

	private static void updateTestCaseStatusInMap(String testCaseKey, String testCaseStatus) {
		JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, testCaseStatus);
	}

	private static void extractTestCaseKey(String message) {
		String testCaseKey = message.substring(0, message.indexOf(":")).trim();
		System.setProperty("testCaseKey", testCaseKey);
	}

	static String format(Object actual, Object expected, String message) {
		String formatted = "";
		if (null != message) {
			formatted = message + " ";
		}
		return formatted + ASSERT_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
	}

	static private void fail(String message, String testCaseKey) {
		updateTestCaseStatusInMap(testCaseKey, "Fail");
		throw new AssertionError(message);
	}

	static private void failSame(Object actual, Object expected, String message) {
		String testCaseKey = System.getProperty("testCaseKey");
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + ASSERT_LEFT2 + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT, testCaseKey);
	}

	static private void failNotSame(Object actual, Object expected, String message) {
		String testCaseKey = System.getProperty("testCaseKey");
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + ASSERT_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT, testCaseKey);
	}

	static private void failNotEquals(Object actual, Object expected, String message, String testCaseKey) {
		fail(format(actual, expected, message), testCaseKey);
	}

	static public void assertNotNull(Object object, String message) {
		assertTrue(object != null, message);
	}

	static public void assertNull(Object object, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (object != null) {
			failNotSame(object, null, message);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	static public void assertSame(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (expected == actual) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}
		failNotSame(actual, expected, message);
	}

	static public void assertNotSame(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (expected == actual) {
			failSame(actual, expected, message);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	static public void assertTrue(boolean condition, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (!condition) {
			failNotEquals(condition, Boolean.TRUE, message, testCaseKey);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	static public void assertFalse(boolean condition, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (condition) {
			failNotEquals(condition, Boolean.TRUE, message, testCaseKey);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
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
		if (expected == null ^ actual == null) {
			failNotEquals(actual, expected, message, testCaseKey);
		}
		if (expected.equals(actual) && actual.equals(expected)) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}
		failNotEquals(actual, expected, message, testCaseKey);
	}

	public static void assertEquals(Object actual, Object expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (expected != null && expected.getClass().isArray()) {
			assertArrayEquals(actual, expected, message, testCaseKey);
			return;
		}
		assertEqualsImpl(actual, expected, message, testCaseKey);
	}

	static public void assertEquals(String actual, String expected, String message) {
		assertEquals((Object) actual, (Object) expected, message);
	}

	static public void assertEquals(long actual, long expected, String message) {
		assertEquals(Long.valueOf(actual), Long.valueOf(expected), message);
	}

	static public void assertEquals(boolean actual, boolean expected, String message) {
		assertEquals(Boolean.valueOf(actual), Boolean.valueOf(expected), message);
	}

	static public void assertEquals(byte actual, byte expected, String message) {
		assertEquals(Byte.valueOf(actual), Byte.valueOf(expected), message);
	}

	static public void assertEquals(char actual, char expected, String message) {
		assertEquals(Character.valueOf(actual), Character.valueOf(expected), message);
	}

	static public void assertEquals(short actual, short expected, String message) {
		assertEquals(Short.valueOf(actual), Short.valueOf(expected), message);
	}

	static public void assertEquals(int actual, int expected, String message) {
		assertEquals(Integer.valueOf(actual), Integer.valueOf(expected), message);
	}

	static public void assertEquals(double actual, double expected, double delta, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (Double.isInfinite(expected)) {
			if (!(expected == actual)) {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		} else if (Double.isNaN(expected)) {
			if (!Double.isNaN(actual)) {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		} else {
			if ((Math.abs(expected - actual) <= delta)) {
				updateTestCaseStatusInMap(testCaseKey, "Pass");
			} else {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		}
	}

	static public void assertEquals(float actual, float expected, float delta, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (Float.isInfinite(expected)) {
			if (!(expected == actual)) {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		} else {
			if ((Math.abs(expected - actual) <= delta)) {
				updateTestCaseStatusInMap(testCaseKey, "Pass");

			} else {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		}
	}

	static public void assertEquals(Object[] actual, Object[] expected, String message) {
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

	static public void assertEquals(Collection<?> actual, Collection<?> expected, String message) {
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

	static public void assertEqualsNoOrder(Object[] actual, Object[] expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if ((actual == null && expected != null) || (actual != null && expected == null)) {
			failAssertNoEqual("Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual), message);
		}

		if (actual.length != expected.length) {
			failAssertNoEqual("Arrays do not have the same size:" + actual.length + " != " + expected.length, message);
		}

		List<Object> actualCollection = Lists.newArrayList();
		for (Object a : actual) {
			actualCollection.add(a);
		}
		for (Object o : expected) {
			actualCollection.remove(o);
		}
		if (actualCollection.size() != 0) {
			failAssertNoEqual("Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual), message);
		} else {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
		}
	}

	private static void failAssertNoEqual(String defaultMessage, String message) {
		if (message != null) {
			fail(message);
		} else {
			fail(defaultMessage);
		}
	}

	// ************************* Modified Recently ****************************

	static public void assertEquals(Iterator<?> actual, Iterator<?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if (actual == null || expected == null) {
			if (message != null) {
				fail(message, testCaseKey);
			} else {
				fail("Iterators not equal: expected: " + expected + " and actual: " + actual, testCaseKey);
			}
		}

		int i = -1;
		while (actual.hasNext() && expected.hasNext()) {
			i++;
			Object e = expected.next();
			Object a = actual.next();
			String explanation = "Iterators differ at element [" + i + "]: " + e + " != " + a;
			String errorMessage = message == null ? explanation : message + ": " + explanation;

			assertEqualsImpl(a, e, errorMessage, testCaseKey);
		}

		if (actual.hasNext()) {
			String explanation = "Actual iterator returned more elements than the expected iterator.";
			String errorMessage = message == null ? explanation : message + ": " + explanation;
			fail(errorMessage, testCaseKey);
		} else if (expected.hasNext()) {
			String explanation = "Expected iterator returned more elements than the actual iterator.";
			String errorMessage = message == null ? explanation : message + ": " + explanation;
			fail(errorMessage, testCaseKey);
		}
	}

	static public void assertEquals(Iterable<?> actual, Iterable<?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if (actual == null || expected == null) {
			if (message != null) {
				fail(message, testCaseKey);
			} else {
				fail("Iterables not equal: expected: " + expected + " and actual: " + actual, testCaseKey);
			}
		}

		Iterator<?> actIt = actual.iterator();
		Iterator<?> expIt = expected.iterator();
		assertEquals(actIt, expIt, message);
	}

	public static void assertEquals(Set<?> actual, Set<?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if (actual == null || expected == null) {
			// Keep the back compatible
			if (message == null) {
				fail("Sets not equal: expected: " + expected + " and actual: " + actual, testCaseKey);
			} else {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		}

		if (!actual.equals(expected)) {
			if (message == null) {
				fail("Sets differ: expected " + expected + " but got " + actual, testCaseKey);
			} else {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		}
	}

	public static void assertEqualsDeep(Set<?> actual, Set<?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");

		if (actual == expected) {
			updateTestCaseStatusInMap(testCaseKey, "Pass");
			return;
		}

		if (actual == null || expected == null) {
			// Keep the back compatible
			if (message == null) {
				fail("Sets not equal: expected: " + expected + " and actual: " + actual, testCaseKey);
			} else {
				failNotEquals(actual, expected, message, testCaseKey);
			}
		}

		Iterator<?> actualIterator = actual.iterator();
		Iterator<?> expectedIterator = expected.iterator();
		while (expectedIterator.hasNext()) {
			Object expectedValue = expectedIterator.next();
			if (!actualIterator.hasNext()) {
				fail(message + ". Sets not equal: expected: " + expected + " and actual: " + actual, testCaseKey);
			}
			Object value = actualIterator.next();
			if (expectedValue.getClass().isArray()) {
				assertArrayEquals(value, expectedValue, message, testCaseKey);
			} else {
				assertEqualsImpl(value, expectedValue, message, testCaseKey);
			}
		}
	}

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

	public static void assertEqualsDeep(Map<?, ?> actual, Map<?, ?> expected, String message) {
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
			if (expectedValue.getClass().isArray()) {
				assertArrayEquals(value, expectedValue, assertMessage, testCaseKey);
			} else {
				assertEqualsImpl(value, expectedValue, assertMessage, testCaseKey);
			}
		}
	}

	// ********************* Modification Completed *********************

	public static void assertNotEquals(Object actual1, Object actual2, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		boolean fail;
		try {
			assertEquals(actual1, actual2, message);
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

	static void assertNotEquals(String actual1, String actual2, String message) {
		assertNotEquals((Object) actual1, (Object) actual2, message);
	}

	static void assertNotEquals(long actual1, long actual2, String message) {
		assertNotEquals(Long.valueOf(actual1), Long.valueOf(actual2), message);
	}

	static void assertNotEquals(boolean actual1, boolean actual2, String message) {
		assertNotEquals(Boolean.valueOf(actual1), Boolean.valueOf(actual2), message);
	}

	static void assertNotEquals(byte actual1, byte actual2, String message) {
		assertNotEquals(Byte.valueOf(actual1), Byte.valueOf(actual2), message);
	}

	static void assertNotEquals(char actual1, char actual2, String message) {
		assertNotEquals(Character.valueOf(actual1), Character.valueOf(actual2), message);
	}

	static void assertNotEquals(short actual1, short actual2, String message) {
		assertNotEquals(Short.valueOf(actual1), Short.valueOf(actual2), message);
	}

	static void assertNotEquals(int actual1, int actual2, String message) {
		assertNotEquals(Integer.valueOf(actual1), Integer.valueOf(actual2), message);
	}

	static public void assertNotEquals(float actual1, float actual2, float delta, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		boolean fail;
		try {
			assertEquals(actual1, actual2, delta, message);
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

	static public void assertNotEquals(double actual1, double actual2, double delta, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		boolean fail;
		try {
			assertEquals(actual1, actual2, delta, message);
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

	public static void assertNotEquals(Set<?> actual, Set<?> expected, String message) {
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

	public static void assertNotEqualsDeep(Set<?> actual, Set<?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		boolean fail;
		try {
			assertEqualsDeep(actual, expected, message);
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

	public static void assertNotEqualsDeep(Map<?, ?> actual, Map<?, ?> expected, String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		boolean fail;
		try {
			assertEqualsDeep(actual, expected, message);
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

	static public void fail(String message) {
		extractTestCaseKey(message);
		String testCaseKey = System.getProperty("testCaseKey");
		updateTestCaseStatusInMap(testCaseKey, "Fail");
		throw new AssertionError(message);
	}

	/**
	 * This interface facilitates the use of {@link #expectThrows} from Java 8.
	 * It allows method references to both void and non-void methods to be
	 * passed directly into expectThrows without wrapping, even if they declare
	 * checked exceptions.
	 * <p/>
	 * This interface is not meant to be implemented directly.
	 */
	private interface ThrowingRunnable {
		void run() throws Throwable;
	}

	/**
	 * Asserts that {@code runnable} throws an exception when invoked. If it
	 * does not, an {@link AssertionError} is thrown.
	 *
	 * @param runnable
	 *            A function that is expected to throw an exception when invoked
	 * @since 6.9.5
	 */
	private static void assertThrows(ThrowingRunnable runnable) {
		assertThrows(Throwable.class, runnable);
	}

	/**
	 * Asserts that {@code runnable} throws an exception of type
	 * {@code throwableClass} when executed. If it does not throw an exception,
	 * an {@link AssertionError} is thrown. If it throws the wrong type of
	 * exception, an {@code AssertionError} is thrown describing the mismatch;
	 * the exception that was actually thrown can be obtained by calling
	 * {@link AssertionError#getCause}.
	 *
	 * @param throwableClass
	 *            the expected type of the exception
	 * @param runnable
	 *            A function that is expected to throw an exception when invoked
	 * @since 6.9.5
	 */
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	private static <T extends Throwable> void assertThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
		expectThrows(throwableClass, runnable);
	}

	/**
	 * Asserts that {@code runnable} throws an exception of type
	 * {@code throwableClass} when executed and returns the exception. If
	 * {@code runnable} does not throw an exception, an {@link AssertionError}
	 * is thrown. If it throws the wrong type of exception, an {@code
	* AssertionError} is thrown describing the mismatch; the exception that was
	 * actually thrown can be obtained by calling
	 * {@link AssertionError#getCause}.
	 *
	 * @param throwableClass
	 *            the expected type of the exception
	 * @param runnable
	 *            A function that is expected to throw an exception when invoked
	 * @return The exception thrown by {@code runnable}
	 * @since 6.9.5
	 */
	private static <T extends Throwable> T expectThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			if (throwableClass.isInstance(t)) {
				return throwableClass.cast(t);
			} else {
				String mismatchMessage = String.format("Expected %s to be thrown, but %s was thrown",
						throwableClass.getSimpleName(), t.getClass().getSimpleName());

				throw new AssertionError(mismatchMessage, t);
			}
		}
		String message = String.format("Expected %s to be thrown, but nothing was thrown",
				throwableClass.getSimpleName());
		throw new AssertionError(message);
	}
}
