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
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendSettingTest  extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermit;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	BuildingPermitPage objBuildPermitPage;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
//		if(driver==null) {
//            setupTest();
//            driver = BrowserDriver.getBrowserInstance();
//        }
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objBuildPermitPage = new BuildingPermitPage(driver);
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
	@Test(description = "SMAB-T133,SMAB-T134: Create and edit BPP Setting with invalid and valid max. equip. index factor", groups = {"smoke","regression","bppTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verify_BppTrend_CreateAndEdit_BppSetting(String loginUser) throws Exception {		
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
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
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step6: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step7: Move & click on BPP Setting drop down icon
		Thread.sleep(3000);
		String bppSettingCountBeforeCreatingNewSetting = objBppTrnPg.getCountOfBppSettings();

		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppSetting, 10);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppSetting));

		//Step8: Click on New option to create BPP Setting entry
		objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppSetting, 10);
		objBppTrnPg.Click(objBppTrnPg.newBppTrendSettingLink);
		
		//Step9: Validate error message with factor values less than minimum range
		String expectedErrorMsgOnIncorrectFactorValue;
		String actualErrorMsgOnIncorrectFactorValue;
		List<String> multipleFactorIncorrectVauesList = Arrays.asList(CONFIG.getProperty("FactorValuesLessThanMinRange").split(","));

		for(int i = 0; i < multipleFactorIncorrectVauesList.size(); i++) {
			objBppTrnPg.enterFactorValue(multipleFactorIncorrectVauesList.get(i));
			objBppTrnPg.Click(objBuildPermit.saveButton);
			if(i == 0) {
				String expectedErrorMsgOnFactorValueWithinRangeWithCharacter = CONFIG.getProperty("ErrorMsgOnFactorValueWithinRangeWithCharacter");
				String actualErrorMsgOnFactorValueWithinRangeWithCharacter = objBppTrnPg.errorMsgOnIncorrectFactorValue();
				boolean isErrorMsgDislayed = actualErrorMsgOnFactorValueWithinRangeWithCharacter.equals(expectedErrorMsgOnFactorValueWithinRangeWithCharacter);
				softAssert.assertTrue(isErrorMsgDislayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnFactorValueWithinRangeWithCharacter + "' for factor within range having a charcter value");
			} else {		
				expectedErrorMsgOnIncorrectFactorValue = CONFIG.getProperty("ErrorMsgOnFactorValueLessThanMinRange");
				actualErrorMsgOnIncorrectFactorValue = objBppTrnPg.errorMsgOnIncorrectFactorValue();
				boolean isErrorMsgDislayed = actualErrorMsgOnIncorrectFactorValue.equals(expectedErrorMsgOnIncorrectFactorValue);
				softAssert.assertTrue(isErrorMsgDislayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnIncorrectFactorValue + "' for factor value less than mumimum range");
			}
		}
		
		//Step10: Validate error message with factor values greater than maximum range
		String FactorValueGreaterThanMaxRange = CONFIG.getProperty("FactorValueGreaterThanMaxRange");
		objBppTrnPg.enterFactorValue(FactorValueGreaterThanMaxRange);
		objBppTrnPg.Click(objBuildPermit.saveButton);
		
		expectedErrorMsgOnIncorrectFactorValue = CONFIG.getProperty("ErrorMsgOnFactorValueGreaterThanMaxRange");
		actualErrorMsgOnIncorrectFactorValue = objBppTrnPg.errorMsgOnIncorrectFactorValue();
		boolean isErrorMsgDislayed = actualErrorMsgOnIncorrectFactorValue.contains(expectedErrorMsgOnIncorrectFactorValue);
		softAssert.assertTrue(isErrorMsgDislayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnIncorrectFactorValue + "' for  factor value greater than minimum range");
		
		//Step11: Close the currently opened BPP Setting entry pop up
		objBppTrnPg.Click(objBuildPermit.closeEntryPopUp);
		
		//Step12: Create and Edit BPP Setting entry with factor values within specified range
		List<String> multipleFactorCorrectVauesList = Arrays.asList(CONFIG.getProperty("FactorValuesWithinRange").split(","));
		for(int i = 0; i < multipleFactorCorrectVauesList.size(); i++) {
			if(i == 0) {
				//Creating the BPP setting entry
				objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppSetting, 10);
				objBppTrnPg.clickAction(objBppTrnPg.dropDownIconBppSetting);
				
				objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBppTrendSettingLink, 10);
				objBppTrnPg.clickAction(objBppTrnPg.newBppTrendSettingLink);
				
				objBppTrnPg.enterRollYearInBppSettingDetails(rollYear);	
				objBppTrnPg.enterFactorValue(multipleFactorCorrectVauesList.get(i));
				objBppTrnPg.Click(objBuildPermit.saveButton);
			} else {
				//Retrieving equipment index factor value before performing edit operation
				String factorValueBeforeEdit = objBppTrnPg.retrieveMaxEqipIndexValueFromPopUp();
				factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
				
				//Editing the BPP newly created setting entry
				objBppTrnPg.clickAction(objBppTrnPg.dropDownIconDetailsSection);
				objBppTrnPg.waitForElementToBeVisible(objBuildPermit.editLinkUnderShowMore, 20);
				objBppTrnPg.clickAction(objBuildPermit.editLinkUnderShowMore);
				objBppTrnPg.enterFactorValue(multipleFactorCorrectVauesList.get(i));
				objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermit.saveBtnEditPopUp));
				
				//Retrieving equipment index factor value after performing edit operation
				objBppTrnPg.waitForElementTextToBe(objBppTrnPg.maxEquipIndexValueOnDetailsPage, multipleFactorCorrectVauesList.get(i), 10);
				String factorValueAfterEdit = objBppTrnPg.retrieveMaxEqipIndexValueFromPopUp();
				factorValueAfterEdit = factorValueAfterEdit.substring(0, factorValueAfterEdit.length()-1);
				
				//Validation for checking whether updated values are reflecting or not
				softAssert.assertTrue(!(factorValueAfterEdit.equals(factorValueBeforeEdit)), "SMAB-T133: Maximum equipment index factor successfully updated & reflecting in right panel. Value before edit: "+ factorValueBeforeEdit +" || Value after edit: "+ factorValueAfterEdit);
			}
		}
		
		//Step13: Validating the count of BPP Setting before creating new BPP Setting
		String bppSettingCountAfterCreatingNewSetting = objBppTrnPg.getCountOfBppSettings();
		softAssert.assertTrue(!(bppSettingCountAfterCreatingNewSetting.equals(bppSettingCountBeforeCreatingNewSetting)), "SMAB-T133: BPP trend setting successfully created & reflecting in right panel. Bpp setting count before creating new setting: "+ bppSettingCountBeforeCreatingNewSetting +" || Bpp setting count after creating and editing new setting: "+ bppSettingCountAfterCreatingNewSetting);

		//Step14: Retrieving the name of newly created BPP setting entry
		String bppSettingName = objBppTrnPg.retrieveBppSettingName();
				
		//Step15: Click ViewAll link to navigate to BPP settings grid and edit existing BPP Setting entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.viewAllBppSettings));
		
		//Step16: Retrieving the equipment index factor value before editing
		String factorValueDisplayedBeforeEditing = objBppTrnPg.retrieveMaxEqipIndexValueFromViewAllGrid(bppSettingName);

		//Step17: Editing and updating the equipment index factor value
		String factorValue = bppTrendSettingDataMap.get("Maximum Equipment index Factor");
		objBuildPermit.clickShowMoreLinkOnRecentlyViewedGrid(bppSettingName);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBuildPermit.editLinkUnderShowMore));
		
		int updatedFactorValue = Integer.parseInt(factorValue) + 1;
		objBppTrnPg.enterFactorValue(Integer.toString(updatedFactorValue));
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermit.saveBtnEditPopUp));

		//Step18: Retrieving and validating the equipment index factor value after editing
		objBppTrnPg.waitForElementTextToBe(objBppTrnPg.maxEquipIndexValueOnDetailsPage, Integer.toString(updatedFactorValue), 10);
		String factorValueDisplayedAfterEditing = objBppTrnPg.retrieveMaxEqipIndexValueFromViewAllGrid(bppSettingName);
		softAssert.assertTrue(!(factorValueDisplayedAfterEditing.equals(factorValueDisplayedBeforeEditing)), "SMAB-T133: Validation to check equipment index updated with new value. Factor value in grid before editing: "+ factorValueDisplayedBeforeEditing +" || Factor value in grid after editing: "+ factorValueDisplayedAfterEditing);
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able select and view  input factor tables on BPP Trend Setup page:: SMAB-T229
	 */
	@Test(description = "SMAB-T229: Check for availability of input factor tables on bpp trend roll year screen",  groups = {"regression","bppTrend"}, dataProvider = "loginBusinessAndPrincipalUsers", dataProviderClass = DataProviders.class, priority = 2, enabled = false)
	public void verify_BppTrend_ValidateInputFactorTables_OnDetailsPage(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step4: Clicking on BPP property index factor tab and validating whether its table is visible
		objBppTrnPg.Click(objBppTrnPg.bppPropertyIndexFactorsTab);
		boolean isPropertyIndexFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.bppPropertyIndexFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isPropertyIndexFactorTableVisible, "SMAB-T229: BPP propery index factors table is visible on roll year details page");

		//Step5: Clicking on BPP property good factor tab and validating whether its table is visible
		objBppTrnPg.Click(objBppTrnPg.bppPropertyGoodFactorsTab);
		boolean isPercentGoodsFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.bppPercentGoodFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isPercentGoodsFactorTableVisible, "SMAB-T229: BPP propery good factors table is visible on roll year details page");
		
		//Step6: Clicking more tab & then clicking on BPP valuation factor tab option & validating whether its table is visible
		objBppTrnPg.Click(objBppTrnPg.moreTabLeftSection);
		objBppTrnPg.javascriptClick(objBppTrnPg.dropDownOptionBppImportedValuationFactors);
		boolean isValuationFactorsTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.bppImportedValuationFactorsTableSection, 20).isDisplayed();
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
	@Test(description = "SMAB-T271,SMAB-T272,SMAB-T273: Edit BPP Setting with with different status of tables", groups = {"smoke","regression","bppTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 1, enabled = false)
	public void verify_BppTrend_EditMaxEquipIndex_WithVariousStatusOfTables(String loginUser) throws Exception {
		//Step1: Resetting the composite factor tables status to Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);

		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step3: Opening the BPP Trend Setup module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();

		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		try {
			//Step5: Retrieving equipment index factor value before performing edit operation
			String factorValueBeforeEdit = objBppTrnPg.retrieveMaxEqipIndexValueFromPopUp();
			factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
					
			//Step6: Edit Maximum Equipment Index factor value
			
			int maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) + 1;
			objBppTrnPg.editBppSettingValueOnDetailsPage(Integer.toString(maxEquipIndexNewValue));
			objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
			String saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.locateElement("//span[@class = 'toastMessage slds-text-heading--small forceActionsText']", 10));
			softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T271: Updated value of maximum equip. index factor has been saved");
			
			//Step7: Retrieving equipment index factor value after performing edit operation
			objBppTrnPg.waitForElementTextToBe(objBppTrnPg.maxEquipIndexValueOnDetailsPage, Integer.toString(maxEquipIndexNewValue), 10);
			String factorValueAfterEdit = objBppTrnPg.retrieveMaxEqipIndexValueFromPopUp();
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
			objApasGenericFunctions.selectAllOptionOnGrid();
	
			//Step13: Clicking on the roll year name in grid to navigate to details page of selected roll year
			objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
			
			//Step14: Retrieving equipment index factor value before performing edit operation
			factorValueBeforeEdit = objBppTrnPg.retrieveMaxEqipIndexValueFromPopUp();
			factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
					
			//Step15: Edit Maximum Equipment Index factor value
			maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) + 1;
			objBppTrnPg.editBppSettingValueOnDetailsPage(Integer.toString(maxEquipIndexNewValue));
			objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
			saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.locateElement("//span[@class = 'toastMessage slds-text-heading--small forceActionsText']", 10));
			softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T271: Updated value of maximum equip. index factor has been saved");
			
			//Step16: Retrieving equipment index factor value after performing edit operation
			objBppTrnPg.waitForElementTextToBe(objBppTrnPg.maxEquipIndexValueOnDetailsPage, Integer.toString(maxEquipIndexNewValue), 10);
			factorValueAfterEdit = objBppTrnPg.retrieveMaxEqipIndexValueFromPopUp();
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
			objApasGenericFunctions.selectAllOptionOnGrid();
	
			//Step22: Clicking on the roll year name in grid to navigate to details page of selected roll year
			objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
			
			//Step23: Retrieving equipment index factor value before performing edit operation
			factorValueBeforeEdit = objBppTrnPg.retrieveMaxEqipIndexValueFromPopUp();
			factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
					
			//Step24: Edit Maximum Equipment Index factor value
			maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) + 1;
			objBppTrnPg.editBppSettingValueOnDetailsPage(Integer.toString(maxEquipIndexNewValue));
			objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
			String errorMessage = objBppTrnPg.getElementText(objBppTrnPg.waitForElementToBeVisible(objBuildPermitPage.errorMsgOnTop, 10));
			objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.cancelButton));
			softAssert.assertContains(errorMessage, "Maximum Equipment index Factor is locked for editing for the Roll Year", "SMAB-T271: Updated value of maximum equip. index factor has been saved");
			
		} finally {
			//Step 25: Reverting the changes done to maximum equipment index factor value
			ExtentTestManager.getTest().log(LogStatus.INFO, "Reverting the maximum equipment index factor value");
			objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Calculated", rollYear);
			driver.navigate().refresh();
			objBppTrnPg.editBppSettingValueOnDetailsPage("125");
			objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		}
		
		//Step28: Log out from application
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
	@Test(description = "SMAB-T170,SMAB-T172: Perform calculation & re-calculation for factors tables individually using calculate & recalclate buttons with updating max. equip. index factor", groups = {"regression","bppTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 3, enabled = false)
	public void verify_BppTrend_CalculateAndReCalculate_ByUpdatingMaxEquipIndexFactor(String loginUser) throws Exception {		
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step1: Fetch composite factor table names from properties file and collect them in a single list
		List<String> compositeFactorTablesList = new ArrayList<String>();
		compositeFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("compositeFactorTablesForBppSetupPage").split(",")));

		//Step2: Fetch composite factor table names from properties file and collect them in a single list		
		List<String> valuationFactorTablesList = new ArrayList<String>();
		valuationFactorTablesList.addAll(Arrays.asList(CONFIG.getProperty("valuationFactorTablesForBppSetupPage").split(",")));

		//Step3: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step4: Navigate to BPP Trend Setup page and check status of composite & valuation tables
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step5: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		String tableName, currentStatus;
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
			softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
		}
		
		//Step6: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page before calculation");
		}
		
		//Step7: Opening the BPP Trend module, selecting roll year and clicking select button
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
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
		objBppTrnPg.clickOnTableOnBppTrendPage("Commercial Composite Factors", false);
		boolean isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(60, "Commercial Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Commercial Composite Factors table");

		objBppTrnPg.clickCalculateBtn("Commercial Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		boolean isTableVisible = objBppTrnPg.isTableDataVisible("Commercial Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Commercial Composite Factors table");
		
		boolean isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Commercial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Commercial Composite Factors table");
		
		/**
		 * Step9: Clicking on Industrial Composite Factors table
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating Calculation For INDUSTRIAL COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Industrial Composite Factors", false);
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(60, "Industrial Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Industrial Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Industrial Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Industrial Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Industrial Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Industrial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Industrial Composite Factors table");

		/**
		 * Step10: Clicking on Construction Mobile Equipment Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating Calculation For CONSTRUCTION MOBILE EQUIP COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Construction Mobile Equipment Composite Factors", true);
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(60, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Construction Mobile Equipment Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Construction Mobile Equipment Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		/**
		 * Step11: Clicking on BPP Prop 13 Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating Calculation For BPP PROP 13 Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("BPP Prop 13 Factors", true);
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(60, "BPP Prop 13 Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for BPP Prop 13 Factors table");
		
		objBppTrnPg.clickCalculateBtn("BPP Prop 13 Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("BPP Prop 13 Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for BPP Prop 13 Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for BPP Prop 13 Factors table");

		//Step12: Navigating to BPP Trend Setup page and again validating status of table 
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);

		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		//Step13: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			if(tableName.equals("Ag. Trends Status") || tableName.equals("Ag. Mobile Equipment Trends Status") || tableName.equals("Const. Trends Status")) {
				softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation. Expected Status:: Not Calculated || Actual Status:: "+ currentStatus);
			} else {
				softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			}
		}
		
		//Step14: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page with partial calculation");
		}
		
		//Step15: Updating the maximum equipment index factor value from 125 to 150.
		objBppTrnPg.editBppSettingValueOnDetailsPage("150");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		String saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.locateElement("//span[@class = 'toastMessage slds-text-heading--small forceActionsText']", 10));
		softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T172: Updated value of maximum equip. index factor has been saved");
		
		/**
		 * Step16: Opening the BPP Trend module, selecting roll year and clicking select button 
		 * Clicking on Commercial Composite Factors table
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);

		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For COMMERCIAL COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Commercial Composite Factors", false);
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Commercial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Commercial Composite Factors table");

		objBppTrnPg.clickReCalculateBtn("Commercial Composite Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Commercial Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for Commercial Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Commercial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Commercial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Commercial Composite Factors table");
		
		/**
		 * Step17: Clicking on Industrial Composite Factors table
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For INDUSTRIAL COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Industrial Composite Factors", false);
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Industrial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Industrial Composite Factors table");
		
		objBppTrnPg.clickReCalculateBtn("Industrial Composite Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Industrial Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for Industrial Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Industrial Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Industrial Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Industrial Composite Factors table");

		/**
		 * Step18: Clicking on Construction Mobile Equipment Composite Factors
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For CONSTRUCTION MOBILE EQUIP COMPOSITE Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("Construction Mobile Equipment Composite Factors", true);
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		objBppTrnPg.clickReCalculateBtn("Construction Mobile Equipment Composite Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for Construction Mobile Equipment Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Construction Mobile Equipment Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Construction Mobile Equipment Composite Factors table");
		
		/**
		 * Step19: Clicking on BPP Prop 13 Factors
		 * Validating availability of ReCalculate button
		 * Clicking ReCalculate button
		 * Validating availability of ReCalculate button
		 */
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Initiating ReCalculation For BPP PROP 13 Factors ******");
		objBppTrnPg.clickOnTableOnBppTrendPage("BPP Prop 13 Factors", true);
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for BPP Prop 13 Factors table");
		
		objBppTrnPg.clickReCalculateBtn("BPP Prop 13 Factors");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.confirmBtnInPopUp, 20));
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("BPP Prop 13 Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T173: User successfully triggered calculation for BPP Prop 13 Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "BPP Prop 13 Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for BPP Prop 13 Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for BPP Prop 13 Factors table");

		//Step20: Navigating to BPP Trend Setup page and again validating status of table 
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);

		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		//Step21: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			if(tableName.equals("Ag. Trends Status") || tableName.equals("Ag. Mobile Equipment Trends Status") || tableName.equals("Const. Trends Status")) {
				softAssert.assertEquals(currentStatus, "Not Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page before calculation. Expected Status:: Not Calculated || Actual Status:: "+ currentStatus);	
			} else {
				softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post Re-Calculation");
			}
		}
		
		//Step22: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page with partial Calculation/Re-Calculation");
		}
		
		//Step23: Reverting the maximum equipment index factor value from 150 to 125.
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Reverting the Max. Equip. Index Factor & Calculating Remaining Tables ******");
		objBppTrnPg.editBppSettingValueOnDetailsPage("125");
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		saveConfirmationMsg = objBppTrnPg.getElementText(objBppTrnPg.locateElement("//span[@class = 'toastMessage slds-text-heading--small forceActionsText']", 10));
		softAssert.assertContains(saveConfirmationMsg, "BPP Setting was saved", "SMAB-T173: Updated value of maximum equip. index factor has been saved");
		
		//Step24: Opening the BPP Trend module, selecting roll year and clicking select button
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
		
		/**
		 * Step25: Clicking on Construction Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		objBppTrnPg.clickOnTableOnBppTrendPage("Construction Composite Factors", false);
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(60, "Construction Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T173: Calcuate button is visible for Construction Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Construction Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Construction Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Construction Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Construction Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Construction Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Construction Composite Factors table");
		
		/**
		 * Step26: Clicking on Agricultural Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		objBppTrnPg.clickOnTableOnBppTrendPage("Agricultural Composite Factors", false);
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(60, "Agricultural Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T173: Calcuate button is visible for Agricultural Composite Factors table");
		
		objBppTrnPg.clickCalculateBtn("Agricultural Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Agricultural Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Agricultural Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Agricultural Composite Factors");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T170: ReCalcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T172: ReCalcuate button is visible for Agricultural Composite Factors table");
		softAssert.assertTrue(isReCalculateBtnDisplayed, "SMAB-T173: ReCalcuate button is visible for Agricultural Composite Factors table");
		
		/**
		 * Step27: Clicking on Agricultural Mobile Equipment Composite Factors
		 * Validating availability of Calculate button
		 * Clicking Calculate button
		 * Validating availability of ReCalculate button
		 */
		objBppTrnPg.clickOnTableOnBppTrendPage("Agricultural Mobile Equipment Composite Factors", true);
		isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(60, "Agricultural Mobile Equipment Composite Factors");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T170: Calcuate button is visible for Agricultural Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T172: Calcuate button is visible for Agricultural Mobile Equipment Composite Factors table");
		softAssert.assertTrue(isCalculateBtnDisplayed, "SMAB-T173: Calcuate button is visible for Agricultural Mobile Equipment Composite Factors table");

		objBppTrnPg.clickCalculateBtn("Agricultural Mobile Equipment Composite Factors");
		objBppTrnPg.waitForSuccessPopUpMsgOnCalculateClick(60);
		isTableVisible = objBppTrnPg.isTableDataVisible("Agricultural Mobile Equipment Composite Factors");
		softAssert.assertTrue(isTableVisible, "SMAB-T172: User successfully triggered calculation for Agricultural Mobile Equipment Composite Factors table");
		
		isReCalculateBtnDisplayed = objBppTrnPg.isReCalculateBtnVisible(60, "Agricultural Mobile Equipment Composite Factors");
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
			objBppTrnPg.clickOnTableOnBppTrendPage(valuationFactorTable, true);
			isCalculateBtnDisplayed = objBppTrnPg.isCalculateBtnVisible(10, "valuationFactorTable");
			softAssert.assertTrue(!isCalculateBtnDisplayed, "SMAB-T249: Calcuate button is NOT visible for "+ valuationFactorTable +" table");
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
		objApasGenericFunctions.selectAllOptionOnGrid();
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);

		//Step32: Iterate over composite factor tables list and validate their status BPP trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "****** Validating Status Of Tables On BPP TREND SETUP Screen ******");
		for(int i = 0; i < compositeFactorTablesList.size(); i++) {
			tableName = compositeFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Calculated", "SMAB-T173: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}
		
		//Step33: Iterate over valuation factor tables list and validate their status BPP trend setup on details page
		for(int i = 0; i < valuationFactorTablesList.size(); i++) {
			tableName = valuationFactorTablesList.get(i);
			currentStatus = objBppTrnPg.getTableStatusFromBppTrendSetupDetailsPage(tableName);
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T170: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T172: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
			softAssert.assertEquals(currentStatus, "Yet to submit for Approval", "SMAB-T173: Status of "+ tableName +" on Bpp Trend Setup page post calculation");
		}

		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
}
