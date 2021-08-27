package com.apas.Tests.ParcelCharacteristics;


import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
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

public class Parcel_Management_Parcel_Characteristics_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject = new JSONObject();
	String apnPrefix = new String();
	ReportsPage objReportsPage;
	ApasGenericPage objApasGenericPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		objReportsPage = new ReportsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
	}

	@Test(description = "SMAB-T2892,SMAB-T2893,SMAB-T2894:Verify Characteristic, Associated Parcel Attributes and Start date validation", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelCharacteristics" })
	public void ParcelCharacteristics_ValidationStartDate_And_AssociatedAttributes(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

		// Step 2: Fetch the APN
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 4: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);

		// Step 5: Create New Characteristic
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));

		// Step 6: Verify that field name Property Type exists
		softAssert.assertEquals(objParcelsPage.verifyElementVisible("Property Type"), true,
				"SMAB-T2893: Validate that field name Property Type exists");

		// Step 7: Verify that field name Characteristics Screen exists
		softAssert.assertEquals(objParcelsPage.verifyElementVisible("Characteristics Screen"), true,
				"SMAB-T2893: Validate that field name Characteristics Screen exists");

		// Step 8: Verify that field name Retired Characteristics exists
		softAssert.assertEquals(objParcelsPage.verifyElementVisible("Retired Characteristics"), true,
				"SMAB-T2892: Validate that field name Retired Characteristics exists");

		// Step 9: Enter the values of required fields
		objParcelsPage.selectOptionFromDropDown("Property Type", "Residential");
		objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "SFR");
		objParcelsPage.selectOptionFromDropDown("Reason", "New Appraisable Event");

		// Step 10: Clear the end date field and enter the value
		objParcelsPage.clearFieldValue("End Date");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("End Date"), "2/3/2021");

		// Step 11: Click on Save
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		String ExpectedErrorMessage = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nThe \"Start Date\" cannot be later than the \"End Date\"\"\nReview the following fields\nStart Date";

		// Step 12: Verify that start date cannot be later then end date
		softAssert.assertEquals(objParcelsPage.getElementText(objApasGenericPage.pageError), ExpectedErrorMessage,
				"SMAB-T2894: Validating if validation rule exists for start date cannot be later then end date");

		// Step 13: After every validations ,click on cancel and logout
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Cancel"));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.tabDetails);
		objParcelsPage.logout();
	}

	@Test(description = "SMAB-T2888,SMAB-T2890:Verify Land Characteristic Validation", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelCharacteristics" })
	public void Parcel_LandCharacteristic_Validation(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);
		
		// Step 2: Fetch the APN
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		String ExpectedErrorMessage = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nOnly one land characteristic record is allowed per parcel, if you need to make any updates to the land record, please reference the following record ID\n";
		
		// Step 4: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		
		// Step 5: Create New Characteristic of Land Type and click on save
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
		objParcelsPage.selectOptionFromDropDown("Property Type", "Land");
		objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "Land");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		
		//If on creating new land characteristic the error message comes it means there was pre existing land characteristic and hence perform the validation
		//else as the new record has been created above , on creating new one again the validation can be performed
		
		if (objParcelsPage.verifyElementVisible(objApasGenericPage.pageError)) {
			String[] msg = objParcelsPage.getElementText(objApasGenericPage.pageError).split("/");
			String ActualErrorMessage = msg[0].trim();

			// Step 6: Validate that on Creating new Land characteristic it will display warning if there is pre existing land record
			softAssert.assertEquals(ActualErrorMessage.toString(), ExpectedErrorMessage.trim().toString(),
					"SMAB-T2888,SMAB-T2890: Validating that on Creating new Land characteristic it will display warning if there is pre existing land record");

		} else {
			objMappingPage.globalSearchRecords(apn);
			objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
			objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
			objParcelsPage.selectOptionFromDropDown("Property Type", "Land");
			objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "Land");
			objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
			Thread.sleep(2000);
			String[] msg = objParcelsPage.getElementText(objApasGenericPage.pageError).split("/");
			String ActualErrorMessage = msg[0].trim();

			// Step 6: Validate that on Creating new Land characteristic it will display warning if there is pre existing land record
			softAssert.assertEquals(ActualErrorMessage.toString(), ExpectedErrorMessage.trim().toString(),
					"SMAB-T2888,SMAB-T2890: Validating that on Creating new Land characteristic it will display warning if there is pre existing land record");
		}

		// Step 7: After every validations ,click on cancel and logout
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Cancel"));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.tabDetails);
		objParcelsPage.logout();
	}
	
	@Test(description = "SMAB-T3112,SMAB-T3113:Verify Notes and Attachments can be created for characteristics", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelCharacteristics" })
	public void Parcel_Characteristics_Notes_And_Attachments(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

		// Step 2: Fetch the APN
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 4: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		if (objParcelsPage.fetchCharacteristicsList().isEmpty()) {
			
			// Step 5: Create New Characteristic
			objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));

			// Step 6: Enter the values of required fields
			objParcelsPage.selectOptionFromDropDown("Property Type", "Residential");
			objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "SFR");
			objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
			objMappingPage.globalSearchRecords(apn);
			objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
			objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchCharacteristicsList().get(0));
		}

		//Step 7: Click on first characteristic record
		objParcelsPage.Click(objParcelsPage.fetchCharacteristicsList().get(0));
		
		//Step 8: open Notes tab of the characteristic
		objParcelsPage.openTab("Notes");
		
		//Step 9: Create new Note and click on Done to save the note
		objParcelsPage.OpenNewEntryFormFromRightHandSidePanel("Notes");
		objParcelsPage.Click(objParcelsPage.notes);
		objParcelsPage.enter(objParcelsPage.notes, "TestData");
		objParcelsPage.Click(objParcelsPage.getButtonWithTextForSidePanels("Done"));
		driver.navigate().refresh();
		objParcelsPage.openTab("Notes");
		ReportLogger.INFO("Validate the Note is created");
		//Step 10: Verify that the Note has been created
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.sidePanelNotesList("TestData")),
				"SMAB-3112: Validating that on Creating new Notes in Characteristics , Notes are created");
		driver.navigate().refresh();
		
		//Step 12: Open the Attachment tab of a characteristic
		objParcelsPage.openTab("Attachment");
		
		
		//Step 13: Upload the attachment
		objParcelsPage.ClickUploadButtonOnSidePanel("Notes & Attachments");
		//objParcelsPage.Click(objParcelsPage.uploadFilesButton);
		objParcelsPage.uploadFile(testdata.CHARACTERISTICS_FILE);

		ReportLogger.INFO("Validate the Attachment is uploaded");
		// Step 14: Verify the attachment is Uploaded
		objParcelsPage.waitForElementToBeClickable((objParcelsPage.sideOptionsAttachmentList("00202")));
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.sideOptionsAttachmentList("00202")),
				"SMAB-T3113 : Validating that on Creating new Attachments,it will display Attachments created");
		
		// Close the pop up window opened for attachment on windows
		Robot robot = new Robot();

		robot.keyPress(KeyEvent.VK_ESCAPE);
		robot.keyRelease(KeyEvent.VK_ESCAPE);
		driver.navigate().refresh();
		
		//Step 16: Logout of the application	
		objParcelsPage.logout();
	}

	@Test(description = "SMAB-T2997,SMAB-T2998,SMAB-T2999:Verify Characteristic End date and Reason fields and verifying the validation checks", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelCharacteristics" })
	public void ParcelCharacteristics_ValidationAndReasonFieldAndUpdates(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

		// Step 2: Fetch the APN
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 4: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);

		// Step 5: Create New Characteristic
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));

		ReportLogger.INFO("Validate the Field Retired Characteristcs");
		// Step 8: Verify that field name Retired Characteristics exists
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Retired Characteristics"),
				"SMAB-T2998: Validate that field name Retired Characteristics exists");

		// Step 9: Enter the values of required fields
		objParcelsPage.selectOptionFromDropDown("Property Type", "Residential");
		objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "SFR");

		// Step 10: Clear the end date field and enter the value
		objParcelsPage.clearFieldValue("End Date");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("End Date"), "2/3/2022");

		// Step 11: Click on Save
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		ReportLogger.INFO("Validate the Reason and End Date fields");
		// Step 12: Verify that End date can be entered manually and reason field is
		// mandatory if end date is populated
		softAssert.assertEquals(objParcelsPage.getIndividualFieldErrorMessage("Reason"),
				"Reason field is required if End Date is populated.",
				"SMAB-T2997,SMAB-T2999: Verify that End date can be entered manually and reason field is mandatory if end date is populated");

		// Step 13: After every validations ,click on cancel and logout
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Cancel"));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.tabDetails);
		objParcelsPage.logout();
	}

	@Test(description = "SMAB-T2997,SMAB-T2998,SMAB-T2999:Verify Characteristic End date and Reason fields and verifying the validation checks", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelCharacteristics" })
	public void ParcelCharacteristics_Security(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

		// Step 2: Fetch the APN
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 4: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);

		ReportLogger.INFO("Validate the creation of new characteristic");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("New"),
				"SMAB-T2649: Verify only  System Admin and other users that have rights are able to create Parcel Characteristics");

		// Step 5: Create New Characteristic
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));

		// Step 6: Enter the values of required fields
		objParcelsPage.selectOptionFromDropDown("Property Type", "Residential");
		objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "SFR");

		// Step 7: Click on Save
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchCharacteristicsList().get(0));

		// Step 8: Click on first characteristic record
		objParcelsPage.Click(objParcelsPage.fetchCharacteristicsList().get(0));

		// Step 9: Verify edit button is visible to users have access
		ReportLogger.INFO("Validate the user with access can see edit nutton characteristic");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Edit"),
				"SMAB-T2649: Verify only  System Admin and other users that have rights are able to see edit button Parcel Characteristics");

		// Step 10: click on Edit button
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Edit"));

		// Step 11: Edit the field value
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Tagged Fields"));
		objParcelsPage.clearFieldValue("Tagged Fields");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("Tagged Fields"), "2");

		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		ReportLogger.INFO("Validate the user with access can edit the characteristic record");
		softAssert.assertEquals(objParcelsPage.getFieldValueFromAPAS("Tagged Fields"), 2,
				"SMAB-T2649: Verify only  System Admin and other users that have rights are able to edit Parcel Characteristics");

		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchCharacteristicsList().get(0));

		// Step 12: Click on view all to view list of all characteristics
		objParcelsPage.Click(objParcelsPage.viewAll);
		Thread.sleep(6000);
		int countBefore = objParcelsPage.fetchAllCreatedChar().size();

		// Step 13: Click on action button in characteristic dropdown
		objParcelsPage.Click(objParcelsPage.charDropdown().get(0));
		Thread.sleep(6000);

		// Step 14: Click on Delete button
		objParcelsPage.ClickDeleteCorrespondingDropdown();
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.getPopUpconfirmation("Delete"), 30);

		// Step 15: Click on delete confirmation pop up
		objParcelsPage.Click(objParcelsPage.getPopUpconfirmation("Delete"));
		driver.navigate().refresh();
		Thread.sleep(6000);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchAllCreatedChar().get(0));
		int countAfter = objParcelsPage.fetchAllCreatedChar().size();

		ReportLogger.INFO("Validate sys admin can delete characteristics");
		softAssert.assertEquals(countAfter, countBefore - 1,
				"SMAB-T2648: Verify System Admin can delete Parcel Characteristics");
		objParcelsPage.logout();
		Thread.sleep(5000);

		// Step 16: login as mapping staff user
		objMappingPage.login(MAPPING_STAFF);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 17: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);

		ReportLogger.INFO("Verify mapping staff cannot create new characteristic");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible("New"),
				"SMAB-T2650: Verify Profiles other than System Admin/RP Business Admin/RP Principal/RP Appraiser have read only access to Parcel Characteristics");
		objParcelsPage.logout();

		Thread.sleep(4000);

		// Login as rp admin
		objMappingPage.login(RP_BUSINESS_ADMIN);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchCharacteristicsList().get(0));
		objParcelsPage.Click(objParcelsPage.viewAll);
		Thread.sleep(6000);
		objParcelsPage.Click(objParcelsPage.charDropdown().get(0));
		Thread.sleep(6000);

		ReportLogger.INFO(
				"Validate that user having access to characteristic but cannot delete characteristic as only system admin can delete characteristic");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible("Delete"),
				"SMAB-T2648: Verify System Admin can delete Parcel Characteristics");
		objParcelsPage.logout();
	}

	@Test(description = "SMAB-T2425,SMAB-T2426,SMAB-T2427,SMAB-T2439:Verify Characteristics foundation with SFR", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelCharacteristics" })
	public void ParcelCharacteristics_Foundation_With_SFR(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

		// Step 2: Fetch the Active APN
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		// Step 2: Fetch the Retired APN
		String queryAPN2 = "Select name,ID  From Parcel__c where name like '0%' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails2 = salesforceAPI.select(queryAPN2);
		String apn2 = responseAPNDetails2.get("Name").get(0);

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 4: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);

		ReportLogger.INFO("Validate the creation of new characteristic with active parcel");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("New"),
				"SMAB-T2425: Verify only  System Admin and other users that have rights are able to create Parcel Characteristics for active parcel");

		// Step 5: Create New Characteristic
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));

		// Step 6: Enter the values of required fields
		objParcelsPage.selectOptionFromDropDown("Property Type", "Residential");
		objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "SFR");

		// Step 7: Click on Save
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchCharacteristicsList().get(0));

		// Step 8: Click on first characteristic record
		objParcelsPage.Click(objParcelsPage.fetchCharacteristicsList().get(0));

		ReportLogger.INFO("Validate user can see edit button on Characteristic of active parcel");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Edit"),
				"SMAB-T2427: Validate that user can see edit button for active parcel");

		// Step 9: click on Edit button
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Edit"));
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Tagged Fields"));
		objParcelsPage.clearFieldValue("Tagged Fields");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("Tagged Fields"), "2");

		// Step 10: Click on save
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		ReportLogger.INFO("Validate user can edit Characteristcic of active parcel");
		softAssert.assertEquals(objParcelsPage.getFieldValueFromAPAS("Tagged Fields"), 2,
				"SMAB-T2427: Validate that user can edit characteristic for active parcel");

		ReportLogger.INFO("Validate user can see clone button on Characteristic of active parcel");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Clone"),
				"SMAB-T2426: Validate that user can see clone button for active parcel");

		//Step 11: Click on clone and save button
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Clone"));
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		ReportLogger.INFO("Validate user can clone Characteristcic of active parcel");
		softAssert.assertTrue(!objParcelsPage.getFieldValueFromAPAS("Retired Characteristics").isEmpty(),
				"SMAB-T2426: Validate that user can clone the  characteristic for active parcel");

		objMappingPage.globalSearchRecords(apn2);

		// Step 12: Open the parcel characteristics tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);

		ReportLogger.INFO("Validate the creation of new characteristic with retired parcel");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("New"),
				"SMAB-T2425: Verify only  System Admin and other users that have rights are able to create Parcel Characteristics for retired parcel");

		// Step 13: Create New Characteristic
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));

		// Step 14: Enter the values of required fields
		objParcelsPage.selectOptionFromDropDown("Property Type", "Residential");
		objParcelsPage.selectOptionFromDropDown("Characteristics Screen", "SFR");

		// Step 15: Click on Save
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		objMappingPage.globalSearchRecords(apn2);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchCharacteristicsList().get(0));

		// Step 16: Click on first characteristic record
		objParcelsPage.Click(objParcelsPage.fetchCharacteristicsList().get(0));
		ReportLogger.INFO("Validate user can see edit button on Characteristic of Retired parcel");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Edit"),
				"SMAB-T2427: Validate that user can see edit button for Retired parcel");

		// Step 17: click on edit button
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Edit"));
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Tagged Fields"));
		objParcelsPage.clearFieldValue("Tagged Fields");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("Tagged Fields"), "2");

		//Step 18: Click on Save button
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		ReportLogger.INFO("Validate user can edit Characteristcic of Retired parcel");
		softAssert.assertEquals(objParcelsPage.getFieldValueFromAPAS("Tagged Fields"), 2,
				"SMAB-T2427: Validate that user can edit characteristic for Retired parcel");

		ReportLogger.INFO("Validate user can see clone button on Characteristic of Retired parcel");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible("Clone"),
				"SMAB-T2426: Validate that user can see clone button for Retired parcel");

		objParcelsPage.Click(objParcelsPage.getButtonWithText("Clone"));
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		ReportLogger.INFO("Validate user can clone Characteristcic of Retired parcel");
		softAssert.assertTrue(!objParcelsPage.getFieldValueFromAPAS("Retired Characteristics").isEmpty(),
				"SMAB-T2426: Validate that user can clone the  characteristic for Retired parcel");


		//Step 19: Logout
		objParcelsPage.logout();
		Thread.sleep(20000);
		
		// Step 20: Login as mapping staff
		objMappingPage.login(MAPPING_STAFF);
		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.fetchCharacteristicsList().get(0));
		objParcelsPage.Click(objParcelsPage.viewAll);
		Thread.sleep(6000);

		// Step 21: Click on dropdoen action
		objParcelsPage.Click(objParcelsPage.charDropdown().get(0));
		Thread.sleep(6000);

		ReportLogger.INFO(
				"Validate user who does not have permissions can only view the characteristic record and can't edit or make any changes");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible("Edit"),
				"SMAB-T2439: Validate user who does not have permissions can only view the characteristic record and can't edit or make any changes");

		ReportLogger.INFO(
				"Verify user who does not have permissions can only view the characteristic record and can't Delete or make any changes");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible("Delete"),
				"SMAB-T2439: Validate user who does not have permissions can only view the characteristic record and can't Delete or make any changes");

		//Step 22: Logout
		objParcelsPage.logout();
	}
}
