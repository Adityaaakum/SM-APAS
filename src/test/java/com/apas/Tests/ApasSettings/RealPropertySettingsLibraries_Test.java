package com.apas.Tests.ApasSettings;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;

public class RealPropertySettingsLibraries_Test extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	RealPropertySettingsLibrariesPage objRPSLPage;
	SoftAssertion softAssert;
	ApasGenericPage objApasgenericpage;
	Util objUtils;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		
		objPage = new Page(driver);
		objUtils = new Util();
		objRPSLPage = new RealPropertySettingsLibrariesPage(driver);
		objApasgenericpage=new ApasGenericPage(driver);
		softAssert = new SoftAssertion();
		objRPSLPage.updateRollYearStatus("Closed", "2020");
	}
	
	 
	/**
	 Below test case will validate that 
	 1. user is able to create Real Property Settings Library for Fututre Roll Year
	 2. User is not able to create duplicate Exemption limit record for any random roll year
	 3. User is not able to create duplicate Exemption limit record for a roll year whose entry already exists	 
	 **/
	@Test(description = "SMAB-T536,SMAB-T540,SMAB-T541:Test Creation of Future Real Property Settings & duplicate creation", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption"})
	public void DisabledVeteran_verifyFututreRPSLCreation(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching Data to create RPSL record			
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateFututreRPSLEntry");
		
		//Step4: Determine & Delete this Roll Year's RPSL if it already exists	
		String date = DateUtil.getCurrentDate("MM/dd/yyyy");
		String strRollYear = ExemptionsPage.determineRollYear(date);
		int futureRollYear = Integer.parseInt(strRollYear)+2;
		String strRPSL = String.valueOf(futureRollYear);
		String strRPSLName = "Exemption Limits - " + strRPSL;
		objRPSLPage.removeRealPropertySettingEntry(strRPSL);
		
		//Step5: Adding a new record	
		ReportLogger.INFO("Adding a new Future 'Real Prpoerty Settings' record");		
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLDataMap,strRPSL);
		
		//Step6: Clicking on Save button and verifying Success Message
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSLName + "\" was created.","SMAB-T536: Verify the User is able to create Exemption limit record for a past and future roll year");	
				
		//Step7: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step8: Create RPSL again for same Roll Year
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLDataMap,strRPSL);
		
		//Step9: Clicking on Save button
		objPage.Click(objRPSLPage.saveButton);
		
		//Step10: Verify Duplicate Future Roll Year cannot be created
		String expectedWarningMessageOnTop = "Close error dialog\nWe hit a snag.\nYou can't save this record because a duplicate record already exists. To save, use different information.\nView Duplicates";
		softAssert.assertEquals(objPage.getElementText(objApasgenericpage.pageError),expectedWarningMessageOnTop,"SMAB-T540,SMAB-T541:Verify the User is not able to create duplicate Exemption limit record for any random roll year");
		
		//Step11: Closing the RPSL creation pop up
		objPage.Click(objRPSLPage.cancelButton);
		
		objRPSLPage.logout();
	}	
	
	/**
	 Below test case will validate that 
	 1. User is able to create Exemption limit record for the current roll year
	 2. User is not able to create duplicate Exemption Limit record for current Roll Year
	 3. User is able to edit Exemption limit record for the current roll year
	 **/
	@Test(description = "SMAB-T535,SMAB-T539: Test Current Roll Year's RPSL creation & duplicate RPSL ",groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class)
	public void DisabledVeteran_verifyCurrentYearRPSLCreation(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		
		// Step1: Login to the APAS application using the credentials passed through		
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Selecting 'All' List View
		objRPSLPage.displayRecords("All");
		
		//Step4: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateCurrentRPSLEntry");
		
		//Step5: Determine current Roll Year & Delete current Roll Year's RPSL if it already exists	
		String date = DateUtil.getCurrentDate("MM/dd/yyyy");
		String strRPSL = ExemptionsPage.determineRollYear(date);
		objRPSLPage.removeRealPropertySettingEntry(strRPSL);
		
		//Step6: Creating the RPSL record for current year
		ReportLogger.INFO("Adding current year's 'Real Property Settings' record");
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLDataMap,strRPSL);
		
		//Step7: Clicking on Save button & Verifying the RPSL record for current year after creation
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		String strRPSLName = "Exemption Limits - " + strRPSL;
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSLName + "\" was created.","SMAB-T535:Verify the User is able to create Exemption limit record for the current roll year");	
				
		//Step8: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step9: Create RPSL again for current Roll Year
		ReportLogger.INFO("Again adding current year's 'Real Property Settings' record");
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLDataMap,strRPSL);
		
		//Step10: Clicking on Save button
		objPage.Click(objRPSLPage.saveButton);		
		
		//Step11: Verify Duplicate Current Roll Year cannot be created
		String expectedWarningMessageOnTop = "Close error dialog\nWe hit a snag.\nYou can't save this record because a duplicate record already exists. To save, use different information.\nView Duplicates";
		objPage.waitForElementToBeVisible(objApasgenericpage.pageError, 30);
		softAssert.assertEquals(objPage.getElementText(objApasgenericpage.pageError),expectedWarningMessageOnTop,"SMAB-T539:Verify the User is not able to create duplicate Exemption limit record for current roll year");
		objPage.Click(objRPSLPage.cancelButton);
		
		//Step12: Click on All List View
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		objRPSLPage.displayRecords("All");
		
		//Step13: Search value of RPSL created above
		objRPSLPage.searchRecords(strRPSLName);
		
		//Step14: Selecting the RPSL
		objRPSLPage.clickShowMoreLink(strRPSLName);
		objPage.clickAction(objPage.waitForElementToBeClickable(objRPSLPage.editLinkUnderShowMore));

		//Step15: Fetching the data to edit the RPSL record
		Map<String, String> editRPSDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToEditRPSLEntry");	
		
		//Step16: Edit the RPSL
		ReportLogger.INFO("Editing the Status field to '"+editRPSDataMap.get("Status") + "'");
		objRPSLPage.selectOptionFromDropDown(objRPSLPage.statusDropDown,editRPSDataMap.get("Status"));
		
		//Step17: Saving the RPSL after editing 'Status' dropdown
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSLName + "\" was saved.","SMAB-T535: Verify the User is able to edit Exemption limit record for the current roll year");			
		
		objRPSLPage.logout();
	}
		
	/**
	 Below test case is used to validate the validation rules on Real Property Settings Library screen
	 **/
	@Test(description = "SMAB-T544: Mandatory Field Validation while creating Real Property Settings", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class )
	public void DisabledVeteran_validateMandatoryFieldErrorsRPSLCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);

		//Step3: Open and save Real Property Settings Libraries form without entering the data
		ReportLogger.INFO("Clicking on New Button");
		objPage.Click(objRPSLPage.newButton);
		Thread.sleep(3000);
		ReportLogger.INFO("Clicking on Save Button without entering any details");
		objPage.waitUntilElementIsPresent("//button[@title = 'Save']", 30);
		objPage.Click(objRPSLPage.saveButton);

		//Step4: Validate the error message appeared for mandatory fields
		String expectedErrorMessageOnTop = "Close error dialog\nWe hit a snag.\nReview the following fields\nRP Setting Name\nRoll Year Settings\nDV Low Income Exemption Amount\nDV Basic Exemption Amount\nDV Low Income Household Limit";
		String expectedIndividualFieldMessage = "Complete this field.";
		softAssert.assertEquals(objPage.getElementText(objApasgenericpage.pageError),expectedErrorMessageOnTop,"SMAB-T544: Validating mandatory fields missing error in manual entry pop up header.");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("RP Setting Name"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'RP Setting Name'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("Roll Year Settings"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'Roll Year Settings'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Exemption Amount"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'DV Low Income Exemption Amount'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Basic Exemption Amount"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'DV Basic Exemption Amount'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Household Limit"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'DV Low Income Household Limit'");

		//Step5: Closing the RPSL creation pop up
		objPage.Click(objRPSLPage.cancelButton);
		objRPSLPage.logout();
	}
	
	/**
	 Below test case will validate that user is not able to create Real Property Settings Library if 'Cancel' button is clicked instead of 'Save' button
	 **/
	@Test(description = "SMAB-T537: Verify Current Roll Year RPSL not created on clicking Cancel button", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption"})
	public void DisabledVeteran_verifyCancelRPSLCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching the data to create RPSL record
		ReportLogger.INFO("Adding a new 'Real Proerty Settings' record");
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateFututreRPSLEntry");
		String date = DateUtil.getCurrentDate("MM/dd/yyyy");
		String strRollYear = ExemptionsPage.determineRollYear(date);
		int futureRollYear = Integer.parseInt(strRollYear)+2;
		String strRPSL = String.valueOf(futureRollYear);
		
		//Step4: Clicking on 'New' button & entering the details
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLdataMap,strRPSL);
		
		//Step5: Clicking on Cancel Button and verifying that record is not created
		objPage.Click(objRPSLPage.cancelButton);
		boolean successAlert = objPage.verifyElementVisible(objRPSLPage.successAlert);
			
		softAssert.assertEquals(successAlert, false, "SMAB-T537: Verify Exemption limit record for the current roll year is not created when User clicks Cancel button");
		
		//Added the below two lines to bring back the focus to handle regression failure - 11/26
		driver.navigate().refresh();
		Thread.sleep(1000);
		objRPSLPage.logout();
	}		
	
	
	 /**
	 Below test case will validate that user is not able to create RPSL having $0 in amount fields
	 **/
	@Test(description = "SMAB-T538: Creation of RPSL with $0 in amount fields", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption"})
	public void DisabledVeteran_verifyValidationRulesOnRPSLCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching Data to create RPSL record	
		ReportLogger.INFO("Adding a new 'Real Proerty Settings' record");
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateRPSLEntryWithZeroAmt");
		String date = DateUtil.getCurrentDate("MM/dd/yyyy");
		String strRollYear = ExemptionsPage.determineRollYear(date);
		int futureRollYear = Integer.parseInt(strRollYear)+2;
		String strRPSL = String.valueOf(futureRollYear);
		
		//Step4: Adding a new record
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLdataMap,strRPSL);
		objPage.enter(objRPSLPage.dvLowIncomeHouseholdLimitDatePicker, "2/15/2020");
		objPage.enter(objRPSLPage.dvAnnualLowIncomeDueDate2DatePicker,"12/12/2020");	
		
		//Step5: Clicking on Save button
		objPage.Click(objRPSLPage.saveButton);
		Thread.sleep(2000);

		//Step6: Validate the error message appeared for mandatory fields
		String expectedDvLowIncomeExemptionAmountMessage = "Low Income Exemption Amount should be greater than 0";
		String expectedDvBasicIncomeExemptionAmountMessage = "Basic Exemption Amount should be greater than 0";
		String expectedDvLowIncomeHouseholdLimitMessage = "Low Income Household Limit should be greater than 0";		
		String expectedDVAnnualLowIncomeDueDateMessage = "Due Date's year must be the same as the associated roll year.";
		String expectedDVAnnualLowIncomeDueDate2Message = "Due Date 2's year must be the same as the associated roll year.";
			
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Exemption Amount"),expectedDvLowIncomeExemptionAmountMessage,"SMAB-T538: Verify the User is not able to create Exemption limit record with $0 values");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Basic Exemption Amount"),expectedDvBasicIncomeExemptionAmountMessage,"SMAB-T538: Verify the User is not able to create Exemption limit record with $0 values");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Household Limit"),expectedDvLowIncomeHouseholdLimitMessage,"SMAB-T538: Verify the User is not able to create Exemption limit record with $0 values");
		
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Annual Due Date"),expectedDVAnnualLowIncomeDueDateMessage,"SMAB-T544: Verify the validation rules on Real Property Settings Library screen");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Annual Due Date 2"),expectedDVAnnualLowIncomeDueDate2Message,"SMAB-T544: Verify the validation rules on Real Property Settings Library screen");

		//Step5: Closing the RPSL creation pop up
		objPage.Click(objRPSLPage.cancelButton);
		
		objRPSLPage.logout();
	}
	
	/**
	 Below test case is used to validate that User is not able to edit and save Exemption limit record for a roll year when entered value is $0
	 **/
	@Test(description = "SMAB-T542:Verify the User is not able to edit and save Exemption limit record for a roll year when entered value is $0", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class )
	public void DisabledVeteran_verifyValidationRulesOnRPSLEditing(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateRPSLEntryForValidation");
		
		//Step4: Delete Roll Year's RPSL if it already exists	
		String date = DateUtil.getCurrentDate("MM/dd/yyyy");
		String strRollYear = ExemptionsPage.determineRollYear(date);
		int futureRollYear = Integer.parseInt(strRollYear)+3;
		String strRPSL = String.valueOf(futureRollYear);
		String strRPSLName = "Exemption Limits - " + strRPSL;
		objRPSLPage.removeRealPropertySettingEntry(strRPSL);
		
		//Step5: Creating the RPSL record for current year
		ReportLogger.INFO("Adding current year's 'Real Property Settings' record");
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLdataMap,strRPSL);
		
		//Step6: Clicking on Save button & Verifying the RPSL record for current year after creation
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSLName + "\" was created.","SMAB-T536 : Verify the User is able to create Exemption limit record");
		
		//Step7: Selecting module & 'All' List View
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		objRPSLPage.displayRecords("All");
				
		//Step8: Fetching value of RPSL created in above Test case
		String value = strRPSLName;
		objRPSLPage.searchRecords(strRPSLName);		
		
		// Step9: Searching and selecting the RPSL for Editing
		ReportLogger.INFO("Selecting RPSL record: '" + value + "' for editing");
		objRPSLPage.clickShowMoreLink(value);
		objPage.clickAction(objPage.waitForElementToBeClickable(objRPSLPage.editLinkUnderShowMore));

		//Step10: Fetching Data to Edit RPSL Record
		Map<String, String> editRPSLDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToEditRPSLEntryWithZeroAmt");	
		
		//Step11: Entering Data to Edit RPSL Record
		ReportLogger.INFO("Entering Data");
		objRPSLPage.enter(objRPSLPage.dvLowIncomeExemptionAmountEditBox,editRPSLDataMap.get("DV Low Income Exemption Amount"));
		objRPSLPage.enter(objRPSLPage.dvBasicIncomeExemptionAmountEditBox,editRPSLDataMap.get("DV Basic Exemption Amount"));
		objRPSLPage.enter(objRPSLPage.dvLowIncomeHouseholdLimitEditBox,editRPSLDataMap.get("DV Low Income Household Limit Amount"));		
		
		//Step12: Clicking on Save Button
		ReportLogger.INFO("Clicked on Save button");
		objPage.Click(objRPSLPage.saveButton);
		Thread.sleep(2000);

		//Step13: Validate the error message appeared for Amount fields
		String expectedDvLowIncomeExemptionAmountMessage = "Low Income Exemption Amount should be greater than 0";
		String expectedDvBasicIncomeExemptionAmountMessage = "Basic Exemption Amount should be greater than 0";
		String expectedDvLowIncomeHouseholdLimitMessage = "Low Income Household Limit should be greater than 0";		
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Exemption Amount"),expectedDvLowIncomeExemptionAmountMessage,"SMAB-T542:Verify the User is not able to edit and save Exemption limit record for a roll year when entered value is $0");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Basic Exemption Amount"),expectedDvBasicIncomeExemptionAmountMessage,"SMAB-T542:Verify the User is not able to edit and save Exemption limit record for a roll year when entered value is $0");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Household Limit"),expectedDvLowIncomeHouseholdLimitMessage,"SMAB-T542:Verify the User is not able to edit and save Exemption limit record for a roll year when entered value is $0");
		
		//Step14: Closing the RPSL creation pop up
		objPage.Click(objRPSLPage.cancelButton);
		
		objRPSLPage.logout();
	
	}
	
	
	/**
	 Below test case is used to validate 8 years of Real Property Settings
	 **/
	@Test(description = "SMAB-T583: Verify user is able to view at least last 8 years of Exemption Limits records", groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class )
	public void DisabledVeteran_verify8YearsRPSL(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Select All List View
		objRPSLPage.displayRecords("All");
		
		//Step4: Verify atleast 8 RPSL are displayed
		int noOfRPSL = objRPSLPage.numberOfRPSL.size();
		
		softAssert.assertTrue(noOfRPSL>=8, "SMAB-T583: Verify user is able to view at least last 8 years of Exemption Limits records");
		
		objRPSLPage.logout();
	}
	
	/**
	 Below test case is used to validate
	 1. 'Real Property Settings: Exemption Limits' record 'Status' field validation and it gets locked once 'Approved'
	 2. Non-System Admin users are not able to update a locked 'Real Property Settings' record
	 **/
	@Test(description = "SMAB-T640,SMAB-T641: Verify 'Real Property Settings: Exemption Limits' record 'Status' field validation and it gets locked once 'Approved'", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class)
	public void DisabledVeteran_verifyEditRPSLUsersAccess(String loginUser) throws Exception {		
		
		String currentRollYear="2021";
		String pastRollYear="2015";
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Select All List View
		ReportLogger.INFO("Selecting 'All' List View");
		objRPSLPage.displayRecords("All");
		
		//Step4 : Click on Approved RPSL for past Roll Year
		ReportLogger.INFO("Clicking on Approved RPSL Link for past Roll Year i.e. " + pastRollYear);
		objPage.Click(objRPSLPage.getRPSLRecord(pastRollYear));
		
		//Step5 : Click on EDIT button for the record and retrieve the error message
		ReportLogger.INFO("Clicking on 'Edit' button for record whose status is 'Approved'");
		objPage.Click(objRPSLPage.editButton);
		objPage.Click(objRPSLPage.saveButton);
		String expectedErrorMessageOnTop = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nRecord is locked. Please check with your system administrator.";
		
		//Step6: Verify Error message
		softAssert.assertEquals(objPage.getElementText(objApasgenericpage.pageError),expectedErrorMessageOnTop,"SMAB-T640,SMAB-T641:Verify 'Real Property Settings: Exemption Limits' record 'Status' field validation and it gets locked once 'Approved'");
		
		//Step7: Closing the Error Pop-Up
	    Thread.sleep(3000);
	    objPage.Click(objRPSLPage.closeErrorPopUp);
		
		//Step8 : Click on Approved RPSL for current Roll Year
	  	objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
	  	ReportLogger.INFO("Selecting 'All' List View");
	  	objRPSLPage.displayRecords("All");
		ReportLogger.INFO("Clicking on Approved RPSL Link for current Roll Year i.e. " + currentRollYear);
		objRPSLPage.searchRecords("Exemption Limits - " + currentRollYear);
		objPage.Click(objRPSLPage.getRPSLRecord(currentRollYear));
		
		//Step9 : Click on EDIT button for the record and retrieve the error message after clicking SAVE button
		ReportLogger.INFO("Clicking on 'Edit' button for record whose status is 'Approved'");
		objPage.Click(objRPSLPage.editButton);
		objPage.Click(objRPSLPage.saveButton);
		Thread.sleep(3000);
		String expectedErrorMessageOnTop1 = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nRecord is locked. Please check with your system administrator.";
		
		//Step10: Verify Error message
		softAssert.assertEquals(objPage.getElementText(objApasgenericpage.pageError),expectedErrorMessageOnTop1,"SMAB-T640,SMAB-T641:Verify 'Real Property Settings: Exemption Limits' record 'Status' field validation and it gets locked once 'Approved'");
		//softAssert.assertEquals(actualErrorMsgText2,expectedErrorMessage2,"SMAB-T641:Verify that Non-System Admin users are not able to update a locked 'Real Property Settings' record");
				
	    //Step11: Closing the Screen
	    Thread.sleep(1000);
	    objPage.Click(objRPSLPage.cancelButton);
	    
	    objRPSLPage.logout();
	}
	
	
	/**
	 Below test case is used to validate user other than Exemption Support Staff is only able to view Exemption limit records
	 **/
	@Test(description = "SMAB-T545: Verify user other than Exemption Support Staff is only able to view Exemption limit records", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class )
	public void DisabledVeteran_verifyViewRPSLUsersAccess(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objRPSLPage.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objRPSLPage.searchModule(modules.EFILE_INTAKE_VIEW); //To close split view mode
		objRPSLPage.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Select All List View
		objRPSLPage.displayRecords("All");
		Thread.sleep(2000);
		
		//Step4: Verify New Button is not present
		boolean flag = objRPSLPage.verifyElementVisible(objRPSLPage.newButton);
		softAssert.assertEquals(flag, false, "SMAB-T545:  Verify user cannot create RPSL");
		
		// Step5: Clicking on 'first' Exemption Limits record 
		objPage.locateElement("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table//tbody//tr[1]//th//a",3);
		WebElement exemptionLink = driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table//tbody//tr[1]//th//a"));		  				  
		System.out.println("Exemption Link text: " + exemptionLink.getText());
		objPage.Click(exemptionLink);
		
		//Step6: Verify Edit Button is not present
			flag = objRPSLPage.verifyElementVisible(objRPSLPage.editButton);
			softAssert.assertEquals(flag, false, "SMAB-T545: Verify user cannot edit RPSL");
	
		//Step7: Verify User can view ExemptionLimits record
			Thread.sleep(2000);
			boolean elePresence =objPage.verifyElementVisible(objRPSLPage.detailsTabLabel);
			//boolean elePresence = objRPSLPage.detailsTabLabel.isDisplayed();
			softAssert.assertTrue(elePresence, "SMAB-T545: Verify user other than Exemption Support Staff is only able to view Exemption limit records");
			
			objRPSLPage.logout();
			
	}
}