package com.apas.Tests.BPPTrends;

import com.apas.PageObjects.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;

public class BPPTrend_BPPSettings_Test extends TestBase{
	RemoteWebDriver driver;
	Page objPage;
	BppTrendPage objBppTrendPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	ApasGenericPage objApasGenericPage;
	BppTrendSetupPage objBppTrendSetupPage;
	BuildingPermitPage objBuildPermitPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrendPage = new BppTrendPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		// Executing all the methods for Roll year: 2019
		rollYear = "2019";
		objApasGenericPage = new ApasGenericPage(driver);
		objBppTrendSetupPage = new BppTrendSetupPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objBppTrendSetupPage.updateRollYearStatus("Open", "2019");
	}
	@AfterMethod
	public void afterMethod() throws Exception {
		objBppTrendSetupPage.updateRollYearStatus("Closed", "2019");
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating the user is able to update and enter the 'Max. Equip. Index Factor' for previous Roll Year:: TestCase/JIRA ID: SMAB-T133
	 * 2. Validating the user is not able to enter invalid value of 'Max. Equip. Index Factor':: TestCase/JIRA ID: SMAB-T134
	 * 3. Validating the user is able to enter the 'Max. Equip. Index Factor' when table status is 'Not Calculated': TestCase/JIRA ID: SMAB-T135
	 */
	@Test(description = "SMAB-T133,SMAB-T134,SMAB-T135: Create and edit BPP Setting with invalid and valid max. equip. index factor", groups={"Smoke","Regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_Verify_BPPSettings_CreationAndValidations(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the given user
		objBppTrendSetupPage.login(loginUser);
		
		//Step2: Updating the composite factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);

		//Step3: Updating the valuation factor tables status
		objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);
				
		//Step8: Delete the existing BPP composite setting entry for given roll year
		objBppTrendPage.removeExistingBppSettingEntry(rollYear);
		
		//Step4: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		
		//Step5: Clicking on the roll year name in grid to navigate to details page of selected roll year
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		
		//Step6: Click on New option to create BPP Setting entry
		Thread.sleep(8000);
		if(objPage.verifyElementVisible(objBppTrendSetupPage.dropDownIconBppSetting))
		objPage.Click(objBppTrendSetupPage.dropDownIconBppSetting);
		if(objBppTrendPage.waitForElementToBeClickable(objBppTrendSetupPage.newBtnToCreateEntry) ==null )
			objPage.javascriptClick(objBppTrendSetupPage.dropDownIconBppSetting);

		objPage.Click(objBppTrendPage.waitForElementToBeClickable(objBppTrendSetupPage.newBtnToCreateEntry));
		
		//Step7: Clear the Default Max Equipment index factor value and verify the error message
		String expectedErrorMessage = "Complete this field.";
		objPage.enter("Maximum Equipment index Factor","");
		objPage.Click(objPage.getButtonWithText("Save"));
		String actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Maximum Equipment index Factor");
		softAssert.assertEquals(actualErrorMsg,expectedErrorMessage,"SMAB-T134: Verify error message when 'Maximum Equipment index Factor' is missing");
		
		//Step8: Validate error message with factor values less than minimum range : 100b
		String expectedErrorMsg = "Maximum Equipment index Factor: value outside of valid range on numeric field: 100000000000";
		objPage.enter("Maximum Equipment index Factor","100b");
		objPage.Click(objPage.getButtonWithText("Save"));
		actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Maximum Equipment index Factor");
		softAssert.assertEquals(actualErrorMsg,expectedErrorMsg,"SMAB-T134: Verify Error message for factor value: 100b");
		
		//Step9: Validate error message with factor values less than minimum range : 100b
		expectedErrorMsg = "Maximum Equipment Factor should be greater than or equal to 100";
		objPage.enter("Maximum Equipment index Factor","60");
		objPage.Click(objPage.getButtonWithText("Save"));
		actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Maximum Equipment index Factor");
		softAssert.assertEquals(actualErrorMsg,expectedErrorMsg,"SMAB-T134: Verify Error message for factor value: 60");
				
		//Step10: Validate error message with factor values less than minimum range : 99.4
		expectedErrorMsg = "Maximum Equipment Factor should be greater than or equal to 100";
		objPage.enter("Maximum Equipment index Factor","60");
		objPage.Click(objPage.getButtonWithText("Save"));
		actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Maximum Equipment index Factor");
		softAssert.assertEquals(actualErrorMsg,expectedErrorMsg,"SMAB-T134: Verify Error message for factor value: 99.4");
		
		//Step11: Validate error message with factor values greater than maximum range	
		expectedErrorMsg = "Maximum Equipment index Factor: value outside of valid range on numeric field: 1000: 1000";
		objPage.enter("Maximum Equipment index Factor","1000");
		objPage.Click(objPage.getButtonWithText("Save"));
		actualErrorMsg = objBppTrendSetupPage.getIndividualFieldErrorMessage("Maximum Equipment index Factor");
		softAssert.assertContains(actualErrorMsg+": 1000",expectedErrorMsg,"SMAB-T134: Verify Error message for factor value: 1000");


		//Step13: Validate error message with factor values within specified range : 124.4
		objPage.enter("Maximum Equipment index Factor","124.4");
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		String factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
		String actualValue = factorValueSaved.substring(0, factorValueSaved.length()-1);
		String expectedValue = String.valueOf(Math.round(Float.parseFloat("124.4")));
		softAssert.assertEquals(actualValue,expectedValue,"SMAB-T134: Verify entered value: 124.4 is saved as "+expectedValue);

		//Step14: Validate error message with factor values within specified range : 160
		Thread.sleep(2000);
		objPage.waitForElementToBeClickable(60, objPage.getButtonWithText("Edit"));
		objPage.Click(objPage.getButtonWithText("Edit"));
		Thread.sleep(1000);
		objPage.enter("Maximum Equipment index Factor","160");
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		
		factorValueSaved = objBppTrendSetupPage.getFieldValueFromAPAS("Maximum Equipment index Factor");
		actualValue = factorValueSaved.substring(0, factorValueSaved.length()-1);
		expectedValue = String.valueOf(Math.round(Float.parseFloat("160")));
		softAssert.assertEquals(actualValue,expectedValue,"SMAB-T134: Verify entered value: 124.4 is saved as "+expectedValue);

		//Step15: Updating the default value : 125
		objPage.waitForElementToBeClickable(60, objPage.getButtonWithText("Edit"));
		objPage.Click(objPage.getButtonWithText("Edit"));
		Thread.sleep(1000);
		objPage.enter("Maximum Equipment index Factor","125");
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		factorValueSaved = objBppTrendSetupPage.getFieldValueFromAPAS("Maximum Equipment index Factor");
		softAssert.assertEquals(factorValueSaved,"125%","SMAB-T133,SMAB-T135: Verify user is able to edit the Max Equipemnt Index factor value when status of tables is 'Not Calculated'");

		objBppTrendSetupPage.logout();
	}
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is unable to update 'Max. Equip. Index Factor' when calculations are either submitted for approval or approved:: TestCase/JIRA ID: SMAB-T137, SMAB-T273, SMAB-T274
	 */
	@Test(description = "SMAB-T137,SMAB-T273,SMAB-T274: Edit BPP Setting when table calculations are approved", groups={"Regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BppTrend_Verify_CreateEditBppSetting_AfterSubmittedAndApprovalOfTableCalculations(String loginUser) throws Exception {		
		
		//Step1: Login to the APAS application using the given user
		objBppTrendSetupPage.login(loginUser);
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		
		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		// Create Maximum Equipment index Factor for given roll year if it does not exist
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);		
		objBppTrendSetupPage.createMaxEquip(rollYear);
		objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
		objBppTrendSetupPage.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);	
		
		//Step4: Verifying the 'Edit' BPP Settings access when status is "Submitted for Approval" and "Approved"
		String[] tableStatus  = {"Submitted for Approval","Approved"};
		for(String status: tableStatus) {
			//Step5: Updating the composite factor tables status
			objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, status, rollYear);
	
			//Step6: Updating the valuation factor tables status
			objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, status, rollYear);

			//Step7: Retrieve Max. Equipment Index factor value and subtract '1' from it
			String factorValueBeforeEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
			factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
			int maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) - 1;

			//Step8: Edit the newly created entry on details page and save the factor value
			objPage.waitForElementToBeClickable(objBppTrendSetupPage.dropDownIconDetailsSection, 10);
			objPage.javascriptClick(objBppTrendSetupPage.dropDownIconDetailsSection);
			objPage.waitForElementToBeClickable(objBppTrendSetupPage.editLinkUnderShowMore, 10);
			objPage.javascriptClick(objBppTrendSetupPage.editLinkUnderShowMore);
			objPage.enter("Maximum Equipment index Factor",String.valueOf(maxEquipIndexNewValue));
			objPage.Click(objPage.getButtonWithText("Save"));
			
			//String expectedErrorMessage = "You do not have the level of access necessary to perform the operation you requested. Please contact the owner of the record or your administrator if access is necessary.";
			//String expectedErrorMessage = "Maximum Equipment index Factor is locked for editing for the Roll Year";
			//String actualErrorMessage = objPage.getElementText(objBuildPermitPage.pageError);
			
			//Step8: Selecting Test Case Id to Map based on status: 'Submitted For approval'/'Approved'
			String TCMapingID;
			if(status.equalsIgnoreCase("Submitted for Approval")) 
				TCMapingID = "SMAB-T137,SMAB-T273";
			else
				TCMapingID = "SMAB-T274";
			
			softAssert.assertTrue(objPage.waitForElementToBeVisible(10,objBuildPermitPage.pageError), TCMapingID+": Validating error message on editing and saving max. equip. index value when calculations are :"+status);
			objPage.Click(objBppTrendSetupPage.closeEntryPopUp);
		}
		objBppTrendSetupPage.logout();
	}
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating user is able to update 'Max. Equip. Index Factor' when status of Tables is 'Calculated':: TestCase/JIRA ID: SMAB-T271
	 * 2. Status of Tables is updated to 'Needs Recalculation' when 'Max. Equip. Index Factor' is updated:: TestCase/JIRA ID: SMAB-T172
	 * 3. Validating user is able to update 'Max. Equip. Index Factor' when status of Tables is 'Needs Recalculation':: TestCase/JIRA ID: SMAB-T272
	 */
	@Test(description = "SMAB-T172,SMAB-T271,SMAB-T139,SMAB-T272: Perform calculation & re-calculation for factors tables individually using calculate & recalclate buttons with updating max. equip. index factor", groups = {"Regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void BPPTrend_VerifyTableStatus_ByUpdatingMaxEquipSettings(String loginUser) throws Exception {	
		
		//Step1: Login to the APAS application using the given user
				objBppTrendSetupPage.login(loginUser);

				//Step2: Updating the composite factor tables status
				objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);

				//Step3: Updating the valuation factor tables status
				objBppTrendPage.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);
				
				//Step4: Navigate to BPP Trend Setup page snd Select roll Year
				// Create Maximum Equipment index Factor for given roll year if it does not exist
				
				Thread.sleep(6000);
				objBppTrendSetupPage.closeDefaultOpenTabs();
				objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
				objBppTrendSetupPage.displayRecords("All");
				objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
				objBppTrendSetupPage.createMaxEquip(rollYear);
						
				//Step5: Retreive Max. Equipment Index factor value and subtract '1' from it 
				String factorValueBeforeEdit = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();
				factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
				int maxEquipIndexNewValue = Integer.parseInt(factorValueBeforeEdit) - 1;
				
				//Step6: Click on Edit Max. Equipment Index factor Settings and update value
				Thread.sleep(1000);
				objBppTrendSetupPage.editSaveFactorValue(Integer.toString(maxEquipIndexNewValue));
				
				//Step7: Verify saved Max. Equipment Index factor Settings value 
				String factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();	
				softAssert.assertEquals(factorValueSaved,Integer.toString(maxEquipIndexNewValue)+"%","SMAB-T271,SMAB-T172,SMAB-T139: Verify user is able to edit the Max Equipemnt Index factor value  when table status is 'Calculated'");
				driver.navigate().refresh();
				Thread.sleep(7000);
				
				//Step8: Verify table status when Max. Equipment Index factor Settings value is updated
				softAssert.assertEquals(objBppTrendSetupPage.getTableStatus("Commercial Composite Factors",rollYear),"Needs Recalculation","SMAB-T172,SMAB-T139: Verify status for Commercial Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
				softAssert.assertEquals(objBppTrendSetupPage.getTableStatus("Industrial Composite Factors",rollYear),"Needs Recalculation","SMAB-T172,SMAB-T139: Verify status for Industrial Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
				softAssert.assertEquals(objBppTrendSetupPage.getTableStatus("Construction Composite Factors",rollYear),"Needs Recalculation","SMAB-T172,SMAB-T139: Verify status for Construction Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
				softAssert.assertEquals(objBppTrendSetupPage.getTableStatus("Agricultural Composite Factors",rollYear),"Needs Recalculation","SMAB-T172,SMAB-T139: Verify status for Agricultural Composite Factors is changed to 'Needs recalculation' from 'Calculated' when Max Equip. Index Settings is updated");
				
				//Step9: Click on Edit Max. Equipment Index factor Settings and update value when table status is 'Needs Recalculation'
				objBppTrendSetupPage.editSaveFactorValue("125");
				
				//Step10: Verify saved Max. Equipment Index factor Settings value 
				factorValueSaved = objBppTrendSetupPage.retrieveMaxEqipIndexValueFromPopUp();	
				softAssert.assertEquals(factorValueSaved,"125%","SMAB-T172,SMAB-T272,SMAB-T139: Verify user is able to edit the Max Equipemnt Index factor value when table status is 'Needs Recalculation'");		
				
				objBppTrendSetupPage.logout();
	}

}











	