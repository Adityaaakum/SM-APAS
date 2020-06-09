package com.apas.Tests.ApasSettings;

import java.io.IOException;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.RealPropertySettingsLibrariesPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class RealPropertySettingsLibrariesTest extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	RealPropertySettingsLibrariesPage objRPSLPage;
	SoftAssertion softAssert;
	Util objUtils;
	Map<String, String> dataMap;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objUtils = new Util();
		objRPSLPage = new RealPropertySettingsLibrariesPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		softAssert = new SoftAssertion();
	}
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business admin and appraisal support in an array
	 **/
	@DataProvider(name = "loginUsers")
	public Object[][] dataProviderLoginUserMethod() {
		return new Object[][] { { users.EXEMPTION_SUPPORT_STAFF } };
	}	    
    /**
	 Below test case will validate that 
	 1. user is able to create Real Property Settings Library for Fututre Roll Year
	 2. User is not able to create duplicate Exemption limit record for any random roll year
	 3. User is not able to create duplicate Exemption limit record for a roll year whose entry already exists	 
	 **/
	@Test(description = "SMAB-T536: Creation of Future Real Property Settings", dataProvider = "loginUsers", groups = {"smoke","regression","DisabledVeteranExemption"})
	public void verifyFututreRPSLCreation(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching Data to create RPSL record			
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		dataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateFututreRPSLEntry");
		String strRPSLName = "Exemption Limits - " + dataMap.get("Roll Year Settings");
		String strRPSL = dataMap.get("Roll Year Settings");
		
		//Step4: Delete this Roll Year's RPSL if it already exists	
		objRPSLPage.verifyAndDeleteExistingRPSL(strRPSL);
		
		//Step5: Adding a new record	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding a new Future 'Real Prpoerty Settings' record");		
		objRPSLPage.enterRealPropertySettingsDetails(dataMap);
		
		//Step6: Clicking on Save button and verifying Success Message
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSL + "\" was created.","SMAB-T536: Verify the User is able to create Exemption limit record for a past and future roll year");	
				
		//Step7: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step8: Create RPSL again for same Roll Year
		objRPSLPage.enterRealPropertySettingsDetails(dataMap);
		
		//Step9: Clicking on Save button
		objPage.Click(objRPSLPage.saveButton);
		
		//Step10: Verify Duplicate Future Roll Year cannot be created
		String expectedWarningMessageOnTop = "This record looks like a duplicate.View Duplicates";
		objPage.waitForElementToBeVisible(objRPSLPage.warningMsgOnTop, 30);
		softAssert.assertEquals(objRPSLPage.warningMsgOnTop.getText(),expectedWarningMessageOnTop,"SMAB-T540:Verify the User is not able to create duplicate Exemption limit record for any random roll year");
		softAssert.assertEquals(objRPSLPage.warningMsgOnTop.getText(),expectedWarningMessageOnTop,"SMAB-T541:Verify the User is not able to create duplicate Exemption limit record for a roll year whose entry already exists");
	
		System.setProperty("futureYearRPSL", strRPSLName);
		
		//Step11: Closing the RPSL creation pop up
		objPage.Click(objRPSLPage.cancelButton);
		
		objApasGenericFunctions.logout();
	}	
	
	/**
	 Below test case will validate that 
	 1. User is able to create Exemption limit record for the current roll year
	 **/
	@Test(description = "SMAB-T535: Creating Current Roll Year's Real Property Settings",groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void verifyCurrentYearRPSLCreation(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		
		// Step1: Login to the APAS application using the credentials passed through		
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//----- This Functions needs to be verified If we're taking this or Sikander's---
		//Step3: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step4: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		dataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateCurrentRPSLEntry");
		
		//Step5: Delete current Roll Year's RPSL if it already exists	
		String strRPSLName = "Exemption Limits - " + dataMap.get("Roll Year Settings");
		String strRPSL = dataMap.get("Roll Year Settings");
		objRPSLPage.verifyAndDeleteExistingRPSL(strRPSL);
		
		//Step6: Creating the RPSL record for current year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding current year's 'Real Property Settings' record");
		objRPSLPage.enterRealPropertySettingsDetails(dataMap);
		
		//Step7: Clicking on Save button & Verifying the RPSL record for current year after creation
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSL + "\" was created.","SMAB-T535:Verify the User is able to create Exemption limit record for the current roll year");	
		
		//Step8: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step9: Create RPSL again for current Roll Year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Again adding current year's 'Real Property Settings' record");
		objRPSLPage.enterRealPropertySettingsDetails(dataMap);
		
		//Step10: Clicking on Save button
		objPage.Click(objRPSLPage.saveButton);		
		
		//Step11: Verify Duplicate Current Roll Year cannot be created
		String expectedWarningMessageOnTop = "This record looks like a duplicate.View Duplicates";
		objPage.waitForElementToBeVisible(objRPSLPage.warningMsgOnTop, 30);
		softAssert.assertEquals(objRPSLPage.warningMsgOnTop.getText(),expectedWarningMessageOnTop,"SMAB-T539:Verify the User is not able to create duplicate Exemption limit record for current roll year");
		objPage.Click(objRPSLPage.cancelButton);
		
		System.setProperty("currentYearRPSL", "strRPSLName");		
		objApasGenericFunctions.logout();
	}
		
	/**
	 Below test case is used to validate the validation rules on Real Property Settings Library screen
	 **/
	@Test(description = "SMAB-T544: Mandatory Field Validation while creating Real Property Settings", groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void validateMandatoryFieldErrorsRPSLCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);

		//Step3: Open and save Real Property Settings Libraries form without entering the data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on New Button");
		objPage.Click(objRPSLPage.newButton);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Save Button without entering any details");
		Thread.sleep(2000);
		objPage.Click(objRPSLPage.saveButton);

		//Step4: Validate the error message appeared for mandatory fields
		String expectedErrorMessageOnTop = "These required fields must be completed: DV Basic Exemption Amount, DV Low Income Exemption Amount, DV Low Income Household Limit, RP Setting Name, Roll Year Settings, Status";
		String expectedIndividualFieldMessage = "Complete this field";
		objPage.waitForElementToBeVisible(objRPSLPage.errorMsgOnTop, 30);
		softAssert.assertEquals(objRPSLPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop,"SMAB-T544: Validating mandatory fields missing error in manual entry pop up header.");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("RP Setting Name"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'RP Setting Name'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("Status"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'Status'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("Roll Year Settings"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'Roll Year Settings'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Exemption Amount"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'DV Low Income Exemption Amount'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Basic Exemption Amount"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'DV Basic Exemption Amount'");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Household Limit"),expectedIndividualFieldMessage,"SMAB-T544: Validating mandatory fields missing error for 'DV Low Income Household Limit'");

		//Step5: Closing the RPSL creation pop up
		objPage.Click(objRPSLPage.cancelButton);
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate User is able to edit Exemption limit record for the current roll year
	 **/
	@Test(description = "SMAB-T535: Verify Editing of Real Property Settings Libraries", groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void editRPSL(String loginUser) throws Exception {
	
		String strSuccessAlertMessage;
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateCurrentRPSLEntry");
		
		//Step4: Delete current Roll Year's RPSL if it already exists	
		String strRPSLName = "Exemption Limits - " + createRPSLdataMap.get("Roll Year Settings");
		String strRPSL = createRPSLdataMap.get("Roll Year Settings");
		objRPSLPage.verifyAndDeleteExistingRPSL(strRPSL);
		
		//Step5: Creating the RPSL record for current year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding current year's 'Real Property Settings' record");
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLdataMap);
		
		//Step6: Clicking on Save button & Verifying the RPSL record for current year after creation
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSL + "\" was created.","SMAB-T535:Verify the User is able to create Exemption limit record for the current roll year");	
		
		//Step7: Selecting module & 'All' List View
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		objApasGenericFunctions.displayRecords("All");
		
		//Step8: Fetching value of RPSL created in above Test case
		String value = strRPSLName;
		objApasGenericFunctions.searchRecords(strRPSLName);
		//Step9: Searching and selecting the RPSL
		objRPSLPage.clickShowMoreLink(value);
		objPage.clickAction(objPage.waitForElementToBeClickable(objRPSLPage.editLinkUnderShowMore));
		objPage.waitUntilPageisReady(driver);

		//Step510: Fetching the data to edit the RPSL record
		//String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> editRPSDataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToEditRPSLEntry");	
		
		//Step11: Edit the RPSL
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the Status field to '"+editRPSDataMap.get("Status") + "'");
		objRPSLPage.selectFromDropDown(objRPSLPage.statusDropDown,editRPSDataMap.get("Status"));		
		
		//Step12: Saving the RPSL after editing 'Status' dropdown
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		System.out.println("success message is :"+strSuccessAlertMessage);
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + value + "\" was saved.","SMAB-T535: Verify the User is able to edit Exemption limit record for the current roll year");			
		
		objApasGenericFunctions.logout();
	
	}
	
	
	
	/**
	 Below test case will validate that user is not able to create Real Property Settings Library if 'Cancel' button is clicked instead of 'Save' button
	 **/
	@Test(description = "SMAB-T537: Verify Current Roll Year RPSL not created on clicking Cancel button", dataProvider = "loginUsers", groups = {"smoke","regression","DisabledVeteranExemption"})
	public void verifyCancelRPSLCreation(String loginUser) throws Exception {
		boolean strSuccessAlert;
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching the data to create RPSL record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding a new 'Real Proerty Settings' record");
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		dataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateFututreRPSLEntry");
		String strRPSL = dataMap.get("Roll Year Settings");
		
		//Step4: Clicking on 'New' button & entering the details
		objRPSLPage.enterRealPropertySettingsDetails(dataMap);
		
		//Step5: Clicking on Cancel Button and verifying that record is not created
		strSuccessAlert = objRPSLPage.cancelRealPropertySettings();	
		softAssert.assertEquals(strSuccessAlert, false, "SMAB-T537: Verify Exemption limit record for the current roll year is not created when User clicks Cancel button");
		objApasGenericFunctions.logout();
	}		
	
	
	 /**
	 Below test case will validate that user is not able to create RPSL having $0 in amount fields
	 **/
	@Test(description = "SMAB-T538: Creation of RPSL with $0 in amount fields", dataProvider = "loginUsers", groups = {"smoke","regression","DisabledVeteranExemption"})
	public void verifyValidationRulesOnRPSLCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching Data to create RPSL record	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding a new 'Real Proerty Settings' record");
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		dataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateRPSLEntryWithZeroAmt");
		
		//Step4: Adding a new record
		objRPSLPage.enterRealPropertySettingsDetails(dataMap);
		
		//Step5: Clicking on Save button
		objPage.Click(objRPSLPage.saveButton);
		Thread.sleep(2000);

		//Step6: Validate the error message appeared for mandatory fields
		String expectedDvLowIncomeExemptionAmountMessage = "Low Income Exemption Amount should be greater than 0";
		String expectedDvBasicIncomeExemptionAmountMessage = "Basic Exemption Amount should be greater than 0";
		String expectedDvLowIncomeHouseholdLimitMessage = "Low Income Household Limit should be greater than 0";		
		String expectedDVAnnualLowIncomeDueDateMessage = "DV Annual Due Date's year must be the same as the associated roll year.";
		String expectedDVAnnualLowIncomeDueDate2Message = "DV Annual Due Date 2's year must be the same as the associated roll year.";
			
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Exemption Amount"),expectedDvLowIncomeExemptionAmountMessage,"SMAB-T538: Verify the User is not able to create Exemption limit record with $0 values");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Basic Exemption Amount"),expectedDvBasicIncomeExemptionAmountMessage,"SMAB-T538: Verify the User is not able to create Exemption limit record with $0 values");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Low Income Household Limit"),expectedDvLowIncomeHouseholdLimitMessage,"SMAB-T538: Verify the User is not able to create Exemption limit record with $0 values");
		
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Annual Due Date"),expectedDVAnnualLowIncomeDueDateMessage,"SMAB-T544: Verify the validation rules on Real Property Settings Library screen");
		softAssert.assertEquals(objRPSLPage.getIndividualFieldErrorMessage("DV Annual Due Date 2"),expectedDVAnnualLowIncomeDueDate2Message,"SMAB-T544: Verify the validation rules on Real Property Settings Library screen");

		//Step5: Closing the RPSL creation pop up
		objPage.Click(objRPSLPage.cancelButton);
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate that User is not able to edit and save Exemption limit record for a roll year when entered value is $0
	 **/
	@Test(description = "SMAB-T542:Verify the User is not able to edit and save Exemption limit record for a roll year when entered value is $0", groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void verifyValidationRulesOnRPSLEditing(String loginUser) throws Exception {
		String strSuccessAlertMessage;
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Fetching data for new record
		String manualEntryData = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;		
		Map<String, String> createRPSLdataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToCreateRPSLEntryForValidation");
		
		//Step4: Delete current Roll Year's RPSL if it already exists	
		String strRPSLName = "Exemption Limits - " + createRPSLdataMap.get("Roll Year Settings");
		String strRPSL = createRPSLdataMap.get("Roll Year Settings");
		objRPSLPage.verifyAndDeleteExistingRPSL(strRPSL);
		
		//Step5: Creating the RPSL record for current year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding current year's 'Real Property Settings' record");
		objRPSLPage.enterRealPropertySettingsDetails(createRPSLdataMap);
		
		//Step6: Clicking on Save button & Verifying the RPSL record for current year after creation
		strSuccessAlertMessage = objRPSLPage.saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRPSL + "\" was created.","SMAB-T535:Verify the User is able to create Exemption limit record for the current roll year");	
		
		//Step7: Selecting module & 'All' List View
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		objApasGenericFunctions.displayRecords("All");
				
		//Step8: Fetching value of RPSL created in above Test case
		String value = strRPSLName;
				
		// Step9: Searching and selecting the RPSL for Editing
		ExtentTestManager.getTest().log(LogStatus.INFO, "Selecting RPSL record: '" + value + "' for editing");
		objRPSLPage.clickShowMoreLink(value);
		objPage.clickAction(objPage.waitForElementToBeClickable(objRPSLPage.editLinkUnderShowMore));
		objPage.waitUntilPageisReady(driver);

		//Step10: Fetching Data to Edit RPSL Record
		dataMap = objUtils.generateMapFromJsonFile(manualEntryData, "DataToEditRPSLEntryWithZeroAmt");	
		
		//Step11: Entering Data to Edit RPSL Record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Entering Data");
		objRPSLPage.clearAndEnterValue(objRPSLPage.dvLowIncomeExemptionAmountEditBox,dataMap.get("DV Low Income Exemption Amount"));
		objRPSLPage.clearAndEnterValue(objRPSLPage.dvBasicIncomeExemptionAmountEditBox,dataMap.get("DV Basic Exemption Amount"));
		objRPSLPage.clearAndEnterValue(objRPSLPage.dvLowIncomeHouseholdLimitEditBox,dataMap.get("DV Low Income Household Limit Amount"));		
		
		//Step12: Clicking on Save Button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicked on Save button");
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
		
		objApasGenericFunctions.logout();
	
	}
	
	
	/**
	 Below test case is used to validate 8 years of Real Property Settings
	 **/
	@Test(description = "SMAB-T583: Verify user is able to view at least last 8 years of Exemption Limits records", groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void verify8YearsRPSL(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Select All List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step4: Verify 8 RPSL are displayed
		boolean noOfRPSL = objRPSLPage.verify8YearsRPSL();
		
		softAssert.assertEquals(noOfRPSL, true, "SMAB-T583: Verify user is able to view at least last 8 years of Exemption Limits records");
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the users business admin and appraisal support in an array
	 **/
	@DataProvider(name = "loginUsers1")
	public Object[][] dataProviderVerifyEditAccessWithUsers() {
		return new Object[][] { { users.EXEMPTION_SUPPORT_STAFF } };
	}
	/**
	 Below test case is used to validate
	 1. 'Real Property Settings: Exemption Limits' record 'Status' field validation and it gets locked once 'Approved'
	 2. Non-System Admin users are not able to update a locked 'Real Property Settings' record
	 **/
	@Test(description = "SMAB-T640,641: Verify 'Real Property Settings: Exemption Limits' record 'Status' field validation and it gets locked once 'Approved'", groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginUsers1")
	public void verifyEditRPSLUsersAccess(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Select All List View
		ExtentTestManager.getTest().log(LogStatus.INFO, "Selecting 'All' List View");
		objApasGenericFunctions.displayRecords("All");
		
		//Step4: Calculate Number of RPSL records
		//int numberOfRPSL = objRPSLPage.numberOfRPSL.size();
		
		//step5 : Verify Edit Access for 'Approved' RPSL
		String actualMsg = objRPSLPage.verifyEditAccess();
		
		// Step6: Verify Error message
		String expectedErrorMessage = "You do not have the level of access necessary to perform the operation you requested. Please contact the owner of the record or your administrator if access is necessary.";
	    softAssert.assertEquals(actualMsg,expectedErrorMessage,"SMAB-T640:Verify 'Real Property Settings: Exemption Limits' record 'Status' field validation and it gets locked once 'Approved'");
	    softAssert.assertEquals(actualMsg,expectedErrorMessage,"SMAB-T641:Verify that Non-System Admin users are not able to update a locked 'Real Property Settings' record");
		
	    //Step7: Closing the Error Pop-Up
	    Thread.sleep(3000);
	    objPage.Click(objRPSLPage.closeErrorPopUp);
	    
	    objApasGenericFunctions.logout();
	}
	
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the users business admin and appraisal support in an array
	 **/
	@DataProvider(name = "loginUsers2")
	public Object[][] dataProviderVerifyViewAccessWithUsers() {
		return new Object[][] { { users.PRINCIPAL_USER }};
	}
	/**
	 Below test case is used to validate user other than Exemption Support Staff is only able to view Exemption limit records
	 **/
	@Test(description = "SMAB-T545: Verify user other than Exemption Support Staff is only able to view Exemption limit records", groups = {"smoke","regression","DisabledVeteranExemption"}, dataProvider = "loginUsers2")
	public void verifyViewRPSLUsersAccess(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the credentials passed through data provider (ExemptionSupportStaff)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step3: Select All List View
		objApasGenericFunctions.displayRecords("All");
		Thread.sleep(2000);
		
		//Step4: Verify New Button is not present
		boolean flag = objRPSLPage.verifyButtonNotPresent("//a[@title = 'New']");
		softAssert.assertEquals(flag, false, "SMABT:545 - Verify user cannot create RPSL");
		
		// Step5: Clicking on 'first' Exemption Limits record 
		  objPage.locateElement("//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr[1]//th//a",3);
		  WebElement exemptionLink = driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr[1]//th//a"));		  				  
		  System.out.println("Exemption Link text: " + exemptionLink.getText());
		  objPage.Click(exemptionLink);
		  objPage.waitUntilPageisReady(driver);
		
		//Step6: Verify Edit Button is not present
			flag = objRPSLPage.verifyButtonNotPresent("//div[contains(@class,'windowViewMode-normal')]//button[text() = 'Edit']");
			softAssert.assertEquals(flag, false, "SMABT:545 - Verify user cannot edit RPSL");
	
		//Step7: Verify User can view ExemptionLimits record
			Thread.sleep(2000);
			boolean elePresence =objPage.verifyElementVisible(objRPSLPage.detailsTabLabel);
			//boolean elePresence = objRPSLPage.detailsTabLabel.isDisplayed();
			softAssert.assertTrue(elePresence, "SMAB-T545: Verify user other than Exemption Support Staff is only able to view Exemption limit records");
			
			objApasGenericFunctions.logout();
			
	}
	
	

}