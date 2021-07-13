package com.apas.Tests.ParcelManagement;

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
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
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

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);

	}
	/**
	 * This method is to Verify that User is not able to perform any mapping action for a Parcel which has open CIO work items. 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3527:Verify the UI validations that There are open CIO work items against the parent parcel. The mapping action is not allowed to be performed until the CIO activities have been completed.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyMappingActionUIValidations(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Id IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Primary_Situs__c !=NULL AND Status__c='Active' limit 2";
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
       System.out.println("hash map values = "+hashMapSplitActionMappingData.values());
		//Step 5: Selecting Action & Taxes Paid fields value as 'yes' and validate error message
	    for( String action : hashMapSplitActionMappingData.values()) {
	    	System.out.println("action = "+action);
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
	    
	    
	}
	
	/**
	 * This method is to Verify that The Net Land Loss cannot be greater than the total size of the parent parcel(s). on mapping screen.
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3529:Verify the UI validations for all mapping action that 'The Net Land Loss cannot be greater than the total size of the parent parcel(s).'", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ValidationForNetLandLossValueOnAllMappingAction(String loginUser) throws Exception {
		String queryActiveAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and (Not Name like '100%') and (Not Name like '800%') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryActiveAPNValue);

		String apn=responseAPNDetails.get("Name").get(0);
		String apn1=responseAPNDetails.get("Name").get(1);
		String concatenateAPNWithSameOwnership = apn+","+apn1;
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "0");
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), "Lot_Size_SQFT__c", "0");
		
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
	    	System.out.println("action = "+action);
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
		String queryAPN = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and (Not Name like '100%') and (Not Name like '800%') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String queryAPNwithownership = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and (Not Name like '100%') and (Not Name like '800%') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
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
		    if(action.equals("Many To Many")){
		    softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"- In order to proceed with a parcel \"Many To Many\" action, the parent APN(s) must have the same ownership and ownership allocation.",
							"SMAB-T3528:Validating Error message on mapping custom screen.");}
		    else {
		    	softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
						"- In order to proceed with a parcel combine action, the parent APN(s) must have the same ownership and ownership allocation.",
								"SMAB-T3528:Validating Error message on mapping custom screen.");}
		    }
			
	    
	    
	}
	
}
