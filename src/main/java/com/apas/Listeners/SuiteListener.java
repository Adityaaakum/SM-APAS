package com.apas.Listeners;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.apas.FailedCasesExecution.FailedCasesXmlGenerator;
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
	private Util objUtil;
	private Map<String, List<String>> classesAndMethodsMap = new HashMap<String, List<String>>();

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
				JiraAdaptavistStatusUpdate.mapTestCaseStatusToJIRA();
			}
			objUtil = new Util();
			objUtil.migrateOldReportsToAcrhive();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onFinish(ITestContext context) {
		// TearDown();
		if(classesAndMethodsMap != null) {
			FailedCasesXmlGenerator objFailedCasesXmlGenerator = new FailedCasesXmlGenerator(classesAndMethodsMap);
			objFailedCasesXmlGenerator.generateFailedCasesXml();
			try {
				objFailedCasesXmlGenerator.runFailedTestCasesXml();
				//objFailedCasesXmlGenerator.deleteExistingXml();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
			String description = "Description: " + result.getMethod().getDescription();
			System.setProperty("currentMethodName", methodName);
			System.setProperty("description", description);
			
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
		if(SoftAssertion.isSoftAssertionUsedFlag == null || !(SoftAssertion.isSoftAssertionUsedFlag)) {
			System.out.println("isSoftAssertionUsedFlag: " + SoftAssertion.isSoftAssertionUsedFlag);
			ExtentTestManager.getTest().log(LogStatus.PASS, "Test Case has been PASSED.");
			ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
			ExtentManager.getExtentInstance().flush();
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String className = result.getInstanceName();
		String methodName = result.getMethod().getMethodName();
	
		if(!(classesAndMethodsMap.containsKey(className))) {
			List<String> datList = new ArrayList<String>();
			datList.add(methodName);
			classesAndMethodsMap.put(className, datList);	
		} else {
			List<String> existingDataList = classesAndMethodsMap.get(className);
			existingDataList.add((existingDataList.size() - 1), methodName);
			classesAndMethodsMap.put(className, existingDataList);	
		}
				
		try {
			if(SoftAssertion.isSoftAssertionUsedFlag == null || !(SoftAssertion.isSoftAssertionUsedFlag)) {
				System.out.println("isSoftAssertionUsedFlag: " + SoftAssertion.isSoftAssertionUsedFlag);
				RemoteWebDriver ldriver = BrowserDriver.getBrowserInstance();
	
				TakesScreenshot ts = (TakesScreenshot) ldriver;
				File source = ts.getScreenshotAs(OutputType.FILE);
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
				String upDate = sdf.format(date);
				String dest = System.getProperty("user.dir") + "//test-output//ErrorScreenshots//" + methodName + upDate + ".png";
				File destination = new File(dest);
				FileUtils.copyFile(source, destination);
	
				ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
				ExtentTestManager.getTest().log(LogStatus.INFO, "Snapshot below: " + upTest.addScreenCapture(dest));
				ExtentManager.getExtentInstance().endTest(ExtentTestManager.getTest());
				ExtentManager.getExtentInstance().flush();
			}
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
