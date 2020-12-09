package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ReportsPage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.ExcelUtils;
import com.apas.config.modules;
import com.apas.config.testdata;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BuildingPermit_Reports_Test extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericPage objApasGenericPage;
	ReportsPage objReportsPage;
	SoftAssertion softAssert  = new SoftAssertion();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception {
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objReportsPage = new ReportsPage(driver);
	}

	/*
	 Below test case will validate that user is able to export the report
	 */
	@Test(description = "SMAB-T433,SMAB-T373: Validation for Building Permit Reports Export and exported format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"smoke","regression"})
	public void BuildingPermit_ReportExport(String loginUser) throws Exception {

		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		String reportName = "Building Permit by City Code";
		String exportedFileName;
		ReportLogger.INFO("Download location : " + downloadLocation);

		//Step1: Login to the APAS application using the credentials passed through data provider
		objApasGenericPage.login(loginUser);

		//Step2: Opening the Reports module
		objApasGenericPage.searchModule(modules.REPORTS);

		//Deleteing all the previously downloaded files
		objApasGenericPage.deleteFilesFromFolder(downloadLocation);

		//Step3: Exporting 'Building Permit by City Code' report in Formatted Mode
		objReportsPage.exportReport(reportName,ReportsPage.FORMATTED_EXPORT);
		File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		exportedFileName = downloadedFile.getName();
		softAssert.assertTrue(exportedFileName.contains("Building Permit by City Code"), "SMAB-T433,SMAB-T373: Exported report name validation. Report name should contain 'Building Permit by City Code' in formatted export. Actual Report Name : " + exportedFileName);
		softAssert.assertEquals(exportedFileName.split("\\.")[1],"xlsx", "SMAB-T433,SMAB-T373: Exported data formatted report should be in XLSX");

		//Step4: Columns validation in exported report
		HashMap<String, ArrayList<String>> hashMapExcelData = ExcelUtils.getExcelSheetData(downloadedFile.getAbsolutePath(),0,10,1);
		String expectedColumnsInExportedExcel = "[Created Date, Permit City Code, Building Permit Number, APN: APN, Issue Date(YYYYMMDD), Completion Date(YYYYMMDD), Reissue, City APN, City Strat Code, Building Permit Fee, Work Description, Square Footage, Estimated Project Value, Application Name, Owner Name, Owner Address Line 1, Owner Address Line 2, Owner Address Line 3, Owner State, Owner Zip Code, Owner Phone Number, Contractor Name, Contractor Phone]";
		softAssert.assertEquals(hashMapExcelData.keySet().toString(),expectedColumnsInExportedExcel,"SMAB-T433: Columns Validation in downloaded building permit report");

		//Step5: Validation that Building Permit Number APN columns dispalyed on the report should be links
		driver.switchTo().frame(0);
		softAssert.assertTrue(objPage.verifyElementExists(objReportsPage.linkBuildingPermitNumber),"SMAB-T433: Validation that Building Permit Number displayed on report should be a link");
		//Removing below validation as APN value can be blank and the report pull the data dynamically
		//softAssert.assertTrue(objPage.verifyElementExists(objReportsPage.linkAPN),"SMAB-T433: Validation that APN displayed on report should be a link");
		driver.switchTo().parentFrame();

		//Step5: Opening the Reports module
		objApasGenericPage.searchModule(modules.REPORTS);

		//Deleting all the previously downloaded files
		objApasGenericPage.deleteFilesFromFolder(downloadLocation);

		//Step6: Exporting 'Building Permit by City Code' report in Data Mode
		objReportsPage.exportReport(reportName,ReportsPage.DATA_EXPORT);
		exportedFileName = Objects.requireNonNull(new File(downloadLocation).listFiles())[0].getName();
		softAssert.assertTrue(exportedFileName.contains("report"), "SMAB-T433,SMAB-T373: Exported report name validation. Report name should contain the word 'Report' in case of data format. Actual Report Name : " + exportedFileName);
		softAssert.assertEquals(exportedFileName.split("\\.")[1],"xls", "SMAB-T433,SMAB-T373: Exported data formatted report should be in XLS");

		//Logout at the end of the test
		objApasGenericPage.logout();
	}
}
