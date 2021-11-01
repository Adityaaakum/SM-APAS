package com.apas.Tests.ParcelManagement;
import java.util.ArrayList;
import java.util.HashMap;


import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
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

public class Parcel_Management_RollEntry_Test  extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	ApasGenericPage objApasGenericPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		salesforceAPI = new SalesforceAPI();
	}
	/*
	 * This method is to Verify the Taxable 'Assessed value' fields mentioned on Roll Entry Object.
	 * 
	 * @param loginUser
	 * 
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3766:Verify the UI validations for the fields mentioned on 'Roll Entry' details page & user should be able to edit those fields.", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "RollEntry" })
	public void ParcelManagement_VerifyRollEntryObjectUIValidations(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
		// Fetching the Active Parcel
		String query = "SELECT Id FROM Roll_Entry__c Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String rollEntryToSearch = response.get("Id").get(0);

		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Opening the Parcels module
		String url = "https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Roll_Entry__c/" + rollEntryToSearch + "/view";
		driver.navigate().to(url);

		//Step 3 : Clicking on the edit parcel button and verifying all the fields
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.editButton);


		objParcelsPage.javascriptClick(objParcelsPage.getButtonWithText("Edit"));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.SaveButton);
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Land Assessed Value"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Improvement Assessed Value"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Personal Property Assessed Value"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Exemption 1(HOE)"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Fixtures"), "2334465");
		objParcelsPage.Click(objParcelsPage.getWebElementWithLabel("Root"));
		objParcelsPage.javascriptClick(objParcelsPage.getButtonWithText("Save"));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.editButton);

		String landAssessedValue = objParcelsPage.getFieldValueFromAPAS("Land Assessed Value");
		String improvementAssessedValue = objParcelsPage.getFieldValueFromAPAS("Improvement Assessed Value");
		String personalPropertyAssessedValue = objParcelsPage.getFieldValueFromAPAS("Personal Property Assessed Value");
		String exemptionValue = objParcelsPage.getFieldValueFromAPAS("Exemption 1(HOE)");
		String fixturesValue = objParcelsPage.getFieldValueFromAPAS("Fixtures");
		String expectedValue ="$2,334,465";
		
		softAssert.assertEquals(landAssessedValue, expectedValue, "SMAB-T3766: Validation that Land Assessed Value is equal to expected value.");
		softAssert.assertEquals(improvementAssessedValue, expectedValue, "SMAB-T3766: Validation that Improvement Assessed Value is equal to expected value.");
		softAssert.assertEquals(personalPropertyAssessedValue, expectedValue, "SMAB-T3766: Validation that Personal Property Assessed Value is equal to expected value.");
		softAssert.assertEquals(exemptionValue, expectedValue, "SMAB-T3766: Validation that Exemption 1(HOE) Value is equal to expected value.");
		softAssert.assertEquals(fixturesValue, expectedValue, "SMAB-T3766: Validation that Fixtures Value is equal to expected value.");
		softAssert.assertTrue(
				!(objMappingPage.verifyElementVisible(
						objParcelsPage.getFieldValueFromAPAS("Information", "Land Assessed Value"))),
				"SMAB-T3766: Validation that Land Assessed Value is visible");
		softAssert.assertTrue(
				!(objMappingPage.verifyElementVisible(
						objParcelsPage.getFieldValueFromAPAS("Information", "Improvement Assessed Value"))),
				"SMAB-T3766: Validation that Improvement Assessed Value is visible");
		softAssert.assertTrue(
				!(objMappingPage.verifyElementVisible(
						objParcelsPage.getFieldValueFromAPAS("Information", "Personal Property Assessed Value"))),
				"SMAB-T3766: Validation that Personal Property Assessed Value is visible");
		softAssert.assertTrue(
				!(objMappingPage
						.verifyElementVisible(objParcelsPage.getFieldValueFromAPAS("Information", "Exemption 1(HOE)"))),
				"SMAB-T3766: Validation that Exemption 1(HOE) is visible");
		softAssert.assertTrue(
				!(objMappingPage.verifyElementVisible(objParcelsPage.getFieldValueFromAPAS("Information", "Fixtures"))),
				"SMAB-T3766: Validation that Fixtures is visible");		
		ReportLogger.INFO("Roll Entry Details Page Validation Completed.");
		
		objMappingPage.logout();
	}

}
