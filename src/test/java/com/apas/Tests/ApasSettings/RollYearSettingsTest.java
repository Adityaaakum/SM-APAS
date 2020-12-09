package com.apas.Tests.ApasSettings;

import java.util.Map;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.RollYearSettingsPage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;

public class RollYearSettingsTest extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
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
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYearData = System.getProperty("user.dir") + testdata.ROLL_YEAR_DATA;
		objRollYearSettingsPage.updateRollYearStatus("Closed", "2020");
	}
	
	/**
	 Below test case is used to validate 
	 -Error message when no mandatory fields are entered in Roll Year record record before saving it
	 -Create Roll Year record for Future Year
	 **/
	@Test(description = "SMAB-T638: Validate that System Admin is able to create Future Roll Year record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginSystemAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void RollYear_CreateFutureRecord(String loginUser) throws Exception {
	
			//Step1: Login to the APAS application using the user passed through the data provider
			objRollYearSettingsPage.login(loginUser);
			
			//Step2: Open the Roll Year Settings module
			objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
			
			//Step3: Save an Roll Year Settings record without entering any details		
			objRollYearSettingsPage.saveRollYearRecordWithNoValues();
			
			//Step4: Validate error messages when no field value is entered and Roll Year Settings record is saved
			String expectedErrorMessageOnTop = "Close error dialog\nWe hit a snag.\nReview the following fields\nRoll Year Settings\nOpen Roll Start Date\nRoll Year\nOpen Roll End Date\nLien Date\nCalendar Start Date\nCalendar End Date\nTax Start Date\nTax End Date\nAnnual Batch Execution Date";
			String expectedIndividualFieldMessage = "Complete this field.";
			softAssert.assertEquals(objPage.getElementText(objRollYearSettingsPage.pageError),expectedErrorMessageOnTop,"SMAB-T638: Validating mandatory fields missing error in Roll Year Settings screen.");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Calendar End Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Calendar End Date'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Calendar Start Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Calendar Start Date'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Open Roll End Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Open Roll End Date'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Lien Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Lien Date'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Roll Year Settings"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Roll Year Settings'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Roll Year"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Roll Year'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Open Roll Start Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Open Roll Start Date'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Tax End Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Tax End Date'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Tax Start Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Tax Start Date'");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage("Annual Batch Execution Date"),expectedIndividualFieldMessage,"SMAB-T638: Validating mandatory fields missing error for 'Annual Batch Execution Date'");
			ReportLogger.INFO("Click 'Cancel' button to move out of the Roll Year screen");
			objPage.Click(objRollYearSettingsPage.cancelButton);
			
			//Step5: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)
			Map<String, String> dataToCreateFutureRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreateFutureRollYear");
			
			//Step6: Select ALL from the list view
			Thread.sleep(1000);
			objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
			objRollYearSettingsPage.displayRecords("All");
			
			//Step7: Search the existing Roll Year record and delete if exists
			if (objRollYearSettingsPage.searchRecords(dataToCreateFutureRollYearMap.get("Roll Year")).substring(0, 6).equals("1 item")) {
				softAssert.assertTrue(objApasGenericPage.clickShowMoreButtonAndAct(dataToCreateFutureRollYearMap.get("Roll Year"), "Delete"),"SMAB-T638: Validate user is able to delete the existing Roll Year record i.e. " + dataToCreateFutureRollYearMap.get("Roll Year"));
			}
			
			//Step8: Change the List view and Create Roll Year record
			objRollYearSettingsPage.displayRecords("Recently Viewed");
			objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToCreateFutureRollYearMap, "New");
				
			//Step9: Validate the Roll Year record and its status
			driver.navigate().refresh();
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataToCreateFutureRollYearMap.get("Status"), "SMAB-T638: Status of the record is validated successfully");
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataToCreateFutureRollYearMap.get("Roll Year"), "SMAB-T638: Roll Year record is created successfully");
			
			//Step10: Edit the status on Roll Year record
			ReportLogger.INFO("Click 'Edit' button to update the status on the Roll Year record");
			objPage.Click(objPage.waitForElementToBeClickable(objRollYearSettingsPage.editButton));
			Thread.sleep(1000);
			objRollYearSettingsPage.selectFromDropDown(objRollYearSettingsPage.status, "Closed");
			objPage.Click(objRollYearSettingsPage.saveButton);
			
			//Step11: Validate the status of Roll Year record
			Thread.sleep(1000);
			driver.navigate().refresh();
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), "Closed", "SMAB-T638: Status of the record is validated successfully");
						
			objRollYearSettingsPage.logout();
		}
		
		/**
		 Below test case is used to validate 
		 -Create Roll Year record for past year
		 -Edit a Roll Year record
		 **/
		
		@Test(description = "SMAB-T638: Validate that System Admin is able to create Past Roll Year record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginSystemAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class)
		public void RollYear_CreateAndEditPastRecord(String loginUser) throws Exception {
			
			//Step1: Login to the APAS application using the user passed through the data provider
			objRollYearSettingsPage.login(loginUser);
			
			//Step2: Open the Roll Year Settings module
			objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
			
			//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
			Map<String, String> dataToCreatePastRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToCreatePastRollYear");
			
			//Step4: Select ALL from the list view
			objRollYearSettingsPage.displayRecords("All");
			
			//Step5: Search the existing Roll Year record and delete if exists
			if (objRollYearSettingsPage.searchRecords(dataToCreatePastRollYearMap.get("Roll Year")).substring(0, 6).equals("1 item")) {
				softAssert.assertTrue(objApasGenericPage.clickShowMoreButtonAndAct(dataToCreatePastRollYearMap.get("Roll Year"), "Delete"),"SMAB-T638: Validate user is able to delete the existing Roll Year record i.e. " + dataToCreatePastRollYearMap.get("Roll Year"));
			}
			
			//Step6: Change the List view and Create Roll Year record
			objRollYearSettingsPage.displayRecords("Recently Viewed");
			//Added the below line to bring back the focus to handle regression failure - 11/26
			driver.navigate().refresh();
			objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToCreatePastRollYearMap, "New");
				
			//Step7: Capture the record id and validate its details
			String recordId = objApasGenericPage.getCurrentRecordId(driver, "Roll Year");
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataToCreatePastRollYearMap.get("Status"), "SMAB-T638: Status of the record is validated successfully");
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataToCreatePastRollYearMap.get("Roll Year"), "SMAB-T638: Roll Year record is created successfully");
			String rollYearName = objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage));
			
			//Step8: Validate the default List View and Create a duplicate record
			objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.recentlyViewedListView)), "Recently Viewed", "SMAB-T638: Default List View is validated successfully");
			ReportLogger.INFO("Click 'New' button to open a Roll Year record");
			Thread.sleep(2000);
			objPage.Click(objPage.waitForElementToBeClickable(objRollYearSettingsPage.newRollYearButton));
			objPage.waitForElementToBeClickable(objRollYearSettingsPage.rollYearSettings);
			objPage.waitForElementToBeClickable(objRollYearSettingsPage.rollYear);
			
			//Step9: Validate no error message is displayed on entering the Roll Year Settings value
			ReportLogger.INFO("Enter 'Roll Year Settings' and 'Calendar End Date' values only");
			objPage.enter(objRollYearSettingsPage.rollYearSettings, dataToCreatePastRollYearMap.get("Roll Year Settings"));
			objApasGenericPage.enter(objRollYearSettingsPage.calendarEndDate, dataToCreatePastRollYearMap.get("Calendar End Date"));
			Thread.sleep(1000);
			softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.duplicateRecord), "Validate no duplicate error message is displayed");
			softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.viewDuplicateRecord), "Validate no duplicate error view link is displayed");
			
			//Step10: Validate the error message is displayed on selecting the duplicate Roll Year
			ReportLogger.INFO("Enter 'Roll Year' and 'Open Roll End Date' values only");
			objRollYearSettingsPage.selectFromDropDown(objRollYearSettingsPage.rollYear, dataToCreatePastRollYearMap.get("Roll Year"));
			objApasGenericPage.enter(objRollYearSettingsPage.openRollEndDate, dataToCreatePastRollYearMap.get("Open Roll End Date"));
			Thread.sleep(2000);
			//softAssert.assertTrue(objRollYearSettingsPage.duplicateRecord.isDisplayed(), "Validate duplicate error message is displayed as Roll Year record exist");
			String	expectedIndividualFieldMessage = "Close error dialog\nWe hit a snag.\nYou can't save this record because a duplicate record already exists. To save, use different information.\nView Duplicates";
			softAssert.assertEquals(objPage.getElementText(objApasGenericPage.pageError),expectedIndividualFieldMessage, "SMAB-T638: Validate duplicate error message text");
			//softAssert.assertTrue(objRollYearSettingsPage.viewDuplicateRecord.isDisplayed(), "Validate duplicate error view link is displayed as Roll Year record exist");
			ReportLogger.INFO("Click 'Cancel' button to move out of the Roll Year screen");
			objPage.Click(objRollYearSettingsPage.cancelButton);
			
			//Step11: Open the Roll Year Settings module
			objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
					
			//Step12: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json) 
			Map<String, String> dataToEditPastRollYearToFutureMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToEditPastRollYearToFuture");
					
			//Step13: Search the existing Roll Year record and delete if exists
			Thread.sleep(1000);
			objRollYearSettingsPage.displayRecords("All");
			if (objRollYearSettingsPage.searchRecords(dataToEditPastRollYearToFutureMap.get("Roll Year")).substring(0, 6).equals("1 item")) {
				softAssert.assertTrue(objApasGenericPage.clickShowMoreButtonAndAct(dataToEditPastRollYearToFutureMap.get("Roll Year"), "Delete"),"SMAB-T638: Validate user is able to delete the existing Roll Year record i.e. " + dataToEditPastRollYearToFutureMap.get("Roll Year"));
			}
			
			//Step14: Search the existing Roll Year Settings record
			objRollYearSettingsPage.displayRecords("All");
			objRollYearSettingsPage.searchRecords(rollYearName);
			objRollYearSettingsPage.openRollYearRecord(recordId, rollYearName);
					
			//Step15: Editing this Roll Year record using EDIT button on Roll Year detail screen
			ReportLogger.INFO("Click 'Edit' pencil icon to update it");
			objPage.Click(objRollYearSettingsPage.editPencilIconForRollYearOnDetailPage);
					
			//Step16: Clear the values from few of the mandatory fields and Save the record
			Thread.sleep(1000);
			objPage.clearFieldValue(objRollYearSettingsPage.calendarStartDateOnDetailEditPage);
			objPage.clearFieldValue(objRollYearSettingsPage.calendarEndDateOnDetailEditPage);
			objPage.clearFieldValue(objRollYearSettingsPage.taxStartDateOnDetailEditPage);
			objPage.Click(objRollYearSettingsPage.saveButtonOnDetailPage);
			objPage.Click(objRollYearSettingsPage.saveButtonOnDetailPage);
					
			//Step17: Validate the error message appears as a pop-up at the bottom of the screen		
			Thread.sleep(2000);
			softAssert.assertTrue(objApasGenericPage.popUpErrorMessageWeHitASnag.isDisplayed(), "SMAB-T638: Validate error message pop-up that appear at the bottom of the page i.e. 'We hit a snag'");
			softAssert.assertTrue(objApasGenericPage.returnElemOnPopUpScreen("Calendar Start Date").isDisplayed(), "SMAB-T638: Validate that 'Calendar Start Date' appears in error message pop-up");
			softAssert.assertTrue(objApasGenericPage.returnElemOnPopUpScreen("Calendar End Date").isDisplayed(), "SMAB-T638: Validate that 'Calendar End Date' appears in error message pop-up");
			softAssert.assertTrue(objApasGenericPage.returnElemOnPopUpScreen("Tax Start Date").isDisplayed(), "SMAB-T638: Validate that 'Tax Start Date' appears in error message pop-up");
					
			//Step18: Click CANCEL button and edit the record
			objPage.Click(objRollYearSettingsPage.cancelButtonOnDetailPage);
			Thread.sleep(1000);
			objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToEditPastRollYearToFutureMap, "Edit");
						
			//Step19: Validate the details post the record is updated and saved
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.statusOnDetailPage)), dataToEditPastRollYearToFutureMap.get("Status"), "SMAB-T638: Status of the record is validated successfully");
			softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objRollYearSettingsPage.rollYearOnDetailPage)), dataToEditPastRollYearToFutureMap.get("Roll Year"), "SMAB-T638: Roll Year record is created successfully");
					
			objRollYearSettingsPage.logout();
		}
		
		/**
		 Below test case is used to validate 
		-Field level validations (error messages)
		 **/
		
		@Test(description = "SMAB-T638, SMAB-T1283: Validate field level validations on Roll Year Screen", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginSystemAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class)
		public void RollYear_FieldLevelValidations(String loginUser) throws Exception {
		
			//Step1: Login to the APAS application using the user passed through the data provider
			objRollYearSettingsPage.login(loginUser);
			
			//Step2: Open the Roll Year Settings module
			objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
			
			//Step3: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
			Map<String, String> dataToValidateFieldLevelErrorMessagesMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToValidateFieldLevelErrorMessages");
			
			//Step4: Search the existing Roll Year record
			objRollYearSettingsPage.displayRecords("All");
			objRollYearSettingsPage.searchRecords(dataToValidateFieldLevelErrorMessagesMap.get("Roll Year"));
			
			//Step5: Create Roll Year record
			objRollYearSettingsPage.createOrUpdateRollYearRecord(dataToValidateFieldLevelErrorMessagesMap, "New");
			
			//Step6: Validate error messages displayed at field level
			String errorOnLienDate = "Lien Date year should be same as Roll Year";
			String errorOnTaxStartDate = "Tax Start Date year should be same as Roll Year";
			String errorOnTaxEndDate = "Tax End Date's year should be one year greater of selected Roll Year";
			String errorOnOpenRollStartDate = "Start Date's year should be one year less of selected Roll Year";
			String errorOnOpenRollEndDate1 = "End Date year should be same as Roll Year";
			String errorOnOpenRollEndDate2 = "End Date must be greater than Start Date";
			String errorOnCalendarStartDate = "Calendar Start Date year should be same as Roll Year";
			String errorOnCalendarEndDate1 = "Calendar End Date must be greater than Calendar Start Date";
			String errorOnCalendarEndDate2 = "Calendar End Date year should be same as Roll Year";
			
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnLienDate), errorOnLienDate, "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnTaxStartDate), errorOnTaxStartDate, "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnTaxEndDate), errorOnTaxEndDate, "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnOpenRollStartDate), errorOnOpenRollStartDate, "SMAB-T638: Validate error message is displayed on 'Open Roll Start Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnOpenRollEndDate1), errorOnOpenRollEndDate1, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnCalendarStartDate), errorOnCalendarStartDate, "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnCalendarEndDate1), errorOnCalendarEndDate1, "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
			
			//Step7: Enter a different 'Calendar End Date' and Save the record
			ReportLogger.INFO("Enter a different value for 'Calendar End Date' i.e. 12/31/2010 and click SAVE button to validate a different error message");
			objApasGenericPage.enter(objRollYearSettingsPage.calendarEndDate, "12/31/2010");
			objPage.Click(objRollYearSettingsPage.saveButton);
			
			//Step8: Validate error messages again that are displayed at field level
			Thread.sleep(2000);
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnLienDate), errorOnLienDate, "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnTaxStartDate), errorOnTaxStartDate, "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnTaxEndDate), errorOnTaxEndDate, "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnOpenRollStartDate), errorOnOpenRollStartDate, "SMAB-T638: Validate error message is displayed on 'Open Roll Start Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnOpenRollEndDate1), errorOnOpenRollEndDate1, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnCalendarStartDate), errorOnCalendarStartDate, "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
			softAssert.assertContains(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnCalendarEndDate1), errorOnCalendarEndDate2, "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
			
			//Step9: Enter a different 'Open Roll End Date' and Save the record
			ReportLogger.INFO("Enter a different value for 'Open Roll End Date' i.e. 12/31/2008 and click SAVE button to validate a different error message");
			objApasGenericPage.enter(objRollYearSettingsPage.openRollEndDate, "12/31/2008");
			objPage.Click(objRollYearSettingsPage.saveButton);
			
			//Step10: Validate error messages again that are displayed at field level
			Thread.sleep(2000);
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnLienDate), errorOnLienDate, "SMAB-T638: Validate error message is displayed on 'Lien Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnTaxStartDate), errorOnTaxStartDate, "SMAB-T638: Validate error message is displayed on 'Tax Start Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnTaxEndDate), errorOnTaxEndDate, "SMAB-T638: Validate error message is displayed on 'Tax End Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnOpenRollStartDate), errorOnOpenRollStartDate, "SMAB-T638: Validate error message is displayed on 'Open Roll Start Date' field");
			softAssert.assertContains(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnOpenRollEndDate1), errorOnOpenRollEndDate1, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
			softAssert.assertContains(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnOpenRollEndDate1), errorOnOpenRollEndDate2, "SMAB-T638: Validate error message is displayed on 'Open Roll End Date' field");
			softAssert.assertEquals(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnCalendarStartDate), errorOnCalendarStartDate, "SMAB-T638: Validate error message is displayed on 'Calendar Start Date' field");
			softAssert.assertContains(objRollYearSettingsPage.getIndividualFieldErrorMessage(objRollYearSettingsPage.errorOnCalendarEndDate1), errorOnCalendarEndDate2, "SMAB-T638: Validate error message is displayed on 'Calendar End Date' field");
			
			//Step11: Click Cancel button
			ReportLogger.INFO("Click 'Cancel' button to move out of the Roll Year screen");
			objPage.Click(objRollYearSettingsPage.cancelButton);
			
			objRollYearSettingsPage.logout();	
		}
	
	/**
	 Below test case is used to validate 
	 -RP Business Admin and Exemption Support Staff are able to only view the Roll Year Record
	 **/
	
	@Test(description = "SMAB-T638: Validate RP Business admin and Exemption Support staff are able to only view the Roll year record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginRpBusinessAdminAndExemptionSupportUsers", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void RollYear_ViewRecord(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objRollYearSettingsPage.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
		
		//Step3: Validate NEW button doesn't appear on the screen  
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.newRollYearButton), "SMAB-T638: Validate NEW button is not displayed");
		objRollYearSettingsPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRollYearSettingsPage.newRollYearButton), "SMAB-T638: Validate NEW button is not displayed");
		
		//Step4: Create data map for the JSON file (RollYear_DataToCreateRollYearRecord.json)  
		Map<String, String> viewRollYearMap = objUtil.generateMapFromJsonFile(rollYearData, "DataToValidateFieldLevelErrorMessages");
		
		//Step5: Select ALL from the List view and Search the Roll Year record
		Thread.sleep(1000);
		objRollYearSettingsPage.displayRecords("All");
		objRollYearSettingsPage.searchRecords(viewRollYearMap.get("Roll Year"));
		
		//Step6: Search the existing Roll Year record - Delete/Edit options should not be visbile to non-admin users
		softAssert.assertTrue(!objApasGenericPage.clickShowMoreButtonAndAct(viewRollYearMap.get("Roll Year"), "Delete"),"SMAB-T638: Validate non system admin user is not able to view 'Delete' option to delete the existing Roll Year record : " + viewRollYearMap.get("Roll Year"));
		softAssert.assertTrue(!objApasGenericPage.clickShowMoreButtonAndAct(viewRollYearMap.get("Roll Year"), "Edit"),"SMAB-T638: Validate non system admin user is not able to view 'Edit' option to update the existing Roll Year record : " + viewRollYearMap.get("Roll Year"));
				
		objRollYearSettingsPage.logout();
	}
	
	
	/**
	 Below test case is used to validate create Roll Year record for current year
	 It has been commented out as we need to identify the environment where we can run this TEST.
	 Current Roll Year can have other instances linked like CPI factor and Exemption & Penalty Calculations due to which current Roll Year record can't be deleted and created again
	 **/
	
	/*@Test(description = "SMAB-T638: Validate user is able to create Roll year record for current year", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void RollYear_CreateCurrentRecord(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objRollYearSettingsPage.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objRollYearSettingsPage.searchModule(modules.ROLL_YEAR_SETTINGS);
		
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
		
		objRollYearSettingsPage.logout();
	}*/
}