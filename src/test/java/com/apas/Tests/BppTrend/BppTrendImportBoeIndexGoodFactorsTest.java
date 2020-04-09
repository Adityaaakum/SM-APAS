package com.apas.Tests.BppTrend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendImportBoeIndexGoodFactorsTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrend;
	EFileImportPage objEfileHomePage;
	Util objUtil;
	SoftAssertion softAssert;

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrend = new BppTrendPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}
		
	@AfterMethod
	public void afterMethod() throws IOException, InterruptedException{
		//objApasGenericFunctions.logout();
	}
	
	/**
	 * Below test case is used to validate the discard functionality on the file by discarding all error records and then approve functionality
	 */
	@Test(description = "SMAB-T362,SMAB-T363,SMAB-T430: Verification for e-file import of BPP Trend CAA & BOE Valuation and BOE Index files in XLSX Format", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression"}, priority = 0, enabled = true)
	public void verifyBppTrendDiscardErrorRecordsAndApproveimport(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
//		//Step3: Uploading the Bpp Trend BOE Index Factors file having error and success records
		String rollYear = CONFIG.getProperty("rollYear");
//		String bppTrendIndexFactorsFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
//		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear, bppTrendIndexFactorsFile);		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
//		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		//Step4: Uploading the Bpp Trend CAA Valuation Factors file having error and success records
		String bppTrendCaaValuationFactorsFile = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VALUATION_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "CAA - Valuation Factors", rollYear, bppTrendCaaValuationFactorsFile);		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

//		//Step5: Uploading the Bpp Trend BOE Valuation Factors file having error and success records
//		String bppTrendBoeValuationFactorsFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VALUATION_FACTORS;
//		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Valuation Factors", rollYear, bppTrendBoeValuationFactorsFile);		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
//		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
//		objEfileHomePage.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		
		objPage.Click(objEfileHomePage.viewLink);
		
		//Step6: Store columns outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tablesOutsideMoreTabOnImportPage").split(",")));
		String tableNamesUnderMoreTab = CONFIG.getProperty("tablesUnderMoreTabOnImportPage");
		allTables.addAll(Arrays.asList(tableNamesUnderMoreTab.split(",")));
		
		List<String> allErrorRecords = new ArrayList<String>();
		allErrorRecords.addAll(Arrays.asList(CONFIG.getProperty("errorRecordsForTablesOutsideMoreTab").split(",")));
		allErrorRecords.addAll(Arrays.asList(CONFIG.getProperty("errorRecordsForTablesUnderMoreTab").split(",")));
		
		//Step7: Iterate over all the columns
		for (int i = 0; i < allTables.size(); i++) {
			// Setting tableNumber property to iterate tables sequentially in DOM during file import
			String tableNumber = Integer.toString(i + 1);
			System.setProperty("tableNumber", tableNumber);
			
			//Clicking on the given table name
			String tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
			if(i == 4) {
				Thread.sleep(10000);
			} else {
				Thread.sleep(4000);
			}
			
			//Validating that correct number of records are moved to error and imported row sections after file import			
			String errorRecords = allErrorRecords.get(i);
			String successRecords = CONFIG.getProperty("successRecordsIndexAndGoodsFactors");
			boolean isErrorRowSectionDisplayed = objBppTrend.checkPresenceOfErrorRowsSection();
			boolean isImportedRowSectionDisplayed = objBppTrend.checkPresenceOfImportedRowsSection();;
			
			if (isErrorRowSectionDisplayed){
				//Validation of number of records in error row section.
				String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection();
				softAssert.assertEquals(numberOfRecordsInErrorRowSection, errorRecords, "SMAB-T362: Validation if correct number of records are displayed in Error Row Section after file import");
			} else {
				//Validating if the error row section is coming on review and approve page after clicking "View Link" from history table 
				softAssert.assertTrue(false,"SMAB-T362: Validation for Error Row Section presence after clickig view link button");	
			}
			
			if (isImportedRowSectionDisplayed){
				//Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file
				String numberOfRecordsInImportedRowSection = objBppTrend.getCountOfRowsFromImportedRowsSection();
				softAssert.assertEquals(numberOfRecordsInImportedRowSection, successRecords, "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after file import");
			} else {
				//Validating if the imported row section is coming on review and approve page after clicking "View Link" from history table
				softAssert.assertTrue(false,"SMAB-T362: Validation for Imported Row Section presence after clickig view link button");	
			}				

			//Validation for Records discard functionality from Review and Approve Page
			ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T363: Validation that error records can be dicarded from Review and Approve Data Page");
			objBppTrend.discardIndividualErrorRow();
			String numberOfRecordsInErrorRowSection = objBppTrend.getCountOfRowsFromErrorRowsSection();

			int updatedCount = Integer.parseInt(errorRecords) - 1;
			String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);
			//validating the number of records in the error row section after discarding a record
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, updatedRecordsInErrorRowPostDelete, "SMAB-T363: Validation if correct number of records are displayed in Error Row Section after discarding a record");

			if(Integer.parseInt(numberOfRecordsInErrorRowSection) > 0) {
				ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T362: Validation that status is approved after approving all the records");
				objBppTrend.discardAllErrorRows();
			}
		}
		
		//Validating the approve functionality after all the records are cleared from error section and approved
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);
		
		//Validating the status of e-file 
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T362: Validation if status of imported file is approved.");
		
		//Step9: Opening the Efile import transaction module
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		objBppTrend.clickOnBoeIndexAndGoodFactorsImportLog(rollYear);
		
		//By default "Details" tab should be opened showing the transaction details of the imported file
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab, "aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		//Status of the file should be displayed as imported as the file is imported 
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");

		softAssert.assertAll();
	}
	
    /**
	 Below test case is used to validate the revert functionality on the file having the error records
	 */
	@Test(description = "SMAB-T361,SMAB-T358: Reverting the error records in BPP Trend import", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression"}, priority = 2, enabled = false)
	public void verifyBppTrendRevertImport(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading BPP Trend BOE Index factors file having error and success records through Efile Intake Import
		String rollYear = CONFIG.getProperty("rollYear");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the Bpp Trend Boe Index Factor file");
		String bppTrendIndexFactorsFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear, bppTrendIndexFactorsFile);		

		//Step4: Validating that status of the imported file is in progress
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T361: Validation if status of imported file is in progress.");
		
		//Step5: Waiting for the status of the file to be converted to Imported
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		ExtentTestManager.getTest().log(LogStatus.INFO, "Reverting  Imported Records on File Import History table");
		//Validation of number of times tried retried column for the imported file. Expected is 1 as it has not be retried/reverted yet
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T358: Validation if number of times try/retry count is correct on file import");
		//Validation of "Total Records in File" column for the imported file. Expected is 10 as 10 records are sent in the file
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T358: Validation if total number of records count is correct on file import");
		//Validation of "Total Records Imported" column for the imported file. Expected is 1 as 1 records has the correct record out of 10 records sent in the file
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "1", "SMAB-T358: Validation if total records in file count  is correct on file import");
		
		//Step6: Reverting the BPP Trend file on Review and Approve Screen
		ExtentTestManager.getTest().log(LogStatus.INFO, "Reverting the imported file");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step7: Validation of the file status after reverting the imported file
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		//Status of the imported file should be changed to Reverted as the whole file is reverted for reimport
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T361: Validation if status of imported file is reverted.");
		softAssert.assertAll();
	}
	
    /**
	 Below test case is used to validate the retry functionality after correction on the records in error
	 */
	@Test(description = "SMAB-T364: Retrying the error records in building permit import", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression"}, priority = 3, enabled = false)
	public void verifyBppTrendRetryImport(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the BPP Trend file having error and success records through Efile Intake Import
		String rollYear = CONFIG.getProperty("rollYear");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the Bpp Trend Boe Index Factor file");
		String bppTrendIndexFactorsFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS;
		objEfileHomePage.uploadFileOnEfileIntake("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear, bppTrendIndexFactorsFile);
		
		//Step4: Validating that status of the imported file is in progress
		ExtentTestManager.getTest().log(LogStatus.INFO, "Retrying Imported Records after error correction on review and approve page");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T364: Validation if status of imported file is in progress.");
		
		//Step5: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);

		//Step6: Store columns outside & under more tab in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tablesOutsideMoreTabOnImportPage").split(",")));
		String tableNamesUnderMoreTab = CONFIG.getProperty("tablesUnderMoreTabOnImportPage");
		allTables.addAll(Arrays.asList(tableNamesUnderMoreTab.split(",")));
		
		List<String> allErrorRecords = new ArrayList<String>();
		allErrorRecords.addAll(Arrays.asList(CONFIG.getProperty("errorRecordsForTablesOutsideMoreTab").split(",")));
		allErrorRecords.addAll(Arrays.asList(CONFIG.getProperty("errorRecordsForTablesUnderMoreTab").split(",")));
		
		for (int i = 0; i < allTables.size(); i++) {			
			//Clicking on the given table name
			String tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrend.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
			if(i == 4) {
				Thread.sleep(10000);
			} else {
				Thread.sleep(4000);
			}
		
			//Step7: Correcting one of the error record to validate the retry functionality
			ExtentTestManager.getTest().log(LogStatus.INFO, "Correcting the error record to retry");
			objPage.Click(objEfileHomePage.rowSelectCheckBox);
			if(tableName.equalsIgnoreCase("M&E Good Factors")) {
				objApasGenericFunctions.editGridCellValue(CONFIG.getProperty("BoeIndexGoodFactorsColumnToUpdateForMEGoodFactors"), "10");
			} else {
				objApasGenericFunctions.editGridCellValue(CONFIG.getProperty("BoeIndexGoodFactorsColumnToUpdate"), "10");
			}
			
			objPage.Click(objEfileHomePage.retryButton);
			Thread.sleep(2000);
			objPage.validateAbsenceOfElement(objBppTrend.statusSpinner, 60);
			objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection, 30);
			
			//Step8: Validating that corrected records are moved to imported row section after retry.		
			ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of error and inported records on Review and Approve Data Screen after retry");
			String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, "8", "SMAB-T364: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
		
			String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileHomePage.importedRowSection).split(":")[1].trim();
			softAssert.assertEquals(numberOfRecordsInImportedRowSection, "2", "SMAB-T364: Validation if correct number of records are displayed in Imported Row Section after correcting and retrying the error record");
			
			//Step9: Validation of import history columns as the value should be updated based on the records retried from error section
			ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of error and inported records on Import History table after retry");
			//Opening the Efile intake module
			objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
			objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
			softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "2", "SMAB-T364: Validation if number of times try/retry count is increased by 1 after retrying the error records");
			softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T364: Validation if total number of records remain same on the table");
			softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "2", "SMAB-T364: Validation if total records in file count is increased by 1 after retrying the error records");
		}
		softAssert.assertAll();
	}
}