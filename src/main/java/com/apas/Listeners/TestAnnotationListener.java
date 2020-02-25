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
			System.out.println(" :Beforeclass step Instance");
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
			System.out.println("TearDown step");
			TearDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
