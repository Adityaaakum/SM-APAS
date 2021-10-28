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

public class Parcel_ComponentACtion_AuditTrail_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	String auditTrailData;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		auditTrailData = testdata.AUDIT_TRAIL_DATA;
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
}