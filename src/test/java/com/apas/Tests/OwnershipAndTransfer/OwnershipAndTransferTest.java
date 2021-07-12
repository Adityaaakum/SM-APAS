package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.generic.NEW;
import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AuditTrail;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.CioTransferScreen;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

import android.R.string;

public class OwnershipAndTransferTest extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();
	CIOTransferPage Cio;
	AuditTrail trail;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		Cio = new CIOTransferPage(driver);
		trail= new AuditTrail(driver);

	}
	
	
	@Test(description = "SMAB-T3106,SMAB-T3111:Verify the type of WI system creates for a recorded document with no APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","RecorderIntegration" })
	public void ParcelManagement_VerifyNewWIgenratedfromRecorderIntegrationForNOAPNRecordedDocument(String loginUser) throws Exception {
		
		String getApnToAdd="Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
  	  HashMap<String, ArrayList<String>> hashMapRecordedApn= salesforceAPI.select(getApnToAdd);
  	    String recordedAPN = hashMapRecordedApn.get("Name").get(0);
  	
			//login with sys admin
		   objMappingPage.login(users.SYSTEM_ADMIN);
		   objMappingPage.searchModule(PARCELS);
		   salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c where Sub_type__c='NO APN - CIO' and status__c ='In pool'", "status__c","In Progress");
		   Cio.generateRecorderJobWorkItems(objMappingPage.DOC_CERTIFICATE_OF_COMPLIANCE, 0);
			String WorkItemQuery="SELECT Id,name FROM Work_Item__c where Type__c='NO APN' AND Sub_type__c='NO APN - MAPPING'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";
			Thread.sleep(3000);			
	        String WorkItemNo=salesforceAPI.select(WorkItemQuery).get("Name").get(0);
	        objMappingPage.globalSearchRecords(WorkItemNo);
	        Thread.sleep(2000);
	        //User tries to close the WI in which no APN is added
            objWorkItemHomePage.Click(objWorkItemHomePage.dataTabCompleted);
            objWorkItemHomePage.Click(objWorkItemHomePage.markAsCurrentStatusButton);
	        softAssert.assertEquals(objWorkItemHomePage.getAlertMessage(),"Status: Work item status cannot be completed as related recorded APN(s) are not migrated yet.", "SMAB-T3106:Verifying User is not able to close WI Before migrating APN");
	        objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
	        //User tries to add the Recorded APN
	        objMappingPage.Click(objWorkItemHomePage.recordedAPNtab);
	        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.NewButton));
	        objWorkItemHomePage.enter(objWorkItemHomePage.apnLabel, recordedAPN);
			objWorkItemHomePage.selectOptionFromDropDown(objWorkItemHomePage.apnLabel, recordedAPN);
			
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.SaveButton));
			Thread.sleep(2000);
			driver.navigate().back();
			driver.navigate().back();
			//User clicks on Migrate button
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.migrateAPN));			
			Thread.sleep(2000);
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
			//User validates the status of added recorded APN
			softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0),"Processed", "SMAB-T3111: Validating that status of added APN is processed");
			//User tries to complete the WI
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);
			//User validates the status of the WI 
			softAssert.assertEquals(salesforceAPI.select("select status__c from work_item__c where name='"+WorkItemNo+"'").get("Status__c").get(0), "Completed", "SMAB-T3111: Validating that status of WI is completed");
			softAssert.assertEquals(salesforceAPI.select("SELECT Id,name FROM Work_Item__c where Type__c='MAPPING'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1").get("Name")!=null, true, "SMAB-T3111:Validating a new WI genrated as soon as New APN is processed.");
			objWorkItemHomePage.logout();
			
			
	}
			
	        
	
		
	

}