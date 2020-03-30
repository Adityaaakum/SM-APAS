package com.apas.Tests.BppTrend;

import java.util.ArrayList;
import java.util.Arrays;
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
		Thread.sleep(3000);
	}

	@Test(description = "SMAB-T205: Approve calculations of factor table individually", dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verifyBppTrendApproveWorkFlow(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " +loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
		
		//Step4: Validating presence of Approve all button at page level.
		boolean isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
		softAssert.assertTrue(isApproveAllBtnDisplayed, "SMAB-T205: Approve all button should be visible");
		
		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("tableNamesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("tableNamesUnderMoreTab");
		
		//Step6: Iterating over the given tables
		for (int i = 0; i < allTables.size()-1; i++) {
			String tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			// Clicking on the given table name
			boolean isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);

			// Retrieve & Assert message displayed above table before clicking Approve button
			String actTableMsgBeforeApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeApprovingCalc = CONFIG.getProperty("tableMsgBeforeApproval");
			softAssert.assertTrue(actTableMsgBeforeApprovingCalc.equalsIgnoreCase(expTableMsgBeforeApprovingCalc), "SMAB-T205: Validating message at table level before approving the submitted calculation for table '" + tableName + "'");
			
			// Editing and saving cell data in the table for first factor table specified the list
			if(i == 0) {
				int cellDataBeforeEdit = Integer.valueOf(objBppTrnPg.getCellDataFromGridForGivenTable().split("\\n")[0]);
				objBppTrnPg.editCellDataInGridForGivenTable(cellDataBeforeEdit + 1);
				objBppTrnPg.Click(objBppTrnPg.saveEditedCellData);
				
				objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
				objBppTrnPg.clickOnGivenRollYear(rollYear);
				objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
				
				objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
				int cellDataAfterEdit = Integer.valueOf(objBppTrnPg.getCellDataFromGridForGivenTable().split("\\n")[0]);
				softAssert.assertTrue(cellDataBeforeEdit != cellDataAfterEdit, "SMAB-T205: Is cell data changed in the table after editing the specified cell");
			}
			
			// Clicking on Approve button at table level to complete the pending approval
			ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Approve' button");
			objBppTrnPg.clickApproveButton(tableName);
			
			//Waiting for pop up message to display and the message displayed above table to update
			objBppTrnPg.waitForPopUpMsgOnApproveClick(180);
						
			// Retrieve & Assert message displayed above table after clicking Approve button
			String actTableMsgAfterApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgAfterApprovingCalc = CONFIG.getProperty("tableMsgAfterApproval");
			softAssert.assertEquals(actTableMsgAfterApprovingCalc, expTableMsgAfterApprovingCalc, "SMAB-T205: Is factor table calculation approved successfully");
			
			// Validating presence of Approve All button at page level on clicking Approve button
			isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
			softAssert.assertTrue(isApproveAllBtnDisplayed, "SMAB-T205: Approve All button should be visible on approving the submitted calculation");
			
			// Validating absence of Approve button once the submitted calculation has been approved by clicking approve button for given table
			boolean isApproveBtnDisplayed = objBppTrnPg.isApproveBtnVisible(5, tableName);
			softAssert.assertTrue(isApproveBtnDisplayed, "SMAB-T205: Approve button should not be visible");
		}		
		softAssert.assertAll();	
	}
	
	@Test(description = "SMAB-T304: Approve calculations of all factor tables in one go", dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verifyBppTrendApproveAllWorkFlow(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
		
		//Step4: Validating presence of Approve all button at page level
		boolean isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
		softAssert.assertTrue(isApproveAllBtnDisplayed, "SMAB-T304: Approve all button should be visible");
					
		//Step5: Clicking on Approve All button at table level to complete the pending approval
		ExtentTestManager.getTest().log(LogStatus.INFO, "* Clicking 'Approve All' button");
		objBppTrnPg.clickApproveAllBtn();

		//Step6: Retrieve & Assert pop up message displayed at page level
		String actPopUpMsg = objBppTrnPg.waitForPopUpMsgOnSubmitAllForApprovalClick(180);
		String expPopUpMsg = CONFIG.getProperty("pageLevelMsgAfterApproveAll");
		softAssert.assertEquals(actPopUpMsg, expPopUpMsg, "SMAB-T304: Calculations successfully approved for all tables");
			
		//Step7: Validating absence of Approve All button once the submitted calculation has been approved by clicking approve button for given table
		isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(5);
		softAssert.assertTrue(isApproveAllBtnDisplayed, "SMAB-T304: Approve all button should not be visible");
		
		softAssert.assertAll();
	}
}