package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.Keys;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CityStratCodesTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	CityStratCodesPage objCityStratCodesPage;
	SoftAssertion softAssert = new SoftAssertion();
	Util objUtils = new Util();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objCityStratCodesPage = new CityStratCodesPage(driver);
	}

	/**
	 * Below test case will validate that County record can have multiple related City Code records
	 */
	@Test(description = "SMAB-T396: Validation for County record can have multiple related City Code records", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression"}, alwaysRun = true)
	public void CityStratCode_verifyCreationOfMultipleRelatedCityCodesAndEdit(String loginUser) throws Exception {
		String strSuccessAlertMessage;

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the City Strat Code module
		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);

		//Step3: Adding a new record
		ReportLogger.INFO("Adding a new 'city strat code' record");
		String strCityStratCode1 = "Test" + objUtils.getCurrentDate("YYYYmmDDHHMMSS");
		strSuccessAlertMessage = objCityStratCodesPage.addAndSaveCityStratcode("SOLAR", "AT", strCityStratCode1, "Active");
		softAssert.assertEquals(strSuccessAlertMessage, "City Strat Code \"" + strCityStratCode1 + "\" was created.", "SMAB-T396: Validation of text message on Success Alert");

		//Step4: Opening the City Strat Code module
		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);

		//Step5: Adding a new 'city strat code' record with the same detail as previous record with different city start code
		ReportLogger.INFO("Adding a new 'city strat code' record with the same detail as previous record with different city strat code");
		String strCityStratCode2 = "Test" + objUtils.getCurrentDate("YYYYmmDDHHMMSS");
		strSuccessAlertMessage = objCityStratCodesPage.addAndSaveCityStratcode("SOLAR", "AT", strCityStratCode2, "Active");
		softAssert.assertEquals(strSuccessAlertMessage, "City Strat Code \"" + strCityStratCode2 + "\" was created.", "SMAB-T396: Validation of text nessage on Success Alert");

		//Step6: Edit the recently created City strat code
		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);
		objApasGenericFunctions.globalSearchRecords(strCityStratCode2);
		objPage.Click(objCityStratCodesPage.editButton);
		objPage.waitForElementToBeClickable(objCityStratCodesPage.countyStratCodeEditBox,10);
		objPage.Select(objCityStratCodesPage.statusDropDown,"Inactive");
		objPage.Click(objCityStratCodesPage.saveButton);
		objPage.waitForElementToBeVisible(objCityStratCodesPage.successAlert,20);
		softAssert.assertEquals(objPage.getElementText(objCityStratCodesPage.successAlertText), "City Strat Code \"" + strCityStratCode2 + "\" was saved.", "SMAB-T396,SMAB-T390: Validation of text nessage on Success Alert");
	}


	/**
	 * Below test case is used to validate the manual creation of City Strat Code entry
	 * Checking validations on mandatory fields
	 **/
	@Test(description = "SMAB-T390,SMAB-T395: Creating manual entry for City Strat Code", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "buildingPermit"}, alwaysRun = true)
	public void CityStratCode_MandatoryFieldValidation(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the City Strat Codes module
		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);

		//Step3: Clicking new button and then save button without entering mandatory details
		objCityStratCodesPage.openNewEntry();
		objPage.clickAction(objCityStratCodesPage.saveButton);

		//Step4: Checking validation messages on clicking save button without providing values in mandatory fields
		ReportLogger.INFO("Validating the mandatory field error messages");
		String expectedErrorMessageOnTop = "These required fields must be completed: City Code, County Strat Code, City Strat Code";
		String expectedFieldLevelErrorMessage = "Complete this field";
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			expectedFieldLevelErrorMessage = "Complete this field.";
		}

		String actualErrorMessageOnTop = objCityStratCodesPage.getElementText(objCityStratCodesPage.errorMsgOnTop);

		softAssert.assertEquals(actualErrorMessageOnTop, expectedErrorMessageOnTop, "SMAB-T390,SMAB-T395: Validating error message displayed on top of the new entry pop up");
		softAssert.assertEquals(objApasGenericFunctions.getIndividualFieldErrorMessage("County Strat Code"), expectedFieldLevelErrorMessage, "SMAB-T390,SMAB-T395: Validating error message on not providing County Strat Code");
		softAssert.assertEquals(objApasGenericFunctions.getIndividualFieldErrorMessage("City Code"), expectedFieldLevelErrorMessage, "SMAB-T390,SMAB-T395: Validating error message on not providing City Code");
		softAssert.assertEquals(objApasGenericFunctions.getIndividualFieldErrorMessage("City Strat Code"), expectedFieldLevelErrorMessage, "SMAB-T390,SMAB-T395: Validating error message on not providing City Strat Code");

		//Step5: Cancelling the pop up by clicking cancel button
		objPage.Click(objCityStratCodesPage.cancelButton);

		//Step13: Logout at the end of the test
		objApasGenericFunctions.logout();
	}


//	/**
//	 * Below test case is used to validate the manual creation of City Strat Code entry
//	 * Checking validations on mandatory fields
//	 **/
//	@Test(description = "SMAB-T390,SMAB-T395: Creating manual entry for City Strat Code", dataProvider = "loginBusinessAndAppraisalUsers", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "buildingPermit"}, alwaysRun = true)
//	public void verify_CityStratCode_ManualCreate_NewCityStratCode_WithDataValidations(String loginUser) throws Exception {
//
//		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
//		objApasGenericFunctions.login(loginUser);
//
//		//Step2: Opening the City Strat Codes module
//		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);
//		objApasGenericFunctions.selectAllOptionOnGrid();
//
//		//Step3: Clicking new button and then save button without entering mandatory details
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the new entry pop and clicking save button without providing mandatory field");
//		objCityStratCodesPage.openNewEntry();
//		objPage.clickAction(objCityStratCodesPage.saveButton);
//
//		//Step4: Checking validation messages on clicking save button without providing values in mandatory fields
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the error messages");
//		softAssert.assertEquals(objCityStratCodesPage.getElementText(objCityStratCodesPage.errorMsgUnderCountyStratCodeField), "Complete this field", "SMAB-T390: Validating error message on not providing County Strat Code");
//		softAssert.assertEquals(objCityStratCodesPage.getElementText(objCityStratCodesPage.errorMsgUnderCountyStratCodeField), "Complete this field", "SMAB-T395: Validating error message on not providing County Strat Code");
//		softAssert.assertEquals(objCityStratCodesPage.getElementText(objCityStratCodesPage.errorMsgUnderCityCodeField), "Complete this field", "SMAB-T390: Validating error message on not providing City Code");
//		softAssert.assertEquals(objCityStratCodesPage.getElementText(objCityStratCodesPage.errorMsgUnderCityCodeField), "Complete this field", "SMAB-T395: Validating error message on not providing City Code");
//		softAssert.assertEquals(objCityStratCodesPage.getElementText(objCityStratCodesPage.errorMsgUnderCityStratCodeField), "Complete this field", "SMAB-T390: Validating error message on not providing City Strat Code");
//		softAssert.assertEquals(objCityStratCodesPage.getElementText(objCityStratCodesPage.errorMsgUnderCityStratCodeField), "Complete this field", "SMAB-T395: Validating error message on not providing City Strat Code");
//
//		String actErrorMsgOnTop = objCityStratCodesPage.getElementText(objBuildingPermitPage.errorMsgOnTop);
//		String expErrorMsgOnTop = "These required fields must be completed: City Code, County Strat Code, City Strat Code";
//		softAssert.assertEquals(actErrorMsgOnTop, expErrorMsgOnTop, "SMAB-T390: Validating error message displayed on top of the new entry pop up");
//		softAssert.assertEquals(actErrorMsgOnTop, expErrorMsgOnTop, "SMAB-T395: Validating error message displayed on top of the new entry pop up");
//
//		//Step5: Cancelling the pop up by clicking cancel button
//		objPage.Click(objCityStratCodesPage.cancelButton);
//
//		//Step6: Prepare a test data to create a new entry
//		String manualEntryData = System.getProperty("user.dir") + testdata.CITY_STRAT_CODES_MANUAL + "\\CityStratCodesManualCreationData.json";
//		Map<String, String> manualEntryDataMap = objUtil.generateMapFromJsonFile(manualEntryData, "CityStratCodesManualCreationData");
//
//		//Step6: Adding a new City Start Code
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking new button again to create a valid entry with valid values");
//		String actSuccessAlertText = objCityStratCodesPage.addAndSaveCityStratCode(manualEntryDataMap);
//		objBppTrendPage.closePageLevelMsgPopUp();
//		String expSuccessAlertText = "City Strat Code " + '"' + manualEntryDataMap.get("City Strat Code") + '"' + " was created.";
//		softAssert.assertEquals(actSuccessAlertText, expSuccessAlertText, "SMAB-T390: Validating the pop message on successful creation of City Strat Code entry");
//		softAssert.assertEquals(actSuccessAlertText, expSuccessAlertText, "SMAB-T395: Validating the pop message on successful creation of City Strat Code entry");
//		Thread.sleep(3000);
//
//		//Step7: Retrieving the name of the newly created entry and navigating back to the grid page
//		String cityStratCodesNameFromDetailsPage = objApasGenericFunctions.getFieldValueFromAPAS("City Strat Code", "Strat Code Information");
//		softAssert.assertEquals(cityStratCodesNameFromDetailsPage, manualEntryDataMap.get("City Strat Code"), "SMAB-T390: Validating the name of city strat code on details page");
//		softAssert.assertEquals(cityStratCodesNameFromDetailsPage, manualEntryDataMap.get("City Strat Code"), "SMAB-T395: Validating the name of city strat code on details page");
//
//		//Step8: Navigating back to grid page and searching for the newly created entry
//		objPage.clickAction(objCityStratCodesPage.recentlyViewedTab);
//		Thread.sleep(1000);
//		objPage.waitForElementToBeVisible(objCityStratCodesPage.searchBox, 10);
//		objPage.enter(objCityStratCodesPage.searchBox, "Test - City Strat Code");
//		objPage.enter(objCityStratCodesPage.searchBox, Keys.ENTER);
//
//		//Step9: Editing the newly created entry on grid page
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created entry from the grid");
//		String cityStratCodesUpdatedName = "Test 1 - City Strat Code";
//		objBuildingPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(manualEntryDataMap.get("City Strat Code"));
//		objPage.clickAction(objBuildingPermitPage.editLinkUnderShowMore);
//		objCityStratCodesPage.editExistingCityStratEntry("CONSTRUCTION", cityStratCodesUpdatedName);
//		objPage.Click(objCityStratCodesPage.saveButton);
//
//		//Step10: Checking the pop up message on editing and saving the existing entry.
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating pop up message on editing the existing entry.");
//		expSuccessAlertText = "City Strat Code " + '"' + "Test 1 - City Strat Code" + '"' + " was saved.";
//		actSuccessAlertText = objPage.getElementText(objCityStratCodesPage.successAlertText);
//		softAssert.assertEquals(actSuccessAlertText, expSuccessAlertText, "SMAB-T390: Validating the pop message on successfully editing the City Strat Code entry");
//		softAssert.assertEquals(actSuccessAlertText, expSuccessAlertText, "SMAB-T395: Validating the pop message on successfully editing the City Strat Code entry");
//
//		//Step11: Delete the newly created created entry
//		//objBuildingPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(cityStratCodesName);
//		//objPage.Click(objBuildingPermitPage.deleteLinkUnderShowMore);
//		//objPage.Click(objCityStratCodesPage.deleteButtonInPopUp);
//
//		//Step12: Deleting the newly created entry via Salesforce API
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Deleting the record via api");
//		String queryForID = "Select Id From City_Strat_Code__c Where Name In ('Test - City Strat Code','"+cityStratCodesUpdatedName+"')";
//		System.out.println("queryForID: "+ queryForID);
//		Map<String, ArrayList<String>> dataMap = objSalesforceAPI.select(queryForID);
//		objSalesforceAPI.delete("City_Strat_Code__c", dataMap.get("Id").get(0));
//
//		//Step13: Logout at the end of the test
//		objApasGenericFunctions.logout();
//	}
//
}