package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ReportsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ReportsTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ReportsPage objReportsPage;
	SoftAssertion softAssert  = new SoftAssertion();

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objReportsPage = new ReportsPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}
		
	@AfterMethod
	public void afterMethod() throws IOException, InterruptedException{
		objApasGenericFunctions.logout();
		softAssert.assertAll();
	}

	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business admin and appraisal support in an array
	 **/
	@DataProvider(name = "loginUsers")
	public Object[][] dataProviderLoginUserMethod() {
		return new Object[][] {{ users.BUSINESS_ADMIN } };
	}

	/**
	 Below test case will validate that user is able to export the report
	 **/
	@Test(description = "SMAB-T433: Validation for Reports Export", dataProvider = "loginUsers", groups = {"smoke","regression"}, priority = 0, alwaysRun = true)
	public void verifyReportExport(String loginUser) throws Exception {

		String downloadLocation = CONFIG.getProperty("downloadFolder");
		String reportName = "Building Permit by City Code";
		String exportedFileName;
		System.out.println("Download location : " + downloadLocation);

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Non Relevant Permit Settings module
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening Reports module");
		objApasGenericFunctions.searchModule(modules.REPORTS);

		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

		//Step3: Exporting 'Building Permit by City Code' report in Formatted Mode
		objReportsPage.exportReport(reportName,ReportsPage.FORMATTED_EXPORT);
		exportedFileName = Objects.requireNonNull(new File(downloadLocation).listFiles())[0].getName();
		softAssert.assertTrue(exportedFileName.contains("Building Permit by City Code"), "SMAB-T433: Exported report name validation");

		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

		//Step3: Exporting 'Building Permit by City Code' report in Data Mode
		objReportsPage.exportReport(reportName,ReportsPage.DATA_EXPORT);
		exportedFileName = Objects.requireNonNull(new File(downloadLocation).listFiles())[0].getName();
		softAssert.assertTrue(exportedFileName.contains("report"), "SMAB-T433: Exported report name validation");
	}
}
