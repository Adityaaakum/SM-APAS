package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.apas.PageObjects.*;
import org.openqa.selenium.WebElement;
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
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_Setup_Setting_Test extends TestBase {
	
	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	BuildingPermitPage objBuildPermitPage;
	ApasGenericPage objApasGenericPage;
	BppTrendSetupPage objBppTrendSetupPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		
		if(driver==null) {
            setupTest();
            driver = BrowserDriver.getBrowserInstance();
        }
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
	}
	
	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able to update and enter the 'Max. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T133
	 * 2. Validating the user is not able to enter invalid value of 'Max. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T134
	 */
	@Test(description = "SMAB-T133,SMAB-T134,SMAB-T139: Create and edit BPP Setting with invalid and valid max. equip. index factor", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_CreateAndEdit_BppSetting(String loginUser) throws Exception {		
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Delete the existing BPP composite setting entry for given roll year
		objBppTrnPg.removeExistingBppSettingEntry(rollYear);
		
		//Step3: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step4: Generating a data map from data file
		String bppTrendSetupData = System.getProperty("user.dir") + testdata.BPP_TREND_DATA;
		Map<String, String> bppTrendSettingDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendSetting");
	
		//Step5: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		
		//Step6: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Step7: Get existing count of BPP Settings and click BPP Setting drop down
		String bppSettingCountBeforeCreatingNewSetting = objBppTrendSetupPage.getCountOfBppSettings();
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconBppSetting));

		//Step8: Click on New option to create BPP Setting entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.newBtnToCreateEntry));
		
		//Step9: Validate error message with factor values less than minimum range
		String expectedErrorMsgOnIncorrectFactorValue;
		String actualErrorMsgOnIncorrectFactorValue;
		List<String> multipleFactorIncorrectVauesList = Arrays.asList(CONFIG.getProperty("FactorValuesLessThanMinRange").split(","));
		for(int i = 0; i < multipleFactorIncorrectVauesList.size(); i++) {
			objBppTrendSetupPage.enterFactorValue(multipleFactorIncorrectVauesList.get(i));
			objBppTrnPg.Click(objBuildPermitPage.saveButton);
			if(i == 0) {
				String expectedErrorMsgOnFactorValueWithinRangeWithCharacter = CONFIG.getProperty("ErrorMsgOnFactorValueWithinRangeWithCharacter");
				String actualErrorMsgOnFactorValueWithinRangeWithCharacter = objBppTrendSetupPage.errorMsgOnIncorrectFactorValue();
				boolean isErrorMsgDisplayed = actualErrorMsgOnFactorValueWithinRangeWithCharacter.equals(expectedErrorMsgOnFactorValueWithinRangeWithCharacter);
				softAssert.assertTrue(isErrorMsgDisplayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnFactorValueWithinRangeWithCharacter + "' for factor within range having a charcter value");
			} else {		
				expectedErrorMsgOnIncorrectFactorValue = CONFIG.getProperty("ErrorMsgOnFactorValueLessThanMinRange");
				actualErrorMsgOnIncorrectFactorValue = objBppTrendSetupPage.errorMsgOnIncorrectFactorValue();
				boolean isErrorMsgDisplayed = actualErrorMsgOnIncorrectFactorValue.equals(expectedErrorMsgOnIncorrectFactorValue);
				softAssert.assertTrue(isErrorMsgDisplayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnIncorrectFactorValue + "' for factor value less than mumimum range");
				softAssert.assertTrue(isErrorMsgDisplayed, "SMAB-T138: Error message displayed: '" + actualErrorMsgOnIncorrectFactorValue + "' for factor value less than mumimum range");			}
		}
		
		//Step10: Validate error message with factor values greater than maximum range
		String FactorValueGreaterThanMaxRange = CONFIG.getProperty("FactorValueGreaterThanMaxRange");
		objBppTrendSetupPage.enterFactorValue(FactorValueGreaterThanMaxRange);
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		
		expectedErrorMsgOnIncorrectFactorValue = CONFIG.getProperty("ErrorMsgOnFactorValueGreaterThanMaxRange");
		actualErrorMsgOnIncorrectFactorValue = objBppTrendSetupPage.errorMsgOnIncorrectFactorValue();
		boolean isErrorMsgDisplayed = actualErrorMsgOnIncorrectFactorValue.contains(expectedErrorMsgOnIncorrectFactorValue);
		softAssert.assertTrue(isErrorMsgDisplayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnIncorrectFactorValue + "' for  factor value greater than minimum range");
		
		//Step11: Close the currently opened BPP setting entry pop up
		objBppTrnPg.Click(objBuildPermitPage.closeEntryPopUp);
		
		//Step12: Create and Edit bpp setting entry with factor values within specified range
		List<String> multipleFactorCorrectVauesList = Arrays.asList(CONFIG.getProperty("FactorValuesWithinRange").split(","));
		for(int i = 0; i < multipleFactorCorrectVauesList.size(); i++) {
			if(i == 0) {
				//Creating the BPP setting entry
				Thread.sleep(1000);
				objBppTrnPg.clickAction(objBppTrendSetupPage.dropDownIconBppSetting);
				objBppTrnPg.clickAction(objBppTrendSetupPage.newBtnToCreateEntry);
				objBppTrendSetupPage.enterRollYearInBppSettingDetails(rollYear);
				objBppTrendSetupPage.enterFactorValue(multipleFactorCorrectVauesList.get(i));
				objBppTrnPg.Click(objBuildPermitPage.saveButton);
			} else {
				//Retrieving equipment index factor value before performing edit operation
				String factorValueBeforeEdit = objBppTrnPg.getElementText(objBppTrnPg.factorValue);
				factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
				
				//Editing the BPP newly created setting entry				
				objBppTrnPg.clickAction(objBppTrendSetupPage.dropDownIconDetailsSection);
				Thread.sleep(1000);
				objBppTrnPg.clickAction(objBuildPermitPage.editLinkUnderShowMore);
				objBppTrendSetupPage.enterFactorValue(multipleFactorCorrectVauesList.get(i));
				objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
				
				//Retrieving equipment index factor value after performing edit operation
				Thread.sleep(2000);
				String factorValueAfterEdit = objBppTrnPg.getElementText(objBppTrnPg.factorValue);
				factorValueAfterEdit = factorValueAfterEdit.substring(0, factorValueAfterEdit.length()-1);
				
				//Validation for checking whether updated values are reflecting or not
				softAssert.assertTrue(!(factorValueAfterEdit.equals(factorValueBeforeEdit)), "SMAB-T133: Maximum equipment index factor successfully updated & reflecting in right panel. Value before edit: "+ factorValueBeforeEdit +" || Value after edit: "+ factorValueAfterEdit);
			}
		}
		
		//Step13: Validating the count of BPP Setting after creating new bpp setting
		String bppSettingCountAfterCreatingNewSetting = objBppTrendSetupPage.getCountOfBppSettings();
		softAssert.assertTrue(!(bppSettingCountAfterCreatingNewSetting.equals(bppSettingCountBeforeCreatingNewSetting)), "SMAB-T133: BPP trend setting successfully created & reflecting in right panel. Bpp setting count before creating new setting: "+ bppSettingCountBeforeCreatingNewSetting +" || Bpp setting count after creating and editing new setting: "+ bppSettingCountAfterCreatingNewSetting);

		//Step14: Retrieving the name of newly created bpp setting entry
		String bppSettingName = objBppTrnPg.getElementText(objBppTrnPg.bppSettingName);
				
		//Step15: Click ViewAll link to navigate to BPP settings grid and edit existing bpp setting entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.viewAllBppSettings));
		
		//Step16: Retrieving the equipment index factor value before editing
		String factorValueDisplayedBeforeEditing = objBppTrendSetupPage.retrieveFactorValueFromGrid(bppSettingName);

		//Step17: Editing and updating the equipment index factor value
		String factorValue = bppTrendSettingDataMap.get("Maximum Equipment index Factor");
		objBuildPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(bppSettingName);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.editLinkUnderShowMore));
		
		int updatedFactorValue = Integer.parseInt(factorValue) + 1;
		objBppTrendSetupPage.enterFactorValue(Integer.toString(updatedFactorValue));
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));

		//Step18: Retrieving and validating the equipment index factor value after editing
		Thread.sleep(2000);
		String factorValueDisplayedAfterEditing = objBppTrendSetupPage.retrieveFactorValueFromGrid(bppSettingName);
		softAssert.assertTrue(!(factorValueDisplayedAfterEditing.equals(factorValueDisplayedBeforeEditing)), "SMAB-T133: Validation to check equipment index updated with new value. Factor value in grid before editing: "+ factorValueDisplayedBeforeEditing +" || Factor value in grid after editing: "+ factorValueDisplayedAfterEditing);
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able select and view  input factor tables on BPP Trend Setup page:: SMAB-T229
	 */
	@Test(description = "SMAB-T229: Check for availability of input factor tables on bpp trend roll year screen",  groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAndPrincipalUsers", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_ValidateInputFactorTables_OnDetailsPage(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		
		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Step4: Clicking on BPP property index factor tab and validating whether its table is visible
		objBppTrnPg.Click(objBppTrendSetupPage.bppPropertyIndexFactorsTab);
		boolean isPropertyIndexFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.bppPropertyIndexFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isPropertyIndexFactorTableVisible, "SMAB-T229: BPP property index factors table is visible on roll year details page");

		//Step5: Clicking on BPP property good factor tab and validating whether its table is visible
		objBppTrnPg.Click(objBppTrendSetupPage.bppPropertyGoodFactorsTab);
		boolean isPercentGoodsFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.bppPercentGoodFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isPercentGoodsFactorTableVisible, "SMAB-T229: BPP property good factors table is visible on roll year details page");
		
		//Step6: Clicking more tab & then clicking on BPP valuation factor tab option & validating whether its table is visible
		objBppTrnPg.Click(objBppTrendSetupPage.moreTabLeftSection);
		objBppTrnPg.javascriptClick(objBppTrendSetupPage.dropDownOptionBppImportedValuationFactors);
		boolean isValuationFactorsTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrendSetupPage.bppImportedValuationFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isValuationFactorsTableVisible, "SMAB-T229: BPP valuation factors table is visible on roll year details page");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	

	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is able to edit Max. Equip. Index when status is Calculated:: Test Case/JIRA ID: SMAB-T271
	 * 2. Validating user is able to edit Max. Equip. Index when status is Needs Recalculation:: Test Case/JIRA ID: SMAB-T272
	 * 3. Validating user is able to edit Max. Equip. Index when status is Submitted for Approval:: Test Case/JIRA ID: SMAB-T273
	 */
	@Test(description = "SMAB-T271,SMAB-T272,SMAB-T273: Edit BPP Setting with with different status of tables", groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_EditMaxEquipIndex_WithVariousStatusOfTables(String loginUser) throws Exception {
		//Step1: Resetting the composite factor tables status to Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the BPP Trend Setup module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step5: Retrieving equipment index factor value before performing edit operation
		String factorValueBeforeEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
					
		//Step6: Edit Maximum Equipment Index factor value
		int maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) + 1;
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage(Integer.toString(maxEquipIndexNewValue));
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		String saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.popUpSaveBtn);
		softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T271: Updated value of maximum equip. index factor has been saved");
			
		//Step7: Retrieving equipment index factor value after performing edit operation
		objBppTrnPg.waitForElementTextToBe(objBppTrnPg.maxEquipIndexValueOnDetailsPage, Integer.toString(maxEquipIndexNewValue), 10);
		String factorValueAfterEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		factorValueAfterEdit = factorValueAfterEdit.substring(0, factorValueAfterEdit.length()-1);
			
		//Step8: Validation for checking whether updated values are reflecting or not
		softAssert.assertTrue(!(factorValueAfterEdit.equals(factorValueBeforeEdit)), "SMAB-T271: Maximum equipment index factor successfully updated & reflecting in right panel. Value before edit: "+ factorValueBeforeEdit +" || Value after edit: "+ factorValueAfterEdit);
			
		//Step9: Log out from application
		objApasGenericFunctions.logout();
			
		//Step10: Reset status of tables to "Needs Recalculation"
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Needs Recalculation", rollYear);
	
		//Step11: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + users.BUSINESS_ADMIN);
		objApasGenericFunctions.login(users.BUSINESS_ADMIN);
			
		//Step12: Opening the BPP Trend Setup module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
	
		//Step13: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
			
		//Step14: Retrieving equipment index factor value before performing edit operation
		factorValueBeforeEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
					
		//Step15: Edit Maximum Equipment Index factor value
		maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) + 1;
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage(Integer.toString(maxEquipIndexNewValue));
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.popUpSaveBtn);
		softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T271: Updated value of maximum equip. index factor has been saved");
			
		//Step16: Retrieving equipment index factor value after performing edit operation
		objBppTrnPg.waitForElementTextToBe(objBppTrnPg.maxEquipIndexValueOnDetailsPage, Integer.toString(maxEquipIndexNewValue), 10);
		factorValueAfterEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		factorValueAfterEdit = factorValueAfterEdit.substring(0, factorValueAfterEdit.length()-1);
			
		//Step17: Validation for checking whether updated values are reflecting or not
		softAssert.assertTrue(!(factorValueAfterEdit.equals(factorValueBeforeEdit)), "SMAB-T271: Maximum equipment index factor successfully updated & reflecting in right panel. Value before edit: "+ factorValueBeforeEdit +" || Value after edit: "+ factorValueAfterEdit);
			
		//Step18: Log out from application
		objApasGenericFunctions.logout();
			
		//Step19: Reset status of tables to "Needs Recalculation"
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Submitted for Approval", rollYear);
	
		//Step20: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + users.BUSINESS_ADMIN);
		objApasGenericFunctions.login(users.BUSINESS_ADMIN);
			
		//Step21: Opening the BPP Trend Setup module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
	
		//Step22: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
			
		//Step23: Retrieving equipment index factor value before performing edit operation
		factorValueBeforeEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
					
		//Step24: Edit Maximum Equipment Index factor value
		maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) + 1;
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage(Integer.toString(maxEquipIndexNewValue));
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		String errorMessage = objBppTrnPg.getElementText(objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.errorMsgOnTop, 10));
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.cancelButton));
		softAssert.assertContains(errorMessage, "Maximum Equipment index Factor is locked for editing for the Roll Year", "SMAB-T271: Updated value of maximum equip. index factor has been saved");

		//Step 25: Reverting the changes done to maximum equipment index factor value
		ExtentTestManager.getTest().log(LogStatus.INFO, "Reverting the maximum equipment index factor value");
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);
		driver.navigate().refresh();
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage("125");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));

		//Step28: Log out from application
		softAssert.assertAll();
		objBppTrnPg.closePageLevelMsgPopUp();
		objApasGenericFunctions.logout();
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able to create and update the 'Max. Equip. Index Factor' when calculations are done:: TestCase/JIRA ID: SMAB-T136
	 */
	@Test(description = "SMAB-T136,SMAB-T139: Crete, edit BPP Setting when table calculcations are done", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_CreateAndEdit_BppSetting_WhenCalculationAreDone(String loginUser) throws Exception {
		//Step1: Resetting the composite factor tables status to Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Delete the existing BPP composite setting entry for given roll year
		objBppTrnPg.removeExistingBppSettingEntry(rollYear);
		
		//Step3: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
	
		//Step4: Opening the BPP Trend module and set All as the view option in grid and select a BPP trend setup from grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on BPP Trend Setup name in the grid");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		/**Step5: Checking the status of tables on details page
		 * a. Fetch composite factor table names from properties file and collect them in a single list
		 * b. Fetch composite factor table names from properties file and collect them in a single list
		 * c. Iterate over composite factor tables list and validate their status BPP trend setup on details page
		 * d. Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		 */
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String tableName, currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T136: Status of "+ tableName +" on Bpp Trend Setup page ");
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T139: Status of "+ tableName +" on Bpp Trend Setup page ");
		}
		
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T136: Status of "+ tableName +" on Bpp Trend Setup page");
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T139: Status of "+ tableName +" on Bpp Trend Setup page");
		}
		
		//Step5: Creating a new entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating an entry for Maximum Equipment Index factor");
		objBppTrendSetupPage.createBppSetting("125");
		String popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T136: New Bpp Setting entry created successfully. Pop up message displayed- "+ popUpMsg);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T139: New Bpp Setting entry created successfully. Pop up message displayed- "+ popUpMsg);
		objBppTrnPg.closePageLevelMsgPopUp();
		//Step6: Edit the newly created entry on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created maximum equipment index factor entry");
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage("150");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was saved")), "SMAB-T136: New Bpp Setting entry edited successfully. Pop up message displayed- "+ popUpMsg);
		softAssert.assertTrue((popUpMsg.contains("was saved")), "SMAB-T139: New Bpp Setting entry edited successfully. Pop up message displayed- "+ popUpMsg);
		
		//Step7: Delete the newly created entry
		//ExtentTestManager.getTest().log(LogStatus.INFO, "Deleting the newly created entry");
		//objBppTrnPg.deleteBppSettingValueOnDetailsPage();
		//popUpMsg = objBppTrnPg.waitForPopUpMsg(10);
		//softAssert.assertTrue((popUpMsg.contains("was deleted")), "SMAB-T136: Bpp Setting entry deleted successfully. Pop up message displayed- "+ popUpMsg);
		//softAssert.assertTrue((popUpMsg.contains("was deleted")), "SMAB-T139: Bpp Setting entry deleted successfully. Pop up message displayed- "+ popUpMsg);
		
		//Step8: Re-Creating a new entry
		//ExtentTestManager.getTest().log(LogStatus.INFO, "Re-Creating an entry for Maximum Equipment Index factor");
		//objBppTrnPg.createBppSetting("160");
		//popUpMsg = objBppTrnPg.waitForPopUpMsg(10);
		//softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T136: New Bpp Setting entry created successfully. Pop up message displayed- "+ popUpMsg);
		//softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T139: New Bpp Setting entry created successfully. Pop up message displayed- "+ popUpMsg);
		
		//Step9: Retrieving the name of newly created BPP setting entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Retrieving the name of newly created BPP setting entry");
		String bppSettingName = objBppTrnPg.getElementText(objBppTrnPg.bppSettingName);
		
		//Step10: Click ViewAll link to navigate to BPP settings grid page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click ViewAll link to navigate to BPP settings grid page");
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.viewAllBppSettings));
		
		//Step11: Retrieving the equipment index factor value before editing
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the maximum equipment index factor value on view all page");
		String factorValueOnVeiwAllPage = objBppTrendSetupPage.retrieveFactorValueFromGrid(bppSettingName);
		softAssert.assertEquals(factorValueOnVeiwAllPage, "150%", "SMAB-T136: Validating maximum equip. index factor value on view all page");
		//softAssert.assertEquals(factorValueOnVeiwAllPage, "160%", "SMAB-T136: Validating maximum equip. index factor value on view all page");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is unable to update 'Max. Equip. Index Factor' when calculations are approved:: TestCase/JIRA ID: SMAB-T137, SMAB-T274
	 * 2. Validating user is unable to delete 'Max. Equip. Index Factor' when calculations are approved:: TestCase/JIRA ID: SMAB-T137
	 */
	@Test(description = "SMAB-T137,SMAB-T274: Edit BPP Setting when table calculations are approved", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_EditBppSetting_AfterApprovalOfTableCalculations(String loginUser) throws Exception {		
		//Step1: Resetting the composite factor tables status to Approved
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Step2: Resetting the valuation factor tables status to Approved
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
		
		//Delete the existing BPP composite setting entry for given roll year
		objBppTrnPg.removeExistingBppSettingEntry(rollYear);
		
		//Step3: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
	
		//Step4: Opening the BPP Trend module and set All as the view option in grid and select a BPP trend setup from grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on the BPP Trend Setup entry in the grid");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		/*Step5: Checking the status of tables on details page
		 * a. Fetch composite factor table names from properties file and collect them in a single list
		 * b. Fetch composite factor table names from properties file and collect them in a single list
		 * c. Iterate over composite factor tables list and validate their status BPP trend setup on details page
		 * d. Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		 */
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String tableName, currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Approved", "SMAB-T137: Status of "+ tableName +" on Bpp Trend Setup page ");
			softAssert.assertEquals(currentStatus, "Approved", "SMAB-T274: Status of "+ tableName +" on Bpp Trend Setup page ");
		}
		
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Approved", "SMAB-T137: Status of "+ tableName +" on Bpp Trend Setup page");
			softAssert.assertEquals(currentStatus, "Approved", "SMAB-T274: Status of "+ tableName +" on Bpp Trend Setup page");
		}
		
		//Step5: Creating a new entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating a new entry for maximum equipment index factor");
		objBppTrendSetupPage.createBppSetting("125");
		String popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T137: New Bpp Setting entry created successfully. Message on Entry creation- "+ popUpMsg);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T274: New Bpp Setting entry created successfully. Message on Entry creation- "+ popUpMsg);
		objBppTrnPg.closePageLevelMsgPopUp();
		//Step6: Edit the newly created entry on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the newly created maximum equipment index factor entry post table calculations are approved");
		objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconDetailsSection, 10);
		objBppTrnPg.clickAction(objBppTrendSetupPage.dropDownIconDetailsSection);
		objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.editLinkUnderShowMore, 10);
		objBppTrnPg.clickAction(objBuildPermitPage.editLinkUnderShowMore);
		objBppTrendSetupPage.enterFactorValue("150");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);

		String expectedErrorMessage = "Maximum Equipment index Factor is locked for editing for the Roll Year";
		String errorMsgOnClickingSaveBtn = objBppTrnPg.getElementText(objBuildPermitPage.errorMsgOnTop);
		softAssert.assertEquals(errorMsgOnClickingSaveBtn, expectedErrorMessage, "SMAB-T137: Validating error message on editing and saving max. equip. index value when calculations are approved");
		
		//Step7: Enter original value and save
		objBppTrendSetupPage.enterFactorValue("125");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was saved")), "SMAB-T137: Bpp Setting entry was edited and saved created successfully with its original value. Message on editing and saving with original value- "+ popUpMsg);
		softAssert.assertTrue((popUpMsg.contains("was saved")), "SMAB-T274: Bpp Setting entry was edited and saved created successfully with its original value. Message on editing and saving with original value- "+ popUpMsg);
		
		//Step8: Delete the entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the absence of delete option once table claculations are approved.");
		objBppTrnPg.clickAction(objBppTrendSetupPage.dropDownIconDetailsSection);
		WebElement deleteButton = objBuildPermitPage.deleteLinkUnderShowMore;
		boolean unAvailabilityOfDeleteBtn = false;
		if(deleteButton == null) {
			unAvailabilityOfDeleteBtn = true;
		} else {
			unAvailabilityOfDeleteBtn = true;
		}
		softAssert.assertTrue(unAvailabilityOfDeleteBtn, "SMAB-T137: For User '"+ loginUser +"': Delete button is not visible under show more drop down when calculations are approved");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing following for table: <ALL APPROVED FACTORS TABLES>
	 * 1. Checking status of tables on BPP Trend Setup page:: Test Case/JIRA ID: SMAB-T170, SMAB-T172
	 * 2. Validating availability of calculate button before calculation is initiated:: Test Case/JIRA ID: SMAB-T170, SMAB-T172
	 * 3. Calculating some tables & validating presence of ReCalculate button post calculating:: Test Case/JIRA ID: SMAB-T170, SMAB-T172
	 * 4. Checking status of tables on BPP Trend Setup page:: Test Case/JIRA ID: SMAB-T170, SMAB-T172
	 * 5. Modify the BPP Setting value from 125 to 150:: Test Case/JIRA ID: SMAB-T172
	 * 5. Calculating rest of the tables and validating presence of Recalculate button after calculation is done:: Test Case/JIRA ID: SMAB-T170
	 * 6. Checking status of tables on BPP Trend Setup page:: Test Case/JIRA ID: SMAB-T170
	 */
	@Test(description = "SMAB-T170,SMAB-T172,SMAB-T173: Perform calculation & re-calculation for factors tables individually using calculate & recalclate buttons with updating max. equip. index factor", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_CalculateAndReCalculate_ByUpdatingMaxEquipIndexFactor(String loginUser) throws Exception {	
		
		//Step3: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step4: Navigate to BPP Trend Setup page and check status of composite & valuation tables
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Deleting and creating BPP Setting Entry
		objBppTrnPg.removeExistingBppSettingEntry(rollYear);
		objBppTrendSetupPage.createBPPSettingEntry(rollYear,"125");
		
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step4: Navigate to BPP Trend Setup page and check status of composite & valuation tables
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Step1: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		//Step2: Fetch composite factor table names from properties file and collect them in a single list		
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
		
		//Step5: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String tableName, currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
			softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
		}
		
		//Step6: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
		}
		
		//Step7: Opening the BPP Trend module, selecting roll year and clicking select button
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
		
		/**
		 * Step8: Clicking on Commercial Composite Factors table
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating Calculation For COMMERCIAL COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Commercial Composite Factors");
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "Commercial Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Commercial Composite Factors table");

		objBppTrnPg.clickCalculateBtn("Commercial Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		boolean isTableVisible = objBppTrnPg.isTableDataVisible("Commercial Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Commercial Composite Factors table");
		
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Commercial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Commercial Composite Factors table");
		
		/**
		 * Step9: Clicking on Industrial Composite Factors table
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating Calculation For INDUSTRIAL COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Industrial Composite Factors");
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "Industrial Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Industrial Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Industrial Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Industrial Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Industrial Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Industrial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Industrial Composite Factors table");

		/**
		 * Step10: Clicking on Construction Mobile Equipment Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating Calculation For CONSTRUCTION MOBILE EQUIP COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Construction Mobile Equipment Composite Factors");
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Construction Mobile Equipment Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Construction Mobile Equipment Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Construction Mobile Equipment Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		/**
		 * Step11: Clicking on BPP Prop 13 Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating Calculation For BPP PROP 13 Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("BPP Prop 13 Factors");
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "BPP Prop 13 Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for BPP Prop 13 Factors table");
		
		objBppTrnPg.clickCalculateBtn("BPP Prop 13 Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("BPP Prop 13 Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for BPP Prop 13 Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for BPP Prop 13 Factors table");

		//Step12: Navigating to BPP Trend Setup page and again validating status of table 
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		//Step13: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			if(tableName.equals("Ag. Trends Status") || tableName.equals("Ag. Mobile Equipment Trends Status") || tableName.equals("Const. Trends Status")) {
				softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation. Expected Status:: Not Calculated || Actual Status:: "+ currentStatus);
			} else {
				softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			}
		}
		
		//Step14: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page with partial calculation");
		}
		
		//Step15: Updating the maximum equipment index factor value from 125 to 150.
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage("150");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		String saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.popUpSaveBtn);
		softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T172: Updated value of maximum equip. index factor has been saved");
		
		/**
		 * Step16: Opening the BPP Trend module, selecting roll year and clicking select button 
		 * Clicking on Commercial Composite Factors table
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For COMMERCIAL COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Commercial Composite Factors");
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Commercial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Commercial Composite Factors table");

		objBppTrnPg.clickReCalculateBtn("Commercial Composite Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Commercial Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for Commercial Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Commercial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Commercial Composite Factors table");
		
		/**
		 * Step17: Clicking on Industrial Composite Factors table
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For INDUSTRIAL COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Industrial Composite Factors");
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Industrial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Industrial Composite Factors table");
		
		objBppTrnPg.clickReCalculateBtn("Industrial Composite Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Industrial Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for Industrial Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Industrial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Industrial Composite Factors table");

		/**
		 * Step18: Clicking on Construction Mobile Equipment Composite Factors
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For CONSTRUCTION MOBILE EQUIP COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Construction Mobile Equipment Composite Factors");
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		objBppTrnPg.clickReCalculateBtn("Construction Mobile Equipment Composite Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Construction Mobile Equipment Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for Construction Mobile Equipment Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		/**
		 * Step19: Clicking on BPP Prop 13 Factors
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For BPP PROP 13 Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("BPP Prop 13 Factors");
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for BPP Prop 13 Factors table");
		
		objBppTrnPg.clickReCalculateBtn("BPP Prop 13 Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("BPP Prop 13 Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for BPP Prop 13 Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for BPP Prop 13 Factors table");

		//Step20: Navigating to BPP Trend Setup page and again validating status of table 
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		//Step21: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			if(tableName.equals("Ag. Trends Status") || tableName.equals("Ag. Mobile Equipment Trends Status") || tableName.equals("Const. Trends Status")) {
				softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation. Expected Status:: Not Calculated || Actual Status:: "+ currentStatus);	
			} else {
				softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post Re-Calculation");
			}
		}
		
		//Step22: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page with partial Calculation/Re-Calculation");
		}
		
		//Step23: Reverting the maximum equipment index factor value from 150 to 125.
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Reverting the Max. Equip. Index Factor & Calculating Remaining Tables ******");
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage("125");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.popUpSaveBtn);
		softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T173: Updated value of maximum equip. index factor has been saved");
		
		//Step24: Opening the BPP Trend module, selecting roll year and clicking select button
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
		
		/**
		 * Step25: Clicking on Construction Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		objBppTrnPg.clickOnTableOnBppTrendPage("Construction Composite Factors");
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "Construction Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T173: Calcuate button is visible for Construction Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Construction Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Construction Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Construction Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Construction Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Construction Composite Factors table");
		
		/**
		 * Step26: Clicking on Agricultural Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		objBppTrnPg.clickOnTableOnBppTrendPage("Agricultural Composite Factors");
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "Agricultural Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T173: Calcuate button is visible for Agricultural Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Agricultural Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Agricultural Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Agricultural Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Agricultural Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Agricultural Composite Factors table");
		
		/**
		 * Step27: Clicking on Agricultural Mobile Equipment Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		objBppTrnPg.clickOnTableOnBppTrendPage("Agricultural Mobile Equipment Composite Factors");
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(30, "Agricultural Mobile Equipment Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Agricultural Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Agricultural Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T173: Calcuate button is visible for Agricultural Mobile Equipment Composite Factors table");

		objBppTrnPg.clickCalculateBtn("Agricultural Mobile Equipment Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(30);
		isTableVisible = objBppTrnPg.isTableDataVisible("Agricultural Mobile Equipment Composite Factors", 20);
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Agricultural Mobile Equipment Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(30, "Agricultural Mobile Equipment Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Agricultural Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Agricultural Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Agricultural Mobile Equipment Composite Factors table");

		/**
		 * Step28: Iterate over a list having all valuation factor tables and validate unavailability of Calculate button
		 * Iterating over a list containing all valuation factor tables
		 * Click on the table name and validating unavailability of CALCULATE button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating unavailability of Calculate Button For Valuation Factors ******");
		List<String> valuationFactorTables = Arrays.asList(CONFIG.getProperty("valuationTablesUnderMoreTab").split(","));
		for(String valuationFactorTable : valuationFactorTables) {
			objBppTrnPg.clickOnTableOnBppTrendPage(valuationFactorTable);
			isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(5, "valuationFactorTable");
			softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T249: Calculate button is NOT visible for "+ valuationFactorTable +" table");
		}
		
		//Step29: Recalculating all the tables with reverted values of maximum equipment index factor
		objBppTrnPg.clickReCalculateAllBtn();
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		
		//Step30: Validating Submit All Factors For Approval button once all composite factors tables are calculated
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating availability of Submit All Factors For Approval Button ******");
		boolean isSubmitAllFactorsBtnDisplayed = objBppTrnPg.isSubmitAllFactorsForApprovalBtnVisible(180);
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T170: Submit All Factors For Aproval button is visible");
		softAssert.assertTrue(isSubmitAllFactorsBtnDisplayed, "SMAB-T173: Submit All Factors For Aproval button is visible");
		
		//Step31: Navigating to BPP Trend Setup page and again validating status of table 
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step32: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T173: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		
		//Step33: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T173: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}

		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}


	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able to create and update 'Max. Equip. Index Factor' when calculations are not done:: TestCase/JIRA ID: SMAB-T133
	 */
	@Test(description = "SMAB-T135,SMAB-T138: Create, edit and delete BPP Setting when calculations are not done", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_CreateAndEdit_BppSetting_WhenCalculationAreNotDone(String loginUser) throws Exception {		
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Delete the existing BPP composite setting entry for given roll year
		objBppTrnPg.removeExistingBppSettingEntry(rollYear);
		
		//Step3: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
	
		//Step4: Opening the BPP Trend module and set All as the view option in grid and select a BPP trend setup from grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on the BPP Trend Setup name");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		/**Step5: Checking the status of tables on details page
		 * a. Fetch composite factor table names from properties file and collect them in a single list
		 * b. Fetch composite factor table names from properties file and collect them in a single list
		 * c. Iterate over composite factor tables list and validate their status BPP trend setup on details page
		 * d. Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		 */
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String tableName, currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T135: Status of "+ tableName +" on Bpp Trend Setup page ");
		}
		
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrendSetupPage.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T135: Status of "+ tableName +" on Bpp Trend Setup page");
		}
		
		//Step5: Creating a new entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating an entry for Maximum Equipment Index factor");
		objBppTrendSetupPage.createBppSetting("125");
		String popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(20);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T135: New Bpp Setting entry created successfully. Pop up message displayed- "+ popUpMsg);
		objBppTrnPg.closePageLevelMsgPopUp();
		
		//Step6: Edit the newly created entry on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing an entry for Maximum Equipment Index factor");
		objBppTrendSetupPage.editBppSettingValueOnDetailsPage("150");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(20);
		softAssert.assertTrue((popUpMsg.contains("was saved")), "SMAB-T135: Bpp Setting entry edited successfully. Pop up message displayed- "+ popUpMsg);
		
		//Step7: Delete the newly created entry
		//ExtentTestManager.getTest().log(LogStatus.INFO, "Deleting an entry for Maximum Equipment Index factor");
		//objBppTrnPg.deleteBppSettingValueOnDetailsPage();
		//popUpMsg = objBppTrnPg.waitForPopUpMsg(10);
		//softAssert.assertTrue((popUpMsg.contains("was deleted")), "SMAB-T135: Bpp Setting entry deleted successfully. Pop up message displayed- "+ popUpMsg);
		
		//Step8: Re-Creating a new entry
		//ExtentTestManager.getTest().log(LogStatus.INFO, "Re-Creating an entry for Maximum Equipment Index factor");
		//objBppTrnPg.createBppSetting("160");
		//popUpMsg = objBppTrnPg.waitForPopUpMsg(10);
		//softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T135: Bpp Setting entry created successfully. Pop up message displayed- "+ popUpMsg);
		
		//Step9: Retrieving the name of newly created BPP setting entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Retrieving the name of newly created BPP Setting");
		boolean bppSettingNameDisplayed = objBppTrnPg.verifyElementExists(objBppTrnPg.xPathBPPSettingName);
		String bppSettingName = objBppTrnPg.getElementText(objBppTrnPg.bppSettingName);
				
		//Step10: Click ViewAll link to navigate to BPP settings grid and edit existing BPP setting entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on View All link to navigate to grid page");
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.viewAllBppSettings));
		
		//Step11: Retrieving the equipment index factor value before editing
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating the value of maximum equipment factor on view all page");
		String factorValueOnVeiwAllPage = objBppTrendSetupPage.retrieveFactorValueFromGrid(bppSettingName);
		softAssert.assertEquals(factorValueOnVeiwAllPage, "150%", "SMAB-T135: Validating maximum equip. index factor value on view all page");
		softAssert.assertEquals(factorValueOnVeiwAllPage, "150%", "SMAB-T138: Validating maximum equip. index factor value on view all page");		

		//softAssert.assertEquals(factorValueOnVeiwAllPage, "160%", "SMAB-T135: Validating maximum equip. index factor value on view all page");
		//softAssert.assertEquals(factorValueOnVeiwAllPage, "160%", "SMAB-T138: Validating maximum equip. index factor value on view all page");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

}