package com.apas.Tests;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;

import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendSetupTest extends TestBase {

	Page objPage;
	LoginPage objLoginPage;
	BppTrendSetupPage objBppTrendSetupPage;
	private RemoteWebDriver driver;

	/*
	 * TEST CASE 1 - Verify the Login functionality (SMAPAS-T28) DESCRIPTION: --
	 * Navigate to Sales-force URL -- Enter 'User name' and 'Password' -- Click
	 * 'Log in' button -- Verify User is able to log in
	 */

	@Test(priority = 0)
	public void verifySalesForceLogin() throws Exception {
		boolean flag = false;

		driver = BrowserDriver.getBrowserInstance();
		SoftAssert s_assert = new SoftAssert();
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);

		/* Navigate to the URL */
		objPage.navigateTo(driver, (CONFIG.getProperty("URL_qa")));
		ExtentTestManager.getTest().log(LogStatus.INFO,
				"Browser launched and navigated to URL: " + (CONFIG.getProperty("URL_qa")));

		/* Enter User name and Password on the login page */
		objLoginPage.enterLoginUserName(CONFIG.getProperty("username"));
		objLoginPage.enterLoginPassword(CONFIG.getProperty("password"));
		ExtentTestManager.getTest().log(LogStatus.INFO,
				"Login details are entered for : " + (CONFIG.getProperty("username")));

		/* Click the 'Log in' button */
		objLoginPage.clickBtnSubmit();
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click the 'Log In to Sandbox' button");

		// Has been put to handle the MFA manually.
		Thread.sleep(30000);

		// Instantiating the page objects for BPP trend Setup Page Class.
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
	}

	@Test(priority = 1)
	public void verifyBppTrendSetupNavigation() throws Exception {
		objBppTrendSetupPage.clickAppLauncher();
		Thread.sleep(5000);
		
		objBppTrendSetupPage.searchForApp("BPP Trend Setup");
		Thread.sleep(5000);
		
		objBppTrendSetupPage.clickNavOptionFromDropDown("BPP Trend Setup");
		Thread.sleep(5000);
		
		objBppTrendSetupPage.getSelectedAppText("BPP Trend Setup");
		Thread.sleep(5000);
	}
}
