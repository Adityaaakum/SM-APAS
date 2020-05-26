package com.apas.Tests.BppTrend;

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
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import org.testng.asserts.SoftAssert;

public class BppTrend_AgriculturalCompostieTable_CalculateAndReCalculate extends TestBase {

	private RemoteWebDriver driver;
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
	 * DESCRIPTION: Performing Following Validations <AGRICULTURAL COMPOSITE FACTORS>::
	 * 1. Validating the availability of CalculateAll button
	 * 2. Validating the availability of Calculate button:: TestCase/JIRA ID: SMAB-T190
	 * 3. Validating the unavailability of Recalculate button for table that is Not Calculated:: TestCase/JIRA ID: SMAB-T198
	 * 4. Validating the message displayed above table before initiating calculations
	 * 5. Performing calculation on clicking Calculate button
	 * 6. Validating the presence of table grid once calculation is successful:: TestCase/JIRA ID: SMAB-T190, SMAB-T194
	 * 7. Validating the message displayed above table after calculation is done
	 * 8. Validating the presence of Recalculate button for Calculated table
	 * 9. Validating the absence of Calculate button
	 * 10. Validating the absence of Submit For Approval button:: TestCase/JIRA ID: SMAB-T442
	 * 11. Validating the data of UI table against the Trend Calculator excel file:: TestCase/JIRA ID: SMAB-T209
	 * 12. Validating the status of the table on BPP Trend Setup Page: SMAB-T269
	 */
	@Test(description = "SMAB-T209,SMAB-T269,SMAB-T190,SMAB-T194,SMAB-T198,SMAB-T442: Performing validation on Agricultural COMPOSITE FACTORS before and after calculation", groups = {"smoke,regression,BppTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verify_BppTrend_AgriculturalCompositeFactors_CalculateAndCompare(String loginUser) throws Exception {					
		//Step1: Reseting the status of all composite factor tables to "Not Calculated" through SalesForce API
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);
		
		//Step2: Log into application and navigate to BPP Trend page and select a roll year which has data
		objApasGenericFunctions.login(loginUser);
		
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		String tableName = "Agricultural Composite Factors";
		
		//Step3: Validating presence of CalculateAll button at page level
		boolean	isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		softAssert.assertTrue(isCalculateAllBtnDisplayed, "SMAB-T166: Calculate all button is visible at page level");
			
		//Step4: Clicking on the given table
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
				
		//Step5: Validating presence of calculate button for given tables individually
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, tableName);
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T190: Calculate button is available above '"+ tableName +"' table");

		//Step6: Validating unavailability of ReCalculate button at table level before performing calculation
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(3, tableName);
		softAssert.assertTrue(!isReCalculateBtnDisplayed, "SMAB-T198: ReCalcuate button is not visible for Not Calculated table");
		
		//Step7: Retrieve message displayed above table before clicking calculate button
		String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeCalculation");
		objSoftAssert.assertEquals(actTableMsgBeforeCalc, expTableMsgBeforeCalc, "Message displayed above '"+ tableName +"' before calculation is initiated");
	 	
		//Step8: Clicking on calculate button to initiate calculation
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Calculate' Button");
		objBppTrnPg.clickCalculateBtn(tableName);
		
		//Step9: Waiting for pop up message to display and the message displayed above table to update
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);			

		//Step10: Validation to check whether calculation is successfully and table data appears for given table
		boolean isTableVisible = objBppTrnPg.isTableDataVisible(tableName);
		softAssert.assertTrue(isTableVisible, "SMAB-T190: User successfully triggered calculation for '"+ tableName +" 'table");
		softAssert.assertTrue(isTableVisible, "SMAB-T194: User successfully triggered calculation for '"+ tableName +" 'table with valid calculation variables");
			
		//Step11: Retrieve & Assert updated message  displayed above table
		String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
		String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
		objSoftAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "Message displayed above the table after Calculation is completed");

		ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and table level");
		//Step12: Validating presence of ReCalculate button at table level after calculate button is clicked
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
		objSoftAssert.assertTrue(isReCalculateBtnDisplayed, "ReCalcuate button is visible for calculated table");
			
		//Below validation has been commented for now as 'Submit For Approval' button has been removed from UI
		//Step13: Validating availability of Submit For Approval buttons at table level on clicking calculate button
		//boolean isSubmitForApprovalBtnDisplayed = objBppTrnPg.isSubmitForApprovalBtnVisible(20, tableName);
		//softAssert.assertTrue(isSubmitForApprovalBtnDisplayed, "SMAB-T442: Submit For Approval button is visible for calculated table");
		
		//Step14: Validating presence of CalculateAll button at page level on performing the calculation
		isCalculateAllBtnDisplayed = objBppTrnPg.isCalculateAllBtnVisible(30);
		objSoftAssert.assertTrue(isCalculateAllBtnDisplayed, "CalcuateAll button is visible at page level");
			
		//Step15: Validating presence of ReCalculateAll button at page level on performing the calculation
		boolean isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
		objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "ReCalcuateAll button is visible at page level");
			
		//Step16: Validating absence of Calculate button at table level once calculation is done
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(2, tableName);
		objSoftAssert.assertTrue(!isCalculateBtnDisplayed, "Calculate button is not available for Calculated table");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "** Comparing the UI grid data against the data available in Trend Calculator file **");
		//Step17: Retrieving the path of excel file to read for data comparison on calculate button click
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_TABLES_DATA;
								
		//Step18: Generating data map from the Trend Calculator excel file using the given table name
		Map<String, List<Object>> dataMapFromExcel = objBppTrnPg.retrieveDataFromExcelForGivenTable(fileName, tableName);
			
		//Step19: Generating data map from the UI grid data	
		List<String> columnNames = objBppTrnPg.retrieveColumnNamesOfGridForGivenTable(tableName);
			
		//Step20: Retrieving column names of table from UI
		Map<String, List<Object>> dataMapFromUI = objBppTrnPg.retrieveDataFromGridForGivenTable(tableName);
			
		//Step21: Comparing the UI grid data after Calculate button click against the data in excel file
		System.setProperty("isElementHighlightedDueToFailre", "false");

		//Step22: Validating the tabular data against data retrieved from excel file
		for (Map.Entry<String, List<Object>> entry : dataMapFromExcel.entrySet()) {
			String currentKey = entry.getKey().toString();
			if (dataMapFromUI.containsKey(currentKey)) {
				List<Object> acquiredYearDataFromUI = dataMapFromUI.get(currentKey);
				List<Object> acquiredYearDataFromExcel = dataMapFromExcel.get(currentKey);
				
				if (acquiredYearDataFromUI.size() == acquiredYearDataFromExcel.size()) {
					boolean isDataMatched = true;
					for (int j = 0; j < acquiredYearDataFromExcel.size(); j++) {
						if (!(acquiredYearDataFromExcel.get(j).equals(acquiredYearDataFromUI.get(j)))) {
							objBppTrnPg.highlightMismatchedCellOnUI(tableName, currentKey, j);
							softAssert.assertTrue(false, "SMAB-T209: Data for '"+ tableName +" 'for year acquired '" + currentKey + "' and column named '"+ columnNames.get(j) + "' matched. Excel data: "+ acquiredYearDataFromExcel.get(j) + " || UI Data: " + acquiredYearDataFromUI.get(j), true);
						}
					}
					if(isDataMatched) {
						softAssert.assertTrue(true, "SMAB-T209: UI Data for '"+ tableName +" 'for year acquired '"+ currentKey +"' matched with Trend Calculator data.", true);
					}
				} else {
					System.setProperty("isElementHighlightedDueToFailre", "true");
					softAssert.assertTrue(false, "SMAB-T209: Data for '"+ tableName +" 'for year acquired '"+ currentKey +"' matched.", true);
				}
			} else {
				System.setProperty("isElementHighlightedDueToFailre", "true");
				softAssert.assertTrue(false, "SMAB-T209: Data for '"+ tableName +" 'for year acquired '"+ currentKey +"' is present in UI table.", true);
			}
		}
		if(System.getProperty("isElementHighlightedDueToFailre").equalsIgnoreCase("true")) {
			softAssert.assertTrue(false, "Excel & UI grid data has mismatched. Taking screen shot of entire table");
		}
		
		//Step23: Validating the table's status on BPP Trend Setup page
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		String statusInBppTrendSetup = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
		softAssert.assertEquals(statusInBppTrendSetup, "Calculated", "SMAB-T269: Table status on Bpp Trend Setup page post calculation");

		//Step24: Log out from the application
		objApasGenericFunctions.logout();
			
		//Step25: Assert all the assertions
		softAssert.assertAll();
	}


	/**
	 * DESCRIPTION: Performing Following Validations <AGRICULTURAL COMPOSITE FACTORS>::
	 * 1. Validating the availability of Recalculate All button for Calculated table
	 * 2. Validating the availability of Recalculate button for Calculated table:: Test Case/JIRA ID: SMAB-T195
	 * 3. Validating the message displayed above table before initiating calculations
	 * 4. Performing ReCalculation on clicking ReCalculate button
	 * 5. Validating warning message in pop up window on ReCalculate button's click:: Test Case/JIRA ID: SMAB-T196
	 * 6. Validating the presence of table grid once calculation is successful
	 * 7. Validating the message displayed above table after ReCalculation is done
	 * 8. Validating the presence of ReCalculate button for Calculated table:: Test Case/JIRA ID: SMAB-T195
	 * 9. Validating the absence of Submit For Approval button:: Test Case/JIRA ID: SMAB-T442
	 * 10. Validating the data of UI table against the Trend Calculator excel file:: Test Case/JIRA ID: SMAB-T302
	 * 11. Validating the status of the table on BPP Trend Setup Page
	 * 12. Reverting the changed settings in excel file and BPP Trend Setup page
	 */
	@Test(description = "SMAB-T195,SMAB-T196,SMAB-T442: Performing validation on AGRICULTURAL COMPOSITE FACTORS before and after calculation", groups = {"smoke,regression,BppTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verify_BppTrend_AgriculturalCompositeFactors_ReCalculateAndCompare(String loginUser) throws Exception {					
		//Step1: Resetting the composite factor tables status to Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Step2: Login with given user & navigate to BPP Trend Setup Page and click on given BPP trend setup
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		String tableName = null;
		String fileName = null;
		
		try {
			//Step3: Generate data map containing values of bpp composite factor settings and edit composite factor setting values
			Map<String, String> updatedSettingValues = new HashMap<String, String>();
			updatedSettingValues.put("Agricultural", "50");
			
			//Step4: Edit the values from view all page
			objBppTrnPg.editBppCompositeFactorValueOnViewAllPage(updatedSettingValues);
					
			//Step5: Opening the BPP Trend module		
			objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
			objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
			objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
			objBppTrnPg.clickOnGivenRollYear(rollYear);
			objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
	
			tableName = "Agricultural Composite Factors";
			
			//Step6: Validating presence of ReCalculateAll button at page level
			boolean	isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
			objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "ReCalcuate all button is visible");
				
			//Step7: Clicking on the given table
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
					
			//Step8: Validating availability of ReCalculate button at table level before performing calculation
			boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(3, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T195: ReCalcuate button is visible for Calculated table");
			
			//Step9: Retrieve message displayed above table before clicking calculate button
			String actTableMsgBeforeCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeCalc = CONFIG.getProperty("tableMsgBeforeReCalculation");
			objSoftAssert.assertEquals(actTableMsgBeforeCalc, expTableMsgBeforeCalc, "Message displayed above '"+ tableName +"' before ReCalculation is initiated");
			
			//Step10: Clicking on ReCalculate button
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'ReCalculate' Button ");
			objBppTrnPg.clickReCalculateBtn(tableName);
			
			//Step11: Validating the warning message on ReCalculate click
			String actWarningMsgInPopUp = objBppTrnPg.retrieveReCalculatePopUpMessage();
			String expWarningMsgInPopUp = CONFIG.getProperty("recalculatePopUpMsg");
			softAssert.assertContains(actWarningMsgInPopUp, expWarningMsgInPopUp, "SMAB-T196: Warning / Pop up message dislayed when 'ReCalculate' button is clicked");
			
			//Step12: Clicking 'Confirm' button in warning pop up to trigger ReCalculation
			objBppTrnPg.javascriptClick(objBppTrnPg.confirmBtnInPopUp);
			
			//Step13: Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);			
	
			//Step14: Validation to check whether ReCalculation is successful and table data appears for given table
			boolean isTableVisible = objBppTrnPg.isTableDataVisible(tableName);
			softAssert.assertTrue(isTableVisible, "SMAB-T195: User successfully triggered ReCalculation for '"+ tableName +" 'table");
				
			//Step15: Retrieve & Assert updated message displayed above table
			String actTableMsgPostCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgPostCalc = CONFIG.getProperty("tableMsgPostCalculation");
			objSoftAssert.assertEquals(actTableMsgPostCalc, expTableMsgPostCalc, "Message displayed above the table after ReCalculation is completed");
	
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Checking status of various buttons at page level and table level");
			//Step16: Validating presence of ReCalculate button at table level after calculate button is clicked
			isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, tableName);
			softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T195: ReCalcuate button is visible for calculated table");
									
			//Step17: Validating presence of ReCalculateAll button at page level on performing the calculation
			isReCalculateAllBtnDisplayed = objBppTrnPg.isReCalculateAllBtnVisible(30);
			objSoftAssert.assertTrue(isReCalculateAllBtnDisplayed, "ReCalcuateAll button is visible at page level");
						
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Comparing the UI grid data against the data available in Trend Calculator file **");
			//Step18: Retrieving the path of excel file to read for data comparison on calculate button click
			fileName = System.getProperty("user.dir") + testdata.BPP_TREND_TABLES_DATA;
									
			//Step19: Update Agricultural Setting value in excel and retrieve data into a data map for give table
			objBppTrnPg.updateTrendSettingInExcel(fileName, updatedSettingValues);
			Map<String, List<Object>> dataMapFromExcel = objBppTrnPg.retrieveDataFromExcelForGivenTable(fileName, tableName);
				
			//Step21: Generating data map from the UI grid data	
			List<String> columnNames = objBppTrnPg.retrieveColumnNamesOfGridForGivenTable(tableName);
				
			//Step20: Retrieving column names of table from UI
			Map<String, List<Object>> dataMapFromUI = objBppTrnPg.retrieveDataFromGridForGivenTable(tableName);
				
			//Step22: Comparing the UI grid data after Calculate button click against the data in excel file
			System.setProperty("isElementHighlightedDueToFailre", "false");
	
			//Step23: Validating the tabular data against data retrieved from excel file
			for (Map.Entry<String, List<Object>> entry : dataMapFromExcel.entrySet()) {
				String currentKey = entry.getKey().toString();
				if (dataMapFromUI.containsKey(currentKey)) {
					List<Object> acquiredYearDataFromUI = dataMapFromUI.get(currentKey);
					List<Object> acquiredYearDataFromExcel = dataMapFromExcel.get(currentKey);
					
					if (acquiredYearDataFromUI.size() == acquiredYearDataFromExcel.size()) {
						boolean isDataMatched = true;
						for (int j = 0; j < acquiredYearDataFromExcel.size(); j++) {
							if (!(acquiredYearDataFromExcel.get(j).equals(acquiredYearDataFromUI.get(j)))) {
								objBppTrnPg.highlightMismatchedCellOnUI(tableName, currentKey, j);
								
								softAssert.assertTrue(false, "SMAB-T302: Data for '"+ tableName +" 'for year acquired '" + currentKey + "' and column named '"+ columnNames.get(j) + "' matched. Excel data: "+ acquiredYearDataFromExcel.get(j) + " || UI Data: " + acquiredYearDataFromUI.get(j), true);
							}
						}
						if(isDataMatched) {
							softAssert.assertTrue(true, "SMAB-T302: UI Data for '"+ tableName +" 'for year acquired '"+ currentKey +"' matched with Trend Calculator data.", true);
						}
					} else {
						System.setProperty("isElementHighlightedDueToFailre", "true");
						softAssert.assertTrue(false, "SMAB-T302: Data for '"+ tableName +" 'for year acquired '"+ currentKey +"' matched.", true);
					}
				} else {
					System.setProperty("isElementHighlightedDueToFailre", "true");
					softAssert.assertTrue(false, "SMAB-T302: Data for '"+ tableName +" 'for year acquired '"+ currentKey +"' is present in UI table.", true);
				}
			}
			if(System.getProperty("isElementHighlightedDueToFailre").equalsIgnoreCase("true")) {
				softAssert.assertTrue(false, "Excel & UI grid data has mismatched. Taking screen shot of entire table");
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			
			//Step24: Reverting the values of BPP composite factor settings in excel file
			objBppTrnPg.revertTrendSettingsDataInExcel(fileName);
			
			//Step25: Reverting the values of BPP composite factor settings from view all page
			objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
			objApasGenericFunctions.selectAllOptionOnGrid();
			objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
			objBppTrnPg.editBppCompositeFactorValueOnViewAllPage(objBppTrnPg.trendSettingsOriginalValues);
		}

		//Step26: Log out from the application
		objApasGenericFunctions.logout();
			
		//Step27: Assert all the assertions
		softAssert.assertAll();
	}
	
}
