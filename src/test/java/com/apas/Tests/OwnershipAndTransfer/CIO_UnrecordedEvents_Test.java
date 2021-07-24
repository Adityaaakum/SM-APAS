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
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
		
		// Step5: Update APN field with active APN and validate Warning message disappears
		objParcelsPage.Click(objCIOTransferPage.editFieldButton("APN"));
		objCIOTransferPage.clearSelectionFromLookup("APN");
		
		objCIOTransferPage.searchAndSelectOptionFromDropDown("APN", activeApn);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText("Save"));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
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
		
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id FROM PUC_Code__c where Name = '105 - Apartment' limit 1");
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
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
		
		objCIOTransferPage.logout();
	}
}