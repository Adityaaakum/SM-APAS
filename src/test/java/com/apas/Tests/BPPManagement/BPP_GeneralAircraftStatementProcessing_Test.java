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
		objEfileHomePage.updateRollYearStatus("Open", rollYear);

	}
		
	/**
	 * DESCRIPTION: Verify creation of Aircraft Annual Settings Work Item 
	 * and restriction of creation of duplicate Annual Settings record
	 */
	
	@Test(description = "SMAB-T3840,SMAB-T3985: Verify creation of Aircraft Annual Settings Work Item and restriction of creation of duplicate Annual Settings record", dataProvider = "loginBPPAppraisalUser", dataProviderClass = DataProviders.class, groups = {"Regression", "GeneralAircraft", "BPPManagement"})
	public void BPP_GeneralAircraft_AircraftAnnualSettings(String loginUser) throws Exception {
		
		//Delete BPP Annual Settings
		objSalesforceAPI.deleteBPPAnnualSettings(rollYear);
		 
		//Delete the existing WI from system before importing files
        String query = "select id from Work_Item__c where Request_Type__c = 'BPP - Annual Setting - BPP-Annual Setting'";
        objSalesforceAPI.delete("Work_Item__c", query);
        
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
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.rollYearLabel),rollYear,
				"SMAB-T3840: Validate Roll year on the Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.statusLabel),"Active",
				"SMAB-T3840: Validate the status on the Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.improvementPILabel),"60%",
				"SMAB-T3840: Validate Improvement PI default % value on the Annual Settings record");
        softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.landPILabel),"40%",
				"SMAB-T3840: Validate Land PI default % value on the Annual Settings record");
        
        //Get the WI id and navigate to it
        String workItemQuery = "Select id, Name from Work_Item__c where Request_Type__c = 'BPP - Annual Setting - BPP-Annual Setting' limit 1";
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
		softAssert.assertContains(objBppManagementPage.getFieldValueFromAPAS(objBppManagementPage.rollYearLabel),rollYear,
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
        objBppManagementPage.searchAndSelectOptionFromDropDown(objBppManagementPage.rollYearLabel,rollYear);
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

}