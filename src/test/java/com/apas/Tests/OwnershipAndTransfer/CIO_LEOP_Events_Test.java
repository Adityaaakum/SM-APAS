package com.apas.Tests.OwnershipAndTransfer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.SupportedSourceVersion;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.AppraisalActivityPage;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.ReportsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class CIO_LEOP_Events_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	ApasGenericPage objApasGenericPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	AppraisalActivityPage objAppraisalActivity;
	String apnPrefix = new String();
	CIOTransferPage objCIOTransferPage;
	ExemptionsPage objExemptionsPage;
	AuditTrailPage trail;
	String unrecordedEventData;
	String ownershipCreationData;
	ReportsPage objReportsPage;
	
	

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objReportsPage = new ReportsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		salesforceAPI = new SalesforceAPI();
		trail = new AuditTrailPage(driver);
		unrecordedEventData = testdata.UNRECORDED_EVENT_DATA;
		ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		objAppraisalActivity= new AppraisalActivityPage(driver);

	}

	/*
	 * This method is to Verify the calculate penalty form on Appraisal activity screen
	 * 
	 * @param loginUser
	 * 
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3918:Verify that User is able to perform LEOP event on component actions and calculate penalty on appraisal activity screen.", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "LEOPEvent", "UnrecordedEvent" })
	public void OwnershipAndTransfer_Penalty_CalculatePenalty_Test(String loginUser) throws Exception {
		String execEnv = System.getProperty("region");
		JSONObject jsonObject = objMappingPage.getJsonObject();
		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithCompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateRpOwnership");

		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String activeApn = response.get("Name").get(0);
		String activeApnId = response.get("Id").get(0);

		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData,
				"DataToCreateLeopEventCreation");
		
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
				"SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String districtValue = "District01";

		jsonObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c", "Active");
		jsonObject.put("District__c", districtValue);
		jsonObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c", responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c", response.get("Id").get(0), jsonObject);
		
		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		// STEP 2- deleting ownership on parcel

		objCIOTransferPage.deleteOwnershipFromParcel(activeApnId);

		// STEP 3- adding owner after deleting for the recorded APN

		String acesseName = objMappingPage.getOwnerForMappingAction();
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + activeApnId
				+ "/related/Property_Ownerships__r/view");
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.getButtonWithText("New"));
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
		Thread.sleep(5000);

		// Step 5: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step 6: Opening the PARCELS page
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		

		// Step 7: Create UT event and validate warning message on CIO Transfer screen
		objCIOTransferPage.createLeopUnrecordedEvent(dataToCreateUnrecordedEventMap);

		// Step 8: Edit the Transfer activity and update the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-REASS");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		String transferScreenURL = driver.getCurrentUrl();
		String recordeAPNTransferID = transferScreenURL.split("/")[6];

		// STEP 9: Creating the new grantee on transfer

		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID,
				hashMapOwnershipAndTransferGranteeCreationData);

		// STEP 10: Validating present grantee

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		objParcelsPage.waitForElementToBeVisible(objParcelsPage.getButtonWithText("New"));
		HashMap<String, ArrayList<String>> granteeHashMap = objCIOTransferPage.getGridDataForRowString("1");
		String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);

		// STEP 11: Creating copy to mail to record

		objCIOTransferPage.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
		objCIOTransferPage.waitForElementToBeClickable(7, objCIOTransferPage.copyToMailToButtonLabel);

		// STEP 12: Validating mail to record created from copy to mail to

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "" + "/related/CIO_Transfer_Mail_To__r/view");
		objCIOTransferPage.waitForElementToBeClickable(5, objCIOTransferPage.newButton);

		// STEP 13: Navigating back to RAT screen

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 14: Clicking on submit for approval quick action button

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		driver.navigate().refresh();
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),
				"Submitted for Approval",
				"SMAB-T3918: Validating CIO Transfer activity status on transfer activity screen after submit for approval.");

		// STEP 15- Get audit trail Value from transfer screen and validate the status
		String auditTrailName = objWorkItemHomePage.getElementText(objCIOTransferPage.CIOAuditTrail);
		String auditTrailID = salesforceAPI
				.select("SELECT Id,Status__c,Name FROM Transaction_Trail__c where Name='" + auditTrailName + "'")
				.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrailID + "/view");
		objCIOTransferPage.waitUntilPageisReady(driver);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
				"SMAB-T3918: Validating that audit trail status should be open after submit for approval.");

		// STEP 16-Navigating back to RAT screen and clicking on back quick action button

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeClickable(5, objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 17-Validating that back button has navigates the user to WI page and status of WI should be submitted for approval.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Submitted for Approval",
				"SMAB-T3918: Validating that status of WI should be submitted for approval.");
		objCIOTransferPage.logout();
		Thread.sleep(5000);

		// STEP 18- login with CIO supervisor

		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);

		// STEP 19-Clicking on approval quick action button
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity("Approve");
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		driver.navigate().refresh();
		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus), "Approved",
				"SMAB-T3918: Validating CIO Transfer activity status on transfer activity screen after approved by supervisor.");

		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		// STEP 20-Validating that WI and audit trail status after approving the transfer activity.

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Completed",
				"SMAB-T3918: Validating that WI status should be completed after approval by supervisor.");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrailID + "/view");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Completed",
				"SMAB-T3918: Validating that audit trail status should be open after submit for approval.");
		objCIOTransferPage.logout();
		Thread.sleep(5000);

		// Step 21: Login to the APAS application
		objMappingPage.login(APPRAISAL_SUPPORT);

		objMappingPage.searchModule(modules.HOME);
		
	
		String workItemQuery = "SELECT Id, Name FROM Work_Item__c Where Type__c= 'Appraiser' AND Sub_Type__c = 'Appraisal Activity'order by createdDate desc limit 1";
		String workItemNo = salesforceAPI.select(workItemQuery).get("Name").get(0);

		// Step 22: Accepting Work Item and checking it in Progress Tab as well
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.TAB_IN_POOL, 20);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
		objMappingPage.waitForElementToBeClickable(workItemNo);

		objWorkItemHomePage.acceptWorkItem(workItemNo);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
		objMappingPage.waitForElementToBeClickable(workItemNo);
		objWorkItemHomePage.findWorkItemInProgress(workItemNo);

		// Step 23: Going to work item's detail tab and clicking on appraisal activity link

		objMappingPage.globalSearchRecords(workItemNo);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindows = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindows);
		String reviewLink = driver.getCurrentUrl();
		driver.navigate().to(reviewLink);
		objMappingPage.waitForElementToBeClickable(objApasGenericPage.getFieldValueFromAPAS("APN"));

		
		// Step 24: Giving Land and Improvement Values
		String expectedApn = objApasGenericPage.getFieldValueFromAPAS("APN");
		String expectedEventId = objApasGenericPage.getFieldValueFromAPAS("EventID");
		String expectedDov = objApasGenericPage.getFieldValueFromAPAS("DOV");
		objMappingPage.Click(objAppraisalActivity.appraisalActivityEditValueButton("Land"));
		objMappingPage.waitForElementToBeVisible(objApasGenericPage.getButtonWithText("Save"));
		objApasGenericPage.enter("Land", "10000");
		objApasGenericPage.enter("Improvement", "10000");

		objApasGenericPage.Click(objApasGenericPage.getButtonWithText("Save"));

		// Step 25: Filling the penalty form and clicking on Save button
		objMappingPage.waitForElementToBeClickable(objApasGenericPage.getButtonWithText("Calculate Penalty"));
		objApasGenericPage.Click(objApasGenericPage.getButtonWithText("Calculate Penalty"));
		objMappingPage.waitForElementToBeClickable(objAppraisalActivity.calculatePenaltySaveButton);
		
		
		String expectedAssessedValue = "$20,000";
		String actualApn = objApasGenericPage.getFieldValueFromAPAS("APN");
		String actualEventId = objApasGenericPage.getFieldValueFromAPAS("EventID");
		String actualDov = objApasGenericPage.getFieldValueFromAPAS("DOV");

		softAssert.assertEquals(actualApn, expectedApn, "SMAB-T3918:APN should match");
		softAssert.assertEquals(actualEventId, expectedEventId, "SMAB-T3918:Event ID should match");
		softAssert.assertEquals(actualDov, expectedDov, "SMAB-T3918:DOV should match");
		
		String penaltyFormCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapPenaltyFormCreationData = objUtil.generateMapFromJsonFile(
				penaltyFormCreationData, "DataToCreatePenaltyRecord");

		objCIOTransferPage.fillCalculatePenaltyForm(hashMapPenaltyFormCreationData);
		// Preparing Penalty data
		String penaltyData = "SELECT Id FROM Penalty__c ORDER By Name Desc LIMIT 1";
		String penaltynumber = salesforceAPI.select(penaltyData).get("Id").get(0);
		ReportLogger.INFO("Created Penalty Number : " + penaltynumber);

		// Step 26: checking the penalty created
		objMappingPage.waitForElementToBeClickable(objApasGenericPage.getButtonWithText("Back"));
		
		String url = "https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Penalty__c/" + penaltynumber + "/view";
		driver.navigate().to(url);

		objParcelsPage.waitForElementToBeClickable(objParcelsPage.editButton);				
		String actualAPNPenaltyPage=objApasGenericPage.getFieldValueFromAPAS("APN");
		String actualDOVPenaltyPage=objApasGenericPage.getFieldValueFromAPAS("DOV");
		String actualPenaltyCodePenaltyPage=objApasGenericPage.getFieldValueFromAPAS("Penalty Code");
		String actualAssessedValuePenaltyPage=objApasGenericPage.getFieldValueFromAPAS("Assessed Value");
		
		softAssert.assertEquals(actualAssessedValuePenaltyPage, expectedAssessedValue,
				"SMAB-T3918:Assessed value should be equal to sum of Land and Improvement Value which one is expected, according to the input, is 20000");
		softAssert.assertEquals(actualAPNPenaltyPage, expectedApn,
				"SMAB-T3918:APN should be same as mentioned on the appraisal activity page.");
		softAssert.assertEquals(actualDOVPenaltyPage, expectedDov,
				"SMAB-T3918:DOV should be same as mentioned on the appraisal activity page.");
		softAssert.assertEquals(actualPenaltyCodePenaltyPage, "PEN-LEOP",
				"SMAB-T3918:Penalty Code should be same as mentioned on the appraisal activity page.");
		ReportLogger.INFO("Penalty Details Validation Completed.");
		objCIOTransferPage.logout();		
		
	}

	/*
	 * This method is to validate LEOP creation, submitted for approval, returned, resubmitted and approved
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3368, SMAB-T3359, SMAB-T3366, SMAB-T3439 : Verify that User is able to perform LEOP event, submit for approval and approved", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "LEOPEvent", "UnrecordedEvent" })
	public void OwnershipAndTransfer_LEOP_Creation_ReturnedAndApproved(String loginUser) throws Exception {
		
		//Data Setup
		String execEnv = System.getProperty("region");
		String userNameForCioStaff = CONFIG.getProperty(users.CIO_STAFF + "UserName");
		String userNameForCioSupervisor = CONFIG.getProperty(users.CIO_SUPERVISOR + "UserName");
		
		JSONObject jsonObjectForLEOP1 = objMappingPage.getJsonObject();
		JSONObject jsonObjectForLEOP2 = objMappingPage.getJsonObject();
		
		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithCompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData, 
				"DataToCreateRpOwnership");
		Map<String, String> dataToAttemptToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData,
				"DataToAttemptToCreateLeopEvent");
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData,
				"DataToCreateLeopEvent");

		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String activeApn = response.get("Name").get(0);
		String activeApnId = response.get("Id").get(0);

		//Get values from Database and enter values in the Parcels
		String legalDescriptionValue="Legal PM 85/25-260";
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.
				select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responseSitusDetails= salesforceAPI.
				select("SELECT Id, Name FROM Situs__c where Name != NULL LIMIT 1");
		String primarySitusId=responseSitusDetails.get("Id").get(0);
		String primarySitusValue=responseSitusDetails.get("Name").get(0);
				
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		jsonObjectForLEOP1.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObjectForLEOP1.put("Primary_Situs__c",primarySitusId);
		jsonObjectForLEOP1.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonObjectForLEOP1.put("TRA__c", responseTRADetails.get("Id").get(0));
		jsonObjectForLEOP1.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", response.get("Id").get(0), jsonObjectForLEOP1);
		
		//Login with SYS-ADMIN
		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		
		//Deleting ownership on parcel
		objCIOTransferPage.deleteOwnershipFromParcel(activeApnId);

		//Adding new owner on APN
		String assesseeName = objMappingPage.getOwnerForMappingAction();
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + activeApnId
				+ "/related/Property_Ownerships__r/view");
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.getButtonWithText("New"));
		objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
		String ownershipId = driver.getCurrentUrl().split("/")[6];

		//Updating the ownership date for current owners
		String dateOfEvent = salesforceAPI
				.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '" + ownershipId + "'")
				.get("Ownership_Start_Date__c").get(0);
		jsonObjectForLEOP2.put("DOR__c", dateOfEvent);
		jsonObjectForLEOP2.put("DOV_Date__c", dateOfEvent);
		salesforceAPI.update("Property_Ownership__c", ownershipId, jsonObjectForLEOP2);
		
		//Deleting old mail to records from APN
		objCIOTransferPage.deleteMailToRecordsFromParcel(activeApn);
				
		objMappingPage.logout();
		Thread.sleep(5000);

		//Login to the APAS application through CIO Staff
		objMappingPage.login(loginUser);

		//Opening the PARCELS page
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		
		//Create LEOP event and validate mandatory field
		objCIOTransferPage.createLeopUnrecordedEvent(dataToAttemptToCreateUnrecordedEventMap);
		String expectedIndividualFieldMessage1 = "Complete this field.";
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date of Event"),expectedIndividualFieldMessage1,
				"SMAB-T3368: Validate the mandatory field error message for 'Date of Event' field");
		
		//Create LEOP event after filling the required field too
		objCIOTransferPage.enter(objCIOTransferPage.dateOfEventInputTextBox, dataToCreateUnrecordedEventMap.get("Date of Event"));
		objCIOTransferPage.selectOptionFromDropDown(objCIOTransferPage.leopReceivedByBOE, dataToCreateUnrecordedEventMap.get("LEOP Received By BOE"));
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveAndNextButton));
		Thread.sleep(5000);//Allows system to create the WI for further query to run fine
		
		//Query to fetch WI created
		String workItemQuery = "SELECT Id, Name, Related_Action__c FROM Work_Item__c where Type__c='CIO' And Work_Pool__r.name='CIO' And Status__c='In Progress' order by createdDate desc limit 1";		
		String workItemId = salesforceAPI.select(workItemQuery).get("Id").get(0);
		softAssert.assertTrue(salesforceAPI.select(workItemQuery).get("Related_Action__c").get(0).contains("LEOP Activity"),
				"SMAB-T3368: Validate the Related Action value is 'LEOP Activity' on the Work Item'");
		
		//Edit the Transfer activity and add the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, objCIOTransferPage.CIO_EVENT_CODE_COPAL);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		
		//Get Transfer activity URL and Leop ID
		String transferScreenURL = driver.getCurrentUrl();
		String unRecordeAPNTransferID = transferScreenURL.split("/")[6];
		String leopEventId = objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel);
		
		//Validate the values on Transfer Screen
		ReportLogger.INFO("Validate the LEOP Transfer activity values");
		softAssert.assertEquals(leopEventId.substring(0, 2),"CO",
				"SMAB-T3368: Validate that CIO staff is able to verify the prefix of Event ID");
		softAssert.assertEquals(leopEventId.substring(10, 11),"P",
				"SMAB-T3368: Validate that CIO staff is able to verify the suffix of Event ID");
		softAssert.assertEquals(leopEventId.length(),"11",
				"SMAB-T3368: Validate that CIO staff is able to verify the length of Event ID");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.situsLabel, ""),primarySitusValue,
				"SMAB-T3368: Validate that CIO staff is able to verify the Situs value on LEOP Activity");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.shortLegalDescriptionLabel, ""),legalDescriptionValue,
				"SMAB-T3368: Validate that CIO staff is able to verify the Short Legal Description value on LEOP Activity");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.pucCodeLabel, ""),responsePUCDetails.get("Name").get(0),
				"SMAB-T3368: Validate that CIO staff is able to verify the PUC value on LEOP Activity");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.doeLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3368: Validate that CIO staff is able to verify the DOE on LEOP Activity");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3368: Validate that CIO staff is able to verify the DOV on LEOP Activity");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dorLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3368: Validate that CIO staff is able to verify the DOR on LEOP Activity");
		
		//Navigating to mail to screen and Create mail to record 
		ReportLogger.INFO("Navigate to Mail-To screen and create a Mail To record");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+unRecordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
		Thread.sleep(5000);		//Added to avoid regression failure in E2E
		
		objCIOTransferPage.Click(objCIOTransferPage.newButtonMailToListViewScreen);
		objCIOTransferPage.enter(objCIOTransferPage.formattedName1Label, hashMapOwnershipAndTransferCreationData.get("Formatted Name1"));
		objCIOTransferPage.enter(objCIOTransferPage.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));   
		objCIOTransferPage.enter(objCIOTransferPage.mailingZip,hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.formattedName1Label );		  
		
		softAssert.assertContains( objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.formattedName1Label),hashMapOwnershipAndTransferCreationData.get("Formatted Name1"),
				"SMAB-T3368: Verify user is able to save mail to record on LEOP Activity");
					
		//Navigate to RAT screen and validate number of Grantors/Grantee on the UT activity 
		ReportLogger.INFO("Navigate back to RAT and validate number of Grantors/Grantee on the UT activity");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+unRecordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.numberOfGrantorLabel);
				
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGrantorLabel),"0",
				"SMAB-T3368: Verify user is able to validate number of Grantors on the LEOP Activity");
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGranteeLabel),"0",
				"SMAB-T3368: Verify user is able to validate number of Grantee on the LEOP Activity");
				
		//Create the new Grantee
		ReportLogger.INFO("Create New Grantee record");
		objCIOTransferPage.createNewGranteeRecords(unRecordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			
				
		//Navigate to RAT screen and click View ALL to see all Grantee records in grid
		ReportLogger.INFO("Navigate to RAT screen and click View ALL to see all Grantee records in grid");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+unRecordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.numberOfGrantorLabel);
		objCIOTransferPage.clickViewAll("CIO Transfer Grantee & New Ownership");
				
		//Validate the details in the grid
		ReportLogger.INFO("Validate the Grantee record in Grid on LEOP Activity");
		HashMap<String, ArrayList<String>> HashMapLatestGrantee  = objCIOTransferPage.getGridDataInHashMap();
		softAssert.assertEquals(HashMapLatestGrantee.get("Status").get(0), "Active", 
			  "SMAB-T3368: Validate the status on Grantee record");
		softAssert.assertEquals(HashMapLatestGrantee.get("Owner Percentage").get(0), hashMapOwnershipAndTransferGranteeCreationData.get("Owner Percentage")+".0000%", 
		      "SMAB-T3368: Validate the percentage on Grantee record");
		softAssert.assertEquals(HashMapLatestGrantee.get("Grantee/Retain Owner Name").get(0),hashMapOwnershipAndTransferGranteeCreationData.get("Last Name") + " " + hashMapOwnershipAndTransferGranteeCreationData.get("First Name"), 
			  "SMAB-T3368: Validate the Grantee Name on Grantee record");
		
		if (HashMapLatestGrantee.containsKey("Recorded Document")) {
		       	softAssert.assertEquals(HashMapLatestGrantee.get("Recorded Document").get(0), leopEventId,
		       		 "SMAB-T3368: Validate the Recorded Document number on Grantee record");
		}
		
		if (HashMapLatestGrantee.containsKey("Recorded Document Number")) {
		       	softAssert.assertEquals(HashMapLatestGrantee.get("Recorded Document Number").get(0), leopEventId,
		       		 "SMAB-T3368: Validate the Recorded Document number on Grantee record");
		}
		        
		//Navigate to RAT screen and click View ALL to see current Ownership records in grid
		ReportLogger.INFO("Navigate to RAT screen and click View ALL to see current Ownership records in grid");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+unRecordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.numberOfGrantorLabel);
		objCIOTransferPage.clickViewAll("Ownership for Parent Parcel");
			
		//Validate the details in the Ownership grid
		ReportLogger.INFO("Validate the Current Ownership record in Grid");
		HashMap<String, ArrayList<String>>HashMapLatestOwner  = objCIOTransferPage.getGridDataInHashMap();
		
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), assesseeName, 
				  "SMAB-T3368: Validate the owner name on Grantee record");
		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active", 
		  		  "SMAB-T3368: Validate the status on Grantee record");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "100.0000%", 
		  		  "SMAB-T3368: Validate the percentage on Grantee record");
		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),hashMapCreateOwnershipRecordData.get("Ownership Start Date") , 
		   		  "SMAB-T3368: Validate the start date on Grantee record");
		
		//Validate the details in the Audit Trail record
		ReportLogger.INFO("Validate the Audit Trail record generated");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Work_Item__c/"+workItemId+"/view");
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);          
		String parentAuditTrailNumber = objWorkItemHomePage.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
		String auditTrailID=salesforceAPI.select("SELECT Id,Status__c,Name FROM Transaction_Trail__c where Name='"+parentAuditTrailNumber+"'").get("Id").get(0);
		
		objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
		objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
		objCIOTransferPage.waitForElementToBeVisible(20, trail.statusLabel);
				
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"), "Open",
				"SMAB-T3359: Validate that audit trail status");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.eventLibraryLabel),"Leops",
				"SMAB-T3359: Validate the 'Event Library' field in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.eventTypeLabel),"LEOP Activity",
				"SMAB-T3359: Validate the 'Event Type' field in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objCIOTransferPage.leopReceivedByBOE),"Yes",
				"SMAB-T3359: Validate the 'LEOP received by BOE' field in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objCIOTransferPage.penaltyRequiredPerBOE),"Yes",
				"SMAB-T3359: Validate the 'Penalty required per BOE' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dateOfEventInputTextBox),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3359: Validate the 'DOE' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dateOfValueInputTextBox),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3359: Validate the 'DOV' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dateOfRecordingInputTextBox),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3359: Validate the 'DOR' field in Audit Trail record.");
		softAssert.assertTrue(objCIOTransferPage.getFieldValueFromAPAS(trail.relatedBusinessEventLabel).equals(""),
				"SMAB-T3359: Validate the 'Related Business Event' field in Audit Trail record is empty");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(trail.eventNumberLabel),leopEventId,
				"SMAB-T3359: Validate the 'Event Number' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(trail.nameLabel),parentAuditTrailNumber,
				"SMAB-T3359: Validate the 'Name' field in Audit Trail record.");
		
		//Navigate back to Work Item followed by transfer activity record using related action link
		driver.navigate().back();
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.firstRelatedBuisnessEvent);
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.reviewLink), "LEOP Activity",
						"SMAB-T3359: Validate the Related action link should be visible on WI");

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.transferCodeLabel);
				
		//Submit the transfer activity for approval
		ReportLogger.INFO("Submit the transfer activity for approval");
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionSubmitForApproval);	
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);	
		Thread.sleep(2000); //Allows the record to save properly
		
		//Transfer Activity submitted for approval
		ReportLogger.INFO("CIO!! Transfer submitted for approval");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Submitted for Approval", 
				"SMAB-T3366: Validate CIO Transfer activity status on LEOP activity screen after submit for approval");
		
		//Validating that back button has navigates the user to WI page and status of WI should be submitted for approval.
		objCIOTransferPage.waitForElementToBeClickable(5,objCIOTransferPage.quickActionButtonDropdownIcon);	          
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
				
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval", 
				"SMAB-T3366: Validate the status of WI");
						
		//Navigate to RAT screen and click View ALL to see current Ownership records in grid
		ReportLogger.INFO("Navigate to RAT screen and click View ALL to see current Ownership records in grid");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+unRecordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.numberOfGrantorLabel);
		        
		ReportLogger.INFO("Update the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.Click(objCIOTransferPage.clearSelectionEventCode);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, objCIOTransferPage.CIO_EVENT_CODE_PART);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		        
		//Validate error messages when user tries to update the record once the LEOP activity is submitted for approval
		String expectedErrorMessageOnTop = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nOops...you don't have the necessary privileges to edit this record. See your administrator for help.";
		softAssert.assertEquals(objCIOTransferPage.getElementText(objApasGenericPage.pageError),expectedErrorMessageOnTop,
				"SMAB-T3366: Validate 'Transfer Code' cannot be updated after LEOP activity is Submitted for Approval");
		objCIOTransferPage.Click(objExemptionsPage.cancelButton);
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.numberOfGrantorLabel);
				
		ReportLogger.INFO("Create another Grantee record");
		objCIOTransferPage.createNewGranteeRecords(unRecordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			
		String expectedErrorMessage = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\ninsufficient access rights on cross-reference id";
		softAssert.assertEquals(objCIOTransferPage.getElementText(objApasGenericPage.pageError),expectedErrorMessage,
				"SMAB-T3366: Validate new Grantee Record cannot be created after LEOP activity is Submitted for Approval");
		objCIOTransferPage.Click(objExemptionsPage.cancelButton);
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.numberOfGrantorLabel);
				
		//Validate the Mail-To record on the parcel
		ReportLogger.INFO("Validate the Mail-To record on the parcel");
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		objParcelsPage.openParcelRelatedTab("Mail-To");
		objCIOTransferPage.waitForElementToBeVisible(20, objParcelsPage.numberOfMailToOnParcelLabel);
				
		HashMap<String, ArrayList<String>> mailToTableDataHashMap = objParcelsPage.getParcelTableDataInHashMap("Mail-To");
		String status = mailToTableDataHashMap.get("Status").get(0);
		String formattedName1 = mailToTableDataHashMap.get("Formatted Name 1").get(0);
				
		softAssert.assertContains(status,"Active",
				"SMAB-T3366: Validate the 'Status' of Mail-To record on the LEOP activity");
		softAssert.assertContains(formattedName1, hashMapOwnershipAndTransferCreationData.get("Formatted Name1"),
				"SMAB-T3366: Validate the 'Formatted Name 1' of Mail-To record on the LEOP activity");
				
		objCIOTransferPage.logout();
		Thread.sleep(5000);
		
		//Login with Supervisor
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
				
		//Clicking on return quick action button
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionReturn);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionReturn);
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.returnReasonTextBox);
		objCIOTransferPage.enter(objCIOTransferPage.returnReasonTextBox, "Returned by CIO Supervisor");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);

		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Returned", 
				"SMAB-T3439: Validate LEOP activity status after returned by supervisor.");
		
		//Validating WI and AUDIT Trail status after returned by supervisor.
		objCIOTransferPage.waitForElementToBeClickable(5,objCIOTransferPage.quickActionButtonDropdownIcon);	          
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
	
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Returned", 
				"SMAB-T3439: Validate the status on WI");
		
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(20, trail.statusLabel);
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", 
				"SMAB-T3439: Validate the audit trail status after LEOP activity is returned");
		
		objCIOTransferPage.logout();
		Thread.sleep(5000);

		//Login with CIO Staff
		objMappingPage.login(loginUser);
		driver.navigate().to(transferScreenURL);
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		//Transfer resubmit for approval by staff
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);

		ReportLogger.INFO("CIO!! Transfer resubmit for approval by staff");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Submitted for Approval", 
				"SMAB-T3439: Validate LEOP activity status after resubmitted for approval by staff.");
		
		//Validating that back button has navigates the user to WI page
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		//Validating WI and AUDIT Trail status after Submitted for Approval
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval", 
				"SMAB-T3439: Validate WI status after resubmit for approval ");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(20, trail.statusLabel);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Open", 
				"SMAB-T3439: Validate the audit trail status after resubmitted for approval.");

		objCIOTransferPage.logout();
		Thread.sleep(5000);

		//Login from CIO Supervisor
		objMappingPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to(transferScreenURL);
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
				
		//Clicking on approval quick action button
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity(objCIOTransferPage.approveButton);
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.finishButtonPopUp);
		objCIOTransferPage.Click(objCIOTransferPage.finishButtonPopUp);
		Thread.sleep(2000);

		ReportLogger.INFO("CIO!! Transfer Approved");
		objCIOTransferPage.waitForElementToBeVisible(20, objCIOTransferPage.CIOstatus);
		objCIOTransferPage.scrollToElement(objCIOTransferPage.CIOstatus);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCIOTransferPage.CIOstatus),"Approved", 
				"SMAB-T3439: Validate LEOP activity status after approved by supervisor.");
		
		//Validating that WI and audit trail status after approving the transfer activity
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionBack);
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", 
				"SMAB-T3439: Validate the WI status after approval by supervisor.");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Transaction_Trail__c/"+auditTrailID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(20, trail.statusLabel);
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Completed", 
				"SMAB-T3439: Validate that audit trail status after LEOP activity is approved");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Processed By", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioStaff + "'").get("Name").get(0),
				"SMAB-T3439: Validate the 'Processed By' field value in Audit Trail record");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Final Approver", "Additional Information"), salesforceAPI.select("SELECT Name FROM User where Username ='" + userNameForCioSupervisor + "'").get("Name").get(0),
				"SMAB-T3439: Validate the 'Final Approver' field value in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.eventLibraryLabel),"Leops",
				"SMAB-T3359: Validate the 'Event Library' field in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.eventTypeLabel),"LEOP Activity",
				"SMAB-T3359: Validate the 'Event Type' field in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objCIOTransferPage.leopReceivedByBOE),"Yes",
				"SMAB-T3359: Validate the 'LEOP received by BOE' field in Audit Trail record.");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objCIOTransferPage.penaltyRequiredPerBOE),"Yes",
				"SMAB-T3359: Validate the 'Penalty required per BOE' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dateOfEventInputTextBox),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3359: Validate the 'DOE' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dateOfValueInputTextBox),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3359: Validate the 'DOV' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dateOfRecordingInputTextBox),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3359: Validate the 'DOR' field in Audit Trail record.");
		softAssert.assertTrue(objCIOTransferPage.getFieldValueFromAPAS(trail.relatedBusinessEventLabel).equals(""),
				"SMAB-T3359: Validate the 'Related Business Event' field in Audit Trail record is empty");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(trail.eventNumberLabel),leopEventId,
				"SMAB-T3359: Validate the 'Event Number' field in Audit Trail record.");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(trail.nameLabel),parentAuditTrailNumber,
				"SMAB-T3359: Validate the 'Name' field in Audit Trail record.");
								
		objCIOTransferPage.logout();
	}
	
	
	/*
	 * This method is to validate LEOP default Mail-To-Records
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3366 : Verify that User is able to view Mail-to records from Parcel on the UT event created on it", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "LEOPEvent", "UnrecordedEvent" })
	public void OwnershipAndTransfer_LEOP_DefaultMailToRecord(String loginUser) throws Exception {
		
		//Data Setup
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData,
				"DataToCreateLeopEvent");
		
		String parcelWithActiveMailTo = "SELECT Parcel__c,Id FROM Mail_To__c where status__c = 'Active'  Limit 1";
		HashMap<String, ArrayList<String>> hashMapParcelWithActiveMailTo = salesforceAPI.select(parcelWithActiveMailTo);
		String activeApnId = hashMapParcelWithActiveMailTo.get("Parcel__c").get(0);
		
		String queryAPN = "Select Name from Parcel__c where Id = '" + activeApnId +  "' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPN);
		String activeApn = response.get("Name").get(0);
		
		//Login to the APAS application through CIO Staff
		objMappingPage.login(loginUser);

		//Opening the PARCELS page and get Mail-To data
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		objParcelsPage.openParcelRelatedTab("Mail-To");
		objCIOTransferPage.waitForElementToBeVisible(20, objParcelsPage.numberOfMailToOnParcelLabel);
				
		HashMap<String, ArrayList<String>> mailToTableDataHashMap = objParcelsPage.getParcelTableDataInHashMap("Mail-To");
		String status = mailToTableDataHashMap.get("Status").get(0);
		String formattedName1 = mailToTableDataHashMap.get("Formatted Name 1").get(0);
		String careOf = mailToTableDataHashMap.get("Care Of").get(0); 
		String mailingState = mailToTableDataHashMap.get("Mailing State").get(0);
			
		//Create LEOP event
		objCIOTransferPage.createLeopUnrecordedEvent(dataToCreateUnrecordedEventMap);
		
		//Validate the Mail-To record on the LEOP Activity
		ReportLogger.INFO("Validate the Mail-To record on the LEOP Activity");
		objCIOTransferPage.scrollToBottom();
		objCIOTransferPage.clickViewAll("CIO Transfer Mail To");
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.formattedName1Label );
		
		//Compare the Mail-To details
		ReportLogger.INFO("Get the grid data for Mail-To");
		HashMap<String, ArrayList<String>>hashMapMailTo  = objCIOTransferPage.getGridDataInHashMap();
		
		softAssert.assertContains(formattedName1, hashMapMailTo.get("Formatted Name1").get(0),
				"SMAB-T3366: Validate the 'Formatted Name 1' of Mail-To record is '" + hashMapMailTo.get("Formatted Name1").get(0) + "' on the LEOP activity");
		softAssert.assertEquals(careOf,hashMapMailTo.get("Care of").get(0),
				"SMAB-T3366: Validate the 'Care Of' of Mail-To record on the LEOP activity");
		softAssert.assertEquals(mailingState,hashMapMailTo.get("Mailing State").get(0),
				"SMAB-T3366: Validate the 'Mailing State' of Mail-To record on the LEOP activity");
		
		objCIOTransferPage.logout();
		
	}
	
}
