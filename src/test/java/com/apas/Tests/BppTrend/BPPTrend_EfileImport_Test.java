package com.apas.Tests.BppTrend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
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
import com.apas.config.users;
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
	String rollYear;
	SalesforceAPI objSalesforceAPI;
	
	@BeforeMethod
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
		//rollYear = CONFIG.getProperty("rollYear");
		rollYear = "2020";
		objSalesforceAPI = new SalesforceAPI();
	}
		
	@AfterMethod
	public void afterMethod() throws IOException, InterruptedException{
		//objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import>::
	 * 1. Validating the restrictions on uploading file with .CSV format:: TestCase/JIRA ID: SMAB-T112
	 * 2. Validating the restrictions on uploading file with .TXT format:: TestCase/JIRA ID: SMAB-T112
	 * 3. Validating the restrictions on uploading file with .XLS format:: TestCase/JIRA ID: SMAB-T112
	 */
	@Test(description = "SMAB-T112: Validating restrictions on uploading BPP Trends data files in invalid format", dataProvider = "invalidFileTypes", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 7, enabled = true)
	public void verify_BppTrend_ImportWithInvalidFormat(String fileName) throws Exception {		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business administrator)
		objApasGenericFunctions.login(users.BUSINESS_ADMIN);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend BOE Index Factors file having invalid format
		String bppTrendIndexFactorsFile = System.getProperty("user.dir") + fileName;
		objEfileHomePage.uploadInvalidFormatFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear, bppTrendIndexFactorsFile);		
				
		if(fileName.endsWith(".xls")) {
			objPage.waitForElementToBeClickable(objEfileHomePage.doneButton);
			objPage.Click(objEfileHomePage.doneButton);
			Thread.sleep(3000);
			objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "File Failed", 120);
			String expectedStatus = CONFIG.getProperty("expStatusForXlsFileImport");
			softAssert.assertEquals(objBppTrend.getElementText(objEfileHomePage.statusImportedFile), expectedStatus, "SMAB-T112: Validation for incorrect file format");
			
		} else {
			String expectedMsgFrInvalidFormat = null;
			String actualMsgFrInvalidFrmat = objBppTrend.getElementText(objBppTrend.locateElement("//div[contains(@id, 'help-message-')]", 30));

			if(fileName.endsWith(".csv")) {
				expectedMsgFrInvalidFormat = CONFIG.getProperty("msgForCsvFormatFile");
			} else if(fileName.endsWith(".txt")) {
				expectedMsgFrInvalidFormat = CONFIG.getProperty("msgForTxtFormatFile");
			}
			softAssert.assertEquals(actualMsgFrInvalidFrmat, expectedMsgFrInvalidFormat, "SMAB-T112: Validation for incorrect file format");
			objBppTrend.Click(objBppTrend.closeButton);
		}
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	

	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Validating revert functionality of error records
	 * 4. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T106,SMAB-T111: Discarding error records and reverting import for BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 1, enabled = true)
	public void verify_BppTrend_BoeIndexFileImportDiscardErrorRecordsAndRevert(String loginUser) throws Exception {
		//Step1: Update the status of Approved data via SalesForce API
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYear + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
				
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step4: Read the excel file and imported retrieve row counts from
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
		Map<String, Object> mapWithExpectedImportedRowForAllTables = objBppTrend.getTotalRowsCountFromExcelForGivenTable(fileName);
		
		//Step5: Uploading the BPP Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		//Step6: Checking the status in import logs
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T111: Validation if status of imported file is imported on import logs page.");

		//Step7: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is imported on transactions page.");
		
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

			//Step12: Validating that correct number of records are moved to error and imported row sections after file import			
			String errorRecords = CONFIG.getProperty("errorRecordsCount");
			String tableNameToRetrieveExpImportedRowsCount = objBppTrend.locateElement("//a[@data-tab-value = '"+ tableNumber +"']", 60).getText();

			String successRecords = mapWithExpectedImportedRowForAllTables.get(tableNameToRetrieveExpImportedRowsCount).toString();
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
				String numberOfRecordsInImportedRowSection = objBppTrend.getCountOfRowsFromImportedRowsSection();
				softAssert.assertEquals(numberOfRecordsInImportedRowSection, successRecords, "SMAB-T112: Validation if correct number of records are displayed in Imported Row Section after file import");
			} else {
				//Step16: Validating if the imported row section is coming on review and approve page after clicking "View Link" from history table
				softAssert.assertTrue(false, "SMAB-T112: Validation for Imported Row Section presence after clicking view link button");	
			}				

			//Step17: Validation for Records discard functionality from Review and Approve Page
			ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T112: Validation that error records can be dicarded from Review and Approve Data Page");
			objBppTrend.discardIndividualErrorRow();

			int updatedCount = Integer.parseInt(errorRecords) - 1;
			String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection("ERROR ROWS : " + updatedCount);
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);
			//Step18: validating the number of records in the error row section after discarding a record
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after discarding a record");

			if(Integer.parseInt(numberOfRecordsInErrorRowSection) > 0) {
				ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T112: Validation that status is approved after approving all the records");
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
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T112: Validation if status of imported file is reverted.");

		//Step22: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T112: Validation if status of imported file is reverted on import logs page.");

		//Step23: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is reverted on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE INDEX File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Correcting all the error records
	 * 4. Validating retry functionality of error records
	 * 5. Approving all records
	 * 6. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T112: Correcting error records and retrying an approving them in BOE Index file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 3, enabled = true)
	public void verify_BppTrend_BoeIndexImportRetryErrorRecordsAndApprove(String loginUser) throws Exception {
		//Update the status of Approved data via SalesForce API
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYear + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
				
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		//Step4: Validations for file count, import count and error count before retrying all errorred records
		Map<String, Object> dataMapWthRowCounts = objBppTrend.countOfDifferentRowTypesInExcel(fileName);
		int fileCountFromExcel = (int)dataMapWthRowCounts.get("File Count");
		int importCountFromExcel = (int)dataMapWthRowCounts.get("Import Count");
		int errorCountFromExcel = (int)dataMapWthRowCounts.get("Error Count");
		
		int fileCountBeforeRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T112: Validating file count before retrying errorred records");
		int importCountBeforeRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T112: Validating import count before retrying errorred records");
		int errorCountBeforeRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());			
		softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T112: Validating error count before retrying errorred records");		
		
		//Step5: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step6: Store columns / tables outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("BoeIndexTablesOutsideMoreTabOnImportPage").split(",")));
		String tableNamesUnderMoreTab = CONFIG.getProperty("BoeIndexTablesUnderMoreTabOnImportPage");
		allTables.addAll(Arrays.asList(tableNamesUnderMoreTab.split(",")));
		
		String errorredRowsBeforeRetrying = null;
		String importedRowsBeforeRetrying = null;
		
		//Step7: Iterate over all the tables / columns
		for (int i = 0; i < allTables.size(); i++) {
			//Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab, true);
					
			int valueToEnter = 80;
			int numberOfErrorRecordsUnderCurrentTable = Integer.parseInt(objBppTrend.getCountOfRowsFromErrorRowsSection());
			
			//Step9: Iterating over all the tables to correct the invalid data 
			for(int j = 1; j <= numberOfErrorRecordsUnderCurrentTable; j++) {
				//Step10: Retrieving the imported and error count for current table
				errorredRowsBeforeRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
				importedRowsBeforeRetrying = objBppTrend.getCountOfRowsFromImportedRowsSection();
				
				//Step11: Editing the value of average column
				objBppTrend.waitForElementToBeClickable(objBppTrend.tableCellWithJunkTextOnImportPageTale);
				objBppTrend.Click(objBppTrend.tableCellWithJunkTextOnImportPageTale);
				
				objBppTrend.waitForElementToBeClickable(objBppTrend.editIconInImportPageTale);
				objBppTrend.javascriptClick(objBppTrend.editIconInImportPageTale);
				
				//Step12: Entering a correct value in the average column text box
				valueToEnter = valueToEnter - 1;
				objBppTrend.enter(objBppTrend.inputBoxOnImportPage, Integer.toString(valueToEnter));
				
				//Step13: Clicking on adjacent cell to move control from input box
				WebElement adjacentCell = objBppTrend.locateElement("((//lightning-formatted-text[text() = 'Junk_A'] | //lightning-formatted-text[text() = 'Junk_B'])[1])//ancestor::td//preceding-sibling::td[1]", 120);
				objBppTrend.Click(adjacentCell);
				Thread.sleep(1000);
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
		
		//Step16: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step17: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab, true);
			
			String errorredRowsAfterRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
			String importedRowsAfterRetrying = objBppTrend.getCountOfRowsFromImportedRowsSection();
			int importedRowsAfterRetryingInt = Integer.parseInt(importedRowsAfterRetrying) + Integer.parseInt(errorredRowsBeforeRetrying);
			importedRowsAfterRetrying = Integer.toString(importedRowsAfterRetryingInt);
			
			//Step20: Validating that corrected records are moved to imported row section after retry.
			softAssert.assertEquals(errorredRowsAfterRetrying, "0", "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			int expimportedRowsAfterRetrying = Integer.parseInt(importedRowsBeforeRetrying) + Integer.parseInt(errorredRowsBeforeRetrying);
			softAssert.assertEquals(importedRowsAfterRetrying, Integer.toString(expimportedRowsAfterRetrying), "SMAB-T112: Validation if correct number of records are displayed in Imported Row Section after correcting and retrying the error record");
		}
		
		//Step21: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step22: Searching the efile intake module to validate the status of the imported file after approve on history table
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T112: Validation if status of imported file is approved.");

		//Step23: Validations to check file count after successfully retrying and approving all error records in history table
		int fileCountAfterRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		int expFileCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(fileCountAfterRetrying, expFileCountAfterRetrying, "SMAB-T112: Validating file count after successfully retrying all errorred records");
		
		//Step24: Validations to check import count after successfully retrying and approving all error records in history table
		int importCountAfterRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		int expImportCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(importCountAfterRetrying, expImportCountAfterRetrying, "SMAB-T112: Validating import count after successfully retrying all errorred records");
		
		//Step25: Validations to check error count after successfully retrying and approving all error records in history table
		int errorCountAfterRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());		
		softAssert.assertEquals(errorCountAfterRetrying, 0, "SMAB-T112: Validating error count after successfully retrying all errorred records");
		
		//Step26: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Approved", "SMAB-T112: Validation if status of imported file is approved on import logs page.");

		//Step27: Checking the status on import logs details page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportLogDetailsPage = objBppTrend.getFieldValuesFromImportLogsDetailsPage("BOE - Index and Percent Good Factors", "Status");
		softAssert.assertEquals(fileStatusOnImportLogDetailsPage, "Approved", "SMAB-T112: Validation if status of imported file is approved on import logs details page.");		
		
		//Step28: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Index and Percent Good Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is approved on transactions page.");
		
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
	@Test(description = "SMAB-T112: Discarding error records and reverting import for BOE valuation file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 1, enabled = true)
	public void verify_BppTrend_BoeValFileImportDiscardErrorRecordsAndRevert(String loginUser) throws Exception {
		//Step1: Update the status of Approved data via SalesForce API
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYear + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
				
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step4: Setting up filePath
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYear, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		//Step6: Checking the status in import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T112: Validation if status of imported file is imported on import logs page.");

		//Step7: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is imported on transactions page.");

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
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false);

			//Step12: Validating that correct number of records are moved to error and imported row sections after file import			
			String errorRecords = CONFIG.getProperty("errorRecordsCount");
			String tableNameToRetrieveExpImportedRowsCount = objBppTrend.locateElement("//a[@data-tab-value = '"+ tableNumber +"']", 60).getText();

			String actualSuccessRecords = objBppTrend.getCountOfRowsFromImportedRowsSection();
			boolean isErrorRowSectionDisplayed = objBppTrend.checkPresenceOfErrorRowsSection();
			boolean isImportedRowSectionDisplayed = objBppTrend.checkPresenceOfImportedRowsSection();;
			
			if (isErrorRowSectionDisplayed){
				//Step13: Validation of number of records in error row section.
				String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection();
				softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after file import");
			} else {
				//Step14: Validating if the error row section is coming on review and approve page after clicking "View Link" from history table 
				softAssert.assertTrue(false, "SMAB-T112: Validation for Error Row Section presence after clicking view link button");	
			}
			
			if (isImportedRowSectionDisplayed){
				//Step15: Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file
				String expectedSuccessRecords = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYear);
				softAssert.assertEquals(expectedSuccessRecords, actualSuccessRecords, "SMAB-T112: Validation if correct number of records are displayed in Imported Row Section after file import");
			} else {
				//Step16: Validating if the imported row section is coming on review and approve page after clicking "View Link" from history table
				softAssert.assertTrue(false, "SMAB-T112: Validation for Imported Row Section presence after clicking view link button");	
			}				

			//Step17: Validation for Records discard functionality from Review and Approve Page
			ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T112: Validation that error records can be dicarded from Review and Approve Data Page");
			objBppTrend.discardIndividualErrorRow();

			int updatedCount = Integer.parseInt(errorRecords) - 1;
			String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection("ERROR ROWS : " + updatedCount);
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);
			//Step18: validating the number of records in the error row section after discarding a record
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after discarding a record");

			if(Integer.parseInt(numberOfRecordsInErrorRowSection) > 0) {
				ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T112: Validation that status is approved after approving all the records");
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
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T112: Validation if status of imported file is reverted.");

		//Step22: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T112: Validation if status of imported file is reverted on import logs page.");

		//Step23: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is reverted on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On BOE VALUATION File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Correcting all the error records
	 * 4. Validating retry functionality of error records
	 * 5. Approving all records
	 * 6. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T112: Correcting error records and retrying an approving them in BOE Valuation file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 4, enabled = true)
	public void verify_BppTrend_BoeValImportRetryErrorRecordsAndApprove(String loginUser) throws Exception {
		//Update the status of Approved data via SalesForce API
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYear + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
				
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		//objApasGenericFunctions.login(users.PRINCIPAL_USER);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYear, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		//Step4: Validations for file count, import count and error count before retrying all errorred records
		Map<String, Object> dataMapWthRowCounts = objBppTrend.countOfDifferentRowTypesInExcel(fileName);
		int fileCountFromExcel = (int)dataMapWthRowCounts.get("File Count");
		int importCountFromExcel = (int)dataMapWthRowCounts.get("Import Count");
		int errorCountFromExcel = (int)dataMapWthRowCounts.get("Error Count");
		
		int fileCountBeforeRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T112: Validating file count before retrying errorred records");
		int importCountBeforeRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T112: Validating import count before retrying errorred records");
		int errorCountBeforeRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());			
		softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T112: Validating error count before retrying errorred records");
		
		//Step5: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step6: Store columns / tables in a list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("BoeValuationTablesOnImportPage").split(",")));
		
		String errorredRowsBeforeRetrying = null;
		String importedRowsBeforeRetrying = null;
		
		//Step7: Iterate over all the tables / columns
		for (int i = 0; i < allTables.size(); i++) {
			//Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false);
					
			int valueToEnter = 80;
			int numberOfErrorRecordsUnderCurrentTable = Integer.parseInt(objBppTrend.getCountOfRowsFromErrorRowsSection());
			
			//Step9: Iterating over all the tables to correct the invalid data 
			for(int j = 1; j <= numberOfErrorRecordsUnderCurrentTable; j++) {
				//Step10: Retrieving the imported and error count for current table
				errorredRowsBeforeRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
				importedRowsBeforeRetrying = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYear);
				
				//Step11: Editing the value of average column
				objBppTrend.waitForElementToBeClickable(objBppTrend.tableCellWithJunkTextOnImportPageTale);
				objBppTrend.Click(objBppTrend.tableCellWithJunkTextOnImportPageTale);
				
				objBppTrend.waitForElementToBeClickable(objBppTrend.editIconInImportPageTale);
				objBppTrend.javascriptClick(objBppTrend.editIconInImportPageTale);
				
				//Step12: Entering a correct value in the average column text box
				valueToEnter = valueToEnter - 1;
				objBppTrend.enter(objBppTrend.inputBoxOnImportPage, Integer.toString(valueToEnter));
				
				//Step13: Clicking on adjacent cell to move control from input box
				WebElement adjacentCell = objBppTrend.locateElement("((//lightning-formatted-text[text() = 'Junk_A'] | //lightning-formatted-text[text() = 'Junk_B'])[1])//ancestor::td//preceding-sibling::td[1]", 120);
				objBppTrend.Click(adjacentCell);
				Thread.sleep(1000);
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
		
		//Step16: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step17: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false);
			
			String errorredRowsAfterRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
			String importedRowsAfterRetrying = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYear);
			int importedRowsAfterRetryingInt = Integer.parseInt(importedRowsAfterRetrying) + Integer.parseInt(errorredRowsBeforeRetrying);
			importedRowsAfterRetrying = Integer.toString(importedRowsAfterRetryingInt);
			
			//Step20: Validating that corrected records are moved to imported row section after retry.
			softAssert.assertEquals(errorredRowsAfterRetrying, "0", "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			int expImportedRowsAfterRetrying = Integer.parseInt(importedRowsBeforeRetrying) + Integer.parseInt(errorredRowsBeforeRetrying);
			softAssert.assertEquals(importedRowsAfterRetrying, Integer.toString(expImportedRowsAfterRetrying), "SMAB-T112: Validation if correct number of records are displayed in Imported Row Section after correcting and retrying the error record");
		}
		
		//Step21: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step22: Searching the efile intake module to validate the status of the imported file after approve on history table
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Valuation Factors");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T112: Validation if status of imported file is approved.");

		//Step23: Validations to check file count after successfully retrying and approving all error records in history table
		int fileCountAfterRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		int expFileCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(fileCountAfterRetrying, expFileCountAfterRetrying, "SMAB-T112: Validating file count after successfully retrying all errorred records");
		
		//Step24: Validations to check import count after successfully retrying and approving all error records in history table
		int importCountAfterRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		int expImportCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(importCountAfterRetrying, expImportCountAfterRetrying, "SMAB-T112: Validating import count after successfully retrying all errorred records");
		
		//Step25: Validations to check error count after successfully retrying and approving all error records in history table
		int errorCountAfterRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());		
		softAssert.assertEquals(errorCountAfterRetrying, 0, "SMAB-T112: Validating error count after successfully retrying all errorred records");
		
		//Step26: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Approved", "SMAB-T112: Validation if status of imported file is approved on import logs page.");

		//Step27: Checking the status on import logs details page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportLogDetailsPage = objBppTrend.getFieldValuesFromImportLogsDetailsPage("BOE - Valuation Factors", "Status");
		softAssert.assertEquals(fileStatusOnImportLogDetailsPage, "Approved", "SMAB-T112: Validation if status of imported file is approved on import logs details page.");		
		
		//Step28: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("BOE - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is approved on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA VALUATION File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Validating revert functionality of error records
	 * 4. Validating status post reverting error records on history page
	 */
	@Test(description = "SMAB-T112: Discarding error records and reverting import for CAA valuation file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 5, enabled = true)
	public void verify_BppTrend_CaaValFileImportDiscardErrorRecordsAndRevert(String loginUser) throws Exception {
		//Step1: Update the status of Approved data via SalesForce API
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYear + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
				
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step4: Setting up fileName variable to read the excel file
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;
		
		//Step5: Uploading the Bpp Trend BOE Index Factors file having error and success records
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYear, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		//Step6: Checking the status in import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("CAA - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Imported", "SMAB-T112: Validation if status of imported file is imported on import logs page.");

		//Step7: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("CAA - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is imported on transactions page.");

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
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false);

			//Step12: Validating that correct number of records are moved to error and imported row sections after file import			
			String errorRecords = CONFIG.getProperty("errorRecordsCount");
			String tableNameToRetrieveExpImportedRowsCount = objBppTrend.locateElement("//a[@data-tab-value = '"+ tableNumber +"']", 60).getText();

			String actualSuccessRecords = objBppTrend.getCountOfRowsFromImportedRowsSection();
			boolean isErrorRowSectionDisplayed = objBppTrend.checkPresenceOfErrorRowsSection();
			boolean isImportedRowSectionDisplayed = objBppTrend.checkPresenceOfImportedRowsSection();;
			
			if (isErrorRowSectionDisplayed){
				//Step13: Validation of number of records in error row section.
				String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection();
				softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after file import");
			} else {
				//Step14: Validating if the error row section is coming on review and approve page after clicking "View Link" from history table 
				softAssert.assertTrue(false, "SMAB-T112: Validation for Error Row Section presence after clicking view link button");	
			}

			if (isImportedRowSectionDisplayed){
				//Step15: Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file
				String expectedImportCount = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYear);
				softAssert.assertEquals(actualSuccessRecords, expectedImportCount, "SMAB-T112: Validation if correct number of records are displayed in Imported Row Section after file import");
			} else {
				//Step16: Validating if the imported row section is coming on review and approve page after clicking "View Link" from history table
				softAssert.assertTrue(false, "SMAB-T112: Validation for Imported Row Section presence after clicking view link button");
			}

			//Step17: Validation for Records discard functionality from Review and Approve Page
			ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T112: Validation that error records can be discarded from Review and Approve Data Page");
			objBppTrend.discardIndividualErrorRow();

			int updatedCount = Integer.parseInt(errorRecords) - 1;
			String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection("ERROR ROWS : " + updatedCount);
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);
			//Step18: validating the number of records in the error row section after discarding a record
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after discarding a record");

			if(Integer.parseInt(numberOfRecordsInErrorRowSection) > 0) {
				ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T112: Validation that status is approved after approving all the records");
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
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T112: Validation if status of imported file is reverted.");
		
		//Step22: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("CAA - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Reverted", "SMAB-T112: Validation if status of imported file is reverted on import logs page.");

		//Step23: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("CAA - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is reverted on transactions page.");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing Following Validations <E-File Import On CAA VALUATION File>
	 * 1. Validating the successful uploading of data file with valid format:: TestCase/JIRA ID: SMAB-T111 
	 * 2. Validating error records cannot be approved on review & approve page:: TestCase/JIRA ID: SMAB-T106
	 * 3. Correcting all the error records
	 * 4. Validating retry functionality of error records
	 * 5. Approving all records
	 * 6. Validating status post approving on history page and transaction import logs page
	 */
	@Test(description = "SMAB-T112: Correcting error records and retrying an approving them in CAA Valuation file", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 4, enabled = true)
	public void verify_BppTrend_CaaValImportRetryErrorRecordsAndApprove(String loginUser) throws Exception {
		//Update the status of Approved data via SalesForce API & delete the records from tables
		String updateQuery = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYear + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", updateQuery, "Status__c", "Reverted");

		String deleteQuery = "select id from BPP_Composite_Factor__c where Roll_Year__c = '"+ rollYear +"'";
		//objSalesforceAPI.delete("E_File_Import_Log__c", deleteQuery);

		deleteQuery = "select id from BPP_Trend_Valuation_Factor__c where Roll_Year__c = '"+ rollYear +"'";
		//objSalesforceAPI.delete("E_File_Import_Log__c", deleteQuery);

		deleteQuery = "select id from BPP_Property_Good_Factor__c where Roll_Year__c = '"+ rollYear +"'";
		//objSalesforceAPI.delete("E_File_Import_Log__c", deleteQuery);

		deleteQuery = "select id from BPP_Property_Index_Factor__c where Roll_Year__c = '"+ rollYear +"'";
		//objSalesforceAPI.delete("E_File_Import_Log__c", deleteQuery);

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYear, fileName);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		//Step4: Validations for file count, import count and error count before retrying all errorred records
		Map<String, Object> dataMapWthRowCounts = objBppTrend.countOfDifferentRowTypesInExcel(fileName);
		int fileCountFromExcel = (int)dataMapWthRowCounts.get("File Count");
		int importCountFromExcel = (int)dataMapWthRowCounts.get("Import Count");
		int errorCountFromExcel = (int)dataMapWthRowCounts.get("Error Count");
		
		int fileCountBeforeRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		softAssert.assertEquals(fileCountBeforeRetrying, fileCountFromExcel, "SMAB-T112: Validating file count before retrying errorred records");
		int importCountBeforeRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		softAssert.assertEquals(importCountBeforeRetrying, importCountFromExcel, "SMAB-T112: Validating import count before retrying errorred records");
		int errorCountBeforeRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());			
		softAssert.assertEquals(errorCountBeforeRetrying, errorCountFromExcel, "SMAB-T112: Validating error count before retrying errorred records");
		
		//Step4: Click View link to navigate to review & approve page
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step5: Store columns / tables in a list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("CaaValuationTablesOnImportPage").split(",")));
		
		String errorredRowsBeforeRetrying = null;
		String importedRowsBeforeRetrying = null;
		
		//Step6: Iterate over all the tables / columns
		for (int i = 0; i < allTables.size(); i++) {
			//Step7: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step8: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false);
					
			int valueToEnter = 80;
			int numberOfErrorRecordsUnderCurrentTable = Integer.parseInt(objBppTrend.getCountOfRowsFromErrorRowsSection());
			
			//Step9: Iterating over all the tables to correct the invalid data 
			for(int j = 1; j <= numberOfErrorRecordsUnderCurrentTable; j++) {
				//Step10: Retrieving the imported and error count for current table
				errorredRowsBeforeRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
				importedRowsBeforeRetrying = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYear);
				
				//Step11: Editing the value of average column
				objBppTrend.waitForElementToBeClickable(objBppTrend.tableCellWithJunkTextOnImportPageTale);
				objBppTrend.Click(objBppTrend.tableCellWithJunkTextOnImportPageTale);
				
				objBppTrend.waitForElementToBeClickable(objBppTrend.editIconInImportPageTale);
				objBppTrend.javascriptClick(objBppTrend.editIconInImportPageTale);
				
				//Step12: Entering a correct value in the average column text box
				valueToEnter = valueToEnter - 1;
				objBppTrend.enter(objBppTrend.inputBoxOnImportPage, Integer.toString(valueToEnter));
				
				//Step13: Clicking on adjacent cell to move control from input box
				WebElement adjacentCell = objBppTrend.locateElement("((//lightning-formatted-text[text() = 'Junk_A'] | //lightning-formatted-text[text() = 'Junk_B'])[1])//ancestor::td//preceding-sibling::td[1]", 120);
				objBppTrend.Click(adjacentCell);
				Thread.sleep(1000);
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
		
		//Step16: Iterate over all the columns to check count of records
		for (int i = 0; i < allTables.size(); i++) {
			//Step17: Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Step18: Clicking on the given table name
			String tableName = allTables.get(i);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, false);
			
			//Step19: Retrieving count from error and import sections			
			String errorredRowsAfterRetrying = objBppTrend.getCountOfRowsFromErrorRowsSection();
			String importedRowsAfterRetrying = objBppTrend.getCountOfRowsFromImportedRowsSectionForValuationFile(rollYear);
			int importedRowsAfterRetryingInt = Integer.parseInt(importedRowsAfterRetrying) + Integer.parseInt(errorredRowsBeforeRetrying);
			importedRowsAfterRetrying = Integer.toString(importedRowsAfterRetryingInt);

			//Step20: Validating that corrected records are moved to imported row section after retry.
			softAssert.assertEquals(errorredRowsAfterRetrying, "0", "SMAB-T112: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
			int expImportedRowsAfterRetrying = Integer.parseInt(importedRowsBeforeRetrying) + Integer.parseInt(errorredRowsBeforeRetrying);
			softAssert.assertEquals(importedRowsAfterRetrying, Integer.toString(expImportedRowsAfterRetrying), "SMAB-T112: Validation if correct number of records are displayed in Imported Row Section after correcting and retrying the error record");
		}
		
		//Step21: Clicking approve button
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);

		//Step22: Searching the efile intake module to validate the status of the imported file after approve on history table
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "CAA - Valuation Factors");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T112: Validation if status of imported file is approved.");
		
		//Step23: Validations to check file count after successfully retrying and approving all error records in history table
		int fileCountAfterRetrying = Integer.parseInt(objBppTrend.getFileCountFromHistoryTable());
		int expFileCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(fileCountAfterRetrying, expFileCountAfterRetrying, "SMAB-T112: Validating file count after successfully retrying all errorred records");
		
		//Step24: Validations to check import count after successfully retrying and approving all error records in history table
		int importCountAfterRetrying = Integer.parseInt(objBppTrend.getImportCountFromHistoryTable());
		int expImportCountAfterRetrying = importCountBeforeRetrying + errorCountBeforeRetrying;
		softAssert.assertEquals(importCountAfterRetrying, expImportCountAfterRetrying, "SMAB-T112: Validating import count after successfully retrying all errorred records");
		
		//Step25: Validations to check error count after successfully retrying and approving all error records in history table
		int errorCountAfterRetrying = Integer.parseInt(objBppTrend.getErrorCountFromHistoryTable());		
		softAssert.assertEquals(errorCountAfterRetrying, 0, "SMAB-T112: Validating error count after successfully retrying all errorred records");
		
		//Step26: Checking the status on import logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportPage = objBppTrend.fileStatusOnImportLogsPage("CAA - Valuation Factors");
		softAssert.assertEquals(fileStatusOnImportPage, "Approved", "SMAB-T112: Validation if status of imported file is approved on import logs page.");

		//Step27: Checking the status on import logs details page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);
		String fileStatusOnImportLogDetailsPage = objBppTrend.getFieldValuesFromImportLogsDetailsPage("CAA - Valuation Factors", "Status");
		softAssert.assertEquals(fileStatusOnImportLogDetailsPage, "Approved", "SMAB-T112: Validation if status of imported file is approved on import logs details page.");		
		
		//Step28: Checking the status on transaction logs page
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		String fileStatusOnTransactionPage = objBppTrend.fileStatusOnImportTransactionPage("CAA - Valuation Factors");
		softAssert.assertEquals(fileStatusOnTransactionPage, "Imported", "SMAB-T112: Validation if status of imported file is approved on transactions page.");
		
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
	@Test(description = "SMAB-T105: Validating transformation rules on BOE Index file import", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke,regression,BppTrend"}, priority = 6, enabled = true)
	public void verify_BppTrend_TransformationRulesForImport(String loginUser) throws Exception {
		//Update the status of Approved data via SalesForce API
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + rollYear + "' and File_Source__C like '%Factors%' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
				
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		//objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.login(users.PRINCIPAL_USER);
		
		//Step2: Opening the file import in-take module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend BOE Index Factors file having error and success records
		String bppTrendIndexFactorsFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_TRANSFORMATION_RULES;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear, bppTrendIndexFactorsFile);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
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
						
			//Step8: Finding total number of records under the selected table
			int numberOfErrorRecordsValidate = 0;
			//int numberOfErrorRecords = Integer.parseInt(objBppTrend.getCountOfRowsFromErrorRowsSection());
			
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
			else if (tableName.equalsIgnoreCase("Construction ME Good Factors")) {
				columnNameToValidate = CONFIG.getProperty("columnWithErrorRecordsInConstMeGoodFactors");
				numberOfErrorRecordsValidate = Integer.parseInt(CONFIG.getProperty("errorRecordsInConstMeGoodFactors"));
				tableNumber = CONFIG.getProperty("indexNumberConstMeGoodFactors");
			}			
			//Step10: Setting table number to retrieve error row count
			System.setProperty("tableNumber", tableNumber);
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Transformation Validation On Column '"+ columnNameToValidate +"' ****");
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