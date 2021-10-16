package com.apas.Tests.OwnershipAndTransfer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;

import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.google.gson.JsonObject;

public class CIO_RecordedEvents_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	BuildingPermitPage objBuildingPermitPage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject = new JSONObject();
	Page objPage;
	String apnPrefix = new String();
	CIOTransferPage objCioTransfer;
	AuditTrailPage trail;
	String ownershipCreationData;
	String OwnershipAndTransferCreationData;
	String MailtoData;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		objPage= new Page(driver);
		objCioTransfer = new CIOTransferPage(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver) ;
		trail = new AuditTrailPage(driver);
		ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		MailtoData = testdata.MAILTO_RECORD_DATA_PARCEL;
		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

	}
	/*
	 * Verify that NO APN WI is genrated for document without APN and user has the
	 * ability to add recorded APN on it to create a WI for MAPPING OR CIO
	 * 
	 */

	@Test(description = "SMAB-T3763,SMAB-T3106,SMAB-T3111:Verify the type of WI system created for a recorded document with no APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration", "Smoke" }, enabled = false)
	public void RecorderIntegration_VerifyNewWIgeneratedfromRecorderIntegrationForNOAPNRecordedDocument(
			String loginUser) throws Exception {

		String getApnToAdd = "Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(getApnToAdd);
		String recordedAPN = hashMapRecordedApn.get("Name").get(0);

		// login with sys admin

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);

		objCioTransfer.generateRecorderJobWorkItems(objMappingPage.DOC_CERTIFICATE_OF_COMPLIANCE, 0);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='NO APN' AND Sub_type__c='NO APN - MAPPING'  And status__c='In pool' order by createdDate desc limit 1";
		String WorkItemNo = salesforceAPI.select(WorkItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(WorkItemNo);

		// adding steps for SMAB-T3763
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.reviewLink), "NO APN - MAPPING",
				"SMAB-T3763: Validation that Related action link should be visible for NO -APN Mapping WI");

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		softAssert.assertEquals(
				objMappingPage.getElementText(
						objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel)),
				"",
				"SMAB-T3763: Validate the APN value in Parent APN field in mapping actions page is blank for NO APN - MAPPING scenario ");

		driver.switchTo().window(parentWindow);

		// User tries to close the WI in which no APN is added

		objWorkItemHomePage.Click(objWorkItemHomePage.dataTabCompleted);
		objWorkItemHomePage.Click(objWorkItemHomePage.markAsCurrentStatusButton);
		softAssert.assertEquals(objWorkItemHomePage.getAlertMessage(),
				"Status: Work item status cannot be completed as related recorded APN(s) are not migrated yet.",
				"SMAB-T3106:Verifying User is not able to close WI Before migrating APN");
		objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
		// User tries to add the Recorded APN
		objMappingPage.Click(objWorkItemHomePage.recordedAPNtab);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.NewButton));
		objWorkItemHomePage.enter(objWorkItemHomePage.apnLabel, recordedAPN);
		objWorkItemHomePage.selectOptionFromDropDown(objWorkItemHomePage.apnLabel, recordedAPN);

		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.SaveButton));
		Thread.sleep(2000);
		driver.navigate().back();
		driver.navigate().back();
		// User clicks on Migrate button
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.migrateAPN));
		Thread.sleep(2000);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);

		// User validates the status of added recorded APN
		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0), "Processed",
				"SMAB-T3111: Validating that status of added APN is processed");

		// User tries to complete the WI
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);

		// User validates the status of the WI
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
				"SMAB-T3111:Validating that status of WI is completed");
		softAssert.assertEquals(salesforceAPI.select(
				"SELECT Id,name FROM Work_Item__c where Type__c='MAPPING'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1")
				.get("Name") != null, true, "SMAB-T3111:Validating a new WI genrated as soon as New APN is processed.");
		objWorkItemHomePage.logout();

	}

	/*
	 * Verify that User is unable to add mail-to and grantee records having end date
	 * prior to start date in recorded APN transfer screen.
	 */

	@Test(description = "SMAB-T3279,SMAB-T3281:Verify that User is not able to enter end date less than start date for mail to and grantee records in CIO transfer", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void OwnershipAndTransfer_VerifyValidationofMailToAndGranteeRecords(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		// login with CIO STAFF

		objMappingPage.login(loginUser);			
		objCioTransfer.generateRecorderJobWorkItems(objCioTransfer.DOC_DEED, 1);

		// Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(workItemNo);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.inProgressOptionInTimeline,10);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// Clicking on related action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Finding the RAT ID
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);
		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		
		//Deleting old mail to records
		
		objCioTransfer.deleteOldMailToRecords(recordeAPNTransferID);

		// Navigating to mail to screen
		
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "" + "/related/CIO_Transfer_Mail_To__r/view");
		
		// Creating mail to record

		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
		objCioTransfer.enter(objCioTransfer.formattedName1Label,
				hashMapOwnershipAndTransferCreationData.get("Formatted Name1"));
		objCioTransfer.enter(objCioTransfer.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
		objCioTransfer.enter(objCioTransfer.endDate, "7/15/2021");
		objCioTransfer.enter(objCioTransfer.mailingZip, hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));
		softAssert.assertContains(objCioTransfer.saveRecordAndGetError(), "Start Date",
				"SMAB-T3279: Verify user is not able to save mail to record with enddate less than start date");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));

		// Creating mail to record with correct data

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.newButton, 3);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
		objCioTransfer.waitForElementToBeVisible(10,objCioTransfer.formattedName1Label);
		objCioTransfer.enter(objCioTransfer.formattedName1Label,
				hashMapOwnershipAndTransferCreationData.get("Formatted Name1"));
		objCioTransfer.enter(objCioTransfer.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
		objCioTransfer.enter(objCioTransfer.endDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
		objCioTransfer.enter(objCioTransfer.mailingZip, hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		objCioTransfer.waitForElementToBeVisible(3, objCioTransfer.formattedName1Label);
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.formattedName1Label),
				hashMapOwnershipAndTransferCreationData.get("Formatted Name1"),
				"SMAB-T3279: Verify user is  able to save mail to record with enddate greater than start date");

		// Navigating to grantee scren

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");

		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.newButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
		objCioTransfer.enter(objCioTransfer.LastNameLabel,
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));		
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		objCioTransfer.waitForElementToBeVisible(10,objCioTransfer.LastNameLabel);
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.LastNameLabel),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
				"SMAB-T3281: Verify user is  able to save grantee record with enddate greater than start date,as by default DOR is taken as a ownership start date");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.Status), "Active",
				"SMAB-T3281: Verifying that status of grantee is active");

		// Editing the grantee record to make ownership end date lesser than ownership
		// start date

		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.OwnershipEndDate);
		objCioTransfer.enter(objCioTransfer.OwnershipStartDate,
				hashMapOwnershipAndTransferGranteeCreationData.get("Ownership Start Date"));
		objCioTransfer.enter(objCioTransfer.OwnershipEndDate,
				"7/15/2021");

		softAssert.assertContains(objCioTransfer.saveRecordAndGetError(), "Start Date",
				"SMAB-T3281: Verify user is not able to save grantee  record with ownership enddate less than ownership start date");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));

		// logging out
		objCioTransfer.logout();

	}

	/*
	 * Verify that User is able to add mail-to and navigate to back to WI using back
	 * button with validation on grantee ownership dates And able to perform partial
	 * transfer on CIO transfer screen
	 */

	@Test(description = "SMAB-T3427,SMAB-T3306,SMAB-T3446,SMAB-T3307,SMAB-T3308,SMAB-T3691,SMAB-T3162,SMAB-T3164,SMAB-T3165,SMAB-T3166,SMAB-T3207:Verify that User is able to perform partial transfer and able to create mail to records ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration", "Smoke" }, enabled = true)
	public void OwnershipAndTransfer_VerifyPartialOwnershipTransfer(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
		String recorderTransferTax = "2612.50";
		String recorderConvTax = "11875.00";
		String pcorExit;

		JSONObject jsonForPartialTransfer = objCioTransfer.getJsonObject();
		
		      String situsId = salesforceAPI.select("SELECT id FROM Situs__c where name !=null").get("Id").get(0);
		      String pucId  = salesforceAPI.select("SELECT Id FROM PUC_Code__c where Legacy__c='No' AND  NAME !='99-RETIRED PARCEL'").get("Id").get(0);
		      JSONObject jsonForParcelValidation =objCioTransfer.getJsonObject();
		      jsonForParcelValidation.put("Primary_Situs__c", situsId);
		      jsonForParcelValidation.put("PUC_Code_Lookup__c", pucId);
		      jsonForParcelValidation.put("Short_Legal_Description__c", "Test Legal Description");
		      

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String recordedDocumentID = salesforceAPI
				.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);

		HashMap<String, ArrayList<String>> hashMapRecordedDocumentData = salesforceAPI.select(
				"Select Name,PCOR_Exits__c,xAPN_Count__c,Recording_Date__c,Recorder_Doc_Type__c  from recorded_document__c where id='"
						+ recordedDocumentID + "'");

		// Formatting date and Pcor exist from recorded document

		String[] dateOfRecordingfromHashMap = hashMapRecordedDocumentData.get("Recording_Date__c").get(0).split("-");
		String dateofRecordingAfterConversion = dateOfRecordingfromHashMap[1].replace("0", "") + "/"
				+ dateOfRecordingfromHashMap[2].replace("0", "") + "/" + dateOfRecordingfromHashMap[0];
		if (hashMapRecordedDocumentData.get("PCOR_Exits__c").get(0) == "true") {
			pcorExit = "Yes";
		} else
			pcorExit = "No";

		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		Thread.sleep(3000);
		objCioTransfer.addRecordedApn(recordedDocumentID, 1);
		

		// Step 1a -Updating transfer and SM tax for the recorded document

		jsonForPartialTransfer.put("Recorder_Conv_Tax__c", recorderConvTax);
		jsonForPartialTransfer.put("Recorder_Transfer_Tax__c", recorderTransferTax);
		salesforceAPI.update("Recorded_Document__c", recordedDocumentID, jsonForPartialTransfer);
		jsonForPartialTransfer.remove("Recorder_Conv_Tax__c");
		jsonForPartialTransfer.remove("Recorder_Transfer_Tax__c");

		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);

		objMappingPage.globalSearchRecords(workItemNo);

		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.ApnLabel, 5);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objCioTransfer.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the recorded APN

		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		// STEP 4- updating the ownership date for current owners

		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		jsonForPartialTransfer.put("DOR__c", dateOfEvent);
		jsonForPartialTransfer.put("DOV_Date__c", dateOfEvent);

		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonForPartialTransfer);
		salesforceAPI.update("Parcel__c",salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0), jsonForParcelValidation);

		objMappingPage.logout();

		// STEP 5-Login with CIO staff

		objMappingPage.login(loginUser);
		objMappingPage.waitForElementToBeClickable(objMappingPage.appLauncher,10);
		objCioTransfer.searchModule(modules.EFILE_INTAKE);
		objMappingPage.globalSearchRecords(workItemNo);
		Thread.sleep(5000);
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the recorded apn transfer id

		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertContains(driver.getCurrentUrl(), navigationUrL.get("Navigation_Url__c").get(0),
				"SMAB-T3306:Validating that user navigates to CIo transfer screenafter clicking on related action hyperlink");

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon, 10);

		// STEP 8 - Verifying fields on Left side of transfer screen

		  
		
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.ApnLabel), apnFromWIPage,
				"SMAB-T3164: Verify that APN field has same apn value from Work Item page");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.documentTypeLabel),
				hashMapRecordedDocumentData.get("Recorder_Doc_Type__c").get(0),
				"SMAB-T3206: Verifying that recorded document type from recorded document is populated in Document field");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.eventIDLabel),
				hashMapRecordedDocumentData.get("Name").get(0),
				"SMAB-T3162: Verifying that Event Id field of Transfer screen is same as document name of recorded document for CIO tranfer");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.apnCountLabel),
				(hashMapRecordedDocumentData.get("xAPN_Count__c").get(0)).substring(0, 1),
				"SMAB-T3165:Verifying that APN count fileds indicates the no of recorded APN's associated to the recorded document");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.dorLabel),
				dateofRecordingAfterConversion,
				"SMAB-T3166:Verifying that DOR field is defaulted to Date of recording of recorded document");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.dovLabel),
				dateofRecordingAfterConversion, "SMAB-T3166: Verify that DOV is defaulted to DOR");

		objCioTransfer.editRecordedApnField(objCioTransfer.doeLabel);

		objCioTransfer.enter(objCioTransfer.doeLabel,
				hashMapOwnershipAndTransferGranteeCreationData.get("IncorrectDOV"));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.doeLabel),
				hashMapOwnershipAndTransferGranteeCreationData.get("IncorrectDOV"),
				"SMAB-T3166:Verifying that DOE fied is editable ");

		objCioTransfer.editRecordedApnField(objCioTransfer.dovLabel);

		objCioTransfer.enter(objCioTransfer.dovLabel,
				hashMapOwnershipAndTransferGranteeCreationData.get("IncorrectDOV"));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		
		
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.errorMessageOnTransferScreen),
				"You cannot enter date for DOV later than DOR.",
				"SMAB-T3166:Verifying that DOV cannot be later than DOR for recorded document");

		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferTaxLabel),
				"$" + recorderTransferTax.substring(0, 1) + "," + recorderTransferTax.substring(1),
				"SMAB-T3206:Verifying that recorder transfer tax of recorded document is transfer tax of the transfer screen");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.valueFromDocTaxLabel),
				"$2,375,000.00", "SMAB-T3206: Verify that DOC TAX of CIO transfer equals ( $Transfer Tax / 0.0011)");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.cityOfSmTaxLabel),
				"$" + recorderConvTax.substring(0, 2) + "," + recorderConvTax.substring(2),
				"SMAB-T3206: Verifying that recorder conv tax equals City of SM Tax ");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.valueFromDocTaxCityLabel),
				"$2,375,000.00", "SMAB-T3206: Verifying that Doc Tax(City) equals (City of SM Tax / 0.005) ");

		ReportLogger.INFO("Add the Transfer Code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel,
				objCioTransfer.CIO_EVENT_CODE_COPAL);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		
       Thread.sleep(5000);

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferCodeLabel),
				objCioTransfer.CIO_EVENT_CODE_COPAL, "SMAB-T3207: Verify that Transfer code is a lookup field");
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS("PCOR?"), pcorExit,
				"SMAB-T3206: Verifying that User should view this information from the recorder feed. Yes or No");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.exemptionRetainLabel), "No",
				"SMAB-T3206: Verifying that exemption retain is a formula field and  pulls value from Event Library Exemption Retain field");

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferDescriptionLabel),
				"Non Sale Ptl%,- Corp Letter Sent",
				" SMAB-T3206:  Verifying that the description of the above event code as stored in the event library object");

		softAssert.assertEquals(objCioTransfer.verifyElementVisible(objCioTransfer.remarksLabel), true,
				"SMAB-T3206: Verifying that remarks field is visible on CIO transfer screen");

		objCioTransfer.editRecordedApnField(objCioTransfer.remarksLabel);
		objCioTransfer.enter(objCioTransfer.remarksLabel, "Test remarks");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.remarksLabel), "Test remarks",
				"SMAB-T3206: Verifying that remarks field is editable");
		softAssert.assertEquals(objCioTransfer.verifyElementVisible(objCioTransfer.createdByLabel), true,
				"SMAB-T3206: Verifying that Created By field is visible on CIO transfer screen");
		softAssert.assertEquals(objCioTransfer.verifyElementVisible(objCioTransfer.lastModifiedByLabel), true,
				"SMAB-T3206: Verifying that Last Modified By field is visible on CIO transfer screen");
		softAssert.assertEquals(objCioTransfer.verifyElementVisible(objCioTransfer.transferStatusLabel), true,
				"SMAB-T3206: Verifying that CIO Transfer Status field is visible on CIO transfer screen");
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.situsLabel), salesforceAPI.select("Select name from Situs__C where id ='"+situsId+"'").get("Name").get(0),
				"SMAB-T3206: Verifying that correct situs field value of parcel is getting reflected in RAT screen");
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.pucCodeLabel), salesforceAPI.select("Select name from PUC_Code__c where id ='"+pucId+"'").get("Name").get(0),
				"SMAB-T3206: Verifying that correct PUC field value of parcel is getting reflected in RAT screen");
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.shortLegalDescriptionLabel), salesforceAPI.select("Select Short_Legal_Description__c from Parcel__c where id ='"+salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0)+"'").get("Short_Legal_Description__c").get(0),
				"SMAB-T3206: Verifying that correct Short Legal description field value of parcel is getting reflected in RAT screen");

		// STEP 8-Creating the new grantee

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);

		// STEP 9-Validating that grantees combined cannot have ownership more than 100%

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.newButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.newButton));
		objCioTransfer.enter(objCioTransfer.ownerPercentage, "120");
		objCioTransfer.enter(objCioTransfer.LastNameLabel,
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));
		softAssert.assertContains(objCioTransfer.saveRecordAndGetError(),
				"The sum of all grantee ownership percentage is more than 100. Please check and make the correction",
				"SMAB-T3427: Verify user is not able to save grantee  record with combined ownership perentage of more than 100%");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));

		// STEP 10-Validating present grantee

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataForRowString("1");
		String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);
		String ownershipDovForNewGrantee = granteeHashMap.get("DOV").get(0);

		// STEP 11- Performing calculate ownership to perform partial transfer

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);
		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));

		// STEP 12-Creating copy to mail to record

		objCioTransfer.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCioTransfer.waitForElementToBeClickable(7, objCioTransfer.copyToMailToButtonLabel);

		// STEP 13-Validating mail to record created from copy to mail to

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "" + "/related/CIO_Transfer_Mail_To__r/view");
		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.newButton);

		HashMap<String, ArrayList<String>> hashMapcopyTomailTo = objCioTransfer.getGridDataInHashMap();

		// STEP 14-Validating the formatted name 1 for mail to record

		softAssert.assertTrue(hashMapcopyTomailTo.get(objCioTransfer.formattedName1Label).contains(granteeForMailTo),
				"SMAB-T3307:Validating that CIO copyTo Mail to record is created ");

		// STEP 15-Navigating back to RAT screen

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 16-Clicking on submit for approval quick action button

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		ReportLogger.INFO("CIO!! Transfer submitted for approval");

		// STEP 17- Since new ownership records are created we are validating them

		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();

		// STEP 18-Validating the Owners ,their status and ownership percentages

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), granteeForMailTo,
				"SMAB-T3446:Validating that the grantee has become  owner");
		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active",
				"SMAB-T3446: Validating that status of new owner is Active");
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(1), acesseName,
				"SMAB-T3446:Validating that the grantee has become active owner");
		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(1), "Active",
				"SMAB-T3446: Validating that status of old owner is Active");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "50.0000%",
				"SMAB-T3446:Validating that new owner has percentage of 50");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(1), "50.0000%",
				"SMAB-T3446:Validating that old owner has percentage of 50");
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(2), acesseName,
				"SMAB-T3446:Validating that the grantor has become active owner");
		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(2), "Retired",
				"SMAB-T3446: Validating that status of old owner is Retired");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(2), "100.0000%",
				"SMAB-T3446:Validating that retired owner had percentage of 100");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0), ownershipDovForNewGrantee,
				"SMAB-T3691: Validating that Ownership start date of new owner is DOV of the recorded document by default.");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(1),
				hashMapCreateOwnershipRecordData.get("Ownership Start Date"),
				"SMAB-T3691: Validating that Ownership start date of old owner remains same as before the partial transfer");

		// STEP 19-Navigating back to RAT screen and clicking on back quick action //

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 20-Validating that back button has navigates the user to WI page.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Action"), "Process Transfer & Ownership",
				"SMAB-T3308: Validating that Back button navigates back to WI page ");
		objCioTransfer.logout();

	}

	/*
	 * This test method verifies that user is able to manually initiate the auto
	 * approve process , if response has come back within 45 days of wait period.
	 * 
	 */

	@Test(description = "SMAB-T3377,SMAB-T10081:Verify that User is able to perform CIO transfer autoconfirm when some response do come back with in 45 days wait period", dataProvider = "dpForCioAutoConfirm", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void OwnershipAndTransfer_VerifyCioTransferAutoConfirm(String InitialEventCode, String finalEventCode,
			String response) throws Exception {
		
		JSONObject jsonForAutoConfirm = objCioTransfer.getJsonObject();

		String execEnv = System.getProperty("region");

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String dataToCreateCorrespondenceEventForAutoConfirm = testdata.UNRECORDED_EVENT_DATA;
		Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
				dataToCreateCorrespondenceEventForAutoConfirm, "DataToCreateCorrespondenceEventForAutoConfirm");

		String recordedDocumentID = salesforceAPI
				.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);
		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		Thread.sleep(3000);		
		objCioTransfer.addRecordedApn(recordedDocumentID, 1);		
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(workItemNo);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objCioTransfer.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the recorded APN

		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		// STEP 4- updating the ownership date for current owners

		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		jsonForAutoConfirm.put("DOR__c", dateOfEvent);
		jsonForAutoConfirm.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonForAutoConfirm);

		objMappingPage.logout();

		// STEP 5-Login with CIO staff

		objMappingPage.login(users.CIO_STAFF);
		objMappingPage.searchModule(modules.EFILE_INTAKE);
		objMappingPage.globalSearchRecords(workItemNo);
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the recorded apn transfer id

		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
		Thread.sleep(5000);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// STEP 8-Creating the new grantee
		objCioTransfer.waitForElementToBeVisible(objCioTransfer.quickActionButtonDropdownIcon,10);
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		Thread.sleep(2000);
		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataForRowString("1");
		String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(objCioTransfer.quickActionButtonDropdownIcon,10);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);
		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));

		// STEP 9-create new mail to record

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCioTransfer.waitForElementToBeClickable(7, objCioTransfer.copyToMailToButtonLabel);

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		ReportLogger.INFO("Add the Transfer Code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, InitialEventCode);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		// Step 9(a): Creating Outbound Event

		objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);
		String urlForTransactionTrail = driver.getCurrentUrl();
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

		// Step 10 : Submitting for review

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.clickQuickActionButtonOnTransferActivity(null,objCioTransfer.quickActionOptionSubmitForReview);

		// STEP 11-Clicking on submit for Review quick action button

		
		ReportLogger.INFO("CIO!! Transfer submitted for Review");
		objCioTransfer.waitForElementToBeVisible(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5),5);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5)),
				"CIO transfer initial determination is submitted for review.",
				"SMAB-T3377,SMAB-T10081:Cio trasnfer is submited for review");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		objCioTransfer.logout();

		// Step-12: Login with CIO supervisor

		objCioTransfer.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon,10);
		
		objCioTransfer.clickQuickActionButtonOnTransferActivity(null,objCioTransfer.quickActionOptionReviewComplete);
		ReportLogger.INFO("CIO!! Transfer Review Completed");
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5)),
				"CIO transfer initial determination review completed.",
				"SMAB-T3377,SMAB-T10081:Cio trasnfer review is completed");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		objCioTransfer.logout();

		// Step 13: If Response comes back within 45 days and no issues are reported.

		if (response.equalsIgnoreCase("No Edits required")) {

			objCioTransfer.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatusLabel);
			objCioTransfer.editRecordedApnField(objCioTransfer.transferStatusLabel);
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatusLabel);
			objCioTransfer.Click(objCioTransfer.getWebElementWithLabel(objCioTransfer.transferStatusLabel));

			// Clicking on review acesse picklist to manually approve the transfer

			objCioTransfer.javascriptClick(objCioTransfer.reviewAssecesseLink);
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

			// Verifying the status of transfer
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatusLabel);
			softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferStatusLabel),
					"Approved", "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferCodeLabel),
					finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.clickQuickActionButtonOnTransferActivity(null,objCioTransfer.quickActionOptionBack);

			// Navigating to WI from back button
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
			objCioTransfer.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
			String parentAuditTrailNumber = objWorkItemHomePage
					.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);

			// Verifying that AT,WI statuses are completed after manual approval

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondence), parentAuditTrailNumber,
					"SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");

			objCioTransfer.logout();
		}
		// Step 14:If response comes back and transfer code is required to be changed as
		// a part of response

		if (response.equalsIgnoreCase("Event Code needs to be changed")) {

			objCioTransfer.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
			ReportLogger.INFO("Changing  the Transfer Code Based on acessor response");
			objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
			objCioTransfer.Click(objCioTransfer.clearSelectionEventCode);
			objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel,
					CIOTransferPage.CIO_EVENT_CODE_CIOGOVT);
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

			ReportLogger
					.INFO("After Changing  the Transfer Code Based on acessor response we will submit it for approval");

			// Step 15 : Submitting for approval

			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.clickQuickActionButtonOnTransferActivity(null,objCioTransfer.quickActionOptionSubmitForApproval);

			// STEP 16-Clicking on submit for approval quick action button
			
			ReportLogger.INFO("CIO!! Transfer submitted for approval");
			objCioTransfer.waitForElementToBeVisible(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5));
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5)),
					"Work Item has been submitted for Approval.",
					"SMAB-T3377,SMAB-T10081:Cio trasnfer is submited for approval");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
			objCioTransfer.logout();

			// login with cio supervisor

			objCioTransfer.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon,10);
			objCioTransfer.clickQuickActionButtonOnTransferActivity(null,objCioTransfer.quickActionOptionApprove);
			
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5)),
					"Work Item has been approved successfully.",
					"SMAB-T3377,SMAB-T10081:Cio transfer is approved successfully");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));

			// Navigating to transfer screen to avoid stale element exception

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
			String parentAuditTrailNumber = objWorkItemHomePage
					.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondence), parentAuditTrailNumber,
					"SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");

			objCioTransfer.logout();

		}
	}
	/*
	 * This test method is used to assert that CIO auto confirm using batch job is
	 * able to autoconfirm transfer after no response came within 45 days of wait
	 * period
	 */

	@Test(description = "SMAB-T3377,SMAB-T10081:Verify that User is able to perform CIO transfer autoconfirm using a batch job (Fully automated) ", dataProvider = "dpForCioAutoConfirmUsingBatchJob", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration", "Smoke" })
	public void OwnershipAndTransfer_VerifyCioTransferAutoConfirmUsingBatchJob(String InitialEventCode,
			String finalEventCode) throws Exception {

		{
			JSONObject jsonForAutoConfirm = objCioTransfer.getJsonObject();
			
			String execEnv = System.getProperty("region");

			String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
			Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
					OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

			String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
			Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
					OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

			Map<String, String> hashMapCreateOwnershipRecordData = objUtil
					.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

			String dataToCreateCorrespondenceEventForAutoConfirm = testdata.UNRECORDED_EVENT_DATA;
			Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
					dataToCreateCorrespondenceEventForAutoConfirm, "DataToCreateCorrespondenceEventForAutoConfirm");

			String recordedDocumentID = salesforceAPI
					.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
					.get("Id").get(0);
			objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

			// STEP 1-login with SYS-ADMIN

			objMappingPage.login(users.SYSTEM_ADMIN);
			Thread.sleep(3000);
			objCioTransfer.addRecordedApn(recordedDocumentID, 1);
			

			salesforceAPI.update("Work_Item__c",
					"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'",
					"status__c", "In Progress");

			objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

			// STEP 2-Query to fetch WI

			String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";
			String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);

			objMappingPage.globalSearchRecords(workItemNo);
			String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
			objCioTransfer.deleteOwnershipFromParcel(salesforceAPI
					.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

			// STEP 3- adding owner after deleting for the recorded APN

			String acesseName = objMappingPage.getOwnerForMappingAction();
			driver.navigate()
					.to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
							.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
							+ "/related/Property_Ownerships__r/view");
			objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
			String ownershipId = driver.getCurrentUrl().split("/")[6];

			// STEP 4- updating the ownership date for current owners

			String dateOfEvent = salesforceAPI.select(
					"Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
					.get("Ownership_Start_Date__c").get(0);
			jsonForAutoConfirm.put("DOR__c", dateOfEvent);
			jsonForAutoConfirm.put("DOV_Date__c", dateOfEvent);
			salesforceAPI.update("Property_Ownership__c", ownershipId, jsonForAutoConfirm);

			objMappingPage.logout();

			// STEP 5-Login with CIO staff

			objMappingPage.login(users.CIO_STAFF);
			objMappingPage.globalSearchRecords(workItemNo);
			Thread.sleep(5000);
			String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo
					+ "'";
			HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

			// STEP 6-Finding the recorded apn transfer id

			String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
			objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
			objCioTransfer.waitForElementToBeClickable(objWorkItemHomePage.inProgressOptionInTimeline,15);
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

			// STEP 7-Clicking on related action link

			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindow);

			// STEP 8-Creating the new grantee

			objCioTransfer.createNewGranteeRecords(recordeAPNTransferID,
					hashMapOwnershipAndTransferGranteeCreationData);
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/"
					+ recordeAPNTransferID + "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
			HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataForRowString("1");
			String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);
			salesforceAPI.update("Recorded_APN_Transfer__c", recordeAPNTransferID, "Auto_Confirm_Start_Date__c",
					"2021-04-07");
			ReportLogger.INFO("Putting Auto confirm date prior to 45 days ");

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.calculateOwnershipButtonLabel);
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.nextButton);
			objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));

			// Step 9: create new mail to record

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
			objCioTransfer.waitForElementToBeClickable(7, objCioTransfer.copyToMailToButtonLabel);

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			ReportLogger.INFO("Add the Transfer Code");
			objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
			objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, InitialEventCode);
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

			// Creating Unrecorded transfer

			objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);
			String urlForTransactionTrail = driver.getCurrentUrl();
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			// Step 10 : Submitting for review

			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

			// STEP 11-Clicking on submit for review quick action button

			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForReview);
			objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForReview);
			ReportLogger.INFO("CIO!! Transfer submitted for review");
			objCioTransfer.waitForElementToBeVisible(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5),5);
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5)),
					"CIO transfer initial determination is submitted for review.",
					"SMAB-T3377,SMAB-T10081:Cio trasnfer is submited for review");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
			String newBusinessEventATRecordId = salesforceAPI.select("SELECT Id, Name FROM Transaction_Trail__c order by Name desc limit 1").get("Id").get(0);
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
					+ newBusinessEventATRecordId + "/view");
	
			Thread.sleep(2000); //Added to handle regression failure
	
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
					"SMAB-T3632: Validating that audit trail status should be open after submit for approval.");
	        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"),finalEventCode,
					"SMAB-T3632: Validating the 'Event Library' field after update transer code value in Audit Trail record.");
	
			objCioTransfer.logout();

			// Login with superviosr to complete reviews

			objCioTransfer.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionReviewComplete);
			objCioTransfer.Click(objCioTransfer.quickActionOptionReviewComplete);
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5)),
					"CIO transfer initial determination review completed.",
					"SMAB-T3377,SMAB-T10081:Cio trasnfer review is completed");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
			objCioTransfer.logout();

			// Step 12:Login with sysadmin to start autoconfirm batch job

			objMappingPage.login(users.SYSTEM_ADMIN);
			salesforceAPI.generateReminderWorkItems(SalesforceAPI.CIO_AUTOCONFIRM_BATCH_JOB);
			objCioTransfer.logout();

			// Step 13: login with cio staff to validate that auto confirm has taken place
			// for impending transfer

			objCioTransfer.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			// STEP 14 : Verifying transfer code has changed after approval and equals to
			// autoconfirm counterpart of the initial code
			objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.quickActionButtonDropdownIcon);

			softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferCodeLabel),
					finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
			String parentAuditTrailNumber = objWorkItemHomePage
					.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);

			// STEP 15:Verifying that AT=BE is completed
			objCioTransfer.waitForElementToBeVisible(10, trail.Status);

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");

			// STEP 16:Verifying that outbound event is completed

			driver.navigate().to(urlForTransactionTrail);
			objCioTransfer.waitForElementToBeVisible(10, trail.Status);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondence), parentAuditTrailNumber,
					"SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");
			//Navigate to the new Audit Trail record created after resubmitting the RAT for approval and validating that updated transfer code should be present in event library field
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
							+ newBusinessEventATRecordId + "/view");
			Thread.sleep(2000); //Added to handle regression failure
			objCioTransfer.waitForElementToBeVisible(10, trail.Status);
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Completed",
							"SMAB-T3632: Validating that audit trail status should be Completed after submit for approval.");
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"),finalEventCode,
							"SMAB-T3632: Validating the 'Event Library' field after update transer code value in Audit Trail record.");
				
			objCioTransfer.logout();

		}

	}

	/**
	 * Verify user is able to use the Calculate Ownership where ownership is
	 * acquired over multiple DOV's for the same owner and owner with one DOV is
	 * completely retained
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3696 : Verify user is able to use the Calculate Ownership where ownership is acquired over multiple DOV's for the same owner and owner with one DOV is completely retained", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement" }, enabled = true)
	public void OwnershipAndTransfer_Calculate_Ownership_SameOwnerMultipleDOV(String loginUser) throws Exception {

		JSONObject jsonForCalculateOwnership = objCioTransfer.getJsonObject();
		
		String  ownershipPercentage[] = {"75","25"};
		String  ownershipStartDate[] = {"5/3/2010" ,"7/2/2018"};
		JSONObject jsonObjectOwnership = new JSONObject();
		
		String execEnv = System.getProperty("region");
		String ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData,
				"DataToCreateOwnershipRecord");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(ownershipCreationData,
				"dataToCreateGranteeWithCompleteOwnership");

		String recordedDocumentID = salesforceAPI
				.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);
		
		// step 1: executing the recorder feed batch job to generate CIO WI
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		// step 2: fetching the recorded apn transfer object associated with the CIO WI and updating the DOV
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + cioWorkItem + "'";
		String recordeAPNTransferID = salesforceAPI.select(queryRecordedAPNTransfer).get("Navigation_Url__c").get(0).split("/")[3];
		
		jsonForCalculateOwnership.put("xDOV__c", "2021-02-03");
		jsonForCalculateOwnership.put("DOR__c", "2021-06-23");

		salesforceAPI.update("Recorded_APN_Transfer__c", recordeAPNTransferID, jsonForCalculateOwnership);

		//deleting the CIO Transfer grantees for the current transfer screen
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);


		// step 3: deleting the current ownership records for the APN linked with CIO WI
		String queryAPN = "SELECT Parcel__c FROM Recorded_APN_Transfer__c where id='" + recordeAPNTransferID + "'";
		String apn = salesforceAPI.select(queryAPN).get("Parcel__c").get(0);
		objCioTransfer.deleteOwnershipFromParcel(apn);
		queryAPN = "SELECT name FROM Parcel__c where id='" + apn + "'";
		String apnvalue = salesforceAPI.select(queryAPN).get("Name").get(0);

		// step 4: Fetching Assessee records
		String queryAssesseeRecord = "SELECT Id, Name,FirstName ,LastName  FROM Account Limit 1";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);
		String assesseeFirstName = responseAssesseeDetails.get("FirstName").get(0);
		if (assesseeFirstName.equals("null"))
			assesseeFirstName="";
		String assesseeLastName = responseAssesseeDetails.get("LastName").get(0);
		if (assesseeLastName.equals("null"))
			assesseeLastName="";
		
		//step 5 : creating two new ownership records with different DOVs but same owner

		objCioTransfer.login(SYSTEM_ADMIN);
		objMappingPage.searchModule(EFILE_INTAKE_VIEW);
		Thread.sleep(5000);
		objMappingPage.closeDefaultOpenTabs();

		for (int i = 0; i < 2; i++) {
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + apn
					+ "/related/Property_Ownerships__r/view");

			hashMapCreateOwnershipRecordData.put("Ownership Percentage", ownershipPercentage[i]);
			hashMapCreateOwnershipRecordData.put("Ownership Start Date", ownershipStartDate[i]);
			objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);

			String ownershipId = driver.getCurrentUrl().split("/")[6];
			String dateOfOwnership = salesforceAPI.select(
					"Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
					.get("Ownership_Start_Date__c").get(0);
			jsonObjectOwnership.put("DOR__c", dateOfOwnership);
			jsonObjectOwnership.put("DOV_Date__c", dateOfOwnership);
			salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObjectOwnership);	
			

		}

		// Step6: Opening the work items and accepting the WI created by recorder batch
		objCioTransfer.logout();
		Thread.sleep(5000);
		objCioTransfer.login(loginUser);
		objCioTransfer.searchModule(EFILE_INTAKE_VIEW);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab,20);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel,20);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);	  	


		// Step7: CIO staff user navigating to transfer screen by clicking on related
		// action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCioTransfer.waitForElementToBeVisible(30,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.scrollToBottom();
		String dov = objCioTransfer.getFieldValueFromAPAS(objCioTransfer.dovLabel);
		String dor = objCioTransfer.getFieldValueFromAPAS(objCioTransfer.dorLabel);

		// step 8: creating new grantee with 10 % ownership
		ReportLogger.INFO("Creating new grantee record");
		hashMapOwnershipAndTransferGranteeCreationData.put("Owner Percentage", "10");
		hashMapOwnershipAndTransferGranteeCreationData.put("First Name", "");
		hashMapOwnershipAndTransferGranteeCreationData.put("Ownership Start Date", "");
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		ReportLogger.INFO("Grantee record created successfully");

		// Step9: CIO staff user navigating to transfer screen by clicking on related
		// action link
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(15,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);

		List<WebElement> cioTransferScreenCalculateOwnershipModalFields = objCioTransfer
				.locateElements(objCioTransfer.fieldsInCalculateOwnershipModal, 10);
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(5)),
				assesseeFirstName,
				"SMAB-T3696: Validation that First Name field is assesseeFirstName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(6)),
				assesseeLastName,
				"SMAB-T3696: Validation that last Name field is assesseelastName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(7)),
				"25",
				"SMAB-T3696: Validation that Ownership Percentage field in calculate owenrship modal is percentage value for owner with latest DOV");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(8)),
				"July 2, 2018",
				"SMAB-T3696: Validation that DOV field in calculate owenrship modal is latest DOV in all ownership records for parcel");

		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "15");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);
		cioTransferScreenCalculateOwnershipModalFields = objCioTransfer
				.locateElements(objCioTransfer.fieldsInCalculateOwnershipModal, 10);
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(5)),
				assesseeFirstName,
				"SMAB-T3696: Validation that First Name field for second owner is assesseeFirstName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(6)),
				assesseeLastName,
				"SMAB-T3696: Validation that last Name field for second owner is assesseelastName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(7)),
				"75",
				"SMAB-T3696: Validation that Ownership Percentage field for second owner in calculate owenrship modal is percentage value for owner with second latest DOV");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(8)),
				"May 3, 2010",
				"SMAB-T3696: Validation that DOV field for second owner in calculate owenrship modal is second latest DOV in all ownership records for parcel");
		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "75");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));

		// step 10 :Validating the grantee table

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.newButton);
		objCioTransfer.waitForElementToBeVisible(10,
				objCioTransfer.columnInGrid.replace("columnName", objCioTransfer.ownershipPercentage));
		objCioTransfer.sortInGrid("Owner Percentage", true);
		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataInHashMap();

		softAssert.assertEquals(granteeHashMap.get("Grantee/Retain Owner Name").get(0),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
				"SMAB-T3696: Validation that Grantee name that was created in grantee table  is correct");
		softAssert.assertEquals(granteeHashMap.get("Status").get(0), "Active",
				"SMAB-T3696: Validation that Grantee that was created has active status");
		softAssert.assertEquals(granteeHashMap.get("Owner Percentage").get(0), "10.0000%",
				"SMAB-T3696: Validation that Owner Percentage of grantee that was created is correct");
		softAssert.assertEquals(granteeHashMap.get("DOR").get(0), dor,
				"SMAB-T3696: Validation that DOR of grantee that was craeted is DOR of recorded document");
		softAssert.assertEquals(granteeHashMap.get("DOV").get(0), dov,
				"SMAB-T3696: Validation that DOV of grantee that was craeted is DOV of recorded document");
		softAssert.assertEquals(granteeHashMap.get("Ownership Start Date").get(0), dov,
				"SMAB-T3696: Validation that Ownership Start Date of grantee that was created is the DOV of recorded document");

		softAssert.assertEquals(granteeHashMap.get("Grantee/Retain Owner Name").get(1),
				assesseeLastName + " " + assesseeFirstName,
				"SMAB-T3696: Validation that current owner name (that was retained partially )in grantee table  after calculate ownership is correct ");
		softAssert.assertEquals(granteeHashMap.get("Status").get(1), "Active",
				"SMAB-T3696: Validation that current owner  (that was retained partially ) in Grantee table has active status");
		softAssert.assertEquals(granteeHashMap.get("Owner Percentage").get(1), "15.0000%",
				"SMAB-T3696: Validation that Owner Percentage of owner that was partially retained is correct");
		softAssert.assertEquals(granteeHashMap.get("DOR").get(1), ownershipStartDate[1],
				"SMAB-T3696: Validation that DOR of owner that was partially retained is DOR of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("DOV").get(1), ownershipStartDate[1],
				"SMAB-T3696: Validation that DOV of owner that was partially retained is DOV of of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("Ownership Start Date").get(1), ownershipStartDate[1],
				"SMAB-T3696: Validation that Ownership Start Date of owner that was partially retained is Ownership Start Date of original ownership record");

		softAssert.assertEquals(granteeHashMap.get("Grantee/Retain Owner Name").get(2),
				assesseeLastName + " " + assesseeFirstName,
				"SMAB-T3696: Validation that current owner name (that was retained fully )in grantee table  after calculate ownership is correct ");
		softAssert.assertEquals(granteeHashMap.get("Status").get(2), "Retained",
				"SMAB-T3696: Validation that current owner  (that was retained fully ) in Grantee table has active status");
		softAssert.assertEquals(granteeHashMap.get("Owner Percentage").get(2), "75.0000%",
				"SMAB-T3696: Validation that Owner Percentage of owner that was fully retained is correct");
		softAssert.assertEquals(granteeHashMap.get("DOR").get(2), ownershipStartDate[0],
				"SMAB-T3696: Validation that DOR of owner that was fully retained is  DOR of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("DOV").get(2), ownershipStartDate[0],
				"SMAB-T3696: Validation that DOV of owner that was fully retained is is DOV of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("Ownership Start Date").get(2), ownershipStartDate[0],
				"SMAB-T3696: Validation that Ownership Start Date of owner that was fully retained is is Ownership Start Date of original ownership record");

		// Step 11: submitting the WI for approval
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(15,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		ReportLogger.INFO("Updating the transfer code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-COPAL");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		ReportLogger.INFO("transfer code updated successfully");

		ReportLogger.INFO("Submitting the WI for approval");
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(30,objCioTransfer.confirmationMessageOnTranferScreen);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.confirmationMessageOnTranferScreen),"Work Item has been submitted for Approval.","SMAB-T3696: Validation that proper mesage is displayed after submit for approval");

		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 6);

		// step 12 : navigating to ownersip records page of parcel
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnvalue + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objCioTransfer.waitForElementToBeVisible(10,
				objCioTransfer.columnInGrid.replace("columnName", objCioTransfer.ownershipPercentage));
		objCioTransfer.sortInGrid(objCioTransfer.ownershipPercentage, true);
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();

		// STEP 13-Validating the Owners ,their status and ownership percentages
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
				"SMAB-T3696:Validating that the grantee that was created from transfer screen has become  new owner : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active",
				"SMAB-T3696: Validating that status of new owner which is the grantee created from transfer screen is Active : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "10.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of new owner which is the grantee created from transfer screen is correct: "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0), dov,
				"SMAB-T3696: Validating that Ownership Start Date of new owner which is the grantee created from transfer screen is DOVof recorded document : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(0), dor,
				"SMAB-T3696: Validating that DOR of new owner which is the grantee created from transfer screen is DOR of recorded doc: "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(0), dov,
				"SMAB-T3696: Validating that DOV of new owner which is the grantee created from transfer screen is DOV of recorded doc : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"));

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(1), assesseeName,
				"SMAB-T3696:Validating that the partially retained owner has become  new owner : " + assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(1), "Active",
				"SMAB-T3696: Validating that status of partially retained owner created from transfer screen is Active : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(1), "15.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of partially retained owner retained from transfer screen is correct: "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(1), ownershipStartDate[1],
				"SMAB-T3696: Validating that Ownership Start Date of partially retained owner created from transfer screen is Ownership Start Date of original record :"
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(1), ownershipStartDate[1],
				"SMAB-T3696: Validating that DOR of partially retained owner created from transfer screen is DOR of original ownership record: "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(1), ownershipStartDate[1],
				"SMAB-T3696: Validating that DOV of partially retained owner created from transfer screen is DOV of original ownership record : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(2), assesseeName,
				"SMAB-T3696:Validating that the old ownership that was  partially retained is retired : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(2), "Retired",
				"SMAB-T3696: Validating that the old ownership that was  partially retained is retired :"
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(2), "25.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of partially retained owner which is now retired is correct: "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(2), ownershipStartDate[1],
				"SMAB-T3696: Validating that Ownership Start Date of partially retained owner which is now retired is Ownership Start Date of original record : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership end Date").get(2), dor,
				"SMAB-T3696: Validating that Ownership end Date of partially retained owner which is now retired is DOR of original ownership record : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(2), ownershipStartDate[1],
				"SMAB-T3696: Validating that DOR of partially retained owner which is now retired is DOR of original ownership record : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(2), ownershipStartDate[1],
				"SMAB-T3696: Validating that DOV of partially retained owner which is now retired is DOV of original ownership record : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(3), assesseeName,
				"SMAB-T3696:Validating that the fully retained owner is now active owner : " + assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(3), "Active",
				"SMAB-T3696: Validating that status of fully retained owner created from transfer screen is Active : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(3), "75.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of fully retained is correct : " + assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(3), ownershipStartDate[0],
				"SMAB-T3696: Validating that Ownership Start Date of fully retained owner  is Ownership Start Date of original record : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(3), ownershipStartDate[0],
				"SMAB-T3696: Validating that DOR of fully retained owner  is DOR of original ownership record : "
						+ assesseeName);

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(3), ownershipStartDate[0],
				"SMAB-T3696: Validating that DOV of fully retained owner  is DOV of original ownership record : "
						+ assesseeName);

		objCioTransfer.logout();
	}

	/*
	 * Verify that User is able to perform CIO transfer for recorded APN and
	 * validate all status
	 */

	@Test(description = "SMAB-T3525, SMAB-T3341, SMAB-T3881, SMAB-T3764:Verify that User is able to perform CIO transfer  for recorded APN and validate all status and values in Audit Trail record", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void OwnershipAndTransfer_VerifyTransferActivityStatus_ReturnedAndCompleted(String loginUser)
			throws Exception {
		
		int i=1; int j=1;
		String rollYear = "2022";
		String execEnv = System.getProperty("region");
		JSONObject jsonForTransferActivityStatus = objCioTransfer.getJsonObject();
		String userNameForCioStaff = CONFIG.getProperty(users.CIO_STAFF + "UserName");
		String userNameForCioSupervisor = CONFIG.getProperty(users.CIO_SUPERVISOR + "UserName");
		
		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String recordedDocumentID = salesforceAPI.select(" SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1").get("Id").get(0);
		String recordedDocumentName = salesforceAPI.select(" SELECT Name from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1").get("Name").get(0);
		objCioTransfer.deleteOldGranteesRecords(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN
		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		salesforceAPI.update("Work_Item__c",
				"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c",
				"In Progress");
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI
		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNo);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objCioTransfer.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the recorded APN
		String acesseName = objMappingPage.getOwnerForMappingAction();
		objParcelsPage.createOwnershipRecord(apnFromWIPage, acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		// STEP 4- updating the ownership date for current owners
		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		jsonForTransferActivityStatus.put("DOR__c", dateOfEvent);
		jsonForTransferActivityStatus.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonForTransferActivityStatus);

		objMappingPage.logout();

		// STEP 5-Login with CIO staff
		objMappingPage.login(loginUser);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNo);

		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the recorded apn transfer id
		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);

		// STEP 7-Clicking on related action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertContains(driver.getCurrentUrl(), navigationUrL.get("Navigation_Url__c").get(0),
				"SMAB-T3306:Validating that user navigates to CIo transfer screenafter clicking on related action hyperlink");
		String transferScreenURL = driver.getCurrentUrl();

		ReportLogger.INFO("Add the Transfer Code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-GLEASM");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		// STEP 8-Creating the new grantee
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);

		// STEP 9-Validating present grantee
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		Thread.sleep(2000);// Allow the screen to appear completely
		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataForRowString("1");
		String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);

		// STEP 10-Creating copy to mail to record
		objCioTransfer.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.copyToMailToButtonLabel);

		// STEP 11-Validating mail to record created from copy to mail to
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "" + "/related/CIO_Transfer_Mail_To__r/view");
		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.newButton);

		// STEP 12-Navigating back to RAT screen
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 13-Clicking on submit for approval quick action button
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		
		String newBusinessEventATRecordId = salesforceAPI.select("SELECT Id, Name FROM Transaction_Trail__c order by Name desc limit 1").get("Id").get(0);
		Thread.sleep(2000); // Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus), "Submitted for Approval",
				"SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after submit for approval.");

		// STEP 14- Get audit trail Value from transfer screen and validate the status
		String auditTrailName = objWorkItemHomePage.getElementText(objCioTransfer.CIOAuditTrail);
		String auditTrailID = salesforceAPI
				.select("SELECT Id,Status__c,Name FROM Transaction_Trail__c where Name='" + auditTrailName + "'")
				.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrailID + "/view");
		Thread.sleep(2000); //Added to handle regression failure
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
				"SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Processed By", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioStaff + "'").get("Name").get(0),
				"SMAB-T3881: Validating the 'Processed By' field value in Audit Trail record");
		softAssert.assertTrue(objWorkItemHomePage.getFieldValueFromAPAS("Final Approver", "Additional Information").equals(""),
				"SMAB-T3881: Validating the 'Final Approver' field value is blank in Audit Trail record.");
		
		//Navigate to the new Audit Trail record created after submitting the RAT for approval
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ newBusinessEventATRecordId + "/view");
		Thread.sleep(2000); //Added to handle regression failure
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
				"SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		softAssert.assertTrue(objWorkItemHomePage.getFieldValueFromAPAS("Processed By", "Additional Information").equals(""),
				"SMAB-T3881: Validating the 'Processed By' field value is blank in Audit Trail record.");
		softAssert.assertTrue(objWorkItemHomePage.getFieldValueFromAPAS("Final Approver", "Additional Information").equals(""),
				"SMAB-T3881: Validating the 'Final Approver' field value is blank in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"),"CIO-GLEASM",
				"SMAB-T3631: Validating the 'Event Library' field value in Audit Trail record.");
		

		// STEP 15-Navigating back to RAT screen and clicking on back quick action button
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 16-Validating that back button has navigates the user to WI page and
		// status of WI should be submitted for approval.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Submitted for Approval",
				"SMAB-T3525: Validating that status of WI should be submitted for approval.");
		
		objCioTransfer.logout();
		Thread.sleep(5000);

		// STEP 17 :- login with CIO supervisor
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 18 - Clicking on return quick action button
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionReturn);
		objCioTransfer.Click(objCioTransfer.quickActionOptionReturn);
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.returnReasonTextBox);
		objCioTransfer.enter(objCioTransfer.returnReasonTextBox, "Returned by CIO Supervisor");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);

		Thread.sleep(2000); // Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus), "Returned",
				"SMAB-T3525, SMAB-T3341: Validating CIO Transfer activity status on transfer activity screen after returned by supervisor.");

		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 19-Validating WI and AUDIT Trail status after returned by supervisor.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Returned",
				"SMAB-T3525, SMAB-T3341: Validating that Back button navigates back to WI page ");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrailID + "/view");
		Thread.sleep(2000); // Allow the screen to appear completely
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
				"SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		objCioTransfer.logout();
		Thread.sleep(5000);

		objMappingPage.login(loginUser);
		driver.navigate().to(transferScreenURL);
		
		ReportLogger.INFO("Edit the Transfer Code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);	
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.clearSelectionFromLookup("Transfer Code");;
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel,objCioTransfer.CIO_EVENT_CODE_COPAL );
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 20-Clicking on submit for approval quick action button
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);

		Thread.sleep(2000);// Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer resubmit for approval by staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus), "Submitted for Approval",
				"SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after resubmit for approval by staff.");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);

		// Validate the Ownership record on the parcel
		ReportLogger.INFO("Validate the Current & New Ownership record in Grid after transfer activity is submitted for approval");
		objCioTransfer.clickViewAll("Ownership for Parent Parcel");
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();
		if(HashMapLatestOwner.get("Status").get(0).equals("Active")) i=0;
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(i), hashMapOwnershipAndTransferGranteeCreationData.get("First Name") + " " +
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
				"SMAB-T3341: Validate the owner name on New Ownership record");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(i), "100.0000%",
				"SMAB-T3341: Validate the ownership percentage on New Ownership record");
		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(i), "Active",
				"SMAB-T3341: Validate the status on New Ownership record");
		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(i==0?i+1:i-1), "Retired",
				"SMAB-T3341: Validate the status on Old Ownership record");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(i==0?i+1:i-1), "7/19/1950",
				"SMAB-T3764: Validate the status on Old Ownership record");
		
		String activeOwnershipStartDate = HashMapLatestOwner.get("Ownership Start Date").get(i);
		driver.navigate().to(transferScreenURL);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 21-Validating that back button has navigates the user to WI page.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Submitted for Approval",
				"SMAB-T3525: Validating WI after resubmit for approval ");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrailID + "/view");
		Thread.sleep(2000); // Allow the screen to appear completely
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
				"SMAB-T3525: Validating that audit trail status should be open after resubmit for approval.");
		
		//Navigate to the new Audit Trail record created after resubmitting the RAT for approval and validating that updated transfer code should be present in event library field
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
						+ newBusinessEventATRecordId + "/view");
		Thread.sleep(2000); //Added to handle regression failure
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
						"SMAB-T3631: Validating that audit trail status should be open after submit for approval.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"),objCioTransfer.CIO_EVENT_CODE_COPAL,
						"SMAB-T3631: Validating the 'Event Library' field after update transer code value in Audit Trail record.");
				

		objCioTransfer.logout();
		Thread.sleep(5000);

		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 22-Clicking on approval quick action button
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionApprove);
		objCioTransfer.Click(objCioTransfer.quickActionOptionApprove);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);

		Thread.sleep(2000);// Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer Approved");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus), "Approved",
				"SMAB-T3525, SMAB-T3341: Validating CIO Transfer activity status on transfer activity screen after approved by supervisor.");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		
		// STEP 23-Get the work item number for type 'Govt CIO Appraisal'
		String workItemNoForGovtCIOAppraisal = salesforceAPI.select("Select Id ,Name from Work_Item__c where type__c='Govt CIO Appraisal' and sub_type__c='Appraisal Activity' order by createdDate desc").get("Name").get(0);

		// STEP 24-Validate the Ownership record on the parcel
		ReportLogger.INFO("Validate the Current & New Ownership record in Grid after transfer activity is Approved");
		objCioTransfer.clickViewAll("Ownership for Parent Parcel");
		HashMap<String, ArrayList<String>> HashMapLatestOwner1 = objCioTransfer.getGridDataInHashMap();
		if(HashMapLatestOwner1.get("Status").get(0).equals("Active")) j=0;
		softAssert.assertEquals(HashMapLatestOwner1.get("Owner").get(j), hashMapOwnershipAndTransferGranteeCreationData.get("First Name") + " " +
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
				"SMAB-T3341: Validate the owner name on New Ownership record");
		softAssert.assertEquals(HashMapLatestOwner1.get("Ownership Percentage").get(j), "100.0000%",
				"SMAB-T3341: Validate the ownership percentage on New Ownership record");
		softAssert.assertEquals(HashMapLatestOwner1.get("Status").get(j), "Active",
				"SMAB-T3341: Validate the status on New Ownership record");
		softAssert.assertEquals(HashMapLatestOwner1.get("Status").get(j==0?j+1:j-1), "Retired",
				"SMAB-T3341: Validate the status on Old Ownership record");

		driver.navigate().to(transferScreenURL);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 25-Validating that WI and audit trail status after approving the transfer activity.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Completed",
				"SMAB-T3525, SMAB-T3341: Validating that WI status should be completed after approval by supervisor.");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrailID + "/view");
		Thread.sleep(2000);// Allow the screen to appear completely
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Completed",
				"SMAB-T3525: Validating that audit trail status");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Processed By", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioStaff + "'").get("Name").get(0),
				"SMAB-T3881: Validating the 'Processed By' field value in Audit Trail record");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Final Approver", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioSupervisor + "'").get("Name").get(0),
				"SMAB-T3881: Validating the 'Final Approver' field value in Audit Trail record.");
		
		//STEP 26-Navigate to the new Audit Trail record created after submitting the RAT for approval
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ newBusinessEventATRecordId + "/view");
		Thread.sleep(2000); //Added to handle regression failure
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Completed",
				"SMAB-T3525: Validating that audit trail status should be completed after submit for approval.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Processed By", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioStaff + "'").get("Name").get(0),
				"SMAB-T3881: Validating the 'Processed By' field value in Audit Trail record");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Final Approver", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioSupervisor + "'").get("Name").get(0),
				"SMAB-T3881: Validating the 'Final Approver' field value in Audit Trail record.");
		
		//STEP 27-Navigate to the new Audit Trail record created after approval
		String newBEAuditTrailRecordId = salesforceAPI.select("SELECT Id, Name FROM Transaction_Trail__c order by Name desc limit 1").get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ newBEAuditTrailRecordId + "/view");
		Thread.sleep(2000); //Added to handle regression failure
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
				"SMAB-T3764: Validating that audit trail status should be open after submit for approval.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"), objCioTransfer.CIO_EVENT_CODE_COPAL,
				"SMAB-T3764: Validating the 'Event Library' field value in Audit Trail record");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Applicable To Roll Year", "Additional Information"), rollYear,
				"SMAB-T3764: Validating the 'Applicable To Roll Year' field value in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Date of Value"), activeOwnershipStartDate,
				"SMAB-T3764: Validating the 'Date of Value' field value in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Date of Recording"), activeOwnershipStartDate,
				"SMAB-T3764: Validating the 'Date of Recording' field value in Audit Trail record.");
		
		objCioTransfer.logout();
		Thread.sleep(5000);

		//STEP 28- Validate the WI details for Type 'Govt CIO Appraisal'
		objMappingPage.login(users.APPRAISAL_SUPPORT);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNoForGovtCIOAppraisal);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Type", "Information"), "Govt CIO Appraisal",
				"SMAB-T3764: Validating the Type field value in Work Item");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Action", "Information"), "Appraisal Activity",
				"SMAB-T3764: Validating the Action field value in Work Item");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"), "Normal Enrollment",
				"SMAB-T3764: Validating the Work Pool field value in Work Item");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information"), recordedDocumentName,
				"SMAB-T3764: Validating the Reference field value in Work Item");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("APN", "Information"), apnFromWIPage,
				"SMAB-T3764: Validating the Reference field value in Work Item");
		
		objCioTransfer.logout();
	
	}

	/**
	 * Verify that APN related details are updated when APN is updated on Recorded
	 * Event
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3232 : Verify that APN related details are updated when APN is updated on Recorded Event", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void RecorderIntegration_VerifyAPNDetailsOnTransferActivityScreen(String loginUser) throws Exception {
		
		JSONObject jsonObject1 = objMappingPage.getJsonObject();
		JSONObject jsonObject2 = objMappingPage.getJsonObject();
		String execEnv = System.getProperty("region");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData,
				"DataToCreateOwnershipRecord");

		// Fetch values from Database and insert it in the Parcels
		String assesseeName = objMappingPage.getOwnerForMappingAction();

		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 1";
		String activeApn1 = salesforceAPI.select(queryForActiveAPN).get("Name").get(0);
		String activeApnId1 = salesforceAPI.select(queryForActiveAPN).get("Id").get(0);

		String queryForRetiredAPN = "select Name, Id from Parcel__c where Status__c='Retired' limit 1";
		String retiredApn = salesforceAPI.select(queryForRetiredAPN).get("Name").get(0);
		String retiredApnId = salesforceAPI.select(queryForRetiredAPN).get("Id").get(0);

		HashMap<String, ArrayList<String>> responsePUCDetails1 = salesforceAPI
				.select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responsePUCDetails2 = salesforceAPI
				.select("SELECT id, Name FROM PUC_Code__c where Name in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responseSitusDetails = salesforceAPI
				.select("SELECT Id, Name FROM Situs__c where Name != NULL LIMIT 2");
		String primarySitusId1 = responseSitusDetails.get("Id").get(0);
		String primarySitusValue1 = responseSitusDetails.get("Name").get(0);
		String primarySitusId2 = responseSitusDetails.get("Id").get(1);
		String primarySitusValue2 = responseSitusDetails.get("Name").get(1);

		String legalDescriptionValue1 = "Test Legal Description PM 85/25-260";
		String legalDescriptionValue2 = "Test Legal Description PM 85/25-270";

		jsonObject1.put("PUC_Code_Lookup__c", responsePUCDetails1.get("Id").get(0));
		jsonObject1.put("Short_Legal_Description__c", legalDescriptionValue1);
		jsonObject1.put("Primary_Situs__c", primarySitusId1);
		salesforceAPI.update("Parcel__c", activeApnId1, jsonObject1);

		jsonObject2.put("PUC_Code_Lookup__c", responsePUCDetails2.get("Id").get(0));
		jsonObject2.put("Short_Legal_Description__c", legalDescriptionValue2);
		jsonObject2.put("Primary_Situs__c", primarySitusId2);
		salesforceAPI.update("Parcel__c", retiredApnId, jsonObject2);

		// Delete existing Ownership records from the Active parcel
		objMappingPage.deleteOwnershipFromParcel(activeApnId1);

		// Step 1: Executing the recorder feed batch job to generate CIO WI & Add
		// ownership records in the parcels
		objCioTransfer.generateRecorderJobWorkItems("DE", 1);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		objParcelsPage.createOwnershipRecord(activeApn1, assesseeName, hashMapCreateOwnershipRecordData);

		objWorkItemHomePage.logout();
		Thread.sleep(5000);

		// Step2: Login to the APAS application with CIO Staff
		objCioTransfer.login(loginUser);
		objCioTransfer.closeDefaultOpenTabs();

		// Step3: Opening the work items and accepting the WI created by recorder batch
		ReportLogger.INFO("Navigate to Work Item and open Transfer activity record");
		objCioTransfer.searchModule(modules.HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		Thread.sleep(1000); // Allows the WI to load completely to avoid regression failure

		String activeApn2 = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(10, objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);

		Thread.sleep(1000); // Allows the other screen to load
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.transferCodeLabel);

		// Step4: Fetch values from the screen
		String pucValue = "";
		String legalDescValue = "Test Short Legal Description";
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];

		HashMap<String, ArrayList<String>> responseApnDetail = salesforceAPI
				.select("SELECT Id FROM Parcel__c Where Name = '" + activeApn2 + "'");
		String activeApnId2 = responseApnDetail.get("Id").get(0);
		HashMap<String, ArrayList<String>> responseSitusDetail = salesforceAPI
				.select("SELECT Id, Name FROM Situs__c LIMIT 1");
		String primarySitusValue = responseSitusDetail.get("Name").get(0);
		salesforceAPI.update("Parcel__c", activeApnId2, "Primary_Situs__c", responseSitusDetail.get("Id").get(0));
		salesforceAPI.update("Parcel__c", activeApnId2, "Short_Legal_Description__c", legalDescValue);

		driver.navigate().refresh();
		Thread.sleep(3000); // Allows the screen to load and update the Situs & Legal Description value

		if (salesforceAPI.select(
				"SELECT Name  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where name='"
						+ activeApn2 + "')") != null)
			pucValue = salesforceAPI.select(
					"SELECT Name  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where name='"
							+ activeApn2 + "')")
					.get("Name").get(0);

		// Step5: Validate the Parcel related values on the screen
		softAssert.assertEquals(
				objCioTransfer.getElementText(objCioTransfer.shortLegalDescriptionOnTransferActivityLabel),
				legalDescValue, "SMAB-T3232: Validate the Legal Description on CIO Transfer screen for " + activeApn2);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.pucCodeTransferActivityLabel), pucValue,
				"SMAB-T3232: Validate the PUC on CIO Transfer screen for " + activeApn2);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),
				primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + activeApn2);

		// Step6: Update the APN value to Retired APN value and validate values
		ReportLogger.INFO("Update the APN value to a Retired Parcel value");
		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.clearSelectionFromLookup(objCioTransfer.ApnLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, retiredApn);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveLabel));
		Thread.sleep(3000); // Allows the record to save properly

		softAssert.assertEquals(
				objCioTransfer.getElementText(objCioTransfer.shortLegalDescriptionOnTransferActivityLabel),
				legalDescriptionValue2,
				"SMAB-T3232: Validate the Legal Description on CIO Transfer screen for " + retiredApn);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.pucCodeTransferActivityLabel),
				responsePUCDetails2.get("Name").get(0),
				"SMAB-T3232: Validate the PUC on CIO Transfer screen for " + retiredApn);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),
				primarySitusValue2.replaceFirst("\\s", ""),
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + retiredApn);
		softAssert.assertTrue(objCioTransfer.verifyElementExists(objCioTransfer.warningMessageArea),
				"SMAB-T3232: Validate that warning message is displayed on CIO Transfer screen for Retired Parcel");

		// Step7: Update the APN value to Active APN value and validate values
		ReportLogger.INFO("Update the APN value to an Active Parcel value");
		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.clearSelectionFromLookup(objCioTransfer.ApnLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, activeApn1);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveLabel));
		Thread.sleep(3000); // Allows the record to save properly

		String numOfMailToRecordOnRAT = objCioTransfer.getElementText(objCioTransfer.numberOfMailToLabel);
		softAssert.assertTrue(!objCioTransfer.verifyElementExists(objCioTransfer.warningMessageArea),
				"SMAB-T3232: Validate that warning message disappears on CIO Transfer screen");

		driver.navigate().refresh();
		Thread.sleep(3000); // Allows the screen to load

		softAssert.assertEquals(
				objCioTransfer.getElementText(objCioTransfer.shortLegalDescriptionOnTransferActivityLabel),
				legalDescriptionValue1,
				"SMAB-T3232: Validate the Legal Description on CIO Transfer screen for " + activeApn1);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.pucCodeTransferActivityLabel),
				responsePUCDetails1.get("Name").get(0),
				"SMAB-T3232: Validate the PUC on CIO Transfer screen for " + activeApn1);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),
				primarySitusValue1.replaceFirst("\\s", ""),
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + activeApn1);

		// Step8: Validate the Ownership record on the parcel
		ReportLogger.INFO("Validate the Current Ownership record in Grid");
		objCioTransfer.clickViewAll("Ownership for Parent Parcel");
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), assesseeName,
				"SMAB-T3232: Validate the owner name on Ownership record");
		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active",
				"SMAB-T3232: Validate the status on Ownership record");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),
				hashMapCreateOwnershipRecordData.get("Ownership Start Date"),
				"SMAB-T3232: Validate the start date on Ownership record");

		// Step 9: Validate the Mail-To record on the parcel, if exist
		ReportLogger.INFO("Validate the Mail-To record on the parcel");
		objCioTransfer.globalSearchRecords(activeApn1);
		objParcelsPage.openParcelRelatedTab("Mail-To");
		objCioTransfer.waitForElementToBeVisible(10, objParcelsPage.numberOfMailToOnParcelLabel);
		String numOfMailToRecordOnParcel = objCioTransfer.getElementText(objParcelsPage.numberOfMailToOnParcelLabel);

		if (!numOfMailToRecordOnParcel.equals("(0)")) {
			ReportLogger.INFO("There is/are Mail-To record(s) present on the parcel");
			HashMap<String, ArrayList<String>> mailToTableDataHashMap = objParcelsPage
					.getParcelTableDataInHashMap("Mail-To");
			String status = mailToTableDataHashMap.get("Status").get(0);
			String formattedName1 = mailToTableDataHashMap.get("Formatted Name 1").get(0);
			String formattedName2 = mailToTableDataHashMap.get("Formatted Name 2").get(0);

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.numberOfGrantorLabel);
			objCioTransfer.clickViewAll("CIO Transfer Mail To");

			// Step9a: Validate the details in the grid
			HashMap<String, ArrayList<String>> HashMapMailTo = objCioTransfer.getGridDataInHashMap();
			softAssert.assertEquals(HashMapMailTo.get("Status").get(0), status,
					"SMAB-T3232: Validate the Status of Mail-To record");
			softAssert.assertEquals(HashMapMailTo.get("Formatted Name1").get(0), formattedName1,
					"SMAB-T3232: Validate the Formatted Name1 of Mail-To record");
			softAssert.assertEquals(HashMapMailTo.get("Formatted Name2").get(0), formattedName2,
					"SMAB-T3232: Validate the Formatted Name2 of Mail-To record");
		} else {
			ReportLogger.INFO("Validate if there is no Mail-To record on the parcel");
			softAssert.assertTrue(numOfMailToRecordOnRAT.contains("0"),
					"SMAB-T3232: Validate that there are no Mail-To Records");
		}

		// Step10: Submit for Approval and verify the status
		ReportLogger.INFO("Navigate to RAT screen and Submit the transfer activity record");
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.numberOfGrantorLabel);

		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.finishButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatusLabel);

		objCioTransfer.logout();
	}

	/*
	 * Verify that User is able validate that orignal transfer list data is being fetched
	 * from transfer feed that displays set of data is predefined format and also in
	 * this test method we try to validate that CIO staff can  add and edit grantor and grantee,
	 * new record and the full name of metioned records is cancatination of last
	 * name first name in the same order.s	 */

	@Test(description = "SMAB-T3342,SMAB-T3343,SMAB-T3629,SMAB-T3630: Verify that User is able validate that orignal transfer list  data fetched from transfer feed that display set of data is predefined format", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void OwnershipAndTransfer_VerifyOrignalTransferList(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOrignalTransferListData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "dataToUpdateOrignalTransferList");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String recordedDocumentID = salesforceAPI.select(
				"SELECT Recorded_Document__c FROM Transfer__c  group by Recorded_Document__c  having count(id)=1 limit 1")
				.get("Recorded_Document__c").get(0);
		HashMap<String, ArrayList<String>> hashMapGrantorGranteeList = salesforceAPI
				.select("SELECT id ,Recorder_Doc_Number__c  FROM Transfer__c where recorded_document__c = '"
						+ recordedDocumentID + "'");

		// Updating the orignal grantor and grantee of the transfer

		String recordedDocumentNumber = hashMapGrantorGranteeList.get("Recorder_Doc_Number__c").get(0);
		if (!hashMapGrantorGranteeList.isEmpty()) {
			hashMapGrantorGranteeList.get("Id").stream().forEach(Id -> {
				salesforceAPI.update("Transfer__c", Id, "Grantee__c", hashMapOrignalTransferListData.get("Grantee"));
				salesforceAPI.update("Transfer__c", Id, "Grantor__c", hashMapOrignalTransferListData.get("Grantor"));
			});
		}
		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		Thread.sleep(3000);
		objCioTransfer.addRecordedApn(recordedDocumentID, 1);
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";		
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		// objMappingPage.searchModule("APAS");
		objMappingPage.globalSearchRecords(workItemNo);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objCioTransfer.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the recorded APN

		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		// STEP 4- updating the ownership date for current owners

		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		jsonObject.put("DOR__c", dateOfEvent);
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
		//objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Property_Ownership__c/"
				+ ownershipId + "/view");

		// STEP 8 -Validating that old owners are not editable by CIO staff

		softAssert.assertEquals(!objCioTransfer.verifyElementVisible(objCioTransfer.Edit), true,
				"SMAB-T3342:VerifyCIO staff is not able to edit old owners from CIO transfer screen");

		objCioTransfer.searchModule(modules.EFILE_INTAKE);
		Thread.sleep(3000);
		

		objCioTransfer.createNewGrantorRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);

		// STEP 9-Verifying that grantors record are editable by CIO staff

		softAssert.assertEquals(objCioTransfer.verifyElementVisible(objCioTransfer.Edit), true,
				"SMAB-T3343:VerifyCIO staff is  able to edit grantors from CIO transfer screen");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		objCioTransfer.enter(objCioTransfer.LastNameLabel, "Will Smith");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.SaveButton));
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.LastNameLabel), "Will Smith",
				"SMAB-T3343:VerifyCIO staff is  able to edit grantors from CIO transfer screen");

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantors__r/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.getButtonWithText(objCioTransfer.newButton), 20);
		HashMap<String, ArrayList<String>> hashMapGrantorGrid = objCioTransfer.getGridDataInHashMap();

		// STEP 10- Validating that grantor name is a cancatination of Last name and
		// first name

		softAssert.assertContains(hashMapGrantorGrid.get("Grantor Name"),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name") + " "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("First Name"),
				"SMAB-T3629:Verify that grantor full name is cancatination of last name and fist name and in same order ");
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.getButtonWithText(objCioTransfer.newButton), 5);
		HashMap<String, ArrayList<String>> hashMapGranteeGrid = objCioTransfer.getGridDataInHashMap();

		// STEP 11-Validating that grantee name is a cancatination of last name and
		// first name

		softAssert.assertEquals(hashMapGranteeGrid.get("Grantee/Retain Owner Name").get(0),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name") + " "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("First Name"),
				"SMAB-T3629:Verify that grantee full name is cancatination of last name and fist name and in same order ");

		// STEP 12-Validating the sequence and name of orignal grantors and granteee
		// from orignal transfer list

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.checkOriginalTransferListButtonLabel);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.checkOriginalTransferListButtonLabel));
		Thread.sleep(5000);//Wait to load the modal screen completely
		List<WebElement> listOfElementsOnOrignalTransferList = objCioTransfer
				.locateElements("//*[@class='flowruntimeRichTextWrapper flowruntimeDisplayText']/div/p | //*[@class='field-element']/div//p", 5);
		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(0).getText(), "Recorded Document",
				"SMAB-T3630:Verify that recorded is present in orignal transfer list");
		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(1).getText(), "Seq No",
				"SMAB-T3630:Verify that sequence no is present in the orignal transfer list and is present next to recorded document column ");
		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(2).getText(), "Grantor",
				"SMAB-T3630:Verify that grantor column   is captured in Orignal transfer list and is present next to sequence number");
		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(3).getText(), "Grantee",
				"SMAB-T3630:Verify that  grantee column is captured in Orignal transfer list and is present next to the grantor column");
		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(4).getText(), recordedDocumentNumber,
				"SMAB-T3630:Verify that recorded-Document number is captured from recorder feed");

		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(7).getText(), "1",
				"SMAB-T3630:Verify that Verify that orignal sequence number from recorder feed is captured in Orignal transfer list");

		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(10).getText(),
				hashMapOrignalTransferListData.get("Grantor"),
				"SMAB-T3630:Verify that orignal grantor from recorder feed is captured in Orignal transfer list");

		softAssert.assertEquals(listOfElementsOnOrignalTransferList.get(13).getText(),
				hashMapOrignalTransferListData.get("Grantee"),
				"SMAB-T3630:Verify that orignal grantee from recorder feed is captured in Orignal transfer list");

		objCioTransfer.logout();

		objCioTransfer.login(users.CIO_SUPERVISOR);

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.checkOriginalTransferListButtonLabel);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.checkOriginalTransferListButtonLabel));
		Thread.sleep(5000);//Wait to load the modal screen completely

		// STEP 13-Validating the sequence and name of orignal grantors and granteee
		// from CIO supervisor
		// from orignal transfer list

		List<WebElement> listOfElementsOnOrignalTransferListForCioSupervisor = objCioTransfer
				.locateElements("//*[@class='flowruntimeRichTextWrapper flowruntimeDisplayText']/div/p | //*[@class='field-element']/div//p", 5);
		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(0).getText(),
				"Recorded Document", "SMAB-T3630:Verify that recorded is present in orignal transfer list");
		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(1).getText(), "Seq No",
				"SMAB-T3630:Verify that sequence no is present in the orignal transfer list and is present next to recorded document column");
		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(2).getText(), "Grantor",
				"SMAB-T3630:Verify that grantor column  is captured in Orignal transfer list and is present next to sequence number");
		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(3).getText(), "Grantee",
				"SMAB-T3630:Verify that  grantee column is captured in Orignal transfer list and is present next to the grantor column");
		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(4).getText(),
				recordedDocumentNumber, "SMAB-T3630:Verify that recorded");
		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(7).getText(), "1",
				"SMAB-T3630:Verify that Verify that orignal sequence number from recorder feed is captured in Orignal transfer list");

		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(10).getText(),
				hashMapOrignalTransferListData.get("Grantor"),
				"SMAB-T3630:Verify that orignal grantor from recorder feed is captured in Orignal transfer list");

		softAssert.assertEquals(listOfElementsOnOrignalTransferListForCioSupervisor.get(13).getText(),
				hashMapOrignalTransferListData.get("Grantee"),
				"SMAB-T3630:Verify that orignal grantee from recorder feed is captured in Orignal transfer list");

		objCioTransfer.logout();

	}
	
	
	
	@Test(description = "SMAB-T3765,SMAB-T3832:Verify that User is not able to submit the records if the ownership percentage is lessthan 100%", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","RecorderIntegration" })
	public void RecorderIntegration_VerifyValidationofMailToAndGranteeRecord(String loginUser) throws Exception {
 
			
		String execEnv = System.getProperty("region");		
		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String recordedDocumentID = salesforceAPI
				.select(" SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);
		objCioTransfer.deleteOldGranteesRecords(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'  And status__c='In pool' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(workItemNo);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objCioTransfer.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the recorded APN

		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);

		objMappingPage.logout();

		// STEP 4-Login with CIO staff

		objMappingPage.login(loginUser);
		objMappingPage.globalSearchRecords(workItemNo);
		Thread.sleep(2000);
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 5-Finding the recorded APN transfer id

		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 6-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
		// STEP 7-Creating the new grantee
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		
		// STEP 8-Validating order of the columns in CIO Transfer Grantee & New Ownership
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataInHashMap();
		softAssert.assertEquals(granteeHashMap.containsKey("Recorded Document"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("DOR"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("DOV"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("Grantee/Retain Owner Name"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("Status"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("Ownership Percentage"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("Ownership Start Date"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("Ownership End Date"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("Name Detail"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");
		softAssert.assertEquals(granteeHashMap.containsKey("Remarks"), "true",
				"SMAB-T3832:Verifying The order of the CIO Transfer Grantee and new Owner Ship");

		// STEP 9-Validating that grantees combined cannot have ownership more than 100%
		driver.navigate().to("https://smcacre--"+ execEnv+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Submit for Approval");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.validateErrorText);
		String errorMessage=objCioTransfer.getElementText(objCioTransfer.validateErrorText);
		ReportLogger.INFO("Alert Message Captured");
		softAssert.assertEquals(errorMessage,
				"The sum of all grantee ownership percentage is less than 100. Please check and make necessary corrections",
				"SMAB-T3765: Validate the error message ");
		
		// STEP 10- Click Finish and calculate owner ship
		objCioTransfer.Click(objCioTransfer.getButtonWithText("Finish"));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);
		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		ReportLogger.INFO("Successfully calculated Ownership % ");
		
		 //Step 11- Validating Order of the columns under ownership details
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
       	HashMap<String, ArrayList<String>> granteeHashMap1 = objCioTransfer.getGridDataInHashMap();
		softAssert.assertEquals(granteeHashMap1.containsKey("Recorded Document"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("DOR"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("DOV"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("Owner"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("Status"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("Ownership Percentage"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("Ownership Start Date"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("Ownership End Date"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("Ownership Id"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
		softAssert.assertEquals(granteeHashMap1.containsKey("Remarks"), "true",
				"SMAB-T3832:Verifying The order of the OwnerShip details");
	     objCioTransfer.logout();
		
	}


	/*
	 * Verify that NO APN WI is genrated for document without APN and user has the
	 * ability to add recorded APN on it to create a WI for CIO
	 * 
	 */

	@Test(description = "SMAB-T3288,SMAB-T3769 :Verify the type of WI system created for a recorded document with no APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void RecorderIntegration_VerifyNewWIgeneratedfromRecorderIntegrationForNOAPNRecordedDocumentforCIO(
			String loginUser) throws Exception {

		String getApnToAdd = "Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(getApnToAdd);
		String recordedAPN = hashMapRecordedApn.get("Name").get(0);

		// login with CIO user

		objMappingPage.login(users.CIO_STAFF);
		objMappingPage.searchModule(PARCELS);

		objCioTransfer.generateRecorderJobWorkItems("DE", 0);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='NO APN' AND Sub_type__c='NO APN - CIO'  And status__c='In pool' order by createdDate desc limit 1";
		String WorkItemNo = salesforceAPI.select(WorkItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(WorkItemNo);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		
		// User tries to close the WI in which no APN is added

		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
        softAssert.assertEquals(objWorkItemHomePage.getAlertMessage(),
				"Status: Work item status cannot be completed as related recorded APN(s) are not migrated yet.",
				"SMAB-T3288:Verifying User is not able to close WI Before migrating APN");
		objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
		
		// User tries to add the Recorded APN
		
		objMappingPage.Click(objWorkItemHomePage.recordedAPNtab);

		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.NewButton));
		objWorkItemHomePage.enter(objWorkItemHomePage.apnLabel, recordedAPN);
		objWorkItemHomePage.selectOptionFromDropDown(objWorkItemHomePage.apnLabel, recordedAPN);

		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.SaveButton));
		Thread.sleep(2000);
		driver.navigate().back();
		driver.navigate().back();
		
		// User clicks on Migrate button
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.migrateAPN));

		// User validates the status of added recorded APN
		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0), "Processed",
				"SMAB-T3769,SMAB-T3288: Validating that status of added APN is processed");

		// User tries to complete the WI
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);
		objWorkItemHomePage.waitForElementToBeInVisible(objWorkItemHomePage.successAlert);

		// User validates the status of the WI
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
				"SMAB-T3769,SMAB-T3288:Validating that status of WI is completed");
		String createdWorkItem = salesforceAPI.select(
				"SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 AND APN__C='" + recordedAPN + "' ")
				.get("Name").get(0);
		objMappingPage.globalSearchRecords(createdWorkItem);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To", "Information"), "CIO StaffAUT",
				"SMAB-T3769: Validate user is able to validate the value of 'Assigned To' field");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),
				"In Progress", "SMAB-T3769: Validate user is able to validate the value of 'Status' field");

		objWorkItemHomePage.logout();

	}

	/*
	 * Verify that NO APN WI is genrated for document with Invalid APN(recorder apn
	 * is 000000000 and apn is blank) and user has the ability to add recorded APN
	 * on it to create a WI for CIO
	 * 
	 */

	@Test(description = "SMAB-T3564 :Verify the type of WI system created for a recorded document with no APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void RecorderIntegration_VerifyNewWIForNOAPNRecordedDocumentforEnvalidAPN(String loginUser)
			throws Exception {

		String getApnToAdd = "Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(getApnToAdd);
		String recordedAPN = hashMapRecordedApn.get("Name").get(0);

		// Step 1: getting recorded document type CIO and recorded apn from recorded
		
		String documentId = objCioTransfer.getRecordedDocumentId("DE", 1);
		hashMapRecordedApn = salesforceAPI
				.select("SELECT ID,Name FROM Recorded_APN__c WHERE RECORDED_DOCUMENT__C='" + documentId + "'");
		String recordedAPNID = hashMapRecordedApn.get("Id").get(0);
		String recordedAPNName = hashMapRecordedApn.get("Name").get(0);

		// Step 2: updating recorder_apn and parcel value in recorded apn
		JSONObject jsonToUpdateRecordedAPN = new JSONObject();
		jsonToUpdateRecordedAPN.put("Parcel__c", "");
		jsonToUpdateRecordedAPN.put("Recorder_APN__c", "000000000");
		jsonToUpdateRecordedAPN.put("Status__c", "Pending");
		salesforceAPI.update("Recorded_APN__c", recordedAPNID, jsonToUpdateRecordedAPN);
		
		objMappingPage.login(users.CIO_STAFF);
		objMappingPage.searchModule(PARCELS);

		// Step 3: generating recorded work items from job
		objCioTransfer.generateRecorderJobWorkItems(documentId);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='NO APN' AND Sub_type__c='NO APN - CIO'  And status__c='In pool' order by createdDate desc limit 1";
		String WorkItemNo = salesforceAPI.select(WorkItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(WorkItemNo);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
	
		// Step 4: User tries to close the WI in which no APN is added
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
        softAssert.assertEquals(objWorkItemHomePage.getAlertMessage(),
				"Status: Work item status cannot be completed as related recorded APN(s) are not migrated yet.",
				"SMAB-T3564:Verifying User is not able to close WI Before migrating APN");
		objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);

		// Step 5: User tries to edit the Recorded APN
		objMappingPage.Click(objWorkItemHomePage.recordedAPNtab);
		objCioTransfer.editRecordedApnOnWorkitem(recordedAPNName, recordedAPN);
		
		driver.navigate().back();
		driver.navigate().back();

		// Step 6: User clicks on Migrate button
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.migrateAPN));
		

		// Step 7: User validates the status of added recorded APN
		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0), "Processed",
				"SMAB-T3564: Validating that status of added APN is processed");

		// Step 8: User tries to complete the WI
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);
		objWorkItemHomePage.waitForElementToBeInVisible(objWorkItemHomePage.successAlert);

		// Step 9: User validates the status of the WI
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
				"SMAB-T3564:Validating that status of WI is completed");
		String createdWorkItem = salesforceAPI.select(
				"SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 AND APN__C='" + recordedAPN + "' ")
				.get("Name").get(0);
		objMappingPage.globalSearchRecords(createdWorkItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To", "Information"), "CIO StaffAUT",
				"SMAB-T3564: Validate user is able to validate the value of 'Assigned To' field");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),
				"In Progress",
				"SMAB-T3564: Validate that status of newly created Workitem should be 'In Progress' in  'Status' field");

		objWorkItemHomePage.logout();

	}
	/*
	 * Verify system creates a NO APN WI for a recorded document which has two
	 * recorded APNs ,one with APN" 000000000" and other with a valid APN ,when SF
	 * batch job is executed and user is not able to close it add recorded APN on it
	 * to create a WI for CIO
	 * 
	 */

	@Test(description = "SMAB-T3566 :Verify the type of WI system created for a recorded document with no APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void RecorderIntegration_VerifyNewWIForNOAPNforEnvalidAPNwithValidAPN(String loginUser)
			throws Exception {

		String getApnToAdd = "Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(getApnToAdd);
		String recordedAPN = hashMapRecordedApn.get("Name").get(0);

		// Step 1: getting recorded document type CIO and recorded apn from recorded
		
		String documentId = objCioTransfer.getRecordedDocumentId("DE", 2);
		System.out.println("documentId ==" + documentId);
		hashMapRecordedApn = salesforceAPI.select(
				"SELECT ID,Name,Parcel__c FROM Recorded_APN__c WHERE RECORDED_DOCUMENT__C='" + documentId + "'");
		String recordedAPNId1 = hashMapRecordedApn.get("Id").get(0);
		String recordedAPNName1 = hashMapRecordedApn.get("Name").get(0);
		String recordedAPNId2 = hashMapRecordedApn.get("Id").get(0);
		String recordedAPN2 = hashMapRecordedApn.get("Parcel__c").get(0);

		// Step 2: updating recorder_apn and parcel value in recorded apn
		JSONObject jsonToUpdateRecordedAPN = new JSONObject();
		jsonToUpdateRecordedAPN.put("Parcel__c", "");
		jsonToUpdateRecordedAPN.put("Recorder_APN__c", "000000000");
		jsonToUpdateRecordedAPN.put("Status__c", "Pending");
		salesforceAPI.update("Recorded_APN__c", recordedAPNId1, jsonToUpdateRecordedAPN);
		salesforceAPI.update("Recorded_APN__c", recordedAPNId2, "Status__c", "Pending");

		objMappingPage.login(users.CIO_STAFF);
		objMappingPage.searchModule(PARCELS);

		// Step 3: generating recorded work items from job
		objCioTransfer.generateRecorderJobWorkItems(documentId);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='NO APN' AND Sub_type__c='NO APN - CIO'  And status__c='In pool' order by createdDate desc limit 1";
		String WorkItemNo = salesforceAPI.select(WorkItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(WorkItemNo);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);

		// Step 4: User tries to close the WI in which no APN is added
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
        softAssert.assertEquals(objWorkItemHomePage.getAlertMessage(),
				"Status: Work item status cannot be completed as related recorded APN(s) are not migrated yet.",
				"SMAB-T3566:Verifying User is not able to close WI Before migrating APN");
		objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);

		// Step 5: User tries to add the Recorded APN
		objCioTransfer.editRecordedApnOnWorkitem(recordedAPNName1, recordedAPN);
		driver.navigate().back();
		driver.navigate().back();

		// Step 6: User clicks on Migrate button
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.migrateAPN));
		

		// Step 7: User validates the status of added recorded APN
		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0), "Processed",
				"SMAB-T3566: Validating that status of added APN is processed");

		// Step 8: User tries to complete the WI
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);
		objWorkItemHomePage.waitForElementToBeInVisible(objWorkItemHomePage.successAlert);

		// Step 9: User validates the status of the WI
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
				"SMAB-T3566:Validating that status of WI is completed");
		String createdWorkItem = salesforceAPI.select(
				"SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 AND APN__C='" + recordedAPN + "' ")
				.get("Name").get(0);
		objMappingPage.globalSearchRecords(createdWorkItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To", "Information"), "CIO StaffAUT",
				"SMAB-T3566: Validate user is able to validate the value of 'Assigned To' field");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),
				"In Progress",
				"SMAB-T3566: Validate that status of newly created Workitem should be 'In Progress' in  'Status' field");

		String createdWorkItem2 = salesforceAPI.select(
				"SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 AND APN__C='" + recordedAPN2 + "' ")
				.get("Name").get(0);
		objMappingPage.globalSearchRecords(createdWorkItem2);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To", "Information"), "CIO StaffAUT",
				"SMAB-T3566: Validate user is able to validate the value of 'Assigned To' field");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),
				"In Progress",
				"SMAB-T3566: Validate that status of newly created Workitem should be 'In Progress' in  'Status' field");

		objWorkItemHomePage.logout();

	}

	@Test(description = "SMAB-T4112-Verify CIO Staff and Appriasal Able to edit Mail-To records", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void RecorderIntegration_VerifyValidationofMailToRecordOnParcel(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
		String mailToRecordFromParcel = "SELECT Parcel__c,Id FROM Mail_To__c where status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(mailToRecordFromParcel);
		String mailToID = hashMapRecordedApn.get("Id").get(0);

		// STEP 1:login with CIOSTAFF and navigate to Mail-To tab for active parcel

		objMappingPage.login(loginUser);
		driver.navigate().to(
				"https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Mail_To__c/" + mailToID + "/view");
		objCioTransfer.waitForElementToBeClickable(5, objBuildingPermitPage.editPermitButton);
		objWorkItemHomePage.Click(objBuildingPermitPage.editPermitButton);

		// STEP 2:Edit the Mail-To record
		Map<String, String> hashMapMailToData = objUtil.generateMapFromJsonFile(MailtoData, "createMailToData");
		objCioTransfer.enter(objCioTransfer.formattedName1LabelForParcelMailTo,
				hashMapMailToData.get("Formatted Name1"));
		objCioTransfer.enter(objCioTransfer.emailId, hashMapMailToData.get("Email"));
		objCioTransfer.enter(objCioTransfer.remarksLabel, hashMapMailToData.get("Remarks"));
		objCioTransfer.enter(objCioTransfer.careOfLabel, hashMapMailToData.get("Care Of"));
		objCioTransfer.enter(objCioTransfer.mailingZip, hashMapMailToData.get("Mailing Zip"));
		objCioTransfer.Click(objCioTransfer.getButtonWithText("Save"));
		Thread.sleep(2000);
		// STEP 3: Validate Startdate and End date fields are disabled
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.startDateInParcelMaito, "class"),
				"is-read-only", "SMAB-T4112-Start date is not editable");
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.endDateInParcelMaito, "class"), "is-read-only",
				"SMAB-T4112-End date is not editable");

		objCioTransfer.Click(objCioTransfer.getButtonWithText("Clone"));
		objCioTransfer.enter(objCioTransfer.formattedName1LabelForParcelMailTo,
				hashMapMailToData.get("Formatted Name1"));
		objCioTransfer.enter(objCioTransfer.mailingZip, hashMapMailToData.get("Mailing Zip"));
		objCioTransfer.Click(objCioTransfer.getButtonWithText("Save"));
		Thread.sleep(2000);
		objWorkItemHomePage.logout();
		Thread.sleep(2000);

		// STEP 4:login with appraisal staff and validate Appraisal support is able to
		// Edit the record

		objMappingPage.login(users.APPRAISAL_SUPPORT);
		driver.navigate().to(
				"https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Mail_To__c/" + mailToID + "/view");
		objCioTransfer.waitForElementToBeClickable(5, objBuildingPermitPage.editPermitButton);
		objWorkItemHomePage.Click(objBuildingPermitPage.editPermitButton);
		Map<String, String> hashMapMailToDataForAppraisal = objUtil.generateMapFromJsonFile(MailtoData,
				"createMailToDataForAppraisalData");
		objCioTransfer.enter(objCioTransfer.formattedName1LabelForParcelMailTo,
				hashMapMailToDataForAppraisal.get("Formatted Name1"));
		objCioTransfer.enter(objCioTransfer.emailId, hashMapMailToDataForAppraisal.get("Email"));
		objCioTransfer.enter(objCioTransfer.remarksLabel, hashMapMailToDataForAppraisal.get("Remarks"));
		objCioTransfer.enter(objCioTransfer.careOfLabel, hashMapMailToDataForAppraisal.get("Care Of"));
		objCioTransfer.enter(objCioTransfer.mailingZip, hashMapMailToDataForAppraisal.get("Mailing Zip"));
		softAssert.assertContains(objCioTransfer.saveRecordAndGetError(),
				"Please clone this record if you want to update any information except Email, Remarks and Mailing Address",
				"T4112: Verify Error message");
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.startDateInParcelMaito, "class"),
				"is-read-only", "SMAB-T4112-Start date is not editable");
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.endDateInParcelMaito, "class"), "is-read-only",
				"SMAB-T4112-End date is not editable");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));

	}

	/**
	 Ownership And Transfers - Verify AT Event and WI are transferred  to new APN when APN# is edited in RAT/CIO Transfer Screen
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3821 : Ownership And Transfers - Verify AT Event and WI are transferred  to new APN when APN# is edited in RAT/CIO Transfer Screen", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement" }, enabled = true)
	public void CIO_TransferScreen_UpdateAPN(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");

		String queryAPN = "select Name, Id from Parcel__c where Status__c='Active' and Primary_Situs__c !=NULL  and  id in ( select parcel__c from mail_to__c where Status__c='Active')";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apnToUpdate=responseAPNDetails.get("Name").get(0);
	
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ apnToUpdate +"')").get("Name").get(0);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		
		String queryCountMailTORecords = "SELECT count(id) FROM Mail_To__c where parcel__c='"+responseAPNDetails.get("Id").get(0)+"' and status__c='active'";
		String countActiveMaiToRecords=salesforceAPI.select(queryCountMailTORecords).get("expr0").get(0);;

		HashMap<String, ArrayList<String>> responseMailToDetails=salesforceAPI.select("SELECT Formatted_Name_2__c ,Formatted_Name_of_Second_Recipient__c ,Care_Of__c ,Mailing_Country__c   FROM Mail_To__c where parcel__c='"+responseAPNDetails.get("Id").get(0)+"' and status__c='Active' ");

		String ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData,
				"DataToCreateOwnershipRecord");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil
				.generateMapFromJsonFile(ownershipCreationData, "dataToCreateGranteeWithCompleteOwnership");

		
		String dataToCreateCorrespondenceEventForAutoConfirm = testdata.UNRECORDED_EVENT_DATA;
		Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
				dataToCreateCorrespondenceEventForAutoConfirm, "DataToCreateCorrespondenceEventForAutoConfirm");
	
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeCIOWithActionAPNLegalDescription");

		String recordedDocumentID = salesforceAPI
				.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);

		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(EFILE_INTAKE_VIEW);
		objCioTransfer.addRecordedApn(recordedDocumentID, 1);

		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 2";
		Thread.sleep(3000);
		String workItemNo1 = salesforceAPI.select(workItemQuery).get("Name").get(0);

		objMappingPage.globalSearchRecords(workItemNo1);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		String apnIDFromWIPage=salesforceAPI.select("SElect id from parcel__c where name='"+apnFromWIPage+"'").get("Id").get(0);
		
		objCioTransfer.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnToUpdate + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the recorded APN

		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnToUpdate + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		// STEP 4- updating the ownership date for current owners

		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		JSONObject jsonObject = objCioTransfer.getJsonObject();
		jsonObject.put("DOR__c", dateOfEvent);
		jsonObject.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

		HashMap<String, ArrayList<String>> responseAPNOwnershipDetails=salesforceAPI.select("SELECT Name FROM Property_Ownership__c where parcel__c='"+responseAPNDetails.get("Id").get(0)+"'");

		objMappingPage.logout();
		Thread.sleep(4000);

		// STEP 5-Login with CIO staff

		objMappingPage.login(loginUser);
		
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo1 + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the  recorded apn transfer id
		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		JSONObject jsonForUpdateAPN = objCioTransfer.getJsonObject();

		jsonForUpdateAPN.put("xDOV__c", "2021-02-03");
		jsonForUpdateAPN.put("DOR__c", "2021-06-23");

		salesforceAPI.update("Recorded_APN_Transfer__c", recordeAPNTransferID, jsonForUpdateAPN);

		// deleting the CIO Transfer grantees for the transfer screen
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);

		objMappingPage.globalSearchRecords(workItemNo1);
		objMappingPage.waitForElementToBeVisible(30, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link and navigating to  transfer screen

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		//objCioTransfer.waitForElementToBeVisible(20,
		//objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		Thread.sleep(7000);
		// STEP 8-Creating the new grantee

		hashMapOwnershipAndTransferGranteeCreationData.put("Owner Percentage", "100");
		hashMapOwnershipAndTransferGranteeCreationData.put("First Name", "");
		hashMapOwnershipAndTransferGranteeCreationData.put("Ownership Start Date", "");

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);

		// STEP 9-Updating The APN from transfer screen 
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		//objCioTransfer.waitForElementToBeVisible(20,
				//objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		Thread.sleep(8000);
		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.ApnLabel);
		objCioTransfer.Click(objCioTransfer.crossIconAPNEditField);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, apnToUpdate);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		ReportLogger.INFO("APN   updated successfully");
		
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 10);

		// fetch the new CIO transfer mail  to record details for updated APN 
		Thread.sleep(5000);
		HashMap<String, ArrayList<String>> responseCIOTransferMailToDetails=salesforceAPI.select("SELECT Formatted_Name1__c ,Formatted_Name2__c ,Care_of__c, Mailing_Country__c  FROM CIO_Transfer_Mail_To__c where Recorded_APN_Transfer__c ='"+recordeAPNTransferID+"'");

		// STEP 10-Verify the updated details on screen as per new APN

		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.situsLabel, ""),primarySitusValue,
				"SMAB-T3821: Validate that primary situs in CIO screen is now that of new APN that was entered in APN field ");
		
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.pucCodeLabel, ""),responsePUCDetails.get("Name").get(0),
				"SMAB-T3821: Validate that PUC   in CIO screen is now that of new APN that was entered in APN field ");
		
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.shortLegalDescriptionLabel, ""),legalDescriptionValue,
				"SMAB-T3821: Validate that legal description on CIO  screen is now that of new APN that was entered in APN field ");
		
		objCioTransfer.scrollToBottom();
		objCioTransfer.waitForElementToBeVisible(10,objCioTransfer.numberOfMailToLabel);
		softAssert.assertContains( objCioTransfer.getElementText(objCioTransfer.numberOfMailToLabel),countActiveMaiToRecords,
				"SMAB-T3821: Verify that "
				+ "only the active mail to records for a parcel are shown on  transfer screen after APN update ");
		
		softAssert.assertEquals( responseMailToDetails.get("Formatted_Name_of_Second_Recipient__c").get(0),responseCIOTransferMailToDetails.get("Formatted_Name2__c").get(0),
				"SMAB-T3821: Verify that "
				+ "formatted name 2 in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Formatted_Name_2__c").get(0),responseCIOTransferMailToDetails.get("Formatted_Name1__c").get(0),
				"SMAB-T3821: Verify that "
				+ "formatted name 1 in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Care_Of__c").get(0),responseCIOTransferMailToDetails.get("Care_of__c").get(0),
				"SMAB-T3821: Verify that "
				+ "Care_Of__c in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Mailing_Country__c").get(0),responseCIOTransferMailToDetails.get("Mailing_Country__c").get(0),
				"SMAB-T3821: Verify that "
				+ "Mailing_Country__c in CIO mail to is that of updated APN 's mail to record ");
		
		objCioTransfer.clickViewAll("Ownership for Parent Parcel");
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();

		softAssert.assertTrue( responseAPNOwnershipDetails.get("Name").contains(HashMapLatestOwner.get("Ownership Id").get(0)),
				"SMAB-T3821: Verify that "
				+ "current ownership records  on transfer screen is that of new APN ");
		
		//navigating back to transfer screen	
		
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		ReportLogger.INFO("Updating the transfer code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-COPAL");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		ReportLogger.INFO("transfer code updated successfully");

		// STEP 11-verifying the correspondence event created after creating correspondence AT from component action
		
		objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);
		String urlForTransactionTrail = driver.getCurrentUrl();
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
Thread.sleep(7000);
		String  auditTrailId=urlForTransactionTrail.split("/")[6];
		
		String auditTrailName =salesforceAPI.select("SELECT Name FROM Transaction_Trail__c where id='"+auditTrailId+"'").get("Name").get(0);
		
		String auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		String countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		
		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),responseAPNDetails.get("Id").get(0),
				"SMAB-T3821: Validation that  correspondence event created after APN update is linked to the update APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
				"SMAB-T3821: Validation that  correspondence event created after APN update is linked to only the updated APN and not the old APN");
		

		// STEP 12-verifying the WI and business event created after creating APN & legal description WI from component action
		
		String workItemNumber=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		String workItemIdQuery = "SELECT id FROM Work_Item__c where name ='"+workItemNumber+"' ";
		
		String workItemId=salesforceAPI.select(workItemIdQuery).get("Id").get(0);
		
		String apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		String countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),responseAPNDetails.get("Id").get(0),
				"SMAB-T3821: Validation that APN & legal description WI created is linked to only new APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,
				"SMAB-T3821: Validation that APN & legal description WI created is linked to only new APN and not the old APN");
		
		String auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		String responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		JSONObject responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),responseAPNDetails.get("Id").get(0),
					"SMAB-T3821: Validation that  business event created from APN & legal description WI after APN update is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3821: Validation that  business event created from APN & legal description WI after APN update is linked to only the updated APN and not the old APN");
			
		// STEP 13-verifying the original CIO WI and Recorder feed Audit trails  created are linked to the updated APN

		 workItemIdQuery = "SELECT id,APN__c FROM Work_Item__c where name ='"+workItemNo1+"' ";
		
		 workItemId=salesforceAPI.select(workItemIdQuery).get("Id").get(0);
		
	     String  apnOnWorkItemDetailspage=salesforceAPI.select(workItemIdQuery).get("APN__c").get(0);

		 apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		 countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' and parcel__c='"+apnIDFromWIPage+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),responseAPNDetails.get("Id").get(0),
				"SMAB-T3821: Validation that CIO  WI created by recorder feed is linked to only new APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),0,
				"SMAB-T3821: Validation that CIO WI created by recorder feed is linked to only new APN and not the old APN");
		
		softAssert.assertEquals(apnOnWorkItemDetailspage,responseAPNDetails.get("Id").get(0),"SMAB-T3821: Validation that CIO WI that was created by recorder feed has  the new APN In details tab  ");

		 auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		 responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		 responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),responseAPNDetails.get("Id").get(0),
					"SMAB-T3821: Validation that  business event created for  CIO WI by recorder feed , is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3821: Validation that  business event created for  CIO WI by recorder feed is linked to only the updated APN and not the old APN");
			
		// STEP 14-Clicking on submit for approval quick action button
		
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(objCioTransfer.confirmationMessageOnTranferScreen);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.locateElement(objCioTransfer.transferSucessMessage, 5)),
				"Work Item has been submitted for Approval.",
				"SMAB-T3821:Cio trasnfer is submited for approval afterAPN Update");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");
		

		// STEP 15-validating that The new ownership  record gets created  for the new APN

		driver.navigate()
		.to("https://smcacre--"
				+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + responseAPNDetails.get("Id").get(0)
				+ "/related/Property_Ownerships__r/view");
		objCioTransfer.waitForElementToBeVisible(10,
		objCioTransfer.columnInGrid.replace("columnName", objCioTransfer.Status));
		objCioTransfer.sortInGrid("Status", true);

		HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0),
		hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
		"SMAB-T3821:The new ownership  record gets created  for the new APN");
		
		// STEP 16-validating that Business event created due to change in transfer code is linked to new APN and not to old APN

		 auditTrailId =salesforceAPI.select("SELECT id FROM Transaction_Trail__c where Event_Library__c in (select id from Event_Library__c  where name='CIO-COPAL') order by createddate desc limit 1").get("Id").get(0);
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where id='"+auditTrailId+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where id='"+auditTrailId+"'";

		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),responseAPNDetails.get("Id").get(0),
						"SMAB-T3821: Validation that  business event created after transfer code update is linked to the update APN");
				
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
						"SMAB-T3821: Validation that  business event created after transfer code update is linked to only the updated APN and not the old APN");
				
			
		// Step 17: CIO supervisor now logs in and navigates to the  transfe screen and approves it
		objCioTransfer.logout();
		Thread.sleep(4000);
		objCioTransfer.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		objCioTransfer.waitForElementToBeVisible(30,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));

		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionApprove);
		objCioTransfer.Click(objCioTransfer.quickActionOptionApprove);
		objCioTransfer.waitForElementTextToBe(objCioTransfer.confirmationMessageOnTranferScreen,
				"Work Item has been approved successfully.", 30);
		
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 6);
		ReportLogger.INFO("CIO!! Transfer approved by Supervisor");
		
		
		//Step 18 : Validate the Appraisal WIs created  

		String workPoolIdQuery="SELECT Id FROM Work_Pool__c where name='Normal Enrollment' ";
		String workPoolId = salesforceAPI.select(workPoolIdQuery).get("Id").get(0);

		workItemQuery = "SELECT Id,name,APN__c   FROM Work_Item__c where Type__c='Appraiser' And Sub_Type__c ='Appraisal Activity' and Work_Pool__c ='"+workPoolId+"' and  status__c='In Pool' order by createdDate desc limit 1";
		 workItemNumber = salesforceAPI.select(workItemQuery).get("Name").get(0);

		workItemId=salesforceAPI.select(workItemQuery).get("Id").get(0);
        apnOnWorkItemDetailspage=salesforceAPI.select(workItemQuery).get("APN__c").get(0);
       
		apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),responseAPNDetails.get("Id").get(0),
				"SMAB-T3821: Validation that Appraisal  WI created after approval by supervisor is linked to only new APN");
						
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,"SMAB-T3821: Validation that Appraisal WI created after approval by supervisor is linked to only new APN and not the old APN");
			
		softAssert.assertEquals(apnOnWorkItemDetailspage,responseAPNDetails.get("Id").get(0),
				"SMAB-T3821: Validation that Appraisal WI created after approval by supervisor has new APN in APN look up field on WI details page");
		
		 auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		 responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		 responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),responseAPNDetails.get("Id").get(0),
					"SMAB-T3821: Validation that  business event created for  Appraisal WI  , is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3821: Validation that  business event created for  Appraisal WI is linked to only the updated APN and not the old APN");
			
		
		workItemQuery = "SELECT Id,name  FROM Work_Item__c where Type__c='Appraiser' And Sub_Type__c ='Questionnaire Correspondence' and Work_Pool__c ='"+workPoolId+"' and  status__c='In Pool' order by createdDate desc limit 1";
		workItemId=salesforceAPI.select(workItemQuery).get("Id").get(0);

		apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),responseAPNDetails.get("Id").get(0),"SMAB-T3821: Validation that Appraisal  Questionnaire Correspondence WI created after approval by supervisor is linked to only new APN");
						
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,"SMAB-T3821: Validation that Appraisal Questionnaire Correspondence WI created after approval by supervisor is linked to only new APN and not the old APN");
			
		//objCioTransfer.logout();
	}
	
}