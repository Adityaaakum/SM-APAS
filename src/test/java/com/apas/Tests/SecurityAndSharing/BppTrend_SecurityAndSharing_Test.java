package com.apas.Tests.SecurityAndSharing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.apas.PageObjects.BppTrendSetupPage;
import org.openqa.selenium.WebElement;
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
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrend_SecurityAndSharing_Test extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	SoftAssertion softAssert;
	String rollYear;
	SalesforceAPI objSalesforceAPI;
	SoftAssert objSoftAssert;
	BuildingPermitPage objBuildPermitPage;
	BppTrendSetupPage objBppTrendSetupPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objSalesforceAPI = new SalesforceAPI();
		objSoftAssert = new SoftAssert();
		objBuildPermitPage = new BuildingPermitPage(driver);
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}


//	/**
//	 * Description: Validating the availability of Calculate and Calculate All buttons for allowed users
//	 */
//	@Test(description = "Perform security verification on Calculate & Calculate All buttons for allowed users", groups = {"BPPTrend","regression"}, dataProvider = "usersAllowedToCalculate", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
//	public void verify_BppTrend_SharingOfCalculateButtons_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Not Calculated
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step6: Validating presence of CalculateAll button at page level
//		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(10);
//		objSoftAssert.assertTrue(isCalculateAllBtnDisplayed, "For User '"+ loginUser +"': Calcuate All button is visible");
//
//		//Step7: Fetch table names from properties file and collect them in a single list
//		List<String> allTables = new ArrayList<String>();
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
//
//		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab");
//
//		//Step8: Iterating over the given tables
//		for (int i = 0; i < allTables.size(); i++) {
//			//Step9: Clicking on the given table name
//			String tableName = allTables.get(i);
//			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
//			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
//			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);
//
//			//Step10: Validating presence of Calculate button at table level
//			boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(10, tableName);
//			objSoftAssert.assertTrue(isCalculateBtnDisplayed, "For User '"+ loginUser +"': Calcuate button is visible under "+ tableName +" table");
//		}
//		objSoftAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}
//
//
//	/**
//	 * Description: Validating the availability of ReCalculate and ReCalculate All buttons for allowed users
//	 */
//	@Test(description = "Perform security verification on ReCalculate & ReCalculate All buttons for allowed users", groups = {"BPPTrend","regression"}, dataProvider = "usersAllowedToReCalculate", dataProviderClass = DataProviders.class, priority = 2, enabled = true)
//	public void verify_BppTrend_SharingOfReCalculateButtons_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Calculated
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step6: Validating presence of ReCalculateAll button at page level
//		boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(10);
//		objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "For User '"+ loginUser +"': ReCalcuate All button is visible");
//
//		//Step7: Fetch table names from properties file and collect them in a single list
//		List<String> allTables = new ArrayList<String>();
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
//
//		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab");
//
//		//Step8: Iterating over the given tables
//		for (int i = 0; i < allTables.size(); i++) {
//			//Step9: Clicking on the given table name
//			String tableName = allTables.get(i);
//			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
//			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
//			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);
//
//			//Step10: Validating presence of ReCalculate button at table level
//			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(10, tableName);
//			objSoftAssert.assertTrue(isReCalculateBtnDisplayed, "For User '"+ loginUser +"': ReCalcuate button is visible under "+ tableName +" table");
//		}
//		objSoftAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}
//
//
//	/**
//	 * Description: Validating the availability of Submit All Factors For Approval button for allowed users
//	 */
//	@Test(description = "Perform security verification on Submit All Factors For Approval button for allowed users", groups = {"BPPTrend","regression"}, dataProvider = "usersAllowedToSubmitAllFactors", dataProviderClass = DataProviders.class, priority = 4, enabled = true)
//	public void verify_BppTrend_SharingOfSubmitAllFactorsButton_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Calculated
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);
//
//		//Resetting the valuation factor tables status to Yet to submit for Approval
//		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step6: Validating presence of Submit All Factors For Approval button at page level
//		boolean	isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(20);
//		objSoftAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "For User '"+ loginUser +"': Submit All Factors For Approval button is visible");
//
//		////Step7: Fetch table names from properties file and collect them in a single list
//		//List<String> allTables = new ArrayList<String>();
//		//allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
//		//allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
//		//allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));
//
//		//String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");
//
//		////Step8: Iterating over the given tables
//		//for (int i = 0; i < allTables.size(); i++) {
//			////Step9: Clicking on the given table name
//			//String tableName = allTables.get(i);
//			//ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
//			//boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
//			//objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
//
//			////Step10: Validating absence of Submit For Approval button at table level
//			//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(10, tableName);
//			//objSoftAssert.assertTrue(!isSubmitForApprovalBtnDisplayed, "For User '"+ loginUser +"': Submit For Approval button is not visible");
//			//objSoftAssert.assertTrue(!isSubmitForApprovalBtnDisplayed, "For User '"+ loginUser +"': Submit For Approval button is not visible");
//		//}
//		objSoftAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}
//
//
//	/**
//	 * Description: Validating the availability of Approve & Approve All buttons for allowed users
//	 */
//	@Test(description = "Perform security verification on Approve & Approve All buttons for allowed users", groups = {"BPPTrend","regression"}, dataProvider = "usersAllowedToApprove", dataProviderClass = DataProviders.class, priority = 6, enabled = true)
//	public void verify_BppTrend_SharingOfApproveButtons_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Submitted for Approval
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Submitted for Approval", rollYear);
//
//		//Resetting the valuation factor tables status to Submitted For Approval
//		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Submitted for Approval", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step6: Validating presence of ApproveAll button at page level
//		boolean	isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(10);
//		objSoftAssert.assertTrue(isApproveAllBtnDisplayed, "For User '"+ loginUser +"': Approve All button is visible");
//
//		//Step7: Fetch table names from properties file and collect them in a single list
//		List<String> allTables = new ArrayList<String>();
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));
//
//		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");
//
//		//Step8: Iterating over the given tables
//		for (int i = 0; i < allTables.size(); i++) {
//			//Step9: Clicking on the given table name
//			String tableName = allTables.get(i);
//			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
//			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
//			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);
//
//			//Step10: Validating presence of Approve button at table level
//			boolean isApproveBtnDisplayed = objBppTrnPg.isApproveBtnVisible(10, tableName);
//			objSoftAssert.assertTrue(isApproveBtnDisplayed, "For User '"+ loginUser +"': Approve button is visible under "+ tableName +" table");
//		}
//		objSoftAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}
//
//
//	/**
//	 * Description: Validating the availability of Download button for allowed users
//	 */
//	@Test(description = "Perform security verification on Download button for allowed users", groups = {"BPPTrend","regression"}, dataProvider = "usersAllowedToDownloadPdfFile", dataProviderClass = DataProviders.class, priority = 8, enabled = true)
//	public void verify_BppTrend_SharingOfDownloadButton_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Approved
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);
//
//		//Resetting the valuation factor tables status to Approved
//		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step6: Validating presence of Download button at page level
//		boolean	isDownloadBtnDisplayed = objBppTrnPg.isDownloadBtnVisible(20);
//		objSoftAssert.assertTrue(isDownloadBtnDisplayed, "For User '"+ loginUser +"': Download button is visible");
//
//		objSoftAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}
//
//
//	/**
//	 * Description: Validating the availability of Export Composite Factors button for allowed users
//	 */
//	@Test(description = "Perform security verification on Export Composite Factors button for allowed users", groups = {"BPPTrend","regression"}, dataProvider = "usersAllowedToExportCompFactorsFile", dataProviderClass = DataProviders.class, priority = 10, enabled = true)
//	public void verify_BppTrend_SharingOfExportCompositeFactorsButton_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Approved
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);
//
//		//Resetting the valuation factor tables status to Approved
//		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step6: Validating presence of Export Composite Factors button at page level
//		boolean	isExportCompFactorsBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(20);
//		objSoftAssert.assertTrue(isExportCompFactorsBtnDisplayed, "For User '"+ loginUser +"': Export Composite Factors button is visible");
//
//		objSoftAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}
//
//
//	/**
//	 * Description: Validating the availability of Export Valuation Factors button for allowed users
//	 */
//	@Test(description = "Perform security verification on Export Valuation Factors button for allowed users", groups = {"BPPTrend","regression"}, dataProvider = "usersAllowedToExportValFactorsFile", dataProviderClass = DataProviders.class, priority = 12, enabled = true)
//	public void verify_BppTrend_SharingOfExportValuationFactorsButton_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Approved
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);
//
//		//Resetting the valuation factor tables status to Approved
//		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step6: Validating presence of Export Valuation Factors button at page level
//		boolean	isExportValFactorsBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(20);
//		objSoftAssert.assertTrue(isExportValFactorsBtnDisplayed, "For User '"+ loginUser +"': Export Valuation Factors button is visible");
//
//		objSoftAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}
//
//
//	/**
//	 * DESCRIPTION: Validating the availability of edit pencil icon to edit table status on BPP Trend Setup
//	 */
//	@Test(description = "Validating the availability of edit pencil icon to edit table status on BPP Trend Setup", groups = {"BPPTrend","regression"}, dataProvider = "usersRestrictedToExportValFactorsFile", dataProviderClass = DataProviders.class, priority = 14, enabled = true)
//	public void verify_BppTrend_SharingOfEditPencilIcon_BppTrendSetupPage_forAllowedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Approved
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);
//
//		//Resetting the valuation factor tables status to Approved
//		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step5: Validating presence of Edit pencil icon to edit table status on BPP Trend Setup page
//		List<String> tableNamesOnBppTrendSetupPage = new ArrayList<String>();
//		tableNamesOnBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));
//		tableNamesOnBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
//
//		boolean editPencilIcons;
//		for(String tableName : tableNamesOnBppTrendSetupPage) {
//			editPencilIcons = objBppTrnPg.isPencilIconToEditTableStatusVisible(tableName);
//			objSoftAssert.assertTrue(!editPencilIcons, "For User '"+ loginUser +"': Edit pencil icon on BPP Trend Setup page is not visible for table '"+ tableName +"'");
//		}
//
//		//Step6: Log out from application
//		objApasGenericFunctions.logout();
//
//		//Step7: Resetting the Composite factor tables status to Calculated
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);
//
//		//Step8: Resetting the valuation factor tables status to Calculated
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
//
//		//Step9: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step10: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
//
//		//Step11: Selecting role year from drop down
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step12: Validating presence of Edit pencil icon to edit table status on BPP Trend Setup page
//		tableNamesOnBppTrendSetupPage = new ArrayList<String>();
//		tableNamesOnBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));
//		tableNamesOnBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
//
//		for(String tableName : tableNamesOnBppTrendSetupPage) {
//			editPencilIcons = objBppTrnPg.isPencilIconToEditTableStatusVisible(tableName);
//			objSoftAssert.assertTrue(!editPencilIcons, "For User '"+ loginUser +"': Edit pencil icon on BPP Trend Setup page is not visible for table '"+ tableName +"'");
//		}
//
//		objApasGenericFunctions.logout();
//		objSoftAssert.assertAll();
//	}


	/**
	 * ***************************** METHODS ASSOCIATED WITH RESTRICTION *****************************
	 */


	/**
	 * DESCRIPTION: Checking Security Level For Restricted Users
	 * 1. Validating the unavailability of Calculate and Calculate All buttons:: Test Case/JIRA ID: SMAB-T174, SMAB-T175
	 */
	@Test(description = "SMAB-T174,SMAB-T175: Perform security verification on Calculate & Calculate All buttons for restricted users", groups = {"BPPTrend","regression"}, dataProvider = "usersRestrictedToCalculate", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verify_BppTrend_RestrictionsOnCalculateButtons_forRestrictedUsers(String loginUser) throws Exception {
		//Step1: Resetting the Composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
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

		//Step4: Validating absence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(10);
		softAssert.assertTrue(!isCalculateAllBtnDisplayed, "SMAB-T174: For User '"+ loginUser +"': Calcuate All button is not visible");
		softAssert.assertTrue(!isCalculateAllBtnDisplayed, "SMAB-T175: For User '"+ loginUser +"': Calcuate All button is not visible");

		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));

		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");

		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			//Step7: Clicking on the given table name
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);

			//Step8: Validating absence of Calculate button at table level
			boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(10, tableName);
			softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T174: For User '"+ loginUser +"': Calcuate button is not visible");
			softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T175: For User '"+ loginUser +"': Calcuate button is not visible");
		}
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Checking Security Level For Restricted Users
	 * 1. Validating the unavailability of ReCalculate and ReCalculate All buttons:: Test Case/JIRA ID: SMAB-T174, SMAB-T175
	 */
	@Test(description = "SMAB-T174,SMAB-T175: Perform security verification on ReCalculate & ReCalculate All buttons for restricted users", groups = {"BPPTrend","regression"}, dataProvider = "usersRestrictedToReCalculate", dataProviderClass = DataProviders.class, priority = 3, enabled = true)
	public void verify_BppTrend_RestrictionsOnReCalculateButtons_forRestrictedUsers(String loginUser) throws Exception {
		//Step1: Resetting the Composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
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

		//Step4: Validating absence of ReCalculateAll button at page level
		boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(10);
		softAssert.assertTrue(!isReCalculateAllBtnDisplayed, "SMAB-T174: For User '"+ loginUser +"': ReCalcuate All button is not visible");
		softAssert.assertTrue(!isReCalculateAllBtnDisplayed, "SMAB-T175: For User '"+ loginUser +"': ReCalcuate All button is not visible");

		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));

		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");

		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			//Step7: Clicking on the given table name
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);

			//Step8: Validating absence of ReCalculate button at table level
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(10, tableName);
			softAssert.assertTrue(!isReCalculateBtnDisplayed, "SMAB-T174: For User '"+ loginUser +"': ReCalcuate button is not visible");
			softAssert.assertTrue(!isReCalculateBtnDisplayed, "SMAB-T175: For User '"+ loginUser +"': ReCalcuate button is not visible");
		}
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Checking Security Level For Restricted Users
	 * 1. Validating the unavailability of Submit Factor For Approval
	 * and Submit Factor For Approval buttons:: Test Case/JIRA ID: SMAB-T174, SMAB-T175
	 */
	@Test(description = "SMAB-T174,SMAB-T175: Perform security verification on Submit All Factors For Approval button for restricted users", groups = {"BPPTrend","regression"}, dataProvider = "usersRestrictedToSubmitAllFactors", dataProviderClass = DataProviders.class, priority = 5, enabled = true)
	public void verify_BppTrend_RestrictionsOnSubmitAllFactorsButton_forRestrictedUsers(String loginUser) throws Exception {
		//Step1: Resetting the Composite factor tables status to Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to submit for Approval
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
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

		//Step4: Validating absence of Submit All Factors For Approval button at page level
		boolean	isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(10);
		softAssert.assertTrue(!isSubmitAllFactorsBtnDisplayed, "SMAB-T174: For User '"+ loginUser +"': Submit All Factors For Approval button is not visible");
		softAssert.assertTrue(!isSubmitAllFactorsBtnDisplayed, "SMAB-T175: For User '"+ loginUser +"': Submit All Factors For Approval button is not visible");

		////Step5: Fetch table names from properties file and collect them in a single list
		//List<String> allTables = new ArrayList<String>();
		//allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		//allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		//allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));

		//String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");

		////Step6: Iterating over the given tables
		//for (int i = 0; i < allTables.size(); i++) {
			////Step7: Clicking on the given table name
			//String tableName = allTables.get(i);
			//ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			//boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			//objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);

			////Step8: Validating absence of Submit For Approval button at table level
			//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(10, tableName);
			//softAssert.assertTrue(!isSubmitForApprovalBtnDisplayed, "SMAB-T174: For User '"+ loginUser +"': Submit For Approval button is not visible");
			//softAssert.assertTrue(!isSubmitForApprovalBtnDisplayed, "SMAB-T175: For User '"+ loginUser +"': Submit For Approval button is not visible");
		//}
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


//	/**
//	 * Description: Validating the unavailability of Approve & Approve All buttons for restricted users
//	 */
//	@Test(description = "Perform security verification on Approve & Approve All buttons for restricted users", groups = {"BPPTrend","regression"}, dataProvider = "usersRestrictedToApprove", dataProviderClass = DataProviders.class, priority = 7, enabled = true)
//	public void verify_BppTrend_RestrictionsOnApproveButtons_forRestrictedUsers(String loginUser) throws Exception {
//		//Step1: Resetting the Composite factor tables status to Submitted for Approval
//		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Submitted for Approval", rollYear);
//
//		//Resetting the valuation factor tables status to Submitted For Approval
//		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
//		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Submitted for Approval", rollYear);
//
//		//Step2: Login to the APAS application using the given user
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
//		objApasGenericFunctions.login(loginUser);
//
//		//Step3: Opening the BPP Trend module
//		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
//		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
//		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
//		objBppTrnPg.clickOnGivenRollYear(rollYear);
//		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
//
//		//Step4: Validating absence of ApproveAll button at page level
//		boolean	isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(10);
//		objSoftAssert.assertTrue(!isApproveAllBtnDisplayed, "For User '"+ loginUser +"': Approve All button is not visible");
//
//		//Step5: Fetch table names from properties file and collect them in a single list
//		List<String> allTables = new ArrayList<String>();
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
//		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));
//
//		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");
//
//		//Step6: Iterating over the given tables
//		for (int i = 0; i < allTables.size(); i++) {
//			//Step7: Clicking on the given table name
//			String tableName = allTables.get(i);
//			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
//			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
//			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);
//
//			//Step8: Validating absence of Approve button at table level
//			boolean isApproveBtnDisplayed = objBppTrnPg.isApproveBtnVisible(10, tableName);
//			objSoftAssert.assertTrue(!isApproveBtnDisplayed, "For User '"+ loginUser +"': Approve button is not visible");
//		}
//		softAssert.assertAll();
//		objApasGenericFunctions.logout();
//	}


	/**
	 * DESCRIPTION: Validating the unavailability of Export Composite Factors button for restricted users
	 * JIRA ID Asserted: SMAB-T450
	 */
	@Test(description = "SMAB-T450: Perform security verification on Export Composite Factors button for restricted users", groups = {"BPPTrend","regression"}, dataProvider = "usersRestrictedToExportCompFactorsFile", dataProviderClass = DataProviders.class, priority = 12, enabled = true)
	public void verify_BppTrend_RestrictionsOn_ExportCompositeFactorsButton_forRestrictedUsers(String loginUser) throws Exception {
		//Step1: Resetting the Composite factor tables status to Approved
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Resetting the valuation factor tables status to Approved
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step4: Validating absence of Export Composite Factors button at page level
		boolean	isDownloadBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(10);
		softAssert.assertTrue(!isDownloadBtnDisplayed, "SMAB-T450: For User '"+ loginUser +"': Export Composite Factors button is not visible");

		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Validating the unavailability of Export Valuation Factors button for restricted users
	 * JIRA ID Asserted: SMAB-T450
	 */
	@Test(description = "SMAB-T450: Perform security verification on Export Valuation Factors button for restricted users", groups = {"regression","BPPTrend"}, dataProvider = "usersRestrictedToExportValFactorsFile", dataProviderClass = DataProviders.class, priority = 13, enabled = true)
	public void verify_BppTrend_RestrictionsOn_ExportValuationFactorsButton_forRestrictedUsers(String loginUser) throws Exception {
		//Step1: Resetting the Composite factor tables status to Approved
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Resetting the valuation factor tables status to Approved
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step6: Validating absence of Export Valuation Factors button at page level
		boolean	isExportValFactorsBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(10);
		softAssert.assertTrue(!isExportValFactorsBtnDisplayed, "SMAB-T450: For User '"+ loginUser +"': Export Valuation Factors button is not visible");

		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Validating the unavailability of edit pencil icon to edit table status on BPP Trend Setup
	 * JIRA ID Asserted: SMAB-T171
	 */
	@Test(description = "SMAB-T171: Validating the unavailability of edit pencil icon to edit table status on BPP Trend Setup", groups = {"smoke","BPPTrend","regression"}, dataProvider = "usersRestrictedEditTableStatusOnBppTrendPage", dataProviderClass = DataProviders.class, priority = 14, enabled = true)
	public void verify_BppTrend_RestrictionsOn_EditPencilIcon_BppTrendSetupPage_forRestrictedUsers(String loginUser) throws Exception {
		//Step1: Resetting the Composite factor tables status to Approved
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Resetting the valuation factor tables status to Approved
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with COMPOSITE TABLES AS NOT CALCULATED & VALUATION TABLES AS YET TO SUBMIT FOR APPROVAL");
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);

		//Step4: Clicking on BPP Trend Setup name
		objApasGenericFunctions.displayRecords("All");
        objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Validating absence of Edit pencil icon to edit table status on BPP Trend Setup page
		List<String> tableNamesOnBppTrendSetupPage = new ArrayList<String>();
		tableNamesOnBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));
		tableNamesOnBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));

		boolean editPencilIcons;
		for(String tableName : tableNamesOnBppTrendSetupPage) {
			editPencilIcons = objBppTrnPg.isPencilIconToEditTableStatusVisible(tableName);
			softAssert.assertTrue(!editPencilIcons, "SMAB-T171: For User '"+ loginUser +"': Edit pencil icon on BPP Trend Setup page is NOT VISIBLE for table '"+ tableName +"'");
		}

		//Step6: Log out from the application
		//objApasGenericFunctions.logout();

		//Step7: Resetting the Composite factor tables status to Approved
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Step8: Resetting the valuation factor tables status to Approved
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);

		//Step9: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with COMPOSITE TABLES AS CALCULATED & VALUATION TABLES AS YET TO SUBMIT FOR APPROVAL");
		//objApasGenericFunctions.login(loginUser);

		//Step10: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);

		//Step11: Clicking on BPP Trend Setup name
		objApasGenericFunctions.displayRecords("All");
        objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step12: Validating absence of Edit pencil icon to edit table status on BPP Trend Setup page
		for(String tableName : tableNamesOnBppTrendSetupPage) {
			editPencilIcons = objBppTrnPg.isPencilIconToEditTableStatusVisible(tableName);
			softAssert.assertTrue(!editPencilIcons, "SMAB-T171: For User '"+ loginUser +"': Edit pencil icon on BPP Trend Setup page is NOT VISIBLE for table '"+ tableName +"'");
		}

		//Step13: Log out from the application
		objApasGenericFunctions.logout();

		softAssert.assertAll();
	}


	/**
	 * DESCRIPTION: Validating the unavailability of edit and delete links under maximum equipment index factor in BPP Trend Setup
	 * JIRA ID Asserted: SMAB-T270
	 */
	@Test(description = "SMAB-T270: Validating the unavailability of edit and delete links under maximum equipment index factor in BPP Trend Setup", groups = {"smoke","regression","BPPTrend"}, dataProvider = "usersRestrictedToModifyMaxEquipIndexFactor", dataProviderClass = DataProviders.class, priority = 15, enabled = true)
	public void verify_BppTrend_RestrictionsOn_editingMaxEquipIndexFactor_forRestrictedUsers(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend Setup module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");

		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
        objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		/**
		 * Step4: Clicking show more drop down and validating absence of following
		 * a. Edit Link
		 * b. Delete Link
		 * And validating presence of "No Actions Available" option
		 */
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconBppSetting));

		boolean isEditLinkAvailable = objBppTrnPg.waitForElementToBeVisible(5, objBuildPermitPage.editLinkUnderShowMore);
		softAssert.assertTrue(!isEditLinkAvailable, "SMAB-T270: For User '"+ loginUser +"': Edit option is not visible on details page");
		boolean isDeleteLinkAvailable = objBppTrnPg.waitForElementToBeVisible(5, objBuildPermitPage.deleteLinkUnderShowMore);
		softAssert.assertTrue(!isDeleteLinkAvailable, "SMAB-T270: For User '"+ loginUser +"': Delete option is not visible on details page");
		boolean isNoActionsOptionAvailable = objBppTrnPg.waitForElementToBeVisible(5, objBppTrnPg.noActionsLinkUnderShowMore);
		softAssert.assertTrue(isNoActionsOptionAvailable, "SMAB-T270: For User '"+ loginUser +"': 'No Actions Available' option is visible on details page");

		//Step5: Clicking View All link
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.viewAllBppSettings));

		//Step6: Clicking show more link on and again performing above validation on View All table
		objBppTrnPg.waitForElementToBeVisible(10, objBppTrnPg.showMoreDropDownViewAllPage);
		objBuildPermitPage.javascriptClick(objBppTrnPg.showMoreDropDownViewAllPage);

		isEditLinkAvailable = objBppTrnPg.waitForElementToBeVisible(5, objBuildPermitPage.editLinkUnderShowMore);
		softAssert.assertTrue(!isEditLinkAvailable, "SMAB-T270: For User '"+ loginUser +"': Edit option is not visible on View All Grid");
		isDeleteLinkAvailable = objBppTrnPg.waitForElementToBeVisible(5, objBuildPermitPage.deleteLinkUnderShowMore);
		softAssert.assertTrue(!isDeleteLinkAvailable, "SMAB-T270: For User '"+ loginUser +"': Delete option is not visible on View All Grid");
		isNoActionsOptionAvailable = objBppTrnPg.waitForElementToBeVisible(5, objBppTrnPg.noActionsLinkUnderShowMore);
		softAssert.assertTrue(isNoActionsOptionAvailable, "SMAB-T270: For User '"+ loginUser +"': 'No Actions Available' option is visible on View All Grid");

		objApasGenericFunctions.logout();
		softAssert.assertAll();
	}


	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Validating the message above each table to confirm it has been Approved:: Test Case/JIRD ID: SMAB-T249
	 * 2. Validating table data should not be editable:: Test Case/JIRD ID: SMAB-T250, SMAB-T301
	 * 3. Validating the unavailability of ReCalculate button for Approved tables:: Test Case/JIRA ID: T199
	 */
	@Test(description = "SMAB-T199,SMAB-T249,SMAB-T250,SMAB-T301: Navigating to all approved tabls and validating table data should not be editable", groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 16, enabled = true)
	public void verify_BppTrend_ViewApprovedTables(String loginUser) throws Exception {
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);

		//Step1: Login and opening the BPP Trend module
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step2: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step3: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));

		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") +","+ CONFIG.getProperty("valuationTablesUnderMoreTab");

		//Step4: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			//Step5: Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName);

			//Step6: Validation to check whether table data appears for given table
			boolean isTableVisible = objBppTrnPg.isTableDataVisible(tableName, 10);
			softAssert.assertTrue(isTableVisible, "SMAB-T249: Data grid for approved table '"+ tableName +"' is visible");
			softAssert.assertTrue(isTableVisible, "SMAB-T250: Data grid for approved table '"+ tableName +"' is visible");

			//Step7: Retrieve & Assert message displayed above approved table
			String actTableMsgBeforeApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeApprovingCalc = CONFIG.getProperty("tableMsgAfterApproval");
			softAssert.assertEquals(actTableMsgBeforeApprovingCalc, expTableMsgBeforeApprovingCalc, "SMAB-T249: Message above approve table '" + tableName + "'");

			//Step8: Validating unavailability of ReCalculate button at table level after table has been Approved
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(5, tableName);
			softAssert.assertTrue(!isReCalculateBtnDisplayed, "SMAB-T199: ReCalcuate button is not visible for Approved table '"+ tableName +"'");

			//Step9: Checking whether edit button in grid's cell data text box is visible after table status is approved
			WebElement cellTxtBox = objBppTrnPg.locateCellToBeEdited(allTables.get(i), 1, 1);
			objBppTrnPg.Click(cellTxtBox);
			WebElement editBtn = objBppTrnPg.locateEditButtonInFocusedCell();
			softAssert.assertTrue((editBtn == null), "SMAB-T250: Edit pencil icon is not visible to update table data in grid for "+ tableName +"");
			if(tableName.equalsIgnoreCase("BPP Prop 13 Factors")) {
				softAssert.assertTrue((editBtn == null), "SMAB-T301: Edit pencil icon is not visible to update table data in grid for "+ tableName +"");
			}
		}
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

}
