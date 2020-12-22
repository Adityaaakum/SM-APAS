package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

public class BPPTrend_CalculateWithMissing_IndexAndFactorSettings_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
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
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating error message for missing BPP Settings (Maximum Equip. Index Value):: TestCase/JIRA Id: SMAB-T192,SMAB-T163
	 * 2. Validating error message for missing BPP Composite Settings (Minimum Index Value):: TestCase/JIRA Id: SMAB-T192
	 * 3. Validating error message for missing good and index factor data:: TestCase/JIRA Id: SMAB-T193,SMAB-T257
	 * 4. Deleting the dummy/test BPP Trend Setup
	 */
	@Test(description = "SMAB-T163,SMAB-T192,SMAB-T193,SMAB-T257: Calculation of INDUSTRIAL COMPOSITE FACTORS with missing calculation variables", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_CompositeFactors_CalculateWithCalculationVariablesMissing(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		objBppTrendSetupPage.login(users.SYSTEM_ADMIN);
		
		//Step2: Opening the BPP Trend module
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		
		//Step8: Deleting the new BPP Trend Setup created
		int year = Integer.parseInt(rollYear) + 2;
		String queryForBppTrendRollYear = "Select Id From BPP_Trend_Roll_Year__c Where Roll_Year__c = '"+ year +"'";
		new SalesforceAPI().delete("BPP_Trend_Roll_Year__c", queryForBppTrendRollYear);

		
		//Step3: Creating a new BPP trend setup with no BPP settings, no composite factors settings, no index & goods factor data for future roll year
		objBppTrendSetupPage.createDummyBppTrendSetupForErrorsValidation("Not Calculated",year);
		String query = "SELECT id FROM BPP_Trend_Roll_Year__c WHERE Roll_Year__c = '" +year+ "'";
		objSalesforceAPI.update("BPP_Trend_Roll_Year__c", query, "Annual_Factor_Status__c", "Reviewed by Admin");

		objBppTrendSetupPage.logout();
		Thread.sleep(20000);

		//Step4: Login with given login user
		objBppTrendSetupPage.login(loginUser);
		
		//Step5: Navigating to BPP Trend page and selecting given roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Navigating to BPP Trends page **");
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS);

		String rollYear = Integer.toString(year);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(Integer.toString(year));
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step6: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));

		//String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab");
		String tableName;

		//Step7: Iterating over the given tables to validate error message on calculate click
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Validating error messages on Calculate button click when Maximum Equipment Index Factor is missing **");
		for (int i = 0; i < allTables.size() - 1; i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Performing Validations For: '"+ allTables.get(i) +"' Table **");
			// Clicking on the given table name
			tableName = allTables.get(i);
			//boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			//objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i), isTableUnderMoreTab);
			Thread.sleep(1000);
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i));

			//Click calculate button for given tables individually
			objBppTrnPg.isCalculateBtnVisible(30, tableName);
			objBppTrnPg.clickCalculateBtn(tableName);

			//Retrieve and return the error message displayed in pop up on clicking calculate button
			String actErrorMessage = objBppTrnPg.waitForErrorPopUpMsgOnCalculateClick(20);
			softAssert.assertContains(actErrorMessage, "Maximum Equipment index factor must be specified", "SMAB-T192: Error message on triggering calculation with missing calculation variables(Max. Equip. Index Factor) for "+ tableName + " table");
			if(tableName.equalsIgnoreCase("Commercial Composite Factors")) {
				softAssert.assertContains(actErrorMessage, "Maximum Equipment index factor must be specified", "SMAB-T163: Error message encountered when triggered calculation with missing calculation variables(Max Index Factor and Min Good Factor)");
			}
		}

		Thread.sleep(2000);
		
		//Step8: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		
		//Step9: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(Integer.toString(year));
		
		//Step10: Create a BPP Setting under selected BPP Trend Setup
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Maximum Equipment Index Factor **");
		objBppTrendSetupPage.createBppSetting("125");
		Thread.sleep(2000);

		//Step11: Navigating to BPP Trend page and selecting given roll year
		objBppTrnPg.selectRollYearOnBPPTrends(Integer.toString(year));

		//Step12: Iterating over the given tables to validate error message on calculate button
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Validating error messages on Calculate button click when BPP Composite Factor Setting is missing **");
		for (int i = 0; i < allTables.size() - 1; i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Performing Validations For: '"+ allTables.get(i) +"' Table **");
			//Clicking on the given table name
			tableName = allTables.get(i);
			
			//Generating expected error message based on the table name
			String[] tableNameBreakup = tableName.split(" ");
			String msgOnMissingCompositeFactorSetting;
			if(tableName.equalsIgnoreCase("Agricultural Mobile Equipment Composite Factors")) {
				msgOnMissingCompositeFactorSetting = "Index factors not found for Agricultural";
			} else if(tableName.equalsIgnoreCase("Construction Mobile Equipment Composite Factors")) {
				msgOnMissingCompositeFactorSetting = "Index factors not found for Construction";
			} else {
				msgOnMissingCompositeFactorSetting = "Bpp Composite Factor setting not found for " + tableNameBreakup[0];
			}
			
			//Clicking on given table name
			//boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			//objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i), isTableUnderMoreTab);
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i));

			//Click calculate button for given tables individually
			objBppTrnPg.isCalculateBtnVisible(30, tableName);
			objBppTrnPg.clickCalculateBtn(tableName);
			
			//Retrieve and return the error message displayed in pop up on clicking calculate button
			String actErrorMessage = objBppTrnPg.waitForErrorPopUpMsgOnCalculateClick(20);
			softAssert.assertContains(actErrorMessage, msgOnMissingCompositeFactorSetting, "SMAB-T192: Error message encountered when triggered calculation with missing calculation variables(Min. Good Factor)");
		}
		
		//Step13: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		
		//Step14: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(Integer.toString(year));
		
		//Step15: Create a BPP Composite Factor Settings
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Clicking on Bpp Composite Factors Settings tab **");	
		if(objBppTrendSetupPage.moreTabRightSection != null) {
			objPage.Click(objBppTrendSetupPage.moreTabRightSection);
			objPage.Click(objBppTrendSetupPage.bppCompositeFactorOption);
			/*objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.dropDownIconBppCompFactorSetting, 10);
			objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconBppCompFactorSetting, 10);
			objPage.Click(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconBppCompFactorSetting));
			*/
        } else {
        	objPage.Click(objBppTrendSetupPage.bppCompFactorSettingTab);
		}
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Commercial property type **");
		
		//objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.newBtnToCreateEntry, 20);
		//objPage.Click(objBppTrendSetupPage.newBtnToCreateEntry);
		
		objBppTrnPg.createRecord();

		objBppTrendSetupPage.enter(objBppTrendSetupPage.minGoodFactorEditBox,"10");
		objBuildPermit.selectOptionFromDropDown("Property Type","Commercial");
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Opening the BPP Trend module and set All as the view option in grid
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(Integer.toString(year));

		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Industrial property type **");
		
		if(objBppTrendSetupPage.moreTabRightSection != null) {
			objPage.Click(objBppTrendSetupPage.moreTabRightSection);
			objPage.Click(objBppTrendSetupPage.bppCompositeFactorOption);
			
        } else {
        	objPage.Click(objBppTrendSetupPage.bppCompFactorSettingTab);
		}
		/*objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconBppCompFactorSetting, 10);
		objPage.Click(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconBppCompFactorSetting));*/
		
		objBppTrnPg.createRecord();

		objBppTrendSetupPage.enter(objBppTrendSetupPage.minGoodFactorEditBox,"9");
		objBuildPermit.selectOptionFromDropDown("Property Type","Industrial");
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Opening the BPP Trend module and set All as the view option in grid
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(Integer.toString(year));

		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Agricultural property type **");
		if(objBppTrendSetupPage.moreTabRightSection != null) {
			objPage.Click(objBppTrendSetupPage.moreTabRightSection);
			objPage.Click(objBppTrendSetupPage.bppCompositeFactorOption);
			
        } else {
        	objPage.Click(objBppTrendSetupPage.bppCompFactorSettingTab);
		}
		objBppTrnPg.createRecord();

		objBppTrendSetupPage.enter(objBppTrendSetupPage.minGoodFactorEditBox,"11");
		objBuildPermit.selectOptionFromDropDown("Property Type","Agricultural");
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Opening the BPP Trend module and set All as the view option in grid
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(Integer.toString(year));

		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Construction property type **");
		if(objBppTrendSetupPage.moreTabRightSection != null) {
			objPage.Click(objBppTrendSetupPage.moreTabRightSection);
			objPage.Click(objBppTrendSetupPage.bppCompositeFactorOption);
			
        } else {
        	objPage.Click(objBppTrendSetupPage.bppCompFactorSettingTab);
		}
		objBppTrnPg.createRecord();

		objBppTrendSetupPage.enter(objBppTrendSetupPage.minGoodFactorEditBox,"10");
		objBuildPermit.selectOptionFromDropDown("Property Type","Construction");
		objPage.Click(objPage.getButtonWithText("Save"));

		Thread.sleep(1000);

		//Step16: Navigating to BPP Trend page and selecting given roll year
		objBppTrnPg.selectRollYearOnBPPTrends(Integer.toString(year));

		//Step17: Iterating over the given tables to validate error message on calculate button
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Validating error messages on Calculate button click when BPP Percent & Goods Factors are missing **");
		for (int i = 0; i < allTables.size() - 1; i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Performing Validations For: '"+ allTables.get(i) +"' Table **");
			//Clicking on the given table name
			tableName = allTables.get(i);
			
			//Generating expected error message based on the table name
			String msgOnMissingPropertyAndGoodsData = "No Goods factor found for type Machinery and Equipment";
			if(tableName.equalsIgnoreCase("Agricultural Mobile Equipment Composite Factors")) {
				msgOnMissingPropertyAndGoodsData = "Index factors not found for Agricultural";
			} else if(tableName.equalsIgnoreCase("Construction Mobile Equipment Composite Factors")) {
				msgOnMissingPropertyAndGoodsData = "Index factors not found for Construction";
			} else {
				msgOnMissingPropertyAndGoodsData = "No Goods factor found for type Machinery and Equipment";
			}
			
			//Clicking on given table name
			//boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			//objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i), isTableUnderMoreTab);
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i));

			//Click calculate button for given tables individually
			objBppTrnPg.isCalculateBtnVisible(30, tableName);
			objBppTrnPg.clickCalculateBtn(tableName);
			
			//Retrieve and return the error message displayed in pop up on clicking calculate button
			String actErrorMessage = objBppTrnPg.waitForErrorPopUpMsgOnCalculateClick(20);
			softAssert.assertContains(actErrorMessage, msgOnMissingPropertyAndGoodsData, "SMAB-T193: Error message encountered when triggered calculation with missing calculation variables(BPP Property Index Factors, BPP Property Good Factors)");
			if(tableName.equalsIgnoreCase("Construction Composite Factors")) {
				softAssert.assertContains(actErrorMessage, msgOnMissingPropertyAndGoodsData, "SMAB-T257: Error message encountered when triggered calculation with missing input tables(BPP Property Index Factors, BPP Property Good Factors)");
			}
		}

		//Step18: Deleting the new BPP Trend Setup created
		queryForBppTrendRollYear = "Select Id From BPP_Trend_Roll_Year__c Where Roll_Year__c = '"+ year +"'";
		new SalesforceAPI().delete("BPP_Trend_Roll_Year__c", queryForBppTrendRollYear);
		
		//Step20: Log out application at end of test case
		softAssert.assertAll();
		objBppTrendSetupPage.logout();
	}
	
}