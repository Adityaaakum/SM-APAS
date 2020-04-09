package com.apas.Tests.BuildingPermit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermitManualCreationTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildingPermitPage;
	SoftAssertion softAssert  = new SoftAssertion();

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}
		
	@AfterMethod
	public void afterMethod() throws IOException{
		objApasGenericFunctions.logout();
		softAssert.assertAll();
	}
	
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business admin and appraisal support in an array
	 **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] {{ users.BUSINESS_ADMIN } };
    }

	/**
	 Below test case is used to validate error appearing if mandatory fields are not filled while manually creating building permit
	 **/
	@Test(description = "SMAB-T418: Mandatory Field Validation while creating manual building permit", groups = {"smoke","regression"}, dataProvider = "loginUsers", alwaysRun = true)
	public void validateMandatoryFieldErrorsBuildingPermitManualCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Open and save building permit manual creation form without entering the data
		objBuildingPermitPage.openManualEntryForm();
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
	}

	/**
	 Below test case is used to validate that building permit can be created when 'Building Permit Number, Permit City Code and APN' is unique
	 **/
	@Test(description = "SMAB-T519: Validate that building permit can be created when 'Building Permit Number, Permit City Code and APN' is unique", groups = {"smoke","regression"}, dataProvider = "loginUsers", alwaysRun = true)
	public void validateDuplicateBuildingPermitManualCreation(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Get any Active Parcel to create the Building Permit record
		objApasGenericFunctions.searchModule(modules.PARCELS);
		objApasGenericFunctions.displayRecords("All Active Parcels");
		String activeParcel = objApasGenericFunctions.getGridDataInHashMap(1,1).get("APN").get(0);

		//Step3: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step4: Prepare a test data to create a new building permit
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		manualBuildingPermitMap.put("Parcel",activeParcel);

		//Step5: Open and save building permit manual creation
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step6: Create another building permit with the data user above. An error should occur
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step7: Validate the error message appeared for mandatory fields
		String expectedWarningMessage = "This record looks like a duplicate.View Duplicates";
		softAssert.assertEquals(objBuildingPermitPage.warningMessage.getText(),expectedWarningMessage,"SMAB-T519: Warning Message validation for duplicate fields");

		//Step8: Validation of the building permit after clicking on View Duplicate Link
		objPage.Click(objBuildingPermitPage.viewDuplicateLink);
		objPage.waitForElementToBeVisible(objBuildingPermitPage.openBuildingPermitLink);
		Thread.sleep(2000);
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
		objPage.Click(objBuildingPermitPage.closeNewBuildingPermitPopUpButton);

	}

	/**
	 Below test case is used to validate the warning message when building permit is created with retired parcel
	 **/
	@Test(description = "SMAB-T626: Validate that warning message appears when a building permit is created with retired parcel", groups = {"smoke","regression"}, dataProvider = "loginUsers", priority = 2, alwaysRun = true)
	public void validateRetiredParcelBuildingPermitManualCreation(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the Parcels module
		objApasGenericFunctions.searchModule(modules.PARCELS);

		//Step3: Search and Open the Parcel
		objApasGenericFunctions.displayRecords("All Retired Parcels");
		String retiredParcel = objApasGenericFunctions.getGridDataInHashMap(1,1).get("APN").get(0);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Prepare a test data to create a new building permit with retired parcel
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestData();
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
		manualBuildingPermitMap.put("Parcel",retiredParcel);

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

	}

}
