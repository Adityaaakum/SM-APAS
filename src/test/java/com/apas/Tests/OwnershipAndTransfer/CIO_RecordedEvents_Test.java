package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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


public class CIO_RecordedEvents_Test extends TestBase implements testdata, modules, users {
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
			"Regression","ChangeInOwnershipManagement","RecorderIntegration","Smoke" })
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
	
	
	/*
	 * Verify that User is able to add mail-to and navigate to back to WI using back button with validation on grantee ownership dates And able to perform partial transfer on CIO transfer screen
	 */
	
	@Test(description = "SMAB-T3427,SMAB-T3306,SMAB-T3446,SMAB-T3307,SMAB-T3308,SMAB-T3691:Verify that User is able to perform partial transfer and able to create mail to records ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","OwnershipAndTransfer","Smoke" })
	public void OwnershipAndTransfer_VerifyPartialOwnershipTransfer(String loginUser) throws Exception {
		
		  String execEnv= System.getProperty("region");		
  	    
  	      String OwnershipAndTransferCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
	       Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData,
				"dataToCreateMailToRecordsWithIncompleteData");
	  
		  String OwnershipAndTransferGranteeCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		  Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData,
				"dataToCreateGranteeWithIncompleteData");
		  
		  Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData,
	                "DataToCreateOwnershipRecord");
  	    
		   String recordedDocumentID=salesforceAPI.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1").get("Id").get(0);
		   objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);
		  
			 // STEP 1-login with SYS-ADMIN
		  
           objMappingPage.login(users.SYSTEM_ADMIN);
           objCioTransfer.addRecordedApn(recordedDocumentID, 1);
  	       objCioTransfer.deleteOldGranteesRecords(recordedDocumentID);
		   
		   salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c","In Progress");
		   objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);
		    
		    //  STEP 2-Query to fetch WI
		   
			String workItemQuery="SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";					
	        String workItemNo=salesforceAPI.select(workItemQuery).get("Name").get(0);
	        objMappingPage.searchModule("APAS");
	        objMappingPage.globalSearchRecords(workItemNo);	
	        String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
	        objCioTransfer.deleteOwnershipFromParcel(salesforceAPI.select("Select Id from parcel__c where name='"+apnFromWIPage+"'").get("Id").get(0));
	        
	         //STEP 3- adding owner after deleting for the recorded APN 
	        
	        String acesseName= objMappingPage.getOwnerForMappingAction();	        
	        driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Parcel__c/"+salesforceAPI.select("Select Id from parcel__C where name='"+apnFromWIPage+"'").get("Id").get(0)+"/related/Property_Ownerships__r/view");
	        objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
	        String ownershipId = driver.getCurrentUrl().split("/")[6];
	        
	        //STEP 4- updating the ownership date for current owners
	       
	        String dateOfEvent= salesforceAPI.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '"+ownershipId+"'").get("Ownership_Start_Date__c").get(0);      
		    jsonObject.put("DOR__c",dateOfEvent);
		    jsonObject.put("DOV_Date__c", dateOfEvent);
			salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);
			             
	        objMappingPage.logout();
	        
	        // STEP 5-Login with CIO staff
	        
	        objMappingPage.login(loginUser);
	        objMappingPage.globalSearchRecords(workItemNo);
	        Thread.sleep(5000);
	        String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
			 HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);	
			 
			 // STEP 6-Finding the recorded apn transfer id
			 
			 String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
	        objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
	        objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);	        
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			
			//STEP 7-Clicking on related action link	
			
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow=driver.getWindowHandle();				  				
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			softAssert.assertContains(driver.getCurrentUrl(),navigationUrL.get("Navigation_Url__c").get(0),"SMAB-T3306:Validating that user navigates to CIo transfer screenafter clicking on related action hyperlink");			
			
			//STEP 8-Creating the new grantee
			 
			objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			
			
			//STEP 9-Validating that grantees combined cannot have ownership more than 100%
			
			driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+"/related/CIO_Transfer_Grantee_New_Ownership__r/view");
			objCioTransfer.waitForElementToBeVisible(5,objCioTransfer.newButton);
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
			objCioTransfer.enter(objCioTransfer.ownerPercentage,"120");
			objCioTransfer.enter(objCioTransfer.LastNameLabel, hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));	
			softAssert.assertContains(objCioTransfer.saveRecordAndGetError(),"The sum of all grantee ownership percentage is more than 100. Please check and make the correction","SMAB-T3427: Verify user is not able to save grantee  record with combined ownership perentage of more than 100%");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));
			
			//STEP 10-Validating present grantee			 
			 
			 driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+"/related/CIO_Transfer_Grantee_New_Ownership__r/view");
	          HashMap<String, ArrayList<String>> granteeHashMap  = objCioTransfer.getGridDataForRowString("1");
	         String granteeForMailTo= granteeHashMap.get("Grantee/Retain Owner Name").get(0);
	         String ownershipDovForNewGrantee=granteeHashMap.get("DOV").get(0);
	         	          
	         //STEP 11- Performing calculate ownership to perform partial transfer
	         
	         driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
	         objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
	         objCioTransfer.waitForElementToBeVisible(5,objCioTransfer.nextButton);
	         objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
	         objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
	         
	         	         
	         // STEP 12-Creating copy to mail to record
	   
	         objCioTransfer.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
	         objCioTransfer.waitForElementToBeClickable(7, objCioTransfer.copyToMailToButtonLabel);
	       
	        //STEP 13-Validating mail to record created from copy to mail to
	       
	         driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
	         objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.newButton);
	       
	         HashMap<String, ArrayList<String>> hashMapcopyTomailTo  = objCioTransfer.getGridDataForRowString("1");
	       
	         //STEP 14-Validating the formatted name 1 for mail to record
	       
             softAssert.assertEquals(hashMapcopyTomailTo.get(objCioTransfer.formattedName1Label).get(0),granteeForMailTo,"SMAB-T3307:Validating that CIO copyTo Mail to record is created ");
           
             //STEP 15-Navigating back to RAT screen
           
            driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
            objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
            objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
           
             // STEP 16-Clicking on submit for approval quick action button
           
            objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
            objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
            ReportLogger.INFO("CIO!! Transfer submitted for approval");
           
             //STEP 17- Since new ownership records are created we are validating them 
           
            driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Parcel__c/"+salesforceAPI.select("Select Id from parcel__C where name='"+apnFromWIPage+"'").get("Id").get(0)+"/related/Property_Ownerships__r/view");
            HashMap<String, ArrayList<String>>HashMapLatestOwner  = objCioTransfer.getGridDataInHashMap();
            
              //STEP 18-Validating the Owners ,their status and ownership percentages
            
            softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), granteeForMailTo, "SMAB-T3446:Validating that the grantee has become  owner");
            softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active", "SMAB-T3446: Validating that status of new owner is Active");
            softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(1), acesseName, "SMAB-T3446:Validating that the grantee has become active owner");
            softAssert.assertEquals(HashMapLatestOwner.get("Status").get(1), "Active", "SMAB-T3446: Validating that status of old owner is Active");
            softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "50.0000%", "SMAB-T3446:Validating that new owner has percentage of 50");
            softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(1),"50.0000%", "SMAB-T3446:Validating that old owner has percentage of 50");
            softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(2), acesseName, "SMAB-T3446:Validating that the grantor has become active owner");
            softAssert.assertEquals(HashMapLatestOwner.get("Status").get(2), "Retired", "SMAB-T3446: Validating that status of old owner is Retired");
            softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(2), "100.0000%", "SMAB-T3446:Validating that retired owner had percentage of 100");
            softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),ownershipDovForNewGrantee , "SMAB-T3691: Validating that Ownership start date of new owner is DOV of the recorded document by default.");
            softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(1),hashMapCreateOwnershipRecordData.get("Ownership Start Date") , "SMAB-T3691: Validating that Ownership start date of old owner remains same as before the partial transfer");
            
            //STEP 19-Navigating back to RAT screen and clicking on back quick action button
            
            driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
            objCioTransfer.waitForElementToBeClickable(5,objCioTransfer.quickActionButtonDropdownIcon);	          
            objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
            objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
            objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
           
           //STEP 20-Validating that back button has navigates the user to WI page.
           
           softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Action"),"Process Transfer & Ownership", "SMAB-T3308: Validating that Back button navigates back to WI page ");
           objCioTransfer.logout();
           

		        
		        

			   
	}
			
	        
	
		
	

}