package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.By;
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

public class CIO_RollBack_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
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
		OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

	}

	/*
	 * Ownership And Transfers - Verify user is able to perform rolled back -active
	 * operation on transfer screen for partial transfers
	 */

	@Test(description = "SMAB-T3384,SMAB-T3430,SMAB-T3510:Ownership And Transfers - Verify user is able  to perform rolled back -active operation on transfer screen for partial transfers", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RollBackOwners" }, enabled = true)
	public void OwnershipAndTransfer_VerifyRollbackActive_RollBackRetire(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");

		String ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData,
				"DataToCreateOwnershipRecord");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil
				.generateMapFromJsonFile(ownershipCreationData, "dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData1 = objUtil
				.generateMapFromJsonFile(ownershipCreationData, "dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData2 = objUtil
				.generateMapFromJsonFile(ownershipCreationData, "dataToCreateGranteeWithCompleteOwnership");

		String recordedDocumentID = salesforceAPI
				.select("SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=1")
				.get("Id").get(0);

		objCioTransfer.deleteRecordedApnFromRecordedDocument(recordedDocumentID);

		// STEP 1-login with SYS-ADMIN

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(EFILE_INTAKE_VIEW);
		objCioTransfer.addRecordedApn(recordedDocumentID, 1);

		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);
		objCioTransfer.generateRecorderJobWorkItems(recordedDocumentID);

		// STEP 2-Query to fetch WI

		String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 2";
		Thread.sleep(3000);
		String workItemNo1 = salesforceAPI.select(workItemQuery).get("Name").get(0);
		String workItemNo2 = salesforceAPI.select(workItemQuery).get("Name").get(1);

		objMappingPage.globalSearchRecords(workItemNo1);
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
		
		if(!objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.getButtonWithText("New")))
		driver.navigate().refresh();

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

		objMappingPage.logout();
		Thread.sleep(4000);

		// STEP 5-Login with CIO staff

		objMappingPage.login(loginUser);
		
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo1 + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

		// STEP 6-Finding the first recorded apn transfer id
		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];

		// deleting the CIO Transfer grantees for the first transfer screen
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);

		queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + workItemNo2 + "'";
		navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);
		String recordeAPNTransferID1 = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];

		// deleting the CIO Transfer grantees for the second transfer screen
		objCioTransfer.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID1);

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
		
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 7-Clicking on related action link and navigating to first transfer
		// screen

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// STEP 8-Creating the new grantee

		hashMapOwnershipAndTransferGranteeCreationData.put("Owner Percentage", "50");
		hashMapOwnershipAndTransferGranteeCreationData.put("First Name", "");
		hashMapOwnershipAndTransferGranteeCreationData.put("Ownership Start Date", "");

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);

		hashMapOwnershipAndTransferGranteeCreationData1.put("Owner Percentage", "50");
		hashMapOwnershipAndTransferGranteeCreationData1.put("Last Name", "Johnathon Johnson");
		hashMapOwnershipAndTransferGranteeCreationData1.put("First Name", "");
		hashMapOwnershipAndTransferGranteeCreationData1.put("Ownership Start Date", "");

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData1);

		// STEP 9-Navigating back to RAT screen and updatiung transfer code

		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		ReportLogger.INFO("Updating the transfer code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-SALE");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		ReportLogger.INFO("transfer code updated successfully");

		// STEP 10-Clicking on submit for approval quick action button
		driver.navigate().refresh();
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.transferCodeLabel);
		
		String dovCIOScreen=objMappingPage.getFieldValueFromAPAS(objCioTransfer.dovLabel);
		String dorCIOScreen=objMappingPage.getFieldValueFromAPAS(objCioTransfer.dorLabel);
		
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		
		
		if (objCioTransfer.waitForElementToBeVisible(7,objCioTransfer.yesRadioButtonRetainMailToWindow))
		{	
		objCioTransfer.Click(objCioTransfer.yesRadioButtonRetainMailToWindow);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		}
		
		objCioTransfer.waitForElementToBeVisible(objCioTransfer.confirmationMessageOnTranferScreen);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");
		

		// Verify that User is able to launch CIO Transfer Activity from business event triggered by Transfer Code update
		
		String auditTrailId =salesforceAPI.select("SELECT id FROM Transaction_Trail__c where Event_Library__c in (select id from Event_Library__c  where name='CIO-SALE') order by createddate desc limit 1").get("Id").get(0);
		 
		driver.navigate().to("https://smcacre--" + execEnv
					+ ".lightning.force.com/lightning/r/Transaction_Trail__c/" + auditTrailId + "/view");
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(trail.relatedActionLabel),"Click Here",
				"SMAB-T3430: Verify related action link name on Business event page generated by trabsfer code update is 'Click Here' ");
		
		softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondenceLabel), parentAuditTrailNumber,
				"SMAB-T3384: Verifying that business event created by transfer code update is child of parent Recorded correspondence event");

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedBusinessEventLabel), "",
				"SMAB-T3384:Verifying that related business event field in business event created by transfer code update  is blank");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel), eventId,
				"SMAB-T3384:Verifying that Event ID field in the business event created by transfer code update detail page should be inherited from parent correspondence event");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel), requestOrigin,
				"SMAB-T3384:Verifying that business event created by transfer code update inherits the Request Origin from parent event");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.dovLabel), dovCIOScreen,
				"SMAB-T3384:Verifying that business event created by transfer code update inherits the DOV from recorded document");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.eventLibraryLabel), "CIO-SALE",
				"SMAB-T3384:Verifying that business event created by transfer code update has event library value as the event code entered in transfer screen");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.statusLabel), "Open",
				"SMAB-T3384:Verifying that business event created by transfer code update has status as open");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.dorLabel), dorCIOScreen,
				"SMAB-T3384:Verifying that business event created by transfer code update inherits the inherits the DOR from recorded document");
		
		
		objCioTransfer.Click(objWorkItemHomePage.reviewLink);

		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		/*objCioTransfer.waitForElementToBeVisible(30,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));*/
		Thread.sleep(5000);
		
		String urlForRATScreen = driver.getCurrentUrl();
		String  RATId=urlForRATScreen.split("/")[6];

		softAssert.assertEquals(RATId,recordeAPNTransferID,
				"SMAB-T3430: Verify that User is able to launch CIO Transfer Activity from business event triggered by Transfer Code update");
		
		// Step 11: CIO supervisor now logs in and navigates to the first transfer
		// screen
		// and returns it
		objCioTransfer.logout();
		Thread.sleep(4000);
		objCioTransfer.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		/*objCioTransfer.waitForElementToBeVisible(30,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));*/
		Thread.sleep(5000);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.waitForElementToBeClickable(objCioTransfer.quickActionOptionReturn);
		objCioTransfer.Click(objCioTransfer.quickActionOptionReturn);
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.returnReasonTextBox);
		objCioTransfer.enter(objCioTransfer.returnReasonTextBox, "return by CIO supervisor");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementTextToBe(objCioTransfer.confirmationMessageOnTranferScreen,
				"Work Item has been returned by the approver.", 30);
		softAssert.assertEquals(objCioTransfer.getElementText(objCioTransfer.confirmationMessageOnTranferScreen),
				"Work Item has been returned by the approver.",
				"SMAB-T3510: Validation that proper mesage is displayed after WI is returned by CIO supervisor");

		objCioTransfer.Click(objCioTransfer.finishButtonPopUp);
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 6);
		ReportLogger.INFO("CIO!! Transfer Returned to staff");
		objCioTransfer.logout();
		Thread.sleep(4000);

		// Step 12: CIO staff Navigating to second transfer screen for same APN
		objCioTransfer.login(users.CIO_STAFF);
		objMappingPage.globalSearchRecords(workItemNo2);
		objMappingPage.waitForElementToBeVisible(30, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// STEP 13-Clicking on related action link

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// STEP 14-Creating the new grantee for second transfer screen

		hashMapOwnershipAndTransferGranteeCreationData2.put("Owner Percentage", "25");
		hashMapOwnershipAndTransferGranteeCreationData2.put("Last Name", "Sandra Jacob");
		hashMapOwnershipAndTransferGranteeCreationData2.put("First Name", "");
		hashMapOwnershipAndTransferGranteeCreationData2.put("Ownership Start Date", "");

		objCioTransfer.createNewGranteeRecords(recordeAPNTransferID1, hashMapOwnershipAndTransferGranteeCreationData2);

		// Step 15: CIO staff user navigating to second transfer screen by clicking on
		// related action link
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID1 + "/view");
		objCioTransfer.waitForElementToBeVisible(15,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);

		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "50");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.nextButton);

		objCioTransfer.enter(objCioTransfer.calculateOwnershipRetainedFeld, "25");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));

		// Step 16: submitting the WI for approval

		ReportLogger.INFO("Updating the transfer code");
		objCioTransfer.editRecordedApnField(objCioTransfer.transferCodeLabel);
		objCioTransfer.waitForElementToBeVisible(6, objCioTransfer.transferCodeLabel);
		objCioTransfer.searchAndSelectOptionFromDropDown(objCioTransfer.transferCodeLabel, "CIO-SALE");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		ReportLogger.INFO("transfer code updated successfully");

		ReportLogger.INFO("Submitting the WI for approval");
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID1 + "/view");
		driver.navigate().refresh();
	/*	objCioTransfer.waitForElementToBeVisible(40,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));*/
		Thread.sleep(10000);
		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		
		if (objCioTransfer.waitForElementToBeVisible(7,objCioTransfer.yesRadioButtonRetainMailToWindow))
		{	
		objCioTransfer.Click(objCioTransfer.yesRadioButtonRetainMailToWindow);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		}
		
		objCioTransfer.waitForElementToBeVisible(30, objCioTransfer.confirmationMessageOnTranferScreen);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 6);

		// step 17 : CIO staff user navigating to first transfer screen and performing
		// roll back
		// operations
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/" + recordeAPNTransferID
				+ "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
		if(!objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.newButton))
			driver.navigate().refresh();
		objCioTransfer.Click(driver.findElement(By.xpath(objCioTransfer.xpathShowMoreLinkForEditOption
				.replace("propertyName", hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase()))));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.editLinkUnderShowMore);
		objCioTransfer.Click(objCioTransfer.editLinkUnderShowMore);
		objCioTransfer.waitForElementToBeVisible(7, objCioTransfer.saveButtonModalWindow);
		objCioTransfer.enter(objCioTransfer.dovLabel, "7/19/2001");
		objCioTransfer.selectOptionFromDropDown(objCioTransfer.Status, "Rolled Back - Retired");
		objCioTransfer.selectOptionFromDropDown(objCioTransfer.originalTransferor, "Yes");
		objCioTransfer.selectOptionFromDropDown(objCioTransfer.vestingType, "JT");
		objCioTransfer.enter(objCioTransfer.remarksLabel, "New Retired Record after Rolled back Retire");
		objCioTransfer.Click(objCioTransfer.saveButtonModalWindow);

		driver.navigate().refresh();
		if(!objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.newButton))
			driver.navigate().refresh();
		objCioTransfer.Click(driver.findElement(By.xpath(objCioTransfer.xpathShowMoreLinkForEditOption
				.replace("propertyName", hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase()))));
		objCioTransfer.waitForElementToBeVisible(5, objCioTransfer.editLinkUnderShowMore);
		objCioTransfer.Click(objCioTransfer.editLinkUnderShowMore);
		objCioTransfer.waitForElementToBeVisible(7, objCioTransfer.saveButtonModalWindow);
		objCioTransfer.selectOptionFromDropDown(objCioTransfer.Status, "Rolled Back - Active");
		objCioTransfer.enter(objCioTransfer.remarksLabel, "New Active record after Rolled back Active");
		objCioTransfer.Click(objCioTransfer.saveButtonModalWindow);

		ReportLogger.INFO("Submitting the WI for approval");
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCioTransfer.waitForElementToBeVisible(15,
				objCioTransfer.getButtonWithText(objCioTransfer.transferCodeLabel));

		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionSubmitForApproval);
		
		if (objCioTransfer.waitForElementToBeVisible(7,objCioTransfer.yesRadioButtonRetainMailToWindow))
		{	
		objCioTransfer.Click(objCioTransfer.yesRadioButtonRetainMailToWindow);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.nextButton));
		}
		
		objCioTransfer.waitForElementToBeVisible(30, objCioTransfer.confirmationMessageOnTranferScreen);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 6);

		// Step 18: CIO staff now navigating to ownership records page of parcel
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships__r/view");
		objCioTransfer.waitForElementToBeVisible(10,
				objCioTransfer.columnInGrid.replace("columnName", objCioTransfer.ownershipPercentage));
		objCioTransfer.sortInGrid(objCioTransfer.ownershipPercentage, true);
		HashMap<String, ArrayList<String>> HashMapLatestOwner = objCioTransfer.getGridDataInHashMap();

		// STEP 19-Validating the Owners ,their status and ownership percentages
		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0),
				hashMapOwnershipAndTransferGranteeCreationData2.get("Last Name").toUpperCase(),
				"SMAB-T3510:Validating that the grantee that was created from second transfer screen has become  new owner : "
						+ hashMapOwnershipAndTransferGranteeCreationData2.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active",
				"SMAB-T3510: Validating that status of new owner which is the grantee created from second transfer screen is Active : "
						+ hashMapOwnershipAndTransferGranteeCreationData2.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "25.0000%",
				"SMAB-T3510: Validating that Ownership Percentage of new owner which is the grantee created from second transfer screen is correct: "
						+ hashMapOwnershipAndTransferGranteeCreationData2.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(1),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase(),
				"SMAB-T3510:Validating that the grantee that was created from first  transfer screen has become  new owner : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(1), "Active",
				"SMAB-T3510: Validating that status of new owner which is the grantee created from first transfer screen is Active : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(1), "25.0000%",
				"SMAB-T3510: Validating that Ownership Percentage of new owner which is the grantee created from first  transfer screen is correct: "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(2),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase(),
				"SMAB-T3510:Validating that the grantee that was created from first  transfer screen has retired due to second transfer screen "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(2), "Retired",
				"SMAB-T3510: Validating that status of new retired owner which is the grantee created from first transfer screen is retired : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(2), "50.0000%",
				"SMAB-T3510: Validating that Ownership Percentage of new retired owner which is the grantee created from first transfer screen is correct: "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("DOV").get(2), "7/19/2001",
				"SMAB-T3510: Validating that status of new retired owner which is the grantee created from first transfer screen is retired : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Remarks").get(2), "New Retired Record after Rolled back Retire",
				"SMAB-T3510: Validating that remarks of new retired owner which is the grantee created from first transfer screen is correct : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(2), "7/19/2001",
				"SMAB-T3510: Validating that Ownership Start Date of new retired owner which is the grantee created from first transfer screen is updatd DOV entered wile performing roll back operation : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		String queryOwnershipDetails = "SELECT id,Vesting_Type__c,Original_Transferor__c  FROM Property_Ownership__c where DOV_Date__c =2001-07-19 and parcel__c in (Select Id from parcel__C where name='"
				+ apnFromWIPage + "')";
		HashMap<String, ArrayList<String>> responseOwnershipDetails = salesforceAPI.select(queryOwnershipDetails);

		softAssert.assertEquals(responseOwnershipDetails.get("Vesting_Type__c").get(0), "JT",
				"SMAB-T3510: Validating that Vesting_Type of new retired owner which is the grantee created from first transfer screen is retired : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(responseOwnershipDetails.get("Original_Transferor__c").get(0), "Yes",
				"SMAB-T3510: Validating that Original_Transferor of new retired owner which is the grantee created from first transfer screen is correct : "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(3),
				hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase(),
				"SMAB-T3510:Validating  the grantee that was created from first  transfer screen "
						+ hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(3), "Active",
				"SMAB-T3510: Validating that status of new active owner which is the grantee created from first transfer screen is Active : "
						+ hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(3), "50.0000%",
				"SMAB-T3510: Validating that Ownership Percentage of new active owner which is the grantee created from first  transfer screen is correct: "
						+ hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Remarks").get(3), "New Active record after Rolled back Active",
				"SMAB-T3510: Validating that remarks of new active owner which is the grantee created from first transfer screen is correct : "
						+ hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(4), acesseName,
				"SMAB-T3510:Validating  the old original parcel owner " + acesseName);

		softAssert.assertEquals(HashMapLatestOwner.get("Status").get(4), "Retired",
				"SMAB-T3510: Validating that status of old original  owner  is Retired : " + acesseName);

		softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(4), "100.0000%",
				"SMAB-T3510: Validating that Ownership Percentage of old original owner is correct: " + acesseName);

		// Step 20 : CIO staff user navigating to rolled bcak records for parcel
		driver.navigate()
				.to("https://smcacre--"
						+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
								.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
						+ "/related/Property_Ownerships1__r/view");
		
		objCioTransfer.waitForElementToBeVisible(10,
				objCioTransfer.columnInGrid.replace("columnName", objCioTransfer.ownershipPercentage));
		HashMap<String, ArrayList<String>> HashMapRolledBackOwner = objCioTransfer.getGridDataInHashMap();

		softAssert.assertEquals(HashMapRolledBackOwner.get("Owner").get(0),
				hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase(),
				"SMAB-T3510:Validating  the first grantee that was created from first  transfer screen is now rolled back "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapRolledBackOwner.get("Status").get(0), "Rolled Back",
				"SMAB-T3510: Validating that status of the first grantee that was created from first  transfer screen is now rolled back "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapRolledBackOwner.get("Ownership Percentage").get(0), "50.0000%",
				"SMAB-T3510: Validating that Ownership Percentage of rolled back owner which is the first grantee created from first  transfer screen is correct after roll back: "
						+ hashMapOwnershipAndTransferGranteeCreationData.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapRolledBackOwner.get("Owner").get(1),
				hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase(),
				"SMAB-T3510:Validating  the second grantee that was created from first  transfer screen is now rolled back "
						+ hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapRolledBackOwner.get("Status").get(1), "Rolled Back",
				"SMAB-T3510: Validating that status of the second grantee that was created from first  transfer screen is now rolled back "
						+ hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase());

		softAssert.assertEquals(HashMapRolledBackOwner.get("Ownership Percentage").get(1), "50.0000%",
				"SMAB-T3510: Validating that Ownership Percentage of rolled back owner which is the second grantee created from first  transfer screen is correct: "
						+ hashMapOwnershipAndTransferGranteeCreationData1.get("Last Name").toUpperCase());

		objCioTransfer.logout();
		Thread.sleep(4000);

		// Step 21: CIO supervisor now logs in and navigates to the first transfer
		// screen
		// and approves it
		objCioTransfer.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		objCioTransfer.waitForElementToBeVisible(30,
				objCioTransfer.getButtonWithText(objCioTransfer.calculateOwnershipButtonLabel));

		objCioTransfer.Click(objCioTransfer.quickActionButtonDropdownIcon);
		objCioTransfer.Click(objCioTransfer.quickActionOptionApprove);
		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.confirmationMessageOnTranferScreen);
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.finishButtonLabel));
		objCioTransfer.waitForElementToBeInVisible(objCioTransfer.xpathSpinner, 6);

		objCioTransfer.waitForElementToBeVisible(20, objCioTransfer.CIOstatus);
		objCioTransfer.scrollToElement(objCioTransfer.CIOstatus);
		objCioTransfer.waitForElementTextToBe(objCioTransfer.CIOstatus,"Approved",30);
		softAssert.assertEquals(objWorkItemHomePage.getElementText(objCioTransfer.CIOstatus), "Approved",
				"SMAB-T3510: Validating CIO Transfer activity status on transfer activity screen after approved by supervisor.");

		objCioTransfer.logout(); 

	}
	
	/*
	 * This method is to Validate Roll entry record fields.
	 * 
	 * @param loginUser
	 * 
	 * @throws Exception
	 */
	@Test(description = "SMAB-T4228, Validate fields on roll entry record", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "RollBackOwners" })
	public void OwnershipAndTransfer_ValidateFieldsOnRollEntryProduct(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Navigating to the Roll Record
		String rollRecordQuery = "SELECT Id FROM Roll_Entry__c limit 1";
		HashMap<String, ArrayList<String>> rollRecordID = salesforceAPI.select(rollRecordQuery);
		String rollID = rollRecordID.get("Id").get(0);

		// Step2: Validate lables on roll entry record
		driver.navigate().to(
				"https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Roll_Entry__c/" + rollID + "/view");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Activity"),
				"SMAB-4228: Activity is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("District"),
				"SMAB-4228: District is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Neighborhood"),
				"SMAB-4228: Neighborhood is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Notice Date"),
				"SMAB-4228: Activity is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Zone"),
				"SMAB-4228: Zone  is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("BC#"),
				"SMAB-4228: BC is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("CPI Factor"),
				"SMAB-4228: CPI Factor	 is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Base Years"),
				"SMAB-4228: Base Years is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Last Transaction"),
				"SMAB-4228: Last Transaction is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("DOR"),
				"SMAB-4228: DOR is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("DOV"),
				"SMAB-4228: DOV is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Approved by"),
				"SMAB-4228: Approved by is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Approved Date"),
				"SMAB-4228: Approved Date is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Temp Land"),
				"SMAB-4228: Temp Land is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Temp Improvement"),
				"SMAB-4228: Temp Improvement is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Total Temp"),
				"SMAB-4228: Total Temp is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Type Number"),
				"SMAB-4228:  Type Number is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Status Number"),
				"SMAB-4228:  Status Number is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Assessment Year"),
				"SMAB-4228: Assessment Year is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Roll Type"),
				"SMAB-4228:  Roll Type is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Event ID"),
				"SMAB-4228:  Event ID is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Temp Code"),
				"SMAB-4228:  Temp Code is present on roll entry object");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Temp Date"),
				"SMAB-4228:  Temp Date is present on roll entry object");
		ReportLogger.INFO("Validated RollEntry Record");
		objMappingPage.logout();
		
	}

}
