package com.apas.Tests.WorkItemsTest.WorkItemWorkFlow;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.FileUtils;
import com.apas.Utils.SalesforceAPI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.apas.Utils.Util;
import com.apas.config.BPFileSource;
import com.apas.config.fileTypes;
import com.apas.config.modules;
import com.apas.config.testdata;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorkItemWorkflow_BuildingPermit_Test extends TestBase {

    RemoteWebDriver driver;
    Page objPage;
    ApasGenericPage objApasGenericPage;
    BuildingPermitPage objBuildingPermitPage;
    WorkItemHomePage objWorkItemHomePage;
    SalesforceAPI salesforceAPI;
    EFileImportPage objEfileImportPage;
    ReportsPage objReportsPage;

    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI objSalesforceAPI = new SalesforceAPI();
    
    String athertonBuildingPermitFile1;
	String athertonBuildingPermitFile;
    
   

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
        objApasGenericPage = new ApasGenericPage(driver);
        
        athertonBuildingPermitFile1 = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "AthertonSingleRecordWithCompletiondate.txt";
    	athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "AthertonSingleRecordWithOutCompletiondate.txt";
    	
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
    
	@Test(description = "SMAB-T3085,SMAB-T3086:Create building permit with the E-File import process with out completion date", dataProvider = "RPAppraiser",dataProviderClass = DataProviders.class, groups = {
			"Regression","EFileImport","WorkItemWorkflow_BuildingPermit", "BuildingPermit"}, alwaysRun = true )
	public void EFileIntake_ApproveImportedFileWithoutCompletionDate(String loginUser) throws Exception{
		String period = "Adhoc";
		String fileType="Building Permit";
		String source="Atherton Building Permits";
		
		    // Step 1:Login as Appraiser user 
		    objEfileImportPage.login(loginUser);

         	//Reverting the Approved Import logs if any in the system
			String query = "Select id From E_File_Import_Log__c where File_type__c = '"+fileType+"' and File_Source__C = '"+source+"' and Import_Period__C='" + period + "' and Status__c in ('Approved','Imported') ";
			salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
			
			//Step1: Creating temporary file with random building permit number
			String missingAPNBuildingPermitNumber = "T" + DateUtil.getCurrentDate("dd-hhmmss");
			String invalidAPNBuildingPermitNumber = "T" + DateUtil.getCurrentDate("dd-hhmmss");
			String buildingPermitFile = System.getProperty("user.dir") + testdata.EFILEIMPORT_BPDATA + "AthertonSingleRecordWithOutCompletiondate.txt";
			String temporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + "AthertonSingleRecordWithOutCompletiondate.txt";
			FileUtils.replaceString(buildingPermitFile,"<PERMITNO>",missingAPNBuildingPermitNumber,temporaryFile);
			FileUtils.replaceString(temporaryFile,"<PERMIT-2>",invalidAPNBuildingPermitNumber,temporaryFile);

		
			//Step 2: Opening the E FILE IMPORT Module
			objEfileImportPage.searchModule(modules.EFILE_INTAKE);
			
			//Step 3: Importing a file
			objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", "AthertonSingleRecordWithOutCompletiondate.txt", temporaryFile);

			
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
			ReportLogger.INFO("Approval completed");
			
			//Step 6: Opening the Home module & Clicking on In Pool Tab
			objBuildingPermitPage.searchModule(modules.HOME);
			ReportLogger.INFO("We are on home page now...................");
	        objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);

	       //Import Review Work Item generation validation after file is imported
	        String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Final Review - AthertonSingleRecordWithOutCompletiondate" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
	        String importReviewWorkItem = salesforceAPI.select(queryWorkItem).get("Name").get(0);
	        ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
	    
            //Step 7: Accepting the work item & Going to Tab In Progress
	        objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
	        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
	        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
	    
	        //Step 8:Navigate to Building permits and select the building permit and editing the completion date and save
	        objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);
	        objBuildingPermitPage.displayRecords("All");
	        objBuildingPermitPage.globalSearchRecords(missingAPNBuildingPermitNumber);
            objBuildingPermitPage.scrollToElement(objBuildingPermitPage.editPermitButton);
            objWorkItemHomePage.Click(objBuildingPermitPage.editPermitButton);
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy ");
            Date date = new Date();        
            String todayDate= dateFormat.format(date);
            objApasGenericPage.enter("Completion Date", todayDate);
            objPage.Click(objPage.getButtonWithText("Save"));
        
           //Step 9:Navigate back to Home-In pool and search for the work item created with Construction Completed.
           objBuildingPermitPage.searchModule(modules.HOME);
           objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
           objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
           String queryWorkItem2 = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - E-file Import" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
	       String importReviewWorkItem2 = salesforceAPI.select(queryWorkItem2).get("Name").get(0);
	       ReportLogger.INFO("Created Work Item : " + importReviewWorkItem2);	
	       WebElement completedWorkItem = driver.findElement(By.xpath("//a[contains(text(),'" + importReviewWorkItem2 + "')]"));
		   String expectedWI=completedWorkItem.getText();
		   softAssert.assertEquals(expectedWI, importReviewWorkItem2, "SMAB-T3085,SMAB-T3086:Status of the work item should be Construction completed");
	      
	    
	}
    
    
	@Test(description = "SMAB-T3087,SMAB-T3088:Create building permit with the E-File import process with completion date", dataProvider = "RPAppraiser",dataProviderClass = DataProviders.class, groups = {
			"Regression","EFileImport","WorkItemWorkflow_BuildingPermit", "BuildingPermit"})
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
		objEfileImportPage.uploadFileOnEfileIntakeBP(fileType, source,"AthertonSingleRecordWithCompletiondate.txt",athertonBuildingPermitFile1);
		
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
        objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
        ReportLogger.INFO("clicked inpool tab");

       //"Import Review" Work Item generation validation after file is imported
        String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Final Review - AthertonSingleRecordWithCompletiondate" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
        String importReviewWorkItem = salesforceAPI.select(queryWorkItem).get("Name").get(0);
        ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
    
        //Step 7: Accepting the work item & Going to Tab In Progress
        objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
        WebElement completedWorkItem = driver.findElement(By.xpath("//a[contains(text(),'" + importReviewWorkItem + "')]"));
		String expectedWI=completedWorkItem.getText();
		softAssert.assertEquals(expectedWI, importReviewWorkItem, "SMAB-T3087,SMAB-T3088 : Status of the workitem should be Final review");
	    

	}
	
    
    /**
     * This test case is to validate work item creation functionality and the work item flow after creating new building permit for active parcel
     **/
   
    @Test(description = "SMAB-T2989, SMAB-T3089: Creating manual entry for Active Parcel building permit With completion date", dataProvider = "RPAppraiser", dataProviderClass = com.apas.DataProviders.DataProviders.class, groups={"WorkItemWorkflow_BuildingPermit","Regression","BuildingPermit"}, alwaysRun = true)
	public void BuildingPermit_ManualCreateNewBuildingPermitActiveParcelWithDataValidations(String loginUser) throws Exception {

		//Fetching the Active Parcel
		String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C !='' limit 1";
		HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the Parcels module
		objBuildingPermitPage.searchModule(modules.PARCELS);

		//Step3: Search and Open the Parcel
		objBuildingPermitPage.globalSearchRecords(parcelToSearch);
		
		//Step4: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step5: Prepare a test data to create a new building permit
		String CompletionDate = "BuildingPermitManualCreationDataWithCompletionDate";
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationAllTestData(CompletionDate);
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
		manualBuildingPermitMap.put("APN",parcelToSearch);
		

		//Step6: Adding a new Building Permit with the APN passed in the above steps
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step7: Opening the Home module & Clicking on In Pool Tab
		driver.navigate().refresh();
		objBuildingPermitPage.searchModule(modules.HOME);

   		objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);


        //Step8: "Import Review" Work Item generation validation after file is imported
        String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
        String importReviewWorkItem = objSalesforceAPI.select(queryWorkItem).get("Name").get(0);
        ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
        
//        Step9: Asserting the WI in pool tab

        objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
        objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
        WebElement completedWorkItem = driver.findElement(By.xpath("//a[contains(text(),'" + importReviewWorkItem + "')]"));
        String expectedWI=completedWorkItem.getText();
        softAssert.assertEquals(expectedWI, importReviewWorkItem, "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");

        //		Step 10: Going to WI page and checking the status        
        objBuildingPermitPage.globalSearchRecords(importReviewWorkItem); 
        
        WebElement statusText=driver.findElement(By.xpath("//*[contains(text(),'Construction Completed')]"));
        objBuildingPermitPage.waitForElementToBeInVisible(statusText);
        String expectedStatus=statusText.getText();
        
        softAssert.assertEquals(expectedStatus, "Construction Completed", "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");
       
//      Step11: Going to Home Page and then In Pool Tab to Accept the work item & Going to Tab In Progress to verify the work item moved to in progress tab.

        driver.navigate().refresh();
    	    
        objBuildingPermitPage.searchModule(modules.HOME);

   		objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
        objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
        
        
        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
        objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
        objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
        objWorkItemHomePage.findWorkItemInProgress(importReviewWorkItem);
        ReportLogger.INFO("Found Work Item! Mission Completed.");

    }
        
    /**
     * This test case is to validate work item creation functionality and the work item flow after creating new building permit for Retired parcel
     **/
      
    @Test(description = "SMAB-T3007 : Creating manual entry for Retired Parcel building permit with completion date.", dataProvider = "RPAppraiser", dataProviderClass = com.apas.DataProviders.DataProviders.class, groups={"WorkItemWorkflow_BuildingPermit","Regression","BuildingPermit"}, alwaysRun = true)
	public void BuildingPermit_ManualCreateNewBuildingPermitRetiredParcelWithDataValidations(String loginUser) throws Exception {


		//Fetching the Retired Parcel
		String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Retired' and Primary_Situs__C !='' limit 1";
		HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the Parcels module
		objBuildingPermitPage.searchModule(modules.PARCELS);

		//Step3: Search and Open the Parcel
		objBuildingPermitPage.globalSearchRecords(parcelToSearch);
		
		//Step4: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step5: Prepare a test data to create a new building permit
		String CompletionDate = "BuildingPermitManualCreationDataWithCompletionDate";
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationAllTestData(CompletionDate);
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");


		//Step6: Adding a new Building Permit with the APN passed in the above steps
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step7: Opening the Home module & Clicking on In Pool Tab

		objBuildingPermitPage.searchModule(modules.HOME);
   		objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);

        //Step8: "Import Review" Work Item generation validation after file is imported
        String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
        String importReviewWorkItem = objSalesforceAPI.select(queryWorkItem).get("Name").get(0);
        ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
        
//      Step9: Asserting the WI in pool tab

      objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
      objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
      WebElement completedWorkItem = driver.findElement(By.xpath("//a[contains(text(),'" + importReviewWorkItem + "')]"));
      String expectedWI=completedWorkItem.getText();
      softAssert.assertEquals(expectedWI, importReviewWorkItem, "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");

      //		Step 10: Going to WI page and checking the status        
      objBuildingPermitPage.globalSearchRecords(importReviewWorkItem); 
      
      WebElement statusText=driver.findElement(By.xpath("//*[contains(text(),'Construction Completed')]"));
      objBuildingPermitPage.waitForElementToBeInVisible(statusText);
      String expectedStatus=statusText.getText();
      
      softAssert.assertEquals(expectedStatus, "Construction Completed", "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");
     
//    Step11: Going to Home Page and then In Pool Tab to Accept the work item & Going to Tab In Progress to verify the work item moved to in progress tab.

      driver.navigate().refresh();
  	    
      objBuildingPermitPage.searchModule(modules.HOME);

 		objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
      objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
      
      
      objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
      objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
      objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
      objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
      objWorkItemHomePage.findWorkItemInProgress(importReviewWorkItem);
      ReportLogger.INFO("Found Work Item! Mission Completed.");

    }

    /**
     * This test case is to validate work item creation functionality and the work item flow after creating new building permit Without completion date and with completion date for Active parcel
     **/

    @Test(description = "SMAB-T2987, SMAB-T2988, SMAB-T2989 : Creating manual entry for Active Parcel building permit without complition date", dataProvider = "RPAppraiser", dataProviderClass = com.apas.DataProviders.DataProviders.class, groups={"WorkItemWorkflow_BuildingPermit","Regression","BuildingPermit"}, alwaysRun = true)
   	public void BuildingPermit_ManualCreateNewBuildingPermitActiveParcelWithoutCompletionDate(String loginUser) throws Exception {


   		//Fetching the Active Parcel
   			String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C !='' limit 1";
   			HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);
   			String parcelToSearch = response.get("Name").get(0);

   		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
   			objBuildingPermitPage.login(loginUser);

   		//Step2: Opening the Parcels module
   			objBuildingPermitPage.searchModule(modules.PARCELS);

   		//Step3: Search and Open the Parcel
   			objBuildingPermitPage.globalSearchRecords(parcelToSearch);
   		
   		//Step4: Opening the building permit module
   			objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

   		//Step5: Prepare a test data to create a new building permit
   			String CompletionDate = "BuildingPermitManualCreationDataWithoutCompletionDate";
   			Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationAllTestData(CompletionDate);
   			String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");


   		//Step6: Adding a new Building Permit with the APN passed in the above steps
   			objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
   			System.out.println("Building Pemrit Number Is :- " + buildingPermitNumber);

   		//Step7: Opening the Home module
   			objBuildingPermitPage.searchModule(modules.HOME);
   		
   			objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
   			objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);

   		//	Step8: "Import Review" Work Item generation validation after file is imported
           String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction In Progress - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
           String importReviewWorkItem = objSalesforceAPI.select(queryWorkItem).get("Name").get(0);
           ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
           
//           Step9: Accepting the work item

           objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
           objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
           objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
           objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_PROGRESS,20);
           objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
           objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_PROGRESS,20);
           
         //Step10: Opening the building permit module
      		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);
      		
      	//Step11: Editing the existing Building Permit entering completion date

    		objBuildingPermitPage.displayRecords("All Manual Building Permits");
    		objBuildingPermitPage.globalSearchRecords(buildingPermitNumber);
    		objBuildingPermitPage.scrollToElement(objBuildingPermitPage.editPermitButton);
    		objWorkItemHomePage.Click(objBuildingPermitPage.editPermitButton);

    		//Step12: Editing Building Permit created before without completion date.
    		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy ");
    		Date date = new Date();
    		String Todaydate= dateFormat.format(date);
    		objWorkItemHomePage.enter("Completion Date", Todaydate);
    		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText("Save"));

       	//Step13: Opening the Home module
       		objBuildingPermitPage.searchModule(modules.HOME);
       		objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_PROGRESS,20);
    
        //Step14: "Import Review" Work Item generation validation after file is imported
               String editQueryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
               String editImportReviewWorkItem = objSalesforceAPI.select(editQueryWorkItem).get("Name").get(0);
               ReportLogger.INFO("Created Work Item : " + editImportReviewWorkItem);
              
               
//             Step15: Asserting the WI in pool tab

               objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
               objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
               objBuildingPermitPage.waitForElementToBeClickable(editImportReviewWorkItem);
               WebElement completedWorkItem = driver.findElement(By.xpath("//a[contains(text(),'" + editImportReviewWorkItem + "')]"));
               String expectedWI=completedWorkItem.getText();
               softAssert.assertEquals(expectedWI, editImportReviewWorkItem, "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");

               //		Step 16: Going to WI page and checking the status        
               objBuildingPermitPage.globalSearchRecords(editImportReviewWorkItem); 
               
               WebElement statusText=driver.findElement(By.xpath("//*[contains(text(),'Construction Completed')]"));
               objBuildingPermitPage.waitForElementToBeInVisible(statusText);
               String expectedStatus=statusText.getText();
               
               softAssert.assertEquals(expectedStatus, "Construction Completed", "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");
              
//             Step17: Going to Home Page and then In Pool Tab to Accept the work item & Going to Tab In Progress to verify the work item moved to in progress tab.

               driver.navigate().refresh();
           	    
               objBuildingPermitPage.searchModule(modules.HOME);

          		objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
         		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
         		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
               objBuildingPermitPage.waitForElementToBeClickable(editImportReviewWorkItem);
               
               
               objWorkItemHomePage.acceptWorkItem(editImportReviewWorkItem);
               objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
               objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
               objBuildingPermitPage.waitForElementToBeClickable(editImportReviewWorkItem);
               objWorkItemHomePage.findWorkItemInProgress(editImportReviewWorkItem);
               ReportLogger.INFO("Found Work Item! Mission Completed.");
               
       }
    /**
     * This test case is to validate work item creation functionality and the work item flow after creating new building permit Without completion date and with completion date for Retired parcel
     **/

    
    @Test(description = "SMAB-T3007: Creating manual entry for building permit for retired parcel without complition date", dataProvider = "RPAppraiser", dataProviderClass = com.apas.DataProviders.DataProviders.class, groups={"WorkItemWorkflow_BuildingPermit","Regression","BuildingPermit"}, alwaysRun = true)
   	public void BuildingPermit_ManualCreateNewBuildingPermitRetiredParcelWithoutCompletionDate(String loginUser) throws Exception {


   		//Fetching the Active Parcel
   			String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Retired' and Primary_Situs__C !='' limit 1";
   			HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);
   			String parcelToSearch = response.get("Name").get(0);

   		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
   			objBuildingPermitPage.login(loginUser);

   		//Step2: Opening the Parcels module
   			objBuildingPermitPage.searchModule(modules.PARCELS);

   		//Step3: Search and Open the Parcel
   			objBuildingPermitPage.globalSearchRecords(parcelToSearch);
   		
   		//Step4: Opening the building permit module
   			objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

   		//Step5: Prepare a test data to create a new building permit
   			String CompletionDate = "BuildingPermitManualCreationDataWithoutCompletionDate";
   			Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationAllTestData(CompletionDate);
   			String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");


   		//Step6: Adding a new Building Permit with the APN passed in the above steps
   			objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
   			ReportLogger.INFO("Building Permit Number Is :- " + buildingPermitNumber);

   		//Step7: Opening the Home module
   			objBuildingPermitPage.searchModule(modules.HOME);
   			objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
   			objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);


   		//	Step8: "Import Review" Work Item generation validation after file is imported
           String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction In Progress - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";

           String importReviewWorkItem = objSalesforceAPI.select(queryWorkItem).get("Name").get(0);
           ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
  
           
       //Step9: Accepting the work item

           objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
           objBuildingPermitPage.waitForElementToBeClickable(importReviewWorkItem);
           objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
           objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_PROGRESS,20);
           objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
           objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_PROGRESS,20);
            
        //Step10: Opening the building permit module
      		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);
      		
      	//Step11: Editing the existing Building Permit entering completion date

    		objBuildingPermitPage.displayRecords("All Manual Building Permits");
    		objBuildingPermitPage.globalSearchRecords(buildingPermitNumber);
    		objBuildingPermitPage.scrollToElement(objBuildingPermitPage.editPermitButton);
    		objWorkItemHomePage.Click(objBuildingPermitPage.editPermitButton);

    	//Step12: Editing Building Permit created before without completion date.
    		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy ");
    		Date date = new Date();
    		String Todaydate= dateFormat.format(date);
    		objWorkItemHomePage.enter("Completion Date", Todaydate);
    		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText("Save"));

       	//Step13: Opening the Home module
       		objBuildingPermitPage.searchModule(modules.HOME);
   			objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_PROGRESS,20);
    
    

        //Step14: "Import Review" Work Item generation validation after file is imported
               String editQueryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
               String editImportReviewWorkItem = objSalesforceAPI.select(editQueryWorkItem).get("Name").get(0);
               ReportLogger.INFO("Created Work Item : " + editImportReviewWorkItem);
   
//             Step15: Asserting the WI in pool tab

               objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
               objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
               objBuildingPermitPage.waitForElementToBeClickable(editImportReviewWorkItem);
               WebElement completedWorkItem = driver.findElement(By.xpath("//a[contains(text(),'" + editImportReviewWorkItem + "')]"));
               String expectedWI=completedWorkItem.getText();
               softAssert.assertEquals(expectedWI, editImportReviewWorkItem, "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");

               //		Step 16: Going to WI page and checking the status        
               objBuildingPermitPage.globalSearchRecords(editImportReviewWorkItem); 
               
               WebElement statusText=driver.findElement(By.xpath("//*[contains(text(),'Construction Completed')]"));
               objBuildingPermitPage.waitForElementToBeInVisible(statusText);
               String expectedStatus=statusText.getText();
               
               softAssert.assertEquals(expectedStatus, "Construction Completed", "SMAB-T2989, SMAB-T3089:Status of the work item should be Construction completed");
              
//             Step17: Going to Home Page and then In Pool Tab to Accept the work item & Going to Tab In Progress to verify the work item moved to in progress tab.

               driver.navigate().refresh();
           	    
               objBuildingPermitPage.searchModule(modules.HOME);

          		objBuildingPermitPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL,20);
         		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
         		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
               objBuildingPermitPage.waitForElementToBeClickable(editImportReviewWorkItem);
               
               
               objWorkItemHomePage.acceptWorkItem(editImportReviewWorkItem);
               objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
               objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
               objBuildingPermitPage.waitForElementToBeClickable(editImportReviewWorkItem);
               objWorkItemHomePage.findWorkItemInProgress(editImportReviewWorkItem);
               ReportLogger.INFO("Found Work Item! Mission Completed.");
               
       }
        
}