package com.apas.Tests.ParcelManagement;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
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

public class Parcel_Management_PUC_NewCodes extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();
	ReportsPage objReportsPage;
	ApasGenericPage objApasGenericPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objReportsPage = new ReportsPage(driver);
		objApasGenericPage= new ApasGenericPage(driver);
	}
		
	@Test(description = "SMAB-T3274,SMAB-T3275:Verify Legacy field and PUC Values", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void PUCValueWithLegacyField(String loginUser) throws Exception {
      
		// Step1: Login to the APAS application using the credentials passed through Data Provider
		objMappingPage.login(loginUser);
        Thread.sleep(2000);
        
		// Step2: Opening the PUC's page  and searching the PUC
		objMappingPage.searchModule(PUC);		
		objMappingPage.globalSearchRecords("189 - Residential Miscellaneous");
		
		//Step 3: Fetching the current value of Legacy field
		objWorkItemHomePage.getFieldValueFromAPAS("Legacy");
		
		//Step 4: Changing the value of Legacy field to Yes
		objApasGenericPage.editAndSelectFieldData("Legacy", "Yes");
		
		//Step 5: Navigating to Parcels Page
		objMappingPage.searchModule(PARCELS);
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		objMappingPage.globalSearchRecords(apn);
		
		//Step 6 : Fetching the current value of PUC
		objWorkItemHomePage.getFieldValueFromAPAS("PUC");
		objParcelsPage.Click(objApasGenericPage.editFieldButton("PUC"));
		objMappingPage.clearSelectionFromLookup("PUC");
			
	    //Step 7 : Update value of PUC field for the PUC for which value was set to Yes
	    Object element= "PUC";	    
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel((String) element), "189 - Residential Miscellaneous");
		objApasGenericPage.Click(objApasGenericPage.getButtonWithText("Save"));
	    
		//Step 8: Validating that User cannot find that particular PUC value as legacy field value was yes and error message appears
		softAssert.assertEquals(objApasGenericPage.getIndividualFieldErrorMessage("PUC"), "Select an option from the picklist or remove the search term.", "SMAB-T3275: Validating if Legacy field value is Yes , PUC will not see the value in picklist and will show error message");
		objApasGenericPage.Click(objApasGenericPage.getButtonWithText("Cancel"));
		
		//Step 9 : Navigate back to PUC's page
        objMappingPage.searchModule(PUC);
		
		objMappingPage.globalSearchRecords("189 - Residential Miscellaneous");
		objWorkItemHomePage.getFieldValueFromAPAS("Legacy");
		
		//Step 10: Change value of particular PUC to No
		objApasGenericPage.editAndSelectFieldData("Legacy", "No");
		
		//Step 11: Navigate to Parcels page
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		objWorkItemHomePage.getFieldValueFromAPAS("PUC");
		
		//Step 12: Update the PUC value
		objParcelsPage.Click(objApasGenericPage.editFieldButton("PUC"));
		objMappingPage.clearSelectionFromLookup("PUC");
	    
		//Step 13: Value is now updated and click on save
		objApasGenericPage.searchAndSelectOptionFromDropDown("PUC", "189 - Residential Miscellaneous");
		objApasGenericPage.Click(objApasGenericPage.getButtonWithText("Save"));
		
		//Step 14: Verify that Now Value of PUC field is updated as Legacy field value was No
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("PUC"), "189 - Residential Miscellaneous",
				"SMAB-T3274: Verify that Now Value of PUC field is updated as Legacy field value was No");
				
		objWorkItemHomePage.logout();
	}
	
	@Test(description = "SMAB-T3272:Verify user is PUC is independent of status field", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" })
	public void PUC_UserRestrictionAndIndependentOfStatus(String loginUser) throws Exception {

		// Step 1: Login as Apas User
		objMappingPage.login(loginUser);
		Thread.sleep(2000);

		// Step2: Opening the PARCELS page and searching the parcel
		objMappingPage.searchModule(PARCELS);
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Fetching PUC value before changing the status field value
		String valueOfPUCBeforeStatusChange = objWorkItemHomePage.getFieldValueFromAPAS("PUC");

		// Step 4: Fetching status value before changing the its value
		String beforeStatus = objWorkItemHomePage.getFieldValueFromAPAS("Status", "Parcel Information");

		// Step 5: Changing the status field value
		objApasGenericPage.editAndSelectFieldData("Status", "To Be Expired");

		// Step 6: Fetching PUC value after changing the status field value
		String valueOfPUCAfterStatusChange = objWorkItemHomePage.getFieldValueFromAPAS("PUC");

		// Step 7: Fetching status value after changing the its value
		String afterStatus = objWorkItemHomePage.getFieldValueFromAPAS("Status");

		// Step 8: verify that the PUC value does not change on change in status
		softAssert.assertEquals(valueOfPUCBeforeStatusChange, valueOfPUCAfterStatusChange,
				"SMAB-T3272: Validating PUC is independent of status field value");
		Assert.assertNotEquals(afterStatus, beforeStatus,
				"SMAB-T3272: Validating PUC is independent of status field value");

		// Step 9: Reverting the status to original state
		objApasGenericPage.editAndSelectFieldData("Status", "Active");

		// Step 10: Logout
		objWorkItemHomePage.logout();
		Thread.sleep(2000);

		// Step 11: login as mapping staff
		objMappingPage.login(MAPPING_STAFF);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		boolean status = objParcelsPage.verifyElementNotVisible(objApasGenericPage.editFieldButton("Status"));

		// Step 12: Verify that status field is non editable for current user.
		softAssert.assertEquals(status, true, "SMAB-T3272:Validating status field is non editable for the current user");
		objWorkItemHomePage.logout();
		
	}   
}		
