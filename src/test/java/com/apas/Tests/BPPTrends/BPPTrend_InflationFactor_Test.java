package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.HashMap;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;


public class BPPTrend_InflationFactor_Test extends TestBase {
	
	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrendPage;
	BuildingPermitPage objBuildPermit;
	Util objUtil;
	SoftAssertion softAssert;
	ApasGenericPage objApasGenericPage;
	BppTrendSetupPage objBppTrendSetupPage;
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBppTrendPage = new BppTrendPage(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		objApasGenericPage = new ApasGenericPage(driver);
		objBppTrendSetupPage = new BppTrendSetupPage (driver);
		objApasGenericFunctions.updateRollYearStatus("Open", "2020");
	}
	
//	/**
//	 * DESCRIPTION: Performing Following Validations::
//	 * 1. Validating that 'Inflation Factor' is >= 0 and <=1.02:: TestCase/JIRA ID: SMAB-T182
//	 * 2. Validate that user is able to create a new Inflation Factors for a year with valid data:: TestCase/JIRA ID: SMAB-T180
//	 */
//	@Test(description = "SMAB-T180,SMAB-T182: Create new CPI Factor", groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
//	public void BppTrend_Create_CpiFactors(String loginUser) throws Exception {
//		String rollYear = "2019";
//		//Step1: Login to the APAS application using the given user
//		objApasGenericFunctions.login(loginUser);
//		
//		//Step2: Opening the BPP Trend module and set All as the view option in grid
//		objApasGenericFunctions.searchModule(modules.CPI_FACTORS);
//		objApasGenericFunctions.displayRecords("All");
//	
//		//Step3: Create CPI Factor with value : -1 
//		objBppTrendPage.Click(objBppTrendSetupPage.newBtnToCreateEntry);
//		objApasGenericPage.searchAndSelectOptionFromDropDown(objBppTrendPage.rollYearForCpiFactor, rollYear);
//		objBppTrendPage.enter(objBppTrendPage.cpiFactorInputBox, "-1");
//		objBppTrendPage.Click(objBppTrendSetupPage.saveButton);
//		
//		//Step4: Validating the error message on providing invalid CPI factor value and canceling the pop up window
//		String expErrorMsgForLessThanMinValue = "CPI Factor could not be less than 0";
//		objBppTrendPage.waitForElementToBeVisible(objBppTrendPage.errorMsgForInvalidCpiFactorValue, 10);
//		String actErrorMsgForLessThanMinValue = objBppTrendPage.getElementText(objBppTrendPage.errorMsgForInvalidCpiFactorValue);
//		softAssert.assertContains(actErrorMsgForLessThanMinValue, expErrorMsgForLessThanMinValue, "SMAB-T182: Validation for CPI Factor value less than minimum range");		
//		objBppTrendPage.Click(objBppTrendSetupPage.cancelBtnInBppSettingPopUp);
//		
//		//Step5: Create CPI Factor with value : 1.22
//		objBppTrendPage.Click(objBppTrendSetupPage.newBtnToCreateEntry);
//		objApasGenericPage.searchAndSelectOptionFromDropDown(objBppTrendPage.rollYearForCpiFactor, rollYear);		
//		objBppTrendPage.enter(objBppTrendPage.cpiFactorInputBox, "1.22000");
//		objBppTrendPage.Click(objBppTrendSetupPage.saveButton);
//		
//		//Step6: Validating the error message on providing invalid CPI factor value and canceling the pop up
//		String expErrorMsgForMoreThanMaxValue = "CPI Factor cannot be more than 1.02";
//		objBppTrendPage.waitForElementToBeVisible(objBppTrendPage.errorMsgForInvalidCpiFactorValue, 10);
//		String actErrorMsgForMoreThanMaxValue = objBppTrendPage.getElementText(objBppTrendPage.errorMsgForInvalidCpiFactorValue);
//		softAssert.assertContains(actErrorMsgForMoreThanMaxValue, expErrorMsgForMoreThanMaxValue, "SMAB-T182: Validation for CPI Factor value more than maximum range");
//		objBppTrendPage.Click(objBppTrendSetupPage.cancelBtnInBppSettingPopUp);
//		
//
//		//Step7: Create CPI Factor with valid value : 1.02 and verify success message
//		objBppTrendPage.Click(objBppTrendSetupPage.newBtnToCreateEntry);		
//		objApasGenericPage.searchAndSelectOptionFromDropDown(objBppTrendPage.rollYearForCpiFactor, rollYear);
//		objBppTrendPage.enter(objBppTrendPage.cpiFactorInputBox, "1.02");
//		objBppTrendPage.Click(objBppTrendSetupPage.saveButton);
//		String actMsgInPopUpOnSave = objBppTrendSetupPage.getSuccessMsgText();
//		boolean isRecordCreated = actMsgInPopUpOnSave.contains("was created");
//		softAssert.assertTrue(isRecordCreated, "SMAB-T180: Validationg message on successfully creating CPI Factor entry. "+ actMsgInPopUpOnSave);
//		
//		//Step8: Deleting Duplicate CPI record Create
//		objBppTrendPage.deleteDuplicateCPI(rollYear);
//		
//		objApasGenericFunctions.logout();
//	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating business administrator is not able to edit approved inflation factor:: TestCase/JIRA ID: SMAB-T183
	 */
	@Test(description = "SMAB-T183: Validating business administrator user is not able to edit the approved inflation factor", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_EditApprovedCpiFactor(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.CPI_FACTORS);
		objApasGenericFunctions.displayRecords("All");	

		//Step3: Retrieving CPI Factor to edit using roll year fetched in above step
		String queryForCpiFactorID = "Select Name FROM CPI_Factor__c Where Status__c = 'Approved'";
		HashMap<String, ArrayList<String>> cpiFactorData = new SalesforceAPI().select(queryForCpiFactorID);
		String cpiFactorName = cpiFactorData.get("Name").get(0);	
		
		//Step4: Click the show more icon and clicking on edit button on grid
		objApasGenericFunctions.searchRecords(cpiFactorName);
		objApasGenericFunctions.clickShowMoreLink(cpiFactorName);
		objBppTrendPage.clickAction(objBppTrendSetupPage.editLinkUnderShowMore);

		//Step5: Validating the error message on clicking edit button
		objBppTrendSetupPage.enter("CPI Factor", "0.01");
		objPage.Click(objPage.getButtonWithText("Save"));
		String actualErrorMsg = objPage.getElementText(objBuildPermit.pageError);
		//String expectedErrorMsg = "CPI Factors can't be updated once Submitted for Approval or Approved";
		String expectedErrorMsg ="insufficient access rights on object id";
		softAssert.assertContains(actualErrorMsg, expectedErrorMsg, "SMAB-T183: Validation for business admin is not able to edit aproved CPI Factor from grid");
		objPage.Click(objPage.getButtonWithText("Cancel"));

		//Step6: Clicking on the CPI Factor name to navigate To details page
		objBppTrendSetupPage.clickOnEntryNameInGrid(cpiFactorName);
		//objBppTrnPg.waitForElementToBeClickable(objBppTrendSetupPage.editButton, 10);

		//Step7: Clicking edit button on details page
		objBppTrendPage.Click(objBppTrendSetupPage.editButton);

		//Step8: Validating the error message on clicking edit button
		objBppTrendSetupPage.enter("CPI Factor", "0.02");
		objPage.Click(objPage.getButtonWithText("Save"));
		actualErrorMsg = objPage.getElementText(objBuildPermit.pageError);
		expectedErrorMsg = "insufficient access rights on object id";
		softAssert.assertContains(actualErrorMsg, expectedErrorMsg, "SMAB-T183: Validation for business admin is not able to edit aproved CPI Factor from grid");
		objPage.Click(objPage.getButtonWithText("Cancel"));

		objApasGenericFunctions.logout();
	}

}