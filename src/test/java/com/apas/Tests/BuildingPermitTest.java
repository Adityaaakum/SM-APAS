package com.apas.Tests;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportTransactions;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.apps;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.SalsesforceStandardFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermitTest extends TestBase implements testdata, apps, users {

	private RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactions objEfileImportTransactionsPage;
	SalsesforceStandardFunctions salesforceStandardFunctions;
	BuildingPermitPage objBuildPermit;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
	
	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactions(driver);
		salesforceStandardFunctions = new SalsesforceStandardFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}
	
	@Test(description = "Transaction record verification for the imported Building Permit in TXT Format", priority = 0, alwaysRun = true, enabled = false)
	public void transactionRecordVerificationBuildingPermitTXT() throws Exception {
		
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_ATHERTON;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", "January 2020",athertonBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(driver.findElement(By.xpath("//a[contains(.,'Import Transaction-00')]")));
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Importedsdfasdf", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softAssert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("Atherton Building Permits"), "SMAB-T430: Validation that latest generated transaction log is for Atherton Building Permits");
	}
	
	@Test(description = "Transaction record verification for the imported Building Permit in XLS Format", priority = 1, alwaysRun = true, enabled = false)
	public void transactionRecordVerificationBuildingPermitXLS() throws Exception {
			
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the San Mateo permit file");
		String sanMateoBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_SAN_MATEO;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "San Mateo Building permits", "January 2020",sanMateoBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(driver.findElement(By.xpath("//a[contains(.,'Import Transaction-00')]")));
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softAssert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("San Mateo Building permits"), "SMAB-T430: Validation that latest generated transaction log is for San Mateo Building permits");
	}
	
	@Test(description = "Creating manual entry for building permit", groups = {"smoke"}, priority = 2, enabled = true)
	public void bldngPrmtsCreateManualEntry() throws Exception {
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(BUILDING_PERMITS);
					
		String newManualEntryData = System.getProperty("user.dir") + BUILDING_PERMIT_NEW_MANUAL_ENTRY_DATA;
		dataMap = objUtil.generateMapFromDataFile(newManualEntryData);
		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Opening manual entry pop and clicking save button without entring any data to validate error messages for mandatory fields.");
//		objBuildPermit.openManualEntryForm();
//		objBuildPermit.waitForManualEntryFormToLoad();
//		objBuildPermit.saveManualEntry();
//		
//		List<String> errorsList = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
//		String expMsgInPopUpHeader = CONFIG.getProperty("expectedErrorMsgForAllMandatoryFieldsNewEntry");
//		String actMsgInPopUpHeader = errorsList.get(0);
//		softAssert.assertEquals(actMsgInPopUpHeader, expMsgInPopUpHeader, "SMAB-T418: Validating mandatory fields missing error in manual entry pop up's header.");		
//		String expMsgForIndividualField = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
//		String actMsgForIndividualField = errorsList.get(1);	
//		softAssert.assertEquals(expMsgForIndividualField, actMsgForIndividualField, "SMAB-T418: Validating mandatory fields missing error against individual fields");
//		int fieldsCountInHeaderMsg = Integer.parseInt(errorsList.get(2));		
//		int individualMsgsCount = Integer.parseInt(errorsList.get(3));
//		softAssert.assertEquals(fieldsCountInHeaderMsg, individualMsgsCount, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
//		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating to ensure entry is not created when all mandatory fields filled and process is aborted without saving.");	
//		objBuildPermit.enterManualEntryData(dataMap);
//		objBuildPermit.abortManualEntry();
//		boolean buildingPermitNotCreated = objBuildPermit.checkBuildingPermitOnGrid();
//		softAssert.assertFalse(buildingPermitNotCreated, "SMAB-T418: Validating whether manual entry successfully aborted without saving.");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Verifying whether manual entry gets saved and pop up window for new entry gets displayed when 'Save & New' option is selected.");
		objBuildPermit.openManualEntryForm();
		objBuildPermit.enterManualEntryData(dataMap);
		boolean isNewManualEntryPopUpDisplayed = objBuildPermit.saveManualEntryAndExit();
		softAssert.assertTrue(isNewManualEntryPopUpDisplayed, "SMAB-T418: Validating whether pop up for new entry is displayed.");
		
		boolean isLoaderVisible = objBuildPermit.checkAndHandlePageLoaderOnEntryCreation();
		if(isLoaderVisible) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating newly created manual entry successfully displayed on recently viewed grid.");
			boolean isEntryDisplayed = objBuildPermit.checkBuildingPermitOnGrid();
			softAssert.assertTrue(isEntryDisplayed, "SMAB-T418: Validating whether manual entry successfully created and displayed on recently viewed grid.");
		} else {
			ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating newly created manual entry successfully displayed on details page.");
			boolean isEntryDisplayed = objBuildPermit.checkBuildingPermitOnDetailsPage();
			softAssert.assertTrue(isEntryDisplayed, "SMAB-T418: Validating whether manual entry successfully created and displayed on details page.");	
		}
		
		softAssert.assertAll();
	}
	
	@Test(description = "Editing and saving existing building permit manual entry with new data", groups = {"smoke"}, priority = 3, enabled = true)
	public void editNewlyCreatedManualEntryFromDetails() throws Exception {
//		objBuildPermit.editPermitEntryOnDetailsPage(true);
		List<String> listOfTxtAndDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntryTxtAndDrpDownFields").split(","));
		List<String> listOfSearchDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntrySearchDrpDownFields").split(","));
		Map<String, String> dataMapBeforeEditing = objBuildPermit.fetchDataFromManualEntry(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
//		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Clearing all mandatory fields and validating messages against them.");
//		objBuildPermit.clearPrePopulatedMandatoryFieldsAndSave();
//		
//		List<String> errorsListWhileEditing = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
//		String expMsgInPopUpHeaderWhileEditing = CONFIG.getProperty("expectedErrorMsgForAllMandatoryFields");
//		String actMsgInPopUpHeaderWhileEditing = errorsListWhileEditing.get(0);
//		softAssert.assertEquals(actMsgInPopUpHeaderWhileEditing, expMsgInPopUpHeaderWhileEditing, "SMAB-T418: Validating mandatory fields missing error in manual entry pop up's header.");		
//		String expMsgForIndividualFieldWhileEditing = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
//		String actMsgForIndividualFieldWhileEditing = errorsListWhileEditing.get(1);	
//		softAssert.assertEquals(actMsgForIndividualFieldWhileEditing, expMsgForIndividualFieldWhileEditing, "SMAB-T418: Validating mandatory fields missing error against individual fields");
//		int fieldsCountInHeaderMsgWhileEditing = Integer.parseInt(errorsListWhileEditing.get(2));		
//		int individualMsgsCountWhileEditing = Integer.parseInt(errorsListWhileEditing.get(3));
//		softAssert.assertEquals(fieldsCountInHeaderMsgWhileEditing, individualMsgsCountWhileEditing, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
//		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Entering data for some mandatory fields and validating messages for remaining fields.");
//		String dataToEditManualEntry = System.getProperty("user.dir") + BUILDING_PERMIT_EDIT_MANUAL_ENTRY_DATA;
//		Map<String, String> dataMapForEdit = objUtil.generateMapFromDataFile(dataToEditManualEntry);
//		objBuildPermit.fillSomeMandatoryFieldsAndSave(dataMapForEdit);
//		
//		errorsListWhileEditing = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
//		expMsgInPopUpHeaderWhileEditing = CONFIG.getProperty("expectedErrorMsgForRemainingMandatoryFields");
//		actMsgInPopUpHeaderWhileEditing = errorsListWhileEditing.get(0);
//		softAssert.assertEquals(actMsgInPopUpHeaderWhileEditing, expMsgInPopUpHeaderWhileEditing, "SMAB-T418: Validating mandatory fields missing error in manual entry pop up's header.");		
//		expMsgForIndividualFieldWhileEditing = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
//		actMsgForIndividualFieldWhileEditing = errorsListWhileEditing.get(1);	
//		softAssert.assertEquals(actMsgForIndividualFieldWhileEditing, expMsgForIndividualFieldWhileEditing, "SMAB-T418: Validating mandatory fields missing error against individual fields");
//		fieldsCountInHeaderMsgWhileEditing = Integer.parseInt(errorsListWhileEditing.get(2));		
//		individualMsgsCountWhileEditing = Integer.parseInt(errorsListWhileEditing.get(3));
//		softAssert.assertEquals(fieldsCountInHeaderMsgWhileEditing, individualMsgsCountWhileEditing, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
//		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Filling all mandatory fields and validating messages for work descripton for restricted description types.");
//		List<String> workDescInvalidEntriesErrorMsgs = Arrays.asList(CONFIG.getProperty("invalidEntriesForWordDesc").split(","));
//		String descErrorMessage = CONFIG.getProperty("descErrorMessage");
//		List<String> descValidationMsgs = objBuildPermit.fillRemainingMandatoryFieldsAndSave(dataMapForEdit, workDescInvalidEntriesErrorMsgs);
			
//		for(int i = 0; i < descValidationMsgs.size(); i++) {
//			if(descValidationMsgs.get(i).equals(descErrorMessage)) {
//				softAssert.assertTrue(true, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
//			} else {
//				softAssert.assertTrue(false, "SMAB-T418: Validation message was not displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
//			}
//		}

		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Comparing the data maps for new entry data against the edited data.");
		Map<String, String> dataMapAfterEditing = objBuildPermit.fetchDataFromManualEntry(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		softAssert.assertNotEquals(dataMapAfterEditing, dataMapBeforeEditing, "SMAB-T418: Validating whether updated data has been saved in the manual entry.");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Editing the manual entry field using edit icon on details page.");
		List<String> fieldsToEditUsingEditIcon = Arrays.asList(CONFIG.getProperty("fieldsToEditUsingEditIconOnDetailsPage").split(","));
		objBuildPermit.editManualEntryFieldsUsingEditIcon(dataMap, fieldsToEditUsingEditIcon);
		
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Editing work description individually on details page to check the validation msg for restricted description types.");
//		descValidationMsgs = objBuildPermit.editWorkDescIndividuallyOnDetailsPage(workDescInvalidEntriesErrorMsgs);
//		for(int i = 0; i < descValidationMsgs.size(); i++) {
//			if(descValidationMsgs.get(i).equals(descErrorMessage)) {
//				softAssert.assertTrue(true, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
//			} else {
//				softAssert.assertTrue(false, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
//			}
//		}
				
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}
	
	@Test(description = "Editing and saving existing building permit manual entry with new data", groups = {"smoke"}, priority = 4, enabled = false)
	public void editNewlyCreatedFromRecentlyViewedGrid() throws Exception {
		objBuildPermit.editPermitEntryOnDetailsPage(true);
		List<String> listOfTxtAndDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntryTxtAndDrpDownFields").split(","));
		List<String> listOfSearchDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntrySearchDrpDownFields").split(","));
		Map<String, String> dataMapBeforeEditing = objBuildPermit.fetchDataFromManualEntry(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Clearing all mandatory fields and validating messages against them.");
		objBuildPermit.clearPrePopulatedMandatoryFieldsAndSave();
		
		List<String> errorsListWhileEditing = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
		String expMsgInPopUpHeaderWhileEditing = CONFIG.getProperty("expectedErrorMsgForAllMandatoryFields");
		String actMsgInPopUpHeaderWhileEditing = errorsListWhileEditing.get(0);
		softAssert.assertEquals(actMsgInPopUpHeaderWhileEditing, expMsgInPopUpHeaderWhileEditing, "SMAB-T418: Validating mandatory fields missing error in manual entry pop up's header.");		
		String expMsgForIndividualFieldWhileEditing = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
		String actMsgForIndividualFieldWhileEditing = errorsListWhileEditing.get(1);	
		softAssert.assertEquals(actMsgForIndividualFieldWhileEditing, expMsgForIndividualFieldWhileEditing, "SMAB-T418: Validating mandatory fields missing error against individual fields");
		int fieldsCountInHeaderMsgWhileEditing = Integer.parseInt(errorsListWhileEditing.get(2));		
		int individualMsgsCountWhileEditing = Integer.parseInt(errorsListWhileEditing.get(3));
		softAssert.assertEquals(fieldsCountInHeaderMsgWhileEditing, individualMsgsCountWhileEditing, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Entering data for some mandatory fields and validating messages for remaining fields.");
		String dataToEditManualEntry = System.getProperty("user.dir") + BUILDING_PERMIT_EDIT_MANUAL_ENTRY_DATA;
		Map<String, String> dataMapForEdit = objUtil.generateMapFromDataFile(dataToEditManualEntry);
		objBuildPermit.fillSomeMandatoryFieldsAndSave(dataMapForEdit);
		
		errorsListWhileEditing = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
		expMsgInPopUpHeaderWhileEditing = CONFIG.getProperty("expectedErrorMsgForRemainingMandatoryFields");
		actMsgInPopUpHeaderWhileEditing = errorsListWhileEditing.get(0);
		softAssert.assertEquals(actMsgInPopUpHeaderWhileEditing, expMsgInPopUpHeaderWhileEditing, "SMAB-T418: Validating mandatory fields missing error in manual entry pop up's header.");		
		expMsgForIndividualFieldWhileEditing = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
		actMsgForIndividualFieldWhileEditing = errorsListWhileEditing.get(1);	
		softAssert.assertEquals(actMsgForIndividualFieldWhileEditing, expMsgForIndividualFieldWhileEditing, "SMAB-T418: Validating mandatory fields missing error against individual fields");
		fieldsCountInHeaderMsgWhileEditing = Integer.parseInt(errorsListWhileEditing.get(2));		
		individualMsgsCountWhileEditing = Integer.parseInt(errorsListWhileEditing.get(3));
		softAssert.assertEquals(fieldsCountInHeaderMsgWhileEditing, individualMsgsCountWhileEditing, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Filling all mandatory fields and validating messages for work descripton for restricted description types.");
		List<String> workDescInvalidEntriesErrorMsgs = Arrays.asList(CONFIG.getProperty("invalidEntriesForWordDesc").split(","));
		String descErrorMessage = CONFIG.getProperty("descErrorMessage");
		List<String> descValidationMsgs = objBuildPermit.fillRemainingMandatoryFieldsAndSave(dataMapForEdit, workDescInvalidEntriesErrorMsgs);
			
		for(int i = 0; i < descValidationMsgs.size(); i++) {
			if(descValidationMsgs.get(i).equals(descErrorMessage)) {
				softAssert.assertTrue(true, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			} else {
				softAssert.assertTrue(false, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			}
		}

		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Editing work description individually on details page to check the validation msg for restricted description types.");
		descValidationMsgs = objBuildPermit.editWorkDescIndividuallyOnDetailsPage(workDescInvalidEntriesErrorMsgs);
		for(int i = 0; i < descValidationMsgs.size(); i++) {
			if(descValidationMsgs.get(i).equals(descErrorMessage)) {
				softAssert.assertTrue(true, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			} else {
				softAssert.assertTrue(false, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			}
		}
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Comparing the data maps for new entry data against the edited data.");
		Map<String, String> dataMapAfterEditing = objBuildPermit.fetchDataFromManualEntry(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		softAssert.assertNotEquals(dataMapAfterEditing, dataMapBeforeEditing, "SMAB-T418: Validating whether updated data has been saved in the manual entry.");
		
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}
}
