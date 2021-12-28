package com.apas.Tests.OwnershipAndTransfer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.AppraisalActivityPage;
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
	ApasGenericPage apasGenericObj;
	AppraisalActivityPage objAppraisalActivity;
	public String assessedValueTableName = "Assessed Values for Parent Parcel";

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
		apasGenericObj = new ApasGenericPage(driver);
		objAppraisalActivity= new AppraisalActivityPage(driver);


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

	@Test(description = "SMAB-T3427,SMAB-T3306,SMAB-T3446,SMAB-T3307,SMAB-T3308,SMAB-T3691,SMAB-T3162,SMAB-T3164,SMAB-T3165,SMAB-T3166,SMAB-T3207,SMAB-T3567,SMAB-T4319:Verify that User is able to perform partial transfer and able to create mail to records ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
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
		String dateofRecordingAfterConversion ;
		
		
		if (dateOfRecordingfromHashMap[1].startsWith("0")) {
			dateofRecordingAfterConversion = dateOfRecordingfromHashMap[1].replace("0", "") + "/"
					+ dateOfRecordingfromHashMap[2].replace("0", "") + "/" + dateOfRecordingfromHashMap[0];
		} else {
			dateofRecordingAfterConversion = dateOfRecordingfromHashMap[1] + "/"
					+ dateOfRecordingfromHashMap[2].replace("0", "") + "/" + dateOfRecordingfromHashMap[0];
		}
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

		softAssert.assertTrue(!objCioTransfer.verifyElementVisible(objCioTransfer.valueFromDocTaxCityLabel),
				 "SMAB-T3206,SMAB-T4319: Verifying that Doc Tax(City) is not visible on RAT screen ");
		
		softAssert.assertTrue(objCioTransfer.verifyElementVisible(objCioTransfer.verifiedValueFromPcorLabel),
				 "SMAB-T3206,SMAB-T4319: Verifying that valueFromPcorLabel  is  visible on RAT screen ");
		
		softAssert.assertTrue(objCioTransfer.editFieldButton(objCioTransfer.verifiedValueFromPcorLabel)!=null,
				 "SMAB-T4319: Verifying that valueFromPcorLabel  is editable  on RAT screen ");

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
				"SMAB-T3427,SMAB-T3567: Verify user is not able to save grantee  record with combined ownership perentage of more than 100%");
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
		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "100");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		softAssert.assertContains(objCioTransfer.getElementText(objCioTransfer.calculateOwnershipPageMessage),
				"The sum of all grantee ownership perce﻿ntage is more than 100. Please check and make the correction",
				"SMAB-T3567: Verify user is not able to save grantee  record with combined ownership perentage of more than 100%");		
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.previousButtonLabel));
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

	@Test(description = "SMAB-T3384,SMAB-T3377,SMAB-T10081:Verify that User is able to perform CIO transfer autoconfirm when some response do come back with in 45 days wait period", dataProvider = "dpForCioAutoConfirm", dataProviderClass = DataProviders.class, groups = {
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
			
			objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
			String eventId=objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel);
			String requestOrigin=objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel);
			
			driver.navigate().back();
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);

			// Verifying that AT,WI statuses are completed after manual approval

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.statusLabel), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.statusLabel), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondenceLabel), parentAuditTrailNumber,
					"SMAB-T3384,SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedBusinessEventLabel), "",
					"SMAB-T3384:Verifying that related business event field in that outbound AT  is blank");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel), eventId,
					"SMAB-T3384:Verifying that Event ID field in the correspondence event detail page should be inherited from parent correspondence event");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel), requestOrigin,
					"SMAB-T3384:Verifying that  child correspondence event created from CIO screen inherits the Request Origin from parent event");
			
			
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

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.statusLabel), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.statusLabel), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondenceLabel), parentAuditTrailNumber,
					"SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");

			objCioTransfer.logout();

		}
	}
	/*
	 * This test method is used to assert that CIO auto confirm using batch job is
	 * able to autoconfirm transfer after no response came within 45 days of wait
	 * period
	 */

	@Test(description = "SMAB-T3377,SMAB-T10081,SMAB-T3632:Verify that User is able to perform CIO transfer autoconfirm using a batch job (Fully automated) ", dataProvider = "dpForCioAutoConfirmUsingBatchJob", dataProviderClass = DataProviders.class, groups = {
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
	
			Thread.sleep(2000); 
	
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
			objCioTransfer.waitForElementToBeVisible(10, trail.statusLabel);

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.statusLabel), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");

			// STEP 16:Verifying that outbound event is completed

			driver.navigate().to(urlForTransactionTrail);
			objCioTransfer.waitForElementToBeVisible(10, trail.statusLabel);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.statusLabel), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondenceLabel), parentAuditTrailNumber,
					"SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");
			//Navigate to the new Audit Trail record created after resubmitting the RAT for approval and validating that updated transfer code should be present in event library field
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
							+ newBusinessEventATRecordId + "/view");
			Thread.sleep(2000); //Added to handle regression failure
			objCioTransfer.waitForElementToBeVisible(10, trail.statusLabel);
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
	@Test(description = "SMAB-T3385,SMAB-T3696 : Verify user is able to use the Calculate Ownership where ownership is acquired over multiple DOV's for the same owner and owner with one DOV is completely retained", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
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

		String parentAuditTrailNumber = objWorkItemHomePage
				.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
		
		objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
		String eventId=objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel);
		String requestOrigin=objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel);
		
		driver.navigate().back();
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
	
		objCioTransfer.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
		objWorkItemHomePage.scrollToBottom();
		
		softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondenceLabel), parentAuditTrailNumber,
				"SMAB-T3385: Verifying that business event created by Recorder feed for CIO WI is child of parent Recorded correspondence event");

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedBusinessEventLabel), "",
				"SMAB-T3385:Verifying that related business event field in business event created by Recorder feed for CIO WI  is blank");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel), eventId,
				"SMAB-T3385:Verifying that Event ID field in the business event created by Recorder feed for CIO WI should be inherited from parent correspondence event");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel), requestOrigin,
				"SMAB-T3385:Verifying that business event created by Recorder feed for CIO WI inherits the Request Origin from parent event");
		

		// Step7: CIO staff user navigating to transfer screen by clicking on related
		// action link
		driver.navigate().back();
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
	
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
				(assesseeLastName + " " + assesseeFirstName).trim(),
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
				(assesseeLastName + " " + assesseeFirstName).trim(),
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

	@Test(description = "SMAB-T3525, SMAB-T3341, SMAB-T3881, SMAB-T3764, SMAB-T3631, SMAB-T3760:Verify that User is able to perform CIO transfer for recorded APN, validate all status and values in Audit Trail record and COS Document Summary detail", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void OwnershipAndTransfer_VerifyTransferActivityStatus_ReturnedAndCompleted(String loginUser) throws Exception {
		
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

		// STEP 1-login with SYS-ADMIN, delete transfer activity records rom the APN and generated WI
		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		
		salesforceAPI.update("Work_Item__c",
				"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c",
				"In Progress");
		
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_Document__c/"+recordedDocumentID+"/view");
		Thread.sleep(2000);
		objCioTransfer.Click(objCioTransfer.relatedListTab);
		String apn = objCioTransfer.getAttributeValue(objCioTransfer.apnFromRecordedDocument, "title");
		objCioTransfer.deleteTransferActivityRecords(apn);
		
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
		Thread.sleep(5000);

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
		String recordedDocumentNumber = objWorkItemHomePage.getFieldValueFromAPAS("EventID");
		String dovOnTransferActivity = objWorkItemHomePage.getFieldValueFromAPAS("DOV");
		String doeOnTransferActivity = objWorkItemHomePage.getFieldValueFromAPAS("DOE");
		
		ReportLogger.INFO("Add the Transfer Code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, objCioTransfer.CIO_EVENT_CODE_GLEASM);
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
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"), objCioTransfer.CIO_EVENT_CODE_GLEASM,
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
				"SMAB-T3525, SMAB-T3341, SMAB-T3433: Validating CIO Transfer activity status on transfer activity screen after returned by supervisor.");

		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 19-Validating WI and AUDIT Trail status after returned by supervisor.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Returned",
				"SMAB-T3525, SMAB-T3341, SMAB-T3433: Validating that Back button navigates back to WI page ");
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
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel,objCioTransfer.CIO_EVENT_CODE_GLEASM );
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
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"),objCioTransfer.CIO_EVENT_CODE_GLEASM,
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
				"SMAB-T3525, SMAB-T3341, SMAB-T3433: Validating that WI status should be completed after approval by supervisor.");
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

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"), objCioTransfer.CIO_EVENT_CODE_GLEASM,
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
		objMappingPage.login(users.RP_APPRAISER);
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
		Thread.sleep(5000);

		//STEP 29- Validate COS Document Summary details on Parcel
		objMappingPage.login(users.CIO_STAFF);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apnFromWIPage);
		objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
		Thread.sleep(2000); //Allows the grid to load completely
		
		HashMap<String, ArrayList<String>> HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
		softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(0), recordedDocumentNumber,
				"SMAB-T3760: Validate the Recorded Document Number on COS Document Summary Screen");
		softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(0), "Approved",
				"SMAB-T3760: Validate the Status on COS Document Summary Screen");
		softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(0), objCioTransfer.CIO_EVENT_CODE_GLEASM,
				"SMAB-T3760: Validate the Transfer Code on COS Document Summary Screen");	
		softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(0), objCioTransfer.updateDateFormat(doeOnTransferActivity),
				"SMAB-T3760: Validate the Event Date on COS Document Summary Screen");	
		softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(0), objCioTransfer.updateDateFormat(dovOnTransferActivity),
				"SMAB-T3760: Validate the Value Date on COS Document Summary Screen");	
		
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

		String queryForActiveAPN = "SELECT Name,Id, primary_situs__r.name, primary_situs__c FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') And primary_situs__c != NULL Limit 1";
		HashMap<String, ArrayList<String>> responseActiveAPNDetails = salesforceAPI.select(queryForActiveAPN);
		String activeApn1 = responseActiveAPNDetails.get("Name").get(0);
		String activeApnId1 = responseActiveAPNDetails.get("Id").get(0);
		String activeApnSitusId1 = responseActiveAPNDetails.get("Primary_Situs__c").get(0);
		String activeApnSitusName1 = salesforceAPI.select("SELECT Name FROM Situs__c where Id = '" + activeApnSitusId1 + "'").get("Name").get(0);
		
		
		String queryForRetiredAPN = "select Name, Id, primary_situs__r.name, primary_situs__c from Parcel__c where Status__c='Retired' And primary_situs__c != NULL limit 1";
		HashMap<String, ArrayList<String>> responseRetiredAPNDetails = salesforceAPI.select(queryForRetiredAPN);
		String retiredApn = responseRetiredAPNDetails.get("Name").get(0);
		String retiredApnId = responseRetiredAPNDetails.get("Id").get(0);
		String retiredApnSitusId = responseRetiredAPNDetails.get("Primary_Situs__c").get(0);
		String retiredApnSitusName = salesforceAPI.select("SELECT Name FROM Situs__c where Id = '" + retiredApnSitusId + "'").get("Name").get(0);
		
		HashMap<String, ArrayList<String>> responsePUCDetails1 = salesforceAPI
				.select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responsePUCDetails2 = salesforceAPI
				.select("SELECT id, Name FROM PUC_Code__c where Name in ('99-RETIRED PARCEL') limit 1");
		
		String legalDescriptionValue1 = "Test Legal Description PM 85/25-260";
		String legalDescriptionValue2 = "Test Legal Description PM 85/25-270";

		jsonObject1.put("Short_Legal_Description__c", legalDescriptionValue1);
		jsonObject1.put("PUC_Code_Lookup__c", responsePUCDetails1.get("Id").get(0));
		salesforceAPI.update("Parcel__c", activeApnId1, jsonObject1);

		jsonObject2.put("Short_Legal_Description__c", legalDescriptionValue2);
		jsonObject2.put("PUC_Code_Lookup__c", responsePUCDetails2.get("Id").get(0));
		salesforceAPI.update("Parcel__c", retiredApnId, jsonObject2);

		// Delete existing Ownership records from the Active parcel
		objMappingPage.deleteOwnershipFromParcel(activeApnId1);

		// Step 1: Executing the recorder feed batch job to generate CIO WI & Add ownership records in the parcels
		objCioTransfer.generateRecorderJobWorkItems("DE", 1);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);
		String queryWI = "Select Id from Work_Item__c where Name = '"+cioWorkItem+"'";
		HashMap<String, ArrayList<String>> responseWI = salesforceAPI.select(queryWI);
		
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
		driver.navigate().to("https://smcacre--"+execEnv+
				".lightning.force.com/lightning/r/Work_Item__c/"+responseWI.get("Id").get(0)+"/view");
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
		Thread.sleep(1000); // Allows the WI to load completely to avoid regression failure
		
		//Get the APN Name and Id
		String primarySitusValue = "";
		String activeApn2 = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		HashMap<String, ArrayList<String>> response = salesforceAPI.select("Select Id, Primary_Situs__c from Parcel__c where Name = '"+activeApn2+"'");	
		String activeApnId2 = response.get("Id").get(0);
		if (response.get("Primary_Situs__c").get(0) != null) {
			primarySitusValue = salesforceAPI.select("SELECT Name FROM Situs__c where Id = '" + response.get("Primary_Situs__c").get(0) + "'").get("Name").get(0);
		}
		
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com"
				+ "/lightning/r/Parcel__c/" + activeApnId2 + "/view");
		objParcelsPage.waitForElementToBeVisible(20, objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		
		//Open WI again and navigate to CIO screen
		driver.navigate().to("https://smcacre--"+execEnv+
				".lightning.force.com/lightning/r/Work_Item__c/"+responseWI.get("Id").get(0)+"/view");
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
		Thread.sleep(1000); // Allows the WI to load completely to avoid regression failure
		
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
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
		
		objCioTransfer.scrollToElement(objCioTransfer.situsOnTransferActivityLabel);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),primarySitusValue,
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + activeApn2);

		// Step6: Update the APN value to Retired APN value and validate values
		ReportLogger.INFO("Update the APN value to a Retired Parcel value");
		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.clearSelectionFromLookup(objCioTransfer.ApnLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, retiredApn);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveLabel));
		Thread.sleep(5000); // Allows the record to save properly

		softAssert.assertEquals(
				objCioTransfer.getElementText(objCioTransfer.shortLegalDescriptionOnTransferActivityLabel),
				legalDescriptionValue2,
				"SMAB-T3232: Validate the Legal Description on CIO Transfer screen for " + retiredApn);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.pucCodeTransferActivityLabel),
				responsePUCDetails2.get("Name").get(0),
				"SMAB-T3232: Validate the PUC on CIO Transfer screen for " + retiredApn);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),
				retiredApnSitusName,
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + retiredApn);
		softAssert.assertTrue(objCioTransfer.verifyElementExists(objCioTransfer.warningMessageArea),
				"SMAB-T3232: Validate that warning message is displayed on CIO Transfer screen for Retired Parcel");

		// Step7: Update the APN value to Active APN value and validate values
		ReportLogger.INFO("Update the APN value to an Active Parcel value");
		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.clearSelectionFromLookup(objCioTransfer.ApnLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, activeApn1);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveLabel));
		Thread.sleep(5000); // Allows the record to save properly

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
				activeApnSitusName1,
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
		objCioTransfer.waitForElementToBeVisible(20, objParcelsPage.numberOfMailToOnParcelLabel);
		String numOfMailToRecordOnParcel = objCioTransfer.getElementText(objParcelsPage.numberOfMailToOnParcelLabel);

		if (!numOfMailToRecordOnParcel.equals("(0)")) {
			ReportLogger.INFO("There is/are Mail-To record(s) present on the parcel");
			HashMap<String, ArrayList<String>> mailToTableDataHashMap = objParcelsPage
					.getParcelTableDataInHashMap("Mail-To");
			String formattedName1 = mailToTableDataHashMap.get("Formatted Name 1").get(0);

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.numberOfGrantorLabel);
			objCioTransfer.scrollToBottom();
			objCioTransfer.clickViewAll("CIO Transfer Mail To");

			// Step9a: Validate the details in the grid
			HashMap<String, ArrayList<String>> HashMapMailTo = objCioTransfer.getGridDataInHashMap();
			softAssert.assertContains(formattedName1, HashMapMailTo.get("Formatted Name1").get(0),
					"SMAB-T3232: Validate the Formatted Name1 of Mail-To record");
		} else {
			ReportLogger.INFO("Validate if there is no Mail-To record on the parcel");
			softAssert.assertTrue(numOfMailToRecordOnRAT.contains("0"),
					"SMAB-T3232: Validate that there are no Mail-To Records");
		}

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

	@Test(description = "SMAB-T4112,SMAB-T3461,SMAB-T3462 -Verify CIO Staff and Appriasal Able to edit Mail-To records", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
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
		softAssert.assertTrue(objCioTransfer.verifyElementVisible(objCioTransfer.mailingStatefield), "SMAB-T3461,SMAB-T3462: Verify that Mailing state field is present on mail to record on CIO transfer screen");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Clone));
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
	@Test(description = "SMAB-T3384,SMAB-T3821,SMAB-T3430 : Ownership And Transfers - Verify AT Event and WI are transferred  to new APN when APN# is edited in RAT/CIO Transfer Screen", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement","RecorderIntegration" }, enabled = true)
	public void CIO_TransferScreen_UpdateAPN(String loginUser) throws Exception {

		JSONObject jsonObjectAPN = objCioTransfer.getJsonObject();
		
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

		jsonObjectAPN.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObjectAPN.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObjectAPN.put("District__c",districtValue);
		jsonObjectAPN.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		
		String queryCountMailTORecords = "SELECT count(id) FROM Mail_To__c where parcel__c='"+responseAPNDetails.get("Id").get(0)+"' and status__c='active'";
		String countActiveMaiToRecords=salesforceAPI.select(queryCountMailTORecords).get("expr0").get(0);;

		HashMap<String, ArrayList<String>> responseMailToDetails=salesforceAPI.select("SELECT Formatted_Name_2__c ,Name,Care_Of__c,Country__c FROM Mail_To__c where parcel__c='"+responseAPNDetails.get("Id").get(0)+"' and status__c='Active'");

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

		String parentAuditTrailNumber = objWorkItemHomePage
				.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
		
		objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
		String eventId=objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel);
		String requestOrigin=objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel);
		
		driver.navigate().back();
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
		
		// STEP 7-Clicking on buisness event genertaed by recorder feed 

		objCioTransfer.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
		objWorkItemHomePage.scrollToBottom();
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.relatedActionLabel),"Click Here",
				"SMAB-T3430: Verify related action link name on Business event page generated by recorder feed for CIO is 'Click Here' ");
		
		objCioTransfer.Click(objWorkItemHomePage.reviewLink);

		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCioTransfer.waitForElementToBeVisible(30,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		
		String urlForRATScreen = driver.getCurrentUrl();
		String  RATId=urlForRATScreen.split("/")[6];

		softAssert.assertEquals(RATId,recordeAPNTransferID,
				"SMAB-T3430: Verify that User is able to launch CIO Transfer Activity from Original BE (associated with the Transfer Record)");
		
		// STEP 8-Creating the new grantee

		hashMapOwnershipAndTransferGranteeCreationData.put("Owner Percentage", "100");
		hashMapOwnershipAndTransferGranteeCreationData.put("First Name", "");
		hashMapOwnershipAndTransferGranteeCreationData.put("Ownership Start Date", "");

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);

		// STEP 9-Updating The APN from transfer screen 
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(30,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));

		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.ApnLabel);
		objCioTransfer.Click(objCioTransfer.crossIconAPNEditField);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, apnToUpdate);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		ReportLogger.INFO("APN   updated successfully");
		
		// fetch the new CIO transfer mail  to record details for updated APN 
		Thread.sleep(6000);
		HashMap<String, ArrayList<String>> responseCIOTransferMailToDetails=salesforceAPI.select("SELECT Formatted_Name1__c ,Formatted_Name2__c ,Care_of__c, Country__c  FROM CIO_Transfer_Mail_To__c where Recorded_APN_Transfer__c ='"+recordeAPNTransferID+"'");

		// STEP 10-Verify the updated details on screen as per new APN

		String updatedAPN_ON_TransferScreen=objCioTransfer.getElementText(objCioTransfer.apnOnTransferActivityLabel);
		
		String queryAPNIdFromTransferSCreen = "select  Id from Parcel__c where name='"+updatedAPN_ON_TransferScreen+"'";
		String APNIdFromTransferSCreen=salesforceAPI.select(queryAPNIdFromTransferSCreen).get("Id").get(0);
		
		
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
		
		softAssert.assertEquals( responseMailToDetails.get("Name").get(0),responseCIOTransferMailToDetails.get("Formatted_Name1__c").get(0),
				"SMAB-T3821: Verify that "
				+ "formatted name 2 in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Formatted_Name_2__c").get(0),responseCIOTransferMailToDetails.get("Formatted_Name2__c").get(0),
				"SMAB-T3821: Verify that "
				+ "formatted name 1 in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Care_Of__c").get(0),responseCIOTransferMailToDetails.get("Care_of__c").get(0),
				"SMAB-T3821: Verify that "
				+ "Care_Of__c in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Country__c").get(0),responseCIOTransferMailToDetails.get("Country__c").get(0),
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
		objCioTransfer.waitForElementToBeVisible(20,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));

		String  auditTrailId=urlForTransactionTrail.split("/")[6];
		
		String auditTrailName =salesforceAPI.select("SELECT Name FROM Transaction_Trail__c where id='"+auditTrailId+"'").get("Name").get(0);
		
		String auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		String countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		
		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
				"SMAB-T3821: Validation that  correspondence event created after APN update is linked to the update APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
				"SMAB-T3821: Validation that  correspondence event created after APN update is linked to only the updated APN and not the old APN");
		

		// STEP 12-verifying the WI and business event created after creating APN & legal description WI from component action
		
		String workItemNumber=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		String workItemIdQuery = "SELECT id FROM Work_Item__c where name ='"+workItemNumber+"' ";
		
		String workItemId=salesforceAPI.select(workItemIdQuery).get("Id").get(0);
		
		String apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		String countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,
				"SMAB-T3821: Validation that APN & legal description WI created is linked to only new APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,
				"SMAB-T3821: Validation that APN & legal description WI created is linked to only new APN and not the old APN");
		
		String auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		String responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		JSONObject responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();

		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
					"SMAB-T3821: Validation that  business event created from APN & legal description WI after APN update is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3821: Validation that  business event created from APN & legal description WI after APN update is linked to only the updated APN and not the old APN");
			
		String auditTrailIdLegalDescriptionWI =salesforceAPI.select("SELECT id FROM Transaction_Trail__c where name='"+auditTrailName+"'").get("Id").get(0);
		 
		driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Transaction_Trail__c/" + auditTrailIdLegalDescriptionWI + "/view");
		
		objMappingPage.scrollToBottom();
		softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondenceLabel), parentAuditTrailNumber,
				"SMAB-T3384: Verifying that business event created for  APN & Legal description WI  is child of parent Recorded correspondence event");

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedBusinessEventLabel), "",
				"SMAB-T3384:Verifying that related business event field in business event created for  APN & Legal description WI  is blank");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel), eventId,
				"SMAB-T3384:Verifying that Event ID field in the business event created for  APN & Legal description WI detail page should be inherited from parent correspondence event");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel), requestOrigin,
				"SMAB-T3384:Verifying that business event created for  APN & Legal description WI inherits the Request Origin from parent event");
		
		// STEP 13-verifying the original CIO WI and Recorder feed Audit trails  created are linked to the updated APN

		 workItemIdQuery = "SELECT id,APN__c FROM Work_Item__c where name ='"+workItemNo1+"' ";
		
		 workItemId=salesforceAPI.select(workItemIdQuery).get("Id").get(0);
		
	     String  apnOnWorkItemDetailspage=salesforceAPI.select(workItemIdQuery).get("APN__c").get(0);

		 apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		 countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' and parcel__c='"+apnIDFromWIPage+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,
				"SMAB-T3821: Validation that CIO  WI created by recorder feed is linked to only new APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),0,
				"SMAB-T3821: Validation that CIO WI created by recorder feed is linked to only new APN and not the old APN");
		
		softAssert.assertEquals(apnOnWorkItemDetailspage,APNIdFromTransferSCreen,"SMAB-T3821: Validation that CIO WI that was created by recorder feed has  the new APN In details tab  ");

		 auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		 responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		 responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
					"SMAB-T3821: Validation that  business event created for  CIO WI by recorder feed , is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3821: Validation that  business event created for  CIO WI by recorder feed is linked to only the updated APN and not the old APN");
			
		// STEP 14-Clicking on submit for approval quick action button
		
		driver.navigate().back();
		objCioTransfer.waitForElementToBeVisible(20,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));

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

		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
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

		workItemQuery = "SELECT Id,name,APN__c   FROM Work_Item__c where Type__c='Appraiser' And Sub_Type__c ='Appraisal Activity' and  status__c='In Pool' order by createdDate desc limit 1";
		 workItemNumber = salesforceAPI.select(workItemQuery).get("Name").get(0);

		workItemId=salesforceAPI.select(workItemQuery).get("Id").get(0);
        apnOnWorkItemDetailspage=salesforceAPI.select(workItemQuery).get("APN__c").get(0);
       
		apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,
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

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
					"SMAB-T3821: Validation that  business event created for  Appraisal WI  , is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3821: Validation that  business event created for  Appraisal WI is linked to only the updated APN and not the old APN");
			
		
		workItemQuery = "SELECT Id,name  FROM Work_Item__c where Type__c='Appraiser' And Sub_Type__c ='Questionnaire Correspondence' and  status__c='In Pool' order by createdDate desc limit 1";
		workItemId=salesforceAPI.select(workItemQuery).get("Id").get(0);

		apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,"SMAB-T3821: Validation that Appraisal  Questionnaire Correspondence WI created after approval by supervisor is linked to only new APN");
						
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,"SMAB-T3821: Validation that Appraisal Questionnaire Correspondence WI created after approval by supervisor is linked to only new APN and not the old APN");
			
		objCioTransfer.logout(); 
	}

	/*
	 * This test method is used to assert that CIO auto confirm using batch job is
	 * able to autoconfirm transfer after no response came within 45 days of wait
	 * period
	 */

	@Test(description = "SMAB-T3759,SMAB-T3568,SMAB-T3636:Verify a new AT Record of type Business Event is created when a Transfer code (Not Auto- Conformable to another Transfer code) is added in the transfer activity and it is submitted to CIO Supervisor for review ", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void OwnershipAndTransfer_VerifyCioTransferAutoConfirmUsingBatch(String loginSystemAdmin) throws Exception {

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
			softAssert.assertEquals(granteeHashMap.get("Recorded Document").get(0),"",
					"SMAB-T3568: Verifying that Recorded Document field of new ownership  is same as document name of recorded document for CIO tranfer");
			ReportLogger.INFO("Putting Auto confirm date prior to 45 days ");
			salesforceAPI.update("Recorded_APN_Transfer__c", recordeAPNTransferID, "Auto_Confirm_Start_Date__c",
					"2021-04-07");
						
			
			//updating recorder document on current ownership on parcel record 
			salesforceAPI.update("Property_Ownership__c", ownershipId,"Recorded_Document__c", recordedDocumentID);

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.calculateOwnershipButtonLabel);
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.nextButton);
			objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
			String eventID=objCioTransfer.getFieldValueFromAPAS(objCioTransfer.eventIDLabel);
			softAssert.assertEquals(granteeHashMap.get("Recorded Document").get(0),eventID,
					"SMAB-T3568: Verifying that Recorded Document field of new ownership  is same as document name of recorded document for CIO tranfer");

			// Step 9: adding transfer code 

			
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			ReportLogger.INFO("Add the Transfer Code");
			objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
			objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-DIVIDM");
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
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
			String newBusinessEventATRecordId = salesforceAPI.select("SELECT Id, Name FROM Transaction_Trail__c order by Name desc limit 1").get("Id").get(0);
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
					+ newBusinessEventATRecordId + "/view");
	
	
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
					"SMAB-T3632: Validating that audit trail status should be open after submit for approval.");
	        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"),"CIO-DIVIDM",
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

			// Step 12: start autoconfirm batch job

			salesforceAPI.generateReminderWorkItems(SalesforceAPI.CIO_AUTOCONFIRM_BATCH_JOB);

			// Step 13: login with cio staff to validate that auto confirm has taken place
			// for impending transfer

			objCioTransfer.login(users.CIO_STAFF);
			
			//Step 14 :Navigate to the new Audit Trail record created after resubmitting the RAT for approval and validating that updated transfer code should be present in event library field
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
							+ newBusinessEventATRecordId + "/view");
			
			objCioTransfer.waitForElementToBeVisible(10, trail.statusLabel);
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Completed",
							"SMAB-T3759: Validating that audit trail status should be Completed after submit for approval.");
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Event Library"),"CIO-DIVIDM",
							"SMAB-T3759: Validating the 'Event Library' field after update transer code value in Audit Trail record.");
			// STEP 15 : navigate to Mail-To record 
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/related/CIO_Transfer_Mail_To__r/view");
			softAssert.assertEquals(objMappingPage.getGridDataInHashMap(0).get("End Date").get(0), "",
					"SMAB-T3636: Validating that end date should not be populated on mailTo");
			softAssert.assertEquals(objMappingPage.getGridDataInHashMap(0).get("Formatted Name1").get(0), acesseName,
					"SMAB-T3636: Validating that end date should not be populated on mailTo");
			
			objCioTransfer.logout();

		}
	
	
	@Test(description = "SMAB-T3345, SMAB-T3392- Verify User is able to create WI from CIO transfer activity screen and Verify Event ID and APN should be displayed in WI and AT Business Event linked to WI", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void CIOTransfer_CreateWiFromCioTransferActivityScreen(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");

		// Creating work item

		String recordedDocumentID = salesforceAPI
				.select(" SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);

		salesforceAPI.update("Work_Item__c",
				"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c",
				"In Progress");
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Query to fetch WI
		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);

		// Step 2:Opening the Work Item module
		objParcelsPage.globalSearchRecords(workItemNo);

		// Step 3: Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		String firstBussinessEventName = objWorkItemHomePage.firstRelatedBuisnessEvent.getText();
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();

		ReportLogger.INFO("Switch to the Appraisal Activity Screen.");
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objWorkItemHomePage.waitForElementToBeClickable(50, objCioTransfer.componentActionsButtonLabel);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateCioApnAndLegalDescriptionWorkItem");

		// Step 4: Creating Manual work item for the Parcel
		String workItemSecond = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		objParcelsPage.globalSearchRecords(workItemSecond);

		// Step5: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.waitForElementToBeClickable(10, objWorkItemHomePage.completedOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.markStatusAsCompleteBtn);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		String newFirstBussinessEventName = objWorkItemHomePage.firstRelatedBuisnessEvent.getText();
		String newSecondAuditTrailID = salesforceAPI.select(
				"SELECT Id,Status__c,Name FROM Transaction_Trail__c where Name='" + newFirstBussinessEventName + "'")
				.get("Id").get(0);
		
		// Step 6: Navigating to the Audit trail page and verifying the details.
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ newSecondAuditTrailID + "/view");
		objCioTransfer.waitUntilPageisReady(driver);
		String relatedCorrespondenceOnNewScreen = objParcelsPage.getFieldValueFromAPAS("Related Correspondence");
		String auditTrailName = objParcelsPage.getFieldValueFromAPAS("Name");
		String statusOnNewScreen = objParcelsPage.getFieldValueFromAPAS("Status");
		softAssert.assertEquals(auditTrailName, newFirstBussinessEventName,
				"SMAB-T3345, SMAB-T3392-Verify that the Audit Trail Name is same on Work Item");
		softAssert.assertEquals(firstBussinessEventName, relatedCorrespondenceOnNewScreen,
				"SMAB-T3345, SMAB-T3392-Verify that the Event ID is same on Child Audit Trail");
		softAssert.assertEquals(statusOnNewScreen, "Completed",
				"SMAB-T3345, SMAB-T3392-Verify that the status should be completed.");
		ReportLogger.INFO("Completed the validation!");
		
		objWorkItemHomePage.logout();

	}

/*
 * Verify that user is able to view COS Document Summary for a parcel record
 */

	@Test(description = "SMAB-T3760, SMAB-T3761, SMAB-T3838, SMAB-T3839, SMAB-T4210 : Verify that user is able to view COS Document Summary for a parcel record", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
		"Regression", "ChangeInOwnershipManagement", "RecorderIntegration", "UnrecordedEvent" })
	public void OwnershipAndTransfer_COS_DocumentSummmary(String loginUser) throws Exception {

	int i=1;
	String execEnv = System.getProperty("region");
	String unrecordedEventData=testdata.UNRECORDED_EVENT_DATA;
	JSONObject jsonForTransferActivityStatus = objCioTransfer.getJsonObject();
	
	Map<String, String> dataToCreateUnrecordedEventMap = objUtil
			.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
	Map<String, String> hashMapCreateOwnershipRecordData = objUtil
			.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");
	
	String recordedItemQuery = "SELECT Id, Name from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1 limit 1";
	String recordedDocumentID = salesforceAPI.select(recordedItemQuery).get("Id").get(0);

	// STEP 1-login with SYS-ADMIN, delete transfer activity records from the APN and generated WI
	objMappingPage.login(loginUser);
	objMappingPage.searchModule(PARCELS);
	
	salesforceAPI.update("Work_Item__c",
			"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c",
			"In Progress");
	
	driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_Document__c/"+recordedDocumentID+"/view");
	Thread.sleep(2000);
	objCioTransfer.Click(objCioTransfer.relatedListTab);
	String apn = objCioTransfer.getAttributeValue(objCioTransfer.apnFromRecordedDocument, "title");
	objCioTransfer.deleteTransferActivityRecords(apn);
	
	objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

	// STEP 2-Query to fetch WI
	String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";
	String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
	objMappingPage.searchModule(WORK_ITEM);
	objMappingPage.globalSearchRecords(workItemNo);
	String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
	String apnIdFromWIPage = salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0);
	objCioTransfer.deleteOwnershipFromParcel(apnIdFromWIPage);
	softAssert.assertEquals(apn, apnFromWIPage,
			"SMAB-T3760: Validate the APN on the recorded transfer activity");
	
	
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

	objMappingPage.searchModule(WORK_ITEM);
	objMappingPage.globalSearchRecords(workItemNo);

	String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
	HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

	// STEP 5-Finding the recorded apn transfer id
	String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
	objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
	objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
	objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
	
	// STEP 6-Clicking on related action link
	objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
	String parentWindow = driver.getWindowHandle();
	objWorkItemHomePage.switchToNewWindow(parentWindow);
	softAssert.assertContains(driver.getCurrentUrl(), navigationUrL.get("Navigation_Url__c").get(0),
			"SMAB-T3306:Validating that user navigates to CIo transfer screenafter clicking on related action hyperlink");
	
	String recordedDocumentNumber = objWorkItemHomePage.getFieldValueFromAPAS("EventID");
	String dovOnTransferActivity = objWorkItemHomePage.getFieldValueFromAPAS("DOV");
	String doeOnTransferActivity = objWorkItemHomePage.getFieldValueFromAPAS("DOE");
	
	// STEP 7 - Add the Transfer Code
	ReportLogger.INFO("Add the Transfer Code");
	objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
	objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
	objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, objCioTransfer.CIO_EVENT_CODE_COPAL);
	objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
	Thread.sleep(2000);		
	
	// STEP 8 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMap<String, ArrayList<String>> HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(0), recordedDocumentNumber,
			"SMAB-T4210: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(0), "In Progress",
			"SMAB-T4210: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(0), objCioTransfer.CIO_EVENT_CODE_COPAL,
			"SMAB-T4210: Validate the Transfer Code on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(0), objCioTransfer.updateDateFormat(dovOnTransferActivity),
			"SMAB-T4210: Validate the Event Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(0), objCioTransfer.updateDateFormat(doeOnTransferActivity),
			"SMAB-T4210: Validate the Value Date on COS Document Summary Screen");	
	
	// STEP 9 - Update the Transfer Code
	driver.navigate().to("https://smcacre--" + execEnv
			+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
	
	ReportLogger.INFO("Update the Transfer Status : NOAC - No Action");
	objCioTransfer.editRecordedApnField(objCioTransfer.transferStatus);
	objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatus);
	objCioTransfer.selectOptionFromDropDown(objCioTransfer.transferStatus, "NOAC - No Action");
	objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
	Thread.sleep(2000);		
	HashMapDocumentDetails.clear();
	
	// STEP 10 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(0), recordedDocumentNumber,
			"SMAB-T3760: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(0), "NOAC - No Action",
			"SMAB-T3760: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(0), objCioTransfer.CIO_EVENT_CODE_COPAL,
			"SMAB-T3760: Validate the Transfer Code on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(0), objCioTransfer.updateDateFormat(dovOnTransferActivity),
			"SMAB-T3760: Validate the Value Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(0), objCioTransfer.updateDateFormat(doeOnTransferActivity),
			"SMAB-T3760: Validate the Event Date on COS Document Summary Screen");	
	
	// STEP 11 - Update the Transfer Code
	driver.navigate().to("https://smcacre--" + execEnv
			+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
	
	ReportLogger.INFO("Update the Transfer Status : CONV - Converted");
	objCioTransfer.editRecordedApnField(objCioTransfer.transferStatus);
	objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatus);
	objCioTransfer.selectOptionFromDropDown(objCioTransfer.transferStatus, "CONV - Converted");
	objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
	Thread.sleep(2000);		
	HashMapDocumentDetails.clear();
	
	// STEP 12 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(0), recordedDocumentNumber,
			"SMAB-T3839: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(0), "CONV - Converted",
			"SMAB-T3839: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(0), objCioTransfer.CIO_EVENT_CODE_COPAL,
			"SMAB-T3839: Validate the Transfer Code on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(0), objCioTransfer.updateDateFormat(dovOnTransferActivity),
			"SMAB-T3839: Validate the Value Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(0), objCioTransfer.updateDateFormat(doeOnTransferActivity),
			"SMAB-T3839: Validate the Event Date on COS Document Summary Screen");	
	
	// STEP 13 - Update the Transfer Code
	driver.navigate().to("https://smcacre--" + execEnv
			+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
	
	ReportLogger.INFO("Update the Transfer Status : ReApproved");
	objCioTransfer.editRecordedApnField(objCioTransfer.transferStatus);
	objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatus);
	objCioTransfer.selectOptionFromDropDown(objCioTransfer.transferStatus, "ReApproved");
	objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
	Thread.sleep(2000);		
	HashMapDocumentDetails.clear();
	
	// STEP 14 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(0), recordedDocumentNumber,
			"SMAB-T3838: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(0), "Approved",
			"SMAB-T3838: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(0), objCioTransfer.CIO_EVENT_CODE_COPAL,
			"SMAB-T3838: Validate the Transfer Code on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(0), objCioTransfer.updateDateFormat(dovOnTransferActivity),
			"SMAB-T3838: Validate the Value Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(0), objCioTransfer.updateDateFormat(doeOnTransferActivity),
			"SMAB-T3838: Validate the Event Date on COS Document Summary Screen");	
	
	objCioTransfer.Click(objCioTransfer.crossButton);
	HashMapDocumentDetails.clear();
	
	// STEP 15 - Create Unrecorded event
	objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
	String unRecordedDocumentNumber = objWorkItemHomePage.getFieldValueFromAPAS("EventID");
	String unrecordedTransferActivityURL = driver.getCurrentUrl();
	
	// STEP 16 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
	if(HashMapDocumentDetails.get("Status").get(0).equals("In Progress")) i=0;
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(i), unRecordedDocumentNumber,
			"SMAB-T3760: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(i), "In Progress",
			"SMAB-T3760: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(i), "",
			"SMAB-T3760: Validate the Transfer Code on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(i), objCioTransfer.updateDateFormat("07/04/2021"),
			"SMAB-T3760: Validate the Event Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(i), objCioTransfer.updateDateFormat("07/04/2021"),
			"SMAB-T3760: Validate the Value Date on COS Document Summary Screen");
	
	
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(i==0?i+1:i-1), recordedDocumentNumber,
			"SMAB-T3760: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(i==0?i+1:i-1), "Approved",
			"SMAB-T3760: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(i==0?i+1:i-1), objCioTransfer.CIO_EVENT_CODE_COPAL,
			"SMAB-T3760: Validate the Transfer Code on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(i==0?i+1:i-1), objCioTransfer.updateDateFormat(doeOnTransferActivity),
			"SMAB-T3760: Validate the Event Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(i==0?i+1:i-1), objCioTransfer.updateDateFormat(dovOnTransferActivity),
			"SMAB-T3760: Validate the Value Date on COS Document Summary Screen");
	
	objCioTransfer.Click(objCioTransfer.crossButton);
	HashMapDocumentDetails.clear();
	
	// STEP 17- Update the Transfer Code
	driver.navigate().to("https://smcacre--" + execEnv
			+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
	
	ReportLogger.INFO("Update the Transfer Status : Reopened");
	objCioTransfer.editRecordedApnField(objCioTransfer.transferStatus);
	objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatus);
	objCioTransfer.selectOptionFromDropDown(objCioTransfer.transferStatus, "Reopened");
	objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
	Thread.sleep(2000);		
	
	// STEP 18 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(0), unRecordedDocumentNumber,
			"SMAB-T3760: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(0), "In Progress",
			"SMAB-T3760: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(0), "",
			"SMAB-T3760: Validate the Transfer Code on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(0), objCioTransfer.updateDateFormat("07/04/2021"),
			"SMAB-T3760: Validate the Event Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(0), objCioTransfer.updateDateFormat("07/04/2021"),
			"SMAB-T3760: Validate the Value Date on COS Document Summary Screen");	
	softAssert.assertTrue(!HashMapDocumentDetails.containsValue("Reopened"),
			"SMAB-T3760: Validate that transfer activity with 'Reopened' status is not present in COS Document Summary Screen");
	softAssert.assertTrue(!HashMapDocumentDetails.containsValue("Approved"),
			"SMAB-T3760: Validate that transfer activity with 'Approved' status is not present in COS Document Summary Screen");
	 
	objCioTransfer.Click(objCioTransfer.crossButton);
	HashMapDocumentDetails.clear();
	
	// STEP 19- Update the Transfer Code
	driver.navigate().to("https://smcacre--" + execEnv
			+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
	
	ReportLogger.INFO("Update the Transfer Status : Submitted for Approval");
	objCioTransfer.editRecordedApnField(objCioTransfer.transferStatus);
	objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatus);
	objCioTransfer.selectOptionFromDropDown(objCioTransfer.transferStatus, "Submitted for Approval");
	objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
	Thread.sleep(2000);		
	
	// STEP 20 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();	
	softAssert.assertEquals(HashMapDocumentDetails.get("Recorded Document Number").get(0), unRecordedDocumentNumber,
			"SMAB-T3760: Validate the Recorded Document Number on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Status").get(0), "In Progress",
			"SMAB-T3760: Validate the Status on COS Document Summary Screen");
	softAssert.assertEquals(HashMapDocumentDetails.get("Transfer Code").get(0), "",
			"SMAB-T3760: Validate the Transfer Code on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Event Date").get(0), objCioTransfer.updateDateFormat("07/04/2021"),
			"SMAB-T3760: Validate the Event Date on COS Document Summary Screen");	
	softAssert.assertEquals(HashMapDocumentDetails.get("Value Date").get(0), objCioTransfer.updateDateFormat("07/04/2021"),
			"SMAB-T3760: Validate the Value Date on COS Document Summary Screen");	
	softAssert.assertTrue(!HashMapDocumentDetails.containsValue("Reopened"),
			"SMAB-T3760: Validate that transfer activity with 'Reopened' status is not present in COS Document Summary Screen");
	softAssert.assertTrue(!HashMapDocumentDetails.containsValue("Submitted for Approval"),
			"SMAB-T3760: Validate that transfer activity with 'Submitted for Approval' status is not present in COS Document Summary Screen");
	
	objCioTransfer.Click(objCioTransfer.crossButton);
	HashMapDocumentDetails.clear();
	
	// STEP 21- Update the Transfer Code
	driver.navigate().to(unrecordedTransferActivityURL);
	ReportLogger.INFO("Update the Transfer Code : CIO-COPAL");
	objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
	objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
	objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, objCioTransfer.CIO_EVENT_CODE_COPAL);
	objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
	Thread.sleep(2000);
	
	// STEP 22 - Validate the COS Document Summary screen
	objMappingPage.searchModule(PARCELS);
	objMappingPage.globalSearchRecords(apnFromWIPage);
	objCioTransfer.clickQuickActionButtonOnTransferActivity(objCioTransfer.documentSummaryButton);
	objWorkItemHomePage.waitForElementToBeVisible(objCioTransfer.documentSummaryCaption, 10);
	
	HashMapDocumentDetails = objCioTransfer.getGridDataInHashMap();		
	softAssert.assertTrue(!HashMapDocumentDetails.containsValue("Reopened"),
			"SMAB-T3760: Validate that transfer activity with 'Reopened' status is not present in COS Document Summary Screen");
	softAssert.assertTrue(!HashMapDocumentDetails.containsValue("Submitted for Approval"),
			"SMAB-T3760: Validate that transfer activity with 'Submitted for Approval' status is not present in COS Document Summary Screen");
	softAssert.assertTrue(!HashMapDocumentDetails.containsValue("In Progress"),
			"SMAB-T3760: Validate that transfer activity with 'In Progress' status is not present in COS Document Summary Screen");
	
	objCioTransfer.logout();
	
	}

	/*
	 * This method is used to Validate that user is able to update the status of Appraisal activity and related Work item to 'Submit for approval', 'Return','Approve' option using Quick Action button
	 * Users: RP Appraiser, RP Supervisor 
	 */
	
	@Test(description = "SMAB-T3668, SMAB-T3626, SMAB-T3627, SMAB-T3628: Validate that user is able to update the status of Appraisal activity and related Work item to Return using the 'Return' option using Quick Action button", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void RecorderIntegration_TestCIOAppraisalActivity(String loginUser) throws Exception {

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		Map<String, String> hashMapMailToData = objUtil.generateMapFromJsonFile(MailtoData, "createMailToData");

		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecord");
		// STEP 1 : Create Appraiser activity
		String[] arrayForWorkItemAfterCIOSupervisorApproval = objCioTransfer
				.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment", "CIO-SALE",
						hashMapMailToData, hashMapOwnershipAndTransferGranteeCreationData,
						hashMapCreateOwnershipRecordData, hashMapCreateAssessedValueRecord);
		
		// STEP 2 : Login as Appriaser user
		ReportLogger.INFO("Login as Appriaser user");
		objMappingPage.login(users.RP_APPRAISER);
		String workItemForAppraiser = arrayForWorkItemAfterCIOSupervisorApproval[0];
		objCioTransfer.globalSearchRecords(workItemForAppraiser);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		String workItemOfAppraisalActivity = driver.getCurrentUrl();
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		
		// STEP 3 - Navigating to Appraisal Activity Screen
		ReportLogger.INFO("Navigating to Appraisal Activity Screen");
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		String navigationUrl = objCioTransfer.getFieldValueFromAPAS("Navigation Url").trim();
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		String appraisalActivityUrl = driver.getCurrentUrl();
		softAssert.assertContains(appraisalActivityUrl, navigationUrl.substring(13, 31),
				"SMAB-T3677:Navigation URL from details page is matching with the AAN URL");
		ReportLogger.INFO("Click on submit for approval");
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.finishButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		Thread.sleep(3000);
		String EventID = objCioTransfer.getFieldValueFromAPAS("EventID");
		String APN = objCioTransfer.getFieldValueFromAPAS("APN");
		String DOR = objCioTransfer.getFieldValueFromAPAS("DOR");
		String DOE = objCioTransfer.getFieldValueFromAPAS("DOE");
		String DOV = objCioTransfer.getFieldValueFromAPAS("DOV");
		String PUCCode = objCioTransfer.getFieldValueFromAPAS("PUCCode");
		String EventCode = objCioTransfer.getFieldValueFromAPAS("Event Code");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("Appraiser Activity Status"),
				"Submit for Approval", "SMAB-T3668Status of the Appraisal activity is submitted for approval");
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("Status").trim(),
				objParcelsPage.SubmittedForApprovalButton.trim(),
				"SMAB-T3668-Status of the work Item is Submitted for approval");
		Thread.sleep(2000);
		objWorkItemHomePage.logout();
		
		// STEP 3: Login as Appraiser supervisor
		ReportLogger.INFO("Login as Appraiser supervisor");
		objMappingPage.login(users.APPRAISAL_SUPERVISOR);
		objCioTransfer.globalSearchRecords(workItemForAppraiser);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 4 -Navigating to appraisal activity screen
		ReportLogger.INFO("Navigating to appraisal activity screen");
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow2 = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow2);

		// STEP 5 - Click on Approve button
		ReportLogger.INFO("Click on Approve button");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.approveButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.approveButton));
		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.finishButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		Thread.sleep(3000);
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("Appraiser Activity Status"), "Approved",
				"SMAB-T3693-Status of the Appraisal activity is submitted for approval");

		// STEP 6 - Click Return button and validate
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.returnButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.returnButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.returnReasonTextBox);
		objCioTransfer.enter(objCioTransfer.returnReasonTextBox, "Returned by CIO Supervisor");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		Thread.sleep(3000);
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("Appraiser Activity Status"), "Returned",
				"SMAB-T3668-Status of the appraisal activity is Returned");
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		softAssert.assertContains(objMappingPage.getGridDataInHashMap(2).get("Name").get(0),"Trail","SMAB-T3693: Validate AT=BE");
		softAssert.assertContains(objMappingPage.getGridDataInHashMap(2).get("Type").get(0),"Business Events","SMAB-T3693: Validate AT=BE");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("Status"), "Returned",
				"SMAB-T3668-Status of the work Item is Returned");
		String currentWorkItem = driver.getCurrentUrl();
		softAssert.assertEquals(currentWorkItem, workItemOfAppraisalActivity, "work Item is equal");
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow3 = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow3);
		
		// STEP 7 - Click View RAT Screen button	
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.viewRATScreenButton));
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("EventID"), EventID,
				"SMAB-T3677:Event ID from CIO and App Activity is Matching");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("APN"), APN,
				"SMAB-T3677:APN from CIO and App Activity is Matching");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("DOR"), DOR,
				"SMAB-T3677:DOR from CIO and App Activity is Matching");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("DOE"), DOE,
				"SMAB-T3677:DOE from CIO and App Activity is Matching");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("DOV"), DOV,
				"SMAB-T3677:DOV from CIO and App Activity is Matching");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("PUCCode"), PUCCode,
				"SMAB-T3677:PUCCOde from CIO and App Activity is Matching");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS(EventCode), "CIO-SALE",
				"SMAB-T3677:PUCCOde from CIO and App Activity is Matching");
		softAssert.assertTrue(objCioTransfer.verifyElementVisible("View Recorder File"),
				"SMAB-T3627: View RAT Screen is navigated back to CIO Transfer activity");
		softAssert.assertEquals(objParcelsPage.verifyElementVisible("EventID"), "true",
				"SMAB-T3627: View RAT Screen is navigated back to CIO Transfer activity");
		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to Validate that user is able to check the labels on the story
	 * 	 * @param loginUsers:System Admin
	 * @throws Exception
	 */

	@Test(description = "SMAB-T3252, SMAB-T3246, SMAB-T3259, SMAB-T3249, SMAB-T3247, SMAB-T3251, SMAB-T3289: Validate that user is able to update the status of Appraisal activity and related Work item to Return using the 'Return' option using Quick Action button", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
		public void RecorderIntegration_ValidateStartdateAndEndDateFieldsOnParcelMailtorec(String loginUser)
			throws Exception {

		String execEnv = System.getProperty("region");
		String mailToRecordFromParcel = "SELECT Parcel__c,Id FROM Mail_To__c where status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(mailToRecordFromParcel);
		String mailToID = hashMapRecordedApn.get("Id").get(0);
		String viewAllParcel = hashMapRecordedApn.get("Parcel__c").get(0);

		// STEP 1:login with Appraisal Staff and navigate to Mail-To tab for active record
		objMappingPage.login(loginUser);
		driver.navigate().to(
				"https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Mail_To__c/" + mailToID + "/view");
		
		// STEP 2: Validate labels on the Mail-To record
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.formattedName1LabelForParcelMailTo);
		softAssert.assertEquals(objParcelsPage.verifyElementVisible(objCioTransfer.formattedName1LabelForParcelMailTo),
				"true", "SMAB-T3252-Formatted name1 lable validation");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.formattedName2LabelForParcelMailTo);
		softAssert.assertEquals(objParcelsPage.verifyElementVisible(objCioTransfer.formattedName2LabelForParcelMailTo),
				"true", "SMAB-T3252-Formatted name1 lable validation");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.careOfLabel);
		softAssert.assertEquals(objParcelsPage.verifyElementVisible(objCioTransfer.careOfLabel), "true",
				"SMAB-T3252:Care Of Lable Validation");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.Status);
		softAssert.assertEquals(objParcelsPage.verifyElementVisible(objCioTransfer.Status), "true",
				"SMAB-T3252-Status lable Validation");
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS("Status"), "Active",
				"SMAB-T3259-Status is active");
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.startDateInParcelMaito, "class"),
				"is-read-only", "SMAB-T3246-Start date is not editable");
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.endDateInParcelMaito, "class"),
				"is-read-only", "SMAB-T3246,SMAB-T3249-End date is not editable");
		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		objWorkItemHomePage.Click(objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.endDateInParcelMaito, "class"),
				"is-read-only", "SMAB-T3246,SMAB-T3249-End date is not editable");
		objCioTransfer.Click(objCioTransfer.getButtonWithText("Save"));
		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.getButtonWithText(objCioTransfer.Clone));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Clone));
		softAssert.assertContains(objPage.getAttributeValue(objCioTransfer.endDateInParcelMaito, "class"),
				"is-read-only", "SMAB-T3246,SMAB-T3249-End date is not editable");
		objCioTransfer.Click(objCioTransfer.getButtonWithText("Save"));
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy ");
		Date date = new Date();
		String todayDate = dateFormat.format(date);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Start Date").trim(), todayDate.trim(),
				"SMAB-T3247-Start date is equal to Today's date");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/"
				+ viewAllParcel + "/related/Mail_To__r/view");

		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataInHashMap();
		softAssert.assertEquals(granteeHashMap.containsKey("Formatted Name 1"), "true",
				"SMAB-T3251:Formatted Name of First reciepient is Formatted Name 1");
		softAssert.assertEquals(granteeHashMap.containsKey("Formatted Name 2"), "true",
				"SMAB-T3251:Formatted Name of Second reciepient is Formatted Name 2");
		String retiredMailToRecordFromParcel = "SELECT Parcel__c,Id,End_Date__c FROM Mail_To__c where status__c = 'Retired' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRetiredParcel = salesforceAPI.select(retiredMailToRecordFromParcel);
		String mailToIDforRetired = hashMapRetiredParcel.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Mail_To__c/"
				+ mailToIDforRetired + "/view");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS("End Date"), "/",
				"SMAB-T3249-End date is not Empty");
		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.getButtonWithText(objCioTransfer.Clone));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Clone));
		softAssert.assertContains(objCioTransfer.saveRecordAndGetError(), "cloned",
				"SMAB-T3289: Retired record cannot be cloned");
		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to Validate start-date and end-date on Mail-to record of Active parcel
	 * @param loginUsers:System Admin
	 * @throws Exception
	 */

	@Test(description = "SMAB-T2995: Validate startdate and enddate on Mailto record of parcel", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "RecorderIntegration", "ChangeInOwnershipManagement" })
	public void RecorderIntegration_ValidateStartDateAndEnddateOnMailtoRecordOfParcel(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
		String mailToRecordFromParcel = "SELECT Parcel__c,Id FROM Mail_To__c where status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(mailToRecordFromParcel);
		String mailToID = hashMapRecordedApn.get("Id").get(0);
		String OwnershipAndTransferCreationData = testdata.MAILTO_RECORD_DATA_PARCEL;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "createMailToData");
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date date = new Date();
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(date);
		calDate.add(Calendar.DATE, 1);
		date = calDate.getTime();
		String endDate = dateFormat.format(date);
		ReportLogger.INFO("Tomorrows date is " + endDate);
		objMappingPage.login(users.SYSTEM_ADMIN);
		driver.navigate().to(
				"https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Mail_To__c/" + mailToID + "/view");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.Edit);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		objCioTransfer.enter(objCioTransfer.endDate, endDate);
		objCioTransfer.enter(objCioTransfer.mailingZip, hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));
		softAssert.assertContains(objCioTransfer.saveRecordAndGetError(), "Future end-date cannot be entered",
				"SMAB-T2995:Future Enddate cannot be entered");
		objCioTransfer.enter(objCioTransfer.endDate, hashMapOwnershipAndTransferCreationData.get("End Date"));
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.SaveButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.Edit);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		objCioTransfer.enter(objCioTransfer.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
		objCioTransfer.enter(objCioTransfer.endDate, hashMapOwnershipAndTransferCreationData.get("End Date"));
		softAssert.assertContains(objCioTransfer.saveRecordAndGetError(), "Start Date cannot be greater than End Date",
				"SMAB-T2995:Start Date cannot be greater than End Date");
		objWorkItemHomePage.logout();
	}

	
	/*
	 Recorder Integration- Verify No WI should be created if recorded document has invalid Recorded APN
	 * 
	 */

	@Test(description = "SMAB-T3963:Recorder Integration- Verify No WI should be created if recorded document has invalid Recorded APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void RecorderIntegration_InvalidRecordedAPN_VerifyNoWICreated(String loginUser) throws Exception {

		
		// Step 1: getting recorded document for CIO and corresponding recorded apn 
		
		String fetchDocId ="SELECT id from recorded_document__c where recorder_doc_type__c='AD' and xAPN_count__c=1";
		String	documentId=salesforceAPI.select(fetchDocId).get("Id").get(0);
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI
				.select("SELECT ID,Name FROM Recorded_APN__c WHERE RECORDED_DOCUMENT__C='" + documentId + "'");
		String recordedAPNID = hashMapRecordedApn.get("Id").get(0);

		if(salesforceAPI.select("SELECT ID FROM Recorded_APN__c WHERE RECORDED_DOCUMENT__C='"+documentId+"'"+" AND PARCEL__C != NULL ").size()!=0)
        		 
		{	
			
		// Step 2: updating recorder_apn and parcel value in recorded apn
		
		JSONObject jsonToUpdateRecordedAPN = new JSONObject();
		jsonToUpdateRecordedAPN.put("Parcel__c", "");
		jsonToUpdateRecordedAPN.put("Recorder_APN__c", "123");
		jsonToUpdateRecordedAPN.put("Status__c", "Pending");
		salesforceAPI.update("Recorded_APN__c", recordedAPNID, jsonToUpdateRecordedAPN);
		}
		
		// Step 3: generating recorded work items from job
		objCioTransfer.generateRecorderJobWorkItems(documentId);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO' and CreatedDate =TODAY and Recorded_Document__c='"+documentId+"'";
		HashMap<String, ArrayList<String>> hashMapWorkItem=salesforceAPI.select(WorkItemQuery);
		
		softAssert.assertEquals(hashMapWorkItem.size(),"0",
				"SMAB-T3963:Verifying that No WI should be created if recorded document has invalid Recorded APN");

		 String statusRECDoc=salesforceAPI.select("SELECT Status__c FROM Recorded_Document__c where id='"+documentId+"'").get("Status__c").get(0);
		 String statusRECAPN=salesforceAPI.select("SELECT Status__c FROM Recorded_APN__c where id='"+recordedAPNID+"'").get("Status__c").get(0);

		softAssert.assertEquals(statusRECDoc,"Processed",
				"SMAB-T3963:Verifying status of rec doc with invalid APN should be processed after REC INtegration batch job execution");

		softAssert.assertEquals(statusRECAPN,"Processed",
				"SMAB-T3963:Verifying status of rec APN  with invalid APN should be processed after REC INtegration batch job execution");

	}
	
	/*
	 Recorder Integration- Verify No WI should be created if recorded document has one valid APN and one invalid Recorded APN
	 * 
	 */

	@Test(description = "SMAB-T3964:Recorder Integration- Verify No WI should be created if recorded document has one valid APN and one invalid Recorded APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void RecorderIntegration_Invalid_ValidRecordedAPN_VerifyWICreated(String loginUser) throws Exception {

		
		// Step 1: getting recorded document for mapping and corresponding recorded apn 
		
		String fetchDocId ="SELECT id from recorded_document__c where recorder_doc_type__c='ES' and xAPN_count__c=2";
		String	documentId=salesforceAPI.select(fetchDocId).get("Id").get(0);
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI
				.select("SELECT ID,Name FROM Recorded_APN__c WHERE RECORDED_DOCUMENT__C='" + documentId + "'");
		String recordedAPNID1 = hashMapRecordedApn.get("Id").get(0);
		String recordedAPNID2 = hashMapRecordedApn.get("Id").get(1);

		JSONObject jsonToUpdateRecordedAPN1 = objCioTransfer.getJsonObject() ;
		
		jsonToUpdateRecordedAPN1.put("Status__c", "Pending");
		salesforceAPI.update("Recorded_APN__c", recordedAPNID1, jsonToUpdateRecordedAPN1);
		salesforceAPI.update("Recorded_APN__c", recordedAPNID2, jsonToUpdateRecordedAPN1);
		
		if(salesforceAPI.select("SELECT ID FROM Recorded_APN__c WHERE id='"+recordedAPNID1+"'"+" AND PARCEL__C != NULL ").size()!=0 && salesforceAPI.select("SELECT ID FROM Recorded_APN__c WHERE id='"+recordedAPNID2+"'"+" AND PARCEL__C != NULL ").size()!=0)
       		 
		{	
			
		// Step 2: updating recorder_apn and parcel value in recorded apn
		
		JSONObject jsonToUpdateRecordedAPN = objCioTransfer.getJsonObject();
		jsonToUpdateRecordedAPN.put("Parcel__c", "");
		jsonToUpdateRecordedAPN.put("Recorder_APN__c", "123");
		salesforceAPI.update("Recorded_APN__c", recordedAPNID1, jsonToUpdateRecordedAPN);
		}
		
		String APNIdFromValidREcordedAPN =salesforceAPI.select("SELECT PARCEL__C FROM Recorded_APN__c WHERE RECORDED_DOCUMENT__C='"+documentId+"' AND PARCEL__C != NULL ").get("Parcel__c").get(0);
		
		// Step 3: generating recorded work items from job
		objCioTransfer.generateRecorderJobWorkItems(documentId);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='Mapping' and CreatedDate =TODAY and Recorded_Document__c='"+documentId+"'";
		HashMap<String, ArrayList<String>> hashMapWorkItem=salesforceAPI.select(WorkItemQuery);
		
		softAssert.assertEquals(hashMapWorkItem.size(),"1",
				"SMAB-T3964:Verifying that only one WI should be created if recorded document has one invalid and one Recorded APN");

		String workItemId=hashMapWorkItem.get("Id").get(0);
				
		String apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromValidREcordedAPN,
						"SMAB-T3964: Validation that one WI for valid recorded APN is created when rec doc has one valid and one invalid APN ");
				
		 String statusRECDoc=salesforceAPI.select("SELECT Status__c FROM Recorded_Document__c where id='"+documentId+"'").get("Status__c").get(0);
		 String statusRECAPN1=salesforceAPI.select("SELECT Status__c FROM Recorded_APN__c where id='"+recordedAPNID1+"'").get("Status__c").get(0);
		 String statusRECAPN2=salesforceAPI.select("SELECT Status__c FROM Recorded_APN__c where id='"+recordedAPNID2+"'").get("Status__c").get(0);

		softAssert.assertEquals(statusRECDoc,"Processed",
				"SMAB-T3964:Verifying status of rec doc with one invalid APN and one valid APN should be processed after REC INtegration batch job execution");

		softAssert.assertEquals(statusRECAPN1,"Processed",
				"SMAB-T3964:Verifying status of rec APN  with invalid APN should be processed after REC INtegration batch job execution");

		softAssert.assertEquals(statusRECAPN2,"Processed",
				"SMAB-T3964:Verifying status of rec APN  with valid APN should be processed after REC INtegration batch job execution");

	}
	
	/*
	 * Verify that For Recorder Feed with no APN , the initial Work Item will be routed to respective Work Pools based on document type. If routed to CIO Pool, the staff will try to attach APN/APN's as needed or use "Change WorkPool" option to acsertain APN
	 * 
	 */

	@Test(description = "SMAB-T3962:Verify for Recorder Feed with no APN ,If routed to CIO Pool, the staff will try to attach APN/APN's as needed or use \"Change WorkPool\" option to acsertain APN", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration"}, enabled = true)
	public void RecorderIntegration_VerifyWorkPoolChange_NewWIgeneratedForNOAPNRecordedDocument(
			String loginUser) throws Exception {

		String getApnToAdd = "Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(getApnToAdd);
		String recordedAPN = hashMapRecordedApn.get("Name").get(0);
		String APNIdFromValidREcordedAPN = hashMapRecordedApn.get("Id").get(0);

		// login with CIO user

		objMappingPage.login(loginUser);
		objMappingPage.searchModule(PARCELS);

		objCioTransfer.generateRecorderJobWorkItems("DE", 0);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='NO APN' AND Sub_type__c='NO APN - CIO'  And status__c='In pool' order by createdDate desc limit 1";
		String WorkItemNo = salesforceAPI.select(WorkItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(WorkItemNo);

		// CIO staff accepts the NO APN WI for  CIO
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		
		// CIO staff changes the work pool of NO APN WO from CIO to Mapping

		objCioTransfer.searchModule(HOME);
		objCioTransfer.Click(objWorkItemHomePage.lnkTABHome);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABWorkItems);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInProgress);
		Thread.sleep(4000);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(WorkItemNo);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeWorkPool));
		objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemHomePage.WorkPool, "Mapping");
		objWorkItemHomePage.enter(objWorkItemHomePage.reasonForTransferring,"Test");
		objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);
		
		// login with Mapping user user
		objCioTransfer.logout();
		Thread.sleep(5000);
		objMappingPage.login(users.MAPPING_STAFF);
		
		// Mapping staff accepts the NO APN WI for  CIO

		objCioTransfer.searchModule(HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABHome);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABWorkItems);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInPool);
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.acceptWorkItemButton);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(WorkItemNo);
		objWorkItemHomePage.Click(objWorkItemHomePage.acceptWorkItemBtn);
		Thread.sleep(2000);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.waitForElementToBeVisible(15, objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeWorkPool));
		objWorkItemHomePage.clickCheckBoxForSelectingWI(WorkItemNo);
		objWorkItemHomePage.openWorkItem(WorkItemNo);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);  	
		
		// User tries to add the Recorded APN
		objMappingPage.Click(objWorkItemHomePage.recordedAPNtab);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.NewButton));
		objWorkItemHomePage.enter(objWorkItemHomePage.apnLabel, recordedAPN);
		objWorkItemHomePage.selectOptionFromDropDown(objWorkItemHomePage.apnLabel, recordedAPN);

		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.SaveButton));
		Thread.sleep(2000);
		
		// User validates the status of added recorded APN
		driver.navigate().back();
		driver.navigate().back();

		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0), "Pending",
						"SMAB-T3962: Validating that status of added recorded APN is Pending");

		// mapping staff changes the work pool of NO APN WI from  Mapping to CIO
		driver.navigate().refresh();
		objCioTransfer.searchModule(HOME);
		objCioTransfer.Click(objWorkItemHomePage.lnkTABHome);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABWorkItems);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.waitForElementToBeVisible(15, objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeWorkPool));
		objWorkItemHomePage.clickCheckBoxForSelectingWI(WorkItemNo);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeWorkPool));
		objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemHomePage.WorkPool, "CIO");
		objWorkItemHomePage.enter(objWorkItemHomePage.reasonForTransferring,"Test");
		objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);
		
		// CIO staff accepts the NO APN WI for CIO
		objCioTransfer.logout();
		Thread.sleep(5000);
		objMappingPage.login(users.CIO_STAFF);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(WorkItemNo);

		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objMappingPage.Click(objWorkItemHomePage.recordedAPNtab);

		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0), "Pending",
				"SMAB-T3962: Validating that status of added recorded APN is Pending");
		
		// CIO staff User clicks on Migrate button
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.migrateAPN));
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);

		// User validates the status of added recorded APN
		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("Status").get(0), "Processed",
				"SMAB-T3962: Validating that status of added Recorded APN is processed once migrated");
		softAssert.assertContains(objMappingPage.getElementText(objWorkItemHomePage.successAlert), "All recorded apn(s) are migrated successfully",
				"SMAB-T3962: validatinmg that Success message appears once Recorded APN is migrated");
		
		// User tries to complete the WI
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert);

		// User validates the status of the WI
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
				"SMAB-T3962:Validating that status of NO APN WI is completed once CIO user completes the WI");
		
		String workItemId=salesforceAPI.select("SELECT Id,name FROM Work_Item__c where Type__c='CIO'  and Sub_Type__c ='Process Transfer & Ownership'  And status__c='In Progress' and CreatedDate =TODAY order by createdDate desc limit 1").get("Id").get(0);
		String apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromValidREcordedAPN,
						"SMAB-T3962:  Validating a new WI is genrated as soon as New APN is migrated ");
				
		objWorkItemHomePage.logout();

	}
	
	/*
	 * Update Returned Reason on CIO screen for Submit for approval flow.
	 */
	@Test(description = "SMAB-T3608 : Update Returned Reason on the Work Item", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecordedEvent" }, enabled = true)
	public void CIO_UpdateReturnedReasonOtheWorkItemFromSubmitforApproval(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithCompleteOwnership");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData, "DataToCreateOwnershipRecord");

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
		String workItemNo = salesforceAPI.select(workItemQuery).get("Id").get(0);
		driver.navigate().to(
				"https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Work_Item__c/" + workItemNo + "/view");
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
		Thread.sleep(5000);

		// STEP 4-Login with CIO staff
		objMappingPage.login(loginUser);
		driver.navigate().to(
				"https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Work_Item__c/" + workItemNo + "/view");
		Thread.sleep(5000);
		
		// STEP 5-Finding the recorded APN transfer id
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 6-Clicking on related action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// STEP 7-Creating the new Grantee
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"
				+ recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.calculateOwnershipButtonLabel);
		ReportLogger.INFO("Create New grantee");
		
		// STEP 8- CIO Staff submitting for approval
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Submit for Approval");
		Thread.sleep(2000);
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		ReportLogger.INFO("CIO activity Submitted for Approval");
		String currentUrl = driver.getCurrentUrl();
		Thread.sleep(2000);
		objCioTransfer.logout();
		Thread.sleep(5000);

		// STEP 9- Login as CIO-Supervisor and Return RAT
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(currentUrl);
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.calculateOwnershipButtonLabel);
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Return");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.returnReasonTextBox);
		objCioTransfer.enter(objCioTransfer.returnReasonTextBox, "Returned by CIO Supervisor");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		ReportLogger.INFO("CIO Activity Returned with Reason");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.calculateOwnershipButtonLabel);
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Back");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.EditButton);
		objParcelsPage.openTab("Details");
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS("Returned Reason"), "Returned by CIO Supervisor",
				"SMAB-T3608 : Validated retirned reason on work item");
		objCioTransfer.logout();
		Thread.sleep(5000);

		// STEP 10: Login as CIO-Staff and correct the return reason and submit for approval
		objMappingPage.login(users.CIO_STAFF);
		driver.navigate().to(currentUrl);
		Thread.sleep(3000);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objParcelsPage.openTab("Details");
		softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS("Returned Reason"), "Returned by CIO Supervisor",
				"SMAB-T3608 : Validated returned reason on work item");
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow1 = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow1);
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Submit for Approval");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		ReportLogger.INFO("CIO Staff reviewed the return reason and Submitted for Approval");
		Thread.sleep(3000);
		objCioTransfer.logout();
		Thread.sleep(5000);

		// STEP :Login as supervisor and approve
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(currentUrl);
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-P19");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.quickActionOptionApprove);
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Approve");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		ReportLogger.INFO("CIO Supervisor approved the UT event");
		Thread.sleep(3000);
		objCioTransfer.logout();
	  }
	
   /*
	* Validate Govt CIO Appraisal - Transfer from one Govt owner to another Govt owner
    */

	@Test(description = "SMAB-T4169, SMAB-T4172, : Govt CIO Post transfer process", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void CIO_GOVTCIOPostTransferProcess(String loginUser) throws Exception {
		
		String execEnv = System.getProperty("region");
		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil
				.generateMapFromJsonFile(testdata.UNRECORDED_EVENT_DATA, "UnrecordedEventCreation");
		Map<String, String> hashMapMailToData = objUtil.generateMapFromJsonFile(MailtoData, "createMailToData");

		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecord");
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String description = dataToCreateUnrecordedEventMap.get("Description") + "_" + timeStamp;

		// STEP 1 : Create Appraiser activity
		String[] arrayForWorkItemAfterCIOSupervisorApproval = objCioTransfer
				.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment", objCioTransfer.CIO_EVENT_CODE_CIOGOVT,
						hashMapMailToData, hashMapOwnershipAndTransferGranteeCreationData,
						hashMapCreateOwnershipRecordData, hashMapCreateAssessedValueRecord);

		// STEP 2 : Login as Appraiser user
		ReportLogger.INFO("Login as Appriaser user");
		objMappingPage.login(users.RP_APPRAISER);
		String workItemForAppraiser = arrayForWorkItemAfterCIOSupervisorApproval[0];
		String workItemQuery = "SELECT Id FROM Work_Item__c where name = '"+workItemForAppraiser+"'";
		String workItemId = salesforceAPI.select(workItemQuery).get("Id").get(0);
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Work_Item__c/"+workItemId+"/view");
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		
		// STEP 3 - Navigating to Appraisal Activity Screen
		ReportLogger.INFO("Navigating to Appraisal Activity Screen");
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.quickActionOptionSubmitForApproval);
		ReportLogger.INFO("Navigated to Appraisal Activity");
		
		//STEP 4 : Validating Assessed value records and Roll entry record tile view values for LCV And ICV as Zero Values
		objCioTransfer.editRecordedApnField("Land Cash Value");
		objPage.enter("Land Cash Value", "0");
		objPage.enter("Improvement Cash Value", "0");
		objCioTransfer.Click(objCioTransfer.saveButtonModalWindow);
		String apnValue = objCioTransfer.getFieldValueFromAPAS("APN");
		String apnQuery = "SELECT Id FROM Parcel__c WHERE Name = '" + apnValue + "'";
		String APN = salesforceAPI.select(apnQuery).get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + APN
				+ "/related/Assessed_Values__r/view");
		ReportLogger.INFO("Opened Assessed value records");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.newButton, 3);
		HashMap<String, ArrayList<String>> gridDataHashMapAssessedValueNew = objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMapAssessedValueNew.get("Land Value").get(1), "0", "SMAB-T4169: LCV for Assessed value record is 0");
		softAssert.assertEquals(gridDataHashMapAssessedValueNew.get("Improvement Value").get(1), "0",
				"SMAB-T4169: ICV for Assessed value record ");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + APN
				+ "/related/Roll_Entry__r/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.newButton, 3);
		ReportLogger.INFO("Opened Roll Entry records");
		HashMap<String, ArrayList<String>> gridDataHashMapRollEntryValue = objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMapRollEntryValue.get("Land Assessed Value").get(0), "$0",
				"SMAB-T4169: LCV for roll entry record is 0");
		softAssert.assertEquals(gridDataHashMapRollEntryValue.get("Improvement Assessed Value").get(0), "$0",
				"SMAB-T4169: ICV for roll entry record is 0");
		objMappingPage.logout();
		Thread.sleep(4000);

		//STEP 5: Create CIO-GOVT Transfer by creating UT Event on the same parcel
		objMappingPage.login(users.SYSTEM_ADMIN);
		driver.navigate().to("https://smcacre--qa.lightning.force.com/lightning/r/Parcel__c/" + APN + "/view");
		objMappingPage.waitForElementToBeClickable(
				objMappingPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.selectOptionDropdown);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.selectOptionDropdown, "Create Audit Trail Record");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.workItemTypeDropDownComponentsActionsModal);
		objParcelsPage.selectOptionFromDropDown("Record Type", dataToCreateUnrecordedEventMap.get("Record Type"));
		objParcelsPage.selectOptionFromDropDown("Group", dataToCreateUnrecordedEventMap.get("Group"));
		Thread.sleep(2000);
		objParcelsPage.selectOptionFromDropDown("Type of Audit Trail Record?",
				dataToCreateUnrecordedEventMap.get("Type of Audit Trail Record?"));
		if (dataToCreateUnrecordedEventMap.get("Source") != null) {
			objParcelsPage.selectOptionFromDropDown("Source", dataToCreateUnrecordedEventMap.get("Source"));
		}
		if (dataToCreateUnrecordedEventMap.get("Date of Event") != null) {
			objParcelsPage.enter("Date of Event", dataToCreateUnrecordedEventMap.get("Date of Event"));
		}
		objParcelsPage.enter("Date of Recording", dataToCreateUnrecordedEventMap.get("Date of Recording"));
		objParcelsPage.enter("Description", description);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save and Next"));
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.calculateOwnershipButtonLabel);
		ReportLogger.INFO("CIO Activity created");
		String currentUrlCIO = driver.getCurrentUrl();
		objCioTransfer.logout();
		Thread.sleep(5000);
		

		// STEP 6: Login with CIO staff and approve the CIO
		objMappingPage.login(users.CIO_STAFF);
		driver.navigate().to(currentUrlCIO);
		Thread.sleep(2000);

		// STEP 5-Creating the new Grantee
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.calculateOwnershipButtonLabel);
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, objCioTransfer.CIO_EVENT_CODE_CIOGOVT);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.calculateOwnershipButtonLabel);
		
		// STEP 6- CIO Staff submitting for Review
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Submit for Approval");
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		Thread.sleep(2000);
		objCioTransfer.logout();
		Thread.sleep(5000);

		// STEP 7- Login as CIO-Supervisor and Approve
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(currentUrlCIO);
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.quickActionOptionApprove);
		objCioTransfer.clickQuickActionButtonOnTransferActivity("Approve");
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
     	Thread.sleep(3000);
		objCioTransfer.logout();
		Thread.sleep(5000);
		
		// STEP 8: Login as RP APPRAISER and Validate GOVT Transfer
		objMappingPage.login(users.RP_APPRAISER);
		String workItemNoForAppraiser = salesforceAPI.select(
				"Select Id,Name from Work_Item__c where type__c='Govt CIO Appraisal' and sub_type__c='Appraisal Activity' order by createdDate desc")
				.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Work_Item__c/"
				+ workItemNoForAppraiser + "/view");
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 9: Navigating to Appraisal Activity Screen
		ReportLogger.INFO("Navigating to Appraisal Activity Screen");
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow1 = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow1);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.editRecordedApnField("Land Cash Value");
		objPage.enter("Land Cash Value", "0");
		objPage.enter("Improvement Cash Value", "0");
		objCioTransfer.Click(objCioTransfer.saveButtonModalWindow);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + APN
				+ "/related/Assessed_Values__r/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.newButton, 3);
		ReportLogger.INFO("Navigated to Assessed value records");		
		HashMap<String, ArrayList<String>> gridDataHashMapAssessedValueNewTransferGovt = objMappingPage
				.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMapAssessedValueNewTransferGovt.get("Land Value").get(2), "0",
				"SMAB-T4169: LCV For Assessed value is 0");
		softAssert.assertEquals(gridDataHashMapAssessedValueNewTransferGovt.get("Improvement Value").get(2), "0",
				"SMAB-T4169: ICV for Assessed value is 0");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + APN
				+ "/related/Roll_Entry__r/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.newButton, 3);
		ReportLogger.INFO("Navigated to Roll entry record");
		HashMap<String, ArrayList<String>> gridDataHashMapRollEntryValueGOVTTransfer = objMappingPage
				.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMapRollEntryValueGOVTTransfer.get("Land Assessed Value").get(0), "$0",
				"SMAB-T4169: LCV For Roll entry record is 0");
		softAssert.assertEquals(gridDataHashMapRollEntryValueGOVTTransfer.get("Improvement Assessed Value").get(0),
				"$0", "SMAB-T4169: ICV For Roll entry record is 0");
		objMappingPage.logout();		
	
	}

	/*
	 * This method is used to Validate that System should have the ability to calculate a composite value based on the assessed values associated to a parcel and the ownership profile.
	 * Users: RP Appraiser, RP Supervisor 
	 */
	
	@Test(description = "SMAB-T3932, SMAB-T4271: Validate that System should have the ability to calculate a composite value based on the assessed values associated to a parcel and the ownership profile.", dataProvider = "loginRPAppraiser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
	public void RecorderIntegration_Composite_Value_Calculation(String loginUser) throws Exception {
		String execEnv = System.getProperty("region");
		
		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteDataPartTransfer");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateRpOwnership");

		Map<String, String> hashMapMailToData = objUtil.generateMapFromJsonFile(MailtoData, "createMailToData");

		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecordCioPart");
		// STEP 1 : Create Appraiser activity
		objCioTransfer.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment", objCioTransfer.CIO_EVENT_CODE_PART,
						hashMapMailToData, hashMapOwnershipAndTransferGranteeCreationData,
						hashMapCreateOwnershipRecordData, hashMapCreateAssessedValueRecord);
		
		// Step2: Login to the APAS application using the credentials passed through dataprovider (RP Appraiser)
		objWorkItemHomePage.login(loginUser);

		String workItemNoForAppraiser = salesforceAPI.select(
				"Select Id ,Name from Work_Item__c where type__c='Appraiser' and sub_type__c='Appraisal Activity' order by createdDate desc")
				.get("Name").get(0);						
		objCioTransfer.globalSearchRecords(workItemNoForAppraiser);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objCioTransfer.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.getFieldValueFromAPAS("APN");
		objWorkItemHomePage.waitForElementToBeInVisible("APN", 5);
		objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		
		// STEP 3 - Navigating to Appraisal Activity Screen
		ReportLogger.INFO("Navigating to Appraisal Activity Screen");
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		String appraisalActivityUrl = driver.getCurrentUrl();
		
		objMappingPage.waitForElementToBeClickable(apasGenericObj.getFieldValueFromAPAS("APN"));
		String apnOnAAS=(apasGenericObj.getFieldValueFromAPAS("APN"));
		String DOV=(apasGenericObj.getFieldValueFromAPAS("DOV"));
		objAppraisalActivity.Click(objAppraisalActivity.assessedValueTableView);
		Thread.sleep(2000);
		objCioTransfer.clickViewAll(assessedValueTableName);
		HashMap<String, ArrayList<String>> gridDataHashMapAssessedValue = objMappingPage.getGridDataInHashMap();
		String assessedValueType = gridDataHashMapAssessedValue.get("Assessed Value Type").get(0);
		String assessedValueStartDate = gridDataHashMapAssessedValue.get("Effective Start Date").get(0);
		String assessedValueEndDate = gridDataHashMapAssessedValue.get("Effective End Date").get(0);
		String assessedValueLandValue = gridDataHashMapAssessedValue.get("Land Value").get(0);
		String assessedValueImprovementValue = gridDataHashMapAssessedValue.get("Improvement Value").get(0);
		
		driver.navigate().to(appraisalActivityUrl);
		objMappingPage.waitForElementToBeClickable(apasGenericObj.getFieldValueFromAPAS("APN"));
		objMappingPage.Click(objAppraisalActivity.appraisalActivityEditValueButton("Land Cash Value"));
		objMappingPage.waitForElementToBeVisible(apasGenericObj.getButtonWithText("Save"));
		
		apasGenericObj.enter("Land Cash Value", "200000");
		apasGenericObj.enter("Improvement Cash Value","200000");
		
		apasGenericObj.Click(apasGenericObj.getButtonWithText("Save"));
		Thread.sleep(2000);
		
		objAppraisalActivity.Click(objAppraisalActivity.assessedValueTableView);
		Thread.sleep(2000);
		objCioTransfer.clickViewAll("Assessed Values for Parent Parcel");
		HashMap<String, ArrayList<String>> gridDataHashMapAssessedValueNew = objMappingPage.getGridDataInHashMap();
		String assessedValueTypeFirst = gridDataHashMapAssessedValueNew.get("Assessed Value Type").get(0);
		String assessedValueStartDateFirst = gridDataHashMapAssessedValueNew.get("Effective Start Date").get(0);
		
		String assessedValueLandValueFirst= gridDataHashMapAssessedValueNew.get("Land Value").get(0);
		String assessedValueImprovementValueFirst = gridDataHashMapAssessedValueNew.get("Improvement Value").get(0);
		String assessedValueTypeSecond = gridDataHashMapAssessedValueNew.get("Assessed Value Type").get(1);
		String assessedValueStartDateSecond = gridDataHashMapAssessedValueNew.get("Effective Start Date").get(1);
		String assessedValueLandValueSecond= gridDataHashMapAssessedValueNew.get("Land Value").get(1);
		String assessedValueImprovementValueSecond = gridDataHashMapAssessedValueNew.get("Improvement Value").get(1);
		String assessedValueEndDateFirst = gridDataHashMapAssessedValue.get("Effective End Date").get(0);
		String assessedValueEndDateSecond = gridDataHashMapAssessedValueNew.get("Effective End Date").get(1);
		softAssert.assertEquals(assessedValueTypeFirst,assessedValueType, "SMAB-T3932, SMAB-T4271:Verify That Both Assessed Value Type Should Be Same.");
		softAssert.assertEquals(assessedValueStartDateFirst,assessedValueStartDate, "SMAB-T3932, SMAB-T4271:Verify That Both Start Date Should Be Same.");
		softAssert.assertEquals(assessedValueLandValueFirst,assessedValueLandValue, "SMAB-T3932, SMAB-T4271:Verify That Both Land Value Should Be Same.");
		softAssert.assertEquals(assessedValueImprovementValueFirst,assessedValueImprovementValue, "SMAB-T3932, SMAB-T4271:Verify That Both Improvement Value Should Be Same.");
		softAssert.assertEquals(assessedValueEndDate,assessedValueEndDateFirst, "SMAB-T3932, SMAB-T4271:Verify That Both End Date Should Be empty.");
	
		softAssert.assertEquals(assessedValueTypeSecond,assessedValueType, "SMAB-T3932, SMAB-T4271:Verify That Both Assessed Value Type Should Be Same.");
		softAssert.assertEquals(assessedValueStartDateSecond,DOV, "SMAB-T3932, SMAB-T4271:Verify That Both Start Date Should Be Same.");
		softAssert.assertEquals(assessedValueLandValueSecond,"200,000", "SMAB-T3932, SMAB-T4271:Verify That Both Land Value Should Be Same.");
		softAssert.assertEquals(assessedValueImprovementValueSecond,"200,000", "SMAB-T3932, SMAB-T4271:Verify That Both Improvement Value Should Be Same.");
		softAssert.assertEquals(assessedValueEndDateSecond,assessedValueEndDateFirst, "SMAB-T3932, SMAB-T4271:Verify That Both End Date Should Be Empty.");
	
		
		String queryAPN = "select Id from Parcel__c where Name='"+apnOnAAS+"'";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apnToUpdateId= responseAPNDetails.get("Id").get(0);
		driver.navigate().to("https://smcacre--"+ execEnv + ".lightning.force.com/lightning/r/" + apnToUpdateId + "/related/Roll_Entry__r/view");
		objMappingPage.waitForElementToBeClickable(objAppraisalActivity.parcelsLink);
		HashMap<String, ArrayList<String>> gridDataHashMapRollEntryNew = objMappingPage.getGridDataInHashMap();
		
		String rollValueTypeFirst = gridDataHashMapRollEntryNew.get("Type").get(0);
		String rollValueYearFirst = gridDataHashMapRollEntryNew.get("Roll Year Settings").get(0);
		String rollValueLandValueFirst= gridDataHashMapRollEntryNew.get("Land Assessed Value").get(0);
		String rollValueImprovementValueFirst = gridDataHashMapRollEntryNew.get("Improvement Assessed Value").get(0);
		String rollValueTypeSecond = gridDataHashMapRollEntryNew.get("Type").get(1);
		String rollValueYearSecond = gridDataHashMapRollEntryNew.get("Roll Year Settings").get(1);
		String rollValueLandValueSecond= gridDataHashMapRollEntryNew.get("Land Assessed Value").get(1);
		String rollValueImprovementValueSecond = gridDataHashMapRollEntryNew.get("Improvement Assessed Value").get(1);
		

		softAssert.assertEquals(rollValueTypeFirst,"Annual", "SMAB-T3932, SMAB-T4271:Verify That Both Roll Value Type Should Be Same.");
		softAssert.assertEquals(rollValueYearFirst,"2022", "SMAB-T3932, SMAB-T4271:Verify That year Should Be Same.");
		softAssert.assertEquals(rollValueLandValueFirst,"$244,355", "SMAB-T3932, SMAB-T4271:Verify That Both Land Value Should Be Same.");
		softAssert.assertEquals(rollValueImprovementValueFirst,"$244,355", "SMAB-T3932, SMAB-T4271:Verify That Both Improvement Value Should Be Same.");
	
		softAssert.assertEquals(rollValueTypeSecond,"Supplemental", "SMAB-T3932, SMAB-T4271:Verify That Both Assessed Value Type Should Be Same.");
		softAssert.assertEquals(rollValueYearSecond,"2021", "SMAB-T3932, SMAB-T4271:Verify That Both Start Date Should Be Same.");
		softAssert.assertEquals(rollValueLandValueSecond,"$241,936", "SMAB-T3932, SMAB-T4271:Verify That Both Land Value Should Be Same.");
		softAssert.assertEquals(rollValueImprovementValueSecond,"$241,936", "SMAB-T3932, SMAB-T4271:Verify That Both Improvement Value Should Be Same.");

		objCioTransfer.logout();

	}
	
	/*
	 * Verify the Recorded Document column in COS Doc Summary has a linked URL , navigating to the recorded document record
	 */
	@Test(description = "SMAB-T4388: Verify user is able to navigate to the recorded document through the COS Doc Summary linked Document column", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" }, enabled = true)
	public void CIO_RecordedEvent_EventIDInCOSDocumentSummary(String loginUser) throws Exception {
		
		// ----- Data set up -----
		
		// Get a recorded document 
		String recordedDocumentQuery = "SELECT Id, Name FROM recorded_document__c WHERE recorder_doc_type__c='DE' and xAPN_count__c=1 limit 1"; 
		String recordedDocumentId = salesforceAPI.select(recordedDocumentQuery).get("Id").get(0);
		String recordedDocumentName = salesforceAPI.select(recordedDocumentQuery).get("Name").get(0);
		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentId);
		
		// Login as SysAdmin
		objMappingPage.login(loginUser);
		Thread.sleep(3000);
				
		objCioTransfer.addRecordedApn(recordedDocumentId, 1);		
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentId);
		
		// Query to fetch WI
		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO' And status__c='In pool' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(workItemNo);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		
		// ----- Steps -----
		
		// Step 1:  User navigates to a parcel with a recorded document associated
		String executionEnv = System.getProperty("region");
		driver.navigate().to("https://smcacre--"
				+ executionEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
						.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
				+ "/view");
		Thread.sleep(3000);
		
		// Step 2: User clicks on "COS Document Summary" button
		objParcelsPage.waitUntilPageisReady(driver);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.cosDocumentSummaryText));
		
		// Step 3: User clicks on the recorded document
		objParcelsPage.Click(objParcelsPage.lastItemInCosDocumentSummary);
		String parentWindow = driver.getWindowHandle();
		objParcelsPage.switchToNewWindow(parentWindow);
		objParcelsPage.waitUntilPageisReady(driver);
		
		String currentRecordedDocumentName = objCioTransfer.getFieldValueFromAPAS("Recorded Document Name");
		softAssert.assertEquals(recordedDocumentName, currentRecordedDocumentName, "SMAB-T4388: Verify the Recorded Document column in COS Doc Summary has a linked URL , navigating to the recorded document record");
		
		objParcelsPage.logout();
	}

}