package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.Page;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.PageObjects.NonRelevantPermitSettingsPage;

public class NonRelevantPermitSettingsTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	NonRelevantPermitSettingsPage objNonRelevantPermitSettingsPage;
	SoftAssertion softAssert = new SoftAssertion();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception {
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objNonRelevantPermitSettingsPage = new NonRelevantPermitSettingsPage(driver);

	}

	/*
     Below test case will validate that user is not allowed to create duplicate Non Relevant Permit Settings
     PreCondition: AT City Code with Active status is already added in Non Relevant Permit Settings
     */
	@Test(description = "SMAB-T398: Validation for Duplicate Non Relevant Permit Settings", groups = {"regression","buildingPermit"},dataProvider = "loginBPPBusinessAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void NonRelevantPermitSettings_DuplicateNonRelevantSettingsNotAllowed(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the credentials passed through data provider
		objNonRelevantPermitSettingsPage.login(loginUser);

		//Step2: Opening the Non Relevant Permit Settings module
		objNonRelevantPermitSettingsPage.searchModule(modules.NON_RELEVANT_PERMIT_SETTINGS);

		//Step3: Adding a preexisting record
		ReportLogger.INFO("Adding a new non relevant permit setting record with duplicte values");
		objPage.Click(objNonRelevantPermitSettingsPage.newButton);
		objNonRelevantPermitSettingsPage.selectOptionFromDropDown(objNonRelevantPermitSettingsPage.cityCodeDrpDown,"AT");
		objNonRelevantPermitSettingsPage.selectOptionFromDropDown(objNonRelevantPermitSettingsPage.statusDrpDown,"Active");

		//Step4: Validation of message appearing for duplicate permit
		String expectedWarningMessage = "Close error dialog\nWe hit a snag.\nYou can't save this record because a duplicate record already exists. To save, use different information.\nView Duplicates";

		softAssert.assertEquals(objPage.getElementText(objNonRelevantPermitSettingsPage.pageError),expectedWarningMessage,"SMAB-T398: Validation for existence of duplicate record message after adding the preexisting record");
		objPage.Click(objPage.getButtonWithText("Cancel"));

		//Logout at the end of the test
		objNonRelevantPermitSettingsPage.logout();
	}
}