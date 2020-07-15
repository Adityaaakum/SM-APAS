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
import com.apas.Reports.ReportLogger;
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
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objRollYearSettingsPage = new RollYearSettingsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYearData = System.getProperty("user.dir") + testdata.ROLL_YEAR_DATA;
		objApasGenericFunctions.updateRollYearStatus("Closed", "2020");
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
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			expectedIndividualFieldMessage = "Complete this field.";
			}
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
		ReportLogger.INFO("Click 'Cancel' button to move out of the Roll Year screen");
		objPage.Click(objRollYearSettingsPage.cancelButton);
		
		//Step5: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)
		Map<String, String> dataToCreateFutureRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreateFutureRollYear");
		
		//Step6: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(dataToCreateFutureRollYearMap.get("Roll Year"));
		
		//Step7: Delete the existing Roll Year record
		Boolean flag = objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToCreateFutureRollYearMap.get("Roll Year"), "Delete");
		if (flag) softAssert.assertTrue(flag.equals(true),"SMAB-T638: Validate user is able to view delete option and delete the existing Roll Year record");
		if (!flag) softAssert.assertTrue(flag.equals(false),"SMAB-T638: Validate user is not able to find the existing roll year or view delete option for it");
			
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
		Boolean flag1 = objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToCreatePastRollYearMap.get("Roll Year"), "Delete");
		if (flag1) softAssert.assertTrue(flag1.equals(true),"SMAB-T638: Validate user is able to view delete option and delete the existing Roll Year record");
		if (!flag1) softAssert.assertTrue(flag1.equals(false),"SMAB-T638: Validate user is not able to find the existing roll year or view delete option for it");
		
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
		ReportLogger.INFO("Click 'New' button to open a Roll Year record");
		Thread.sleep(2000);
		objPage.Click(objPage.waitForElementToBeClickable(objRollYearSettingsPage.newRollYearButton));
		objPage.waitForElementToBeClickable(objRollYearSettingsPage.rollYearSettings);
		objPage.waitForElementToBeClickable(objRollYearSettingsPage.rollYear);
		
		//Step9: Validate no error message is displayed on entering the Roll Year Settings value
		ReportLogger.INFO("Enter 'Roll Year Settings' and 'Calendar End Date' values only");
		objPage.enter(objRollYearSettingsPage.rollYearSettings, dataToCreatePastRollYearMap.get("Roll Year Settings"));
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.calendarEndDate, dataToCreatePastRollYearMap.get("Calendar End Date"));
		Thread.sleep(1000);
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.duplicateRecord), "Validate no duplicate error message is displayed");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.viewDuplicateRecord), "Validate no duplicate error view link is displayed");
		
		//Step10: Validate the error message is displayed on selecting the duplicate Roll Year
		ReportLogger.INFO("Enter 'Roll Year' and 'Open Roll End Date' values only");
		objApasGenericFunctions.selectFromDropDown(objRollYearSettingsPage.rollYear, dataToCreatePastRollYearMap.get("Roll Year"));
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.openRollEndDate, dataToCreatePastRollYearMap.get("Open Roll End Date"));
		Thread.sleep(2000);
		softAssert.assertTrue(objRollYearSettingsPage.duplicateRecord.isDisplayed(), "Validate duplicate error message is displayed as Roll Year record exist");
		
		String expectedIndividualFieldMessage = "This record looks like a duplicate.View Duplicates";
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			expectedIndividualFieldMessage = "You can't save this record because a duplicate record already exists. To save, use different information.View Duplicates";
			}
		
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.duplicateRecord)),expectedIndividualFieldMessage, "SMAB-T638: Validate duplicate error message text");
		softAssert.assertTrue(objRollYearSettingsPage.viewDuplicateRecord.isDisplayed(), "Validate duplicate error view link is displayed as Roll Year record exist");
		ReportLogger.INFO("Click 'Cancel' button to move out of the Roll Year screen");
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
		Boolean flag2 = objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", dataToEditPastRollYearToFutureMap.get("Roll Year"), "Delete");
		if (flag2) softAssert.assertTrue(flag2.equals(true),"SMAB-T638: Validate user is able to view delete option and delete the existing Roll Year record");
		if (!flag2) softAssert.assertTrue(flag2.equals(false),"SMAB-T638: Validate user is not able to find the existing roll year or view delete option for it");
				
		//Step15: Search the existing Roll Year Settings record
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(rollYearName);
		objRollYearSettingsPage.openRollYearRecord(rollYearName);
				
		//Step16: Editing this Roll Year record using EDIT button on Roll Year detail screen
		ReportLogger.INFO("Click 'Edit' pencil icon to update it");
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
		softAssert.assertTrue(objApasGenericPage.popUpErrorMessageWeHitASnag.isDisplayed(), "SMAB-T638: Validate error message pop-up that appear at the bottom of the page i.e. 'We hit a snag'");
		softAssert.assertTrue(objApasGenericPage.returnElemOnPopUpScreen("Calendar Start Date").isDisplayed(), "SMAB-T638: Validate that 'Calendar Start Date' appears in error message pop-up");
		softAssert.assertTrue(objApasGenericPage.returnElemOnPopUpScreen("Calendar End Date").isDisplayed(), "SMAB-T638: Validate that 'Calendar End Date' appears in error message pop-up");
		softAssert.assertTrue(objApasGenericPage.returnElemOnPopUpScreen("Tax Start Date").isDisplayed(), "SMAB-T638: Validate that 'Tax Start Date' appears in error message pop-up");
				
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
		
		//Step5: Create Roll Year record
		objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToValidateFieldLevelErrorMessagesMap, "New");
		
		//Step6: Validate error messages displayed at field level
		Thread.sleep(1000);
		String errorOnLienDate = "Lien Date year should be same as Roll Year";
		String errorOnTaxStartDate = "Tax Start Date year should be same as Roll Year";
		String errorOnTaxEndDate = "Tax End Date's year should be one year greater of selected Roll Year";
		String errorOnOpenRollStartDate = "Start Date's year should be one year less of selected Roll Year";
		String errorOnOpenRollEndDate1 = "End Date year should be same as Roll Year";
		String errorOnOpenRollEndDate2 = "End Date must be greater than Start Date";
		String errorOnCalendarStartDate = "Calendar Start Date year should be same as Roll Year";
		String errorOnCalendarEndDate1 = "Calendar End Date must be greater than Calendar Start Date";
		String errorOnCalendarEndDate2 = "Calendar End Date year should be same as Roll Year";
		
		
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnLienDate)), errorOnLienDate, "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnTaxStartDate)), errorOnTaxStartDate, "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnTaxEndDate)), errorOnTaxEndDate, "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnOpenRollStartDate)), errorOnOpenRollStartDate, "SMAB-T638: Validate error message is displayed on 'Open Roll Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnOpenRollEndDate1)), errorOnOpenRollEndDate1, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnCalendarStartDate)), errorOnCalendarStartDate, "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnCalendarEndDate1)), errorOnCalendarEndDate1, "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
		
		
		/*softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Lien Date' field : Lien Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Tax Start Date' field : Tax Start Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Tax End Date' field : Tax End Date's year should be one year greater of selected Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnOpenRollStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Open Roll Start Date' field : Start Date's year should be one year less of selected Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnOpenRollEndDate1.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Open Roll End Date' field : End Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Calendar Start Date' field : Calendar Start Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate1.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Calendar End Date' field : Calendar End Date must be greater than Calendar Start Date");
		*/
		
		//Step7: Enter a different 'Calendar End Date' and Save the record
		ReportLogger.INFO("Enter a different value for 'Calendar End Date' i.e. 12/31/2010 and click SAVE button to validate a different error message");
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.calendarEndDate, "12/31/2010");
		objPage.Click(objRollYearSettingsPage.saveButton);
		
		//Step8: Validate error messages again that are displayed at field level
		Thread.sleep(2000);
		
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnLienDate)), errorOnLienDate, "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnTaxStartDate)), errorOnTaxStartDate, "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnTaxEndDate)), errorOnTaxEndDate, "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnOpenRollStartDate)), errorOnOpenRollStartDate, "SMAB-T638: Validate error message is displayed on 'Open Roll Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnOpenRollEndDate1)), errorOnOpenRollEndDate1, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnCalendarStartDate)), errorOnCalendarStartDate, "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnCalendarEndDate2)), errorOnCalendarEndDate2, "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
		
		/*softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Lien Date' field : Lien Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Tax Start Date' field : Tax Start Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Tax End Date' field : Tax End Date's year should be one year greater of selected Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnOpenRollStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Open Roll Start Date' field : Start Date's year should be one year less of selected Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnOpenRollEndDate1.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Open Roll End Date' field : End Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Calendar Start Date' field : Calendar Start Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate2.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Calendar End Date' field : Calendar End Date year should be same as Roll Year");
		*/
		
		//Step9: Enter a different 'Open Roll End Date' and Save the record
		ReportLogger.INFO("Enter a different value for 'Open Roll End Date' i.e. 12/31/2008 and click SAVE button to validate a different error message");
		objRollYearSettingsPage.enterDate(objRollYearSettingsPage.openRollEndDate, "12/31/2008");
		objPage.Click(objRollYearSettingsPage.saveButton);
		
		//Step10: Validate error messages again that are displayed at field level
		Thread.sleep(2000);
		
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnLienDate)), errorOnLienDate, "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnTaxStartDate)), errorOnTaxStartDate, "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnTaxEndDate)), errorOnTaxEndDate, "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnOpenRollStartDate)), errorOnOpenRollStartDate, "SMAB-T638: Validate error message is displayed on 'Open Roll Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnOpenRollEndDate1)), errorOnOpenRollEndDate1, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnOpenRollEndDate2)), errorOnOpenRollEndDate2, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnCalendarStartDate)), errorOnCalendarStartDate, "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.errorOnCalendarEndDate2)), errorOnCalendarEndDate2, "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
		
		/*softAssert.assertTrue(objRollYearSettingsPage.errorOnLienDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Lien Date' field : Lien Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Tax Start Date' field : Tax Start Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnTaxEndDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Tax End Date' field : Tax End Date's year should be one year greater of selected Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnOpenRollStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Open Roll Start Date' field : Start Date's year should be one year less of selected Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnOpenRollEndDate1.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Open Roll End Date' field : End Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnOpenRollEndDate2.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Open Roll End Date' field : End Date must be greater than Start Date");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarStartDate.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Calendar Start Date' field : Calendar Start Date year should be same as Roll Year");
		softAssert.assertTrue(objRollYearSettingsPage.errorOnCalendarEndDate2.isDisplayed(), "SMAB-T638: Validate the following error message is displayed on 'Calendar End Date' field : Calendar End Date year should be same as Roll Year");
		*/
		
		//Step11: Click Cancel button
		ReportLogger.INFO("Click 'Cancel' button to move out of the Roll Year screen");
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
		Map<String, String> viewRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToValidateFieldLevelErrorMessages");
		
		//Step5: Search the existing Roll Year record
		Thread.sleep(1000);
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(viewRollYearMap.get("Roll Year"));
		
		//Step6: Delete the existing Roll Year record
		softAssert.assertTrue(!objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", viewRollYearMap.get("Roll Year"), "Delete"),"SMAB-T638: Validate non system admin user is not able to view 'Delete' option to delete the existing Roll Year record");
		softAssert.assertTrue(!objApasGenericPage.clickShowMoreButtonAndAct("Roll Year Settings", viewRollYearMap.get("Roll Year"), "Edit"),"SMAB-T638: Validate non system admin user is not able to view 'Edit' option to update the existing Roll Year record");
		
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