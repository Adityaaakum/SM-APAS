package com.apas.Tests.ParcelManagement;

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
import com.apas.PageObjects.ApasGenericPage;
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

public class Parcel_AuditTrail_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	String auditTrailData;
	AuditTrailPage objTrailPage;
	ApasGenericPage objApasGenericPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		auditTrailData = testdata.AUDIT_TRAIL_DATA;
		objTrailPage=new AuditTrailPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
	}

	@Test(description = "SMAB-T3700:Verify that user is able to create audit trail and linkage relationship should be created having EventID populated", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void Parcel_AuditTrailUpdatesViaComponentActions(String loginUser) throws Exception {

		String activeApn = objParcelsPage.fetchActiveAPN();
		Map<String, String> dataToCreateAuditTrailRecord = objUtil.generateMapFromJsonFile(auditTrailData,
				"DataToCreateAuditTrail");
		
		Map<String, String> dataToCreateAuditTrailRecordToLinkWithParent = objUtil.generateMapFromJsonFile(auditTrailData,
				"DataToCreateAuditTrailToLinkWithParent");

		// Step 1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step 2: Opening the PARCELS page
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);

		// Step 3: Create audit Trail
		objParcelsPage.createUnrecordedEvent(dataToCreateAuditTrailRecord);

		//Step 4: Add the Event Id and get Event Title value
		String eventID = "1234Test";
		objParcelsPage.Click(objParcelsPage.editFieldButton("Event ID"));
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Event ID"), eventID);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		

		// Step 5: Opening the PARCELS page
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);

		// Step 6: Create audit trail to link with parent audit trail
		objParcelsPage.createUnrecordedEvent(dataToCreateAuditTrailRecordToLinkWithParent);

		String eventIdChild = objParcelsPage.getFieldValueFromAPAS("Event ID");

		//Step 7: Verify that the audit trail is linked to parent audit trail as Event Id would be auto populated
		softAssert.assertEquals(eventIdChild, eventID,
				"SMAB-T3700: Verify that user is able to create audit trail and linkage relationship should be created having EventID populated");

		//Step 8 : Logout
		objParcelsPage.logout();
	}
	
	/**
	 * This method is to Verify that audit trail is created in Characteristic and business event with linkage record created"
	 * @param loginUser
	 * @throws Exception
	 */	
	
	@Test(description = "SMAB-T3702,SMAB-T3703:Verify that audit trail is created in Characteristica nd business event with linkage record created", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void ParcelManagement_AudiTrail_Characteristics(String loginUser)
			throws Exception {

		String executionEnv = System.getProperty("region");
		String queryAPN = "Select Name,Id  From Parcel__c where name like '0%' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);
		String apnId = responseAPNDetails.get("Id").get(0);

		ReportLogger.INFO("Delete characteristic record");
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);


		String mappingActionCreationData = testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String characteristicsRecordCreationData = testdata.CHARACTERISTICS;
		Map<String, String> hashMapImprovementCharacteristicsData = objUtil
				.generateMapFromJsonFile(characteristicsRecordCreationData, "DataToCreateImprovementCharacteristics");

		// Adding Characteristic record in the parcel
		ReportLogger.INFO("Adding Characteristic record in the parcel");
		objMappingPage.login(users.SYSTEM_ADMIN);

		driver.navigate().to(
				"https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/" + apnId + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
				objParcelsPage.getButtonWithText(objParcelsPage.parcelMapInGISPortal));

		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapImprovementCharacteristicsData, apn);

		objMappingPage.logout();
		Thread.sleep(4000);
		
		// Step1: Login to the APAS application using the credentials passed through
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform one to
		// one mapping
		driver.navigate().to(
				"https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/" + apnId + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
				objParcelsPage.getButtonWithText(objParcelsPage.parcelMapInGISPortal));

		// Step 3: Creating Manual work item for the Parcel

		String WorkItemNo = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link

		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		// Clicking on Action Dropdown

		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,
				hashMapOneToOneMappingData.get("Are taxes fully paid?"));
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapOneToOneMappingData.get("Reason code"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);

		// Fetching the GRID data

		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
		String childApn = gridDataHashMap.get("APN").get(0);

		// Clicking on generate parcel button

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		Thread.sleep(5000);

		// Completing the work Item
		ReportLogger.INFO("Completing the work Item");
		String queryWI = "Select Id from Work_Item__c where Name = '" + WorkItemNo + "'";
		HashMap<String, ArrayList<String>> responseWI = salesforceAPI.select(queryWI);
		driver.switchTo().window(parentWindow);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
				+ responseWI.get("Id").get(0) + "/view");
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.appLauncher);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
	   	objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);		
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		ReportLogger.INFO("mapping supervisor login");
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
				+ responseWI.get("Id").get(0) + "/view");
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.appLauncher);
		objWorkItemHomePage.completeWorkItem();
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.linkedItemsWI);
		Thread.sleep(2000);
		
		ReportLogger.INFO("Navigate to child Apn");
		String query = "Select Id from Parcel__c where Name = '" + childApn + "'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
				+ response.get("Id").get(0) + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
				objParcelsPage.getButtonWithText(objParcelsPage.parcelMapInGISPortal));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.Click(objParcelsPage.fetchCharacteristicsList().get(0));
		ReportLogger.INFO("Verify Audit trail is displayed");
		objParcelsPage.waitForElementToBeVisible(objTrailPage.businessEventCharacteristicsAuditTrail, 10);
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objTrailPage.businessEventCharacteristicsAuditTrail),
				"SMAB-T3702: Verify that When a mapping work item is completed, Audit Trail component should be displayed at the characteristic level");

		objParcelsPage.Click(objTrailPage.businessEventCharacteristicsAuditTrail);
		objParcelsPage.waitForElementToBeVisible(objTrailPage.relatedBusinessRecords);
		objParcelsPage.Click(objTrailPage.relatedBusinessRecords);
		ReportLogger.INFO("verify Linked record is visible");
		objWorkItemHomePage.waitForElementToBeVisible(20, objTrailPage.linkedRecord);
		String trailSubject = objTrailPage.linkedRecord.getText();
		softAssert.assertEquals(trailSubject, "Linked Record",
				"SMAB-T3703: Verified that Related Businnes Events displays Linked Record");

		objWorkItemHomePage.logout();
	}
	
	/**
	 Verify audit trail update with Parcel Transfer Allowed value for business event audit trail
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3698,SMAB-T3699,SMAB-T3708:Verify audit trail update with Parcel Transfer Allowed value for business event audit trail", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void ParcelManagement_EventLibraray_ParcelTransfer_Allowed_BusinessEvent(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);

		String executionEnv = System.getProperty("region");
		// fetching audit trail records
		String queryAuditTrail1 = "SELECT Id, Name, Parcel__c FROM Transaction_Trail__c WHERE Event_Type__c='Combined Performed'"
				+ " and parcel__c!=NULL ";
		HashMap<String, ArrayList<String>> responseAuditTrailDetails = salesforceAPI.select(queryAuditTrail1);
		String apnId1 = responseAuditTrailDetails.get("Parcel__c").get(0);
		String queryAuditTrail2 = "SELECT Id, Name, Parcel__c FROM Transaction_Trail__c WHERE Event_Type__c='Combined Performed'"
				+ " and parcel__c!=NULL and parcel__c!='" + apnId1 + "'";

		HashMap<String, ArrayList<String>> responseAuditTrailDetails2 = salesforceAPI.select(queryAuditTrail2);
		String auditTrail1 = responseAuditTrailDetails.get("Name").get(0);
		String auditTrail2 = responseAuditTrailDetails2.get("Name").get(0);
		
		// Navigating to event library and updating field parcel Transfer allowed
		String Eventlib="Combined Performed";
		String queryEventLibraryID = "Select Id from Event_Library__c where Name = '" + Eventlib + "'";	
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Event_Library__c/"
				+ queryEventLibraryID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Parcel Transfer Allowed");
		
		ReportLogger.INFO("Verify parecl Transfer Allowed field is visible");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Parcel Transfer Allowed"),
				"SMAB-T3698: Validate that field name Parcel Transfer Allowed exists");

		// set value of parcel Transfer allowed to "No"
		objParcelsPage.Click(objParcelsPage.editFieldButton("Parcel Transfer Allowed"));
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "No");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		// Logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		ReportLogger.INFO("Login as mapping supervisor");
		objMappingPage.login(users.MAPPING_SUPERVISOR);

		// Navigate to audit trail record and editing the Related Business Event field
		String auditTrail1ID = responseAuditTrailDetails.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrail1ID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Related Business Event");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Related Business Event"));
		objParcelsPage.clearSelectionFromLookup("Related Business Event");
		objParcelsPage.searchAndSelectOptionFromDropDown("Related Business Event", auditTrail2);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		String ExpectedErrorMessage = "The Audit Trail record cannot be associated to an audit trail not associated to this parcel. If needed, please contact an admin to update the associated Event Library record.";
		
		// Verify when parcel transfer allowed is "No" the audit trail cannot be linked  to audit trail with another parcel
		softAssert.assertEquals(objParcelsPage.getElementText(objApasGenericPage.pageError), ExpectedErrorMessage,
				"SMAB-T3699: Verify when parcel transfer allowed is \"No\" the audit trail cannot be linked to audit trail with another parcel");

       //Logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		// Login as System admin
		objMappingPage.login(users.SYSTEM_ADMIN);

		// Navigating to event library and changing the value of parcel Transfer allowed to "yes"	
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Event_Library__c/"
				+ queryEventLibraryID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Parcel Transfer Allowed");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Parcel Transfer Allowed"));
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "Yes");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		// Logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		// Login as Mapping supervisor
		objMappingPage.login(users.MAPPING_STAFF);

		// Navigating to audit trail and updating the value of Related Correspondence field	
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrail1ID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Related Business Event");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Related Business Event"));
		objParcelsPage.clearSelectionFromLookup("Related Business Event");
		objParcelsPage.searchAndSelectOptionFromDropDown("Related Business Event", auditTrail2);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		String linkedauditTrailvalue = objParcelsPage.getFieldValueFromAPAS("Related Business Event");

		// Verify when parcel transfer allowed is "Yes" the audit trail can be linked to audit trail with another parcel
		softAssert.assertEquals(linkedauditTrailvalue, auditTrail2,
				"SMAB-T3708: Verify when parcel transfer allowed is \"Yes\" the audit trail can be linked to audit trail with another parcel");

		// Logout
		objParcelsPage.logout();

	}

	
	/**
	 Verify audit trail update with Parcel Transfer Allowed value for correspondence audit trail
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3698,SMAB-T3699,SMAB-T3708:Verify audit trail update with Parcel Transfer Allowed value for correspondence audit trail", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void ParcelManagement_EventLibraray_ParcelTransfer_Allowed_Correspondence(String loginUser)
			throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);

		String executionEnv = System.getProperty("region");
		
		// fetching audit trail records
		String queryAuditTrail1 = "SELECT  Id, Name, Parcel__c FROM Transaction_Trail__c WHERE Event_Type__c='Correspondence Received - Mapping'"
				+ " and parcel__c!=NULL ";
		HashMap<String, ArrayList<String>> responseAuditTrailDetails = salesforceAPI.select(queryAuditTrail1);
		String apnId1 = responseAuditTrailDetails.get("Parcel__c").get(0);
		String queryAuditTrail2 = "SELECT Id, Name, Parcel__c FROM Transaction_Trail__c WHERE Event_Type__c='Correspondence Received - Mapping'"
				+ " and parcel__c!=NULL and parcel__c!='" + apnId1 + "'";

		HashMap<String, ArrayList<String>> responseAuditTrailDetails2 = salesforceAPI.select(queryAuditTrail2);
		String auditTrail1 = responseAuditTrailDetails.get("Name").get(0);
		String auditTrail2 = responseAuditTrailDetails2.get("Name").get(0);

		// Navigating to event library and updating field parcel Transfer allowed
		String Eventlib="Correspondence Received - Mapping";
		String queryEventLibraryID = "Select Id from Event_Library__c where Name = '" + Eventlib + "'";	
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Event_Library__c/"
				+ queryEventLibraryID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Parcel Transfer Allowed");


		ReportLogger.INFO("Verify parecl Transfer Allowed field is visible");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Parcel Transfer Allowed"),
				"SMAB-T3698: Validate that field name Parcel Transfer Allowed exists");

		// set value of parcel Transfer allowed to "No"
		objParcelsPage.Click(objParcelsPage.editFieldButton("Parcel Transfer Allowed"));
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "No");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		// Logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		ReportLogger.INFO("Login as mapping supervisor");
		objMappingPage.login(users.MAPPING_SUPERVISOR);

		// Navigate to audit trail record and editing the Related correspondence field
		String auditTrail1ID = responseAuditTrailDetails.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrail1ID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Related Correspondence");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Related Correspondence"));
		objParcelsPage.clearSelectionFromLookup("Related Correspondence");
		objParcelsPage.searchAndSelectOptionFromDropDown("Related Correspondence", auditTrail2);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		// Verify when parcel transfer allowed is "No" the audit trail cannot be linked to audit trail with another parcel
		String ExpectedErrorMessage = "The Audit Trail record cannot be associated to an audit trail not associated to this parcel. If needed, please contact an admin to update the associated Event Library record.";
		softAssert.assertEquals(objParcelsPage.getElementText(objApasGenericPage.pageError), ExpectedErrorMessage,
				"SMAB-T3699: Verify when parcel transfer allowed is \"No\" the audit trail cannot be linked to audit trail with another parcel");

		// Logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		// Login as system admin
		objMappingPage.login(users.SYSTEM_ADMIN);

		// Navigating to event library and changing the value of parcel Transfer allowed to "yes"
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Event_Library__c/"
				+ queryEventLibraryID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Parcel Transfer Allowed");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Parcel Transfer Allowed"));
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "Yes");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		// logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		// Login as mapping supervisor
		objMappingPage.login(users.MAPPING_STAFF);

		// Navigating to audit trail and updating the value of Related Correspondence field
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrail1ID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Related Correspondence");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Related Correspondence"));
		objParcelsPage.clearSelectionFromLookup("Related Correspondence");
		objParcelsPage.searchAndSelectOptionFromDropDown("Related Correspondence", auditTrail2);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		String linkedauditTrailvalue = objParcelsPage.getFieldValueFromAPAS("Related Correspondence");

		// Verify when parcel transfer allowed is "Yes" the audit trail can be linked to audit trail with another parcel
		softAssert.assertEquals(linkedauditTrailvalue, auditTrail2,
				"SMAB-T3708: Verify when parcel transfer allowed is \"Yes\" the audit trail can be linked to audit trail with another parcel");

		// Logout
		objParcelsPage.logout();

	}
}