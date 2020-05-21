package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.*;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.apas.generic.DataProviders;
import com.relevantcodes.extentreports.LogStatus;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BuildingPermit_ManualCreationAndProcessing_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildingPermitPage;
	ParcelsPage objParcelsPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	Util objUtil  = new Util();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objParcelsPage = new ParcelsPage(driver);
	}

	/**
	 Below test case is used to validate the manual creation of building permit
	 **/
	@Test(description = "SMAB-T383,SMAB-T520,SMAB-T402,SMAB-T421: Creating manual entry for building permit", dataProvider = "loginUsers", dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit_Demo"}, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_ManualCreateNewBuildingPermitWithDataValidations(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Parcels module
		objApasGenericFunctions.searchModule(modules.PARCELS);	
		
		//Step3: Search and Open the Parcel
		objApasGenericFunctions.displayRecords("All Active Parcels");
		String parcelToSearch = objApasGenericFunctions.getGridDataInHashMap(1).get("APN").get(0);
		System.out.println("Parcel to be linked with the building permit record : " + parcelToSearch);
		objApasGenericFunctions.searchRecords(parcelToSearch);
		objParcelsPage.openParcel(parcelToSearch);

		//Step4: Opening the Primary Situs Screen using the primary situs link on parcel tab and store the values of "situs code" and "primary situs"
		objPage.Click(objParcelsPage.linkPrimarySitus);
		Thread.sleep(3000);
		String situsName = objApasGenericFunctions.getFieldValueFromAPAS("Situs Name");
		String situsCityCode = objApasGenericFunctions.getFieldValueFromAPAS("Situs City Code");
		String situsType = objApasGenericFunctions.getFieldValueFromAPAS("Situs Type");
		String situsDirection = objApasGenericFunctions.getFieldValueFromAPAS("Direction");
		String situsNumber = objApasGenericFunctions.getFieldValueFromAPAS("Situs Number");
		String situsUnitNumber = objApasGenericFunctions.getFieldValueFromAPAS("Situs Unit Number");
		String situsStreetName = objApasGenericFunctions.getFieldValueFromAPAS("Situs Street Name");

		//Step5: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step6: Prepare a test data to create a new building permit
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
		manualBuildingPermitMap.put("APN",parcelToSearch);

		//Step7: Adding a new Building Permit with the APN passed in the above steps
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step8: Opening the Building Permit with the Building Permit Number Passed above and validating the field values
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objApasGenericFunctions.displayRecords("All Manual Building Permits");
		objApasGenericFunctions.searchRecords(buildingPermitNumber);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap();
		objBuildingPermitPage.openBuildingPermit(buildingPermitNumber);

		//Grid Data validation for the building permit created manually
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0), buildingPermitNumber,"SMAB-T383: 'Building Permit Number' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Permit City Code").get(0), manualBuildingPermitMap.get("Permit City Code"),"SMAB-T383: 'Permit City Code' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("County Strat Code Description").get(0), manualBuildingPermitMap.get("County Strat Code Description"),"SMAB-T383: 'County Strat Code Description' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Estimated Project Value").get(0), "$" + manualBuildingPermitMap.get("Estimated Project Value"),"SMAB-T383: 'Estimated Project Value' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Issue Date").get(0), manualBuildingPermitMap.get("Issue Date"),"SMAB-T383: 'Issue Date' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Calculated Processing Status").get(0), "No Process","SMAB-T383: 'Calculated Processing Status' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Processing Status").get(0), "No Process","SMAB-T383: 'Processing Status' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Warning Message").get(0), "","SMAB-T383: 'Warning Message' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Work Description").get(0), manualBuildingPermitMap.get("Work Description"),"SMAB-T383: 'Work Description' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("APN").get(0), parcelToSearch,"SMAB-T383: 'Parcel' validation on the data displayed on the grid");

		//Validation for the fields in the section Building Permit Information
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Building Permit Number", "Building Permit Information"), buildingPermitNumber, "SMAB-T383: 'Building Permit Number' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("County Strat Code Description", "Building Permit Information"), manualBuildingPermitMap.get("County Strat Code Description"), "SMAB-T383: 'County Strat Code Description' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Issue Date", "Building Permit Information"), manualBuildingPermitMap.get("Issue Date"), "SMAB-T383: 'Issue Date' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Description", "Building Permit Information"), manualBuildingPermitMap.get("Work Description"), "SMAB-T383: 'Work Description' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("APN", "Building Permit Information"), parcelToSearch, "SMAB-T383: 'Parcel' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Estimated Project Value", "Building Permit Information"), "$" + manualBuildingPermitMap.get("Estimated Project Value"), "SMAB-T383: 'Estimated Project Value' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Completion Date", "Building Permit Information"), manualBuildingPermitMap.get("Completion Date"), "SMAB-T383: 'Completion Date' Field Validation in 'Building Permit Information' section");

		//Validation for the fields auto populated in the section Situs Information
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs City Code","Situs Information"), situsCityCode, "SMAB-T520: 'Situs City Code' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs","Situs Information"), situsName, "SMAB-T520, SMAB-T421: 'Situs' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Type","Situs Information"), situsType, "SMAB-T383: 'Situs Type' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Direction"), situsDirection, "SMAB-T383: 'Situs Direction' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Number","Situs Information"), situsNumber, "SMAB-T383: 'Situs Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Unit Number","Situs Information"), situsUnitNumber, "SMAB-T383: 'Situs Unit Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Street Name","Situs Information"), situsStreetName, "SMAB-T383: 'Situs Street Name' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit City Code","Situs Information"), manualBuildingPermitMap.get("Permit City Code"), "SMAB-T383: 'Permit City Code' Field Validation in 'Situs Information' section");

		//Validation for the fields auto populated in the section Processing Status
		//Processing and Calculating Processing Status are calculated based on "Processing Status Information" section from "County Strat Code Infomration"
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process", "SMAB-T520, SMAB-T402: 'Processing Status' Field Validation in 'Processing Status' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process", "SMAB-T402: 'Calculated Processing Status' Field Validation in 'Processing Status' section");

		//Validation for the fields auto populated in the section System Information
		//Strat Code reference Number can be fetched from the County Strat Code Screen of the code choosen while creating the building permit
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Strat Code Reference Number","System Information"), "42", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Record Type","System Information"), "Manual Entry Building Permit", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate
	 1. Error appearing if mandatory fields are not filled while editing the existing building permit record
	 2. Save the record after updating the value in a field
	 **/
	@Test(description = "SMAB-T466: Mandatory Field Validation while editing manual building permit and editing a record", groups = {"smoke","regression","buildingPermit_Demo"}, dataProvider = "loginUsers", dataProviderClass = DataProviders.class, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_EditAndValidateRequiredFieldsValidationsForExistingPermit(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Editing the existing Building Permit without giving all the mandatory fields and validating the error messages appearing
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the existing Building Permit without giving all the mandatory fields");
		objApasGenericFunctions.displayRecords("All Manual Building Permits");
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap(1);
		String buildingPermitNumber = manualBuildingPermitGridDataMap.get("Building Permit Number").get(0);
		objApasGenericFunctions.searchRecords(buildingPermitNumber);
		objBuildingPermitPage.openBuildingPermit(buildingPermitNumber);
		objPage.Click(objBuildingPermitPage.editButton);
		Thread.sleep(2000);

		//Step4: Save after entering 'Tree Removal' in Work Description. There should be an error
		String expectedWorkDescriptionError = "This is a permit type that will not be further processed. No Process for Work Desc with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"";
		objPage.waitForElementToBeClickable(objBuildingPermitPage.workDescriptionTxtBox,30);
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,"Tree Removal");
		objPage.Click(objBuildingPermitPage.saveButton);
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.errorMsgOnTop),expectedWorkDescriptionError,"SMAB-T466: Warning message validation on the top when 'Work Description' field is having following values 'Tree Removal', 'public works permits', 'temporary signs/banners'");

		//Step5: Save after clearing the mandatory work description field
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,"");
		objPage.Click(objBuildingPermitPage.saveButton);

		//Step6: Validating the error message on edit pop when mandatory fields are not filled
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.errorMsgOnTop),"These required fields must be completed: Work Description","SMAB-T466: Warning message validation on the top when 'Work Description' field is not entered while editing the building permit record");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Work Description"),"Complete this field","SMAB-T466: Warning message validation at the field level 'Work Description' field is not entered while editing the building permit record");

		//Step7: Enter the updated estimated project value and builing permit number and save the record
		String updatedWorkDescriptionValue = "New Construction " + objUtil.getCurrentDate("mmss");
		String updatedBuildingPermitNumber = "LM-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		System.out.println("Value to be updated in 'Work Description' field : " + updatedWorkDescriptionValue);
		System.out.println("Old 'Building Permit Number' value : " + buildingPermitNumber);
		System.out.println("Value to be updated in 'Building Permit Number' field : " + updatedBuildingPermitNumber);

		objPage.enter(objBuildingPermitPage.buildingPermitNumberTxtBox,updatedBuildingPermitNumber);
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,updatedWorkDescriptionValue);
		objPage.Click(objBuildingPermitPage.saveButton);

		Thread.sleep(3000);

		//Step8: Validation for record with old building permit number exists or not
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objApasGenericFunctions.searchRecords(buildingPermitNumber);
		manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap();
		softAssert.assertTrue(manualBuildingPermitGridDataMap.get("Building Permit Number") == null, "SMAB-T466: Validation that record with old building permit number " + buildingPermitNumber + " should not exist as building permit number is updated");

		//Step9: Search the building permit number record edited above
		objApasGenericFunctions.searchRecords(updatedBuildingPermitNumber);

		//Step10: Validating that new value entered in estimated project value filed is saved
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMapAfterEdit = objApasGenericFunctions.getGridDataInHashMap(1);
		softAssert.assertEquals(manualBuildingPermitGridDataMapAfterEdit.get("Work Description").get(0),updatedWorkDescriptionValue,"SMAB-T466: Validating the 'Work Description' after editing the record");
		softAssert.assertEquals(manualBuildingPermitGridDataMapAfterEdit.get("Building Permit Number").get(0),updatedBuildingPermitNumber,"SMAB-T466: Validating the 'Building Permit Number' after editing the record");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate error appearing if mandatory fields are not filled while manually creating building permit
	 **/
	@Test(description = "SMAB-T418: Mandatory Field Validation while creating manual building permit", groups = {"smoke","regression","buildingPermit_Demo"}, dataProvider = "loginUsers", dataProviderClass =  DataProviders.class, alwaysRun = true, enabled = true)
	public void verify_ManualCreationWithRequiredieldsValidations(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open and save building permit manual creation form without entering the data
		objBuildingPermitPage.openNewForm();
		objPage.Click(objBuildingPermitPage.saveButton);

		//Step4: Validate the error message appeared for mandatory fields
		String expectedErrorMessageOnTop = "These required fields must be completed: Estimated Project Value, Issue Date, Building Permit Number, APN, Permit City Code, County Strat Code Description, Work Description";
		String expectedIndividualFieldMessage = "Complete this field";
		softAssert.assertEquals(objBuildingPermitPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop,"SMAB-T418: Validating mandatory fields missing error in manual entry pop up header.");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Building Permit Number"),expectedIndividualFieldMessage,"SMAB-T418: Validating mandatory fields missing error for 'Building Permit Number'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("APN"),expectedIndividualFieldMessage,"SMAB-T418: Validating mandatory fields missing error for 'Parcel'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("County Strat Code Description"),expectedIndividualFieldMessage,"SMAB-T418: Validating mandatory fields missing error for 'County Strat Code Description'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Estimated Project Value"),expectedIndividualFieldMessage,"SMAB-T418: Validating mandatory fields missing error for 'Estimated Project Value'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Issue Date"),expectedIndividualFieldMessage,"SMAB-T418: Validating mandatory fields missing error for 'Issue Date'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Work Description"),expectedIndividualFieldMessage,"SMAB-T418: Validating mandatory fields missing error for 'Work Description'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Permit City Code"),expectedIndividualFieldMessage,"SMAB-T418: Validating mandatory fields missing error for 'Permit City Code'");

		//Step5: Closing the Manual building permit creation pop up
		objPage.Click(objBuildingPermitPage.cancelButton);

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate that building permit can be created when 'Building Permit Number, Permit City Code and APN' is unique
	 **/
	@Test(description = "SMAB-T519: Validate that building permit can be created when 'Building Permit Number, Permit City Code and APN' is unique", groups = {"smoke","regression","buildingPermit_Demo"}, dataProvider = "loginUsers", dataProviderClass = DataProviders.class, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_ManualCreationForDuplicateCheck(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Get any Active Parcel to create the Building Permit record
		objApasGenericFunctions.searchModule(modules.PARCELS);
		objApasGenericFunctions.displayRecords("All Active Parcels");
		String activeParcel = objApasGenericFunctions.getGridDataInHashMap(1).get("APN").get(0);

		//Step3: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step4: Prepare a test data to create a new building permit
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("APN",activeParcel);

		//Step5: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
        
		//Step6: Create another building permit with the data user above. An error should occur
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
		
		objPage.scrollToElement(objBuildingPermitPage.warningMessage);
        
		//Step7: Validate the error message appeared for mandatory fields
		String expectedWarningMessage = "This record looks like a duplicate.View Duplicates";
		softAssert.assertEquals(objBuildingPermitPage.warningMessage.getText(),expectedWarningMessage,"SMAB-T519: Warning Message validation for duplicate fields");
        
		//Step8: Validation of the building permit after clicking on View Duplicate Link
		objPage.Click(objBuildingPermitPage.viewDuplicateLink);
		
		objPage.waitForElementToBeVisible(objBuildingPermitPage.openBuildingPermitLink);
		
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("Building Permit Number"),manualBuildingPermitMap.get("Building Permit Number"),"SMAB-T519: 'Building Permit Number' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("Permit City Code"),manualBuildingPermitMap.get("Permit City Code"),"SMAB-T519: 'Permit City Code' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("County Strat Code Description"),manualBuildingPermitMap.get("County Strat Code Description"),"SMAB-T519: 'County Strat Code Description' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("Estimated Project Value"),"$" + manualBuildingPermitMap.get("Estimated Project Value"),"SMAB-T519: 'Estimated Project Value' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("Issue Date"),manualBuildingPermitMap.get("Issue Date"),"SMAB-T519: 'Issue Date' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("Calculated Processing Status"),"No Process","SMAB-T519: 'Calculated Processing Status' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("Processing Status"),manualBuildingPermitMap.get("Processing Status"),"SMAB-T519: 'Processing Status' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("Work Description"),manualBuildingPermitMap.get("Work Description"),"SMAB-T519: 'Work Description' field validation on View Duplicate screen");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromViewDuplicateScreen("APN"),manualBuildingPermitMap.get("APN"),"SMAB-T519: 'Parcel' field validation on View Duplicate screen");

		//Step9: Closing the pops opened on the application
		objPage.Click(objBuildingPermitPage.closeViewDuplicatePopUpButton);
		objPage.Click(objBuildingPermitPage.closeEntryPopUp);

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate the warning message when building permit is created with retired parcel
	 **/
	@Test(description = "SMAB-T626: Validate that warning message appears when a building permit is created with retired parcel", groups = {"smoke","regression","buildingPermit_Demo"}, dataProvider = "loginUsers",dataProviderClass = DataProviders.class, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_ManualCreationWithRetiredParcel(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Parcels module
		objApasGenericFunctions.searchModule(modules.PARCELS);

		//Step3: Search and Open the Parcel
		objApasGenericFunctions.displayRecords("All Retired Parcels");
		String retiredParcel = objApasGenericFunctions.getGridDataInHashMap(1).get("APN").get(0);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Prepare a test data to create a new building permit with retired parcel
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
		manualBuildingPermitMap.put("APN",retiredParcel);

		//Step4: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step5: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Validation for the warning message appearing on the grid
		objApasGenericFunctions.displayRecords("All Manual Building Permits");
		objApasGenericFunctions.searchRecords(buildingPermitNumber);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap();
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Warning Message").get(0), "The parcel is retired. Please review and confirm the APN on the Building Permit.","SMAB-T626: 'Priority Message' validation on the data displayed on the grid");

		//Step6: open the building permit created with retired parcel
		objBuildingPermitPage.openBuildingPermit(buildingPermitNumber);

		//Validate the warning message for retired parcel is appearing on the Details page as well
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.warningMessageWithPriorityFlag), "The parcel is retired. Please review and confirm the APN on the Building Permit.","SMAB-T626: 'Priority Message' validation on building permit details page screen");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate the values in Processing Status and Calculated Processing Status fields when processing status was not selected
	 **/
	@Test(description = "SMAB-T399: Validate that Process and Calculated Process Status fields are auto populated when process status field is not selected", groups = {"regression","buildingPermit_Demo"}, dataProvider = "loginUsers",dataProviderClass = DataProviders.class, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_Manual_ProcessFieldsStatus_ProcessingStatusNotSelectedByUser(String loginUser) throws Exception {

		//Pre-requisite: Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		System.out.println("Active APN fetched through Salesforce API : " + activeAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Prepare a test data to create a new building permit with active parcel fetched through SQL Query
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("APN",activeAPN);
		manualBuildingPermitMap.put("Processing Status","--None--");

		//Step4: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);

		//Step5: Validation of Processing Status and Calculated Processing Status values when Processing Status is not selected
		//Below is the rule based on Selected County Strat Code 'REPAIR ROOF'
		//Processing Status should be populated as 'No Process'
		//Calculated Processing Status should be populated as 'No Process' as Estimated project value is not satisfying the criteria "Greater Than or Equal to $25,000"
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T399: Validation of 'Calculated Processing Status' field when Process Status was not selected while creating building permit manually");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T399: Validation of 'Processing Status' field when Process Status was not selected while creating building permit manually");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate the values in Processing Status and Calculated Processing Status fields when processing status was selected
	 **/
	@Test(description = "SMAB-T400: Validate that Process and Calculated Process Status fields are auto populated when process status field is selected", groups = {"regression","buildingPermit_Demo"}, dataProvider = "loginUsers",dataProviderClass = DataProviders.class, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_Manual_ProcessFieldsStatus_ProcessingStatusSelectedByUser(String loginUser) throws Exception {

		//Pre-requisite: Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		System.out.println("Active APN fetched through Salesforce API : " + activeAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Prepare a test data to create a new building permit with retired parcel
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("APN",activeAPN);
		manualBuildingPermitMap.put("Processing Status","Process");

		//Step4: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);

		//Step5: Validation of Processing Status and Calculated Processing Status values when Processing Status is not selected
		//Below is the rule based on Selected County Strat Code 'REPAIR ROOF'
		//Processing Status should be populated as 'Process' as it has been explicitly selected by user
		//Calculated Processing Status should be populated as 'No Process' as Estimated project value is not satisfying the criteria "Greater Than or Equal to $25,000"
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Process Status was selected while creating building permit manually");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when Process Status was selected while creating building permit manually");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate the values in Processing Status and Calculated Processing Status fields when Estimated Project value or County Strat Code is changed
	 Below is the rule based on Selected County Strat Code 'REPAIR ROOF'
	 Processing Status should be populated as it has been explicitly selected by user
	 Calculated Processing Status should be populated as 'Process' if Estimated project value is "Greater Than or Equal to $25,000" ELSE 'No Process'
	 **/
	@Test(description = "SMAB-T403, SMAB-T404: Validated the Calculated Process Status field is automatically updated when estimated project value or county strat code is updated", groups = {"regression","buildingPermit_Demo"}, dataProvider = "loginUsers", dataProviderClass = DataProviders.class, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_Manual_CalculatedProcessingStatus_EstimatedProjectValueOrCountyStratCodeUpdated(String loginUser) throws Exception {

		//Pre-requisite: Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		System.out.println("Active APN fetched through Salesforce API : " + activeAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Prepare a test data to create a new building permit with retired parcel
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("APN",activeAPN);
		manualBuildingPermitMap.put("Processing Status","No Process");
		manualBuildingPermitMap.put("Estimated Project Value","3000");

		//Step4: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step5: validation of process status after adding the building permit
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when Estimated Project value is selected 3000 and county strat code as REPAIR ROOF");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Estimated Project value is selected 3000 and county strat code as REPAIR ROOF");

		//step6: Update the estimated project value as less than 25000 and check the status of Calculated processing status field is updated accordingly
		objPage.Click(objBuildingPermitPage.editButton);
		objBuildingPermitPage.enterEstimatedProjectValue("25000");
		objPage.Click(objBuildingPermitPage.saveButton);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Estimated Project value is selected 25000 and county strat code as REPAIR ROOF");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when Estimated Project value is selected 25000 and county strat code as REPAIR ROOF");

		//Step7: Update the estimated project value such that value of calculated processing status is switched
		objPage.Click(objBuildingPermitPage.editButton);
		objBuildingPermitPage.enterEstimatedProjectValue("7000");
		objPage.Click(objBuildingPermitPage.saveButton);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Estimated Project value is selected 7000 and county strat code as REPAIR ROOF");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when Process Status when Estimated Project value is selected 7000 and county strat code as REPAIR ROOF");

		//Step8: Update the county strat code such that value of calculated processing status is switched
		//Following is the condition for SOLAR County Strat Code "'No Process' if estimated project value is Less Than or Equal To 5000
		objPage.Click(objBuildingPermitPage.editButton);
		objPage.Click(objBuildingPermitPage.deleteRepairRoof);
		objBuildingPermitPage.searchAndSelectFromDropDown(objBuildingPermitPage.countyStratCodeSearchBox, "SOLAR");
		objPage.Click(objBuildingPermitPage.saveButton);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "Process","SMAB-T400: Validation of 'Calculated Processing Status' field when County strat code is changed to SOLAR from REPAIR ROOF");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when County strat code is changed to SOLAR from REPAIR ROOF");

		//Step9: Update the processing status as No Process and Calculated Processing status should be updated with the same value
		objPage.Click(objBuildingPermitPage.editButton);
		objBuildingPermitPage.selectFromDropDown(objBuildingPermitPage.processingStatusDrpDown, "No Process");
		objPage.Click(objBuildingPermitPage.saveButton);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Processing Status is changed to 'No Process' from 'Process'");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when Processing Status is changed to 'No Process' from 'Process'");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

}
