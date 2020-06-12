package com.apas.Tests.SecurityAndSharing;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Map;

public class BuildingPermit_SecurityAndSharing_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildingPermitPage;
	SoftAssertion softAssert = new SoftAssertion();

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
	}

	/**
	 Below test case is used to validate that BPP Auditor/Appraiser and BPP Principal should not be able to create/edit/delete building permit
	 **/
	@Test(description = "SMAB-T470: Validate that BPP Auditor/Appraiser and BPP Principal should not be able to create/edit/delete building permit", groups = {"smoke","regression","buildingPermit"}, dataProvider = "BPPAuditorAndPrincipal", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void verify_BuildingPermit_CREDPermissionValidation(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.selectListViewButton);

		//Step3: Validating that user should not be able to create new building permit record
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T470: Validation that 'New' button should not be visible to " + loginUser);

		//Step4: Validation for user not able to edit/delete building permit record created through e-file
		objApasGenericFunctions.displayRecords("All Imported E-File Building Permits");
		Map<String, ArrayList<String>> efileBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap(1,1);
		String efileBuildingPermitNumber = efileBuildingPermitGridDataMap.get("Building Permit Number").get(0);
		objBuildingPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(efileBuildingPermitNumber);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.noActionAvailableOption);
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T470: Validation that " + loginUser + " should see the option 'No Action Available' on clicking show more button for efile building permit records");
		objBuildingPermitPage.openBuildingPermit(efileBuildingPermitNumber);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.editButton),"SMAB-T470: Validation that 'Edit' button should  for efile building permit records not be visible to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButton),"SMAB-T470: Validation that 'Delete' button  for efile building permit records should not be visible to " + loginUser);

		//Step5: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.selectListViewButton);

		//Step4: Validation for user not able to edit/delete building permit record created manually
		objApasGenericFunctions.displayRecords("All Manual Building Permits");
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericFunctions.getGridDataInHashMap(1,1);
		String manualBuildingPermitNumber = manualBuildingPermitGridDataMap.get("Building Permit Number").get(0);
		objBuildingPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(manualBuildingPermitNumber);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.noActionAvailableOption);
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T470: Validation that " + loginUser + " should see the option 'No Action Available' on clicking show more button for manual building permit records");
		objBuildingPermitPage.openBuildingPermit(manualBuildingPermitNumber);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.editButton),"SMAB-T470: Validation that 'Edit' button for manual building permit records should not be visible to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButton),"SMAB-T470: Validation that 'Delete' button for manual building permit records should not be visible to " + loginUser);

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

}
