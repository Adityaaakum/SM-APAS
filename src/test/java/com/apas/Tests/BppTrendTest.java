package com.apas.Tests;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendTest extends TestBase {
	private RemoteWebDriver driver;
	BppTrendPage objBppTrendsPage = null;
	Page objPage = null;
	LoginPage objLoginPage = null;
	List<String> tableNamesList = null;
	String rollYear = null;

	@Test(description = "SMAB-T133: Log in & navigate to BPP Trend page", priority = 0, alwaysRun = true)
	public void searchBppTrendInNavApp() throws Exception {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		
		tableNamesList = Arrays.asList(CONFIG.getProperty("tableNames").split(","));
		rollYear = CONFIG.getProperty("rollYear");	
		
		objPage.navigateTo(driver, (CONFIG.getProperty("URL_qa")));
		ExtentTestManager.getTest().log(LogStatus.INFO, "Browser launched & URL opened: " + (CONFIG.getProperty("URL_qa")));

		objLoginPage.enterLoginUserName(CONFIG.getProperty("username"));
		objLoginPage.enterLoginPassword(CONFIG.getProperty("password"));
		ExtentTestManager.getTest().log(LogStatus.INFO, "Login details are entered for : " + (CONFIG.getProperty("username")));
		objLoginPage.clickBtnSubmit();
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click the 'Log In to Sandbox' button");

		objBppTrendsPage = new BppTrendPage(driver);
		objBppTrendsPage.clickAppLauncher();
		objBppTrendsPage.searchForApp("BPP Trend");
		objBppTrendsPage.clickNavOptionFromDropDown("BPP Trend");
	}

	@Test(description = "Selecting Roll Year", priority = 1, alwaysRun = true)
	public void selectRollYearFromDropDown() throws Exception {
		objBppTrendsPage.clickRollYearDropDown();
		objBppTrendsPage.clickOnGivenRollYear(rollYear);
		objBppTrendsPage.clickBtnSelect();
	}

	@Test(description = "Selecting given table & validate its data", priority = 2, enabled = false)
	public void selectTableAndValidateData() throws Exception {
		for (int i = 0; i <= tableNamesList.size(); i++) {
			objBppTrendsPage.clickOnGivenTableName(tableNamesList.get(i));
		}
	}
}
