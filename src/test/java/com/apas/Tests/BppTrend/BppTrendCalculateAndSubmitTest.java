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
		Thread.sleep(3000);
	}
	
	@Test(description = "SMAB-T190,SMAB-T239: Perform calculation for factors tables individually", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verifyBppTrendCalculate(String loginUser) throws Exception {
		//Resetting the factor tables status to Not Calculated
		String rollYear = CONFIG.getProperty("rollYear");
		//List<String> factorTablesToReset = Arrays.asList(CONFIG.getProperty("factorTablesToReset").split(","));
		//objBppTrnPg.resetTablesStatusForGivenRollYear(factorTablesToReset, "Not Calculated", rollYear);
		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of CalculateAll button at page level before any table is accessed
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: Calcuate all button should be visible when none of the tables have been calculated");
		
		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");
		
		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			//Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
			
			//Validating presence of calculate button for given tables individually
			boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
			softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Calcuate button should be visible for selected table");
			
			//Retrieve message displayed above table before clicking calculate button
			String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
			softAssert.assertTrue(actTableMsgBeforeCalc.equalsIgnoreCase(expTableMsgBeforeCalc), "SMAB-T190: Is '"+ expTableMsgBeforeCalc +"' displayed above table before calculation is initiated" + tableName + "'");
			
			//Clicking on calculate button to initiate calculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button ");
			objBppTrnPg.clickCalculateBtn(tableName);

			//Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);
			
			//Retrieve & Assert updated message  displayed above table
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
			softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T190: Calculation successfully performed for table '" + tableName + "'");

			ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
			
			//Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking calculate button
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: ReCalcuate button should be visible once calculation is done:");
			//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
			//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Is Submit For Approval button visible when calculate button is clicked:");
			
			if(i < (allTables.size()-1)) {				
				//Validating presence CalculateAll button on performing the calculation
				isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
				softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T190: CalcuateAll button should visible on completing calculation even given table:");
			} else {
				//Validating Submit All For Approval button is visible on performing calculation for last factor table
				boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(30);
				softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T190: Submit All Factors For Approval button visible once calculation for last factor table is done:");

				//Validating Calculate all button is not visible on performing calculation for last factor table
				isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(5);
				softAssert.assertTrue(!isCalculateAllBtnDisplayed, "SMAB-T190: CalcuateAll button should not visible on completing calculation for last factor table:");				
			}
			
			boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
			softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: ReCalcuateAll button should be visible once calculation is done");
			
			//Validating absence of Calculate button at table level once calculation is done
			isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, tableName);
			softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T190: Calcuate button should not be visible visible once calculation is done");
		}
		softAssert.assertAll();		
	}

	@Test(description = "SMAB-T195: Perform Recalculation for factor tables individually", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verifyBppTrendReCalculate(String loginUser) throws Exception {
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
		boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T195: Recalcuate all should be visible");
		boolean	isSubmitAllForApprovalBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllForApprovalBtnDisplayed, "SMAB-T195: Submit All Factors For Approval button should be visible");
		
		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");
		
		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			//Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
			
			//Validating presence of Recalculate button given table individually
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T195: ReCalcuate button should be visible for selected table");
			
			//Clicking on Recalculate button and Confirm button in pop up window to ReInitiate calculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Clicking 'ReCalculate' Button **");
			objBppTrnPg.clickReCalculateBtn(tableName);
			objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp));
			
			//Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnCalculateClick(180);
			
			//Retrieve & Assert message displayed above table on clicking Recalculate button
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
			softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T195: Recalculation successfully performed for table '" + tableName + "'");
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Checking status of various buttons at page level and individual table level **");
			
			//Validating presence of ReCalculate All & Submit All For Approval buttons at page level on clicking calculate button
			boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(30);
			softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T195: Submit All Factors For Approval button should be visible");
			isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
			softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T195: ReCalcuateAll button should be visible");
			
			//Validating presence of ReCalculate & Submit For Approval buttons at table level on clicking Recalculate button
			isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T195: ReCalcuate button should be visible");
			//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(30, tableName);
			//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T195: Submit For Approval button should be visible");
		}
		softAssert.assertAll();
	}
		
	@Test(description = "SMAB-T191: Perform calculation for all factor tables in one go", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 2, enabled = true)
	public void verifyBppTrendCalculateAll(String loginUser) throws Exception {
		//Resetting the factor tables status to Not Calculated
		String rollYear = CONFIG.getProperty("rollYear");
		List<String> factorTablesToReset = Arrays.asList(CONFIG.getProperty("factorTablesToReset").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(factorTablesToReset, "Not Calculated", rollYear);
		
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
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(20);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T191: Calcuate all button should be visible");

		//Step5: Clicking on Calculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate all' button");
		objBppTrnPg.clickCalculateAllBtn();
		
		//Step6: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnCalculateAllClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterCalculateAll");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T191: Calculation successfully performed for all tables");
		
		//Step7: Validating presence of ReCalculate All & Submit All For Approval buttons at page level on clicking Calculate all button
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T191: Submit All Factors For Approval button should be visible:");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: ReCalcuateAll button should be visible:");
		
		//Step8: Validating absence of CalculateAll at page level.
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(5);
		softAssert.assertTrue(!isCalculateAllBtnDisplayed, "SMAB-T191: Calcuate all button should not be visible:");
	}

	@Test(description = "SMAB-T191: Perform ReCalculation for all factor tables in one go", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 3, enabled = true)
	public void verifyBppTrendReCalculateAll(String loginUser) throws Exception {
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
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: ReCalcuateAll button should be visible");

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
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T191: ReCalcuateAll button should be visible");
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T191: Submit All Factors For Approval button should be visible");
	}

	@Test(description = "SMAB-T190: Sumbit calculations for approval for factor tables individually", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 4, enabled = false)
	public void verifyBppTrendSubmitForApproval(String loginUser) throws Exception {
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
		boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: Recalcuate all button should be visible");
		boolean	isSubmitAllForApprovalBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllForApprovalBtnDisplayed, "SMAB-T190: Submit All Factors For Approval button should be visible");
		
		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");
		
		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size()-1; i++) {
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			// Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
															
			// Clicking on Submit For Approval button to submit the Recalculation
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Submit For Approval' Button");
			objBppTrnPg.clickSubmitForApprovalBtn(tableName);

			//Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnSubmitForApprovalClick(180);		
			
			// Retrieve & Assert message displayed above table on clicking submit for approval button
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostSubmitForApproval");
			softAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "SMAB-T190: Calculated data submitted for approval successfully");
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and individual table level");
			
			// Validating absence of ReCalculate & Submit For Approval buttons at table level on clicking submit for approval button
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(10, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T190: Is ReCalcuate button should not be visible");
			boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(10, tableName);
			softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T190: Submit For Approval button should not be visible");
		
			// Validating absence of ReCalculate All button at page level on clicking submit for approval button
			isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(5);
			softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T!190: ReCalcuateAll button should not be visible");
		}
		softAssert.assertAll();
	}
    
	@Test(description = "Sumbit calculations for approval for all factor tables in one go", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 5, enabled = true)
	public void verifyBppTrendSubmitAllForApproval(String loginUser) throws Exception {	
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

		//Step4: Checking whether Submit all For Approval button is visible
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T190: Submit All Factors For Approval button should be visible");
		
		//Step5: Clicking on Submit all For Approval button to submit calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Submit All Factors For Approval' button");
		objBppTrnPg.clickSubmitAllFactorsForApprovalBtn();
		
		//Step6: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnSubmitAllForApprovalClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterSubmitAllForApproval");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T190: Calculation successfully submitted for approval for all tables");
		
		//Step7: Validating message displayed at page level on clicking submit all factors for approval
		String expMsgOnSubmitForApp = CONFIG.getProperty("pageLevelMsgPostSubmitForApproval");
		String actMsgOnSubmitForApp = objBppTrnPg.getElementText(objBppTrnPg.pageLevelMsg);
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T190: Calculations for all tables submitted successfully");
		
		//Step8: Validating absence of ReCalculate All & Submit All Factors For Approval buttons at page level on clicking ReCalculate all button
		isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllForApprovalBtnVisible(5);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T190: Submit All Factors For Approval button should not be visible");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(5);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T190: ReCalcuateAll button should not be visible");

		softAssert.assertAll();
	}
}