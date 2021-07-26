package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.apas.Reports.ReportLogger;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.config.modules;
import com.apas.config.users;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_CalculateWithMissing_IndexAndFactorSettings_Test extends TestBase {

	RemoteWebDriver driver;
	BppTrendPage objBppTrnPg;
	BppTrendSetupPage objBppTrendSetupPage;
	String rollYear;

	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI objSalesforceAPI  = new SalesforceAPI();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objBppTrnPg = new BppTrendPage(driver);

		rollYear = "2022";
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
		objBppTrendSetupPage.updateRollYearStatus("Open", rollYear);
		objBppTrendSetupPage.updateRollYearStatus("Open", Integer.toString(Integer.parseInt(rollYear) + 2));

	}

	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating error message for missing BPP Settings (Maximum Equip. Index Value):: TestCase/JIRA Id: SMAB-T192,SMAB-T163
	 * 2. Validating error message for missing BPP Composite Settings (Minimum Index Value):: TestCase/JIRA Id: SMAB-T192
	 * 3. Validating error message for missing good and index factor data:: TestCase/JIRA Id: SMAB-T193,SMAB-T257
	 * 4. Deleting the dummy/test BPP Trend Setup
	 */
	@Test(description = "SMAB-T163,SMAB-T192,SMAB-T193,SMAB-T257: Calculation of INDUSTRIAL COMPOSITE FACTORS with missing calculation variables", groups = {"Regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_CompositeFactors_CalculateWithCalculationVariablesMissing(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		objBppTrendSetupPage.login(users.SYSTEM_ADMIN);
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		Thread.sleep(6000);
		objBppTrendSetupPage.closeDefaultOpenTabs();
		
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
		Thread.sleep(5000);
		objBppTrendSetupPage.closeDefaultOpenTabs();
		
		//Step5: Navigating to BPP Trend page and selecting given roll year
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS);

		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
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
		ReportLogger.INFO("Validating error messages on Calculate button click when Maximum Equipment Index Factor is missing");
		for (int i = 0; i < allTables.size() - 1; i++) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Performing Validations For: '"+ allTables.get(i) +"' Table **");
			// Clicking on the given table name
			tableName = allTables.get(i);
			Thread.sleep(1000);
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i));

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
		Thread.sleep(3000);
		objBppTrendSetupPage.displayRecords("All");
		
		//Step9: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(Integer.toString(year));
		
		//Step10: Create a BPP Setting under selected BPP Trend Setup
		ReportLogger.INFO("** Creating entry for Maximum Equipment Index Factor **");
		objBppTrendSetupPage.createBppSetting("125");
		Thread.sleep(2000);

		//Step11: Navigating to BPP Trend page and selecting given roll year
		objBppTrnPg.selectRollYearOnBPPTrends(Integer.toString(year));

		//Step12: Iterating over the given tables to validate error message on calculate button
		ReportLogger.INFO("Validating error messages on Calculate button click when BPP Composite Factor Setting is missing **");
		for (int i = 0; i < allTables.size() - 1; i++) {
			ReportLogger.INFO("** Performing Validations For: '"+ allTables.get(i) +"' Table **");

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
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i));

			//Click calculate button for given tables individually
			objBppTrnPg.clickCalculateBtn(tableName);
			
			//Retrieve and return the error message displayed in pop up on clicking calculate button
			String actErrorMessage = objBppTrnPg.waitForErrorPopUpMsgOnCalculateClick(20);
			softAssert.assertContains(actErrorMessage, msgOnMissingCompositeFactorSetting, "SMAB-T192: Error message encountered when triggered calculation with missing calculation variables(Min. Good Factor)");
		}

		//Creating the BPP Trend Factors
		String bppTrendSetUpYear = Integer.toString(year);
		objBppTrendSetupPage.createCompositeFactor(bppTrendSetUpYear,"Commercial", "10");
		objBppTrendSetupPage.createCompositeFactor(bppTrendSetUpYear,"Industrial","9");
		objBppTrendSetupPage.createCompositeFactor(bppTrendSetUpYear,"Agricultural","11");
		objBppTrendSetupPage.createCompositeFactor(bppTrendSetUpYear,"Construction","10");

		//Step16: Navigating to BPP Trend page and selecting given roll year
		objBppTrnPg.selectRollYearOnBPPTrends(Integer.toString(year));

		//Step17: Iterating over the given tables to validate error message on calculate button
		ReportLogger.INFO("Validating error messages on Calculate button click when BPP Percent & Goods Factors are missing **");
		for (int i = 0; i < allTables.size() - 1; i++) {
			ReportLogger.INFO("** Performing Validations For: '"+ allTables.get(i) +"' Table **");
			//Clicking on the given table name
			tableName = allTables.get(i);
			
			//Generating expected error message based on the table name
			String msgOnMissingPropertyAndGoodsData = "Error Calculating data.";
			
			objBppTrnPg.clickOnTableOnBppTrendPage(allTables.get(i));

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