package com.apas.Tests.ParcelManagement;

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

public class Parcel_Management_Parcel_Ancestry_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
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

	@Test(description = "SMAB-T3704,SMAB-T3705,SMAB-T3706:Verify Parcel Ancestry Setup", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" ,"ParcelAncestry" })
	public void ParcelAncestry_SetUp(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		// Data Provider
		objMappingPage.login(loginUser);

		// Step 2: Fetch the APN
		String apn = objApasGenericPage.fetchInProgressAPN();

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 4: Verify that tab parcel ancestry exists
				softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.parcelAncestry),
						"SMAB-T3704: Validate that tab parcel ancestry exists");
				
		// Step 5: Open the parcel Ancestry tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelAncestry);

		driver.navigate().refresh();
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelAncestry);

		// Step 6: Verify that header of parcel ancestry exists
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.parcelAncestoryHeader),
				"SMAB-T3705: Validate that header of parcel ancestry exists");

		// Step 7: Verify that ancestry hierarchy field name APN exists
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.ancestoryAPN), 
				"SMAB-T3706: Validate that ancestry hierarchy field name APN exists");

		// Step 8: Verify that ancestry hierarchy field name Reason Code exists
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.ancestoryReasonCode),
				"SMAB-T3706: Validate that ancestry hierarchy field name Reason Code exists");

		// Step 9: Verify that ancestry hierarchy field name Sqft exists
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.ancestorySqft),
				"SMAB-T3706: Validate that ancestry hierarchy field name Sqft exists");

		objParcelsPage.logout();
	}
}



