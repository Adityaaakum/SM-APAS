package com.apas.Tests.ParcelManagement;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class Parcel_Management_MappingAction_CommonTests extends TestBase implements testdata, modules, users{
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();
	ApasGenericPage objApasGenericPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);

	}
	/**
	 * This method is to Verify that User is not able to perform any mapping action for a Parcel which has open CIO work items. 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3527:Verify the UI validations that There are open CIO work items against the parent parcel. The mapping action is not allowed to be performed until the CIO activities have been completed.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyMappingActionUIValidations(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Id IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Status__c='Active' limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String apn1=responseAPNDetails.get("Name").get(1);
		String concatenateAPNWithSameOwnership = apn+","+apn1;
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = testdata.PARCEL_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformMappingAction");

		// Step1: Login to the APAS application using the credentials passed through dataprovider
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform mapping action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
       //Step 5: Selecting Action & Taxes Paid fields value as 'yes' and validate error message
	    for( String action : hashMapSplitActionMappingData.values()) {
	        objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
	        if(action.equals("Many To Many")||action.equals("Perform Parcel Combine")) {
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
			}else {
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn);
			}
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		    objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		    objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,action);
		    if(objMappingPage.verifyElementVisible(objMappingPage.taxesPaidDropDownLabel)) {
		    objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");}
		    softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"There are open CIO work items against the parent parcel. The mapping action is not allowed to be performed until the CIO activities have been completed.",
						"SMAB-T3527:Validating Error message on mapping custom screen.");
	    }
	    objMappingPage.logout();
	}
	
	/**
	 * This method is to Verify that The Net Land Loss cannot be greater than the total size of the parent parcel(s). on mapping screen.
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3529:Verify the UI validations for all mapping action that 'The Net Land Loss cannot be greater than the total size of the parent parcel(s).'", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ValidationForNetLandLossValueOnAllMappingAction(String loginUser) throws Exception {
		String queryActiveAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryActiveAPNValue);

		String apn=responseAPNDetails.get("Name").get(0);
		String apn1=responseAPNDetails.get("Name").get(1);
		String concatenateAPNWithSameOwnership = apn+","+apn1;
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "5");
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), "Lot_Size_SQFT__c", "5");
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = testdata.PARCEL_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformValidationForNetLandLossOnMappingActionScreen");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform split mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action & Taxes Paid fields value as 'Yes' and validate error message
	    for( String action : hashMapSplitActionMappingData.values()) {
	        objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
	        if(action.equals("Many To Many")||action.equals("Perform Parcel Combine")) {
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
			}else {
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn);
			}
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,action);
		if(objMappingPage.verifyElementVisible(objMappingPage.taxesPaidDropDownLabel)) {
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");}
		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "10");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"The Net Land Loss cannot be greater than the total size of the parent parcel(s).",
						"SMAB-T3529:Validating Error message on mapping custom screen.");		
		}	    
	}
	
	/**
	 * This method is to Verify that In order to proceed with a the mapping action(many to many,combine) selected, the parent APN(s) must have the same ownership and ownership allocation.
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3529:Verify that In order to proceed with a the mapping action selected, the parent APN(s) must have the same ownership and ownership allocation.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ValidationOfOwnershipOnMappingAction(String loginUser) throws Exception {
		String queryAPN = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String queryAPNwithownership = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c)  and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
	    responseAPNDetails = salesforceAPI.select(queryAPNwithownership);		
		String apn1=responseAPNDetails.get("Name").get(0);
		
		String concatenateAPNWithSameOwnership = apn+","+apn1;

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = testdata.PARCEL_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformValidationForOwnershipOnMappingActionScreen");

		// Step1: Login to the APAS application using the credentials passed through dataprovider 
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform  mapping action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action & Taxes Paid fields value as 'Yes' and validate error message
	    for( String action : hashMapSplitActionMappingData.values()) {	    	
	    	objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		    objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		    objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,action);
		    objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		    if(action.equals("Many To Many"))softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"- In order to proceed with a parcel \"Many To Many\" action, the parent APN(s) must have the same ownership and ownership allocation.",
							"SMAB-T3528:Validating Error message on mapping custom screen.");
		    if(action.equals("Perform Parcel Combine"))softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
						"- In order to proceed with a parcel combine action, the parent APN(s) must have the same ownership and ownership allocation.",
								"SMAB-T3528:Validating Error message on mapping custom screen.");
	    }	    
	}
	
	/*
	 * Parcel Management Retrofit - Verify that when user performs multiple mapping
	 * actions, the work item gets rejected then all the WI's will be deleted.
	 */

	@Test(description = "SMAB-T4064,SMAB-T4065,SMAB-T4066,SMAB-T4123,SMAB-T4124:Parcel Management Retrofit - Verify that when user performs multiple mapping actions, the work item gets rejected then all the WI's will be deleted.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" })
	public void ParcelManagement_VerifyMultipleChildWorkItemsForMappingActions(String loginUser) throws Exception {

		JSONObject jsonParcelObject = objMappingPage.getJsonObject();

		// Fetching the Active Parcel
		String query = "SELECT Name, Id FROM Parcel__c WHERE Status__c = 'Active' AND (Not Name like '134%') AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO')";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);

		String parcelToSearch = response.get("Name").get(0);
		String parcelToSearch1 = response.get("Name").get(1);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
				"SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		objMappingPage.deleteCharacteristicInstanceFromParcel(parcelToSearch);
		
		String legalDescriptionValue = "Legal PM 85/25-260";
		String districtValue = "District01";
		String parcelSize = "1000";

		jsonParcelObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Status__c", "Active");
		jsonParcelObject.put("Short_Legal_Description__c", legalDescriptionValue);
		jsonParcelObject.put("District__c", districtValue);
		jsonParcelObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c", responseTRADetails.get("Id").get(0));
		jsonParcelObject.put("Lot_Size_SQFT__c", parcelSize);

		salesforceAPI.update("Parcel__c", response.get("Id").get(0), jsonParcelObject);
		salesforceAPI.update("Parcel__c", response.get("Id").get(1), jsonParcelObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionForWorkItemRollBack");

		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Opening the Parcels module and searching for the parcel
		objParcelsPage.searchModule(modules.PARCELS);
		objParcelsPage.globalSearchRecords(parcelToSearch);

		// Step 3: Creating Manual work item for the Active Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		String findWorkItemNumber = "SELECT Name FROM Work_Item__c WHERE Type__c = 'Mapping' AND Sub_Type__c = 'Other Mapping Work' order by createdDate desc limit 1";
		HashMap<String, ArrayList<String>> workItemResponse = salesforceAPI.select(findWorkItemNumber);

		String firstWorkItemNumber = workItemResponse.get("Name").get(0);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		String workItemLink1 = driver.getCurrentUrl();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'Yes'
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, "Yes");
		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);


		HashMap<String, ArrayList<String>> gridDataHashMap1 = objMappingPage.getGridDataInHashMap();

		// updating child parcel size in second screen on mapping action
		for (int i = 1; i <= gridDataHashMap1.get("APN").size(); i++) {
			objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "500", i);
		}

		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		Thread.sleep(2000);
		objMappingPage.waitForElementToBeVisible(objMappingPage.createNewParcelSuccessMessage);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.performAdditionalMappingButton));
		Thread.sleep(2000);

		// Step 8: Going back to the work item and checking the new created WI has been attached to this work Item
		
		driver.navigate().to(workItemLink1);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentWorkItemLink);

		String findSecondWorkItemNumber = "SELECT Name FROM Work_Item__c WHERE Type__c = 'Mapping' AND Sub_Type__c = 'Other Mapping Work' order by createdDate desc limit 1";
		HashMap<String, ArrayList<String>> workItemResponse1 = salesforceAPI.select(findSecondWorkItemNumber);

		String secondWorkItemNumber = workItemResponse1.get("Name").get(0);

		objWorkItemHomePage.Click(objWorkItemHomePage.parentWorkItemLink);

		String workItemLink2 = driver.getCurrentUrl();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(2000);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.reviewLink);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow1 = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow1);

		objMappingPage.deleteCharacteristicInstanceFromParcel(parcelToSearch1);
		// Step 9: Now on the Mapping action page from second WI and here user will perform One to One Mapping action
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.getButtonWithText(objMappingPage.EditButton));
		objApasGenericPage.Click(objMappingPage.getButtonWithText(objMappingPage.EditButton));
		objApasGenericPage.enter("Parent APN(s)", parcelToSearch1);
		objApasGenericPage.Click(objMappingPage.getButtonWithText(objMappingPage.SaveButton));
		objApasGenericPage.selectOptionFromDropDown("Action", "One To One");
		objApasGenericPage.selectOptionFromDropDown("Are Taxes Fully Paid?", "Yes");
		objApasGenericPage.Click(objApasGenericPage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));

		// updating child parcel size in second screen on mapping action
		objMappingPage.waitForElementToBeVisible(5, objMappingPage.parcelSizeColumnSecondScreen);
		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "1000", 1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.createNewParcelSuccessMessage);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.performAdditionalMappingButton));
		Thread.sleep(2000);

		// Step 10: User came back to second WI and checking here that it created another WI
		driver.navigate().to(workItemLink2);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentWorkItemLink);

		String findThirdWorkItemNumber = "SELECT Name FROM Work_Item__c WHERE Type__c = 'Mapping' AND Sub_Type__c = 'Other Mapping Work' order by createdDate desc limit 1";
		HashMap<String, ArrayList<String>> workItemResponse2 = salesforceAPI.select(findThirdWorkItemNumber);

		String thirdWorkItemNumber = workItemResponse2.get("Name").get(0);

		objWorkItemHomePage.Click(objWorkItemHomePage.parentWorkItemLink);

		String workItemLink3 = driver.getCurrentUrl();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.reviewLink);

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow2 = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow2);

		// Fetching parcels that are Active with same Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '%990') and (Not Name like '134%') and Status__c = 'Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1 = responseAPNDetails.get("Name").get(0);
		String apn2 = responseAPNDetails.get("Name").get(1);

		objMappingPage.deleteCharacteristicInstanceFromParcel(apn1);

		// Fetching parcels that are Active with different map book and map page
		String mapBookForAPN1 = apn1.split("-")[0];
		String mapPageForAPN1 = apn1.split("-")[1];
		String mapBookForAPN2 = apn2.split("-")[0];
		String mapPageForAPN2 = apn2.split("-")[1];
		queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '%990') and (Not Name like '134%') and (Not Name like '"
				+ mapBookForAPN1 + "%') and (Not Name like '" + mapBookForAPN1 + "-" + mapPageForAPN1
				+ "%') and (Not Name like '" + mapBookForAPN2 + "%') and (Not Name like '" + mapBookForAPN2 + "-"
				+ mapPageForAPN2
				+ "%') and Status__c = 'Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 1";
		HashMap<String, ArrayList<String>> responseAPN3Details = salesforceAPI.select(queryAPNValue);
		String apn3 = responseAPN3Details.get("Name").get(0);

		// Deleting ownerships from parcels
		objMappingPage.deleteOwnershipFromParcel(responseAPNDetails.get("Id").get(0));
		objMappingPage.deleteOwnershipFromParcel(responseAPNDetails.get("Id").get(1));
		objMappingPage.deleteOwnershipFromParcel(responseAPN3Details.get("Id").get(0));

		// Add the parcels in a Hash Map for later use in the combine action. Those parcels will be combined.

		String concatenateAPNWithDifferentMapBookMapPage = apn2 + "," + apn3;
		String legalDescriptionValue2 = "Legal PM 85/25-260";
		String districtValue2 = "District01";
		String parcelSize1 = "200";
		
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn2);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn3);

		
		// Creating Json Object

		jsonParcelObject.put("Status__c", "Active");
		jsonParcelObject.put("Short_Legal_Description__c", legalDescriptionValue2);
		jsonParcelObject.put("District__c", districtValue2);
		jsonParcelObject.put("Lot_Size_SQFT__c", parcelSize1);

		// updating PUC details
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonParcelObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), jsonParcelObject);
		salesforceAPI.update("Parcel__c", responseAPN3Details.get("Id").get(0), jsonParcelObject);

		String mappingActionCreationData1 = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil
				.generateMapFromJsonFile(mappingActionCreationData1, "DataToPerformCombineMappingActionWithSitusData");

		// Step 11: Selecting Action as 'perform parcel combine'
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, concatenateAPNWithDifferentMapBookMapPage);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, "Yes");

		// Step 12: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapCombineActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap2 = objMappingPage.getGridDataInHashMap();
		gridDataHashMap2 = objMappingPage.getGridDataInHashMap();

		String apn = gridDataHashMap2.get("APN").get(0);
		String situs = gridDataHashMap2.get("Situs").get(0);

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.createNewParcelSuccessMessage);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.performAdditionalMappingButton));
				Thread.sleep(2000);
		driver.navigate().to(workItemLink3);

		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentWorkItemLink);

		String findFourthWorkItemNumber = "SELECT Name FROM Work_Item__c WHERE Type__c = 'Mapping' AND Sub_Type__c = 'Other Mapping Work' order by createdDate desc limit 1";
		HashMap<String, ArrayList<String>> workItemResponse3 = salesforceAPI.select(findFourthWorkItemNumber);

		String fourthWorkItemNumber = workItemResponse3.get("Name").get(0);

		objWorkItemHomePage.Click(objWorkItemHomePage.parentWorkItemLink);

		// Step 12: User is on the 4th WI and will submit it for the approval
		String workItemLink4 = driver.getCurrentUrl();
		objMappingPage.globalSearchRecords(fourthWorkItemNumber);
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);

		ReportLogger.INFO("Work Item Numbers : " + firstWorkItemNumber + ", " + secondWorkItemNumber + ", "
				+ thirdWorkItemNumber + ", " + fourthWorkItemNumber);
		objWorkItemHomePage.logout();

		Thread.sleep(5000);

		// Step 11: Login from Mapping Supervisor to approve the WI
		ReportLogger.INFO(
				"Now logging in as Mapping Supervisor to reject the work item and validate that child WIs and releated data has been deleted");
		objWorkItemHomePage.login(MAPPING_SUPERVISOR);

		objMappingPage.globalSearchRecords(fourthWorkItemNumber);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.editRejected);
		objWorkItemHomePage.Click(objWorkItemHomePage.editRejected);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.SaveButton);
		objApasGenericPage.selectOptionFromDropDown("Rejected?", "Yes");
		objApasGenericPage.selectOptionFromDropDown("Rejection Reason", "Other");
		objApasGenericPage.enter("Rejection Comments", "Please review it again and work on it.");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.SaveButton));

		Thread.sleep(2000);
		driver.navigate().to(workItemLink4);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.pageNotExist);
		String actualPageText = objWorkItemHomePage.pageNotExist.getText();
		ReportLogger.INFO("Text of the WI page after deleting it :- " + actualPageText);
		String expectedPageText = "Looks like there's a problem.";
		softAssert.assertEquals(actualPageText, expectedPageText,
				"SMAB-T4064,SMAB-T4065,SMAB-T4066,SMAB-T4123,SMAB-T4124 : WI Page should not be existed after rejection");

		String queryWorkItemId = "SELECT Id FROM Work_Item__c where Name = '" + fourthWorkItemNumber + "'";
		HashMap<String, ArrayList<String>> workItemNameNull = salesforceAPI.select(queryWorkItemId);
		boolean workItemNameText = workItemNameNull.get("Id") == null;

		String queryApnId = "SELECT Id FROM Work_Item__c where Name = '" + apn + "'";
		HashMap<String, ArrayList<String>> apnNameNull = salesforceAPI.select(queryApnId);
		boolean apnNameNullValue = apnNameNull.get("Id") == null;

		String querySitusId = "SELECT Id FROM Work_Item__c where Name = '" + situs + "'";
		HashMap<String, ArrayList<String>> situsNameNull = salesforceAPI.select(querySitusId);
		boolean situsNullValue = situsNameNull.get("Id") == null;

		softAssert.assertTrue(workItemNameText,
				"SMAB-T4064,SMAB-T4065,SMAB-T4066,SMAB-T4123,SMAB-T4124 : Work Item should be Null");
		softAssert.assertTrue(apnNameNullValue,
				"SMAB-T4064,SMAB-T4065,SMAB-T4066,SMAB-T4123,SMAB-T4124 : APN should be Null");
		softAssert.assertTrue(situsNullValue,
				"SMAB-T4064,SMAB-T4065,SMAB-T4066,SMAB-T4123,SMAB-T4124 : Situs should be Null");
		ReportLogger.INFO("Story Completed. Validation Done.");
	}

}