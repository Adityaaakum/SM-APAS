package com.apas.Tests.DisabledVeteran;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ReportsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class DisabledVeteran_ExemptionReport_Test extends TestBase{
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	SoftAssertion softAssert;
	Util objUtils;
	ExemptionsPage objExemptionsPage;
	ApasGenericPage objApasGenericPage;
	ReportsPage objReportsPage;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception {
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objUtils = new Util();
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		softAssert = new SoftAssertion();
		objApasGenericPage = new ApasGenericPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);	
		objReportsPage = new ReportsPage(driver);
		objApasGenericFunctions.updateRollYearStatus("Closed", "2020");
	}

	/**
	 * Below test case will 
	 * 1. Verify if user is able to download the report
	 * 2. Verify Columns of Report are in expected Order
	 * 3. Verify if expected column data contain link 
	 * 4. Create Exemptions with status - Active & In Active
	 * 5. Verify if created Exemptions are visible on Report or not
	 **/
	@Test(description = "SMAB-T635: Verify the exemption support staff can export the Report", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class ,groups = {"regression", "DisabledVeteranExemption" })
	public void DisabledVeteran_verifyExportReport(String loginUser) throws Exception {
		
		String downloadLocation = CONFIG.getProperty("downloadFolder");
		String reportName = "DV Exemption Export";
		String exportedFileName;
		ReportLogger.INFO("Download location : " + downloadLocation);

		//Step1: Login to the APAS application using the credentials passed through data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Reports module
		objApasGenericFunctions.searchModule(modules.REPORTS);

		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

		//Step3: Exporting 'DV Exemption Report' report in Formatted Mode
		objReportsPage.exportReport(reportName,ReportsPage.FORMATTED_EXPORT);
		Thread.sleep(15000);
		File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		exportedFileName = downloadedFile.getName();
		softAssert.assertTrue(exportedFileName.contains("DV Exemption Export"), "SMAB-T635: Verify the exemption support staff can export the Report");
		softAssert.assertEquals(exportedFileName.split("\\.")[1],"xlsx", "SMAB-T635: Exported data formatted report should be in XLSX");
		
		//Step4: Fetching expected column names
		Map<String, String> exemptionReportDataMap;
		String exemptionData = System.getProperty("user.dir") + testdata.EXEMPTION_REPORT_DATA;		
		exemptionReportDataMap = objUtils.generateMapFromJsonFile(exemptionData, "columnNames");
		
		//Step5: Columns validation in exported report
		HashMap<String, ArrayList<String>> hashMapExcelData = ExcelUtils.getExcelSheetData(downloadedFile.getAbsolutePath(),0,13,1);
		String expectedColumnsInExportedExcel = "["+exemptionReportDataMap.get("Column Names")+"]";
		softAssert.assertEquals(hashMapExcelData.keySet().toString(),expectedColumnsInExportedExcel,"SMAB-T606: Columns Validation in downloaded DV Exemption report");
		
		//Step6: Validation that Building Permit Number APN columns dispalyed on the report should be links
		driver.switchTo().frame(0);
		softAssert.assertTrue(objPage.verifyElementExists(objReportsPage.linkAPNDV),"SMAB-T606: Validation that APN Number displayed on report should be a link");
		softAssert.assertTrue(objPage.verifyElementExists(objReportsPage.linkExemptionName),"SMAB-T606: Validation that Exemption Name displayed on report should be a link");
		softAssert.assertTrue(objPage.verifyElementExists(objReportsPage.linkClaimantsName),"SMAB-T606: Validation that Claimant's Name displayed on report should be a link");
		softAssert.assertTrue(objPage.verifyElementExists(objReportsPage.linkRollYearSettings),"SMAB-T606: Validation that Roll Year settings Name displayed on report should be a link");
		
		driver.switchTo().parentFrame();	
		
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * Below test case will 
	 * 1. Create Exemptions with status - Active & In Active
	 * 2. Verify if created Exemptions are visible on Report or not 
	 **/
	@Test(description = "SMAB-T606: Verify the Exemption Support staff is able to generate the report with the Active and deactivated exemptions", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class ,groups = {"regression", "DisabledVeteranExemption" })
	public void DisabledVeteran_verifyCreatedExemptionsVisiblityInReport(String loginUser) throws Exception {
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
				
		// Step3: Fetching data to verify column names in report
		Map<String, String> activeExemptionDataMap;
		String currentRollYear = "2021";
		String previousRollYear = "2020";
		String removeRollYear = "2019";
		String removeEntry1 = "6/30/" + previousRollYear;
		String removeEntry2 = "7/1/" + removeRollYear;
		String exemptionData = System.getProperty("user.dir") + testdata.EXEMPTION_REPORT_DATA;		
		activeExemptionDataMap = objUtils.generateMapFromJsonFile(exemptionData, "DataToCreateExemptionWithMandatoryFields");
		
		/*Step4: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - Active
		 Capture the Exemption Name*/		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating Active Exemption");
		activeExemptionDataMap.put("Veteran Name", activeExemptionDataMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		objExemptionsPage.createExemption(activeExemptionDataMap);
		String activeExemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		ReportLogger.INFO("Active Exemption Created: "+activeExemptionName);
		
		// Step5: Fetching data to verify column names in report
		Map<String, String> inActiveExemptionDataMap;	
		inActiveExemptionDataMap = objUtils.generateMapFromJsonFile(exemptionData, "DataToCreateExemptionWithEndDate");
		
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		/*Step7: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - InActive
		 Capture the Exemption Name*/
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating In-Active Exemption");
		inActiveExemptionDataMap.put("Veteran Name", inActiveExemptionDataMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		objExemptionsPage.createExemption(inActiveExemptionDataMap);
		driver.navigate().refresh();
		String inActiveExemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		ReportLogger.INFO("In Active Exemption Created: "+inActiveExemptionName);	
		
		// Step8: Searching Reports Module
		objApasGenericFunctions.searchModule(modules.REPORTS);
		
		// Step9: Navigating to 'DV Exemption Export' Report
		objReportsPage.navigateToReport("DV Exemption Export");
		
		// Step10: Switching to frame to access elements
		objPage.switchToFrameByIndex(0);
		
		// Step11: Waiting for 'DV Exemption Export' Report to be visible on screen
		objReportsPage.waitForElementToBeClickable(60,objReportsPage.exemptionNameLabel);		
		boolean flagReportDisplayed = objPage.verifyElementEnabled(objReportsPage.exemptionNameLabel);
		
		ReportLogger.INFO("DV Exemption Export Report is visible: "+flagReportDisplayed);
		if(flagReportDisplayed) {

			// Step12: Update the year values on default filters set
			ReportLogger.INFO("Update the default filters displayed with correct year values");
			objPage.Click(objReportsPage.filterIcon);
			objReportsPage.editFilterAndUpdate("2", removeEntry1, currentRollYear);
			objReportsPage.editFilterAndUpdate("3", removeEntry2, previousRollYear);
			objReportsPage.editFilterAndUpdate("4", removeEntry1, currentRollYear);
			objReportsPage.editFilterAndUpdate("5", removeEntry2, previousRollYear);
			
			// Step13: Sort the column "Exemption: Exemption Name" in Descending Order
			objReportsPage.sortReportColumn("Exemption: Exemption Name");
			
			// Step14: Fetch the data from 2nd row of Report
			HashMap<String, ArrayList<String>> getReportDataInActiveExemp = objApasGenericFunctions.getGridDataInLinkedHM(2);
			String actualInActiveExemption = getReportDataInActiveExemp.get("Exemption: Exemption Name").get(0).replace("[", "").replace("]", "");
			softAssert.assertEquals(actualInActiveExemption, inActiveExemptionName, "SMAB-T606:Verify In Active Exemption created is visible in report");
			
			// Step15: Fetch the data from 4th row of Report
			HashMap<String, ArrayList<String>> getReportDataActiveExemp = objApasGenericFunctions.getGridDataInLinkedHM(4);

			// Step16: Verify Exemption with Status 'Active' created above is visible in report
			String actualActiveExemption = getReportDataActiveExemp.get("Exemption: Exemption Name").get(0).replace("[", "").replace("]", "");
			softAssert.assertEquals(actualActiveExemption, activeExemptionName, "SMAB-T606:Verify Active Exemption created is visible in report");
			
		}	 
		objPage.switchBackFromFrame();
		objApasGenericFunctions.logout();
	}
	
	
}