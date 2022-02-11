package com.apas.Tests.BPPManagement;

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
import com.apas.PageObjects.BppManagementPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.EFileImportLogsPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.relevantcodes.extentreports.LogStatus;

public class BPP_GeneralAircraftStatementProcessing_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	BppManagementPage objBppManagementPage;
	EFileImportPage objEfileHomePage;
	EFileImportLogsPage objEFileImportLogPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();
	WorkItemHomePage objWorkItemHomePage;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		
		objPage = new Page(driver);
		objBppManagementPage = new BppManagementPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objEFileImportLogPage=new EFileImportLogsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = "2022";
		objWorkItemHomePage = new WorkItemHomePage(driver);
		
	}
		
	/**
	 * DESCRIPTION: Verify creation of Aircraft Annual Settings Work Item 
	 * and restriction of creation of duplicate Annual Settings record
	 */
	
	@Test(description = "SMAB-T3840,SMAB-T3985: Verify creation of Aircraft Annual Settings Work Item and restriction of creation of duplicate Annual Settings record", dataProvider = "loginBPPAppraisalUser", dataProviderClass = DataProviders.class, enabled = true, groups = {"Regression", "GeneralAircraft", "BPPManagement"})
	public void BPP_GeneralAircraft_AircraftAnnualSettings(String loginUser) throws Exception {
		
		//Delete BPP Annual Settings
		objSalesforceAPI.deleteCurrentRollYearBPPAnnualSettings(rollYear);
		 
		//Delete the existing WI from system before importing files
        String query = "select id from Work_Item__c where Request_Type__c = 'BPP - Annual Setting - BPP-Annual Setting' and (Status__c = 'In Pool' Or Status__c = 'In Progress')";
        if (query != null) objSalesforceAPI.delete("Work_Item__c", query);
        objSalesforceAPI.deleteAircraftSDRWIs();
        
        //Generate Reminder WI
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_AIRCRAFT_ANNUAL_SETTINGS);

        //Login to the APAS application using the credentials passed through data provider
        objBppManagementPage.login(loginUser);
        
        //Open the Work Item Home Page
        objBppManagementPage.searchModule(modules.HOME);
        objBppManagementPage.waitForElementToBeClickable(20, objWorkItemHomePage.lnkTABInPool);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInPool);
		objWorkItemHomePage.waitForElementToBeVisible(20,objWorkItemHomePage.acceptWorkItemButton);
        
        //Verify that only one WI is generated
        String aircraftAnnualSettingsRequestType = "BPP - Annual Setting - BPP-Annual Setting";
        int aircraftAnnualSettingsWorkItemCount = objWorkItemHomePage.getWorkItemCount(aircraftAnnualSettingsRequestType,objWorkItemHomePage.TAB_IN_POOL);
        softAssert.assertEquals(aircraftAnnualSettingsWorkItemCount, 1, "SMAB-T3840: Validate that only one WI for Annual Settings is generated");
        
        //Verify the WorkPool name on the WI
        String aircraftAnnualSettingsWorkItem = objWorkItemHomePage.getWorkItemName(aircraftAnnualSettingsRequestType,objWorkItemHomePage.TAB_IN_POOL);
        softAssert.assertEquals(objWorkItemHomePage.getGridDataForRowString(aircraftAnnualSettingsRequestType).get("Work Pool").get(0), "BPP Work Pool", "SMAB-T3840: Validate the Work Pool name on WI for Annual Settings");

        //Accepting the work item and opening the link under 'Action' Column
        ReportLogger.INFO("Accept the WI");
        objWorkItemHomePage.acceptWorkItem(aircraftAnnualSettingsWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(aircraftAnnualSettingsWorkItem);
        
        //Validate the values on BPP Annual Settings record
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.rollYearSettingsLabel),rollYear,
				"SMAB-T3840: Validate Roll year on the Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.statusLabel),"Active",
				"SMAB-T3840: Validate the status on the Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.improvementPILabel),"60%",
				"SMAB-T3840: Validate Improvement PI default % value on the Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.landPILabel),"40%",
				"SMAB-T3840: Validate Land PI default % value on the Annual Settings record");
        
        //Get the WI id and navigate to it
        String workItemQuery = "Select id, Name from Work_Item__c where Name = '" + aircraftAnnualSettingsWorkItem  + "' limit 1";
		String workItemId = objSalesforceAPI.select(workItemQuery).get("Id").get(0);
        
		String executionEnv = System.getProperty("region");
		driver.navigate().to("https://smcacre--"+executionEnv.toLowerCase()+
				 ".lightning.force.com/lightning/r/Work_Item__c/"+workItemId+"/view");
		
		objBppManagementPage.waitForElementToBeClickable(20, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objBppManagementPage.waitForElementToBeClickable(20, objWorkItemHomePage.inProgressOptionInTimeline);
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "In Progress",
				"SMAB-T3840: Validate the status of WI for Annual Settings record");
		
		//Validate the WI details
		ReportLogger.INFO("Validate the WI details");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 20);
		
		softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.relatedActionLabel),"Annual Setting",
				"SMAB-T3840: Validate the Related Action label value on WI for Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.workPoolLabel),"BPP Work Pool",
				"SMAB-T3840: Validate the Work Pool name on WI for Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.actionLabel),"Annual Setting",
				"SMAB-T3840: Validate the Action label value on WI for Annual Settings record");
        
        //Click on Related action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
		objWorkItemHomePage.waitForElementToBeVisible(20,objBppManagementPage.rollYearLabel);
		softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.rollYearSettingsLabel),rollYear,
				"SMAB-T3840: Validate Roll year on the Annual Settings record");
	    softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.statusLabel),"Active",
	    		"SMAB-T3840: Validate the status on the Annual Settings record");
	    softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.improvementPILabel),"60%",
				"SMAB-T3840: Validate Improvement PI default % value on the Annual Settings record");
	    softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.landPILabel),"40%",
				"SMAB-T3840: Validate Land PI default % value on the Annual Settings record");
	    
	    //Validate that record can be updated
	    ReportLogger.INFO("Validate the BPP Annual Settings record can be updated");
        objBppManagementPage.editAndInputFieldData(objBppManagementPage.salesTaxLabel,objBppManagementPage.salesTaxLabel,"10");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.salesTaxLabel),"10%",
				"SMAB-T3840: Validate Sales Tax % value on the Annual Settings record");
        
        //Validate duplicate BPP Annual Settings record cannot be created for same Roll Year
        ReportLogger.INFO("Validate duplicate BPP Annual Settings record can't be created");
        objBppManagementPage.searchModule(modules.BPP_ANNUAL_SETTINGS);
        objBppManagementPage.Click(objBppManagementPage.newButton);
        objBppManagementPage.searchAndSelectOptionFromDropDown(objBppManagementPage.rollYearSettingsLabel,rollYear);
        Thread.sleep(1000);
        
        String expectedErrorMessageOnTop = "You can't save this record because a duplicate";
		softAssert.assertContains(objPage.getElementText(objBppManagementPage.pageError),expectedErrorMessageOnTop,"SMAB-T3985: Validate duplicate BPP Annual Settings record cannot be created");
		
        objBppManagementPage.Click(objPage.getButtonWithText("Save"));
        Thread.sleep(1000);
        objBppManagementPage.Click(objBppManagementPage.viewDuplicateLink);
        objBppManagementPage.waitForElementToBeVisible(20, objBppManagementPage.viewDuplicateScreenMessageArea);
        String errorMessageOnViewDuplicateScreen = "More than one Annual Settings record cannot be saved for a Roll Year.";
        
        softAssert.assertEquals(objPage.getElementText(objBppManagementPage.viewDuplicateScreenMessageArea),errorMessageOnViewDuplicateScreen,"SMAB-T3985: Validate duplicate BPP Annual Settings record cannot be created with and error message appears");
        objBppManagementPage.Click(objBppManagementPage.closeViewDuplicatesPopUpButton);
        objBppManagementPage.Click(objPage.getButtonWithText("Cancel"));
        Thread.sleep(1000);
        
        //Log out from the application
        objBppManagementPage.logout();
	}
	
	
	/**
	 * DESCRIPTION: Verify creation of Airport Listing Work Item 
	 * and Import Airport Listing data
	 */
	
	@Test(description = "SMAB-T4449,SMAB-T4266,SMAB-T4267: Verify creation of Airport Listing Work Item and Import Airport Listing data", dataProvider = "loginBPPAppraisalUser", dataProviderClass = DataProviders.class, enabled = true, groups = {"Regression", "GeneralAircraft", "BPPManagement"})
	public void BPP_GeneralAircraft_AirportListingImport(String loginUser) throws Exception {
		
		//Login to the APAS application 
		objEfileHomePage.login(loginUser);
		
		//Delete or Revert the existing data from system before importing files
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Airport Listing' and Import_Period__C='" + rollYear + "' and File_Source__C = 'Airports' and (Status__c = 'Imported' Or Status__c = 'Approved')";
		objSalesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
		objSalesforceAPI.deleteAircraftListingWIs();
		objSalesforceAPI.deleteAircraftSDRWIs();

		//Generate Reminder WI
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);

		//Fetch the excel file to be imported for Airport Listing
		String fileName = System.getProperty("user.dir") + testdata.BPP_AIRPORT_LISTING_DATA;		
				
		//Open the Work Item Home Page
        objBppManagementPage.searchModule(modules.HOME);
        objBppManagementPage.waitForElementToBeClickable(20, objWorkItemHomePage.lnkTABInPool);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInPool);
		objWorkItemHomePage.waitForElementToBeVisible(20,objWorkItemHomePage.acceptWorkItemButton);
       
        //Verify that only one WI is generated
        String airportListingRequestType = "BPP - Airport Listing Import - BPP - Airport Listing Import";
        int airportListingWorkItemCount = objWorkItemHomePage.getWorkItemCount(airportListingRequestType, objWorkItemHomePage.TAB_IN_POOL);
        softAssert.assertEquals(airportListingWorkItemCount, 1, 
        		"SMAB-T4266: Validate that only one WI for Airport Listing is generated");
        
        //Verify the WorkPool name on the WI
        String aircraftAnnualSettingsWorkItem = objWorkItemHomePage.getWorkItemName(airportListingRequestType,objWorkItemHomePage.TAB_IN_POOL);
        softAssert.assertEquals(objWorkItemHomePage.getGridDataForRowString(airportListingRequestType).get("Work Pool").get(0), "Aircraft Work Pool", 
        		"SMAB-T4266: Validate the Work Pool name on WI for Airport Listing");

        //Accepting the work item and opening the link under 'Action' Column
        ReportLogger.INFO("Accept the WI");
        objWorkItemHomePage.acceptWorkItem(aircraftAnnualSettingsWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(aircraftAnnualSettingsWorkItem);
        Thread.sleep(3000); //Allow upload button to appear
        objPage.verifyElementExists("//span[contains(.,'Upload Files')]");
        
        //Get the WI id and navigate to it
        String workItemQuery = "Select id from Work_Item__c where Name = '" + aircraftAnnualSettingsWorkItem  + "' limit 1";
		String workItemId = objSalesforceAPI.select(workItemQuery).get("Id").get(0);
        
		String executionEnv = System.getProperty("region");
		driver.navigate().to("https://smcacre--"+executionEnv.toLowerCase()+
				 ".lightning.force.com/lightning/r/Work_Item__c/"+workItemId+"/view");
		
		objBppManagementPage.waitForElementToBeClickable(20, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objBppManagementPage.waitForElementToBeClickable(20, objWorkItemHomePage.inProgressOptionInTimeline);
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "In Progress",
				"SMAB-T4266: Validate the status of WI for Airport Listing");
		
		//Validate the WI details
		ReportLogger.INFO("Validate the WI details");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 20);
		
		softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.relatedActionLabel),"Airport Listing Import",
				"SMAB-T4267: Validate the Related Action label value on WI for Airport Listing");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.workPoolLabel),"Aircraft Work Pool",
				"SMAB-T4267: Validate the Work Pool name on WI for Airport Listing");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.actionLabel),"Airport Listing Import",
				"SMAB-T4267: Validate the Action label value on WI for Airport Listing");
        
        //Click on Related action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
		//Uploading the Airport Listing file having error and success records
		objEfileHomePage.uploadFile(fileName + "AirportListingExcelSheet.xlsx");		
		
		//Waiting for the status of the file to be converted to Imported
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 360);
		
		//Opening the Efile Import Logs module
		objEfileHomePage.searchModule(modules.EFILE_IMPORT_LOGS);
		HashMap<String, ArrayList<String>> importLogsGridData = objEfileHomePage.getGridDataInHashMap(1, 1);
		
		//Import Logs grid validation for the imported Airport Listing file
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"Airport Listing :Airports :" + rollYear,
				"SMAB-T4449: Validate the name of imported file on import logs page");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Imported", 
				"SMAB-T4449: Validate if status of imported file is imported on import logs page");
		
		//Opening the Efile Import Transactions module
		objEfileHomePage.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		HashMap<String, ArrayList<String>> importTransactionsGridData = objEfileHomePage.getGridDataInHashMap(1, 1);
		
		//Import Transactions grid validation for the imported Airport Listing file
		softAssert.assertEquals(importTransactionsGridData.get("E-File Import Log").get(0),"Airport Listing :Airports :" + rollYear,
				"SMAB-T4449: Validate the name of imported file on import transactions page");
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported", 
				"SMAB-T4449: Validate if status of imported file is imported on import transactions page");
		
		//Opening the e-file intake module
		objEfileHomePage.searchModule(modules.EFILE_INTAKE);
		
		//Selecting the File Type and Source and clicking on 'View All' link 
		objEfileHomePage.selectFileAndSource("Airport Listing", "Airports");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		//Validating that correct number of records are present in error row section after file import
		String expectedNoOfErrorRecords = "2";    //Imported File has 2 error records in each table			
		
		//Validation of number of records in error row section
		String actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
		softAssert.assertEquals(actualNoOfErrorRecords, expectedNoOfErrorRecords, 
				"SMAB-T4449: Validate if correct number of records are displayed in Error Row Section after file import");
				
		//Validation of number of records in imported row section					
		String expectedImportedRowsCount = "15";	
		String actualImportedRowsCount = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("IMPORTED");
		softAssert.assertEquals(actualImportedRowsCount, expectedImportedRowsCount, 
				"SMAB-T4449: Validation if correct number of records are displayed in Imported Row Section after file import");
			
		//********* Validate Discard Functionality *********	
		//Validation for Records discard functionality from Review and Approve Page
		ReportLogger.INFO("Validate that error records can be discarded from Review and Approve Data Page");
		objEfileHomePage.discardErrorRecords("1");
			
		//Validating the number of records in the error row section after discarding a record
		int updatedCount = Integer.parseInt(expectedNoOfErrorRecords) - 1;
		actualNoOfErrorRecords = objEfileHomePage.getCountOfRowsFromErrorOrImportedRowsSection("ERROR");
		String updatedRecordsInErrorRowPostDelete = Integer.toString(updatedCount);		
		softAssert.assertEquals(actualNoOfErrorRecords, updatedRecordsInErrorRowPostDelete, 
				"SMAB-T4449: Validation if correct number of records are displayed in Error Row Section after discarding a record");

		objBppManagementPage.logout();
	}
	
	/**
	 * DESCRIPTION: Create and Validate the PI Space Record
	 */
	
	@Test(description = "SMAB-4164: Create record on PI Setting object", dataProvider = "loginBPPAppraisalUser", dataProviderClass = DataProviders.class, enabled = true, groups = {
			"Regression", "GeneralAircraft", "BPPManagement" })
	public void BPP_CreateNewPISettingsRecord(String loginUser) throws Exception {

		Map<String, String> mapToCreatePISpaceSettingsData = objUtil.generateMapFromJsonFile(
				testdata.PI_SPACE_SETTINGS_RECORD_CREATION_DATA, "DataToCreatePISpaceSettingsRecord");
		Map<String, String> mapToUpdatePISpaceSettingsData = objUtil.generateMapFromJsonFile(
				testdata.PI_SPACE_SETTINGS_RECORD_CREATION_DATA, "DataToUpdatePISpaceSettingsRecord");

		// Login to the APAS application as Aircraft Auditor
		objBppManagementPage.login(loginUser);
		ReportLogger.INFO("Login to the APAS application as Aircraft Auditor");

		// Open the PI Space settings
		objBppManagementPage.searchModule(modules.PI_SPACE_SETTINGS);
		ReportLogger.INFO("Opened the Space settings");

		// Create new pi space settings record
		objBppManagementPage.createPISpaceRecord(mapToCreatePISpaceSettingsData);

		// Assert the values on the record created
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.rollYearLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.rollYearLabel),
				"SMAB-4164:Roll year is matching");
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.rentFeeLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.rentFeeLabel),
				"SMAB-4164: Rent fee is matching");
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.rentFactorLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.rentFactorLabel),
				"SMAB-4164:Rent factor is matching");
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.svcRateLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.svcRateLabel),
				"SMAB-4164:SVC Rate is Matching");
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.svcDescriptionLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.svcDescriptionLabel),
				"SMAB-4164:SVC Desc is matching");
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.airPortLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.airPortLabel), "SMAB-4164:Airport is matching");
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.typeOfSpaceLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.typeOfSpaceLabel), "Type of space is matching");
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.spaceNameLabel),
				mapToCreatePISpaceSettingsData.get(objBppManagementPage.spaceNameLabel),
				"SMAB-4164space name is matching");
		ReportLogger.INFO("Validated the created PI record");
		
		//Update PI record
		objBppManagementPage.Click(objBppManagementPage.getButtonWithText("Edit"));
		objBppManagementPage.enter(objBppManagementPage.svcRateLabel,
				mapToUpdatePISpaceSettingsData.get(objBppManagementPage.svcRateLabel));
		objBppManagementPage.Click(objPage.getButtonWithText("Save"));
		objBppManagementPage.waitForElementToBeClickable(5, objBppManagementPage.getButtonWithText("Edit"));
		softAssert.assertEquals(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.svcRateLabel),
				mapToUpdatePISpaceSettingsData.get(objBppManagementPage.svcRateLabel),
				"SMAB-4164:SVC Rate is Matching");
		ReportLogger.INFO("PI Record updated and Validated");
		
		// Log out from the application
		objBppManagementPage.logout();

	}
}