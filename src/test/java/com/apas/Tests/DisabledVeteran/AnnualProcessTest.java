package com.apas.Tests.DisabledVeteran;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.RealPropertySettingsLibrariesPage;
import com.apas.PageObjects.RollYearSettingsPage;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;


public class AnnualProcessTest extends TestBase{

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ValueAdjustmentsPage objValueAdjustmentPage;
	ExemptionsPage objExemptionsPage;
	SoftAssertion softAssert;
	Util objUtils;
	RollYearSettingsPage objRYSPage;
	RealPropertySettingsLibrariesPage objRPSLPage;
	Map<String, String> dataMap1; Map<String, String> dataMap2;
	Map<String, String> dataMap3; Map<String, String> dataMap4;
	String activeExemptionName;
	ApasGenericPage objApasGenericPage;
	String iNActiveExemptionName;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception {
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objUtils = new Util();
		objValueAdjustmentPage = new ValueAdjustmentsPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		softAssert = new SoftAssertion();
		objApasGenericPage = new ApasGenericPage(driver);
		objRYSPage = new RollYearSettingsPage(driver);
		objRPSLPage = new RealPropertySettingsLibrariesPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);		
	}
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the Exemption Support Staff in an array
	 **/
	@DataProvider(name = "loginUsers")
	public Object[][] dataProviderLoginUserMethod() {
		return new Object[][] { { users.EXEMPTION_SUPPORT_STAFF } };
	}	
	/**
	 * Below test case will 
	 * 1. Delete Real Property settings Record for current Roll Year
	 * 2. Create Exemption records with Status - Active
	 * 3. Verify for Active Exemption record, Current Year's VA contains Blank values from RPSL record
	 **/
	@Test(description = "SMAB-T566: Verify when RPSL record is missing, for Active Exemption records 'VA' gets created with blank values", dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, alwaysRun = true, enabled = true)
	public void verifyBlankVACreatedWithMissingRPSLForActiveExemption(String loginUser) throws Exception {
		
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		dataMap1 = objUtils.generateMapFromJsonFile(manualEntryData, "RollYear");
		
		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Selecting 'All' List View
		objApasGenericFunctions.selectListView("All");
		
		//Step4: Delete current Roll Year's RPSL if it already exists	
		String strRollYear = dataMap1.get("Roll Year Settings");
		objRPSLPage.verifyAndDeleteExistingRPSL(strRollYear);
		
		//Step5: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
				
		/*Step6: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - Active
		 Capture the Exemption Name*/		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating Active Exemption");
		
		dataMap2 = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateExemptionWithMandatoryFields");
		dataMap2.put("Veteran Name", dataMap2.get("Veteran Name") + objExemptionsPage.getRandomIntegerBetweenRange(100, 10000));
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataMap2);
		objPage.locateElement("//lightning-formatted-text[contains(text(),'EXMPTN-')]",20);
		activeExemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
			
		System.setProperty("activeExemptionName", activeExemptionName);	
		
		//Step7: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step8: Click on 2020 Roll Year's Value Adjustment
		String vaLinkName = objValueAdjustmentPage.selectVAByRollYear(strRollYear);	
		softAssert.assertContains(vaLinkName, "VALADJMT-","Verify that when the 'Annual Batch process' runs 'Value Adjustment Record's gets created for all the Active Exemption records");
		
		//Step9: Verify RPSL values are blank in current Roll Year's VA
		dataMap3 = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForDeletedRPSL");
		
		String actualRollYearBasicRefAmount = objValueAdjustmentPage.vaRollYearBasicRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearBasicRefAmount,dataMap3.get("Roll Year Basic Ref Amount"),"SMAB-T566:Verify \'Roll Year Basic Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,dataMap3.get("Roll Year LowIncome Ref Amount"),"SMAB-T566:Verify \'Roll Year Low Income Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,dataMap3.get("Roll Year Threshhold Amount"),"SMAB-T566:Verify \'Roll Year Low Income Threshhold Amount\' is $0.00");
		
		String actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,dataMap3.get("Roll Year Due Date"),"SMAB-T566:Verify \'Roll Year Due Date\' is blank");
		
		String actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,dataMap3.get("Roll Year Due Date2"),"SMAB-T566:Verify \'Roll Year Due Date2\' is blank");
		
		String actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,dataMap3.get("Roll Year LowIncome Penalty"),"SMAB-T566:Verify \'Roll Year Low Income Penalty\' is 0.00%");
		
		String actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,dataMap3.get("Roll Year LowIncome Penalty2"),"SMAB-T566:Verify \'Roll Year Low Income Penalty2\' is 0.00%");		
				
		objApasGenericFunctions.logout();		
	
	}
	
	/**
	 * Below test case will 
	 * 1. Delete Real Property settings Record for current Roll Year
	 * 2. Create Exemption records with Status - In-Active
	 * 3. Verify for In-Active Exemption record, Current Year's VA is not created
	 **/
	@Test(description = "SMAB-T1380: Verify when RPSL record is missing, for In-Active Exemption records 'VA' does not get created", dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, alwaysRun = true, enabled = true)
	public void verifyNoVACreatedWithMissingRPSLForInActiveExemption(String loginUser) throws Exception {
		
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		dataMap1 = objUtils.generateMapFromJsonFile(manualEntryData, "RollYear");
		
		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Selecting 'All' List View
		objApasGenericFunctions.selectListView("All");
		
		//Step4: Delete current Roll Year's RPSL if it already exists	
		String strRollYear = dataMap1.get("Roll Year Settings");
		objRPSLPage.verifyAndDeleteExistingRPSL(strRollYear);
		
		//Step5: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
					
		/*Step6: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - InActive
		 Capture the Exemption Name*/
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating In-Active Exemption");
		
		dataMap4 = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateExemptionWithEndDate");
		dataMap4.put("Veteran Name", dataMap4.get("Veteran Name") + objExemptionsPage.getRandomIntegerBetweenRange(100, 10000));
		objExemptionsPage.createExemptionWithEndDateOfRating(dataMap4);
		Thread.sleep(2000);
		iNActiveExemptionName = objExemptionsPage.getExemptionNameFromSuccessAlert();
		String inActiveExemptionName = "EXMPTN-"+iNActiveExemptionName;
				
		//Step7: Verify Exemption Status is In-Active
		objPage.locateElement("//span[contains(text(),'Status')]/parent::div/following-sibling::div//span//slot[@slot='outputField']//lightning-formatted-text", 10);
		String exemptionStatus=objExemptionsPage.exemationStatusOnDetails.getText().trim();		
		
		softAssert.assertEquals(exemptionStatus, "Inactive","SMAB-T1380:Verify In-Active Exemption is created");
		System.setProperty("inActiveExemptionName", inActiveExemptionName);
		
		//Step8: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step9: verify 2020 Roll Year's Value Adjustment is not created
		boolean fVACreated = objValueAdjustmentPage.verifyVANotCreated(strRollYear);
		softAssert.assertTrue(fVACreated,"SMAB-T1380:VVerify when RPSL record is missing, for In-Active Exemption records 'VA' does not get created");
		objApasGenericFunctions.logout();
		
	}	
	
	/**
	 * Below test case will 
	 * 1. Create Real Property settings Record for current Roll Year with Status other than 'Approved'
	 * 2. Verify for Active Exemption record created in above test, Current Year's VA is updated with blank values from RPSL record
	 **/
	@Test(description = "SMAB-T1381: Verify when 'Annual Batch process' runs and a 'VAR' is present for next Tax Year then it gets updated with blank values if status of RPSL is other than 'Approved'", dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, dependsOnMethods = {"verifyBlankVACreatedWithMissingRPSLForActiveExemption"}, alwaysRun = true, enabled = true)
	public void verifyVANotUpdatedWithUnApprovedRPSLForActiveExemption(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Fetching data for Working Roll Year
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		dataMap1 = objUtils.generateMapFromJsonFile(manualEntryData, "RollYear");		
		String strRollYear = dataMap1.get("Roll Year Settings");
		
		//Step3: Fetching data to create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		dataMap2 = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");

		//Step4: Create new RPSL record
		objRPSLPage.createRPSL(dataMap2);
	
		//Step5: Search and Select Active Exemption created in previous Test
		String recordId1 = System.getProperty("activeExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId1);

		//Step6: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step7: Click on 2020 Roll Year's Value Adjustment
		objValueAdjustmentPage.selectVAByRollYear(strRollYear);		
		
		//Step8: Verify RPSL values are updated in selected VA from Active Exemption
		dataMap3 = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForDeletedRPSL");
		
		String actualRollYearBasicRefAmount = objValueAdjustmentPage.vaRollYearBasicRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearBasicRefAmount,dataMap3.get("Roll Year Basic Ref Amount"),"SMAB-T1381:Verify \'Roll Year Basic Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,dataMap3.get("Roll Year LowIncome Ref Amount"),"SMAB-T1381:Verify \'Roll Year Low Income Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,dataMap3.get("Roll Year Threshhold Amount"),"SMAB-T1381:Verify \'Roll Year Low Income Threshhold Amount\' is $0.00");
		
		String actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,dataMap3.get("Roll Year Due Date"),"SMAB-T1381:Verify \'Roll Year Due Date\' is blank");
		
		String actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,dataMap3.get("Roll Year Due Date2"),"SMAB-T1381:Verify \'Roll Year Due Date 2\' is blank");
		
		String actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,dataMap3.get("Roll Year LowIncome Penalty"),"SMAB-T1381:Verify \'Roll Year Low Income Penalty\' is 0.00%");
		
		String actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,dataMap3.get("Roll Year LowIncome Penalty2"),"SMAB-T1381:Verify \'Roll Year Low Income Penalty 2\' is 0.00%");		
				
		objApasGenericFunctions.logout();
	}
	/**
	 * Below test case will 
	 * 1. Create Real Property settings Record for current Roll Year with Status other than 'Approved'
	 * 2. Verify for In-Active Exemption record created in above test, Current Year's VA is still not created even if RPSL is created
	 **/
	@Test(description = "SMAB-T510: Verify when 'Annual Batch process' runs for In-Active Exemption record 'VAR' for working Tax Year is not created if status of RPSL is other than 'Approved'", dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, dependsOnMethods = {"verifyNoVACreatedWithMissingRPSLForInActiveExemption"}, alwaysRun = true, enabled = true)
	public void verifyVANotCreatedWithUnApprovedRPSLFoInActiveExemption(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Fetching data for Working Roll Year
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		dataMap1 = objUtils.generateMapFromJsonFile(manualEntryData, "RollYear");		
		String strRollYear = dataMap1.get("Roll Year Settings");
		
		//Step3: Fetching data to create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		dataMap2 = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");

		//Step4: Create new RPSL record
		objRPSLPage.createRPSL(dataMap2);
				
		//Step5: Search and Select In-Active Exemption created in previous Test
		String recordId2 = System.getProperty("inActiveExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId2);
		
		//Step6: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step7: verify 2020 Roll Year's Value Adjustment does not exist for In-Active Exemption
		boolean fVACreated = objValueAdjustmentPage.verifyVANotCreated(strRollYear);
		softAssert.assertTrue(fVACreated,"SMAB-T510:Verify when 'Annual Batch process' runs for In-Active Exemption record 'VAR' for working Tax Year is not created if status of RPSL is other than 'Approved'");
		
		objApasGenericFunctions.logout();
				
	}
	
	
	/**
	 * Below test case will 
	 * 1. Create Real Property settings Record for current Roll Year with Status 'Approved'
	 * 2. Verify for Active Exemption record created in above test, Current Year's VA is updated with relevant values from RPSL record
	 * 3. Verify for In-Active Exemption record created in above test, Current Year's VA is still not created even if RPSL is created
	 **/
	@Test(description = "SMAB-T1382, T511: Verify that when the 'Annual Batch process' runs and status of RPSL is 'Approved', VAR for Active Exemption gets updated with relevant values & for In-Active Exemption, does not get created", dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, dependsOnMethods = {"verifyBlankVACreatedWithMissingRPSLForActiveExemption", "verifyNoVACreatedWithMissingRPSLForInActiveExemption"}, alwaysRun = true, enabled = true)
	
	
	public void verifyVAWithApprovedRPSL(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Fetching data for Working Roll Year
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		dataMap1 = objUtils.generateMapFromJsonFile(manualEntryData, "RollYear");
		
		String strRollYear = dataMap1.get("Roll Year Settings");
		
		//Step3: Fetching data to create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		dataMap2 = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");

		//Step4: Create new RPSL record
		objRPSLPage.createRPSL(dataMap2);
		
		//Step5: Edit Status of  RPSL record
		objRPSLPage.editRPSLStatus(strRollYear, "Approved");
	
		//Step6: Search and Select Active Exemption created in previous Test
		String recordId1 = System.getProperty("activeExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId1);

		//Step7: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step8: Click on 2020 Roll Year's Value Adjustment
		objValueAdjustmentPage.selectVAByRollYear(strRollYear);		
		
		//Step9: Verify RPSL values are updated in selected VA from Active Exemption
		dataMap3 = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForCreatedRPSL");
		
		String actualRollYearBasicRefAmount = objValueAdjustmentPage.vaRollYearBasicRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearBasicRefAmount,dataMap3.get("Roll Year Basic Ref Amount"),"SMAB-T1382: Verify \'Roll Year Basic Reference Amount\'");
		
		String actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,dataMap3.get("Roll Year LowIncome Ref Amount"),"SMAB-T1382: Verify \'Roll Year Low Income Reference Amount\'");
		
		String actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,dataMap3.get("Roll Year Threshhold Amount"),"SMAB-T1382: Verify \'Roll Year Low Income Threshhold Amount\'");
		
		String actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,dataMap3.get("Roll Year Due Date"),"SMAB-T1382: Verify \'Roll Year Due Date\'");
		
		String actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,dataMap3.get("Roll Year Due Date2"),"SMAB-T1382: Verify \'Roll Year Due Date 2\'");
		
		String actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,dataMap3.get("Roll Year LowIncome Penalty"),"SMAB-T1382: Verify \'Roll Year Low Income Penalty\'");
		
		String actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,dataMap3.get("Roll Year LowIncome Penalty2"),"SMAB-T1382: Verify \'Roll Year Low Income Penalty 2\'");		
				
		objApasGenericFunctions.logout();
		
		//Step10: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Ste11: Search and Select In-Active Exemption created in previous Test
		String recordId2 = System.getProperty("inActiveExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId2);
		
		//Step12: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step13: verify 2020 Roll Year's Value Adjustment does not exist for In-Active Exemption
		boolean fVACreated = objValueAdjustmentPage.verifyVANotCreated(strRollYear);
		softAssert.assertTrue(fVACreated,"SMAB-T511: Verify when annual batch process runs VAR does not get created for In-Active Exemption Record");
		
		objApasGenericFunctions.logout();
				
	}
	
}