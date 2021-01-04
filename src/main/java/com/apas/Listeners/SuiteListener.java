package com.apas.Listeners;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
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
import com.apas.Utils.PasswordUtils;
import com.apas.Utils.Util;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class SuiteListener extends TestBase implements ITestListener {

	public ExtentReports extent;
	ExtentTest upTest;
	Util objUtils = new Util();
	SalesforceAPI salesforceAPI = new SalesforceAPI();

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
		//killing chrome driver process if there is any process left from previous regressions
		if(System.getProperty("os.name").contains("Windows")) {
			try {				
				Process process = Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
				process.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ExtentManager.setOutputDirectory(context);
		CONFIG = new Properties();
		try {
			TestBase.loadPropertyFiles();
			extent = new ExtentManager().getInstance(context.getSuite().getName());
			if (flagToUpdateJira && testCycle != null) {
				JiraAdaptavistStatusUpdate.retrieveJiraTestCases();
			}
			//This will move old report to archive folder
			objUtils.migrateOldReportsToAcrhive();;
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
			System.out.println("Test cases execution status on end of execution: " + JiraAdaptavistStatusUpdate.testStatus);
			//This will create the file with the test case execution status
			objUtils.writeHashMapToCsv(JiraAdaptavistStatusUpdate.testStatus,ExtentManager.testCaseMappingFile);
			TearDown();
			JiraAdaptavistStatusUpdate.mapTestCaseStatusToJIRA();
			JiraAdaptavistStatusUpdate.uploadAttachmentInJira(ExtentManager.resultFile);
		}
	}

	/**
	 * Description: This method will be executed on the start of test case
	 * @param result: Object of ITestResult
	 */
	@Override
	public void onTestStart(ITestResult result) {
		try {
			if (CONFIG.getProperty("deleteWorkItemsFlag").equals("true")){
				System.out.println("Deleting the work items for the age greater than 1");
				salesforceAPI.deleteWorkItemsBasedOnAge(1);
				CONFIG.setProperty("deleteWorkItemsFlag","false");
			}
			//Making isSoftAssertionUsedFlag flag as False to reset soft assertion status
			SoftAssertion.isSoftAssertionUsedFlag = false;
			//Updating the system properties with test case properties details
			String methodName = result.getMethod().getMethodName();
			String className = result.getMethod().toString();
			String description = result.getMethod().getDescription();
			System.setProperty("currentMethodName", methodName);
			System.setProperty("description", description);
			upTest = ExtentTestManager.startTest(methodName, "Description: " + description);
			System.out.println("Starting the test with method name : " + methodName);
			String[] arrayClassName = className.split("\\.");
			String upClassname = arrayClassName[0];
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
		
		System.out.print("This is onSuccess Listner method");
		//Updating the extent report with the step that test case is passed.
		if (!SoftAssertion.isSoftAssertionUsedFlag) {
			ExtentTestManager.getTest().log(LogStatus.PASS, "Test Case has been PASSED.");
		} else{
			ExtentTestManager.getTest().log(LogStatus.FAIL, "Test Case has been FAILED.");
			failedMethods.add(result.getMethod().getMethodName());
		}
		ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
		ExtentManager.getExtentInstance().flush();
		TearDown();
	}

	/**
	 * Description: This method will be executed if the test case is Failed
	 * @param result: Object of ITestResult
	 */
	@Override
	public void onTestFailure(ITestResult result) {
		System.out.println("Method Failed:" + result.getMethod().getMethodName());
		try {
			//Updating the test case status for Jira.
			String testCaseKeys =  JiraAdaptavistStatusUpdate.extractTestCaseKey(System.getProperty("description"));
			JiraAdaptavistStatusUpdate.updateTestCaseStatusInMap(testCaseKeys,"Fail");

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

			ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
			//Adding the screenshot to the report
			File imageFile = new File(dest);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Snapshot below: " + upTest.addScreenCapture(objUtils.encodeFileToBase64Binary(imageFile)));

			//Finishing the test case
			ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
			ExtentManager.getExtentInstance().flush();
			TearDown();
//			setupTest();

		} catch (Exception e) {
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
		upTest = ExtentTestManager.startTest(methodName, "Description: " + result.getMethod().getDescription());
        ReportLogger.SKIP("Method Skipped : " + methodName);
        ReportLogger.SKIP("Test skipped : " + result.getThrowable());
        ReportLogger.SKIP(methodName + "Method Skipped because following parent method failed on which this method depends : " + result.getMethod().getMethodsDependedUpon());
		ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
		ExtentManager.getExtentInstance().flush();
		TearDown();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
		// TODO Auto-generated method stub
	}
}