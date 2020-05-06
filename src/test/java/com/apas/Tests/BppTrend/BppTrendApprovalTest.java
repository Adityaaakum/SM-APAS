package com.apas.Tests.BppTrend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebElement;
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

public class BppTrendApprovalTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}

	@AfterMethod(alwaysRun=true)
	public void afterMethod() throws Exception {
		objApasGenericFunctions.logout();
	}

	@Test(description = "SMAB-T205,SMAB-T304,SMAB-T157: Approve calculations of valuation, composite & prop 13 tables", groups = {"smoke","regression","BPP Trends"}, dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verifyBppTrendApproveWorkFlow(String loginUser) throws Exception {		
		//Step1: Fetch table names from properties file and collect them in a single list
		List<String> allTablesBppTrendSetupPage = new ArrayList<String>();
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(",")));
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(",")));
		String rollYear = CONFIG.getProperty("rollYear");
		
		//Step2: Check status of the composite & valuation tables on bpp trend status page before approving
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		String tableName;
		for(int i = 0; i < allTablesBppTrendSetupPage.size(); i++) {
			tableName = allTablesBppTrendSetupPage.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Submitted for Approval", "SMAB-T157: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}
		
		//Step3: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step4: Selecting role year from drop down
		objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
		
		//Step5: Validating presence of Approve all button at page level.
		boolean isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
		softAssert.assertTrue(isApproveAllBtnDisplayed, "SMAB-T304: Approve all button is visible");
		
		//Step6: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");
		boolean isTableUnderMoreTab;
		//Step7: Iterating over the given tables and approving the calculation using Approve button
		for (int i = 0; i < allTables.size()-1; i++) {
			tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			// Clicking on the given table name
			isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);

			// Retrieve & Assert message displayed above table before clicking Approve button
			String actTableMsgBeforeApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeApprovingCalc = CONFIG.getProperty("tableMsgBeforeApproval");
			softAssert.assertTrue(actTableMsgBeforeApprovingCalc.equalsIgnoreCase(expTableMsgBeforeApprovingCalc), "SMAB-T205: Validating message at table level before approving the submitted calculation for table '" + tableName + "'");
			
			// Editing and saving cell data in the table for first & last factor tables specified the list
			if((i == 0) || (i == allTables.size()-1)) {
				WebElement cellTxtBox = objBppTrnPg.locateCellTxtBoxElementInGrid(tableName);
				int cellDataBeforeEdit = Integer.valueOf(objBppTrnPg.getElementText(cellTxtBox).split("\\n")[0]);
				WebElement editBtn = objBppTrnPg.locateEditButtonInFocusedCellTxtBox();
				objBppTrnPg.Click(cellTxtBox);
				objBppTrnPg.Click(editBtn);
				
				objBppTrnPg.editCellDataInGridForGivenTable(cellDataBeforeEdit + 1);
				objBppTrnPg.Click(objBppTrnPg.saveEditedCellData);
				
				objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
				objBppTrnPg.clickOnGivenRollYear(rollYear);
				objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
				
				objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
				cellTxtBox = objBppTrnPg.locateCellTxtBoxElementInGrid(tableName);
				int cellDataAfterEdit = Integer.valueOf(objBppTrnPg.getElementText(cellTxtBox).split("\\n")[0]);
				softAssert.assertTrue(cellDataBeforeEdit != cellDataAfterEdit, "SMAB-T205: Cell data changed in the table after editing the specified cell");
			}
			
			// Clicking on Approve button at table level to complete the pending approval
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Approve' button");
			objBppTrnPg.clickApproveButton(tableName);
			
			// Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnApproveClick(180);
			
			// Retrieve & Assert message displayed above table after clicking Approve button
			String actTableMsgAfterApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgAfterApprovingCalc = CONFIG.getProperty("tableMsgAfterApproval");
			softAssert.assertEquals(actTableMsgAfterApprovingCalc, expTableMsgAfterApprovingCalc, "SMAB-T205: Factor table calculation approved successfully");
			
			// Validating presence of Approve All button at page level on clicking Approve button
			isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
			softAssert.assertTrue(isApproveAllBtnDisplayed, "SMAB-T205: Approve All button is visible");
			
			// Validating absence of Approve button once the submitted calculation has been approved by clicking approve button for given table
			boolean isApproveBtnDisplayed = objBppTrnPg.isApproveBtnVisible(5, tableName);
			softAssert.assertTrue(!isApproveBtnDisplayed, "SMAB-T205: Approve button is not visible");
		}

		//Step8: Clicking on the last table given in the tables list to approve it using Approve All button
		tableName = allTables.get(allTables.size()-1);
		isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
		objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
		
		//Step9: Clicking Approve All button to approve the calculations of the last table in tables list
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Approve All' button");
		objBppTrnPg.clickApproveAllBtn();

		//Step10: Retrieve & Assert pop up message displayed at page level
		objBppTrnPg.waitForPopUpMsgOnApproveAllClick(60);
		
		//Step11: Validating absence of Approve button once the submitted calculation has been approved by clicking approve button for given table
		boolean isApproveBtnDisplayed = objBppTrnPg.isApproveBtnVisible(5, tableName);
		softAssert.assertTrue(!isApproveBtnDisplayed, "SMAB-T205: Approve button is not be visible");
		
		//Step12: Validating presence of Download, Export Composite Factors & Export Valuation Factors buttons		
		boolean isDownloadBtnDisplayed = objBppTrnPg.isDownloadBtnVisible(20);
		softAssert.assertTrue(isDownloadBtnDisplayed, "SMAB-T304: 'Download' button is visible");
		boolean isExportCompositeBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(20);
		softAssert.assertTrue(isExportCompositeBtnDisplayed, "SMAB-T304: 'Export Composite Factors' button is visible");		
		boolean isExportValuationBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(20);
		softAssert.assertTrue(isExportValuationBtnDisplayed, "SMAB-T304: 'Export Valuation Factors' button is visible");

		//Step13: Validating absence of Approve All button once the submitted calculation has been approved by clicking approve button for given table
		isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(5);
		softAssert.assertTrue(!isApproveAllBtnDisplayed, "SMAB-T304: Approve all button is not be visible at page level");

		//Step14: Check status of the composite & valuation tables on bpp trend status page after approving
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		for(int i = 0; i < allTablesBppTrendSetupPage.size(); i++) {
			tableName = allTablesBppTrendSetupPage.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Approved", "SMAB-T157: Status of "+ tableName +" table on Bpp Trend Page before approving");
		}

		//Step15: Navigating to Bpp Trend page and selecting role year from drop down and clicking select button
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);

		//Step16: Checking whether edit button in grid's cell data text box is visible after table status is approved
		WebElement cellTxtBox = objBppTrnPg.locateCellTxtBoxElementInGrid(allTables.get(0));
		objBppTrnPg.Click(cellTxtBox);
		WebElement editBtn = objBppTrnPg.locateEditButtonInFocusedCellTxtBox();
		softAssert.assertTrue((editBtn == null), "SMAB-T157: Edit button is not visible to update cell data in grid after table status is 'Approved'");
				
		softAssert.assertAll();	
	}
	
	@Test(description = "SMAB-T249: Navigating to all tables post approval with business admin user", groups = {"smoke","regression","BPP Trends"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verifyBppTrendViewAllApprovedValueTables(String loginUser) throws Exception {		
		//Step1: Login and opening the BPP Trend module
		objApasGenericFunctions.login(loginUser);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step2: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
		
		//Step3: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");

		//Step4: Iterating over the given tables
		for (int i = 0; i < allTables.size(); i++) {
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			// Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);

			// Retrieve & Assert message displayed above approved table
			String actTableMsgBeforeApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeApprovingCalc = CONFIG.getProperty("tableMsgAfterApproval");
			softAssert.assertEquals(actTableMsgBeforeApprovingCalc, expTableMsgBeforeApprovingCalc, "SMAB-T249: Navigating to approved table '" + tableName + "' with business admin user login & validating message above it");
		}
		softAssert.assertAll();
	}
}