package com.apas.Tests.BPPTrends;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.EFileImportLogsPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_EfileImport_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrend;
	EFileImportPage objEfileHomePage;
	EFileImportLogsPage objEFileImportLogPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYearForImport;
	SalesforceAPI objSalesforceAPI;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		
		objPage = new Page(driver);
		objBppTrend = new BppTrendPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objEFileImportLogPage=new EFileImportLogsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYearForImport = "2021";
		objSalesforceAPI = new SalesforceAPI();
	}
		
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 2. Validating Discard functionality of error records
	 * 3. Validating no of Error records post discarding error records
	 */
	@Test(description = "SMAB-T106,SMAB-T111,SMAB-T79: Discarding error records and reverting import for BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","BPPTrend"})
	public void BppTrend_BOEIndexFileImportAndDiscardErrorRecords(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Fetch the excel file imported
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;		
				
		//Step5: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, fileName + "BOE Equipment Index Factors and Percent Good Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Step8: Opening the Efile Import Logs module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step9: Import Logs grid validation for the imported BOE - Index and Percent Good Factors file
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"BPP Trend Factors :BOE - Index and Percent Good Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import logs page");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Imported", "SMAB-T111: Validation if status of imported file is imported on import logs page");
		
		//Step10: Opening the Efile Import Transactions module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		
		HashMap<String, ArrayList<String>> importTransactionsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step11: Import Transactions grid validation for the imported BOE - Index and Percent Good Factors file
		softAssert.assertEquals(importTransactionsGridData.get("E-File Import Log").get(0),"BPP Trend Factors :BOE - Index and Percent Good Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import transactions page");
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported", "SMAB-T111: Validation if status of imported file is imported on import transactions page");
		
		//Step12: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step13: Selecting the File Type and Source and clicking on 'View All' link 
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		//Step14: Iterate over all the tables
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(BPPTablesData.EFILE_IMPORT_PAGE_BOE_INDEX_TABLES_NAMES.split(",")));	
		for (int i = 0; i < allTables.size(); i++) {
						
			//Step15: Clicking on the table
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName);

		//********* Validating that correct number of records are present in error row section after file import *********
			String expectedNoOfErrorRecords = "2";    //Imported File has 2 error records in each table			
			//Step16: Validation of number of records in error row section
			String actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			softAssert.assertEquals(actualNoOfErrorRecords, expectedNoOfErrorRecords, "SMAB-T79,SMAB-T106: Validate if correct number of records are displayed in Error Row Section after file import");
			
		//********* Validating that correct number of records are present in Imported row section after file import *********	
			//Step17: Counting no of records imported from Excel File Imported			
			int countOfTotalRecordsInt = objEfileHomePage.getRowCountSpecificToTable(fileName+ "BOE Equipment Index Factors and Percent Good Factors 2021.xlsx",tableName);
			
			//Step18: Fetching expected Imported Rows Count
			String expectedImportedRowsCount = Integer.toString(countOfTotalRecordsInt - Integer.parseInt(expectedNoOfErrorRecords));			
			
			//Step19: Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file					
			String actualImportedRowsCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			softAssert.assertEquals(actualImportedRowsCount, expectedImportedRowsCount, "SMAB-T79,SMAB-T111: Validation if correct number of records are displayed in Imported Row Section after file import");
			
		//********* Validate Discard Functionality *********	
			//Step20: Validation for Records discard functionality from Review and Approve Page
			ReportLogger.INFO("SMAB-T111: Validate that error records can be discarded from Review and Approve Data Page");
			objEfileHomePage.discardErrorRecords("1");
			
			//Step21: validating the number of records in the error row section after discarding a record
			int updatedCount = Integer.parseInt(expectedNoOfErrorRecords) - 1;
			actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);		
			softAssert.assertEquals(actualNoOfErrorRecords, updatedRecordsInErrorRowPostDelete, "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after discarding a record");
		}

		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 2. Validating revert functionality of error records
	 * 3. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T106,SMAB-T111,SMAB-T79: Discarding error records and reverting import for BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","BPPTrend"})
	public void BppTrend_BOEIndexFileImportAndRevert(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
				
		//Step4: Fetch the excel file imported
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;		
				
		//Step5: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, fileName + "BOE Equipment Index Factors and Percent Good Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
				
		//Step8: Selecting the File Type and Source and clicking on 'View All' link 
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);

		//Step9: Reverting the imported file
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step10: Validation of the file status after reverting the imported file
		ReportLogger.INFO("Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");

		//Step11: Status of the imported file should be changed to Reverted as the whole file is reverted
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T111: Validation if status of imported file is reverted.");

		//Step12: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"BPP Trend Factors :BOE - Index and Percent Good Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import logs page");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Reverted", "SMAB-T111: Validation if status of imported file is reverted on import logs page");
				
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 3. Correcting some of the error records
	 * 4. Validating retry functionality of error records
	 * 5. Validating no of records in Error and Imported rows section post retrying
	 */
	@Test(description = "SMAB-T111: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void BppTrend_BOEIndexImportAndRetryErrorRecords(String loginUser) throws Exception {
		//Step1: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step2: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step3: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);				
		
		//Step4: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, fileName + "BOE Equipment Index Factors and Percent Good Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Validation of number of times tried retried column for the imported file. Expected is 1 as it has not be retried/reverted yet
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T111: Validate if number of times try/retry count is correct on file import");			
		//Validations for Total file count before retrying error records		
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "704", "SMAB-T111: Validating total records count before retrying errorred records");
		//Validations for Imported count before retrying error records	
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "690", "SMAB-T111: Validating import count before retrying errorred records");		
		//Validations for Error count before retrying error records
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.errorRecordsImportedFile), "14", "SMAB-T111: Validating error count before retrying errorred records");		
		
		//Step8: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		Map<String, Integer> expectedImportedRowsCountBeforeRetry = new HashMap<String, Integer>();
		Map<String, Integer> expectedErrorRowsCountBeforeRetry = new HashMap<String, Integer>();
		String importedRowsCount, errorRowCount;		
		
		//Step7: Iterate over all the tables / columns
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(BPPTablesData.EFILE_IMPORT_PAGE_BOE_INDEX_TABLES_NAMES.split(",")));	
		
		for (int i = 0; i < allTables.size(); i++) {
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);			
			objBppTrend.clickOnTableOnBppTrendPage(tableName);
			
			//Step9: Store Error and Imported records count for each table in a map to verify count after Retrying error records 
			importedRowsCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			expectedImportedRowsCountBeforeRetry.put(tableName, Integer.parseInt(importedRowsCount));
			
			errorRowCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");		
			expectedErrorRowsCountBeforeRetry.put(tableName, Integer.parseInt(errorRowCount));
			
			//Step10: Enter correct data in one of the Error Row 'Cell' 
			ReportLogger.INFO("Deleting junk data and entering valid data in the table: "+tableName);
			if(!tableName.equalsIgnoreCase("M&E Good Factors")) {
				objApasGenericFunctions.editGridCellValue("Average","80");
			}else
				objApasGenericFunctions.editGridCellValue("Factor","80");

			objPage.Click(objEfileHomePage.rowSelectCheckBox);
			objEfileHomePage.collapseSection(objEfileHomePage.errorRowSectionExpandButton);
			objPage.scrollToTop();

		}
		
		//Step11: Clicking retry button and waiting for corrected records to move to imported section
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton);
		objPage.javascriptClick(objEfileHomePage.retryButton);
		objPage.waitUntilElementIsPresent(objEfileHomePage.xpathSpinner,20);
		objPage.waitForElementToDisappear(objEfileHomePage.xpathSpinner,50);
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton,20);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,160);
		
		//Step12: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName);
			
			//Step13: Validate that correct no.of records are remain in Error row section after retry
			String actualErrorRowsAfterRetrying = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			String expectedErrorRowCountAfterRetry = Integer.toString(expectedErrorRowsCountBeforeRetry.get(tableName)-1);
			softAssert.assertEquals(actualErrorRowsAfterRetrying, expectedErrorRowCountAfterRetry, "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			
			//Step14: Validate that corrected no. of records are moved to Imported row section after retry
			String actualImportedRowsAfterRetrying = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			String expectedImportedRowCountAfterRetry = Integer.toString(expectedImportedRowsCountBeforeRetry.get(tableName)+1);
			softAssert.assertEquals(actualImportedRowsAfterRetrying, expectedImportedRowCountAfterRetry, "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section under "+ tableName +" after correcting and retrying the error record");
		}
		
		objApasGenericFunctions.logout();
	}
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 2. Approving all records
	 * 3. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-957,SMAB-T111: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void BppTrend_BOEIndexImportAndApprove(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);	
		
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
	
		//Step4: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_VALID;
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, fileName + "BOE Equipment Index Factors and Percent Good Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Step8: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
				
		//Step9: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step10: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step11: Select File Type and File Source
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		
		//Step12: Verify the status of File Imported is updated to Approved
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T111: Validation if status of imported file is approved.");

		//Step13: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step14: Import Logs grid validation for the imported BOE - Index and Percent Good Factors file
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Approved", "SMAB-957,SMAB-T111: Validate if status of imported file is approved on import logs page");
		
		objApasGenericFunctions.logout();
	}
		
	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE Valuation Factor File>
	 * 1. Validating the successful uploading of data file with valid format 
	 * 2. Validating Discard functionality of error records
	 * 3. Validating no of Error records post discarding error records
	 */
	@Test(description = "SMAB-T106,SMAB-T111: Discarding error records and reverting import for BOE valuation file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","BPPTrend"})
	public void BppTrend_BOEValFileImportAndDiscardErrorRecords(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Fetch the excel file imported
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;		
				
		//Step5: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYearForImport, fileName + "BOE Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Step8: Opening the Efile Import Logs module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step9: Import Logs grid validation for the imported BOE - Valuation Factors file
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"BPP Trend Factors :BOE - Valuation Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import logs page");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Imported", "SMAB-T111: Validation if status of imported file is imported on import logs page");
		
		//Step10: Opening the Efile Import Transactions module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		
		HashMap<String, ArrayList<String>> importTransactionsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step11: Import Transactions grid validation for the imported BOE - Valuation Factors file
		softAssert.assertEquals(importTransactionsGridData.get("E-File Import Log").get(0),"BPP Trend Factors :BOE - Valuation Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import transactions page");
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported", "SMAB-T111: Validation if status of imported file is imported on import transactions page");
		
		//Step12: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step13: Selecting the File Type and Source and clicking on 'View All' link 
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		//Step14: Iterate over all the tables
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(BPPTablesData.EFILE_IMPORT_PAGE_BOE_VAL_TABLES_NAMES.split(",")));	
		for (int i = 0; i < allTables.size(); i++) {
						
			//Step15: Clicking on the table
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName);

		//********* Validating that correct number of records are present in error row section after file import *********
			String expectedNoOfErrorRecords = "2";    //Imported File has 2 error records in each table			
			//Step16: Validation of number of records in error row section
			String actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			softAssert.assertEquals(actualNoOfErrorRecords, expectedNoOfErrorRecords, "SMAB-T106: Validate if correct number of records are displayed in Error Row Section after file import");
			
		//********* Validating that correct number of records are present in Imported row section after file import *********	
			//Step17: Counting no of records imported from Excel File Imported			
			int countOfTotalRecordsInt = objEfileHomePage.getRowCountSpecificToTable(fileName + "BOE Valuation Factors 2021.xlsx",tableName);
			
			//Step18: Fetching expected Imported Rows Count
			String expectedImportedRowsCount = Integer.toString(countOfTotalRecordsInt - Integer.parseInt(expectedNoOfErrorRecords));			
			
			//Step19: Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file					
			String actualImportedRowsCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			softAssert.assertEquals(actualImportedRowsCount, expectedImportedRowsCount, "SMAB-T79,SMAB-T111: Validation if correct number of records are displayed in Imported Row Section after file import");
			
		//********* Validate Discard Functionality *********	
			//Step20: Validation for Records discard functionality from Review and Approve Page
			ReportLogger.INFO("SMAB-T111: Validate that error records can be discarded from Review and Approve Data Page");
			objEfileHomePage.discardErrorRecords("1");
			
			//Step21: validating the number of records in the error row section after discarding a record
			int updatedCount = Integer.parseInt(expectedNoOfErrorRecords) - 1;
			actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);		
			softAssert.assertEquals(actualNoOfErrorRecords, updatedRecordsInErrorRowPostDelete, "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after discarding a record");
		}
		
		objApasGenericFunctions.logout();	
	}
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE Valuation Factor File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 2. Validating revert functionality of error records
	 * 3. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T106,SMAB-T111,SMAB-T79: Discarding error records and reverting import for BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","BPPTrend"})
	public void BppTrend_BOEValFileImportAndRevert(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Fetch the excel file imported
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;		
				
		//Step5: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYearForImport, fileName + "BOE Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
				
		//Step8: Selecting the File Type and Source and clicking on 'View All' link 
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);

		//Step9: Reverting the imported file
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step10: Validation of the file status after reverting the imported file
		ReportLogger.INFO("Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");

		//Step11: Status of the imported file should be changed to Reverted as the whole file is reverted
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T111: Validation if status of imported file is reverted.");

		//Step12: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"BPP Trend Factors :BOE - Valuation Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import logs page");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Reverted", "SMAB-T111: Validation if status of imported file is reverted on import logs page");
				
		objApasGenericFunctions.logout();
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE Valuation Factor File>
	 * 1. Validating the successful uploading of data file with valid format 
	 * 3. Correcting some of the error records
	 * 4. Validating retry functionality of error records
	 * 5. Validating no of records in Error and Imported rows section post retrying
	 */
	@Test(description = "SMAB-T111,SMAB-T91: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void BppTrend_BOEValImportAndRetryErrorRecords(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);				
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
				
		//Step4: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYearForImport, fileName + "BOE Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Validation of number of times tried retried column for the imported file. Expected is 1 as it has not be retried/reverted yet
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T111: Validate if number of times try/retry count is correct on file import");			
		//Validations for Total file count before retrying error records		
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "410", "SMAB-T111: Validating total records count before retrying errorred records");
		//Validations for Imported count before retrying error records	
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "400", "SMAB-T111: Validating import count before retrying errorred records");		
		//Validations for Error count before retrying error records
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.errorRecordsImportedFile), "10", "SMAB-T111: Validating error count before retrying errorred records");		
		
		//Step8: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		Map<String, Integer> expectedImportedRowsCountBeforeRetry = new HashMap<String, Integer>();
		Map<String, Integer> expectedErrorRowsCountBeforeRetry = new HashMap<String, Integer>();
		String importedRowsCount, errorRowCount;		
		
		//Step7: Iterate over all the tables / columns
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(BPPTablesData.EFILE_IMPORT_PAGE_BOE_VAL_TABLES_NAMES.split(",")));	
		
		for (int i = 0; i < allTables.size(); i++) {
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);			
			objBppTrend.clickOnTableOnBppTrendPage(tableName);

			//Step9: Store Error and Imported records count for each table in a map to verify count after Retrying error records 
			importedRowsCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			expectedImportedRowsCountBeforeRetry.put(tableName, Integer.parseInt(importedRowsCount));
			
			errorRowCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");		
			expectedErrorRowsCountBeforeRetry.put(tableName, Integer.parseInt(errorRowCount));
			
			//Step10: Enter correct data in one of the Error Row 'Cell' 
			ReportLogger.INFO("Deleting junk data and entering valid data in the table: "+tableName);
			objApasGenericFunctions.editGridCellValue("Valuation Factor","80");
			objPage.Click(objEfileHomePage.rowSelectCheckBox);
			objEfileHomePage.collapseSection(objEfileHomePage.errorRowSectionExpandButton);
			objPage.scrollToTop();
		}
		
		//Step11: Clicking retry button and waiting for corrected records to move to imported section
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton);
		objPage.javascriptClick(objEfileHomePage.retryButton);
		objPage.waitUntilElementIsPresent(objEfileHomePage.xpathSpinner,20);
		objPage.waitForElementToDisappear(objEfileHomePage.xpathSpinner,50);
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton,20);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,160);
		
		//Step12: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName);
			
			//Step13: Validate that correct no.of records are remain in Error row section after retry
			String actualErrorRowsAfterRetrying = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			String expectedErrorRowCountAfterRetry = Integer.toString(expectedErrorRowsCountBeforeRetry.get(tableName)-1);
			softAssert.assertEquals(actualErrorRowsAfterRetrying, expectedErrorRowCountAfterRetry, "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			
			//Step14: Validate that corrected no. of records are moved to Imported row section after retry
			String actualImportedRowsAfterRetrying = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			String expectedImportedRowCountAfterRetry = Integer.toString(expectedImportedRowsCountBeforeRetry.get(tableName)+1);
			softAssert.assertEquals(actualImportedRowsAfterRetrying, expectedImportedRowCountAfterRetry, "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section under "+ tableName +" after correcting and retrying the error record");
		}
		
		//step14a:verifying Transaction object is created for retried records	
		ReportLogger.INFO("Verifying Transaction record is created after performaing Retry");	
		objPage.Click(objEfileHomePage.sourceDetails);	
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeClickable(objEfileHomePage.statusImportedFile, 10);	
		objApasGenericFunctions.openLogRecordForImportedFile("BPP Trend Factors","BOE - Valuation Factors",rollYearForImport,fileName + "BOE Valuation Factors 2021.xlsx");	
		objPage.waitForElementToBeClickable(objEFileImportLogPage.logStatus, 10);	
		objPage.Click(objEfileImportTransactionsPage.transactionsTab);	
		objPage.waitForElementToBeClickable(objEFileImportLogPage.viewAlllink, 10);	
		softAssert.assertEquals(objEfileImportTransactionsPage.transactionsRecords.size(), "2", "SMAB-T91:Verify that admin is able to see Transactions in 'E-File Import Transactions' screen for edited records after file import");
		
		objApasGenericFunctions.logout();
	}
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE Valuation Factor File>
	 * 1. Validating the successful uploading of data file with valid format 
	 * 2. Approving all records
	 * 3. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T111: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void BppTrend_BOEValImportAndApprove(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);				
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VAL_FACTORS_VALID;
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYearForImport, fileName + "BOE Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Step8: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
				
		//Step9: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step10: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step11: Select File Type and File Source
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");
		
		//Step12: Verify the status of File Imported is updated to Approved
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T111: Validation if status of imported file is approved.");

		//Step13: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step14: Import Logs grid validation for the imported BOE - Index and Percent Good Factors file
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Approved", "SMAB-T111: Validate if status of imported file is approved on import logs page");
		
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA - Valuation Factors File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 2. Validating Discard functionality of error records
	 * 3. Validating no of Error records post discarding error records
	 */
	@Test(description = "SMAB-T106,SMAB-T111,SMAB-T79: Discarding error records and reverting import for BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","BPPTrend"})
	public void BppTrend_CAAValFileImportAndDiscardErrorRecords(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Fetch the excel file imported
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;		
				
		//Step5: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYearForImport, fileName + "CAA Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Step8: Opening the Efile Import Logs module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step9: Import Logs grid validation for the imported BOE - Index and Percent Good Factors file
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"BPP Trend Factors :CAA - Valuation Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import logs page");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Imported", "SMAB-T111: Validation if status of imported file is imported on import logs page");
		
		//Step10: Opening the Efile Import Transactions module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		
		HashMap<String, ArrayList<String>> importTransactionsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step11: Import Transactions grid validation for the imported BOE - Index and Percent Good Factors file
		softAssert.assertEquals(importTransactionsGridData.get("E-File Import Log").get(0),"BPP Trend Factors :CAA - Valuation Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import transactions page");
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported", "SMAB-T111: Validation if status of imported file is imported on import transactions page");
		
		//Step12: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step13: Selecting the File Type and Source and clicking on 'View All' link 
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		//Step14: Iterate over all the tables
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(BPPTablesData.EFILE_IMPORT_PAGE_CAA_VAL_TABLES_NAMES.split(",")));	
		for (int i = 0; i < allTables.size(); i++) {
						
			//Step15: Clicking on the table
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName);

		//********* Validating that correct number of records are present in error row section after file import *********
			String expectedNoOfErrorRecords = "2";    //Imported File has 2 error records in each table			
			//Step16: Validation of number of records in error row section
			String actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			softAssert.assertEquals(actualNoOfErrorRecords, expectedNoOfErrorRecords, "SMAB-T955,SMAB-T956,SMAB-T106: Validate if correct number of records are displayed in Error Row Section after file import");
			
		//********* Validating that correct number of records are present in Imported row section after file import *********	
			//Step17: Counting no of records imported from Excel File Imported			
			int countOfTotalRecordsInt = objEfileHomePage.getRowCountSpecificToTable(fileName + "CAA Valuation Factors 2021.xlsx",tableName);
			
			//Step18: Fetching expected Imported Rows Count
			String expectedImportedRowsCount = Integer.toString(countOfTotalRecordsInt - Integer.parseInt(expectedNoOfErrorRecords));			
			
			//Step19: Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file					
			String actualImportedRowsCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			softAssert.assertEquals(actualImportedRowsCount, expectedImportedRowsCount, "SMAB-T955,SMAB-T956,SMAB-T111: Validation if correct number of records are displayed in Imported Row Section after file import");
			
		//********* Validate Discard Functionality *********	
			//Step20: Validation for Records discard functionality from Review and Approve Page
			ReportLogger.INFO("SMAB-T111: Validate that error records can be discarded from Review and Approve Data Page");
			objEfileHomePage.discardErrorRecords("1");
			
			//Step21: validating the number of records in the error row section after discarding a record
			int updatedCount = Integer.parseInt(expectedNoOfErrorRecords) - 1;
			actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);		
			softAssert.assertEquals(actualNoOfErrorRecords, updatedRecordsInErrorRowPostDelete, "SMAB-T956,SMAB-T111: Validation if correct number of records are displayed in Error Row Section after discarding a record");
		}

		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA - Valuation Factors File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 2. Validating revert functionality of error records
	 * 3. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T106,SMAB-T111,SMAB-T79: Discarding error records and reverting import for BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","BPPTrend"})
	public void BppTrend_CAAValFileImportAndRevert(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Fetch the excel file imported
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;		
				
		//Step5: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYearForImport, fileName + "CAA Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
				
		//Step8: Selecting the File Type and Source and clicking on 'View All' link 
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);

		//Step9: Reverting the imported file
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step10: Validation of the file status after reverting the imported file
		ReportLogger.INFO("Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");

		//Step11: Status of the imported file should be changed to Reverted as the whole file is reverted
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T955,SMAB-T956,SMAB-T111: Validation if status of imported file is reverted.");

		//Step12: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"BPP Trend Factors :CAA - Valuation Factors :" + rollYearForImport,"SMAB-T111: Validation for name of imported file on import logs page");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Reverted", "SMAB-T955,SMAB-T956,SMAB-T111: Validation if status of imported file is reverted on import logs page");
				
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA - Valuation Factors File>
	 * 1. Validating the successful uploading of data file with valid format 
	 * 3. Correcting some of the error records
	 * 4. Validating retry functionality of error records
	 * 5. Validating no of records in Error and Imported rows section post retrying
	 */
	@Test(description = "SMAB-T111,SMAB-T958: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void BppTrend_CAAValImportAndRetryErrorRecords(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);				
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYearForImport, fileName + "CAA Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Validation of number of times tried retried column for the imported file. Expected is 1 as it has not be retried/reverted yet
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T111: Validate if number of times try/retry count is correct on file import");			
		//Validations for Total file count before retrying error records		
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "123", "SMAB-T111: Validating total records count before retrying errorred records");
		//Validations for Imported count before retrying error records	
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "117", "SMAB-T111: Validating import count before retrying errorred records");		
		//Validations for Error count before retrying error records
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.errorRecordsImportedFile), "6", "SMAB-T111: Validating error count before retrying errorred records");		
		
		//Step8: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		Map<String, Integer> expectedImportedRowsCountBeforeRetry = new HashMap<String, Integer>();
		Map<String, Integer> expectedErrorRowsCountBeforeRetry = new HashMap<String, Integer>();
		String importedRowsCount, errorRowCount;		
		
		//Step9: Iterate over all the tables / columns
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(BPPTablesData.EFILE_IMPORT_PAGE_CAA_VAL_TABLES_NAMES.split(",")));	
		
		for (int i = 0; i < allTables.size(); i++) {
			//Step10: Clicking on the given table name
			String tableName = allTables.get(i);			
			objBppTrend.clickOnTableOnBppTrendPage(tableName);

			//Step11: Store Error and Imported records count for each table in a map to verify count after Retrying error records 
			importedRowsCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			expectedImportedRowsCountBeforeRetry.put(tableName, Integer.parseInt(importedRowsCount));
			
			errorRowCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");		
			expectedErrorRowsCountBeforeRetry.put(tableName, Integer.parseInt(errorRowCount));
			
			//Step12: Enter correct data in one of the Error Row 'Cell' 
			ReportLogger.INFO("Deleting junk data and entering valid data in the table: "+tableName);
			objApasGenericFunctions.editGridCellValue("Valuation Factor","80");
			objPage.Click(objEfileHomePage.rowSelectCheckBox);
			objEfileHomePage.collapseSection(objEfileHomePage.errorRowSectionExpandButton);
			objPage.scrollToTop();
		}
		
		//Step13: Clicking retry button and waiting for corrected records to move to imported section
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton);
		objPage.javascriptClick(objEfileHomePage.retryButton);
		objPage.waitUntilElementIsPresent(objEfileHomePage.xpathSpinner,20);
		objPage.waitForElementToDisappear(objEfileHomePage.xpathSpinner,50);
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton,20);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,160);
		
		//Step14: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName);
			
			//Step15: Validate that correct no.of records are remain in Error row section after retry
			String actualErrorRowsAfterRetrying = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
			String expectedErrorRowCountAfterRetry = Integer.toString(expectedErrorRowsCountBeforeRetry.get(tableName)-1);
			softAssert.assertEquals(actualErrorRowsAfterRetrying, expectedErrorRowCountAfterRetry, "SMAB-T958,SMAB-T111: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			
			//Step16: Validate that corrected no. of records are moved to Imported row section after retry
			String actualImportedRowsAfterRetrying = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
			String expectedImportedRowCountAfterRetry = Integer.toString(expectedImportedRowsCountBeforeRetry.get(tableName)+1);
			softAssert.assertEquals(actualImportedRowsAfterRetrying, expectedImportedRowCountAfterRetry, "SMAB-T958,SMAB-T111: Validation if correct number of records are displayed in Imported Row Section under "+ tableName +" after correcting and retrying the error record");
		}
		
		objApasGenericFunctions.logout();
	}
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA - Valuation Factors File>
	 * 1. Validating the successful uploading of data file with valid format
	 * 2. Approving all records
	 * 3. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T111: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void BppTrend_CAAValImportAndApprove(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);			
				
		//Step2: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step3: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step4: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VAL_FACTORS_VALID;
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYearForImport, fileName + "CAA Valuation Factors 2021.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Step8: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
				
		//Step9: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step10: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step11: Select File Type and File Source
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");
		
		//Step12: Verify the status of File Imported is updated to Approved
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T111: Validation if status of imported file is approved.");

		//Step13: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);		
		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		
		//Step14: Import Logs grid validation for the imported CAA - Valuation Factors file
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Approved", "SMAB-T111: Validate if status of imported file is approved on import logs page");
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import>::
	 * 1. Validating the restrictions on uploading file with .CSV format:: TestCase/JIRA ID: SMAB-T112
	 * 2. Validating the restrictions on uploading file with .TXT format:: TestCase/JIRA ID: SMAB-T112
	 * 3. Validating the restrictions on uploading file with .XLS format:: TestCase/JIRA ID: SMAB-T112
	 */
	@Test(description = "SMAB-T112: Validating restrictions on uploading BPP Trends data files in invalid format", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","BPPTrend"})
	public void BppTrend_ImportWithInvalidFormat(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business administrator)
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend BOE Index Factors file having invalid format XLS
		ReportLogger.INFO("* Validating upload for file with XLS format");
		String xlsFormatFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_XLS;
		objEfileHomePage.uploadInvalidFormatFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, xlsFormatFile);
		Thread.sleep(1000);
		objPage.waitForElementToBeClickable(objEfileHomePage.doneButton);
		objPage.Click(objEfileHomePage.doneButton);
		Thread.sleep(5000);
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "File Failed", 160);
		
		//Step4: Verify the status of BOE Index Factors file having invalid format XLS is "File Failed"
		softAssert.assertEquals(objBppTrend.getElementText(objEfileHomePage.statusImportedFile), "File Failed", "SMAB-T112: Validation for incorrect file format");
		
		//Step5: Uploading the BPP Trend BOE Index Factors file having invalid format CSV
		ReportLogger.INFO("* Validating upload for file with CSV format");
		String csvFormatFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_CSV;
		objEfileHomePage.uploadInvalidFormatFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, csvFormatFile);		
		objBppTrend.waitForElementToBeVisible(objBppTrend.errorMsgOnImportForInvalidFileFormat, 10);
		
		//Step6: Verify error message after uploading BOE Index Factors file having invalid format CSV
		String actualMsgFrInvalidFrmat = objBppTrend.getElementText(objBppTrend.errorMsgOnImportForInvalidFileFormat);
		softAssert.assertEquals(actualMsgFrInvalidFrmat, "Your company doesn't support the following file types: .csv", "SMAB-T112: Validation for incorrect (CSV) file format");
		objBppTrend.Click(objBppTrend.closeButton);
		
		//Step7: Uploading the BPP Trend BOE Index Factors file having invalid format TXT
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Validating upload for file with TXT format");
		String txtFormatFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_TXT;
		objEfileHomePage.uploadInvalidFormatFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, txtFormatFile);		
		objBppTrend.waitForElementToBeVisible(objBppTrend.errorMsgOnImportForInvalidFileFormat, 10);
		
		//Step8: Verify error message after uploading BOE Index Factors file having invalid format TXT
		actualMsgFrInvalidFrmat = objBppTrend.getElementText(objBppTrend.errorMsgOnImportForInvalidFileFormat);
		softAssert.assertEquals(actualMsgFrInvalidFrmat, "Your company doesn't support the following file types: .txt", "SMAB-T112: Validation for incorrect (TXT) file format");
		objBppTrend.Click(objBppTrend.closeButton);
		
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import>::
	 * 1. Validating transformation rules on Year, Age and Average columns:: Test Case/JIRA ID: SMAB-T105
	 * 		a. Duplicate records
	 * 		b. Alphanumeric Values
	 * 		c. Special character values
	 * 		d. Year not in YYYY format
	 * 		e. Year less than 1974 and greater than roll year selected
	 * 		. Year left blank
	 * 		h. Age is greater than 40 and less than 1
	 * 		i. Index / Average value beyond range
	 */
	@Test(description = "SMAB-T105: Validating transformation rules on BOE Index file import", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void BppTrend_TransformationRules_On_BoeIndexAndPercentFactors_Import(String loginUser) throws Exception {
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step2: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);
		
		//Step3: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);				
		
		//Step4: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_TRANSFORMATION_RULES;
		
		//Step6: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, fileName + "BOE Equipment Index Factors and Percent Good Factors Sample_Transformation Rules.xlsx");		
		
		//Step7: Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Step8: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		//Step9: Comparing the data from the error row table with the expected data	
		
		ReportLogger.INFO("Error Message Validation of Error Row Records in table: 'Commercial Equipment Index' on Review and Approve Data Page");
		objBppTrend.clickOnTableOnBppTrendPage("Commercial Equipment Index");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("^&$"),"Field avg, found ^&$ but expected a number greater than 0","SMAB-T105 : Error Message validation for the scenario 'Special Characters in Average'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("abcd"),"Field avg, found abcd but expected a number greater than 0","SMAB-T105 : Error Message validation for the scenario 'Alphabets in Average'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("-110"),"Field avg, found -110 but expected a number greater than 0","SMAB-T105 : Error Message validation for the scenario 'Negative Number in Average'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("1X0Y6Z"),"Field avg, found 1X0Y6Z but expected a number greater than 0","SMAB-T105 : Error Message validation for the scenario 'Alpha Numeric characters in Average'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("0"),"Index Factor should be 1 or more","SMAB-T105 : Error Message validation for the scenario '0 Number in Average'");
		
		ReportLogger.INFO("Error Message Validation of Error Row Records in table: 'Agricultural Index' on Review and Approve Data Page");
		objBppTrend.clickOnTableOnBppTrendPage("Agricultural Index");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("XYZ"),"Field year, found XYZ but expected a valid year\r\nField year, found XYZ but expected it to be less than 2021","SMAB-T105 : Error Message validation for the scenario 'Alphabets in Year'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("%_&#"),"Field year, found %_&# but expected a valid year","SMAB-T105 : Error Message validation for the scenario 'Special Characters in Year'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("20"),"Field year, found 20 but expected greater than 1973","SMAB-T105 : Error Message validation for the scenario 'Numbers in Year'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("-2014"),"Field year, found -2014 but expected a valid year","SMAB-T105 : Error Message validation for the scenario 'Negative Number in Year'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("2013AB"),"Field year, found 2013AB but expected a valid year","SMAB-T105 : Error Message validation for the scenario 'Alpha Numeric characters in Year'");
		softAssert.assertContains(objEfileHomePage.getErrorMessageFromErrorGrid("2015"),"DUPLICATE_VALUE:duplicate value found: Identifier__c duplicates value on record with id:","SMAB-T105 : Error Message validation for the scenario 'Duplicate Year Acquired in Year'");
		softAssert.assertTrue((!objEfileHomePage.getErrorMessageFromErrorGrid("2015").contains(":--")),"SMAB-T105 : Error Message validation for the scenario 'Duplicate Year Acquired in Year' contains ':--' ");
		
		ReportLogger.INFO("Error Message Validation of Error Row Records in table: 'Agricultural ME Good Factors' on Review and Approve Data Page");
		objBppTrend.clickOnTableOnBppTrendPage("Agricultural ME Good Factors");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("0"),"Field Age, found 0 but expected a number between 1.0 and 40.0","SMAB-T105 : Error Message validation for the scenario '0 Number in Age'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("41"),"Field Age, found 41 but expected a number between 1.0 and 40.0","SMAB-T105 : Error Message validation for the scenario 'Number greater than 40 in Age'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid(""),"Age, found blank but expected a number between 1.0 and 40.0","SMAB-T105 : Error Message validation for the scenario 'Blank Value in Age'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("-4"),"Field Age, found -4 but expected a number between 1.0 and 40.0","SMAB-T105 : Error Message validation for the scenario 'Negative Number in Age'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("A"),"Field Age, found A but expected a number between 1.0 and 40.0","SMAB-T105 : Error Message validation for the scenario 'Alphabets in Age'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("#$"),"Field Age, found #$ but expected a number between 1.0 and 40.0","SMAB-T105 : Error Message validation for the scenario 'Special Characters in Age'");
		softAssert.assertEquals(objEfileHomePage.getErrorMessageFromErrorGrid("7B"),"Field Age, found 7B but expected a number between 1.0 and 40.0","SMAB-T105 : Error Message validation for the scenario 'Alpha Numeric characters in Age'");
		
		objApasGenericFunctions.logout();
	}
	
	

	/**
	 * This method is to verify File is not imported for already approved file
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T974,SMAB-T951,SMAB-T954:Verify user is not able to import a file for BPP Trends if the previous Import for a particular File Type, File Source and Period was Approved", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {
		"regression","EFileImport" })
	public void BPPTrends_VerifyAlreadyApprovedFileForSamePeriodIsNotImportedAgain(String loginUser) throws Exception{
		//String rollYearForImport = "2021";
		String fileType="BPP Trend Factors";
		String source="BOE - Valuation Factors";

		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYearForImport);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYearForImport);

		String boebppTrendIndexFactorsFile=System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS_VALID_DATA+"BOE Valuation Factors 2021.xlsx";
		objApasGenericFunctions.login(loginUser);

		//Step1: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and Import_Period__C='" + rollYearForImport + "' and File_Source__C ='"+source+"' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Step2: Opening the E FILE IMPORT Module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		///step3: importing a file
		objEfileHomePage.uploadFileOnEfileIntake(fileType, source,rollYearForImport,boebppTrendIndexFactorsFile);
		
		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 400);
		
		//step5: verifying import log and transactions is non editable
		ReportLogger.INFO("verifying import log generated is non editable");
		objApasGenericFunctions.openLogRecordForImportedFile(fileType,source,rollYearForImport,boebppTrendIndexFactorsFile);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.logStatus, 10);
		softAssert.assertTrue(objApasGenericFunctions.isNotDisplayed(objEFileImportLogPage.inlineEditButton),"SMAB-T954:Verify that User is able to view and not edit the log records after uploading the BPP Trend e-Files");
		ReportLogger.INFO("verifying transaction log generated is non editable");
		objPage.Click(objEfileImportTransactionsPage.transactionsTab);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.viewAlllink, 10);
		objPage.javascriptClick(objEfileImportTransactionsPage.transactionsRecords.get(0));
		objPage.waitForElementToBeClickable(objEfileImportTransactionsPage.statusLabel, 10);
		softAssert.assertTrue(objApasGenericFunctions.isNotDisplayed(objEFileImportLogPage.inlineEditButton),"SMAB-T951:Verify that User is able to view and not edit the transaction records after uploading the BPP Trend e-Files");
			
		
		//step6: approving the imported file
		objPage.javascriptClick(objEfileHomePage.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEfileHomePage.fileTypedropdown, 10);
		objEfileHomePage.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEfileHomePage.nextButton, 15);
		objPage.Click(objEfileHomePage.viewLinkRecord);
		ReportLogger.INFO("Approving the imported file");
		objPage.waitForElementToBeClickable(objEfileHomePage.errorRowSection, 20);
		objPage.waitForElementToBeClickable(objEfileHomePage.approveButton, 10);
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);
		
		//step7: trying to upload a file for the same file type ,source and rollYearForImport
		objPage.Click(objEfileHomePage.sourceDetails);
		objPage.waitForElementToBeClickable(objEfileHomePage.statusImportedFile,30);
		objPage.Click(objEfileHomePage.nextButton);
		objApasGenericFunctions.selectFromDropDown(objEfileHomePage.periodDropdown, rollYearForImport);
		
		//step8: verifying error message while trying to import file for already approved file type,source and rollYearForImport
		softAssert.assertContains(objPage.getElementText(objEfileHomePage.fileAlreadyApprovedMsg), "This file has been already approved", "SMAB-T974:Verify user is not able to import a file for BPP Trends if the previous Import for a particular File Type, File Source and Period was Approved");
		objPage.Click(objEfileHomePage.closeButton);
		
		objApasGenericFunctions.logout();
	}
	
	

	/**
	 * This method is to verify records count in import logs
	 * @param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T83,SMAB-T84,SMAB-T1458:Verify user is able to see number of records count from file import action on 'E-File Import Logs' screen", dataProvider = "loginBusinessAdmin",dataProviderClass = DataProviders.class, groups = {
		"regression","EFileImport" })	
	public void BPPTrends_VerifyImportedLogsTransactionsRecordCountAndTrailFields(String loginUser) throws Exception{
		String uploadedDate = objUtil.getCurrentDate("MMM d, YYYY");
		
		//String rollYearForImport = "2021";
		String fileType="BPP Trend Factors";
		String source="BOE - Valuation Factors";
		String boebppTrendIndexFactorsFile=System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS+"BOE Valuation Factors 2021.xlsx";
		String boeTableErrorRecords=System.getProperty("user.dir") + testdata.BOE_ERRORREOCRDS_COUNT+"BOEErrorReocrdsCountFile.json";

		Map<String, String> errorRecordsCount = objUtil.generateMapFromJsonFile(boeTableErrorRecords, "ErrorRecordsTableWise");
		
		//Step1: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and Import_Period__C='" + rollYearForImport + "' and File_Source__C ='"+source+"' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);
				
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		
		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileHomePage.uploadFileOnEfileIntake(fileType,source, rollYearForImport ,boebppTrendIndexFactorsFile);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 600);
		
		
		//step6: verify import list record entry and data
		ReportLogger.INFO("Verifying records count in history list for imported record");
		
		HashMap<String, ArrayList<String>> importedEntry=objApasGenericFunctions.getGridDataInHashMap(1, 1);				
		softAssert.assertEquals(importedEntry.get("Uploaded Date").get(0), uploadedDate, "verify import list history data");
		softAssert.assertEquals(importedEntry.get("Period").get(0), rollYearForImport, "verify import list history data");
		softAssert.assertEquals(importedEntry.get("File Count").get(0), errorRecordsCount.get("TotalFileRecords"), "verify import list history data");
		softAssert.assertEquals(importedEntry.get("Import Count").get(0), errorRecordsCount.get("TotalImportedRecords"), "verify import list history data");
		softAssert.assertEquals(importedEntry.get("Error Count").get(0), errorRecordsCount.get("TotalErrorRecords"), "verify import list history data");
		softAssert.assertEquals(importedEntry.get("Discard Count").get(0), "0", "verify import list history data");
		softAssert.assertEquals(importedEntry.get("Number of Tries").get(0), "1", "verify import list history data");
				
		
		//step7: navigating to EFile import logs screen and verifying the records count
		ReportLogger.INFO("Verifying Import count,File count, Error count and status of import logs record");
		objApasGenericFunctions.openLogRecordForImportedFile(fileType,source,rollYearForImport,boebppTrendIndexFactorsFile);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.logStatus, 10);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logFileCount),errorRecordsCount.get("TotalFileRecords"), "SMAB-T83:Verify that admin is able to see logs record for file type with status 'Imported' on 'E-File Import Logs' screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logImportCount),errorRecordsCount.get("TotalImportedRecords"), "SMAB-T83:Verify that admin is able to see logs record for file type with status 'Imported' on 'E-File Import Logs' screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logErrorCount),errorRecordsCount.get("TotalErrorRecords"), "SMAB-T83:Verify that admin is able to see logs record for file type with status 'Imported' on 'E-File Import Logs' screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"Imported", "SMAB-T83:Verify that admin is able to see logs record for file type with status 'Imported' on 'E-File Import Logs' screen");
		
		
		ReportLogger.INFO("Verifying Import count,File count and Error count in Import Transaction record");
		objPage.Click(objEfileImportTransactionsPage.transactionsTab);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.viewAlllink, 10);
		String expectedTransactionID=objPage.getElementText(objEfileImportTransactionsPage.transactionsRecords.get(0));
		objPage.javascriptClick(objEfileImportTransactionsPage.transactionsRecords.get(0));
		objPage.waitForElementToBeClickable(objEfileImportTransactionsPage.statusLabel, 10);
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.transactionErrorCount),errorRecordsCount.get("TotalErrorRecords"), "SMAB-T84:Verify that admin is able to see Transactions record for file type with status 'Imported' on 'E-File Import Transaction' screen");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel),"Imported", "SMAB-T84:Verify that admin is able to see Transactions record for file type with status 'Imported' on 'E-File Import Transaction' screen");
		
		ReportLogger.INFO("Verifying fileds for Transaction/Audit trail record");
		objPage.javascriptClick(objEfileImportTransactionsPage.transactionTrailTab);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.viewAlllink, 10);
		objPage.javascriptClick(objEfileImportTransactionsPage.transactionTrailRecords.get(0));
		objPage.waitForElementToBeClickable(objEfileImportTransactionsPage.statusLabel, 10);
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.transactionType), "Transactions", "SMAB-T1458:Verify that User is able to see Transactions trail with updated fields once a BPP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.transactionSubType), "E-File Intake", "SMAB-T1458:Verify that User is able to see Transactions trail with updated fields once a BPP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.transactionDescription), "Factor uploaded - Electronic File Intake", "SMAB-T1458:Verify that User is able to see Transactions trail with updated fields once a BPP file is uploaded via EFile");		
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.efileImportTransactionLookUp), expectedTransactionID, "SMAB-T1458:Verify that User is able to see Transactions trail with updated fields once a BPP file is uploaded via EFile");
		
		objApasGenericFunctions.logout();
	}
	
	
}