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
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
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

public class OwnershipAndTransfer_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();
	CIOTransferPage objCioTransfer;
	AuditTrailPage trail;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objCioTransfer = new CIOTransferPage(driver);
		trail= new AuditTrailPage(driver);

	}
	/*
	 * Verify that NO APN WI is genrated for document without APN and user has the ability to add recorded APN on it to create a WI for MAPPING OR CIO
	 * 
	 */
	
	@Test(description = "SMAB-T3106,SMAB-T3111:Verify the type of WI system created for a recorded document with no APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","RecorderIntegration" })
	public void RecorderIntegration_VerifyNewWIgeneratedfromRecorderIntegrationForNOAPNRecordedDocument(String loginUser) throws Exception {
		
		String getApnToAdd="Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
  	  HashMap<String, ArrayList<String>> hashMapRecordedApn= salesforceAPI.select(getApnToAdd);
  	    String recordedAPN = hashMapRecordedApn.get("Name").get(0);
  	
			//login with sys admin
  	    
		   objMappingPage.login(users.SYSTEM_ADMIN);
		   objMappingPage.searchModule(PARCELS);
		   salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c where Sub_type__c='NO APN - CIO' and status__c ='In pool'", "status__c","In Progress");
		   objCioTransfer.generateRecorderJobWorkItems(objMappingPage.DOC_CERTIFICATE_OF_COMPLIANCE, 0);
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
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed", "SMAB-T3111:Validating that status of WI is completed");
			softAssert.assertEquals(salesforceAPI.select("SELECT Id,name FROM Work_Item__c where Type__c='MAPPING'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1").get("Name")!=null, true, "SMAB-T3111:Validating a new WI genrated as soon as New APN is processed.");
			objWorkItemHomePage.logout();
			
			
	}
	
	
	/*
	 * Verify that User is unable to add mail-to and grantee records having end date prior to start date in recorded APN transfer screen.	  
	 */
	
	@Test(description = "SMAB-T3279,SMAB-T3281:Verify that User is not able to enter end date less than start date for mail to and grantee records in CIO transfer", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","OwnershipAndTransfer" })
	public void OwnershipAndTransfer_VerifyValidationofMailToAndGranteeRecords(String loginUser) throws Exception {
		
		  String execEnv= System.getProperty("region");		
  	    
  	      String OwnershipAndTransferCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
	       Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData,
				"dataToCreateMailToRecordsWithIncompleteData");
	  
		  String OwnershipAndTransferGranteeCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		  Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData,
				"dataToCreateGranteeWithIncompleteData");
  	    
  	
			//login with CIO STAFF
		  
           objMappingPage.login(loginUser);
		   objMappingPage.searchModule(PARCELS);
		   salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c","In Progress");
		   objCioTransfer.generateRecorderJobWorkItems(objCioTransfer.DOC_DEED, 1);
		    
		   //Query to fetch WI
		   
			String workItemQuery="SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";					
	        String workItemNo=salesforceAPI.select(workItemQuery).get("Name").get(0);
	        objMappingPage.globalSearchRecords(workItemNo);	
	        Thread.sleep(5000);
	        objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
	        objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);	        
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			
			//Clicking on related action link			
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow=driver.getWindowHandle();				  				
			objWorkItemHomePage.switchToNewWindow(parentWindow);			
			
			//Finding the RAT ID
			String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
			HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);			   
			   String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
			 
			 //Navigating to mail to screen	   
		   driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
		    //Creating mail to record
		  
		   objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
		   objCioTransfer.enter(objCioTransfer.formattedName1Label, hashMapOwnershipAndTransferCreationData.get("Formatted Name1"));
		   objCioTransfer.enter(objCioTransfer.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
		   objCioTransfer.enter(objCioTransfer.endDate, "7/15/2021");
		   objCioTransfer.enter(objCioTransfer.mailingZip,hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));		   
		   softAssert.assertContains(objCioTransfer.saveRecordAndGetError(),"Start Date","SMAB-T3279: Verify user is not able to save mail to record with enddate less than start date");
	       objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));
	       
	       //Creating mail to record with correct data
	       
	       objCioTransfer.waitForElementToBeClickable(objCioTransfer.newButton, 3);
           objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
	       objCioTransfer.enter(objCioTransfer.formattedName1Label, hashMapOwnershipAndTransferCreationData.get("Formatted Name1"));
	       objCioTransfer.enter(objCioTransfer.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
	       objCioTransfer.enter(objCioTransfer.endDate,  hashMapOwnershipAndTransferCreationData.get("Start Date"));
		   objCioTransfer.enter(objCioTransfer.mailingZip,hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));
		   objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		   objCioTransfer.waitForElementToBeVisible(3,objCioTransfer.formattedName1Label );		  
		   softAssert.assertContains( objCioTransfer.getFieldValueFromAPAS(objCioTransfer.formattedName1Label),hashMapOwnershipAndTransferCreationData.get("Formatted Name1"),"SMAB-T3279: Verify user is  able to save mail to record with enddate greater than start date");
		  
		   //Navigating to grantee scren
		   
		   driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+"/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		   
		   objCioTransfer.waitForElementToBeVisible(5,objCioTransfer.newButton);
		   objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
		   objCioTransfer.enter(objCioTransfer.LastNameLabel, hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));
		   objCioTransfer.enter(objCioTransfer.OwnershipStartDate, hashMapOwnershipAndTransferGranteeCreationData.get("Ownership Start Date"));
		   objCioTransfer.enter(objCioTransfer.OwnershipEndDate, "7/15/2021");
		   objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		   softAssert.assertContains( objCioTransfer.getFieldValueFromAPAS(objCioTransfer.LastNameLabel),hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),"SMAB-T3281: Verify user is  able to save mail to record with enddate greater than start date,as by default DOR is taken as a ownership start date");
		   softAssert.assertContains( objCioTransfer.getFieldValueFromAPAS(objCioTransfer.Status),"Active","SMAB-T3281: Verifying that status of grantee is active");
		   
		   //Editing the grantee record to make ownership end date lesser than ownership start date
		   
		   objCioTransfer.waitForElementToBeVisible(5,objCioTransfer.Edit);
		   objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		   objCioTransfer.enter(objCioTransfer.OwnershipStartDate, hashMapOwnershipAndTransferGranteeCreationData.get("Ownership Start Date"));
		   
		   softAssert.assertContains(objCioTransfer.saveRecordAndGetError(),"Start Date","SMAB-T3281: Verify user is not able to save grantee  record with ownership enddate less than ownership start date");
		   objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));
		
		   //logging out
		   objCioTransfer.logout();
		   
		   
		   
		   

}
			
	        
	
		
	

}