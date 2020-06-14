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
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
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
	Util objUtil;
	SoftAssertion softAssert;
	int rollYear;
	String rollYearForImport;
	SalesforceAPI objSalesforceAPI;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		
		if(driver==null) {
            setupTest();
            driver = BrowserDriver.getBrowserInstance();
        }
		
		objPage = new Page(driver);
		objBppTrend = new BppTrendPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = Integer.parseInt(CONFIG.getProperty("rollYear"));
		rollYear = rollYear - 1;
		rollYearForImport = Integer.toString(rollYear); 
		objSalesforceAPI = new SalesforceAPI();
	}
		
	@AfterMethod
	public void afterMethod() throws IOException, InterruptedException {
		//objApasGenericFunctions.logout();
	}
	

	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Validating revert functionality of error records
	 * 4. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T106,SMAB-T111: Discarding error records and reverting import for BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void verify_BppTrend_BoeIndexFileImportDiscardErrorRecordsAndRevert(String loginUser) throws Exception {
		//Step1: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Resetting the composite factor tables status
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYearForImport);

		//Resetting the valuation factor tables status
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYearForImport);
		
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step4: Read the excel file and imported retrieve row counts from
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
		Map<String, Object> mapWithExpectedImportedRowForAllTables = objBppTrend.getTotalRowsCountFromExcelForGivenTable(fileName, rollYearForImport);
		
		//Step5: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 180);
		
		//Step6: Checking the status in import logs
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T111: Validation if status of imported file is imported on import logs page.");

		//Step7: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is imported on transactions page.");
		
		//Step8: Navigating back to history table and clicking view link to navigate to review and approve page
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step9: Store columns outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("BoeIndexTablesOutsideMoreTabOnImportPage").split(",")));
		String tableNamesUnderMoreTab = CONFIG.getProperty("BoeIndexTablesUnderMoreTabOnImportPage");
		allTables.addAll(Arrays.asList(tableNamesUnderMoreTab.split(",")));
		
		//Step10: Iterate over all the columns
		for (int i = 0; i < allTables.size(); i++) {
			//Step10: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			
			System.setProperty("tableNumber", tableNumber);
			
			//Step11: Clicking on the given table name
			String tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			
			//Step12: Validating that correct number of records are moved to error and imported row sections after file import			
			String errorRecords = CONFIG.getProperty("errorRecordsCount");

			//String countOfTotalRecordsStr = mapWithExpectedImportedRowForAllTables.get(tableNameToRetrieveExpImportedRowsCount).toString();
			String countOfTotalRecordsStr = mapWithExpectedImportedRowForAllTables.get(tableName).toString();
			int countOfTotalRecordsInt = Integer.parseInt(countOfTotalRecordsStr);
			String expectedImportedRowsCount = Integer.toString(countOfTotalRecordsInt - Integer.parseInt(errorRecords));
			boolean isErrorRowSectionDisplayed = objBppTrend.checkPresenceOfErrorRowsSection();
			boolean isImportedRowSectionDisplayed = objBppTrend.checkPresenceOfImportedRowsSection();;
			
			if (isErrorRowSectionDisplayed){
				//Step13: Validation of number of records in error row section.
				String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection();
				softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T106: Validation if correct number of records are displayed in Error Row Section after file import");
			} else {
				//Step14: Validating if the error row section is coming on review and approve page after clicking "View Link" from history table 
				softAssert.assertTrue(false, "SMAB-T106: Validation for Error Row Section presence after clicking view link button");	
			}
			
			if (isImportedRowSectionDisplayed){
				//Step15: Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file
				String actualImportedRowsCount = objBppTrend.getCountOfRowsFromImportedRowsSection();
				softAssert.assertEquals(actualImportedRowsCount, expectedImportedRowsCount, "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section after file import");
			} else {
				//Step16: Validating if the imported row section is coming on review and approve page after clicking "View Link" from history table
				softAssert.assertTrue(false, "SMAB-T111: Validation for Imported Row Section presence after clicking view link button");	
			}				

			//Step17: Validation for Records discard functionality from Review and Approve Page
			ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T111: Validation that error records can be dicarded from Review and Approve Data Page");
			objBppTrend.discardIndividualErrorRow();

			int updatedCount = Integer.parseInt(errorRecords) - 1;
			String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection("ERROR ROWS : " + updatedCount);
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);
			//Step18: validating the number of records in the error row section after discarding a record
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after discarding a record");

			if(Integer.parseInt(numberOfRecordsInErrorRowSection) > 0) {
				objBppTrend.discardAllErrorRows();
			}		
		}

		//Step19: Reverting the imported file
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step20: Validation of the file status after reverting the imported file
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");

		//Step21: Status of the imported file should be changed to Reverted as the whole file is reverted
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T111: Validation if status of imported file is reverted.");

		//Step22: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T111: Validation if status of imported file is reverted on import logs page.");

		//Step23: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is reverted on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 3. Correcting all the error records
	 * 4. Validating retry functionality of error records
	 * 5. Approving all records
	 * 6. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T111: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void verify_BppTrend_BoeIndexImportRetryErrorRecordsAndApprove(String loginUser) throws Exception {
		//Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Resetting the composite factor tables status
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYearForImport);

		//Resetting the valuation factor tables status
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYearForImport);
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business administrator or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 180);

		//Step4: Validations for file count, import count and error count before retrying all errorred records
		Map<String, Object> dataMapWthRowCounts = objBppTrend.countOfDifferentRowTypesInExcel(fileName, rollYearForImport);
		int fileCountFromExcel = (int)dataMapWthRowCounts.get("File Count");
		int importCountFromExcel = (int)dataMapWthRowCounts.get("Import Count");
		int errorCountFromExcel = (int)dataMapWthRowCounts.get("Error Count");
		
		int fileCountBeforeRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T111: Validating file count before retrying errorred records");
		int importCountBeforeRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T111: Validating import count before retrying errorred records");
		int errorCountBeforeRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());			
		softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T111: Validating error count before retrying errorred records");		
		
		//Step5: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step6: Store columns / tables outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("BoeIndexTablesOutsideMoreTabOnImportPage").split(",")));
		String tableNamesUnderMoreTab = CONFIG.getProperty("BoeIndexTablesUnderMoreTabOnImportPage");
		allTables.addAll(Arrays.asList(tableNamesUnderMoreTab.split(",")));
		
		Map<String, Integer> dataMapForExpImportedRowsCountBeforeRetry = new HashMap<String, Integer>();
		String importedRowsCount;
		
		//Step7: Iterate over all the tables / columns
		for (int i = 0; i < allTables.size(); i++) {
			//Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			
			importedRowsCount = objBppTrend.getCountOfRowsFromImportedRowsSection();
			dataMapForExpImportedRowsCountBeforeRetry.put(tableName, Integer.parseInt(importedRowsCount));
			
			int valueToEnter = 80;
			int numberOfErrorRecordsUnderCurrentTable = Integer.parseInt(objBppTrend.getCountOfRowsFromErrorRowsSection());
			
			//Step9: Iterating over all the tables to correct the invalid data 
			for(int j = 1; j <= numberOfErrorRecordsUnderCurrentTable; j++) {
				//Step10: Editing the value of average column
				valueToEnter = valueToEnter - 1;
				ExtentTestManager.getTest().log(LogStatus.INFO, "Deleting junk data and entering valid data in the table");
				objBppTrend.updateCorrectDataInTable(tableNumber, Integer.toString(valueToEnter));
			}			
		}
		
		//Step14: Clicking retry button and waiting for corrected records to move to imported section
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton);
		objPage.javascriptClick(objEfileHomePage.retryButton);
		objBppTrend.waitForPageSpinnerToDisappear();
		
		//Step15: Re navigating to retry and approve page
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		objPage.Click(objEfileHomePage.viewLink);
		
		int errorredRowsBeforeRetrying = Integer.parseInt(CONFIG.getProperty("errorRecordsCount"));
		int expImportedRowsBeforeRetrying;
		
		//Step16: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step17: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			
			String errorredRowsAfterRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
			String actualImportedRowsAfterRetrying = objBppTrend.getCountOfRowsFromImportedRowsSection();
			
			//Step19: Validating that corrected records are moved to imported row section after retry.
			softAssert.assertEquals(errorredRowsAfterRetrying, "0", "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			
			expImportedRowsBeforeRetrying = dataMapForExpImportedRowsCountBeforeRetry.get(tableName);
			int expImportedRowsAfterRetrying = expImportedRowsBeforeRetrying + errorredRowsBeforeRetrying;
			softAssert.assertEquals(actualImportedRowsAfterRetrying, Integer.toString(expImportedRowsAfterRetrying), "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section under "+ tableName +" after correcting and retrying the error record");
		}
		
		//Step20: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step21: Searching the efile intake module to validate the status of the imported file after approve on history table
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T111: Validation if status of imported file is approved.");

		//Step22: Validations to check file count after successfully retrying and approving all error records in history table
		int fileCountAfterRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		int expFileCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(fileCountAfterRetrying, expFileCountAfterRetrying, "SMAB-T111: Validating file count after successfully retrying all errorred records");
		
		//Step23: Validations to check import count after successfully retrying and approving all error records in history table
		int importCountAfterRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		int expImportCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(importCountAfterRetrying, expImportCountAfterRetrying, "SMAB-T111: Validating import count after successfully retrying all errorred records");
		
		//Step24: Validations to check error count after successfully retrying and approving all error records in history table
		int errorCountAfterRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());		
		softAssert.assertEquals(errorCountAfterRetrying, 0, "SMAB-T111: Validating error count after successfully retrying all errorred records");
		
		//Step25: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Approved", "SMAB-T111: Validation if status of imported file is approved on import logs page.");

		//Step26: Checking the status on import logs details page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportLogDetailsPage = objBppTrend.getFieldValuesFromImportLogsDetailsPage("BOE - Index and Percent Good Factors", "Status");
		softAssert.assertEquals(fileStatusOnImportLogDetailsPage, "Approved", "SMAB-T111: Validation if status of imported file is approved on import logs details page.");		
		
		//Step27: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is approved on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
		
	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE VALUATION File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Validating revert functionality of error records
	 * 4. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T106,SMAB-T111: Discarding error records and reverting import for BOE valuation file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void verify_BppTrend_BoeValFileImportDiscardErrorRecordsAndRevert(String loginUser) throws Exception {
		//Step1: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Resetting the composite factor tables status
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYearForImport);

		//Resetting the valuation factor tables status
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYearForImport);
		
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step4: Setting up filePath
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYearForImport, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 180);
		
		//Step6: Checking the status in import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T111: Validation if status of imported file is imported on import logs page.");

		//Step7: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is imported on transactions page.");

		//Step8: Navigating back to history table and clicking view link to navigate to review and approve page
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step9: Store columns outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("BoeValuationTablesOnImportPage").split(",")));
		
		//Step10: Iterate over all the columns
		for (int i = 0; i < allTables.size(); i++) {
			//Step10: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step11: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			
			//Step12: Validating that correct number of records are moved to error and imported row sections after file import			
			String errorRecords = CONFIG.getProperty("errorRecordsCount");
			String actualSuccessRecords = objBppTrend.getCountOfRowsFromImportedRowsSection();
			
			boolean isErrorRowSectionDisplayed = objBppTrend.checkPresenceOfErrorRowsSection();
			boolean isImportedRowSectionDisplayed = objBppTrend.checkPresenceOfImportedRowsSection();;
			
			if (isErrorRowSectionDisplayed){
				//Step13: Validation of number of records in error row section.
				String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection();
				softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T106: Validation if correct number of records are displayed in Error Row Section after file import");
			} else {
				//Step14: Validating if the error row section is coming on review and approve page after clicking "View Link" from history table 
				softAssert.assertTrue(false, "SMAB-T106: Validation for Error Row Section presence after clicking view link button");	
			}
			
			if (isImportedRowSectionDisplayed){
				String expectedSuccessRecords = null;
				String strTotalCount;
				int intTotalCount = 0;
				//Generating expected number of records for various BOE Valuation tables
				if(tableName.equalsIgnoreCase("Computer Val Factors") || tableName.equalsIgnoreCase("Semiconductor Val Factors")) {
					objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYearForImport, tableName);
					strTotalCount = System.getProperty("totalRecordsCount");
					int numberOfDataColumns = Integer.parseInt(CONFIG.getProperty("dataColumnsInComputeAndSemiConductorValTables"));
					intTotalCount = Integer.parseInt(strTotalCount) * numberOfDataColumns;
					intTotalCount = intTotalCount - Integer.parseInt(errorRecords);
					expectedSuccessRecords = Integer.toString(intTotalCount);
				} else if(tableName.equalsIgnoreCase("Biopharmaceutical Val Factors")) {
					objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYearForImport, tableName);
					strTotalCount = System.getProperty("totalRecordsCount");
					int numberOfDataColumns = Integer.parseInt(CONFIG.getProperty("dataColumnsToBeApprovedInBioPharmaTable"));
					intTotalCount = Integer.parseInt(strTotalCount) * numberOfDataColumns;
					intTotalCount = intTotalCount - Integer.parseInt(errorRecords);
					expectedSuccessRecords = Integer.toString(intTotalCount);
				} else {
					expectedSuccessRecords = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYearForImport, tableName);
				}
				
				//Step15: Validation of number of records in imported row section.
				softAssert.assertEquals(expectedSuccessRecords, actualSuccessRecords, "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section after file import");
			} else {
				//Step16: Validating if the imported row section is coming on review and approve page after clicking "View Link" from history table
				softAssert.assertTrue(false, "SMAB-T111: Validation for Imported Row Section presence after clicking view link button");	
			}				

			//Step17: Validation for Records discard functionality from Review and Approve Page
			ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T111: Validation that error records can be dicarded from Review and Approve Data Page");
			objBppTrend.discardIndividualErrorRow();

			int updatedCount = Integer.parseInt(errorRecords) - 1;
			String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection("ERROR ROWS : " + updatedCount);
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);
			//Step18: validating the number of records in the error row section after discarding a record
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after discarding a record");

			if(Integer.parseInt(numberOfRecordsInErrorRowSection) > 0) {
				ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T111: Validation that status is approved after approving all the records");
				objBppTrend.discardAllErrorRows();
			}
		}

		//Step19: Reverting the imported file
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step20: Validation of the file status after reverting the imported file
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");

		//Step21: Status of the imported file should be changed to Reverted as the whole file is reverted
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T111: Validation if status of imported file is reverted.");

		//Step22: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T111: Validation if status of imported file is reverted on import logs page.");

		//Step23: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is reverted on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE VALUATION File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111
	 * 3. Correcting all the error records
	 * 4. Validating retry functionality of error records
	 * 5. Approving all records
	 * 6. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T111: Correcting error records and retrying an approving them in BOE Valuation file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void verify_BppTrend_BoeValImportRetryErrorRecordsAndApprove(String loginUser) throws Exception {
		//Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);
				
		//Resetting the composite factor tables status
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYearForImport);

		//Resetting the valuation factor tables status
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYearForImport);
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYearForImport, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 180);

		//Step4: Validations for file count, import count and error count before retrying all errorred records
		Map<String, Object> dataMapWthRowCounts = objBppTrend.countOfDifferentRowTypesInExcel(fileName, rollYearForImport);
		int fileCountFromExcel = (int)dataMapWthRowCounts.get("File Count");
		int importCountFromExcel = (int)dataMapWthRowCounts.get("Import Count");
		int errorCountFromExcel = (int)dataMapWthRowCounts.get("Error Count");
		
		int fileCountBeforeRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T111: Validating file count before retrying errorred records");
		int importCountBeforeRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T111: Validating import count before retrying errorred records");
		int errorCountBeforeRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());			
		softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T111: Validating error count before retrying errorred records");
		
		Map<String, Integer> dataMapForExpImportedRowsCountBeforeRetry = new HashMap<String, Integer>();
		String importedRowsCount;
		
		//Step5: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step6: Store columns / tables in a list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("BoeValuationTablesOnImportPage").split(",")));
		
		//Step7: Iterate over all the tables / columns
		for (int i = 0; i < allTables.size(); i++) {
			//Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
					
			//Step9: Generating expected imported error count for current table
			importedRowsCount = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYearForImport, tableName);
			dataMapForExpImportedRowsCountBeforeRetry.put(tableName, Integer.parseInt(importedRowsCount));
			
			int valueToEnter = 80;
			int numberOfErrorRecordsUnderCurrentTable = Integer.parseInt(objBppTrend.getCountOfRowsFromErrorRowsSection());
			
			//Step10: Iterating over all the tables to correct the invalid data 
			for(int j = 1; j <= numberOfErrorRecordsUnderCurrentTable; j++) {
				//Step11: Editing the value of average column
				valueToEnter = valueToEnter - 1;
				ExtentTestManager.getTest().log(LogStatus.INFO, "Deleting junk data and entering valid data in the table");
				objBppTrend.updateCorrectDataInTable(tableNumber, Integer.toString(valueToEnter));
			}			
		}
		
		//Step14: Clicking retry button and waiting for corrected records to move to imported section
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton);
		objPage.javascriptClick(objEfileHomePage.retryButton);
		objBppTrend.waitForPageSpinnerToDisappear();
		
		//Step15: Re navigating to retry and approve page from history page
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");
		objPage.Click(objEfileHomePage.viewLink);
		
		int errorredRowsBeforeRetrying = Integer.parseInt(CONFIG.getProperty("errorRecordsCount"));
		int expImportedRowsBeforeRetrying;
		
		//Step16: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step17: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);

			String errorredRowsAfterRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
			String actualImportedRowsAfterRetrying = objBppTrend.getCountOfRowsFromImportedRowsSection();
			
			//Step19: Validating error records are zero and corrected records are moved to imported row section after retry.
			softAssert.assertEquals(errorredRowsAfterRetrying, "0", "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			
			expImportedRowsBeforeRetrying = dataMapForExpImportedRowsCountBeforeRetry.get(tableName);
			int expImportedRowsAfterRetrying = expImportedRowsBeforeRetrying + errorredRowsBeforeRetrying;
			softAssert.assertEquals(actualImportedRowsAfterRetrying, Integer.toString(expImportedRowsAfterRetrying), "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section under "+ tableName +" after correcting and retrying the error record");
		}
		
		//Step21: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step22: Searching the efile intake module to validate the status of the imported file after approve on history table
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T111: Validation if status of imported file is approved.");

		//Step23: Validations to check file count after successfully retrying and approving all error records in history table
		int fileCountAfterRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		int expFileCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(fileCountAfterRetrying, expFileCountAfterRetrying, "SMAB-T111: Validating file count after successfully retrying all errorred records");
		
		//Step24: Validations to check import count after successfully retrying and approving all error records in history table
		int importCountAfterRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		int expImportCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(importCountAfterRetrying, expImportCountAfterRetrying, "SMAB-T111: Validating import count after successfully retrying all errorred records");
		
		//Step25: Validations to check error count after successfully retrying and approving all error records in history table
		int errorCountAfterRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());		
		softAssert.assertEquals(errorCountAfterRetrying, 0, "SMAB-T111: Validating error count after successfully retrying all errorred records");
		
		//Step26: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Approved", "SMAB-T111: Validation if status of imported file is approved on import logs page.");

		//Step27: Checking the status on import logs details page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportLogDetailsPage = objBppTrend.getFieldValuesFromImportLogsDetailsPage("BOE - Valuation Factors", "Status");
		softAssert.assertEquals(fileStatusOnImportLogDetailsPage, "Approved", "SMAB-T111: Validation if status of imported file is approved on import logs details page.");		
		
		//Step28: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is approved on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA VALUATION File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Validating discard functionality of error records:: TestCase/JIRA ID: SMAB-T956
	 * 4. Validating revert functionality of error records:: TestCase/JIRA ID: SMAB-T955
	 * 5. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T106,SMAB-T111,SMAB-T955,SMAB-T956: Discarding error records and reverting import for CAA valuation file", dataProvider = "loginBppAndRpBusinessAdminUsers", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void verify_BppTrend_CaaValFileImportDiscardErrorRecordsAndRevert(String loginUser) throws Exception {
		//Step1: Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);

		//Resetting the composite factor tables status
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYearForImport);

		//Resetting the valuation factor tables status
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYearForImport);
		
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step4: Setting up fileName variable to read the excel file
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYearForImport, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 180);
		
		//Step6: Checking the status in import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("CAA - Valuation Factors");
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T955: Validation if status of imported file is imported on import logs page.");
			softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T956: Validation if status of imported file is imported on import logs page.");
		} else {
			softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T111: Validation if status of imported file is imported on import logs page.");
		}
		
		//Step7: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("CAA - Valuation Factors");
		if(loginUser.contains("rpBusinessAdmin")) {			
			softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T955: Validation if status of imported file is imported on transactions page.");
			softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T956: Validation if status of imported file is imported on transactions page.");
		} else {
			softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is imported on transactions page.");
		}
		
		//Step8: Navigating back to history table and clicking view link to navigate to review and approve page
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step9: Store columns outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("CaaValuationTablesOnImportPage").split(",")));
		
		//Step10: Iterate over all the columns
		for (int i = 0; i < allTables.size(); i++) {
			//Step10: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step11: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			
			//Step12: Validating that correct number of records are moved to error and imported row sections after file import			
			String errorRecords = CONFIG.getProperty("errorRecordsCount");

			String actualSuccessRecords = objBppTrend.getCountOfRowsFromImportedRowsSection();
			boolean isErrorRowSectionDisplayed = objBppTrend.checkPresenceOfErrorRowsSection();
			boolean isImportedRowSectionDisplayed = objBppTrend.checkPresenceOfImportedRowsSection();;
			
			if (isErrorRowSectionDisplayed){
				//Step13: Validation of number of records in error row section.
				String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection();				
				if(loginUser.contains("rpBusinessAdmin")) {
					softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T955: Validation if correct number of records are displayed in Error Row Section after file import");
					softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T956: Validation if correct number of records are displayed in Error Row Section after file import");
				} else {
					softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T106: Validation if correct number of records are displayed in Error Row Section after file import");
				}				
			} else {
				//Step14: Validating if the error row section is coming on review and approve page after clicking "View Link" from history table 	
				if(loginUser.contains("rpBusinessAdmin")) {
					softAssert.assertTrue(false, "SMAB-T955: Validation for Error Row Section presence after clicking view link button");
					softAssert.assertTrue(false, "SMAB-T956: Validation for Error Row Section presence after clicking view link button");
				} else {
					softAssert.assertTrue(false, "SMAB-T106: Validation for Error Row Section presence after clicking view link button");
				}			
			}

			if (isImportedRowSectionDisplayed){
				//Step15: Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file
				String expectedImportCount = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYearForImport, tableName);
				softAssert.assertEquals(actualSuccessRecords, expectedImportCount, "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section after file import");
				if(loginUser.contains("rpBusinessAdmin")) {
					softAssert.assertEquals(actualSuccessRecords, expectedImportCount, "SMAB-T955: Validation if correct number of records are displayed in Imported Row Section after file import");
				} else {
					softAssert.assertEquals(actualSuccessRecords, expectedImportCount, "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section after file import");
				}
			} else {
				//Step16: Validating if the imported row section is coming on review and approve page after clicking "View Link" from history table
				if(loginUser.contains("rpBusinessAdmin")) {
					softAssert.assertTrue(false, "SMAB-T955: Validation for Imported Row Section presence after clicking view link button");
				} else {
					softAssert.assertTrue(false, "SMAB-T111: Validation for Imported Row Section presence after clicking view link button");
				}
			}

			//Step17: Validation for Records discard functionality from Review and Approve Page
			ExtentTestManager.getTest().log(LogStatus.INFO, "Validation that error records can be discarded from Review and Approve Data Page");
			objBppTrend.discardIndividualErrorRow();

			int updatedCount = Integer.parseInt(errorRecords) - 1;
			String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection("ERROR ROWS : " + updatedCount);
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);
			//Step18: validating the number of records in the error row section after discarding a record
			if(loginUser.contains("rpBusinessAdmin")) {
				softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T956: Validation if correct number of records are displayed in Error Row Section after discarding a record");
			} else {
				softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after discarding a record");
			}
			
			if(Integer.parseInt(numberOfRecordsInErrorRowSection) > 0) {
				ExtentTestManager.getTest().log(LogStatus.INFO, "Validation that status is approved after approving all the records");
				objBppTrend.discardAllErrorRows();
			}
		}

		//Step19: Reverting the imported file
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step20: Validation of the file status after reverting the imported file
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");

		//Step21: Status of the imported file should be changed to Reverted as the whole file is reverted		
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T955: Validation if status of imported file is reverted.");
		} else {
			softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T111: Validation if status of imported file is reverted.");
		}
		
		//Step22: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("CAA - Valuation Factors");
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T955: Validation if status of imported file is reverted on import logs page.");
			softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T956: Validation if status of imported file is reverted on import logs page.");
		} else {
			softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T111: Validation if status of imported file is reverted on import logs page.");
		}

		
		//Step23: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("CAA - Valuation Factors");
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T955: Validation if status of imported file is reverted on transactions page.");
			softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T956: Validation if status of imported file is reverted on transactions page.");
		} else {
			softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is reverted on transactions page.");
		}
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA VALUATION File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 3. Correcting all the error records
	 * 4. Validating retry functionality of error records:: TestCase/JIRA ID: SMAB-T958
	 * 5. Approving all records:: TestCase/JIRA ID: SMAB-T957
	 * 6. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T111,SMAB-T957,SMAB-T958: Correcting error records and retrying an approving them in CAA Valuation file", dataProvider = "loginBppAndRpBusinessAdminUsers", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void verify_BppTrend_CaaValImportRetryErrorRecordsAndApprove(String loginUser) throws Exception {
		//Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);
		
		//Resetting the composite factor tables status
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYearForImport);

		//Resetting the valuation factor tables status
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYearForImport);
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business administrator or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYearForImport, fileName);	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 180);
		
		//Step4: Validations for file count, import count and error count before retrying all errorred records
		Map<String, Object> dataMapWthRowCounts = objBppTrend.countOfDifferentRowTypesInExcel(fileName, rollYearForImport);
		int fileCountFromExcel = (int)dataMapWthRowCounts.get("File Count");
		int importCountFromExcel = (int)dataMapWthRowCounts.get("Import Count");
		int errorCountFromExcel = (int)dataMapWthRowCounts.get("Error Count");
		
		int fileCountBeforeRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T957: Validating file count before retrying errorred records");
			softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T958: Validating file count before retrying errorred records");			
		} else {
			softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T111: Validating file count before retrying errorred records");
		}
		
		int importCountBeforeRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T957: Validating import count before retrying errorred records");
			softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T958: Validating import count before retrying errorred records");	
		} else {
			softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T111: Validating import count before retrying errorred records");
		}
		
		int errorCountBeforeRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());			
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T957: Validating error count before retrying errorred records");
			softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T958: Validating error count before retrying errorred records");	
		} else {
			softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T111: Validating error count before retrying errorred records");
		}
		
		//Step4: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		
		Map<String, Integer> dataMapForExpImportedRowsCountBeforeRetry = new HashMap<String, Integer>();
		String importedRowsCount;
		
		//Step5: Store columns / tables in a list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("CaaValuationTablesOnImportPage").split(",")));
		
		//Step6: Iterate over all the tables / columns
		for (int i = 0; i < allTables.size(); i++) {
			//Step7: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on table name");
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);

			//Step9: Generating expected imported error count for current table
			importedRowsCount = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYearForImport, tableName);
			dataMapForExpImportedRowsCountBeforeRetry.put(tableName, Integer.parseInt(importedRowsCount));
			
			int valueToEnter = 80;
			int numberOfErrorRecordsUnderCurrentTable = Integer.parseInt(objBppTrend.getCountOfRowsFromErrorRowsSection());
			
			//Step10: Iterating over all the tables to correct the invalid data 
			for(int j = 1; j <= numberOfErrorRecordsUnderCurrentTable; j++) {
				//Step11: Editing the value of average column
				valueToEnter = valueToEnter - 1;
				ExtentTestManager.getTest().log(LogStatus.INFO, "Deleting junk data and entering valid data in the table");
				objBppTrend.updateCorrectDataInTable(tableNumber, Integer.toString(valueToEnter));
			}			
		}
		
		//Step14: Clicking retry button and waiting for corrected records to move to imported section
		objPage.waitForElementToBeClickable(objEfileHomePage.retryButton);
		objPage.javascriptClick(objEfileHomePage.retryButton);
		objBppTrend.waitForPageSpinnerToDisappear();
		
		//Step15: Re navigating to retry and approve page
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");
		objPage.Click(objEfileHomePage.viewLink);
		
		int errorredRowsBeforeRetrying = Integer.parseInt(CONFIG.getProperty("errorRecordsCount"));
		int expImportedRowsBeforeRetrying;
		
		//Step16: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step17: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			
			String errorredRowsAfterRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
			String actualImportedRowsAfterRetrying = objBppTrend.getCountOfRowsFromImportedRowsSection();
			
			//Step19: Validating error records are zero and corrected records are moved to imported row section after retry.	
			if(loginUser.contains("rpBusinessAdmin")) {
				softAssert.assertEquals(errorredRowsAfterRetrying, "0", "SMAB-T958: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			} else {
				softAssert.assertEquals(errorredRowsAfterRetrying, "0", "SMAB-T111: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			}
			
			expImportedRowsBeforeRetrying = dataMapForExpImportedRowsCountBeforeRetry.get(tableName);
			int expImportedRowsAfterRetrying = expImportedRowsBeforeRetrying + errorredRowsBeforeRetrying;
			if(loginUser.contains("rpBusinessAdmin")) {
				softAssert.assertEquals(actualImportedRowsAfterRetrying, Integer.toString(expImportedRowsAfterRetrying), "SMAB-T958: Validation if correct number of records are displayed in Imported Row Section under "+ tableName +" after correcting and retrying the error record");
			} else {
				softAssert.assertEquals(actualImportedRowsAfterRetrying, Integer.toString(expImportedRowsAfterRetrying), "SMAB-T111: Validation if correct number of records are displayed in Imported Row Section under "+ tableName +" after correcting and retrying the error record");
			}
		}
		
		//Step21: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step22: Searching the efile intake module to validate the status of the imported file after approve on history table
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");
		if(loginUser.contains("rpBusinessAdmin")) {
			softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T957: Validation if status of imported file is approved.");
		} else {
			softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T111: Validation if status of imported file is approved.");
		}
						
		//Step23: Validations to check file count after successfully retrying and approving all error records in history table
		int fileCountAfterRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		int expFileCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		if(loginUser.contains("bppBusinessAdmin")) {
			softAssert.assertEquals(fileCountAfterRetrying, expFileCountAfterRetrying, "SMAB-T111: Validating file count after successfully retrying all errorred records");
		}
		
		//Step24: Validations to check import count after successfully retrying and approving all error records in history table
		int importCountAfterRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		int expImportCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		if(loginUser.contains("bppBusinessAdmin")) {
			softAssert.assertEquals(importCountAfterRetrying, expImportCountAfterRetrying, "SMAB-T111: Validating import count after successfully retrying all errorred records");
		}
		
		//Step25: Validations to check error count after successfully retrying and approving all error records in history table
		int errorCountAfterRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());		
		if(loginUser.contains("bppBusinessAdmin")) {
			softAssert.assertEquals(errorCountAfterRetrying, 0, "SMAB-T111: Validating error count after successfully retrying all errorred records");
		}
		
		//Step26: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("CAA - Valuation Factors");
		if(loginUser.contains("bppBusinessAdmin")) {
			softAssert.assertEquals(fileStatusOnImportPage, "Approved", "SMAB-T111: Validation if status of imported file is approved on import logs page.");
		}

		//Step27: Checking the status on import logs details page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportLogDetailsPage = objBppTrend.getFieldValuesFromImportLogsDetailsPage("CAA - Valuation Factors", "Status");		
		if(loginUser.contains("bppBusinessAdmin")) {
			softAssert.assertEquals(fileStatusOnImportLogDetailsPage, "Approved", "SMAB-T111: Validation if status of imported file is approved on import logs details page.");
		}
		
		//Step28: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("CAA - Valuation Factors");		
		if(loginUser.contains("bppBusinessAdmin")) {
			softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T111: Validation if status of imported file is approved on transactions page.");
		}
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import>::
	 * 1. Validating the restrictions on uploading file with .CSV format:: TestCase/JIRA ID: SMAB-T112
	 * 2. Validating the restrictions on uploading file with .TXT format:: TestCase/JIRA ID: SMAB-T112
	 * 3. Validating the restrictions on uploading file with .XLS format:: TestCase/JIRA ID: SMAB-T112
	 */
	@Test(description = "SMAB-T112: Validating restrictions on uploading BPP Trends data files in invalid format", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BPPTrend"})
	public void verify_BppTrend_ImportWithInvalidFormat(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business administrator)
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Log into application with user: "+ loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend BOE Index Factors file having invalid format XLS
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Validating upload for file with XLS format");
		String xlsFormatFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_XLS;
		objEfileHomePage.uploadInvalidFormatFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, xlsFormatFile);
		Thread.sleep(1000);
		objPage.waitForElementToBeClickable(objEfileHomePage.doneButton);
		objPage.Click(objEfileHomePage.doneButton);
		Thread.sleep(5000);
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "File Failed", 120);
		String expectedStatus = CONFIG.getProperty("expStatusForXlsFileImport");
		softAssert.assertEquals(objBppTrend.getElementText(objEfileHomePage.statusImportedFile), expectedStatus, "SMAB-T112: Validation for incorrect file format");
		
		//Step4: Uploading the BPP Trend BOE Index Factors file having invalid format CSV
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Validating upload for file with CSV format");
		String csvFormatFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_CSV;
		objEfileHomePage.uploadInvalidFormatFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, csvFormatFile);
		
		objBppTrend.waitForElementToBeVisible(objBppTrend.errorMsgOnImportForInvalidFileFormat, 10);
		String actualMsgFrInvalidFrmat = objBppTrend.getElementText(objBppTrend.errorMsgOnImportForInvalidFileFormat);
		String expectedMsgFrInvalidFormat = CONFIG.getProperty("msgForCsvFormatFile");

		softAssert.assertEquals(actualMsgFrInvalidFrmat, expectedMsgFrInvalidFormat, "SMAB-T112: Validation for incorrect (CSV) file format");
		objBppTrend.Click(objBppTrend.closeButton);
		
		//Step5: Uploading the BPP Trend BOE Index Factors file having invalid format TXT
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Validating upload for file with TXT format");
		String txtFormatFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_TXT;
		objEfileHomePage.uploadInvalidFormatFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, txtFormatFile);
		
		objBppTrend.waitForElementToBeVisible(objBppTrend.errorMsgOnImportForInvalidFileFormat, 10);
		actualMsgFrInvalidFrmat = objBppTrend.getElementText(objBppTrend.errorMsgOnImportForInvalidFileFormat);
		expectedMsgFrInvalidFormat = CONFIG.getProperty("msgForTxtFormatFile");

		softAssert.assertEquals(actualMsgFrInvalidFrmat, expectedMsgFrInvalidFormat, "SMAB-T112: Validation for incorrect (TXT) file format");
		objBppTrend.Click(objBppTrend.closeButton);

		softAssert.assertAll();
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
	public void verify_BppTrend_TransformationRules_On_BoeIndexAndPercentFactors_Import(String loginUser) throws Exception {
		//Delete the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYearForImport + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteBPPTrendRollYearData(rollYearForImport);
		
		//Resetting the composite factor tables status
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYearForImport);

		//Resetting the valuation factor tables status
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrend.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYearForImport);
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business administrator or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend BOE Index Factors file having error and success records
		String bppTrendIndexFactorsFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_TRANSFORMATION_RULES;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYearForImport, bppTrendIndexFactorsFile);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 180);
		
		//Step4: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step5: Store columns / tables outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tablesForTransformationRulesOustideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tablesForTransformationRulesUnderMoreTab").split(",")));
		String tableNamesUnderMoreTab = CONFIG.getProperty("tablesForTransformationRulesUnderMoreTab");
		
		//Step6: Iterate over all the tables / columns
		for (int i = 0; i < allTables.size(); i++) {			
			//Step7: Clicking on the current table name
			String tableName = allTables.get(i);			
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab, true);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false, true);
			
			//Step8: Finding total number of records under the selected table
			int numberOfErrorRecordsValidate = 0;
			
			//Step9: Setting the value of column to read from current table based on current table's name
			String columnNameToValidate = null;
			String tableNumber = null;
			if(tableName.equalsIgnoreCase("Commercial Equipment Index")) {
				columnNameToValidate = CONFIG.getProperty("columnWithErrorRecordsInCommEquipIndex");
				numberOfErrorRecordsValidate = Integer.parseInt(CONFIG.getProperty("errorRecordsInCommEquipIndex"));
				tableNumber = CONFIG.getProperty("indexNumberCommEquipIndex");
			}
			else if (tableName.equalsIgnoreCase("Agricultural ME Good Factors")) {
				columnNameToValidate = CONFIG.getProperty("columnWithErrorRecordsInAgrMeGoodFactors");
				numberOfErrorRecordsValidate = Integer.parseInt(CONFIG.getProperty("errorRecordsInAgrMeGoodFactors"));
				tableNumber = CONFIG.getProperty("indexNumberAgrMeGoodFactors");
			}
			else if (tableName.equalsIgnoreCase("Agricultural Index")) {
				columnNameToValidate = CONFIG.getProperty("columnWithErrorRecordsInAgriculturalIndex");
				numberOfErrorRecordsValidate = Integer.parseInt(CONFIG.getProperty("errorRecordsInAgriculturalIndex"));
				tableNumber = CONFIG.getProperty("indexNumberAgriculturalIndex");
			}			
			//Step10: Setting table number to retrieve error row count
			System.setProperty("tableNumber", tableNumber);
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Transformation Validation On " + tableName + " Table On Column '"+ columnNameToValidate +"' ****");
			//Step11: Iterating over all the error rows to validate the error message 
			for(int rowNum = 1; rowNum <= numberOfErrorRecordsValidate; rowNum++) {
				//Fetching error message from table
				String actErrorMsg = objBppTrend.getErrorMessageFromTable(tableName, rowNum);
				
				//Fetching value of the column to be validated from table
				String valueOfColumn = objBppTrend.readDataFromBppTrendFactorTableOnEfileImportPage(tableName, rowNum, columnNameToValidate);
				
				//Generating Expected error message based on the value displayed in table
				String expErrorMsg = objBppTrend.getnerateExpectedErrorMsgForTableColumn(columnNameToValidate, valueOfColumn);
				
				//Asserting the expected and the actual error message				
				String validationMsg = "SMAB-T105: Validating transformation rule for column '"+ columnNameToValidate + "' having value --> "+ valueOfColumn +". Expected Msg:: "+ expErrorMsg +" || Actual Msg: "+ actErrorMsg;
				softAssert.assertTrue(actErrorMsg.contains(expErrorMsg), validationMsg);
			}
		}
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


}