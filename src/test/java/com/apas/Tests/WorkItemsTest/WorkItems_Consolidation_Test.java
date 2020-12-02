package com.apas.Tests.WorkItemsTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.SupportedSourceVersion;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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
		apasGenericObj = new ApasGenericPage(driver);
		// objBppTrendSetupPage = new BppTrendSetupPage(driver);
		objUtil= new Util();
	}

	@Test(description = "SMAB-T2259,SMAB-T2260,SMAB-T2261, SMAB-T2276, SMAB-T2287,SMAB-T2288,SMAB-T2289: Verify auto generated Reminder WI, Revert Imported BOE Index & Goods Factors, auto generated Import WI again upon revert", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
	public void WorkItem_Consolidation(String loginUser) throws Exception {

		String PrimaryWorkitem ="";
		String secondaryWorkitem = "";
		String Workitem = "";

		  //fetching a parcel where PUC is not blank but Primary Situs is blank 
		  String queryAPNValue ="select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 2" ;  
		  HashMap<String, ArrayList<String>> response =salesforceAPI.select(queryAPNValue); 
		  String apnValue=response.get("Name").get(0); 
		  String apnValue1=response.get("Name").get(1);
		  String workItemCreationData = System.getProperty("user.dir") +testdata.MANUAL_WORK_ITEMS; 
		  Map<String, String> hashMapmanualWorkItemData =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeRP");
		   // Step1: Login to the APAS application using the credentials of staff user)
		  objApasGenericFunctions.login(users.EXEMPTION_SUPPORT_STAFF);
		  // Step2: Opening the PARCELS page and searching a parcel
		  objApasGenericFunctions.searchModule(PARCELS);
		  objApasGenericFunctions.globalSearchRecords(apnValue); 
		  // Step 3: Creating Manual work item for the Parcel 
		  PrimaryWorkitem =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		  // Step5: Opening the PARCELS page and searching a parcel
		  objApasGenericFunctions.searchModule(PARCELS);
		  objApasGenericFunctions.globalSearchRecords(apnValue); 
		  // Step 6: Creating Manual work item for the Parcel 
		  secondaryWorkitem =objParcelsPage.createWorkItem(hashMapmanualWorkItemData); 
		  // Step 7: Opening the PARCELS page and searching a parcel 
		  Map<String, String> hashMapmanualWorkItemData1 =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeDisableveterans");
		  objApasGenericFunctions.searchModule(PARCELS); 
		  objApasGenericFunctions.globalSearchRecords(apnValue1); 
		  Workitem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData1); 
		  //wait for 5 sec to load created work item in In progress tab 
		  Thread.sleep(5000); 
		  //Stpe 8: Open the Work Item Home Page 
		  driver.navigate().refresh(); 
		  objApasGenericFunctions.searchModule(HOME);
		  objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		  Thread.sleep(2000); 
		  //steps 9: selecting 2 work item with different apn and work pool and validate error msg after consolidate both wi
		  objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
		  objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		  objWorkItemHomePage.Click(objPage.getButtonWithText(objWorkItemHomePage.ConsolidateButton));
		  softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(),"Please select two or more Work Items from the same Work Pool and having the same APN/Account # to consolidate.","SMAB-T2259,SMAB-T2260 Validation on workitem with diff APN and Work pool can not consolidate "); objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg); 
		  //steps 10:select 2 work item with same APn and work pool and consolidate
		  objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		  objWorkItemHomePage.selectWorkItemOnHomePage(secondaryWorkitem);
		  objWorkItemHomePage.Click(objPage.getButtonWithText(objWorkItemHomePage.ConsolidateButton));
		  apasGenericObj.selectOptionFromDropDown("Select Primary",PrimaryWorkitem+" RP - CPI Factor");
		  objWorkItemHomePage.Click(objPage.getButtonWithText(objWorkItemHomePage.SaveButton));
          //steps 11: verify primary work item should be visible in in progress tab after consolidation
		  Thread.sleep(2000);
		  HashMap<String, ArrayList<String>> PrimaryWorkItems =objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_PROGRESS);
		  softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(PrimaryWorkitem),"SMAB-T2261: Validation that Primary WorkItem should be visible In In Progress Tab On Home page after consolidate " ); 
		  softAssert.assertTrue(!(PrimaryWorkItems.get("Work Item Number").contains(secondaryWorkitem)),"SMAB-T2261: Validation that Secondary WorkItem should not be visible In In Progress Tab On Home page after consolidate "  ); 
		  objWorkItemHomePage.openWorkItem(PrimaryWorkitem);
		  objPage.waitForElementToBeClickable(objWorkItemHomePage.ChildWorkItemsTab);
		  objWorkItemHomePage.Click(objWorkItemHomePage.ChildWorkItemsTab);
		  Thread.sleep(2000);
		  objWorkItemHomePage.openWorkItem(secondaryWorkitem);
		  objPage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		  objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		  Thread.sleep(2000);
		  //steps 12: validation of parent- child relationship and status on child work item
		  softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Request Type"),"RP - CPI Factor","SMAB-T2276 : request type matched RP - CPI Factor - Test WI");
		  softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Status"),"In Progress","SMAB-T2276 : Status Of Child WI Should be In Progress ");
		  softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Parent-Child Relationship Type"),"Consolidated Secondary","SMAB-T2276 : parent-Child Relationship Type Of Child WI Should be Consolidated Secondary");
		  driver.navigate().back();
		  //steps 13: validation of can not change status in progress to complete from  work item page 
		  objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.dataTabCompleted, 5);
		  objWorkItemHomePage.javascriptClick(objWorkItemHomePage.dataTabCompleted);
		  objWorkItemHomePage.javascriptClick(objWorkItemHomePage.markAsCurrentStatusButton);
		  //objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.ErrormsgOnWI, 20);
		  softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(),"Status: You cannot change status from In Progress to Completed","SMAB-T2287 : Error validation You cannot change status from In Progress to Completed On WI page" ); 
		  objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
		  driver.navigate().back();
		  objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		  objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
		  objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		  objWorkItemHomePage.Click(objWorkItemHomePage.submittedforApprovalTimeline);
		  //steps 14: validating primary work item should be visible on submitted for approval tab after click on mark complete button 
		  PrimaryWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		  softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(PrimaryWorkitem),"SMAB-T2276, SMAB-T2287: Validation that Primary WorkItem should be visible In Submitted for approval Tab On Home page after consolidate " ); 
		  objApasGenericFunctions.logout();
		  Thread.sleep(15000);
		  //Login With supervisor 1 with rp admin 
		  objApasGenericFunctions.login(loginUser);
		  objApasGenericFunctions.searchModule(modules.HOME);
		  objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		  objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
		  objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		  objWorkItemHomePage.Click(objWorkItemHomePage.submittedforApprovalTimeline);
		  //steps 15: Validation on after approve by first lavel approval work item should be visible in submited for approval tab
		  PrimaryWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		  softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(PrimaryWorkitem),"SMAB-T2288: Validation that Primary WorkItem should be visible In Submit For approval tab after first lavel of approval ");  
		  objApasGenericFunctions.logout();
		  Thread.sleep(15000);
          // Login supervisor 2 users.DATA_ADMIN
		  objApasGenericFunctions.login(users.DATA_ADMIN);
		  objApasGenericFunctions.searchModule(modules.HOME);
          Thread.sleep(2000);
		  objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		  objWorkItemHomePage.selectWorkItemOnHomePage(PrimaryWorkitem);
		  objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		  objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);
		  // steps 16 : Validation on after approve by second lavel approval work item should be visible in completed tab
	      PrimaryWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
          softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(PrimaryWorkitem), "SMAB-T2289: Validation that Primary WorkItem should be visible In completed Tab after after 2nd lavel of approval "); 
	      objApasGenericFunctions.logout();
     }
}
