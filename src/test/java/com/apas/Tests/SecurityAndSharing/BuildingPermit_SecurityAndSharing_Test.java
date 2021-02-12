package com.apas.Tests.SecurityAndSharing;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.config.users;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Map;

public class BuildingPermit_SecurityAndSharing_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericPage objApasGenericPage;
	BuildingPermitPage objBuildingPermitPage;
	SoftAssertion softAssert = new SoftAssertion();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
	}

	/*
 Below test case will validate generic user is not able to access below modules
 City Strat Codes, County Strat Codes, Non Relevant Permit Settings
 */
	@Test(description = "SMAB-T439: Validation for user not having the access to certain modules", groups = {"Regression","BuildingPermit","SecurityAndSharing"})
	public void BuildingPermit_GenericUserNotHavingAccess() throws Exception {

		//Step1: Login to the APAS application using the General User
		objApasGenericPage.login(users.OTHER_COUNTY_STAFF);
		objPage.Click(objApasGenericPage.appLauncher);

		//Step2: Validating that generic user is not having the access to City Strat Code Module
		ReportLogger.INFO("Validating for generic user not having the access to City Strat Code Module");
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.CITY_STRAT_CODES);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'City Strat Code' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'City Strat Code' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Step3: Validating that generic user is not having the access to County Strat Code Module
		ReportLogger.INFO("Validating for generic user not having the access to County Strat Code Module");
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.COUNTY_STRAT_CODES);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'County Strat Code' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'County Strat Code' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Step4: Validating that generic user is not having the access to Non Relevant Permit Settings module
		ReportLogger.INFO("Validating for generic user not having the access to Non Relevant Permit Settings Module");
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.NON_RELEVANT_PERMIT_SETTINGS);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'Non Relevant Permit Settings' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T439: Validation that search module didn't return any 'Non Relevant Permit Settings' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Logout at the end of the test
		objApasGenericPage.logout();
	}

	/**
	 Below test case is used to validate that BPP Auditor/Appraiser and BPP Principal should not be able to create/edit/delete building permit
	 **/
	@Test(description = "SMAB-T470: Validate that BPP Auditor/Appraiser and BPP Principal should not be able to create/edit/delete building permit", groups = {"Regression","BuildingPermit","SecurityAndSharing"}, dataProvider = "BPPAuditorAndPrincipal", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_CREDPermissionValidation(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericPage.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericPage.searchModule(modules.BUILDING_PERMITS);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.selectListViewButton);

		//Step3: Validating that user should not be able to create new building permit record
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T470: Validation that 'New' button should not be visible to " + loginUser);

		//Step4: Validation for user not able to edit/delete building permit record created through e-file
		objApasGenericPage.displayRecords("All Imported E-File Building Permits");
		Map<String, ArrayList<String>> efileBuildingPermitGridDataMap = objApasGenericPage.getGridDataInHashMap(1,1);
		String efileBuildingPermitNumber = efileBuildingPermitGridDataMap.get("Building Permit Number").get(0);
		objApasGenericPage.clickShowMoreLinkOnRecentlyViewedGrid(efileBuildingPermitNumber);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.noActionAvailableOption);
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T470: Validation that " + loginUser + " should see the option 'No Action Available' on clicking show more button for efile building permit records");
		objBuildingPermitPage.openBuildingPermit(efileBuildingPermitNumber);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.editButton),"SMAB-T470: Validation that 'Edit' button should  for efile building permit records not be visible to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButton),"SMAB-T470: Validation that 'Delete' button  for efile building permit records should not be visible to " + loginUser);

		//Step5: Opening the building permit module
		objApasGenericPage.searchModule(modules.BUILDING_PERMITS);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.selectListViewButton);

		//Step4: Validation for user not able to edit/delete building permit record created manually
		objApasGenericPage.displayRecords("All Manual Building Permits");
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericPage.getGridDataInHashMap(1,1);
		String manualBuildingPermitNumber = manualBuildingPermitGridDataMap.get("Building Permit Number").get(0);
		objApasGenericPage.clickShowMoreLinkOnRecentlyViewedGrid(manualBuildingPermitNumber);
		objPage.waitForElementToBeClickable(objBuildingPermitPage.noActionAvailableOption);
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T470: Validation that " + loginUser + " should see the option 'No Action Available' on clicking show more button for manual building permit records");
		objBuildingPermitPage.openBuildingPermit(manualBuildingPermitNumber);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.editButton),"SMAB-T470: Validation that 'Edit' button for manual building permit records should not be visible to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButton),"SMAB-T470: Validation that 'Delete' button for manual building permit records should not be visible to " + loginUser);

		//Logout at the end of the test
		objApasGenericPage.logout();
	}

	/**
	 Below test case is used to validate the permissions given to BPP Business Admin
	 **/
	@Test(description = "SMAB-T1820: BPP Business Admin access validations", groups = {"Regression","BuildingPermit","SecurityAndSharing"}, dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_AccessValidations_BPPBusinessAdmin(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericPage.login(loginUser);

		//Step2: Validating that user should be able to create/edit new building permit record and should not be able to delete building permit
		objApasGenericPage.searchModule(modules.BUILDING_PERMITS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Building Permit Screen - Validation that 'New' button should be visible to " + loginUser);
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.newButton),"SMAB-T1820 : Building Permit Screen - Validation that 'New' button should be Enabled to " + loginUser);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : Building Permit Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : Building Permit Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Step3: validation of the user access on Situs screen
		objApasGenericPage.searchModule(modules.SITUS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Situs Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> situsGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(situsGridDataMap.get("Situs Name").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : Situs Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step4: validation of the user access on city strat code screen
		objApasGenericPage.searchModule(modules.CITY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : City Strat Code Screen - Validation that 'New' button should be visible to " + loginUser);
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.newButton),"SMAB-T1820 : City Strat Code Screen - Validation that 'New' button should be enabled to " + loginUser);
		Map<String, ArrayList<String>> cityStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(cityStratCodeGridDataMap.get("City Strat Code").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : City Strat Code Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : City Strat Code Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Step5: validation of the user access on County strat code screen
		objApasGenericPage.searchModule(modules.COUNTY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : County Strat Code Screen - Validation that 'New' button should be visible to " + loginUser);
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.newButton),"SMAB-T1820 : County Strat Code Screen - Validation that 'New' button should be visible to " + loginUser);
		Map<String, ArrayList<String>> countyStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(countyStratCodeGridDataMap.get("Strat Code Description").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : County Strat Code Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : County Strat Code Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Step6: validation of the user access on Non Relevant Permit Setting screen
		objApasGenericPage.searchModule(modules.NON_RELEVANT_PERMIT_SETTINGS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Non Relevant Permit Setting Screen - Validation that 'New' button should be visible to " + loginUser);
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.newButton),"SMAB-T1820 : Non Relevant Permit Setting Screen - Validation that 'New' button should be enabled to " + loginUser);
		Map<String, ArrayList<String>> nonRelevantPermitSettingsGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(nonRelevantPermitSettingsGridDataMap.get("Non Relevant Permit Setting Name").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : Non Relevant Permit Setting Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : Non Relevant Permit Setting Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Logout at the end of the test
		objApasGenericPage.logout();
	}

	/**
	 Below test case is used to validate the permissions given to RP Business Admin
	 **/
	@Test(description = "SMAB-T1820 : RP Business Admin access validations", groups = {"Regression","BuildingPermit","SecurityAndSharing"}, dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_AccessValidations_RPBusinessAdmin(String loginUser) throws Exception {
		BuildingPermit_AccessValidations_BPPBusinessAdmin(loginUser);
	}

	/**
	 Below test case is used to validate the permissions given to BPP Auditor/Appraiser
	 **/
	@Test(description = "SMAB-T1820 : BPP Auditor/Appraiser access validations", groups = {"Regression","BuildingPermit","SecurityAndSharing"}, dataProvider = "loginBppAuditor", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_AccessValidations_BPPAuditor(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericPage.login(loginUser);

		//Step3: Validating that user should be able to create/edit new building permit record and should not be able to delete building permit
		objApasGenericPage.searchModule(modules.BUILDING_PERMITS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Building Permit Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : Building Permit Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step4: validation of the user access on Situs screen
		objApasGenericPage.searchModule(modules.SITUS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Situs Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> situsGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(situsGridDataMap.get("Situs Name").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : Situs Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step4: validation of the user access on city strat code screen
		objApasGenericPage.searchModule(modules.CITY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : City Strat Code Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> cityStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(cityStratCodeGridDataMap.get("City Strat Code").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : City Strat Code Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step4: validation of the user access on County strat code screen
		objApasGenericPage.searchModule(modules.COUNTY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : County Strat Code Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> countyStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(countyStratCodeGridDataMap.get("Strat Code Description").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : County Strat Code Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step4: validation of the user access on Non Relevant Permit Setting screen
		objPage.Click(objApasGenericPage.appLauncher);
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.NON_RELEVANT_PERMIT_SETTINGS);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T1820 : Validation that search module didn't return any 'Non Relevant Permit Settings' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T1820 : Validation that search module didn't return any 'Non Relevant Permit Settings' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Logout at the end of the test
		objApasGenericPage.logout();
	}

	/**
	 Below test case is used to validate the permissions given to RP Appraiser
	 **/
	@Test(description = "SMAB-T1820 : RP Appraiser access validations", groups = {"Regression","BuildingPermit","SecurityAndSharing"}, dataProvider = "RPAppraiser", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_AccessValidations_RPAppraiser(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericPage.login(loginUser);

		//Step2: Validating that user should be able to create/edit new building permit record and should not be able to delete building permit
		objApasGenericPage.searchModule(modules.BUILDING_PERMITS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Building Permit Screen - Validation that 'New' button should be visible to " + loginUser);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : Building Permit Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : Building Permit Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Step3: validation of the user access on Situs screen
		objApasGenericPage.searchModule(modules.SITUS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Situs Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> situsGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(situsGridDataMap.get("Situs Name").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : Situs Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step4: validation of the user access on city strat code screen
		objApasGenericPage.searchModule(modules.CITY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : City Strat Code Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> cityStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(cityStratCodeGridDataMap.get("City Strat Code").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : City Strat Code Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step5: validation of the user access on County strat code screen
		objApasGenericPage.searchModule(modules.COUNTY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : County Strat Code Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> countyStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(countyStratCodeGridDataMap.get("Strat Code Description").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : County Strat Code Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step6: validation of the user access on Non Relevant Permit Setting screen
		objPage.Click(objApasGenericPage.appLauncher);
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.NON_RELEVANT_PERMIT_SETTINGS);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T1820 : Validation that search module didn't return any 'Non Relevant Permit Settings' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T1820 : Validation that search module didn't return any 'Non Relevant Permit Settings' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Logout at the end of the test
		objApasGenericPage.logout();
	}

	/**
	 Below test case is used to validate the permissions given to Appraisal Support
	 **/
	@Test(description = "SMAB-T1820 : Appraisal Support access validations", groups = {"Regression","BuildingPermit","SecurityAndSharing"}, dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void BuildingPermit_AccessValidations_AppraisalSupport(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericPage.login(loginUser);

		//Step2: Validating that user should be able to create/edit new building permit record and should not be able to delete building permit
		objApasGenericPage.searchModule(modules.BUILDING_PERMITS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Building Permit Screen - Validation that 'New' button should be visible to " + loginUser);
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.newButton),"SMAB-T1820 : Building Permit Screen - Validation that 'New' button should be Enabled to " + loginUser);
		Map<String, ArrayList<String>> manualBuildingPermitGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(manualBuildingPermitGridDataMap.get("Building Permit Number").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : Building Permit Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : Building Permit Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Step3: validation of the user access on Situs screen
		objApasGenericPage.searchModule(modules.SITUS);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : Situs Screen - Validation that 'New' button should not be visible to " + loginUser);
		Map<String, ArrayList<String>> situsGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(situsGridDataMap.get("Situs Name").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.noActionAvailableOption),"SMAB-T1820 : Situs Screen - Validation that " + loginUser + " should see the option 'No Action Available' as user is not able to edit/delete Situs");

		//Step4: validation of the user access on city strat code screen
		objApasGenericPage.searchModule(modules.CITY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : City Strat Code Screen - Validation that 'New' button should be visible to " + loginUser);
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.newButton),"SMAB-T1820 : City Strat Code Screen - Validation that 'New' button should be enabled to " + loginUser);
		Map<String, ArrayList<String>> cityStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(cityStratCodeGridDataMap.get("City Strat Code").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : City Strat Code Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : City Strat Code Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Step5: validation of the user access on County strat code screen
		objApasGenericPage.searchModule(modules.COUNTY_STRAT_CODES);
		objApasGenericPage.displayRecords("All");
		softAssert.assertTrue(objPage.verifyElementVisible(objBuildingPermitPage.newButton),"SMAB-T1820 : County Strat Code Screen - Validation that 'New' button should be visible to " + loginUser);
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.newButton),"SMAB-T1820 : County Strat Code Screen - Validation that 'New' button should be visible to " + loginUser);
		Map<String, ArrayList<String>> countyStratCodeGridDataMap = objApasGenericPage.getGridDataInHashMap();
		objApasGenericPage.clickShowMoreLink(countyStratCodeGridDataMap.get("Strat Code Description").get(0));
		softAssert.assertTrue(objPage.verifyElementEnabled(objBuildingPermitPage.editButtonMenuOption),"SMAB-T1820 : County Strat Code Screen - Validation that 'Edit' button should be enabled to " + loginUser);
		softAssert.assertTrue(!objPage.verifyElementVisible(objBuildingPermitPage.deleteButtonMenuOption),"SMAB-T1820 : County Strat Code Screen - Validation that 'Delete' button should not be visible to " + loginUser);

		//Step6: validation of the user access on Non Relevant Permit Setting screen
		objPage.Click(objApasGenericPage.appLauncher);
		objPage.enter(objApasGenericPage.appLauncherSearchBox, modules.NON_RELEVANT_PERMIT_SETTINGS);
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.appsListBox),"No results","SMAB-T1820 : Validation that search module didn't return any 'Non Relevant Permit Settings' apps for logged in generic user");
		softAssert.assertEquals(objPage.getElementText(objApasGenericPage.itemsListBox),"No results","SMAB-T1820 : Validation that search module didn't return any 'Non Relevant Permit Settings' items for logged in generic user");
		objPage.Click(objApasGenericPage.searchClearButton);

		//Logout at the end of the test
		objApasGenericPage.logout();
	}

}
