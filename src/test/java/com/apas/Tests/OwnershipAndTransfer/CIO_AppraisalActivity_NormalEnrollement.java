package com.apas.Tests.OwnershipAndTransfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.AppraisalActivityPage;

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

public class CIO_AppraisalActivity_NormalEnrollement extends TestBase implements users {

	private RemoteWebDriver driver;
	Page objPage;
	ParcelsPage objParcelsPage;
	ApasGenericPage objApasGenericPage;
	CIOTransferPage objCIOTransferPage;
	Util objUtil;
	SoftAssertion softAssert;
	WorkItemHomePage objWorkItemHomePage;	
	MappingPage objMappingPage;
	AppraisalActivityPage objAppraisalActivity;

	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objParcelsPage = new ParcelsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		objMappingPage =  new MappingPage(driver);
		objAppraisalActivity= new AppraisalActivityPage(driver);
		
	}
	/*
	 * Verify user is able to create Appraisal WI after approval of CIO WI for recorded documents
	 * 
	 * Last Modified by -Aditya 
	 * 
	 */
	
	@Test(description = "SMAB-T3786 : Verify user is able to CIO supervisor on approval is able to create Appraisal WI for non exempted CIO transfers ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement" }, enabled = true)
	public void OwnershipAndTransfer_CreateAppraiserActivityWorkItem(String loginUser) throws Exception {

		String excEnv = System.getProperty("region");

		JSONObject jsonForAppraiserActivity = objCIOTransferPage.getJsonObject();

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecord");

		objCIOTransferPage.login(SYSTEM_ADMIN);

		String recordedDocumentID = salesforceAPI
				.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);

		objCIOTransferPage.deleteRecordedApnFromRecordedDocument(recordedDocumentID);
		Thread.sleep(3000);
		objCIOTransferPage.addRecordedApn(recordedDocumentID, 1);

		objCIOTransferPage.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);

		objMappingPage.globalSearchRecords(workItemNo);

		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.ApnLabel, 5);
		String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);

		// Updating neighborhood code of parcel so Normal enrollement WI is generated

		salesforceAPI.update("Parcel__C",
				salesforceAPI.select("Select Id from parcel__c where name ='" + apnFromWIPage + "'").get("Id").get(0),
				"Neighborhood_Reference__c",
				salesforceAPI.select("Select Id from Neighborhood__c where name like '03%'").get("Id").get(0));

		// Deletin existing ownership from parcel
		objCIOTransferPage.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the recorded APN

		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate()
				.to("https://smcacre--"
						+ excEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];
		objParcelsPage.deleteOldAndCreateNewAssessedValuesRecords(hashMapCreateAssessedValueRecord, apnFromWIPage);

		// STEP 4- updating the ownership date for current owners

		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		jsonForAppraiserActivity.put("DOR__c", dateOfEvent);
		jsonForAppraiserActivity.put("DOV_Date__c", dateOfEvent);

		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonForAppraiserActivity);

		objMappingPage.logout();

		objMappingPage.login(loginUser);
		objMappingPage.waitForElementToBeClickable(objMappingPage.appLauncher, 10);
		objCIOTransferPage.searchModule(modules.EFILE_INTAKE);
		objMappingPage.globalSearchRecords(workItemNo);
		Thread.sleep(5000);
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the recorded apn transfer id

		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		objCIOTransferPage.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
		objCIOTransferPage.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertContains(driver.getCurrentUrl(), navigationUrL.get("Navigation_Url__c").get(0),
				"SMAB-T3306:Validating that user navigates to CIo transfer screenafter clicking on related action hyperlink");

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon, 10);
		ReportLogger.INFO("Add the Transfer Code");

		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel,
				objCIOTransferPage.CIO_EVENT_CODE_COPAL);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));

		// STEP 8-Creating the new grantee

		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID,
				hashMapOwnershipAndTransferGranteeCreationData);
		driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap = objCIOTransferPage.getGridDataForRowString("1");
		String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);

		// STEP 11- Performing calculate ownership to perform partial transfer

		driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"
				+ recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.calculateOwnershipButtonLabel);
		objCIOTransferPage
				.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		objCIOTransferPage.waitForElementToBeVisible(5, objCIOTransferPage.nextButton);
		objCIOTransferPage.enter(objCIOTransferPage.calculateOwnershipRetainedFeld, "50");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));

		// STEP 12-Creating copy to mail to record

		objCIOTransferPage.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCIOTransferPage.waitForElementToBeClickable(7, objCIOTransferPage.copyToMailToButtonLabel);

		// STEP 15-Navigating back to RAT screen

		driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"
				+ recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 16-Clicking on submit for approval quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.finishButton);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));

		objCIOTransferPage.logout();

		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"
				+ recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionApprove);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionApprove);
		objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.finishButton);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		
		//Fetching appraiser WI genrated on approval of CIO WI
		
		String workItemNoForAppraiser = salesforceAPI.select(
				"Select Id ,Name from Work_Item__c where type__c='Appraiser' and sub_type__c='Appraisal Activity' order by createdDate desc")
				.get("Name").get(0);

		objCIOTransferPage.logout();

		objCIOTransferPage.login(users.APPRAISAL_SUPPORT);
		objMappingPage.globalSearchRecords(workItemNoForAppraiser);
		Thread.sleep(5000);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel,10);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		
		//Verifying WI details of new appraisal activity
		
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Work Pool"), "Normal Enrollment", "SMAB-T3786:Verify that Workpool of Appraisal WI is NORMAL-ENROLLMENT");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Type"), "Appraiser", "SMAB-T3786:Verify that Type of Appraisal WI is Appraisal");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Action"), "Appraisal Activity", "SMAB-T3786:Verify that Action of Appraisal WI is Appraisal Activity");
		
		objCIOTransferPage.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		

		// STEP 7-Clicking on related action link
		
        objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.reviewLink,10);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindowForAppraisal = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindowForAppraisal);
		
		objAppraisalActivity.waitForElementToBeClickable(10, objAppraisalActivity.returnButton);	    
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.appraisalActivityStatus),"In Progress" ,"SMAB-T3786: Verify that by default status of appraiser activity is In Progress");
	 	
		objAppraisalActivity.logout();

	}
	

}
