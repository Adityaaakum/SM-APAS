package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.generic.NEW;
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
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.ExemptionsPage;
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

public class CIO_UnrecordedEvents_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();
	CIOTransferPage objCIOTransferPage;
	ExemptionsPage objExemptionsPage;
	AuditTrailPage trail;
	String unrecordedEventData;
	String ownershipCreationData;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		trail= new AuditTrailPage(driver);
		unrecordedEventData = testdata.UNRECORDED_EVENT_DATA;
		ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
	}
	
	/*
	 * Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel With 99 PUC
	 */
	
	@Test(description = "SMAB-T3287:Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel with 99 PUC", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_WarningMessageForRetiredParcelWith99PUC(String loginUser) throws Exception {
		
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Retired' limit 1";
		String retiredApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String retiredApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		String activeApn = objCIOTransferPage.fetchActiveAPN();
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id FROM PUC_Code__c where Name = '99-RETIRED PARCEL' limit 1");
		salesforceAPI.update("Parcel__c", retiredApnId, "PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(retiredApn);
		
		// Step3: Create UT event and validate warning message on CIO Transfer screen
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.transferPageMessageArea),"Please select an active APN before performing any action related to CIO Transfer",
				"SMAB-T3287: Validate the warning message on CIO Transfer screen");
		
		// Step4: Edit the Transfer activity and update the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
		
		// Step5: Update APN field with active APN and validate Warning message disappears
		ReportLogger.INFO("Update the APN value to an Active Parcel value");
		objParcelsPage.Click(objCIOTransferPage.editFieldButton("APN"));
		objCIOTransferPage.clearSelectionFromLookup("APN");
		
		objCIOTransferPage.searchAndSelectOptionFromDropDown("APN", activeApn);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText("Save"));
		Thread.sleep(5000);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementExists(objCIOTransferPage.warningMessageArea),
				"SMAB-T3287: Validate that no warning message is displayed on CIO Transfer screen");
	
		objCIOTransferPage.logout();
	}
	
	/*
	 * Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel Without 99 PUC
	 */
	
	@Test(description = "SMAB-T3287:Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel without 99 PUC", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_WarningMessageForRetiredParcelWithout99PUC(String loginUser) throws Exception {
		
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Retired' limit 1";
		String retiredApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String retiredApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id FROM PUC_Code__c where Name in ('101- Single Family Home','105 - Apartment') limit 1");
		salesforceAPI.update("Parcel__c", retiredApnId, "PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(retiredApn);
		
		// Step3: Create UT event and validate warning message on CIO Transfer screen
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementExists(objCIOTransferPage.warningMessageArea),
				"SMAB-T3287: Validate that no warning message is displayed on CIO Transfer screen");
		
		// Step4: Edit the Transfer activity and update the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
		
		objCIOTransferPage.logout();
	}
	

	/*
	 *This test  method verifies that user is able to manually initiate the auto approve process , if response has come back within 45 days of wait period.
	 * 
	 */
	
	@Test(description = "SMAB-T3287:Verify that User is able to perform CIO transfer autoconfirm when some response do come back with in 45 days wait period", dataProvider = "dpForCioAutoConfirm" ,dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_VerifyCioTransferAutoConfirm(String InitialEventCode, String finalEventCode,
			String response) throws Exception {
		
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
		String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		
		
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		String execEnv = System.getProperty("region");

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");
		
		
		Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
				unrecordedEventData, "DataToCreateCorrespondenceEventForAutoConfirm");
		
		
		// Step1: Login to the APAS application
		objMappingPage.login(users.SYSTEM_ADMIN);

		// Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		
		objCIOTransferPage.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + activeApn + "'").get("Id").get(0));


		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + activeApn + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		// STEP 3- updating the ownership date for current owners

		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		jsonObject.put("DOR__c", dateOfEvent);
		jsonObject.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

		objMappingPage.logout();
		
		// Step 4: Create UT event TO Navigate to CIO transfer screen
		
		objCIOTransferPage.login(users.CIO_STAFF);
		objMappingPage.globalSearchRecords(activeApn);
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		String recordeAPNTransferID =driver.getCurrentUrl().split("/")[6];
		
		//Step 4(a):creating new grantee records
		
		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap = objCIOTransferPage.getGridDataForRowString("1");
		String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		objCIOTransferPage.waitForElementToBeVisible(5, objCIOTransferPage.nextButton);
		objCIOTransferPage.enter(objCIOTransferPage.calculateOwnershipRetainedFeld, "50");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));

		//  STEP 5-create new mail to record
		
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCIOTransferPage.waitForElementToBeClickable(7, objCIOTransferPage.copyToMailToButtonLabel);

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		ReportLogger.INFO("Adding  the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, InitialEventCode);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		
		//Creating a new outbound event
		
		objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);		
		String urlForTransactionTrail = driver.getCurrentUrl();
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

		// Step 7 : Submitting for review

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 8-Clicking on submit for review quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForReview);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForReview);
		ReportLogger.INFO("CIO!! Transfer submitted for review");
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.cioTransferSuccessMsg);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferSuccessMsg),
				"CIO transfer initial determination is submitted for review.",
				"SMAB-T3377,SMAB-T10081:Cio transfer is submited for review");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		objCIOTransferPage.logout();
		
		//Step-9: Login with CIO supervisor

		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		
		//Clicking on review completed quick action button
		
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionReviewComplete);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionReviewComplete);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferSuccessMsg),
				"CIO transfer initial determination review completed.", "SMAB-T3377,SMAB-T10081:Cio transfer review is completed");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		objCIOTransferPage.logout();
		
		//Step 10: If Response comes back within 45 days and no issues are reported.

		if (response.equalsIgnoreCase("No Edits required")) {

			objCIOTransferPage.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferStatusLabel);
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferStatusLabel);
			objCIOTransferPage.Click(objCIOTransferPage.getWebElementWithLabel(objCIOTransferPage.transferStatusLabel));
			
			// Clicking on review acesse picklist to manually approve the transfer
			
			objCIOTransferPage.javascriptClick(objCIOTransferPage.reviewAssecesseLink);
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
			
			//Verifying the status of transfer
			
			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel),
					"Approved", "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel),
					finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			objCIOTransferPage.waitForElementToBeClickable(5, objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
			
			//Navigating to WI from back button
			
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
			objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			
			//Navigating to the outbound event
			
			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");
			
			objCIOTransferPage.logout();
		}
		//Step 11:If response comes back and transfer code is required to be changed as a part of response

		if (response.equalsIgnoreCase("Event Code needs to be changed")) {

			objCIOTransferPage.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			ReportLogger.INFO("Changing  the Transfer Code Based on acessor response");
			objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
			objCIOTransferPage.Click(objCIOTransferPage.clearSelectionEventCode);
			objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel,
					CIOTransferPage.CIO_EVENT_CODE_CIOGOVT);
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));

			ReportLogger
					.INFO("After Changing  the Transfer Code Based on acessor response we will submit it for approval");
			
			// Step 12 : Submitting for approval

			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

			// STEP 16-Clicking on submit for approval quick action button

			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForApproval);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
			ReportLogger.INFO("CIO!! Transfer submitted for approval");
			objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.cioTransferSuccessMsg);
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferSuccessMsg),
					"Work Item has been submitted for Approval.", "SMAB-T3377,SMAB-T10081:Cio transfer is submited for approval");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
			objCIOTransferPage.logout();

			// login with cio supervisor
			
			objCIOTransferPage.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
			
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionApprove);
			ReportLogger.INFO("CIO!! Transfer is approved");
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferSuccessMsg),
					"Work Item has been approved successfully.", "SMABT123:Cio transfer is approved successfully");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));

			// Navigating to transfer screen to avoid stale element exception
			
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeClickable(5, objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.firstRelatedBuisnessEvent);
			
			//Clicking on AT=BE From WI linked Items
			
			objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			
			//Navigating to outbound event
			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");			
			objCIOTransferPage.logout();
	}
	}
		
		/*
		 * This test method is used to assert that CIO auto confirm using batch job is able to autoconfirm transfer after no response came within 45 days of wait period
		 */
		
		@Test(description = "SMAB-T3377,SMAB-T10081:Verify that User is able to perform CIO transfer autoconfirm using a batch job (Fully automated)", dataProvider = "dpForCioAutoConfirmUsingBatchJob" ,dataProviderClass = DataProviders.class, groups = {
				"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
		public void UnrecordedEvent_VerifyCioTransferAutoConfirmUsingBatchJob(String InitialEventCode, String finalEventCode) throws Exception {
			
			String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
			String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
			
			
			Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
			
			String execEnv = System.getProperty("region");

			String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
			Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
					OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

			String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
			Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
					OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

			Map<String, String> hashMapCreateOwnershipRecordData = objUtil
					.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");
			
			
			Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
					unrecordedEventData, "DataToCreateCorrespondenceEventForAutoConfirm");
			
			
			// Step1: Login to the APAS application
			objMappingPage.login(users.SYSTEM_ADMIN);

			// Step2: Opening the PARCELS page 
			objMappingPage.searchModule(PARCELS);
			objMappingPage.globalSearchRecords(activeApn);
			
			objCIOTransferPage.deleteOwnershipFromParcel(
					salesforceAPI.select("Select Id from parcel__c where name='" + activeApn + "'").get("Id").get(0));

			String acesseName = objMappingPage.getOwnerForMappingAction();
			driver.navigate()
					.to("https://smcacre--"
							+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
									.select("Select Id from parcel__C where name='" + activeApn + "'").get("Id").get(0)
							+ "/related/Property_Ownerships__r/view");
			objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
			String ownershipId = driver.getCurrentUrl().split("/")[6];

			// STEP 3- updating the ownership date for current owners

			String dateOfEvent = salesforceAPI
					.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
					.get("Ownership_Start_Date__c").get(0);
			jsonObject.put("DOR__c", dateOfEvent);
			jsonObject.put("DOV_Date__c", dateOfEvent);
			salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

			objMappingPage.logout();
			
			// Step4: Create UT event and validate warning message on CIO Transfer screen
			
			objCIOTransferPage.login(users.CIO_STAFF);
			objMappingPage.globalSearchRecords(activeApn);
			objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
			String recordeAPNTransferID =driver.getCurrentUrl().split("/")[6];
			
			//Step 5: Creating a grantee record
			
			objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
			driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
					+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
			HashMap<String, ArrayList<String>> granteeHashMap = objCIOTransferPage.getGridDataForRowString("1");
			String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);
			
			//Updating auto confirm date for auto approval
			salesforceAPI.update("Recorded_APN_Transfer__c",recordeAPNTransferID, "Auto_Confirm_Start_Date__c","2021-04-07" );
			ReportLogger.INFO("Putting Auto confirm date prior to 45 days");

			//Navigating to RAT screen
			
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
			objCIOTransferPage.waitForElementToBeVisible(5, objCIOTransferPage.nextButton);
			objCIOTransferPage.enter(objCIOTransferPage.calculateOwnershipRetainedFeld, "50");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));

			//  STEP 5-create new mail to record
			
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
			objCIOTransferPage.waitForElementToBeClickable(7, objCIOTransferPage.copyToMailToButtonLabel);

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			ReportLogger.INFO("Add the Transfer Code");
			objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
			objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, InitialEventCode);
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
			objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);			
			String urlForTransactionTrail = driver.getCurrentUrl();
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			// Step 6 : Submitting for review

			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

			// STEP 7-Clicking on submit for approval quick action button

			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForReview);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForReview);
			ReportLogger.INFO("CIO!! Transfer submitted for review");
			objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.cioTransferSuccessMsg);
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferSuccessMsg),
					"CIO transfer initial determination is submitted for review.",
					"SMAB-T3377,SMAB-T10081:Cio transfer is submited for review");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
			objCIOTransferPage.logout();
			
			//Step-8: Login with CIO supervisor

			objCIOTransferPage.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

			//Step 9:Clicking on review complete
			
			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionReviewComplete);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionReviewComplete);
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferSuccessMsg),
					"CIO transfer initial determination review completed.", "SMAB-T3377,SMAB-T10081:Cio trasnfer review is completed");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
			objCIOTransferPage.logout();
			
			// Step 10:Login with sysadmin to start autoconfirm batch job
			
			objMappingPage.login(users.SYSTEM_ADMIN);
			salesforceAPI.generateReminderWorkItems(salesforceAPI.CIO_AUTOCONFIRM_BATCH_JOB);
			objCIOTransferPage.logout();

			// Step 11: login with cio staff to validate that auto confirm has taken place
			// for impending transfer

			objCIOTransferPage.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			// STEP 12 : Verifying transfer code has changed after approval and equals to
			// autoconfirm counterpart of the initial code

			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel),
					finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			objCIOTransferPage.waitForElementToBeClickable(5, objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.firstRelatedBuisnessEvent);
			
			//Clicking on AT=BE
			objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);

			// STEP 13:Verifying that AT=BE is completed

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");

			// STEP 14:Verifying that outbound event is completed

			driver.navigate().to(urlForTransactionTrail);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");

			objCIOTransferPage.logout();
		}

	
	/*
	 * Verify details on the Unrecorded Transfer event
	 */
	
	@Test(description = "SMAB-T3231:Verify details on the Unrecorded Transfer event", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_TransferScreenConfiguration(String loginUser) throws Exception {
		
		//Getting Owner or Account records
		String assesseeName = objMappingPage.getOwnerForMappingAction();
		
		//Getting Active APN
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
		String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String activeApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		//Setup the data for the validations
		String legalDescriptionValue="Legal PM 85/25-260";
		String execEnv= System.getProperty("region");	
		
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData, "DataToCreateOwnershipRecord");
		
		String OwnershipAndTransferCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
	    Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData,
				"dataToCreateMailToRecordsWithIncompleteData");
	    
	    String OwnershipAndTransferGranteeCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData,
					"dataToCreateGranteeWithIncompleteData");
		   
		//Get values from Database and enter values in the Parcels
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responseSitusDetails= salesforceAPI.select("SELECT Id, Name FROM Situs__c where Name != NULL LIMIT 1");
		String primarySitusId=responseSitusDetails.get("Id").get(0);
		String primarySitusValue=responseSitusDetails.get("Name").get(0);
		
		jsonObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("Primary_Situs__c",primarySitusId);
		salesforceAPI.update("Parcel__c", activeApnId, jsonObject);
		
		//Delete Ownership records on the Parcel
		objMappingPage.deleteOwnershipFromParcel(activeApnId);
		
		// Add ownership records in the parcels
        objMappingPage.login(users.SYSTEM_ADMIN);
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(activeApn);
        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
        objMappingPage.scrollToBottom();
        objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
        
        objWorkItemHomePage.logout();
        Thread.sleep(5000);
		
		//Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		//Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		
		//Step3: Create UT event and get the Transfer ID
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];
		String unrecordedEventId = objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel);
		
		//Step4 : Validate the values on Transfer Screen
		ReportLogger.INFO("Validate the UT values");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel, "").substring(0, 2),"UT",
				"SMAB-T3231: Validate that CIO staff is able to verify the prefix of Event ID");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel, "").length(),"10",
				"SMAB-T3231: Validate that CIO staff is able to verify the length of Event ID");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.situsLabel, ""),primarySitusValue,
				"SMAB-T3231: Validate that CIO staff is able to verify the Situs value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.shortLegalDescriptionLabel, ""),legalDescriptionValue,
				"SMAB-T3231: Validate that CIO staff is able to verify the Short Legal Description value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.pucCodeLabel, ""),responsePUCDetails.get("Name").get(0),
				"SMAB-T3231: Validate that CIO staff is able to verify the PUC value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.doeLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3231: Validate that CIO staff is able to verify the DOE on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3231: Validate that CIO staff is able to verify the DOV on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dorLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3231: Validate that CIO staff is able to verify the DOR on UT");
		
		//Step5: Edit the Transfer activity and update the DOE
		ReportLogger.INFO("Update the DOE");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.doeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.doeLabel);
		objCIOTransferPage.enter(objCIOTransferPage.doeLabel, "07/05/2021");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.doeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.doeLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Updated DOE")),
				"SMAB-T3231: Validate that CIO staff is able to update and save DOE on CIO Transfer Screen");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3231: Validate that DOV on CIO Transfer Screen still remains the same");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dorLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3231: Validate that DOR on CIO Transfer Screen still remains the same");
		
		//Step6: Navigating to mail to screen and Create mail to record 
		ReportLogger.INFO("Navigate to Mail-To screen and create a Mail To record");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
	    objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.newButton));
	    objCIOTransferPage.enter(objCIOTransferPage.formattedName1Label, hashMapOwnershipAndTransferCreationData.get("Formatted Name1"));
	    objCIOTransferPage.enter(objCIOTransferPage.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
	    objCIOTransferPage.enter(objCIOTransferPage.endDate,  hashMapOwnershipAndTransferCreationData.get("End Date"));
		objCIOTransferPage.enter(objCIOTransferPage.mailingZip,hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(3,objCIOTransferPage.formattedName1Label );		  
		softAssert.assertContains( objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.formattedName1Label),hashMapOwnershipAndTransferCreationData.get("Formatted Name1"),
				"SMAB-T3231: Verify user is  able to save mail to record");
		
		//Step7: Navigate to RAT screen and validate number of Grantors/Grantee on the UT activity 
		ReportLogger.INFO("Navigate back to RAT and validate number of Grantors/Grantee on the UT activity");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGrantorLabel),"0",
				"SMAB-T3231: Verify user is  able to validate number of Grantors on the UT activity");
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGranteeLabel),"0",
				"SMAB-T3231: Verify user is  able to validate number of Grantee on the UT activity");
		
		//Step8 :Create the new Grantee
		ReportLogger.INFO("Create New Grantee record");
		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			
		
		//Step9: Navigate to RAT screen and click View ALL to see all Grantee records in grid
		ReportLogger.INFO("Navigate to RAT screen and click View ALL to see all Grantee records in grid");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
		objCIOTransferPage.clickViewAll("CIO Transfer Grantee & New Ownership");
		
        // Step10: Validate the details in the grid
        ReportLogger.INFO("Validate the Grantee record in Grid");
        HashMap<String, ArrayList<String>>HashMapLatestGrantee  = objCIOTransferPage.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestGrantee.get("Recorded Document").get(0), unrecordedEventId, 
    		  "SMAB-T3231: Validate the Recorded Document number on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Status").get(0), "Active", 
    		  "SMAB-T3231: Validate the status on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Owner Percentage").get(0), hashMapOwnershipAndTransferGranteeCreationData.get("Owner Percentage")+".0000%", 
    		  "SMAB-T3231: Validate the percentage on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Grantee/Retain Owner Name").get(0),hashMapOwnershipAndTransferGranteeCreationData.get("Last Name") , 
        		  "SMAB-T3231: Validate the Grantee Name on Grantee record");
        
        //Step11: Navigate to RAT screen and click View ALL to see current Ownership records in grid
        ReportLogger.INFO("Navigate to RAT screen and click View ALL to see current Ownership records in grid");
        driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
        objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
        objCIOTransferPage.clickViewAll("Ownership for Parent Parcelp");
	
        // Step12: Validate the details in the grid
        ReportLogger.INFO("Validate the Current Ownership record in Grid");
        HashMap<String, ArrayList<String>>HashMapLatestOwner  = objCIOTransferPage.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), assesseeName, 
    		  "SMAB-T3231: Validate the owner name on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active", 
    		  "SMAB-T3231: Validate the status on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "100.0000%", 
    		  "SMAB-T3231: Validate the percentage on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),hashMapCreateOwnershipRecordData.get("Ownership Start Date") , 
    		  "SMAB-T3231: Validate the start date on Grantee record");
      
       
		objCIOTransferPage.logout();
	}
	/*
     * Verify that User is able to perform CIO transfer  for Unrecorded events APN and validate all status
	 */

	@Test(description = "SMAB-T3525:Verify that User is able to perform CIO transfer  for Unrecorded events APN and validate all status", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_VerifyStatusForUTEvents(String loginUser) throws Exception {
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
		objCIOTransferPage.deleteOldGranteesRecords(recordedDocumentID);

		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
		String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String activeApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);

		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule("APAS");
		objMappingPage.searchModule(PARCELS);
		//  STEP 2- deleting ownership on parcel

		objCIOTransferPage.deleteOwnershipFromParcel(activeApnId);

		//STEP 3- adding owner after deleting for the recorded APN 

		String acesseName= objMappingPage.getOwnerForMappingAction();	        
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Parcel__c/"+activeApnId+"/related/Property_Ownerships__r/view");
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];
		Thread.sleep(10000);
		
		//STEP 4- updating the ownership date for current owners

		String dateOfEvent= salesforceAPI.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '"+ownershipId+"'").get("Ownership_Start_Date__c").get(0);      
		jsonObject.put("DOR__c",dateOfEvent);
		jsonObject.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

		objMappingPage.logout();

		// Step 5: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step 6: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);

		// Step 7: Create UT event and validate warning message on CIO Transfer screen
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		
		// Step 8: Edit the Transfer activity and update the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		String transferScreenURL=driver.getCurrentUrl();
		String recordeAPNTransferID=transferScreenURL.split("/")[6];
		
		// STEP 9: Creating the new grantee on transfer

		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			

		// STEP 10: Validating present grantee			 

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+"/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		HashMap<String, ArrayList<String>> granteeHashMap  = objCIOTransferPage.getGridDataForRowString("1");
		String granteeForMailTo= granteeHashMap.get("Grantee/Retain Owner Name").get(0);
		String ownershipDovForNewGrantee=granteeHashMap.get("DOV").get(0);

		// STEP 11: Creating copy to mail to record

		objCIOTransferPage.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCIOTransferPage.waitForElementToBeClickable(7, objCIOTransferPage.copyToMailToButtonLabel);

		//STEP 12: Validating mail to record created from copy to mail to

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
		objCIOTransferPage.waitForElementToBeClickable(5, objCIOTransferPage.newButton);

		// STEP 13: Navigating back to RAT screen

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 14: Clicking on submit for approval quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Submitted for Approval", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after submit for approval.");

		//STEP 15- Get audit trail Value from transfer screen and validate the status
		String auditTrailName =objWorkItemHomePage.getElementText(objCIOTransferPage.CIOAuditTrail);
		String auditTrailID=salesforceAPI.select("SELECT Id,Status__c,Name FROM Transaction_Trail__c where Name='"+auditTrailName+"'").get("Id").get(0);
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		objCIOTransferPage.waitUntilPageisReady(driver);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", "SMAB-T3525: Validating that audit trail status should be open after submit for approval.");


		//STEP 16-Navigating back to RAT screen and clicking on back quick action button

		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeClickable(5,objCIOTransferPage.quickActionButtonDropdownIcon);	          
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 17-Validating that back button has navigates the user to WI page and status of WI should be submitted for approval.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval", "SMAB-T3525: Validating that status of WI should be submitted for approval.");
		objCIOTransferPage.logout();
		Thread.sleep(5000);

		// STEP 18- login with  CIO supervisor
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);          
		
		// STEP 19- Clicking on return quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionReturn);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionReturn);
		objCIOTransferPage.waitForElementToBeVisible(5,objCIOTransferPage.returnReasonTextBox);
		objCIOTransferPage.enter(objCIOTransferPage.returnReasonTextBox, "return by CIO supervisor");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);

		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Returned", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after returned by supervisor.");

		objCIOTransferPage.waitForElementToBeClickable(5,objCIOTransferPage.quickActionButtonDropdownIcon);	          
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 20-Validating WI and AUDIT Trail status after returned by supervisor.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Returned", "SMAB-T3525: Validating that Back button navigates back to WI page ");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", "SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		objCIOTransferPage.logout();
		Thread.sleep(5000);

		objMappingPage.login(loginUser);
		driver.navigate().to(transferScreenURL);
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 21-Clicking on submit for approval quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);

		ReportLogger.INFO("CIO!! Transfer resubmit for approval by staff");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Submitted for Approval", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after resubmit for approval by staff.");

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 22-Validating that back button has navigates the user to WI page.
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval", "SMAB-T3525: Validating WI after resubmit for approval ");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", "SMAB-T3525: Validating that audit trail status should be open after resubmit for approval.");


		objCIOTransferPage.logout();
		Thread.sleep(5000);

		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 23-Clicking on approval quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionApprove);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionApprove);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);

		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Approved", "SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after approved by supervisor.");

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//STEP 24-Validating that WI and audit trail status after approving the transfer activity.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", "SMAB-T3525: Validating that WI status should be completed after approval by supervisor.");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", "SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		objCIOTransferPage.logout();	

	}	

}