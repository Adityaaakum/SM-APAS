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

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;
import com.apas.Reports.ExtentManager;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
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

	@Override
	public void onStart(ITestContext context) {
		ExtentManager.setOutputDirectory(context);
		CONFIG = new Properties();
		try {
			TestBase.loadPropertyFiles();
			// setupTest();

			extent = new ExtentManager().getInstance(context.getSuite().getName());
			if (flagToUpdateJira && testCycle != null) {
				System.out.println("Test cases map on start of execution: " + JiraAdaptavistStatusUpdate.testStatus);
				JiraAdaptavistStatusUpdate.retrieveJiraTestCases();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onFinish(ITestContext context) {
		// TearDown();
		
		if (flagToUpdateJira && testCycle != null) {
			System.out.println("Test cases map on end of execution: " + JiraAdaptavistStatusUpdate.testStatus);
			JiraAdaptavistStatusUpdate.mapTestCaseStatusToJIRA();
		}
	}

	@Override
	public void onTestStart(ITestResult result) {
		try {
			String methodName = result.getMethod().getMethodName();
			String className = result.getMethod().toString();

			upTest = ExtentTestManager.startTest(methodName, "Description: " + result.getMethod().getDescription());
			String[] arrayClassName = className.split("\\.");
			String upClassname = arrayClassName[0].replace("Test", "");
			upTest.assignCategory(upClassname);
			System.out.println("Environment URL:" + envURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		// JiraAdaptavistStatusUpdate.testStatus.put(result.getName().toString(), "Pass");
		// JiraAdaptavistStatusUpdate.updateTestCaseStatus(result.getName().toString(), "Pass");

		ExtentTestManager.getTest().log(LogStatus.PASS, "Test Case has been PASSED.");
		ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
		ExtentManager.getExtentInstance().flush();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		try {
			// JiraAdaptavistStatusUpdate.testStatus.put(result.getName().toString(), "Fail");
			// JiraAdaptavistStatusUpdate.updateTestCaseStatus(result.getName().toString(), "Fail");
			RemoteWebDriver ldriver = BrowserDriver.getBrowserInstance();

			TakesScreenshot ts = (TakesScreenshot) ldriver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
			String upDate = sdf.format(date);
			String dest = System.getProperty("user.dir") + "//test-output//ErrorScreenshots//"
					+ result.getMethod().getMethodName() + upDate + ".png";
			File destination = new File(dest);
			FileUtils.copyFile(source, destination);

			ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));

			ExtentTestManager.getTest().log(LogStatus.INFO, "Snapshot below: " + upTest.addScreenCapture(dest));
			ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
			ExtentManager.getExtentInstance().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String methodName = result.getMethod().getMethodName();
		upTest = ExtentTestManager.startTest(methodName, "Description: " + result.getMethod().getDescription());

		ExtentTestManager.getTest().log(LogStatus.SKIP, "Test skipped " + result.getThrowable());
		ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
		ExtentManager.getExtentInstance().flush();
		TearDown();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
		// TODO Auto-generated method stub
	}
}
