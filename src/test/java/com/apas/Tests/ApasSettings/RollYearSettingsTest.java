package com.apas.Tests.ApasSettings;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.RollYearSettingsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class RollYearSettingsTest extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	RollYearSettingsPage objRollYearSettingsPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYearData;
	
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objRollYearSettingsPage = new RollYearSettingsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYearData = System.getProperty("user.dir") + testdata.ROLL_YEAR_DATA;
	}
	
	/**
	 Below test case is used to validate 
	 -Error message when no mandatory fields are entered in Roll Year record record before saving it
	 -Create Roll Year record for Future Year
	 **/
	
	@Test(description = "SMAB-T638: Validate that System Admin is able to create Future Roll Year record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginSystemAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_RollYear_CreateFutureRecord(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Save an Roll Year Settings record without entering any details		
		objRollYearSettingsPage.saveRollYearRecordWithNoValues();
		
		//Step4: Validate error messages when no field value is entered and Roll Year Settings record is saved
		String expectedErrorMessageOnTop = "These required fields must be completed: Calendar End Date, Calendar Start Date, Open Roll End Date, Lien Date, Roll Year Settings, Roll Year, Open Roll Start Date, Tax End Date, Tax Start Date";
		String expectedIndividualFieldMessage = "Complete this field";
		softAssert.assertEquals(objRollYearSettingsPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop,"SMAB-T638: Validating mandatory fields missing error in Roll Year Settings screen.");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Calendar End Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Calendar End Date'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Calendar Start Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Calendar Start Date'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Open Roll End Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Open Roll End Date'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Lien Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Lien Date'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Roll Year Settings"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Roll Year Settings'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Roll Year"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Roll Year'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Open Roll Start Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Open Roll Start Date'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Tax End Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Tax End Date'");
		softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Tax Start Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Tax Start Date'");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Roll Year screen");
		objPage.Click(objRollYearSettingsPage.cancelButton);
		
		//Step5: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)
		Map<String, String> dataToCreateFutureRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreateFutureRollYear");
		
		//Step6: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataToCreateFutureRollYearMap.get("Roll Year"));
		
		//Step7: Delete the existing Roll Year record
		objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToCreateFutureRollYearMap.get("Roll Year"), "Delete");
		
		//Step8: Change the List view and Create Roll Year record
		objApasGenericFunctions.displayRecords("Recently Viewed");
		objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToCreateFutureRollYearMap, "New");
			
		//Step9: Capture the record id and Roll Year Settings Name
		String recordId = objApasGenericPage.getCurrentRecordId(driver, "Roll Year");
		Thread.sleep(2000);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataToCreateFutureRollYearMap.get("Status"), "SMAB-T638: Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataToCreateFutureRollYearMap.get("Roll Year"), "SMAB-T638: Roll Year record is created successfully");
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate 
	 -Create Roll Year record for past year
	 -Edit a Roll Year record
	 **/
	
	@Test(description = "SMAB-T638: Validate that System Admin is able to create Past Roll Year record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginSystemAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_RollYear_CreateAndEditPastRecord(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
		Map<String, String> dataToCreatePastRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreatePastRollYear");
		
		//Step4: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataToCreatePastRollYearMap.get("Roll Year"));
		
		//Step5: Delete the existing Roll Year record
		objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToCreatePastRollYearMap.get("Roll Year"), "Delete");
		
		//Step6: Change the List view and Create Roll Year record
		objApasGenericFunctions.displayRecords("Recently Viewed");
		objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToCreatePastRollYearMap, "New");
			
		//Step7: Capture the record id and validate its details
		String recordId = objApasGenericPage.getCurrentRecordId(driver, "Roll Year");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataToCreatePastRollYearMap.get("Status"), "SMAB-T638: Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataToCreatePastRollYearMap.get("Roll Year"), "SMAB-T638: Roll Year record is created successfully");
		String rollYearName = objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage));
		
		//Step8: Validate the default List View and Create a duplicate record
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.recentlyViewedListView)), "Recently Viewed", "SMAB-T638: Default List View is validated successfully");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to open a Roll Year record");
		Thread.sleep(2000);
		objPage.Click(objPage.waitForElementToBeClickable(objRollYearSettingsPage.newRollYearButton));
		objPage.waitForElementToBeClickable(objRollYearSettingsPage.rollYearSettings);
		objPage.waitForElementToBeClickable(objRollYearSettingsPage.rollYear);
		
		//Step9: Validate no error message is displayed on entering the Roll Year Settings value
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter 'Roll Year Settings' and 'Calendar End Date' values only");
		objPage.enter(objRollYearSettingsPage.rollYearSettings, dataToCreatePastRollYearMap.get("Roll Year Settings"));
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.calendarEndDate, dataToCreatePastRollYearMap.get("Calendar End Date"));
		Thread.sleep(1000);
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.duplicateRecord), "Validate no duplicate error message is displayed");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.viewDuplicateRecord), "Validate no duplicate error view link is displayed");
		
		//Step10: Validate the error message is displayed on selecting the duplicate Roll Year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter 'Roll Year' and 'Fiscal End Date' values only");
		objRollYearSettingsPage.selectFromDropDown(objRollYearSettingsPage.rollYear, dataToCreatePastRollYearMap.get("Roll Year"));
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.fiscalEndDate, dataToCreatePastRollYearMap.get("Fiscal End Date"));
		Thread.sleep(2000);
		softAssert.assertTrue(objRollYearSettingsPage.duplicateRecord.isDisplayed(), "Validate duplicate error message is displayed as Roll Year record exist");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.duplicateRecord)),"This record looks like a duplicate.View Duplicates", "SMAB-T638: Validate duplicate error message text");
		softAssert.assertTrue(objRollYearSettingsPage.viewDuplicateRecord.isDisplayed(), "Validate duplicate error view link is displayed as Roll Year record exist");

		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Roll Year screen");
		objPage.Click(objRollYearSettingsPage.cancelButton);
		
		//Step11: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
				
		//Step12: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json) 
		Map<String, String> dataToEditPastRollYearToFutureMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToEditPastRollYearToFuture");
				
		//Step13: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataToEditPastRollYearToFutureMap.get("Roll Year"));
					
		//Step14: Delete the existing Roll Year record
		objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToEditPastRollYearToFutureMap.get("Roll Year"), "Delete");
				
		//Step15: Search the existing Roll Year Settings record
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(rollYearName);
		objRollYearSettingsPage.openRollYearRecord(rollYearName);
				
		//Step16: Editing this Roll Year record using EDIT button on Roll Year detail screen
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Edit' pencil icon to update it");
		objPage.Click(objRollYearSettingsPage.editPencilIconForRollYearOnDetailPage);
				
		//Step17: Clear the values from few of the mandatory fields and Save the record
		Thread.sleep(1000);
		objPage.clearFieldValue(objRollYearSettingsPage.calendarStartDateOnDetailEditPage);
		objPage.clearFieldValue(objRollYearSettingsPage.calendarEndDateOnDetailEditPage);
		objPage.clearFieldValue(objRollYearSettingsPage.taxStartDateOnDetailEditPage);
		objPage.Click(objRollYearSettingsPage.saveButtonOnDetailPage);
		objPage.Click(objRollYearSettingsPage.saveButtonOnDetailPage);
				
		//Step18: Validate the error message appears as a pop-up at the bottom of the screen		
		Thread.sleep(2000);
		softAssert.assertTrue(driver.findElements(By.xpath("//h2[@class='slds-truncate slds-text-heading_medium']")).size() == 1, "SMAB-T638: Validate error message pop-up that appear at the bottom of the page i.e. 'We hit a snag'");
		softAssert.assertTrue(driver.findElements(By.xpath("//a[contains(text(), 'Calendar Start Date')]")).size() == 1, "SMAB-T638: Validate that 'Calendar Start Date' appears in error message pop-up");
		softAssert.assertTrue(driver.findElements(By.xpath("//a[contains(text(), 'Calendar End Date')]")).size() == 1, "SMAB-T638: Validate that 'Calendar End Date' appears in error message pop-up");
		softAssert.assertTrue(driver.findElements(By.xpath("//a[contains(text(), 'Tax Start Date')]")).size() == 1, "SMAB-T638: Validate that 'Tax Start Date' appears in error message pop-up");
				
		//Step19: Click CANCEL button and edit the record
		objPage.Click(objRollYearSettingsPage.cancelButtonOnDetailPage);
		Thread.sleep(1000);
		objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToEditPastRollYearToFutureMap, "Edit");
					
		//Step20: Validate the details post the record is updated and saved
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataToEditPastRollYearToFutureMap.get("Status"), "SMAB-T638: Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataToEditPastRollYearToFutureMap.get("Roll Year"), "SMAB-T638: Roll Year record is created successfully");
				
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate 
	-Field level validations (error messages)
	 **/
	
	@Test(description = "SMAB-T638, SMAB-T1283: Validate field level validations on Roll Year Screen", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginSystemAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_RollYear_FieldLevelValidations(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
		Map<String, String> dataToValidateFieldLevelErrorMessagesMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToValidateFieldLevelErrorMessages");
		
		//Step4: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataToValidateFieldLevelErrorMessagesMap.get("Roll Year"));
		
		//Step5: Delete the existing Roll Year record
		objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToValidateFieldLevelErrorMessagesMap.get("Roll Year"), "Delete");
				
		//Step6: Change the List view and Create Roll Year record
		objApasGenericFunctions.displayRecords("Recently Viewed");
		objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToValidateFieldLevelErrorMessagesMap, "New");
		
		//Step7: Validate error messages displayed at field level
		Thread.sleep(1000);
		softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Fiscal Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate1.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate1.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
		
		//Step8: Enter a different 'Calendar End Date' and Save the record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter a different value for 'Calendar End Date' i.e. 12/31/2010 and click SAVE button to validate a different error message");
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.calendarEndDate, "12/31/2010");
		objPage.Click(objRollYearSettingsPage.saveButton);
		
		//Step9: Validate error messages again that are displayed at field level
		Thread.sleep(2000);
		softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Fiscal Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate1.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate2.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
		
		//Step10: Enter a different 'Fiscal End Date' and Save the record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter a different value for 'Fiscal End Date' i.e. 12/31/2008 and click SAVE button to validate a different error message");
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.fiscalEndDate, "12/31/2008");
		objPage.Click(objRollYearSettingsPage.saveButton);
		
		//Step11: Validate error messages again that are displayed at field level
		Thread.sleep(2000);
		softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Fiscal Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate1.isDisplayed(), "SMAB-T638: Validate first error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate2.isDisplayed(), "SMAB-T638: Validate second error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate2.isDisplayed(), "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Roll Year screen");
		objPage.Click(objRollYearSettingsPage.cancelButton);
		
		objApasGenericFunctions.logout();	
	}

	
	/**
	 Below test case is used to validate 
	 -RP Business Admin and Exemption Support Staff are able to only view the Roll Year Record
	 **/
	
	@Test(description = "SMAB-T638: Validate RP Business admin and Exemption Support staff are able to view Roll year record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginRpBusinessAdminAndExemptionSupportUsers", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_RollYear_ViewRecord(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Validate NEW button doesn't appear on the screen  
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.newRollYearButton), "SMAB-T638: Validate NEW button is not displayed");
		objApasGenericFunctions.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.newRollYearButton), "SMAB-T638: Validate NEW button is not displayed");
		
		//Step4: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
		Map<String, String> dataToCreatePastRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreatePastRollYear");
		
		//Step5: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataToCreatePastRollYearMap.get("Roll Year"));
		
		//Step6: Delete the existing Roll Year record
		objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToCreatePastRollYearMap.get("Roll Year"), "Delete");
		
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 Below test case is used to validate create Roll Year record for current year
	 It has been commented out as we need to identify the environment where we can run this TEST.
	 Current Roll Year can have other instances linked like CPI factor and Exemption & Penalty Calculations due to which current Roll Year record can't be deleted and created again
	 **/
	
	/*@Test(description = "SMAB-T638: Validate user is able to create Roll year record for current year", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void verify_RollYear_CreateCurrentRecord(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json) and delete the existing Roll Year record
		//dataMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreateCurrentRollYear");
		Map<String, String> dataToCreateCurrentRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreateCurrentRollYear");
		
		objSalesforceAPI.delete("Roll_Year_Settings__c", "SELECT Id FROM Roll_Year_Settings__c Where Name = '" + dataToCreateCurrentRollYearMap.get("Roll Year") + "'");
		Thread.sleep(2000);
		
		//Step4: Create Roll Year record
		objRollYearSettingsPage.createRollYearRecord(dataToCreateCurrentRollYearMap);
			
		//Step5: Capture the record id and validate its details
		recordId = objRollYearSettingsPage.getCurrentUrl(driver);
		Thread.sleep(2000);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataToCreateCurrentRollYearMap.get("Status"), "Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataToCreateCurrentRollYearMap.get("Roll Year"), "Roll Year record is created successfully");
		
		objApasGenericFunctions.logout();
	}*/
}