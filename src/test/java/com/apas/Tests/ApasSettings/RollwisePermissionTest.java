package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RollwisePermissionTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	SoftAssertion softAssert  = new SoftAssertion();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception {
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}

     /*
	 Below test case will validate generic user is not able to access below modules
	 City Strat Codes, County Strat Codes, Non Relevant Permit Settings
	 */
	@Test(description = "SMAB-T439: Validation for user not having the access to certain modules", groups = {"smoke","regression","buildingPermit"})
	public void BuildingPermit_GenericUserNotHavingAccess() throws Exception {

		//Step1: Login to the APAS application using the General User
		objApasGenericFunctions.login(users.BPP_AUDITOR);
		objPage.Click(objApasGenericPage.appLauncher);

		//Step2: Validating that generic user is not having the access to City Strat Code Module
		ReportLogger.INFO("Validating for generic user not having the access to City Strat Code Module");
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.CITY_STRAT_CODES);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'City Strat Code' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'City Strat Code' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Step3: Validating that generic user is not having the access to County Strat Code Module
		ReportLogger.INFO("Validating for generic user not having the access to County Strat Code Module");
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.COUNTY_STRAT_CODES);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'County Strat Code' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'County Strat Code' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Step4: Validating that generic user is not having the access to Non Relevant Permit Settings module
		ReportLogger.INFO("Validating for generic user not having the access to Non Relevant Permit Settings Module");
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.NON_RELEVANT_PERMIT_SETTINGS);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'Non Relevant Permit Settings' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'Non Relevant Permit Settings' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
}