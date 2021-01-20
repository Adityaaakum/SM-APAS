package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.*;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.DataProviders.DataProviders;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingPermit_ManualCreationAndProcessing_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	BuildingPermitPage objBuildingPermitPage;
	ParcelsPage objParcelsPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	Util objUtil  = new Util();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objParcelsPage = new ParcelsPage(driver);
	}

	/**
	 Below test case is used to validate the manual creation of building permit
	 **/
	@Test(description = "SMAB-T383,SMAB-T520,SMAB-T402,SMAB-T421,SMAB-T416: Creating manual entry for building permit", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class, groups={"smoke","regression","buildingPermit"}, alwaysRun = true)
	public void BuildingPermit_ManualCreateNewBuildingPermitWithDataValidations(String loginUser) throws Exception {


		//Fetching the Active Parcel
		String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C !='' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the Parcels module
		objBuildingPermitPage.searchModule(modules.PARCELS);

		//Step3: Search and Open the Parcel
		objBuildingPermitPage.globalSearchRecords(parcelToSearch);

		//Step4: Opening the Primary Situs Screen using the primary situs link on parcel tab and store the values of "situs code" and "primary situs"
		objPage.Click(objParcelsPage.linkPrimarySitus);
		Thread.sleep(3000);
		String situsName = objBuildingPermitPage.getFieldValueFromAPAS("Situs Name");
		String situsCityCode = objBuildingPermitPage.getFieldValueFromAPAS("Situs City Code");
		String situsType = objBuildingPermitPage.getFieldValueFromAPAS("Situs Type");
		String situsDirection = objBuildingPermitPage.getFieldValueFromAPAS("Direction");
		String situsNumber = objBuildingPermitPage.getFieldValueFromAPAS("Situs Number");
		String situsUnitNumber = objBuildingPermitPage.getFieldValueFromAPAS("Situs Unit Number");
		String situsStreetName = objBuildingPermitPage.getFieldValueFromAPAS("Situs Street Name");

		//Step5: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step6: Prepare a test data to create a new building permit
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
		manualBuildingPermitMap.put("APN",parcelToSearch);

		//Step7: Adding a new Building Permit with the APN passed in the above steps
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step8: Opening the Building Permit with the Building Permit Number Passed above and validating the field values
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);
		objBuildingPermitPage.displayRecords("All Manual Building Permits");
		objBuildingPermitPage.searchRecords(buildingPermitNumber);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objBuildingPermitPage.getGridDataInHashMap();
		objBuildingPermitPage.openBuildingPermit(buildingPermitNumber);

		//Grid Data validation for the building permit created manually
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0), buildingPermitNumber,"SMAB-T383,SMAB-T416: 'Building Permit Number' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Permit City Code").get(0), manualBuildingPermitMap.get("Permit City Code"),"SMAB-T383: 'Permit City Code' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("County Strat Code Description").get(0), manualBuildingPermitMap.get("County Strat Code Description"),"SMAB-T383: 'County Strat Code Description' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Estimated Project Value").get(0), "$" + manualBuildingPermitMap.get("Estimated Project Value"),"SMAB-T383: 'Estimated Project Value' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Issue Date").get(0), manualBuildingPermitMap.get("Issue Date"),"SMAB-T383: 'Issue Date' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Calculated Processing Status").get(0), "No Process","SMAB-T383: 'Calculated Processing Status' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Processing Status").get(0), "No Process","SMAB-T383: 'Processing Status' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Warning Message").get(0), "","SMAB-T383: 'Warning Message' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Work Description").get(0), manualBuildingPermitMap.get("Work Description"),"SMAB-T383,SMAB-T416: 'Work Description' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("APN").get(0), parcelToSearch,"SMAB-T383,SMAB-T416: 'Parcel' validation on the data displayed on the grid");

		//Validation for the fields in the section Building Permit Information
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Building Permit Number", "Building Permit Information"), buildingPermitNumber, "SMAB-T383: 'Building Permit Number' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("County Strat Code Description", "Building Permit Information"), manualBuildingPermitMap.get("County Strat Code Description"), "SMAB-T383: 'County Strat Code Description' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Issue Date", "Building Permit Information"), manualBuildingPermitMap.get("Issue Date"), "SMAB-T383: 'Issue Date' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Work Description", "Building Permit Information"), manualBuildingPermitMap.get("Work Description"), "SMAB-T383: 'Work Description' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("APN", "Building Permit Information"), parcelToSearch, "SMAB-T383: 'Parcel' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Estimated Project Value", "Building Permit Information"), "$" + manualBuildingPermitMap.get("Estimated Project Value"), "SMAB-T383: 'Estimated Project Value' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Completion Date", "Building Permit Information"), manualBuildingPermitMap.get("Completion Date"), "SMAB-T383: 'Completion Date' Field Validation in 'Building Permit Information' section");

		//Validation for the fields auto populated in the section Situs Information
		//Removed Situs City Code as part of Defect#5849
//		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs City Code","Situs Information"), situsCityCode, "SMAB-T520: 'Situs City Code' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs","Situs Information"), situsName, "SMAB-T520, SMAB-T421: 'Situs' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Type","Situs Information"), situsType, "SMAB-T383: 'Situs Type' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Direction"), situsDirection, "SMAB-T383: 'Situs Direction' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Number","Situs Information"), situsNumber, "SMAB-T383: 'Situs Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Unit Number","Situs Information"), situsUnitNumber, "SMAB-T383: 'Situs Unit Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Street Name","Situs Information"), situsStreetName, "SMAB-T383: 'Situs Street Name' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Permit City Code","Situs Information"), manualBuildingPermitMap.get("Permit City Code"), "SMAB-T383: 'Permit City Code' Field Validation in 'Situs Information' section");

		//Validation for the fields auto populated in the section Processing Status
		//Processing and Calculating Processing Status are calculated based on "Processing Status Information" section from "County Strat Code Infomration"
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process", "SMAB-T520, SMAB-T402: 'Processing Status' Field Validation in 'Processing Status' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process", "SMAB-T402: 'Calculated Processing Status' Field Validation in 'Processing Status' section");

		//Validation for the fields auto populated in the section System Information
		//Strat Code reference Number can be fetched from the County Strat Code Screen of the code choosen while creating the building permit
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Strat Code Reference Number","System Information"), "42", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Record Type","System Information"), "Manual Entry Building Permit", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}
	
	/**
	 Below test case is used to validate
	 1. Error appearing if mandatory fields are not filled while editing the existing building permit record
	 2. Save the record after updating the value in a field
	 **/
	@Test(description = "SMAB-T466,SMAB-T349,SMAB-T419: Mandatory Field Validation while editing manual building permit and editing a record", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class, alwaysRun = true)
	public void BuildingPermit_EditAndValidateRequiredFieldsValidationsForExistingPermit(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Editing the existing Building Permit without giving all the mandatory fields and validating the error messages appearing
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the existing Building Permit without giving all the mandatory fields");
		objBuildingPermitPage.displayRecords("All Manual Building Permits");
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objBuildingPermitPage.getGridDataInHashMap(1);
		String buildingPermitNumber = manualBuildingPermitGridDataMap.get("Building Permit Number").get(0);
		objBuildingPermitPage.globalSearchRecords(buildingPermitNumber);
		objPage.Click(objBuildingPermitPage.editButton);
		Thread.sleep(2000);

		//Step4: Save after entering 'Tree Removal' in Work Description. There should be an error
		String expectedWorkDescriptionError = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nNo Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"";
		String expectedFieldLevelError = "Complete this field.";

		objPage.waitForElementToBeClickable(30,objBuildingPermitPage.workDescriptionTxtBox);
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,"Tree Removal");
		softAssert.assertEquals(objBuildingPermitPage.saveRecordAndGetError(),expectedWorkDescriptionError,"SMAB-T466,SMAB-T349: Warning message validation on the top when 'Work Description' field is having following values 'Tree Removal', 'public works permits', 'temporary signs/banners'");
		objPage.Click(objBuildingPermitPage.pageErrorButton);

		//Step5: Save after clearing the mandatory work description field
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,"");

		//Step6: Validating the error message on edit pop when mandatory fields are not filled
		softAssert.assertEquals(objBuildingPermitPage.saveRecordAndGetError(),"Close error dialog\nWe hit a snag.\nReview the following fields\nWork Description","SMAB-T466: Warning message validation on the top when 'Work Description' field is not entered while editing the building permit record");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Work Description"),expectedFieldLevelError,"SMAB-T466: Warning message validation at the field level 'Work Description' field is not entered while editing the building permit record");
		objBuildingPermitPage.Click(objBuildingPermitPage.pageErrorButton);

		//Step7: Enter the updated estimated project value and builing permit number and save the record
		String updatedWorkDescriptionValue = "New Construction " + DateUtil.getCurrentDate("mmss");
		String updatedBuildingPermitNumber = "LM-" + DateUtil.getCurrentDate("yyyMMdd-HHmmss");
		ReportLogger.INFO("Value to be updated in 'Work Description' field : " + updatedWorkDescriptionValue);
		ReportLogger.INFO("Old 'Building Permit Number' value : " + buildingPermitNumber);
		ReportLogger.INFO("Value to be updated in 'Building Permit Number' field : " + updatedBuildingPermitNumber);

		objPage.enter(objBuildingPermitPage.buildingPermitNumberTxtBox,updatedBuildingPermitNumber);
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,updatedWorkDescriptionValue);
		objBuildingPermitPage.saveRecord();

		//Step8: Validation for record with old building permit number exists or not
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step9: Search the building permit number record edited above
		objBuildingPermitPage.searchRecords(updatedBuildingPermitNumber);

		//Step10: Validating that new value entered in estimated project value filed is saved
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMapAfterEdit = objBuildingPermitPage.getGridDataInHashMap(1);
		softAssert.assertEquals(manualBuildingPermitGridDataMapAfterEdit.get("Work Description").get(0),updatedWorkDescriptionValue,"SMAB-T466,SMAB-T419: Validating the 'Work Description' after editing the record");
		softAssert.assertEquals(manualBuildingPermitGridDataMapAfterEdit.get("Building Permit Number").get(0),updatedBuildingPermitNumber,"SMAB-T466,SMAB-T419: Validating the 'Building Permit Number' after editing the record");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}
	
	/**
	 Below test case is used to validate error appearing if mandatory fields are not filled while manually creating building permit
	 **/
	@Test(description = "SMAB-T418: Mandatory Field Validation while creating manual building permit", groups = {"smoke","regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin", dataProviderClass =  DataProviders.class, alwaysRun = true)
	public void BuildingPermit_ManualCreationWithRequiredFieldsValidations(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open and save building permit manual creation form without entering the data
		objBuildingPermitPage.openNewForm();
		objPage.Click(objPage.getButtonWithText("Save"));

		//Step4: Validate the error message appeared for mandatory fields
		String expectedErrorMessageOnTop = "Close error dialog\nWe hit a snag.\nReview the following fields\nBuilding Permit Number\nAPN\nCounty Strat Code Description\nEstimated Project Value\nIssue Date\nWork Description\nPermit City Code";
		String expectedIndividualFieldMessage = "Complete this field.";
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.pageError),expectedErrorMessageOnTop,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error in manual entry pop up header.");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Building Permit Number"),expectedIndividualFieldMessage,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error for 'Building Permit Number'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("APN"),expectedIndividualFieldMessage,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error for 'Parcel'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("County Strat Code Description"),expectedIndividualFieldMessage,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error for 'County Strat Code Description'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Estimated Project Value"),expectedIndividualFieldMessage,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error for 'Estimated Project Value'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Issue Date"),expectedIndividualFieldMessage,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error for 'Issue Date'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Work Description"),expectedIndividualFieldMessage,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error for 'Work Description'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Permit City Code"),expectedIndividualFieldMessage,"SMAB-T418,SMAB-T348: Validating mandatory fields missing error for 'Permit City Code'");

		//Step5: Closing the Manual building permit creation pop up
		objPage.Click(objPage.getButtonWithText("Cancel"));

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate that building permit can be created when 'Building Permit Number, Permit City Code and APN' is unique
	 **/
	@Test(description = "SMAB-T519: Validate that building permit can be created when 'Building Permit Number, Permit City Code and APN' is unique", groups = {"smoke","regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_ManualCreationForDuplicateCheck(String loginUser) throws Exception {

		//Pre-requisite: Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		System.out.println("Active APN fetched through Salesforce API : " + activeAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step3: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step4: Prepare a test data to create a new building permit
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("APN",activeAPN);

		//Step5: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
        
		//Step6: Create another building permit with the data user above. An error should occur
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);
		objBuildingPermitPage.openNewForm();
		objBuildingPermitPage.enterManualEntryData(manualBuildingPermitMap);
		String actualErrorMessage = objBuildingPermitPage.saveRecordAndGetError();

		//Step7: Validate the error message appeared for mandatory fields
		String expectedWarningMessage = "Close error dialog\nWe hit a snag.\nYou can't save this record because a duplicate record already exists. To save, use different information.\nView Duplicates";

		softAssert.assertEquals(actualErrorMessage,expectedWarningMessage,"SMAB-T519: Warning Message validation for duplicate fields");
        
		//Step8: Validation of the building permit after clicking on View Duplicate Link
		Thread.sleep(1000);
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

		objBuildingPermitPage.cancelRecord();


		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the warning message when building permit is created with retired parcel
	 **/
	@Test(description = "SMAB-T626: Validate that warning message appears when a building permit is created with retired parcel", groups = {"smoke","regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_ManualCreationWithRetiredParcel(String loginUser) throws Exception {

		//Pre-requisite: Fetch Retired APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String retiredAPN = response.get("Name").get(0);
		ReportLogger.INFO("Retired APN fetched through Salesforce API : " + retiredAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Prepare a test data to create a new building permit with retired parcel
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
		manualBuildingPermitMap.put("APN",retiredAPN);

		//Step4: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step5: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Validation for the warning message appearing on the grid
		objBuildingPermitPage.displayRecords("All Manual Building Permits");
		objBuildingPermitPage.searchRecords(buildingPermitNumber);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objBuildingPermitPage.getGridDataInHashMap();
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Warning Message").get(0), "APN is retired.","SMAB-T626: 'Priority Message' validation on the data displayed on the grid");

		//Step6: open the building permit created with retired parcel
		objBuildingPermitPage.openBuildingPermit(buildingPermitNumber);

		//Validate the warning message for retired parcel is appearing on the Details page as well
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.warningMessageWithPriorityFlag), "APN is retired.","SMAB-T626: 'Priority Message' validation on building permit details page screen");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the values in Processing Status and Calculated Processing Status fields when processing status was not selected
	 **/
	@Test(description = "SMAB-T399: Validate that Process and Calculated Process Status fields are auto populated when process status field is not selected", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_ProcessFieldsStatus_ProcessingStatusNotSelectedByUser(String loginUser) throws Exception {

		//Pre-requisite: Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		System.out.println("Active APN fetched through Salesforce API : " + activeAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

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
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T399: Validation of 'Calculated Processing Status' field when Process Status was not selected while creating building permit manually");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T399: Validation of 'Processing Status' field when Process Status was not selected while creating building permit manually");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the values in Processing Status and Calculated Processing Status fields when processing status was selected
	 **/
	@Test(description = "SMAB-T400: Validate that Process and Calculated Process Status fields are auto populated when process status field is selected", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_ProcessFieldsStatus_ProcessingStatusSelectedByUser(String loginUser) throws Exception {

		//Pre-requisite: Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		System.out.println("Active APN fetched through Salesforce API : " + activeAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

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
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Process Status was selected while creating building permit manually");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when Process Status was selected while creating building permit manually");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the values in Processing Status and Calculated Processing Status fields when Estimated Project value or County Strat Code is changed
	 Below is the rule based on Selected County Strat Code 'REPAIR ROOF'
	 Processing Status should be populated as it has been explicitly selected by user
	 Calculated Processing Status should be populated as 'Process' if Estimated project value is "Greater Than or Equal to $25,000" ELSE 'No Process'
	 **/
	@Test(description = "SMAB-T403, SMAB-T404,SMAB-T909,SMAB-T400: Validated the Calculated Process Status field is automatically updated when estimated project value or county strat code is updated", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_CalculatedProcessingStatus_EstimatedProjectValueOrCountyStratCodeUpdated(String loginUser) throws Exception {

		//Pre-requisite: Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		System.out.println("Active APN fetched through Salesforce API : " + activeAPN);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Prepare a test data to create a new building permit with retired parcel
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("APN",activeAPN);
		manualBuildingPermitMap.put("Processing Status","No Process");
		manualBuildingPermitMap.put("Estimated Project Value","3000");

		//Step4: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.successAlert,20);
		objPage.waitForElementToDisappear(objBuildingPermitPage.successAlert,10);
		//Step5: validation of process status after adding the building permit
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Processing Status' field when Estimated Project value is selected 3000 and county strat code as REPAIR ROOF");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Estimated Project value is selected 3000 and county strat code as REPAIR ROOF");

		//step6: Update the estimated project value as less than 25000 and check the status of Calculated processing status field is updated accordingly
		objPage.Click(objBuildingPermitPage.editButton);
		ReportLogger.INFO("Updating the value of Estimated Project Value to 25000");
		objPage.enter(objBuildingPermitPage.estimatedProjectValueTxtBox,"25000");
		objBuildingPermitPage.saveRecord();
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "Process","SMAB-T400,SMAB-T909: Validation of 'Calculated Processing Status' field when Estimated Project value is selected 25000 and county strat code as REPAIR ROOF");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "Process","SMAB-T400,SMAB-T404,SMAB-T909: Validation of 'Processing Status' field when Estimated Project value is selected 25000 and county strat code as REPAIR ROOF");

		//Step7: Update the estimated project value such that value of calculated processing status is switched
		objPage.Click(objBuildingPermitPage.editButton);
		ReportLogger.INFO("Updating the value of Estimated Project Value to 7000");
		objPage.enter(objBuildingPermitPage.estimatedProjectValueTxtBox,"7000");
		objBuildingPermitPage.saveRecord();
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process","SMAB-T400,SMAB-T909: Validation of 'Calculated Processing Status' field when Estimated Project value is selected 7000 and county strat code as REPAIR ROOF");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400,SMAB-T909: Validation of 'Processing Status' field when Process Status when Estimated Project value is selected 7000 and county strat code as REPAIR ROOF");

		//Step8: Update the county strat code such that value of calculated processing status is switched
		//Following is the condition for SOLAR County Strat Code "'No Process' if estimated project value is Less Than or Equal To 5000
		objPage.Click(objBuildingPermitPage.editButton);
		objPage.Click(objBuildingPermitPage.deleteRepairRoof);
		ReportLogger.INFO("Updating the value of County Strat Code to 'SOLAR'");
		objBuildingPermitPage.searchAndSelectOptionFromDropDown(objBuildingPermitPage.countyStratCodeSearchBox, "SOLAR");
		objBuildingPermitPage.saveRecord();
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "Process","SMAB-T400,SMAB-T909: Validation of 'Calculated Processing Status' field when County strat code is changed to SOLAR from REPAIR ROOF");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "Process","SMAB-T400,SMAB-T909: Validation of 'Processing Status' field when County strat code is changed to SOLAR from REPAIR ROOF");

		//Step9: Update the processing status as No Process and Calculated Processing status should be updated with the same value
		objPage.Click(objBuildingPermitPage.editButton);
		objBuildingPermitPage.selectOptionFromDropDown(objBuildingPermitPage.processingStatusDrpDown, "No Process");
		objBuildingPermitPage.saveRecord();
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "Process","SMAB-T400,SMAB-T403: Validation of 'Calculated Processing Status' field when Processing Status is changed to 'No Process' from 'Process'");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process","SMAB-T400,SMAB-T403: Validation of 'Processing Status' field when Processing Status is changed to 'No Process' from 'Process'");

		//Step10: Update the processing status as No Process and Calculated Processing status should be updated with the same value
		objPage.Click(objBuildingPermitPage.editButton);
		objBuildingPermitPage.selectOptionFromDropDown(objBuildingPermitPage.processingStatusDrpDown, "Process");
		objBuildingPermitPage.saveRecord();
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "Process","SMAB-T400: Validation of 'Calculated Processing Status' field when Processing Status is changed to 'Process' from 'No Process'");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Processing Status","Processing Status"), "Process","SMAB-T400: Validation of 'Processing Status' field when Processing Status is changed to 'Process' from 'No Process'");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the error message as per the business rule validation while creating building permit manually
	 **/
	@Test(description = "SMAB-T630,SMAB-T627,SMAB-T628,SMAB-T631: Validate the error message as per the business rule validation while creating building permit manually", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_ErrorMessageValidations(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open the Intake Form to create manual building permit
		objBuildingPermitPage.openNewForm();
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		objBuildingPermitPage.enterManualEntryData(manualBuildingPermitMap);

		//Step4: Estimated Project Value Error validation for less than zero value (SMAB-T630)
		objPage.enter(objBuildingPermitPage.estimatedProjectValueTxtBox,"-20");
		softAssert.assertEquals(objBuildingPermitPage.saveRecordAndGetError(),"Close error dialog\nWe hit a snag.\nReview the errors on this page.\nInvalid Permit Value or Fee","SMAB-T630: Error message validation when 'Estimated Project Value' is less than zero");
		objPage.Click(objBuildingPermitPage.pageErrorButton);

		//Step5: Future Issue Date Error validation (SMAB-T627)
		objBuildingPermitPage.enter(objBuildingPermitPage.issueDateCalender, "11/10/2039");
		objBuildingPermitPage.saveRecordAndGetError();
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Issue Date"),"Issue Date is greater than Current Date","SMAB-T627: 'Issue Date' error validation when data is selected in future");
		objPage.Click(objBuildingPermitPage.pageErrorButton);

		//This step is captured as part of implementation of SMAB-2007 for revised error messages
		objBuildingPermitPage.enter(objBuildingPermitPage.completionDateCalender, "11/10/2039");
		objBuildingPermitPage.saveRecordAndGetError();
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Completion Date"),"Completion Date is greater than Current Date","SMAB-T627: 'Issue Date' error validation when data is selected in future");
		objPage.Click(objBuildingPermitPage.pageErrorButton);

		//Step6: Completion Date Less Than Issue Date Error validation (SMAB-T628)
		objBuildingPermitPage.enter(objBuildingPermitPage.issueDateCalender, "11/10/2019");
		objBuildingPermitPage.enter(objBuildingPermitPage.completionDateCalender, "11/08/2019");
		objBuildingPermitPage.saveRecordAndGetError();
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Completion Date"),"Completion Date is less than Issue Date","SMAB-T628: Completion Date Less Than Issue Date Error message validation");
		objPage.Click(objBuildingPermitPage.pageErrorButton);

		//Step7: Wrong Format Issue Date Error validation (SMAB-T631)
		objBuildingPermitPage.enter(objBuildingPermitPage.issueDateCalender, "15/23/2019");
		objBuildingPermitPage.saveRecordAndGetError();
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Issue Date"),"Your entry does not match the allowed format M/d/yyyy.","SMAB-T631: Issue Date in invalid format Error message validation");
		objPage.Click(objBuildingPermitPage.pageErrorButton);
		//Step8: Wrong Format Completion Date Error validation (SMAB-T631)
		objBuildingPermitPage.enter(objBuildingPermitPage.completionDateCalender, "15/23/2019");
		objBuildingPermitPage.saveRecordAndGetError();
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Completion Date"),"Your entry does not match the allowed format M/d/yyyy.","SMAB-T631: Completion Date in invalid format Error message validation");
		objPage.Click(objBuildingPermitPage.pageErrorButton);

		objBuildingPermitPage.cancelRecord();

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the the correct values in Permit City Code drop down while creating manual building permit
	 **/
	@Test(description = "SMAB-T506: Validate the correct values in Permit City Code drop down while creating manual building permit", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_PermitCityCodeValues(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open the Intake Form to create manual building permit
		objBuildingPermitPage.openNewForm();

		//Step4: Permit City Code values validation for manual building permit
		objPage.Click(objPage.getWebElementWithLabel(objBuildingPermitPage.permitCityCodeDrpDown));
		String expectedPermitCityCodeValues = "--None--\nBR\nCL\nDC\nEG\nEH\nEP\nFC\nHM\nLH\nLM\nMB\nMO\nMP\nPC\nPR\nPV\nSC\nSG";
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.permitCityCodeDrpDownOptions),expectedPermitCityCodeValues,"SMAB-T506: Validation for Permit City Code values population for manual building permit");
		objBuildingPermitPage.cancelRecord();

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate situs information is not populated when APN doesn't have any primary situs linked
	 **/
	@Test(description = "SMAB-T424: Validation for no Situs when primary situs doesn't exist for the parcel", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_NoSitusPopulation(String loginUser) throws Exception {

		//Pre-Requisite: Pull the APN which doenst have primary situs linked
		String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C='' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPNWithoutPrimarySitus = response.get("Name").get(0);
		ReportLogger.INFO("Active APN without primary situs fetched through Salesforce API : " + activeAPNWithoutPrimarySitus);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step4: Permit City Code values validation for manual building permit
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("APN",activeAPNWithoutPrimarySitus);
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);

		//Validation for the situs fields not auto populated as selected APN doesn't have the primary situs linked
		ReportLogger.INFO("Validation for the situs fields not auto populated as selected APN " + activeAPNWithoutPrimarySitus + "doesn't have the primary situs linked");
		//Situs City Code removed as part of Defect#5849
		//softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs City Code","Situs Information"), "", "SMAB-T424: 'Situs City Code' Field Validation in 'Situs Information' section should be blank");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs","Situs Information"), "", "SMAB-T424: 'Situs' Field Validation in 'Situs Information' section should be blank");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Type","Situs Information"), "", "SMAB-T424: 'Situs Type' Field Validation in 'Situs Information' section should be blank");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Direction"), "", "SMAB-T424: 'Situs Direction' Field Validation in 'Situs Information' section should be blank");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Number","Situs Information"), "", "SMAB-T424: 'Situs Number' Field Validation in 'Situs Information' section should be blank");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Unit Number","Situs Information"), "", "SMAB-T424: 'Situs Unit Number' Field Validation in 'Situs Information' section should be blank");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Situs Street Name","Situs Information"), "", "SMAB-T424: 'Situs Street Name' Field Validation in 'Situs Information' section should be blank");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate successful creation of manual building permit with non relevant permit prefixes
	 **/
	@Test(description = "SMAB-T443: Validation for successful creation of manual building permit with non relevant permit prefixes", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_NonRelevantPermitPrefixes(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Add the building permit with Non Relevant Permit Prefixes
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		String buildingPermitNumber = "TR-" + DateUtil.getCurrentDate("yyyMMdd-HHmmss");
		manualBuildingPermitMap.put("Building Permit Number",buildingPermitNumber);
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step4: Validating the building permit is added successfully
		objPage.waitForElementToBeVisible(objBuildingPermitPage.successAlert,20);
		String actualSuccessMessage = objBuildingPermitPage.successAlert.getText();
		softAssert.assertEquals(actualSuccessMessage,"success\nBuilding Permit \"" + buildingPermitNumber + "\" was created.\nClose","SMAB-T443: Validation for Permit City Code values population for manual building permit");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate that building permit record can be created manually by system/data admin for the cities providing E-File
	 **/
	@Test(description = "SMAB-T345,SMAB-T517,SMAB-T327: Validate manual creation of building permits for cities providing e-files", groups = {"regression","buildingPermit"}, dataProvider = "loginSystemAdmin",dataProviderClass = com.apas.DataProviders.DataProviders.class, alwaysRun = true)
	public void BuildingPermit_Manual_CreateForCitiesProvidingEFile(String loginSystemAdmin) throws Exception {

		//Pre-requisiet: Fetching available city strat code based on permit city code
		String queryCityStratCode = "SELECT Name FROM City_Strat_Code__c where city_code__C = 'BL' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryCityStratCode);
		String cityStratCode = response.get("Name").get(0);
		ReportLogger.INFO("City Strat Code pulled through APAS Salesforce API : " + cityStratCode);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginSystemAdmin);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open the Intake Form to create manual building permit
		objBuildingPermitPage.openNewForm("E-File Building Permit");

		//Step4: Permit City Code values validation for manual building permit
		objPage.Click(objPage.getWebElementWithLabel(objBuildingPermitPage.permitCityCodeDrpDown));
		String expectedPermitCityCodeValues = "--None--\nAT\nBG\nBL\nHB\nML\nRC\nSB\nSM\nSS\nUN\nWD";
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.permitCityCodeDrpDownOptions),expectedPermitCityCodeValues,"SMAB-T517: Validation for Permit City Code values population for building permit for cities providing e-files");

		//Validation for mandatory fields
		String actualErrorMessageOnTop = objBuildingPermitPage.saveRecordAndGetError();
		String expectedErrorMessageOnTop = "Close error dialog\nWe hit a snag.\nReview the following fields\nBuilding Permit Number\nEstimated Project Value\nIssue Date\nWork Description\nPermit City Code\nOwner Name";
		String expectedIndividualFieldMessage = "Complete this field.";

		softAssert.assertEquals(actualErrorMessageOnTop,expectedErrorMessageOnTop,"SMAB-T345: Validating mandatory fields missing error in manual entry pop up header.");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Building Permit Number"),expectedIndividualFieldMessage,"SMAB-T345: Validating mandatory fields missing error for 'Building Permit Number'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Estimated Project Value"),expectedIndividualFieldMessage,"SMAB-T345: Validating mandatory fields missing error for 'Estimated Project Value'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Issue Date"),expectedIndividualFieldMessage,"SMAB-T345: Validating mandatory fields missing error for 'Issue Date'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Work Description"),expectedIndividualFieldMessage,"SMAB-T345: Validating mandatory fields missing error for 'Work Description'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Permit City Code"),expectedIndividualFieldMessage,"SMAB-T345: Validating mandatory fields missing error for 'Permit City Code'");
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Owner Name"),expectedIndividualFieldMessage,"SMAB-T345: Validating mandatory fields missing error for 'Owner Name'");
		objBuildingPermitPage.Click(objBuildingPermitPage.pageErrorButton);

		//Step5: Validation for non relevant keywords in description
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("Permit City Code","BL");
		manualBuildingPermitMap.put("City Strat Code",cityStratCode);
		manualBuildingPermitMap.put("Work Description","Public Work Permit");
		objBuildingPermitPage.enterManualEntryData(manualBuildingPermitMap);

		//Step4: Save after entering 'Tree Removal' in Work Description. There should be an error
		String expectedWorkDescriptionError = "Close error dialog\nWe hit a snag.\nReview the errors on this page.\nNo Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"";

		softAssert.assertEquals(objBuildingPermitPage.saveRecordAndGetError(),expectedWorkDescriptionError,"SMAB-T327: Warning message validation on the top when 'Work Description' field is having following values 'Tree Removal', 'public works permits', 'temporary signs/banners'");
		objPage.waitForElementToBeClickable(30,objBuildingPermitPage.workDescriptionTxtBox);
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,"abc");
		String actualSuccessMessage = objBuildingPermitPage.saveRecord();

		//Step5: Validating the building permit is added successfully
		softAssert.assertEquals(actualSuccessMessage,"success\nBuilding Permit \"" + manualBuildingPermitMap.get("Building Permit Number") + "\" was created.\nClose","SMAB-T345: Validation for successful creation of E-File Building Permit record manually");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate that Building Permit records imported through E-File are editable
	 **/
	@Test(description = "SMAB-T912: Validtion for edit functionality on building permits imported through e-file", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class, alwaysRun = true)
	public void BuildingPermit_EditBuildingPermitsImportedThroughEfile(String loginUser) throws Exception {

		//Fetch the APN to be used to create building permit
		String queryRecordType ="SELECT Id FROM RecordType where name = 'E-File Building Permit'";
		HashMap<String, ArrayList<String>> responseRecordType = salesforceAPI.select(queryRecordType);
		String efileBuildingPermitType = responseRecordType.get("Id").get(0);
		String queryBuildingPermitEfile ="SELECT Name,City_APN__C,City_strat_code__C FROM Building_Permit__c where RecordTypeId = '" + efileBuildingPermitType + "' and City_APN__C != '' and calculated_processing_status__C = 'No Process' and City_strat_code__C != '' limit 1";
		HashMap<String, ArrayList<String>> responseBuildingPermit = salesforceAPI.select(queryBuildingPermitEfile);
		String buildingPermitNumber = responseBuildingPermit.get("Name").get(0);

		ReportLogger.INFO("Efile Building Permit fetched through Salesforce API : " + buildingPermitNumber);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open an existing E-file Building Permit record
		objBuildingPermitPage.globalSearchRecords(buildingPermitNumber);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,10);
		objPage.Click(objBuildingPermitPage.editButton);
		objPage.waitForElementToBeClickable(10,objBuildingPermitPage.buildingPermitNumberTxtBox);

		//Step4: Update the work description and building permit number on efile building permit number and save the record
		ReportLogger.INFO("Editing the existing E-File Building Permit record with Building Permit Number : " + buildingPermitNumber);
		String updatedWorkDescriptionValue = "New Construction " + DateUtil.getCurrentDate("mmss");
		String updatedBuildingPermitNumber = buildingPermitNumber + DateUtil.getCurrentDate("HHmmss");
		ReportLogger.INFO("Value to be updated in 'Work Description' field : " + updatedWorkDescriptionValue);
		ReportLogger.INFO("Old 'Building Permit Number' value : " + buildingPermitNumber);
		ReportLogger.INFO("Value to be updated in 'Building Permit Number' field : " + updatedBuildingPermitNumber);

		//Step5: Update the values
		objPage.enter(objBuildingPermitPage.buildingPermitNumberTxtBox,updatedBuildingPermitNumber);
		objPage.enter(objBuildingPermitPage.workDescriptionTxtBox,updatedWorkDescriptionValue);

		String actualSuccessMessage = objBuildingPermitPage.saveRecord();
		softAssert.assertEquals(actualSuccessMessage,"success\nBuilding Permit \"" + updatedBuildingPermitNumber + "\" was saved.\nClose","SMAB-T912: Validation for successful edit of E-File Building Permit record manually");
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,20);

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate that APN is mandatory only for Processing Status= "Process" while updating Building Permit records imported through E-File
	 **/
	@Test(description = "SMAB-T1537: Validation for mandatory APN on building permits imported through e-file when processing status is 'Process'", groups = {"regression","buildingPermit"}, dataProvider = "loginBPPBusinessAdmin", dataProviderClass = com.apas.DataProviders.DataProviders.class, alwaysRun = true)
	public void BuildingPermit_ApnMandatoryForProcess(String loginUser) throws Exception {

		//Fetch the APN to be used to create building permit
		String queryRecordType ="SELECT Id FROM RecordType where name = 'E-File Building Permit'";
		HashMap<String, ArrayList<String>> responseRecordType = salesforceAPI.select(queryRecordType);
		String efileBuildingPermitType = responseRecordType.get("Id").get(0);
		String queryBuildingPermitEfile ="SELECT Name,City_APN__C,City_strat_code__C FROM Building_Permit__c where RecordTypeId = '" + efileBuildingPermitType + "' and City_APN__C = '' and processing_status__C = 'No Process' and City_strat_code__C != '' and Import_Status__c = 'Import Approved' limit 1";
		HashMap<String, ArrayList<String>> responseBuildingPermit = salesforceAPI.select(queryBuildingPermitEfile);
		String buildingPermitNumber = responseBuildingPermit.get("Name").get(0);

		ReportLogger.INFO("Efile Building Permit fetched through Salesforce API : " + buildingPermitNumber);

		//Step1: Login to the APAS application using the user passed through the data provider
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open an existing E-file Building Permit record
		objBuildingPermitPage.globalSearchRecords(buildingPermitNumber);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,10);
		objPage.Click(objBuildingPermitPage.editButton);
		objPage.waitForElementToBeClickable(10,objBuildingPermitPage.buildingPermitNumberTxtBox);

		//Step4: Update the processing status as "Process" and APN should be mandatory
		ReportLogger.INFO("Updating processing status as 'Process' in the existing E-File Building Permit record with Building Permit Number : " + buildingPermitNumber);
		//Step5: Update the values
		objBuildingPermitPage.selectOptionFromDropDown(objBuildingPermitPage.processingStatusDrpDown, "Process");
		objBuildingPermitPage.saveRecordAndGetError();

		String expectedMandatoryAPNError = "APN required for Process";
		softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("APN"),expectedMandatoryAPNError,"SMAB-T1537: Warning message validation at the field level 'Work Description' field is not entered while editing the building permit record");

		objBuildingPermitPage.cancelRecord();

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

}