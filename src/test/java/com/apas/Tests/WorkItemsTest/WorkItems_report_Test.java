package com.apas.Tests.WorkItemsTest;

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
import java.util.Map;
import java.util.Objects;

public class WorkItems_report_Test extends TestBase{

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericPage objApasGenericPage;
	ReportsPage objReportsPage;
	Util objUtil = new Util();
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
	@Test(description = "SMAB-T2520: Validation the Work Item Type and Action based on work item configuration for BPP Accounts", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Items_Manual"}, alwaysRun = true)
    public void WorkItems_ExecutiveViewReports(String loginUser) throws Exception {
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		String reportName = "Executive All Work Items";
		String exportedFileName;
		ReportLogger.INFO("Download location : " + downloadLocation);
		
        // Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
		objApasGenericPage.login(loginUser);
        // Step2: Opening the BPP Account page and searching a BPP Account
		objApasGenericPage.searchModule(modules.REPORTS);
        
		//Deleteing all the previously downloaded files
		objApasGenericPage.deleteFilesFromFolder(downloadLocation);

		//Step3: Exporting 'Executive All Work Items' report in Formatted Mode
		objReportsPage.exportReport(reportName,ReportsPage.FORMATTED_EXPORT);
		File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		exportedFileName = downloadedFile.getName();
		softAssert.assertTrue(exportedFileName.contains("Executive All Work Items"), "SMAB-T2520: Exported report name validation. Report name should contain 'Executive All Work Items' in formatted export. Actual Report Name : " + exportedFileName);
		softAssert.assertEquals(exportedFileName.split("\\.")[1],"xlsx", "SMAB-T2520: Exported data formatted report should be in XLSX");

		//Step4: Columns validation in exported report
		HashMap<String, ArrayList<String>> hashMapExcelData = ExcelUtils.getExcelSheetData(downloadedFile.getAbsolutePath(),0,8,1);
		
		String expectedColumnsInExportedExcel = "[Work Item: Work Item Number, Account #, Name, Roll Code, APN, Street, Use Code, Work Pool, Request Type, Status, Date, Value, Created In Roll Year, Remarks, Age(Days), Completed Date]";
		softAssert.assertContains(hashMapExcelData.keySet().toString(),expectedColumnsInExportedExcel,"SMAB-T2520: Columns Validation in downloaded Executive All Work Items report");
		
        //Logout of APAS Application
		objApasGenericPage.logout();
    }
}
