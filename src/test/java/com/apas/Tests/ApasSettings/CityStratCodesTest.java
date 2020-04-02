package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.*;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

public class CityStratCodesTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	CityStratCodesPage objCityStratCodesPage;
	SoftAssertion softAssert = new SoftAssertion();
	Util objUtils = new Util();

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objCityStratCodesPage = new CityStratCodesPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}
		
	@AfterMethod
	public void afterMethod() throws IOException{
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business admin and appraisal support in an array
	 **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] {{ users.BUSINESS_ADMIN }};
    }
	
    
    /**
	 Below test case will validate that County record can have multiple related City Code records
	 **/
	@Test(description = "SMAB-T396: Validation for County record can have multiple related City Code records", dataProvider = "loginUsers", groups = {"smoke","regression"}, priority = 0, alwaysRun = true, enabled = true)
	public void verifyCreationOfMultipleRelatedCityCodes(String loginUser) throws Exception {
		String countSortedByFilteredBy;
		String strSuccessAlertMessage;

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the City Strat Code module
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening City Strat Code module");
		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);

		//Step3: Adding a new record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding a new 'city strat code' record");
		String strCityStratCode1 = "Test" + objUtils.getCurrentDate("YYYYmmDDHHMMSS");
		strSuccessAlertMessage = objCityStratCodesPage.addAndSaveCityStratcode("121","AT",strCityStratCode1,"Active");
		softAssert.assertEquals(strSuccessAlertMessage,"City Strat Code \"" + strCityStratCode1 + "\" was created.","SMAB-T396: Validation of text nessage on Success Alert");

		//Step4: Opening the City Strat Code module
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening City Strat Code module");
		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);

		//Step5: Adding a new 'city strat code' record with the same detail as previous record with different city start code
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding a new 'city strat code' record with the same detail as previous record with different city start code");
		String strCityStratCode2 = "Test" + objUtils.getCurrentDate("YYYYmmDDHHMMSS");
		strSuccessAlertMessage = objCityStratCodesPage.addAndSaveCityStratcode("121","AT",strCityStratCode2,"Active");
		softAssert.assertEquals(strSuccessAlertMessage,"City Strat Code \"" + strCityStratCode2 + "\" was created.","SMAB-T396: Validation of text nessage on Success Alert");

		//Step6: Opening the City Strat Code module
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening City Strat Code module");
		objApasGenericFunctions.searchModule(modules.CITY_STRAT_CODES);

		//Step7: Validation of existence of multiple related City Code records
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of the records existence added in the previous step");
		objApasGenericFunctions.displayRecords("All");
		String expectedValidationMessage = "1 item • Sorted by City Strat Code • Filtered by all city strat codes •";

		countSortedByFilteredBy = objApasGenericFunctions.searchRecords(strCityStratCode1);
		softAssert.assertEquals(countSortedByFilteredBy,expectedValidationMessage,"SMAB-T396: Validation of records displayed on the grid with the City Strat code " + strCityStratCode1);

		countSortedByFilteredBy = objApasGenericFunctions.searchRecords(strCityStratCode2);
		softAssert.assertEquals(countSortedByFilteredBy,expectedValidationMessage,"SMAB-T396: Validation of records displayed on the grid with the City Strat code " + strCityStratCode2);
		softAssert.assertAll();
	}
}
