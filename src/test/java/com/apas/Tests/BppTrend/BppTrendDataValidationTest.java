package com.apas.Tests.BppTrend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendDataValidationTest extends TestBase {

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

	@Test(description = "SMAB-T165,SMAB-T240,SMAB-T254,SMAB-T277: Perform composie factor tables data verification for data from UI against excel", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verifyBppTrendsCompositeFactorTablesCalculations(String loginUser) throws Exception {
		List<String> tablesAndJiraIDsList = Arrays.asList(CONFIG.getProperty("tablesNamesWithJiraIdsForCalculation").split(","));
		Map<String, String> tablesAndJiraIDsMap = new HashMap<String, String>();
		for (String item : tablesAndJiraIDsList) {
			String[] dataArray = item.split(":");
			tablesAndJiraIDsMap.put(dataArray[0], dataArray[1]);
		}

		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_TABLES_DATA;

		// Step1: Resetting the factor tables status to Not Calculated
		String rollYear = CONFIG.getProperty("rollYear");
		List<String> factorTablesToReset = Arrays.asList(CONFIG.getProperty("factorTablesToReset").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(factorTablesToReset, "Not Calculated", rollYear);

		// Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		// Step3: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		// Step4: Selecting role year from drop down
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		// Step5: Fetching factor table names from properties file and consolidating them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));

		// Step6: Validating whether given table falls under more tab and the clicking the given table name
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");

		// Step7: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			String tableToValidate = allTables.get(i);
			
			// If map contains the current table name as a key, then check calculation else continue 
			if (tablesAndJiraIDsMap.containsKey(tableToValidate)) {
				// Extract data from tables in excel file
				Map<String, List<Object>> dataMapFromExcel = objBppTrnPg.retrieveDataFromExcelForGivenTable(fileName, tableToValidate);

				// Clicking on the given table name
				boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableToValidate);
				objBppTrnPg.clickOnTableOnBppTrendPage(tableToValidate, isTableUnderMoreTab);

				// Clicking on calculate button to initiate calculation
				objBppTrnPg.clickCalculateBtn(tableToValidate);
				objBppTrnPg.retrieveMsgDisplayedAboveTable(tableToValidate);

				List<String> columnNames = objBppTrnPg.retrieveColumnNamesOfGridForGivenTable(tableToValidate);
				Map<String, List<Object>> dataMapFromUI = objBppTrnPg.retrieveDataFromGridForGivenTable(tableToValidate);

				for (Map.Entry<String, List<Object>> entry : dataMapFromExcel.entrySet()) {
					String currentKey = entry.getKey().toString();
					if (dataMapFromUI.containsKey(currentKey)) {
						List<Object> acquiredYearDataFromUI = dataMapFromUI.get(currentKey);
						List<Object> acquiredYearDataFromExcel = dataMapFromExcel.get(currentKey);
						if (acquiredYearDataFromUI.size() == acquiredYearDataFromExcel.size()) {
							for (int j = 0; j < acquiredYearDataFromExcel.size(); j++) {
								if (!(acquiredYearDataFromExcel.get(j).equals(acquiredYearDataFromUI.get(j)))) {
									// objBppTrnPg.highlightMismatchedCellOnUI(tableToValidate, currentKey, j);
									String jiraID = tablesAndJiraIDsMap.get(tableToValidate);
									softAssert.assertTrue(false,  jiraID + ": Data value for year acquired '" + currentKey + "' and column '"
													+ columnNames.get(j) + "' does not match. Excel data: "
													+ acquiredYearDataFromExcel.get(j) + " || UI Data: " + acquiredYearDataFromUI.get(j));
								}
							}
						} else {
							softAssert.assertTrue(false, "SMAB-T303: Size of data list for year acquired '"+ currentKey +"' does not match.");
						}
					} else {
						softAssert.assertTrue(false, "SMAB-T303: Data for year acquired'" + currentKey +"' is not available in UI table.");
					}
				}
			}
		}
	}
}