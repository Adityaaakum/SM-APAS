package com.apas.Tests.ParcelManagement;


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
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class Parcel_Management_Parcel_Characteristics extends TestBase implements testdata, modules, users {
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
			"Regression", "ParcelManagement" })
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
		Object element = "End Date";
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel((String) element), "2/3/2021");

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
			"Regression", "ParcelManagement" })
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
}
