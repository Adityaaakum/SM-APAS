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
import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;

public class BPPTrend_BPPSettings_Test extends TestBase{
	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrendPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	ApasGenericPage objApasGenericPage;
	BppTrendSetupPage objBppTrendSetupPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrendPage = new BppTrendPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		// Executing all the methods for Roll year: 2019
		rollYear = "2019";
		objApasGenericPage = new ApasGenericPage(driver);
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
		objApasGenericFunctions.updateRollYearStatus("Open", "2019");
	}
	@AfterMethod
	public void afterMethod() throws Exception {
		objApasGenericFunctions.updateRollYearStatus("Closed", "2019");
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able to update and enter the 'Max. Equip. Index Factor' for previous Roll Year:: TestCase/JIRA ID: SMAB-T133
	 * 2. Validating the user is not able to enter invalid value of 'Max. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T134
	 * 3. Validating the user is able to enter the 'Max. Equip. Index Factor' when table status is 'Not Calculated': TestCase/JIRA ID: SMAB-T135
	 */
	@Test(description = "SMAB-T133,SMAB-T134,SMAB-T135: Create and edit BPP Setting with invalid and valid max. equip. index factor", groups={"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_Verify_BPPSettings_CreationAndValidations(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Updating the composite factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);
	
		//Step3: Updating the valuation factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);
				
		//Step8: Delete the existing BPP composite setting entry for given roll year
		objBppTrendPage.removeExistingBppSettingEntry(rollYear);
		
		//Step4: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		
		//Step5: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Step6: Click on New option to create BPP Setting entry
		objPage.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconBppSetting, 20);
		objPage.clickAction(objBppTrendSetupPage.dropDownIconBppSetting);
		objPage.clickAction(objBppTrendPage.waitForElementToBeClickable(objBppTrendSetupPage.newBtnToCreateEntry));
		
		//Step7: Clear the Default Max Equipment index factor value and verify the error message
		String expectedErrorMessage = "These required fields must be completed: Maximum Equipment index Factor";
		objBppTrendSetupPage.enterFactorValue("");
		objPage.Click(objBppTrendSetupPage.saveButton);
		String actualErrorMsg = objPage.getElementText(objBppTrendSetupPage.errorMsgOnTop);
		softAssert.assertEquals(actualErrorMsg,expectedErrorMessage,"SMAB-T134: Verify error message when 'Maximum Equipment index Factor' is missing");
		
		//Step8: Validate error message with factor values less than minimum range : 100b
		String expectedErrorMsg = "Maximum Equipment index Factor: value outside of valid range on numeric field: 100000000000";
		objBppTrendSetupPage.enterFactorValue("100b");
		objPage.Click(objBppTrendSetupPage.saveButton);
		actualErrorMsg = objBppTrendSetupPage.errorMsgOnIncorrectFactorValue();
		softAssert.assertEquals(actualErrorMsg,expectedErrorMsg,"SMAB-T134: Verify Error message for factor value: 100b");
		
		//Step9: Validate error message with factor values less than minimum range : 100b
		expectedErrorMsg = "Maximum Equipment Factor should be greater than or equal to 100";
		objBppTrendSetupPage.enterFactorValue("60");
		objPage.Click(objBppTrendSetupPage.saveButton);
		actualErrorMsg = objBppTrendSetupPage.errorMsgOnIncorrectFactorValue();
		softAssert.assertEquals(actualErrorMsg,expectedErrorMsg,"SMAB-T134: Verify Error message for factor value: 60");
				
		//Step10: Validate error message with factor values less than minimum range : 99.4
		expectedErrorMsg = "Maximum Equipment Factor should be greater than or equal to 100";
		objBppTrendSetupPage.enterFactorValue("60");
		objPage.Click(objBppTrendSetupPage.saveButton);
		actualErrorMsg = objBppTrendSetupPage.errorMsgOnIncorrectFactorValue();
		softAssert.assertEquals(actualErrorMsg,expectedErrorMsg,"SMAB-T134: Verify Error message for factor value: 99.4");
		
		//Step11: Validate error message with factor values greater than maximum range	
		expectedErrorMsg = "Maximum Equipment index Factor: value outside of valid range on numeric field";
		objBppTrendSetupPage.enterFactorValue("1000");
		objPage.Click(objBppTrendSetupPage.saveButton);
		actualErrorMsg = objBppTrendSetupPage.errorMsgOnIncorrectFactorValue();
		softAssert.assertEquals(actualErrorMsg,expectedErrorMsg+": 1000","SMAB-T134: Verify Error message for factor value: 1000");
		
		//Step12: Validate error message with factor values within specified range : 99.5
		objBppTrendSetupPage.enterFactorValue("99.5");
		objPage.Click(objBppTrendSetupPage.saveButton);
		Thread.sleep(2000);
		String factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		String actualValue = factorValueSaved.substring(0, factorValueSaved.length()-1);			
		String expectedValue = String.valueOf(Math.round(Float.parseFloat("99.5")));			
		softAssert.assertEquals(actualValue,expectedValue,"SMAB-T134: Verify entered value: 99.5 is saved as "+expectedValue);
		
		//Step13: Validate error message with factor values within specified range : 124.4
		objPage.clickAction(objBppTrendSetupPage.dropDownIconDetailsSection);
		Thread.sleep(1000);
		objPage.clickAction(objBppTrendSetupPage.editLinkUnderShowMore);
		objBppTrendSetupPage.enterFactorValue("124.4");
		objPage.Click(objBppTrendSetupPage.saveButton);
		Thread.sleep(2000);
		factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		actualValue = factorValueSaved.substring(0, factorValueSaved.length()-1);			
		expectedValue = String.valueOf(Math.round(Float.parseFloat("124.4")));			
		softAssert.assertEquals(actualValue,expectedValue,"SMAB-T134: Verify entered value: 124.4 is saved as "+expectedValue);
		
		//Step14: Validate error message with factor values within specified range : 160
		objPage.clickAction(objBppTrendSetupPage.dropDownIconDetailsSection);
		Thread.sleep(1000);
		objPage.clickAction(objBppTrendSetupPage.editLinkUnderShowMore);
		objBppTrendSetupPage.enterFactorValue("160");
		objPage.Click(objBppTrendSetupPage.saveButton);
		Thread.sleep(2000);
		factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		actualValue = factorValueSaved.substring(0, factorValueSaved.length()-1);			
		expectedValue = String.valueOf(Math.round(Float.parseFloat("160")));			
		softAssert.assertEquals(actualValue,expectedValue,"SMAB-T134: Verify entered value: 124.4 is saved as "+expectedValue);
		
		//Step15: Updating the default value : 125
		objPage.clickAction(objBppTrendSetupPage.dropDownIconDetailsSection);
		Thread.sleep(1000);
		objPage.clickAction(objBppTrendSetupPage.editLinkUnderShowMore);
		objBppTrendSetupPage.enterFactorValue("125");
		objPage.Click(objBppTrendSetupPage.saveButton);
		Thread.sleep(2000);
		factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();	
		softAssert.assertEquals(factorValueSaved,"125%","SMAB-T133,SMAB-T135: Verify user is able to edit the Max Equipemnt Index factor value when status of tables is 'Not Calculated'");
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is unable to update 'Max. Equip. Index Factor' when calculations are either submitted for approval or approved:: TestCase/JIRA ID: SMAB-T137, SMAB-T273, SMAB-T274
	 */
	@Test(description = "SMAB-T137,SMAB-T273,SMAB-T274: Edit BPP Setting when table calculations are approved", groups={"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_Verify_CreateEditBppSetting_AfterSubmittedAndApprovalOfTableCalculations(String loginUser) throws Exception {		
		
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		
		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		// Create Maximum Equipment index Factor for given roll year if it does not exist
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);		
		objBppTrendSetupPage.createMaxEquip(rollYear);
		
		//Step4: Verifying the 'Edit' BPP Settings access when status is "Submitted for Approval" and "Approved"
		String[] tableStatus  = {"Submitted for Approval","Approved"};
		for(String status: tableStatus) {
			//Step5: Updating the composite factor tables status
			objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, status, rollYear);
	
			//Step6: Updating the valuation factor tables status
			objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, status, rollYear);
			
			//Step7: Edit the newly created entry on details page and save the factor value	
			objBppTrendSetupPage.editSaveFactorValue("150");
	
			String expectedErrorMessage = "Maximum Equipment index Factor is locked for editing for the Roll Year";
			String errorMsgOnClickingSaveBtn = objBppTrendPage.getElementText(objBppTrendSetupPage.errorMsgOnTop);
			
			//Step8: Selecting Test Case Id to Map based on status: 'Submitted For approval'/'Approved'
			String TCMapingID;
			if(status.equalsIgnoreCase("Submitted for Approval")) 
				TCMapingID = "SMAB-T137,SMAB-T273";
			else
				TCMapingID = "SMAB-T274";
			
			softAssert.assertEquals(errorMsgOnClickingSaveBtn, expectedErrorMessage, TCMapingID+": Validating error message on editing and saving max. equip. index value when calculations are :"+status);
			objPage.Click(objBppTrendSetupPage.closeEntryPopUp);
		}
		objApasGenericFunctions.logout();
	}
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is able to update 'Max. Equip. Index Factor' when status of Tables is 'Calculated':: TestCase/JIRA ID: SMAB-T271
	 * 2. Status of Tables is updated to 'Needs Recalculation' when 'Max. Equip. Index Factor' is updated:: TestCase/JIRA ID: SMAB-T172
	 * 3. Validating user is able to update 'Max. Equip. Index Factor' when status of Tables is 'Needs Recalculation':: TestCase/JIRA ID: SMAB-T272
	 */
	@Test(description = "SMAB-T172, SMAB-T271: Perform calculation & re-calculation for factors tables individually using calculate & recalclate buttons with updating max. equip. index factor", groups = {"regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BPPTrend_VerifyTableStatus_ByUpdatingMaxEquipSettings(String loginUser) throws Exception {	
		
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Updating the composite factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);

		//Step3: Updating the valuation factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);
		
		//Step4: Navigate to BPP Trend Setup page snd Select roll Year
		// Create Maximum Equipment index Factor for given roll year if it does not exist
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		objBppTrendSetupPage.createMaxEquip(rollYear);
				
		//Step5: Retreive Max. Equipment Index factor value and subtract '1' from it 
		String factorValueBeforeEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
		int maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) - 1;
		
		//Step6: Click on Edit Max. Equipment Index factor Settings and update value 
		objBppTrendSetupPage.editSaveFactorValue(Integer.toString(maxEquipIndexNewValue));
		
		//Step7: Verify saved Max. Equipment Index factor Settings value 
		String factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();	
		softAssert.assertEquals(factorValueSaved,Integer.toString(maxEquipIndexNewValue)+"%","SMAB-T271,SMAB-T172: Verify user is able to edit the Max Equipemnt Index factor value  when table status is 'Calculated'");
		Thread.sleep(2000);
		
		//Step8: Verify table status when Max. Equipment Index factor Settings value is updated
		softAssert.assertEquals("Needs Recalculation",objBppTrendSetupPage.getTableStatus("Commercial Composite Factors",rollYear),"SMAB-T172: Verify status for Commercial Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
		softAssert.assertEquals("Needs Recalculation",objBppTrendSetupPage.getTableStatus("Industrial Composite Factors",rollYear),"SMAB-T172: Verify status for Industrial Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
		softAssert.assertEquals("Needs Recalculation",objBppTrendSetupPage.getTableStatus("Construction Composite Factors",rollYear),"SMAB-T172: Verify status for Construction Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
		softAssert.assertEquals("Needs Recalculation",objBppTrendSetupPage.getTableStatus("Agricultural Composite Factors",rollYear),"SMAB-T172: Verify status for Agricultural Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
		
		//Step9: Click on Edit Max. Equipment Index factor Settings and update value when table status is 'Needs Recalculation'
		objBppTrendSetupPage.editSaveFactorValue("125");
		
		//Step10: Verify saved Max. Equipment Index factor Settings value 
		factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();	
		softAssert.assertEquals(factorValueSaved,"125%","SMAB-T172,SMAB-T272: Verify user is able to edit the Max Equipemnt Index factor value when table status is 'Needs Recalculation'");		
		
		objApasGenericFunctions.logout();
	}

}











	
