package com.apas.Tests.EFileIntake;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

import org.openqa.selenium.By;
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

public class EFileIntake_Tests extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
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
		"Regression","EFileImport" })
	public void EFile_verifyInvalidFileTypesNotAllowedToImport(String loginUser)throws Exception{

		File dir=new File(EFileinvalidFormatFilepath);
		String[] fileList = dir.list();
		
		//changing the status of Approved Import logs if any in the system in order to import a new file
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and File_Source__C = 'BOE - Index and Percent Good Factors' and Status__c = 'Approved' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		objEFileImport.login(loginUser);
		
		//Step2: Opening the E FILE IMPORT Module
		objEFileImport.searchModule(EFILE_INTAKE);
		
		//step3:verifying for Building Permit, only excel(xlsx) files are allowed
		objEFileImport.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		objPage.Click(objEFileImport.nextButton);
		objEFileImport.selectOptionFromDropDown("Period", "2021");//period drop down xpath needs to be updated
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
        
        objEFileImport.logout();
	
	}
	
	
	/**
	 * This method is to verify EFile import for New status and 'View' and 'File' link 
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T15,SMAB-T65,SMAB-T87,SMAB-T100,SMAB-T49,SMAB-T58,SMAB-T88,SMAB-T959,SMAB-T575,SMAB-T915,SMAB-T1155,SMAB-T1550,SMAB-T1511,SMAB-T1510,SMAB-T1793:Verify that Users are able to import e-files through E-File Import Tool for 'New' status records", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression","EFileImport" })
	public void EFileImport_VerifyImportForNewStatus_AndApporveImportFile(String loginUser) throws Exception{
		String period = "Adhoc";

		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported','New') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		
		objEFileImport.login(loginUser);
		
		//Step2: Opening the E FILE IMPORT Module
		objEFileImport.searchModule(EFILE_INTAKE);
		
		//step3: creating a file import record with New status
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		ReportLogger.INFO("Verify user has ability to view and verify more than 10 records in History table ");
		softAssert.assertEquals(objEFileImport.historyListItems.size(), 10, "SMAB-T15:Verify user has ability to view and verify more than 10 records in History table");
		ReportLogger.INFO("Verify user is able to see More button");
		objPage.scrollToBottomOfPage();
		objPage.Click(objEFileImport.moreButton);
		objPage.scrollToTopOfPage();
		
		//step4:creating an entry with New Status
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsTestdata123.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		objPage.waitForElementToBeClickable(objEFileImport.confirmButton, 20);
		objPage.Click(objEFileImport.confirmButton);
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 15);
		objPage.Click(objEFileImport.closeButton);
		
		//step4a:try Creating a 'New' status entry again in system
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		objPage.Click(objEFileImport.confirmButton);
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 10);
		objPage.Click(objEFileImport.closeButton);		
		
		//step4b:verifying the count of 'new' status record
		String newStatusQuery = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Status__c ='New'";
		HashMap<String,ArrayList<String>> response1=salesforceAPI.select(newStatusQuery);
		int newCountInsystem=response1.get("Id").size();
		
		softAssert.assertEquals(newCountInsystem, 1, "SMAB-T1793:Verify user is able to use existing \"New\" entry Import log instead of creating a new \"New\" Import log record while importing a Building Permit file");
		
		
		//step5: verifying View link for New status
		driver.navigate().refresh();
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeVisible(objEFileImport.nextButton, 15);
		ReportLogger.INFO("Verify View link is displayed for 'New' status record");
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord),"View","SMAB-T65:Verify 'View' link is displayed for records with status- 'New' in history table");
		
		//step5a: verifying backward navigation for New status record 
		objPage.Click(objEFileImport.viewLinkRecord);
		objPage.Click(objEFileImport.sourceDetails);
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.fileTypedropdown), fileType, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.sourceDropdown), source, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");

		//step6: verifying log created With New status
		//ReportLogger.INFO("Verify log genertared for 'New' status record");
		//objEFileImport.globalSearchRecords(fileType+" :"+source+" :"+period);
		//softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"New", "SMAB-T87:Verify that user is able to see logs record for file type with status 'New' on 'E-File Import Logs' screen");

		//step7: importing file for New status record from history list table
		ReportLogger.INFO("Verify File import for 'New' status record");
		/*
		 * objPage.javascriptClick(objEFileImport.efileImportToolLabel);
		 * objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		 * objEFileImport.selectFileAndSource(fileType,source);
		 * objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		 */objPage.Click(objEFileImport.viewLinkRecord);
		ReportLogger.INFO("Verify duplicate messgae does not appear for 'New' status record");
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 10);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.fileAlreadyApprovedMsg), "SMAB-T100:Verify duplicate warning message should not appear for New Status");
		objEFileImport.uploadFileInputBox.sendKeys(sanMateoBuildingPermitFile);
		Thread.sleep(2000);
		objPage.waitForElementToBeClickable(objEFileImport.doneButton);
		Thread.sleep(4000);
		objPage.javascriptClick(objEFileImport.doneButton);
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
	
		//stp7a:verifying New Status entry in system
		HashMap<String,ArrayList<String>> response2=salesforceAPI.select(newStatusQuery);
		int newCountInsystemAfterImport=response2.size();
		softAssert.assertEquals(newCountInsystemAfterImport, 0, "SMAB-T1793:Verify user is able to use existing \"New\" entry Import log instead of creating a new \"New\" Import log record while importing a Building Permit file");
		
		
		//step8:verifying status of file import, view link for imported file and file download link
		softAssert.assertEquals(objPage.getElementText(objEFileImport.statusImportedFile),"Imported","SMAB-T959:Verify that Users are able to import e-files through E-File Import Tool for 'New' status records");
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord),"View","SMAB-T65:Verify 'View' link is displayed for records with status- 'Imported' in history table");
		softAssert.assertEquals(objEFileImport.fileLink.isDisplayed(),true,"SMAB-T915:Verify 'File' link is displayed for records with status- 'Imported' in history table");
		
		//step9: verifying log for Imported status 
		ReportLogger.INFO("Verify log for 'Imported' status record");
		//objEFileImport.globalSearchRecords(fileType+" :"+source+" :"+period);
		objEFileImport.openLogRecordForImportedFile(fileType, source, period, sanMateoBuildingPermitFile);
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
		softAssert.assertEquals(objEFileImportTransactionpage.transactionTrailrecordsCount.size(), 2, "SMAB-T1510:Verify that user is able to see imported records in transaction trail as per insert or update operations");
		objPage.javascriptClick(objEFileImportTransactionpage.transactionTrailRecords.get(0));
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.statusLabel, 10);
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.transactionType), "Transactions", "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.transactionSubType), "E-File Intake", "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.efileImportTransactionLookUp), expectedTransactionID, "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objEFileImportTransactionpage.transactionBuildingPermit.isDisplayed(), true, "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objEFileImportTransactionpage.transactionAPN.isDisplayed(), true, "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.transactionDescription), "Permit uploaded - Electronic File Intake", "SMAB-T1155:Verify that User is able to see Transactions trail with updated fields once a BP file is uploaded via EFile");		
		softAssert.assertTrue(objEFileImportTransactionpage.uploadedFileInAuditTrail.isDisplayed(),  "SMAB-T1550:Verify user is able to view and download file from transaction trail objects of the imported BP file");
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.uploadedFileInAuditTrail), "Click Here", "SMAB-T1550:Verify user is able to view and download file from transaction trail objects of the imported BP file");		
		String parentwindow = driver.getWindowHandle();
		objPage.Click(objEFileImportTransactionpage.uploadedFileInAuditTrail);
		for (String winHandle : driver.getWindowHandles()) {
			   driver.switchTo().window(winHandle);
			 }
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.downloadButtonTransactionTrail, 40);
		softAssert.assertTrue(objEFileImportTransactionpage.downloadButtonTransactionTrail.isDisplayed(), "SMAB-T1550:Verify user is able to view and download file from transaction trail objects of the imported BP file");
		softAssert.assertEquals(driver.getTitle(), "SanMateoBuildingPermitsWithValidAndInvalidData4 | Salesforce", "SMAB-T1550:Verify user is able to view and download file from transaction trail objects of the imported BP file");
		driver.switchTo().window(parentwindow);
		
		//step11: approving the imported file and verifying View link, file download link for Approved status
		ReportLogger.INFO("Verify Approving the imported file record");
		objEFileImport.searchModule(EFILE_INTAKE);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeVisible(objEFileImport.nextButton, 15);
		objPage.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
		softAssert.assertTrue(objEFileImport.errorRowSection.isDisplayed(), "SMAB-T49:Verify Error & Success records are displayed to user");
		softAssert.assertTrue(objEFileImport.successRowSection.isDisplayed(), "SMAB-T49:Verify Error & Success records are displayed to user");
		ReportLogger.INFO("Verify Discarding error records if any");
		objPage.Click(objEFileImport.approveButton);
		objPage.waitForElementToBeClickable(objEFileImport.efileRecordsApproveSuccessMessage, 10);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.revertButton),"SMAB-T58:Verify 'Revert' button is disabled after approve button is clicked");

		//step11a: verifying user does not get warning pop-up while navigating backwards after approving the records and for approved status
		objPage.Click(objEFileImport.sourceDetails);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.continueButton), "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.fileTypedropdown), fileType, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.sourceDropdown), source, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		
		//step12:navigating back to efile import tool screen
		objPage.waitForElementToBeVisible(objEFileImport.nextButton, 15);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord),"View","SMAB-T65,SMAB-T575:Verify 'View' link is displayed for records with status- 'Approved' in history table");
		softAssert.assertEquals(objEFileImport.fileLink.isDisplayed(),true,"SMAB-T915:Verify 'File' link is displayed for records with status- 'Approved' in history table");
		
		//step13: verifying log for Approved status record
		ReportLogger.INFO("Verify log for 'Approved 'record");
		//objEFileImport.globalSearchRecords(fileType+" :"+source+" :"+period);
		objEFileImport.openLogRecordForImportedFile(fileType, source, period, sanMateoBuildingPermitFile);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"Approved","SMAB-T88:Verify that user is able to see Logs record for file once records has been 'Approved' on 'E-File Import Logs' screen");
		
		objEFileImport.logout();
	}	
	
	
	/**
	 * This method is to verify File is not imported for already approved file
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T578,SMAB-T975:Verify user is not able to import a file for BP if the previous Import for a particular File Type, File Source and Period was Approved", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression","EFileImport" })
	public void EFileIntake_VerifyAlreadyApprovedFileNotImported(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="Atherton Building Permits";
		
		objEFileImport.login(loginUser);

		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
	
		//Step2: Opening the E FILE IMPORT Module
		objEFileImport.searchModule(EFILE_INTAKE);
		
		///step3: importing a file
		objEFileImport.uploadFileOnEfileIntakeBP(fileType, source,"Import_TestData_ValidAndInvalidScenarios_AT1.txt",athertonBuildingPermitFile);
		
		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
		
		
		///step5: importing the file again for same file type, source and period to verify view link is not available for previous import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType, source,"Import_TestData_ValidAndInvalidScenarios_AT2.txt",athertonBuildingPermitFile1);
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 200);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.viewLinkRecord), "View", "SMAB-T578:Verify user is able to see 'View' button only for the latest Imported file from all 'Imported' status log");
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.viewLinkForPreviousImport), "SMAB-T578:Verify user is able to see 'View' button only for the latest Imported file from all 'Imported' status log");
		//step6: approving the imported file
		objPage.Click(objEFileImport.viewLinkRecord);
		ReportLogger.INFO("Approving the imported file : Import_TestData_ValidAndInvalidScenarios_AT2.txt");
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
		objPage.waitForElementToBeClickable(objEFileImport.approveButton, 10);
		objPage.Click(objEFileImport.approveButton);
		objPage.waitForElementToBeVisible(objEFileImport.efileRecordsApproveSuccessMessage, 20);
		
		//step7: trying to upload a file for the same file type ,source and period
		objPage.Click(objEFileImport.sourceDetails);
		objPage.waitForElementToBeClickable(10,objEFileImport.statusImportedFile);
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "Import_TestData_ValidAndInvalidScenarios_AT2.txt");
		objPage.Click(objEFileImport.fileNameNext);
		
		//step8: verifying error message while trying to import file for already approved file type,source and period
		softAssert.assertContains(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg), "This file has been already approved", "SMAB-T975:Verify user is not able to import a file for BPP Trends if the previous Import for a particular File Type, File Source and Period was Approved");
		objPage.Click(objEFileImport.closeButton);
		
		objEFileImport.logout();
	}
	
	
	/**
	 * This method is to verify validation after reverting the file
	 * @param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T68,SMAB-T90,SMAB-T915,SMAB-T101,SMAB-T1512:Verify that user without permission is not able to revert the records from file", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"Regression","EFileImport" })
	public void EFileIntake_VerifyValidationAfterRevertedStatus(String loginUser) throws Exception{
		objEFileImport.login(loginUser);
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

					
		//Step2: Opening the E FILE IMPORT Module
		objEFileImport.searchModule(EFILE_INTAKE);
		
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
		//step6a:verifying user does not get warning pop-up while navigating backwards after Reverting the records
		objPage.Click(objEFileImport.sourceDetails);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.continueButton), "SMAB-T1512:Verify user is able to navigate backwards from review and Approve screen for Reverted status");
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.fileTypedropdown), fileType, "SMAB-T1512:Verify user is able to navigate backwards from review and Approve screen for Reverted status");
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.sourceDropdown), source, "SMAB-T1512:Verify user is able to navigate backwards from review and Approve screen for Reverted status");
		
		//step7:navigating back to efile import tool screen
		objPage.waitForElementToBeVisible(objEFileImport.statusImportedFile, 15);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.viewLinkRecord),"SMAB-T68:Verify View link is not displayed for records in history table apart from statuses - 'Imported','New' and 'In Progress' and Approved");
		softAssert.assertTrue(objEFileImport.fileLink.isDisplayed(),"SMAB-T915:Verify 'File' link is displayed for records with status- 'Reverted' in history table");
			
		//step8: verifying log for Reverted status record
		objEFileImport.openLogRecordForImportedFile(fileType, source, period, sanMateoBuildingPermitFile);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logStatus),"Reverted","SMAB-T90:Verify that user is able to see Logs record for file once records has been 'Reverted' on 'E-File Import Logs' screen");
		
		//step9: verifying transaction for Reverted status record
		ReportLogger.INFO("Verify transaction for 'Reverted' status record");
		objPage.Click(objEFileImportTransactionpage.transactionsTab);
		Thread.sleep(4000);
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.transactionsRecords.get(0), 10);
		//String expectedTransactionID=objPage.getElementText(objEFileImportTransactionpage.transactionsRecords.get(0));
		objPage.javascriptClick(objEFileImportTransactionpage.transactionsRecords.get(0));
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.statusLabel, 10);
		
		
		//step10:verifying duplicate error msg does not come for reverted status
		objEFileImport.searchModule(EFILE_INTAKE);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 15);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeVisible(objEFileImport.nextButton, 15);
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		
		//objEFileImport.selectOptionFromDropDown(objEFileImport.periodDropdown, period);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg),"Do you want to import ?","SMAB-T101:Verify duplicate warning message should not appear for Reverted Status from Imported status");
		
		objPage.Click(objEFileImport.closeButton);
		objEFileImport.logout();
}
	
	/**
	 * This method is to verify File Revert is not allowed to unassigned user
	 * @param loginUser
	 * @throws Exception
	 */
	
	
	@Test(description = "SMAB-T28:Verify that user without permission is not able to revert the records from file", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"Regression","EFileImport" })
	public void EFileIntake_VerifyRevertNotAllowedForUnAssigednUser(String loginUser) throws Exception{
		
		//String period = objUtil.getCurrentDate("MMMM YYYY");
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objEFileImport.login(loginUser);

		//Step3: Opening the file import intake module
		objEFileImport.searchModule(modules.EFILE_INTAKE);

		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType,source, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx" ,sanMateoBuildingPermitFile);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
		objEFileImport.logout();

		Thread.sleep(20000);
		
		//step6:now logging in with different user and verifying 'Revert' button invisibility 
		objEFileImport.login(BPP_AUDITOR);
		objEFileImport.searchModule(modules.EFILE_INTAKE);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeVisible(objEFileImport.nextButton, 15);
		objEFileImport.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.revertButton),  "SMAB-T28:Verify that user without permission is not able to revert the records from file");
		objEFileImport.logout();
	}
	
	/**
	 * This method is to verify records count in import logs
	 * @param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T32,SMAB-T33,SMAB-T36,SMAB-T1403,SMAB-T1402,SMAB-T1511,SMAB-T1513,SMAB-T1566,SMAB-T1600:Verify user is able to see number of records count from file import action on 'E-File Import Logs' screen", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"Smoke", "Regression","EFileImport" })	
	public void EFileIntake_VerifyImportLogsRecordCount(String loginUser) throws Exception{
		//String uploadedDate = objUtil.getCurrentDate("MM/dd/YYYY");
		String converteddate=objUtil.convertCurrentDateISTtoPST("Asia/Kolkata", "America/Los_Angeles","MM/dd/yyyy");
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//Step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		
		String queryToDeleteBpRecords="Select id from Building_Permit__c where Name in ('BD-2020-543Test','BD-2020-589Test','BD-2020-998Test','BD-2020-4515Test')";
		salesforceAPI.delete("Building_Permit__c", queryToDeleteBpRecords);
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objEFileImport.login(loginUser);

		//Step3: Opening the file import intake module
		objEFileImport.searchModule(modules.EFILE_INTAKE);

		
		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType,source, "SanMateoBuildingPermitsWithValidAndInvalidData5.xlsx" ,sanMateoBuildingPermitFileWithError);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);

		//step6: verify import list record entry and data
		HashMap<String, ArrayList<String>> importedEntry=objEFileImport.getGridDataInHashMap(1, 1);
		softAssert.assertEquals(importedEntry.get("Uploaded Date").get(0), converteddate, "SMAB-T33 : Uploaded Date - verify import list history data");
		softAssert.assertEquals(importedEntry.get("Period").get(0), "Adhoc", "SMAB-T33 : Period - verify import list history data");
		softAssert.assertEquals(importedEntry.get("File Count").get(0), "6", "SMAB-T33 : File Count - verify import list history data");
		softAssert.assertEquals(importedEntry.get("Import Count").get(0), "2", "SMAB-T33 : Import Count - verify import list history data");
		softAssert.assertEquals(importedEntry.get("Error Count").get(0), "4", "SMAB-T33 : Error Count - verify import list history data");
		softAssert.assertEquals(importedEntry.get("Discard Count").get(0), "0", "SMAB-T33 : Discard Count - verify import list history data");
		softAssert.assertEquals(importedEntry.get("Number of Tries").get(0), "1", "SMAB-T33 : Number of Tries - verify import list history data");
		softAssert.assertEquals(importedEntry.get("Duplicate in File").get(0), "2", "SMAB-T1566: Duplicate In file - Verify user is able to view duplicate records in Error rows section and corresponding count in import history logs under 'Duplicate Count' column after file import");

		objEFileImport.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 10);
		String errorrecords=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();
		String successRecords=objEFileImport.importedRowCount.getText().substring(objEFileImport.importedRowCount.getText().indexOf(":")+1, objEFileImport.importedRowCount.getText().length()).trim();;
		String totalRecords=Integer.toString(Integer.parseInt(errorrecords)+Integer.parseInt(successRecords));

		//step7: navigating to EFile import logs screen and verifying the records count
		objPage.Click(objEFileImport.sourceDetails);
		objPage.Click(objEFileImport.continueButton);
		objPage.waitForElementToBeClickable(10,objEFileImport.statusImportedFile);
		objEFileImport.openLogRecordForImportedFile(fileType, source, period,sanMateoBuildingPermitFileWithError);
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logFileCount),totalRecords, "SMAB-T32:Verify user is able to see number of records count from file import action on 'E-File Import Logs' screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logImportCount),successRecords, "SMAB-T33:Verify user is able to see the number of successful imports completed in 'E-File import Logs' Screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.logErrorCount),errorrecords, "SMAB-T36:Verify user is able to track number of error records for the log in 'E-File Import Logs' screen");
		softAssert.assertEquals(objPage.getElementText(objEFileImportLogPage.duplicatesInFileImportLog),"2", "SMAB-T1600:Verify user is able to view duplicate records count in efile import logs and efile import transaction records after file import");
		objPage.Click(objEFileImportTransactionpage.transactionsTab);
		objPage.waitForElementToBeClickable(objEFileImportLogPage.viewAlllink, 10);
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.transactionsRecords, 30);
		Thread.sleep(25000);
		objPage.javascriptClick(objEFileImportTransactionpage.transactionsRecords.get(0));
		objPage.waitForElementToBeClickable(objEFileImportTransactionpage.statusLabel, 10);
		softAssert.assertEquals(objPage.getElementText(objEFileImportTransactionpage.duplicateCountTransaction),"2", "SMAB-T1600:Verify user is able to view duplicate records count in efile import logs and efile import transaction records after file import");
		
		//step8:verifying the discarded count scenario
		objEFileImport.searchModule(modules.EFILE_INTAKE);
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		objEFileImport.Click(objEFileImport.viewLinkRecord);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 10);
		softAssert.assertEquals(Integer.parseInt(errorrecords), "4", "SMAB-T36: Verify number of error records are same before discard(ERROR ROWS:Count)");
		softAssert.assertEquals(objEFileImport.errorRecordsRows.size(), "4","SMAB-T36: Verify number of error records are same before discard(actual records count)");
		
		ReportLogger.INFO("Now discarding an Error record");
		objPage.Click(objEFileImport.errorRecordsRows.get(0));
		objPage.Click(objEFileImport.discardButton);
		objPage.waitForElementToBeClickable(objEFileImport.discardContinue, 5);
		objPage.Click(objEFileImport.discardContinue);
		objBPPTrendPage.waitForPageSpinnerToDisappear(15);
		Thread.sleep(2000);
		String errorrecordsAfterDisacrd=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();

		softAssert.assertEquals(Integer.parseInt(errorrecordsAfterDisacrd),"3", "SMAB-T36: Verify number of error records are same after discarding an error record(ERROR ROWS:Count)");
		softAssert.assertEquals(objEFileImport.errorRecordsRows.size(),"3" ,"SMAB-T36: Verify number of error records are same after discarding an error record(actual records count)");

		//step8a:verifying backward navigation for Imported status with>0 Error records and =0 error records 
		ReportLogger.INFO("Verifying user gets warning if Error records>0 and user tries navigating backwards by clicking on 'Source Details'");
		objPage.Click(objEFileImport.sourceDetails);
		softAssert.assertTrue(objEFileImport.continueButton.isDisplayed(), "SMAB-T1513:Verify user gets a warning message when error record>0(on Review and Approve data screen) and user tries to navigate back to EFile import list page by clicking on the backward breadcrumb (Source Details) icon");
		softAssert.assertEquals(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg), "You will loose all the changes on the error rows, Are you sure?", "SMAB-T1513:Verify user gets a warning message when error record>0(on Review and Approve data screen) and user tries to navigate back to EFile import list page by clicking on the backward breadcrumb (Source Details) icon");
		ReportLogger.INFO("Verifying that on cancelling the warning User stays on same page");
		objPage.Click(objEFileImport.cancelButton);
		objPage.waitForElementToBeVisible(objEFileImport.errorRowSection, 2);
		objPage.Click(objEFileImport.sourceDetails);
		objPage.waitForElementToBeVisible(objEFileImport.continueButton, 3);
		ReportLogger.INFO("Verifying that on Confirming/Continuing the warning user navigates back to previous page");
		objPage.Click(objEFileImport.continueButton);
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		objEFileImport.selectFileAndSource(fileType,source);
		//softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.fileTypedropdown), fileType, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		//softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.sourceDropdown), source, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
	
		objPage.waitForElementToBeVisible(objEFileImport.nextButton, 15);
		softAssert.assertEquals(objEFileImport.getElementText(objEFileImport.disacrdCount),"1","SMAB-T68:Verify View link is not displayed for records in history table apart from statuses - 'Imported','New' and 'In Progress' and Approved");
		objPage.Click(objEFileImport.viewLink);
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 30);
		String errorrecordsAfterDisacrd1=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();
		softAssert.assertEquals(Integer.parseInt(errorrecordsAfterDisacrd1),"3", "SMAB-T36 : Verify number of error records are same after discarding an error record(ERROR ROWS:Count)");
		softAssert.assertEquals(objEFileImport.errorRecordsRows.size(),"3" ,"SMAB-T36 : Verify number of error records are same after discarding an error record(actual records count)");
		
		ReportLogger.INFO("Now discarding all error records");
		objPage.Click(objEFileImport.discardAllCheckbox);
		objPage.Click(objEFileImport.discardButton);
		objPage.waitForElementToBeClickable(objEFileImport.discardContinue, 5);
		objPage.Click(objEFileImport.discardContinue);
		objBPPTrendPage.waitForPageSpinnerToDisappear(15);
		Thread.sleep(2000);
		String errorCountAfterDiscard=objEFileImport.errorRowCount.getText().substring(objEFileImport.errorRowCount.getText().indexOf(":")+1, objEFileImport.errorRowCount.getText().length()).trim();
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.discardAllCheckbox), "SMAB-T1403,SMAB-T1402:Verify user is not able to view discarded record(s) for a file that is imported for Building Permit file type");
		softAssert.assertEquals(errorCountAfterDiscard, "0", "SMAB-T1403,SMAB-T1402:Verify user is not able to view discarded record(s) for a file that is imported for Building Permit file type");
		
		//step8b:verifying user does not get warning pop-up if imported file has no error records
		objPage.Click(objEFileImport.sourceDetails);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.continueButton), "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.fileTypedropdown), fileType, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		softAssert.assertEquals(objPage.getSelectedDropDownValue(objEFileImport.sourceDropdown), source, "SMAB-T1511:Verify user is able to navigate backwards from review and Approve screen for New,Imported and Approved status");
		
		
		//step9: verifying the discard count
		objPage.waitForElementToBeVisible(objEFileImport.nextButton, 15);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.disacrdCount),"4","SMAB-T68:Verify View link is not displayed for records in history table apart from statuses - 'Imported','New' and 'In Progress' and Approved");
		objEFileImport.logout();
	}
	
	
	/**
	 * This method is to verify File type has correct Source values
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T102:Verify user is able to see number of records count from file import action on 'E-File Import Logs' screen", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"Regression","EFileImport" })
	public void EFileIntake_VerifyFileTypeAndCorrespondingSources(String loginUser) throws Exception{
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objEFileImport.login(loginUser);

		//Step2: Opening the file import intake module
		objEFileImport.searchModule(modules.EFILE_INTAKE);
		
		//step3:verifying file type and sources
		ReportLogger.INFO("Verifying Sources for BPP Trend Factors");
		objEFileImport.selectOptionFromDropDown(objEFileImport.fileTypedropdown, "BPP Trend Factors");
		objPage.waitForElementToBeClickable(objEFileImport.sourceDropdown, 5);
		objPage.Click(objEFileImport.sourceDropdown);
		String expectedSourcesBPP = "CAA - Valuation Factors\nBOE - Index and Percent Good Factors\nBOE - Valuation Factors";
		String actualSourcesBPP = objPage.getElementText(objEFileImport.sourceDropdownOptions);
		softAssert.assertTrue(objPage.compareDropDownvalues(actualSourcesBPP, expectedSourcesBPP), "SMAB-T102: Verify user is able to view and select the file types and source for that file type on E-File Import Tool screen");
		
		driver.navigate().refresh();
		ReportLogger.INFO("Verifying Sources for Building Permit");
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 15);
		objEFileImport.selectOptionFromDropDown(objEFileImport.fileTypedropdown, "Building Permit");
		objPage.waitForElementToBeClickable(objEFileImport.sourceDropdown, 5);
		objPage.Click(objEFileImport.sourceDropdown);
		String expectedSourcesBP = "Millbrae Building permits\nUnincorporated Building permits\nBelmont Building permits\nSan Bruno Building permits\nBurlingame Building permits\nHillsborough Building permits\nWoodside Building permits\nSan Mateo Building permits\nSouth San Francisco Building permits\nRedwood City Building permits\nAtherton Building Permits";
		String actualSourcesBP = objPage.getElementText(objEFileImport.sourceDropdownOptions);
		softAssert.assertTrue(objPage.compareDropDownvalues(actualSourcesBP, expectedSourcesBP), "SMAB-T102: Verify user is able to view and select the file types and source for that file type on E-File Import Tool screen");
		
		driver.navigate().refresh();
		objPage.waitForElementToBeClickable(objEFileImport.fileTypedropdown, 10);
		objEFileImport.logout();
		
	}
	
	/**
	 * This method is to verify File type has correct Source values
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1144:Verify that user is not able to upload a file if a file is already 'In Progress' status for the selected 'File type' ,'Source' and 'Period'", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"Regression","EFileImport" })
	public void EFileIntake_VerifyFileNotimportedIfAlreadyInProgress(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//Step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
							
	
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objEFileImport.login(loginUser);
		
		
		objEFileImport.searchModule(modules.EFILE_INTAKE);
		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEFileImport.uploadFileOnEfileIntakeBP(fileType,source, "SanMateoBuildingPermitsWithValidAndInvalidData5.xlsx" ,sanMateoBuildingPermitFileWithError);

		//Step5: Waiting for Status of the imported file to be converted to "InProgress"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to InProgress");
		
		//Step6:trying to upload another file and verify msg
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData5.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.confirmButton), "SMAB-T1144:Verify that user is not able to upload a file if a file is already 'In Progress' status for the selected 'File type' ,'Source' and 'Period'");
		softAssert.assertContains(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg),"This is already in In Progress" ,"SMAB-T1144:Verify that user is not able to upload a file if a file is already 'In Progress' status for the selected 'File type' ,'Source' and 'Period'");
		
		objPage.Click(objEFileImport.closeButton);
		objEFileImport.logout();
	}
	
	
	/**
	 * This method is to verify File type has correct Source values
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1569,SMAB-T1570,SMAB-T1572,SMAB-T1571:Verify BP File import validation on Input box ", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression","EFileImport" })	
	public void EFileIntake_VerifyBPInputBoxValidationsForBPImport(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="San Mateo Building permits";
		
		//Step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
							
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objEFileImport.login(loginUser);
		objEFileImport.searchModule(modules.EFILE_INTAKE);
		//step3:verifying file type and sources
		ReportLogger.INFO("Verifying Period Drop Down is not displayed for BP EFile import");
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		ReportLogger.INFO("Verifying Input box for import does not accept file names with error keywords");
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithError.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.errorInFileNameMsg), "'Error' word is not allowed in file name.", "SMAB-T1570:Verify while uploading E-File, Text Box displayed to enter File Name for Building Permits cannot have 'Error' Keyword");
		softAssert.assertTrue(objEFileImport.isNotDisplayed(objEFileImport.confirmButton), "SMAB-T1570:Verify while uploading E-File, Text Box displayed to enter File Name for Building Permits cannot have 'Error' Keyword");
		
		ReportLogger.INFO("Verifying FileName uploaded is the same as File Name given in the Text box");
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsDuplicateRecordsData.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		objPage.Click(objEFileImport.confirmButton);
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 10);
		objEFileImport.uploadFileInputBox.sendKeys(sanMateoBuildingPermitFileWithError);
		objPage.waitForElementToBeClickable(objEFileImport.doneButton);
		Thread.sleep(4000);
		objPage.Click(objEFileImport.doneButton);
		objPage.waitForElementToBeClickable(objEFileImport.duplicateFileMsg,5);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.duplicateFileMsg), "Uploaded file name doesn't match with given file name", "SMAB-T1572:Verify FileName uploaded is the same as File Name given in the Text box");
		objPage.Click(objEFileImport.closeButton);
		
		ReportLogger.INFO("Verifying user is not able to upload same file(same 'FileName') again");
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		objPage.Click(objEFileImport.confirmButton);
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 10);
		objEFileImport.uploadFileInputBox.sendKeys(sanMateoBuildingPermitFile);
		objPage.waitForElementToBeClickable(objEFileImport.doneButton);
		Thread.sleep(5000);
		objPage.Click(objEFileImport.doneButton);
		objPage.waitForElementToBeClickable(objEFileImport.statusImportedFile,30);
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 200);
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		objPage.enter(objEFileImport.fileNameInputBox, "SanMateoBuildingPermitsWithValidAndInvalidData4.xlsx");
		objPage.Click(objEFileImport.fileNameNext);
		softAssert.assertContains(objPage.getElementText(objEFileImport.fileAlreadyApprovedMsg), "This file has been previously imported by", "SMAB-T1571:Verify user is not able to upload same file(same filename) again if already imported");
		objPage.Click(objEFileImport.closeButton);
		
		objEFileImport.logout();
	}
	
	
	
	
	/**
	 * This method is to verify only One 'New' entry for BPP Trend Factor File type and Sources
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1791:Verify user is able to see only One 'new' entry and same is used while importing a BPP Trends File Type ", dataProvider = "loginRPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {
		"Regression","EFileImport" })	
	public void EFileIntake_VerifyOnlyOneNewStatusRecordForBPPTrends(String loginUser) throws Exception{
		String period = "2021";
		String fileType="BPP Trend Factors";
		String source="CAA - Valuation Factors";
		
		//Step1:Reverting the New Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__c='"+period+"' and Status__c in ('New','Approved')";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		
		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objEFileImport.login(loginUser);
		objEFileImport.searchModule(modules.EFILE_INTAKE);
		
		//step3:Creating a 'New' status entry in system
		ReportLogger.INFO("Creating a 'New' status entry in system");
		objEFileImport.selectFileAndSource(fileType,source);
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		objPage.waitForElementToBeClickable(objEFileImport.periodDropdown, 10);
		objEFileImport.selectOptionFromDropDown(objEFileImport.periodDropdown, period);
    	objPage.waitForElementToBeClickable(objEFileImport.confirmButton, 10);
		objPage.Click(objEFileImport.confirmButton);
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 10);
		objPage.Click(objEFileImport.closeButton);
		
		//step4:try Creating a 'New' status entry again in system
		objPage.scrollToTop();
		objPage.Click(objEFileImport.nextButton);
		objPage.waitForElementToBeClickable(objEFileImport.periodDropdown, 10);
		objEFileImport.selectOptionFromDropDown(objEFileImport.periodDropdown, period);
    	objPage.waitForElementToBeClickable(objEFileImport.confirmButton, 10);
		objPage.Click(objEFileImport.confirmButton);
		objPage.waitForElementToBeClickable(objEFileImport.uploadFilebutton, 10);
		objPage.Click(objEFileImport.closeButton);
		
		//step5:Verifying the count of 'New' status entry in system
		ReportLogger.INFO("Verifying only 1 'New' entry in system and is displayed on top(recently created one)");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "New", 120);
		softAssert.assertEquals(objPage.getElementText(objEFileImport.statusImportedFile), "New", "SMAB-T1791: New status entry cretaed");
		
		HashMap<String,ArrayList<String>> response1=salesforceAPI.select(query);
		int newCountInsystem=response1.get("Id").size();
		
		softAssert.assertEquals(newCountInsystem, 1, "SMAB-T1791:Verify user is able to use existing \"New\" entry Import log instead of creating a new \"New\" Import log record while importing a BPP trend Factor file");
		
		//step6:importing a file and verifying existing 'new' entry is used for import	
		objPage.scrollToTop();
		objEFileImport.uploadFileOnEfileIntake(fileType, source, period, eFileTestDataPath);
		HashMap<String,ArrayList<String>> response2=salesforceAPI.select(query);
		int newCountInsystemAfterImport=response2.size();
		softAssert.assertEquals(newCountInsystemAfterImport, 0, "SMAB-T1791:Verify user is able to use existing \"New\" entry Import log instead of creating a new \"New\" Import log record while importing a BPP trend factor file");
		
		objEFileImport.logout();
	}
	
}
		
		
	
	