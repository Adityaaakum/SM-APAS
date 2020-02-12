package com.apas.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.apas.Assertions.CustomHardAssert;
import com.apas.Assertions.CustomSoftAssert;
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
	List<String> tableNamesOutsideMoreTab = null;
	List<String> tableNamesUnderMoreTab = null;
	String rollYear = null;

	@Test(description = "SMAB-T190: Log in & navigate to BPP Trend page", priority = 0, alwaysRun = true)
	public void bppTrendsLoginAndSearchBppTrendInNavApp() throws Exception {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		
		// Creating an instance of BppTrendPage class and initializing all its page objects.
		objLoginPage = new LoginPage(driver);

		// Fetching data from properties file to instantiate class instance variables.
		tableNamesOutsideMoreTab = Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(","));
		tableNamesUnderMoreTab = Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(","));
		rollYear = CONFIG.getProperty("rollYear");

		// Navigating to the salesforce url.
		objPage.navigateTo(driver, (CONFIG.getProperty("URL_qa")));
		ExtentTestManager.getTest().log(LogStatus.INFO, "Browser launched & URL opened: " + (CONFIG.getProperty("URL_qa")));

		// Entering username, password & clicking login button.
		objLoginPage.enterLoginUserName(CONFIG.getProperty("username"));
		objLoginPage.enterLoginPassword(CONFIG.getProperty("password"));
		ExtentTestManager.getTest().log(LogStatus.INFO, "Login details are entered for : " + (CONFIG.getProperty("username")));
		objLoginPage.clickBtnSubmit();
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click the 'Log In to Sandbox' button");
	
		// Asserting whether user has landed on E-File import page.
		String expectedTitle = "E-File Import Tool | Salesforce";
		objPage.wait.until(ExpectedConditions.titleContains(expectedTitle));
		String actualTitle = driver.getTitle();
		CustomHardAssert.assertTrue(actualTitle.equalsIgnoreCase(expectedTitle), "SMAB-T190: Validating user has landed on E-File import page");

		// Creating an instance of BppTrendPage class and initializing all its page objects.
		objBppTrendsPage = new BppTrendPage(driver);
		
		// Clicking app launcer, searching & selecting BPP Trend option.
		objBppTrendsPage.clickAppLauncher();
		objBppTrendsPage.searchForApp("BPP Trend");
		objBppTrendsPage.clickNavOptionFromDropDown("BPP Trend");

		// Asserting whether user has landed on BPP Trend page.
		expectedTitle = "BPP Trend | Salesforce";
		objPage.wait.until(ExpectedConditions.titleContains(expectedTitle));
		actualTitle = driver.getTitle();
		CustomHardAssert.assertTrue(actualTitle.equalsIgnoreCase(expectedTitle), "SMAB-T190: Validating user has landed on home BPP Trend page");
	}

	@Test(enabled = true, description = "Selecting Roll Year", dependsOnMethods = {"bppTrendsLoginAndSearchBppTrendInNavApp"}, priority = 1)
	public void bppTrendsSelectRollYear() throws Exception {
		objBppTrendsPage.clickRollYearDropDown();
		objBppTrendsPage.clickOnGivenRollYear(rollYear);
		objBppTrendsPage.clickBtnSelect();
	}

	@Test(enabled = true, description = "To check whether given tables are displayed outside more tab not for selected roll year", dependsOnMethods = {"bppTrendsSelectRollYear"}, priority=2)
	public void bppTrendsValidateTablesGridDisplay() throws Exception {
		CustomSoftAssert softAssert = new CustomSoftAssert();
		List<WebElement> tablesVisbibleOnPage = objBppTrendsPage.getVisibleTables();
		
		// Assertion to check whether number of tables shown on UI matches the user's expectation
		CustomHardAssert.assertEquals(tablesVisbibleOnPage.size(), tableNamesOutsideMoreTab.size(), "SMAB-T190: Validating tables are visible outside more tab");

		// Creating an ArrayList and adding tables names (tables visible outside more tab on UI) to it
		List<String> tablesNamesFromWebPage = new ArrayList<String>();
		for (WebElement currentTable : tablesVisbibleOnPage) {
			String tableName = currentTable.getText();
			tablesNamesFromWebPage.add(tableName);
		}

		// Asserting the tables names in expected (provided by user) & actual (generated above) lists
		for(String currentExpTblName : tableNamesOutsideMoreTab) {
			softAssert.assertTrue(tablesNamesFromWebPage.contains(currentExpTblName), "SMAB-T190: Validating expected table '" + currentExpTblName + "' is visible on webpage (outside more tab)");	
		}
		softAssert.assertAll();
	}
	
	@Test(enabled = true, description = "BPP trends method to trigger Table wise calculations", dependsOnMethods = {"bppTrendsValidateTablesGridDisplay"}, priority=3)
	public void bppTrendsCalculateTableWise() throws Exception {
		CustomSoftAssert softAssert = new CustomSoftAssert();
		String txtForCalculationStatus = null;
		
		// Iterating on table names list provided by user (tables that are not under more tab)
		for (int i = 0; i < tableNamesOutsideMoreTab.size(); i++) {
			// Clicking on the table name to populate its content
			objBppTrendsPage.clickOnGivenTableName(tableNamesOutsideMoreTab.get(i), true);
			
			// Clicking on calculate button to initiate calculation
			txtForCalculationStatus = objBppTrendsPage.clickCalculateButton(tableNamesOutsideMoreTab.get(i));
			
			// Asserting wether calculation action is successful
			softAssert.assertTrue(txtForCalculationStatus.equalsIgnoreCase("Yet to be submitted for approval"), "SMAB-T190: Is calculation successful for table '" + tableNamesOutsideMoreTab.get(i) + "'");
		}

		// Iterating on table names list provided by user (tables that are available under more tab)
		for (int i = 0; i < tableNamesUnderMoreTab.size(); i++) {
			// Clicking on more tab to select given table from its drop down list
			objBppTrendsPage.moreTabs.click();
			
			objBppTrendsPage.clickOnGivenTableName(tableNamesUnderMoreTab.get(i), false);
			txtForCalculationStatus = objBppTrendsPage.clickCalculateButton(tableNamesUnderMoreTab.get(i));
			softAssert.assertTrue(txtForCalculationStatus.equalsIgnoreCase("Yet to be submitted for approval"), "SMAB-T190: Is calculation successful for table '" + tableNamesOutsideMoreTab.get(i) + "'");
		}
		softAssert.assertAll();
	}
}
