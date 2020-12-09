package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.apas.PageObjects.BppTrendSetupPage;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.users;
import com.relevantcodes.extentreports.LogStatus;


public class BPPTrend_Calculate_ReCalculate_Submit_Approve_ALL_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	SalesforceAPI objSalesforceAPI;
	BuildingPermitPage objBuildPermit;
	SoftAssert objSoftAssert;
    BppTrendSetupPage objBppTrendSetupPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objSalesforceAPI = new SalesforceAPI();
		objBuildPermit = new BuildingPermitPage(driver);
		objSoftAssert = new SoftAssert();
        objBppTrendSetupPage = new BppTrendSetupPage(driver);
        objBppTrendSetupPage.updateRollYearStatus("Open", "2020");
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		//objBppTrendSetupPage.logout();
	}


	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating presence of Calculate all button:: Test Case/JIRA ID: SMAB-T191
	 * 2. Trigger calculations for all tables using Calculate all button:: Test Case/JIRA ID: SMAB-T191
	 * 3. Validating pop up message:: Test Case/JIRA ID: SMAB-T191
	 * 4. Checking presence of ReCalculate all and Submit All Factors For Approval buttons
	 * 5. Checking unavailability of Calculate button for valuation factor tables:: Test Case/JIRA ID: SMAB-T247
	 * 6. Checking the status of all tables in BPP Trends page
	 */
	@Test(description = "SMAB-T191,SMAB-T247,SMAB-T313: Perform calculation for all factor tables in one go", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_CalculateAll(String loginUser) throws Exception {
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objBppTrendSetupPage.login(loginUser);

		//Step3: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step5: Validating presence of CalculateAll at page level.
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(90);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T191: Calcuate all button is visible");

		//Step6: Navigating over valuation factor tables to check absence of calculate button
		boolean isCalculateBtnVisible;
		String tableName;
		List<String> valuationFactorTablesList = Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(","));
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			Thread.sleep(1000);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);
			isCalculateBtnVisible = objBppTrnPg.isCalculateBtnVisible(5, tableName);
			softAssert.assertTrue(!isCalculateBtnVisible, "SMAB-T247: Calculate button under '"+ tableName +"' table is not visible");
		}

		//Step7: Clicking on Calculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate all' button");
		objBppTrnPg.clickCalculateAllBtn();

		//Step8: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnCalculateAllClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterCalculateAll");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T191: Calculation successfully performed for all tables");

		//Step9: Validating presence of ReCalculate All button at page level
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(20);
		objSoftAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "Submit All Factors For Approval button is visible");

		//Step10: Validating presence of Submit All Factors For Approval button at page level
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "ReCalcuateAll button is visible");

		//Step11: Validating absence of CalculateAll at page level.
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(5);
		objSoftAssert.assertTrue(!isCalculateAllBtnDisplayed, "Calcuate all button is not visible");

		//Step12: Check unavailability of Export button
		boolean isExportValuationTableBtnVisible = objBppTrnPg.isExportValuationFactorsBtnVisible(5);
		objSoftAssert.assertTrue(!isExportValuationTableBtnVisible, "Export valuation factor tables button to export excel file is not visible");
		boolean isExportCompositeTableBtnVisible = objBppTrnPg.isExportCompositeFactorsBtnVisible(5);
		objSoftAssert.assertTrue(!isExportCompositeTableBtnVisible, "Export composite factor tables button to export excel file is not visible");



		//Step14: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));

		List<String> valFactorTablesList = new ArrayList<String>();
		valFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));

		//Step15: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatus(tableName,rollYear);
			objSoftAssert.assertEquals(currentStatus, "Calculated", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}

		//Step16: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int j = 0; j < valFactorTablesList.size(); j++) {
			tableName = valFactorTablesList.get(j);
			currentStatus = objBppTrendSetupPage.getTableStatus(tableName,rollYear);
			objSoftAssert.assertEquals(currentStatus, "Yet to submit for Approval", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		objSoftAssert.assertAll();
		objBppTrendSetupPage.logout();
	}


	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating presence of ReCalculate all button:: Test Case/JIRA ID: SMAB-T197
	 * 2. Trigger Re-calculation for all tables using Recalculate all button:: Test Case/JIRA ID: SMAB-T197
	 * 3. Validating pop up message:: Test Case/JIRA ID: SMAB-T197
	 * 4. Checking presence of ReCalculate all and Submit All Factors For Approval buttons
	 * 5. Checking the status of all tables in BPP Trends page
	 */
	@Test(description = "SMAB-T197: Perform ReCalculation for all factor tables in one go", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_ReCalculateAll(String loginUser) throws Exception {
		//Resetting the composite factor tables status to Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);

		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objBppTrendSetupPage.login(loginUser);

		//Step2: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of ReCalculate All & Submit All For Approval buttons at page level once data has been calculated by clicking Calculate all button
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T197: ReCalcuateAll button is visible");

		//Step5: Clicking on ReCalculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Recalculate all' button");
		objBppTrnPg.clickReCalculateAllBtn();
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 10);
		softAssert.assertTrue(isReCalculateAllBtnDisplayed, "SMAB-T197: Pop up message on recalculae button is visible");

		String actWarningMsgInPopUp = objBppTrnPg.retrieveReCalculatePopUpMessage();
		String expWarningMsgInPopUp = CONFIG.getProperty("recalculatePopUpMsg");
		softAssert.assertContains(actWarningMsgInPopUp, expWarningMsgInPopUp, "SMAB-T197: Validating warning / pop up message on clicking 'ReCalculate' button");
		objBppTrnPg.Click(objBppTrnPg.confirmBtnInPopUp);

		//Step6: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnCalculateAllClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterCalculateAll");
		objSoftAssert.assertEquals(actPopUpMsg, expPopUpMsg, "ReCalculation successfully performed for all tables");

		//Step7: Validating presence of ReCalculate All button at page level after performing ReCalculation
		isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "ReCalcuateAll button is visible");

		//Step8: Validating presence of Submit All Factors For Approval button at page level after performing ReCalculation
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(20);
		objSoftAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "Submit All Factors For Approval button is visible");

		//Step9: Fetch table names from properties file and collect them in a single list
		List<String> allTablesBppTrendSetupPage = new ArrayList<String>();
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));

		//Step10: Navigating to BPP Trend Setup page and checking status of the composite & valuation tables
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step11: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		//Step12: Fetch composite factor table names from properties file and collect them in a single list
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));

		//Step13: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String tableName, currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Calculated", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}

		//Step14: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Yet to submit for Approval", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		objSoftAssert.assertAll();
		objBppTrendSetupPage.logout();
	}


	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating the status of all the composite factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442
	 * 2. Validating the status of all the valuation factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442
	 * 3. Clicking SubmitAll Factors For Approval button:: Test Case/JIRA ID: SMAB-T250, SMAB-T442
	 * 4. Validating message displayed at page level post submitting for approval:: Test Case/JIRA ID: SMAB-T442,SMAB-T211
	 * 5. Validating unavailability of ReCalculate & Submit All Factors For Approval buttons:: Test Case/JIRA ID: SMAB-T442
	 * 6. Validating CPI Factor input box should be disabled on submitting for approval:: Test Case/JIRA ID: SMAB-T211
	 * 7. Validating the status of all the composite factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442
	 * 8. Validating the status of all the valuation factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442, SMAB-T247
	 */
	@Test(description = "SMAB-T250,SMAB-T442,SMAB-T247,SMAB-T211: Sumbit calculations for approval for all factor tables in one go",groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_SubmitAllFactorForApproval(String loginUser) throws Exception {
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);

		//Step3: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		//Step4: Fetch composite factor table names from properties file and collect them in a single list
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));

		//Step5: Navigate to BPP trend setup page before approving
		objBppTrendSetupPage.login(loginUser);
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		Thread.sleep(2000);

		//Step6: Iterate over composite factor tables list and validate their status Bpp trend setup on details page
		String tableName;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step7: Iterate over valuation factor tables list and validate their status Bpp trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step8: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step10: Checking whether Submit all For Approval button is visible
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking whether Submit all For Approval button is visible");
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T442: Submit All Factors For Approval button is visible");

		//Step11: Clicking on Submit all For Approval button to submit calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Submit All Factors For Approval' button");
		objBppTrnPg.clickSubmitAllFactorsForApprovalBtn();

		//Step12: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterSubmitAllForApproval");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T442: Calculation successfully submitted for approval for all tables");

		//Step13: Validating message displayed at page level on clicking submit all factors for approval
		String expMsgOnSubmitForApp = CONFIG.getProperty("pageLevelMsgPostSubmitForApproval");
		String actMsgOnSubmitForApp = objBppTrnPg.getElementText(objBppTrnPg.pageLevelMsg);
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T442: Calculations for all tables submitted successfully");
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T250: Calculations for all tables submitted successfully");
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T211: Calculations for all tables submitted successfully");

		//Step14: Validating absence of ReCalculate All & Submit All Factors For Approval buttons at page level on clicking ReCalculate all button
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Validating absence of ReCalculate All & Submit All Factors For Approval buttons at page level on clicking ReCalculate all button");
		isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(5);
		softAssert.assertTrue(!isSubmitAllFactorsBtnDisplayed, "SMAB-T442: Submit All Factors For Approval button is not visible");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(5);
		softAssert.assertTrue(!isReCalculateAllBtnDisplayed, "SMAB-T442: ReCalcuateAll button is not visible");

		//Step15: Check unavailability of Export excel files buttons
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Check unavailability of buttons to export valuation and composite factor files as excel");
		boolean isExportValuationTableBtnVisible = objBppTrnPg.isExportValuationFactorsBtnVisible(5);
		objSoftAssert.assertTrue(!isExportValuationTableBtnVisible, "Export valuation factor tables button to export excel file is not visible");
		boolean isExportCompositeTableBtnVisible = objBppTrnPg.isExportCompositeFactorsBtnVisible(5);
		objSoftAssert.assertTrue(!isExportCompositeTableBtnVisible, "Export composite factor tables button to export excel file is not visible");

		//Step16: Navigating on BPP Prop 13 tables to validate CPI Factor input field is disabled
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Navigating on BPP Prop 13 tables to validate CPI Factor input field is disabled");
		Thread.sleep(1000);
		objBppTrnPg.clickOnTableOnBppTrendPage("BPP Prop 13 Factors");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.disabledCpiInputField, 10);
		softAssert.assertTrue((objBppTrnPg.disabledCpiInputField != null), "SMAB-T211: CPI Factor filed is locked for editing after table data is submitted for approval");

		//Step17: Navigating over valuation factor tables to check absence of calculate button on BPP Trend page
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Navigating over valuation factor tables to check absence of calculate button on BPP Trend page");
		List<String> valuationFactorTablesOnBppTrendPage = Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(","));
		boolean isCalculateBtnVisible;
		for(int i = 0; i < valuationFactorTablesOnBppTrendPage.size(); i++) {
			tableName = valuationFactorTablesOnBppTrendPage.get(i);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);
			isCalculateBtnVisible = objBppTrnPg.isCalculateBtnVisible(5, tableName);
			softAssert.assertTrue(!isCalculateBtnVisible, "SMAB-T247: Calculate button under '"+ tableName +"' table is not visible");
		}

		//Step18: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step19: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Validating status of Composite factor on BPP Trend Setup page");
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step20: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Validating status of Valuation factor on BPP Trend Setup page");
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T247: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		softAssert.assertAll();
		objBppTrendSetupPage.logout();
	}


	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validation to approve all tables in one go using Approving all button:: Test Case/JIRA ID: SMAB-T304
	 * 1. Validation to check download and export buttons once table are approved:: Test Case/JIRA ID: SMAB-T304
	 */
	@Test(description = "SMAB-T304: Aproving all factor tables in one go using ApproveAll button", groups = {"regression","BPPTrend"}, dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class)
	public void BppTrend_ApproveAll(String loginUser) throws Exception {
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Submitted for Approval", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Submitted for Approval", rollYear);

		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objBppTrendSetupPage.login(loginUser);

		//Step2: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of Approve All button at page level
		boolean isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
		softAssert.assertTrue(isApproveAllBtnDisplayed, "SMAB-T304: Approve All button is visible");

		//Step5: Clicking on Approve all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Approve all' button");
		objBppTrnPg.clickApproveAllBtn();

		//Step6: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterApproveAll");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T304: Message to validate all tables are successfully approved");

		//Step7: Validating presence of Download, Export Composite Factors & Export Valuation Factors buttons
		boolean isDownloadBtnDisplayed = objBppTrnPg.isDownloadBtnVisible(20);
		softAssert.assertTrue(isDownloadBtnDisplayed, "SMAB-T304: 'Download' button is visible");
		boolean isExportCompositeBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(20);
		softAssert.assertTrue(isExportCompositeBtnDisplayed, "SMAB-T304: 'Export Composite Factors' button is visible");
		boolean isExportValuationBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(20);
		softAssert.assertTrue(isExportValuationBtnDisplayed, "SMAB-T304: 'Export Valuation Factors' button is visible");

		//Step8: Validating absence of Approve All button once the submitted calculation has been approved by clicking approve button for given table
		isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(5);
		softAssert.assertTrue(!isApproveAllBtnDisplayed, "SMAB-T304: Approve all button is not be visible at page level");

		//Step9: Fetch table names from properties file and collect them in a single list
		List<String> allTablesBppTrendSetupPage = new ArrayList<String>();
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));

		//Step10: Navigating to BPP Trend Setup page and checking status of the composite & valuation tables
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		String tableName;
		for(int i = 0; i < allTablesBppTrendSetupPage.size(); i++) {
			tableName = allTablesBppTrendSetupPage.get(i);
			String currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Approved", "Status of "+ tableName +" table on Bpp Trend Page");
		}

		softAssert.assertAll();
		objSoftAssert.assertAll();
		objBppTrendSetupPage.logout();
	}


	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating absence of Calculate all and Calculate button:: Test Case/JIRA ID: SMAB-T1145
	 */
	@Test(description = "SMAB-T1145: Check availlbility of calculate button when BPP trend files are not imported", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_Calculate_When_InputFiles_NotImported(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		objBppTrendSetupPage.login(users.SYSTEM_ADMIN);

		//Step2: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		
		//Step8: Deleting the new BPP Trend Setup created
		int year = Integer.parseInt(rollYear) + 2;
		String queryForBppTrendRollYear = "Select Id From BPP_Trend_Roll_Year__c Where Roll_Year__c = '"+ year +"'";
		new SalesforceAPI().delete("BPP_Trend_Roll_Year__c", queryForBppTrendRollYear);

		//Step3: Creating a new BPP trend setup with no BPP settings, no composite factors settings, no index & goods factor data for future roll year
		objBppTrendSetupPage.createDummyBppTrendSetupForErrorsValidation("Yet to be Imported",year);
		objBppTrendSetupPage.logout();

		Thread.sleep(20000);

		//Step4: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objBppTrendSetupPage.login(loginUser);

		//Step5: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS);
		//String rollYear = System.getProperty("rollYearForErrorValidationOnCalculate");
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(Integer.toString(year));
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step6: Validating presence of CalculateAll at page level.
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the absence of 'Calculate All' button when input files are not imported in the system");
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(5);
		softAssert.assertTrue(!isCalculateAllBtnDisplayed, "SMAB-T1145: Calcuate all button is not visible");

		//Step7: Navigating over valuation factor tables to check absence of calculate button
		boolean isCalculateBtnVisible;
		String tableName;

		List<String> allTablesList = new ArrayList<String>();
		allTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTabExcludingProp13").split(",")));
		allTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));

		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");
		boolean isTableUnderMoreTab;

		ExtentTestManager.getTest().log(LogStatus.INFO, "Navigaing over all the tables on BPP Tredn page and validating the absence of 'Calculate' button when input files are not imported in the system");
		for(int i = 0; i < allTablesList.size(); i++) {
			tableName = allTablesList.get(i);
			isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);
			isCalculateBtnVisible = objBppTrnPg.isCalculateBtnVisible(3, tableName);
			softAssert.assertTrue(!isCalculateBtnVisible, "SMAB-T1145: Calculate button under '"+ tableName +"' table is not visible");
		}

		//Step8: Deleting the new BPP Trend Setup created
		queryForBppTrendRollYear = "Select Id From BPP_Trend_Roll_Year__c Where Roll_Year__c = '"+ year +"'";
		new SalesforceAPI().delete("BPP_Trend_Roll_Year__c", queryForBppTrendRollYear);

		//Step9: Logging out of the application
		softAssert.assertAll();
		objBppTrendSetupPage.logout();
	}

}