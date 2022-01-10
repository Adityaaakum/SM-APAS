package com.apas.Tests.OwnershipAndTransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ReportsPage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;

public class CIO_Reports_Test extends TestBase {

	private RemoteWebDriver driver;
	ReportsPage objReportsPage;
	SoftAssertion softAssert = new SoftAssertion();
	Util objUtil = new Util();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objReportsPage = new ReportsPage(driver);
	}
	
	/**
	 * Below test case will verify if a new report called "RP Activity list" exists with its respective fields
	 **/
	@Test(description = "SMAB-T4063: Validation of MCL report 1 in reports", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "Reports" }, alwaysRun = true)
	public void Reports_verifyMCLReport1(String loginUser) throws Exception {
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		String reportName = "MCL Report 1";
		String exportedFileName;
		ReportLogger.INFO("Download location : " + downloadLocation);
		String parcelReports = testdata.HOME_OWNER_EXEMPTION_Reports;
		Map<String, String> parcelReportsfileData = objUtil.generateMapFromJsonFile(parcelReports,
				"VerifyMCLReport1Columns");
		
		// Step1: Login to the APAS application using the credentials passed through data provider
		objReportsPage.login(loginUser);
		
		
				
		// Step3 : export and Validate header of reports
		String expectedColumnsInExportedExcel = parcelReportsfileData.get("MCL Report 1 List");

		objReportsPage.searchModule(modules.REPORTS);

		// Deleting all the previously downloaded files
		objReportsPage.deleteFilesFromFolder(downloadLocation);

		// Step4: Exporting 'Parcel management' report in Formatted Mode
		objReportsPage.exportReport(reportName, ReportsPage.FORMATTED_EXPORT);
		File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		exportedFileName = downloadedFile.getName();
		softAssert.assertTrue(exportedFileName.contains(reportName),
				"SMAB-T4063: Exported report name validation. Actual Report Name : " + exportedFileName);

		// Columns validation in exported report
		HashMap<String, ArrayList<String>> hashMapExcelData = ExcelUtils
				.getExcelSheetData(downloadedFile.getAbsolutePath(), 0, 14, 1);
			
		softAssert.assertEquals(hashMapExcelData.keySet().toString(), expectedColumnsInExportedExcel,
				"SMAB-T4063: Columns Validation in downloaded " + reportName + "Report.");
			
		objReportsPage.logout();
	}
}
