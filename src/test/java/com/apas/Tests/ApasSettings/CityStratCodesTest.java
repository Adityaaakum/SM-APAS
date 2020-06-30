package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
}