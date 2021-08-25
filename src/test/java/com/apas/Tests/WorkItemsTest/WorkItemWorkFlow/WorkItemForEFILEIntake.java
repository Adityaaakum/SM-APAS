package com.apas.Tests.WorkItemsTest.WorkItemWorkFlow;

import java.io.File;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportLogsPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItemForEFILEIntake extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	BppTrendPage objBPPTrendPage;
	BuildingPermitPage objBuildingPermitPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil;
	SoftAssertion softAssert;
	String eFileTestDataPath;
	String athertonBuildingPermitFile;
	String athertonBuildingPermitFile1;
	String athertonBuildingPermitFile2;
	String athertonBuildingPermitFile3;
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
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objBPPTrendPage= new BppTrendPage(driver);
		softAssert  = new SoftAssertion();
		objWorkItemHomePage =new WorkItemHomePage(driver);
		objUtil = new Util();
		salesforceAPI=new SalesforceAPI();
		
		//eFileTestDataPath= System.getProperty("user.dir") + testdata.EFILEIMPORT_BPPTRENDSDATA + "BOE-IndexAndPercentGoodFactor.xlsx";
		eFileTestDataPath= System.getProperty("user.dir") + testdata.EFILEIMPORT_BPPTRENDSDATA + "CAAValuationFactors.xlsx";
		athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "Import_TestData_ValidAndInvalidScenarios_AT1.txt";
		athertonBuildingPermitFile1 = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "Import_TestData_ValidAndInvalidScenarios_AT2.txt";
		athertonBuildingPermitFile2 = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "AthertonSingleRecordWithCompletiondate.txt";
		athertonBuildingPermitFile3 = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "AthertonSingleRecordWithOutCompletiondate.txt";
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
	
	
	@SuppressWarnings("unlikely-arg-type")
	@Test(description = "SMAB-T3087,SMAB-T3088: :Create building permit with the E-File import process with completion date", dataProvider = "RPAppraiser",dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression","EFileImport" })
	public void EFileIntake_ApproveImportedFileWithCompletionDate(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="Atherton Building Permits";
		
		//Step 1: Login as Appraiser user
		objEFileImport.login(loginUser);

        //Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
	
		//Step 2: Opening the E FILE IMPORT Module
		objEFileImport.searchModule(EFILE_INTAKE);
		 
		//Step 3: importing a file
		objEFileImport.uploadFileOnEfileIntakeBP(fileType, source,"AthertonSingleRecordWithCompletiondate.txt",athertonBuildingPermitFile2);
		
		//Step 4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
		
		//Step 5: approving the imported file
		objPage.Click(objEFileImport.viewLinkRecord);
		ReportLogger.INFO("Approving the imported file : AthertonSingleRecordWithCompletiondate.txt");
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
		objPage.waitForElementToBeClickable(objEFileImport.approveButton, 10);
		objPage.Click(objEFileImport.approveButton);
		objPage.waitForElementToBeVisible(objEFileImport.efileRecordsApproveSuccessMessage, 20);
		ReportLogger.INFO("Approval completed");
    
		//Step 6: Opening the Home module & Clicking on In Pool Tab
		objBuildingPermitPage.searchModule(modules.HOME);
        System.out.println("We are on home page now...................");
        objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
        ReportLogger.INFO("clicked inpool tab");

      //"Import Review" Work Item generation validation after file is imported
       String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Final Review - AthertonSingleRecordWithCompletiondate" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
       String importReviewWorkItem = salesforceAPI.select(queryWorkItem).get("Name").get(0);
       ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
       System.out.println("We are on home page now..................." + importReviewWorkItem);
    
       //Step 7: Accepting the work item & Going to Tab In Progress

    objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
    System.out.println("clicked on age");
    objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
    objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
    softAssert.equals(importReviewWorkItem);

	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test(description = "SMAB-T3085,SMAB-T3086:Create building permit with the E-File import process with out completion date", dataProvider = "RPAppraiser",dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression","EFileImport" })
	public void EFileIntake_ApproveImportedFileWithoutCompletionDate(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="Atherton Building Permits";
		
		    // Step 1:Login as Appraiser user 
		    objEFileImport.login(loginUser);

         	//Reverting the Approved Import logs if any in the system
			String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
			salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		
			//Step 2: Opening the E FILE IMPORT Module
			objEFileImport.searchModule(EFILE_INTAKE);
			
			//Step 3: Importing a file
			objEFileImport.uploadFileOnEfileIntakeBP(fileType, source,"AthertonSingleRecordWithOutCompletiondate.txt",athertonBuildingPermitFile3);
			
			//Step 4: Waiting for Status of the imported file to be converted to "Imported"
			ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
			objPage.waitForElementTextToBe(objEFileImport.statusImportedFile, "Imported", 400);
			
			//Step 5: Approving the imported file
			objPage.Click(objEFileImport.viewLinkRecord);
			ReportLogger.INFO("Approving the imported file : AthertonSingleRecordWithCompletiondate.txt");
			objPage.waitForElementToBeClickable(objEFileImport.errorRowSection, 20);
			objPage.waitForElementToBeClickable(objEFileImport.approveButton, 10);
			objPage.Click(objEFileImport.approveButton);
			objPage.waitForElementToBeVisible(objEFileImport.efileRecordsApproveSuccessMessage, 20);
			System.out.println("Approval completed");
			
			//Step 6: Opening the Home module & Clicking on In Pool Tab
			objBuildingPermitPage.searchModule(modules.HOME);
	        System.out.println("We are on home page now...................");
	        Thread.sleep(10000);
	       objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
	        Thread.sleep(10000);

	       //Import Review Work Item generation validation after file is imported
	       String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Final Review - AthertonSingleRecordWithOutCompletiondate" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
	       String importReviewWorkItem = salesforceAPI.select(queryWorkItem).get("Name").get(0);
	       ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
	       System.out.println("We are on home page now..................." + importReviewWorkItem);
	    
           //Step 7: Accepting the work item & Going to Tab In Progress
	       objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
	       objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
	       objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
	       System.out.println("Found Work Item");
	    
	    //Step 8:Navigate to Building permits and select the building permit and editing the completion date and save
	    objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);
	    objBuildingPermitPage.displayRecords("All Manual Building Permits");
        objBuildingPermitPage.globalSearchRecords("<PERMITNO>");
        objBuildingPermitPage.scrollToElement(objBuildingPermitPage.editPermitButton);
        objWorkItemHomePage.Click(objBuildingPermitPage.editPermitButton);
        objBuildingPermitPage.clickCompetionboxAndEnterDate();
        
        //Step 9:Navigate back to Home-In pool and search for the work item created with Construction Completed.
        objBuildingPermitPage.searchModule(modules.HOME);
        objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
        String queryWorkItem2 = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - E-file Import" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
	    String importReviewWorkItem2 = salesforceAPI.select(queryWorkItem2).get("Name").get(0);
	    ReportLogger.INFO("Created Work Item : " + importReviewWorkItem2);
	    System.out.println("We are on home page now..................." + importReviewWorkItem2);
	    softAssert.equals(importReviewWorkItem2);
	}
        
 }
