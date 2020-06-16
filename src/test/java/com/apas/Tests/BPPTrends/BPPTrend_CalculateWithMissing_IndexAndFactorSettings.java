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
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_CalculateWithMissing_IndexAndFactorSettings extends TestBase {

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
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating error message for missing BPP Settings (Maximum Equip. Index Value):: TestCase/JIRA Id: SMAB-T192,SMAB-T163
	 * 2. Validating error message for missing BPP Composite Settings (Minimum Index Value):: TestCase/JIRA Id: SMAB-T192
	 * 3. Validating error message for missing good and index factor data:: TestCase/JIRA Id: SMAB-T193,SMAB-T257
	 * 4. Deleting the dummy/test BPP Trend Setup
	 */
	@Test(description = "SMAB-T163,SMAB-T192,SMAB-T193,SMAB-T257: Calculation of INDUSTRIAL COMPOSITE FACTORS with missing calculation variables", groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_CompositeFactors_CalculateWithCalculationVariablesMissing(String loginUser) throws Exception {					
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		
		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		
		//Step3: Creating a new BPP trend setup with no BPP settings, no composite factors settings, no index & goods factor data for future roll year
		objBppTrnPg.createDummyBppTrendSetupForErrorsValidation("Not Calculated");
		objApasGenericFunctions.logout();
		
		//Step4: Login with given login user
		objApasGenericFunctions.login(loginUser);
		
		//Step5: Navigating to BPP Trend page and selecting given roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Navigating back to BPP Trends page **");
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		
		String rollYear = System.getProperty("rollYearForErrorValidationOnCalculate");
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step6: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab");
		String tableName;
		
		//Step7: Iterating over the given tables to validate error message on calculate click
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Validating error messages on Calculate button click when Maximum Equipment Index Factor is missing **");
		for (int i = 0; i < allTables.size() - 1; i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Performing Validations For: '"+ allTables.get(i) +"' Table **");
			// Clicking on the given table name
			tableName = allTables.get(i);
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i), isTableUnderMoreTab);
			
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
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step9: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrnPg.clickOnEntryNameInGrid(rollYear);
		
		//Step10: Create a BPP Setting under selected BPP Trend Setup
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Maximum Equipment Index Factor **");
		objBppTrnPg.createBppSetting("125");
		Thread.sleep(2000);
		
		//Step11: Navigating to BPP Trend page and selecting given roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Navigating back to BPP Trends page **");
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

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
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i), isTableUnderMoreTab);
			
			//Click calculate button for given tables individually
			objBppTrnPg.isCalculateBtnVisible(30, tableName);
			objBppTrnPg.clickCalculateBtn(tableName);
			
			//Retrieve and return the error message displayed in pop up on clicking calculate button
			String actErrorMessage = objBppTrnPg.waitForErrorPopUpMsgOnCalculateClick(20);
			softAssert.assertContains(actErrorMessage, msgOnMissingCompositeFactorSetting, "SMAB-T192: Error message encountered when triggered calculation with missing calculation variables(Min. Good Factor)");
		}
		
		//Step13: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step14: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrnPg.clickOnEntryNameInGrid(rollYear);
		
		//Step15: Create a BPP Composite Factor Settings
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Clicking on Bpp Composite Factors Settings tab **");	
		if(objBppTrnPg.moreTabRightSection != null) {
			objBppTrnPg.clickAction(objBppTrnPg.moreTabRightSection);
			objBppTrnPg.clickAction(objBppTrnPg.bppCompositeFactorOption);
        } else {
        	objBppTrnPg.clickAction(objBppTrnPg.bppCompFactorSettingTab);
		}
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Commercial property type **");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnToCreateEntry, 20);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBtnToCreateEntry));

		objBppTrnPg.enterFactorValue("10");
		objBppTrnPg.enterPropertyType("Commercial");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		Thread.sleep(1000);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Industrial property type **");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnToCreateEntry, 20);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBtnToCreateEntry));

		objBppTrnPg.enterFactorValue("9");
		objBppTrnPg.enterPropertyType("Industrial");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		Thread.sleep(1000);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Agricultural property type **");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnToCreateEntry, 20);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBtnToCreateEntry));

		objBppTrnPg.enterFactorValue("11");
		objBppTrnPg.enterPropertyType("Agricultural");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		Thread.sleep(1000);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Creating entry for Construction property type **");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting, 10);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnToCreateEntry, 20);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBtnToCreateEntry));

		objBppTrnPg.enterFactorValue("10");
		objBppTrnPg.enterPropertyType("Construction");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		Thread.sleep(1000);	
		
		//Step16: Navigating to BPP Trend page and selecting given roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Navigating back to BPP Trends page **");
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

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
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(allTables.get(i));
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i), isTableUnderMoreTab);
			
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
		String queryForBppTrendRollYear = "Select Id From BPP_Trend_Roll_Year__c Where Roll_Year__c = '"+ rollYear +"'";
		new SalesforceAPI().delete("BPP_Trend_Roll_Year__c", queryForBppTrendRollYear);
		
		//Step20: Log out application at end of test case
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
}