package com.apas.Tests.BppTrend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WebElement;
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
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendCalculateAndSubmitTest extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
	String rollYear;
	
	@BeforeMethod
	public void beforeMethod() throws Exception {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}
	
	@Test(description = "SMAB-T190: Perform calculation for Commercial Composite Factors table", groups = {"regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = false)
	public void verifyBppTrendCalculateCommercialCompositeFactors(String loginUser) throws Exception {
		//Step1: Login with system administrator and reset composite factor tables status to "Not Calculated"
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);
		//Step2: Reset valuation factor tables status to "Yet to be submitted"
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear); 
		objApasGenericFunctions.logout();
		
		//Step3: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step4: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step5: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
		
		//Step6: Validating presence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Calcuate all button is visible");
		
		//Step7: Clicking on the given table
		String tableName = "Commercial Composite Factors";
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
			
		//Step8: Validating presence of calculate button for given tables individually
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is visible");
			
		//Step9: Retrieve message displayed above table before clicking calculate button
		String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
		softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T190: '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
 		
		//Step10: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
		objBppTrnPg.clickCalculateBtn(tableName);
		
		//Step11: Waiting for pop up message to display and the message displayed above table to update
		objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);

		//Step12: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T190: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");
		
		//Step13: Retrieve & Assert updated message  displayed above table
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T190: Calculation successfully performed for table '" + tableName + "'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
		
		//Step14: Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: ReCalcuate button is visible");
		//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
		//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Submit For Approval button is visible");
		
		//Step15: Validating presence of CalculateAll button on performing the calculation
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: CalcuateAll button is visible");
			
		//Step16: Validating presence of ReCalculateAll button on performing the calculation
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: ReCalcuateAll button is visible");
		
		//Step17: Validating absence of Calculate button at table level once calculation is done
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, tableName);
		softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is not visible");
		
		softAssert.assertAll();		
	}

	@Test(description = "SMAB-T190 Perform calculation for Industrial Composite Factors table", groups = {"regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 1, enabled = false)
	public void verifyBppTrendCalculateIndustrialCompositeFactors(String loginUser) throws Exception {		
		//Step1: Validating presence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Calcuate all button is visible");
		
		//Step2: Clicking on the given table
		String tableName = "Industrial Composite Factors";
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
			
		//Step3: Validating presence of calculate button for given tables individually
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is visible");
			
		//Step4: Retrieve message displayed above table before clicking calculate button
		String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
		softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T190: '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
			
		//Step5: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
		objBppTrnPg.clickCalculateBtn(tableName);

		//Step6: Waiting for pop up message to display and the message displayed above table to update
		objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);

		//Step7: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T190: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");
		
		//Step8: Retrieve & Assert updated message  displayed above table
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T190: Calculation successfully performed for table '" + tableName + "'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
		
		//Step9: Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: ReCalcuate button is visible");
		//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
		//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Submit For Approval button is visible");
		
		//Step10: Validating presence of CalculateAll button on performing the calculation
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: CalcuateAll button is visible");
			
		//Step11: Validating presence of ReCalculateAll button on performing the calculation
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: ReCalcuateAll button ia be visible");
		
		//Step12: Validating absence of Calculate button at table level once calculation is done
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, tableName);
		softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is not visible");
		
		softAssert.assertAll();		
	}
	
	@Test(description = "SMAB-T190: Perform calculation for Agricultural Composite Factors table", groups = {"regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 2, enabled = false)
	public void verifyBppTrendCalculateAgriculturalCompositeFactors(String loginUser) throws Exception {		
		//Step1: Validating presence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Calcuate all button is visible");
		
		//Step2: Clicking on the given table
		String tableName = "Agricultural Composite Factors";
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
			
		//Step3: Validating presence of calculate button for given tables individually
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is visible");
			
		//Step4: Retrieve message displayed above table before clicking calculate button
		String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
		softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T190: '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
			
		//Step5: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
		objBppTrnPg.clickCalculateBtn(tableName);

		//Step6: Waiting for pop up message to display and the message displayed above table to update
		objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);

		//Step7: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T190: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");
		
		//Step8: Retrieve & Assert updated message  displayed above table
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T190: Calculation successfully performed for table '" + tableName + "'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
		
		//Step9: Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: ReCalcuate button is visible");
		//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
		//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Submit For Approval button is visible");
		
		//Step10: Validating presence of CalculateAll button on performing the calculation
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: CalcuateAll button is visible");
			
		//Step11: Validating presence of ReCalculateAll button on performing the calculation
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: ReCalcuateAll button is visible");
		
		//Step12: Validating absence of Calculate button at table level once calculation is done
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, tableName);
		softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is not visible");
		
		softAssert.assertAll();		
	}
	
	@Test(description = "SMAB-T190: Perform calculation for Agricultural Mobile Equipment Composite Factors tables", groups = {"regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 3, enabled = false)
	public void verifyBppTrendCalculateAgrMobileEquipFactors(String loginUser) throws Exception {		
		//Step1: Validating presence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Calcuate all button is visible");
		
		//Step2: Clicking on the given table
		String tableName = "Agricultural Mobile Equipment Composite Factors";
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, true);
			
		//Step3: Validating presence of calculate button for given tables individually
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is visible");
			
		//Step4: Retrieve message displayed above table before clicking calculate button
		String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
		softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T190: '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
			
		//Step5: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
		objBppTrnPg.clickCalculateBtn(tableName);

		//Step6: Waiting for pop up message to display and the message displayed above table to update
		objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);

		//Step7: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T190: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");		
	
		//Step8: Retrieve & Assert updated message displayed above table
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T190: Calculation successfully performed for table '" + tableName + "'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
		
		//Step9: Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: ReCalcuate button is visible");
		//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
		//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Submit For Approval button is visible");
		
		//Step10: Validating presence of CalculateAll button on performing the calculation
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: CalcuateAll button is visible");
			
		//Step11: Validating presence of ReCalculateAll button on performing the calculation
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: ReCalcuateAll button is visible");
		
		//Step12: Validating absence of Calculate button at table level once calculation is done
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, tableName);
		softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is not visible");
		
		softAssert.assertAll();		
	}
	
	
	@Test(description = "SMAB-T253: Perform calculation for Construction Composite Factors table", groups = {"smoke","regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 4, enabled = false)
	public void verifyBppTrendCalculateConstructionCompositeFactors(String loginUser) throws Exception {		
		//Step1: Login with system administrator and reset composite factor tables status to "Not Calculated"
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);
		//Step2: Reset valuation factor tables status to "Yet to be submitted"
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear); 
		objApasGenericFunctions.logout();
		
		//Step3: Validating presence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T253: Calcuate all button is visible");
		
		//Step4: Clicking on the given table
		String tableName = "Construction Composite Factors";
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
			
		//Step5: Validating presence of calculate button for given tables individually
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T253: Calcuate button is visible");
			
		//Step6: Retrieve message displayed above table before clicking calculate button
		String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
		softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T253: '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
			
		//Step7: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
		objBppTrnPg.clickCalculateBtn(tableName);

		//Step8: Waiting for pop up message to display and the message displayed above table to update
		objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);

		//Step9: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T253: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");
		
		//Step10: Retrieve & Assert updated message  displayed above table
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T253: Calculation successfully performed for table '" + tableName + "'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
		
		//Step11: Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T253: ReCalcuate button is visible");
		//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
		//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T253: Submit For Approval button is visible");
		
		//Step12: Validating presence of CalculateAll button on performing the calculation
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T253: CalcuateAll button is visible");
			
		//Step13: Validating presence of ReCalculateAll button on performing the calculation
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T253: ReCalcuateAll button is visible");
		
		//Step14: Validating absence of Calculate button at table level once calculation is done
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, tableName);
		softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T253: Calcuate button is not visible");
				
		softAssert.assertAll();
	}
	
	@Test(description = "SMAB-T239: Perform calculation for Construction Mobile Equipment Composite Factors tables", groups = {"smoke","regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 5, enabled = false)
	public void verifyBppTrendCalculateConstMobileEquipFactors(String loginUser) throws Exception {
		//Step1: Validating presence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T239: Calcuate all button is visible");
		
		//Step2: Clicking on the given table
		String tableName = "Construction Mobile Equipment Composite Factors";
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, true);
			
		//Step3: Validating presence of calculate button for given tables individually
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T239: Calcuate button is visible");
			
		//Step4: Retrieve message displayed above table before clicking calculate button
		String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
		softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T239: '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
			
		//Step5: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
		objBppTrnPg.clickCalculateBtn(tableName);

		//Step6: Waiting for pop up message to display and the message displayed above table to update
		objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);
		
		//Step7: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T239: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");
		
		//Step8: Retrieve & Assert updated message  displayed above table
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T239: Calculation successfully performed for table '" + tableName + "'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
		
		//Step9: Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T239: ReCalcuate button is visible");
		//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
		//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T239: Submit For Approval button is visible");
		
		//Step10: Validating Submit All For Approval button is visible on performing calculation for last factor table
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(30);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T239: Submit All Factors For Approval button is visible");

		//Step11: Validating absence of Calculate all button on performing calculation for last composite factor table
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(5);
		softAssert.assertTrue(!isCalculateAllBtnDisplayed, "SMAB-T239: CalcuateAll button is not visible");
			
		//Step12: Validating presence of ReCalculateAll button on performing the calculation
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T239: ReCalcuateAll button is visible");
		
		//Step13: Validating absence of Calculate button at table level once calculation is done
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, tableName);
		softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T239: Calcuate button is not visible");
		
		softAssert.assertAll();		
	}	
	
	@Test(description = "SMAB-T277, SMAB-T577: Perform calculation for BPP Prop 13 table", groups = {"smoke","regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 6, enabled = false)
	public void verifyBppTrendCalculateProp13(String loginUser) throws Exception {
		//Step1: Validating presence of CalculateAll button at page level before
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T277: Calcuate all button is visible");

		ExtentTestManager.getTest().log(LogStatus.INFO, "** Validationg CPI Factor Values In Grid For: BPP Prop 13 Factors Table **");
		//Step2: Clicking on the BPP Prop 13 table
		String tableName = "BPP Prop 13 Factors";
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, true);

		//Step3: Validating presence of calculate button for given table
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "BPP Prop 13 Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T277: Calcuate button is visible");

		//Step4: Enter updated value CPI Factor text-box
		objBppTrnPg.enter(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.cpiFactorTxtBox), CONFIG.getProperty("cpiFactorFirstValue"));
		
		//Step5: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking 'Calculate' Button");
		objBppTrnPg.clickCalculateBtn("BPP Prop 13 Factors");
		//objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.confirmBtnInPopUp));
	
		//Step6: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T277: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");
		
		//Step7: Retrieve & Assert message displayed above table on clicking calculate button
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable("BPP Prop 13 Factors");
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		softAssert.assertTrue(actTableMsgPostCalc.equalsIgnoreCase(expTableMsgPostCalc), "SMAB-T277: Calculation is successful for table 'BPP Prop 13 Factors'");

		//Step8: Retrieve and collect grid CPI Factor data for given roll year in a list
		List<String> gridDataWithFirstCpiFactor = objBppTrnPg.getTextOfMultipleElementsFromProp13Table();
		
		//Step9: Validating presence of ReCalculate button at table level on clicking calculate button
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(20, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T277: ReCalcuate button is visible");

		//Step10: Enter updated value CPI Factor text-box
		//objBppTrnPg.enter(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.cpiFactorTxtBox), CONFIG.getProperty("cpiFactorSecondValue"));
		
		//Step11: Clicking on Recalculate button to re-initiate calculation
		//ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking 'ReCalculate' Button");
		//objBppTrnPg.clickReCalculateBtn("BPP Prop 13 Factors");
		//objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.confirmBtnInPopUp));
		
		//Step12: Retrieve & Assert message displayed above table on clicking Recalculate button
		//actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable("BPP Prop 13 Factors");
		//expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		//softAssert.assertTrue(actTableMsgPostCalc.equalsIgnoreCase(expTableMsgPostCalc), "SMAB-T277: Is Recalculation successful for table 'BPP Prop 13 Factors'");

		//Step13: Retrieve and collect grid CPI Factor data for given roll year in a list
		//List<String> gridDataWithSecondCpiFactor = objBppTrnPg.getTextOfMultipleElements(xpathCpiValues);

		//Step14: Checking whether CPI Factor values have updated on UI when calculation done with updated cPI factor value
		//softAssert.assertTrue(!(gridDataWithFirstCpiFactor.equals(gridDataWithSecondCpiFactor)), "SMAB-T277: Is calculation updated in grid post "
		//		+ "changing the CPI factor value. "+ "gridDataWithFirstCpiFactor: "+ gridDataWithFirstCpiFactor +"  "
		//				+ "||  gridDataWithSecondCpiFactor: "+ gridDataWithSecondCpiFactor);

		//Step15: Retrieving roll year and year acquired values to check roll year values are 1 year greater than acquired year values
		List<WebElement> rollYears = objBppTrnPg.locateElements("//th[@data-label = 'Roll Year']//lightning-formatted-text", 60);
		List<WebElement> yearAcquired = objBppTrnPg.locateElements("//td[@data-label = 'Year Acquired']//lightning-formatted-text", 60);
		for(int i = 0; i < rollYears.size(); i++) {
			int currentRollYear = Integer.parseInt(objBppTrnPg.getElementText(rollYears.get(i)));
			int currentYearAcquired = Integer.parseInt(objBppTrnPg.getElementText(yearAcquired.get(i)));
			boolean yearsComparisonStatus = false;
			if(currentRollYear == (currentYearAcquired + 1)) {
				yearsComparisonStatus = true;
			} 
			softAssert.assertTrue(yearsComparisonStatus, "SMAB-T577: 'Roll Year' is 1 year greatrer than 'Year Acquired'. Roll Year Value: "+ currentRollYear + " || Year Acquired Value: "+ currentYearAcquired);
		}
		
		objApasGenericFunctions.logout();
		softAssert.assertAll();
	}
	
	@Test(description = "SMAB-T190: Perform calculation for factors tables individually", groups = {"smoke","regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 7, enabled = false)
	public void verifyBppTrendCalculateCompositeFactorAndProp13Tables(String loginUser) throws Exception {
		//Step1: Login with system administrator and reset composite factor tables status to "Not Calculated"
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);

		//Step3: Logging out from Bpp Trend Setup screen 
		objApasGenericFunctions.logout();
		
		//Step4: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step5: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step6: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step7: Retrieving the path of excel file to read for data comparison on calculate button click
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_TABLES_DATA;
		
		//Step8: Validating presence of CalculateAll button at page level before any table is accessed
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Calcuate all button is visible");
		
		//Step9: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab");
		
		objBppTrnPg.zoomOutBrowserWindow();
		
		//Step10: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			String tableName = allTables.get(i);
			
			//Step11: Retrieving table data from excel and store in a map
			Map<String, List<Object>> dataMapFromExcel = objBppTrnPg.retrieveDataFromExcelForGivenTable(fileName, tableName);
			System.out.println("Size of dataMapFromExcel: " + dataMapFromExcel.size());
			
			//Step12: Clicking on the given table name
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
			
			//Step13: Validating presence of calculate button for given tables individually
			boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
			softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Calcuate button is visible for "+ tableName +" table");
			
			//Step14: Retrieve message displayed above table before clicking calculate button
			String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
			softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T190: '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
			
			//Step15: Clicking on calculate button to initiate calculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
			objBppTrnPg.clickCalculateBtn(tableName);
			
			//Step16: Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);
			
			//Step17: Validation to check whether calculation is successful and table data appears for given table
			boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
			softAssert.assertTrue(isTableVisible, "SMAB-T190: Tabular data visible successfully on clicking calculate button for table '" + tableName + "'");
			
			//Step18: Retrieve & Assert updated message  displayed above table
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
			softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T190: Calculation successfully performed for table '" + tableName + "'");
			
			//Step19: Retrieving grid data into a map			
			List<String> columnNames = objBppTrnPg.retrieveColumnNamesOfGridForGivenTable(tableName);
			
			//Step20: Retrieving column names of table from UI
			Map<String, List<Object>> dataMapFromUI = objBppTrnPg.retrieveDataFromGridForGivenTable(tableName);
			System.out.println("Size of dataMapFromUI: " + dataMapFromUI.size());
			
			//Step21: Comparing the UI grid data after Calculate button click against the data in excel file
			System.setProperty("isElementHighlightedDueToFailre", "false");
			
			//Step22: Scrolling to the bottom of the page
			objBppTrnPg.scrollToBottomOfPage();
			objBppTrnPg.scrollToElement(objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false));
			
			//Step23: Validating the tabular data against data retrieved from excel file
			for (Map.Entry<String, List<Object>> entry : dataMapFromExcel.entrySet()) {
				String currentKey = entry.getKey().toString();
				if (dataMapFromUI.containsKey(currentKey)) {
					List<Object> acquiredYearDataFromUI = dataMapFromUI.get(currentKey);
					List<Object> acquiredYearDataFromExcel = dataMapFromExcel.get(currentKey);
					
					if (acquiredYearDataFromUI.size() == acquiredYearDataFromExcel.size()) {
						for (int j = 0; j < acquiredYearDataFromExcel.size(); j++) {
							if (!(acquiredYearDataFromExcel.get(j).equals(acquiredYearDataFromUI.get(j)))) {
								objBppTrnPg.highlightMismatchedCellOnUI(tableName, currentKey, j);
								softAssert.assertTrue(false, "SMAB-T190: Data for '"+ tableName +" 'for year acquired '" + currentKey + "' and column named '"
								+ columnNames.get(j) + "' does not match. Excel data: "+ acquiredYearDataFromExcel.get(j) + " || UI Data: " + acquiredYearDataFromUI.get(j), true);
							}
						}
					} else {
						softAssert.assertTrue(false, "SMAB-T190: Size of data list for year acquired '"+ currentKey +"' does not match.", true);
					}
				} else {
					softAssert.assertTrue(false, "SMAB-T190: Data for year acquired '" + currentKey +"' is not available in UI table.", true);
				}
			}
			if(System.getProperty("isElementHighlightedDueToFailre").equalsIgnoreCase("true")) {
				System.out.println("Taking screen shot after all comparison is done");
				softAssert.assertTrue(false, "Excel & UI grid data has mismatched. Taking screen shot of entire table");
			}
		}
		//Step24: Zoom into the browser window to reset web-page size.
		objBppTrnPg.zoomInBrowserWindow();
		
		softAssert.assertAll();
	}

	@Test(description = "SMAB-T195: Perform calculation for factors tables individually", groups = {"smoke","regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 8, enabled = true)
	public void verifyBppTrendReCalculateCompositeFactorTables(String loginUser) throws Exception {
		//Step1: Login with system administrator & navigate to Bpp Trend Setup Page
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step2: Reset composite factor tables status to "Calculated"
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);		
		
		//Step3: Logging out from Bpp Trend Setup screen 
		objApasGenericFunctions.logout();
		
		//Step4: Login with given user & navigate to Bpp Trend Setup Page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);

		//Step5: Generate data map containing values of bpp composite factor settings and edit composite factor setting values
		String[] bppCompositeSettingValues = CONFIG.getProperty("bppCompositeSettingValues").split(",");
		Map<String, String> updatedSettingValues = new HashMap<String, String>();
		updatedSettingValues.put("Commercial", bppCompositeSettingValues[0]);
		updatedSettingValues.put("Industrial", bppCompositeSettingValues[1]);
		updatedSettingValues.put("Construction", bppCompositeSettingValues[2]);
		updatedSettingValues.put("Agricultural", bppCompositeSettingValues[3]);
		
		//Step6: Edit the values from view all page
		objBppTrnPg.editBppCompositeFactorValueOnViewAllPage(updatedSettingValues);
				
		//Step7: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		
		//Step8: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step9: Retrieving the path of excel file to read for data comparison on calculate button click
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_TABLES_DATA;
		
		//Step10: Validating presence of ReCalculateAll button at page level before any table is accessed
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: ReCalcuate all button is visible");
		
		//Step11: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		
		objBppTrnPg.zoomOutBrowserWindow();
		
		//Step12: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			String tableName = allTables.get(i);
			
			//Step13: Updating trend settings values & retrieving table data from excel and store in a map
			objBppTrnPg.updateTrendSettingInExcel(fileName, updatedSettingValues);
			Map<String, List<Object>> dataMapFromExcel = objBppTrnPg.retrieveDataFromExcelForGivenTable(fileName, tableName);
			System.out.println("Size of dataMapFromExcel: " + dataMapFromExcel.size());
			
			//Step14: Clicking on the given table name
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
						
			//Step15: Retrieve message displayed above table before clicking ReCalculate button
			String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeReCalculation");
			softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T195: '"+ expTableMsgBeforeCalc +"' displayed above table before ReCalculation is initiated" + tableName + "'");
			
			//Step16: Clicking on ReCalculate button to initiate calculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'ReCalculate' Button ");
			objBppTrnPg.clickReCalculateBtn(tableName);
			
			//Step17: Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);
			
			//Step18: Validation to check whether calculation is successful and table data appears for given table
			boolean isTableVisible = objBppTrnPg.isTableVisibleOnCalculateClick(tableName);
			softAssert.assertTrue(isTableVisible, "SMAB-T195: Tabular data visible successfully on clicking ReCalculate button for table '" + tableName + "'");
			
			//Step19: Retrieve & Assert updated message displayed above table
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
			softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T195: ReCalculation successfully performed for table '" + tableName + "'");
			
			//Step20: Validating presence of ReCalculate button at table level on clicking calculate button
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T195: ReCalcuate button is visible for table '" + tableName + "'");

			//Step21: Retrieving grid data into a map			
			List<String> columnNames = objBppTrnPg.retrieveColumnNamesOfGridForGivenTable(tableName);
			
			//Step22: Retrieving column names of table from UI
			Map<String, List<Object>> dataMapFromUI = objBppTrnPg.retrieveDataFromGridForGivenTable(tableName);
			System.out.println("Size of dataMapFromUI: " + dataMapFromUI.size());
			
			//Step23: Comparing the UI grid data after Calculate button click against the data in excel file
			System.setProperty("isElementHighlightedDueToFailre", "false");
			
			//Step24: Scrolling to the bottom of the page
			objBppTrnPg.scrollToBottomOfPage();
			objBppTrnPg.scrollToElement(objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false));
			
			//Step25: Validating the tabular data against data retrieved from excel file
			for (Map.Entry<String, List<Object>> entry : dataMapFromExcel.entrySet()) {
				String currentKey = entry.getKey().toString();
				if (dataMapFromUI.containsKey(currentKey)) {
					List<Object> acquiredYearDataFromUI = dataMapFromUI.get(currentKey);
					List<Object> acquiredYearDataFromExcel = dataMapFromExcel.get(currentKey);
					
					if (acquiredYearDataFromUI.size() == acquiredYearDataFromExcel.size()) {
						for (int j = 0; j < acquiredYearDataFromExcel.size(); j++) {
							if (!(acquiredYearDataFromExcel.get(j).equals(acquiredYearDataFromUI.get(j)))) {
								objBppTrnPg.highlightMismatchedCellOnUI(tableName, currentKey, j);
								softAssert.assertTrue(false, "SMAB-T195: Data for '"+ tableName +" 'for year acquired '" + currentKey + "' and column named '"
								+ columnNames.get(j) + "' does not match. Excel data: "+ acquiredYearDataFromExcel.get(j) + " || UI Data: " + acquiredYearDataFromUI.get(j), true);
							}
						}
					} else {
						softAssert.assertTrue(false, "SMAB-T195: Size of data list for year acquired '"+ currentKey +"' does not match.", true);
					}
				} else {
					softAssert.assertTrue(false, "SMAB-T195: Data for year acquired '" + currentKey +"' is not available in UI table.", true);
				}
			}
			if(System.getProperty("isElementHighlightedDueToFailre").equalsIgnoreCase("true")) {
				System.out.println("Taking screen shot after all comparison is done");
				softAssert.assertTrue(false, "Excel & UI grid data has mismatched. Taking screen shot of entire table");
			}
		}
		//Step26: Zoom into the browser window to reset web-page size.
		objBppTrnPg.zoomInBrowserWindow();
				
		//Step27: Reverting the values of Bpp composite factor settings from view all page
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		objBppTrnPg.editBppCompositeFactorValueOnViewAllPage(objBppTrnPg.trendSettingsOriginalValues);
		
		softAssert.assertAll();
	}	

	@Test(description = "SMAB-T191,SMAB-T241,SMAB-T255: Perform calculation for all factor tables in one go", groups = {"smoke","regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 9, enabled = true)
	public void verifyBppTrendCalculateAll(String loginUser) throws Exception {
		//Step1: Resetting the composite factor tables status to Not Calculated
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);
		
		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step3: Logging out from Bpp Trend Setup screen 
		objApasGenericFunctions.logout();
		Thread.sleep(2000);		
		
		//Step4: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step5: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step6: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step7: Validating presence of CalculateAll at page level.
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(20);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T191: Calcuate all button is visible");

		//Step8: Clicking on Calculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate all' button");
		objBppTrnPg.clickCalculateAllBtn();
		
		//Step9: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnCalculateAllClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterCalculateAll");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T191: Calculation successfully performed for all tables");
		
		//Step10: Validating presence of ReCalculate All & Submit All For Approval buttons at page level on clicking Calculate all button
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T191: Submit All Factors For Approval button is visible");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: ReCalcuateAll button is visible");
		
		//Step11: Validating absence of CalculateAll at page level.
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(5);
		softAssert.assertTrue(!isCalculateAllBtnDisplayed, "SMAB-T191: Calcuate all button is not visible");
		
		//Step11: Validating status of composite factor tables on Bpp Trend Setup details page post completing calculation
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();

		String statusInBppTrendSetup = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage("Const. Mobile Equipment Trends Status");
		softAssert.assertTrue(statusInBppTrendSetup.equals("Calculated"), "SMAB-T241: Const. Mobile Equipment Trends Status on Bpp Trend Setup page post calculation is displayed as 'Calculated'");

		statusInBppTrendSetup = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage("Const. Trends Status");
		softAssert.assertTrue(statusInBppTrendSetup.equals("Calculated"), "SMAB-T255: Const. Trends status on Bpp Trend Setup page post calculation is displayed as 'Calculated'");
	}


	@Test(description = "SMAB-T191: Perform ReCalculation for all factor tables in one go", groups = {"regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 10, enabled = false)
	public void verifyBppTrendReCalculateAll(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of ReCalculate All & Submit All For Approval buttons at page level once data has been calculated by clicking Calculate all button
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: ReCalcuateAll button is visible");

		//Step5: Clicking on ReCalculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Recalculate all' button");
		objBppTrnPg.clickReCalculateAllBtn();
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp));
		
		//Step6: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnCalculateAllClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterCalculateAll");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T191: ReCalculation successfully performed for all tables");
		
		//Step7: Validating presence of ReCalculate All & Submit All For Approval buttons at page level after performing ReCalculation
		isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: ReCalcuateAll button is visible");
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T191: Submit All Factors For Approval button is visible");
	}

	
	@Test(description = "SMAB-T442: Sumbit calculations for approval for composite & valuation tables individually", groups = {"regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 11, enabled = false)
	public void verifyBppTrendSubmitForApprovalForCompositeAndValuationTables(String loginUser) throws Exception {
		//Step1: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(",")));
		//Step2: Fetch composite factor table names from properties file and collect them in a single list		
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(",")));
		
		//Step3: Navigate to bpp trend setup page before approving
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step4: Iterate over composite factor tables and validate the status
		String tableName;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step5: Iterate over valuation factor tables and validate the status
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to be Submit for approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		
		//Step6: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step7: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step8: Validating presence of ReCalculateAll & SubmitAllForApproval button at page level before any table is accessed
		boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T442: Recalcuate all button is visible");
		boolean	isSubmitAllForApprovalBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllForApprovalBtnDisplayed, "SMAB-T442: Submit All Factors For Approval button is visible");
		
		//Step9: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");
		
		//Step10: Iterating over the given tables
		for (int i = 0; i < allTables.size()-1; i++) {
			tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			//Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
															
			//Clicking on Submit For Approval button to submit the Recalculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Submit For Approval' Button");
			objBppTrnPg.clickSubmitForApprovalBtn(tableName);

			//Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnSubmitForApprovalClick(180);		
			
			//Retrieve & Assert message displayed above table on clicking submit for approval button
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostSubmitForApproval");
			softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T442: Calculated data submitted for approval successfully");
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
			
			//Validating absence of ReCalculate & Submit For Approval buttons at table level on clicking submit for approval button
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(10, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T442: Is ReCalcuate button is not visible");
			boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(10, tableName);
			softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T442: Submit For Approval button is not visible");
		
			//Validating absence of ReCalculate All button at page level on clicking submit for approval button
			isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(5);
			softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T442: ReCalcuateAll button is not visible");
		}
		
		//Step11: Iterate over composite factor tables and validate the status
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step12: Iterate over valuation factor tables and validate the status
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		softAssert.assertAll();
	}
    
	@Test(description = "SMAB-T442: Sumbit calculations for approval for all factor tables in one go",groups = {"smoke,regression"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 12, enabled = true)
	public void verifyBppTrendSubmitAllForApproval(String loginUser) throws Exception {	
		//Step1: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(",")));
		//Step2: Fetch composite factor table names from properties file and collect them in a single list		
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(",")));
				
		//Step3: Navigate to bpp trend setup page before approving
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
			
		//Step4: Iterate over composite factor tables and validate the status
		String tableName;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		
		//Step5: Iterate over valuation factor tables and validate the status
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to be Submit for approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		
		//Step6: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step7: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step8: Checking whether Submit all For Approval button is visible
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T442: Submit All Factors For Approval button is visible");
		
		//Step9: Clicking on Submit all For Approval button to submit calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Submit All Factors For Approval' button");
		objBppTrnPg.clickSubmitAllFactorsForApprovalBtn();
		
		//Step10: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnSubmitAllForApprovalClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterSubmitAllForApproval");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T442: Calculation successfully submitted for approval for all tables");
		
		//Step11: Validating message displayed at page level on clicking submit all factors for approval
		String expMsgOnSubmitForApp = CONFIG.getProperty("pageLevelMsgPostSubmitForApproval");
		String actMsgOnSubmitForApp = objBppTrnPg.getElementText(objBppTrnPg.pageLevelMsg);
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T442: Calculations for all tables submitted successfully");
		
		//Step12: Validating absence of ReCalculate All & Submit All Factors For Approval buttons at page level on clicking ReCalculate all button
		isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(5);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T442: Submit All Factors For Approval button is not visible");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(5);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T442: ReCalcuateAll button is not visible");

		//Step13: Iterate over composite factor tables and validate the status
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step14: Iterate over valuation factor tables and validate the status
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		
		softAssert.assertAll();
	}
}