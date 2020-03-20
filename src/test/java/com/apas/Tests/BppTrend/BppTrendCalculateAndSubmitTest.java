package com.apas.Tests.BppTrend;

import java.util.ArrayList;
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

public class BppTrendCalculateAndSubmitTest extends TestBase {
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
	
	@Test(description = "SMAB-T190,SMAB-T239: Perform calculation for factors tables individually", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verifyBppTrendCalculateFlow(String loginUser) throws Exception {
		//Resetting the factor tables status to Not Calculated
		String rollYear = CONFIG.getProperty("rollYear");
		List<String> factorTablesToReset = Arrays.asList(CONFIG.getProperty("factorTablesToReset").split(","));
		objBppTrnPg.resetStatusForFactorTables(factorTablesToReset, "Not Calculated", rollYear);
		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of CalculateAll button at page level before any table is accessed
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Calculate all", 20);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Is 'Calcuate all' button visible when no calculation has been initiated:");
		
		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");
		
		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<<<<<<< Performing Validations For: '"+ allTables.get(i) +"' Table >>>>>>>>>>");
			// Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			objBppTrnPg.clickOnTableName(allTables.get(i), isTableUnderMoreTab);
			
			// Validating presence of calculate button for given tables individually
			boolean isCalculateBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Calculate", 30, allTables.get(i));
			softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Is Calcuate button visible for given table");
			
			// Retrieve message displayed above table before clicking calculate button
			String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(allTables.get(i));
			String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
			softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T190: Is correct msg displayed above table before calculation is initiated '" + allTables.get(i) + "'");
			
			// Clicking on calculate button to initiate calculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<< Clicking 'Calculate' Button >>>>>");
			objBppTrnPg.clickRequiredButton("Calculate", allTables.get(i));
			
			// Retrieve & Assert message displayed above table on clicking calculate button
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(allTables.get(i));
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
			softAssert.assertTrue(actTableMsgPostCalc.equalsIgnoreCase(expTableMsgPostCalc), "SMAB-T190: Is calculation successful for table '" + allTables.get(i) + "'");
			
			// Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
			boolean isReCalculateBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Recalculate", 20, allTables.get(i));
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: Is ReCalcuate button visible when calculate button is clicked:");
			//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit For Approval", 20, allTables.get(i));
			//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Is Submit For Approval button visible when calculate button is clicked:");
			
			// Validating presence of CalculateAll, ReCalculateAll and Submit All For Approval buttons at page level on clicking calculate button
			if(i < (allTables.size()-1)) {				
				// Validating presence CalculateAll button on performing the calculation
				isCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Calculate all", 20);
				softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Is CalcuateAll button visible on initiating calculation even for one table:");
			} else {
				// Validating Submit All For Approval button is visible on performing calculation for last factor table
				boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit All Factors for Approval", 20);
				softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T190: Is Submit All Factors For Approval button visible on initiating calculation for last factor table:");
				// Validating Calculate all button is not visible on performing calculation for last factor table
				isCalculateAllBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("Calculate all", 10);
				softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Is CalcuateAll button not visible on initiating calculation for last factor table:");				
			}
			
			boolean isReCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("ReCalculate all", 20);
			softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: Is ReCalcuateAll button visible:");

			// Validating absence of Calculate button at table level once calculation is done
			isCalculateBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("Calculate", 10, allTables.get(i));
			softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Is Calcuate button no longer visible:");						
		}
		softAssert.assertAll();		
	}

	@Test(description = "SMAB-T195: Perform Recalculation for factor tables individually", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verifyBppTrendReCalculateFlow(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of ReCalculateAll & SubmitAllForApproval button at page level before any table is accessed
		boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("ReCalculate all", 20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T195: Is 'Recalcuate all' button visible when calculation for all factors tables is done:");
		boolean	isSubmitAllForApprovalBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit All Factors for Approval", 20);
		softAssert.assertTrue(isSubmitAllForApprovalBtnDisplayed, "SMAB-T195: Is 'Submit All For Approval' button visible when calculation for all factors tables is done:");
		
		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");
		
		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<<<<<<< Performing Validations For: '"+ allTables.get(i) +"' Table >>>>>>>>>>");
			// Clicking on the given table name			
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			objBppTrnPg.clickOnTableName(allTables.get(i), isTableUnderMoreTab);
			
			// Validating presence of Recalculate button given table individually
			boolean isReCalculateBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Recalculate", 30, allTables.get(i));
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T195: Is ReCalcuate button visible for given table:");
			
			// Clicking on Recalculate button and Confirm button in pop up window to ReInitiate calculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<< Clicking 'ReCalculate' Button >>>>>");
			objBppTrnPg.clickRequiredButton("Recalculate", allTables.get(i));
			objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp));
			
			// Retrieve & Assert message displayed above table on clicking Recalculate button
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(allTables.get(i));
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
			softAssert.assertTrue(actTableMsgPostCalc.equalsIgnoreCase(expTableMsgPostCalc), "SMAB-T195: Is Recalculation successful for table '" + allTables.get(i) + "'");
			
			// Validating presence of ReCalculate All & Submit All For Approval buttons at page level on clicking calculate button
			boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit All Factors for Approval", 30);
			softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T195: Is Submit All Factors For Approval button visible:");
			isReCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("ReCalculate all", 30);
			softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T195: Is ReCalcuateAll button visible:");
			
			// Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking Recalculate button
			isReCalculateBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Recalculate", 30, allTables.get(i));
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T195: Is ReCalcuate button visible:");
			//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit For Approval", 20, allTables.get(i));
			//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T195: Is Submit For Approval button visible :");
		}
		softAssert.assertAll();
	}
		
	@Test(description = "SMAB-T191: Perform calculation for all factor tables in one go", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 2, enabled = true)
	public void verifyBppTrendCalculateAllFlow(String loginUser) throws Exception {
		//Resetting the factor tables status to Not Calculated
		String rollYear = CONFIG.getProperty("rollYear");
		List<String> factorTablesToReset = Arrays.asList(CONFIG.getProperty("factorTablesToReset").split(","));
		objBppTrnPg.resetStatusForFactorTables(factorTablesToReset, "Not Calculated", rollYear);
		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of CalculateAll at page level.
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Calculate all", 20);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T191: Is 'Calcuate all' button visible when user has select a roll year:");

		//Step5: Clicking on Calculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<< Clicking 'Calculate all' button >>>>>");
		objBppTrnPg.clickRequiredButton("Calculate all");
		
		//Step6: Validating presence of ReCalculate All & Submit All For Approval buttons at page level on clicking Calculate all button
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit All Factors for Approval", 20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T191: Is Submit All Factors For Approval button visible:");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("ReCalculate all", 20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: Is ReCalcuateAll button visible:");
		
		//Step7: Validating absence of CalculateAll at page level.
		isCalculateAllBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("Calculate all", 20);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T191: Is 'Calcuate all' button visible:");
	}

	@Test(description = "SMAB-T191: Perform ReCalculation for all factor tables in one go", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 3, enabled = true)
	public void verifyBppTrendReCalculateAllFlow(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of ReCalculate All & Submit All For Approval buttons at page level once data has been calculated by clicking Calculate all button
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("ReCalculate all", 20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: Is ReCalcuateAll button visible:");

		//Step5: Clicking on ReCalculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<< Clicking 'Recalculate all' button >>>>>");
		objBppTrnPg.clickRequiredButton("ReCalculate all");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp));
		
		//Step6: Validating presence of ReCalculate All & Submit All For Approval buttons at page level after performing ReCalculation
		isReCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("ReCalculate all", 20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: Is ReCalcuateAll button visible:");
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit All Factors for Approval", 20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T191: Is Submit All Factors For Approval button visible:");
	}

	@Test(description = "SMAB-T190: Sumbit calculations for approval for factor tables individually", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 4, enabled = false)
	public void verifyBppTrendSubmitForApprovalFlow(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of ReCalculateAll & SubmitAllForApproval button at page level before any table is accessed
		boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("ReCalculate all", 20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: Is 'Recalcuate all' button visible when calculation for all factors tables is done:");
		boolean	isSubmitAllForApprovalBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit All Factors for Approval", 20);
		softAssert.assertTrue(isSubmitAllForApprovalBtnDisplayed, "SMAB-T190: Is 'Submit All For Approval' button visible when calculation for all factors tables is done:");
		
		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");
		
		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size()-1; i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<<<<<<< Performing Validations For: '"+ allTables.get(i) +"' Table >>>>>>>>>>");
			// Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			objBppTrnPg.clickOnTableName(allTables.get(i), isTableUnderMoreTab);
															
			// Clicking on Submit For Approval button to submit the Recalculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<< Clicking 'Submit For Approval' Button >>>>>");
			objBppTrnPg.clickRequiredButton("Submit For Approval", allTables.get(i));

			// Retrieve & Assert message displayed above table on clicking submit for approval button
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(allTables.get(i));
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostSubmitForApproval");
			softAssert.assertTrue(actTableMsgPostCalc.equalsIgnoreCase(expTableMsgPostCalc), "SMAB-T190: Is Recalculated data submitted for approval successfully:");
			
			// Validating absence of ReCalculate & Submit For Approval buttons at table level on clicking submit for approval button
			boolean isReCalculateBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("Recalculate", 10, allTables.get(i));
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: Is ReCalcuate button no longer visible");
			boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("Submit For Approval", 10, allTables.get(i));
			softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Is Submit For Approval button no longer visible");
		
			// Validating absence of ReCalculate All button at page level on clicking submit for approval button
			isReCalculateAllBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("ReCalculate all", 10);
			softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: Is ReCalcuateAll button no longer visible");
		}
		softAssert.assertAll();
	}
    
	@Test(description = "Sumbit calculations for approval for all factor tables in one go", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 5, enabled = true)
	public void verifyBppTrendSubmitAllForApprovalWorkFlow(String loginUser) throws Exception {	
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Checking whether Submit all For Approval button is visible Or not
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.checkAvailabilityOfRequiredButton("Submit All Factors for Approval", 20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T190: Is Submit All Factors For Approval button no longer visible:");
		
		//Step5: Clicking on Submit all For Approval button to submit calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "<<<<< Clicking 'Submit All Factors For Approval' button >>>>>");
		objBppTrnPg.clickRequiredButton("Submit All Factors for Approval");
		
		//Step6: Validating message displayed at page level on clicking submit all factors for approval
		String expMsgOnSubmitForApp = CONFIG.getProperty("pageLevelMsgPostSubmitForApproval");
		String actMsgOnSubmitForApp = objBppTrnPg.getElementText(objBppTrnPg.pageLevelMsg);
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T190: Are calculations for all tables submitted successfully:");
		
		//Step7: Validating absence of ReCalculate All & Submit All Factors For Approval buttons at page level on clicking ReCalculate all button
		isSubmitAllFactorsBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("Submit All Factors for Approval", 20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T190: Is Submit All Factors For Approval button no longer visible:");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.checkUnAvailabilityOfRequiredButton("ReCalculate all", 20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: Is ReCalcuateAll button no longer visible:");

		softAssert.assertAll();
	}
}