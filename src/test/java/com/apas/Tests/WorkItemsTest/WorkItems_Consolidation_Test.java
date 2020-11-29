package com.apas.Tests.WorkItemsTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.SupportedSourceVersion;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;
import org.testng.asserts.SoftAssert;
import com.apas.config.modules;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;

import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;

public class WorkItems_Consolidation_Test extends TestBase implements testdata, modules, users {
	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendSetupPage objBppTrendSetupPage;
	BppTrendPage objBppTrendPage;
	WorkItemHomePage objWorkItemHomePage;
	EFileImportPage objEfileImportPage;
	ApasGenericPage apasGenericObj;
	ParcelsPage objParcelsPage;
	Util objUtil;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		  objParcelsPage= new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		// objBppTrendPage = new BppTrendPage(driver);
		// objBppTrendSetupPage = new BppTrendSetupPage(driver);
		objUtil= new Util();
	}

	@Test(description = "SMAB-T2259,SMAB-T2260,SMAB-T2261: Verify auto generated Reminder WI, Revert Imported BOE Index & Goods Factors, auto generated Import WI again upon revert", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression", "Work_Item_BPP" }, alwaysRun = true)
	public void WorkItem_Consolidation(String loginUser) throws Exception {

		String PrimaryWorkitem ="WI-00011398";
		String secondaryWorkitem = "WI-00011395";
		String Workitem = "WI-00007923";
		String workItemNumber1;
        String workItemNumber2;
        
        // fetching a parcel where PUC is not blank but  Primary Situs is blank
        String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
        HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
        String apnValue= response.get("Name").get(0);
        
        String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeRP");
                
        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objApasGenericFunctions.login(users.EXEMPTION_SUPPORT_STAFF);

        // Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
        objApasGenericFunctions.searchModule("Parcels");
        objApasGenericFunctions.globalSearchRecords(apnValue);

        

        // Step 3: Creating Manual work item for the Parcel 
        PrimaryWorkitem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
        
         //objApasGenericFunctions.logout();
			/*
			 * Thread.sleep(15000); HashMap<String, ArrayList<String>>response2 =
			 * salesforceAPI.select(queryAPNValue); apnValue= response2.get("Name").get(0);
			 * String workItemCreationData2 = System.getProperty("user.dir") +
			 * testdata.MANUAL_WORK_ITEMS; Map<String, String> hashMapmanualWorkItemData2 =
			 * objUtil.generateMapFromJsonFile(workItemCreationData2,
			 * "DataToCreateWorkItemOfTypeRP");
			 */
         Thread.sleep(2000);
         //objApasGenericFunctions.login(loginUser);
         
        objApasGenericFunctions.searchModule("Parcels");
        objApasGenericFunctions.globalSearchRecords(apnValue);

        
        secondaryWorkitem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		//wait for 30 sec to load created  work item in In progress tab
        Thread.sleep(30000);
		//Stpe5: Open the Work Item Home Page
		driver.navigate().refresh();
        Thread.sleep(2000);
        objApasGenericFunctions.searchModule(HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		Thread.sleep(2000);
				 // objWorkItemHomePage.Click(objWorkItemHomePage.toggleBUtton);
	
			  objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
			  //objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
			  //objWorkItemHomePage.Click(objWorkItemHomePage.ConsolidateButton);
			  //objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.ErrorMessage, 5);
			  // softAssert.assertEquals(objWorkItemHomePage.ErrorMessage.getText()
			  // ,"Please select two or more Work Items from the same Work Pool and having the same APN/Account # to consolidate."
			  // ,"SMAB-T2260,SMAB-T2260 Validation on workitem with diff APN and Work pool can not consolidate ");//apserror msg
			  // objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);//apasgeneric
			  // objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
			  objWorkItemHomePage.selectWorkItemOnHomePage(secondaryWorkitem);
			  objWorkItemHomePage.Click(objWorkItemHomePage.ConsolidateButton); //
			  objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.
			  SelectPrimaryButton, 5);
			  objWorkItemHomePage.Click(objWorkItemHomePage.SelectPrimaryButton);
			  objWorkItemHomePage.clickElementOnVisiblity(
			  "//label[text()='Select Primary']/following-sibling::div//span[contains(@title,'"
			  + PrimaryWorkitem + "')]");///
			  objWorkItemHomePage.Click(objPage.getButtonWithText("Save"));//get buttontext
			 
			  Thread.sleep(2000);
			  HashMap<String, ArrayList<String>> PrimaryWorkItems =
			  objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_PROGRESS);
			  softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(PrimaryWorkitem),"SMAB-T2261: Validation that Primary WorkItem should be visible In In Progress Tab On Home page after consolidate ");
			  softAssert.assertTrue(!(PrimaryWorkItems.get("Work Item Number").contains(secondaryWorkitem)),"SMAB-T2261: Validation that Secondary WorkItem should not be visible In In Progress Tab On Home page after consolidate ");
			  objWorkItemHomePage.openWorkItem(PrimaryWorkitem);
			  Thread.sleep(2000);
			  objWorkItemHomePage.Click(objWorkItemHomePage.ChildWorkItemsTab);
			  Thread.sleep(2000);
			  objWorkItemHomePage.clickElementOnVisiblity("//span[text()='" +
			  secondaryWorkitem + "']");
			  //objWorkItemHomePage.openWorkItem(secondaryWorkitem);
			  objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			  Thread.sleep(2000); softAssert.assertEquals(objApasGenericFunctions.
			  getFieldValueFromAPAS("Request Type"),"RP - CPI Factor - Test WI"
			  ,"SMAB-T2276 : request type matched RP - CPI Factor - Test WI");
			  softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS(
			  "Status"),"In Progress","SMAB-T2276 : Status Of Child WI Should be In Progress ");
			  softAssert.assertEquals(objApasGenericFunctions.
			  getFieldValueFromAPAS("Parent-Child Relationship Type"
			  ),"Consolidated Secondary"
			  ,"SMAB-T2276 : parent-Child Relationship Type Of Child WI Should be Consolidated Secondary");
			 
			  driver.navigate().back();
			  objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.
			  dataTabCompleted, 5);
			  objWorkItemHomePage.javascriptClick(objWorkItemHomePage.dataTabCompleted);
			  objWorkItemHomePage.javascriptClick(objWorkItemHomePage.
			  markAsCurrentStatusButton);
			  objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.
			  ErrormsgOnWI, 20);
			  softAssert.assertEquals(objWorkItemHomePage.ErrormsgOnWI.getText()
			  ,"Status: You cannot change status from In Progress to Completed",
			  "SMAB-T2287 : Error validation You cannot change status from In Progress to Completed On WI page");
			  objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			  driver.navigate().back();
			  objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
			  objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
			  objWorkItemHomePage.Click(objWorkItemHomePage.MarkCompleteButton);
			  objWorkItemHomePage.Click(objWorkItemHomePage.submittedforApprovalTimeline);
			  PrimaryWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.
			  TAB_MY_SUBMITTED_FOR_APPROVAL);
			  softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(
			  PrimaryWorkitem),
			  "SMAB-T2276: Validation that Primary WorkItem should be visible In In Progress Tab On Home page after consolidate "
			  ); softAssert.assertEquals(objWorkItemHomePage.getCurrentStatusOFWI(secondaryWorkitem), "Submitted for Approval",
			  "SMAB-T2287: Verify status of WI : 'Perform Calculations' is 'Submitted for Approval'"
			  );
			 
		//String query = "select Work_Item_Configuration__c from Work_Item__c where Name='" + PrimaryWorkitem + "'";
		//objSalesforceAPI.update("Work_Item_Configuration__c", query, "Multi_tier_Approval__c", "Yes");
		//objApasGenericFunctions.logout();
		// Login with supervisor_1
		
		  objApasGenericFunctions.login(loginUser);
		  objApasGenericFunctions.searchModule(modules.HOME);
		  
		  objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		  //Thread.sleep(2000);
		  objWorkItemHomePage.Click(objWorkItemHomePage.toggleBUtton);
		  Thread.sleep(2000);
		  objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
		  objWorkItemHomePage.Click(objWorkItemHomePage.ApproveButton);
		  objWorkItemHomePage.Click(objWorkItemHomePage.submittedforApprovalTimeline);
		  PrimaryWorkItems =
		  objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		  softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(
		  PrimaryWorkitem),
		  "SMAB-T2288: Validation that Primary WorkItem should be visible In Submit For approval tab after first lavel of approval ");
		  
		objApasGenericFunctions.logout();
		// Login supervisor 2 users.DATA_ADMIN
		objApasGenericFunctions.login(users.DATA_ADMIN);
		objApasGenericFunctions.searchModule(modules.HOME);
	    objWorkItemHomePage.Click(objWorkItemHomePage.toggleBUtton);
        Thread.sleep(2000);
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.ApproveButton);
		objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);
	    PrimaryWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(PrimaryWorkitem), "SMAB-T2289: Validation that Primary WorkItem should be visible In completed Tab after after 2nd lavel of approval "); 
	objApasGenericFunctions.logout();


	}
}
