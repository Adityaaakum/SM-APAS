package com.apas.Tests.BuildingPermit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermitManualCreateAndEditTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildPermit;
	EFileImportPage objEfileHomePage;
	Util objUtil;
	SoftAssertion softAssert;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}
		
	@AfterMethod(alwaysRun=true)
	public void afterMethod() throws IOException, InterruptedException{
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate the manual creation of building permit 
	 **/
	@Test(description = "Creating new manual entry for building permit", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke","regression","BuildingPermit"}, priority = 0, enabled = true)
	public void createBldngPrmtManualEntry(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		if(loginUser.equalsIgnoreCase("dataAdmin")) {
			System.setProperty("isDataAdminLoggedIn", "true");
		}
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the building permit module and select all view on grid
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objApasGenericFunctions.selectAllManualBuildingPermitOptionOnGrid();			
		
		//Step3: Enter the data to on manual create building permit screen
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Opening manual entry pop and clicking save button without entring any data to validate error messages for mandatory fields.");
		objBuildPermit.openNewForm();
		objBuildPermit.waitForManualEntryPopUpToLoad();
		objPage.Click(objBuildPermit.saveButton);

		//Step4: Validate the error messages displayed on clicking save button without entering mandatory fields
		List<String> errorsList = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
		String expMsgInPopUpHeader = CONFIG.getProperty("expectedErrorMsgForAllMandatoryFieldsNewEntry");
		String actMsgInPopUpHeader = errorsList.get(0);
		softAssert.assertEquals(actMsgInPopUpHeader, expMsgInPopUpHeader, "SMAB-T418, SMT-420: Validating mandatory fields missing error in manual entry pop up's header.");		
		String expMsgForIndividualField = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
		String actMsgForIndividualField = errorsList.get(1);	
		softAssert.assertEquals(expMsgForIndividualField, actMsgForIndividualField, "SMAB-T418: Validating mandatory fields missing error against individual fields");
		int fieldsCountInHeaderMsg = Integer.parseInt(errorsList.get(2));		
		int individualMsgsCount = Integer.parseInt(errorsList.get(3));
		softAssert.assertEquals(fieldsCountInHeaderMsg, individualMsgsCount, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
		
		//Step5: Create data map from the JSON file
		String manualEntryData = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_MANUAL_ENTRY_DATA;
		Map<String, String> dataMap = objUtil.generateMapFromJsonFile(manualEntryData, "DataToCreateBuildingPermitManualEntry");
		
		//Step6: Filling out mandatory and required fields and aborting the manual entry by clicking Cancel button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating to ensure entry is not created when all mandatory fields filled and process is aborted without saving.");	
		objBuildPermit.enterManualEntryData(dataMap);
		objPage.Click(objBuildPermit.cancelButton);
		
		//Step7: Validating manual entry should not have been created on clicking Cancel button.
		String permitNum = System.getProperty("permitNumber");
		boolean buildingPermitNotCreated = objBuildPermit.checkManualPermitEntryOnGrid(permitNum);
		softAssert.assertTrue(!buildingPermitNotCreated, "SMAB-T418: Manual entry is not created on closing new manual entry window without saving.");
		
		//Step8: Opening the manual entry form again and filling required fields and saving the form
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Verifying whether manual entry gets saved and pop up window for new entry gets displayed when 'Save & New' option is selected.");
		objBuildPermit.openNewForm();
		objBuildPermit.enterManualEntryData(dataMap);
		boolean isNewManualEntryPopUpDisplayed = objBuildPermit.saveEntryAndOpenNewAndExit();
		softAssert.assertTrue(isNewManualEntryPopUpDisplayed, "SMAB-T418: Validating whether pop up for new entry is displayed.");
				
		//Step9: Checking whether the newly created manual entry is successfully reflecting or not
		boolean isLoaderVisible = objBuildPermit.checkAndHandlePageLoaderOnEntryCreation();
		if(isLoaderVisible) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating newly created manual entry successfully displayed on recently viewed grid.");
			boolean isEntryDisplayed = objBuildPermit.checkManualPermitEntryOnGrid(permitNum);
			softAssert.assertTrue(isEntryDisplayed, "SMAB-T418: Validating whether manual entry successfully created and displayed on recently viewed grid.");
		} else {
			ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating newly created manual entry successfully displayed on details page.");
			boolean isEntryDisplayed = objBuildPermit.checkManualPermitEntryOnDetailsPage(permitNum);
			softAssert.assertTrue(isEntryDisplayed, "SMAB-T418: Validating whether manual entry successfully created and displayed on details page.");	
		}
		softAssert.assertAll();
	}

	/**
	 Below test case is used to validate the edit process of manual building permit through EDIT button on details page
	 **/
	@Test(description = "Editing & saving existing building permit manual entry from Details Page Using Edit Button", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke"}, priority = 1, enabled = true)
	public void editBldngPrmtManualEntryOnDetailsPageUsingEditButton(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		
		//Step3: Clicking on given building permit number from recently viewed grid to navigate to details page
		String permitNum = System.getProperty("permitNumber");
		objBuildPermit.navToDetailsPageOfGivenBuildingPermit(permitNum);
		
		//Step4: Collecting the existing data of manual building permit entry from all the fields into a map
		List<String> listOfTxtAndDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntryTxtAndDrpDownFields").split(","));
		List<String> listOfSearchDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntrySearchDrpDownFields").split(","));
		Map<String, String> dataMapBeforeEditing = objBuildPermit.getExistingManualEntryData(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		
		//Step5: Clicking on the EDIT BUTTON to open the manual entry in edit mode
		objPage.Click(objBuildPermit.editBtnDetailsPage);
		
		//Step6: Clearing all the pre-populated mandatory fields & then clicking SAVE BUTTON
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Clearing all mandatory fields and validating messages against them.");
		objBuildPermit.clearPrePopulatedMandatoryFields();
		objPage.Click(objBuildPermit.saveBtnEditPopUp);
		
		//Step7: Validating all the error messages against individual fields and error message in header of pop up window
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
		
		//Step8: Filling some mandatory fields & keeping rest as blank and then saving the entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Entering data for some mandatory fields and validating messages for remaining fields.");
		String manualEntryData = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_MANUAL_ENTRY_DATA;
		Map<String, String> dataMapForEdit = objUtil.generateMapFromJsonFile(manualEntryData, "DataToUpdateUsingEditButtonOnDetailsPage");
		objBuildPermit.fillSomeMandatoryFields(dataMapForEdit);
		objPage.Click(objBuildPermit.saveBtnEditPopUp);
		
		//Step9: Validating all the error messages against remaining individual fields and error message in header of pop up window
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
		
		//Step10: Filling remaining mandatory fields and the entring restricted types in work desc & saving the entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Filling all mandatory fields and validating messages for work descripton for restricted description types.");
		List<String> workDescInvalidEntriesErrorMsgs = Arrays.asList(CONFIG.getProperty("invalidEntriesForWordDesc").split(","));
		String descErrorMessage = CONFIG.getProperty("descErrorMessage");
		List<String> descValidationMsgs = objBuildPermit.fillRemainingMandatoryFields(dataMapForEdit, workDescInvalidEntriesErrorMsgs);
		objPage.Click(objBuildPermit.saveBtnEditPopUp);
		
		//Step11: Validating all the error messages against restricted work description types
		for(int i = 0; i < descValidationMsgs.size(); i++) {
			if(descValidationMsgs.get(i).equals(descErrorMessage)) {
				softAssert.assertTrue(true, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			} else {
				softAssert.assertTrue(false, "SMAB-T418: Validation message was not displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			}
		}

		//Step12: Comparing the data maps: Entry data before edit against Entry data post edit to ensure entry has been updated
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Comparing the data maps for new entry data against the edited data.");
		Map<String, String> dataMapAfterEditing = objBuildPermit.getExistingManualEntryData(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		for(Map.Entry<String, String> entry : dataMapAfterEditing.entrySet()) {
			String key = entry.getKey();
			boolean doesValueDiffer = !(dataMapAfterEditing.get(key).equals(dataMapBeforeEditing.get(key)));
			softAssert.assertTrue(doesValueDiffer, "SMAB-T418: Validating whether value has updated in manual "
					+ "entry after editing '"+ key +"'. Value Before Editing - "+ dataMapBeforeEditing.get(key) + " || Value After Editing - "+ dataMapAfterEditing.get(key));
		}
		softAssert.assertAll();
	}

	/**
	 Below test case is used to validate the edit process of manual building permit through EDIT button on recently viewed grid
	 **/
	@Test(description = "Editing & saving existing building permit manual entry from recently viewed grid using Edit Button", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke"}, priority = 2, enabled = true)
	public void editBldngPrmtManualOnRecentlyViewedGridUsingEditButton(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Clicking on given building permit number from recently viewed grid to navigate to details page
		String permitNum = System.getProperty("permitNumber");
		objBuildPermit.navToDetailsPageOfGivenBuildingPermit(permitNum);
		
		//Step4: Collecting the existing data of manual building permit entry from all the fields into a map
		List<String> listOfTxtAndDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntryTxtAndDrpDownFields").split(","));
		List<String> listOfSearchDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntrySearchDrpDownFields").split(","));
		Map<String, String> dataMapBeforeEditing = objBuildPermit.getExistingManualEntryData(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		
		//Step5: Navigate back from details page to recently viewed grid & clicking Show More link & click Edit link under it
		objBuildPermit.clickAction(objBuildPermit.waitForElementToBeClickable(objBuildPermit.bldngPrmtTabDetailsPage));
		objBuildPermit.clickShowMoreLinkOnRecentlyViewedGrid(permitNum);
		objBuildPermit.clickAction(objBuildPermit.waitForElementToBeClickable(objBuildPermit.editLinkUnderShowMore));
		
		//Step6: Clearing all the pre-populated mandatory fields & then clicking SAVE BUTTON
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Clearing all mandatory fields and validating messages against them.");
		objBuildPermit.clearPrePopulatedMandatoryFields();
		objBuildPermit.Click(objBuildPermit.saveBtnEditPopUp);
		
		//Step7: Validating all the error messages against individual fields and error message in header of pop up window
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
		
		//Step8: Filling some mandatory fields & keeping rest as blank and then saving the entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Entering data for some mandatory fields and validating messages for remaining fields.");
		String manualEntryData = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_MANUAL_ENTRY_DATA;
		Map<String, String> dataMapForEdit = objUtil.generateMapFromJsonFile(manualEntryData, "DataToUpdateUsingEditLinkOnRecentlyViewedGrid");
		objBuildPermit.fillSomeMandatoryFields(dataMapForEdit);
		objBuildPermit.Click(objBuildPermit.saveBtnEditPopUp);
		
		//Step9: Validating all the error messages against remaining individual fields and error message in header of pop up window
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
		
		//Step10: Filling remaining mandatory fields and the entring restricted types in work desc & saving the entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Filling all mandatory fields and validating messages for work descripton for restricted description types.");
		List<String> workDescInvalidEntriesErrorMsgs = Arrays.asList(CONFIG.getProperty("invalidEntriesForWordDesc").split(","));
		String descErrorMessage = CONFIG.getProperty("descErrorMessage");
		List<String> descValidationMsgs = objBuildPermit.fillRemainingMandatoryFields(dataMapForEdit, workDescInvalidEntriesErrorMsgs);
		objBuildPermit.Click(objBuildPermit.saveBtnEditPopUp);
		
		//Step11: Validating all the error messages against restricted work description types
		for(int i = 0; i < descValidationMsgs.size(); i++) {
			if(descValidationMsgs.get(i).equals(descErrorMessage)) {
				softAssert.assertTrue(true, "SMAB-T418: Validation message was displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			} else {
				softAssert.assertTrue(false, "SMAB-T418: Validation message was not displayed for restricted description '" + workDescInvalidEntriesErrorMsgs.get(i) +"'");
			}
		}

		//Step12: Comparing the data maps: Entry data before edit Vs Entry data post edit
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Comparing the data maps for new entry data against the edited data.");
		Map<String, String> dataMapAfterEditing = objBuildPermit.getExistingManualEntryData(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		for(Map.Entry<String, String> entry : dataMapAfterEditing.entrySet()) {
			String key = entry.getKey();
			boolean doesValueDiffer = !(dataMapAfterEditing.get(key).equals(dataMapBeforeEditing.get(key)));
			softAssert.assertTrue(doesValueDiffer, "SMAB-T418: Validating whether value has updated in manual "
					+ "entry after editing '"+ key +"'. Value Before Editing - "+ dataMapBeforeEditing.get(key) + " || Vc11qaz mjy6alue After Edit- "+ dataMapAfterEditing.get(key));
		}		
		softAssert.assertAll();
	}
	
	/**
	 Below test case is used to validate the edit process of manual building permit through EDIT icon on details page
	 **/
	@Test(description = "Editing & saving existing building permit manual entry from Details Page Using Edit Icon", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke"}, priority = 3, enabled = true)
	public void editBldngPrmtManualEntryOnDetailsPageUsingEditIcon(String loginUser) throws Exception {
		//Step1: Login to the APAS application using the given user
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		
		//Step3: Clicking on given building permit number from recently viewed grid to navigate to details page
		String permitNum = System.getProperty("permitNumber");
		objBuildPermit.navToDetailsPageOfGivenBuildingPermit(permitNum);
		
		//Step4: Collecting the existing data of manual building permit entry from all the fields into a map
		List<String> listOfTxtAndDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntryTxtAndDrpDownFields").split(","));
		List<String> listOfSearchDrpDowns = Arrays.asList(CONFIG.getProperty("manualEntrySearchDrpDownFields").split(","));
		Map<String, String> dataMapBeforeEditing = objBuildPermit.getExistingManualEntryData(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		
		//Step5: Clicking on the EDIT ICON in details section to open the manual entry in edit mode
		objBuildPermit.Click(objBuildPermit.editIconDetailsPage);
		
		//Step6: Reading the data file and generating data map to enter new data in existing permit entry
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Entering new / updated data for remaining fields.");
		String manualEntryData = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_MANUAL_ENTRY_DATA;
		Map<String, String> dataMapForEdit = objUtil.generateMapFromJsonFile(manualEntryData, "DataToUpdateUsingEditIconOnDetailsPage");
		
		//Step6: Entering new data in all the required fields
		List<String> TxtFieldsToEdit = Arrays.asList(CONFIG.getProperty("TxtFieldsToEditUsingEditIcon").split(","));
		List<String> DrpDownFieldsToEdit = Arrays.asList(CONFIG.getProperty("DrpDownFieldsToEditUsingEditIcon").split(","));
		List<String> SearchAndSelectFieldsToEdit = Arrays.asList(CONFIG.getProperty("SearchAndSelectFieldsToEditUsingEditIcon").split(","));
		objBuildPermit.enterUpdatedDataForRequiredFields(dataMapForEdit, TxtFieldsToEdit, DrpDownFieldsToEdit, SearchAndSelectFieldsToEdit);
		objBuildPermit.Click(objBuildPermit.saveBtnDetailsPage);
		
		//Step7: Entering restricted word description types in work description field
		List<String> workDescInvalidEntries = Arrays.asList(CONFIG.getProperty("invalidEntriesForWordDesc").split(","));
		List<String> descValidationMsgs = objBuildPermit.editWorkDescIndividuallyOnDetailsPage(workDescInvalidEntries);
		String descErrorMessage = CONFIG.getProperty("descErrorMessage");
		
		//Step8: Validating error message for restricted work description entries
		for(int i = 0; i < descValidationMsgs.size(); i++) {
			if(descValidationMsgs.get(i).equals(descErrorMessage)) {
				softAssert.assertTrue(true, "SMAB-T418: Validation message displayed for restricted description '" + workDescInvalidEntries.get(i) +"'");
			} else {
				softAssert.assertTrue(false, "SMAB-T418: Validation message not displayed for restricted description '" + workDescInvalidEntries.get(i) +"'");
			}
		}
		
		List<String> issueDateValues = objBuildPermit.editAndCancelIssueDateDetailsPage();
		softAssert.assertEquals(issueDateValues.get(0), issueDateValues.get(1),"SMAB-T418: Validation value of Issue Date field before and after edit wihout saving new value");		
		
		Map<String, String> autoDisplayedFields = objBuildPermit.getSitusCodeAndCalProcStatusFromDetailsPage();
		for(Map.Entry<String, String> entry : autoDisplayedFields.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			softAssert.assertTrue(!(value.equals("")), "SMAB-T418: Validation auto populated value of '" + key +"'. Value displayed post saving manual entry: " + value);
		}
		
		//Step9: Comparing the data maps: Entry data before edit Vs Entry data post edit to ensure entry has been updated
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Comparing the data maps for new entry data against the edited data.");
		Map<String, String> dataMapAfterEditing = objBuildPermit.getExistingManualEntryData(listOfTxtAndDrpDowns, listOfSearchDrpDowns);
		for(Map.Entry<String, String> entry : dataMapAfterEditing.entrySet()) {
			String key = entry.getKey();
			boolean doesValueDiffer = !(dataMapAfterEditing.get(key).equals(dataMapBeforeEditing.get(key)));
			softAssert.assertTrue(doesValueDiffer, "SMAB-T418: Validating whether value has updated in manual "
					+ "entry after editing '"+ key +"'. Value Before Editing - "+ dataMapBeforeEditing.get(key) + " || Vc11qaz mjy6alue After Edit- "+ dataMapAfterEditing.get(key));
		}				
		softAssert.assertAll();
	}
}