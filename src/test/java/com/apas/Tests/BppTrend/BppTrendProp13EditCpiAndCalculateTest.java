package com.apas.Tests.BppTrend;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendProp13EditCpiAndCalculateTest extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;

	@BeforeMethod
	public void beforeMethod() {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		objApasGenericFunctions.logout();
	}

	@Test(description = "SMAB-T277: Perform calculation for factors tables individually", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verifyBppTrendCalculateFlow(String loginUser) throws Exception {
		// Resetting the factor tables status to Not Calculated
		String rollYear = CONFIG.getProperty("rollYear");
		List<String> factorTablesToReset = Arrays.asList(CONFIG.getProperty("factorTablesToReset").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(factorTablesToReset, "Not Calculated", rollYear);

		// Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		// Step3: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		ExtentTestManager.getTest().log(LogStatus.INFO, "<<<< Validationg CPI Factor Values In Grid For: BPP Prop 13 Factors Table >>>>");
		// Clicking on the BPP Prop 13 table
		objBppTrnPg.clickOnTableOnBppTrendPage("BPP Prop 13 Factors", CONFIG.getProperty("tableNamesUnderMoreTab").contains("BPP Prop 13 Factors"));

		// Validating presence of calculate button for given table
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "BPP Prop 13 Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T277: Calcuate button should be visible for given table");

		// Enter updated value CPI Factor text-box
		objBppTrnPg.enter(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.cpiFactorTxtBox), CONFIG.getProperty("cpiFactorFirstValue"));
		
		// Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking 'Calculate' Button");
		objBppTrnPg.clickCalculateBtn("BPP Prop 13 Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.confirmBtnInPopUp));
		
		// Retrieve & Assert message displayed above table on clicking calculate button
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable("BPP Prop 13 Factors");
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertTrue(actTableMsgPostCalc.equalsIgnoreCase(expTableMsgPostCalc), "SMAB-T277: Is calculation successful for table 'BPP Prop 13 Factors'");

		// Retrieve and collect grid CPI Factor data for given roll year in a list
		String xpathCpiValues = "//lightning-tab[contains(@data-id, 'BPP Prop 13 Factors')]//table"
				+ "//tbody//th//lightning-formatted-text[text() = '"+ rollYear +"']//ancestor::th"
				+ "//following-sibling::td[contains(@data-label, 'CPI Factor')]";
		
		List<String> gridDataWithFirstCpiFactor = objBppTrnPg.getTextOfMultipleElements(xpathCpiValues);
		
		// Validating presence of ReCalculate button at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(20, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T277: ReCalcuate button should be visible when calculate button is clicked:");

		// Enter updated value CPI Factor text-box
		objBppTrnPg.enter(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.cpiFactorTxtBox), CONFIG.getProperty("cpiFactorSecondValue"));
		
		// Clicking on Recalculate button to re-initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking 'Calculate' Button");
		objBppTrnPg.clickReCalculateBtn("BPP Prop 13 Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.confirmBtnInPopUp));
		
		// Retrieve & Assert message displayed above table on clicking Recalculate button
		actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable("BPP Prop 13 Factors");
		expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertTrue(actTableMsgPostCalc.equalsIgnoreCase(expTableMsgPostCalc), "SMAB-T277: Is Recalculation successful for table 'BPP Prop 13 Factors'");

		// Retrieve and collect grid CPI Factor data for given roll year in a list
		List<String> gridDataWithSecondCpiFactor = objBppTrnPg.getTextOfMultipleElements(xpathCpiValues);

		// Checking whether CPI Factor values have updated on UI when calculation done with updated cPI factor value
		softAssert.assertTrue(!(gridDataWithFirstCpiFactor.equals(gridDataWithSecondCpiFactor)), "SMAB-T277: Is calculation updated in grid post "
				+ "changing the CPI factor value. "+ "gridDataWithFirstCpiFactor: "+ gridDataWithFirstCpiFactor +"  "
						+ "||  gridDataWithSecondCpiFactor: "+ gridDataWithSecondCpiFactor);
		
		softAssert.assertAll();
	}
}