package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_PrincipalApprovalAndOutputTables_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	SoftAssert objSoftAssert;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		
		if(driver==null) {
            setupTest();
            driver = BrowserDriver.getBrowserInstance();
        }
		
		driver = BrowserDriver.getBrowserInstance();
		objSoftAssert = new SoftAssert();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}
	
	/**
	 * DESCRIPTION: Performing following for table: <SUBMITTED FOR APPROVAL TABLES>
	 * 1. Checking status of tables on BPP Trend Setup page:: Test Case/JIRA ID: SMAB-T157
	 * 2. Validating presence of Approve button for each table:: Test Case/JIRA ID: SMAB-T156,SMAB-T157
	 * 3. Editing and Saving new value in commercial composite table before approving it:: Test Case/JIRA ID: SMAB-T205
	 * 4. Approving the tables that are submitted for approval:: Test Case/JIRA ID: SMAB-T205
	 * 5. Validating the absence of Approval button post approving table:: Test Case/JIRA ID:SMAB-T156
	 * 6. Validating table data is not editable once approved:: Test Case/JIRA ID: SMAB-T157
	 * 7. Validating table status on BPP Trend Setup page post approving:: Test Case/JIRA ID:SMAB-T155
	 */
	@Test(description = "SMAB-T205,SMAB-T155,SMAB-T156,SMAB-T157,SMAB-T250: Approve calculations of valuation, composite & prop 13 tables", groups = {"smoke","regression","BppTrend"}, dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verify_BppTrend_Approve(String loginUser) throws Exception {	
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Submitted for Approval", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Submitted for Approval", rollYear);
		
		//Step1: Fetch table names from properties file and collect them in a single list
		List<String> allTablesBppTrendSetupPage = new ArrayList<String>();
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));
		allTablesBppTrendSetupPage.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
		String rollYear = CONFIG.getProperty("rollYear");
		
		//Step2: Check status of the composite & valuation tables on BPP trend status page before approving
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
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
		
		//Step5: Validating presence of Approve all button at page level.
		boolean isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
		objSoftAssert.assertTrue(isApproveAllBtnDisplayed, "Approve all button is visible at page level");
		
		//Step6: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<String>();
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesOutsideMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("compositeTablesUnderMoreTab").split(",")));
		allTables.addAll(Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(",")));
		
		String tableNamesUnderMoreTab = CONFIG.getProperty("compositeTablesUnderMoreTab") + "," + CONFIG.getProperty("valuationTablesUnderMoreTab");
		boolean isTableUnderMoreTab;
		//Step7: Iterating over the given tables and approving the calculation using Approve button
		for (int i = 0; i < allTables.size(); i++) {
			tableName = allTables.get(i);
			ExtentTestManager.getTest().log(LogStatus.INFO, "**** Performing Validations For: '"+ tableName +"' Table ****");
			//Step8: Clicking on the given table name
			isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
			
			//Step9: Retrieve & Assert message displayed above table before clicking Approve button
			String actTableMsgBeforeApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgBeforeApprovingCalc = CONFIG.getProperty("tableMsgBeforeApproval");
			softAssert.assertEquals(actTableMsgBeforeApprovingCalc, expTableMsgBeforeApprovingCalc, "SMAB-T205: Validating the message at table level before approving the table '" + tableName + "'");
			
			//Step10: Validating availability of Approve button at table level before performing calculation
			boolean isApproveBtnDisplayed = objBppTrnPg.isApproveBtnVisible(10, tableName);
			softAssert.assertTrue(isApproveBtnDisplayed, "SMAB-T156: Approve button is visible for Calculated table '"+ tableName +"'");
			softAssert.assertTrue(isApproveBtnDisplayed, "SMAB-T157: Approve button is visible for Calculated table '"+ tableName +"'");
			
			//Step11: Editing and saving cell data in the table for first & last factor tables specified the list
			WebElement editedCell = objBppTrnPg.locateCellToBeEdited(tableName);
			int cellDataBeforeEdit;
			if(tableName.equalsIgnoreCase("BPP Prop 13 Factors")) {
				double cellData = Double.valueOf(objBppTrnPg.getElementText(editedCell).split("\\n")[0]);
				cellDataBeforeEdit = (int) cellData;
			} else {
				cellDataBeforeEdit = Integer.valueOf(objBppTrnPg.getElementText(editedCell).split("\\n")[0]);
			}
			
			WebElement editBtn = objBppTrnPg.locateEditButtonInFocusedCell();
			objBppTrnPg.Click(editedCell);
			objBppTrnPg.Click(editBtn);
			
			objBppTrnPg.editCellDataInGridForGivenTable(tableName, (cellDataBeforeEdit + 1));
			boolean isEditedCellHighlighted = objBppTrnPg.isEditedCellHighlighted(editedCell);
			softAssert.assertTrue(isEditedCellHighlighted, "SMAB-T449: Edited cell is highlighted in yellow color for "+ tableName +" table");
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "** Editing table data and clicking Approve button wihtout saving the data **");
			if(i > 0) {
				//Step12: Approving the table data without saving edited cell
				objBppTrnPg.clickApproveButton(tableName);
				
				//Step13: Validating the warning message on clicking Approve button
				String actWarningMsgInPopUp = objBppTrnPg.retrieveApproveTabDataPopUpMessage();
				String expWarningMsgInPopUp = CONFIG.getProperty("approveTabDataMsg");
				softAssert.assertContains(actWarningMsgInPopUp, expWarningMsgInPopUp, "SMAB-T449: Warning / Pop up message dislayed when 'Approve' button is clicked without saving the data for "+ tableName +" table");
				
				//Step14: Clicking Confirm button in the pop up
				objBppTrnPg.javascriptClick(objBppTrnPg.confirmBtnInApproveTabPopUp);
				
				//Step15: Waiting for pop up message to display and the message displayed above table to update
				objBppTrnPg.waitForPopUpMsgOnApproveClick(60);
				
				objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
				editedCell = objBppTrnPg.locateCellToBeEdited(tableName);

				int cellDataAfterEdit;
				if(tableName.equalsIgnoreCase("BPP Prop 13 Factors")) {
					double cellData = Double.valueOf(objBppTrnPg.getElementText(editedCell).split("\\n")[0]);
					cellDataAfterEdit = (int) cellData;
				} else {
					cellDataAfterEdit = Integer.valueOf(objBppTrnPg.getElementText(editedCell).split("\\n")[0]);
				}

				softAssert.assertEquals(cellDataBeforeEdit, cellDataAfterEdit, "SMAB-T205: Validating table data on clicking approve button without saving the edited data for "+ tableName +" table");
				softAssert.assertEquals(cellDataBeforeEdit, cellDataAfterEdit, "SMAB-T449: Validating table data on clicking approve button without saving the edited data for "+ tableName +" table");
			} else {
				ExtentTestManager.getTest().log(LogStatus.INFO, "** Editing table data and clicking Approve button after saving the data **");
				//Step16: Clicking approve button to approve tab data
				objBppTrnPg.clickApproveButton(tableName);
				
				//Step17: Cancel the pop up message and save the cell data
				objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.cancelBtnInApproveTabPopUp, 10);
				objBppTrnPg.Click(objBppTrnPg.cancelBtnInApproveTabPopUp);
				objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.saveBtnToSaveEditedCellData, 10);
				objBppTrnPg.Click(objBppTrnPg.saveBtnToSaveEditedCellData);

				//Step18: Clicking approve button to approve tab data
				//objBppTrnPg.waitForPageSpinnerToDisappear();
				objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
				objBppTrnPg.clickApproveButton(tableName);
				
				//Step19: Waiting for pop up message to display and the message displayed above table to update
				objBppTrnPg.waitForPopUpMsgOnApproveClick(60);
				
				//Step20: Validating whether updated value has been saved in the edited cell post clicking save button
				objBppTrnPg.clickOnTableOnBppTrendPage(tableName, false);
				editedCell = objBppTrnPg.locateCellToBeEdited(tableName);
				int cellDataAfterEdit = Integer.valueOf(objBppTrnPg.getElementText(editedCell).split("\\n")[0]);
				softAssert.assertTrue(cellDataBeforeEdit != cellDataAfterEdit, "SMAB-T205: Data has been updated in  approved "+ tableName +" table. Value before updating: "+ cellDataBeforeEdit + " || Value after updating: "+ cellDataAfterEdit);
			}
			
			//Step22: Retrieve & Assert message displayed above table after clicking Approve button
			String actTableMsgAfterApprovingCalc = objBppTrnPg.retrieveMsgDisplayedAboveTable(tableName);
			String expTableMsgAfterApprovingCalc = CONFIG.getProperty("tableMsgAfterApproval");
			softAssert.assertEquals(actTableMsgAfterApprovingCalc, expTableMsgAfterApprovingCalc, "SMAB-T205: Validating the message at table level before approving the table '" + tableName + "'");
			softAssert.assertEquals(actTableMsgAfterApprovingCalc, expTableMsgAfterApprovingCalc, "SMAB-T250: Validating the message at table level before approving the table '" + tableName + "'");
			
			if(i < allTables.size()-1) {
				//Step22: Validating presence of Approve All button at page level on clicking Approve button
				isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(20);
				objSoftAssert.assertTrue(isApproveAllBtnDisplayed, "Approve All button is visible at page level");
			} else {
				//Step22: Validating absence of Approve All button once the submitted calculation has been approved by clicking approve button for given table
				isApproveAllBtnDisplayed = objBppTrnPg.isApproveAllBtnVisible(5);
				objSoftAssert.assertTrue(!isApproveAllBtnDisplayed, "Approve all button is not be visible at page level");				
			}
			
			//Step23: Validating absence of Approve button once the submitted calculation has been approved by clicking approve button for given table
			isApproveBtnDisplayed = objBppTrnPg.isApproveBtnVisible(5, tableName);
			softAssert.assertTrue(!isApproveBtnDisplayed, "SMAB-T205: Approve button is not visible for table '"+ tableName +"'");
			softAssert.assertTrue(!isApproveBtnDisplayed, "SMAB-T157: Approve button is not visible for table '"+ tableName +"'");
			softAssert.assertTrue(!isApproveBtnDisplayed, "SMAB-T156: Approve button is not visible for table '"+ tableName +"'");
		}

		//Step24: Navigating to BPP Trend Setup page and checking status of the composite & valuation tables
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		for(int i = 0; i < allTablesBppTrendSetupPage.size(); i++) {
			tableName = allTablesBppTrendSetupPage.get(i);
			String currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Approved", "SMAB-T157: Status of "+ tableName +" table on Bpp Trend Page after approving");
			softAssert.assertEquals(currentStatus, "Approved", "SMAB-T155: Status of "+ tableName +" table on Bpp Trend Page after approving");
		}

		//Step25: Navigating to BPP Trend page, selecting role year from drop down and clicking select button
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		//Step26: Checking whether edit button in table's cell data is visible after table data is approved
		for(int i = 0; i < allTables.size(); i++) {
			tableName = allTables.get(i);
			isTableUnderMoreTab = tableNamesUnderMoreTab.contains(tableName);
			objBppTrnPg.clickOnTableOnBppTrendPage(tableName, isTableUnderMoreTab);
			
			boolean isTableVisible = objBppTrnPg.isTableDataVisible(tableName);
			softAssert.assertTrue(isTableVisible, "SMAB-T155: Pinciapl user is able to see calculated table "+ tableName +" post approval");
			softAssert.assertTrue(isTableVisible, "SMAB-T157: Pinciapl user is able to see calculated table "+ tableName +" post approval");
			
			WebElement cellTxtBox = objBppTrnPg.locateCellToBeEdited(allTables.get(i));
			objBppTrnPg.Click(cellTxtBox);
			WebElement editBtn = objBppTrnPg.locateEditButtonInFocusedCell();
			softAssert.assertTrue((editBtn == null), "SMAB-T157: Edit button is not visible to update cell data in grid after table status is 'Approved'");
		}
				
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
}