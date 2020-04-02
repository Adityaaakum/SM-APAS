package com.apas.Listeners;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;
import com.apas.Reports.ExtentManager;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class SuiteListener extends TestBase implements ITestListener {

	public ExtentReports extent;
	ExtentTest upTest;

	protected String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * Description: This method will be executed before the suite starts
	 * @param context: Object of ITestContext
	 */
	@Override
	public void onStart(ITestContext context) {
		ExtentManager.setOutputDirectory(context);
		CONFIG = new Properties();
		try {
			
			TestBase.loadPropertyFiles();
			extent = new ExtentManager().getInstance(context.getSuite().getName());
			if (flagToUpdateJira && testCycle != null) {
				System.out.println("Test cases map on start of execution: " + JiraAdaptavistStatusUpdate.testStatus);
				JiraAdaptavistStatusUpdate.retrieveJiraTestCases();
				JiraAdaptavistStatusUpdate.mapTestCaseStatusToJIRA();
			}

			//This will move old report to archive folder
			new Util().migrateOldReportsToAcrhive();;

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Description: This method will be executed after the suite finished
	 * @param context: Object of ITestContext
	 */
	@Override
	public void onFinish(ITestContext context) {
		if (flagToUpdateJira && testCycle != null) {
			//Updating the Jira tickets status at the end of the execution
			System.out.println("Test cases map on end of execution: " + JiraAdaptavistStatusUpdate.testStatus);
			JiraAdaptavistStatusUpdate.mapTestCaseStatusToJIRA();
		}
	}

	/**
	 * Description: This method will be executed on the start of test case
	 * @param result: Object of ITestResult
	 */
	@Override
	public void onTestStart(ITestResult result) {
		try {
			//Updating the system properties with test case properties details
			String methodName = result.getMethod().getMethodName();
			String className = result.getMethod().toString();
			String description = result.getMethod().getDescription();
			System.setProperty("currentMethodName", methodName);
			System.setProperty("description", description);

			upTest = ExtentTestManager.startTest(methodName, "Description: " + description);
			System.out.println("Starting the test with method name : " + methodName);
			String[] arrayClassName = className.split("\\.");
			String upClassname = arrayClassName[0].replace("Test", "");
			upTest.assignCategory(upClassname);
			System.out.println("Environment URL:" + envURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Description: This method will be executed if the test case is passed
	 * @param result: Object of ITestResult
	 */
	@Override
	public void onTestSuccess(ITestResult result) {
		//Updating the extent report with the step that test case is passed.
		if (!SoftAssertion.isSoftAssertionUsedFlag) {
			ExtentTestManager.getTest().log(LogStatus.PASS, "Test Case has been PASSED.");
			ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
			ExtentManager.getExtentInstance().flush();
		} else{
			ExtentTestManager.getTest().log(LogStatus.PASS, "Test Case has been FAILED.");
		}
	}

	/**
	 * Description: This method will be executed if the test case is Failed
	 * It would updated the test cases as Fail in test case map
	 * if any exception other than AssertionError has occurred
	 * 
	 * @param result: Object of ITestResult
	 */
	private void updateTestCaseMapOnExceptionOtherThanAssertionError(ITestResult result) {
		String description = System.getProperty("description");			
		String[] descriptionArr = description.split(":");			
		String testCaseKey;
		
		if(descriptionArr[0].contains(",")) {
			String[] testCaseIdArr = descriptionArr[0].split(",");			
			for(int i = 0; i < testCaseIdArr.length; i++) {
				testCaseKey = testCaseIdArr[i];
				String currentStatus = JiraAdaptavistStatusUpdate.testStatus.get(testCaseKey);
				if((currentStatus == null) || !(currentStatus.equalsIgnoreCase("Fail"))) {
					JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, "Fail");
				}	
			}
		} else {
			testCaseKey = descriptionArr[0];
			JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, "Fail");
		}
	}
	
	/**
	 * Description: This method will be executed if the test case is Failed
	 * @param result: Object of ITestResult
	 */
	@Override
	public void onTestFailure(ITestResult result) {
		System.out.println("Method Failed:" + result.getMethod().getMethodName());
		try {

			RemoteWebDriver ldriver = BrowserDriver.getBrowserInstance();

			//Taking the screenshot as the test case is failed
			File source = ((TakesScreenshot) ldriver).getScreenshotAs(OutputType.FILE);
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
			String upDate = sdf.format(date);
			String dest = System.getProperty("user.dir") + "//test-output//ErrorScreenshots//"
					+ result.getMethod().getMethodName() + upDate + ".png";
			File destination = new File(dest);
			FileUtils.copyFile(source, destination);

			if(!(result.getThrowable() instanceof AssertionError)) {
				updateTestCaseMapOnExceptionOtherThanAssertionError(result);
			}

			ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
			//Adding the screenshot to the report
			ExtentTestManager.getTest().log(LogStatus.INFO, "Snapshot below: " + upTest.addScreenCapture(dest));
			//Finishing the test case
			ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
			ExtentManager.getExtentInstance().flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Description: This method will be executed if the test case is Skipped
	 * @param result: Object of ITestResult
	 */
	@Override
	public void onTestSkipped(ITestResult result) {
		String methodName = result.getMethod().getMethodName();
		System.out.println("Method Skipped : " + methodName);
		upTest = ExtentTestManager.startTest(methodName, "Description: " + result.getMethod().getDescription());

		ExtentTestManager.getTest().log(LogStatus.SKIP, "Test skipped : " + result.getThrowable());
		ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
		ExtentManager.getExtentInstance().flush();
		TearDown();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
		// TODO Auto-generated method stub
	}
}
