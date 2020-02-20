package com.apas.Tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.HardAssertion;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.apps;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.SalsesforceStandardFunctions;

public class BppTrendTest extends TestBase implements testdata, apps, users {
	private RemoteWebDriver driver;
	BppTrendPage objBppTrendsPage;
	Page objPage;
	SalsesforceStandardFunctions salesforceStandardFunctions;
	Util objUtil;
	SoftAssertion softAssert;
	List<String> tableNamesOutsideMoreTab;
	List<String> tableNamesUnderMoreTab;
	String rollYear;

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		salesforceStandardFunctions = new SalsesforceStandardFunctions(driver);
		objBppTrendsPage = new BppTrendPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}
	
	@AfterTest
	public void afterTest() throws IOException{
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}
	
	@Test(description = "SMAB-T190: Log in & navigate to BPP Trend page", priority = 0, alwaysRun = true)
	public void bppTrendsLoginAndSearchBppTrendInNavApp() throws Exception {
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(BPP_TRENDS);
		
		// Fetching data from properties file to instantiate class instance variables.
		tableNamesOutsideMoreTab = Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(","));
		tableNamesUnderMoreTab = Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(","));
		rollYear = CONFIG.getProperty("rollYear");
	
		// Asserting whether user has landed on E-File import page.
		String expectedTitle = "E-File Import Tool | Salesforce";
		objPage.wait.until(ExpectedConditions.titleContains(expectedTitle));
		String actualTitle = driver.getTitle();
		HardAssertion.assertTrue(actualTitle.equalsIgnoreCase(expectedTitle), "SMAB-T190: Validating user has landed on E-File import page");

		expectedTitle = "BPP Trend | Salesforce";
		objPage.wait.until(ExpectedConditions.titleContains(expectedTitle));
		actualTitle = driver.getTitle();
		HardAssertion.assertTrue(actualTitle.equalsIgnoreCase(expectedTitle), "SMAB-T190: Validating user has landed on home BPP Trend page");
	}

	@Test(enabled = true, description = "Selecting Roll Year", dependsOnMethods = {"bppTrendsLoginAndSearchBppTrendInNavApp"}, priority = 1)
	public void bppTrendsSelectRollYear() throws Exception {
		objBppTrendsPage.clickRollYearDropDown();
		objBppTrendsPage.clickOnGivenRollYear(rollYear);
		objBppTrendsPage.clickBtnSelect();
	}

	@Test(enabled = true, description = "To check whether given tables are displayed outside more tab not for selected roll year", dependsOnMethods = {"bppTrendsSelectRollYear"}, priority=2)
	public void bppTrendsValidateTablesGridDisplay() throws Exception {
		SoftAssertion softAssert = new SoftAssertion();
		List<WebElement> tablesVisbibleOnPage = objBppTrendsPage.getVisibleTables();
		
		// Assertion to check whether number of tables shown on UI matches the user's expectation
		HardAssertion.assertEquals(tablesVisbibleOnPage.size(), tableNamesOutsideMoreTab.size(), "SMAB-T190: Validating tables are visible outside more tab");

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
	}
	
	@Test(enabled = true, description = "BPP trends method to trigger Table wise calculations", dependsOnMethods = {"bppTrendsValidateTablesGridDisplay"}, priority=3)
	public void bppTrendsCalculateTableWise() throws Exception {
		SoftAssertion softAssert = new SoftAssertion();
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
	}
}
