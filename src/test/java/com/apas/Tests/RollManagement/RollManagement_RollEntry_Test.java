package com.apas.Tests.RollManagement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
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

public class RollManagement_RollEntry_Test  extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	ApasGenericPage objApasGenericPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	CIOTransferPage objCIOTransferPage;

	

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		salesforceAPI = new SalesforceAPI();
		objCIOTransferPage = new CIOTransferPage(driver);

	}
	/*
	 * This method is to Verify the Taxable 'Assessed value' fields mentioned on Roll Entry Object.
	 * 
	 * @param loginUser
	 * 
	 * @throws Exception
	 */
	@Test(description = "SMAB-T7565,SMAB-T3766:Verify the UI validations for the fields mentioned on 'Roll Entry' details page & user should be able to edit those fields.", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "RollManagement" })
	public void CIO_VerifyRollEntryObjectUIValidations(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
		// Fetching the Active Parcel
		String query = "SELECT Id FROM Roll_Entry__c Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String rollEntryToSearch = response.get("Id").get(0);

		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Navigating to the Roll Entry Record
		String url = "https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Roll_Entry__c/" + rollEntryToSearch + "/view";
		driver.navigate().to(url);

		//Step 3 : Clicking on the edit button and verifying all the fields
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.editButton);

		//adding verification for picklist values in Status field
		
		objCIOTransferPage.editRecordedApnField("Status");
		objCIOTransferPage.waitForElementToBeVisible(6, "Status");		
		objParcelsPage.Click(objParcelsPage.getWebElementWithLabel("Status"));
		List<Object> statusFieldOptions= objParcelsPage.getAllOptionFromDropDown("Status");
		
		String[]  expectedSubSetStatusFieldOptions= {"Direct Enrollment","Conversion","Cancellation","Record Calculated","Ready for Early Release","Approved","Assessment held for further review","Manual record insert","For Record Only, No Processing Done","Notice Sent","Same as NOTC. Used by Appraiser Support when SUP NOTC is opened to add / delete exemption","Processed and ready to be released","Released to Controller" ,"Current Working Roll","Manual Calc Roll"};
		
		List<String> expectedStatusFieldOptions=Arrays.asList(expectedSubSetStatusFieldOptions);
		
		
		softAssert.assertTrue(statusFieldOptions.containsAll(expectedStatusFieldOptions),"SMAB-T7565 : Validation of picklist values in  Status field in roll entry object");		
 
		objParcelsPage.javascriptClick(objParcelsPage.getButtonWithText("Edit"));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.SaveButton);
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Land Assessed Value"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Improvement Assessed Value"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Personal Property Assessed Value"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Exemption 1(HOE)"), "2334465");
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Fixtures"), "2334465");
		objParcelsPage.Click(objParcelsPage.getWebElementWithLabel("Root"));
		objParcelsPage.clearFieldValue("TRA");
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
				(objMappingPage.verifyElementVisible(
						objParcelsPage.getFieldValueFromAPAS("Information", "Land Assessed Value"))),
				"SMAB-T3766: Validation that Land Assessed Value is visible");
		softAssert.assertTrue(
				(objMappingPage.verifyElementVisible(
						objParcelsPage.getFieldValueFromAPAS("Information", "Improvement Assessed Value"))),
				"SMAB-T3766: Validation that Improvement Assessed Value is visible");
		softAssert.assertTrue(
				(objMappingPage.verifyElementVisible(
						objParcelsPage.getFieldValueFromAPAS("Information", "Personal Property Assessed Value"))),
				"SMAB-T3766: Validation that Personal Property Assessed Value is visible");
		softAssert.assertTrue(
				(objMappingPage
						.verifyElementVisible(objParcelsPage.getFieldValueFromAPAS("Information", "Exemption 1(HOE)"))),
				"SMAB-T3766: Validation that Exemption 1(HOE) is visible");
		softAssert.assertTrue(
				(objMappingPage.verifyElementVisible(objParcelsPage.getFieldValueFromAPAS("Information", "Fixtures"))),
				"SMAB-T3766: Validation that Fixtures is visible");		
		ReportLogger.INFO("Roll Entry Details Page Validation Completed.");
		
		objMappingPage.logout();
	}
	/*
	 * This method is to Verify the Quick Actions Buttons to Generate/Update Annual Roll Entry Records.
	 * 
	 * @param loginUser
	 * 
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3932, SMAB-T4268:Verify the Quick Actions Buttons to Generate/Update Annual Roll Entry Records.", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression",  "RollManagement" })
	public void CIO_VerifyAnnualRollEntryRecordsButtons(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");
			// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Navigating to the Roll Year Setting
		objWorkItemHomePage.searchModule(ROLL_YEAR_SETTINGS);
		
		//Step 3 : Navigating to the Open Roll Year Settings
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.newButton);
		String query = "SELECT Id FROM Roll_Year_Settings__c Where Status__c ='Open'";
		HashMap<String, ArrayList<String>> responseIdRollYearSettings = salesforceAPI.select(query);
		String openRollYear=responseIdRollYearSettings.get("Id").get(0);
		driver.navigate().to("https://smcacre--"+ execEnv + ".lightning.force.com/lightning/r/Roll_Year_Settings__c/" + openRollYear + "/view");
		objMappingPage.waitForElementToBeClickable(objMappingPage.getButtonWithText("Edit"));
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objMappingPage.getButtonWithText("Generate Annual Record")),
				"SMAB-T3932, SMAB-T4268: Validation that Generate Annual Record Button should be visible.");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objMappingPage.getButtonWithText("Update Annual Record")),
				"SMAB-T3932, SMAB-T4268: Validation that Update Annual Record Button should be visible.");		
		
		objMappingPage.logout();
	}


}
