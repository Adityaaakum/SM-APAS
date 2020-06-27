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
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_Setup_CompositeFactorSetting_Test extends TestBase {
	
	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermit;
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
		objBuildPermit = new BuildingPermitPage(driver);
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
	 * 1. Validating the user is able to create 'Minimum Equip. Index Factor':: TestCase/JIRA ID: SMAB-T186
	 * 2. Validating the user is not able to enter invalid value of 'Max. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T188
	 * 3. Validating the user able to update value of 'Minimum. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T185
	 */
	@Test(description = "SMAB-T186,SMAB-T188,SMAB-T185: Create BPP Composite Factor Setting with invalid and valid min. equip. index factor values", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_Create_BppCompositeFactorSetting(String loginUser) throws Exception {		
		//Step1: Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step3: Delete the existing BPP composite setting entry for given roll year
		objBppTrnPg.removeExistingBppFactorSettingEntry(rollYear);
		
		//Step4: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
	
		//Step5: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		
		//Step6: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Step7: Clicking on BPP Composite Factor Settings tab
		objBppTrendSetupPage.createBppCompositeFactorSetting("Commercial", "10");
		
		//Step8: Clicking view all button to navigate to grid page
		objBppTrnPg.clickAction(objBppTrendSetupPage.viewAllBppCompositeFactorSettings);
		
		//Step9: Clicking New button to create Industrial Settings on grid page
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);

		objBppTrendSetupPage.enterFactorValue("9");
		objBppTrendSetupPage.enterPropertyType("Industrial");
		objBppTrnPg.Click(objBppTrendSetupPage.saveBtnInBppSettingPopUp);
		String popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Industrial type created successfully. Pop up message on entry creation- "+ popUpMsg);
		Thread.sleep(1000);
		
		//Step10: Clicking New button to create Agricultural Settings on grid page
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);

		objBppTrendSetupPage.enterFactorValue("50");
		objBppTrendSetupPage.enterPropertyType("Agricultural");
		objBppTrnPg.Click(objBppTrendSetupPage.saveBtnInBppSettingPopUp);
		popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Agricultural type created successfully. Pop up message on entry creation- "+ popUpMsg);
		Thread.sleep(1000);
		
		//Step11: Clicking New button to create Construction Settings on grid page
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);
		
		//Step12: Entering property type, an invalid factor value and clicking save button 
		objBppTrendSetupPage.enterFactorValue("-1");
		objBppTrendSetupPage.enterPropertyType("Construction");
		objBppTrnPg.Click(objBppTrendSetupPage.saveBtnInBppSettingPopUp);
		
		//Step13: Validating the error displayed on entering invalid value and closing the pop up
		String actualErrorMsg = objBppTrendSetupPage.errorMsgUnderMinEquipFactorIndexField();
		softAssert.assertEquals(actualErrorMsg, "Minimum Good Factor should be greater than 0", "SMAB-T188: Validation on entering factor value less as 0");
		objBppTrnPg.Click(objBppTrendSetupPage.cancelBtnInBppSettingPopUp);
		Thread.sleep(1000);
		
		//Step14: Clicking New button and entering invalid factor value and clicking save button
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);

		objBppTrendSetupPage.enterFactorValue("150");
		objBppTrendSetupPage.enterPropertyType("Construction");
		objBppTrnPg.Click(objBppTrendSetupPage.saveBtnInBppSettingPopUp);
		
		//Step15: Validating the error displayed on entering invalid value and closing the pop up
		actualErrorMsg = objBppTrendSetupPage.errorMsgUnderMinEquipFactorIndexField();
		softAssert.assertEquals(actualErrorMsg, "Minimum Good Factor: value outside of valid range on numeric field: 150", "SMAB-T188: Validation on entering factor value less as 150");		
		objBppTrnPg.Click(objBppTrendSetupPage.cancelBtnInBppSettingPopUp);
		Thread.sleep(1000);
		
		//Step16: Clicking New button and entering a valid value within range, clicking save button and validation pop up message
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);

		objBppTrendSetupPage.enterFactorValue("10");
		objBppTrendSetupPage.enterPropertyType("Construction");
		objBppTrnPg.Click(objBppTrendSetupPage.saveBtnInBppSettingPopUp);
		popUpMsg = objBppTrendSetupPage.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Construction type created successfully. Pop up message on entry creation- "+ popUpMsg);
		
		//Step17: Retrieving the minimum equipment index factor value before editing
		String factorValueBeforeEditing = objBppTrendSetupPage.retrieveExistingMinEquipFactorValueFromGrid("Agricultural");

		//Step17: Editing and updating the equipment index factor value
		objBppTrendSetupPage.clickOnShowMoreLinkInGridForGivenPropertyType("Agricultural");
		objBppTrnPg.clickAction(objBuildPermitPage.editLinkUnderShowMore);

		objBppTrendSetupPage.enterFactorValue("11");
		objBppTrendSetupPage.enterPropertyType("Agricultural");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		Thread.sleep(1000);
		
		//Step18: Retrieving and validating the equipment index factor value after editing
		String factorValueAfterEditing = objBppTrendSetupPage.retrieveExistingMinEquipFactorValueFromGrid("Agricultural");
		softAssert.assertTrue(!(factorValueAfterEditing.equals(factorValueBeforeEditing)), "SMAB-T185: Validation to check mimimum equip index has updated with new value. Value before editing: "+ factorValueBeforeEditing +" || Value after editing: "+ factorValueAfterEditing);
				
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is not able to edit 'Minimum Equip. Index Factor' when calculations are approved:: TestCase/JIRA ID: SMAB-T187
	 */
	@Test(description = "SMAB-T187: Edit BPP Setting when table calculcations for tables are approved", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_CreatAndEdit_BppSetting_WhenCalculationAreAproved(String loginUser) throws Exception {				
		//Step1: Delete the existing BPP composite setting entry for given roll year
		objBppTrnPg.removeExistingBppFactorSettingEntry(rollYear);
		
		//Step2: Updating tables status as Approved
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);
		
		//Step3: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationTablesToResetViaApi").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
		
		//Step4: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
	
		//Step5: Opening the BPP Trend module and set All as the view option in grid
		ExtentTestManager.getTest().log(LogStatus.INFO, "Navigating to BPP Trend Setup page");
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		
		//Step6: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicked on the BPP Trend Setup name in the grid");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Step7: Clicking on BPP Composite Factor Settings tab
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating a new Composite factor entry of 'COMMERCIAL' type");
		objBppTrendSetupPage.createBppCompositeFactorSetting("Commercial", "10");
		
		//Step8: Clicking view all button to navigate to grid page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking View All button and navigating to grid view page");
		objBppTrnPg.clickAction(objBppTrendSetupPage.viewAllBppCompositeFactorSettings);
		
		//Step9: Editing and updating the equipment index factor value
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking show more drop down and clicking edit option");
		objBppTrendSetupPage.clickOnShowMoreLinkInGridForGivenPropertyType("Commercial");
		objBppTrnPg.clickAction(objBuildPermitPage.editLinkUnderShowMore);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Updating new value and clicking save button");
		objBppTrendSetupPage.enterFactorValue("25");
		objBppTrendSetupPage.enterPropertyType("Commercial");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating error message for updating values for approved tables");
		String expectedErrorMessage = "Minimum Good Factor is locked for editing for the Roll Year";
		String actualErrorMessage = objBppTrendSetupPage.errorMsgUnderMinEquipFactorIndexField();
		softAssert.assertEquals(actualErrorMessage, expectedErrorMessage, "SMAB-T187: Validating error message on editing minimum equip. index value when calculations are approved");
		objBppTrnPg.Click(objBppTrendSetupPage.cancelBtnInBppSettingPopUp);
		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
}