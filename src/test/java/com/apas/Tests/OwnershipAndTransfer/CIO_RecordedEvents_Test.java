package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
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
	JSONObject jsonObject = new JSONObject();
	String apnPrefix = new String();
	CIOTransferPage objCioTransfer;
	AuditTrailPage trail;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		objCioTransfer = new CIOTransferPage(driver);
		trail = new AuditTrailPage(driver);
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

	}
	/*
	 * Verify that NO APN WI is genrated for document without APN and user has the
	 * ability to add recorded APN on it to create a WI for MAPPING OR CIO
	 * 
	 */

	@Test(description = "SMAB-T3106,SMAB-T3111:Verify the type of WI system created for a recorded document with no APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration", "Smoke" }, enabled = true)
	public void RecorderIntegration_VerifyNewWIgeneratedfromRecorderIntegrationForNOAPNRecordedDocument(
			String loginUser) throws Exception {

		String getApnToAdd = "Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) AND Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> hashMapRecordedApn = salesforceAPI.select(getApnToAdd);
		String recordedAPN = hashMapRecordedApn.get("Name").get(0);

		// login with sys admin

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		salesforceAPI.update("Work_Item__c",
				"SELECT Id FROM Work_Item__c where Sub_type__c='NO APN - CIO' and status__c ='In pool'", "status__c",
				"In Progress");
		objCioTransfer.generateRecorderJobWorkItems(objMappingPage.DOC_CERTIFICATE_OF_COMPLIANCE, 0);

		String WorkItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='NO APN' AND Sub_type__c='NO APN - MAPPING'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";
		Thread.sleep(3000);
		String WorkItemNo = salesforceAPI.select(WorkItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(WorkItemNo);
		Thread.sleep(2000);

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
			"Regression", "ChangeInOwnershipManagement", "OwnershipAndTransfer" }, enabled = true)
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
		objMappingPage.searchModule(PARCELS);
		salesforceAPI.update("Work_Item__c",
				"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c",
				"In Progress");
		objCioTransfer.generateRecorderJobWorkItems(objCioTransfer.DOC_DEED, 1);

		// Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		objMappingPage.globalSearchRecords(workItemNo);
		Thread.sleep(5000);
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
		objCioTransfer.enter(objCioTransfer.OwnershipStartDate,
				hashMapOwnershipAndTransferGranteeCreationData.get("Ownership Start Date"));
		objCioTransfer.enter(objCioTransfer.OwnershipEndDate, "7/15/2021");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.LastNameLabel),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
				"SMAB-T3281: Verify user is  able to save mail to record with enddate greater than start date,as by default DOR is taken as a ownership start date");
		softAssert.assertContains(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.Status), "Active",
				"SMAB-T3281: Verifying that status of grantee is active");

		// Editing the grantee record to make ownership end date lesser than ownership
		// start date

		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.Edit);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.Edit));
		objCioTransfer.enter(objCioTransfer.OwnershipStartDate,
				hashMapOwnershipAndTransferGranteeCreationData.get("Ownership Start Date"));

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

	@Test(description = "SMAB-T3427,SMAB-T3306,SMAB-T3446,SMAB-T3307,SMAB-T3308,SMAB-T3691:Verify that User is able to perform partial transfer and able to create mail to records ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "OwnershipAndTransfer", "Smoke" }, enabled = true)
	public void OwnershipAndTransfer_VerifyPartialOwnershipTransfer(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");

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
		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		objCioTransfer.addRecordedApn(recordedDocumentID, 1);
		objCioTransfer.deleteOldGranteesRecords(recordedDocumentID);

		salesforceAPI.update("Work_Item__c",
				"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c",
				"In Progress");
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";
		Thread.sleep(3000);
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		objMappingPage.searchModule("APAS");
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
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertContains(driver.getCurrentUrl(), navigationUrL.get("Navigation_Url__c").get(0),
				"SMAB-T3306:Validating that user navigates to CIo transfer screenafter clicking on related action hyperlink");

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

		HashMap<String, ArrayList<String>> hashMapcopyTomailTo = objCioTransfer.getGridDataForRowString("1");

		// STEP 14-Validating the formatted name 1 for mail to record

		softAssert.assertEquals(hashMapcopyTomailTo.get(objCioTransfer.formattedName1Label).get(0), granteeForMailTo,
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

		// STEP 19-Navigating back to RAT screen and clicking on back quick action
		// button

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
	 * This test  method verifies that user is able to manually initiate the auto approve process , if response has come back within 45 days of wait period.
	 * 
	 */

	@Test(description = "SMAB-T3377,SMAB-T10081:Verify that User is able to perform CIO transfer autoconfirm when some response do come back with in 45 days wait period", dataProvider = "dpForCioAutoConfirm", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "OwnershipAndTransfer" })
	public void OwnershipAndTransfer_VerifyCioTransferAutoConfirm(String InitialEventCode, String finalEventCode,
			String response) throws Exception {

		String execEnv = System.getProperty("region");

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String dataToCreateCorrespondenceEventForAutoConfirm = 	testdata.UNRECORDED_EVENT_DATA;
		Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
				dataToCreateCorrespondenceEventForAutoConfirm, "DataToCreateCorrespondenceEventForAutoConfirm");

		String recordedDocumentID = salesforceAPI
				.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);
		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);

		// objMappingPage.searchModule("APAS");
		objCioTransfer.addRecordedApn(recordedDocumentID, 1);
		objCioTransfer.deleteOldGranteesRecords(recordedDocumentID);

		salesforceAPI.update("Work_Item__c",
				"SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c",
				"In Progress");
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
		jsonObject.put("DOR__c", dateOfEvent);
		jsonObject.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

		objMappingPage.logout();

		// STEP 5-Login with CIO staff

		objMappingPage.login(users.CIO_STAFF);
		objMappingPage.globalSearchRecords(workItemNo);		
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the recorded apn transfer id

		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];		
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// STEP 8-Creating the new grantee

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataForRowString("1");
		String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);
		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));

		//  STEP 9-create new mail to record

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

		// Step 9(a): Creating Outbound Event

		objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);		
		String urlForTransactionTrail = driver.getCurrentUrl();
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

		// Step 10 : Submitting for review

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 11-Clicking on submit for Review quick action button

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForReview);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForReview);
		ReportLogger.INFO("CIO!! Transfer submitted for Review");
		objCioTransfer.waitForElementToBeVisible(objCioTransfer.cioTransferSuccessMsg);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.cioTransferSuccessMsg),
				"CIO transfer initial determination is submitted for review.",
				"SMAB-T3377,SMAB-T10081:Cio trasnfer is submited for review");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		objCioTransfer.logout();

		//Step-12: Login with CIO supervisor

		objCioTransfer.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionReviewComplete);
		objCioTransfer.Click(objCioTransfer.quickActionOptionReviewComplete);
		ReportLogger.INFO("CIO!! Transfer Review Completed");		
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.cioTransferSuccessMsg),
				"CIO transfer initial determination review completed.", "SMAB-T3377,SMAB-T10081:Cio trasnfer review is completed");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		objCioTransfer.logout();

		//Step 13: If Response comes back within 45 days and no issues are reported.

		if (response.equalsIgnoreCase("No Edits required")) {

			objCioTransfer.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			objCioTransfer.editRecordedApnField(objCioTransfer.transferStatusLabel);
			objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatusLabel);
			objCioTransfer.Click(objCioTransfer.getWebElementWithLabel(objCioTransfer.transferStatusLabel));

			// Clicking on review acesse picklist to manually approve the transfer

			objCioTransfer.javascriptClick(objCioTransfer.reviewAssecesseLink);
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

			//Verifying the status of transfer

			softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferStatusLabel),
					"Approved", "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			softAssert.assertEquals(objCioTransfer.getFieldValueFromAPAS(objCioTransfer.transferCodeLabel),
					finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

			//Navigating to WI from back button

			objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
			String parentAuditTrailNumber = objWorkItemHomePage
					.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);

			//Verifying that AT,WI statuses are completed after manual approval

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
		//Step 14:If response comes back and transfer code is required to be changed as a part of response

		if (response.equalsIgnoreCase("Event Code needs to be changed")) {

			objCioTransfer.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

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
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

			// STEP 16-Clicking on submit for approval quick action button

			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
			objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
			ReportLogger.INFO("CIO!! Transfer submitted for approval");
			objCioTransfer.waitForElementToBeVisible(objCioTransfer.cioTransferSuccessMsg);
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.cioTransferSuccessMsg),
					"Work Item has been submitted for Approval.", "SMAB-T3377,SMAB-T10081:Cio trasnfer is submited for approval");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
			objCioTransfer.logout();

			// login with cio supervisor

			objCioTransfer.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionApprove);
			objCioTransfer.Click(objCioTransfer.quickActionOptionApprove);
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.cioTransferSuccessMsg),
					"Work Item has been approved successfully.", "SMAB-T3377,SMAB-T10081:Cio transfer is approved successfully");
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
	 * This test method is used to assert that CIO auto confirm using batch job is able to autoconfirm transfer after no response came within 45 days of wait period
	 */

	@Test(description = "SMAB-T3377,SMAB-T10081:Verify that User is able to perform CIO transfer autoconfirm using a batch job (Fully automated) ", dataProvider = "dpForCioAutoConfirmUsingBatchJob", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "OwnershipAndTransfer", "Smoke" })
	public void OwnershipAndTransfer_VerifyCioTransferAutoConfirmUsingBatchJob(String InitialEventCode,
			String finalEventCode) throws Exception {

		{
			String execEnv = System.getProperty("region");

			String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
			Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
					OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

			String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
			Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
					OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

			Map<String, String> hashMapCreateOwnershipRecordData = objUtil
					.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

			String dataToCreateCorrespondenceEventForAutoConfirm = 	testdata.UNRECORDED_EVENT_DATA;
			Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
					dataToCreateCorrespondenceEventForAutoConfirm, "DataToCreateCorrespondenceEventForAutoConfirm");

			String recordedDocumentID = salesforceAPI
					.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
					.get("Id").get(0);
			objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

			// STEP 1-login with SYS-ADMIN

			objMappingPage.login(users.SYSTEM_ADMIN);
			// objMappingPage.searchModule("APAS");
			objCioTransfer.addRecordedApn(recordedDocumentID, 1);
			objCioTransfer.deleteOldGranteesRecords(recordedDocumentID);

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
			jsonObject.put("DOR__c", dateOfEvent);
			jsonObject.put("DOV_Date__c", dateOfEvent);
			salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

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
			salesforceAPI.update("Recorded_APN_Transfer__c",recordeAPNTransferID, "Auto_Confirm_Start_Date__c","2021-04-07" );
			ReportLogger.INFO("Putting Auto confirm date prior to 45 days ");

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
			objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);
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

			//Creating Unrecorded transfer

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
			objCioTransfer.waitForElementToBeVisible(objCioTransfer.cioTransferSuccessMsg);
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.cioTransferSuccessMsg),
					"CIO transfer initial determination is submitted for review.",
					"SMAB-T3377,SMAB-T10081:Cio trasnfer is submited for review");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
			objCioTransfer.logout();

			//Login with superviosr to complete reviews

			objCioTransfer.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
			objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

			objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionReviewComplete);
			objCioTransfer.Click(objCioTransfer.quickActionOptionReviewComplete);
			softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.cioTransferSuccessMsg),
					"CIO transfer initial determination review completed.",
					"SMAB-T3377,SMAB-T10081:Cio trasnfer review is completed");
			objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
			objCioTransfer.logout();

			// Step 12:Login with sysadmin to start autoconfirm batch job

			objMappingPage.login(users.SYSTEM_ADMIN);
			salesforceAPI.generateReminderWorkItems(SalesforceAPI.CIO_AUTOCONFIRM_BATCH_JOB);
			objCioTransfer.logout();

			//Step 13: login with cio staff to validate that auto confirm has taken place for impending transfer

			objCioTransfer.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			//STEP 14 : Verifying transfer code has changed after approval and equals to autoconfirm counterpart of the initial code

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

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");

			// STEP 16:Verifying that outbound event is completed

			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondence), parentAuditTrailNumber,
					"SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");

			objCioTransfer.logout();


		}

	}

	/**
	 * Verify user is able to use the Calculate Ownership where ownership is acquired over multiple DOV's for the same owner and owner with one DOV is completely retained
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3696 : Verify user is able to use the Calculate Ownership where ownership is acquired over multiple DOV's for the same owner and owner with one DOV is completely retained", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement" },enabled=true)
	public void OwnershipAndTransfer_Calculate_Ownership_SameOwnerMultipleDOV(String loginUser) throws Exception {

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
		Thread.sleep(7000);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		// step 2: fetching the recorded apn transfer object associated with the CIO WI and updating the DOV
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + cioWorkItem + "'";
		String recordeAPNTransferID = salesforceAPI.select(queryRecordedAPNTransfer).get("Navigation_Url__c").get(0).split("/")[3];
		
		jsonObject.put("xDOV__c", "2007-02-03");
		salesforceAPI.update("Recorded_APN_Transfer__c", recordeAPNTransferID, jsonObject);

		//deleting the CIO Transfer grantees for the current transfer screen
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);

		// step 3: deleting the current ownership records for the APN linked with CIO WI
		String queryAPN = "SELECT Parcel__c FROM Recorded_APN_Transfer__c where id='" + recordeAPNTransferID + "'";
		String apn=salesforceAPI.select(queryAPN).get("Parcel__c").get(0);
		objCioTransfer.deleteOwnershipFromParcel(apn);
		queryAPN = "SELECT name FROM Parcel__c where id='" + apn + "'";
		String apnvalue=salesforceAPI.select(queryAPN).get("Name").get(0);

		//step 4: Fetching Assessee records
		String queryAssesseeRecord = "SELECT Id, Name,FirstName ,LastName  FROM Account Limit 1";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);
		String assesseeFirstName = responseAssesseeDetails.get("FirstName").get(0);
		String assesseeLastName = responseAssesseeDetails.get("LastName").get(0);

		//step 5 : creating two new ownership records with different DOVs but same owner
		objCioTransfer.login(SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		Thread.sleep(5000);
		objMappingPage.closeDefaultOpenTabs();

		for(int i=0;i<2;i++)
		{
			objMappingPage.globalSearchRecords(apnvalue);
			objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
			if(i==0)
			{	hashMapCreateOwnershipRecordData.put("Ownership Percentage", "75");
				hashMapCreateOwnershipRecordData.put("Ownership Start Date", "5/3/2010");
			}
			if(i==1)
			{	hashMapCreateOwnershipRecordData.put("Ownership Percentage", "25");
				hashMapCreateOwnershipRecordData.put("Ownership Start Date", "7/2/2018");
			}
			objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);

			String ownershipId = driver.getCurrentUrl().split("/")[6];
			String date = salesforceAPI
					.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
					.get("Ownership_Start_Date__c").get(0);
			//  updating the ownership dates for new owners
			jsonObject.put("DOR__c", date);
			jsonObject.put("DOV_Date__c", date);
			salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);
		}

		// Step6: Opening the work items and accepting the WI created by recorder batch
		objCioTransfer.logout();
		Thread.sleep(5000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		objCioTransfer.login(loginUser);
		objCioTransfer.searchModule(HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);	  	

		// Step7: CIO staff user navigating to transfer screen by clicking on related action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCioTransfer.waitForElementToBeVisible(20,objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.scrollToBottom();
		String dov=objCioTransfer.getFieldValueFromAPAS(objCioTransfer.dovLabel);
		String dor=objCioTransfer.getFieldValueFromAPAS(objCioTransfer.dorLabel);

		//step 8: creating new grantee with 10 % ownership 
		ReportLogger.INFO("Creating new grantee record");
		hashMapOwnershipAndTransferGranteeCreationData.put("Owner Percentage", "10");
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);	
		ReportLogger.INFO("Grantee record created successfully");

		// Step9: CIO staff user navigating to transfer screen by clicking on related action link
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(15,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);

		List<WebElement> cioTransferScreenCalculateOwnershipModalFields=objCioTransfer.locateElements(objCioTransfer.fieldsInCalculateOwnershipModal, 10);	
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(5)),assesseeFirstName,
				"SMAB-T3696: Validation that First Name field is assesseeFirstName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(6)),assesseeLastName,
				"SMAB-T3696: Validation that last Name field is assesseelastName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(7)),"25",
				"SMAB-T3696: Validation that Ownership Percentage field in calculate owenrship modal is percentage value for owner with latest DOV");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(8)),"July 2, 2018",
				"SMAB-T3696: Validation that DOV field in calculate owenrship modal is latest DOV in all ownership records for parcel");

		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "15");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);
		cioTransferScreenCalculateOwnershipModalFields=objCioTransfer.locateElements(objCioTransfer.fieldsInCalculateOwnershipModal, 10);	
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(5)),assesseeFirstName,
				"SMAB-T3696: Validation that First Name field for second owner is assesseeFirstName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(6)),assesseeLastName,
				"SMAB-T3696: Validation that last Name field for second owner is assesseelastName in calculate ownership modal");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(7)),"75",
				"SMAB-T3696: Validation that Ownership Percentage field for second owner in calculate owenrship modal is percentage value for owner with second latest DOV");
		softAssert.assertEquals(objCioTransfer.getElementText(cioTransferScreenCalculateOwnershipModalFields.get(8)),"May 3, 2010",
				"SMAB-T3696: Validation that DOV field for second owner in calculate owenrship modal is second latest DOV in all ownership records for parcel");
		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "75");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));

		//step 10 :Validating the grantee table

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		objCioTransfer.waitForElementToBeVisible(5,objCioTransfer.newButton);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.columnInGrid.replace("columnName",objCioTransfer.ownershipPercentage));
		objCioTransfer.sortInGrid(objCioTransfer.ownershipPercentage,true);
		HashMap<String, ArrayList<String>> granteeHashMap = objCioTransfer.getGridDataInHashMap();

		softAssert.assertEquals(granteeHashMap.get("Grantee/Retain Owner Name").get(0),hashMapOwnershipAndTransferGranteeCreationData.get("Last Name") ,
				"SMAB-T3696: Validation that Grantee name that was created in grantee table  is correct" );
		softAssert.assertEquals(granteeHashMap.get("Status").get(0),"Active",
				"SMAB-T3696: Validation that Grantee that was created has active status");
		softAssert.assertEquals(granteeHashMap.get("Owner Percentage").get(0),"10.0000%",
				"SMAB-T3696: Validation that Owner Percentage of grantee that was created is correct");
		softAssert.assertEquals(granteeHashMap.get("DOR").get(0),dor,
				"SMAB-T3696: Validation that DOR of grantee that was craeted is DOR of recorded document");
		softAssert.assertEquals(granteeHashMap.get("DOV").get(0),dov,
				"SMAB-T3696: Validation that DOV of grantee that was craeted is DOV of recorded document");
		softAssert.assertEquals(granteeHashMap.get("Ownership Start Date").get(0),dov,
				"SMAB-T3696: Validation that Ownership Start Date of grantee that was created is the DOV of recorded document");

		softAssert.assertEquals(granteeHashMap.get("Grantee/Retain Owner Name").get(1),assesseeLastName+" "+assesseeFirstName,
				"SMAB-T3696: Validation that current owner name (that was retained partially )in grantee table  after calculate ownership is correct ");
		softAssert.assertEquals(granteeHashMap.get("Status").get(1),"Active",
				"SMAB-T3696: Validation that current owner  (that was retained partially ) in Grantee table has active status");
		softAssert.assertEquals(granteeHashMap.get("Owner Percentage").get(1),"15.0000%",
				"SMAB-T3696: Validation that Owner Percentage of owner that was partially retained is correct");
		softAssert.assertEquals(granteeHashMap.get("DOR").get(1),"7/2/2018",
				"SMAB-T3696: Validation that DOR of owner that was partially retained is DOR of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("DOV").get(1),"7/2/2018",
				"SMAB-T3696: Validation that DOV of owner that was partially retained is DOV of of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("Ownership Start Date").get(1),"7/2/2018",
				"SMAB-T3696: Validation that Ownership Start Date of owner that was partially retained is Ownership Start Date of original ownership record");

		softAssert.assertEquals(granteeHashMap.get("Grantee/Retain Owner Name").get(2),assesseeLastName+" "+assesseeFirstName,
				"SMAB-T3696: Validation that current owner name (that was retained fully )in grantee table  after calculate ownership is correct ");
		softAssert.assertEquals(granteeHashMap.get("Status").get(2),"Retained",
				"SMAB-T3696: Validation that current owner  (that was retained fully ) in Grantee table has active status");
		softAssert.assertEquals(granteeHashMap.get("Owner Percentage").get(2),"75.0000%",
				"SMAB-T3696: Validation that Owner Percentage of owner that was fully retained is correct");
		softAssert.assertEquals(granteeHashMap.get("DOR").get(2),"5/3/2010",
				"SMAB-T3696: Validation that DOR of owner that was fully retained is  DOR of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("DOV").get(2),"5/3/2010",
				"SMAB-T3696: Validation that DOV of owner that was fully retained is is DOV of original ownership record");
		softAssert.assertEquals(granteeHashMap.get("Ownership Start Date").get(2),"5/3/2010",
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
		objCioTransfer.waitForElementToBeVisible(objCioTransfer.confirmationMessageOnTranferScreen);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.confirmationMessageOnTranferScreen),"Work Item has been submitted for Approval.",
				"SMAB-T3696: Validation that proper mesage is displayed after submit for approval");
		
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 6);
		
		//step 12 : navigating to ownersip records page of parcel
		driver.navigate()
		.to("https://smcacre--"
				+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
				.select("Select Id from parcel__C where name='" + apnvalue + "'").get("Id").get(0)
				+ "/related/Property_Ownerships__r/view");
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.columnInGrid.replace("columnName",objCioTransfer.ownershipPercentage));
		objCioTransfer.sortInGrid(objCioTransfer.ownershipPercentage,true);
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();

		// STEP 13-Validating the Owners ,their status and ownership percentages
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
				"SMAB-T3696:Validating that the grantee that was created from transfer screen has become  new owner");

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active",
				"SMAB-T3696: Validating that status of new owner which is the grantee created from transfer screen is Active");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "10.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of new owner which is the grantee created from transfer screen is correct");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0), dov,
				"SMAB-T3696: Validating that Ownership Start Date of new owner which is the grantee created from transfer screen is DOVof recorded document");

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(0), dor,
				"SMAB-T3696: Validating that DOR of new owner which is the grantee created from transfer screen is DOR of recorded doc");

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(0), dov,
				"SMAB-T3696: Validating that DOV of new owner which is the grantee created from transfer screen is DOV of recorded doc");

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(1), assesseeName,
				"SMAB-T3696:Validating that the partially retained owner has become  new owner");

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(1), "Active",
				"SMAB-T3696: Validating that status of partially retained owner created from transfer screen is Active");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(1), "15.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of partially retained owner retained from transfer screen is correct");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(1), "7/2/2018",
				"SMAB-T3696: Validating that Ownership Start Date of partially retained owner created from transfer screen is Ownership Start Date of original record");

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(1), "7/2/2018",
				"SMAB-T3696: Validating that DOR of partially retained owner created from transfer screen is DOR of original ownership record");

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(1), "7/2/2018",
				"SMAB-T3696: Validating that DOV of partially retained owner created from transfer screen is DOV of original ownership record");


		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(2), assesseeName,
				"SMAB-T3696:Validating that the old ownership that was  partially retained is retired");

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(2), "Retired",
				"SMAB-T3696: Validating that the old ownership that was  partially retained is retired");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(2), "25.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of partially retained owner which is now retired is correct");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(2), "7/2/2018",
				"SMAB-T3696: Validating that Ownership Start Date of partially retained owner which is now retired is Ownership Start Date of original record");
		
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership end Date").get(2), dor,
				"SMAB-T3696: Validating that Ownership end Date of partially retained owner which is now retired is DOR of original ownership record");

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(2), "7/2/2018",
				"SMAB-T3696: Validating that DOR of partially retained owner which is now retired is DOR of original ownership record");

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(2), "7/2/2018",
				"SMAB-T3696: Validating that DOV of partially retained owner which is now retired is DOV of original ownership record");

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(3), assesseeName,
				"SMAB-T3696:Validating that the fully retained owner is now active owner");

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(3), "Active",
				"SMAB-T3696: Validating that status of fully retained owner created from transfer screen is Active");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(3), "75.0000%",
				"SMAB-T3696: Validating that Ownership Percentage of fully retained is correct");

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(3), "5/3/2010",
				"SMAB-T3696: Validating that Ownership Start Date of fully retained owner  is Ownership Start Date of original record");

		softAssert.assertEquals(HashMapLatestOwner.get("DOR").get(3), "5/3/2010",
				"SMAB-T3696: Validating that DOR of fully retained owner  is DOR of original ownership record");

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(3), "5/3/2010",
				"SMAB-T3696: Validating that DOV of fully retained owner  is DOV of original ownership record");

		objCioTransfer.logout();
	}
	/*
     * Verify that User is able to perform CIO transfer  for recorded APN and validate all status
	 */
	
	@Test(description = "SMAB-T3525:Verify that User is able to perform CIO transfer  for recorded APN and validate all status", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","OwnershipAndTransfer" })
	public void OwnershipAndTransfer_VerifyStatus(String loginUser) throws Exception {
		
		String execEnv= System.getProperty("region");		

		String OwnershipAndTransferCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData,
				"dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData,
				"dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData,
				"DataToCreateOwnershipRecord");

		String recordedDocumentID=salesforceAPI.select(" SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1").get("Id").get(0);
		objCioTransfer.deleteOldGranteesRecords(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c where Type__c='CIO' AND AGE__C=0 AND status__c ='In Pool'", "status__c","In Progress");
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		//  STEP 2-Query to fetch WI

		String workItemQuery="SELECT Id,name FROM Work_Item__c where Type__c='CIO'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";					
		String workItemNo=salesforceAPI.select(workItemQuery).get("Name").get(0);
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
		String transferScreenURL=driver.getCurrentUrl();
		
		ReportLogger.INFO("Add the Transfer Code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-SALE");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		Thread.sleep(2000);
		
		//STEP 8-Creating the new grantee

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			


		//STEP 9-Validating present grantee			 

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+"/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap  = objCioTransfer.getGridDataForRowString("1");
		String granteeForMailTo= granteeHashMap.get("Grantee/Retain Owner Name").get(0);
		String ownershipDovForNewGrantee=granteeHashMap.get("DOV").get(0);
		
		// STEP 10-Creating copy to mail to record

		objCioTransfer.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCioTransfer.waitForElementToBeClickable(7, objCioTransfer.copyToMailToButtonLabel);
		
		//STEP 11-Validating mail to record created from copy to mail to

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
		objCioTransfer.waitForElementToBeClickable(5, objCioTransfer.newButton);

		//STEP 12-Navigating back to RAT screen

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 13-Clicking on submit for approval quick action button

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		Thread.sleep(2000);
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus),"Submitted for Approval", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after submit for approval.");

		//STEP 14- Get audit trail Value from transfer screen and validate the status
		String auditTrailName =objWorkItemHomePage.getElementText(objCioTransfer.CIOAuditTrail);
		String auditTrailID=salesforceAPI.select("SELECT Id,Status__c,Name FROM Transaction_Trail__c where Name='"+auditTrailName+"'").get("Id").get(0);
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		objCioTransfer.waitUntilPageisReady(driver);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", "SMAB-T3525: Validating that audit trail status should be open after submit for approval.");


		//STEP 15-Navigating back to RAT screen and clicking on back quick action button

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCioTransfer.waitForElementToBeClickable(5,objCioTransfer.quickActionButtonDropdownIcon);	          
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 16-Validating that back button has navigates the user to WI page and status of WI should be submitted for approval.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval", "SMAB-T3525: Validating that status of WI should be submitted for approval.");
		objCioTransfer.logout();
		Thread.sleep(5000);

		// STEP 17 :- login with  CIO supervisor
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);          
		
		// STEP 18 - Clicking on return quick action button

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionReturn);
		objCioTransfer.Click(objCioTransfer.quickActionOptionReturn);
		objCioTransfer.waitForElementToBeVisible(5,objCioTransfer.returnReasonTextBox);
		objCioTransfer.enter(objCioTransfer.returnReasonTextBox, "return by CIO supervisor");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		Thread.sleep(2000);
		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus),"Returned", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after returned by supervisor.");

		objCioTransfer.waitForElementToBeClickable(5,objCioTransfer.quickActionButtonDropdownIcon);	          
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 19-Validating WI and AUDIT Trail status after returned by supervisor.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Returned", "SMAB-T3525: Validating that Back button navigates back to WI page ");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", "SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		objCioTransfer.logout();
		Thread.sleep(5000);

		objMappingPage.login(loginUser);
		driver.navigate().to(transferScreenURL);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 20-Clicking on submit for approval quick action button

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		Thread.sleep(2000);
		ReportLogger.INFO("CIO!! Transfer resubmit for approval by staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus),"Submitted for Approval", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after resubmit for approval by staff.");

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 21-Validating that back button has navigates the user to WI page.
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval", "SMAB-T3525: Validating WI after resubmit for approval ");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", "SMAB-T3525: Validating that audit trail status should be open after resubmit for approval.");


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
		Thread.sleep(2000);
		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus),"Approved", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after approved by supervisor.");

		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 23-Validating that WI and audit trail status after approving the transfer activity.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", "SMAB-T3525: Validating that WI status should be completed after approval by supervisor.");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", "SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		objCioTransfer.logout();

	}			
	       
}