package com.apas.Tests.ApasSettings;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.apas.PageObjects.NonRelevantPermitSettingsPage;
import java.io.IOException;

public class NonRelevantPermitSettingsTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	NonRelevantPermitSettingsPage objNonRelevantPermitSettingsPage;
	SoftAssertion softAssert = new SoftAssertion();

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objNonRelevantPermitSettingsPage = new NonRelevantPermitSettingsPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}
		
	@AfterMethod
	public void afterMethod() throws IOException{
		objApasGenericFunctions.logout();
		softAssert.assertAll();
	}
	
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business admin and appraisal support in an array
	 **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] {{ users.BUSINESS_ADMIN } };
    }


    /**
	 Below test case will validate that user is not allowed to create duplicate Non Relevant Permit Settings
	 PreCondition: AT City Code with Active status is already added in Non Relevant Permit Settings
	 **/
	@Test(description = "SMAB-T398: Validation for Duplicate Non Relevant Permit Settings", dataProvider = "loginUsers", groups = {"smoke","regression"}, priority = 0, alwaysRun = true, enabled = true)
	public void verifyDuplicateNonRelevantSettingsNotAllowed(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Non Relevant Permit Settings module
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening Non Relevant Permit Settings module");
		objApasGenericFunctions.searchModule(modules.NON_RELEVANT_PERMIT_SETTINGS);

		//Step3: Adding a preexisting record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding a new non relevant permit setting record with duplicte values");
		objPage.Click(objNonRelevantPermitSettingsPage.newButton);
		objPage.Select(objNonRelevantPermitSettingsPage.cityCodeDrpDown,"AT");
		objPage.Select(objNonRelevantPermitSettingsPage.statusDrpDown,"Active");

		//Step4: Validation of message appearing for duplicate permit
		softAssert.assertTrue(objPage.verifyElementVisible(objNonRelevantPermitSettingsPage.messageDuplicateData),"SMAB-T398: Validation for existence of duplicate record message after adding the preexisting record");
		softAssert.assertTrue(objPage.verifyElementVisible(objNonRelevantPermitSettingsPage.linkViewDuplicates),"SMAB-T398: Validation for existence of View Duplicate Link after adding the preexisting record");
		objPage.Click(objNonRelevantPermitSettingsPage.cancelButton);
	}
}
