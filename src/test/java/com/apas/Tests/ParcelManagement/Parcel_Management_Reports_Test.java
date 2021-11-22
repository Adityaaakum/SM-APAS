package com.apas.Tests.ParcelManagement;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ReportsPage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class Parcel_Management_Reports_Test extends TestBase {

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

	@Test(description = "SMAB-T3444: Validation of parcel management reports", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "WorkItemWorkflow_Reports","ParcelManagement_Reports", "ParcelManagement" }, alwaysRun = true)
	public void Reports_ParcelManagementReports(String loginUser) throws Exception {
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		String reportName;
		String exportedFileName;
		ReportLogger.INFO("Download location : " + downloadLocation);
		String Parcelreports = testdata.PARCEL_MANAGEMENT_REPORTS;
		Map<String, String> ParcelReportsName = objUtil.generateMapFromJsonFile(Parcelreports, "VerifyReportsName");
		Map<String, String> ParcelReportsfileData = objUtil.generateMapFromJsonFile(Parcelreports,
				"VerifyReportsColumns");
		// Step1: Login to the APAS application using the credentials passed through data provider

		objReportsPage.login(loginUser);
		// Step2: Opening parcel management reports and validate

		for (Map.Entry<String, String> entry : ParcelReportsName.entrySet()) {
			reportName = entry.getKey();
			objReportsPage.searchModule(modules.REPORTS);
			String actualReportName = objReportsPage.navigateToReport(reportName);
			softAssert.assertEquals(actualReportName, reportName,
					"SMAB-T3444: report name validation. Actual Report Name : " + actualReportName);

		}
		objReportsPage.logout();
		
		// Step3 : export and Validate header of reports
		for (Map.Entry<String, String> entry : ParcelReportsfileData.entrySet()) {
			objReportsPage.login(loginUser);

			String expectedColumnsInExportedExcel = entry.getValue().split("-")[0];
			int rowNumber = Integer.parseInt(entry.getValue().split("-")[1]);
			reportName = entry.getKey();
			objReportsPage.searchModule(modules.REPORTS);

			// Deleteing all the previously downloaded files
			objReportsPage.deleteFilesFromFolder(downloadLocation);

			// Step4: Exporting 'Parcel management' report in Formatted Mode
			objReportsPage.exportReport(reportName, ReportsPage.FORMATTED_EXPORT);
			File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
			exportedFileName = downloadedFile.getName();
			softAssert.assertTrue(exportedFileName.contains(reportName),
					"SMAB-T3444: Exported report name validation. Actual Report Name : " + exportedFileName);

			// Step4: Columns validation in exported report
			HashMap<String, ArrayList<String>> hashMapExcelData = ExcelUtils
					.getExcelSheetData(downloadedFile.getAbsolutePath(), 0, rowNumber, 1);

			softAssert.assertEquals(hashMapExcelData.keySet().toString(), expectedColumnsInExportedExcel,
					"SMAB-T3444: Columns Validation in downloaded " + reportName + "Report.");
			objReportsPage.logout();

		}

	}
}
