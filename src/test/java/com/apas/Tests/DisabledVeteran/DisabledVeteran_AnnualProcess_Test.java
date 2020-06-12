package com.apas.Tests.DisabledVeteran;

import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.RealPropertySettingsLibrariesPage;
import com.apas.PageObjects.RollYearSettingsPage;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;


public class DisabledVeteran_AnnualProcess_Test extends TestBase{

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ValueAdjustmentsPage objValueAdjustmentPage;
	ExemptionsPage objExemptionsPage;
	SoftAssertion softAssert;
	Util objUtils;
	RollYearSettingsPage objRYSPage;
	RealPropertySettingsLibrariesPage objRPSLPage;
	ApasGenericPage objApasGenericPage;
	
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
	 * Below test case will 
	 * 1. Delete Real Property settings Record for current Roll Year
	 * 2. Create Exemption records with Status - Active
	 * 3. Verify for Active Exemption record, Current Year's VA contains Blank values from RPSL record
	 **/
	@Test(description = "SMAB-T566: Verify when RPSL record is missing, for Active Exemption records 'VA' gets created with blank values",dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {
			"smoke", "regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyBlankVACreatedWithMissingRPSLForActiveExemption(String loginUser) throws Exception {
		
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		Map<String, String> rollYearAndStartDateDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "RollYearAndStartDate");
		
		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step4: Delete current Roll Year's RPSL if it already exists	
		String strRollYear = rollYearAndStartDateDataMap.get("Roll Year Settings");
		objRPSLPage.removeRealPropertySettingEntry(strRollYear);
		
		//Step5: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
				
		/*Step6: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - Active
		 Capture the Exemption Name*/		
		ReportLogger.INFO("Creating Active Exemption");		
		Map<String, String> exemptionCreationDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateExemptionWithMandatoryFields");
		String timeStamp = java.time.LocalDateTime.now().toString();
		exemptionCreationDataMap.put("Veteran Name", exemptionCreationDataMap.get("Veteran Name").concat(timeStamp));
		objExemptionsPage.createExemptionWithoutEndDateOfRating(exemptionCreationDataMap);
		objPage.locateElement("//lightning-formatted-text[contains(text(),'EXMPTN-')]",20);
		String activeExemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
			
		ReportLogger.INFO("Active Exemption Create is: "+ activeExemptionName);	
		
		//Step7: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step8: Click on 2020 Roll Year's Value Adjustment
		String vaLinkName = objPage.getElementText(objValueAdjustmentPage.vAforRY2020);
		objPage.waitForElementToBeClickable(50, objValueAdjustmentPage.vAforRY2020);
		ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vaLinkName);
		objPage.Click(objValueAdjustmentPage.vAforRY2020);
		objPage.waitUntilPageisReady(driver);
		softAssert.assertContains(vaLinkName, "VALADJMT-","Verify that when the 'Annual Batch process' runs 'Value Adjustment Record's gets created for all the Active Exemption records");
		
		//Step9: Verify RPSL values are blank in current Roll Year's VA
		Map<String, String> verifyWithNoRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForDeletedRPSL");
		
		String actualRollYearBasicRefAmount = objPage.getElementText(objValueAdjustmentPage.vaRollYearBasicRefAmount);
		softAssert.assertEquals(actualRollYearBasicRefAmount,verifyWithNoRPSLdataMap.get("Roll Year Basic Ref Amount"),"SMAB-T566:Verify \'Roll Year Basic Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,verifyWithNoRPSLdataMap.get("Roll Year LowIncome Ref Amount"),"SMAB-T566:Verify \'Roll Year Low Income Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,verifyWithNoRPSLdataMap.get("Roll Year Threshhold Amount"),"SMAB-T566:Verify \'Roll Year Low Income Threshhold Amount\' is $0.00");
		
		String actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,verifyWithNoRPSLdataMap.get("Roll Year Due Date"),"SMAB-T566:Verify \'Roll Year Due Date\' is blank");
		
		String actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,verifyWithNoRPSLdataMap.get("Roll Year Due Date2"),"SMAB-T566:Verify \'Roll Year Due Date2\' is blank");
		
		String actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,verifyWithNoRPSLdataMap.get("Roll Year LowIncome Penalty"),"SMAB-T566:Verify \'Roll Year Low Income Penalty\' is 0.00%");
		
		String actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,verifyWithNoRPSLdataMap.get("Roll Year LowIncome Penalty2"),"SMAB-T566:Verify \'Roll Year Low Income Penalty2\' is 0.00%");		
				
		objApasGenericFunctions.logout();		
	
	}
	
	/**
	 * Below test case will 
	 * 1. Delete Real Property settings Record for current Roll Year
	 * 2. Create Exemption records with Status - In-Active
	 * 3. Verify for In-Active Exemption record, Current Year's VA is not created
	 **/
	@Test(description = "SMAB-T1380: Verify when RPSL record is missing, for In-Active Exemption records 'VA' does not get created", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {
			"smoke", "regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyNoVACreatedWithMissingRPSLForInActiveExemption(String loginUser) throws Exception {
		
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		Map<String, String> rollYearAndStartDateDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "RollYearAndStartDate");
		
		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step4: Delete current Roll Year's RPSL if it already exists	
		String strRollYear = rollYearAndStartDateDataMap.get("Roll Year Settings");
		objRPSLPage.removeRealPropertySettingEntry(strRollYear);
		
		//Step5: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
					
		/*Step6: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - InActive
		 Capture the Exemption Name*/
		ReportLogger.INFO("Creating In Active Exemption");
		
		Map<String, String> exemptionCreationDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateExemptionWithEndDate");
		String timeStamp = java.time.LocalDateTime.now().toString();
		exemptionCreationDataMap.put("Veteran Name", exemptionCreationDataMap.get("Veteran Name").concat(timeStamp));
		objExemptionsPage.createExemptionWithEndDateOfRating(exemptionCreationDataMap);
		Thread.sleep(2000);
		String iNActiveExemptionName = objExemptionsPage.getExemptionNameFromSuccessAlert();
		String inActiveExemptionName = "EXMPTN-"+iNActiveExemptionName;
		ReportLogger.INFO("In Active Exemption Created is: "+inActiveExemptionName);
				
		//Step7: Verify Exemption Status is In-Active
		objPage.waitForElementToBeClickable(60,objExemptionsPage.exemationStatusOnDetails);
		String exemptionStatus = objPage.getElementText(objExemptionsPage.exemationStatusOnDetails);
		//String exemptionStatus = objExemptionsPage.exemationStatusOnDetails.getText().trim();		
		
		softAssert.assertEquals(exemptionStatus, "Inactive","SMAB-T1380:Verify In-Active Exemption is created");
		System.setProperty("inActiveExemptionName", inActiveExemptionName);
		
		//Step8: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step9: verify 2020 Roll Year's Value Adjustment is not created
		boolean fVACreated = objPage.verifyElementVisible(objValueAdjustmentPage.vAforRY2020);
		softAssert.assertEquals(fVACreated,false,"SMAB-T1380:Verify when RPSL record is missing, for In-Active Exemption records 'VA' does not get created");
		objApasGenericFunctions.logout();
		
	}	
	
	/**
	 * Below test case will 
	 * 1. Create Real Property settings Record for current Roll Year with Status other than 'Approved'
	 * 2. Verify for Active Exemption record created in above test, Current Year's VA is updated with blank values from RPSL record
	 **/
	@Test(description = "SMAB-T1381: Verify when 'Annual Batch process' runs and a 'VAR' is present for next Tax Year then it gets updated with blank values if status of RPSL is other than 'Approved'", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {
			"smoke", "regression","DisabledVeteranExemption" }, dependsOnMethods = {"DisabledVeteran_verifyBlankVACreatedWithMissingRPSLForActiveExemption"})
	public void DisabledVeteran_verifyVANotUpdatedWithUnApprovedRPSLForActiveExemption(String loginUser) throws Exception {
	if(!failedMethods.contains("verifyBlankVACreatedWithMissingRPSLForActiveExemption")) {
		
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
				
		//Step3: Fetching data to create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> RPSLCreationDataMap = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");

		//Step4: Create new RPSL record
		objRPSLPage.createRPSL(RPSLCreationDataMap);
	
		//Step5: Search and Select Active Exemption created in previous Test
		String recordId1 = System.getProperty("activeExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId1);
		
		//Step6: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step7: Click on 2020 Roll Year's Value Adjustment
		String vALinkName = objPage.getElementText(objValueAdjustmentPage.vAforRY2020);
		objPage.waitForElementToBeClickable(50, objValueAdjustmentPage.vAforRY2020);
		ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vALinkName);
		objPage.Click(objValueAdjustmentPage.vAforRY2020);
		objPage.waitUntilPageisReady(driver);	
		
		//Step8: Verify RPSL values are updated in selected VA from Active Exemption
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;	
		Map<String, String> verifyWithUnApprovedRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForDeletedRPSL");
		
		String actualRollYearBasicRefAmount = objPage.getElementText(objValueAdjustmentPage.vaRollYearBasicRefAmount);
		softAssert.assertEquals(actualRollYearBasicRefAmount,verifyWithUnApprovedRPSLdataMap.get("Roll Year Basic Ref Amount"),"SMAB-T1381:Verify \'Roll Year Basic Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,verifyWithUnApprovedRPSLdataMap.get("Roll Year LowIncome Ref Amount"),"SMAB-T1381:Verify \'Roll Year Low Income Reference Amount\' is $0.00");
		
		String actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,verifyWithUnApprovedRPSLdataMap.get("Roll Year Threshhold Amount"),"SMAB-T1381:Verify \'Roll Year Low Income Threshhold Amount\' is $0.00");
		
		String actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,verifyWithUnApprovedRPSLdataMap.get("Roll Year Due Date"),"SMAB-T1381:Verify \'Roll Year Due Date\' is blank");
		
		String actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,verifyWithUnApprovedRPSLdataMap.get("Roll Year Due Date2"),"SMAB-T1381:Verify \'Roll Year Due Date 2\' is blank");
		
		String actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,verifyWithUnApprovedRPSLdataMap.get("Roll Year LowIncome Penalty"),"SMAB-T1381:Verify \'Roll Year Low Income Penalty\' is 0.00%");
		
		String actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,verifyWithUnApprovedRPSLdataMap.get("Roll Year LowIncome Penalty2"),"SMAB-T1381:Verify \'Roll Year Low Income Penalty 2\' is 0.00%");		
				
		objApasGenericFunctions.logout();
	}
	else {
		softAssert.assertTrue(false, "This Test depends on 'verifyBlankVACreatedWithMissingRPSLForActiveExemption' which got failed");	
	}
}
	/**
	 * Below test case will 
	 * 1. Create Real Property settings Record for current Roll Year with Status other than 'Approved'
	 * 2. Verify for In-Active Exemption record created in above test, Current Year's VA is still not created even if RPSL is created
	 **/
	@Test(description = "SMAB-T510: Verify when 'Annual Batch process' runs for In-Active Exemption record 'VAR' for working Tax Year is not created if status of RPSL is other than 'Approved'", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {
			"smoke", "regression","DisabledVeteranExemption" }, dependsOnMethods = {"DisabledVeteran_verifyNoVACreatedWithMissingRPSLForInActiveExemption"})
	public void DisabledVeteran_verifyVANotCreatedWithUnApprovedRPSLForInActiveExemption(String loginUser) throws Exception {
	if(!failedMethods.contains("verifyNoVACreatedWithMissingRPSLForInActiveExemption")) {
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
				
		//Step3: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step4: Fetching data for Working Roll Year
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		Map<String, String> rollYearAndStartDateDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "RollYearAndStartDate");
		
		//Step5: Delete current Roll Year's RPSL if it already exists	
		String strRollYear = rollYearAndStartDateDataMap.get("Roll Year Settings");
		objRPSLPage.removeRealPropertySettingEntry(strRollYear);
				
		//Step6: Fetching data to create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLDataMap = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");

		//Step7: Create new RPSL record
		objRPSLPage.createRPSL(createRPSLDataMap);
				
		//Step8: Search and Select In-Active Exemption created in previous Test
		String recordId2 = System.getProperty("inActiveExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId2);
		
		//Step9: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step10: verify 2020 Roll Year's Value Adjustment does not exist for In-Active Exemption
		boolean fVACreated = objPage.verifyElementVisible(objValueAdjustmentPage.vAforRY2020);
		softAssert.assertEquals(fVACreated,false,"SMAB-T510:Verify when 'Annual Batch process' runs for In-Active Exemption record 'VAR' for working Tax Year is not created if status of RPSL is other than 'Approved'");
		
		objApasGenericFunctions.logout();
	}
	else {
		softAssert.assertTrue(false, "This Test depends on 'verifyNoVACreatedWithMissingRPSLForInActiveExemption' which got failed");	
	}			
	}
	
	
	/**
	 * Below test case will 
	 * 1. Create Real Property settings Record for current Roll Year with Status 'Approved'
	 * 2. Verify for Active Exemption record created in above test, Current Year's VA is updated with relevant values from RPSL record
	 * 3. Verify for In-Active Exemption record created in above test, Current Year's VA is still not created even if RPSL is created
	 **/
	@Test(description = "SMAB-T1382, T511: Verify that when the 'Annual Batch process' runs and status of RPSL is 'Approved', VAR for Active Exemption gets updated with relevant values & for In-Active Exemption, does not get created", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {
			"smoke", "regression","DisabledVeteranExemption" }, dependsOnMethods = {"DisabledVeteran_verifyBlankVACreatedWithMissingRPSLForActiveExemption", "DisabledVeteran_verifyNoVACreatedWithMissingRPSLForInActiveExemption"})
	
	public void DisabledVeteran_verifyVAWithApprovedRPSL(String loginUser) throws Exception {
	if(!failedMethods.contains("verifyBlankVACreatedWithMissingRPSLForActiveExemption")) {
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
						
		//Step3: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step4: Fetching data for Working Roll Year
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		Map<String, String> rollYearAndStartDateDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "RollYearAndStartDate");
		
		//Step5: Delete current Roll Year's RPSL if it already exists	
		String strRollYear = rollYearAndStartDateDataMap.get("Roll Year Settings");
		objRPSLPage.removeRealPropertySettingEntry(strRollYear);
				
		//Step6: Fetching data and create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLDataMap = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");
		String strSuccessAlertMessage = objRPSLPage.createRPSL(createRPSLDataMap);
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRollYear + "\" was created.","Verify the User is able to create Exemption limit record for the current roll year");
		
		//Step7: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		objApasGenericFunctions.displayRecords("All");
		
		//Step8: Edit RPSL status
	
		//Step9: Search and click Edit button of RPSL
		String value = "Exemption Limits - "+ strRollYear;
		objApasGenericFunctions.searchRecords(value);
		objRPSLPage.clickShowMoreLink(value);
		objPage.clickAction(objPage.waitForElementToBeClickable(objRPSLPage.editLinkUnderShowMore));
		objPage.waitUntilPageisReady(driver);
		
		//Step10: Update the RPSL Status
		ReportLogger.INFO("Editing the Status field to 'Approved'");
		objRPSLPage.selectFromDropDown(objRPSLPage.statusDropDown,"Approved");		
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		System.out.println("success message is :"+strSuccessAlertMessage);
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + value + "\" was saved.","RPSL is edited successfully");
	
		//Step11: Search and Select Active Exemption created in previous Test
		String recordId1 = System.getProperty("activeExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId1);

		//Step12: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step13: Click on 2020 Roll Year's Value Adjustment
		String vaLinkName = objPage.getElementText(objValueAdjustmentPage.vAforRY2020);
		objPage.waitForElementToBeClickable(50, objValueAdjustmentPage.vAforRY2020);
		ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vaLinkName);
		objPage.Click(objValueAdjustmentPage.vAforRY2020);
		objPage.waitUntilPageisReady(driver);		
		
		//Step14: Verify RPSL values are updated in selected VA from Active Exemption
		Map<String, String> verifyWithUnApprovedRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForCreatedRPSL");
		
		String actualRollYearBasicRefAmount = objPage.getElementText(objValueAdjustmentPage.vaRollYearBasicRefAmount);
		softAssert.assertEquals(actualRollYearBasicRefAmount,verifyWithUnApprovedRPSLdataMap.get("Roll Year Basic Ref Amount"),"SMAB-T1382: Verify \'Roll Year Basic Reference Amount\'");
		
		String actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,verifyWithUnApprovedRPSLdataMap.get("Roll Year LowIncome Ref Amount"),"SMAB-T1382: Verify \'Roll Year Low Income Reference Amount\'");
		
		String actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,verifyWithUnApprovedRPSLdataMap.get("Roll Year Threshhold Amount"),"SMAB-T1382: Verify \'Roll Year Low Income Threshhold Amount\'");
		
		String actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,verifyWithUnApprovedRPSLdataMap.get("Roll Year Due Date"),"SMAB-T1382: Verify \'Roll Year Due Date\'");
		
		String actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,verifyWithUnApprovedRPSLdataMap.get("Roll Year Due Date2"),"SMAB-T1382: Verify \'Roll Year Due Date 2\'");
		
		String actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,verifyWithUnApprovedRPSLdataMap.get("Roll Year LowIncome Penalty"),"SMAB-T1382: Verify \'Roll Year Low Income Penalty\'");
		
		String actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,verifyWithUnApprovedRPSLdataMap.get("Roll Year LowIncome Penalty2"),"SMAB-T1382: Verify \'Roll Year Low Income Penalty 2\'");		
				
		objApasGenericFunctions.logout();
		
		//Step15: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
		
		//Ste16: Search and Select In-Active Exemption created in previous Test
		String recordId2 = System.getProperty("inActiveExemptionName");
		objExemptionsPage.searchAndSelectExemption(recordId2);
		
		//Step17: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step18: verify 2020 Roll Year's Value Adjustment does not exist for In-Active Exemption
		boolean fVACreated = objPage.verifyElementVisible(objValueAdjustmentPage.vAforRY2020);
		softAssert.assertEquals(fVACreated,false,"SMAB-T511: Verify when annual batch process runs VAR does not get created for In-Active Exemption Record");
		
		objApasGenericFunctions.logout();
	}
	else {
		softAssert.assertTrue(false, "This Test depends on 'verifyBlankVACreatedWithMissingRPSLForActiveExemption' which got failed");	
	}			
	}
	
}