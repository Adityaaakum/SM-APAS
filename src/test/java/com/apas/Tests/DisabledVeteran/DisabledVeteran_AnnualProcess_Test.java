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
	String activeExemptionName;
	String inActiveExemptionName;
	
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
	 * 4. Create Real Property settings Record for current Roll Year with Status other than 'Approved'
	 * 5. Verify for Active Exemption record created in above test, Current Year's VA is still updated with blank values from RPSL record
	 **/
	@Test(description = "SMAB-T566,SMAB-T1381: Verify when 'Annual Batch process' runs and a 'VAR' is present for next Tax Year then it gets updated with blank values if status of RPSL is other than 'Approved'", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyActiveExemptionWithDeletedAndUnApprovedRPSL(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Fetching data for roll year to delete
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		Map<String, String> rollYearAndStartDateDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "RollYearAndStartDate");
				
		//Step3: Delete current Roll Year's RPSL
		String strRollYear = rollYearAndStartDateDataMap.get("Roll Year Settings");
		objRPSLPage.removeRealPropertySettingEntry(strRollYear);
		
		//Step4: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
				
		/*Step5: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - Active
		 Capture the Exemption Name*/		
		ReportLogger.INFO("Creating Active Exemption");		
		Map<String, String> exemptionCreationDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateExemptionWithMandatoryFields");
		String timeStamp = java.time.LocalDateTime.now().toString();
		exemptionCreationDataMap.put("Veteran Name", exemptionCreationDataMap.get("Veteran Name").concat(timeStamp));
		objExemptionsPage.createExemptionWithoutEndDateOfRating(exemptionCreationDataMap);
		
		objPage.waitUntilElementIsPresent(objExemptionsPage.exemptionNumber,30);
		activeExemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
			
		ReportLogger.INFO("Active Exemption Create is: "+ activeExemptionName);	
		
		//Step6: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step7: Click on 2020 Roll Year's Value Adjustment
		String vaLinkName = objPage.getElementText(objValueAdjustmentPage.vAforRY2020);
		objPage.waitForElementToBeClickable(50, objValueAdjustmentPage.vAforRY2020);
		ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vaLinkName);
		objPage.Click(objValueAdjustmentPage.vAforRY2020);
		softAssert.assertContains(vaLinkName, "VALADJMT-","Verify that when the 'Annual Batch process' runs 'Value Adjustment Record's gets created for all the Active Exemption records");
		
		//Step8: Verify RPSL values are blank in current Roll Year's VA
		Map<String, String> verifyDataWithNoRPSLDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForDeletedRPSL");
		
		String actualRollYearBasicRefAmount = objPage.getElementText(objValueAdjustmentPage.vaRollYearBasicRefAmount);
		softAssert.assertEquals(actualRollYearBasicRefAmount,verifyDataWithNoRPSLDataMap.get("Roll Year Basic Ref Amount"),"SMAB-T566:Verify \'Roll Year Basic Reference Amount\' is $0.00 when RPSL for current Roll Year does not exist");
		
		String actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,verifyDataWithNoRPSLDataMap.get("Roll Year LowIncome Ref Amount"),"SMAB-T566:Verify \'Roll Year Low Income Reference Amount\' is $0.00 when RPSL for current Roll Year does not exist");
		
		String actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,verifyDataWithNoRPSLDataMap.get("Roll Year Threshhold Amount"),"SMAB-T566:Verify \'Roll Year Low Income Threshhold Amount\' is $0.00 when RPSL for current Roll Year does not exist");
		
		String actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,verifyDataWithNoRPSLDataMap.get("Roll Year Due Date"),"SMAB-T566:Verify \'Roll Year Due Date\' is blank when RPSL for current Roll Year does not exist");
		
		String actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,verifyDataWithNoRPSLDataMap.get("Roll Year Due Date2"),"SMAB-T566:Verify \'Roll Year Due Date2\' is blank when RPSL for current Roll Year does not exist");
		
		String actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,verifyDataWithNoRPSLDataMap.get("Roll Year LowIncome Penalty"),"SMAB-T566:Verify \'Roll Year Low Income Penalty\' is 0.00% when RPSL for current Roll Year does not exist");
		
		String actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,verifyDataWithNoRPSLDataMap.get("Roll Year LowIncome Penalty2"),"SMAB-T566:Verify \'Roll Year Low Income Penalty2\' is 0.00% when RPSL for current Roll Year does not exist");		
		
				
		//Step9: Fetching data to create new RPSL record for current roll year
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> RPSLCreationDataMap = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");

		//Step10: Create new RPSL record
		objRPSLPage.createRPSL(RPSLCreationDataMap);
	
		//Step11: Search and Select Active Exemption created in previous Test	
		objApasGenericFunctions.globalSearchRecords(activeExemptionName);		
		
		//Step12: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step13: Click on 2020 Roll Year's Value Adjustment
		String vALinkName = objPage.getElementText(objValueAdjustmentPage.vAforRY2020);
		objPage.waitForElementToBeClickable(50, objValueAdjustmentPage.vAforRY2020);
		ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vALinkName);
		objPage.Click(objValueAdjustmentPage.vAforRY2020);
		
		//Step14: Verify RPSL values are updated in selected VA from Active Exemption	
		Map<String, String> verifyDataWithUnApprovedRPSLDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToVerifyForDeletedRPSL");
		
		actualRollYearBasicRefAmount = objPage.getElementText(objValueAdjustmentPage.vaRollYearBasicRefAmount);
		softAssert.assertEquals(actualRollYearBasicRefAmount,verifyDataWithUnApprovedRPSLDataMap.get("Roll Year Basic Ref Amount"),"SMAB-T1381:Verify \'Roll Year Basic Reference Amount\' is $0.00 when RPSL for current Roll Year is not Approved");
		
		actualRollYearLowIncomeRefAmount = objValueAdjustmentPage.vaRollYearLowIncomeRefAmount.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeRefAmount,verifyDataWithUnApprovedRPSLDataMap.get("Roll Year LowIncome Ref Amount"),"SMAB-T1381:Verify \'Roll Year Low Income Reference Amount\' is $0.00 when RPSL for current Roll Year is not Approved");
		
		actualRollYearLowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText().trim();
		softAssert.assertEquals(actualRollYearLowIncomeThreshholdAmount,verifyDataWithUnApprovedRPSLDataMap.get("Roll Year Threshhold Amount"),"SMAB-T1381:Verify \'Roll Year Low Income Threshhold Amount\' is $0.00 when RPSL for current Roll Year is not Approved");
		
		actualRollYearDueDate = objValueAdjustmentPage.penaltyDate1.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate,verifyDataWithUnApprovedRPSLDataMap.get("Roll Year Due Date"),"SMAB-T1381:Verify \'Roll Year Due Date\' is blank when RPSL for current Roll Year is not Approved");
		
		actualRollYearDueDate2 = objValueAdjustmentPage.penaltyDate2.getText().trim();
		softAssert.assertEquals(actualRollYearDueDate2,verifyDataWithUnApprovedRPSLDataMap.get("Roll Year Due Date2"),"SMAB-T1381:Verify \'Roll Year Due Date 2\' is blank when RPSL for current Roll Year is not Approved");
		
		actualvaRollYearLowIncomeLatePenaltyLabel = objValueAdjustmentPage.vaRollYearLowIncomeLatePenaltyLabel.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenaltyLabel,verifyDataWithUnApprovedRPSLDataMap.get("Roll Year LowIncome Penalty"),"SMAB-T1381:Verify \'Roll Year Low Income Penalty\' is 0.00% when RPSL for current Roll Year is not Approved");
		
		actualvaRollYearLowIncomeLatePenalty2Label = objValueAdjustmentPage.vaRollYearLowIncomeLatePenalty2Label.getText().trim();
		softAssert.assertEquals(actualvaRollYearLowIncomeLatePenalty2Label,verifyDataWithUnApprovedRPSLDataMap.get("Roll Year LowIncome Penalty2"),"SMAB-T1381:Verify \'Roll Year Low Income Penalty 2\' is 0.00% when RPSL for current Roll Year is not Approved");		
				
		objApasGenericFunctions.logout();
	
}
	/**
	 * Below test case will 
	 * 1. Delete Real Property settings Record for current Roll Year
	 * 2. Create Exemption records with Status - In-Active
	 * 3. Verify for In-Active Exemption record, Current Year's VA is not created
	 * 4. Create Real Property settings Record for current Roll Year with Status other than 'Approved'
	 * 5. Verify for In-Active Exemption record created in above test, Current Year's VA is still not created even if RPSL is created
	 **/
	@Test(description = "SMAB-T510,SMAB-T1380: Verify when 'Annual Batch process' runs for In-Active Exemption record 'VAR' for working Tax Year is not created if status of RPSL is other than 'Approved'", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyInActiveExemptionWithDeletedAndUnApprovedRPSL(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Fetching data for roll year to delete
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		Map<String, String> rollYearAndStartDateDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "RollYearAndStartDate");
		
		//Step3: Delete current Roll Year's RPSL
		String strRollYear = rollYearAndStartDateDataMap.get("Roll Year Settings");
		objRPSLPage.removeRealPropertySettingEntry(strRollYear);
		
		//Step4: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
					
		/*Step5: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record - InActive
		 Capture the Exemption Name*/
		ReportLogger.INFO("Creating In Active Exemption");		
		Map<String, String> exemptionCreationDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateExemptionWithEndDate");
		String timeStamp = java.time.LocalDateTime.now().toString();
		exemptionCreationDataMap.put("Veteran Name", exemptionCreationDataMap.get("Veteran Name").concat(timeStamp));
		objExemptionsPage.createExemptionWithEndDateOfRating(exemptionCreationDataMap);
		objPage.waitUntilElementIsPresent(objExemptionsPage.exemptionNumber,30);
		inActiveExemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
			
		ReportLogger.INFO("Active Exemption Created is: "+ inActiveExemptionName);	
		
		
		//Step6: Verify Exemption Status is In-Active
		objPage.waitForElementToBeClickable(60,objExemptionsPage.exemationStatusOnDetails);
		String exemptionStatus = objPage.getElementText(objExemptionsPage.exemationStatusOnDetails);
		softAssert.assertEquals(exemptionStatus, "Inactive","SMAB-T1380:Verify In-Active Exemption is created");
		
		//Step7: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step8: verify 2020 Roll Year's Value Adjustment is not created
		boolean fVACreated = objPage.verifyElementVisible(objValueAdjustmentPage.vAforRY2020);
		softAssert.assertEquals(fVACreated,false,"SMAB-T1380:Verify when RPSL record is missing, for In-Active Exemption records 'VA' does not get created");
		
		//Step9: Fetching data to create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLDataMap = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");

		//Step10: Create new RPSL record
		objRPSLPage.createRPSL(createRPSLDataMap);
				
		//Step11: Search and Select In-Active Exemption created in previous Test
		objApasGenericFunctions.globalSearchRecords(inActiveExemptionName);
		
		//Step12: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step13: verify 2020 Roll Year's Value Adjustment does not exist for In-Active Exemption
		fVACreated = objPage.verifyElementVisible(objValueAdjustmentPage.vAforRY2020);
		softAssert.assertEquals(fVACreated,false,"SMAB-T510:Verify when 'Annual Batch process' runs for In-Active Exemption record 'VAR' for working Tax Year is not created if status of RPSL is other than 'Approved'");
		
		objApasGenericFunctions.logout();
				
	}
	
	
	/**
	 * Below test case will 
	 * 1. Create Real Property settings Record for current Roll Year with Status 'Approved'
	 * 2. Verify for Active Exemption record created in above test, Current Year's VA is updated with relevant values from RPSL record
	 * 3. Verify for In-Active Exemption record created in above test, Current Year's VA is still not created even if RPSL is created
	 **/
	@Test(description = "SMAB-T1382, T511: Verify that when the 'Annual Batch process' runs and status of RPSL is 'Approved', VAR for Active Exemption gets updated with relevant values & for In-Active Exemption, does not get created", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" }, dependsOnMethods = {"DisabledVeteran_verifyActiveExemptionWithDeletedAndUnApprovedRPSL", "DisabledVeteran_verifyInActiveExemptionWithDeletedAndUnApprovedRPSL"})
	
	public void DisabledVeteran_verifyVAWithApprovedRPSL(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);

		//Step2: Fetching data for Working Roll Year
		String manualEntryData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;		
		Map<String, String> rollYearAndStartDateDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "RollYearAndStartDate");
		
		//Step3: Delete current Roll Year's RPSL if it already exists	
		String strRollYear = rollYearAndStartDateDataMap.get("Roll Year Settings");
		objRPSLPage.removeRealPropertySettingEntry(strRollYear);
				
		//Step4: Fetching data and create new RPSL record
		String entryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLDataMap = objUtils.generateMapFromJsonFile(entryData, "DataToCreateCurrentRPSLEntry");
		String strSuccessAlertMessage = objRPSLPage.createRPSL(createRPSLDataMap);
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRollYear + "\" was created.","Verify the User is able to create Exemption limit record for the current roll year");
		
		//Step5: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		objApasGenericFunctions.displayRecords("All");
		
		//Step6: Edit RPSL status	
		//Step7: Search and click Edit button of RPSL
		String value = "Exemption Limits - "+ strRollYear;
		objApasGenericFunctions.searchRecords(value);
		objRPSLPage.clickShowMoreLink(value);
		objPage.clickAction(objPage.waitForElementToBeClickable(objRPSLPage.editLinkUnderShowMore));
		objPage.waitUntilPageisReady(driver);
		
		//Step8: Update the RPSL Status
		ReportLogger.INFO("Editing the Status field to 'Approved'");
		objRPSLPage.selectFromDropDown(objRPSLPage.statusDropDown,"Approved");		
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		System.out.println("success message is :"+strSuccessAlertMessage);
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + value + "\" was saved.","RPSL is edited successfully");
	
		//Step9: Search and Select Active Exemption created in previous Test
		objApasGenericFunctions.globalSearchRecords(activeExemptionName);

		//Step10: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step11: Click on 2020 Roll Year's Value Adjustment
		String vaLinkName = objPage.getElementText(objValueAdjustmentPage.vAforRY2020);
		objPage.waitForElementToBeClickable(50, objValueAdjustmentPage.vAforRY2020);
		ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vaLinkName);
		objPage.Click(objValueAdjustmentPage.vAforRY2020);	
		
		//Step12: Verify RPSL values are updated in selected VA from Active Exemption
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
				
		//objApasGenericFunctions.logout();
		
		//Step13: Login to the APAS application using the credentials passed through		
		//objApasGenericFunctions.login(loginUser);
		
		//Ste14: Search and Select In-Active Exemption created in previous Test
		objApasGenericFunctions.globalSearchRecords(inActiveExemptionName);
		
		//Step15: Navigate to Value Adjustment List View in Exemption
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step16: verify 2020 Roll Year's Value Adjustment does not exist for In-Active Exemption
		boolean fVACreated = objPage.verifyElementVisible(objValueAdjustmentPage.vAforRY2020);
		softAssert.assertEquals(fVACreated,false,"SMAB-T511: Verify when annual batch process runs VAR does not get created for In-Active Exemption Record");
		
		objApasGenericFunctions.logout();
				
	}
	
}