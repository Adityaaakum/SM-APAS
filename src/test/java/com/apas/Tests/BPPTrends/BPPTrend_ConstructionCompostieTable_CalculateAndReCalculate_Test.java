package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.FileUtils;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_ConstructionCompostieTable_CalculateAndReCalculate_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrend;
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
		objBppTrend = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objSalesforceAPI = new SalesforceAPI();
		objBuildPermit = new BuildingPermitPage(driver);
		objSoftAssert = new SoftAssert();
        objBppTrendSetupPage = new BppTrendSetupPage(driver);
        objApasGenericFunctions.updateRollYearStatus("Open", "2020");
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations <CONSTRUCTION COMPOSITE FACTORS>::
	 * 1. Validating the availability of CalculateAll button
	 * 2. Validating the availability of Calculate button:: TestCase/JIRA ID: SMAB-T190
	 * 3. Validating the unavailability of Recalculate button for table that is Not Calculated:: TestCase/JIRA ID: SMAB-T198
	 * 4. Validating the message displayed above table before initiating calculations
	 * 5. Performing calculation on clicking Calculate button
	 * 6. Validating the presence of table grid once calculation is successful:: TestCase/JIRA ID: SMAB-T190, SMAB-T194, SMAB-T253
	 * 7. Validating the message displayed above table after calculation is done
	 * 8. Validating the presence of Recalculate button for Calculated table
	 * 9. Validating the absence of Calculate button
	 * 11. Validating the data of UI table against the Trend Calculator excel file:: TestCase/JIRA ID: SMAB-T254
	 * 12. Validating the status of the table on BPP Trend Setup Page: SMAB-T255
	 */
	@Test(description = "SMAB-T170,SMAB-T253,SMAB-T254,SMAB-T255,SMAB-T190,SMAB-T194,SMAB-T198: Performing validation on CONSTRUCTION COMPOSITE FACTORS before and after calculation", groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_ConstructionCompostieTable_CalculateAndCompare(String loginUser) throws Exception {
		String tableName = "Construction Composite Factors";
		
		String tableMessages = System.getProperty("user.dir") + testdata.BPP_TREND_COMPOSITE_FACTORS_DATA;
		Map<String, String> tableMessagesDataMap = objUtil.generateMapFromJsonFile(tableMessages, "MessagesOnTable");
		
		//Step1: Updating the composite factor tables to "Not Calculated"
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYear);
		
		//Resetting the maximum equipment index factor value to default value
		objBppTrend.updateMaximumEquipmentIndexFactorValue("125", rollYear);

		//Resetting the minimum equipment index factor value to default value
		objBppTrend.updateMinimumEquipmentIndexFactorValue("Construction", "10", rollYear);

		//Step2: Login to the APAS application, Opening the BPP Trend module and selecting the Roll Year
		objApasGenericFunctions.login(loginUser);
		objBppTrend.selectRollYearOnBPPTrends(rollYear);

		//Step3: Validating presence of CalculateAll button at page level
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathCalculateAllBtn, 20)), "SMAB-T166: Calculate all button is visible at page level");
				
		//Step4: Clicking on the given table
		objBppTrend.clickOnTableOnBppTrendPage(tableName);

		//Step5: Validating presence of calculate button for given tables individually
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathCalculateBtn, 20)), "SMAB-T190: Calculate button is available above '"+ tableName +"' table");
		
		//Step6: Validating unavailability of ReCalculate button at table level before performing calculation
		softAssert.assertTrue(Objects.isNull(objPage.locateElement(objBppTrend.xPathReCalculateBtn, 20)), "SMAB-T198: ReCalcuate button is not visible for Not Calculated table");

		//Step7: Retrieve message displayed above table before clicking calculate button
		objPage.waitUntilElementIsPresent(objBppTrend.xpathTableMessage, 420);
		objSoftAssert.assertEquals(objPage.getElementText(objBppTrend.tableMessage), tableMessagesDataMap.get("tableMsgBeforeReCalculation"), "Message displayed above '"+ tableName +"' before Calculation is initiated");

		//Step8: Clicking on calculate button to initiate calculation
		ReportLogger.INFO("* Clicking 'Calculate' Button");
		objPage.Click(objBppTrend.calculateBtn);
		objPage.waitForElementToDisappear(objBppTrend.xpathSpinner, 120);
		Thread.sleep(5000);
		//Step9: Waiting for pop up message to display and the message displayed above table to update
		//softAssert.assertContains(objBppTrend.getErrorMsgFromPopUp(30), tableMessagesDataMap.get("popUpMsgPostCalculation"), "SMAB-T190: Validating pop up message on calculating the table data");

		//Step10: Validation to check whether calculation is successfully and table data appears for given table
		softAssert.assertTrue(objBppTrend.isTableDataVisible(tableName, 30), "SMAB-T190,SMAB-T194,SMAB-T253: User successfully triggered calculation for '"+ tableName +" 'table");
		
		//Step11: Retrieve & Assert updated message  displayed above table
		objPage.waitUntilElementIsPresent(objBppTrend.xpathTableMessage, 420);
		objSoftAssert.assertEquals(objPage.getElementText(objBppTrend.tableMessage), tableMessagesDataMap.get("tableMsgPostCalculation"), "Message displayed above the table after Calculation is completed");

		ReportLogger.INFO("* Checking status of various buttons at page level and table level");
		//Step12: Validating presence of ReCalculate button at table level after calculate button is clicked
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathReCalculateBtn, 20)), "ReCalculate button is visible for calculated table");

		//Step14: Validating presence of CalculateAll button at page level on performing the calculation
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathCalculateAllBtn, 20)), "Calculate all button is visible at page level");

		//Step15: Validating presence of ReCalculateAll button at page level on performing the calculation
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathReCalculateAllBtn, 20)),  "ReCalcuateAll button is visible at page level");

		//Step16: Validating absence of Calculate button at table level once calculation is done
		softAssert.assertTrue(Objects.isNull(objPage.locateElement(objBppTrend.xPathCalculateBtn, 20)), "Calculate button is not available for Calculated table");

		ReportLogger.INFO("** Comparing the UI grid data against the data available in Trend Calculator file **");
		//Step17: Retrieving the path of excel file to read for data comparison on calculate button click
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CALCULATOR + "2020_Trend_Factors_Calculator.xlsx";
		
		//Step18: Generating data map from the Trend Calculator excel file using the given table name
		HashMap<String, ArrayList<String>> dataMapFromExcel = ExcelUtils.getExcelSheetData(fileName, 4);
		dataMapFromExcel.remove("Age");

		//Step19: Generating data map from the UI grid data
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objApasGenericFunctions.getGridDataInHashMap(1);

		//Step20: Comparing the UI grid data after Calculate button click against the data in excel file
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,dataMapFromExcel),"","SMAB-T254: Data Comparison validation for Imported Row Table");
		
		//Step21: Validating the table's status on BPP Trend Setup page
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		String statusInBppTrendSetup = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
		softAssert.assertEquals(statusInBppTrendSetup, "Calculated", "SMAB-T170,SMAB-T255: Table status on Bpp Trend Setup page post calculation");

		//Step22: Log out from the application
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Performing Following Validations <CONSTRUCTION COMPOSITE FACTORS>::
	 * 1. Validating the availability of Recalculate All button for Calculated table
	 * 2. Validating the availability of Recalculate button for Calculated table:: Test Case/JIRA ID: SMAB-T195
	 * 3. Validating the message displayed above table before initiating calculations
	 * 4. Performing ReCalculation on clicking ReCalculate button
	 * 5. Validating warning message in pop up window on ReCalculate button's click:: Test Case/JIRA ID: SMAB-T196
	 * 6. Validating the presence of table grid once calculation is successful
	 * 7. Validating the message displayed above table after ReCalculation is done
	 * 8. Validating the presence of ReCalculate button for Calculated table:: Test Case/JIRA ID: SMAB-T195
	 * 9. Validating the data of UI table against the Trend Calculator excel file:: Test Case/JIRA ID: SMAB-T195
	 */
	@Test(description = "SMAB-T173,SMAB-T195,SMAB-T196: Performing validation on CONSTRUCTION COMPOSITE FACTORS before and after calculation", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_ConstructionCompostieTable_ReCalculateAndCompare(String loginUser) throws Exception {
		String tableName = "Construction Composite Factors";
		
		String tableMessages = System.getProperty("user.dir") + testdata.BPP_TREND_COMPOSITE_FACTORS_DATA;
		Map<String, String> tableMessagesDataMap = objUtil.generateMapFromJsonFile(tableMessages, "MessagesOnTable");
		
		//Step1: Updating the composite factor tables to "Not Calculated"
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);
		
		//Resetting the maximum equipment index factor value to default value
		objBppTrend.updateMaximumEquipmentIndexFactorValue("125", rollYear);

		//Resetting the minimum equipment index factor value to default value
		objBppTrend.updateMinimumEquipmentIndexFactorValue("Construction", "50", rollYear);

		//Step2: Login to the APAS application, Opening the BPP Trend module and selecting the Roll Year
		objApasGenericFunctions.login(loginUser);
		objBppTrend.selectRollYearOnBPPTrends(rollYear);
		
		//Step3: Validating presence of ReCalculateAll button at page level
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathReCalculateAllBtn, 20)), "ReCalcuate all button is visible");

		//Step4: Clicking on the given table
		objBppTrend.clickOnTableOnBppTrendPage(tableName);

		//Step5: Validating availability of ReCalculate button at table level before performing calculation
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathReCalculateBtn, 20)), "SMAB-T195: ReCalcuate button is visible for Calculated table");

		//Step6: Retrieve message displayed above table before clicking calculate button
		objPage.waitUntilElementIsPresent(objBppTrend.xpathTableMessage, 420);
		objSoftAssert.assertEquals(objPage.getElementText(objBppTrend.tableMessage), tableMessagesDataMap.get("tableMsgBeforeReCalculation"), "Message displayed above '"+ tableName +"' before ReCalculation is initiated");

		//Step7: Clicking on ReCalculate button
		ReportLogger.INFO("* Clicking 'ReCalculate' Button ");
		objPage.Click(objBppTrend.reCalculateBtn);
		Thread.sleep(3000);
		
		//Step8: Clicking 'Confirm' button in warning pop up to trigger ReCalculation
		objSoftAssert.assertEquals(objPage.getElementText(objBppTrend.reCalculateWarningMessage),"The new calculations will overwrite the previously made calculations for the roll year. Would you like to proceed?","SMAB-T196: Verify warning message on triggering recalculation");
		objBppTrend.javascriptClick(objBppTrend.confirmBtnInPopUp);	
		objPage.waitForElementToDisappear(objBppTrend.xpathSpinner, 120);
		Thread.sleep(5000);

		//Step9: Waiting for pop up message to display and the message displayed above table to update
		//softAssert.assertContains(objBppTrend.getSuccessMessageFromPopUp(30), tableMessagesDataMap.get("popUpMsgPostCalculation"), "SMAB-T195: Validating pop up message on calculating the table data");

		//Step10: Validation to check whether ReCalculation is successful and table data appears for given table
		softAssert.assertTrue(objBppTrend.isTableDataVisible(tableName, 30), "SMAB-T195: User successfully triggered ReCalculation for '"+ tableName +" 'table");
				
		//Step11: Retrieve & Assert updated message displayed above table
		objPage.waitUntilElementIsPresent(objBppTrend.xpathTableMessage, 420);
		objSoftAssert.assertEquals(objPage.getElementText(objBppTrend.tableMessage), tableMessagesDataMap.get("tableMsgPostCalculation"), "Message displayed above the table after ReCalculation is completed");
		
		ReportLogger.INFO("* Checking status of various buttons at page level and table level");
		//Step12: Validating presence of ReCalculate button at table level after calculate button is clicked
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathReCalculateBtn, 20)), "SMAB-T195: ReCalculate button is visible for calculated table");

		//Step13: Validating presence of ReCalculateAll button at page level on performing the calculation
		softAssert.assertTrue(Objects.nonNull(objPage.locateElement(objBppTrend.xPathReCalculateAllBtn, 20)), "ReCalculateAll button is visible at page level");
				
		ReportLogger.INFO("** Comparing the UI grid data against the data available in Trend Calculator file **");
		//Step14: Retrieving the path of excel file to read for data comparison on calculate button click
		String fileName = System.getProperty("user.dir") + testdata.BPP_TREND_CALCULATOR_WITH_UPDATED_MIN_EQIP_INDEX_FACTOR + "2020_Trend_Factors_Calculator.xlsx";

		//Step15: Generating data map from the Trend Calculator excel file using the given table name
		HashMap<String, ArrayList<String>> dataMapFromExcel = ExcelUtils.getExcelSheetData(fileName, 4);
		dataMapFromExcel.remove("Age");

		//Step16: Generating data map from the UI grid data
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objApasGenericFunctions.getGridDataInHashMap(1);

		//Step17: Comparing the UI grid data after Calculate button click against the data in excel file
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,dataMapFromExcel),"","SMAB-T195: Data Comparison validation for Imported Row Table");
		
		//Step18: Validating the table's status on BPP Trend Setup page
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		String statusInBppTrendSetup = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
		softAssert.assertEquals(statusInBppTrendSetup, "Calculated", "SMAB-T173: Verify Table status on Bpp Trend Setup page post calculation");

		//Step19: Log out from the application
		objApasGenericFunctions.logout();
	}
}