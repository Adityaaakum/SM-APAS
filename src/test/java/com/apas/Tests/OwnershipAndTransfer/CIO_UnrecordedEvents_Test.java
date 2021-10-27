package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

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
	

	@Test(description = "SMAB-T3287, SMAB-T3286:Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel with 99 PUC", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
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
				"SMAB-T3287, SMAB-T3286: Validate the warning message on CIO Transfer screen");
		
		//Step 3(a): Verifying that orignal transfer list quick action button is not visible for unrecorded document
		softAssert.assertEquals(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.checkOriginalTransferListButtonLabel),false, "SMAB-T3630:Verify that check Orignal Transfer List quick action button is not available for unrecorded transfer.");
		
		// Step4: Edit the Transfer activity and update the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		
		//Added below code to handle regression failure
		Thread.sleep(2000);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
		
		// Step5: Update APN field with active APN and validate Warning message disappears
		ReportLogger.INFO("Update the APN value to an Active Parcel value");
		objParcelsPage.Click(objCIOTransferPage.editFieldButton("APN"));
		objCIOTransferPage.clearSelectionFromLookup("APN");
		
		objCIOTransferPage.searchAndSelectOptionFromDropDown("APN", activeApn);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText("Save"));
		Thread.sleep(5000); //Added to handle regression failure
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
		
		//Added below code to handle regression failure
		Thread.sleep(2000);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
						"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
					
		objCIOTransferPage.logout();
	}
	

	/*
	 *This test  method verifies that user is able to manually initiate the auto approve process , if response has come back within 45 days of wait period.
	 * 
	 */
	
	@Test(description = "SMAB-T3457,SMAB-T3287:Verify that User is able to perform CIO transfer autoconfirm when some response do come back with in 45 days wait period", dataProvider = "dpForCioAutoConfirm" ,dataProviderClass = DataProviders.class, groups = {
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
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.calculateOwnershipButtonLabel);
		
		objCIOTransferPage.Click(objCIOTransferPage.eventIDOnTransferActivityLabel);
		String  parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		String parentAuditTrailNumber=objCIOTransferPage.getFieldValueFromAPAS(trail.nameField);
		
		String eventId=trail.getFieldValueFromAPAS(trail.EventId);
		String requestOrigin=trail.getFieldValueFromAPAS(trail.RequestOrigin);
		
		driver.switchTo().window(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(30,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.nextButton);
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
		objWorkItemHomePage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, InitialEventCode);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		
		//Creating a new outbound event
		
		objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);		
		String urlForTransactionTrail = driver.getCurrentUrl();
		
		softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedBuisnessEvent), parentAuditTrailNumber,
				"SMAB-T3457: Verifying that outbound AT is child of parent  business event");

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedCorrespondence), "",
				"SMAB-T3457:Verifying that related business event field in that outbound AT  is blank");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.EventId), eventId,
				"SMAB-T3457:Verifying that Event ID field in the correspondence event detail page should be inherited from parent business event");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.RequestOrigin), requestOrigin,
				"SMAB-T3457:Verifying that  child correspondence event created from CIO screen inherits the Request Origin from parent event");
		
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

		// Step 7 : Submitting for review

		objCIOTransferPage.waitForElementToBeClickable(10,objCIOTransferPage.quickActionButtonDropdownIcon);
		

		// STEP 8-Clicking on submit for review quick action button

		
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionSubmitForReview);
		ReportLogger.INFO("CIO!! Transfer submitted for review");
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 10));
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 10)),
				"CIO transfer initial determination is submitted for review.",
				"SMAB-T3377,SMAB-T10081:Cio transfer is submited for review");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		objCIOTransferPage.logout();
		
		//Step-9: Login with CIO supervisor

		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(10,objCIOTransferPage.quickActionButtonDropdownIcon);
		
		
		//Clicking on review completed quick action button
		
		
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionReviewComplete);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 5)),
				"CIO transfer initial determination review completed.", "SMAB-T3377,SMAB-T10081:Cio transfer review is completed");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		objCIOTransferPage.logout();
		
		//Step 10: If Response comes back within 45 days and no issues are reported.

		if (response.equalsIgnoreCase("No Edits required")) {

			objCIOTransferPage.login(users.CIO_STAFF);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objWorkItemHomePage.waitForElementToBeVisible(10, objCIOTransferPage.transferStatusLabel);
			objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferStatusLabel);
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferStatusLabel);
			objCIOTransferPage.Click(objCIOTransferPage.getWebElementWithLabel(objCIOTransferPage.transferStatusLabel));
			
			// Clicking on review acesse picklist to manually approve the transfer
			
			objCIOTransferPage.javascriptClick(objCIOTransferPage.reviewAssecesseLink);
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
			
			//Verifying the status of transfer
			objWorkItemHomePage.waitForElementToBeVisible(10, objCIOTransferPage.transferStatusLabel);
			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel),
					"Approved", "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel),
					finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.quickActionButtonDropdownIcon);
			
			
			//Navigating to WI from back button
			
			objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(10, objWorkItemHomePage.secondRelatedBuisnessEvent);
			objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objWorkItemHomePage.waitForElementToBeVisible(10, trail.Status);

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(10, objWorkItemHomePage.wiStatus);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			
			//Navigating to the outbound event
			
			driver.navigate().to(urlForTransactionTrail);
			objWorkItemHomePage.waitForElementToBeVisible(10, trail.Status);
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
			objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 5));
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 5)),
					"Work Item has been submitted for Approval.", "SMAB-T3377,SMAB-T10081:Cio transfer is submited for approval");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
			objCIOTransferPage.logout();

			// login with cio supervisor
			
			objCIOTransferPage.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeClickable(10,objCIOTransferPage.quickActionButtonDropdownIcon);
			
			objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionApprove);
			ReportLogger.INFO("CIO!! Transfer is approved");
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 5)),
					"Work Item has been approved successfully.", "SMABT123:Cio transfer is approved successfully");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));

			// Navigating to transfer screen to avoid stale element exception
			
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
			objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.firstRelatedBuisnessEvent);
			
			//Clicking on AT=BE From WI linked Items
			
			objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objWorkItemHomePage.waitForElementToBeVisible(10, trail.Status);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.wiStatus);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");
			
			//Navigating to outbound event
			driver.navigate().to(urlForTransactionTrail);
			objWorkItemHomePage.waitForElementToBeVisible(10, trail.Status);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");			
			objCIOTransferPage.logout();
	}
	}
		
		/*
		 * This test method is used to assert that CIO auto confirm using batch job is able to autoconfirm transfer after no response came within 45 days of wait period
		 */
		
		@Test(description = "SMAB-T3377,SMAB-T10081,SMAB-T3690:Verify that User is able to perform CIO transfer autoconfirm using a batch job (Fully automated)", dataProvider = "dpForCioAutoConfirmUsingBatchJob" ,dataProviderClass = DataProviders.class, groups = {
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
			
			HashMap<String,ArrayList<String>> recordedDocumentMap= salesforceAPI.select("SELECT id,Name from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1");
					String recordedDocumentID = recordedDocumentMap.get("Id").get(0);
					String recordedDocumentName = recordedDocumentMap.get("Name").get(0);

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
			jsonObject.put("Recorded_Document__c", recordedDocumentID);
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
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.newButton);
			HashMap<String, ArrayList<String>> granteeHashMap = objCIOTransferPage.getGridDataInHashMap();
			String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);
		
			softAssert.assertEquals(granteeHashMap.get("Recorded Document").get(0),recordedDocumentName,
					"SMAB-T3690: Verifying that Recorded Document field of new ownership  is same as document name of recorded document for CIO tranfer");
			//Updating auto confirm date for auto approval
			salesforceAPI.update("Recorded_APN_Transfer__c",recordeAPNTransferID, "Auto_Confirm_Start_Date__c","2021-04-07" );
			ReportLogger.INFO("Putting Auto confirm date prior to 45 days");

			//Navigating to RAT screen
			
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.calculateOwnershipButtonLabel);
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.nextButton);
			objCIOTransferPage.enter(objCIOTransferPage.calculateOwnershipRetainedFeld, "50");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));

			//  STEP 5-create new mail to record
			
			
			objCIOTransferPage.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
			objCIOTransferPage.waitForElementToBeClickable(7, objCIOTransferPage.copyToMailToButtonLabel);

			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			ReportLogger.INFO("Add the Transfer Code");
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
			objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
			objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, InitialEventCode);
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
			objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);			
			String urlForTransactionTrail = driver.getCurrentUrl();
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			// Step 6 : Submitting for review

			objCIOTransferPage.waitForElementToBeClickable(10,objCIOTransferPage.quickActionButtonDropdownIcon);
			

			// STEP 7-Clicking on submit for approval quick action button

			
			objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionSubmitForReview);
			ReportLogger.INFO("CIO!! Transfer submitted for review");
			objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 10));
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 5)),
					"CIO transfer initial determination is submitted for review.",
					"SMAB-T3377,SMAB-T10081:Cio transfer is submited for review");
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
			objCIOTransferPage.logout();
			
			//Step-8: Login with CIO supervisor

			objCIOTransferPage.login(users.CIO_SUPERVISOR);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon,10);
			

			//Step 9:Clicking on review complete			
			
			objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionReviewComplete);
			softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 10)),
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
			Thread.sleep(5000);
			driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");

			// STEP 12 : Verifying transfer code has changed after approval and equals to
			// autoconfirm counterpart of the initial code
			objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel),
					finalEventCode, "SMAB-T3377,SMAB-T10081: Verfyfing the status of the CIO transfer");
			objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.quickActionButtonDropdownIcon);			
			objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionBack);
			objWorkItemHomePage.waitForElementToBeVisible(10, objWorkItemHomePage.firstRelatedBuisnessEvent);
			
			//Clicking on AT=BE
			objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
			objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);

			// STEP 13:Verifying that AT=BE is completed
			objCIOTransferPage.waitForElementToBeVisible(10, trail.Status);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Buisnessevent AuditTrail");
			driver.navigate().back();
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objCIOTransferPage.waitForElementToBeVisible(10, objWorkItemHomePage.wiStatus);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying status of WI is completed ");

			// STEP 14:Verifying that outbound event is completed

			driver.navigate().to(urlForTransactionTrail);
			objCIOTransferPage.waitForElementToBeVisible(10, trail.Status);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed",
					"SMAB-T3377,SMAB-T10081:Verifying Status of Outbound  AuditTrail");

			objCIOTransferPage.logout();
		}

	
	/*
	 * Verify details on the Unrecorded Transfer event
	 */
	
	@Test(description = "SMAB-T3139,SMAB-T3231:Verify details on the Unrecorded Transfer event", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
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
		JSONObject jsonForParcelUpdate = objMappingPage.getJsonObject();
		
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
		
		jsonForParcelUpdate.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonForParcelUpdate.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonForParcelUpdate.put("Primary_Situs__c",primarySitusId);
		salesforceAPI.update("Parcel__c", activeApnId, jsonForParcelUpdate);
		
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
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the prefix of Event ID");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel, "").length(),"10",
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the length of Event ID");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.situsLabel, ""),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the Situs value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.shortLegalDescriptionLabel, ""),legalDescriptionValue,
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the Short Legal Description value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.pucCodeLabel, ""),responsePUCDetails.get("Name").get(0),
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the PUC value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.doeLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the DOE on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the DOV on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dorLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3139,SMAB-T3231: Validate that CIO staff is able to verify the DOR on UT");
		
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
				"SMAB-T3139,SMAB-T3231: Verify user is  able to save mail to record");
		
		//Step7: Navigate to RAT screen and validate number of Grantors/Grantee on the UT activity 
		ReportLogger.INFO("Navigate back to RAT and validate number of Grantors/Grantee on the UT activity");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGrantorLabel),"0",
				"SMAB-T3139,SMAB-T3231: Verify user is  able to validate number of Grantors on the UT activity");
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGranteeLabel),"0",
				"SMAB-T3139,SMAB-T3231: Verify user is  able to validate number of Grantee on the UT activity");
		
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
        HashMap<String, ArrayList<String>> HashMapLatestGrantee  = objCIOTransferPage.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestGrantee.get("Status").get(0), "Active", 
      		  "SMAB-T3139,SMAB-T3231: Validate the status on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Owner Percentage").get(0), hashMapOwnershipAndTransferGranteeCreationData.get("Owner Percentage")+".0000%", 
    		  "SMAB-T3139,SMAB-T3231: Validate the percentage on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Grantee/Retain Owner Name").get(0),hashMapOwnershipAndTransferGranteeCreationData.get("Last Name") , 
        		  "SMAB-T3139,SMAB-T3231: Validate the Grantee Name on Grantee record");
        if (HashMapLatestGrantee.containsKey("Recorded Document")) {
        	softAssert.assertEquals(HashMapLatestGrantee.get("Recorded Document").get(0), unrecordedEventId,
       			 "SMAB-T3231: Validate the Recorded Document number on Grantee record");
        }
        if (HashMapLatestGrantee.containsKey("Recorded Document Number")) {
        	softAssert.assertEquals(HashMapLatestGrantee.get("Recorded Document Number").get(0), unrecordedEventId,
       			 "SMAB-T3231: Validate the Recorded Document number on Grantee record");
        }
        
        //Step11: Navigate to RAT screen and click View ALL to see current Ownership records in grid
        ReportLogger.INFO("Navigate to RAT screen and click View ALL to see current Ownership records in grid");
        driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
        objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
        objCIOTransferPage.clickViewAll("Ownership for Parent Parcel");
	
        // Step12: Validate the details in the grid
        ReportLogger.INFO("Validate the Current Ownership record in Grid");
        HashMap<String, ArrayList<String>>HashMapLatestOwner  = objCIOTransferPage.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), assesseeName, 
    		  "SMAB-T3139,SMAB-T3231: Validate the owner name on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active", 
    		  "SMAB-T3139,SMAB-T3231: Validate the status on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "100.0000%", 
    		  "SMAB-T3139,SMAB-T3231: Validate the percentage on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),hashMapCreateOwnershipRecordData.get("Ownership Start Date") , 
    		  "SMAB-T3139,SMAB-T3231: Validate the start date on Grantee record");
      
       
		objCIOTransferPage.logout();
	}
	/*
     * Verify that User is able to perform CIO transfer  for Unrecorded events APN and validate all status
	 */

	@Test(description = "SMAB-T3431,SMAB-T3139,SMAB-T3525, SMAB-T3929 : Verify that User is able to perform CIO transfer  for Unrecorded events APN and validate all status and values in Audit Trail record", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_VerifyStatusForUTEvents(String loginUser) throws Exception {
		
		String execEnv= System.getProperty("region");		
		String userNameForCioStaff = CONFIG.getProperty(users.CIO_STAFF + "UserName");
		String userNameForCioSupervisor = CONFIG.getProperty(users.CIO_SUPERVISOR + "UserName");
		
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
		
		//  STEP 2- deleting ownership on parcel
		objCIOTransferPage.deleteOwnershipFromParcel(activeApnId);

		//STEP 3- adding owner after deleting for the recorded APN 
		String acesseName= objMappingPage.getOwnerForMappingAction();
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];
		Thread.sleep(5000);
		
		//STEP 4- updating the ownership date for current owners
		String dateOfEvent= salesforceAPI.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '"+ownershipId+"'").get("Ownership_Start_Date__c").get(0);      
		jsonObject.put("DOR__c",dateOfEvent);
		jsonObject.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

		objMappingPage.logout();
		Thread.sleep(5000);
		
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
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.confirmationMessageOnTranferScreen);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.confirmationMessageOnTranferScreen),"Work Item has been submitted for Approval.","SMAB-T3139: Validation that transfer activity is performed successfully for Unrecorded events");
		
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		
		Thread.sleep(2000); //Allows the record to save properly
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Submitted for Approval", "SMAB-T3139,SMAB-T3525: Validating CIO Transfer activity status on transfer activity screen after submit for approval. for UT transfer");

		//STEP 15- Get audit trail Value from transfer screen and validate the status
		String auditTrailName =objWorkItemHomePage.getElementText(objCIOTransferPage.CIOAuditTrail);
		String auditTrailID=salesforceAPI.select("SELECT Id,Status__c,Name FROM Transaction_Trail__c where Name='"+auditTrailName+"'").get("Id").get(0);
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		Thread.sleep(2000); //Added to handle regression failure
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
		Thread.sleep(2000); //Added to handle regression failure
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
		
		// STEP 23-Clicking on approval quick action button
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity(objCIOTransferPage.approveButton);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);

		ReportLogger.INFO("CIO!! Transfer Approved");
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
		Thread.sleep(2000); //Added to handle regression failure
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", 
				"SMAB-T3525: Validating that audit trail status should be open after submit for approval.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Processed By", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioStaff + "'").get("Name").get(0),
				"SMAB-T3929: Validating the 'Processed By' field value in Audit Trail record");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Final Approver", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioSupervisor + "'").get("Name").get(0),
				"SMAB-T3929: Validating the 'Final Approver' field value in Audit Trail record.");
		
		
		// Verify that User is able to launch CIO Transfer Activity from business event triggered by Transfer Code update
		
		String auditTrailId =salesforceAPI.select("SELECT id FROM Transaction_Trail__c where Event_Library__c in (select id from Event_Library__c  where name='CIO-COPAL') order by createddate desc limit 1").get("Id").get(0);
				 
		driver.navigate().to("https://smcacre--" + execEnv
							+ ".lightning.force.com/lightning/r/Transaction_Trail__c/" + auditTrailId + "/view");
				
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.relatedActionLabel),"Click Here",
						"SMAB-T3431: Verify related action link name on Business event page generated by trabsfer code update is 'Click Here' ");
				
		objCIOTransferPage.Click(objWorkItemHomePage.reviewLink);

		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(30,
		objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
				
		String urlForRATScreen = driver.getCurrentUrl();
		String  RATId=urlForRATScreen.split("/")[6];

		softAssert.assertEquals(RATId,recordeAPNTransferID,
						"SMAB-T3431: Verify that User is able to launch CIO Transfer Activity from business event triggered by Transfer Code update");
				
		objCIOTransferPage.logout();	

	}	
	
	/*
	 * Ownership And Transfers - Verify user is able to create an Unrecorded Transfer Event and corresponding WI for Mobile Home Parcel from Component Action
	 */
	
	@Test(description = "SMAB-T3139,SMAB-T3127,SMAB-T3431:Verify user is able to create an Unrecorded Transfer Event and corresponding WI for Mobile Home Parcel from Component Action", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_MobileHomeParcel(String loginUser) throws Exception {
				
		//Getting Active APN
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' and name like '134%' and id in ( select parcel__c from mail_to__c where Status__c='Active')";
		String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String activeApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		String queryCountMailTORecords = "SELECT count(id) FROM Mail_To__c where parcel__c='"+activeApnId+"' and status__c='active'";
		String countActiveMaiToRecords=salesforceAPI.select(queryCountMailTORecords).get("expr0").get(0);;
		
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		//Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		//Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		
		//Step3: Create UT event perform validations
		ReportLogger.INFO("Create Unrecorded Event Transfer");
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String description = dataToCreateUnrecordedEventMap.get("Description") + "_" + timeStamp;
		
		objMappingPage.waitForElementToBeClickable(objMappingPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.selectOptionDropdown);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.selectOptionDropdown, "Create Audit Trail Record");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.workItemTypeDropDownComponentsActionsModal);
		
		objParcelsPage.selectOptionFromDropDown("Record Type", dataToCreateUnrecordedEventMap.get("Record Type"));
		objParcelsPage.selectOptionFromDropDown("Group",dataToCreateUnrecordedEventMap.get("Group"));

		Thread.sleep(2000);
		objParcelsPage.selectOptionFromDropDown("Type of Audit Trail Record?", dataToCreateUnrecordedEventMap.get("Type of Audit Trail Record?"));
		objParcelsPage.Click(objParcelsPage.getWebElementWithLabel("Source"));
		Object[] sourceFieldOptions =objParcelsPage.getAllOptionFromDropDown("Source").toArray();
		String[] expectedSourceFieldOptions= {"--None--", "Unrecorded document", "Property owner death document", "Verification of document", "Indirect discovery", "HCD Report - MH packet"};
		softAssert.assertTrue(Arrays.equals(sourceFieldOptions, expectedSourceFieldOptions),"SMAB-T3127 : Validation of picklist values in  Source field while creating UT from Parcel Component action button");		
 
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save and Next"));
	    softAssert.assertEquals(objParcelsPage.getElementText(objParcelsPage.sourceFieldComponentActionErrorMessage),"Complete this field.","SMAB-T3127 : Validation that Source is a mandatory field while creating UT from Parcel Component action button");		
	    objParcelsPage.Click(objParcelsPage.getWebElementWithLabel("Source"));

        if(dataToCreateUnrecordedEventMap.get("Source")!=null) {objParcelsPage.selectOptionFromDropDown("Source", dataToCreateUnrecordedEventMap.get("Source"));}
		if(dataToCreateUnrecordedEventMap.get("Date of Event")!=null) {objParcelsPage.enter("Date of Event", dataToCreateUnrecordedEventMap.get("Date of Event"));}
		objParcelsPage.enter("Date of Recording", dataToCreateUnrecordedEventMap.get("Date of Recording"));
		objParcelsPage.enter("Description", description);
		
		String utEventNumber= objParcelsPage.getAttributeValue(objParcelsPage.getWebElementWithLabel(objParcelsPage.eventNumberComponentAction),"value");
		
		//Step4: Verify the UT event ID generated

		softAssert.assertEquals(utEventNumber.substring(0, 2),"UT",
				"SMAB-T3127: Validate that CIO staff is able to verify the prefix of Event ID is UT");
		softAssert.assertEquals(utEventNumber.length(),"10",
				"SMAB-T3127: Validate that CIO staff is able to verify the length of Unrecorded Event ID is 10");
		softAssert.assertTrue(!utEventNumber.substring(2, 8).contains("[a-zA-Z]+"),
				"SMAB-T3127: Validate that CIO staff is able to verify the  Unrecorded Event ID contains all digits after UT prefix");
		softAssert.assertTrue(!objParcelsPage.getWebElementWithLabel(objParcelsPage.eventNumberComponentAction).isEnabled(),
				"SMAB-T3127: Validate that CIO staff is able to verify the  Unrecorded Event ID field is disabled");
		
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save and Next"));
		Thread.sleep(5000);
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];
		
		//Step5 : Validate the values on Transfer Screen
		ReportLogger.INFO("Validate the UT values");
		objCIOTransferPage.waitForElementToBeVisible(35,objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3139: Validate that in case DOV value is not entered through component action, then DOV should be same as DOE");
		
		//Step6 : Validate the values on CIO UT WI

		String workPoolIdQuery="SELECT Id FROM Work_Pool__c where name='CIO' ";
		String workPoolId = salesforceAPI.select(workPoolIdQuery).get("Id").get(0);

		String workItemQuery = "SELECT Id,name,Assigned_To__c  FROM Work_Item__c where Type__c='CIO' And Sub_Type__c ='UT Activity' and Work_Pool__c ='"+workPoolId+"' and  status__c='In Progress' and APN__c ='"+activeApnId+"'order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		String assignedToUSerIdUTWorkItem = salesforceAPI.select(workItemQuery).get("Assigned_To__c").get(0);
		String expectedAssignedToUSernameUTWorkItem=CONFIG.getProperty(loginUser + "UserName");
		
		String expectedAssignedToUSerIdUTWorkItem = "SELECT Id FROM User where Username ='"+expectedAssignedToUSernameUTWorkItem+"'";

		softAssert.assertEquals(assignedToUSerIdUTWorkItem,salesforceAPI.select(expectedAssignedToUSerIdUTWorkItem).get("Id").get(0),
				"SMAB-T3127: Validate that UT WI created is assigned to the user who created the unrecorded event");
		
		//Step7 : Seraching the  UT WI on APAS

		objMappingPage.globalSearchRecords(workItemNo);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(objUtil.convertCurrentDateISTtoPST("Asia/Kolkata", "America/Los_Angeles","MM/dd/yyyy")),
				"SMAB-T3127: Validation that 'Date' fields in CIO UT WI is the date when the WI was created");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("DOV", "Information"),DateUtil.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3127: Validation that 'DOV' fields in CIO UT WI is DOV of audit trail");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information"),utEventNumber,
				"SMAB-T3127: Validation that 'Reference' fields in CIO UT WI is UT event number  ");

		//Step8 : Validate the Audit Trails created for CIO UT WI

		String auditTrailsQuery = "SELECT Business_EVENT__r.NAME,BUSINESS_EVENT__r.type__c,BUSINESS_EVENT__r.event_type__c,BUSINESS_EVENT__r.status__c,BUSINESS_EVENT__r.Request_Origin__c ,BUSINESS_EVENT__r.Date_of_Event__c ,BUSINESS_EVENT__r.Date_of_Value__c,BUSINESS_EVENT__r.Recording_Date__c  ,BUSINESS_EVENT__r.Event_Number__c  from work_item_linkage__c where work_item__r.name='"+workItemNo+"'";
		String responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		JSONObject responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		String auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Request_Origin__c").toString(),dataToCreateUnrecordedEventMap.get("Source"),
				"SMAB-T3127: Validation that Request Origin of  UT event should be the source field value selecting while creating UT event");
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Event_Number__c").toString(),utEventNumber,
				"SMAB-T3127: Validation that Event_Number__c field value of  UT event should be the UT event number generated at the time of creating UT event");
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Type__c").toString(),"Business Events",
				"SMAB-T3127: Validation that type of  UT event should be Business Events");
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Date_of_Value__c").toString(),DateUtil.getDateInRequiredFormat(dataToCreateUnrecordedEventMap.get("Date of Event"),"MM/dd/yyyy","yyyy-MM-dd"),
				"SMAB-T3127: Validation that Date_of_Value__c of  UT event should be DOV entered while creating UT event");
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Recording_Date__c").toString(),DateUtil.getDateInRequiredFormat(dataToCreateUnrecordedEventMap.get("Date of Recording"),"MM/dd/yyyy","yyyy-MM-dd"),
				"SMAB-T3127: Validation that Recording_Date__c of  UT event should be DOR entered while creating UT event");
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Date_of_Event__c").toString(),DateUtil.getDateInRequiredFormat(dataToCreateUnrecordedEventMap.get("Date of Event"),"MM/dd/yyyy","yyyy-MM-dd"),
				"SMAB-T3127: Validation that Date_of_Event__c of  UT event should be DOE entered while creating UT event");
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Status__c").toString(),"Open",
				"SMAB-T3127: Validation that Status__c of  UT event should be the Open");
		
		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Event_Type__c").toString(),"Unrecorded Event",
				"SMAB-T3127: Validation that Event_Type__c of  UT event should be the Unrecorded Event"); 
		
		String auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),activeApnId,
				"SMAB-T3127: Validation that  UT event created is linked to the parcel for which UT event was created from component action button");
		
		//Step9 : Validate that the related action link of CIO UT WI should direct to CIO transfer screen. 
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(35,objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		
		String recordeAPNTransferIDFromUTWI = driver.getCurrentUrl().split("/")[6];
		softAssert.assertEquals(recordeAPNTransferIDFromUTWI,recordeAPNTransferID,
				"SMAB-T3127: Validation that the related action link of CIO UT WI should direct to CIO transfer screen.");
		
		//Step10 : Validate of different fields in UT CIO transfer screen. 

		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.documentTypeLabel, ""),"",
				"SMAB-T3139: Validate that CIO staff is able to verify the document type on UT");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferTaxLabel, ""),"$0.00",
				"SMAB-T3139: Validate that CIO staff is able to verify the transfer tax on UT");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.valueFromDocTaxLabel, ""),"$0.00",
				"SMAB-T3139: Validate that CIO staff is able to verify the value from doc tax on UT");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.cityOfSmTaxLabel, ""),"$0.00",
				"SMAB-T3139: Validate that CIO staff is able to verify the city of SM tax on UT");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.valueFromDocTaxCityLabel, ""),"$0.00",
				"SMAB-T3139: Validate that CIO staff is able to verify the value from doc tax on UT");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.pcorLable, ""),"No",
				"SMAB-T3139: Validate that pcorLable is NO for UT transfer screen");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel, ""),utEventNumber,
				"SMAB-T3139: Validate that event ID in UT transfer is UT event number ");
		
		objCIOTransferPage.scrollToBottom();
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfMailToLabel);
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfMailToLabel),countActiveMaiToRecords,
				"SMAB-T3139: Verify that "
				+ "only the active mail to records for a parcel are shown on UT transfer screen ");
		
		//Step11 : Validate clicking of UT event number form UT CIO transfer scree

		objCIOTransferPage.Click(objCIOTransferPage.eventIDOnTransferActivityLabel);
		 parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(trail.nameField), auditTrailName,
				"SMAB-T3139: Verifying that click on UT business event  directs to the buisness event audit trail recordt");

		
		// STEP 12-Verify that User is able to launch CIO Transfer Activity from Original Unrecorded type BE  

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.relatedActionLabel),"Click Here",
					"SMAB-T3431: Verify related action link name on Unrecorded Business event page 'Click Here' ");
				
		objCIOTransferPage.Click(objWorkItemHomePage.reviewLink);

		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(30,
		objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
				
		String urlForRATScreen = driver.getCurrentUrl();
		String  RATId=urlForRATScreen.split("/")[6];

		softAssert.assertEquals(RATId,recordeAPNTransferID,
				"SMAB-T3431: Verify that User is able to launch CIO Transfer Activity from Original Unrecorded BE (associated with the Transfer Record)");
				
		objCIOTransferPage.logout();
	}

	/*
	 * Ownership And Transfers - Verify MH appraiser creates an CIO WI from component Action at parcel level of existing MH APN and it routes to CIO staff
	 */
	
	@Test(description = "SMAB-T3533:Verify MH appraiser creates an CIO WI from component Action at parcel level of existing MH APN and it routes to CIO staff", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_ExistingMHTransfer(String loginUser) throws Exception {
				
		//Getting Active APN
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' and name like '134%' and id in ( select parcel__c from mail_to__c where Status__c='Active')";
		String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String activeApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "ExistingMHTransferventCreation");
		
		//Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		//Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		
		//Step3: Create UT event perform validations
		ReportLogger.INFO("Create Existing MH Transfer");
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String description = dataToCreateUnrecordedEventMap.get("Description") + "_" + timeStamp;
		
		objMappingPage.waitForElementToBeClickable(objMappingPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.selectOptionDropdown);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.selectOptionDropdown, "Create Audit Trail Record");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.workItemTypeDropDownComponentsActionsModal);
		
		objParcelsPage.selectOptionFromDropDown("Record Type", dataToCreateUnrecordedEventMap.get("Record Type"));
		objParcelsPage.selectOptionFromDropDown("Group",dataToCreateUnrecordedEventMap.get("Group"));

		Thread.sleep(2000);
		objParcelsPage.selectOptionFromDropDown("Type of Audit Trail Record?", dataToCreateUnrecordedEventMap.get("Type of Audit Trail Record?"));
		
        if(dataToCreateUnrecordedEventMap.get("Source")!=null) {objParcelsPage.selectOptionFromDropDown("Source", dataToCreateUnrecordedEventMap.get("Source"));}
		if(dataToCreateUnrecordedEventMap.get("Date of Event")!=null) {objParcelsPage.enter("Date of Event", dataToCreateUnrecordedEventMap.get("Date of Event"));}
		objParcelsPage.enter("Date of Recording", dataToCreateUnrecordedEventMap.get("Date of Recording"));
		objParcelsPage.enter("Description", description);
		String utEventNumber= objParcelsPage.getAttributeValue(objParcelsPage.getWebElementWithLabel(objParcelsPage.eventNumberComponentAction),"value");

		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save and Next"));
		
		//Step4 : Validate user is directed to transfer screen
		ReportLogger.INFO("Validate that user is directed to transfer screen ");
		objCIOTransferPage.waitForElementToBeVisible(35,objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.cioTransferActivityLabel) ,"SMAB-T3533: verify that user  is directed to transfer screen after creating existing MH transfer event");
		
		//Step5 : Validate the values on CIO Existing MH Transfer  WI

		Thread.sleep(3000);
		String workPoolIdQuery="SELECT Id FROM Work_Pool__c where name='CIO' ";
		String workPoolId = salesforceAPI.select(workPoolIdQuery).get("Id").get(0);

		String workItemQuery = "SELECT Id,name,Assigned_To__c  FROM Work_Item__c where Type__c='CIO' And Sub_Type__c ='Existing MH Transfer' and Work_Pool__c ='"+workPoolId+"' and  status__c='In Progress' and APN__c ='"+activeApnId+"'order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		String assignedToUSerIdUTWorkItem = salesforceAPI.select(workItemQuery).get("Assigned_To__c").get(0);
		String expectedAssignedToUSernameUTWorkItem=CONFIG.getProperty(loginUser + "UserName");
		
		String expectedAssignedToUSerIdUTWorkItem = "SELECT Id FROM User where Username ='"+expectedAssignedToUSernameUTWorkItem+"'";

		softAssert.assertEquals(assignedToUSerIdUTWorkItem,salesforceAPI.select(expectedAssignedToUSerIdUTWorkItem).get("Id").get(0),
				"SMAB-T3533: Validate that Existing MH transfer  WI created is assigned to the user who created the existing MH transfer event");
		
		String auditTrailsQuery = "SELECT Business_EVENT__r.NAME,BUSINESS_EVENT__r.type__c,BUSINESS_EVENT__r.event_type__c,BUSINESS_EVENT__r.status__c,BUSINESS_EVENT__r.Request_Origin__c ,BUSINESS_EVENT__r.Date_of_Event__c ,BUSINESS_EVENT__r.Date_of_Value__c,BUSINESS_EVENT__r.Recording_Date__c  ,BUSINESS_EVENT__r.Event_Number__c  from work_item_linkage__c where work_item__r.name='"+workItemNo+"'";
		String responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		JSONObject responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		String auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		
		//Step6 :Navigating to WI from back button
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.firstRelatedBuisnessEvent);
		objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
		objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Open",
				"SMAB-T3533:Verifying Status of Buisnessevent AuditTrail is open ");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.recordTypeLabel), "Business Event",
				"SMAB-T3533:Verifying Record Type of  AuditTrail is Buisnessevent ");
		
		driver.navigate().back();
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(objUtil.convertCurrentDateISTtoPST("Asia/Kolkata", "America/Los_Angeles","MM/dd/yyyy")),
				"SMAB-T3533: Validation that 'Date' fields in Existing MH transfer WI is the date when the WI was created");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("DOV", "Information"),DateUtil.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3533: Validation that 'DOV' fields in Existing MH transfer WI is DOV of audit trail");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information"),utEventNumber,
				"SMAB-T3533: Validation that 'Reference' fields in Existing MH transfer WI is UT event number  ");

		//Step7 : Validate the Audit Trails created for CIO existing MH transfer WI

		softAssert.assertEquals(responseAuditTrailDetailsJson.get("Type__c").toString(),"Business Events",
				"SMAB-T3533: Validation that type of  UT event should be Business Events");
		
		String auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),activeApnId,
				"SMAB-T3533: Validation that  UT event created is linked to the parcel for which Existing MH transfer event was created from component action button");
		objCIOTransferPage.logout();
}
	
	/*
	 * Ownership And Transfers >> UnRecorded Events & LEOPS  >>  Verify AT Event and WI are transferred  to new APN when APN# is edited in RAT/CIO Transfer Screen
	 */
	@Test(description = "SMAB-T3457,SMAB-T3822 : Ownership And Transfers >> UnRecorded Events & LEOPS  >>  Verify AT Event and WI are transferred  to new APN when APN# is edited in RAT/CIO Transfer Screen", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement","UnrecordedEvent" }, enabled = true)
	public void CIO_TransferScreen_UnrecordedEvents_UpdateAPN(String loginUser) throws Exception {

		JSONObject jsonObjectAPN = objCIOTransferPage.getJsonObject();
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

		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
		String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String activeApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);

		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(EFILE_INTAKE_VIEW);
		
		// STEP 2-delete current owners

		objCIOTransferPage.deleteOwnershipFromParcel(
				salesforceAPI.select("Select Id from parcel__c where name='" + apnToUpdate + "'").get("Id").get(0));

		// STEP 3- adding owner after deleting for the new  APN

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
		JSONObject jsonObject = objCIOTransferPage.getJsonObject();
		jsonObject.put("DOR__c", dateOfEvent);
		jsonObject.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObject);

		HashMap<String, ArrayList<String>> responseAPNOwnershipDetails=salesforceAPI.select("SELECT Name FROM Property_Ownership__c where parcel__c='"+responseAPNDetails.get("Id").get(0)+"'");

		objMappingPage.logout();
		Thread.sleep(4000);

		// STEP 5-Login with CIO staff

		objMappingPage.login(loginUser);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);

		// Step 6: Create UT event
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		objCIOTransferPage.waitForElementToBeVisible(20,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		
		String workItemQuery = "SELECT Id,name  FROM Work_Item__c where Type__c='CIO' And Sub_Type__c ='UT Activity' order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);
		
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the  recorded apn transfer id
		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		JSONObject jsonForUpdateAPN = objCIOTransferPage.getJsonObject();

		jsonForUpdateAPN.put("xDOV__c", "2021-02-03");
		jsonForUpdateAPN.put("DOR__c", "2021-06-23");

		salesforceAPI.update("Recorded_APN_Transfer__c", recordeAPNTransferID, jsonForUpdateAPN);

		// deleting the CIO Transfer grantees for the transfer screen
		objCIOTransferPage.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
		
		// STEP 8-Creating the new grantee

		hashMapOwnershipAndTransferGranteeCreationData.put("Owner Percentage", "100");
		hashMapOwnershipAndTransferGranteeCreationData.put("First Name", "");
		hashMapOwnershipAndTransferGranteeCreationData.put("Ownership Start Date", "");

		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);

		// STEP 9-Updating The APN from transfer screen 
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeVisible(30,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		
		objCIOTransferPage.Click(objCIOTransferPage.eventIDOnTransferActivityLabel);
		String  parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		String parentAuditTrailNumber=objCIOTransferPage.getFieldValueFromAPAS(trail.nameField);
		
		String eventId=trail.getFieldValueFromAPAS(trail.EventId);
		String requestOrigin=trail.getFieldValueFromAPAS(trail.RequestOrigin);
		
		driver.switchTo().window(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(30,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		
		String dovCIOScreen=objMappingPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel);
		String dorCIOScreen=objMappingPage.getFieldValueFromAPAS(objCIOTransferPage.dorLabel);
		
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.ApnLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.ApnLabel);
		objCIOTransferPage.Click(objCIOTransferPage.crossIconAPNEditField);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.ApnLabel, apnToUpdate);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		ReportLogger.INFO("APN   updated successfully");
		
		// fetch the new CIO transfer mail  to record details for updated APN 
		Thread.sleep(6000);
		HashMap<String, ArrayList<String>> responseCIOTransferMailToDetails=salesforceAPI.select("SELECT Formatted_Name1__c ,Formatted_Name2__c ,Care_of__c, Country__c  FROM CIO_Transfer_Mail_To__c where Recorded_APN_Transfer__c ='"+recordeAPNTransferID+"'");

		// STEP 10-Verify the updated details on screen as per new APN

		String updatedAPN_ON_TransferScreen=objCIOTransferPage.getElementText(objCIOTransferPage.apnOnTransferActivityLabel);
		
		String queryAPNIdFromTransferSCreen = "select  Id from Parcel__c where name='"+updatedAPN_ON_TransferScreen+"'";
		String APNIdFromTransferSCreen=salesforceAPI.select(queryAPNIdFromTransferSCreen).get("Id").get(0);
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.situsLabel, ""),primarySitusValue,
				"SMAB-T3822: Validate that primary situs in CIO screen is now that of new APN that was entered in APN field ");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.pucCodeLabel, ""),responsePUCDetails.get("Name").get(0),
				"SMAB-T3822: Validate that PUC   in CIO screen is now that of new APN that was entered in APN field ");
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.shortLegalDescriptionLabel, ""),legalDescriptionValue,
				"SMAB-T3822: Validate that legal description on CIO  screen is now that of new APN that was entered in APN field ");
		
		objCIOTransferPage.scrollToBottom();
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfMailToLabel);
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfMailToLabel),countActiveMaiToRecords,
				"SMAB-T3822: Verify that "
				+ "only the active mail to records for a parcel are shown on  transfer screen after APN update ");
		
		softAssert.assertEquals( responseMailToDetails.get("Name").get(0),responseCIOTransferMailToDetails.get("Formatted_Name1__c").get(0),
				"SMAB-T3822: Verify that "
				+ "formatted name 2 in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Formatted_Name_2__c").get(0),responseCIOTransferMailToDetails.get("Formatted_Name2__c").get(0),
				"SMAB-T3822: Verify that "
				+ "formatted name 1 in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Care_Of__c").get(0),responseCIOTransferMailToDetails.get("Care_of__c").get(0),
				"SMAB-T3822: Verify that "
				+ "Care_Of__c in CIO mail to is that of updated APN 's mail to record ");
		
		softAssert.assertEquals( responseMailToDetails.get("Country__c").get(0),responseCIOTransferMailToDetails.get("Country__c").get(0),
				"SMAB-T3822: Verify that "
				+ "Mailing_Country__c in CIO mail to is that of updated APN 's mail to record ");
		
		objCIOTransferPage.clickViewAll("Ownership for Parent Parcel");
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCIOTransferPage.getGridDataInHashMap();

		softAssert.assertTrue( responseAPNOwnershipDetails.get("Name").contains(HashMapLatestOwner.get("Ownership Id").get(0)),
				"SMAB-T3822: Verify that "
				+ "current ownership records  on transfer screen is that of new APN ");
		
		//navigating back to transfer screen	
		
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		ReportLogger.INFO("Updating the transfer code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-COPAL");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		ReportLogger.INFO("transfer code updated successfully");

		// STEP 11-verifying the correspondence event created after creating correspondence AT from component action
		
		objParcelsPage.createUnrecordedEvent(hashMapCorrespondenceEventForAutoConfirm);
		String urlForTransactionTrail = driver.getCurrentUrl();
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeVisible(30,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		String  auditTrailId=urlForTransactionTrail.split("/")[6];
		
		String auditTrailName =salesforceAPI.select("SELECT Name FROM Transaction_Trail__c where id='"+auditTrailId+"'").get("Name").get(0);
		
		String auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		String countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		
		
		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
				"SMAB-T3822: Validation that  correspondence event created after APN update is linked to the update APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
				"SMAB-T3822: Validation that  correspondence event created after APN update is linked to only the updated APN and not the old APN");
		

		// STEP 12-verifying the WI and business event created after creating APN & legal description WI from component action
		
		String workItemNumber=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		String workItemIdQuery = "SELECT id FROM Work_Item__c where name ='"+workItemNumber+"' ";
		
		String workItemId=salesforceAPI.select(workItemIdQuery).get("Id").get(0);
		
		String apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		String countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,
				"SMAB-T3822: Validation that APN & legal description WI created is linked to only new APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,
				"SMAB-T3822: Validation that APN & legal description WI created is linked to only new APN and not the old APN");
		
		String auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		String responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		JSONObject responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
					"SMAB-T3822: Validation that  business event created from APN & legal description WI after APN update is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3822: Validation that  business event created from APN & legal description WI after APN update is linked to only the updated APN and not the old APN");
			
		
		String auditTrailIdLegalDescriptionWI =salesforceAPI.select("SELECT id FROM Transaction_Trail__c where name='"+auditTrailName+"'").get("Id").get(0);
		 
		driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Transaction_Trail__c/" + auditTrailIdLegalDescriptionWI + "/view");
		
		objMappingPage.scrollToBottom();
		softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedBuisnessEvent), parentAuditTrailNumber,
				"SMAB-T3457: Verifying that business event created for  APN & Legal description WI  is child of parent Recorded correspondence event");

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedCorrespondence), "",
				"SMAB-T3457:Verifying that related correspondence event field in business event created for  APN & Legal description WI  is blank");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.EventId), eventId,
				"SMAB-T3457:Verifying that Event ID field in the business event created for  APN & Legal description WI detail page should be inherited from parent correspondence event");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.RequestOrigin), requestOrigin,
				"SMAB-T3457:Verifying that business event created for  APN & Legal description WI inherits the Request Origin from parent event");
		
		// STEP 13-verifying the original UT WI and Buisness event  created are linked to the updated APN

		 workItemIdQuery = "SELECT id,APN__c FROM Work_Item__c where name ='"+workItemNo+"' ";
		
		 workItemId=salesforceAPI.select(workItemIdQuery).get("Id").get(0);
		
	     String  apnOnWorkItemDetailspage=salesforceAPI.select(workItemIdQuery).get("APN__c").get(0);

		 apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		 countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' and parcel__c='"+activeApnId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,
				"SMAB-T3822: Validation that CIO  UT WI created  is linked to only new APN");
		
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),0,
				"SMAB-T3822: Validation that CIO UT WI created  is linked to only new APN and not the old APN");
		
		softAssert.assertEquals(apnOnWorkItemDetailspage,responseAPNDetails.get("Id").get(0),"SMAB-T3822: Validation that CIO UT WI has  the new APN In details tab  ");

		 auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		 responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		 responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
					"SMAB-T3822: Validation that  business event created for  CIO UT  WI , is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3822: Validation that  business event created for  CIO UT WI is linked to only the updated APN and not the old APN");
			
		// STEP 14-Clicking on submit for approval quick action button
		
		driver.navigate().back();
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.calculateOwnershipButtonLabel);

		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.locateElement(objCIOTransferPage.transferSucessMessage, 5)),
				"Work Item has been submitted for Approval.",
				"SMAB-T3822:Cio trasnfer is submited for approval afterAPN Update");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");
		
		
		// STEP 15-validating that The new ownership  record gets created  for the new APN

		driver.navigate()
		.to("https://smcacre--"
				+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + responseAPNDetails.get("Id").get(0)
				+ "/related/Property_Ownerships__r/view");
		objCIOTransferPage.waitForElementToBeVisible(10,
				objCIOTransferPage.columnInGrid.replace("columnName", objCIOTransferPage.Status));
		objCIOTransferPage.sortInGrid("Status", true);

		HashMapLatestOwner = objCIOTransferPage.getGridDataInHashMap();

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0),
		hashMapOwnershipAndTransferGranteeCreationData.get("Last Name"),
		"SMAB-T3822:The new ownership  record gets created  for the new APN");
		
		// STEP 16-validating that Business event created due to change in transfer code is linked to new APN and not to old APN

		 auditTrailId =salesforceAPI.select("SELECT id FROM Transaction_Trail__c where Event_Library__c in (select id from Event_Library__c  where name='CIO-COPAL') order by createddate desc limit 1").get("Id").get(0);
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where id='"+auditTrailId+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where id='"+auditTrailId+"'";

		softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
						"SMAB-T3822: Validation that  business event created after transfer code update is linked to the update APN");
				
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
						"SMAB-T3822: Validation that  business event created after transfer code update is linked to only the updated APN and not the old APN");
				
		
		// Verify that AT event hierarchy for business event created after transfer code update
		
				driver.navigate().to("https://smcacre--" + execEnv
						+ ".lightning.force.com/lightning/r/Transaction_Trail__c/" + auditTrailId + "/view");
			
			softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedBuisnessEvent), parentAuditTrailNumber,
					"SMAB-T3384: Verifying that business event created by transfer code update is child of parent Recorded correspondence event");

			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedCorrespondence), "",
					"SMAB-T3384:Verifying that related correspondence event field in business event created by transfer code update  is blank");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.EventId), eventId,
					"SMAB-T3384:Verifying that Event ID field in the business event created by transfer code update detail page should be inherited from parent correspondence event");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.RequestOrigin), requestOrigin,
					"SMAB-T3384:Verifying that business event created by transfer code update inherits the Request Origin from parent event");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.dovLabel), dovCIOScreen,
					"SMAB-T3384:Verifying that business event created by transfer code update inherits the DOV from recorded document");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.EventLibrary), "CIO-COPAL",
					"SMAB-T3384:Verifying that business event created by transfer code update has event library value as the event code entered in transfer screen");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.Status), "Open",
					"SMAB-T3384:Verifying that business event created by transfer code update has status as open");
			
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.dorLabel), dorCIOScreen,
					"SMAB-T3384:Verifying that business event created by transfer code update inherits the inherits the DOR from recorded document");
			
		// Step 17: CIO supervisor now logs in and navigates to the  transfe screen and approves it
		objCIOTransferPage.logout();
		Thread.sleep(4000);
		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		objCIOTransferPage.waitForElementToBeClickable(10,objCIOTransferPage.quickActionButtonDropdownIcon);
		
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity(objCIOTransferPage.approveButton);
		objCIOTransferPage.waitForElementTextToBe(objCIOTransferPage.confirmationMessageOnTranferScreen,
				"Work Item has been approved successfully.", 30);
		
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);
		ReportLogger.INFO("CIO!! Transfer approved by Supervisor");
		
		
		//Step 18 : Validate the Appraisal WIs created  

		workItemQuery = "SELECT Id,name,APN__c   FROM Work_Item__c where Type__c='Appraiser' And Sub_Type__c ='Appraisal Activity'  and  status__c='In Pool' order by createdDate desc limit 1";
		 workItemNumber = salesforceAPI.select(workItemQuery).get("Name").get(0);

		workItemId=salesforceAPI.select(workItemQuery).get("Id").get(0);
        apnOnWorkItemDetailspage=salesforceAPI.select(workItemQuery).get("APN__c").get(0);
       
		apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,"SMAB-T3822: Validation that Appraisal  WI created after approval by supervisor is linked to only new APN");
						
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,"SMAB-T3822: Validation that Appraisal WI created after approval by supervisor is linked to only new APN and not the old APN");
			
		softAssert.assertEquals(apnOnWorkItemDetailspage,responseAPNDetails.get("Id").get(0),"SMAB-T3822: Validation that Appraisal WI created after approval by supervisor has new APN in APN look up field on WI details page");
		
		 auditTrailsQuery = "SELECT Business_EVENT__r.NAME from work_item_linkage__c where work_item__r.name='"+workItemNumber+"'";
		 responseAuditTrailDetails = salesforceAPI.select(auditTrailsQuery).toString().replace("{Business_Event__r=[", "").replace("}]", "");
		
		 responseAuditTrailDetailsJson = new JSONObject(responseAuditTrailDetails);  
		
		 auditTrailName=responseAuditTrailDetailsJson.get("Name").toString();
		 auditTrailsParcelLinkage= "SELECT Associated_APNs__c FROM Transaction_Trail__c where Name='"+auditTrailName+"'";
		countAuditTrailsParcelLinkage= "SELECT count(id) FROM Transaction_Trail__c where Name='"+auditTrailName+"'";

		 softAssert.assertEquals(salesforceAPI.select(auditTrailsParcelLinkage).get("Associated_APNs__c").get(0),APNIdFromTransferSCreen,
					"SMAB-T3822: Validation that  business event created for  Appraisal WI  , is linked to the update APN");
			
		softAssert.assertEquals(salesforceAPI.select(countAuditTrailsParcelLinkage).get("expr0").get(0),1,
					"SMAB-T3822: Validation that  business event created for  Appraisal WI is linked to only the updated APN and not the old APN");
			
		
		workItemQuery = "SELECT Id,name  FROM Work_Item__c where Type__c='Appraiser' And Sub_Type__c ='Questionnaire Correspondence'  and  status__c='In Pool' order by createdDate desc limit 1";
		workItemId=salesforceAPI.select(workItemQuery).get("Id").get(0);

		apnWorkItemQuery = "SELECT parcel__c FROM Work_Item_Linkage__c where Work_Item__c ='"+workItemId+"' ";
		countAPNWorkItemQuery = "SELECT count(id) FROM Work_Item_Linkage__c where Work_Item__c  ='"+workItemId+"' ";

		softAssert.assertEquals(salesforceAPI.select(apnWorkItemQuery).get("Parcel__c").get(0),APNIdFromTransferSCreen,"SMAB-T3822: Validation that Appraisal  Questionnaire Correspondence WI created after approval by supervisor is linked to only new APN");
						
		softAssert.assertEquals(salesforceAPI.select(countAPNWorkItemQuery).get("expr0").get(0),1,"SMAB-T3822: Validation that Appraisal Questionnaire Correspondence WI created after approval by supervisor is linked to only new APN and not the old APN");
			
		objCIOTransferPage.logout();

}
}