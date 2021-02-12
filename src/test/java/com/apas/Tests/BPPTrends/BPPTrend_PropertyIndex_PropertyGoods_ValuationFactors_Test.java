package com.apas.Tests.BPPTrends;

import java.util.Arrays;
import java.util.List;

import com.apas.PageObjects.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.users;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_PropertyIndex_PropertyGoods_ValuationFactors_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	BppTrendPage objBppTrnPg;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	BuildingPermitPage objBuildPermitPage;
	ApasGenericPage objApasGenericPage;
	BppTrendSetupPage objBppTrendSetupPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
		objBppTrendSetupPage.updateRollYearStatus("Open", "2020");
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		//objBppTrendSetupPage.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating that user is able to edit an entry:: TestCase/JIRA ID: SMAB-T235
	 * 2. Validating the business rules for Year Acquired value:: TestCase/JIRA ID: SMAB-T238
	 * 3. Validating user is unable to create duplicate entry:: TestCase/JIRA ID: SMAB-T236
	 */
	@Test(description = "SMAB-T235,SMAB-T236,SMAB-T238: Edit new factors entry under BPP Property Index factor table having different status of tables before submitting calculation", groups = {"Regression","BPPTrend"}, dataProvider = "variousStatusOfCompositeTablesBeforeSubmitting", dataProviderClass = DataProviders.class)
	public void BppTrend_Edit_PropertyIndexFactorEntry_WithDifferentStatusOfTables(String tablesStatus) throws Exception {
		//Step1: Resetting the composite factor tables status
		List<String> tablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(tablesToReset, tablesStatus, rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with tables status as status: " + tablesStatus);
		objBppTrendSetupPage.login(users.BUSINESS_ADMIN);

		//Step3: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup entry in grid to naivgate to details page");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Validating the status of table on Details tab
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the status of table on details page");
		String propertyType = "Agricultural";
		String currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage("Ag. Trends Status");
		softAssert.assertEquals(currentStatus, tablesStatus, "SMAB-T235: Validation for status of Ag. Trends Status table");
		softAssert.assertEquals(currentStatus, tablesStatus, "SMAB-T238: Validation for status of Ag. Trends Status table");

		//Step5: Clicking on BPP property index factor tab and validating whether its table is visible
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Property Index Factors tab on details page");
		objBppTrnPg.Click(objBppTrendSetupPage.bppPropertyIndexFactorsTab);
		objBppTrnPg.waitForElementToBeVisible(20, objBppTrendSetupPage.bppPropertyIndexFactorsTableSection);
		boolean bppPropIndexFactorTablevisible = objBppTrnPg.verifyElementVisible(objBppTrendSetupPage.bppPropertyIndexFactorsTableSection);
		softAssert.assertTrue(bppPropIndexFactorTablevisible, "SMAB-T229: Verify BPP Property Index Factors tab is displayed on BPP Trend setup Page");
		//Step6: Retrieve existing values from tables for very first row
		String factorTableName = "BPP Property Index Factors";

		ExtentTestManager.getTest().log(LogStatus.INFO, "Retrieving the details of record to delete, create and edit from details page");
		String entryName = objBppTrendSetupPage.readNameValueFromGivenFactorTable(factorTableName, propertyType);
		String yearAcquired = objBppTrendSetupPage.readAcquiredYearValueFromGivenFactorTable(factorTableName, propertyType);
		String propertyValue = objBppTrendSetupPage.readPropertyTypeValueFromBppPropIndexFactors(propertyType);
		String existingIndexFactor = objBppTrendSetupPage.readIndexFactorValue(propertyType);

		//Step7: Click New button for given factors table to create a new entry under it
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking New Button");
		objBppTrendSetupPage.clickNewButtonUnderFactorSection(factorTableName);

		//Step8: Enter mandatory details in the new factor pop up
		ExtentTestManager.getTest().log(LogStatus.INFO, "Entering the mandatory details for creating new Property Index Factor record");
		objBppTrendSetupPage.enter("Name (Roll Year - Property Type)", entryName);
		objApasGenericPage.selectOptionFromDropDown("Year Acquired", yearAcquired);
		objApasGenericPage.selectOptionFromDropDown("Property Type", propertyValue);
		objBppTrendSetupPage.enter("Index Factor", existingIndexFactor);

		//Step9: Clicking save button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking save button");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step10: Validating the error message on duplicate entry creation
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the error message on duplicate entry creation");
		String expErrorMsgForDuplicateEntry = CONFIG.getProperty("errorMsgOnDuplicateFactorRecordCreation");
		String actualErrorMsgForDuplicateEntry = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgForDuplicateEntry, expErrorMsgForDuplicateEntry, "SMAB-T236: Validation for error mesage on creating duplicate entry");

		//Step11: Closing the new entry pop with with cancel button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking close button to cancel new entry pop up");
		objPage.Click(objPage.getButtonWithText("Cancel"));

		//Step19: Validating Edit button is visible under show more drop down
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking show more button for property type: "+entryName);
		Thread.sleep(2000);
		objBppTrendSetupPage.clickShowMoreLink(entryName);
		Thread.sleep(2000);
		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		boolean isEditBtnPresent = objBuildPermitPage.editLinkUnderShowMore.isDisplayed();
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T235: EDIT button is present under show more link");
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T238: EDIT button is present under show more link");

		//Step20: Editing and saving the newly created entry by updating name and index factor value as 0
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created entry");
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);

		objBppTrendSetupPage.enter("Index Factor", "0");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step21: Validating the error message displayed on entering index factor value as 0
		String expErrorMsgOnInvalidIndexValue = CONFIG.getProperty("errorMsgOnProvidingInvalidIndexFactor");
		String actualErrorMsgOnInvalidIndexValue = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgOnInvalidIndexValue, expErrorMsgOnInvalidIndexValue, "SMAB-T235,SMAB-T238: Validation for error mesage on entering invalid index factor value as 0");

		//Step22: Entering index factor value as -1
		objBppTrendSetupPage.enter("Index Factor", "-1");
		objPage.Click(objPage.getButtonWithText("Save"));

		Thread.sleep(4000);
		//Step23: Validating the error message displayed on entering index factor value as -1
		actualErrorMsgOnInvalidIndexValue = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgOnInvalidIndexValue, expErrorMsgOnInvalidIndexValue, "SMAB-T235,SMAB-T238: Validation for error mesage on entering invalid index factor value as -1");

		//Step24: Entering a valid value for index factor field
		objBppTrendSetupPage.enter("Index Factor", existingIndexFactor);

		//Step25: Entering an invalid value for year acquired and clicking the save button
		objApasGenericPage.selectOptionFromDropDown("Year Acquired", rollYear);
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Step26: Validating the error message displayed on entering year acquired value
		String expErrorMsgOnInvalidYearAcquired = CONFIG.getProperty("errorMsgOnProvidingInvalidAcquiredYear");
		String actualErrorMsgOnInvalidYearAcquired = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgOnInvalidYearAcquired, expErrorMsgOnInvalidYearAcquired, "SMAB-T235,SMAB-T238: Validation for error mesage on entering invalid year acquired value");

		//Step27: Entering a valid value for year acquired field and clicking save button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Saving the edited entry with valid data");
		objApasGenericPage.selectOptionFromDropDown("Year Acquired", yearAcquired);
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		if(tablesStatus.equalsIgnoreCase("Calculated")) {
			//Search the BPP Trend Setup module
			objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
			objBppTrendSetupPage.displayRecords("All");

			//Clicking on the roll year name in grid to navigate to details page of selected roll year
			ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup entry in grid to naivgate to details page");
			objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

			//Validating the status of table has changed to Needs Recalculation when factor tables entry is edited
			ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the status of table has changed to Needs Recalculation when factor tables entry is edited");
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage("Ag. Trends Status");
			softAssert.assertEquals(currentStatus, "Needs Recalculation", "SMAB-T235: Status of 'Ag. Trends Status' table on BPP Trend Page after editing factor table entry");
			softAssert.assertEquals(currentStatus, "Needs Recalculation", "SMAB-T238: Status of 'Ag. Trends Status' table on BPP Trend Page after editing factor table entry");
		}

		//Step28: Log out from the application
		softAssert.assertAll();

		//Step29: Assert all the assertions
		objBppTrendSetupPage.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is not able to edit the data once table calculation are submitted or approved:: TestCase/JIRA ID: SMAB-T238
	 */
	@Test(description = "SMAB-T238: Edit a new factors entry under BPP Property Index factor table", groups = {"Regression","BPPTrend"}, dataProvider = "variousStatusOfTablesPostSubmittingCalculations", dataProviderClass = DataProviders.class)
	public void BppTrend_Edit_BppPropertyIndex_PostSubmittingCalculations(String tableStatus) throws Exception {
		//Step1: Resetting the composite and valuation factor tables status
		List<String> tablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(tablesToReset, tableStatus, rollYear);

		List<String> valTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valTablesToReset, "Yet to submit for Approval", rollYear);

		List<String> Prop13TableToReset = Arrays.asList(CONFIG.getProperty("Prop13TableToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(Prop13TableToReset, "Calculated", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with tables status as '"+ tableStatus +"'");
		objBppTrendSetupPage.login(users.BUSINESS_ADMIN);

		//Step3: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup entry in grid to naivgate to details page");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Validating the status of table on Details tab
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the status of Commercial Trends Status table");
		String currentStatus = objBppTrendSetupPage.getTableStatus("Commercial Composite Factors",rollYear);
		softAssert.assertEquals(currentStatus, tableStatus, "SMAB-T238: Validation for status of Commercial Trends Status table");

		//Step5: Clicking on BPP property index factor tab and validating whether its table is visible
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Property Index Factors tab on details page");
		objBppTrnPg.Click(objBppTrendSetupPage.bppPropertyIndexFactorsTab);
		objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.bppPropertyIndexFactorsTableSection, 20);

		//Step6: Retrieve existing values from tables for very first row
		String factorTableName = "BPP Property Index Factors";
		String propertyType = "Commercial";

		ExtentTestManager.getTest().log(LogStatus.INFO, "Retrieving the details of record to delete, create and edit from details page");
		String entryName = objBppTrendSetupPage.readNameValueFromGivenFactorTable(factorTableName, propertyType);
		String yearAcquired = objBppTrendSetupPage.readAcquiredYearValueFromGivenFactorTable(factorTableName, propertyType);
		String propertyValue = objBppTrendSetupPage.readPropertyTypeValueFromBppPropIndexFactors(propertyType);
		String indexFactor = objBppTrendSetupPage.readIndexFactorValue(propertyType);

		//Step7: Click New button for given factors table to create a new entry under it
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking New Button");
		objBppTrendSetupPage.clickNewButtonUnderFactorSection(factorTableName);

		//Step8: Enter mandatory details in the new factor pop up
		ExtentTestManager.getTest().log(LogStatus.INFO, "Entering the mandatory details for creating new Property Index Factor record");
		objBppTrendSetupPage.enter("Name (Roll Year - Property Type)", entryName);
		objApasGenericPage.selectOptionFromDropDown("Year Acquired", yearAcquired);
		objApasGenericPage.selectOptionFromDropDown("Property Type", propertyValue);
		objBppTrendSetupPage.enter("Index Factor", indexFactor);

		//Step9: Clicking save button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking save button");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step10: Validating the error message on entry creation when table status is Approved
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the error message on entry creation when table status is Aproved");
		String expErrorMsgOnClickingSaveBtn = CONFIG.getProperty("errorMsgOnSavingRecordWhenTableDataIsSubmittedOrApproved");
		String actErrorMsgOnClickingSaveBtn =objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actErrorMsgOnClickingSaveBtn, expErrorMsgOnClickingSaveBtn, "SMAB-T238: Validation for error mesage on creating entry when status is "+ tableStatus);

		//Step11: Closing the new entry pop with with cancel button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking close button to cancel new entry pop up");
		objPage.Click(objPage.getButtonWithText("Cancel"));
		Thread.sleep(1000);

		//Step12: Clicking on the show more button and validating presence of Edit button
		objBppTrendSetupPage.clickShowMoreDropDownForGivenFactorEntry(factorTableName);
		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		boolean isEditBtnPresent = objBuildPermitPage.editLinkUnderShowMore.isDisplayed();
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T238: EDIT button is present under show more link");

		//Step13: Editing and saving the newly created entry by updating name and index factor value as 0
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created entry");
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);

		objBppTrendSetupPage.enter("Index Factor", "0");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step14: Validating the error message on entry creation when table status is Submitted For Approval / Approved
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the error message on entry creation when table status is Aproved");
		expErrorMsgOnClickingSaveBtn = CONFIG.getProperty("errorMsgOnSavingRecordWhenTableDataIsSubmittedOrApproved");
		actErrorMsgOnClickingSaveBtn = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actErrorMsgOnClickingSaveBtn, expErrorMsgOnClickingSaveBtn, "SMAB-T238: Validation for error mesage on creating entry when status is "+ tableStatus);

		objPage.Click(objPage.getButtonWithText("Cancel"));
		Thread.sleep(1000);

		//Step15: Log out from the application
		objBppTrendSetupPage.logout();

		//Step16: Assert all the assertions
		softAssert.assertAll();
	}

	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating that user is able to edit an entry:: TestCase/JIRA ID: SMAB-T284
	 */
	@Test(description = "SMAB-T284: Edit new factors entry under Valuation factor table having different status of tables before submitting calculation", groups = {"Regression","BPPTrend"}, dataProvider = "variousStatusOfValuationTablesBeforeSubmitting", dataProviderClass = DataProviders.class)
	public void BppTrend_Edit_ValuationFactorEntry_WithDifferentStatusOfTables(String tablesStatus) throws Exception {
		//Step1: Resetting the valuation factor tables status
		List<String> tablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(tablesToReset, tablesStatus, rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with tables status as status: " + tablesStatus);
		objBppTrendSetupPage.login(users.BUSINESS_ADMIN);

		//Step3: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup entry in grid to naivgate to details page");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Clicking on Imported Valuation Factors tab and validating whether its table is visible
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Imported Valuation Factors tab on details page");
		objBppTrnPg.Click(objBppTrendSetupPage.moreTabLeftSection);
		objBppTrnPg.javascriptClick(objBppTrendSetupPage.dropDownOptionBppImportedValuationFactors);
		objBppTrnPg.waitForElementToBeVisible(20,objBppTrendSetupPage.bppImportedValuationFactorsTableSection);
		boolean bppImportedValFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(20,objBppTrendSetupPage.bppImportedValuationFactorsTableSection);
		softAssert.assertTrue(bppImportedValFactorTableVisible, "SMAB-T229: Verify Imported Valuation Factors tab is displayed on BPP Trend setup Page");

		//Step6: Validating Edit button is visible under show more drop down
		objBppTrendSetupPage.clickShowMoreDropDownForGivenFactorEntry("Imported Valuation Factors");
		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		boolean isEditBtnPresent = objBuildPermitPage.editLinkUnderShowMore.isDisplayed();
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T284: EDIT button is present under show more link");

		//Step7: Editing and saving the newly created entry by updating name and index factor value as 0
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created entry");
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);

		objBppTrendSetupPage.enter("Valuation Factor", "0");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step8: Validating the error message displayed on entering index factor value as 0
		String expErrorMsgOnInvalidIndexValue = CONFIG.getProperty("errorMsgOnProvidingInvalidValuationFactor");
		String actualErrorMsgOnInvalidIndexValue = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgOnInvalidIndexValue, expErrorMsgOnInvalidIndexValue, "SMAB-T284: Validation for error mesage on entering invalid valuation factor value as 0");

		//Step9: Entering index factor value as -1
		objBppTrendSetupPage.enter("Valuation Factor", "-1");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step10: Validating the error message displayed on entering index factor value as -1
		actualErrorMsgOnInvalidIndexValue = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgOnInvalidIndexValue, expErrorMsgOnInvalidIndexValue, "SMAB-T284: Validation for error mesage on entering invalid valuation factor value as -1");
		Thread.sleep(1000);

		//Step11: Entering an invalid value for year acquired and clicking the save button
		objApasGenericPage.selectOptionFromDropDown("Year Acquired", rollYear);
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Step12: Validating the error message displayed on entering year acquired value
		//String expErrorMsgOnInvalidYearAcquired = CONFIG.getProperty("errorMsgOnProvidingInvalidAcquiredYearForValuation");
		String actualErrorMsgOnInvalidYearAcquired = objPage.getElementText(objBppTrendSetupPage.fieldError);
		softAssert.assertContains(actualErrorMsgOnInvalidYearAcquired, "Year Acquired", "SMAB-T284: Validation for error mesage on entering invalid year acquired value");

		//Step13: Canceling the pop up
		objPage.Click(objPage.getButtonWithText("Cancel"));

		//Step14: Opening the pop up again and clicking save button
		objBppTrendSetupPage.clickShowMoreDropDownForGivenFactorEntry("Imported Valuation Factors");
		Thread.sleep(2000);
		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);
		Thread.sleep(2000);
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step15: Validating the pop up message on saving the entry
		String popUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		softAssert.assertTrue((popUpMsg.contains("was saved")), "SMAB-T284: Pop up message displayed on editing and saving the entry- "+ popUpMsg);

		//Step16: Log out from the application
		softAssert.assertAll();

		//Step17: Assert all the assertions
		objBppTrendSetupPage.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is not able to edit the data once table calculation are submitted or approved:: TestCase/JIRA ID: SMAB-T284
	 */
	@Test(description = "SMAB-T284: Edit a new factors entry under Valuation factor table", groups = {"Regression","BPPTrend"}, dataProvider = "variousStatusOfTablesPostSubmittingCalculations", dataProviderClass = DataProviders.class)
	public void BppTrend_Edit_ValuationIndex_PostSubmittingCalculations(String tableStatus) throws Exception {
		//Step1: Resetting the status of valuation tables
		List<String> tablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(tablesToReset, tableStatus, rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with tables status as '"+ tableStatus +"'");
		objBppTrendSetupPage.login(users.BUSINESS_ADMIN);

		//Step3: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup entry in grid to naivgate to details page");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Clicking on BPP property index factor tab and validating whether its table is visible
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Valuation Factors tab on details page");
		objBppTrnPg.Click(objBppTrendSetupPage.moreTabLeftSection);
		objBppTrnPg.javascriptClick(objBppTrendSetupPage.dropDownOptionBppImportedValuationFactors);
		objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.bppImportedValuationFactorsTableSection, 20);

		//Step6: Clicking on the show more button and validating presence of Edit button
		objBppTrendSetupPage.clickShowMoreDropDownForGivenFactorEntry("Imported Valuation Factors");
		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		boolean isEditBtnPresent = objBuildPermitPage.editLinkUnderShowMore.isDisplayed();
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T284: EDIT button is present under show more link");
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T284: EDIT button is present under show more link");

		//Step7: Editing and saving the newly created entry by updating name and index factor value as 0
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created entry");
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);

		objBppTrendSetupPage.enter("Valuation Factor", "0");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step8: Validating the error message on entry creation when table status is Submitted For Approval / Approved
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the error message on entry creation when table status is Submitted For Approval / Approved");
		String expErrorMsgOnClickingSaveBtn = CONFIG.getProperty("errorMsgOnSavingRecordWhenValuationSubmittedOrApproved");
		String actErrorMsgOnClickingSaveBtn = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actErrorMsgOnClickingSaveBtn, expErrorMsgOnClickingSaveBtn, "SMAB-T284: Validation for error mesage on creating entry when status is "+ tableStatus);

		objPage.Click(objPage.getButtonWithText("Cancel"));
		Thread.sleep(1000);

		//Step9: Log out from the application
		objBppTrendSetupPage.logout();

		//Step10: Assert all the assertions
		softAssert.assertAll();
	}

	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating that user is able to edit an entry:: TestCase/JIRA ID: SMAB-T283, SMAB-T285, SMAB-T288
	 */
	@Test(description = "SMAB-T283,SMAB-T285,SMAB-T288: Edit new factors entry under BPP Percent Goods Factors table having different status of tables before submitting calculation", groups = {"Regression","BPPTrend"}, dataProvider = "variousStatusOfCompositeTablesBeforeSubmitting", dataProviderClass = DataProviders.class)
	public void BppTrend_Edit_PercentGoodsFactorEntry_WithDifferentStatusOfTables(String tablesStatus) throws Exception {
		//Step1: Resetting the composite factor tables status
		List<String> tablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(tablesToReset, tablesStatus, rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with tables status as status: " + tablesStatus);
		objBppTrendSetupPage.login(users.BUSINESS_ADMIN);

		//Step3: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup entry in grid to naivgate to details page");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Clicking on BPP PErcent Goods Factors tab and validating whether its table is visible
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Percent Goods Factors tab on details page");
		objBppTrnPg.Click(objBppTrendSetupPage.bppPropertyGoodFactorsTab);
		objBppTrnPg.waitForElementToBeVisible(20,objBppTrendSetupPage.bppPercentGoodFactorsTableSection);
		boolean bppPercentGoodsFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(20,objBppTrendSetupPage.bppPercentGoodFactorsTableSection);
		softAssert.assertTrue(bppPercentGoodsFactorTableVisible, "SMAB-T229: Verify BPP Percent Goods Factors tab is displayed on BPP Trend setup Page");

		//Step6: Validating Edit button is visible under show more drop down
		objBppTrendSetupPage.clickShowMoreDropDownForGivenFactorEntry("BPP Percent Good Factors");
		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		boolean isEditBtnPresent = objBuildPermitPage.editLinkUnderShowMore.isDisplayed();
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T283,SMAB-T285,SMAB-T288: EDIT button is present under show more link");

		//Step7: Editing and saving the newly created entry by updating name and index factor value as 0
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created entry");
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);

		//Step8: Entering index factor value as -1
		objBppTrendSetupPage.enter("General Good Factor", "-1");
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Step9: Validating the error message displayed on entering index factor value as -1
		String expErrorMsgOnInvalidIndexValue = CONFIG.getProperty("errorMsgOnProvidingInvalidGoodFactor");
		String actualErrorMsgOnInvalidIndexValue = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgOnInvalidIndexValue, expErrorMsgOnInvalidIndexValue, "SMAB-T283,SMAB-T285,SMAB-T288: Validation for error mesage on entering invalid good factor value as -1");

		//Step10: Entering an invalid value for year acquired and clicking the save button
		objApasGenericPage.selectOptionFromDropDown("Year Acquired", rollYear);
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Step11: Validating the error message displayed on entering year acquired value
		String expErrorMsgOnInvalidYearAcquired = CONFIG.getProperty("errorMsgOnProvidingInvalidAcquiredYear");
		String actualErrorMsgOnInvalidYearAcquired = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actualErrorMsgOnInvalidYearAcquired, expErrorMsgOnInvalidYearAcquired, "SMAB-T283,SMAB-T285,SMAB-T288: Validation for error mesage on entering invalid year acquired value");

		//Step12: Canceling the pop up
		objPage.Click(objPage.getButtonWithText("Cancel"));
		Thread.sleep(1000);

		//Step13: Opening the pop up again and clicking save button
		objBppTrendSetupPage.clickShowMoreDropDownForGivenFactorEntry("BPP Percent Good Factors");

		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);
		Thread.sleep(1000);
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step14: Validating the pop up message on saving the entry
		String popUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		softAssert.assertTrue((popUpMsg.contains("was saved")), "SMAB-T283,SMAB-T285,SMAB-T288: Pop up message displayed on editing and saving the entry- "+ popUpMsg);

		//Step15: Log out from the application
		softAssert.assertAll();

		//Step16: Assert all the assertions
		objBppTrendSetupPage.logout();
	}

	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is not able to edit the data once table calculation are submitted or approved:: TestCase/JIRA ID: SMAB-T285
	 */
	@Test(description = "SMAB-T285: Edit a new factors entry under BPP Percent Goods factor table", groups = {"Regression","BPPTrend"}, dataProvider = "variousStatusOfTablesPostSubmittingCalculations", dataProviderClass = DataProviders.class)
	public void BppTrend_Edit_PercentGoods_PostSubmittingCalculations(String tableStatus) throws Exception {
		//Step1: Resetting the composite and valuation factor tables status
		List<String> tablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(tablesToReset, tableStatus, rollYear);

		List<String> valTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valTablesToReset, "Yet to submit for Approval", rollYear);

		List<String> Prop13TableToReset = Arrays.asList(CONFIG.getProperty("Prop13TableToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(Prop13TableToReset, "Calculated", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with tables status as '"+ tableStatus +"'");
		objBppTrendSetupPage.login(users.BUSINESS_ADMIN);

		//Step3: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup entry in grid to naivgate to details page");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Clicking on BPP Percent Goods Factors tab and validating whether its table is visible
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Percent Goods Factors tab on details page");
		objBppTrnPg.Click(objBppTrendSetupPage.bppPropertyGoodFactorsTab);
		objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.bppPercentGoodFactorsTableSection, 20);

		//Step6: Clicking on the show more button and validating presence of Edit button
		objBppTrendSetupPage.clickShowMoreDropDownForGivenFactorEntry("BPP Percent Good Factors");
		objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		boolean isEditBtnPresent = objBuildPermitPage.editLinkUnderShowMore.isDisplayed();
		softAssert.assertTrue(isEditBtnPresent, "SMAB-T285: EDIT button is present under show more link");

		//Step7: Editing and saving the newly created entry by updating name and index factor value as 0
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created entry");
		objBuildPermitPage.clickAction(objBuildPermitPage.editLinkUnderShowMore);

		objBppTrendSetupPage.enter("General Good Factor", "0");
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step8: Validating the error message on entry creation when table status is Submitted For Approval / Approved
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the error message on entry creation when table status is Submitted For Approval / Approved");
		String expErrorMsgOnClickingSaveBtn = CONFIG.getProperty("errorMsgOnSavingRecordWhenPercentGoodsSubmittedOrApproved");
		String actErrorMsgOnClickingSaveBtn = objPage.getElementText(objBuildPermitPage.pageError);
		softAssert.assertContains(actErrorMsgOnClickingSaveBtn, expErrorMsgOnClickingSaveBtn, "SMAB-T285: Validation for error mesage on creating entry when status is "+ tableStatus);

		objPage.Click(objPage.getButtonWithText("Cancel"));
		Thread.sleep(1000);

		//Step9: Log out from the application
		objBppTrendSetupPage.logout();

		//Step10: Assert all the assertions
		softAssert.assertAll();
	}
}
