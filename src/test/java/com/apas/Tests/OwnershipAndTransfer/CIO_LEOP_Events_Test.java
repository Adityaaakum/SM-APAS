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
			"Regression", "LEOPEvent" })
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

}
