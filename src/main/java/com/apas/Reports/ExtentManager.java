package com.apas.Reports;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.relevantcodes.extentreports.LogStatus;
import org.testng.ITestContext;
import org.testng.Reporter;

import com.apas.TestBase.TestBase;
import com.relevantcodes.extentreports.ExtentReports;

public class ExtentManager {
	public static ExtentReports extent;
	int iCounter = 0;
	public static ITestContext context;
	public static ThreadLocal<ExtentReports> dr = new ThreadLocal<ExtentReports>();
	public static ThreadLocal<String> emaildirectoryPath = new ThreadLocal<String>();

	public synchronized ExtentReports getInstance(String SuiteName) {
		if (extent == null) {
			File outputDirectory = new File((context).getOutputDirectory());
			File resultDirectory = new File(outputDirectory.getParentFile(), "html");
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
			String upDate = sdf.format(date);
			String environment = System.getProperty("region").toUpperCase();
			String buildNumber = "";
			//This will add Jenkins Build Number in the report name
			if (System.getProperty("jenkinsbuild") != null){
				buildNumber = "_Build#" + System.getProperty("jenkinsbuild");
			}
			String resultFile = System.getProperty("user.dir") + "//test-output//AutomationReport//" + SuiteName + "_" + environment + buildNumber + "_" + upDate + ".html";
			extent = new ExtentReports(resultFile, true);
			// setEmaildirectoryPath(resultFile);
			extent.config().reportHeadline(", Env: " + environment);
			extent.config().reportName(SuiteName);
			Reporter.log("Extent Report directory: " + resultDirectory, true);
			// setExtent(extent);
		}
		return extent;
	}

	/**
	 * Description: This function will return the extent report instance
	 * @return : Returns the instance of extent report
	 */
	public static ExtentReports getExtentInstance() {
		return extent;
	}

	/**
	 * Description: Setting the email directory path
	 */
	public void setEmaildirectoryPath(String filepath) {
		emaildirectoryPath.set(filepath);
	}

	/**
	 * Description: This will return the pathe of the email directory
	 */
	public String getEmaildirectoryPath() {
		return emaildirectoryPath.get();
	}

	/**
	 * Description: This will set the context of the extent manager
	 */
	public static void setOutputDirectory(ITestContext context) {
		ExtentManager.context = context;
	}

	/**
	 * This function print the message in extent report based on flag
	 *  @param flag: True/False. Step will be passed or failed based on this flag
	 *  @param Message: Message to be printed on the extent report
	 *
	 */
	public static void reportLogger(boolean flag, String Message) {
		if (flag) {
			ExtentTestManager.getTest().log(LogStatus.PASS, "Test Step Passed :" + Message);
		} else {
			ExtentTestManager.getTest().log(LogStatus.FAIL, "Test Step Failed: " + Message);
		}
	}
}
