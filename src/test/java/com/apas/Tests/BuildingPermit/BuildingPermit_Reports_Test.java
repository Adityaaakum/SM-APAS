package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ReportsPage;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.File;
import java.util.Objects;

public class BuildingPermit_Reports_Test extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ReportsPage objReportsPage;
	SoftAssertion softAssert  = new SoftAssertion();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception {
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objReportsPage = new ReportsPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}

	/*
	 Below test case will validate that user is able to export the report
	 */
	@Test(description = "SMAB-T433,SMAB-T373: Validation for Reports Export", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"smoke","regression"})
	public void verify_BuildingPermit_ReportExport(String loginUser) throws Exception {

		String downloadLocation = CONFIG.getProperty("downloadFolder");
		String reportName = "Building Permit by City Code";
		String exportedFileName;
		System.out.println("Download location : " + downloadLocation);

		//Step1: Login to the APAS application using the credentials passed through data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Reports module
		objApasGenericFunctions.searchModule(modules.REPORTS);

		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

		//Step3: Exporting 'Building Permit by City Code' report in Formatted Mode
		objReportsPage.exportReport(reportName,ReportsPage.FORMATTED_EXPORT);
		exportedFileName = Objects.requireNonNull(new File(downloadLocation).listFiles())[0].getName();
		softAssert.assertTrue(exportedFileName.contains("Building Permit by City Code"), "SMAB-T433,SMAB-T373: Exported report name validation. Report name should contain 'Building Permit by City Code' in formatted export. Actual Report Name : " + exportedFileName);
		softAssert.assertEquals(exportedFileName.split("\\.")[1],"xlsx", "SMAB-T433,SMAB-T373: Exported data formatted report should be in XLSX");

		//Step2: Opening the Reports module
		objApasGenericFunctions.searchModule(modules.REPORTS);

		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

		//Step3: Exporting 'Building Permit by City Code' report in Data Mode
		objReportsPage.exportReport(reportName,ReportsPage.DATA_EXPORT);
		exportedFileName = Objects.requireNonNull(new File(downloadLocation).listFiles())[0].getName();
		softAssert.assertTrue(exportedFileName.contains("report"), "SMAB-T433,SMAB-T373: Exported report name validation. Report name should contain the word 'Report' in case of data format. Actual Report Name : " + exportedFileName);
		softAssert.assertEquals(exportedFileName.split("\\.")[1],"xls", "SMAB-T433,SMAB-T373: Exported data formatted report should be in XLS");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
}
