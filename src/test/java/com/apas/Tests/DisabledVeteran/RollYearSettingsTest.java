package com.apas.Tests.DisabledVeteran;

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
	//SalesforceAPI objSalesforceAPI;
	Util objUtil;
	Map<String, String> dataMap;
	SoftAssertion softAssert;
	String rollYearData, recordId, rollYearName;
	
	
	@BeforeMethod
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
		//objSalesforceAPI = new SalesforceAPI();
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYearData = System.getProperty("user.dir") + testdata.ROLL_YEAR_DATA;
	}
	
	@AfterMethod
	public void afterMethod() throws IOException, InterruptedException{
		//objApasGenericFunctions.logout();
		softAssert.assertAll();
	}
	
	/**
	 * Below function will be used to login to application with different users
	 * @return Return the user Exemption Support Staff
	 **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] {{ users.SYSTEM_ADMIN }};
    }
    
    @DataProvider(name = "loginUsers1")
    public Object[][] dataProviderLoginUserMethod1() {
        return new Object[][] {{users.EXEMPTION_SUPPORT_STAFF}, {users.RP_BUSINESS_ADMIN}};
    }
    
	/**
	 Below test case is used to validate 
	 -Error message when no mandatory fields are entered in Roll Year record record before saving it
	 -Create Roll Year record for Future Year
	 **/
	
	@Test(description = "SMAB-T638: Disabled Veterans- Verify that System Admins are able to CRED(Create/Read/Edit/Delete) the 'Roll Year Settings' record", groups = {"smoke", "Regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void createFutureRollYearRecord(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Save an Roll Year Settings record without entering any details		
		objRollYearSettingsPage.saveRollYearRecordWithNoValues();
		
		//Step4: Validate error messages when no field value is entered and Roll Year Settings record is saved		
		List<String> errorsList = objRollYearSettingsPage.retrieveRollYearMandatoryFieldsValidationErrorMsgs();
		String expMsgInPopUpHeader = CONFIG.getProperty("expectedErrorMsgForAllMandatoryFieldsRollYearRecord");
		String actMsgInPopUpHeader = errorsList.get(0);
		softAssert.assertEquals(actMsgInPopUpHeader, expMsgInPopUpHeader, "Validate error message related to mandatory fields in Roll Year screen's header");		
		String expMsgForIndividualField = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryFieldExemptionRecord");
		String actMsgForIndividualField = errorsList.get(1);	
		softAssert.assertEquals(expMsgForIndividualField, actMsgForIndividualField, "Validate error message related to individual fields in Roll Year screen's layout");
		int fieldsCountInHeaderMsg = Integer.parseInt(errorsList.get(2));		
		int individualMsgsCount = Integer.parseInt(errorsList.get(3));
		softAssert.assertEquals(fieldsCountInHeaderMsg, individualMsgsCount, "Validate the count of field names in header message against the count of individual error message");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
		objRollYearSettingsPage.cancelRollYearRecord();
		
		//Step5: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)
		dataMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreateFutureRollYear");
		
		//Step6: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataMap.get("Roll Year"));
		
		//Step7: Delete the existing Roll Year record
		objRollYearSettingsPage.clickShowMoreButton(dataMap.get("Roll Year"), "Delete");
		
		//Step8: Change the List view and Create Roll Year record
		objApasGenericFunctions.displayRecords("Recently Viewed");
		objRollYearSettingsPage.createRollYearRecord(dataMap);
			
		//Step9: Capture the record id and Roll Year Settings Name
		recordId = objRollYearSettingsPage.getCurrentUrl(driver);
		Thread.sleep(2000);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataMap.get("Status"), "Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataMap.get("Roll Year"), "Roll Year record is created successfully");
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate 
	 -Create Roll Year record for past year
	 **/
	
	@Test(description = "SMAB-T638: Disabled Veterans- Verify that System Admins are able to CRED(Create/Read/Edit/Delete) the 'Roll Year Settings' record", groups = {"Regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void createPastRollYearRecord(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
		dataMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreatePastRollYear");
		
		//Step4: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataMap.get("Roll Year"));
		
		//Step5: Delete the existing Roll Year record
		objRollYearSettingsPage.clickShowMoreButton(dataMap.get("Roll Year"), "Delete");
		
		//Step6: Change the List view and Create Roll Year record
		objApasGenericFunctions.displayRecords("Recently Viewed");
		objRollYearSettingsPage.createRollYearRecord(dataMap);
			
		//Step7: Capture the record id and validate its details
		recordId = objRollYearSettingsPage.getCurrentUrl(driver);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataMap.get("Status"), "Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataMap.get("Roll Year"), "Roll Year record is created successfully");
		rollYearName = objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage));
		
		//Step8: Validate the default List View and Create a duplicate record
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.recentlyViewedListView)), "Recently Viewed", "Default List View is validated successfully");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to open a Roll Year record");
		objRollYearSettingsPage.openRollYearScreen();
		objRollYearSettingsPage.waitForRollYearScreenToLoad();
		
		//Step9: Validate no error message is displayed on entering the Roll Year Settings value
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter 'Roll Year Settings' and 'Calendar End Date' values only");
		objPage.enter(objRollYearSettingsPage.rollYearSettings, dataMap.get("Roll Year Settings"));
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.calendarEndDate, dataMap.get("Calendar End Date"));
		Thread.sleep(1000);
		softAssert.assertTrue(objPage.verifyElementVisible(objRollYearSettingsPage.duplicateRecord) == false, "Validate no duplicate error message is displayed");
		softAssert.assertTrue(objPage.verifyElementVisible(objRollYearSettingsPage.viewDuplicateRecord) == false, "Validate no duplicate error view link is displayed");
		
		//Step10: Validate the error message is displayed on selecting the duplicate Roll Year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter 'Roll Year' and 'Fiscal End Date' values only");
		objRollYearSettingsPage.selectFromDropDown(objRollYearSettingsPage.rollYear, dataMap.get("Roll Year"));
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.fiscalEndDate, dataMap.get("Fiscal End Date"));
		Thread.sleep(2000);
		softAssert.assertTrue(objRollYearSettingsPage.duplicateRecord.isDisplayed() == true, "Validate duplicate error message is displayed as Roll Year record exist");
		softAssert.assertTrue(objRollYearSettingsPage.viewDuplicateRecord.isDisplayed() == true, "Validate duplicate error view link is displayed as Roll Year record exist");
		objRollYearSettingsPage.cancelRollYearRecord();
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate 
	 -Edit a Roll Year record
	 **/
	
	@Test(description = "SMAB-T638: Disabled Veterans- Verify that System Admins are able to CRED(Create/Read/Edit/Delete) the 'Roll Year Settings' record", groups = {"Regression", "DisabledVeteran"}, dependsOnMethods = {"createPastRollYearRecord"}, dataProvider = "loginUsers", alwaysRun = true)
	public void editPastRollYearRecord(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json) 
		dataMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToEditPastRollYearToFuture");
		
		//Step4: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataMap.get("Roll Year"));
				
		//Step5: Delete the existing Roll Year record
		objRollYearSettingsPage.clickShowMoreButton(dataMap.get("Roll Year"), "Delete");
		
		//Step6: Search the existing Roll Year Settings record
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(rollYearName);
		objRollYearSettingsPage.openRollYearRecord(rollYearName);
		
		//Step7: Editing this Roll Year record using EDIT button on Roll Year detail screen
		objRollYearSettingsPage.editPencilIconRollYearRecord();
		
		//Step8: Clear the values from few of the mandatory fields and Save the record
		objRollYearSettingsPage.clearFieldValue(objRollYearSettingsPage.calendarStartDateOnDetailEditPage);
		objRollYearSettingsPage.clearFieldValue(objRollYearSettingsPage.calendarEndDateOnDetailEditPage);
		objRollYearSettingsPage.clearFieldValue(objRollYearSettingsPage.taxStartDateOnDetailEditPage);
		objPage.Click(objRollYearSettingsPage.saveButtonOnDetailPage);
		objPage.Click(objRollYearSettingsPage.saveButtonOnDetailPage);
		//Step9: Validate the error message appears as a pop-up at the bottom of the screen		
		Thread.sleep(1000);
		softAssert.assertTrue(driver.findElements(By.xpath("//h2[@class='slds-truncate slds-text-heading_medium']")).size() == 1, "Validate error message pop-up that appear at the bottom of the page i.e. 'We hit a snag'");
		softAssert.assertTrue(driver.findElements(By.xpath("//a[contains(text(), 'Calendar Start Date')]")).size() == 1, "Validate that 'Calendar Start Date' appears in error message pop-up");
		softAssert.assertTrue(driver.findElements(By.xpath("//a[contains(text(), 'Calendar End Date')]")).size() == 1, "Validate that 'Calendar End Date' appears in error message pop-up");
		softAssert.assertTrue(driver.findElements(By.xpath("//a[contains(text(), 'Tax Start Date')]")).size() == 1, "Validate that 'Tax Start Date' appears in error message pop-up");
		
		
		
		
		//Step10: Click CANCEL button and edit the record
		objPage.Click(objRollYearSettingsPage.cancelButtonOnDetailPage);
		Thread.sleep(1000);
		objRollYearSettingsPage.editRollYearRecord(dataMap);
			
		//Step11: Validate the details post the record is updated and saved
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataMap.get("Status"), "Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataMap.get("Roll Year"), "Roll Year record is created successfully");
		
		objApasGenericFunctions.logout();
	}

	
	/**
	 Below test case is used to validate 
	-Field level validations (error messages)
	 **/
	
	@Test(description = "SMAB-T638: Disabled Veterans- Verify that System Admins are able to CRED(Create/Read/Edit/Delete) the 'Roll Year Settings' record, SMAB-T1283: Disabled Veterans - Verify the layout changes on Roll Year Settings Page", groups = {"Regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void fieldLevelValidationsInRollYearSettings(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
		dataMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToValidateFieldLevelErrorMessages");
		
		//Step4: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataMap.get("Roll Year"));
		
		//Step5: Delete the existing Roll Year record
		objRollYearSettingsPage.clickShowMoreButton(dataMap.get("Roll Year"), "Delete");
				
		//Step6: Change the List view and Create Roll Year record
		objApasGenericFunctions.displayRecords("Recently Viewed");
		objRollYearSettingsPage.createRollYearRecord(dataMap);
		
		//Step7: Validate error messages displayed at field level
		Thread.sleep(1000);
		softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed() == true, "Validate error message is displayed on 'Lien Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed() == true, "Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed() == true, "Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalStartDate.isDisplayed() == true, "Validate error message is displayed on 'Fiscal Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate1.isDisplayed() == true, "Validate error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed() == true, "Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate1.isDisplayed() == true, "Validate error message is displayed on 'Calendar End Date' field");
		
		//Step8: Enter a different 'Calendar End Date' and Save the record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter a different value for 'Calendar End Date' i.e. 12/31/2010 and click SAVE button to validate a different error message");
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.calendarEndDate, "12/31/2010");
		objRollYearSettingsPage.saveRollYearRecord();
		
		//Step9: Validate error messages again that are displayed at field level
		Thread.sleep(1000);
		softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed() == true, "Validate error message is displayed on 'Lien Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed() == true, "Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed() == true, "Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalStartDate.isDisplayed() == true, "Validate error message is displayed on 'Fiscal Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate1.isDisplayed() == true, "Validate error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed() == true, "Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate2.isDisplayed() == true, "Validate error message is displayed on 'Calendar End Date' field");
		
		//Step10: Enter a different 'Fiscal End Date' and Save the record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter a different value for 'Fiscal End Date' i.e. 12/31/2008 and click SAVE button to validate a different error message");
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.fiscalEndDate, "12/31/2008");
		objRollYearSettingsPage.saveRollYearRecord();
		
		//Step11: Validate error messages again that are displayed at field level
		Thread.sleep(1000);
		softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed() == true, "Validate error message is displayed on 'Lien Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed() == true, "Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed() == true, "Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalStartDate.isDisplayed() == true, "Validate error message is displayed on 'Fiscal Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate1.isDisplayed() == true, "Validate first error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnFiscalEndDate2.isDisplayed() == true, "Validate second error message is displayed on 'Fiscal End Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed() == true, "Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate2.isDisplayed() == true, "Validate error message is displayed on 'Calendar End Date' field");
		objRollYearSettingsPage.cancelRollYearRecord();
		
		objApasGenericFunctions.logout();	
	}

	
	/**
	 Below test case is used to validate 
	 -Create Roll Year record for past year
	 **/
	
	@Test(description = "SMAB-T638: Disabled Veterans- Verify that System Admins are able to CRED(Create/Read/Edit/Delete) the 'Roll Year Settings' record", groups = {"Regression", "DisabledVeteran"}, dataProvider = "loginUsers1", alwaysRun = true)
	public void viewRollYearRecord(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Validate NEW button doesn't appear on the screen  
		softAssert.assertTrue(objPage.verifyElementVisible(objRollYearSettingsPage.newExemptionButton) == false, "Validate NEW button is not displayed");
		objApasGenericFunctions.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objRollYearSettingsPage.newExemptionButton) == false, "Validate NEW button is not displayed");
		
		//Step4: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
		dataMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreatePastRollYear");
		
		//Step5: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataMap.get("Roll Year"));
		
		//Step6: Delete the existing Roll Year record
		objRollYearSettingsPage.clickShowMoreButton(dataMap.get("Roll Year"), "Delete");
		
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 Below test case is used to validate create Roll Year record for current year
	 It has been commented out as we need to identify the environment where we can run this TEST.
	 Current Roll Year can have other instances linked like CPI factor and Exemption & Penalty Calculations due to which current Roll Year record can't be deleted and created again
	 **/
	
	/*@Test(description = "SMAB-T638: Disabled Veterans- Verify that System Admins are able to CRED(Create/Read/Edit/Delete) the 'Roll Year Settings' record", groups = {"Regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void createCurrentRollYearRecord(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objApasGenericFunctions.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json) and delete the existing Roll Year record
		dataMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreateCurrentRollYear");
		objSalesforceAPI.delete("Roll_Year_Settings__c", "SELECT Id FROM Roll_Year_Settings__c Where Name = '" + dataMap.get("Roll Year") + "'");
		Thread.sleep(2000);
		
		//Step4: Create Roll Year record
		objRollYearSettingsPage.createRollYearRecord(dataMap);
			
		//Step5: Capture the record id and validate its details
		recordId = objRollYearSettingsPage.getCurrentUrl(driver);
		Thread.sleep(2000);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataMap.get("Status"), "Status of the record is validated successfully");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataMap.get("Roll Year"), "Roll Year record is created successfully");
		
		objApasGenericFunctions.logout();
	}*/
}
