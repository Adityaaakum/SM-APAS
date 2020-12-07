package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Map;

public class CountyStratCodesTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	CountyStratCodesPage objCountyStratCodesPage;
	CityStratCodesPage objCityStratCodesPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();
	ApasGenericPage objApasGenericPage;
	Util objUtil = new Util();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objCountyStratCodesPage = new CountyStratCodesPage(driver);
		objCityStratCodesPage = new CityStratCodesPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
	}


	/**
	 * Below test case is used to validate the manual creation of County Strat Code entry
	 * Checking the validations for mandatory fields
	 * Checking for validation on duplicate entry creation
	 **/
	@Test(description = "SMAB-T392,SMAB-T437: Creating manual entry for County Strat Code, editing it and checking for duplicate entry creation", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "buildingPermit"})
	public void CountyStratCode_CREDAndMandatoryFieldValidations(String loginUser) throws Exception {

		String stratCodeDerscription = "CountyStratCodeValidationTest";
		//Step13: Deleting pre-existing created entry via Salesforce API
		ReportLogger.INFO("Deleting the newly created entry via Salesforce API");
		String queryForID = "Select Id From County_Strat_Code__c Where Name like '" + stratCodeDerscription +"%'";
		Map<String, ArrayList<String>> dataMap = objSalesforceAPI.select(queryForID);
		if (dataMap.size()>0) {
			objSalesforceAPI.delete("County_Strat_Code__c", dataMap.get("Id").get(0));
		}

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objCountyStratCodesPage.login(loginUser);

		//Step2: Opening the County Strat Codes module
		objCountyStratCodesPage.searchModule(modules.COUNTY_STRAT_CODES);

		//Step3: Clicking new button and then save button without entering mandatory details
		ReportLogger.INFO("Opening the new entry pop and clicking save button without providing mandatory field");
		objCountyStratCodesPage.openNewEntry();

		//Step4: Checking validation messages on clicking save button without providing values in mandatory fields
		String expErrorMsgOnTop = "Close error dialog\nWe hit a snag.\nReview the following fields\nStrat Code Reference Number\nStrat Code Description\nProcessing Status";
		String expectedFieldLevelErrorMessage = "Complete this field.";

		ReportLogger.INFO("Validating the error messages for individual fields");
		String actualErrorMessage = objCountyStratCodesPage.saveRecordAndGetError();
		softAssert.assertEquals(objCountyStratCodesPage.getIndividualFieldErrorMessage("Strat Code Reference Number"), expectedFieldLevelErrorMessage, "SMAB-T392: Validating error message on not providing Strat Code Reference Number");
		softAssert.assertEquals(objCountyStratCodesPage.getIndividualFieldErrorMessage("Strat Code Description"),expectedFieldLevelErrorMessage, "SMAB-T392: Validating error message on not providing Strat Code Description");
		softAssert.assertEquals(objCountyStratCodesPage.getIndividualFieldErrorMessage("Processing Status"), expectedFieldLevelErrorMessage, "SMAB-T392: Validating error message on not providing Processing Status");
		softAssert.assertEquals(actualErrorMessage, expErrorMsgOnTop, "SMAB-T392: Validating error message displayed on top of the new entry pop up");

		//Step5: Cancelling the pop up by clicking cancel button
		objPage.Click(objPage.getButtonWithText("Cancel"));

		//Step6: Prepare a test data to create a new entry
		String manualEntryData = System.getProperty("user.dir") + testdata.COUNTY_STRAT_CODES + "\\CountyStratCodesManualCreationData.json";
		Map<String, String> manualEntryDataMap = objUtil.generateMapFromJsonFile(manualEntryData, "CountyStratCodesManualCreationData");
		manualEntryDataMap.put("Strat Code Description",stratCodeDerscription);

		//Step7: Adding a new County Start Code
		ReportLogger.INFO("creating a new count strat code with valid values");
		String actSuccessAlertText = objCountyStratCodesPage.addAndSaveCountyStratCode(manualEntryDataMap);
		String expSuccessAlertText = "success\nCounty Strat Code " + '"' + manualEntryDataMap.get("Strat Code Description") + '"' + " was created.\nClose";
		softAssert.assertEquals(actSuccessAlertText, expSuccessAlertText, "SMAB-T392: Validating the pop message on successful creation of County Strat Code entry");

		objPage.Click(objPage.getButtonWithText("Edit"));
		Thread.sleep(1000);

		//Step10: Editing the newly created entry on grid page
		ReportLogger.INFO("Editing the newly created entry from the grid");
		stratCodeDerscription = stratCodeDerscription + "Updated";
		objPage.enter(objCountyStratCodesPage.stratCodeDescInputField, stratCodeDerscription);

		//Step11: Checking the pop up message on editing and saving the existing entry.
		ReportLogger.INFO("Validating pop up message on editing the existing entry.");
		expSuccessAlertText = "success\nCounty Strat Code " + '"' + stratCodeDerscription + '"' + " was saved.\nClose";
		actSuccessAlertText = objCountyStratCodesPage.saveRecord();
		softAssert.assertEquals(actSuccessAlertText, expSuccessAlertText, "SMAB-T392: Validating the pop message on successfully editing the County Strat Code entry");

		//Step2: Opening the County Strat Codes module
		objCountyStratCodesPage.searchModule(modules.COUNTY_STRAT_CODES);

		//Step12: Checking validation on duplicate entry creation
		String expectedWarningMessage = "Close error dialog\nWe hit a snag.\nYou can't save this record because a duplicate record already exists. To save, use different information.\nView Duplicates";

		ReportLogger.INFO("Creating duplicate entry to check validation message for duplicate entry creation");
		manualEntryDataMap.put("Strat Code Description", stratCodeDerscription);
		objCountyStratCodesPage.openNewEntry();
		objCountyStratCodesPage.enterCountyStratCodeDetails(manualEntryDataMap);
		softAssert.assertEquals(objCountyStratCodesPage.saveRecordAndGetError(), expectedWarningMessage, "SMAB-T437: Validating warning message for duplicate entry");
		objCountyStratCodesPage.cancelRecord();

		//Step13: Deleting the newly created entry via Salesforce API
		ReportLogger.INFO("Deleting the newly created entry via Salesforce API");
		queryForID = "Select Id From County_Strat_Code__c Where Name = '" + stratCodeDerscription + "'";
		dataMap = objSalesforceAPI.select(queryForID);
		objSalesforceAPI.delete("County_Strat_Code__c", dataMap.get("Id").get(0));

		//Step14: Logout at the end of the test
		objCountyStratCodesPage.logout();
	}


	/**
	 * Below test case is used to validate validation for dependency of permit value operator and permit value limit fields on each other
	 **/
	@Test(description = "SMAB-T465: Creating manual entry to check dependent permit value operator and permit value limit fields on each other", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "buildingPermit"})
	public void CountyStratCode_PermitValueFieldsDependency(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objCountyStratCodesPage.login(loginUser);

		//Step2: Opening the County Strat Codes module
		objCountyStratCodesPage.searchModule(modules.COUNTY_STRAT_CODES);

		//Step3: Prepare a test data to create a new entry
		String manualEntryData = System.getProperty("user.dir") + testdata.COUNTY_STRAT_CODES + "\\CountyStratCodesManualCreationData.json";
		Map<String, String> manualEntryDataMap = objUtil.generateMapFromJsonFile(manualEntryData, "CountyStratCodesManualCreationData");

		//Step4: Enter values in mandatory fields and Permit Value Limit
		ReportLogger.INFO("Clicking new button and entering fields values");
		objCountyStratCodesPage.openNewEntry();
		objCountyStratCodesPage.enterCountyStratCodeDetails(manualEntryDataMap);
		objPage.enter(objCountyStratCodesPage.permitValueLimit, manualEntryDataMap.get("Permit Value Limit"));
		String actualErrorMessage = objCountyStratCodesPage.saveRecordAndGetError();

		//Step5: Validation the error message on not providing Permit Value Operator value
		String expectedErrorMsg = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nPermit Value Limit and Permit Value Operator should be filled together";
		softAssert.assertEquals(actualErrorMessage, expectedErrorMsg, "SMAB-T465: Validating the pop message on successful creation of County Strat Code entry");
		objCountyStratCodesPage.cancelRecord();

		//Step6: Enter values in mandatory fields and Permit Value Operator
		ReportLogger.INFO("Clicking new button again to create a valid entry with valid values");
		objCountyStratCodesPage.openNewEntry();
		objCountyStratCodesPage.enterCountyStratCodeDetails(manualEntryDataMap);
		objApasGenericPage.selectOptionFromDropDown(objCountyStratCodesPage.permitValueOperatorDropDown, manualEntryDataMap.get("Permit Value Operator"));
		actualErrorMessage = objCountyStratCodesPage.saveRecordAndGetError();

		//Step7: Validation the error message on not providing Permit Value Limit value
		softAssert.assertEquals(actualErrorMessage, expectedErrorMsg, "SMAB-T465: Validating the pop message on successful creation of County Strat Code entry");
		objCountyStratCodesPage.cancelRecord();

		//Step8: Logout at the end of the test
		objCountyStratCodesPage.logout();
	}


	/**
	 * Below test case is used to create city strat code from details page of county strat code
	 * Checking the validations for mandatory fields
	 **/
	@Test(description = "SMAB-T436: Creating manual entry for City Strat Code from details page of County Strat Code", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "buildingPermit"})
	public void CountyStratCode_CreateCityStratCodeFromCountyStratDetailsPage(String loginUser) throws Exception {

		//Step1: Retrieving details of an existing county strat code using salesforce API
		String queryCountyStratCode = "Select Name From County_Strat_Code__C Where Status__c = 'Active' Limit 1";
		Map<String, ArrayList<String>> dataMapCountStratCode = objSalesforceAPI.select(queryCountyStratCode);

		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objCountyStratCodesPage.login(loginUser);

		//Step3: Opening the County Strat Codes module
		objCountyStratCodesPage.searchModule(modules.COUNTY_STRAT_CODES);

		//Step4: Open the county strat code fetched thorugh salesforce API
		objCountyStratCodesPage.globalSearchRecords(dataMapCountStratCode.get("Name").get(0));

		//Step5: Clicking on the drop down icon and new button to open new entry pop up for city strat code
		objCountyStratCodesPage.OpenNewEntryFormFromRightHandSidePanel("City Strat Codes");

		//Step7: Create new entry for County Strat Codes
		String cityStratCode = objUtil.getCurrentDate("YYYYmmDDHHMMSS");
		objPage.waitForElementToBeClickable(objCityStratCodesPage.cityCodeDropDown,10);
		objCityStratCodesPage.selectOptionFromDropDown(objCityStratCodesPage.cityCodeDropDown,"AT");
		objPage.enter(objCityStratCodesPage.cityStratCodeEditBox,cityStratCode);
		objCityStratCodesPage.selectOptionFromDropDown(objCityStratCodesPage.statusDropDown,"Inactive");

		String actualSuccessAlertText = objCountyStratCodesPage.saveRecord();
		String expectedSuccessAlertText = "success\nCity Strat Code \"" + cityStratCode + "\" was created.\nClose";
		softAssert.assertEquals(actualSuccessAlertText, expectedSuccessAlertText, "SMAB-T436: Validating the pop message on successful creation of City Strat Code entry from County Strat details page");

		//Step8: Logout at the end of the test
		objCountyStratCodesPage.logout();
	}
}