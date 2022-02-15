package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.apas.PageObjects.*;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;

public class BPPTrend_Setup_CompositeFactorSetting_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	BppTrendPage objBppTrendPage;
	SoftAssertion softAssert;
	String rollYear;
	String rollYearToUpdate;

	ApasGenericPage objApasGenericPage;
	BppTrendSetupPage objBppTrendSetupPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrendPage = new BppTrendPage(driver);
		softAssert = new SoftAssertion();
		// Executing all the methods for Roll year: 2019
		rollYear = "2019";
		rollYearToUpdate="2022";
		objApasGenericPage = new ApasGenericPage(driver);
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
		objBppTrendSetupPage.updateRollYearStatus("Open", "2019");
	}
	@AfterMethod
	public void afterMethod() throws Exception {
		objBppTrendSetupPage.updateRollYearStatus("Closed", "2019");
	}

	@AfterClass(alwaysRun = true)
	public void afterClass() throws Exception {
		
		//updating the cpi factor and status for 2022 [ roll year used in this class]

		String queryForCpiFactorName = "Select Name FROM CPI_Factor__c Where Roll_Year__c In (Select Id From Roll_Year_Settings__c Where Roll_Year__c = '"+ rollYearToUpdate +"')";
		String previouRollYear=Integer.toString(Integer.parseInt(rollYearToUpdate)-1);
		String queryForCpiFactorNamePreviouYear = "Select Name FROM CPI_Factor__c Where Roll_Year__c In (Select Id From Roll_Year_Settings__c Where Roll_Year__c = '"+ previouRollYear +"')";
		
		String cpifactorName =  new SalesforceAPI().select(queryForCpiFactorName).get("Name").get(0);

		HashMap<String, ArrayList<String>> cpiFactorData = new SalesforceAPI().select("Select Id, Status__c FROM CPI_Factor__c Where Name = '"+ cpifactorName +"'");
		String cpiFactorID = cpiFactorData.get("Id").get(0);
		
		JSONObject jsonForCPIUpdate= objBppTrendSetupPage.getJsonObject();	

		jsonForCPIUpdate.put("Status__c", "Import Approved");
		jsonForCPIUpdate.put("CPI_Factor__c", "1.0200000");
		
		new SalesforceAPI().update("CPI_Factor__c", cpiFactorID, jsonForCPIUpdate);
		
		//updating the cpi factor and status for 2021 [ 2022-1, previous roll year ]
		 cpifactorName = new SalesforceAPI().select(queryForCpiFactorNamePreviouYear).get("Name").get(0);

		 cpiFactorData = new SalesforceAPI().select("Select Id, Status__c FROM CPI_Factor__c Where Name = '"+ cpifactorName +"'");
		 cpiFactorID = cpiFactorData.get("Id").get(0);
		
		 JSONObject jsonForCPIUpdatePreviousRollYear= objBppTrendSetupPage.getJsonObject();	

		 jsonForCPIUpdatePreviousRollYear.put("Status__c", "Import Approved");
		 jsonForCPIUpdatePreviousRollYear.put("CPI_Factor__c", "1.0103600");
		
		new SalesforceAPI().update("CPI_Factor__c", cpiFactorID, jsonForCPIUpdatePreviousRollYear);

	
	}
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able to create 'Minimum Equip. Index Factor':: TestCase/JIRA ID: SMAB-T186
	 * 2. Validating the user is not able to enter invalid value of 'Max. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T188
	 * 3. Validating the user able to update value of 'Minimum. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T185
	 * 4. Validating the user is not able to edit 'Minimum Equip. Index Factor' when calculations are approved:: TestCase/JIRA ID: SMAB-T187
	 */
	@Test(description = "SMAB-T186,SMAB-T188,SMAB-T185,SMAB-T187: Create BPP Composite Factor Setting with invalid and valid min. equip. index factor values", groups={"Smoke","Regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_Create_BppCompositeFactorSetting(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		objBppTrendSetupPage.login(loginUser);
		Thread.sleep(8000);
		objBppTrendSetupPage.closeDefaultOpenTabs();
		
		//Step2: Updating the composite factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYear);

		//Step3: Updating the valuation factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);

		//Step3: Delete the existing BPP composite setting entry for given roll year
		objBppTrendPage.removeExistingBppFactorSettingEntry(rollYear);

		//Step5: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");

		//Step6: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step7: Creating Commercial BPP Composite Factor Settings
		objBppTrendSetupPage.createBppCompositeFactorSetting("Commercial", "10");
		String popUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Commercial type created successfully. Pop up message on entry creation- "+ popUpMsg);

		//Step8: Opening the BPP Trend module and set All as the view option in grid
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step9: Creating Industrial BPP Composite Factor Settings
		objBppTrendSetupPage.createBppCompositeFactorSetting("Industrial", "9");
		popUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Industrial type created successfully. Pop up message on entry creation- "+ popUpMsg);

		//Step10: Opening the BPP Trend module and set All as the view option in grid
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step11: Creating Agricultural BPP Composite Factor Settings
		objBppTrendSetupPage.createBppCompositeFactorSetting("Agricultural", "50");
		popUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Agricultural type created successfully. Pop up message on entry creation- "+ popUpMsg);

		//Step12: Opening the BPP Trend module and set All as the view option in grid
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		
		objBppTrendSetupPage.closeDefaultOpenTabs();
		
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		
		//Step13: Creating Construction BPP Composite Factor Settings with incorrect factor value : -1
		objBppTrendSetupPage.createBppCompositeFactorSetting("Construction", "-1");

		//Step14: Validating the error displayed on entering invalid value
		String actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Minimum Good Factor");
		softAssert.assertContains(actualErrorMsg, "Minimum Good Factor should be greater than 0", "SMAB-T188: Validation on entering factor value less as 0");

		//Step15: Creating Construction BPP Composite Factor Settings with incorrect factor value : 150
		objBppTrendSetupPage.enter("Minimum Good Factor","150");
		objBppTrendSetupPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(1000);

		//Step16: Validating the error displayed on entering invalid value
		actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Minimum Good Factor");
		softAssert.assertContains(actualErrorMsg, "Minimum Good Factor: value outside of valid range on numeric field: 150", "SMAB-T188: Validation on entering factor value less as 150");

		//Step17: Creating Construction BPP Composite Factor Settings
		objBppTrendSetupPage.enter("Minimum Good Factor","10");
		objBppTrendSetupPage.Click(objPage.getButtonWithText("Save"));
		popUpMsg = objBppTrendSetupPage.getSuccessMsgText();
		softAssert.assertTrue((popUpMsg.contains("was created")), "SMAB-T186: Bpp Composite Setting entry for Construction type created successfully. Pop up message on entry creation- "+ popUpMsg);

		//Step18: Opening the BPP Trend module and set All as the view option in grid
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

		//Step19: Retrieving the minimum equipment index factor value for Agricultural before editing
		if(objBppTrendSetupPage.waitForElementToBeClickable(10,objBppTrendSetupPage.moreTabCompositeFactorSettings) != null) {
			objPage.Click(driver.findElement(By.xpath(objBppTrendSetupPage.moreTabCompositeFactorSettings)));
			objPage.Click(objBppTrendSetupPage.bppCompositeFactorOption);
		}
		objBppTrendPage.scrollToElement(objBppTrendSetupPage.viewAllBppCompositeFactorSettings);
		objBppTrendPage.javascriptClick(objBppTrendSetupPage.viewAllBppCompositeFactorSettings);
		String factorValueBeforeEditing = objBppTrendSetupPage.retrieveExistingMinEquipFactorValueFromGrid("Agricultural");

		//Step20: Editing and updating the equipment index factor value
		objBppTrendSetupPage.clickOnShowMoreLinkInGridForGivenPropertyType("Agricultural");
		objBppTrendPage.javascriptClick(objBppTrendSetupPage.editLinkUnderShowMore);
		objBppTrendSetupPage.enter("Minimum Good Factor","11");
		objApasGenericPage.selectOptionFromDropDown("Property Type","Agricultural");
		objBppTrendPage.Click(objBppTrendSetupPage.saveBtnInBppSettingPopUp);
		Thread.sleep(2000);

		//Step21: Retrieving and validating the equipment index factor value after editing
		String factorValueAfterEditing = objBppTrendSetupPage.retrieveExistingMinEquipFactorValueFromGrid("Agricultural");
		softAssert.assertTrue(!(factorValueAfterEditing.equals(factorValueBeforeEditing)), "SMAB-T185: Validation to check mimimum equip index has updated with new value. Value before editing: "+ factorValueBeforeEditing +" || Value after editing: "+ factorValueAfterEditing);

		//Step22: Updating the composite factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Approved", rollYear);

		//Step23: Updating the valuation factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Approved", rollYear);

		//Step24: Editing and updating the equipment index factor value
		ReportLogger.INFO("Clicking show more drop down and clicking edit button");
		objBppTrendSetupPage.clickOnShowMoreLinkInGridForGivenPropertyType("Agricultural");
		objBppTrendPage.javascriptClick(objBppTrendSetupPage.editLinkUnderShowMore);

		//Step21: Validating error message on updating Min. Equipment Index Factor value for approved tables
		objBppTrendSetupPage.enter("Minimum Good Factor","12");
		objBppTrendPage.Click(objBppTrendSetupPage.saveBtnInBppSettingPopUp);
		Thread.sleep(5000);
		ReportLogger.INFO("Validating error message on updating Min. Equipment Index Factor Value for approved tables");

		//actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Minimum Good Factor");
		//softAssert.assertTrue(actualErrorMsg, "Minimum Good Factor is locked for editing for the Roll Year", "SMAB-T187: Validating error message on editing minimum equip. index value when calculations are approved");
		softAssert.assertTrue(objBppTrendSetupPage.waitForElementToBeVisible(10,objApasGenericPage.pageError), "SMAB-T187: Validating error message on editing minimum equip. index value when calculations are approved");
		objBppTrendSetupPage.logout();
	}
}