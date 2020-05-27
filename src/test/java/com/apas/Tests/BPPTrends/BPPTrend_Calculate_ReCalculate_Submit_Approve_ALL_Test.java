package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;


public class BPPTrend_Calculate_ReCalculate_Submit_Approve_ALL_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
	String rollYear;
	SalesforceAPI objSalesforceAPI;
	BuildingPermitPage objBuildPermit;
	SoftAssert objSoftAssert;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		
		if(driver==null) {
            setupTest();
            driver = BrowserDriver.getBrowserInstance();
        }
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objSalesforceAPI = new SalesforceAPI();
		objBuildPermit = new BuildingPermitPage(driver);
		objSoftAssert = new SoftAssert();
	}
	
	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating presence of Calculate all button:: Test Case/JIRA ID: SMAB-T191
	 * 2. Trigger calculations for all tables using Calculate all button:: Test Case/JIRA ID: SMAB-T191
	 * 3. Validating pop up message:: Test Case/JIRA ID: SMAB-T191
	 * 4. Checking presence of ReCalculate all and Submit All Factors For Approval buttons
	 * 5. Checking the status of all tables in BPP Trends page
	 */
	@Test(description = "SMAB-T191: Perform calculation for all factor tables in one go", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verify_BppTrend_CalculateAll(String loginUser) throws Exception {		
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step5: Validating presence of CalculateAll at page level.
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(90);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T191: Calcuate all button is visible");

		//Step6: Clicking on Calculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate all' button");
		objBppTrnPg.clickCalculateAllBtn();
		
		//Step7: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnCalculateAllClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterCalculateAll");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T191: Calculation successfully performed for all tables");
		
		//Step8: Validating presence of ReCalculate All button at page level
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(20);
		objSoftAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "Submit All Factors For Approval button is visible");
		
		//Step9: Validating presence of Submit All Factors For Approval button at page level
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "ReCalcuateAll button is visible");
		
		//Step10: Validating absence of CalculateAll at page level.
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(5);
		objSoftAssert.assertTrue(!isCalculateAllBtnDisplayed, "Calcuate all button is not visible");
		
		//Step11: Fetch table names from properties file and collect them in a single list
		List<String> allTablesBppTrendSetupPage = new ArrayList<String>();
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
		
		//Step12: Navigating to BPP Trend Setup page and checking status of the composite & valuation tables
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step13: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		//Step14: Fetch composite factor table names from properties file and collect them in a single list		
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
				
		//Step15: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String tableName, currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Calculated", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		
		//Step16: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Yet to submit for Approval", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		
		objSoftAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating presence of ReCalculate all button:: Test Case/JIRA ID:
	 * 2. Trigger Re-calculation for all tables using Recalculate all button:: Test Case/JIRA ID:
	 * 3. Validating pop up message:: Test Case/JIRA ID:
	 * 4. Checking presence of ReCalculate all and Submit All Factors For Approval buttons
	 * 5. Checking the status of all tables in BPP Trends page
	 */
	@Test(description = "Perform ReCalculation for all factor tables in one go", groups={"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verify_BppTrend_ReCalculateAll(String loginUser) throws Exception {
		//Resetting the composite factor tables status to Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating presence of ReCalculate All & Submit All For Approval buttons at page level once data has been calculated by clicking Calculate all button
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(20);
		objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "ReCalcuateAll button is visible");

		//Step5: Clicking on ReCalculate all button to initiate calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Recalculate all' button");
		objBppTrnPg.clickReCalculateAllBtn();
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp));
		
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
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);

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
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Calculated", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		
		//Step14: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Yet to submit for Approval", "Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		
		objSoftAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating the status of all the composite factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442
	 * 2. Validating the status of all the valuation factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442
	 * 3. Clicking SubmitAll Factors For Approval button:: Test Case/JIRA ID: SMAB-T250, SMAB-T442
	 * 4. Validating message displayed at page level post approval:: Test Case/JIRA ID: SMAB-T442
	 * 5. Validating unavailability of ReCalculate & Submit All Factors For Approval buttons:: Test Case/JIRA ID: SMAB-T442
	 * 6. Validating the status of all the composite factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442
	 * 7. Validating the status of all the valuation factor table on BPP Trend Setup age:: Test Case/JIRA ID: SMAB-T442
	 */
	@Test(description = "SMAB-T250,SMAB-T442: Sumbit calculations for approval for all factor tables in one go",groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 2, enabled = true)
	public void verify_BppTrend_SubmitAllFactorForApproval(String loginUser) throws Exception {	
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step3: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		//Step4: Fetch composite factor table names from properties file and collect them in a single list		
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
				
		//Step5: Navigate to BPP trend setup page before approving
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		Thread.sleep(2000);
			
		//Step6: Iterate over composite factor tables list and validate their status Bpp trend setup on details page
		String tableName;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		
		//Step7: Iterate over valuation factor tables list and validate their status Bpp trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		
		//Step8: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step10: Checking whether Submit all For Approval button is visible
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(20);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T442: Submit All Factors For Approval button is visible");
		
		//Step11: Clicking on Submit all For Approval button to submit calculations for all tables in one go
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Submit All Factors For Approval' button");
		objBppTrnPg.clickSubmitAllFactorsForApprovalBtn();
		
		//Step12: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnSubmitAllForApprovalClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterSubmitAllForApproval");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T442: Calculation successfully submitted for approval for all tables");
		
		//Step13: Validating message displayed at page level on clicking submit all factors for approval
		String expMsgOnSubmitForApp = CONFIG.getProperty("pageLevelMsgPostSubmitForApproval");
		String actMsgOnSubmitForApp = objBppTrnPg.getElementText(objBppTrnPg.pageLevelMsg);
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T442: Calculations for all tables submitted successfully");
		softAssert.assertTrue(actMsgOnSubmitForApp.equals(expMsgOnSubmitForApp), "SMAB-T250: Calculations for all tables submitted successfully");
		
		//Step14: Validating absence of ReCalculate All & Submit All Factors For Approval buttons at page level on clicking ReCalculate all button
		isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(5);
		softAssert.assertTrue(!isSubmitAllFactorsBtnDisplayed, "SMAB-T442: Submit All Factors For Approval button is not visible");
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(5);
		softAssert.assertTrue(!isReCalculateAllBtnDisplayed, "SMAB-T442: ReCalcuateAll button is not visible");

		//Step15: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step16: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step17: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T442: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}	
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validation to approve all tables in one go using Approving all button:: Test Case/JIRA ID: SMAB-T304
	 * 1. Validation to check download and export buttons once table are approved:: Test Case/JIRA ID: SMAB-T304
	 */
	@Test(description = "SMAB-T304: Aproving all factor tables in one go using ApproveAll button", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class, priority = 3, enabled = true)
	public void verify_BppTrend_ApproveAll(String loginUser) throws Exception {
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Submitted for Approval", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Submitted for Approval", rollYear);
		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
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
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnApproveAllClick(180);
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
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		String tableName;
		for(int i = 0; i < allTablesBppTrendSetupPage.size(); i++) {
			tableName = allTablesBppTrendSetupPage.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			objSoftAssert.assertEquals(currentStatus, "Approved", "Status of "+ tableName +" table on Bpp Trend Page");
		}
		
		softAssert.assertAll();
		objSoftAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
}
