package com.apas.Tests.EFileIntake;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.EFileImportLogsPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;


public class EFileIntake_Tests extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	ApasGenericFunctions apasGenericObj;
	BppTrendPage objBPPTrendPage;
	Util objUtil;
	SoftAssertion softAssert;
	String eFileTestDataPath;
	String athertonBuildingPermitFile;
	String athertonBuildingPermitFile1;
	String sanMateoBuildingPermitFile;
	String sanMateoBuildingPermitFileWithError;
	String unincorporatedBuildingPermitFile;
	SalesforceAPI salesforceAPI;
	EFileImportPage objEFileImport;
	EFileImportLogsPage objEFileImportLogPage;
	EFileImportTransactionsPage objEFileImportTransactionpage;
	String EFileinvalidFormatFilepath;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		apasGenericObj = new ApasGenericFunctions(driver);
		objEFileImport=new EFileImportPage(driver);
		objEFileImportLogPage=new EFileImportLogsPage(driver);
		objEFileImportTransactionpage=new EFileImportTransactionsPage(driver);
		objBPPTrendPage= new BppTrendPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		salesforceAPI=new SalesforceAPI();
		
		//eFileTestDataPath= System.getProperty("user.dir") + testdata.EFILEIMPORT_BPPTRENDSDATA + "BOE-IndexAndPercentGoodFactor.xlsx";
		eFileTestDataPath= System.getProperty("user.dir") + testdata.EFILEIMPORT_BPPTRENDSDATA + "CAAValuationFactors.xlsx";
		athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "Import_TestData_ValidAndInvalidScenarios_AT1.txt";
		athertonBuildingPermitFile1 = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "Import_TestData_ValidAndInvalidScenarios_AT2.txt";
		sanMateoBuildingPermitFile = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx";
		sanMateoBuildingPermitFileWithError = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "SanMateoBuildingPermitsWithValidAndInvalidData5.xlsx";
		unincorporatedBuildingPermitFile = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "Import_TestData_ValidAndInvalidScenarios_UN.txt";
		EFileinvalidFormatFilepath =System.getProperty("user.dir") + testdata.EFILEIMPORT_INVALIDDATA ;
		}
	
	/**
	 * This method is to verify invalid file types verification on E File import
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T82:Verify the admin user is not able to select file for import with unacceptable formats using Upload button", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })
	public void EFile_verifyInvalidFileTypesNotAllowedToImport(String loginUser)throws Exception{
		File dir=new File(EFileinvalidFormatFilepath);
		String[] fileList = dir.list();
		String period = objUtil.getCurrentDate("YYYY");
		
		//Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and Import_Period__C='" + period + "' and File_Source__C = 'BOE - Index and Percent Good Factors' and Status__c = 'Approved' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		apasGenericObj.login(loginUser);
		
		//Step2: Opening the E FILE IMPORT Module
		apasGenericObj.searchModule(EFILE_INTAKE);
		
		//step3:verifying for Building Permit, only excel(xlsx) files are allowed
		objEFileImport.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		objPage.Click(objEFileImport.nextButton);
		apasGenericObj.selectFromDropDown(objEFileImport.periodDropdown, period);//period drop down xpath needs to be updated
		objPage.waitForElementToBeClickable(objEFileImport.confirmButton, 10);
		objPage.Click(objEFileImport.confirmButton);
		Thread.sleep(2000);
		
		  for(String name:fileList){
        	objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton,5);
        	ReportLogger.INFO("Verify invalid file format not allowed for file:"+name);
        	objEFileImport.uploadFileInputBox.sendKeys(EFileinvalidFormatFilepath+name);
        	objPage.waitForElementToBeClickable(objEFileImport.invalidFileErrorMsg,5);
        	
        	softAssert.assertEquals(objEFileImport.invalidFileErrorMsg.getText(),"Your company doesn't support the following file types: ."+name.substring(name.lastIndexOf(".")+1),"SMAB-T82:Verify the admin user is not able to select file for import with unacceptable formats using Upload button");
    	 }
        objPage.Click(objEFileImport.closeButton);
        
        apasGenericObj.logout();
	}
	
	
	/**
	 * This method is to verify EFile import for New status and 'View' and 'File' link 
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T15,SMAB-T65,SMAB-T87,SMAB-T100,SMAB-T49,SMAB-T58,SMAB-T88,SMAB-T959,SMAB-T575,SMAB-T915,SMAB-T1155:Verify that Users are able to import e-files through E-File Import Tool for 'New' status records", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })
	public void EFileImport_VerifyImportForNewStatus_AndApporveImportFile(String loginUser) throws Exception{
	
		//String period = objUtil.getCurrentDate("MMMM YYYY");
		String period = "Adhoc";

		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		
		apasGenericObj.login(loginUser);
		
		//Step2: Opening the E FILE IMPORT Module
		apasGenericObj.searchModule(EFILE_INTAKE);
		
		//step3: creating a file import record with New status
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		ReportLogger.INFO("Verify user has ability to view and verify more than 10 records in History table ");
		softAssert.assertEquals(objEFileImport.historyListItems.size(), 10, "SMAB-T15:Verify user has ability to view and verify more than 10 records in History table");
		ReportLogger.INFO("Verify user is able to see More button");
		objPage.Click(objEFileImport.moreButton);
		objPage.scrollToTop();
		
		//step4:creating an entry with New Status
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		objPage.waitForElementToBeClickable(objEFileImport.confirmButton, 20);
		objPage.Click(objEFileImport.confirmButton);
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 15);
		objPage.Click(objEFileImport.closeButton);
		
		//step5: verifying View link for New status
		driver.navigate().refresh();
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		ReportLogger.INFO("Verify View link is displayed for 'New' status record");
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord),"View","SMAB-T65:Verify 'View' link is displayed for records with status- 'New' in history table");
		
		
		//step6: verifying log created With New status
		ReportLogger.INFO("Verify log genertared for 'New' status record");
		//objPage.Click(objEFileImportTransactionpage.efileImportLogLabel);
		apasGenericObj.globalSearchRecords(fileType+" :"+source+" :"+period);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"New", "SMAB-T87:Verify that user is able to see logs record for file type with status 'New' on 'E-File Import Logs' screen");

		//step7: importing file for New status record from history list table
		ReportLogger.INFO("Verify File import for 'New' status record");
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		objPage.Click(objEFileImport.viewLinkRecord);
		ReportLogger.INFO("Verify duplicate messgae does not appear for 'New' status record");
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 10);
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(objEFileImport.fileAlreadyApprovedMsg), "SMAB-T100:Verify duplicate warning message should not appear for New Status");
		objEFileImport.uploadFileInputBox.sendKeys(sanMateoBuildingPermitFile);
		Thread.sleep(2000);
		objPage.waitForElementToBeClickable(objEFileImport.doneButton);
		Thread.sleep(4000);
		objPage.javascriptClick(objEFileImport.doneButton);
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
	
		
		//step8:verifying status of file import, view link for imported file and file download link
		softAssert.assertEquals(objPage.getElementText(objEFileImport.statusImportedFile),"Imported","SMAB-T959:Verify that Users are able to import e-files through E-File Import Tool for 'New' status records");
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord),"View","SMAB-T65:Verify 'View' link is displayed for records with status- 'Imported' in history table");
		softAssert.assertEquals(objEFileImport.fileLink.isDisplayed(),true,"SMAB-T915:Verify 'File' link is displayed for records with status- 'Imported' in history table");
		
		//step9: verifying log for Imported status 
		ReportLogger.INFO("Verify log for 'Imported' status record");
		apasGenericObj.globalSearchRecords(fileType+" :"+source+" :"+period);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"Imported", "SMAB-T87:Verify that user is able to see logs record for file type with status 'Imported' on 'E-File Import Logs' screen");

		//step10:verifying transaction trail fields
		ReportLogger.INFO("Verify transaction for 'Imported' status record");
		objPage.Click(objEFileImportTransactionpage.transactionsTab);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.viewAlllink, 10);
		String expectedTransactionID=objPage.getElementText(objEFileImportTransactionpage.transactionsRecords.get(0));
		objPage.javascriptClick(objEFileImportTransactionpage.transactionsRecords.get(0));
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.statusLabel, 10);
		ReportLogger.INFO("Verify transaction trail record for 'Imported' status record");
		objPage.javascriptClick(objEFileImportTransactionpage.transactionTrailTab);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.viewAlllink, 10);
		objPage.javascriptClick(objEFileImportTransactionpage.transactionTrailRecords.get(0));
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.statusLabel, 10);
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.transactionType), "Transactions", "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.transactionSubType), "E-File Intake", "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.efileImportTransactionLookUp), expectedTransactionID, "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objEFileImportTransactionpage.transactionBuildingPermit.isDisplayed(), true, "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objEFileImportTransactionpage.transactionAPN.isDisplayed(), true, "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.transactionDescription), "Permit uploaded - Electronic File Intake", "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");		
		
		
		//step11: approving the imported file and verifying View link, file download link for Approved status
		ReportLogger.INFO("Verify Approving the imported file record");
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		objPage.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
		softAssert.assertTrue(objEFileImport.errorRowSection.isDisplayed(), "SMAB-T49:Verify Error & Success records are displayed to user");
		softAssert.assertTrue(objEFileImport.successRowSection.isDisplayed(), "SMAB-T49:Verify Error & Success records are displayed to user");
		ReportLogger.INFO("Verify Discarding error records if any");
		objPage.Click(objEFileImport.approveButton);
		objPage.waitForElementToBeClickable(objEFileImport.efileRecordsApproveSuccessMessage, 10);
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(objEFileImport.revertButton),"SMAB-T58:Verify 'Revert' button is disabled after approve button is clicked");

		//step12:navigating back to efile import tool screen
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord),"View","SMAB-T65,SMAB-T575:Verify 'View' link is displayed for records with status- 'Approved' in history table");
		softAssert.assertEquals(objEFileImport.fileLink.isDisplayed(),true,"SMAB-T915:Verify 'File' link is displayed for records with status- 'Approved' in history table");
		
		//step13: verifying log for Approved status record
		ReportLogger.INFO("Verify log for 'Approved 'record");
		apasGenericObj.globalSearchRecords(fileType+" :"+source+" :"+period);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"Approved","SMAB-T88:Verify that user is able to see Logs record for file once records has been 'Approved' on 'E-File Import Logs' screen");
		
		apasGenericObj.logout();
	}	
	
	
	/**
	 * This method is to verify File is not imported for already approved file
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T578,SMAB-T975:Verify user is not able to import a file for BPP Trends if the previous Import for a particular File Type, File Source and Period was Approved", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })
	public void EFileIntake_VerifyAlreadyApprovedFileNotImported(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="Atherton Building Permits";
		
		apasGenericObj.login(loginUser);

		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
	
		//Step2: Opening the E FILE IMPORT Module
		apasGenericObj.searchModule(EFILE_INTAKE);
		
		///step3: importing a file
		objEFileImport.uploadFileOnEfileIntakeBP(fileType, source,"Import_TestData_ValidAndInvalidScenarios_AT1.txt",athertonBuildingPermitFile);
		
		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
		
		
		///step5: importing the file again for same file type, source and period to verify view link is not available for previous import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType, source,"Import_TestData_ValidAndInvalidScenarios_AT2.txt",athertonBuildingPermitFile1);
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord), "View", "SMAB-T578:Verify user is able to see 'View' button only for the latest Imported file from all 'Imported' status log");
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(objEFileImport.viewLinkForPreviousImport), "SMAB-T578:Verify user is able to see 'View' button only for the latest Imported file from all 'Imported' status log");
		//step6: approving the imported file
		objPage.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
		objPage.waitForElementToBeClickable(objEFileImport.approveButton, 10);
		objPage.Click(objEFileImport.approveButton);
		objPage.waitForElementToBeClickable(objEFileImport.efileRecordsApproveSuccessMessage, 20);
		
		//step7: trying to upload a file for the same file type ,source and period
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "Import_TestData_ValidAndInvalidScenarios_AT1.txt");
		objPage.Click(objEFileImport.fileNameNext);
		
		//step8: verifying error message while trying to import file for already approved file type,source and period
		softAssert.assertContains(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg), "This file has been already approved", "SMAB-T975:Verify user is not able to import a file for BPP Trends if the previous Import for a particular File Type, File Source and Period was Approved");
		objPage.Click(objEFileImport.closeButton);
		
		apasGenericObj.logout();
	}
	
	
	/**
	 * This method is to verify validation after reverting the file
	 * @param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T68,SMAB-T90,SMAB-T915,SMAB-T101:Verify that user without permission is not able to revert the records from file", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })
	public void EFileIntake_VerifyValidationAfterRevertedStatus(String loginUser) throws Exception{
		//String period = objUtil.getCurrentDate("YYYY");
		apasGenericObj.login(loginUser);
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

					
		//Step2: Opening the E FILE IMPORT Module
		apasGenericObj.searchModule(EFILE_INTAKE);
		
		///step3: importing a file
		objEFileImport.uploadFileOnEfileIntakeBP(fileType,source,"SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx",sanMateoBuildingPermitFile);
				
		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);

		
		//Step6: Reverting the imported file
		objPage.Click(objEFileImport.viewLink);
		objPage.waitForElementToBeVisible(objEFileImport.errorRowSection,30);
		objPage.Click(objEFileImport.revertButton);
		objPage.waitForElementToBeClickable(objEFileImport.discardContinue, 10);
		objPage.Click(objEFileImport.discardContinue);
		objPage.waitForElementToBeVisible(objEFileImport.revertSuccessMessage, 30);

		
		//step7:navigating back to efile import tool screen
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(objEFileImport.viewLinkRecord),"SMAB-T68:Verify View link is not displayed for records in history table apart from statuses - 'Imported','New' and 'In Progress' and Approved");
		softAssert.assertTrue(objEFileImport.fileLink.isDisplayed(),"SMAB-T915:Verify 'File' link is displayed for records with status- 'Reverted' in history table");
			
		//step8: verifying log for Reverted status record
		apasGenericObj.globalSearchRecords(fileType+" :"+source+" :"+period);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"Reverted","SMAB-T90:Verify that user is able to see Logs record for file once records has been 'Reverted' on 'E-File Import Logs' screen");
		
		//step9: verifying transaction for Reverted status record
		ReportLogger.INFO("Verify transaction for 'Revrted' status record");
		objPage.Click(objEFileImportTransactionpage.transactionsTab);
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.transactionsRecords.get(0), 10);
		//String expectedTransactionID=objPage.getElementText(objEFileImportTransactionpage.transactionsRecords.get(0));
		objPage.javascriptClick(objEFileImportTransactionpage.transactionsRecords.get(0));
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.statusLabel, 10);
		
		
		//step10:verifying duplicate error msg does not come for reverted status
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 15);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		
		//apasGenericObj.selectFromDropDown(objEFileImport.periodDropdown, period);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg),"Do you want to import ?","SMAB-T101:Verify duplicate warning message should not appear for Reverted Status from Imported status");
		
		objPage.Click(objEFileImport.closeButton);
		apasGenericObj.logout();
}
	
	/**
	 * This method is to verify File Revert is not allowed to unassigned user
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T28:Verify that user without permission is not able to revert the records from file", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })	
	public void EFileIntake_VerifyRevertNotAllowedForUnAssigednUser(String loginUser) throws Exception{
		
		//String period = objUtil.getCurrentDate("MMMM YYYY");
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		apasGenericObj.login(loginUser);

		//Step3: Opening the file import intake module
		apasGenericObj.searchModule(modules.EFILE_INTAKE);

		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType,source, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx" ,sanMateoBuildingPermitFile);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
		apasGenericObj.logout();
		
		//step6:now logging in with different user and verifying 'Revert' button invisibility 
		apasGenericObj.login(BPP_AUDITOR);
		apasGenericObj.searchModule(modules.EFILE_INTAKE);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		objEFileImport.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(objEFileImport.revertButton),  "SMAB-T28:Verify that user without permission is not able to revert the records from file");
		apasGenericObj.logout();
	}
	
	/**
	 * This method is to verify records count in import logs
	 * @param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T32,SMAB-T33,SMAB-T36,SMAB-T1403,SMAB-T1402:Verify user is able to see number of records count from file import action on 'E-File Import Logs' screen", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })	
	public void EFileIntake_VerifyImportLogsRecordCount(String loginUser) throws Exception{
		String uploadedDate = objUtil.getCurrentDate("MMM d, YYYY");
		
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//Step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
						
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		apasGenericObj.login(loginUser);

		//Step3: Opening the file import intake module
		apasGenericObj.searchModule(modules.EFILE_INTAKE);

		
		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType,source, "SanMateoBuildingPermitsWithValidAndInvalidData5.xlsx" ,sanMateoBuildingPermitFileWithError);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
		
		
		//step6: verify import list record entry and data
		
				HashMap<String, ArrayList<String>> importedEntry=apasGenericObj.getGridDataInHashMap(1, 1);
				softAssert.assertEquals(importedEntry.get("Uploaded Date").toString().replaceAll("[\\[\\]\\(\\)]", ""), uploadedDate, "verify import list history data");
				softAssert.assertEquals(importedEntry.get("Period").toString().replaceAll("[\\[\\]\\(\\)]", ""), "Adhoc", "verify import list history data");
				softAssert.assertEquals(importedEntry.get("File Count").toString().replaceAll("[\\[\\]\\(\\)]", ""), "4", "verify import list history data");
				softAssert.assertEquals(importedEntry.get("Import Count").toString().replaceAll("[\\[\\]\\(\\)]", ""), "2", "verify import list history data");
				softAssert.assertEquals(importedEntry.get("Error Count").toString().replaceAll("[\\[\\]\\(\\)]", ""), "2", "verify import list history data");
				softAssert.assertEquals(importedEntry.get("Discard Count").toString().replaceAll("[\\[\\]\\(\\)]", ""), "0", "verify import list history data");
				softAssert.assertEquals(importedEntry.get("Number of Tries").toString().replaceAll("[\\[\\]\\(\\)]", ""), "1", "verify import list history data");
				
		
		
		objEFileImport.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 10);
		String errorrecords=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();
		String successRecords=objEFileImport.importedRowCount.getText().substring(objEFileImport.importedRowCount.getText().indexOf(":")+1, objEFileImport.importedRowCount.getText().length()).trim();;
		String totalRecords=Integer.toString(Integer.parseInt(errorrecords)+Integer.parseInt(successRecords));

		//step7: navigating to EFile import logs screen and verifying the records count
		apasGenericObj.globalSearchRecords(fileType+" :"+source+" :"+period);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logFileCount),totalRecords, "SMAB-T32:Verify user is able to see number of records count from file import action on 'E-File Import Logs' screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logImportCount),successRecords, "SMAB-T33:Verify user is able to see the number of successful imports completed in 'E-File import Logs' Screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logErrorCount),errorrecords, "SMAB-T36:Verify user is able to track number of error records for the log in 'E-File Import Logs' screen");
		
		
		//step8:verifying the discarded count scenario
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		objEFileImport.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 10);
		softAssert.assertEquals(Integer.parseInt(errorrecords), "2", "Verify number of error records are same before discard(ERROR ROWS:Count)");
		softAssert.assertEquals(objEFileImport.errorRecordsRows.size(), "2","Verify number of error records are same before discard(actual records count)");
		
		ReportLogger.INFO("Now discarding an Error record");
		objPage.Click(objEFileImport.errorRecordsRows.get(0));
		objPage.Click(objEFileImport.discardButton);
		objPage.waitForElementToBeClickable(objEFileImport.discardContinue, 5);
		objPage.Click(objEFileImport.discardContinue);
		objBPPTrendPage.waitForPageSpinnerToDisappear(15);
		Thread.sleep(2000);
		String errorrecordsAfterDisacrd=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();

		softAssert.assertEquals(Integer.parseInt(errorrecordsAfterDisacrd),"1", "Verify number of error records are same after discarding an error record(ERROR ROWS:Count)");
		softAssert.assertEquals(objEFileImport.errorRecordsRows.size(),"1" ,"Verify number of error records are same after discarding an error record(actual records count)");
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		softAssert.assertEquals(objEFileImport.getElementText(objEFileImport.disacrdCount),"1","SMAB-T68:Verify View link is not displayed for records in history table apart from statuses - 'Imported','New' and 'In Progress' and Approved");
		objPage.Click(objEFileImport.viewLink);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 30);
		String errorrecordsAfterDisacrd1=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();
		softAssert.assertEquals(Integer.parseInt(errorrecordsAfterDisacrd1),"1", "Verify number of error records are same after discarding an error record(ERROR ROWS:Count)");
		softAssert.assertEquals(objEFileImport.errorRecordsRows.size(),"1" ,"Verify number of error records are same after discarding an error record(actual records count)");
		
		ReportLogger.INFO("Now discarding all error records");
		objPage.Click(objEFileImport.discardAllCheckbox);
		objPage.Click(objEFileImport.discardButton);
		objPage.waitForElementToBeClickable(objEFileImport.discardContinue, 5);
		objPage.Click(objEFileImport.discardContinue);
		objBPPTrendPage.waitForPageSpinnerToDisappear(15);
		Thread.sleep(2000);
		String errorCountAfterDiscard=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(objEFileImport.discardAllCheckbox), "SMAB-T1403,SMAB-T1402:Verify user is not able to view discarded record(s) for a file that is imported for Building Permit file type");
		softAssert.assertEquals(errorCountAfterDiscard, "0", "SMAB-T1403,SMAB-T1402:Verify user is not able to view discarded record(s) for a file that is imported for Building Permit file type");
		
		//step9:navigating back to efile import tool screen and verifying the discard count
		objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitUntilElementDisplayed(objEFileImport.nextButton, 15);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.disacrdCount),"2","SMAB-T68:Verify View link is not displayed for records in history table apart from statuses - 'Imported','New' and 'In Progress' and Approved");
		apasGenericObj.logout();
	}
	
	
	/**
	 * This method is to verify File type has correct Source values
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T102:Verify user is able to see number of records count from file import action on 'E-File Import Logs' screen", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })
	public void EFile_VerifyFileTypeAndCorrespondingSources(String loginUser) throws Exception{
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		apasGenericObj.login(loginUser);

		//Step2: Opening the file import intake module
		apasGenericObj.searchModule(modules.EFILE_INTAKE);
		
		//step3:verifying file type and sources
		ReportLogger.INFO("Verifying Sources for BPP Trend Factors");
		apasGenericObj.selectFromDropDown(objEFileImport.fileTypedropdown, "BPP Trend Factors");
		objPage.waitForElementToBeClickable(objEFileImport.sourceDropdown, 5);
		objPage.Click(objEFileImport.sourceDropdown);
		String expectedSourcesBPP = "CAA - Valuation Factors\nBOE - Index and Percent Good Factors\nBOE - Valuation Factors";
		String actualSourcesBPP = objPage.getElementText(objEFileImport.sourceDropdownOptions);
		//softAssert.assertEquals(objPage.getElementText(objEFileImport.sourceDropdownOptions),expectedSourcesBPP,"SMAB-T102: Verify user is able to view and select the file types and source for that file type on E-File Import Tool screen");
		softAssert.assertTrue(objPage.compareDropDownvalues(actualSourcesBPP, expectedSourcesBPP), "SMAB-T102: Verify user is able to view and select the file types and source for that file type on E-File Import Tool screen");
		
		driver.navigate().refresh();
		ReportLogger.INFO("Verifying Sources for Building Permit");
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 15);
		apasGenericObj.selectFromDropDown(objEFileImport.fileTypedropdown, "Building Permit");
		objPage.waitForElementToBeClickable(objEFileImport.sourceDropdown, 5);
		objPage.Click(objEFileImport.sourceDropdown);
		String expectedSourcesBP = "Millbrae Building permits\nUnincorporated Building permits\nBelmont Building permits\nSan Bruno Building permits\nBurlingame Building permits\nHillsborough Building permits\nWoodside Building permits\nSan Mateo Building permits\nSouth San Francisco Building permits\nRedwood City Building permits\nAtherton Building Permits";
		String actualSourcesBP = objPage.getElementText(objEFileImport.sourceDropdownOptions);
		//softAssert.assertEquals(objPage.getElementText(objEFileImport.sourceDropdownOptions),expectedSourcesBP,"SMAB-T102: Verify user is able to view and select the file types and source for that file type on E-File Import Tool screen");
		softAssert.assertTrue(objPage.compareDropDownvalues(actualSourcesBP, expectedSourcesBP), "SMAB-T102: Verify user is able to view and select the file types and source for that file type on E-File Import Tool screen");
		
		driver.navigate().refresh();
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		apasGenericObj.logout();
		
	}
	
	/**
	 * This method is to verify File type has correct Source values
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1144:Verify that user is not able to upload a file if a file is already 'In Progress' status for the selected 'File type' ,'Source' and 'Period'", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","EFileImport" })	
	public void EFile_VerifyFileNotimportedIfAlreadyInProgress(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//Step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
							
	
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		apasGenericObj.login(loginUser);
		
		
		apasGenericObj.searchModule(modules.EFILE_INTAKE);
		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType,source, "SanMateoBuildingPermitsWithValidAndInvalidData5.xlsx" ,sanMateoBuildingPermitFileWithError);

		//Step5: Waiting for Status of the imported file to be converted to "InProgress"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to InProgress");
		
		//Step6:trying to upload another file and verify msg
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData5.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(objEFileImport.confirmButton), "SMAB-T1144:Verify that user is not able to upload a file if a file is already 'In Progress' status for the selected 'File type' ,'Source' and 'Period'");
		softAssert.assertContains(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg),"This is already in In Progress" ,"SMAB-T1144:Verify that user is not able to upload a file if a file is already 'In Progress' status for the selected 'File type' ,'Source' and 'Period'");
		
		objPage.Click(objEFileImport.closeButton);
		apasGenericObj.logout();
	}
	
	
}
		
		
	
	