package com.apas.Tests.SecurityAndSharing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.config.BPPTablesData;
import com.apas.config.users;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.server.handler.DeleteSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.config.modules;

public class BppTrend_SecurityAndSharing_Test extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	BppTrendPage objBppTrendPage;
	SoftAssertion softAssert;
	String rollYear = "2022";
	SalesforceAPI objSalesforceAPI;
	SoftAssert objSoftAssert;
	BuildingPermitPage objBuildPermitPage;
	BppTrendSetupPage objApasGenericFunctions;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBppTrendPage = new BppTrendPage(driver);
		softAssert = new SoftAssertion();
		objSalesforceAPI = new SalesforceAPI();
		objSoftAssert = new SoftAssert();
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new BppTrendSetupPage(driver);
		objApasGenericFunctions.updateRollYearStatus("Open", "2022");
	}

	/**
	 * DESCRIPTION: Checking Security Level For Restricted Users : Validating the unavailability of Calculate and Calculate All buttons:
	 */
	@Test(description = "SMAB-T174,SMAB-T175: Validate that Calculate & Calculate All buttons should not be visible to restricted users", groups = {"BPPTrend","Regression"}, dataProvider = "usersRestrictedToCalculate", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BppTrend_RestrictionsOnCalculateButtons_RestrictedUsers(String loginUser) throws Exception {

		//Step1: Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);

		//Step2: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the BPP Trend module
		objBppTrendPage.selectRollYearOnBPPTrends(rollYear);

		//Step4: Validating absence of CalculateAll button at page level
		softAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.calculateAllBtn), "SMAB-T174,SMAB-T175: For User '"+ loginUser +"': Calculate All button is not visible");

		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<>(Arrays.asList(BPPTablesData.BPP_TREND_TABLES.split(",")));

		//Step6: Iterating over the given tables
		for (String tableName : allTables){
			Thread.sleep(1000);
			objBppTrendPage.clickOnTableOnBppTrendPage(tableName);
			softAssert.assertTrue(!objBppTrendPage.isCalculateButtonVisible(tableName), "SMAB-T174,SMAB-T175: For User '" + loginUser + "': Calcuate button is not visible");
		}

		//Logging Out of the application
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Checking Security Level For Restricted User : Validating the unavailability of ReCalculate and ReCalculate All buttons
	 */
	@Test(description = "SMAB-T174,SMAB-T175: Validate that ReCalculate & 'ReCalculate All' and 'Submit All Factor For Approval' buttons should not be visible to restricted users", groups = {"BPPTrend","Regression"}, dataProvider = "usersRestrictedToReCalculate", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BppTrend_RestrictionsOnReCalculateButtons_RestrictedUsers(String loginUser) throws Exception {

		//Step1: Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);

		//Step2: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the BPP Trend module
		objBppTrendPage.selectRollYearOnBPPTrends(rollYear);

		//Step4: Validating absence of ReCalculateAll button at page level
		softAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.reCalculateAllBtn), "SMAB-T174,SMAB-T175: For User '"+ loginUser +"': ReCalcuate All button is not visible");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.submitAllFactorForApprovalButton), "SMAB-T174,SMAB-T175: For User '"+ loginUser +"':  Submit All Factors For Approval button is not visible");

		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<>(Arrays.asList(BPPTablesData.BPP_TREND_TABLES.split(",")));

		//Step6: Iterating over the given tables
		for (String tableName : allTables){
			Thread.sleep(1000);
			objBppTrendPage.clickOnTableOnBppTrendPage(tableName);
			softAssert.assertTrue(!objBppTrendPage.isReCalculateButtonVisible(tableName), "SMAB-T174,SMAB-T175: For User '"+ loginUser +"': ReCalculate button is not visible");
		}

		//Logging Out of the application
		objApasGenericFunctions.logout();
	}

	/**
	 * Description: Validating the unavailability of Approve & Approve All buttons for restricted users
	 */
	@Test(description = "SMAB-T174,SMAB-T175,SMAB-T249,SMAB-T199,SMAB-T250,SMAB-T301: Perform security verification on Approve & Approve All buttons for restricted users", groups = {"BPPTrend","Regression","Smoke"}, dataProvider = "usersRestrictedToApprove", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BppTrend_RestrictionsOnApproveButtons_RestrictedUsers(String loginUser) throws Exception {

		//Step1: Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Submitted for Approval", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Submitted for Approval", rollYear);

		//Step2: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the BPP Trend module
		objBppTrendPage.selectRollYearOnBPPTrends(rollYear);

		//Step4: Validating absence of ApproveAll button at page level
		boolean	isApproveAllBtnDisplayed = objBppTrendPage.isApproveAllBtnVisible(10);
		objSoftAssert.assertTrue(!isApproveAllBtnDisplayed, "For User '"+ loginUser +"': Approve All button is not visible");

		//Step4: Validating absence of ReCalculateAll button at page level
		softAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.approveAllButton), "SMAB-T174,SMAB-T175: For User '"+ loginUser +"': Approve All button is not visible");

		//Step5: Fetch table names from properties file and collect them in a single list
		List<String> allTables = new ArrayList<>(Arrays.asList(BPPTablesData.BPP_TREND_TABLES.split(",")));

		//Step6: Iterating over the given tables
		for (String tableName : allTables){
			Thread.sleep(1000);
			objBppTrendPage.clickOnTableOnBppTrendPage(tableName);

			//Validation that Approve button should not be visible
			softAssert.assertTrue(!objBppTrendPage.isApproveButtonVisible(tableName), "SMAB-T174,SMAB-T175: For User '"+ loginUser +"': Approve button is not visible");

			//Validation to check whether table data appears for given table
			softAssert.assertTrue(objBppTrendPage.isTableDataVisible(tableName, 10), "SMAB-T249: Data grid for approved table '"+ tableName +"' is visible");

			//Retrieve & Assert message displayed above approved table
			softAssert.assertEquals(objBppTrendPage.retrieveMsgDisplayedAboveTable(tableName), "Already submitted for approval", "SMAB-T249: Message above approve table '" + tableName + "'");

			//Step8: Validating unavailability of ReCalculate button at table level after table has been Approved
			softAssert.assertTrue(!objBppTrendPage.isReCalculateButtonVisible(tableName), "SMAB-T199: ReCalcuate button is not visible for Approved table '"+ tableName +"'");

			//Step9: Checking whether edit button in grid's cell data text box is visible after table status is approved
			WebElement cellTxtBox = objBppTrendPage.locateCellToBeEdited(tableName, 1, 1);
			objBppTrendPage.Click(cellTxtBox);
			softAssert.assertTrue((objBppTrendPage.locateEditButtonInFocusedCell() == null), "SMAB-T250,SMAB-T301: Edit pencil icon is not visible to update table data in grid for "+ tableName +"");
		}

		//Logging Out of the application
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Validating the unavailability of Export Composite Factors button for restricted users
	 */
	@Test(description = "SMAB-T450: Perform security verification on Export Composite and validation Factors button for restricted users", groups = {"BPPTrend","Regression"}, dataProvider = "usersRestrictedToExportCompFactorsFile", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BppTrend_RestrictionsOnExportFactorsButton_RestrictedUsers(String loginUser) throws Exception {

		//Step1: Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Approved", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Approved", rollYear);

		//Step2: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the BPP Trend module
		objBppTrendPage.selectRollYearOnBPPTrends(rollYear);

		//Step4: Validating absence of ApproveAll and Export Report Buttons at page level
		objSoftAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.approveAllButton), "For User '"+ loginUser +"': Approve All button is not visible");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.exportCompositeFactorsBtn), "SMAB-T450: For User '"+ loginUser +"': Export Composite Factors button is not visible");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.exportValuationFactorsBtn), "SMAB-T450: For User '"+ loginUser +"': Export Composite Factors button is not visible");

		//Logging Out of the application
		objApasGenericFunctions.logout();

	}

	/**
	 * DESCRIPTION: Validating the unavailability of edit pencil icon to edit table status on BPP Trend Setup
	 */
	@Test(description = "SMAB-T171: Validating the unavailability of edit pencil icon to edit table status on BPP Trend Setup", groups = {"BPPTrend","Regression"}, dataProvider = "usersRestrictedEditTableStatusOnBppTrendPage", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BppTrend_RestrictionsEditingTableStatus_RestrictedUsers(String loginUser) throws Exception {

		//Step1: Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);

		//Step2: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");

		objApasGenericFunctions.clickOnEntryNameInGrid(rollYear);

		//Step3: Fetch table names from properties file and collect them in a single list
		List<String> tableStatusFields = new ArrayList<>(Arrays.asList(BPPTablesData.BPP_TREND_SETUP_FACTOR_STATUS_FIELDS.split(",")));

		for(String tableStatusFieldName : tableStatusFields) {
			softAssert.assertTrue(!objBppTrendPage.isPencilIconToEditTableStatusVisible(tableStatusFieldName), "SMAB-T171: For User '"+ loginUser +"': Edit pencil icon on BPP Trend Setup page is NOT VISIBLE for table '"+ tableStatusFieldName +"'");
		}

		//Step4: Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);

		//Step5: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.clickOnEntryNameInGrid(rollYear);

		//Step6: Validating absence of Edit pencil icon to edit table status on BPP Trend Setup page
		for(String tableStatusFieldName : tableStatusFields) {
			softAssert.assertTrue(!objBppTrendPage.isPencilIconToEditTableStatusVisible(tableStatusFieldName), "SMAB-T171: For User '"+ loginUser +"': Edit pencil icon on BPP Trend Setup page is NOT VISIBLE for table '"+ tableStatusFieldName +"'");
		}


		//Step7: Log out from the application
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Validating that (Appraiser/Auditor) are not able to create and edit inflation factors records:: TestCase/JIRA ID: SMAB-T210
	 * Edit Restriction for business admin scenario is covered in BPPTrend_InflationFactor_Test class
	 */
	@Test(description = "SMAB-T210: Appraiser and Auditor users unable to create/edit CPI factor", groups = {"Regression","BPPTrend"}, dataProvider = "rpApprasierAndBPPAuditor", dataProviderClass = DataProviders.class)
	public void BppTrend_CreateEdit_InflationFactor_RestrictedUsers(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.CPI_FACTORS);
		objApasGenericFunctions.displayRecords("All");

		if (loginUser.equals(users.BPP_AUDITOR)){
			HashMap<String, ArrayList<String>> cpiFactorTableData  = objApasGenericFunctions.getGridDataInHashMap();
			softAssert.assertEquals(cpiFactorTableData.size(),0,"SMAB-T210 : BPP Auditor should not see any CPI Factor data hence record count should be zero");
		}else{
			//Step3: Checking unavailability of new button on grid page
			softAssert.assertTrue(!objPage.verifyElementVisible(objBppTrendPage.newBtnViewAllPage), "SMAB-T210: For User "+ loginUser +"-- New button is not visible on grid page");

			//Step4: Finding the first entry from the grid to find edit button for it
			String cpiFactorToEdit = objPage.getElementText(objBppTrendPage.firstEntryInGrid);
			objApasGenericFunctions.clickShowMoreLink(cpiFactorToEdit);

			//Step5: Checking unavailability of edit link under show more drop down on view all grid
			softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.editLinkUnderShowMore), "SMAB-T210: For User "+ loginUser +"-- Edit link is not visible under show more option on grid");

			//Step6: Checking unavailability of edit button on details page
			objApasGenericFunctions.clickOnEntryNameInGrid(cpiFactorToEdit);
			softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.editButton), "SMAB-T210: For User "+ loginUser +"-- Edit button is not visible on details page");
			softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.deleteButton), "SMAB-T210: For User "+ loginUser +"-- Delete button is not visible on details page");
		}

		//Logging out of the application
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Validating the unavailability of create/edit/delete links under BPP Settings
	 */
	@Test(description = "SMAB-T270: Validating that restricted user is unable to create/edit BPP Settings", groups = {"Regression","BPPTrend"}, dataProvider = "usersRestrictedToModifyMaxEquipIndexFactor", dataProviderClass = DataProviders.class,alwaysRun = true)
	public void BppTrend_RestrictionsEditingBPPSettingsAfterApprove_RestrictedUsers(String loginUser) throws Exception {

		//Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Approved", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Approved", rollYear);

		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend Setup module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");

		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
        objApasGenericFunctions.clickOnEntryNameInGrid(rollYear);

		//Step4: Validating that user doesn't have any option to create/edit BPP Settings
		objBppTrendPage.clickAction(objBppTrendPage.waitForElementToBeClickable(20,objApasGenericFunctions.dropDownIconBppSetting));
		softAssert.assertTrue(objBppTrendPage.waitForElementToBeVisible(5, objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Actions Available' option is visible on details page");

		//Step5: Validating that user doesn't have the access to create/Edit Composite Factor Settings
		objPage.Click(objApasGenericFunctions.bppCompFactorSettingTab);
		objBppTrendPage.clickAction(objBppTrendPage.waitForElementToBeClickable(20,objApasGenericFunctions.dropDownIconBppCompFactorSetting));
		softAssert.assertTrue(objBppTrendPage.waitForElementToBeVisible(5, objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Actions Available' option is visible on details page");

		//Step6: Validating that user doesn't have the access to create/edit BPP Property Index Good Factors
		objApasGenericFunctions.openFactorTab("BPP Property Index Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for BPP Property Index Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("BPP Property Index Factors");
		softAssert.assertTrue(objPage.verifyElementVisible(objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Action Available' option is displayed for BPP Property Index Factors");

		//Step7: Validating that user doesn't have the access to create/edit BPP Percent Good Factors
		objApasGenericFunctions.openFactorTab("BPP Percent Good Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for BPP Percent Good Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("BPP Percent Good Factors");
		softAssert.assertTrue(objPage.verifyElementVisible(objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Action Available' option is displayed for BPP Percent Good Factors");

		//Step8: Validating that user doesn't have the access to create/edit Imported Valuation Factors
		String expectedErrorMessage = "You do not have the level of access necessary to perform the operation you requested. Please contact the owner of the record or your administrator if access is necessary.";
		objApasGenericFunctions.openFactorTab("Imported Valuation Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for Imported Valuation Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("Imported Valuation Factors");
		objPage.javascriptClick(objBppTrendPage.editLinkUnderShowMore);
		
		objPage.Click(objPage.getButtonWithText("Save"));
		 expectedErrorMessage = "Oops...you don't have the necessary privileges to edit this record. See your administrator for help.";
		String actualErrorMessage = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertTrue(actualErrorMessage.contains(expectedErrorMessage) || actualErrorMessage.contains("insufficient access rights on object id"), "SMAB-T270: User is not able to Edit Imported Valuation record");
		objPage.Click(objApasGenericFunctions.closeEntryPopUp);

		//Step9: Validating that user doesn't have the access to create/edit Composite Factors
		objApasGenericFunctions.openFactorTab("Composite Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for Composite Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("Composite Factors");
		objPage.javascriptClick(objBppTrendPage.editLinkUnderShowMore);
		
		objPage.Click(objPage.getButtonWithText("Save"));
		 expectedErrorMessage = "Oops...you don't have the necessary privileges to edit this record. See your administrator for help.";
		 actualErrorMessage = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertTrue(actualErrorMessage.contains(expectedErrorMessage) || actualErrorMessage.contains("insufficient access rights on object id"), "SMAB-T270: User is not able to composite factors");
		objPage.Click(objApasGenericFunctions.closeEntryPopUp);
		
		//Logging out of the application
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Validating the unavailability of create/edit/delete links under BPP Settings
	 */
	@Test(description = "SMAB-T270: Validating that restricted user is unable to create/edit BPP Settings", groups = {"Regression","BPPTrend"}, dataProvider = "usersRestrictedToModifyMaxEquipIndexFactor", dataProviderClass = DataProviders.class,alwaysRun = true)
	public void BppTrend_RestrictionsEditingBPPSettingsBeforeApprove_RestrictedUsers(String loginUser) throws Exception {

		//Resetting the Composite and Valuation factor tables status to Not Calculated
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);

		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend Setup module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");

		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objApasGenericFunctions.clickOnEntryNameInGrid(rollYear);

		//Step4: Validating that user doesn't have any option to create/edit BPP Settings
		objBppTrendPage.clickAction(objBppTrendPage.waitForElementToBeClickable(20,objApasGenericFunctions.dropDownIconBppSetting));
		softAssert.assertTrue(objBppTrendPage.waitForElementToBeVisible(5, objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Actions Available' option is visible on details page");

		//Step5: Validating that user doesn't have the access to create/Edit Composite Factor Settings
		objPage.Click(objApasGenericFunctions.bppCompFactorSettingTab);
		objBppTrendPage.Click(objBppTrendPage.waitForElementToBeClickable(20,objApasGenericFunctions.dropDownIconBppCompFactorSetting));
		softAssert.assertTrue(objBppTrendPage.waitForElementToBeVisible(5, objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Actions Available' option is visible on details page");

		//Step6: Validating that user doesn't have the access to create/edit BPP Property Index Good Factors
		objApasGenericFunctions.openFactorTab("BPP Property Index Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for BPP Property Index Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("BPP Property Index Factors");
		softAssert.assertTrue(objPage.verifyElementVisible(objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Action Available' option is displayed for BPP Property Index Factors");

		//Step6: Validating that user doesn't have the access to create/edit BPP Percent Good Factors
		objApasGenericFunctions.openFactorTab("BPP Percent Good Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for BPP Percent Good Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("BPP Percent Good Factors");
		softAssert.assertTrue(objPage.verifyElementVisible(objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Action Available' option is displayed for BPP Percent Good Factors");

		//Step6: Validating that user doesn't have the access to create/edit Imported Valuation Factors
		objApasGenericFunctions.openFactorTab("Imported Valuation Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for Imported Valuation Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("Imported Valuation Factors");
		if (loginUser.equals(users.PRINCIPAL_USER))
			softAssert.assertTrue(objPage.verifyElementVisible(objApasGenericFunctions.editLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'Edit' option is displayed for Imported Valuation Factors");
		else
			softAssert.assertTrue(objPage.verifyElementVisible(objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Action Available' option is displayed for Imported Valuation Factors");

		//Step6: Validating that user doesn't have the access to create/edit Composite Factors
		objApasGenericFunctions.openFactorTab("Composite Factors");
		softAssert.assertTrue(!objPage.verifyElementVisible(objApasGenericFunctions.newButton), "SMAB-T270: For User '"+ loginUser +"': 'New Button' option is not visible for Composite Factors");
		objApasGenericFunctions.clickShowMoreDropDownForGivenFactorEntry("Composite Factors");
		Thread.sleep(4000);
		if (loginUser.equals(users.PRINCIPAL_USER))
			softAssert.assertTrue(objBppTrendPage.waitForElementToBeVisible(5, objBppTrendPage.editLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'Edit' option is visible for Composite factors");

		else
			softAssert.assertTrue(objPage.verifyElementVisible(objBppTrendPage.noActionsLinkUnderShowMore), "SMAB-T270: For User '"+ loginUser +"': 'No Action Available' option is displayed for Composite Factors");

		//Logging out of the application
		objApasGenericFunctions.logout();
	}

}
