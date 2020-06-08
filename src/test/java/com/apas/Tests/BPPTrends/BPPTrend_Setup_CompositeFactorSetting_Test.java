package com.apas.Tests.BPPTrends;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
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
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Not Calculated", rollYear);

		//Step2: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Yet to submit for Approval", rollYear);
		
		//Step3: Delete the existing BPP composite setting entry for given roll year
		objBppTrnPg.removeExistingBppFactorSettingEntry(rollYear);
		
		//Step4: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
	
		//Step5: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step6: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrnPg.clickOnEntryNameInGrid(rollYear);
		
		//Step7: Clicking on BPP Composite Factor Settings tab
		objBppTrnPg.createBppCompositeFactorSetting("Commercial", "10");
		
		//Step8: Clicking view all button to navigate to grid page
		objBppTrnPg.clickAction(objBppTrnPg.viewAllBppCompositeFactorSettings);
		
		//Step9: Clicking New button to create Industrial Settings on grid page
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);
		
		objBppTrnPg.enterFactorValue("9");
		objBppTrnPg.enterPropertyType("Industrial");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		String popUpMsg = objBppTrnPg.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Industrial type created successfully. Pop up message on entry creation- "+ popUpMsg);
		Thread.sleep(1000);
		
		//Step10: Clicking New button to create Agricultural Settings on grid page
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);
		
		objBppTrnPg.enterFactorValue("50");
		objBppTrnPg.enterPropertyType("Agricultural");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		popUpMsg = objBppTrnPg.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Agricultural type created successfully. Pop up message on entry creation- "+ popUpMsg);
		Thread.sleep(1000);
		
		//Step11: Clicking New button to create Construction Settings on grid page
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);
		
		//Step12: Entering property type, an invalid factor value and clicking save button 
		objBppTrnPg.enterFactorValue("-1");
		objBppTrnPg.enterPropertyType("Construction");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		
		//Step13: Validating the error displayed on entering invalid value and closing the pop up
		String actualErrorMsg = objBppTrnPg.errorMsgUnderMinEquipFactorIndexField();
		softAssert.assertEquals(actualErrorMsg, "Minimum Good Factor should be greater than 0", "SMAB-T188: Validation on entering factor value less as 0");
		objBppTrnPg.Click(objBppTrnPg.cancelBtnInBppSettingPopUp);
		Thread.sleep(1000);
		
		//Step14: Clicking New button and entering invalid factor value and clicking save button
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);
		
		objBppTrnPg.enterFactorValue("150");
		objBppTrnPg.enterPropertyType("Construction");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		
		//Step15: Validating the error displayed on entering invalid value and closing the pop up
		actualErrorMsg = objBppTrnPg.errorMsgUnderMinEquipFactorIndexField();
		softAssert.assertEquals(actualErrorMsg, "Minimum Good Factor: value outside of valid range on numeric field: 150", "SMAB-T188: Validation on entering factor value less as 150");		
		objBppTrnPg.Click(objBppTrnPg.cancelBtnInBppSettingPopUp);
		Thread.sleep(1000);
		
		//Step16: Clicking New button and entering a valid value within range, clicking save button and validation pop up message
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.newBtnViewAllPage, 10);
		objBppTrnPg.clickAction(objBppTrnPg.newBtnViewAllPage);
		
		objBppTrnPg.enterFactorValue("10");
		objBppTrnPg.enterPropertyType("Construction");
		objBppTrnPg.Click(objBppTrnPg.saveBtnInBppSettingPopUp);
		popUpMsg = objBppTrnPg.waitForPopUpMsg(10);
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Construction type created successfully. Pop up message on entry creation- "+ popUpMsg);
		
		//Step17: Retrieving the minimum equipment index factor value before editing
		String factorValueBeforeEditing = objBppTrnPg.retrieveExistingMinEquipFactorValueFromGridForGivenProprtyType("Agricultural");

		//Step17: Editing and updating the equipment index factor value
		objBppTrnPg.clickOnShowMoreLinkInGridForGivenProprtyType("Agricultural");
		objBppTrnPg.clickAction(objBuildPermitPage.editLinkUnderShowMore);
		
		objBppTrnPg.enterFactorValue("11");
		objBppTrnPg.enterPropertyType("Agricultural");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		Thread.sleep(1000);
		
		//Step18: Retrieving and validating the equipment index factor value after editing
		String factorValueAfterEditing = objBppTrnPg.retrieveExistingMinEquipFactorValueFromGridForGivenProprtyType("Agricultural");
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
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);
		
		//Step3: Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
		
		//Step4: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
	
		//Step5: Opening the BPP Trend module and set All as the view option in grid
		ExtentTestManager.getTest().log(LogStatus.INFO, "Navigating to BPP Trend Setup page");
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step6: Clicking on the roll year name in grid to navigate to details page of selected roll year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicked on the BPP Trend Setup name in the grid");
		objBppTrnPg.clickOnEntryNameInGrid(rollYear);
		
		//Step7: Clicking on BPP Composite Factor Settings tab
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating a new Composite factor entry of 'COMMERCIAL' type");
		objBppTrnPg.createBppCompositeFactorSetting("Commercial", "10");
		
		//Step8: Clicking view all button to navigate to grid page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking View All button and navigating to grid view page");
		objBppTrnPg.clickAction(objBppTrnPg.viewAllBppCompositeFactorSettings);
		
		//Step9: Editing and updating the equipment index factor value
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking show more drop down and clicking edit option");
		objBppTrnPg.clickOnShowMoreLinkInGridForGivenProprtyType("Commercial");
		objBppTrnPg.clickAction(objBuildPermitPage.editLinkUnderShowMore);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Updating new value and clicking save button");
		objBppTrnPg.enterFactorValue("25");
		objBppTrnPg.enterPropertyType("Commercial");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating error message for updating values for approved tables");
		String expectedErrorMessage = "Minimum Good Factor is locked for editing for the Roll Year";
		String actualErrorMessage = objBppTrnPg.errorMsgUnderMinEquipFactorIndexField();
		softAssert.assertEquals(actualErrorMessage, expectedErrorMessage, "SMAB-T187: Validating error message on editing minimum equip. index value when calculations are approved");
		objBppTrnPg.Click(objBppTrnPg.cancelBtnInBppSettingPopUp);
		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
}