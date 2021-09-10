package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
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
	String ownershipCreationData;
	String OwnershipAndTransferCreationData;

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
		ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		OwnershipAndTransferCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
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
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration", "Smoke" }, enabled = true)
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
		objMappingPage.searchModule("APAS");
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
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" })
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
		Thread.sleep(2000);
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
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration", "Smoke" })
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
	/*
     * Verify that User is able to perform CIO transfer  for recorded APN and validate all status
	 */
	
	@Test(description = "SMAB-T3525, SMAB-T3341:Verify that User is able to perform CIO transfer  for recorded APN and validate all status", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","RecorderIntegration" })
	public void OwnershipAndTransfer_VerifyTransferActivityStatus_ReturnedAndCompleted(String loginUser) throws Exception {
		
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
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNo);	
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objCioTransfer.deleteOwnershipFromParcel(salesforceAPI.select("Select Id from parcel__c where name='"+apnFromWIPage+"'").get("Id").get(0));

		//STEP 3- adding owner after deleting for the recorded APN 
		String acesseName= objMappingPage.getOwnerForMappingAction();	        
		objParcelsPage.createOwnershipRecord(apnFromWIPage, acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		//STEP 4- updating the ownership date for current owners
		String dateOfEvent= salesforceAPI.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '"+ownershipId+"'").get("Ownership_Start_Date__c").get(0);      
		jsonObject.put("DOR__c",dateOfEvent);
		jsonObject.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

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
		
		//STEP 8-Creating the new grantee
		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			

		//STEP 9-Validating present grantee			 
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+"/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap  = objCioTransfer.getGridDataForRowString("1");
		String granteeForMailTo= granteeHashMap.get("Grantee/Retain Owner Name").get(0);
		
		// STEP 10-Creating copy to mail to record
		objCioTransfer.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.copyToMailToButtonLabel);
		
		//STEP 11-Validating mail to record created from copy to mail to
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
		objCioTransfer.waitForElementToBeClickable(10, objCioTransfer.newButton);

		//STEP 12-Navigating back to RAT screen
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);

		// STEP 13-Clicking on submit for approval quick action button
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		Thread.sleep(2000); //Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
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
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
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
		objCioTransfer.enter(objCioTransfer.returnReasonTextBox, "Returned by CIO Supervisor");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.finishButtonPopUp);
		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		
		Thread.sleep(2000); //Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus),"Returned", "SMAB-T3525, SMAB-T3341: Validating CIO Transfer activity status on transfer activity screen after returned by supervisor.");

		objCioTransfer.waitForElementToBeClickable(5,objCioTransfer.quickActionButtonDropdownIcon);	          
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 19-Validating WI and AUDIT Trail status after returned by supervisor.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Returned", "SMAB-T3525, SMAB-T3341: Validating that Back button navigates back to WI page ");
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
		
		Thread.sleep(2000);//Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer resubmit for approval by staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus),"Submitted for Approval", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after resubmit for approval by staff.");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		
		// Validate the Ownership record on the parcel
     	ReportLogger.INFO("Validate the Current & New Ownership record in Grid after transfer activity is submitted for approval");
     	objCioTransfer.clickViewAll("Ownership for Parent Parcel");
        HashMap<String, ArrayList<String>>HashMapLatestOwner  = objCioTransfer.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"), 
        	  "SMAB-T3341: Validate the owner name on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "100.0000%", 
          	  "SMAB-T3341: Validate the ownership percentage on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active", 
        	  "SMAB-T3341: Validate the status on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),"1/6/2021" , 
        	  "SMAB-T3341: Validate the start date on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner.get("Status").get(1), "Retired", 
          	  "SMAB-T3341: Validate the status on Old Ownership record");     
		
        driver.navigate().to(transferScreenURL);
        objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 21-Validating that back button has navigates the user to WI page.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval", "SMAB-T3525: Validating WI after resubmit for approval ");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		Thread.sleep(2000); //Allow the screen to appear completely
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
		
		Thread.sleep(2000);//Allow the screen to appear completely
		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus),"Approved", "SMAB-T3525, SMAB-T3341: Validating CIO Transfer activity status on transfer activity screen after approved by supervisor.");
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		
        // Validate the Ownership record on the parcel
     	ReportLogger.INFO("Validate the Current & New Ownership record in Grid after transfer activity is Approved");
     	objCioTransfer.clickViewAll("Ownership for Parent Parcel");
        HashMap<String, ArrayList<String>>HashMapLatestOwner1  = objCioTransfer.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestOwner1.get("Owner").get(0), hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"), 
        	  "SMAB-T3341: Validate the owner name on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner1.get("Ownership Percentage").get(0), "100.0000%", 
          	  "SMAB-T3341: Validate the ownership percentage on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner1.get("Status").get(0), "Active", 
        	  "SMAB-T3341: Validate the status on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner1.get("Ownership Start Date").get(0),"1/6/2021" , 
        	  "SMAB-T3341: Validate the start date on New Ownership record");
        softAssert.assertEquals(HashMapLatestOwner1.get("Status").get(1), "Retired", 
          	  "SMAB-T3341: Validate the status on Old Ownership record");     
		
        driver.navigate().to(transferScreenURL);
        objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 23-Validating that WI and audit trail status after approving the transfer activity.
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.wiStatusDetailsPage);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", "SMAB-T3525, SMAB-T3341: Validating that WI status should be completed after approval by supervisor.");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		Thread.sleep(2000);//Allow the screen to appear completely
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", "SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		
		
		objCioTransfer.logout();

	}			

	/**
	 * Verify that APN related details are updated when APN is updated on Recorded Event
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3232 : Verify that APN related details are updated when APN is updated on Recorded Event", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RecorderIntegration" },enabled=true)
	public void RecorderIntegration_VerifyAPNDetailsOnTransferActivityScreen(String loginUser) throws Exception {
		
		JSONObject jsonObject1 = new JSONObject();
		JSONObject jsonObject2 = new JSONObject();
		String execEnv= System.getProperty("region");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData, "DataToCreateOwnershipRecord");
		
		//Fetch values from Database and insert it in the Parcels
		String assesseeName = objMappingPage.getOwnerForMappingAction();
		
		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 1";
		String activeApn1 = salesforceAPI.select(queryForActiveAPN).get("Name").get(0);
		String activeApnId1 = salesforceAPI.select(queryForActiveAPN).get("Id").get(0);
		
		String queryForRetiredAPN = "select Name, Id from Parcel__c where Status__c='Retired' limit 1";
		String retiredApn = salesforceAPI.select(queryForRetiredAPN).get("Name").get(0);
		String retiredApnId = salesforceAPI.select(queryForRetiredAPN).get("Id").get(0);
		
		HashMap<String, ArrayList<String>> responsePUCDetails1= salesforceAPI.select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responsePUCDetails2= salesforceAPI.select("SELECT id, Name FROM PUC_Code__c where Name in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responseSitusDetails= salesforceAPI.select("SELECT Id, Name FROM Situs__c where Name != NULL LIMIT 2");
		String primarySitusId1=responseSitusDetails.get("Id").get(0);
		String primarySitusValue1=responseSitusDetails.get("Name").get(0);
		String primarySitusId2=responseSitusDetails.get("Id").get(1);
		String primarySitusValue2=responseSitusDetails.get("Name").get(1);
		
		String legalDescriptionValue1="Test Legal Description PM 85/25-260";	
		String legalDescriptionValue2="Test Legal Description PM 85/25-270";	
		
		jsonObject1.put("PUC_Code_Lookup__c", responsePUCDetails1.get("Id").get(0));
		jsonObject1.put("Short_Legal_Description__c",legalDescriptionValue1);
		jsonObject1.put("Primary_Situs__c",primarySitusId1);
		salesforceAPI.update("Parcel__c", activeApnId1, jsonObject1);
		
		jsonObject2.put("PUC_Code_Lookup__c", responsePUCDetails2.get("Id").get(0));
		jsonObject2.put("Short_Legal_Description__c",legalDescriptionValue2);
		jsonObject2.put("Primary_Situs__c",primarySitusId2);
		salesforceAPI.update("Parcel__c", retiredApnId, jsonObject2);
		
		//Delete existing Ownership records from the Active parcel
		objMappingPage.deleteOwnershipFromParcel(activeApnId1);
		
		// Step 1: Executing the recorder feed batch job to generate CIO WI & Add ownership records in the parcels
		objCioTransfer.generateRecorderJobWorkItems("DE", 1);
		Thread.sleep(7000);
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
		Thread.sleep(1000); //Allows the WI to load completely to avoid regression failure
		
		String activeApn2 = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		
		Thread.sleep(1000); //Allows the other screen to load
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.transferCodeLabel);
		
		//Step4: Fetch values from the screen
		String pucValue = "";
		String legalDescValue = "Test Short Legal Description";
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];
		
		HashMap<String, ArrayList<String>> responseApnDetail= salesforceAPI.select("SELECT Id FROM Parcel__c Where Name = '" + activeApn2+ "'");
		String activeApnId2 = responseApnDetail.get("Id").get(0);
		HashMap<String, ArrayList<String>> responseSitusDetail= salesforceAPI.select("SELECT Id, Name FROM Situs__c LIMIT 1");
		String primarySitusValue=responseSitusDetail.get("Name").get(0);
		salesforceAPI.update("Parcel__c", activeApnId2, "Primary_Situs__c", responseSitusDetail.get("Id").get(0));
		salesforceAPI.update("Parcel__c", activeApnId2, "Short_Legal_Description__c", legalDescValue);
		
		driver.navigate().refresh();
		Thread.sleep(3000); //Allows the screen to load and update the Situs & Legal Description value
		
		if (salesforceAPI.select("SELECT Name  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where name='"+ activeApn2 + "')") != null) 
			pucValue = salesforceAPI.select("SELECT Name  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where name='"+ activeApn2 + "')").get("Name").get(0);
		
		//Step5: Validate the Parcel related values on the screen
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.shortLegalDescriptionOnTransferActivityLabel),legalDescValue,
				"SMAB-T3232: Validate the Legal Description on CIO Transfer screen for " + activeApn2);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.pucCodeTransferActivityLabel),pucValue,
				"SMAB-T3232: Validate the PUC on CIO Transfer screen for " + activeApn2);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + activeApn2);
		
		// Step6: Update the APN value to Retired APN value and validate values
		ReportLogger.INFO("Update the APN value to a Retired Parcel value");
		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.clearSelectionFromLookup(objCioTransfer.ApnLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, retiredApn);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveLabel));
		Thread.sleep(3000); //Allows the record to save properly
		
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.shortLegalDescriptionOnTransferActivityLabel),legalDescriptionValue2,
				"SMAB-T3232: Validate the Legal Description on CIO Transfer screen for " + retiredApn);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.pucCodeTransferActivityLabel),responsePUCDetails2.get("Name").get(0),
				"SMAB-T3232: Validate the PUC on CIO Transfer screen for " + retiredApn);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),primarySitusValue2.replaceFirst("\\s", ""),
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + retiredApn);
		softAssert.assertTrue(objCioTransfer.verifyElementExists(objCioTransfer.warningMessageArea),
				"SMAB-T3232: Validate that warning message is displayed on CIO Transfer screen for Retired Parcel");
		
		// Step7: Update the APN value to Active APN value and validate values
		ReportLogger.INFO("Update the APN value to an Active Parcel value");
		objCioTransfer.editRecordedApnField(objCioTransfer.ApnLabel);
		objCioTransfer.clearSelectionFromLookup(objCioTransfer.ApnLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.ApnLabel, activeApn1);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveLabel));
		Thread.sleep(3000); //Allows the record to save properly
		
		String numOfMailToRecordOnRAT = objCioTransfer.getElementText(objCioTransfer.numberOfMailToLabel);
		softAssert.assertTrue(!objCioTransfer.verifyElementExists(objCioTransfer.warningMessageArea),
				"SMAB-T3232: Validate that warning message disappears on CIO Transfer screen");
		
		driver.navigate().refresh();
		Thread.sleep(3000); //Allows the screen to load
		
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.shortLegalDescriptionOnTransferActivityLabel),legalDescriptionValue1,
				"SMAB-T3232: Validate the Legal Description on CIO Transfer screen for " + activeApn1);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.pucCodeTransferActivityLabel),responsePUCDetails1.get("Name").get(0),
				"SMAB-T3232: Validate the PUC on CIO Transfer screen for " + activeApn1);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.situsOnTransferActivityLabel),primarySitusValue1.replaceFirst("\\s", ""),
				"SMAB-T3232: Validate the Situs on CIO Transfer screen for " + activeApn1);
		
		// Step8: Validate the Ownership record on the parcel
		ReportLogger.INFO("Validate the Current Ownership record in Grid");
		objCioTransfer.clickViewAll("Ownership for Parent Parcel");
        HashMap<String, ArrayList<String>>HashMapLatestOwner  = objCioTransfer.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), assesseeName, 
    		  "SMAB-T3232: Validate the owner name on Ownership record");
        softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active", 
    		  "SMAB-T3232: Validate the status on Ownership record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),hashMapCreateOwnershipRecordData.get("Ownership Start Date") , 
    		  "SMAB-T3232: Validate the start date on Ownership record");
        
        //Step 9: Validate the Mail-To record on the parcel, if exist
        ReportLogger.INFO("Validate the Mail-To record on the parcel");
        objCioTransfer.globalSearchRecords(activeApn1);
        objParcelsPage.openParcelRelatedTab("Mail-To");
        objCioTransfer.waitForElementToBeVisible(10, objParcelsPage.numberOfMailToOnParcelLabel);
        String numOfMailToRecordOnParcel = objCioTransfer.getElementText(objParcelsPage.numberOfMailToOnParcelLabel);
        
        if (!numOfMailToRecordOnParcel.equals("(0)")){
        	HashMap<String, ArrayList<String>> mailToTableDataHashMap = objParcelsPage.getParcelTableDataInHashMap("Mail-To");
        	String status = mailToTableDataHashMap.get("Status").get(0);
        	String formattedName1 = mailToTableDataHashMap.get("Formatted Name 1").get(0);
        	String formattedName2 = mailToTableDataHashMap.get("Formatted Name 2").get(0);
        	
        	driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
    		objCioTransfer.waitForElementToBeVisible(10,objCioTransfer.numberOfGrantorLabel);
    		objCioTransfer.clickViewAll("CIO Transfer Mail To");
			
            // Step9a: Validate the details in the grid
            HashMap<String, ArrayList<String>>HashMapMailTo  = objCioTransfer.getGridDataInHashMap();
            softAssert.assertEquals(HashMapMailTo.get("Status").get(0), status, 
        		  "SMAB-T3232: Validate the Status of Mail-To record");
            softAssert.assertEquals(HashMapMailTo.get("Formatted Name1").get(0), formattedName1, 
        		  "SMAB-T3232: Validate the Formatted Name1 of Mail-To record");
            softAssert.assertEquals(HashMapMailTo.get("Formatted Name2").get(0), formattedName2, 
        		  "SMAB-T3232: Validate the Formatted Name2 of Mail-To record");    
        }
        else
        {
        	ReportLogger.INFO("Validate if there is no Mail-To record on the parcel");
        	softAssert.assertTrue(numOfMailToRecordOnRAT.contains("0"), 
          		  "SMAB-T3232: Validate that there are no Mail-To Records");
        }
       
		//Step10: Submit for Approval and verify the status
        ReportLogger.INFO("Navigate to RAT screen and Submit the transfer activity record");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCioTransfer.waitForElementToBeVisible(10,objCioTransfer.numberOfGrantorLabel);
		
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.finishButton);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButton));
		objCioTransfer.waitForElementToBeVisible(10, objCioTransfer.transferStatusLabel);
		
		
		objCioTransfer.logout();	
	}

}