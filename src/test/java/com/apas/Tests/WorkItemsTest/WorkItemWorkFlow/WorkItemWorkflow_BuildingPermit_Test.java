package com.apas.Tests.WorkItemsTest.WorkItemWorkFlow;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.BPFileSource;
import com.apas.config.fileTypes;
import com.apas.config.modules;
import com.apas.config.testdata;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class WorkItemWorkflow_BuildingPermit_Test extends TestBase {

    RemoteWebDriver driver;
    Page objPage;
    BuildingPermitPage objBuildingPermitPage;
    WorkItemHomePage objWorkItemHomePage;
    SalesforceAPI salesforceAPI;
    EFileImportPage objEfileImportPage;
    ReportsPage objReportsPage;

    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI objSalesforceAPI = new SalesforceAPI();
    
    String athertonBuildingPermitFile2;
	String athertonBuildingPermitFile3;
    
   

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {
        
    	driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        
        objBuildingPermitPage = new BuildingPermitPage(driver);
        objPage = new Page(driver);
        objEfileImportPage = new EFileImportPage(driver);
        salesforceAPI =new SalesforceAPI();
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objReportsPage = new ReportsPage(driver);
        
        athertonBuildingPermitFile2 = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "AthertonSingleRecordWithCompletiondate.txt";
    	athertonBuildingPermitFile3 = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "AthertonSingleRecordWithOutCompletiondate.txt";
    	
    }
    
    /**
     * This test case is to validate work item creation functionality and the work item flow after file is approved
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and RP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1890, SMAB-T1892, SMAB-T1900, SMAB-T1901, SMAB-T1902,SMAB-T2081,SMAB-T2121,SMAB-T2122,SMAB-T1903: Validation for work item generation after building permit file import and approve", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"Smoke", "Regression", "WorkItemWorkflow_BuildingPermit", "BuildingPermit"}, alwaysRun = true)
    public void WorkItemWorkflow_BuildingPermit_ReviewAndFinalReviewWorkItem_ImportAndApprove(String loginUser) throws Exception {

        String downloadLocation = testdata.DOWNLOAD_FOLDER;
        ReportLogger.INFO("Download location : " + downloadLocation);

        //Deleting all the previously downloaded files
        objBuildingPermitPage.deleteFilesFromFolder(downloadLocation);

        //Creating a temporary copy of the file to be processed to create unique name
        String timeStamp = DateUtil.getCurrentDate("ddhhmmss");
        String sourceFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "MultipleValidRecord.xlsx";
        File tempFile = objBuildingPermitPage.createTempFile(sourceFile);
        String destFile = tempFile.getAbsolutePath();
        String fileName = tempFile.getName();
        String fileNameWithoutExtension = fileName.split("\\.")[0];

        //Step1: Reverting the Approved Import logs if any in the system
        objEfileImportPage.revertImportedAndApprovedFiles(BPFileSource.SAN_MATEO);

        //Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
        objBuildingPermitPage.login(loginUser);

        //Step3: Importing the Building Permit file having all correct records
        objEfileImportPage.importFileOnEfileIntake(fileTypes.BUILDING_PERMITS, BPFileSource.SAN_MATEO, fileName, destFile);
       
        //Stpe4: Open the Work Item Home Page
        driver.navigate().refresh();
        objBuildingPermitPage.searchModule(modules.HOME);

        //Step5: "Import Review" Work Item generation validation after file is imported
        String queryWorkItem = "SELECT Name FROM Work_Item__c where Request_Type__c = 'Building Permit - Review - " + fileNameWithoutExtension + "'";
        String importReviewWorkItem = objSalesforceAPI.select(queryWorkItem).get("Name").get(0);
        ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);

        //Step6: Accepting the work item and approving the file
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objBuildingPermitPage.scrollToBottom();

		//SMAB-T2121: opening the action link to validate that link redirects to Review Error and success Records page
        objWorkItemHomePage.openActionLink(importReviewWorkItem);

        objWorkItemHomePage.waitForElementToBeClickable(objEfileImportPage.approveButton,90);
		softAssert.assertTrue(objBuildingPermitPage.verifyElementVisible(objEfileImportPage.approveButton),"SMAB-T2121: Validation that approve button is visible");
		softAssert.assertTrue(objBuildingPermitPage.verifyElementVisible(objEfileImportPage.errorRowSection),"SMAB-T2121: Validation that error Row Section is visible");
		softAssert.assertTrue(objBuildingPermitPage.verifyElementVisible(objEfileImportPage.buildingPermitLabel),"SMAB-T2121: Validation that Building Permits Label is visible") ;
		softAssert.assertTrue(objBuildingPermitPage.verifyElementVisible(objEfileImportPage.importedRowSection),"SMAB-T2121: Validation that imported Rows Section is visible");

        objBuildingPermitPage.searchModule(modules.HOME);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		objWorkItemHomePage.openWorkItem(importReviewWorkItem);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);

		//Validating that 'Date' field gets automatically populated in the work item record WHERE date should be date of import
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2081: Validation that 'Date' fields is equal to import date");

		String parentWindow = driver.getWindowHandle();
        objBuildingPermitPage.searchModule(modules.HOME);
        objWorkItemHomePage.openRelatedActionRecord(importReviewWorkItem);
        objBuildingPermitPage.switchToNewWindow(parentWindow);
        objEfileImportPage.approveImportedFile();
		driver.close();
        driver.switchTo().window(parentWindow);

        //Step7: Open Home Page
        objBuildingPermitPage.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step8: Validation for Import Review work item moved to completed status
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        Thread.sleep(5000);
        softAssert.assertTrue(completedWorkItems.get("Work item #").contains(importReviewWorkItem), "SMAB-T1890: Validation that import review work item moved to Completed status after import file is approved");

        //Step9: Validation for generation of "Final Review" Work Item
        String queryFinalReviewWorkItem = "SELECT Name FROM Work_Item__c where Request_Type__c = 'Building Permit - Final Review - " + fileNameWithoutExtension + "'";
        String finalReviewWorkItem = objSalesforceAPI.select(queryFinalReviewWorkItem).get("Name").get(0);
        ReportLogger.INFO("Created Final Review Work Item : " + finalReviewWorkItem);

        //Step6: Accepting the work item
        driver.navigate().refresh();
        
      
        objBuildingPermitPage.searchModule(modules.HOME);
        Thread.sleep(2000);
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
        objWorkItemHomePage.acceptWorkItem(finalReviewWorkItem);
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
        objBuildingPermitPage.scrollToBottom();
        objWorkItemHomePage.openWorkItem(finalReviewWorkItem);
        objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
        softAssert.assertEquals(objWorkItemHomePage.getIndividualFieldErrorMessage("Request Type"), "Building Permit - Final Review - " + fileNameWithoutExtension, "SMAB-T1900: File Name and Final Review Work Item Name validation in the final review work item");
        softAssert.assertEquals(objWorkItemHomePage.getIndividualFieldErrorMessage("Work Pool"), "RP Admin", "SMAB-T1900: Final review work item pool name validation");
        //Validating that 'Date' field gets automatically populated in the work item record WHERE date should be date of import 
      	softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2081: Validation that 'Date' fields is equal to import date");

        //Step11: Validation that E-File logs are linked to the work item and the status of the E-logs is Approved
        objWorkItemHomePage.openTab(objWorkItemHomePage.tabLinkedItems);
        softAssert.assertTrue(objBuildingPermitPage.verifyElementExists(objWorkItemHomePage.linkedItemEFileIntakeLogs), "SMAB-T1900: Validation that efile logs are linked with work item");
        HashMap<String, ArrayList<String>> efileImportLogsData = objBuildingPermitPage.getGridDataInHashMap();
        softAssert.assertEquals(efileImportLogsData.get("Status").get(0), "Approved", "SMAB-T1900: Validation that status of the efile logs linked with work item is reverted");

        //Step12: Validation the building permit records are linked with the work item
        objBuildingPermitPage.scrollToBottom();
        softAssert.assertTrue(objBuildingPermitPage.verifyElementExists(objWorkItemHomePage.relatedBuildingPermits), "SMAB-T1900: Validation that Building Permit Records are linked with work item");
        HashMap<String, ArrayList<String>> buildingPermitRecordsData = objBuildingPermitPage.getGridDataInHashMap(2);
        softAssert.assertEquals(buildingPermitRecordsData.get("Building Permit Number").size(), "3", "SMAB-T1900: Building Permit Records Count validation");

        //Step13: Update the building permit record
        objBuildingPermitPage.openBuildingPermit(buildingPermitRecordsData.get("Building Permit Number").get(0));
        objBuildingPermitPage.scrollToBottom();
        objBuildingPermitPage.editAndInputFieldData("Estimated Project Value",timeStamp);
        softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Estimated Project Value").replace(",",""), "$" + timeStamp, "SMAB-T1901: Validation that updated valueis reflected in the building permit");

        //Step14: Open Home Page
        objBuildingPermitPage.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step15: Open the work item
        objBuildingPermitPage.scrollToBottom();
        parentWindow = driver.getWindowHandle();
		//SMAB-T2122:opening the action link to validate that link redirects to Final Review Building Permits  page
        objWorkItemHomePage.openActionLink(finalReviewWorkItem);
		objBuildingPermitPage.switchToNewWindow(parentWindow);
		softAssert.assertTrue(objBuildingPermitPage.verifyElementVisible(objReportsPage.buildingPermitHeaderText), "SMAB-T2122: Validation that Final Review Building Permits label is visible");
		driver.close();
		driver.switchTo().window(parentWindow);
        objBuildingPermitPage.searchModule(modules.HOME);
        objWorkItemHomePage.openRelatedActionRecord(finalReviewWorkItem);

        //Step16: Report data validation for linked building permit records
        Thread.sleep(5000);
        parentWindow = driver.getWindowHandle();
        objBuildingPermitPage.switchToNewWindow(parentWindow);
        //Switching the frame as the generated report is in different frame
        driver.switchTo().frame(0);
        objBuildingPermitPage.Click(objReportsPage.arrowButton);
        objBuildingPermitPage.Click(objReportsPage.linkExport);
        //Switching back to parent frame to export the report
        driver.switchTo().parentFrame();
        objBuildingPermitPage.Click(objReportsPage.formattedExportLabel);
        objBuildingPermitPage.Click(objReportsPage.exportButton);
        //Added this wait to allow the file to download
        Thread.sleep(10000);
        File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];

        //Step17: Columns validation in exported report
        HashMap<String, ArrayList<String>> hashMapExcelData = ExcelUtils.getExcelSheetData(downloadedFile.getAbsolutePath(),0,10,1);
        String expectedColumnsInExportedExcel = "[Created Date, Permit City Code, Building Permit Number, APN: APN, Issue Date(YYYYMMDD), Completion Date(YYYYMMDD), Reissue, City APN, City Strat Code, Building Permit Fee, Work Description, Square Footage, Estimated Project Value, Application Name, Owner Name, Owner Address Line 1, Owner Address Line 2, Owner Address Line 3, Owner State, Owner Zip Code, Owner Phone Number, Contractor Name, Contractor Phone]";
        softAssert.assertEquals(hashMapExcelData.keySet().toString(),expectedColumnsInExportedExcel,"SMAB-T1900: Columns Validation in downloaded building permit report");

        driver.switchTo().window(parentWindow);

        //Step18: Complete the final review work item
        String successMessageText = objWorkItemHomePage.completeWorkItem();
        softAssert.assertEquals(successMessageText, "success\nStatus changed successfully.\nClose", "SMAB-T1903,SMAB-T1902,SMAB-T1901: Validation that status of the efile logs linked with work item is reverted");

        //Step19: Open Home Page
        driver.navigate().refresh();
        objBuildingPermitPage.searchModule(modules.HOME);

        //Step20: Close "Final Review" Work Item
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work item #").contains(finalReviewWorkItem + "\nLaunch Data Entry Screen"), "SMAB-T1903: Validation that Final Review work item moved to Completed status after Final Review work item is manually completed");

        //Logout at the end of the test
        objBuildingPermitPage.logout();
    }


    /**
     * This test case is to validate work item creation functionality and the work item flow after file is reverted
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and RP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1890, SMAB-T1899: Validation for work item generation after building permit file import and revert", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"Smoke", "Regression", "WorkItemWorkflow_BuildingPermit", "BuildingPermit"}, alwaysRun = true)
    public void WorkItemWorkflow_BuildingPermit_ReviewWorkItem_ImportAndRevert(String loginUser) throws Exception {

        //Creating a temporary copy of the file to be processed to create unique name
        String sourceFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "SingleValidRecord_AT.txt";
        File tempFile = objBuildingPermitPage.createTempFile(sourceFile);
        String destFile = tempFile.getAbsolutePath();
        String fileName = tempFile.getName();
        String fileNameWithoutExtension = fileName.split("\\.")[0];

        //Step1: Reverting the Approved Import logs if any in the system
        objEfileImportPage.revertImportedAndApprovedFiles(BPFileSource.ATHERTON);

        //Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
        objBuildingPermitPage.login(loginUser);

        //Step3: Importing the Building Permit file having all correct records
        objEfileImportPage.importFileOnEfileIntake(fileTypes.BUILDING_PERMITS, BPFileSource.ATHERTON, fileName, destFile);

        //Stpe4: Open the Work Item Home Page
        driver.navigate().refresh();
        objBuildingPermitPage.searchModule(modules.HOME);

        //Step5: "Import Review" Work Item generation validation after file is imported
        String queryWorkItem = "SELECT Name FROM Work_Item__c where Request_Type__c like '%" + fileNameWithoutExtension + "%'";
        String importReviewWorkItem = objSalesforceAPI.select(queryWorkItem).get("Name").get(0);
        ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);

        //Step6: Accepting the work item
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
        objBuildingPermitPage.scrollToBottom();
        objWorkItemHomePage.openWorkItem(importReviewWorkItem);
        objWorkItemHomePage.openTab("Details");
        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Request Type"), "Building Permit - Review - " + fileNameWithoutExtension, "SMAB-T1890: File Name and Review Work Item Name validation in the imported review work item");
        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool"), "RP Admin", "SMAB-T1890: Imported review work item pool name validation");

        driver.navigate().refresh();
        objBuildingPermitPage.searchModule(modules.HOME);
        objWorkItemHomePage.openRelatedActionRecord(importReviewWorkItem);
        String parentWindow = driver.getWindowHandle();
        objBuildingPermitPage.switchToNewWindow(parentWindow);
        objEfileImportPage.revertImportedFile();
        driver.switchTo().window(parentWindow);

        //Step7: Open Home Page
        objBuildingPermitPage.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step8: Validation for Import Review work item moved to completed status
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work item #").contains(importReviewWorkItem + "\nLaunch Data Entry Screen"), "SMAB-T1890: Validation that import review work item moved to Completed status after import file is reverted");

        //Step9: Opening the completed work item
        objBuildingPermitPage.scrollToBottom();
        objWorkItemHomePage.openWorkItem(importReviewWorkItem);

        //Step10: Validation that E-File logs are linked to the work item and the status of the E-logs is reverted
        softAssert.assertTrue(objBuildingPermitPage.verifyElementExists(objWorkItemHomePage.linkedItemEFileIntakeLogs), "SMAB-T1900: Validation that efile logs are linked with work item");
        HashMap<String, ArrayList<String>> efileImportLogsData = objBuildingPermitPage.getGridDataInHashMap();
        softAssert.assertEquals(efileImportLogsData.get("Status").get(0), "Reverted", "SMAB-T1899: Validation that status of the efile logs linked with work item is reverted");

        //Step11: Validating the status should be completed
        objBuildingPermitPage.openTab(objWorkItemHomePage.tabDetails);
        objBuildingPermitPage.scrollToBottom();
        softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Status"), "Completed", "SMAB-T1890: Validation that status of the work item should be completed");

        //Logout at the end of the test
        objBuildingPermitPage.logout();

    }
    
    @SuppressWarnings("unlikely-arg-type")
	@Test(description = "SMAB-T3085,SMAB-T3086:Create building permit with the E-File import process with out completion date", dataProvider = "RPAppraiser",dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression","EFileImport" })
	public void EFileIntake_ApproveImportedFileWithoutCompletionDate(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="Atherton Building Permits";
		
		    // Step 1:Login as Appraiser user 
		objEfileImportPage.login(loginUser);

         	//Reverting the Approved Import logs if any in the system
			String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
			salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		
			//Step 2: Opening the E FILE IMPORT Module
			objEfileImportPage.searchModule(modules.EFILE_INTAKE);
			
			//Step 3: Importing a file
			objEfileImportPage.uploadFileOnEfileIntakeBP(fileType, source,"AthertonSingleRecordWithOutCompletiondate.txt",athertonBuildingPermitFile3);
			
			//Step 4: Waiting for Status of the imported file to be converted to "Imported"
			ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
			objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 400);
			
			//Step 5: Approving the imported file
			objPage.Click(objEfileImportPage.viewLinkRecord);
			ReportLogger.INFO("Approving the imported file : AthertonSingleRecordWithCompletiondate.txt");
			objPage.waitForElementToBeClickable(objEfileImportPage.errorRowSection, 20);
			objPage.waitForElementToBeClickable(objEfileImportPage.approveButton, 10);
			objPage.Click(objEfileImportPage.approveButton);
			objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);
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
    
    
	@SuppressWarnings("unlikely-arg-type")
	@Test(description = "SMAB-T3087,SMAB-T3088: :Create building permit with the E-File import process with completion date", dataProvider = "RPAppraiser",dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression","EFileImport" })
	public void EFileIntake_ApproveImportedFileWithCompletionDate(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="Atherton Building Permits";
		
		//Step 1: Login as Appraiser user
		objEfileImportPage.login(loginUser);

        //Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
	
		//Step 2: Opening the E FILE IMPORT Module
		objEfileImportPage.searchModule(modules.EFILE_INTAKE);
		 
		//Step 3: importing a file
		objEfileImportPage.uploadFileOnEfileIntakeBP(fileType, source,"AthertonSingleRecordWithCompletiondate.txt",athertonBuildingPermitFile2);
		
		//Step 4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 400);
		
		//Step 5: approving the imported file
		objPage.Click(objEfileImportPage.viewLinkRecord);
		ReportLogger.INFO("Approving the imported file : AthertonSingleRecordWithCompletiondate.txt");
		objPage.waitForElementToBeClickable(objEfileImportPage.errorRowSection, 20);
		objPage.waitForElementToBeClickable(objEfileImportPage.approveButton, 10);
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);
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
        
}
