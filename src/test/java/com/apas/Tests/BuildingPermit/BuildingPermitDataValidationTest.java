package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.*;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
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

public class BuildingPermitDataValidationTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildingPermitPage;
	ParcelsPage objParcelsPage;
	SoftAssertion softAssert = new SoftAssertion();

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objParcelsPage = new ParcelsPage(driver);
	}
		
	@AfterMethod
	public void afterMethod() throws IOException{
		objApasGenericFunctions.logout();
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
	 Below test case is used to validate the manual creation of building permit
	 **/
	@Test(description = "SMAB-T383,SMAB-T520,SMAB-T402,SMAB-T421: Creating manual entry for building permit", dataProvider = "loginUsers", groups = {"smoke","regression"}, priority = 2, enabled = true)
	public void validateManuallyCreatedBuildingPermitData(String loginUser) throws Exception {

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
		manualBuildingPermitMap.put("Parcel",parcelToSearch);

		//Step7: Adding a new Building Permit with the APN passed in the above steps
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step8: Opening the Building Permit with the Building Permit Number Passed above and validating the field values
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objApasGenericFunctions.displayRecords("All Manual Building Permits");
		objApasGenericFunctions.searchRecords(buildingPermitNumber);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap();
		objBuildingPermitPage.openBuildingPermit(buildingPermitNumber);

		//Grid Data validation for the building permit created manually
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0), buildingPermitNumber,"'Building Permit Number' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Permit City Code").get(0), manualBuildingPermitMap.get("Permit City Code"),"'Permit City Code' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("County Strat Code Description").get(0), manualBuildingPermitMap.get("County Strat Code Description"),"'County Strat Code Description' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Estimated Project Value").get(0), "$" + manualBuildingPermitMap.get("Estimated Project Value"),"'Estimated Project Value' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Issue Date").get(0), manualBuildingPermitMap.get("Issue Date"),"'Issue Date' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Calculated Processing Status").get(0), "No Process","'Calculated Processing Status' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Processing Status").get(0), "No Process","'Processing Status' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Warning Message").get(0), "","'Warning Message' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Work Description").get(0), manualBuildingPermitMap.get("Work Description"),"'Work Description' validation on the data displayed on the grid");
		softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Parcel").get(0), parcelToSearch,"'Parcel' validation on the data displayed on the grid");

		//Validation for the fields in the section Building Permit Information
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Building Permit Number", "Building Permit Information"), buildingPermitNumber, "SMAB-T383: 'Building Permit Number' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("County Strat Code Description", "Building Permit Information"), manualBuildingPermitMap.get("County Strat Code Description"), "SMAB-T383: 'County Strat Code Description' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Issue Date", "Building Permit Information"), manualBuildingPermitMap.get("Issue Date"), "SMAB-T383: 'Issue Date' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Description", "Building Permit Information"), manualBuildingPermitMap.get("Work Description"), "SMAB-T383: 'Work Description' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Parcel", "Building Permit Information"), parcelToSearch, "SMAB-T383: 'Parcel' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Estimated Project Value", "Building Permit Information"), "$" + manualBuildingPermitMap.get("Estimated Project Value"), "SMAB-T383: 'Estimated Project Value' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Completion Date", "Building Permit Information"), manualBuildingPermitMap.get("Completion Date"), "SMAB-T383: 'Completion Date' Field Validation in 'Building Permit Information' section");

		//Validation for the fields auto populated in the section Situs Information
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs City Code","Situs Information"), situsCityCode, "SMAB-T520: 'Situs City Code' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs","Situs Information"), situsName, "SMAB-T520: SMAB-T421: 'Situs' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Type","Situs Information"), situsType, "SMAB-T383: 'Situs Type' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Direction"), situsDirection, "SMAB-T383: 'Situs Direction' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Number","Situs Information"), situsNumber, "SMAB-T383: 'Situs Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Unit Number","Situs Information"), situsUnitNumber, "SMAB-T383: 'Situs Unit Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Street Name","Situs Information"), situsStreetName, "SMAB-T383: 'Situs Street Name' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit City Code","Situs Information"), manualBuildingPermitMap.get("Permit City Code"), "SMAB-T383: 'Permit City Code' Field Validation in 'Situs Information' section");

		//Validation for the fields auto populated in the section Processing Status
		//Processing and Calculating Processing Status are calculated based on "Processing Status Information" section from "County Strat Code Infomration"
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process", "SMAB-T520: SMAB-T402: 'Processing Status' Field Validation in 'Processing Status' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process", "SMAB-T402: 'Calculated Processing Status' Field Validation in 'Processing Status' section");

		//Validation for the fields auto populated in the section System Information
		//Strat Code reference Number can be fetched from the County Strat Code Screen of the code choosen while creating the building permit
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Strat Code Reference Number","System Information"), "42", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Record Type","System Information"), "Manual Entry Building Permit", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");

		softAssert.assertAll();
	}

    /**
     Below test case is used to validate the building permit data imported through Efile Intake
     **/
    @Test(description = "SMAB-T383", dataProvider = "loginUsers", groups = {"smoke","regression"}, priority = 1,enabled = false)
    public void validateImportedBuildingPermitData(String loginUser) throws Exception {
        //**********This test case needs to be refactored once efile import functionality is integrated
        //Below Data needs to be fetched from the imported file once efile import is integrated as there are few challenged in integrating efile import
        String parcelToSearch = "002-162-090";
        String buildingPermitNumber = "108562";
        Map<String, String> manualBuildingPermitMap = new HashMap<String, String>();
        manualBuildingPermitMap.put("Permit City Code","LM");
        manualBuildingPermitMap.put("Parcel",parcelToSearch);
        manualBuildingPermitMap.put("Building Permit Number",buildingPermitNumber);
        manualBuildingPermitMap.put("Processing Status","--None--");
        manualBuildingPermitMap.put("Issue Date","11/10/2019");
        manualBuildingPermitMap.put("Completion Date","11/10/2019");
        manualBuildingPermitMap.put("County Strat Code Description","REPAIR ROOF");
        manualBuildingPermitMap.put("Work Description","New Construction");

        //Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
        objApasGenericFunctions.login(loginUser);

        //Step2: Opening the Parcels module
        objApasGenericFunctions.searchModule(modules.PARCELS);

        //Step3: Search and Open the Parcel
        objApasGenericFunctions.displayRecords("All Active Parcels");
        objApasGenericFunctions.globalSearchRecords(parcelToSearch);

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

        //Step6: Opening the Building Permit with the Building Permit Number imported through Efile import
        objApasGenericFunctions.displayRecords("All Imported E-File Building Permits");
        objApasGenericFunctions.globalSearchRecords(buildingPermitNumber);
        Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap();

        //Grid Data validation for the building permit created manually
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0), buildingPermitNumber,"'Building Permit Number' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Permit City Code").get(0), manualBuildingPermitMap.get("Permit City Code"),"'Permit City Code' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("County Strat Code Description").get(0), manualBuildingPermitMap.get("County Strat Code Description"),"'County Strat Code Description' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Estimated Project Value").get(0), "$" + manualBuildingPermitMap.get("Estimated Project Value"),"'Estimated Project Value' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Issue Date").get(0), manualBuildingPermitMap.get("Issue Date"),"'Issue Date' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Calculated Processing Status").get(0), "No Process","'Calculated Processing Status' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Processing Status").get(0), "No Process","'Processing Status' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Warning Message").get(0), "","'Warning Message' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Work Description").get(0), manualBuildingPermitMap.get("Work Description"),"'Work Description' validation on the data displayed on the grid");
        softAssert.assertEquals(manualBuildingPermitGridDataMap.get("Parcel").get(0), parcelToSearch,"'Parcel' validation on the data displayed on the grid");

        //Validation for the fields in the section Building Permit Information
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Building Permit Number", "Building Permit Information"), buildingPermitNumber, "SMAB-T383: 'Building Permit Number' Field Validation in 'Building Permit Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("County Strat Code Description", "Building Permit Information"), manualBuildingPermitMap.get("County Strat Code Description"), "SMAB-T383: 'County Strat Code Description' Field Validation in 'Building Permit Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Issue Date", "Building Permit Information"), manualBuildingPermitMap.get("Issue Date"), "SMAB-T383: 'Issue Date' Field Validation in 'Building Permit Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Description", "Building Permit Information"), manualBuildingPermitMap.get("Work Description"), "SMAB-T383: 'Work Description' Field Validation in 'Building Permit Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Parcel", "Building Permit Information"), parcelToSearch, "SMAB-T383: 'Parcel' Field Validation in 'Building Permit Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Estimated Project Value", "Building Permit Information"), "$" + manualBuildingPermitMap.get("Estimated Project Value"), "SMAB-T383: 'Estimated Project Value' Field Validation in 'Building Permit Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Completion Date", "Building Permit Information"), manualBuildingPermitMap.get("Completion Date"), "SMAB-T383: 'Completion Date' Field Validation in 'Building Permit Information' section");

        //Validation for the fields auto populated in the section Situs Information
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs City Code","Situs Information"), situsCityCode, "SMAB-T520: 'Situs City Code' Field Validation in 'Situs Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs","Situs Information"), situsName, "SMAB-T520: 'Situs' Field Validation in 'Situs Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Type","Situs Information"), situsType, "SMAB-T383: 'Situs Type' Field Validation in 'Situs Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Direction"), situsDirection, "SMAB-T383: 'Situs Direction' Field Validation in 'Situs Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Number","Situs Information"), situsNumber, "SMAB-T383: 'Situs Number' Field Validation in 'Situs Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Unit Number","Situs Information"), situsUnitNumber, "SMAB-T383: 'Situs Unit Number' Field Validation in 'Situs Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Street Name","Situs Information"), situsStreetName, "SMAB-T383: 'Situs Street Name' Field Validation in 'Situs Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit City Code","Situs Information"), manualBuildingPermitMap.get("Permit City Code"), "SMAB-T383: 'Permit City Code' Field Validation in 'Situs Information' section");

        //Validation for the fields auto populated in the section Processing Status
        //Processing and Calculating Processing Status are calculated based on "Processing Status Information" section from "County Strat Code Infomration"
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "No Process", "SMAB-T520: SMAB-T402: 'Processing Status' Field Validation in 'Processing Status' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "No Process", "SMAB-T402: 'Calculated Processing Status' Field Validation in 'Processing Status' section");

        //Validation for the fields auto populated in the section System Information
        //Strat Code reference Number can be fetched from the County Strat Code Screen of the code choosen while creating the building permit
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Strat Code Reference Number","System Information"), "42", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Record Type","System Information"), "Manual Entry Building Permit", "SMAB-T383: 'Record Type' Field Validation in 'System Information' section");

        softAssert.assertAll();
    }
}
