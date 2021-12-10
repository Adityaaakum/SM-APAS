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
	

	@Test(description = "SMAB-T3698,SMAB-T3699,SMAB-T23708:Verify audit trail update with Parcel Transfer Allowed value", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void ParcelManagement_EventLibraray_ParcelTransfer_Allowed_BusinessEvent(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

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
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "No");

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
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "Yes");

		// Logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		// Login as Mapping supervisor
		objMappingPage.login(users.MAPPING_SUPERVISOR);

		// Navigating to audit trail and updating the value of Related Correspondence field	
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrail1ID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Related Business Event");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Related Business Event"));
		objParcelsPage.clearSelectionFromLookup("Related Business Event");
		objParcelsPage.searchAndSelectOptionFromDropDown("Related Business Event", auditTrail2);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		String value = objParcelsPage.getFieldValueFromAPAS("Related Business Event");

		// Verify when parcel transfer allowed is "Yes" the audit trail can be linked to audit trail with another parcel
		softAssert.assertEquals(value, auditTrail2,
				"SMAB-T3708: Verify when parcel transfer allowed is \"Yes\" the audit trail can be linked to audit trail with another parcel");

		// Logout
		objParcelsPage.logout();

	}

	@Test(description = "SMAB-T3698,SMAB-T3699,SMAB-T23708:Verify audit trail update with Parcel Transfer Allowed value", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void ParcelManagement_EventLibraray_ParcelTransfer_Allowed_Correspondence(String loginUser)
			throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

		String executionEnv = System.getProperty("region");
		
		// fetching audit trail records
		String queryAuditTrail1 = "SELECT  Name, Parcel__c FROM Transaction_Trail__c WHERE Event_Type__c='Correspondence Received - Mapping'"
				+ " and parcel__c!=NULL ";
		HashMap<String, ArrayList<String>> responseAuditTrailDetails = salesforceAPI.select(queryAuditTrail1);
		String apnId1 = responseAuditTrailDetails.get("Parcel__c").get(0);
		String queryAuditTrail2 = "SELECT  Name, Parcel__c FROM Transaction_Trail__c WHERE Event_Type__c='Correspondence Received - Mapping'"
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
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "No");

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
		objParcelsPage.selectOptionFromDropDown("Parcel Transfer Allowed", "Yes");

		// logout
		objParcelsPage.logout();
		Thread.sleep(5000);

		// Login as mapping supervisor
		objMappingPage.login(users.MAPPING_SUPERVISOR);

		// Navigating to audit trail and updating the value of Related Correspondence field
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Transaction_Trail__c/"
				+ auditTrail1ID + "/view");
		objParcelsPage.waitForElementToBeVisible(10, "Related Correspondence");
		objParcelsPage.Click(objParcelsPage.editFieldButton("Related Correspondence"));
		objParcelsPage.clearSelectionFromLookup("Related Correspondence");
		objParcelsPage.searchAndSelectOptionFromDropDown("Related Correspondence", auditTrail2);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		String value = objParcelsPage.getFieldValueFromAPAS("Related Correspondence");

		// Verify when parcel transfer allowed is "Yes" the audit trail can be linked to audit trail with another parcel
		softAssert.assertEquals(value, auditTrail2,
				"SMAB-T3708: Verify when parcel transfer allowed is \"Yes\" the audit trail can be linked to audit trail with another parcel");

		// Logout
		objParcelsPage.logout();

	}
}