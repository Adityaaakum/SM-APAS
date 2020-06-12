package com.apas.Tests.DisabledVeteran;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.DVExemptionExportReportPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ReportsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
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
	DVExemptionExportReportPage objDVReport;
	ExemptionsPage objExemptionsPage;
	ApasGenericPage objApasGenericPage;
	ReportsPage objReportsPage;
	
	@BeforeMethod
	public void beforeMethod() throws Exception {
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objUtils = new Util();
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		softAssert = new SoftAssertion();
		objApasGenericPage = new ApasGenericPage(driver);
		objDVReport = new DVExemptionExportReportPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);	
		objReportsPage = new ReportsPage(driver);
	}

	
	/**
	 * Below test case will 
	 * 1. Verify Columns of Report are in expected Order
	 * 2. Verify if expected column data contain link 
	 **/
	@Test(description = "SMAB-T606: Verify the Exemption Support staff is able to generate the report with the Active and deactivated exemptions", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class ,groups = {"smoke", "regression", "DisabledVeteranExemption" })
	public void DisabledVeteran_verifyColNamesandLinksInData(String loginUser) throws Exception {
		
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		// Step2: Searching Reports Module
		objApasGenericFunctions.searchModule(modules.REPORTS);
		
		// Step3: Navigating to 'DV Exemption Export' Report
		objReportsPage.navigateToReport("DV Exemption Export");
		
		// Step4: Switching to frame to access elements
		objPage.switchToFrameByIndex(0);
		
		// Step5: Waiting for 'DV Exemption Export' Report to be visible on screen
		objPage.waitForElementToBeClickable(40,objDVReport.exemptionNameLabel);		
		//Thread.sleep(2000);
		boolean flagReportDisplayed = objPage.verifyElementEnabled(objDVReport.exemptionNameLabel);
		ReportLogger.INFO("DV Exemption Export Report is visible: "+flagReportDisplayed);
		if(flagReportDisplayed) {
			// Step6: Minimizing the browser to 50%
			objApasGenericFunctions.zoomOutPageContent();			
			// Step7: Calculating number of columns in report
			int noOfColumns = objDVReport.colNames.size();			
			ReportLogger.INFO("No of columns in DV Exemption Export Report are : "+noOfColumns);
			
			// Step8: Fetching data to verify column names in report
			Map<String, String> exemptionReportDataMap;
			String exemptionData = System.getProperty("user.dir") + testdata.EXEMPTION_REPORT_DATA;		
			exemptionReportDataMap = objUtils.generateMapFromJsonFile(exemptionData, "columnNames");
			String expectedColumnNames = exemptionReportDataMap.get("Column Names");
			
			// Step9: Fetch column names displayed in report
			HashMap<String, ArrayList<String>> getReportDataInActiveExemp = objApasGenericFunctions.getGridDataInLinkedHM(2);
			List<String> actualColumnNames = new ArrayList<String>(getReportDataInActiveExemp.keySet());
			//List<String> actualColumnNames = objDVReport.getAllHeaders(getReportDataInActiveExemp);
			
			// Step10: Convert AraayList to comma separated string
			StringBuilder actualColNames =  new StringBuilder();
			for(String col: actualColumnNames) {
				col = col.split("_1")[0];
				actualColNames.append(col);
				actualColNames.append(",");
			}			
			String  actualColumns= actualColNames.substring(0, actualColNames.length()-1);	
			
			// Step11: Verify Column Names visible on Report
			ReportLogger.INFO("Column names in application are : "+actualColumns);
			softAssert.assertEquals(actualColumns, expectedColumnNames, "SMAB-T606: Verify Column Names in Report");
			
			// Step12: Verify data corresponding to Column Names contains link or not
			if(actualColumns.equals(expectedColumnNames)) {
			for(int i=1; i<=actualColumnNames.size();i++) {
				String columnsContainsLink = exemptionReportDataMap.get("Columns contains Link");
				String column = actualColumnNames.get(i-1);
				String xpathLink = "";				
				String columnName = column.split("_1")[0];						
				column = columnName.split("'")[0];
				xpathLink = "//table[contains(@class,'full')]//span[contains(text(),'"+column+"')]//ancestor::tr//following-sibling::tr[1]//td["+i+"]//a";
					
				boolean fLink = objDVReport.waitForElementToBeVisible(2,xpathLink);				
				
				softAssert.assertEquals(columnsContainsLink.contains(column),fLink,"SMAB-T606: Verify column: " +columnName+" contains link in data");
			}
			}
			else{
				ReportLogger.FAIL("Verification of Links in data is not executed as Columns Name or Count is not as expected");
			}
				
		}
		objApasGenericFunctions.zoomInPageContent();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * Below test case will 
	 * 1. Verify if user is able to download the report
	 **/
	@Test(description = "SMAB-T635: Verify the exemption support staff can export the Report", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class ,groups = {"smoke", "regression", "DisabledVeteranExemption" })
	public void DisabledVeteran_verifyExportReport(String loginUser) throws Exception {
		
		// Step1: Count No. Of Files with extension '.xlsx' in downloads folder
		int noOfXLFiles = objUtils.countFilesInFolder("xlsx","C:\\Downloads");	
		System.out.println("Files b4 download: "+noOfXLFiles);
		
		// Step2: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		// Step3: Searching Reports Module
		objApasGenericFunctions.searchModule(modules.REPORTS);
		
		// Step4: Navigating to 'DV Exemption Export' Report
		objDVReport.exportReport("DV Exemption Export","formatted-export");
		
		Thread.sleep(10000);
		// Step5: Count No. Of Files with extension '.xlsx' in downloads folder after downloading DV Exemption Export Report
		int noOfXLFilesafterDownload = objUtils.countFilesInFolder("xlsx","C:\\Downloads");		
		
		System.out.println("Files after download: "+noOfXLFilesafterDownload);
		
		// Step6: Get last modified File Name from Downloads folder 
		String lastDownloadedFile = objApasGenericFunctions.getLastModifiedFile("C:\\Downloads");
		ReportLogger.INFO("Last downloaded file name is: "+lastDownloadedFile);
		softAssert.assertTrue(lastDownloadedFile.contains("DV Exemption Export"), "SMAB-T635: Verify the exemption support staff can export the Report");
		
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * Below test case will 
	 * 1. Create Exemptions with status - Active & In Active
	 * 2. Verify if created Exemptions are visible on Report or not 
	 **/
	@Test(description = "SMAB-T606: Verify the Exemption Support staff is able to generate the report with the Active and deactivated exemptions", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class ,groups = {"smoke", "regression", "DisabledVeteranExemption" })
	public void DisabledVeteran_verifyCreatedExemptionsVisiblityInReport(String loginUser) throws Exception {
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
				
		// Step3: Fetching data to verify column names in report
		Map<String, String> activeExemptionDataMap;
		String exemptionData = System.getProperty("user.dir") + testdata.EXEMPTION_REPORT_DATA;		
		activeExemptionDataMap = objUtils.generateMapFromJsonFile(exemptionData, "DataToCreateExemptionWithMandatoryFields");
		
		/*Step4: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - Active
		 Capture the Exemption Name*/		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating Active Exemption");
		activeExemptionDataMap.put("Veteran Name", activeExemptionDataMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		objExemptionsPage.createExemptionWithoutEndDateOfRating(activeExemptionDataMap);
		String activeExemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		System.out.println("active exemption name: "+activeExemptionName);
		
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
		objExemptionsPage.createExemptionWithEndDateOfRating(inActiveExemptionDataMap);
		Thread.sleep(2000);
		String iNActiveExemptionName = objExemptionsPage.getExemptionNameFromSuccessAlert();
		String inActiveExemptionName = "EXMPTN-"+iNActiveExemptionName;
		System.out.println("In active exemption name: "+inActiveExemptionName);
		
		
		// Step8: Searching Reports Module
		objApasGenericFunctions.searchModule(modules.REPORTS);
		
		// Step9: Navigating to 'DV Exemption Export' Report
		objReportsPage.navigateToReport("DV Exemption Export");
		
		// Step10: Switching to frame to access elements
		objPage.switchToFrameByIndex(0);
		
		// Step11: Waiting for 'DV Exemption Export' Report to be visible on screen
		objDVReport.waitForElementToBeClickable(40,objDVReport.exemptionNameLabel);		
		Thread.sleep(2000);
		boolean flagReportDisplayed = objPage.verifyElementEnabled(objDVReport.exemptionNameLabel);
		
		ReportLogger.INFO("DV Exemption Export Report is visible: "+flagReportDisplayed);
		if(flagReportDisplayed) {
		// Step12: Minimizing the browser to 50%
			objApasGenericFunctions.zoomOutPageContent();				
			
			// Step13: Sort the column "Exemption: Exemption Name" in Descending Order
			objDVReport.sortReportColumn("Exemption: Exemption Name");
			
			// Step13: Fetch the data from 2nd row of Report
			HashMap<String, ArrayList<String>> getReportDataInActiveExemp = objApasGenericFunctions.getGridDataInLinkedHM(2);
			String actualInActiveExemption = getReportDataInActiveExemp.get("Exemption: Exemption Name").get(0).replace("[", "").replace("]", "");
			softAssert.assertEquals(actualInActiveExemption, inActiveExemptionName, "SMAB-T606:Verify In Active Exemption created is visible in report");
			
			// Step14: Fetch the data from 4th row of Report
			HashMap<String, ArrayList<String>> getReportDataActiveExemp = objApasGenericFunctions.getGridDataInLinkedHM(4);

			// Step15: Verify Exemption with Status 'Active' created above is visible in report
			String actualActiveExemption = getReportDataActiveExemp.get("Exemption: Exemption Name").get(0).replace("[", "").replace("]", "");
			softAssert.assertEquals(actualActiveExemption, activeExemptionName, "SMAB-T606:Verify Active Exemption created is visible in report");
			
	}	 
		objApasGenericFunctions.zoomInPageContent();
		objPage.waitUntilElementIsPresent("//button//div/span[@class='uiImage']", 40);
		objApasGenericFunctions.logout();
	}
	
	
}