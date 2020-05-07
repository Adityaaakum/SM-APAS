package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
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
import java.util.Map;

public class BuildingPermitEditTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildingPermitPage;
	Util objUtil  = new Util();
	SoftAssertion softAssert  = new SoftAssertion();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}
		
	@AfterMethod(alwaysRun=true)
	public void afterMethod() throws IOException, InterruptedException{
		System.out.print("This is Edit After Method");
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
	 Below test case is used to validate
	 1. Error appearing if mandatory fields are not filled while editing the existing building permit record
	 2. Save the record after updating the value in a field
	 **/
	@Test(description = "SMAB-T466: Mandatory Field Validation while editing manual building permit and editing a record", groups = {"smoke","regression","BuildingPermit"}, dataProvider = "loginUsers", alwaysRun = true)
	public void editBuildingPermitAndMandatoryFiledErrorValidation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Editing the existing Building Permit without giving all the mandatory fields and validating the error messages appearing
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the existing Building Permit without giving all the mandatory fields");
		objApasGenericFunctions.displayRecords("All Manual Building Permits");
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap(1,1);
		String buildingPermitNumber = manualBuildingPermitGridDataMap.get("Building Permit Number").get(0);
		objApasGenericFunctions.searchRecords(buildingPermitNumber);
		objBuildingPermitPage.openBuildingPermit(buildingPermitNumber);
		objPage.Click(objBuildingPermitPage.editButton);
		Thread.sleep(2000);

		//Step4: Save after entering 'Tree Removal' in Work Description. There should be an error
		String expectedWorkDescriptionError = "This is a permit type that will not be further processed. Description should not have the following ('Tree Removal', 'public works permits', 'temporary signs/banners')";
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
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMapAfterEdit = objApasGenericFunctions.getGridDataInHashMap(1,1);
		softAssert.assertEquals(manualBuildingPermitGridDataMapAfterEdit.get("Work Description").get(0),updatedWorkDescriptionValue,"SMAB-T466: Validating the 'Work Description' after editing the record");
		softAssert.assertEquals(manualBuildingPermitGridDataMapAfterEdit.get("Building Permit Number").get(0),updatedBuildingPermitNumber,"SMAB-T466: Validating the 'Building Permit Number' after editing the record");
	}
}

