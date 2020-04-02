package com.apas.Listeners;

import org.testng.IClassListener;
import org.testng.ITestClass;
import com.apas.TestBase.TestBase;

public class TestAnnotationListener extends TestBase implements IClassListener {

	/**
	 * Description: This method will be used before each test class
	 * @param testClass: Object of ITestClass
	 */
	@Override
	public void onBeforeClass(ITestClass testClass) {
		try {
			System.out.println("Before Class Steps for Class " + testClass.getName());
			setupTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Description: This method will be used after each test class
	 * @param testClass: Object of ITestClass
	 */
	@Override
	public void onAfterClass(ITestClass testClass) {
		try {
			System.out.println("Tear Down (After Class) step for class " + testClass.getName());
			TearDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
