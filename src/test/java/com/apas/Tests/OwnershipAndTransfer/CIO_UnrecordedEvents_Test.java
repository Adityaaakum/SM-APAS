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
	AuditTrailPage trail;
	String unrecordedEventData;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		trail= new AuditTrailPage(driver);
		unrecordedEventData = System.getProperty("user.dir") + testdata.UNRECORDED_EVENT_DATA;
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
		Thread.sleep(2000);
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
		
		String dataToCreateCorrespondenceEventForAutoConfirm = System.getProperty("user.dir")
				+ testdata.UNRECORDED_EVENT_DATA;
		Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
				dataToCreateCorrespondenceEventForAutoConfirm, "DataToCreateCorrespondenceEventForAutoConfirm");
		
		
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
		System.out.println(recordeAPNTransferID);
		
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
		objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);		
		String urlForTransactionTrail = driver.getCurrentUrl();
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

		// Step 7 : Submitting for review

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 8-Clicking on submit for approval quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForReview);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForReview);
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.cioTransferSuccessMsg);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferSuccessMsg),
				"CIO transfer initial determination is submitted for review.",
				"SMAB-T3377,SMAB-T10081:Cio trasnfer is submited for review");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		objCIOTransferPage.logout();
		
		//Step-9: Login with CIO supervisor

		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

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
			
			String dataToCreateCorrespondenceEventForAutoConfirm = System.getProperty("user.dir")
					+ testdata.UNRECORDED_EVENT_DATA;
			Map<String, String> hashMapCorrespondenceEventForAutoConfirm = objUtil.generateMapFromJsonFile(
					dataToCreateCorrespondenceEventForAutoConfirm, "DataToCreateCorrespondenceEventForAutoConfirm");
			
			
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
						
						//Step 11: login with cio staff to validate that auto confirm has taken place for impending transfer
						
						objCIOTransferPage.login(users.CIO_STAFF);
						driver.navigate().to("https://smcacre--" + execEnv
								+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
						
						//STEP 12 : Verifying transfer code has changed after approval and equals to autoconfirm counterpart of the initial code
						
						softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel),
								finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
						objCIOTransferPage.waitForElementToBeClickable(5, objCIOTransferPage.quickActionButtonDropdownIcon);
						objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
						objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
						objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.firstRelatedBuisnessEvent);
						String parentAuditTrailNumber = objWorkItemHomePage
								.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
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
						softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondence), parentAuditTrailNumber,
								"SMAB-T3377,SMAB-T10081: Verifying that outbound AT is child of parent Recorded correspondence event");

						objCIOTransferPage.logout();
}
}