package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.ReportsPage;
import com.apas.PageObjects.WorkItemHomePage;

import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class Parcel_Management_Situs_Management extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	String apnPrefix = new String();
	ReportsPage objReportsPage;
	ApasGenericPage objApasGenericPage;
	String situsData;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		objReportsPage = new ReportsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		situsData = testdata.SITUS_DATA;
	}

	@Test(description = "SMAB-T3824,SMAB-T3825,Verify Duplicate Situs", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelSitusManagement" })
	public void Situs_Management_DuplicateSitus(String loginUser) throws Exception {
		
		Map<String, String> dataToCreateSitusRecord = objUtil.generateMapFromJsonFile(situsData,
				"NewSitusCreationData");

		
		String situsNameDeleteQuery = "SELECT Id FROM Situs__c where Name='101 ST DR #102, ATHERTON'";
		objMappingPage.login(loginUser);
		if(!situsNameDeleteQuery.isEmpty()) {
		salesforceAPI.delete("Situs__c", situsNameDeleteQuery);
		}
		

		
		// Step1: Login to the APAS application using the credentials passed through
		

		// Step3: Opening the Situs page and Clicking on New Button
		objMappingPage.searchModule(SITUS);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
		objParcelsPage.createSitus(dataToCreateSitusRecord);

		// Step 5: Creating the duplicate situs
		objMappingPage.searchModule(SITUS);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
		objParcelsPage.createSitus(dataToCreateSitusRecord);

		// Step 6: Verify that Error message is thrown at creation of duplicate situs
		String ExpectedErrorMessage = "duplicate value found";
		softAssert.assertTrue(
				objParcelsPage.getElementText(objApasGenericPage.pageError).contains(ExpectedErrorMessage),
				"SMAB-T3824: Verify the system should not allow duplicate Situses in the system");

		// Step 7: Creating situs with different situs name to verify duplicate
		// validation
		objMappingPage.searchModule(SITUS);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));

		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("Situs Number"), "101");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("Situs Street Name"), "ST");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("Situs Unit Number"), "102");
		objParcelsPage.clearFieldValue("Situs Type");
		objApasGenericPage.selectOptionFromDropDown("Situs Type", "DR");
		objParcelsPage.clearFieldValue("City Name");
		objApasGenericPage.selectOptionFromDropDown("City Name", "ATHERTON");
		objApasGenericPage.enter(objApasGenericPage.getWebElementWithLabel("Situs Name"), "1024");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));

		// Step 8: validation on situs name
		softAssert.assertTrue(
				objParcelsPage.getElementText(objApasGenericPage.pageError).contains(ExpectedErrorMessage),
				"SMAB-T3825: Verify the system should not allow duplicate Situses in the system and validation should be on the Situs Name");

		// Step 9: Logout
		objParcelsPage.logout();
	}

	@Test(description = "SMAB-T3487:Verify Situs Management on Mapping First Screen", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelSitusManagement" })
	public void Situs_Management_MappingFirstScreen(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where (Not Name like '1%') and (Not Name like '8%')AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(
				mappingActionCreationData, "DataToPerformBrandNewParcelMappingActionWithoutAllFields");
		Map<String, String> dataToCreateSitusRecord = objUtil.generateMapFromJsonFile(situsData,
				"NewSitusCreationData");
		
		String situsCount = "SELECT Id FROM Situs__c where Name='101 ST DR #102, ATHERTON'";
		
		// Step1: Login to the APAS application using the credentials passed through
		// data provider (login Mapping User)
				objMappingPage.login(loginUser);
		if (situsCount.isEmpty()) {
			objMappingPage.searchModule(SITUS);
			objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
			objParcelsPage.createSitus(dataToCreateSitusRecord);
		}

		// Step2: Opening the PARCELS page and searching the parcel to perform brand new
		// parcel mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapBrandNewParcelMappingData.get("Action"));

		objMappingPage.scrollToBottomOfPage();

		// Step 8: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionFormWithSitus(hashMapBrandNewParcelMappingData);
		
		// Step 9: Verify that after creating situs via mapping action First screen it
		// does not create duplicate
		String countAfter = "SELECT Id FROM Situs__c where Name='101 ST DR #102, ATHERTON'";
		softAssert.assertEquals(countAfter, 1,
				"SMAB-T3487: Verify that while creating a Situs via a mapping action at first custom page when user mentions already existing Situs,"
						+ " it gets selected and new duplicate Situs is not created");
		

		// Step 10: Logout
		objParcelsPage.logout();

	}

	@Test(description = "SMAB-T3488:Verify Parcel Situs Management on Mapping second screen", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelSitusManagement" })
	public void Situs_Management_MappingSecondScreen(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where (Not Name like '1%') and (Not Name like '8%')AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(
				mappingActionCreationData, "DataToPerformBrandNewParcelMappingActionWithoutAllFields");
		Map<String, String> hashMapBrandNewParcelMappingDataWithSitus = objUtil.generateMapFromJsonFile(
				mappingActionCreationData, "DataToPerformBrandNewParcelMappingActionWithSitusDataForDuplicateSitus");

		Map<String, String> dataToCreateSitusRecord = objUtil.generateMapFromJsonFile(situsData,
				"NewSitusCreationData");
		
		String situsCount = "SELECT Id FROM Situs__c where Name='101 ST DR #102, ATHERTON'";
		objMappingPage.login(loginUser);
		if (situsCount.isEmpty()) {
			objMappingPage.searchModule(SITUS);
			objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
			objParcelsPage.createSitus(dataToCreateSitusRecord);
		}

		

		// Opening the PARCELS page and searching the parcel to perform brand new parcel
		// mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Clicking the details tab for the work item newly created and clicking on
		// Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		// entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.legalDescriptionColumnSecondScreen);
		objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
		objMappingPage.editActionInMappingSecondScreen(hashMapBrandNewParcelMappingDataWithSitus);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);
		objMappingPage.Click(objParcelsPage.getButtonWithText("Update Parcel(s)"));

		// Verify that the duplicate situs record is not created in the system while we
		// add situs via second screen of mapping action
		String countAfter = "SELECT Id FROM Situs__c where Name='101 ST DR #102, ATHERTON'";
		softAssert.assertEquals(countAfter, 1,
				"SMAB-T3488: Verify that while creating a Situs via a mapping action at second custom page, when user mentions already existing Situs,"
						+ " it gets selected and new duplicate Situs is not created");
	
		// Logout
		objParcelsPage.logout();

	}

	@Test(description = "SMAB-T3481,SMAB-T3482:Verify Parcel Situs Object And Multiple situses", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelSitusManagement" })
	public void ParcelSitus_Management_ObjectAndMutipleSituses(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		objMappingPage.login(loginUser);

		// Step 2: Fetch the APN
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		// Step3: Opening the Parcels page and searching the Parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Open parcel Situs Tab
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelSitus);

		// Creating new situs
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.isPrimaryDropdown);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.isPrimaryDropdown, "Yes");

		objParcelsPage.searchAndSelectOptionFromDropDown(objParcelsPage.situsSearch,
				salesforceAPI.select("Select Name from Situs__c where name != null limit 1").get("Name").get(0));
		
		String situsName1 =salesforceAPI.select("Select Name from Situs__c where name != null limit 1").get("Name").get(0);
		objParcelsPage.Click(objParcelsPage.saveButton);
		// Creating another situs
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.isPrimaryDropdown);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.isPrimaryDropdown, "Yes");

		objParcelsPage.searchAndSelectOptionFromDropDown(objParcelsPage.situsSearch,
				salesforceAPI.select("Select Name from Situs__c where name != null limit 1").get("Name").get(0));
		String situsName2 =salesforceAPI.select("Select Name from Situs__c where name != null limit 1").get("Name").get(0);
		objParcelsPage.Click(objParcelsPage.saveButton);

		HashMap<String, ArrayList<String>> parcelSitusDataHashMap = objParcelsPage
				.getParcelTableDataInHashMap("Parcel Situs");
		int count = parcelSitusDataHashMap.size();
		boolean condition = count >= 2;

		// Verify that corresponding to one parcel more than one situs can exists
		softAssert.assertTrue(condition,
				"SMAB-T3481, SMAB-T3482: Verify that system should allow an APN to have multiple Situses associated to it while selecting a Situs");
		softAssert.assertEquals(situsName1,parcelSitusDataHashMap.get("Situs Name").get(0),
				"SMAB-T3481, SMAB-T3482: Verify that system should allow an APN to have multiple Situses with their name associated to it while selecting a Situs");
		softAssert.assertEquals(situsName2,parcelSitusDataHashMap.get("Situs Name").get(1),
				"SMAB-T3481, SMAB-T3482: Verify that system should allow an APN to have multiple Situses with their name associated to it while selecting a Situs");

		
		objParcelsPage.logout();
	}
}
